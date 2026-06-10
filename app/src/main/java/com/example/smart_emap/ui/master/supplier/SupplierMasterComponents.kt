package com.example.smart_emap.ui.master.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterSupplierDto
import kotlin.math.max
import kotlin.math.min

val supplierPageBackground = Brush.linearGradient(listOf(Color(0xFFF8F5FF), Color(0xFFEDE9FE)))
private val supplierGradient = Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C)))
private val supplierAccent = Color(0xFFDB2777)
private val supplierControlHeight = 32.dp
private val supplierTableRowHeight = 40.dp
private val supplierTableHeaderHeight = 38.dp

private data class SupplierTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val supplierTableColumns = listOf(
    SupplierTableColumn("supplier_cd", "仕入先CD", 96, TextAlign.Center),
    SupplierTableColumn("supplier_name", "仕入先名", 132),
    SupplierTableColumn("contact_person", "担当者", 88),
    SupplierTableColumn("phone", "電話", 104),
    SupplierTableColumn("email", "メール", 148),
    SupplierTableColumn("actions", "操作", 76, TextAlign.Center),
)

private val supplierTableWidth = (supplierTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun SupplierMasterHeroBar(total: Int, emailRegistered: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x40F093FB))
            .clip(shape)
            .background(supplierGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(22.dp),
            )
            Column {
                Text("仕入先マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("仕入先情報の登録・編集", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SupplierStatChip(total.toString(), "総件数")
            SupplierStatChip(emailRegistered.toString(), "メール")
        }
    }
}

@Composable
private fun SupplierStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
        }
    }
}

@Composable
fun SupplierMasterFilterCard(
    keyword: String,
    displayedCount: Int,
    total: Int,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFFDF4FF), Color(0xFFFCE7F3))))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = supplierAccent, modifier = Modifier.size(15.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF334155))
                    Text(
                        "表示 $displayedCount / $total",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onClear,
                        enabled = !actionLoading,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF64748B))
                        Text("クリア", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    SupplierGradientButton("仕入先追加", supplierGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("キーワード", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(supplierControlHeight)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        decorationBox = { inner ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                Box(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                                    if (keyword.isEmpty()) {
                                        Text("仕入先CD・名称・担当者", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                    inner()
                                }
                                if (keyword.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onKeywordChange("") },
                                    )
                                }
                            }
                        },
                    )
                }
                SupplierGradientButton(
                    "検索",
                    Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFDB2777))),
                    Icons.Default.Search,
                    enabled = !actionLoading,
                    onClick = onSearch,
                )
            }
        }
    }
}

@Composable
private fun SupplierGradientButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.background(brush, RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun SupplierMasterTable(
    suppliers: List<MasterSupplierDto>,
    loading: Boolean,
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onEdit: (MasterSupplierDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        if (loading && suppliers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = supplierAccent, strokeWidth = 2.dp)
            }
            return@Card
        }
        Column(modifier = Modifier.horizontalScroll(hScroll)) {
            Row(
                modifier = Modifier
                    .width(supplierTableWidth)
                    .height(supplierTableHeaderHeight)
                    .background(Brush.linearGradient(listOf(Color(0xFFFDF4FF), Color(0xFFFCE7F3))))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                supplierTableColumns.forEach { col ->
                    Text(
                        col.label,
                        modifier = Modifier.width(col.widthDp.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        textAlign = col.align,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            if (suppliers.isEmpty()) {
                Box(modifier = Modifier.width(supplierTableWidth).height(100.dp), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                suppliers.forEachIndexed { index, supplier ->
                    SupplierMasterTableRow(supplier, index, onEdit, onDelete)
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        SupplierMasterCompactPagination(
            page = page,
            pageSize = pageSize,
            total = total,
            onPageChange = onPageChange,
            onPageSizeChange = onPageSizeChange,
        )
    }
}

@Composable
private fun SupplierMasterTableRow(
    supplier: MasterSupplierDto,
    index: Int,
    onEdit: (MasterSupplierDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val id = supplier.id ?: return
    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    Row(
        modifier = Modifier
            .width(supplierTableWidth)
            .height(supplierTableRowHeight)
            .background(bg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFDF2F8), modifier = Modifier.width(96.dp)) {
            Text(
                supplier.supplierCd.orEmpty(),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = supplierAccent,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            supplier.supplierName.orEmpty(),
            modifier = Modifier.width(132.dp).padding(horizontal = 4.dp),
            fontSize = 11.sp,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            supplier.contactPerson.orEmpty().ifBlank { "—" },
            modifier = Modifier.width(88.dp),
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            supplier.phone.orEmpty().ifBlank { "—" },
            modifier = Modifier.width(104.dp),
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SupplierEmailCell(supplier.email, Modifier.width(148.dp))
        Row(modifier = Modifier.width(76.dp), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { onEdit(supplier) }, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "編集", modifier = Modifier.size(15.dp), tint = supplierAccent)
            }
            TextButton(onClick = { onDelete(id) }, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "削除", modifier = Modifier.size(15.dp), tint = Color(0xFFEF4444))
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
private fun SupplierEmailCell(email: String?, modifier: Modifier = Modifier) {
    val value = email.orEmpty().trim()
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (value.isBlank()) {
            Text("—", fontSize = 10.sp, color = Color(0xFF94A3B8))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                Text(value, fontSize = 10.sp, color = Color(0xFF475569), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierMasterCompactPagination(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val maxPage = max(1, (total + pageSize - 1) / pageSize)
    val window = 5
    val start = max(1, min(page - 2, maxPage - window + 1))
    val end = min(maxPage, start + window - 1)
    var pageSizeExpanded by remember { mutableStateOf(false) }
    val pageSizeOptions = listOf(10, 20, 50, 100)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFBFC))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("全 $total 件", fontSize = 10.sp, color = Color(0xFF64748B))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            SupplierPageNavButton("‹", enabled = page > 1) { onPageChange(page - 1) }
            for (p in start..end) {
                val active = p == page
                Surface(
                    onClick = { onPageChange(p) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (active) Color.Transparent else Color(0xFFF1F5F9),
                    modifier = if (active) Modifier.background(supplierGradient, RoundedCornerShape(6.dp)) else Modifier,
                ) {
                    Text(
                        "$p",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = if (active) Color.White else Color(0xFF64748B),
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
            SupplierPageNavButton("›", enabled = page < maxPage) { onPageChange(page + 1) }
        }
        ExposedDropdownMenuBox(expanded = pageSizeExpanded, onExpandedChange = { pageSizeExpanded = it }) {
            Surface(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                onClick = { pageSizeExpanded = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$pageSize/頁", fontSize = 10.sp, color = Color(0xFF64748B))
                    ExposedDropdownMenuDefaults.TrailingIcon(pageSizeExpanded)
                }
            }
            ExposedDropdownMenu(expanded = pageSizeExpanded, onDismissRequest = { pageSizeExpanded = false }) {
                pageSizeOptions.forEach { size ->
                    DropdownMenuItem(
                        text = { Text("$size 件/頁", fontSize = 12.sp) },
                        onClick = {
                            onPageSizeChange(size)
                            pageSizeExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplierPageNavButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF1F5F9),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 12.sp,
            color = if (enabled) Color(0xFF64748B) else Color(0xFFCBD5E1),
        )
    }
}
