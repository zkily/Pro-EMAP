package com.example.smart_emap.ui.master.productmachineconfig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProductMachineConfigFormDialog(
    row: ProductMachineConfigUiRow,
    isEdit: Boolean,
    loading: Boolean,
    productOptions: List<Pair<String, String>>,
    machineOptions: List<MachineOption>,
    onUpdate: ((ProductMachineConfigUiRow) -> ProductMachineConfigUiRow) -> Unit,
    onProductSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var section by remember { mutableIntStateOf(0) }
    val sections = listOf("製品", "内部", "外注")
    Dialog(onDismissRequest = { if (!loading) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val layout = pmcLayoutMode(maxWidth)
            val dialogWidth = when (layout) {
                PmcLayoutMode.Compact -> 0.98f
                PmcLayoutMode.Medium -> 0.92f
                PmcLayoutMode.Wide -> 0.72f
            }
            val maxDialogWidth = when (layout) {
                PmcLayoutMode.Compact -> 9999.dp
                PmcLayoutMode.Medium -> 520.dp
                PmcLayoutMode.Wide -> 640.dp
            }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.widthIn(max = maxDialogWidth).fillMaxWidth(dialogWidth).shadow(14.dp, RoundedCornerShape(16.dp)),
        ) {
            Column {
                Box(Modifier.fillMaxWidth().background(PmcTheme.HeroBg).padding(horizontal = 12.dp, vertical = 9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Column {
                            Text(
                                if (isEdit) "機器設定編集" else "機器設定新規登録",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                            )
                            if (isEdit) {
                                Text("${row.productCd} · ${row.productName}", fontSize = 9.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sections.forEachIndexed { i, label ->
                        val selected = section == i
                        Surface(
                            onClick = { section = i },
                            shape = PmcTheme.ChipShape,
                            color = if (selected) PmcTheme.Indigo600 else Color.White,
                            border = BorderStroke(1.dp, if (selected) PmcTheme.Indigo600 else PmcTheme.Slate200),
                        ) {
                            Text(label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else PmcTheme.Slate600)
                        }
                    }
                }
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp).verticalScroll(rememberScrollState())) {
                    AnimatedContent(
                        targetState = section,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "pmc-form-section",
                    ) { tab ->
                        when (tab) {
                            0 -> PmcFormSection("製品情報", Icons.Default.Inventory2) {
                                if (isEdit) {
                                    PmcReadonlyField("製品コード", row.productCd)
                                    PmcReadonlyField("製品名", row.productName)
                                } else {
                                    PmcSearchDropdown("製品コード", row.productCd, productOptions, "製品を選択", onProductSelected)
                                    PmcReadonlyField("製品名", row.productName.ifBlank { "—" })
                                }
                            }
                            1 -> PmcFormSection("内部工程機器", Icons.Default.Build) {
                                if (layout == PmcLayoutMode.Wide) {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        PmcMachineDropdownWide("切断機", row.cuttingMachine, machineOptions) { v -> onUpdate { it.copy(cuttingMachine = v) } }
                                        PmcMachineDropdownWide("面取機", row.chamferingMachine, machineOptions) { v -> onUpdate { it.copy(chamferingMachine = v) } }
                                        PmcMachineDropdownWide("sw機", row.swMachine, machineOptions) { v -> onUpdate { it.copy(swMachine = v) } }
                                        PmcMachineDropdownWide("成型機", row.moldingMachine, machineOptions) { v -> onUpdate { it.copy(moldingMachine = v) } }
                                        PmcMachineDropdownWide("メッキ治具", row.platingMachine, machineOptions) { v -> onUpdate { it.copy(platingMachine = v) } }
                                        PmcMachineDropdownWide("溶接機", row.weldingMachine, machineOptions) { v -> onUpdate { it.copy(weldingMachine = v) } }
                                        PmcMachineDropdownWide("検査員", row.inspectorMachine, machineOptions) { v -> onUpdate { it.copy(inspectorMachine = v) } }
                                    }
                                } else {
                                    PmcMachineDropdown("切断機", row.cuttingMachine, machineOptions) { v -> onUpdate { it.copy(cuttingMachine = v) } }
                                    PmcMachineDropdown("面取機", row.chamferingMachine, machineOptions) { v -> onUpdate { it.copy(chamferingMachine = v) } }
                                    PmcMachineDropdown("sw機", row.swMachine, machineOptions) { v -> onUpdate { it.copy(swMachine = v) } }
                                    PmcMachineDropdown("成型機", row.moldingMachine, machineOptions) { v -> onUpdate { it.copy(moldingMachine = v) } }
                                    PmcMachineDropdown("メッキ治具", row.platingMachine, machineOptions) { v -> onUpdate { it.copy(platingMachine = v) } }
                                    PmcMachineDropdown("溶接機", row.weldingMachine, machineOptions) { v -> onUpdate { it.copy(weldingMachine = v) } }
                                    PmcMachineDropdown("検査員", row.inspectorMachine, machineOptions) { v -> onUpdate { it.copy(inspectorMachine = v) } }
                                }
                            }
                            else -> PmcFormSection("外注工程", Icons.Default.Apartment) {
                                if (layout == PmcLayoutMode.Wide) {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        PmcMachineDropdownWide("外注メッキ先", row.outsourcedPlatingMachine, machineOptions) { v -> onUpdate { it.copy(outsourcedPlatingMachine = v) } }
                                        PmcMachineDropdownWide("外注溶接先", row.outsourcedWeldingMachine, machineOptions) { v -> onUpdate { it.copy(outsourcedWeldingMachine = v) } }
                                    }
                                } else {
                                    PmcMachineDropdown("外注メッキ先", row.outsourcedPlatingMachine, machineOptions) { v -> onUpdate { it.copy(outsourcedPlatingMachine = v) } }
                                    PmcMachineDropdown("外注溶接先", row.outsourcedWeldingMachine, machineOptions) { v -> onUpdate { it.copy(outsourcedWeldingMachine = v) } }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = PmcTheme.Slate200)
                Row(Modifier.fillMaxWidth().background(Color(0xFFFAFBFC)).padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル", fontSize = 12.sp) }
                    Spacer(Modifier.size(6.dp))
                    PmcSaveButton(loading, onConfirm)
                }
            }
        }
        }
    }
}

@Composable
private fun PmcSaveButton(loading: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(80), label = "save")
    Button(
        onClick = onClick,
        enabled = !loading,
        interactionSource = interaction,
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = PmcTheme.Indigo600),
        shape = RoundedCornerShape(8.dp),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = Color.White)
        else Text("保存", fontSize = 12.sp)
    }
}

@Composable
private fun PmcFormSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Surface(shape = PmcTheme.PanelShape, color = Color.White, border = BorderStroke(1.dp, PmcTheme.Slate200)) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(icon, null, tint = PmcTheme.Indigo500, modifier = Modifier.size(14.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PmcTheme.Slate600)
            }
            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = Color(0x2E6366F1))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
        }
    }
}

@Composable
private fun PmcReadonlyField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 11.sp, color = PmcTheme.Slate600, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = PmcTheme.Slate700, disabledBorderColor = PmcTheme.Slate200, disabledContainerColor = PmcTheme.Slate50),
            enabled = false,
        )
    }
}

@Composable
private fun PmcMachineDropdownWide(
    label: String,
    value: String,
    options: List<MachineOption>,
    onChange: (String) -> Unit,
) {
    Box(Modifier.widthIn(min = 240.dp, max = 300.dp)) {
        PmcMachineDropdown(label, value, options, onChange)
    }
}

@Composable
private fun PmcMachineDropdown(label: String, value: String, options: List<MachineOption>, onChange: (String) -> Unit) {
    PmcSearchDropdown(label, value, options.map { it.value to it.label }, "選択", onChange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PmcSearchDropdown(label: String, value: String, options: List<Pair<String, String>>, placeholder: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val display = options.find { it.first == value }?.second ?: placeholder
    val filtered = remember(search, options) {
        if (search.isBlank()) options else options.filter { (_, text) -> text.lowercase().contains(search.lowercase()) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, fontSize = 11.sp, color = PmcTheme.Slate600, fontWeight = FontWeight.Medium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it; if (!it) search = "" }) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(36.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = PmcTheme.FieldShape,
                border = BorderStroke(1.dp, if (expanded) PmcTheme.Indigo500 else PmcTheme.Slate200),
                color = Color.White,
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(display, fontSize = 11.sp, color = if (value.isBlank()) PmcTheme.Slate400 else PmcTheme.Slate700, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; search = "" }, modifier = Modifier.widthIn(min = 260.dp)) {
                DropdownMenuItem(
                    text = {
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            decorationBox = { inner ->
                                if (search.isEmpty()) Text("検索...", fontSize = 11.sp, color = PmcTheme.Slate400)
                                inner()
                            },
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                DropdownMenuItem(text = { Text("（クリア）", fontSize = 11.sp, color = PmcTheme.Slate500) }, onClick = { onChange(""); expanded = false; search = "" })
                filtered.forEach { (v, text) ->
                    DropdownMenuItem(
                        text = { Text(text, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        onClick = { onChange(v); expanded = false; search = "" },
                    )
                }
            }
        }
    }
}
