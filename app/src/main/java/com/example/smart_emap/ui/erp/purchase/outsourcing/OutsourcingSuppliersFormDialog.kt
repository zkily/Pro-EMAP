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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun OutsourcingSuppliersFormDialog(
    visible: Boolean,
    isEdit: Boolean,
    form: OutsourcingSupplierFormUi,
    duplicateCodeError: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onFormChange: ((OutsourcingSupplierFormUi) -> OutsourcingSupplierFormUi) -> Unit,
    onCodeBlur: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(16.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OutsourcingAccentGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isEdit) "外注先編集" else "外注先登録",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                        )
                        Text(
                            if (isEdit) "情報を更新します" else "新しい外注先を登録します",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isEdit) Color(0x40FFC850) else Color.White.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            if (isEdit) "編集" else "新規",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SupplierFormGroup("基本情報") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            SupplierFormField(
                                label = "外注先コード",
                                value = form.supplierCd,
                                onValueChange = { onFormChange { f -> f.copy(supplierCd = it) } },
                                enabled = !isEdit,
                                onBlur = onCodeBlur,
                                error = duplicateCodeError,
                                modifier = Modifier.weight(1f),
                            )
                            SupplierFormField(
                                label = "外注先名",
                                value = form.supplierName,
                                onValueChange = { onFormChange { f -> f.copy(supplierName = it) } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            SupplierTypeDropdown(
                                value = form.supplierType,
                                onSelect = { onFormChange { f -> f.copy(supplierType = it) } },
                                modifier = Modifier.weight(1f),
                            )
                            SupplierLeadTimeField(
                                value = form.leadTimeDays,
                                onChange = { onFormChange { f -> f.copy(leadTimeDays = it) } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    SupplierFormGroup("連絡先情報") {
                        SupplierFormField(
                            label = "住所",
                            value = form.address,
                            onValueChange = { onFormChange { f -> f.copy(address = it) } },
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            SupplierFormField(
                                label = "電話番号",
                                value = form.phone,
                                onValueChange = { onFormChange { f -> f.copy(phone = it) } },
                                modifier = Modifier.weight(1f),
                            )
                            SupplierFormField(
                                label = "FAX番号",
                                value = form.fax,
                                onValueChange = { onFormChange { f -> f.copy(fax = it) } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            SupplierFormField(
                                label = "担当者",
                                value = form.contactPerson,
                                onValueChange = { onFormChange { f -> f.copy(contactPerson = it) } },
                                modifier = Modifier.weight(1f),
                            )
                            SupplierFormField(
                                label = "メール",
                                value = form.email,
                                onValueChange = { onFormChange { f -> f.copy(email = it) } },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    SupplierFormGroup("その他情報", last = true) {
                        SupplierFormField(
                            label = "支払条件",
                            value = form.paymentTerms,
                            onValueChange = { onFormChange { f -> f.copy(paymentTerms = it) } },
                        )
                        Spacer(Modifier.height(8.dp))
                        SupplierFormField(
                            label = "備考",
                            value = form.remarks,
                            onValueChange = { onFormChange { f -> f.copy(remarks = it) } },
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !loading) {
                        Text("キャンセル", fontSize = 12.sp)
                    }
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
                            Text(if (isEdit) "更新" else "登録", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierFormGroup(title: String, last: Boolean = false, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!last) Modifier.padding(bottom = 4.dp) else Modifier),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(13.dp)
                    .background(OutsourcingAccentGradient, RoundedCornerShape(2.dp)),
            )
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667EEA))
        }
        Spacer(Modifier.height(8.dp))
        content()
        if (!last) {
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F2F5)))
        }
    }
}

@Composable
private fun SupplierFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    onBlur: (() -> Unit)? = null,
    error: String = "",
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(fontSize = 12.sp, color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (singleLine) 34.dp else (minLines * 22).dp)
                .border(
                    1.dp,
                    if (error.isNotBlank()) Color(0xFFF56C6C) else Color(0xFFDCDEE6),
                    RoundedCornerShape(6.dp),
                )
                .background(if (enabled) Color.White else Color(0xFFF5F7FA), RoundedCornerShape(6.dp))
                .padding(horizontal = 9.dp, vertical = 8.dp)
                .then(
                    if (onBlur != null) {
                        Modifier.onFocusChanged { if (!it.isFocused) onBlur() }
                    } else {
                        Modifier
                    },
                ),
        )
        if (error.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFF56C6C), modifier = Modifier.size(12.dp))
                Text(error, fontSize = 10.sp, color = Color(0xFFF56C6C))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierTypeDropdown(value: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = OUTSOURCING_SUPPLIER_FORM_TYPE_OPTIONS.find { it.first == value }?.second ?: "選択"
    Column(modifier = modifier) {
        Text("外注種別", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Box(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFDCDEE6), RoundedCornerShape(6.dp))
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 8.dp),
            ) {
                Text(display, fontSize = 12.sp)
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                OUTSOURCING_SUPPLIER_FORM_TYPE_OPTIONS.forEach { (code, text) ->
                    DropdownMenuItem(
                        text = { Text(text, fontSize = 12.sp) },
                        onClick = {
                            onSelect(code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplierLeadTimeField(value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("リードタイム", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicTextField(
                value = value.coerceIn(1, 90).toString(),
                onValueChange = { raw ->
                    val n = raw.filter { it.isDigit() }.toIntOrNull() ?: 1
                    onChange(n.coerceIn(1, 90))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 12.sp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFDCDEE6), RoundedCornerShape(6.dp))
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 8.dp),
            )
            Text("日", fontSize = 12.sp, color = Color(0xFF888888))
        }
    }
}
