package com.kyoapps.maniac.dagger.components

import android.content.SharedPreferences
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.modules.ApiManiacModule
import com.kyoapps.maniac.dagger.modules.DaoModule
import com.kyoapps.maniac.dagger.modules.DefaultSettingsModule
import com.kyoapps.maniac.dagger.modules.MainDSModule
import com.kyoapps.maniac.room.dao.ReplyDao
import com.kyoapps.maniac.room.dao.ThreadDao
import com.kyoapps.maniac.viewmodel.MainDS
import com.kyoapps.maniac.viewmodel.MainVM
import com.kyoapps.maniac.viewmodel.MainVMF
import dagger.Component
import okhttp3.OkHttpClient


@CommonActivityScope
@Component(modules = [ApiManiacModule::class, MainDSModule::class, DaoModule::class, DefaultSettingsModule::class])
interface ActivityComponent {

    val maniacApiLEGACY: ManiacApiLEGACY

    val okHttpClient: OkHttpClient

    val mainVM: MainVM

    val mainDS: MainDS

    val threadDao: ThreadDao
    val replyDao: ReplyDao

    val defaultSettings: SharedPreferences
}