package com.example.smart_emap.core.deviceowner

import android.content.Context

/** Device Owner / Kiosk 本地设置（PIN、是否自动锁定等）。 */
class KioskSettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 是否在 Device Owner 下自动进入 Lock Task（默认 true）。 */
    var isKioskEnabled: Boolean
        get() = prefs.getBoolean(KEY_KIOSK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_KIOSK_ENABLED, value).apply()

    fun verifyPin(input: String): Boolean {
        val expected = prefs.getString(KEY_ADMIN_PIN, DEFAULT_PIN).orEmpty()
        return input == expected
    }

    fun updatePin(newPin: String): Boolean {
        val trimmed = newPin.trim()
        if (trimmed.length !in 4..8 || !trimmed.all { it.isDigit() }) return false
        prefs.edit().putString(KEY_ADMIN_PIN, trimmed).apply()
        return true
    }

    companion object {
        private const val PREFS_NAME = "smart_emap_kiosk"
        private const val KEY_KIOSK_ENABLED = "kiosk_enabled"
        private const val KEY_ADMIN_PIN = "admin_pin"

        /** 出厂默认管理 PIN（部署后请立即修改）。 */
        const val DEFAULT_PIN = "2468"
    }
}
