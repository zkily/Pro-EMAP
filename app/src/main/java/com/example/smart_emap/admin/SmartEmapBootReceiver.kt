package com.example.smart_emap.admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smart_emap.core.deviceowner.DeviceOwnerController
import com.example.smart_emap.core.deviceowner.KioskSettingsStore

/** 开机后若为本机 Device Owner 且开启 Kiosk，自动启动 Smart-EMAP。 */
class SmartEmapBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        if (!DeviceOwnerController.isDeviceOwner(context)) return
        if (!KioskSettingsStore(context).isKioskEnabled) return

        Log.i(TAG, "Boot completed — launching Smart-EMAP kiosk")
        DeviceOwnerController.applyPoliciesIfOwner(context)
        SmartEmapDeviceAdminReceiver.launchMain(context)
    }

    private companion object {
        const val TAG = "SmartEmapBoot"
    }
}
