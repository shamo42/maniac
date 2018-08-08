package com.kyoapps.maniac.helpers.classes

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "KotlinExtensions"

fun <T> Flowable<T>.subOnNewObsOnMain(): Flowable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
}

fun <T> Single<T>.subOnNewObsOnMain(): Single<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
}

fun <T> Maybe<T>.subOnNewObsOnMain(): Maybe<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
}

fun <T> Single<T>.retryIfTimeOut(retries: Int, waitMs: Long): Single<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= retries && t is SocketTimeoutException
        if (shouldRetry) {
            Log.e(TAG, "Retry nr $i", t)
            Thread.sleep(waitMs * i)
        }
        shouldRetry
    }
}

fun <T> Flowable<T>.retryIfTimeOut(retries: Int, waitMs: Long): Flowable<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= retries && t is SocketTimeoutException
        if (shouldRetry) {
            Log.e(TAG, "Retry nr $i", t)
            Thread.sleep(waitMs * i)
        }
        shouldRetry
    }
}

fun <T> Single<T>.retryIfErrorCode(retries: Int, code: Int): Single<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= retries && (t.localizedMessage != null && t.localizedMessage!!.contains(code.toString()))
        if (shouldRetry) {
            Log.e(TAG, "Retry error code nr $i ${t.localizedMessage}")
            Thread.sleep(2000L * i)
        }
        shouldRetry
    }
}


fun Single<Response<ResponseBody>>.throwIfWrongResponseCode(code: Int): Maybe<Response<ResponseBody>> {
    return this.filter {
        val success = code == it.code()
        if (!success) throw Throwable("responseCode ${it.code()} ${it.errorBody()?.string()}")
        success }
}
fun Maybe<Response<ResponseBody>>.throwIfWrongResponseCode(code: Int): Maybe<Response<ResponseBody>> {
    return this.filter {
        val success = code == it.code()
        if (!success) throw Throwable("responseCode ${it.code()} ${it.errorBody()?.string()}")
        success }
}


fun <T> Single<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Single<T> {
    return this.retry { i, t ->
        Log.d(TAG, "waitAndRetryIfOverRateLimit t.localizedMessage ${t.localizedMessage}")
        val shouldRetry = i < 5 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.e(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}")
            Thread.sleep(timeWaitMs * i)
        }
        shouldRetry
    }
}
fun <T> Flowable<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Flowable<T> {
    return this.retry { i, t ->
        Log.d(TAG, "waitAndRetryIfOverRateLimit t.localizedMessage ${t.localizedMessage}")
        val shouldRetry = i < 5 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.e(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}")
            Thread.sleep(timeWaitMs * i)
        }
        shouldRetry
    }
}
fun <T> Maybe<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Maybe<T> {
    return this.retry { i, t ->
        Log.d(TAG, "waitAndRetryIfOverRateLimit t.localizedMessage ${t.localizedMessage}")
        val shouldRetry = i < 5 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.e(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}")
            Thread.sleep(timeWaitMs * i)
        }
        shouldRetry
    }
}


fun <T> Flowable<T>.retryWhenConnectionLost(): Flowable<T> {
    return this.retry { i, t ->
        val shouldRetry = t is UnknownHostException
        if (shouldRetry) {
            Log.e(TAG, "retry $i because of UnknownHostException: ${t.localizedMessage}", t)
            Thread.sleep(10000)
        }
        shouldRetry
    }
}



fun <T> Flowable<T>.delayEach(mS: Long): Flowable<T> {
    return this.zipWith(Flowable.interval(mS, java.util.concurrent.TimeUnit.MILLISECONDS), io.reactivex.functions.BiFunction { t1: T, _: Long -> t1  })
}


fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

inline fun View.doOnPreDraw(crossinline action: (view: View) -> Unit) {
    val vto = viewTreeObserver
    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action(this@doOnPreDraw)
            when {
                vto.isAlive -> vto.removeOnPreDrawListener(this)
                else -> viewTreeObserver.removeOnPreDrawListener(this)
            }
            return true
        }
    })
}


/*fun <T> Flowable<T>.responseToResult(): Flowable<Result<T>> {
    return this.map { it.asResult() }
            .onErrorReturn {
                if (it is HttpException || it is IOException) {
                    return@onErrorReturn it.asErrorResult<T>()
                } else {
                    throw it
                }
            }
}

fun <T> Single<T>.responseToResult(): Single<Result<T>> {
    return this.map { it.asResult() }
            .onErrorReturn {
                if (it is HttpException || it is IOException) {
                    return@onErrorReturn it.asErrorResult<T>()
                } else {
                    throw it
                }
            }
}

fun <T> T.asResult(): Result<T> {
    return Result.Success<T>(this)
}

fun <T> Throwable.asErrorResult(): Result<T> {
    return Result.Error<T>(this)
}*/

