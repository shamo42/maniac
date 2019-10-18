package com.kyoapps.maniac.viewmodel

import androidx.lifecycle.*
import android.util.Log
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ThreadEnt
import androidx.paging.PagedList
import androidx.paging.LivePagedListBuilder
import com.kyoapps.maniac.helpers.classes.commonrrxwrap.ResultObject
import com.kyoapps.maniac.helpers.classes.commonrrxwrap.Status
import com.kyoapps.maniac.helpers.classes.pojo.Board
import io.reactivex.schedulers.Schedulers


class MainVM(private val dataSource: MainDS) : ViewModel() {

    //private val disposables = CompositeDisposable()
    // Threads database
    private val threadsDatabaseMLD = MutableLiveData<LoadRequestItem>()
    private var threadsDatabaseLiveData: LiveData<ResultObject<List<ThreadEnt>>>? = null
    // Replies database
    private val repliesDatabaseMLD = MutableLiveData<LoadRequestItem>()
    private var repliesDatabasePagedLiveData: LiveData<PagedList<ReplyEnt>>? = null
    // Message fetch online
    private val messageFetchMLD = MutableLiveData<LoadRequestItem>()
    private var messageFetchLiveData: LiveData<ResultObject<String>>? = null
    // Boards fetch online
    private var boardsFetchLiveData: LiveData<ResultObject<List<Board>>>? = null

    // Data sharing
    private val loadingMsgMLD = MutableLiveData<Boolean>()
    private val loadingThreadsMLD = MutableLiveData<Boolean>()
    private val loadingRepliesMLD = MutableLiveData<Boolean>()


    // 1. Threads
    fun setThreadsRequestItem(loadRequestItem: LoadRequestItem) {
        Log.d(TAG, "loadRequestItem: ${loadRequestItem.toString()}")
        this.threadsDatabaseMLD.value = loadRequestItem
    }

    /*private fun requestLiveData(brdid: Short): LiveData<List<ThreadEnt>>? {
        Log.d(TAG, "threadsDatabaseLiveData null ${threadsDatabaseLiveData == null}")
        if (threadsDatabaseLiveData == null) {
            threadsDatabaseLiveData = MutableLiveData()
        }
        threadsRxDb(brdid)
        return threadsDatabaseLiveData
    }*/

    fun threadsLiveDataRx(): LiveData<ResultObject<List<ThreadEnt>>>? {
        Log.d(TAG, "threadsDatabaseLiveData null ${threadsDatabaseLiveData == null}")
        if (threadsDatabaseLiveData == null) {
            threadsDatabaseLiveData = Transformations.switchMap(threadsDatabaseMLD) { loadRequestItem ->
                LiveDataReactiveStreams.fromPublisher(dataSource.getThreadsFromDb(loadRequestItem.brdid)
                        .map { ResultObject.Success(it) as ResultObject<List<ThreadEnt>> }
                        .onErrorReturn { ResultObject.Error(it) }
                        .subscribeOn(Schedulers.io()))
            }
        }
        return threadsDatabaseLiveData
    }



    // 2. Replies
    fun setRepliesRequestItem(loadRequestItem: LoadRequestItem) {
        Log.d(TAG, "loadRequestItem ${loadRequestItem.thrdid == null} ${loadRequestItem.thrdid}")
        this.repliesDatabaseMLD.value = loadRequestItem
    }

    fun repliesLiveDataPaged(): LiveData<PagedList<ReplyEnt>>? {
        Log.d(TAG, "repliesDatabasePagedLiveData null ${repliesDatabasePagedLiveData == null}")
        Log.d(TAG, "thrdid null ${repliesDatabaseMLD.value?.thrdid == null}")
        if (repliesDatabasePagedLiveData == null) { //&& repliesDatabaseMLD.value?.thrdid != null) {
            val pagedListConfig = PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(50)
                    .setPageSize(25)
                    .setPrefetchDistance(10)
                    .build()
            repliesDatabasePagedLiveData = Transformations.switchMap(repliesDatabaseMLD) {
                //setMessageRequestItem(it)
                LivePagedListBuilder(dataSource.getRepliesPagedFromDb(it.thrdid!!), pagedListConfig)
                        .build()
            }
        }
        return repliesDatabasePagedLiveData
    }

    // 3. Message
    fun setMessageRequestItem(messageLoadItem: LoadRequestItem) {

        if (messageLoadItem.msgid != null && messageLoadItem.msgid != messageFetchMLD.value?.msgid) {
            this.setIsLoadingMsg(true)
            this.messageFetchMLD.value = messageLoadItem
        }
    }

    fun getMessageLiveDataRx(): LiveData<ResultObject<String>>? {
        Log.d(TAG, "messageFetchLiveData null ${messageFetchLiveData == null}")
        if (messageFetchLiveData == null) {
            messageFetchLiveData = Transformations.switchMap(messageFetchMLD) { loadItem ->
                LiveDataReactiveStreams.fromPublisher(dataSource.getMessage(loadItem.brdid, loadItem.msgid!!)
                        .map { ResultObject.Success(it) as ResultObject<String> }
                        .onErrorReturn { ResultObject.Error(it) }
                        .toFlowable()
                        .subscribeOn(Schedulers.io()))
            }
        }
        return messageFetchLiveData
    }


    // 4. Boards
    fun getBoardsLiveDataRx(): LiveData<ResultObject<List<Board>>>? {
        Log.d(TAG, "boardsFetchLiveData null ${boardsFetchLiveData == null}")
        if (boardsFetchLiveData == null) {
            boardsFetchLiveData =
                LiveDataReactiveStreams.fromPublisher(dataSource.getBoards()
                        .map { ResultObject.Success(it) as ResultObject<List<Board>> }
                        .onErrorReturn { ResultObject.Error(it) }
                        .toFlowable()
                        .subscribeOn(Schedulers.io()))
        }
        return boardsFetchLiveData
    }

    fun getLatestRequestItem(): LiveData<LoadRequestItem> {
        return messageFetchMLD
    }



    // 5. Data sharing
    fun setIsLoadingThreads(loading: Boolean) { this.loadingThreadsMLD.value = loading }
    fun setIsLoadingReplies(loading: Boolean) { this.loadingRepliesMLD.value = loading }
    fun setIsLoadingMsg(loading: Boolean) { this.loadingMsgMLD.value = loading }

    fun getIsLoadingLiveData(): LiveData<Boolean> {
        val result = MediatorLiveData<Boolean>()

        result.addSource(loadingThreadsMLD) { result.value = loadingThreadsMLD.value?:true || loadingMsgMLD.value?:true }
        result.addSource(loadingRepliesMLD) { _ -> result.value = loadingThreadsMLD.value?:true || loadingMsgMLD.value?:true }
        result.addSource(loadingMsgMLD) { _ -> result.value = loadingThreadsMLD.value?:true || loadingMsgMLD.value?:true }
        return result
    }


    companion object {
        private const val TAG = "MainVM"
    }
}


