package com.kyoapps.maniac.dagger.modules

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import com.kyoapps.maniac.viewmodel.viewModelFactory

import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module(includes = [(DaoModule::class)])
object ViewModels {

    @Provides
    @Reusable
    fun mainVM(context: Context, dataSource: MainDS): MainVM {
        return viewModelFactory { MainVM(dataSource) }.let { factory ->
            ViewModelProvider(context as FragmentActivity, factory).get(MainVM::class.java)
        }
    }



    @Provides
    @Reusable
    fun mainDS(maniacApiLEGACY: ManiacApiLEGACY, threadDao: ThreadDao, replyDao: ReplyDao): MainDS {
        return MainDS(maniacApiLEGACY, threadDao, replyDao)
    }


}
