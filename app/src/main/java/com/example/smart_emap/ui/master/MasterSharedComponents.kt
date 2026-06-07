package com.example.smart_emap.ui.master

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground

@Composable
fun MasterHeroBar(
    title: String,
    subtitle: String,
    icon: ImageVector,
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                    Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
            if (stats.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    stats.forEach { (label, value) ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF6366F1))
                                Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterFilterBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    onAdd: (() -> Unit)?,
    secondaryLabel: String? = null,
    secondaryValue: String = "",
    secondaryOptions: List<Pair<String, String>> = emptyList(),
    onSecondaryChange: (String) -> Unit = {},
    loading: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("検索・フィルター", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF475569))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("キーワード", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                )
                if (secondaryLabel != null && secondaryOptions.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = secondaryOptions.find { it.first == secondaryValue }?.second ?: secondaryLabel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .width(100.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            secondaryOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 12.sp) },
                                    onClick = { onSecondaryChange(value); expanded = false },
                                )
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onSearch,
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier.height(36.dp),
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("検索", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onReset, enabled = !loading, modifier = Modifier.height(36.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("リセット", fontSize = 12.sp)
                }
                if (onAdd != null) {
                    Button(
                        onClick = onAdd,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("新規追加", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MasterDataTable(
    columns: List<String>,
    columnWidths: List<Int>,
    rows: List<MasterTableRow>,
    onEdit: (MasterTableRow) -> Unit,
    onDelete: (MasterTableRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9D5FF)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .horizontalScroll(scroll)
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            ) {
                columns.forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier.width((columnWidths.getOrElse(i) { 64 }).dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), maxLines = 1)
                    }
                }
                Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                    Text("操作", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            if (rows.isEmpty()) {
                Text(
                    "データがありません",
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                )
            } else {
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) Color.White else Color(0xFFFAFAFA))
                            .horizontalScroll(scroll)
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.cells.forEachIndexed { i, cell ->
                            Box(
                                modifier = Modifier.width((columnWidths.getOrElse(i) { 64 }).dp).padding(horizontal = 2.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    cell?.take(20).orEmpty().ifEmpty { "—" },
                                    fontSize = 10.sp,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(modifier = Modifier.width(72.dp), horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { onEdit(row) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "編集", tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
fun MasterFormDialog(
    title: String,
    fields: List<MasterFormField>,
    values: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    loading: Boolean = false,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.94f)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                fields.forEach { field ->
                    Text(field.label + if (field.required) " *" else "", fontSize = 11.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = values[field.key].orEmpty(),
                        onValueChange = { onValueChange(field.key, it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        singleLine = field.key != "note" && field.key != "remarks" && field.key != "remark",
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, enabled = !loading, modifier = Modifier.weight(1f)) {
                        Text("キャンセル")
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun MasterPageScaffold(content: @Composable () -> Unit) {
    PurchasePageBackground { content() }
}

@Composable
fun MasterLoadingBox(visible: Boolean) {
    if (visible) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6366F1))
        }
    }
}
