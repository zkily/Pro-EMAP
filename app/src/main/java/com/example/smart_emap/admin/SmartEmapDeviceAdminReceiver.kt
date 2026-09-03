package com.example.smart_emap.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smart_emap.MainActivity
import com.example.smart_emap.core.deviceowner.DeviceOwnerController

/**
 * Device Owner / Device Admin 入口。
 *
 * 激活方式见 docs/DEVICE_OWNER.md：
 * - 开发：adb shell dpm set-device-owner ...
 * - 生产：QR / Zero-touch Provisioning
 */
class SmartEmapDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device admin enabled")
        DeviceOwnerController.applyPoliciesIfOwner(context)
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Profile/device provisioning complete")
        DeviceOwnerController.applyPoliciesIfOwner(context)
        launchMain(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.i(TAG, "Device admin disabled")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Lock task entered: $pkg")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.i(TAG, "Lock task exited")
    }

    companion object {
        private const val TAG = "SmartEmapDeviceAdmin"

        fun componentName(context: Context): ComponentName =
            ComponentName(context.applicationContext, SmartEmapDeviceAdminReceiver::class.java)

        /** 开机后拉起主界面（仅 Device Owner 场景由 BootReceiver 调用）。 */
        fun launchMain(context: Context) {
            val launch = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(launch)
        }
    }
}
