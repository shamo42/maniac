package com.kyoapps.maniac.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.room.entities.ReplyEnt
import com.kyoapps.maniac.room.entities.ThreadEnt


@Database(entities = [(ThreadEnt::class), (ReplyEnt::class)], version = 32, exportSchema = false)

abstract class DatabaseDefault : RoomDatabase() {
    abstract fun threadDao(): ThreadDao
    abstract fun replyDao(): ReplyDao

    companion object {
        private var sInstance: DatabaseDefault? = null

        @Synchronized fun getInstance(context: Context): DatabaseDefault? {
            if (sInstance == null) {
                sInstance = Room
                        .databaseBuilder(context.applicationContext, DatabaseDefault::class.java, "appdatabasekotlin")
                        //.addMigrations(MIGRATION_28_29)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return sInstance
        }



        // migration examples:
        /*val MIGRATION_28_29: Migration = object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE " + GameEnt.TABLE_NAME
                        + " ADD COLUMN " + GameEnt.COLUMN_ADDED_DATE_MS + " INTEGER NOT NULL DEFAULT 0");
            }
        }*/

        /*val MIGRATION_29_30: Migration = object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val TABLE_NAME_TEMP = "GameNew"

                // 1. Create new table
                database.execSQL("CREATE TABLE IF NOT EXISTS `$TABLE_NAME_TEMP` " +
                        "(`${GameEnt.COLUMN_ID}` TEXT NOT NULL, PRIMARY KEY(`${GameEnt.COLUMN_ID}`))")

                // 2. Copy the data
                database.execSQL("INSERT INTO $TABLE_NAME_TEMP (${GameEnt.COLUMN_ID}) "
                        + "SELECT ${GameEnt.COLUMN_ID} "
                        + "FROM ${GameEnt.TABLE_NAME}")

                // 3. Remove the old table
                database.execSQL("DROP TABLE ${GameEnt.TABLE_NAME}")

                // 4. Change the table name to the correct one
                database.execSQL("ALTER TABLE $TABLE_NAME_TEMP RENAME TO ${GameEnt.TABLE_NAME}")
            }
        }*/
    }



}