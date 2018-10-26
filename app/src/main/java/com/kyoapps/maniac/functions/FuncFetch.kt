package com.kyoapps.maniac.functions

import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.subOnNewObsOnMain
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

object FuncFetch {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    // optional delay loading for faster start and smoother animations
    fun fetchThreads(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem, delayLoading: Boolean) {
        mainVM.setIsLoadingThreads(true)
        compositeDisposable.add(mainDS.fetchThreadsIntoDb(loadRequestItem.brdid)
                .delay(if (delayLoading) 2 else 0, TimeUnit.SECONDS)
                .subOnNewObsOnMain()
                .subscribe({result ->
                    Log.i(TAG, "fetchThreads success $result")
                    mainVM.setIsLoadingThreads(false)
                },
                        {error -> error.printStackTrace()})
        )
    }

    fun fetchReplies(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem) {
        mainVM.setIsLoadingReplies(true)
        if (loadRequestItem.thrdid != null && loadRequestItem.thrdid != -1) {
            compositeDisposable.add(mainDS.fetchRepliesIntoDb(loadRequestItem.brdid, loadRequestItem.thrdid)
                    .subOnNewObsOnMain()
                    .subscribe({result ->
                        Log.i(TAG, "fetchReplies success $result")
                        mainVM.setIsLoadingReplies(false)
                    },
                            {error -> error.printStackTrace()})
            )
        }
    }

    fun clearDisposables() { compositeDisposable.clear() }

    private const val TAG = "FuncFetch"

}