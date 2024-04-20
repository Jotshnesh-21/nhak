package com.sellinout.code

import retrofit2.Response

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
data class Resource<out T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null,
    val throwable: Throwable? = null,
    val retrofitResponse: Response<*>? = null
) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data)
        }

        fun <T> consumed(): Resource<T> {
            return Resource(Status.CONSUMMED, null)
        }

        fun <T> error(
            message: String?,
            data: T? = null,
            throwable: Throwable? = null,
            retrofitResponse: Response<*>? = null
        ): Resource<T> {
            return Resource(Status.ERROR, data, message, throwable, retrofitResponse)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data)
        }
    }
}


enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    CONSUMMED
}