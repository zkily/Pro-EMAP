package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.MasterProductItemDto

@Composable
fun OutsourcingProcessProductsFormDialog(
    visible: Boolean,
    isEdit: Boolean,
    form: OutsourcingProcessProductFormUi,
    supplierOptions: List<Pair<String, String>>,
    productOptions: List<MasterProductItemDto>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onFormChange: ((OutsourcingProcessProductFormUi) -> OutsourcingProcessProductFormUi) -> Unit,
    onSupplierSelected: (String) -> Unit,
    onProductSelected: (String) -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .shadow(12.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OutsourcingAccentGradient)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isEdit) "外注工程製品編集" else "外注工程製品登録",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                        )
                        Text(
                            "外注工程の製品情報を${if (isEdit) "編集" else "登録"}します",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.92f),
                        )
                    }
                    OutsourcingTag(
                        text = if (isEdit) "編集モード" else "新規登録",
                        containerColor = if (isEdit) Color(0xFFE6A23C) else Color(0xFF67C23A),
                        contentColor = Color.White,
                    )
                }

                Column(
                    modifier = Modifier
                        .background(Color(0xFFF8FAFC))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProcessTypeSelectCard(
                        value = form.processType,
                        enabled = !isEdit,
                        onSelect = { onFormChange { f -> f.copy(processType = it) } },
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ProcessFormCard("基本情報", Icons.Default.GridView, Modifier.weight(1f)) {
                            ProcessSupplierDropdown(form.supplierCd, supplierOptions, enabled = !isEdit, onSupplierSelected)
                            Spacer(Modifier.height(8.dp))
                            ProcessTextField("外注先名", form.supplierName) { onFormChange { f -> f.copy(supplierName = it) } }
                            Spacer(Modifier.height(8.dp))
                            ProcessProductDropdown(form.productCd, productOptions, enabled = !isEdit, onProductSelected)
                            Spacer(Modifier.height(8.dp))
                            ProcessTextField("品名", form.productName) { onFormChange { f -> f.copy(productName = it) } }
                            Spacer(Modifier.height(8.dp))
                            ProcessTextField("規格", form.specification) { onFormChange { f -> f.copy(specification = it) } }
                        }
                        ProcessFormCard("取引情報", Icons.Default.Business, Modifier.weight(1f)) {
                            ProcessNumberField("単価", form.unitPrice, decimals = true) {
                                onFormChange { f -> f.copy(unitPrice = it) }
                            }
                            Spacer(Modifier.height(8.dp))
                            ProcessIntField("リードタイム", form.deliveryLeadTime) {
                                onFormChange { f -> f.copy(deliveryLeadTime = it) }
                            }
                            Spacer(Modifier.height(8.dp))
                            ProcessStringDropdown("納入場所", form.deliveryLocation, OUTSOURCING_DELIVERY_LOCATIONS) {
                                onFormChange { f -> f.copy(deliveryLocation = it) }
                            }
                            Spacer(Modifier.height(8.dp))
                            ProcessStringDropdown("区分", form.category, OUTSOURCING_CATEGORIES) {
                                onFormChange { f -> f.copy(category = it) }
                            }
                        }
                    }

                    ProcessFormCard("その他情報", Icons.Default.GridView, Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            ProcessStringDropdown("内容", form.content, OUTSOURCING_CONTENTS, Modifier.weight(1f)) {
                                onFormChange { f -> f.copy(content = it) }
                            }
                            ProcessTextField("備考", form.remarks, Modifier.weight(1f)) {
                                onFormChange { f -> f.copy(remarks = it) }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSubmit,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isEdit) "更新" else "登録", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessTypeSelectCard(value: String, enabled: Boolean, onSelect: (String) -> Unit) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = OUTSOURCING_PROCESS_FORM_TYPES.find { it.key == value }?.label ?: "工程種別を選択してください"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OutsourcingAccentGradient)
                .padding(10.dp),
        ) {
            Column {
                Text("工程種別選択", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }) {
                    Box(
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(display, fontSize = 12.sp, color = Color(0xFF334155))
                    }
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        OUTSOURCING_PROCESS_FORM_TYPES.forEach { tab ->
                            DropdownMenuItem(
                                text = { Text(tab.label, fontSize = 12.sp) },
                                onClick = {
                                    onSelect(tab.key)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessFormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1E293B))
            }
            Column(modifier = Modifier.padding(10.dp)) { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessSupplierDropdown(
    value: String,
    options: List<Pair<String, String>>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = options.find { it.first == value }?.let { "${it.first} - ${it.second}" } ?: "外注先を選択"
    ProcessFieldLabel("外注先")
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }) {
        Box(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(if (enabled) Color.White else Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
        ) {
            Text(display, fontSize = 11.sp)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text("$cd - $name", fontSize = 11.sp) },
                    onClick = { onSelect(cd); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessProductDropdown(
    value: String,
    options: List<MasterProductItemDto>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = options.find { it.productCd == value }?.let { "${it.productCd} - ${it.productName}" } ?: "製品を選択"
    ProcessFieldLabel("製品CD")
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }) {
        Box(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(if (enabled) Color.White else Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
        ) {
            Text(display, fontSize = 11.sp, maxLines = 1)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { product ->
                DropdownMenuItem(
                    text = { Text("${product.productCd} - ${product.productName}", fontSize = 11.sp) },
                    onClick = { onSelect(product.productCd.orEmpty()); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessStringDropdown(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(value) { mutableStateOf(false) }
    ProcessFieldLabel(label, modifier)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Box(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
        ) {
            Text(value.ifBlank { "選択" }, fontSize = 11.sp)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 11.sp) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ProcessFieldLabel(label: String, modifier: Modifier = Modifier) {
    Text(label, modifier = modifier.padding(bottom = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
}

@Composable
private fun ProcessTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    ProcessFieldLabel(label, modifier)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = label != "備考",
        textStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (label == "備考") 48.dp else 32.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
    )
}

@Composable
private fun ProcessNumberField(label: String, value: Double, decimals: Boolean, onChange: (Double) -> Unit) {
    ProcessFieldLabel(label)
    BasicTextField(
        value = if (decimals) "%.2f".format(value) else value.toInt().toString(),
        onValueChange = { raw ->
            val parsed = raw.toDoubleOrNull() ?: 0.0
            onChange(if (parsed < 0) 0.0 else parsed)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = TextStyle(fontSize = 11.sp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
    )
}

@Composable
private fun ProcessIntField(label: String, value: Int, onChange: (Int) -> Unit) {
    ProcessFieldLabel(label)
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value.toString(),
            onValueChange = { raw ->
                val n = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                onChange(n.coerceIn(0, 365))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(fontSize = 11.sp),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 7.dp),
        )
        Spacer(Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF667EEA)) {
            Text("日", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
