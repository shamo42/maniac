package com.kyoapps.maniac.helpers.classes.commonrrxwrap

sealed class ResultObject<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : ResultObject<T>()
    data class Loading<out T>(val partialData: T? = null, val message: String? = null) : ResultObject<T>()
    data class Error<out T>(val throwable: Throwable, val message: String? = null) : ResultObject<T>()


    val extractData: T? get() = when (this) {
        is Success -> data
        is Loading -> partialData
        is Error -> null
    }
}