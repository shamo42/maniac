package com.kyoapps.maniac.dagger.modules

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import com.kyoapps.maniac.viewmodel.viewModelFactory

import dagger.Module
import dagger.Provides

@Module(includes = [(DaoModule::class)])
class MainDSModule {



    @Provides
    @CommonActivityScope
    fun mainVM(context: Context, dataSource: MainDS): MainVM {
        return viewModelFactory { MainVM(dataSource) }.let { factory ->
            ViewModelProvider(context as FragmentActivity, factory).get(MainVM::class.java)
        }
    }


    @Provides
    @CommonActivityScope
    fun mainDS(maniacApiLEGACY: ManiacApiLEGACY, threadDao: ThreadDao, replyDao: ReplyDao): MainDS {
        return MainDS(maniacApiLEGACY, threadDao, replyDao)
    }


}
