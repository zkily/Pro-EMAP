package com.example.smart_emap.core.network

import com.example.smart_emap.BuildConfig

object ApiDefaults {
    /** 登录页 API サーバー 文本框默认显示（与 BuildConfig 一致） */
    val displayBaseUrl: String
        get() = ensureTrailingSlash(BuildConfig.DEFAULT_API_BASE_URL)

    private val legacyDevUrls = setOf(
        "http://10.0.2.2:8005",
        "http://localhost:8005",
        "http://127.0.0.1:8005",
    )

    fun resolveApiBaseUrl(saved: String?): String {
        val normalized = saved?.trim().orEmpty()
        if (normalized.isBlank() || isLegacyDevUrl(normalized)) {
            return displayBaseUrl
        }
        return ensureTrailingSlash(normalized)
    }

    fun isLegacyDevUrl(url: String): Boolean {
        val key = url.trim().trimEnd('/')
        return legacyDevUrls.contains(key)
    }

    fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
