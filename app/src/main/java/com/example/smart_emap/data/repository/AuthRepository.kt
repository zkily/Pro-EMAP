package com.example.smart_emap.data.repository

import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ApiErrorBody
import com.example.smart_emap.data.model.LoginRequest
import com.example.smart_emap.data.model.UserDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

class AuthRepository(
    private val sessionStore: SessionStore,
    private val apiClient: ApiClient,
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiErrorBody::class.java)

    suspend fun getSavedUser(): UserDto? = sessionStore.getUser()

    suspend fun getApiBaseUrl(defaultUrl: String): String = sessionStore.getApiBaseUrl(defaultUrl)

    suspend fun getRememberedCredentials() = sessionStore.getRememberedCredentials()

    suspend fun saveApiBaseUrl(url: String) {
        sessionStore.saveApiBaseUrl(url)
        apiClient.invalidate()
    }

    suspend fun clearRememberedCredentials() {
        sessionStore.saveRememberMe(remember = false, username = "", password = "")
    }

    suspend fun login(
        username: String,
        password: String,
        apiBaseUrl: String,
        rememberMe: Boolean,
    ): Result<UserDto> {
        return runCatching {
            val identifier = username.trim().let {
                if (it.contains('@')) it.lowercase() else it
            }
            sessionStore.saveApiBaseUrl(apiBaseUrl)
            apiClient.invalidate()
            val response = apiClient.authApi().login(
                LoginRequest(username = identifier, password = password),
            )
            sessionStore.saveSession(response.accessToken, response.user)
            sessionStore.saveRememberMe(
                remember = rememberMe,
                username = identifier,
                password = if (rememberMe) password else "",
            )
            response.user
        }.recoverCatching { e ->
            throw mapError(e)
        }
    }

    suspend fun logout() {
        runCatching { apiClient.authApi().logout() }
        sessionStore.clear()
        apiClient.invalidate()
    }

    suspend fun refreshMe(): Result<UserDto> {
        return runCatching {
            val user = apiClient.authApi().me()
            val token = sessionStore.getToken() ?: throw IllegalStateException("未登录")
            sessionStore.saveSession(token, user)
            user
        }.recoverCatching { e ->
            throw mapError(e)
        }
    }

    private fun mapError(throwable: Throwable): Exception {
        if (throwable is HttpException) {
            val body = throwable.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
                val message = parsed?.error?.message
                    ?: parsed?.detail
                    ?: throwable.message()
                    ?: "请求失败 (${throwable.code()})"
                return Exception(message)
            }
            if (throwable.code() == 401) {
                return Exception(
                    "認証に失敗しました。ユーザー名・パスワード、または API サーバー起動状態を確認してください。",
                )
            }
            return Exception("请求失败 (${throwable.code()})")
        }
        return Exception(throwable.message ?: "ネットワークエラー。サーバーアドレスを確認してください。")
    }
}
