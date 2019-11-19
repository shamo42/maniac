package com.kyoapps.maniac.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

//todo replace with new api
interface ManiacApiLEGACY {

    @GET("pxmboard.php")
    fun getBoards(
    ): Single<Response<ResponseBody>>

    @GET("pxmboard.php")
    fun getThreads(
            @Query("mode") threadlist: String,
            @Query("brdid") brdid: Int
    ): Single<Response<ResponseBody>>


    @GET("pxmboard.php")
    fun getReplies(
            @Query("mode") thread: String,
            @Query("brdid") brdid: Int,
            @Query("thrdid") thrdid: Int
    ): Single<Response<ResponseBody>>


    @GET("pxmboard.php")
    fun getMessage(
            @Query("mode") message: String,
            @Query("brdid") brdid: Int,
            @Query("msgid") msgid: Int
    ): Single<Response<ResponseBody>>


}
