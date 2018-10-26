package com.kyoapps.maniac.room.dao

import androidx.paging.DataSource
import androidx.room.*
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ReplyEntReadTuple
import io.reactivex.Flowable


@Dao
interface ReplyDao {


    @Update
    fun update(replyEnt: ReplyEnt): Int

    @Update
    fun updateAll(replyEntList: List<ReplyEnt>): Int

    @Insert
    fun insert(replyEnt: ReplyEnt): Long

    @Insert
    fun insertAll(replyEntList: List<ReplyEnt>)

    @Query("SELECT $COLUMN_MSGID FROM $TABLE_NAME WHERE $COLUMN_THRDID = :thrdid AND $COLUMN_READ")
    fun getReadTuples(thrdid: Int): List<ReplyEntReadTuple>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_THRDID = :thrdid ORDER BY $COLUMN_FETCH_TIME")
    fun getRepliesOrderedRx(thrdid: Int): Flowable<List<ReplyEnt>>

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_THRDID = :thrdid ORDER BY $COLUMN_FETCH_TIME")
    fun getRepliesOrderedPaged(thrdid: Int):  DataSource.Factory<Int, ReplyEnt>


    @Query("DELETE FROM $TABLE_NAME")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_THRDID = :thrdid")
    fun delete(thrdid: Int)

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_BRDID = :brdid")
    fun delete(brdid: Short)


    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_THRDID = :thrdid")
    fun count(thrdid: Int): Int


    
    companion object {
        const val TABLE_NAME = "reply"
        const val COLUMN_BRDID = "brdid"
        const val COLUMN_THRDID = "thrdid"
        const val COLUMN_MSGID = "msgid"
        const val COLUMN_FETCH_TIME = "fetchTime"
        const val COLUMN_READ = "read"
    }


}