package com.example.smart_emap.core.network

import com.example.smart_emap.BuildConfig

object ApiDefaults {
    /** 登录页下拉可选的 API 地址（按原样连接，不再自动改写端口） */
    val presetBaseUrls: List<String> = listOf(
        "https://192.168.1.62:5005/",
        "http://192.168.1.62:3005/",
        "http://192.168.1.62:3010/",
        "https://192.168.1.62:5010/",
        "https://substance-ream-these.ngrok-free.dev/",
    )

    /** 默认 API 地址（优先 BuildConfig；若不在预设中则用第一个预设） */
    val displayBaseUrl: String
        get() {
            val fromBuild = ensureTrailingSlash(BuildConfig.DEFAULT_API_BASE_URL)
            return matchPreset(fromBuild) ?: presetBaseUrls.first()
        }

    private val legacyDevUrls = setOf(
        "http://10.0.2.2:8005",
        "http://localhost:8005",
        "http://127.0.0.1:8005",
        // 旧默认地址（自动迁移到当前预设默认）
        "http://192.168.0.12:8010",
        "https://192.168.0.12:5010",
        "http://192.168.0.12:5010",
        "http://192.168.1.62:8010",
        "http://192.168.1.62:8005",
    )

    fun resolveApiBaseUrl(saved: String?): String {
        val normalized = saved?.trim().orEmpty()
        if (normalized.isBlank() || isLegacyDevUrl(normalized)) {
            return displayBaseUrl
        }
        val matched = matchPreset(normalized)
        if (matched != null) return matched
        return migrateDevApiUrl(normalized)
    }

    /** 将任意保存地址对齐到预设（用于下拉框选中项）；无匹配时返回默认。 */
    fun resolvePresetSelection(saved: String?): String {
        val resolved = resolveApiBaseUrl(saved)
        return matchPreset(resolved) ?: displayBaseUrl
    }

    fun matchPreset(url: String): String? {
        val key = normalizeKey(url)
        return presetBaseUrls.firstOrNull { normalizeKey(it) == key }
    }

    /**
     * 开发环境兼容迁移。
     * 登录页预设地址按原样保留；其余旧前端端口仍映射到后端端口。
     */
    fun migrateDevApiUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        matchPreset(trimmed)?.let { return it }

        var current = trimmed
        val httpsBackend = Regex("^https://([^/:]+):(8010|8005|8020)$", RegexOption.IGNORE_CASE).find(current)
        if (httpsBackend != null && isPrivateOrLocalHost(httpsBackend.groupValues[1])) {
            current = "http://${httpsBackend.groupValues[1]}:${httpsBackend.groupValues[2]}"
        }
        // 非预设的旧前端端口：映射到后端
        val frontendProd = Regex("^(https?)://([^/:]+):(3005|5005)$", RegexOption.IGNORE_CASE).find(current)
        if (frontendProd != null) {
            return ensureTrailingSlash("http://${frontendProd.groupValues[2]}:8005")
        }
        val match = Regex("^(https?)://([^/:]+):(5010|5000)$", RegexOption.IGNORE_CASE).find(current)
            ?: return ensureTrailingSlash(current)
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
        if (matchPreset(key) != null) return false
        if (legacyDevUrls.contains(key)) return true
        // 任意 IP 的旧前端端口（不在预设中的才视为 legacy）
        return Regex("^https?://[^/:]+:(5000)$", RegexOption.IGNORE_CASE).matches(key)
    }

    fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    private fun normalizeKey(url: String): String =
        ensureTrailingSlash(url.trim().trimEnd('/')).lowercase()
}
