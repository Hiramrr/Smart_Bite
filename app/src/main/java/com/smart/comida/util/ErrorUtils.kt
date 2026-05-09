package com.smart.comida.util

import android.content.Context
import com.smart.comida.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorUtils {

    data class ErrorDetails(val title: String, val message: String)

    fun getErrorDetails(context: Context, throwable: Throwable?): ErrorDetails {
        return when (throwable) {
            is UnknownHostException, is IOException -> {
                ErrorDetails(
                    context.getString(R.string.error_network),
                    context.getString(R.string.error_network)
                )
            }
            is SocketTimeoutException -> {
                ErrorDetails(
                    context.getString(R.string.error_timeout),
                    context.getString(R.string.error_timeout)
                )
            }
            else -> {
                ErrorDetails(
                    context.getString(R.string.error_unknown),
                    throwable?.message ?: context.getString(R.string.error_unknown)
                )
            }
        }
    }
}
