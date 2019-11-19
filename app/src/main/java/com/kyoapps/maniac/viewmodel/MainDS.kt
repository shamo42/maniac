package com.kyoapps.maniac.viewmodel

import androidx.paging.DataSource
import android.util.Log
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.functions.FuncParse
import com.kyoapps.maniac.helpers.classes.LoadRequestItem
import com.kyoapps.maniac.helpers.classes.pojo.Board
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ThreadEnt
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class MainDS(private val maniacApiLEGACY: ManiacApiLEGACY, private val threadDao: ThreadDao, private val replyDao: ReplyDao) {


    // todo replace legacy code
    fun getBoards(): Maybe<List<Board>> {
        Log.d(TAG, "getBoards")
        return maniacApiLEGACY.getBoards()
                .map { FuncParse.parseBoardsLegacy(it) }
                .filter { it.isNotEmpty() }

    }


    // todo replace legacy code
    private fun getThreads(brdid: Int): Single<List<ThreadEnt>> {
        Log.d(TAG, "getThreads")
        return maniacApiLEGACY.getThreads("threadlist", brdid)
                .map { FuncParse.parseThreadsLegacy(it, brdid) }
    }

    fun fetchThreadsIntoDb(brdid: Int): Single<Boolean> {
        return getThreads(brdid)
                .map {list ->
                    val oldThrdMap: HashMap<Int, ThreadEnt> = HashMap(list.size)
                    threadDao.getAll().forEach { oldThrdMap[it.thrdid] = it }
                    list.forEach {
                        if (oldThrdMap.containsKey(it.thrdid)) {
                            it.hide = oldThrdMap[it.thrdid]!!.hide
                            it.oldReplies = oldThrdMap[it.thrdid]!!.totalReplies
                        }
                    }
                    threadDao.delete(brdid)
                    threadDao.insertAll(list)
                }
                .map { threadDao.count(brdid) > 0 }
    }

    fun getThreadsFromDb(brdid: Int): Flowable<List<ThreadEnt>> {
        return threadDao.getThreadsOrderedRx(brdid)
    }

    // todo replace legacy code
    private fun getReplies(brdid: Int, thrdid: Int): Single<List<ReplyEnt>> {
        return maniacApiLEGACY.getReplies("thread", brdid, thrdid)
                .map { FuncParse.parseRepliesLegacy(it, brdid, thrdid) }
    }

    fun fetchRepliesIntoDb(brdid: Int, thrdid: Int): Single<Boolean> {
        return getReplies(brdid, thrdid)
                .map {list ->
                    val oldReplyList= replyDao.getReadTuples(thrdid).map { it.msgid }
                    list.forEach {
                        //Log.d(TAG, "contains msgid: ${oldReplyList.contains(it.msgid)}")
                        if (oldReplyList.contains(it.msgid)) it.read = true
                    }
                    //list.forEach { Log.d(TAG, "read?: ${it.subject} ${it.read}")}
                    replyDao.delete(thrdid)
                    replyDao.insertAll(list)
                }
                .map {
                    val count = replyDao.count(thrdid)
                    Log.d(TAG, "fetchRepliesIntoDb($brdid, $thrdid) count: $count")
                    count > 0
                }
    }

    fun markReplyReadDb(loadRequestItem: LoadRequestItem) {
        if (loadRequestItem.thrdid != null && loadRequestItem.msgid != null) {
            replyDao.getReply(loadRequestItem.thrdid, loadRequestItem.msgid).copy(read = true).also { readReply ->
                replyDao.insert(readReply)
            }
        }
    }

    fun markReplyReadDb(replyEnt: ReplyEnt): Single<Int> {
        return Single.just(replyEnt)
                .map { it.read = true;it}
                .map { replyDao.update(it) }
    }
    fun markRepliesReadDb(replyEntList: List<ReplyEnt>): Single<Int> {
        return Single.just(replyEntList)
                .map { replyDao.updateAll(it) }
    }

    /*fun getRepliesFromDb(thrdid: Int): Flowable<List<ReplyEnt>> {
        return  replyDao.getRepliesOrderedRx(thrdid)
    }*/

    fun getRepliesPagedFromDb(thrdid: Int):  DataSource.Factory<Int, ReplyEnt> {
        return replyDao.getRepliesOrderedPaged(thrdid)
    }

    // todo replace legacy code
    fun getMessage(brdid: Int, msgid: Int): Single<String> {
        Log.i(TAG, "getMessage($brdid, $msgid)")
        return maniacApiLEGACY.getMessage("message", brdid, msgid)
                .map { FuncParse.parseMessageLegacy(it, true, true) }
                //.map { FuncParse.formatHtmlLegacy(it,) }
    }


    private fun getThrdid(brdid: Int, msgid: Int): Single<LoadRequestItem> {
        return maniacApiLEGACY.getMessage("message", brdid, msgid)
                .map { FuncParse.parseMessageForThrdid(it) }
                .flatMap {
                    if (it == -1) Single.error(Throwable("parse error: getThrdid($brdid, $msgid)"))
                    else Single.just(LoadRequestItem(brdid, it, msgid))
                }
    }

    fun parseManiacUrl(url: String): Single<LoadRequestItem> {

        // ?mode=message&brdid=1&msgid=4314202
        // ?mode=messagelist&brdid=1&thrdid=163008
        // ?mode=board&brdid=1&thrdid=148641&msgid=3433133

        val brdidString = "brdid="
        val msgidString = "msgid="
        val thrdidString = "thrdid="

        Log.d(TAG, "PARSE_ start $url")
        return Single.just(url)
                .flatMap {
                    if (it.contains("?mode=message") && it.contains(brdidString) && it.contains(msgidString)
                    || (it.contains("?mode=messagelist") && it.contains(brdidString) && it.contains(thrdidString))
                    || (it.contains("?mode=board") && it.contains(brdidString) && it.contains(thrdidString) && it.contains(msgidString))
                    ) { Single.just(it) } else {Single.error(Throwable("parse error: parseManiacUrl($url) ")) }
                }
                .flatMap {
                    val brdidIndex = it.indexOf(brdidString)

                    if (it.contains("?mode=messagelist")) {
                        val brdid = it.substring(brdidIndex + brdidString.length, it.indexOf("&", brdidIndex)).toIntOrNull()
                        val thrdidIndex = it.indexOf(thrdidString)
                        val thrdid = it.substring(thrdidIndex + thrdidString.length).toIntOrNull()
                        if (brdid == null || thrdid == null) Single.error(Throwable("parse error: parseManiacUrl($url) "))
                        else Single.just(LoadRequestItem(brdid, thrdid, null))
                    } else if (it.contains("?mode=message")) {
                        val brdid = it.substring(brdidIndex + brdidString.length, it.indexOf("&", brdidIndex)).toIntOrNull()
                        val msgidIndex = it.indexOf(msgidString)
                        val msgid = it.substring(msgidIndex + msgidString.length).toIntOrNull()
                        if (brdid == null || msgid == null) Single.error(Throwable("parse error: parseManiacUrl($url) "))
                        else getThrdid(brdid, msgid)
                    } else {
                        val brdid = it.substring(brdidIndex +  brdidString.length, it.indexOf("&$thrdidString")).toIntOrNull()
                        val thrdidIndex = it.indexOf(thrdidString)
                        val thrdid = it.substring(thrdidIndex + thrdidString.length, it.indexOf("&$msgidString")).toIntOrNull()
                        val msgidIndex = it.indexOf(msgidString)
                        val msgid = it.substring(msgidIndex + msgidString.length).toIntOrNull()
                        if (brdid == null || thrdid == null || msgid == null) Single.error(Throwable("parse error: parseManiacUrl($url) "))
                        else Single.just(LoadRequestItem(brdid, thrdid, msgid))
                    }
                }
    }



    companion object {
        const val TAG = "MainDS"
    }


}