package com.example.smart_emap.ui.system.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ResetPasswordDialog(
    form: ResetPasswordFormState,
    isSubmitting: Boolean,
    onFormChange: ((ResetPasswordFormState) -> ResetPasswordFormState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(16.dp, SystemUserTheme.shapePage, spotColor = SystemUserTheme.ShadowSoft),
            shape = SystemUserTheme.shapePage,
            color = SystemUserTheme.CardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.Border),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SystemUserTheme.headerGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(SystemUserTheme.shapeInput)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), SystemUserTheme.shapeInput),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("パスワード再設定", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("新しいパスワードを入力してください", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PasswordField(
                        label = "新しいパスワード",
                        value = form.newPassword,
                        onValueChange = { v -> onFormChange { f -> f.copy(newPassword = v) } },
                    )
                    PasswordField(
                        label = "パスワード確認",
                        value = form.confirmPassword,
                        onValueChange = { v -> onFormChange { f -> f.copy(confirmPassword = v) } },
                    )
                }
                androidx.compose.material3.HorizontalDivider(color = SystemUserTheme.Border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DialogTextButton(text = "キャンセル", onClick = onDismiss, enabled = !isSubmitting)
                    DialogPrimaryButton(text = "再設定", isSubmitting = isSubmitting, onClick = onConfirm)
                }
            }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SystemUserTheme.TextMuted)
        Surface(
            shape = SystemUserTheme.shapeInput,
            color = SystemUserTheme.SurfaceMuted,
            border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.Border),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = SystemUserTheme.TextPrimary),
                visualTransformation = PasswordVisualTransformation(),
                cursorBrush = SolidColor(SystemUserTheme.PrimaryStart),
            )
        }
    }
}
