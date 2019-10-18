package com.kyoapps.maniac.dagger.modules

import android.content.Context
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.room.DatabaseDefault
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao

import dagger.Module
import dagger.Provides
import dagger.Reusable


@Module
object DaoModule {

    @Provides @JvmStatic
    @Reusable
    fun threadDao(context: Context): ThreadDao {
        return DatabaseDefault.getInstance(context)!!.threadDao()
    }

    @Provides @JvmStatic
    @Reusable
    fun replyDao(context: Context): ReplyDao {
        return DatabaseDefault.getInstance(context)!!.replyDao()
    }


}
