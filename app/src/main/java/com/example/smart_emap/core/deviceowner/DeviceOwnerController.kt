package com.example.smart_emap.core.deviceowner

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserManager
import android.util.Log
import com.example.smart_emap.MainActivity
import com.example.smart_emap.admin.SmartEmapDeviceAdminReceiver

/**
 * Device Owner 策略与 Kiosk（Lock Task）控制。
 *
 * 仅在 [isDeviceOwner] 为 true 时生效；普通安装下所有方法安全空操作。
 */
object DeviceOwnerController {

    private const val TAG = "DeviceOwnerController"

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return false
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun isAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return false
        return dpm.isAdminActive(SmartEmapDeviceAdminReceiver.componentName(context))
    }

    fun isInLockTaskMode(activity: Activity): Boolean {
        val am = activity.getSystemService(ActivityManager::class.java) ?: return false
        return when (am.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_LOCKED,
            ActivityManager.LOCK_TASK_MODE_PINNED,
            -> true
            else -> false
        }
    }

    /**
     * 应用企业专用机策略：允许本包 Lock Task、禁用状态栏、限制用户操作等。
     * 应在成为 Device Owner 后、以及每次启动时调用。
     */
    fun applyPoliciesIfOwner(context: Context) {
        if (!isDeviceOwner(context)) {
            Log.d(TAG, "Not device owner — skip policies")
            return
        }

        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return
        val admin = SmartEmapDeviceAdminReceiver.componentName(context)
        val pkg = context.packageName

        try {
            dpm.setLockTaskPackages(admin, arrayOf(pkg))
            configurePersistentHome(context, dpm, admin, enabled = KioskSettingsStore(context).isKioskEnabled)

            // Kiosk：关闭 Home / Overview / 通知等系统入口
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(admin, true)
                dpm.setKeyguardDisabled(admin, true)
            }

            // 专用平板：限制常见干扰操作（可按现场需要增减）
            setRestriction(dpm, admin, UserManager.DISALLOW_FACTORY_RESET, true)
            setRestriction(dpm, admin, UserManager.DISALLOW_ADD_USER, true)
            setRestriction(dpm, admin, UserManager.DISALLOW_SAFE_BOOT, true)
            setRestriction(dpm, admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setRestriction(dpm, admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, true)
            }
            setRestriction(dpm, admin, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, true)
            setRestriction(dpm, admin, UserManager.DISALLOW_USB_FILE_TRANSFER, true)

            // 0 = 不强制超时锁屏；亮屏由 KeepAwakeHelper 负责
            dpm.setMaximumTimeToLock(admin, 0)

            Log.i(TAG, "Device owner policies applied")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to apply policies", e)
        }
    }

    /** 进入 Lock Task（真正的 Kiosk）。非 Owner 或未启用时忽略。 */
    fun startKioskIfNeeded(activity: Activity) {
        val store = KioskSettingsStore(activity)
        if (!isDeviceOwner(activity) || !store.isKioskEnabled) return
        if (isInLockTaskMode(activity)) return

        applyPoliciesIfOwner(activity)
        try {
            activity.startLockTask()
            Log.i(TAG, "startLockTask() requested")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "startLockTask failed — package not whitelisted?", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "startLockTask security failure", e)
        }
    }

    /** 退出 Lock Task（需先通过管理 PIN）。 */
    fun stopKiosk(activity: Activity) {
        if (!isInLockTaskMode(activity)) return
        try {
            activity.stopLockTask()
            Log.i(TAG, "stopLockTask() requested")
        } catch (e: Exception) {
            Log.e(TAG, "stopLockTask failed", e)
        }
    }

    /**
     * 临时关闭 Kiosk 并解除部分限制（仍保持 Device Owner）。
     * 用于维护；之后可再 [startKioskIfNeeded]。
     */
    fun enterMaintenanceMode(activity: Activity) {
        val store = KioskSettingsStore(activity)
        store.isKioskEnabled = false
        stopKiosk(activity)

        if (!isDeviceOwner(activity)) return
        val dpm = activity.getSystemService(DevicePolicyManager::class.java) ?: return
        val admin = SmartEmapDeviceAdminReceiver.componentName(activity)
        try {
            configurePersistentHome(activity, dpm, admin, enabled = false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(admin, false)
                dpm.setKeyguardDisabled(admin, false)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "exit restrictions failed", e)
        }
    }

    /** 重新开启 Kiosk。 */
    fun resumeKioskMode(activity: Activity) {
        KioskSettingsStore(activity).isKioskEnabled = true
        applyPoliciesIfOwner(activity)
        startKioskIfNeeded(activity)
    }

    /** 清除 Device Owner（仅调试；生产环境通常不可随意清除）。 */
    fun clearDeviceOwner(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return false
        return try {
            dpm.clearDeviceOwnerApp(context.packageName)
            true
        } catch (e: Exception) {
            Log.e(TAG, "clearDeviceOwnerApp failed", e)
            false
        }
    }

    fun statusSummary(context: Context): String {
        val owner = isDeviceOwner(context)
        val admin = isAdminActive(context)
        val kiosk = KioskSettingsStore(context).isKioskEnabled
        return buildString {
            append("Device Owner: "); append(if (owner) "YES" else "NO"); append('\n')
            append("Device Admin: "); append(if (admin) "YES" else "NO"); append('\n')
            append("Kiosk enabled: "); append(if (kiosk) "YES" else "NO")
        }
    }

    private fun setRestriction(
        dpm: DevicePolicyManager,
        admin: ComponentName,
        key: String,
        disallow: Boolean,
    ) {
        try {
            if (disallow) {
                dpm.addUserRestriction(admin, key)
            } else {
                dpm.clearUserRestriction(admin, key)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "restriction $key failed: ${e.message}")
        }
    }

    /**
     * 把 Smart-EMAP 注册为 Device Owner 下的持久 Home。
     *
     * 相比在 BOOT_COMPLETED 中直接启动 Activity，这不会受后台 Activity 启动限制影响：
     * 系统进入 Home 时会直接解析到 MainActivity。
     */
    private fun configurePersistentHome(
        context: Context,
        dpm: DevicePolicyManager,
        admin: ComponentName,
        enabled: Boolean,
    ) {
        dpm.clearPackagePersistentPreferredActivities(admin, context.packageName)
        if (!enabled) return

        val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val homeActivity = ComponentName(context, MainActivity::class.java)
        dpm.addPersistentPreferredActivity(admin, homeFilter, homeActivity)
        Log.i(TAG, "Smart-EMAP registered as persistent Home")
    }
}
