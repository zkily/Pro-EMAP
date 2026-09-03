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
        
        // login / qr-login は未認証のため Token を付けない；logout は Bearer が必要
        if (path.endsWith("/api/auth/login") || path.endsWith("/api/auth/qr-login")) {
            return chain.proceed(request)
        }

        // 优先从内存缓存中获取 Token，避免 runBlocking 阻塞线程池
        val token = sessionStore.cachedToken ?: runBlocking { sessionStore.getToken() }
        
        if (token.isNullOrBlank()) {
            // 如果本地没有 Token，直接返回 401 拦截请求
            val body = """{"detail":"Not authenticated (No local token)"}"""
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
