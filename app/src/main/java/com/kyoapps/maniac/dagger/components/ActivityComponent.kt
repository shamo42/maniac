package com.kyoapps.maniac.dagger.components

import android.content.Context
import android.content.SharedPreferences
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.modules.ApiManiacModule
import com.kyoapps.maniac.dagger.modules.DaoModule
import com.kyoapps.maniac.dagger.modules.DefaultSettingsModule
import com.kyoapps.maniac.dagger.modules.ViewModels
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient


@CommonActivityScope
@Component(modules = [ApiManiacModule::class, ViewModels::class, DaoModule::class, DefaultSettingsModule::class])
interface ActivityComponent {

    val maniacApiLEGACY: ManiacApiLEGACY

    val okHttpClient: OkHttpClient

    val mainVM: MainVM

    val mainDS: MainDS

    val threadDao: ThreadDao
    val replyDao: ReplyDao

    val defaultSettings: SharedPreferences

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(applicationContext: Context): Builder
        fun build(): ActivityComponent
    }


}