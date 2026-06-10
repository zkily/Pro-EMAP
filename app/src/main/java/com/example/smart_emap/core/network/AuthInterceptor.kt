package com.example.smart_emap.core.network

import com.example.smart_emap.core.auth.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
        if (token.isNullOrBlank()) {
            val body = """{"detail":"Not authenticated"}"""
                .toResponseBody("application/json".toMediaType())
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(body)
                .build()
        }
        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}
