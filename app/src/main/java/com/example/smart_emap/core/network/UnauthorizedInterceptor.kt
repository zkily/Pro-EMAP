package com.example.smart_emap.core.network

import com.example.smart_emap.core.auth.SessionEvents
import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor(
    private val sessionEvents: SessionEvents,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val path = request.url.encodedPath
        if (response.code == 401 && !path.endsWith("/api/auth/login")) {
            sessionEvents.notifyUnauthorized()
        }
        return response
    }
}
