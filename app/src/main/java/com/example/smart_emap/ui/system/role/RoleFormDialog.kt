package com.example.smart_emap.ui.system.role

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.ui.system.user.DialogPrimaryButton
import com.example.smart_emap.ui.system.user.DialogTextButton

@Composable
fun RoleFormDialog(
    isEdit: Boolean,
    form: RoleFormState,
    isSubmitting: Boolean,
    onFormChange: ((RoleFormState) -> RoleFormState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .shadow(14.dp, RolePermissionTheme.shapePage, spotColor = RolePermissionTheme.ShadowSoft),
            shape = RolePermissionTheme.shapePage,
            color = RolePermissionTheme.CardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, RolePermissionTheme.Border),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RolePermissionTheme.headerGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (isEdit) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            if (isEdit) "ロール編集" else "ロール新規登録",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RoleFormField(
                        label = "ロール名",
                        required = true,
                        value = form.name,
                        onValueChange = { v -> onFormChange { it.copy(name = v) } },
                        singleLine = true,
                    )
                    RoleFormField(
                        label = "説明",
                        value = form.description,
                        onValueChange = { v -> onFormChange { it.copy(description = v) } },
                        singleLine = false,
                        minLines = 2,
                    )
                }
                HorizontalDivider(color = RolePermissionTheme.BorderLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DialogTextButton(text = "キャンセル", onClick = onDismiss, enabled = !isSubmitting)
                    DialogPrimaryButton(text = "保存", isSubmitting = isSubmitting, onClick = onConfirm)
                }
            }
        }
    }
}

@Composable
private fun RoleFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    required: Boolean = false,
    minLines: Int = 1,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextSecondary)
            if (required) Text("*", fontSize = 11.sp, color = RolePermissionTheme.Error)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = RolePermissionTheme.TextPrimary),
            cursorBrush = SolidColor(Color(0xFF667EEA)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RolePermissionTheme.shapeInput)
                .border(1.dp, RolePermissionTheme.Border, RolePermissionTheme.shapeInput)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .then(if (!singleLine) Modifier.heightIn(min = 56.dp) else Modifier),
        )
    }
}
