package com.example.smart_emap.core.system

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/** 应用在前台使用时保持亮屏，并引导用户关闭对本应用的电池优化限制。 */
object KeepAwakeHelper {

    private const val PREFS_NAME = "smart_emap_system"
    private const val KEY_BATTERY_OPT_PROMPTED = "battery_optimization_prompt_shown"

    fun bindActivity(activity: ComponentActivity) {
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                override fun onStop(owner: LifecycleOwner) {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            },
        )
    }

    /** 若尚未豁免电池优化且从未提示过，则弹出系统设置页（仅首次）。 */
    fun requestBatteryOptimizationExemptionIfNeeded(activity: Activity) {
        val powerManager = activity.getSystemService(PowerManager::class.java) ?: return
        if (powerManager.isIgnoringBatteryOptimizations(activity.packageName)) return

        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_BATTERY_OPT_PROMPTED, false)) return

        prefs.edit().putBoolean(KEY_BATTERY_OPT_PROMPTED, true).apply()

        try {
            activity.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                },
            )
        } catch (_: ActivityNotFoundException) {
            // 部分定制 ROM 不支持该 Intent，忽略即可
        }
    }
}
