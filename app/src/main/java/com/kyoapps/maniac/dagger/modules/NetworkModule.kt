package com.kyoapps.maniac.dagger.modules


import com.kyoapps.maniac.dagger.scopes.CommonActivityScope
import com.kyoapps.maniac.helpers.C_COMMON

import java.util.concurrent.TimeUnit

import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import javax.inject.Named

@Module
object NetworkModule {


    @Provides
    @Reusable
    @Named(RETROFIT_MANIAC)
    fun retrofitManiac(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(C_COMMON.MANIAC_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //.addConverterFactory(MoshiConverterFactory.create())
                .build()
    }


    @Provides
    @Reusable
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .addInterceptor { chain ->  val request = chain.request().newBuilder()
                        .addHeader("user-agent", C_COMMON.USER_AGENT)
                        .build()
                    chain.proceed(request)}
                //.cache(mCache)
                .build()
    }


    /*@Provides
    @CommonActivityScope
    fun cache(mDirectory: File): Cache {
        return okhttp3.Cache(mDirectory, (CACHE_SIZE_MB * 1024 * 1024).toLong())
    }

    @Provides
    @CommonActivityScope
    fun file(context: Context): File {
        return File(context.cacheDir, "okhttp_cache")
    }*/

    private val CACHE_SIZE_MB = 80
    const val RETROFIT_MANIAC = "retrofitManiac"



}
