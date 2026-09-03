package com.example.smart_emap.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** ngrok 免费域名会拦截无浏览器头的请求，补上跳过警告页的头。 */
object NgrokInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val host = request.url.host.lowercase()
        if (!host.contains("ngrok")) {
            return chain.proceed(request)
        }
        return chain.proceed(
            request.newBuilder()
                .header("ngrok-skip-browser-warning", "true")
                .build(),
        )
    }
}
