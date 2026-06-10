package com.example.smart_emap.ui.master.carrier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CarrierMasterFormDialog(
    isEdit: Boolean,
    values: Map<String, String>,
    loading: Boolean,
    onValueChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚚", fontSize = 22.sp)
                    Column {
                        Text(
                            if (isEdit) "運送便情報の編集" else "運送便情報の登録",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B),
                        )
                        Text("運送便の基本情報を入力してください", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("基本情報", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0EA5E9))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = values["carrier_cd"].orEmpty(),
                            onValueChange = { if (it.length <= 50) onValueChange("carrier_cd", it) },
                            label = { Text("運送便CD", fontSize = 12.sp) },
                            placeholder = { Text("例: U01", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !isEdit && !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                        OutlinedTextField(
                            value = values["carrier_name"].orEmpty(),
                            onValueChange = { onValueChange("carrier_name", it) },
                            label = { Text("運送便名称", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                    Text("連絡先", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0EA5E9))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = values["contact_person"].orEmpty(),
                            onValueChange = { onValueChange("contact_person", it) },
                            label = { Text("連絡人", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                        OutlinedTextField(
                            value = values["phone"].orEmpty(),
                            onValueChange = { onValueChange("phone", it) },
                            label = { Text("電話番号", fontSize = 12.sp) },
                            placeholder = { Text("03-1234-5678", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                    Text("その他", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0EA5E9))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = values["shipping_time"].orEmpty(),
                            onValueChange = { onValueChange("shipping_time", it) },
                            label = { Text("出荷時間", fontSize = 12.sp) },
                            placeholder = { Text("HH:mm", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                        OutlinedTextField(
                            value = values["report_no"].orEmpty(),
                            onValueChange = { onValueChange("report_no", it) },
                            label = { Text("報告No", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = carrierFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                    OutlinedTextField(
                        value = values["note"].orEmpty(),
                        onValueChange = { onValueChange("note", it) },
                        label = { Text("備考", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        minLines = 2,
                        maxLines = 3,
                        colors = carrierFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("有効", fontSize = 12.sp, color = Color(0xFF64748B))
                        Switch(
                            checked = values["status"] != "0",
                            onCheckedChange = { onValueChange("status", if (it) "1" else "0") },
                            enabled = !loading,
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF0EA5E9)),
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", color = Color(0xFF64748B)) }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 8.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("保存", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun carrierFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF0EA5E9),
    focusedLabelColor = Color(0xFF0EA5E9),
    cursorColor = Color(0xFF0EA5E9),
)
