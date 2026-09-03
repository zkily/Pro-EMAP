package com.example.smart_emap.ui.auth

import org.json.JSONObject

/** ログインQRの解析。公式形式はパスワードを含めない。 */
object LoginQrPayload {
    const val PREFIX = "SEMAP-LOGIN:v1:"

    sealed class Parsed {
        data class TokenLogin(val username: String, val token: String) : Parsed()
        data class PasswordLogin(val username: String, val password: String) : Parsed()
        data class UsernameOnly(val username: String) : Parsed()
        data object Invalid : Parsed()
    }

    fun parse(raw: String): Parsed {
        val code = raw.trim()
        if (code.isEmpty()) return Parsed.Invalid

        if (code.startsWith(PREFIX)) {
            val rest = code.removePrefix(PREFIX)
            val sep = rest.lastIndexOf(':')
            if (sep <= 0 || sep >= rest.length - 1) return Parsed.Invalid
            val username = rest.substring(0, sep).trim()
            val token = rest.substring(sep + 1).trim()
            if (username.isEmpty() || token.isEmpty()) return Parsed.Invalid
            return Parsed.TokenLogin(username, token)
        }

        if (code.startsWith("{")) {
            val json = runCatching { JSONObject(code) }.getOrNull() ?: return Parsed.Invalid
            val username = json.optString("u").ifBlank { json.optString("username") }.trim()
            val token = json.optString("t").ifBlank { json.optString("token") }.trim()
            val password = json.optString("p").ifBlank { json.optString("password") }
            if (username.isNotEmpty() && token.isNotEmpty()) {
                return Parsed.TokenLogin(username, token)
            }
            if (username.isNotEmpty() && password.isNotEmpty()) {
                return Parsed.PasswordLogin(username, password)
            }
            if (username.isNotEmpty()) {
                return Parsed.UsernameOnly(username)
            }
            return Parsed.Invalid
        }

        if (code.length in 3..50 && !code.contains('\n') && !code.contains(' ')) {
            return Parsed.UsernameOnly(code)
        }
        return Parsed.Invalid
    }
}
