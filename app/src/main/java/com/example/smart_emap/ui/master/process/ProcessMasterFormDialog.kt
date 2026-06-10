package com.example.smart_emap.ui.master.process

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private val processFormPrimary = Color(0xFF667EEA)
private val processFormControlHeight = 34.dp

@Composable
fun ProcessMasterFormDialog(
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
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column {
                val headerGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(
                                if (isEdit) "工程編集" else "工程追加",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                            Text("工程情報を入力してください", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { if (!loading) onDismiss() }, enabled = !loading) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProcessFormTextField(
                            label = "工程コード",
                            value = values["process_cd"].orEmpty(),
                            onChange = { onValueChange("process_cd", it) },
                            modifier = Modifier.weight(1f),
                            enabled = !isEdit,
                            required = true,
                            placeholder = "例: KT01",
                        )
                        ProcessFormTextField(
                            label = "略称",
                            value = values["short_name"].orEmpty(),
                            onChange = { onValueChange("short_name", it) },
                            modifier = Modifier.weight(1f),
                            placeholder = "2〜3文字",
                        )
                    }
                    ProcessFormTextField(
                        label = "工程名称",
                        value = values["process_name"].orEmpty(),
                        onChange = { onValueChange("process_name", it) },
                        required = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProcessFormSelectField(
                            label = "分類",
                            value = values["category"].orEmpty(),
                            onChange = { onValueChange("category", it) },
                            options = listOf("" to "選択") + PROCESS_CATEGORIES,
                            modifier = Modifier.weight(1f),
                        )
                        ProcessFormSelectField(
                            label = "能力単位",
                            value = values["capacity_unit"].orEmpty().ifBlank { "pcs" },
                            onChange = { onValueChange("capacity_unit", it) },
                            options = PROCESS_CAPACITY_UNITS.map { it to it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProcessFormNumberField(
                            label = "標準サイクル(秒)",
                            value = values["default_cycle_sec"].orEmpty(),
                            onChange = { onValueChange("default_cycle_sec", it) },
                            modifier = Modifier.weight(1f),
                            step = 0.1,
                        )
                        ProcessFormNumberField(
                            label = "歩留(%)",
                            value = values["default_yield_percent"].orEmpty(),
                            onChange = { onValueChange("default_yield_percent", it) },
                            modifier = Modifier.weight(1f),
                            step = 0.1,
                            maxValue = 100.0,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("外注", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
                        Switch(
                            checked = values["is_outsource"] == "true",
                            onCheckedChange = { onValueChange("is_outsource", if (it) "true" else "false") },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color(0xFFE74C3C),
                                checkedThumbColor = Color.White,
                            ),
                        )
                    }
                    ProcessFormTextField(
                        label = "備考",
                        value = values["remark"].orEmpty(),
                        onChange = { onValueChange("remark", it) },
                        singleLine = false,
                        minLines = 2,
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", fontSize = 12.sp) }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        modifier = Modifier.padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = processFormPrimary),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("保存", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessFormFieldLabel(label: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
        if (required) Text(" *", fontSize = 11.sp, color = Color(0xFFEF4444))
    }
}

@Composable
private fun ProcessFormTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    required: Boolean = false,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column(modifier = modifier) {
        ProcessFormFieldLabel(label, required)
        val shape = RoundedCornerShape(8.dp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = TextStyle(fontSize = 12.sp, color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8)),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(processFormControlHeight) else Modifier.heightIn(min = processFormControlHeight * minLines))
                .clip(shape)
                .background(if (enabled) Color.White else Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 10.dp, vertical = if (singleLine) 0.dp else 8.dp),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth().then(if (singleLine) Modifier.fillMaxHeight() else Modifier),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotBlank()) {
                        Text(placeholder, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    inner()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessFormSelectField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "選択" }
    Column(modifier = modifier) {
        ProcessFormFieldLabel(label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(processFormControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (v, text) ->
                    DropdownMenuItem(
                        text = { Text(text, fontSize = 12.sp) },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessFormNumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    step: Double = 0.1,
    minValue: Double = 0.0,
    maxValue: Double = Double.MAX_VALUE,
) {
    Column(modifier = modifier) {
        ProcessFormFieldLabel(label)
        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(processFormControlHeight)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { raw -> if (raw.isEmpty() || raw.toDoubleOrNull() != null) onChange(raw) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155), textAlign = TextAlign.End),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { inner() }
                },
            )
            Column(
                modifier = Modifier
                    .width(22.dp)
                    .fillMaxHeight()
                    .border(1.dp, Color(0xFFE2E8F0)),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            val current = value.toDoubleOrNull() ?: minValue
                            onChange(formatProcessNumField(kotlin.math.min(maxValue, current + step), 2))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
                }
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            val current = value.toDoubleOrNull() ?: minValue
                            onChange(formatProcessNumField(kotlin.math.max(minValue, current - step), 2))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
                }
            }
        }
    }
}

private fun formatProcessNumField(value: Double, decimals: Int): String =
    if (decimals <= 0) value.toInt().toString()
    else String.format(Locale.JAPAN, "%.${decimals}f", value)
