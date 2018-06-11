package com.kyoapps.maniac.dagger.modules

import android.content.Context
import android.content.SharedPreferences
import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.helpers.C_SETTINGS


import dagger.Module
import dagger.Provides

@Module(includes = [(ContextModule::class)])
class DefaultSettingsModule {

    @CommonActivityScope
    @Provides
    fun settings(context: Context): SharedPreferences {
        return context.getSharedPreferences(C_SETTINGS.DEFAULT, Context.MODE_PRIVATE)
    }

}
