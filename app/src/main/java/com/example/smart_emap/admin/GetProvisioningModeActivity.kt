package com.example.smart_emap.admin

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * QR / Zero-touch 开通时由系统调用，声明管理方式。
 * 必须返回 [DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE]（整机 Device Owner）。
 */
class GetProvisioningModeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fullyManaged = DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
        val allowed = intent.getIntegerArrayListExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES,
        )

        if (allowed != null && allowed.isNotEmpty() && fullyManaged !in allowed) {
            Log.e(TAG, "FULLY_MANAGED_DEVICE not allowed: $allowed")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val result = Intent().apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, fullyManaged)
        }
        Log.i(TAG, "Returning PROVISIONING_MODE_FULLY_MANAGED_DEVICE")
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        private const val TAG = "GetProvisioningMode"
    }
}
