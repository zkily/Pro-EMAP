package com.example.smart_emap.ui.master.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterPartDto
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

val partPageBackground = Brush.linearGradient(listOf(Color(0xFFF0F4F8), Color(0xFFE2E8F0)))
private val partPurpleGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private const val PART_SUBTITLE =
    "BOM子品目：分類・種別(T/N/F)・単価・部品材料単価（原通貨）・通貨・為替。総単価（円）＝単価×為替(>0)＋部品材料単価"

private val partFilterControlHeight = 36.dp
private val partTableRowHeight = 40.dp
private val partTableHeaderHeight = 42.dp

fun formatPartDisplayNum(value: Double?, decimals: Int = 2): String {
    if (value == null) return "—"
    return if (decimals <= 0) value.roundToInt().toString()
    else String.format(Locale.JAPAN, "%.${decimals}f", value)
}

fun settlementLabel(value: String?): String = when (value) {
    "有償支給", "無償支給", "自給", "その他" -> value
    null, "" -> "—"
    else -> value
}

@Composable
fun PartMasterHeroBar(total: Int, displayed: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(partPurpleGradient)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("部品マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(PART_SUBTITLE, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PartStatChip(total.toString(), "総件数")
            PartStatChip(displayed.toString(), "表示件数")
        }
    }
}

@Composable
private fun PartStatChip(value: String, label: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartMasterFilterCard(
    keyword: String,
    statusFilter: String,
    total: Int,
    displayedCount: Int,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onExportCsv: () -> Unit,
    onQrPrint: () -> Unit,
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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                        Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                    if (displayedCount > 0 || hasActiveFilters) {
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(12.dp))
                            Text("表示: $displayedCount / $total 件", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(buttonScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PartGradientButton("検索", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))), Icons.Default.Search, enabled = !actionLoading, onClick = onSearch)
                    TextButton(onClick = onClear, enabled = !actionLoading, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                        Text("リセット", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    PartGradientButton("CSV出力", Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2))), Icons.Default.Download, enabled = total > 0 && !actionLoading, onClick = onExportCsv)
                    PartGradientButton("QRコード印刷", Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))), Icons.Default.Print, enabled = !actionLoading, onClick = onQrPrint)
                    PartGradientButton("部品を追加", partPurpleGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (hasActiveFilters) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        if (keyword.isNotBlank()) PartFilterTag("キーワード: $keyword")
                        if (statusFilter == "1" || statusFilter == "0") {
                            PartFilterTag("状態: ${if (statusFilter == "1") "有効" else "無効"}")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    PartFilterTextField(
                        label = "🔍 キーワード",
                        hint = "部品CD・名称",
                        value = keyword,
                        onValueChange = onKeywordChange,
                        onSearch = onSearch,
                        modifier = Modifier.weight(1f),
                    )
                    PartFilterStatusDropdown(
                        value = statusFilter,
                        onChange = onStatusChange,
                        modifier = Modifier.width(132.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PartFilterTextField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(partFilterControlHeight)
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
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(hint, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartFilterStatusDropdown(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("" to "選択", "1" to "有効", "0" to "無効")
    val display = options.find { it.first == value }?.second ?: "選択"
    Column(modifier = modifier) {
        Text("🔖 状態", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(partFilterControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color(0xFFFAFBFC),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(display, fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (v, label) ->
                    DropdownMenuItem(
                        text = { Text(label, fontSize = 13.sp) },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun PartFilterTag(text: String) {
    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF)) {
        Text(text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 11.sp, color = Color(0xFF667EEA))
    }
}

@Composable
private fun PartGradientButton(
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
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

private enum class PartColAlign { Start, Center, End }

private data class PartTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: PartColAlign = PartColAlign.Start,
)

private val partTableColumns = listOf(
    PartTableColumn("part_cd", "部品CD", 76, PartColAlign.Start),
    PartTableColumn("part_name", "部品名", 136, PartColAlign.Start),
    PartTableColumn("category", "分類", 72, PartColAlign.Start),
    PartTableColumn("kind", "種別", 44, PartColAlign.Center),
    PartTableColumn("settlement_type", "決済種類", 76, PartColAlign.Start),
    PartTableColumn("uom", "単位", 44, PartColAlign.Center),
    PartTableColumn("unit_price", "単価（原通貨）", 92, PartColAlign.End),
    PartTableColumn("material_unit_price", "部品材料単価", 92, PartColAlign.End),
    PartTableColumn("exchange_rate", "為替レート", 68, PartColAlign.End),
    PartTableColumn("standard_price_jpy", "総単価（円）", 84, PartColAlign.End),
    PartTableColumn("supplier", "仕入先", 112, PartColAlign.Start),
    PartTableColumn("status", "状態", 56, PartColAlign.Center),
    PartTableColumn("actions", "操作", 80, PartColAlign.Center),
)

private val partTableWidth: Dp
    get() = (partTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun PartMasterTable(
    parts: List<MasterPartDto>,
    loading: Boolean,
    total: Int,
    modifier: Modifier = Modifier,
    onEdit: (MasterPartDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                Row(
                    modifier = Modifier
                        .width(partTableWidth)
                        .height(partTableHeaderHeight)
                        .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    partTableColumns.forEach { col ->
                        PartTableHeaderCell(col)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
            ) {
                when {
                    loading && parts.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                        }
                    }
                    parts.isEmpty() -> {
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
                            parts.forEachIndexed { index, part ->
                                PartMasterTableRow(
                                    part = part,
                                    index = index,
                                    onEdit = onEdit,
                                    onDelete = onDelete,
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "表示件数: ${parts.size} / $total",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun PartTableHeaderCell(col: PartTableColumn) {
    val alignment = when (col.align) {
        PartColAlign.Start -> Alignment.CenterStart
        PartColAlign.Center -> Alignment.Center
        PartColAlign.End -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .width(col.widthDp.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = alignment,
    ) {
        Text(
            col.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            textAlign = when (col.align) {
                PartColAlign.Start -> TextAlign.Start
                PartColAlign.Center -> TextAlign.Center
                PartColAlign.End -> TextAlign.End
            },
            maxLines = 2,
            lineHeight = 12.sp,
        )
    }
}

@Composable
private fun PartMasterTableRow(
    part: MasterPartDto,
    index: Int,
    onEdit: (MasterPartDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val id = part.id ?: return
    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    Row(
        modifier = Modifier
            .width(partTableWidth)
            .height(partTableRowHeight)
            .background(bg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        partTableColumns.forEach { col ->
            when (col.key) {
                "part_cd" -> PartTableTextCell(part.partCd.orEmpty(), col)
                "part_name" -> PartTableTextCell(part.partName.orEmpty(), col, bold = false)
                "category" -> PartTableTextCell(part.category.orEmpty().ifBlank { "—" }, col)
                "kind" -> PartTableTextCell(part.kind.orEmpty(), col)
                "settlement_type" -> PartTableTextCell(settlementLabel(part.settlementType), col)
                "uom" -> PartTableTextCell(part.uom.orEmpty(), col)
                "unit_price" -> PartTablePriceCell(part.unitPrice, part.currency, col)
                "material_unit_price" -> PartTablePriceCell(part.materialUnitPrice, part.currency, col)
                "exchange_rate" -> PartTableTextCell(formatPartDisplayNum(part.exchangeRate, 2), col)
                "standard_price_jpy" -> PartTableJpyCell(part.standardPriceJpy, col)
                "supplier" -> PartTableTextCell(part.supplierName ?: part.supplierCd ?: "—", col)
                "status" -> PartTableStatusCell(part.status == 1, col)
                "actions" -> PartTableActionsCell(col, onEdit = { onEdit(part) }, onDelete = { onDelete(id) })
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

@Composable
private fun PartTableTextCell(text: String, col: PartTableColumn, bold: Boolean = false) {
    val alignment = when (col.align) {
        PartColAlign.Start -> Alignment.CenterStart
        PartColAlign.Center -> Alignment.Center
        PartColAlign.End -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = Color(0xFF334155),
            textAlign = when (col.align) {
                PartColAlign.Start -> TextAlign.Start
                PartColAlign.Center -> TextAlign.Center
                PartColAlign.End -> TextAlign.End
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp,
        )
    }
}

@Composable
private fun PartTablePriceCell(value: Double?, currency: String?, col: PartTableColumn) {
    Box(
        modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatPartDisplayNum(value),
                fontSize = 10.sp,
                color = Color(0xFF334155),
                maxLines = 1,
            )
            if (!currency.isNullOrBlank()) {
                Text(currency, fontSize = 9.sp, color = Color(0xFF94A3B8), maxLines = 1)
            }
        }
    }
}

@Composable
private fun PartTableJpyCell(value: Double?, col: PartTableColumn) {
    Box(
        modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            "¥${formatPartDisplayNum(value)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F46E5),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PartTableStatusCell(active: Boolean, col: PartTableColumn) {
    Box(
        modifier = Modifier.width(col.widthDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (active) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (active) Color(0xFFBBF7D0) else Color(0xFFE2E8F0),
            ),
        ) {
            Text(
                if (active) "有効" else "無効",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 10.sp,
                color = if (active) Color(0xFF16A34A) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PartTableActionsCell(col: PartTableColumn, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.width(col.widthDp.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "編集",
            color = Color(0xFF667EEA),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onEdit).padding(horizontal = 4.dp, vertical = 4.dp),
        )
        Text(
            "削除",
            color = Color(0xFFEF4444),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onDelete).padding(horizontal = 4.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun PartMasterPaginationBar(page: Int, pageSize: Int, total: Int, onPageChange: (Int) -> Unit) {
    val maxPage = max(1, (total + pageSize - 1) / pageSize)
    val window = 5
    val start = max(1, min(page - 2, maxPage - window + 1))
    val end = min(maxPage, start + window - 1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("全 $total 件", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(end = 12.dp))
        PartPageNavButton("‹", enabled = page > 1) { onPageChange(page - 1) }
        for (p in start..end) {
            val active = p == page
            Surface(
                onClick = { onPageChange(p) },
                shape = RoundedCornerShape(6.dp),
                color = if (active) Color.Transparent else Color(0xFFF1F5F9),
                modifier = if (active) {
                    Modifier
                        .padding(horizontal = 2.dp)
                        .background(partPurpleGradient, RoundedCornerShape(6.dp))
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
        PartPageNavButton("›", enabled = page < maxPage) { onPageChange(page + 1) }
    }
}

@Composable
private fun PartPageNavButton(label: String, enabled: Boolean, onClick: () -> Unit) {
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
