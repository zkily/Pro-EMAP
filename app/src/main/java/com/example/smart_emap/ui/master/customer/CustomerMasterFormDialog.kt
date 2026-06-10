package com.example.smart_emap.ui.master.customer

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMasterFormDialog(
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
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (isEdit) "顧客情報の編集" else "顧客情報の登録",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = values["customer_cd"].orEmpty(),
                            onValueChange = { if (it.length <= 50) onValueChange("customer_cd", it) },
                            label = { Text("顧客CD", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !isEdit && !loading,
                            singleLine = true,
                            colors = customerFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                        CustomerTypeSelect(
                            value = values["customer_type"].orEmpty(),
                            onChange = { onValueChange("customer_type", it) },
                            enabled = !loading,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = values["customer_name"].orEmpty(),
                        onValueChange = { onValueChange("customer_name", it) },
                        label = { Text("顧客名", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        singleLine = true,
                        colors = customerFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = values["phone"].orEmpty(),
                            onValueChange = { onValueChange("phone", it) },
                            label = { Text("電話番号", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            enabled = !loading,
                            singleLine = true,
                            colors = customerFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                        )
                        Row(
                            modifier = Modifier.weight(1f),
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
                    OutlinedTextField(
                        value = values["address"].orEmpty(),
                        onValueChange = { onValueChange("address", it) },
                        label = { Text("住所", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        singleLine = true,
                        colors = customerFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerTypeSelect(
    value: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("" to "選択", "法人" to "法人", "個人" to "個人", "代理店" to "代理店")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == value }?.second.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("種別", fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            enabled = enabled,
            colors = customerFieldColors(),
            shape = RoundedCornerShape(8.dp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.drop(1).forEach { (v, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onChange(v); expanded = false })
            }
        }
    }
}

@Composable
private fun customerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF0EA5E9),
    focusedLabelColor = Color(0xFF0EA5E9),
    cursorColor = Color(0xFF0EA5E9),
)
