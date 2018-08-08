package com.kyoapps.maniac.viewmodel

import android.arch.lifecycle.*
import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ThreadEnt
import android.arch.paging.PagedList
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import com.kyoapps.maniac.helpers.classes.CommonRxDataWrap.CommonResource
import com.kyoapps.maniac.helpers.classes.CommonRxDataWrap.Status
import com.kyoapps.maniac.helpers.classes.pojo.Board
import io.reactivex.schedulers.Schedulers


class MainVM(private val dataSource: MainDS) : ViewModel() {

    //private val disposables = CompositeDisposable()
    //Threads
    private val loadThreadsMLD = MutableLiveData<LoadRequestItem>()
    private var threadsLiveData: LiveData<CommonResource<List<ThreadEnt>>>? = null
    //Replies
    private val loadRepliesMLD = MutableLiveData<LoadRequestItem>()
    private var repliesPagedLiveData: LiveData<PagedList<ReplyEnt>>? = null
    //Message
    private val messageLoadMLD = MutableLiveData<LoadRequestItem>()
    private var messageLiveData: LiveData<CommonResource<String>>? = null
    //Boards
    private var boardsLiveData: LiveData<CommonResource<List<Board>>>? = null

    //1. Threads
    fun setThreadsRequestItem(loadRequestItem: LoadRequestItem) {
        Log.d(TAG, "loadRequestItem: ${loadRequestItem.toString()}")
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

    fun threadsLiveDataRx(): LiveData<CommonResource<List<ThreadEnt>>>? {
        Log.d(TAG, "threadsLiveData null ${threadsLiveData == null}")
        if (threadsLiveData == null) {
            threadsLiveData = Transformations.switchMap(loadThreadsMLD) {loadRequestItem ->
                LiveDataReactiveStreams.fromPublisher(dataSource.getThreadsFromDb(loadRequestItem.brdid)
                        .map { CommonResource(Status.SUCCESS, it, null) }
                        .onErrorReturn { CommonResource(Status.ERROR, null, it.message) }
                        .subscribeOn(Schedulers.newThread()))
            }
        }
        return threadsLiveData
    }



    //2. Replies
    fun setRepliesRequestItem(loadRequestItem: LoadRequestItem) {
        Log.d(TAG, "loadRequestItem ${loadRequestItem.thrdid == null} ${loadRequestItem.thrdid}")
        this.loadRepliesMLD.value = loadRequestItem
    }

    fun repliesLiveDataPaged(): LiveData<PagedList<ReplyEnt>>? {
        Log.d(TAG, "repliesPagedLiveData null ${repliesPagedLiveData == null}")
        Log.d(TAG, "thrdid null ${loadRepliesMLD.value?.thrdid == null}")
        if (repliesPagedLiveData == null) { //&& loadRepliesMLD.value?.thrdid != null) {
            val pagedListConfig = PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(50)
                    .setPageSize(25)
                    .setPrefetchDistance(10)
                    .build()
            repliesPagedLiveData = Transformations.switchMap(loadRepliesMLD) {
                //setMessageRequestItem(it)
                LivePagedListBuilder(dataSource.getRepliesPagedFromDb(it.thrdid!!), pagedListConfig)
                        .build()
            }
        }
        return repliesPagedLiveData
    }

    //3. Message
    fun setMessageRequestItem(messageLoadItem: LoadRequestItem) {
        if (messageLoadItem.msgid != null && messageLoadItem.msgid != messageLoadMLD.value?.msgid) this.messageLoadMLD.value = messageLoadItem
    }

    fun getMessageLiveDataRx(): LiveData<CommonResource<String>>? {
        Log.d(TAG, "messageLiveData null ${messageLiveData == null}")
        if (messageLiveData == null) {
            messageLiveData = Transformations.switchMap(messageLoadMLD) {
                LiveDataReactiveStreams.fromPublisher(dataSource.getMessage(it.brdid, it.msgid!!)
                        .map { CommonResource(Status.SUCCESS, it, null) }
                        .onErrorReturn { CommonResource(Status.ERROR, null, it.message) }
                        .toFlowable()
                        .subscribeOn(Schedulers.newThread()))
            }
        }
        return messageLiveData
    }


    //4. Boards
    fun getBoardsLiveDataRx(): LiveData<CommonResource<List<Board>>>? {
        Log.d(TAG, "boardsLiveData null ${boardsLiveData == null}")
        if (boardsLiveData == null) {
            boardsLiveData =
                LiveDataReactiveStreams.fromPublisher(dataSource.getBoards()
                        .map { CommonResource(Status.SUCCESS, it, null) }
                        .onErrorReturn { CommonResource(Status.ERROR, null, it.message) }
                        .toFlowable()
                        .subscribeOn(Schedulers.newThread()))
        }
        return boardsLiveData
    }

    fun getLatestRequestItem(): LiveData<LoadRequestItem> {
        return messageLoadMLD
    }

    //override fun onCleared() { disposables.clear() }

    companion object {
        private const val TAG = "MainVM"
    }
}


