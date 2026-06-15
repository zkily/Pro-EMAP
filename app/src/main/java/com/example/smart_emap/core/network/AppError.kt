package com.example.smart_emap.core.network

/** 统一的应用层错误封装 */
sealed class AppError : Exception() {
    data class Network(val original: Throwable) : AppError()
    data class Api(val code: Int, val detail: String?) : AppError()
    data class Auth(val authMessage: String) : AppError()
    data class Unknown(val original: Throwable) : AppError()

    override val message: String?
        get() = when (this) {
            is Network -> "网络连接失败，请检查网络设置"
            is Api -> detail ?: "服务器请求失败 ($code)"
            is Auth -> authMessage
            is Unknown -> "发生未知错误: ${original.message}"
        }
}
