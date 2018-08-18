package com.kyoapps.maniac.helpers.classes.CommonRxDataWrap


data class CommonResource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {

        fun <T> success(data: T?): CommonResource<T> {
            return CommonResource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): CommonResource<T> {
            return CommonResource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): CommonResource<T> {
            return CommonResource(Status.LOADING, data, null)
        }
    }


}

