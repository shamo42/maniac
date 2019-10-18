package com.kyoapps.maniac.dagger.modules

import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.modules.NetworkModule.RETROFIT_MANIAC
import dagger.Module
import dagger.Provides
import dagger.Reusable
import retrofit2.Retrofit
import javax.inject.Named

@Module(includes = [(NetworkModule::class)])
object ApiManiacModule {

    @Provides @JvmStatic
    @Reusable
    fun maniacOldApi(@Named(RETROFIT_MANIAC) retrofit: Retrofit): ManiacApiLEGACY {
        return retrofit.create(ManiacApiLEGACY::class.java)
    }



}
