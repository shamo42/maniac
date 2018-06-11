package com.kyoapps.maniac.dagger.modules

import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.api.ManiacApiLEGACY
import com.kyoapps.maniac.dagger.modules.NetworkModule.Companion.RETROFIT_MANIAC
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module(includes = [(NetworkModule::class)])
class ApiManiacModule {

    @Provides
    @CommonActivityScope
    fun maniacOldApi(@Named(RETROFIT_MANIAC) retrofit: Retrofit): ManiacApiLEGACY {
        return retrofit.create(ManiacApiLEGACY::class.java)
    }



}
