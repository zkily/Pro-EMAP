package com.example.smart_emap.core.network

import com.example.smart_emap.core.auth.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isLogin = path.endsWith("/api/auth/login")

        if (isLogin) {
            return chain.proceed(request)
        }

        val token = runBlocking { sessionStore.getToken() }
        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
