package com.kyoapps.maniac.viewmodel

import android.arch.lifecycle.*
import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ThreadEnt
import android.arch.paging.PagedList
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import io.reactivex.schedulers.Schedulers


class MainVM(private val dataSource: MainDS) : ViewModel() {

    //private val disposables = CompositeDisposable()
    //Threads
    private val loadThreadsMLD = MutableLiveData<LoadRequestItem>()
    private var threadsLiveData: LiveData<List<ThreadEnt>>? = null
    //Replies
    private val loadRepliesMLD = MutableLiveData<LoadRequestItem>()
    private var repliesPagedLiveData: LiveData<PagedList<ReplyEnt>>? = null
    //Message
    private val loadMessageMLD = MutableLiveData<LoadRequestItem>()
    private var messageLiveData: LiveData<String>? = null


    //1. Threads
    fun setThreadsRequestItem(loadRequestItem: LoadRequestItem) {
        this.loadThreadsMLD.value = loadRequestItem
    }

    /*private fun requestLiveData(brdid: Short): LiveData<List<ThreadEnt>>? {
        Log.d(TAG, "threadsLiveData null ${threadsLiveData == null}")
        if (threadsLiveData == null) {
            threadsLiveData = MutableLiveData()
        }
        threadsRxDb(brdid)
        return threadsLiveData
    }*/

    fun threadsLiveDataRx(): LiveData<List<ThreadEnt>>? {
        Log.d(TAG, "threadsLiveData null ${threadsLiveData == null}")
        if (threadsLiveData == null) {
            threadsLiveData = Transformations.switchMap(loadThreadsMLD) {
                LiveDataReactiveStreams.fromPublisher(dataSource.getThreadsFromDb(it.brdid).subscribeOn(Schedulers.newThread()))
            }
        }
        return threadsLiveData
    }



    //2. Replies
    fun setRepliesRequestItem(loadRequestItem: LoadRequestItem) {
        this.loadRepliesMLD.value = loadRequestItem
    }

    fun repliesLiveDataPaged(): LiveData<PagedList<ReplyEnt>>? {
        Log.d(TAG, "repliesPagedLiveData null ${repliesPagedLiveData == null}")
        if (repliesPagedLiveData == null) {
            val pagedListConfig = PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(50)
                    .setPageSize(25)
                    .setPrefetchDistance(10)
                    .build()
            repliesPagedLiveData = Transformations.switchMap(loadRepliesMLD) {
                LivePagedListBuilder(dataSource.getRepliesPagedFromDb(it.thrdid), pagedListConfig)
                        .build()
            }
        }
        return repliesPagedLiveData
    }


    //3. Message
    fun setMessageRequestItem(loadRequestItem: LoadRequestItem) {
        if (loadRequestItem.msgid != null && loadRequestItem.msgid != loadMessageMLD.value?.msgid) this.loadMessageMLD.value = loadRequestItem
    }

    fun getMessageLiveDataRx(): LiveData<String>? {
        Log.d(TAG, "messageLiveData null ${messageLiveData == null}")
        if (messageLiveData == null) {
            messageLiveData = Transformations.switchMap(loadMessageMLD, {
                LiveDataReactiveStreams.fromPublisher(dataSource.getMessage(it.brdid, it.msgid!!)
                        .subscribeOn(Schedulers.newThread()))
            })
        }
        return messageLiveData
    }

    fun getLatestRequestItem(): LiveData<LoadRequestItem> {
        return loadMessageMLD
    }

    //override fun onCleared() { disposables.clear() }

    companion object {
        private const val TAG = "MainVM"
    }
}


