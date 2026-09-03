package com.example.smart_emap.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.smart_emap.MainActivity
import com.example.smart_emap.core.deviceowner.DeviceOwnerController

/**
 * Device Owner 设置完成后由系统调用，应用 Kiosk 策略并进入主界面。
 * 同时处理 [android.app.admin.DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE]
 * 与较旧流程的 [android.app.admin.DevicePolicyManager.ACTION_PROVISIONING_SUCCESSFUL]。
 */
class AdminPolicyComplianceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Provisioning compliance: action=${intent?.action}")

        DeviceOwnerController.applyPoliciesIfOwner(this)

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
        )

        setResult(RESULT_OK)
        finish()
    }

    companion object {
        private const val TAG = "AdminPolicyCompliance"
    }
}
