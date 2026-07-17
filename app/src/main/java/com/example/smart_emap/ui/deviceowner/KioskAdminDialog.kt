package com.example.smart_emap.ui.deviceowner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.deviceowner.DeviceOwnerController
import com.example.smart_emap.core.deviceowner.KioskSettingsStore

/**
 * Device Owner / Kiosk 管理对话框：查看状态、PIN 退出维护模式、修改 PIN、重新锁定。
 */
@Composable
fun KioskAdminDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val store = remember { KioskSettingsStore(context) }

    var pin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val status = remember(store.isKioskEnabled) {
        DeviceOwnerController.statusSummary(context) +
            "\nLock Task: " +
            if (activity != null && DeviceOwnerController.isInLockTaskMode(activity)) "YES" else "NO"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("端末管理（Device Owner）", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = status,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter(Char::isDigit).take(8)
                        error = null
                    },
                    label = { Text("管理 PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it.filter(Char::isDigit).take(8)
                        error = null
                    },
                    label = { Text("新しい PIN（任意・4〜8桁）") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "默认 PIN：${KioskSettingsStore.DEFAULT_PIN}（请尽快修改）",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(end = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("閉じる")
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = {
                        if (!store.verifyPin(pin)) {
                            error = "PIN が正しくありません"
                            return@OutlinedButton
                        }
                        if (newPin.isNotEmpty()) {
                            if (!store.updatePin(newPin)) {
                                error = "新しい PIN は 4〜8 桁の数字にしてください"
                                return@OutlinedButton
                            }
                            message = "PIN を更新しました"
                            newPin = ""
                        }
                        if (activity != null) {
                            DeviceOwnerController.enterMaintenanceMode(activity)
                            message = (message?.plus(" / ") ?: "") + "メンテナンスモード（Kiosk 解除）"
                        }
                        pin = ""
                    },
                ) {
                    Text("退出 Kiosk")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = {
                        if (!store.verifyPin(pin)) {
                            error = "PIN が正しくありません"
                            return@Button
                        }
                        if (activity != null) {
                            DeviceOwnerController.resumeKioskMode(activity)
                            message = "Kiosk を再開しました"
                        }
                        pin = ""
                    },
                ) {
                    Text("再開 Kiosk")
                }
            }
        },
        dismissButton = null,
    )
}
