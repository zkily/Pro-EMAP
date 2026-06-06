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
        // 旧默认：误指向前端 Vite HTTPS 端口
        "https://192.168.1.62:5010",
        "http://192.168.1.62:5010",
    )

    fun resolveApiBaseUrl(saved: String?): String {
        val normalized = saved?.trim().orEmpty()
        if (normalized.isBlank() || isLegacyDevUrl(normalized)) {
            return displayBaseUrl
        }
        return migrateDevApiUrl(normalized)
    }

    /**
     * 开发环境：前端 Vite 端口 (5010/5000) 仅用于浏览器，Android 应直连后端 API。
     * 使用 HTTP 避免 LAN 自签名证书在 OkHttp 上出现 BAD_DECRYPT。
     */
    fun migrateDevApiUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        val match = Regex("^(https?)://([^/:]+):(5010|5000)$", RegexOption.IGNORE_CASE).find(trimmed)
            ?: return ensureTrailingSlash(url)
        val host = match.groupValues[2]
        val backendPort = if (match.groupValues[3] == "5010") 8010 else 8005
        return ensureTrailingSlash("http://$host:$backendPort")
    }

    fun isLegacyDevUrl(url: String): Boolean {
        val key = url.trim().trimEnd('/')
        if (legacyDevUrls.contains(key)) return true
        // 任意 IP 的前端 dev 端口
        return Regex("^https?://[^/:]+:(5010|5000)$", RegexOption.IGNORE_CASE).matches(key)
    }

    fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
