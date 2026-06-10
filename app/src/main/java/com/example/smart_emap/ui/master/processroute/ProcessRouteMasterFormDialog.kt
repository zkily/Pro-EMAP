package com.example.smart_emap.ui.master.processroute

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val formGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val controlHeight = 34.dp

@Composable
fun ProcessRouteFormDialog(
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
        Surface(modifier = Modifier.fillMaxWidth(0.94f), shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(formGradient)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (isEdit) "✏️" else "➕", fontSize = 22.sp)
                        Column {
                            Text(if (isEdit) "ルート編集" else "ルート追加", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("工程ルート情報を入力してください", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProcessRouteFormField("ルートコード", "例: R-STD01", values["route_cd"].orEmpty(), enabled = !isEdit && !loading) {
                        onValueChange("route_cd", it)
                    }
                    ProcessRouteFormField("ルート名称", "ルート名を入力", values["route_name"].orEmpty(), enabled = !loading) {
                        onValueChange("route_name", it)
                    }
                    ProcessRouteFormTextArea("説明", "ルートの説明を入力（任意）", values["description"].orEmpty(), enabled = !loading) {
                        onValueChange("description", it)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ProcessRouteSwitchItem(
                                label = "使用",
                                checked = values["is_active"] != "false",
                                enabled = !loading,
                                onCheckedChange = { onValueChange("is_active", if (it) "true" else "false") },
                                modifier = Modifier.weight(1f),
                            )
                            ProcessRouteSwitchItem(
                                label = "デフォルト",
                                checked = values["is_default"] == "true",
                                enabled = !loading,
                                onCheckedChange = { onValueChange("is_default", if (it) "true" else "false") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", fontSize = 12.sp) }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("💾 保存", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessRouteStepFormDialog(
    isEdit: Boolean,
    values: Map<String, String>,
    processOptions: List<Pair<String, String>>,
    loading: Boolean,
    onValueChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxWidth(0.94f), shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().background(formGradient).padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (isEdit) "✏️" else "➕", fontSize = 22.sp)
                        Column {
                            Text(if (isEdit) "ステップ編集" else "ステップ追加", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("工程ステップ情報を設定", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !loading, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProcessRouteFormField(
                            "順番",
                            "1",
                            values["step_no"].orEmpty(),
                            enabled = !loading,
                            modifier = Modifier.weight(1f),
                        ) { onValueChange("step_no", it) }
                        ProcessRouteFormField(
                            "歩留率(%)",
                            "100",
                            values["yield_percent"].orEmpty(),
                            enabled = !loading,
                            modifier = Modifier.weight(1f),
                        ) { onValueChange("yield_percent", it) }
                    }
                    ProcessRouteProcessSelect(
                        selected = values["process_cd"].orEmpty(),
                        options = processOptions,
                        enabled = !loading,
                        onSelect = { onValueChange("process_cd", it) },
                    )
                    ProcessRouteFormField(
                        "標準サイクル(秒)",
                        "0",
                        values["cycle_sec"].orEmpty(),
                        enabled = !loading,
                    ) { onValueChange("cycle_sec", it) }
                    Text("💡 工程選択時に自動設定", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    ProcessRouteFormTextArea("備考", "補足情報など", values["remarks"].orEmpty(), enabled = !loading) {
                        onValueChange("remarks", it)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", fontSize = 12.sp) }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("💾 保存", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessRouteFormField(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(controlHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    inner()
                }
            },
        )
    }
}

@Composable
private fun ProcessRouteFormTextArea(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                .padding(10.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                    if (value.isEmpty()) Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    inner()
                }
            },
        )
    }
}

@Composable
private fun ProcessRouteSwitchItem(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF667EEA)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessRouteProcessSelect(
    selected: String,
    options: List<Pair<String, String>>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == selected }?.let { "${it.first}｜${it.second}" }
        ?: selected.ifBlank { "工程を選択" }
    Column {
        Text("工程", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151), modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(controlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                color = if (enabled) Color.White else Color(0xFFF8FAFC),
                onClick = { if (enabled) expanded = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (selected.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (cd, name) ->
                    DropdownMenuItem(
                        text = { Text("$cd｜$name", fontSize = 12.sp) },
                        onClick = { onSelect(cd); expanded = false },
                    )
                }
            }
        }
    }
}
