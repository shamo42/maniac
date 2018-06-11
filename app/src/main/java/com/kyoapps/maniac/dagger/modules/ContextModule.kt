package com.kyoapps.maniac.dagger.modules
import android.content.Context

import dagger.Module
import dagger.Provides


@Module
class ContextModule(private val context: Context) {

    @Provides
    fun context(): Context {
        return context
    }
}
