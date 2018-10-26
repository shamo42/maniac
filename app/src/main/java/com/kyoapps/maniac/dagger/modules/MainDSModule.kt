package com.kyoapps.maniac.dagger.modules

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import com.kyoapps.maniac.viewmodel.MainVMF

import dagger.Module
import dagger.Provides

@Module(includes = [(ContextModule::class),(DaoModule::class)])
class MainDSModule {

    @Provides
    @CommonActivityScope
    fun mainVM(context: Context, mainVMF: MainVMF): MainVM {
        return ViewModelProviders.of(context as androidx.fragment.app.FragmentActivity, mainVMF).get(MainVM::class.java)
    }

    @Provides
    @CommonActivityScope
    fun mainVMF(mainDS: MainDS): MainVMF {
        return MainVMF(mainDS)
    }

    @Provides
    @CommonActivityScope
    fun mainDS(maniacApiLEGACY: ManiacApiLEGACY, threadDao: ThreadDao, replyDao: ReplyDao): MainDS {
        return MainDS(maniacApiLEGACY, threadDao, replyDao)
    }


}
