package com.example.smart_emap.ui.master.supplier

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val supplierFormPrimary = Color(0xFFDB2777)
private val supplierFormControlHeight = 32.dp
private val supplierFormFieldGap = 6.dp

@Composable
fun SupplierMasterFormDialog(
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
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFDF2F8), Color.White))),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 44.dp, top = 14.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("🔧", fontSize = 20.sp)
                        Text(
                            if (isEdit) "仕入先編集" else "仕入先追加",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFF909399), modifier = Modifier.size(18.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEBEEF5)),
                    )
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SupplierFormSection("基本情報") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupplierFormTextField("仕入先CD", values["supplier_cd"].orEmpty(), { if (it.length <= 20) onValueChange("supplier_cd", it) }, modifier = Modifier.weight(1f), required = true, enabled = !isEdit, maxLength = 20)
                            SupplierFormTextField("仕入先カナ", values["supplier_kana"].orEmpty(), { if (it.length <= 100) onValueChange("supplier_kana", it) }, modifier = Modifier.weight(1f), maxLength = 100)
                        }
                        SupplierFormTextField("仕入先名", values["supplier_name"].orEmpty(), { if (it.length <= 100) onValueChange("supplier_name", it) }, required = true, maxLength = 100)
                    }
                    SupplierFormSection("連絡先") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupplierFormTextField("担当者", values["contact_person"].orEmpty(), { if (it.length <= 100) onValueChange("contact_person", it) }, modifier = Modifier.weight(1f), maxLength = 100)
                            SupplierFormTextField("電話番号", values["phone"].orEmpty(), { if (it.length <= 20) onValueChange("phone", it) }, modifier = Modifier.weight(1f), maxLength = 20)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupplierFormTextField("FAX番号", values["fax"].orEmpty(), { if (it.length <= 20) onValueChange("fax", it) }, modifier = Modifier.weight(1f), maxLength = 20)
                            SupplierFormTextField("メールアドレス", values["email"].orEmpty(), { if (it.length <= 100) onValueChange("email", it) }, modifier = Modifier.weight(1f), maxLength = 100)
                        }
                    }
                    SupplierFormSection("住所") {
                        SupplierFormTextField("郵便番号", values["postal_code"].orEmpty(), { if (it.length <= 10) onValueChange("postal_code", it) }, maxLength = 10, modifier = Modifier.fillMaxWidth(0.5f))
                        SupplierFormTextField("住所1", values["address1"].orEmpty(), { if (it.length <= 200) onValueChange("address1", it) }, maxLength = 200)
                        SupplierFormTextField("住所2", values["address2"].orEmpty(), { if (it.length <= 200) onValueChange("address2", it) }, maxLength = 200)
                    }
                    SupplierFormSection("支払・通貨") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupplierFormTextField("支払条件", values["payment_terms"].orEmpty(), { if (it.length <= 50) onValueChange("payment_terms", it) }, modifier = Modifier.weight(1f), maxLength = 50)
                            SupplierFormTextField("通貨", values["currency"].orEmpty(), { if (it.length <= 10) onValueChange("currency", it) }, modifier = Modifier.weight(1f), placeholder = "例：JPY, USD", maxLength = 10)
                        }
                    }
                    SupplierFormSection("備考") {
                        SupplierFormTextArea(values["remarks"].orEmpty(), { if (it.length <= 500) onValueChange("remarks", it) }, maxLength = 500)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .border(1.dp, Color(0xFFEBEEF5))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !loading,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDFE6)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF606266)),
                    ) {
                        Text("キャンセル", fontSize = 13.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = supplierFormPrimary),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("保存", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierFormSection(
    title: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(supplierFormFieldGap),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = supplierFormPrimary)
            Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(1.dp).background(Color(0xFFEAEAEA)))
        }
        content()
    }
}

@Composable
private fun SupplierFormFieldLabel(label: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 2.dp)) {
        if (required) Text("* ", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), lineHeight = 13.sp)
    }
}

@Composable
private fun SupplierFormTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    required: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
    maxLength: Int? = null,
) {
    Column(modifier = modifier) {
        SupplierFormFieldLabel(label, required)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontSize = 12.sp, color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8)),
            modifier = Modifier
                .fillMaxWidth()
                .height(supplierFormControlHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) Color.White else Color(0xFFF5F7FA))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            decorationBox = { inner ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                    if (maxLength != null) {
                        Text("${value.length}/$maxLength", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    }
                }
            },
        )
    }
}

@Composable
private fun SupplierFormTextArea(value: String, onChange: (String) -> Unit, maxLength: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155), lineHeight = 16.sp),
        )
        Text(
            "${value.length}/$maxLength",
            fontSize = 9.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
        )
    }
}
