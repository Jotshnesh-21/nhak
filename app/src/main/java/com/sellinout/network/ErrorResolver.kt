package com.sellinout.network

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sellinout.code.ErrorMessage
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import com.sellinout.ui.login.ActivityLogin
import com.sellinout.R

import com.sellinout.SellApp
import com.sellinout.utils.SharePrefsKey
import com.sellinout.utils.navigateClearStack
import com.sellinout.utils.showToast
import com.sellzinout.network.*
import com.sellzinout.network.isNetworkError
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Singleton

/**
 * Error handling for API responses. [handleErrorResponse] is the entry point for this.
 * [ErrorCallback] can be implemented at the view level to show appropriate UI for the error.
 */
@Singleton
object ErrorResolver {
    const val NO_SHOW = "NO_SHOW"

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ErrorResolverPoint {
        fun getGson(): Gson
    }

    private var gson: Gson


    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            SellApp.INSTANCE, ErrorResolverPoint::class.java
        )
        gson = entryPoint.getGson()
    }


    fun handleErrorResponse(
        context: Activity,
        fragment: Fragment? = null,
        retrofitResponse: Response<*>? = null,
        throwable: Throwable?,
        message: String? = null,
        retryAction: () -> Unit = {},
    ) {
        //handle the responses
        when {
            throwable.isNetworkError() -> showError(
                context,
                context.getString(R.string.no_internet_connectivity),
                error = true
            )

            else -> handleNonNetworkErrorResponse(
                context,
                fragment,
                retrofitResponse,
                throwable,
                message
            )
        }
    }

    private fun handleNonNetworkErrorResponse(
        context: Activity,
        fragment: Fragment? = null,
        response: Response<*>?,
        throwable: Throwable?,
        message: String?
    ) {
        //this is api error show error snackbar
        var callback: (() -> Unit)? = null
        val errorMessage = when (response?.code()) {
            HTTP_409_CONFLICT_RESPONSE, HTTP_422_UNPROCESSIBLE_ENTITY, HTTP_400_BAD_REQUEST -> {

                val error = response.errorBody()?.string()
                if (response != null && !error.isNullOrBlank()) {
                    gson.fromJson<ErrorMessage>(error, ErrorMessage::class.java).message
                } else {
                    response.message().orEmpty()
                }
            }

            HTTP_404_NOT_FOUND -> {
                try {
                    if (fragment is Any) {
                        "Email is not registered."
                    } else {
                        val jsonObject = JSONObject(response.errorBody()?.string())
                        jsonObject.optString("message", "").orEmpty()
                    }
                } catch (e: Exception) {
                    context.getString(R.string.link_not_found)
                }
            }

            HTTP_403_FORBIDDEN -> context.getString(R.string.access_denied)
            HTTP_401_UNAUTHORIZED -> {
                if (!SharePrefsKey.isGuestUser()) {
                    Toast.makeText(context, R.string.session_expired, Toast.LENGTH_SHORT).show()
                    context.run {
                        Prefs.clear()
                        context.navigateClearStack<ActivityLogin>()
                    }
                    context.getString(R.string.session_expired)
                } else {
                    NO_SHOW
                }

            }

            HTTP_500_INTERNAL_ERROR -> context.getString(R.string.server_error)
            else -> {
                if (throwable == null) {
                    message ?: //we shouldn't get to this state!
                    //Trying to handle an error with a null throwable!
                    ""
                } else {
                    //we have a non-HTTP, non-network exception like CancellationException,
                    // TooManyRequestsException, AppException, etc
                    if (throwable is AppException) {
                        //we've already figured out what message should be displayed here
                        throwable.message
                    } else {
                        //let AppException figure out what should be the message we display
                        AppException(throwable).message
                    }
                }
            }
        }

        if (errorMessage.isNotBlank() && errorMessage != NO_SHOW) {
            showError(context, errorMessage, error = true)
        } else if (errorMessage != NO_SHOW) {
            showError(context, context.getString(R.string.unknown_error), error = true)
        }
    }


    fun showError(
        context: Activity, message: String,
        title: String? = null,
        error: Boolean = false,
        callback: (() -> Unit)? = null
    ) {
        context.showToast(message)

    }
}
