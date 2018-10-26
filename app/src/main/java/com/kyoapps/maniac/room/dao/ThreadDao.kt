package com.kyoapps.maniac.room.dao

import androidx.room.*
import com.kyoapps.maniac.room.entities.ThreadEnt
import io.reactivex.Flowable


@Dao
interface ThreadDao {


    @Update
    fun update(threadEnt: ThreadEnt): Int


    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_THRDID = :id LIMIT 1")
    fun getThreadRx(id: String): Flowable<ThreadEnt>
    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_THRDID = :id LIMIT 1")
    fun getThread(id: String): List<ThreadEnt>


    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_BRDID = :brdid ORDER BY $COLUMN_FETCH_TIME")
    fun getThreadsOrderedRx(brdid: Short): Flowable<List<ThreadEnt>>

    @Insert
    fun insert(gameEnt: ThreadEnt): Long

    @Insert
    fun insertAll(gameEntList: List<ThreadEnt>)

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): List<ThreadEnt>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllRx(): Flowable<List<ThreadEnt>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThread(gameEnt: ThreadEnt)

    @Query("DELETE FROM $TABLE_NAME")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_BRDID = :brdid")
    fun delete(brdid: Short)


    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_BRDID = :brdid")
    fun count(brdid: Short): Int


    
    companion object {
        const val TABLE_NAME = "thread"
        const val COLUMN_BRDID = "brdid"
        const val COLUMN_THRDID = "thrdid"
        const val COLUMN_FETCH_TIME = "fetchTime"
    }


}