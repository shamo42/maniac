package com.kyoapps.maniac.dagger.modules

import android.content.Context
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.room.DatabaseDefault
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao

import dagger.Module
import dagger.Provides


@Module(includes = [(ContextModule::class)])
class DaoModule {

    @Provides
    @CommonActivityScope
    fun gameDao(context: Context): ThreadDao {
        return DatabaseDefault.getInstance(context)!!.threadDao()
    }

    @Provides
    @CommonActivityScope
    fun replyDao(context: Context): ReplyDao {
        return DatabaseDefault.getInstance(context)!!.replyDao()
    }


}
