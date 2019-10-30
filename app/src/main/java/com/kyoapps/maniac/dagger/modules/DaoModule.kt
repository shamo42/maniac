package com.kyoapps.maniac.dagger.modules

import android.content.Context
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.room.DatabaseDefault
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao

import dagger.Module
import dagger.Provides
import dagger.Reusable


@Module(includes = [DbModule::class])
object DaoModule {

    @Provides
    @Reusable
    fun threadDao(databaseDefault: DatabaseDefault): ThreadDao {
        return databaseDefault.threadDao()
    }

    @Provides
    @Reusable
    fun replyDao(databaseDefault: DatabaseDefault): ReplyDao {
        return databaseDefault.replyDao()
    }


}
