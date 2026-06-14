package com.example.smart_emap.ui.master.equipmentefficiency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentEfficiencyFormDialog(
    row: EquipmentEfficiencyUiRow,
    isEdit: Boolean,
    loading: Boolean,
    machineOptions: List<EeMachineOption>,
    productOptions: List<Pair<String, String>>,
    onUpdate: ((EquipmentEfficiencyUiRow) -> EquipmentEfficiencyUiRow) -> Unit,
    onMachineSelected: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val layout = eeLayoutMode(maxWidth)
            val dialogWidth = when (layout) {
                EeLayoutMode.Compact -> 0.98f
                EeLayoutMode.Medium -> 0.92f
                EeLayoutMode.Wide -> 0.72f
            }
            val maxDialogWidth = when (layout) {
                EeLayoutMode.Compact -> 9999.dp
                EeLayoutMode.Medium -> 520.dp
                EeLayoutMode.Wide -> 580.dp
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.widthIn(max = maxDialogWidth).fillMaxWidth(dialogWidth).shadow(14.dp, RoundedCornerShape(16.dp)),
            ) {
                Column {
                    Box(Modifier.fillMaxWidth().background(EeTheme.HeroBg).padding(horizontal = 12.dp, vertical = 9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Column {
                                Text(
                                    if (isEdit) "能率設定編集" else "能率設定新規登録",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                )
                                if (isEdit) {
                                    Text(
                                        "${row.machineCd} · ${row.productCd}",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        EeFormSectionTitle("設備・製品")
                        EeMachineDropdown(row.machineCd, machineOptions, enabled = !loading && !isEdit, onMachineSelected)
                        EeReadonlyField("設備名", row.machinesName)
                        EeProductDropdown(row.productCd, productOptions, enabled = !loading && !isEdit, onProductSelected)
                        EeReadonlyField("製品名", row.productName)
                        HorizontalDivider(color = EeTheme.Slate200)
                        EeFormSectionTitle("能率設定")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EeNumberField(
                                label = "能率",
                                value = row.efficiencyRate,
                                enabled = !loading,
                                modifier = Modifier.weight(1f),
                                onChange = { v -> onUpdate { r -> r.copy(efficiencyRate = v) } },
                            )
                            EeIntField(
                                label = "段取時間(分)",
                                value = row.stepTime,
                                enabled = !loading,
                                modifier = Modifier.weight(1f),
                                onChange = { v -> onUpdate { r -> r.copy(stepTime = v) } },
                            )
                        }
                        EeStatusRadio(row.status, !loading) { v -> onUpdate { r -> r.copy(status = v) } }
                        EeRemarksField(row.remarks, !loading) { v -> onUpdate { r -> r.copy(remarks = v) } }
                    }
                    HorizontalDivider(color = EeTheme.Slate200)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", fontSize = 12.sp) }
                        Button(
                            onClick = onConfirm,
                            enabled = !loading,
                            colors = ButtonDefaults.buttonColors(containerColor = EeTheme.Violet600),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            if (loading) {
                                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("保存", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EeFormSectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.Build, null, tint = EeTheme.Violet600, modifier = Modifier.size(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EeTheme.Slate700)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EeMachineDropdown(
    selected: String,
    options: List<EeMachineOption>,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.value == selected }?.label ?: selected
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("設備", fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EeTheme.Violet600),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label, fontSize = 11.sp) },
                    onClick = {
                        expanded = false
                        onSelected(opt.value)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EeProductDropdown(
    selected: String,
    options: List<Pair<String, String>>,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.first == selected }?.second ?: selected
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("製品", fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EeTheme.Violet600),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 11.sp) },
                    onClick = {
                        expanded = false
                        onSelected(cd)
                    },
                )
            }
        }
    }
}

@Composable
private fun EeReadonlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label, fontSize = 10.sp) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
    )
}

@Composable
private fun EeNumberField(
    label: String,
    value: Double,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Double) -> Unit,
) {
    var text by remember(value) { mutableStateOf(if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            input.toDoubleOrNull()?.let(onChange)
        },
        enabled = enabled,
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EeTheme.Violet600),
    )
}

@Composable
private fun EeIntField(
    label: String,
    value: Int?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Int?) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value?.toString().orEmpty()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input.filter { it.isDigit() }
            onChange(text.toIntOrNull())
        },
        enabled = enabled,
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EeTheme.Violet600),
    )
}

@Composable
private fun EeStatusRadio(status: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("状態", fontSize = 10.sp, color = EeTheme.Slate600, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = status == 1, onClick = { if (enabled) onChange(1) }, enabled = enabled, colors = RadioButtonDefaults.colors(selectedColor = EeTheme.Violet600))
                Text("有効", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = status == 0, onClick = { if (enabled) onChange(0) }, enabled = enabled, colors = RadioButtonDefaults.colors(selectedColor = EeTheme.Violet600))
                Text("無効", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EeRemarksField(value: String, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        label = { Text("備考", fontSize = 10.sp) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EeTheme.Violet600),
    )
}
