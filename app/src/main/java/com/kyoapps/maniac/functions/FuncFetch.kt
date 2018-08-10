package com.kyoapps.maniac.functions

import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.viewmodel.MainDS
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object FuncFetch {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    // optional delay loading for faster start and smoother animations
    fun fetchThreads(mainDS: MainDS, loadRequestItem: LoadRequestItem, delayLoading: Boolean) {
        compositeDisposable.add(mainDS.fetchThreadsIntoDb(loadRequestItem.brdid)
                .delay(if (delayLoading) 2 else 0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe({result ->  Log.i(TAG, "fetchThreads success $result") },
                        {error -> error.printStackTrace()})
        )
    }

    fun fetchReplies(mainDS: MainDS, loadRequestItem: LoadRequestItem) {
        if (loadRequestItem.thrdid != null && loadRequestItem.thrdid != -1) {
            compositeDisposable.add(mainDS.fetchRepliesIntoDb(loadRequestItem.brdid, loadRequestItem.thrdid)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe({result ->  Log.i(TAG, "fetchReplies success $result") },
                            {error -> error.printStackTrace()})
            )
        }
    }

    fun clearDisposables() { compositeDisposable.clear() }

    private const val TAG = "FuncFetch"

}