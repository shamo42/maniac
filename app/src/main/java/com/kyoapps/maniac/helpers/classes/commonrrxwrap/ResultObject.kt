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

    inline fun <Y> mapResult(crossinline transform: (T) -> Y): ResultObject<Y> = try {
        when (this) {
            is Success<T> -> Success(transform(data))
            is Loading<T> -> Loading(partialData?.let { transform(it) })
            is Error<T> -> Error(throwable)
        }
    } catch (e: Throwable) {
        Error(e)
    }

    fun asIs(): ResultObject<T> = this




    fun onSuccess(onSuccess: (data: T) -> Unit): ResultObject<T> {
        if (this is Success)
            onSuccess(data)

        return this
    }

    fun onLoading(onLoading: (partialData: T?) -> Unit): ResultObject<T> {
        if (this is Loading)
            onLoading(partialData)

        return this
    }

    fun onError(onError: (throwable: Throwable?) -> Unit): ResultObject<T> {
        if (this is Error)
            onError(throwable)
        return this
    }

}


inline fun <T> ResultObject<List<T>>.filterResultObject(predicate: (T) -> Boolean): ResultObject<List<T>> = try {
    when (this) {
        is ResultObject.Success<List<T>> -> ResultObject.Success<List<T>>(data.filter(predicate))
        is ResultObject.Loading<List<T>> -> ResultObject.Loading<List<T>>(partialData?.filter(predicate))
        is ResultObject.Error<List<T>> -> ResultObject.Error<List<T>>(throwable)
    }
} catch (e: Throwable) {
    ResultObject.Error<List<T>>(e)
}


inline fun <T, U, V> ResultObject<V>.map(transform: (T) -> U): ResultObject<List<U>> where V : Iterable<T> = try {
    when (this) {
        is ResultObject.Success<V> -> ResultObject.Success<List<U>>(data.map(transform))
        is ResultObject.Loading<V> -> ResultObject.Loading<List<U>>(partialData?.map(transform))
        is ResultObject.Error<V> -> ResultObject.Error<List<U>>(throwable)
    }
} catch (e: Throwable) {
    ResultObject.Error<List<U>>(e)
}


