package com.kyoapps.maniac.functions

import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object FuncFetch {

    // optional delay loading for faster start and smoother animations
    fun fetchThreads(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem, delayLoading: Boolean): Single<Boolean> {
        mainVM.setIsLoadingThreads(true)
        return mainDS.fetchThreadsIntoDb(loadRequestItem.brdid)
                .delay(if (delayLoading) 2 else 0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { mainVM.setIsLoadingThreads(false) }

    }

    fun fetchReplies(mainVM: MainVM, mainDS: MainDS, loadRequestItem: LoadRequestItem): Single<Boolean> {
        mainVM.setIsLoadingReplies(true)
        return if (loadRequestItem.thrdid != null && loadRequestItem.thrdid != -1) {
            mainDS.fetchRepliesIntoDb(loadRequestItem.brdid, loadRequestItem.thrdid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { mainVM.setIsLoadingReplies(false) }
        } else Single.error(Throwable("thread id invalid: ${loadRequestItem.thrdid}"))
    }

    private const val TAG = "FuncFetch"

}