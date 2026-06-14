package com.example.smart_emap.core.network

import com.example.smart_emap.BuildConfig

object ApiDefaults {
    /** 默认 API 地址（BuildConfig.DEFAULT_API_BASE_URL，登录页可编辑覆盖） */
    val displayBaseUrl: String
        get() = ensureTrailingSlash(BuildConfig.DEFAULT_API_BASE_URL)

    private val legacyDevUrls = setOf(
        "http://10.0.2.2:8005",
        "http://localhost:8005",
        "http://127.0.0.1:8005",
        // 旧默认：直连后端 API 8010
        "http://192.168.1.62:8010",
        "https://192.168.1.62:5010",
        "http://192.168.1.62:5010",
    )

    fun resolveApiBaseUrl(saved: String?): String {
        val normalized = saved?.trim().orEmpty()
        if (normalized.isBlank() || isLegacyDevUrl(normalized)) {
            return migrateDevApiUrl(displayBaseUrl.trimEnd('/')).let { ensureTrailingSlash(it) }
        }
        return migrateDevApiUrl(normalized)
    }

    /**
     * 开发环境：前端 Vite 端口 (5010/5000) 仅用于浏览器，Android 应直连后端 API。
     * 局域网 HTTPS / 自签名证书在 OkHttp 上易出现 BAD_DECRYPT，私有网段后端改 HTTP。
     */
    fun migrateDevApiUrl(url: String): String {
        var trimmed = url.trim().trimEnd('/')
        val httpsBackend = Regex("^https://([^/:]+):(8010|8005|8020)$", RegexOption.IGNORE_CASE).find(trimmed)
        if (httpsBackend != null && isPrivateOrLocalHost(httpsBackend.groupValues[1])) {
            trimmed = "http://${httpsBackend.groupValues[1]}:${httpsBackend.groupValues[2]}"
        }
        val frontendProd = Regex("^(https?)://([^/:]+):(3005|5005)$", RegexOption.IGNORE_CASE).find(trimmed)
        if (frontendProd != null) {
            return ensureTrailingSlash("http://${frontendProd.groupValues[2]}:8005")
        }
        val match = Regex("^(https?)://([^/:]+):(5010|5000)$", RegexOption.IGNORE_CASE).find(trimmed)
            ?: return ensureTrailingSlash(trimmed)
        val host = match.groupValues[2]
        val backendPort = if (match.groupValues[3] == "5010") 8010 else 8005
        return ensureTrailingSlash("http://$host:$backendPort")
    }

    private fun isPrivateOrLocalHost(host: String): Boolean {
        val h = host.lowercase()
        if (h == "localhost" || h == "10.0.2.2" || h == "127.0.0.1") return true
        if (h.startsWith("192.168.")) return true
        if (h.startsWith("10.")) return true
        return Regex("^172\\.(1[6-9]|2\\d|3[01])\\.").containsMatchIn(h)
    }

    fun isLegacyDevUrl(url: String): Boolean {
        val key = url.trim().trimEnd('/')
        if (legacyDevUrls.contains(key)) return true
        // 任意 IP 的前端端口（Vite dev / start.py 本番静态服务）
        return Regex("^https?://[^/:]+:(3005|5005|5010|5000)$", RegexOption.IGNORE_CASE).matches(key)
    }

    fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
