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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
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

val supplierPageBackground = Brush.linearGradient(listOf(Color(0xFFF0F4F8), Color(0xFFE2E8F0)))
private val supplierPurpleGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private const val SUPPLIER_SUBTITLE = "仕入先情報の登録・編集・管理を行います"

private val supplierFilterControlHeight = 34.dp
private val supplierTableRowHeight = 38.dp
private val supplierTableHeaderHeight = 40.dp

private data class SupplierTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val supplierTableColumns = listOf(
    SupplierTableColumn("supplier_cd", "仕入先CD", 108, TextAlign.Center),
    SupplierTableColumn("supplier_name", "仕入先名", 160),
    SupplierTableColumn("contact_person", "担当者", 96),
    SupplierTableColumn("phone", "電話", 120),
    SupplierTableColumn("email", "メール", 168),
    SupplierTableColumn("actions", "操作", 88, TextAlign.Center),
)

private val supplierTableWidth = (supplierTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun SupplierMasterHeroBar(total: Int, emailRegistered: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(supplierPurpleGradient)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("仕入先マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(SUPPLIER_SUBTITLE, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupplierStatChip(total.toString(), "総仕入先数")
            SupplierStatChip(emailRegistered.toString(), "メール登録")
        }
    }
}

@Composable
private fun SupplierStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 20.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun SupplierMasterFilterCard(
    keyword: String,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onAdd: () -> Unit,
) {
    val buttonScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                }
                Row(
                    modifier = Modifier.horizontalScroll(buttonScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onClear,
                        enabled = !actionLoading,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                        Text("クリア", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    SupplierGradientButton("仕入先を追加", supplierPurpleGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔍 キーワード検索", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(supplierFilterControlHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFAFBFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        decorationBox = { inner ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (keyword.isEmpty()) {
                                        Text("仕入先CD・名称", fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                                    }
                                    inner()
                                }
                                if (keyword.isNotBlank()) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                    )
                }
                SupplierGradientButton("検索", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Search, enabled = !actionLoading, onClick = onSearch)
            }
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF)) {
                        Text(
                            "キーワード: $keyword",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF667EEA),
                        )
                    }
                }
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
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .then(
                if (enabled) Modifier.background(brush, shape).clickable(onClick = onClick)
                else Modifier.background(Color(0xFFE2E8F0), shape),
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun SupplierMasterTable(
    suppliers: List<MasterSupplierDto>,
    loading: Boolean,
    total: Int,
    modifier: Modifier = Modifier,
    onEdit: (MasterSupplierDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    Card(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                Row(
                    modifier = Modifier
                        .width(supplierTableWidth)
                        .height(supplierTableHeaderHeight)
                        .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    supplierTableColumns.forEach { col ->
                        Text(
                            col.label,
                            modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            textAlign = col.align,
                            maxLines = 1,
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().heightIn(min = 140.dp)) {
                when {
                    loading && suppliers.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                        }
                    }
                    suppliers.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("データがありません", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(vScroll)
                                .horizontalScroll(hScroll),
                        ) {
                            suppliers.forEachIndexed { index, supplier ->
                                SupplierMasterTableRow(supplier, index, onEdit, onDelete)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "表示件数: ${suppliers.size} / $total",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
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
        Row(
            modifier = Modifier.width(108.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
            Text(
                supplier.supplierCd.orEmpty(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF667EEA),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SupplierTableTextCell(supplier.supplierName.orEmpty(), 160)
        SupplierTableTextCell(supplier.contactPerson.orEmpty().ifBlank { "—" }, 96)
        SupplierTableTextCell(supplier.phone.orEmpty().ifBlank { "—" }, 120)
        SupplierTableTextCell(supplier.email.orEmpty().ifBlank { "—" }, 168)
        Row(
            modifier = Modifier.width(88.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "編集",
                color = Color(0xFF667EEA),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onEdit(supplier) }.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Text(
                "削除",
                color = Color(0xFFEF4444),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onDelete(id) }.padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

@Composable
private fun SupplierTableTextCell(text: String, widthDp: Int) {
    Text(
        text,
        modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp),
        fontSize = 10.sp,
        color = Color(0xFF334155),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 13.sp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierMasterPaginationBar(
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("全 $total 件", fontSize = 11.sp, color = Color(0xFF64748B))
                ExposedDropdownMenuBox(expanded = pageSizeExpanded, onExpandedChange = { pageSizeExpanded = it }) {
                    Surface(
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        onClick = { pageSizeExpanded = true },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("$pageSize 件/頁", fontSize = 11.sp, color = Color(0xFF64748B))
                            ExposedDropdownMenuDefaults.TrailingIcon(pageSizeExpanded)
                        }
                    }
                    ExposedDropdownMenu(expanded = pageSizeExpanded, onDismissRequest = { pageSizeExpanded = false }) {
                        pageSizeOptions.forEach { size ->
                            DropdownMenuItem(
                                text = { Text("$size 件/頁", fontSize = 12.sp) },
                                onClick = { onPageSizeChange(size); pageSizeExpanded = false },
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                SupplierPageNavButton("‹", enabled = page > 1) { onPageChange(page - 1) }
                for (p in start..end) {
                    val active = p == page
                    Surface(
                        onClick = { onPageChange(p) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (active) Color.Transparent else Color(0xFFF1F5F9),
                        modifier = if (active) {
                            Modifier.padding(horizontal = 2.dp).background(supplierPurpleGradient, RoundedCornerShape(6.dp))
                        } else {
                            Modifier.padding(horizontal = 2.dp)
                        },
                    ) {
                        Text(
                            "$p",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 12.sp,
                            color = if (active) Color.White else Color(0xFF64748B),
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
                SupplierPageNavButton("›", enabled = page < maxPage) { onPageChange(page + 1) }
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
        modifier = Modifier.padding(horizontal = 2.dp),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            fontSize = 14.sp,
            color = if (enabled) Color(0xFF64748B) else Color(0xFFCBD5E1),
        )
    }
}
