package com.example.smart_emap.ui.master.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductMasterStatsDto

private val productPurple = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))

private data class ProductActionBtn(
    val label: String,
    val brush: Brush,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean = true,
)

@Composable
fun ProductMasterHeroBar(stats: ProductMasterStatsDto) {
    val shape = RoundedCornerShape(12.dp)
    val statScroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(productPurple)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp),
            )
            Column {
                Text("製品マスタ管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    "製品情報の登録・編集・管理を行います",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(statScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductStatChip(stats.total.toString(), "総製品数")
            ProductStatChip(stats.massProduction.toString(), "量産品")
            ProductStatChip(stats.prototype.toString(), "試作品")
            ProductStatChip(stats.supply.toString(), "補給品")
            ProductStatChip(stats.other.toString(), "その他")
        }
    }
}

@Composable
private fun ProductStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 18.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
fun ProductMasterActionSection(
    totalCount: Int,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    listNotEmpty: Boolean,
    onColumnSettings: () -> Unit,
    onCsv: () -> Unit,
    onQrPrint: () -> Unit,
    onCuttingPrint: () -> Unit,
    onScrapCalc: () -> Unit,
    onAdd: () -> Unit,
) {
    val buttonScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                .padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                }
                if (totalCount > 0 || hasActiveFilters) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Text("${totalCount}件", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(buttonScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val buttons = listOf(
                    ProductActionBtn("列表示設定", Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))), Icons.Default.Settings),
                    ProductActionBtn("CSV出力", Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))), Icons.Default.Download, totalCount > 0),
                    ProductActionBtn("QRコード印刷", Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))), Icons.Default.Print),
                    ProductActionBtn("切断長印刷", Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))), Icons.Default.Print, listNotEmpty),
                    ProductActionBtn("端材長計算", Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))), Icons.Default.Analytics),
                    ProductActionBtn("製品追加", productPurple, Icons.Default.Add),
                )
                buttons.forEach { btn ->
                    ProductGradientButton(
                        label = btn.label,
                        brush = btn.brush,
                        icon = btn.icon,
                        enabled = btn.enabled && !actionLoading,
                        onClick = when (btn.label) {
                            "列表示設定" -> onColumnSettings
                            "CSV出力" -> onCsv
                            "QRコード印刷" -> onQrPrint
                            "切断長印刷" -> onCuttingPrint
                            "端材長計算" -> onScrapCalc
                            else -> onAdd
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductGradientButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .background(if (enabled) brush else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) Color.White else Color(0xFF94A3B8), maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMasterFilterGrid(
    keyword: String,
    category: String,
    kind: String,
    materialCd: String,
    materialOptions: List<Pair<String, String>>,
    onKeywordChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onKindChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            ProductFilterField(
                label = "🔍 キーワード",
                hint = "製品名 / 品番 / 別名",
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1.35f),
            )
            ProductFilterDropdown(
                label = "📁 カテゴリ",
                value = category,
                options = listOf("" to "選択") + listOf("一般", "一般溶接", "メカ溶接", "自動車", "その他").map { it to it },
                onChange = onCategoryChange,
                modifier = Modifier.weight(1f),
            )
            ProductFilterDropdown(
                label = "🏷️ 分類(kind)",
                value = kind,
                options = listOf("" to "選択", "T" to "T", "N" to "N", "F" to "F"),
                onChange = onKindChange,
                modifier = Modifier.weight(0.85f),
            )
            ProductFilterDropdown(
                label = "🧱 材料CD",
                value = materialCd,
                options = listOf("" to "選択") + materialOptions.map { (cd, name) -> cd to "$cd｜$name" },
                onChange = onMaterialChange,
                modifier = Modifier.weight(1.3f),
                menuMatchAnchorWidth = false,
            )
            ProductFilterClearButton(onClick = onClear)
        }
    }
}

@Composable
private fun ProductFilterClearButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
    ) {
        Icon(Icons.Default.Clear, contentDescription = "クリア", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("クリア", fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProductFilterField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFAFBFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(hint, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    inner()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    menuMatchAnchorWidth: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "選択" }
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color(0xFFFAFBFC),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
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
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(matchAnchorWidth = menuMatchAnchorWidth),
            ) {
                options.forEach { (v, labelText) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                labelText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = if (menuMatchAnchorWidth) Modifier else Modifier.widthIn(min = 220.dp),
                            )
                        },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
fun ProductMasterTable(
    products: List<MasterProductDto>,
    loading: Boolean,
    visibleColumns: Map<String, Boolean>,
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onEdit: (MasterProductDto) -> Unit,
    onDelete: (MasterProductDto) -> Unit,
) {
    val scroll = rememberScrollState()
    val columns = remember(visibleColumns) { resolveVisibleProductColumns(visibleColumns) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA))
                    .horizontalScroll(scroll)
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            ) {
                columns.forEach { col ->
                    Box(modifier = Modifier.width(col.widthDp.dp), contentAlignment = Alignment.Center) {
                        val labelText = if (col.key == "product_name") "${col.label} ↑" else col.label
                        Text(
                            labelText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (col.key == "product_name") Color(0xFF409EFF) else Color(0xFF475569),
                            maxLines = 1,
                        )
                    }
                }
                Box(modifier = Modifier.width(88.dp), contentAlignment = Alignment.Center) {
                    Text("操作", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            when {
                loading && products.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                }
                products.isEmpty() -> Text("データがありません", modifier = Modifier.padding(24.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF94A3B8))
                else -> {
                    products.forEachIndexed { index, row ->
                        ProductMasterTableRow(row, index, scroll, columns, onEdit, onDelete)
                    }
                    ProductMasterPaginationBar(
                        page = page,
                        pageSize = pageSize,
                        total = total,
                        onPageChange = onPageChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductMasterPaginationBar(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
) {
    val maxPage = maxOf(1, (total + pageSize - 1) / pageSize)
    val visiblePages = remember(page, maxPage) {
        val start = maxOf(1, page - 2)
        val end = minOf(maxPage, start + 4)
        val adjustedStart = maxOf(1, end - 4)
        (adjustedStart..end).toList()
    }
    val rangeText = if (total == 0) {
        "0件"
    } else {
        val start = (page - 1) * pageSize + 1
        val end = minOf(page * pageSize, total)
        "$start-$end / ${total}件"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFBFC))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(rangeText, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ProductPaginationNavBtn(enabled = page > 1, forward = false) { onPageChange(page - 1) }
            visiblePages.forEach { p ->
                ProductPaginationPageBtn(pageNum = p, active = p == page) { onPageChange(p) }
            }
            ProductPaginationNavBtn(enabled = page < maxPage, forward = true) { onPageChange(page + 1) }
        }
    }
}

@Composable
private fun ProductPaginationNavBtn(enabled: Boolean, forward: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (forward) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = if (enabled) Color(0xFF334155) else Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ProductPaginationPageBtn(pageNum: Int, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (active) Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
                else Brush.linearGradient(listOf(Color.White, Color.White)),
            )
            .border(1.dp, if (active) Color(0xFF667EEA) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            pageNum.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else Color(0xFF334155),
        )
    }
}

@Composable
private fun ProductMasterTableRow(
    row: MasterProductDto,
    index: Int,
    scroll: androidx.compose.foundation.ScrollState,
    columns: List<ProductTableColumnDef>,
    onEdit: (MasterProductDto) -> Unit,
    onDelete: (MasterProductDto) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 0) Color.White else Color(0xFFFAFCFE))
            .horizontalScroll(scroll)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { col ->
            Box(
                modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isProductStatusColumn(col.key)) {
                    ProductStatusBadge(row.status)
                } else {
                    val cell = productCellText(row, col.key)
                    Text(
                        cell?.take(16).orEmpty().ifEmpty { "—" },
                        fontSize = 10.sp,
                        color = Color(0xFF334155),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(modifier = Modifier.width(88.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onEdit(row) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF409EFF)),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF409EFF), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("編集", fontSize = 9.sp, color = Color(0xFF409EFF))
            }
            Spacer(Modifier.width(4.dp))
            OutlinedButton(
                onClick = { onDelete(row) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF56C6C)),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF56C6C), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("削除", fontSize = 9.sp, color = Color(0xFFF56C6C))
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
private fun ProductStatusBadge(status: String?) {
    val isActive = status == "active" || status == "現行"
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isActive) Color(0xFFE8F8EF) else Color(0xFFF1F5F9),
    ) {
        Text(
            if (isActive) "現行" else "終息",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Color(0xFF67C23A) else Color(0xFF909399),
        )
    }
}
