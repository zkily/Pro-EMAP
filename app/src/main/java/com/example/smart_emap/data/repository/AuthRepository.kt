package com.example.smart_emap.data.repository

import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.core.network.ApiDefaults
import com.example.smart_emap.core.network.NetworkErrorHints
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.data.model.ApiErrorBody
import com.example.smart_emap.data.model.LoginRequest
import com.example.smart_emap.data.model.QrLoginRequest
import com.example.smart_emap.data.model.UserDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val sessionStore: SessionStore,
    private val apiClient: ApiClient,
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiErrorBody::class.java)

    suspend fun getSavedUser(): UserDto? = sessionStore.getUser()

    suspend fun getToken(): String? = sessionStore.getToken()

    suspend fun getApiBaseUrl(defaultUrl: String): String = sessionStore.getApiBaseUrl(defaultUrl)

    /** 清除本地登录态，不调用后端 logout（用于 token 已失效时）。 */
    suspend fun clearLocalSession() {
        sessionStore.clear()
        apiClient.invalidate()
    }

    /**
     * 启动时恢复会话：必须同时存在 user + token，且 /api/auth/me 校验通过。
     * 失败则清空本地会话，避免无 token 进入主界面触发大量 401。
     */
    suspend fun restoreSession(): UserDto? {
        val user = sessionStore.getUser() ?: return null
        val token = sessionStore.getToken()
        if (token.isNullOrBlank()) {
            clearLocalSession()
            return null
        }
        return refreshMe().getOrElse {
            clearLocalSession()
            null
        }
    }

    suspend fun getRememberedCredentials() = sessionStore.getRememberedCredentials()

    suspend fun saveApiBaseUrl(url: String) {
        sessionStore.saveApiBaseUrl(url)
        apiClient.invalidate()
    }

    suspend fun clearRememberedCredentials() {
        sessionStore.saveRememberMe(remember = false, username = "")
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
            val normalizedUrl = ApiDefaults.ensureTrailingSlash(
                ApiDefaults.migrateDevApiUrl(apiBaseUrl.trim().trimEnd('/')),
            )
            sessionStore.saveApiBaseUrl(normalizedUrl)
            apiClient.invalidate()
            val response = apiClient.authApiForBaseUrl(normalizedUrl).login(
                LoginRequest(username = identifier, password = password),
            )
            persistLoginSession(response.accessToken, response.user, rememberMe, identifier)
            response.user
        }.recoverCatching { e ->
            throw mapError(e)
        }
    }

    suspend fun qrLogin(
        code: String,
        apiBaseUrl: String,
        rememberMe: Boolean,
    ): Result<UserDto> {
        return runCatching {
            val normalizedUrl = ApiDefaults.ensureTrailingSlash(
                ApiDefaults.migrateDevApiUrl(apiBaseUrl.trim().trimEnd('/')),
            )
            sessionStore.saveApiBaseUrl(normalizedUrl)
            apiClient.invalidate()
            val response = apiClient.authApiForBaseUrl(normalizedUrl).qrLogin(
                QrLoginRequest(code = code),
            )
            persistLoginSession(
                token = response.accessToken,
                user = response.user,
                rememberMe = rememberMe,
                username = response.user.username,
            )
            response.user
        }.recoverCatching { e ->
            throw mapError(e)
        }
    }

    private suspend fun persistLoginSession(
        token: String,
        user: UserDto,
        rememberMe: Boolean,
        username: String,
    ) {
        sessionStore.saveSession(token, user)
        sessionStore.saveRememberMe(
            remember = rememberMe,
            username = username,
        )
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
        if (throwable is IOException) {
            val sslHints = NetworkErrorHints(
                ssl = "SSL 连接失败。请将 API 地址改为后端 HTTP（如 http://局域网IP:8010/），勿用 https 或前端端口 5010/3005。",
                connection = "无法连接服务器，请检查地址、端口与后端是否已启动。",
                timeout = "连接超时，请检查网络或稍后重试。",
                server = "服务器暂时不可用，请稍后重试。",
                noConnection = "无法连接网络，请检查 Wi‑Fi 或移动数据。",
            )
            return Exception(
                NetworkErrors.formatError(throwable, "ネットワークエラー。サーバーアドレスを確認してください。", sslHints),
            )
        }
        return Exception(throwable.message ?: "ネットワークエラー。サーバーアドレスを確認してください。")
    }
}
