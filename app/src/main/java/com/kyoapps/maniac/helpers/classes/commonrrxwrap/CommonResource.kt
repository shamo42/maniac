<<<<<<< HEAD:app/src/main/java/com/kyoapps/maniac/helpers/classes/commonrrxwrap/CommonResource.kt
package com.kyoapps.maniac.helpers.classes.commonrrxwrap


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

=======
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

>>>>>>> 8a1e94693e63922ed0fd786654e66d081f7a6a63:app/src/main/java/com/kyoapps/maniac/helpers/classes/CommonRxDataWrap/CommonResource.kt
