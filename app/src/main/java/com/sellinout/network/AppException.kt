package com.sellzinout.network

import android.app.Application
import androidx.annotation.StringRes
import androidx.collection.ArrayMap
import com.sellinout.code.Resource
import com.sellinout.R
import com.sellinout.SellApp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.internal.http2.ConnectionShutdownException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLHandshakeException

/**
 * Google common error codes: https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
 * Google sign in error codes: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInStatusCodes.html
 */
//http errors
const val HTTP_400_BAD_REQUEST = 400
const val HTTP_401_UNAUTHORIZED = 401
const val HTTP_403_FORBIDDEN = 403
const val HTTP_404_NOT_FOUND = 404
const val HTTP_413_PAYLOAD_TOO_LARGE = 413
const val HTTP_422_UNPROCESSIBLE_ENTITY = 422
const val HTTP_409_CONFLICT_RESPONSE = 409
const val HTTP_500_INTERNAL_ERROR = 500
const val HTTP_502_BAD_GATEWAY = 502
const val USER_LOGIN_FAILED = 451

//general exceptions
const val UNKNOWN_ERROR = 1000
const val NO_ACTIVE_CONNECTION = 1001
const val TIMEOUT = 1002
const val CONNECTION_SHUT_DOWN = 1003
const val UNKNOWN_HTTP_EXCEPTION = 1006
const val MISSING_DATA = 1007
const val CANT_CONNECT_TO_SERVER = 1008
const val MISSING_JSON_DATA = 1009
const val MALFORMED_JSON = 1010
const val HTTP_INTERCEPTOR_EXCEPTION = 1011
const val DEVELOPER_SETUP_ERROR = 1012
const val CANCELLATION_EXCEPTION = 1013
const val ROCKET_CHAT_EXCEPTION = 1014

//google and firebase errors
const val NULL_POINTER = 1315
const val SSL_HANDSHAKE = 1316
const val FIREBASE_DATABASE_ERROR = 1321
const val GOOGLE_GENERIC_ERROR = 1329
const val INVALID_CREDENTIALS = 1330
const val TOO_MANY_REQUESTS = 1331
const val GENERIC_FIREBASE_ERROR = 1334

class AppException : RuntimeException {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppExceptionEntryPoint {
        fun getContext(): Application
    }

    private lateinit var context: Application

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            SellApp.INSTANCE, AppExceptionEntryPoint::class.java
        )
        context = entryPoint.getContext()
    }

    companion object {
        private val errorCodeStringMap = ArrayMap<Int, Int>()
        val networkErrorCodes =
            listOf(
                NO_ACTIVE_CONNECTION, TIMEOUT, CANT_CONNECT_TO_SERVER, SSL_HANDSHAKE,
                CONNECTION_SHUT_DOWN
            )
    }

    init {
        synchronized(this) {
            errorCodeStringMap[HTTP_400_BAD_REQUEST] = R.string.bad_request
            errorCodeStringMap[HTTP_401_UNAUTHORIZED] = R.string.unauthorized_error
            errorCodeStringMap[UNKNOWN_ERROR] = R.string.unknown_error
            errorCodeStringMap[NO_ACTIVE_CONNECTION] = R.string.no_internet_connectivity
            errorCodeStringMap[TIMEOUT] = R.string.error_timeout
            errorCodeStringMap[CONNECTION_SHUT_DOWN] = R.string.no_internet_connectivity
            errorCodeStringMap[HTTP_403_FORBIDDEN] = R.string.internal_error
            errorCodeStringMap[HTTP_409_CONFLICT_RESPONSE] = R.string.conflict_response
            errorCodeStringMap[HTTP_413_PAYLOAD_TOO_LARGE] = R.string.payload_too_large
            errorCodeStringMap[HTTP_500_INTERNAL_ERROR] = R.string.internal_error
            errorCodeStringMap[HTTP_502_BAD_GATEWAY] = R.string.internal_error
            errorCodeStringMap[UNKNOWN_HTTP_EXCEPTION] = R.string.internal_error
            errorCodeStringMap[USER_LOGIN_FAILED] = R.string.internal_error
            errorCodeStringMap[CANT_CONNECT_TO_SERVER] = R.string.internal_error
            errorCodeStringMap[NULL_POINTER] = R.string.internal_error
            errorCodeStringMap[SSL_HANDSHAKE] = R.string.no_internet_connectivity
            errorCodeStringMap[FIREBASE_DATABASE_ERROR] = R.string.internal_error
            errorCodeStringMap[MISSING_DATA] = R.string.internal_error
            errorCodeStringMap[MISSING_JSON_DATA] = R.string.internal_error
            errorCodeStringMap[MALFORMED_JSON] = R.string.internal_error
            errorCodeStringMap[GOOGLE_GENERIC_ERROR] = R.string.internal_error
            errorCodeStringMap[INVALID_CREDENTIALS] = R.string.invalid_credentials
            errorCodeStringMap[TOO_MANY_REQUESTS] = R.string.internal_error
            errorCodeStringMap[DEVELOPER_SETUP_ERROR] = R.string.internal_error
            errorCodeStringMap[CANCELLATION_EXCEPTION] = R.string.internal_error
            errorCodeStringMap[ROCKET_CHAT_EXCEPTION] = R.string.internal_error
        }
    }


    var errorCode: Int = 0
    private var throwable: Throwable? = null
    private var errorMessage: String? = null

    constructor(errorCode: Int) : super() {
        this.errorCode = errorCode
        errorMessage = if (getErrorCodeMessageRes() == null) super.message
        else context.getString(getErrorCodeMessageRes()!!)
    }

    constructor(errorCode: Int, message: String) : super() {
        this.errorCode = errorCode
        this.errorMessage = message
    }

    constructor(throwable: Throwable?) : super(throwable) {
        this.throwable = throwable

        /**
         * Higher priority exceptions should be placedc higher in the when statement. If an exception
         * is a subclass of multiple cases, the first case would be selected.
         */
        when (throwable) {
            is AppException -> {
                this.errorCode = throwable.errorCode
                this.errorMessage = throwable.errorMessage
                this.throwable = throwable.throwable
            }
            is HttpException -> errorCode = when (throwable.code()) {
                400 -> HTTP_400_BAD_REQUEST
                401 -> HTTP_401_UNAUTHORIZED
                403 -> HTTP_403_FORBIDDEN
                404 -> INVALID_CREDENTIALS //for sandbox login API
                409 -> HTTP_409_CONFLICT_RESPONSE
                413 -> HTTP_413_PAYLOAD_TOO_LARGE
                422 -> HTTP_422_UNPROCESSIBLE_ENTITY
                500 -> HTTP_500_INTERNAL_ERROR
                502 -> HTTP_502_BAD_GATEWAY
                else -> UNKNOWN_HTTP_EXCEPTION
            }
            is UnknownHostException -> errorCode = NO_ACTIVE_CONNECTION
            is NullPointerException -> //if a stacktrace shows that an API call leads to this,
                // we have received a null "data" object in the response, despite it being successful
                // (having "success" in the status).
                errorCode = NULL_POINTER
            is SSLHandshakeException -> errorCode = SSL_HANDSHAKE
            is SocketTimeoutException -> errorCode = TIMEOUT
            is ConnectException -> errorCode = CANT_CONNECT_TO_SERVER
            is ConnectionShutdownException -> errorCode = CONNECTION_SHUT_DOWN
            is CancellationException -> errorCode = CANCELLATION_EXCEPTION
            else -> errorCode = UNKNOWN_ERROR
        }

        val errorCodeMessageRes = getErrorCodeMessageRes()
        if (errorCodeMessageRes != null) {
            errorMessage = context.getString(errorCodeMessageRes)
        } else if (errorMessage == null) {
            errorMessage = context.getString(R.string.unknown_error)
        }
    }

    override val cause: Throwable?
        get() = throwable

    @StringRes
    private fun getErrorCodeMessageRes(): Int? {
        return errorCodeStringMap[errorCode]
    }

    override val message: String
        get() = errorMessage?.let { "$it: $errorCode" } ?: ""

    fun isNetworkError() = networkErrorCodes.contains(errorCode)
}

fun Throwable?.isNetworkError(): Boolean {
    return when (this) {
        is AppException -> isNetworkError()
        is UnknownHostException,
        is SSLHandshakeException,
        is SocketTimeoutException,
        is ConnectException,
        is ConnectionShutdownException -> true
        else -> false
    }
}

fun <T> Resource<T>.isNetworkError() = throwable.isNetworkError()
