package com.kyoapps.maniac.functions

import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.viewmodel.MainDS
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

object FuncFetch {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun fetchThreads(mainDS: MainDS, loadRequestItem: LoadRequestItem) {
        compositeDisposable.add(mainDS.fetchThreadsIntoDb(loadRequestItem.brdid)
                .subscribeOn(Schedulers.newThread())
                .subscribe({result ->  Log.i(TAG, "fetchThreads success $result") },
                        {error -> error.printStackTrace()})
        )
    }

    fun fetchReplies(mainDS: MainDS, loadRequestItem: LoadRequestItem) {
        if (loadRequestItem.thrdid != -1) {
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