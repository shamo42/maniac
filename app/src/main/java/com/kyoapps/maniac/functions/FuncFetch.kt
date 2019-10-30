package com.kyoapps.maniac.functions

import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import com.kyoapps.zkotlinextensions.extensions.disposeOn
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object FuncFetch {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    // optional delay loading for faster start and smoother animations
    fun fetchThreads(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem, delayLoading: Boolean) {
        mainVM.setIsLoadingThreads(true)
        mainDS.fetchThreadsIntoDb(loadRequestItem.brdid)
                .delay(if (delayLoading) 2 else 0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    result -> Log.i(TAG, "fetchThreads success $result")
                    mainVM.setIsLoadingThreads(false)
                },  {Log.e(TAG, it.localizedMessage, it)})
               .disposeOn(compositeDisposable)
    }

    fun fetchReplies(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem) {
        mainVM.setIsLoadingReplies(true)
        if (loadRequestItem.thrdid != null && loadRequestItem.thrdid != -1) {
            mainDS.fetchRepliesIntoDb(loadRequestItem.brdid, loadRequestItem.thrdid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        result -> Log.i(TAG, "fetchReplies success $result")
                        mainVM.setIsLoadingReplies(false)
                    },  {Log.e(TAG, it.localizedMessage, it)})
                    .disposeOn(compositeDisposable)

        }
    }

    fun clearDisposables() { compositeDisposable.clear() }

    private const val TAG = "FuncFetch"

}