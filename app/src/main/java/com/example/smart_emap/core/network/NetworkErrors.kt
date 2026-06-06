package com.example.smart_emap.core.network

import com.example.smart_emap.data.repository.InspectionPatchException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/** 本地化网络错误提示，由 UI 层按当前语言传入 */
data class NetworkErrorHints(
    val ssl: String,
    val connection: String,
    val timeout: String,
    val server: String,
    val noConnection: String,
)

object NetworkErrors {
    /** 是否因网络/服务器不可用而适合进入离线队列 */
    fun isNetworkFailure(throwable: Throwable): Boolean {
        if (throwable is InspectionPatchException) {
            return throwable.statusCode >= 500 || throwable.statusCode == 408
        }
        if (throwable is HttpException) {
            val code = throwable.code()
            return code >= 500 || code == 408
        }
        if (throwable is SocketTimeoutException || throwable is UnknownHostException) return true
        if (throwable is IOException) return true
        val detail = collectMessages(throwable)
        return detail.contains("timeout", ignoreCase = true) ||
            detail.contains("Failed to connect", ignoreCase = true) ||
            detail.contains("Unable to resolve host", ignoreCase = true) ||
            detail.contains("Connection refused", ignoreCase = true)
    }

    fun formatError(throwable: Throwable, fallback: String, hints: NetworkErrorHints): String {
        if (throwable is HttpException) {
            return formatHttpError(throwable.code(), throwable.message(), fallback, hints)
        }
        return when (classify(throwable, collectMessages(throwable))) {
            ErrorKind.Ssl -> hints.ssl
            ErrorKind.Connection -> hints.connection
            ErrorKind.Timeout -> hints.timeout
            ErrorKind.Server -> hints.server
            ErrorKind.NoConnection -> hints.noConnection
            ErrorKind.Unknown -> throwable.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    fun formatHttpError(
        statusCode: Int,
        serverMessage: String?,
        fallback: String,
        hints: NetworkErrorHints,
    ): String {
        val trimmed = serverMessage?.trim().orEmpty()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("HTTP")) {
            return trimmed
        }
        return when (statusCode) {
            in 500..599 -> hints.server
            408, 504 -> hints.timeout
            else -> fallback
        }
    }

    /** @deprecated 请改用 [formatError] 并传入 [NetworkErrorHints] */
    fun formatLoadError(throwable: Throwable, fallback: String): String = formatError(
        throwable,
        fallback,
        NetworkErrorHints(
            ssl = "网络 SSL 连接失败。请将服务器地址改为后端 API（如 http://局域网IP:8010/），不要使用前端端口 5010。",
            connection = "无法连接服务器，请检查地址、端口与后端是否已启动。",
            timeout = "连接超时，请检查网络或稍后重试。",
            server = "服务器暂时不可用，请稍后重试。",
            noConnection = "无法连接网络，请检查 Wi‑Fi 或移动数据。",
        ),
    )

    private enum class ErrorKind {
        Ssl, Connection, Timeout, Server, NoConnection, Unknown,
    }

    private fun classify(throwable: Throwable, detail: String): ErrorKind {
        if (throwable is SocketTimeoutException) return ErrorKind.Timeout
        if (throwable is UnknownHostException) return ErrorKind.NoConnection
        if (throwable is HttpException && throwable.code() in 500..599) return ErrorKind.Server
        if (detail.contains("SSL", ignoreCase = true) ||
            detail.contains("BAD_DECRYPT", ignoreCase = true) ||
            detail.contains("DECRYPTION_FAILED", ignoreCase = true) ||
            detail.contains("BAD_RECORD_MAC", ignoreCase = true)
        ) {
            return ErrorKind.Ssl
        }
        if (detail.contains("timeout", ignoreCase = true) ||
            detail.contains("timed out", ignoreCase = true)
        ) {
            return ErrorKind.Timeout
        }
        if (detail.contains("Failed to connect", ignoreCase = true) ||
            detail.contains("Connection refused", ignoreCase = true) ||
            detail.contains("Unable to resolve host", ignoreCase = true) ||
            detail.contains("Network is unreachable", ignoreCase = true) ||
            detail.contains("No address associated with hostname", ignoreCase = true)
        ) {
            return ErrorKind.Connection
        }
        if (throwable is IOException && detail.contains("network", ignoreCase = true)) {
            return ErrorKind.NoConnection
        }
        return ErrorKind.Unknown
    }

    private fun collectMessages(throwable: Throwable): String = sequence {
        var current: Throwable? = throwable
        while (current != null) {
            yield(current.message.orEmpty())
            current = current.cause
        }
    }.joinToString(" ")
}
