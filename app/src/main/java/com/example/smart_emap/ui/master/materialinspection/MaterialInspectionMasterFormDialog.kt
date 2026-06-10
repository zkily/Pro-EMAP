package com.example.smart_emap.ui.master.materialinspection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val formSlateGradient = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))

@Composable
fun MaterialInspectionMasterFormDialog(
    isEdit: Boolean,
    inspectionCd: String,
    inspectionStandard: String,
    loading: Boolean,
    onCdChange: (String) -> Unit,
    onStandardChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    if (isEdit) "材料検品マスタ編集" else "材料検品マスタ新規追加",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B),
                )
                OutlinedTextField(
                    value = inspectionCd,
                    onValueChange = { if (it.length <= 50) onCdChange(it) },
                    label = { Text("材料検品CD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("${inspectionCd.length}/50", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF64748B),
                        focusedLabelColor = Color(0xFF64748B),
                        cursorColor = Color(0xFF64748B),
                    ),
                    shape = RoundedCornerShape(10.dp),
                )
                Column {
                    Text("材料検品規格", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    BasicTextField(
                        value = inspectionStandard,
                        onValueChange = { if (it.length <= 500) onStandardChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .padding(top = 6.dp),
                        decorationBox = { inner ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                color = Color(0xFFFAFBFC),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    inner()
                                }
                            }
                        },
                    )
                    Text(
                        "${inspectionStandard.length}/500",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !loading) {
                        Text("キャンセル", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.background(formSlateGradient, RoundedCornerShape(10.dp)),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(if (isEdit) "更新" else "作成", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
