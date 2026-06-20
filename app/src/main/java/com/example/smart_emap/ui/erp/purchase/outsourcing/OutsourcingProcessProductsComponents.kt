package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OutsourcingProcessProductDto

private val FilterAccent = Color(0xFF667EEA)

@Composable
fun OutsourcingProcessTabsRow(
    selected: OutsourcingProcessTab,
    counts: (OutsourcingProcessTab) -> Int,
    onSelect: (OutsourcingProcessTab) -> Unit,
) {
    val scroll = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutsourcingProcessTab.entries.forEach { tab ->
                val active = tab == selected
                Surface(
                    modifier = Modifier.clickable { onSelect(tab) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (active) Color(0xFF667EEA) else Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            tab.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Color.White else Color(0xFF475569),
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color.White.copy(alpha = 0.25f) else Color(0x14667EEA),
                        ) {
                            Text(
                                counts(tab).toString(),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else Color(0xFF667EEA),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutsourcingProcessProductsFilterSection(
    supplierCd: String,
    keyword: String,
    isActiveFilter: String,
    supplierOptions: List<Pair<String, String>>,
    onSupplierChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    onActiveChange: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProcessFilterGroup("外注先", Icons.Default.Business) {
                ProcessCompactDropdown(
                    value = supplierCd,
                    options = listOf("" to "すべて") + supplierOptions.map { (cd, name) -> cd to "$cd $name".trim() },
                    width = 180.dp,
                    onSelect = onSupplierChange,
                )
            }
            ProcessFilterDivider()
            ProcessFilterGroup("キーワード", Icons.Default.Search) {
                ProcessKeywordField(keyword, onKeywordChange)
            }
            ProcessFilterDivider()
            ProcessFilterGroup("状態", Icons.Default.CheckCircle) {
                ProcessCompactDropdown(
                    value = isActiveFilter,
                    options = listOf("all" to "すべて", "true" to "有効", "false" to "無効"),
                    width = 88.dp,
                    onSelect = onActiveChange,
                )
            }
        }
    }
}

@Composable
fun OutsourcingProcessProductsActionSection(actionLoading: Boolean, onCreate: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981),
                modifier = Modifier.clickable(enabled = !actionLoading, onClick = onCreate),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (actionLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("新規登録", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingProcessProductsTablePanel(
    isLoading: Boolean,
    items: List<OutsourcingProcessProductDto>,
    onEdit: (OutsourcingProcessProductDto) -> Unit,
    onToggle: (OutsourcingProcessProductDto) -> Unit,
    onDelete: (OutsourcingProcessProductDto) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.97f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        when {
            isLoading && items.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = FilterAccent)
                }
            }
            items.isEmpty() -> {
                Text(
                    "データがありません",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                )
            }
            else -> {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.width(1180.dp)) {
                        OutsourcingProcessProductsTable(items, onEdit, onToggle, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingProcessProductsTable(
    items: List<OutsourcingProcessProductDto>,
    onEdit: (OutsourcingProcessProductDto) -> Unit,
    onToggle: (OutsourcingProcessProductDto) -> Unit,
    onDelete: (OutsourcingProcessProductDto) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("工程", "外注先CD", "外注先名", "製品CD", "品名", "規格", "単価", "納入LT", "納入場所", "区分", "内容", "状態", "操作").forEach { h ->
                Text(
                    h,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.weight(
                        when (h) {
                            "品名", "外注先名", "区分" -> 1.1f
                            "納入場所" -> 1.0f
                            "操作" -> 1.2f
                            else -> 0.8f
                        },
                    ),
                    maxLines = 1,
                )
            }
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        items.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(bg)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(modifier = Modifier.weight(0.8f)) {
                    OutsourcingTag(
                        text = row.processTypeName ?: outsourcingProcessTypeLabel(row.processType),
                        containerColor = outsourcingProcessTypeColor(row.processType),
                    )
                }
                Text(row.supplierCd.orEmpty(), fontSize = 9.sp, modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.supplierName.orEmpty(), fontSize = 9.sp, modifier = Modifier.weight(1.1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.productCd.orEmpty(), fontSize = 9.sp, modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.productName.orEmpty(), fontSize = 9.sp, modifier = Modifier.weight(1.1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.specification.orEmpty().ifBlank { "—" }, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatOutsourcingPrice(row.unitPrice), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857), modifier = Modifier.weight(0.8f))
                Text("${row.deliveryLeadTime ?: "-"}日", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB), modifier = Modifier.weight(0.8f))
                Text(row.deliveryLocation.orEmpty().ifBlank { "—" }, fontSize = 9.sp, modifier = Modifier.weight(1.0f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.category.orEmpty().ifBlank { "—" }, fontSize = 9.sp, modifier = Modifier.weight(1.1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.content.orEmpty().ifBlank { "—" }, fontSize = 9.sp, modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(modifier = Modifier.weight(0.8f)) {
                    OutsourcingTag(
                        text = if (row.isActive == true) "有効" else "無効",
                        containerColor = if (row.isActive == true) Color(0xFF67C23A) else Color(0xFF909399),
                    )
                }
                Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    TextButton(onClick = { onEdit(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("編集", fontSize = 9.sp, color = Color(0xFF667EEA))
                    }
                    TextButton(onClick = { onToggle(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text(if (row.isActive == true) "無効" else "有効", fontSize = 9.sp, color = if (row.isActive == true) Color(0xFFE6A23C) else Color(0xFF67C23A))
                    }
                    TextButton(onClick = { onDelete(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("削除", fontSize = 9.sp, color = Color(0xFFF56C6C))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun ProcessFilterGroup(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = FilterAccent, modifier = Modifier.size(14.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FilterAccent)
        }
        content()
    }
}

@Composable
private fun ProcessFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFFE2E8F0)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcessCompactDropdown(
    value: String,
    options: List<Pair<String, String>>,
    width: androidx.compose.ui.unit.Dp,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "すべて" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier
                .width(width)
                .height(32.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0x33667EEA)),
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(display, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = { onSelect(code); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ProcessKeywordField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(32.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0x33667EEA)),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
                cursorBrush = SolidColor(FilterAccent),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("品番・品名", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    inner()
                },
            )
        }
    }
}
