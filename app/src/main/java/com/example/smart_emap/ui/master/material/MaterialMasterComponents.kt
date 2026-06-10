package com.example.smart_emap.ui.master.material

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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterMaterialDto
import com.example.smart_emap.data.model.MaterialMasterStatsDto

private val materialGradient = Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))

private data class MaterialActionBtn(
    val label: String,
    val brush: Brush,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean = true,
    val isText: Boolean = false,
)

@Composable
fun MaterialMasterHeroBar(stats: MaterialMasterStatsDto) {
    val shape = RoundedCornerShape(12.dp)
    val statScroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x4011998E))
            .clip(shape)
            .background(materialGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Category, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("材料マスタ管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("材料の登録・編集・仕入先管理を行います", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }
        Row(modifier = Modifier.horizontalScroll(statScroll), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MaterialStatChip(stats.total.toString(), "総材料数")
            MaterialStatChip(stats.active.toString(), "有効材料")
            MaterialStatChip(stats.inactive.toString(), "無効材料", highlight = true)
        }
    }
}

@Composable
private fun MaterialStatChip(value: String, label: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) Color(0x33EF4444) else Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (highlight) Color(0x55EF4444) else Color.White.copy(alpha = 0.15f),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
fun MaterialMasterActionSection(
    filteredCount: Int,
    totalCount: Int,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    canCreate: Boolean = true,
    canEdit: Boolean = true,
    canExport: Boolean = true,
    onClear: () -> Unit,
    onColumnSettings: () -> Unit,
    onPrint: () -> Unit,
    onQrPrint: () -> Unit,
    onCsv: () -> Unit,
    onCalcPrice: () -> Unit,
    onAdd: () -> Unit,
) {
    val buttonScroll = rememberScrollState()
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
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF11998E), modifier = Modifier.size(16.dp))
                        Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                    if (hasActiveFilters || filteredCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            Text("${filteredCount}件 / ${totalCount}件中", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(buttonScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val buttons = buildList {
                        add(MaterialActionBtn("クリア", Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))), Icons.Default.Clear, isText = true))
                        add(MaterialActionBtn("列表示設定", Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))), Icons.Default.Settings, isText = true))
                        if (canExport) {
                            add(MaterialActionBtn("印刷", Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569))), Icons.Default.Print, filteredCount > 0, isText = true))
                            add(MaterialActionBtn("QRコード印刷", Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))), Icons.Default.Print, totalCount > 0))
                            add(MaterialActionBtn("CSV出力", Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))), Icons.Default.Download, filteredCount > 0))
                        }
                        if (canEdit) {
                            add(MaterialActionBtn("単価計算", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))), Icons.Default.Settings, totalCount > 0))
                        }
                        if (canCreate) {
                            add(MaterialActionBtn("材料追加", materialGradient, Icons.Default.Add))
                        }
                    }
                    buttons.forEach { btn ->
                        if (btn.isText) {
                            TextButton(
                                onClick = when (btn.label) {
                                    "クリア" -> onClear
                                    "列表示設定" -> onColumnSettings
                                    else -> onPrint
                                },
                                enabled = btn.enabled && !actionLoading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Icon(btn.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                                Spacer(Modifier.width(4.dp))
                                Text(btn.label, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        } else {
                            MaterialGradientButton(
                                label = btn.label,
                                brush = btn.brush,
                                icon = btn.icon,
                                enabled = btn.enabled && !actionLoading,
                                onClick = when (btn.label) {
                                    "QRコード印刷" -> onQrPrint
                                    "CSV出力" -> onCsv
                                    "単価計算" -> onCalcPrice
                                    else -> onAdd
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
private fun MaterialGradientButton(
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
fun MaterialMasterFilterGrid(
    keyword: String,
    statusFilter: String,
    materialType: String,
    supplyClassification: String,
    usage: String,
    storageLocation: String,
    onKeywordChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onMaterialTypeChange: (String) -> Unit,
    onSupplyClassificationChange: (String) -> Unit,
    onUsageChange: (String) -> Unit,
    onStorageLocationChange: (String) -> Unit,
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
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            MaterialFilterField("🔍 キーワード", "材料CD・材料名・仕入先・規格", keyword, onKeywordChange, Modifier.width(180.dp))
            MaterialFilterDropdown("状態", statusFilter, listOf("" to "全て", "1" to "有効", "0" to "無効"), onStatusChange, Modifier.width(100.dp))
            MaterialFilterDropdown("材料種類", materialType, listOf("" to "全て", "鋼管" to "鋼管", "鋼材" to "鋼材", "樹脂" to "樹脂", "アルミ" to "アルミ", "その他" to "その他"), onMaterialTypeChange, Modifier.width(100.dp))
            MaterialFilterDropdown("支給区分", supplyClassification, listOf("" to "全て", "無償" to "無償", "有償" to "有償", "自給" to "自給"), onSupplyClassificationChange, Modifier.width(100.dp))
            MaterialFilterDropdown("用途", usage, listOf("" to "全て", "生産用" to "生産用", "試作用" to "試作用", "支給用" to "支給用", "その他" to "その他"), onUsageChange, Modifier.width(100.dp))
            MaterialFilterField("保管場所", "保管場所", storageLocation, onStorageLocationChange, Modifier.width(120.dp))
        }
    }
}

@Composable
private fun MaterialFilterField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
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
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(hint, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    inner()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialFilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: "全て"
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(36.dp).menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color(0xFFFAFBFC),
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(display, fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (v, text) ->
                    DropdownMenuItem(text = { Text(text, fontSize = 12.sp) }, onClick = { onChange(v); expanded = false })
                }
            }
        }
    }
}

@Composable
fun MaterialMasterTable(
    materials: List<MasterMaterialDto>,
    loading: Boolean,
    visibleColumns: Map<String, Boolean>,
    totalCount: Int,
    statusUpdatingIds: Set<Int>,
    canEdit: Boolean = true,
    canDelete: Boolean = true,
    onEdit: (MasterMaterialDto) -> Unit,
    onDelete: (MasterMaterialDto) -> Unit,
    onToggleStatus: (MasterMaterialDto, Boolean) -> Unit,
) {
    val scroll = rememberScrollState()
    val columns = remember(visibleColumns) { resolveVisibleMaterialColumns(visibleColumns) }
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
                        val labelText = if (col.key == "material_name") "${col.label} ↑" else col.label
                        Text(
                            labelText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (col.key == "material_name") Color(0xFF409EFF) else Color(0xFF475569),
                            maxLines = 1,
                        )
                    }
                }
                if (canEdit || canDelete) {
                    Box(modifier = Modifier.width(88.dp), contentAlignment = Alignment.Center) {
                        Text("操作", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            when {
                loading && materials.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF11998E), modifier = Modifier.size(28.dp))
                }
                materials.isEmpty() -> Text("データがありません", modifier = Modifier.padding(24.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF94A3B8))
                else -> materials.forEachIndexed { index, row ->
                    MaterialMasterTableRow(
                        row, index, scroll, columns, statusUpdatingIds, canEdit, canDelete,
                        onEdit, onDelete, onToggleStatus,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "表示件数: ${materials.size} / $totalCount",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun MaterialMasterTableRow(
    row: MasterMaterialDto,
    index: Int,
    scroll: androidx.compose.foundation.ScrollState,
    columns: List<MaterialTableColumnDef>,
    statusUpdatingIds: Set<Int>,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: (MasterMaterialDto) -> Unit,
    onDelete: (MasterMaterialDto) -> Unit,
    onToggleStatus: (MasterMaterialDto, Boolean) -> Unit,
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
            Box(modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                when {
                    isMaterialStatusColumn(col.key) -> {
                        val updating = row.id != null && row.id in statusUpdatingIds
                        if (updating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Switch(
                                checked = row.status == 1,
                                onCheckedChange = { onToggleStatus(row, it) },
                                enabled = canEdit,
                                modifier = Modifier.height(24.dp),
                            )
                        }
                    }
                    col.key == "material_type" -> MaterialTypeTag(row.materialType)
                    col.key == "supply_classification" -> SupplyTag(row.supplyClassification)
                    col.key == "usegae" -> UsageTag(row.usegae)
                    col.key == "dimensions" -> Text(formatMaterialDimensions(row), fontSize = 8.sp, color = Color(0xFF334155), textAlign = TextAlign.Center, lineHeight = 10.sp)
                    col.key == "price_info" -> Text(formatMaterialPriceInfo(row), fontSize = 8.sp, color = Color(0xFF16A34A), textAlign = TextAlign.Center, lineHeight = 10.sp)
                    col.key == "stock_info" -> Text(formatMaterialStockInfo(row), fontSize = 8.sp, color = Color(0xFFDC2626), textAlign = TextAlign.Center, lineHeight = 10.sp)
                    else -> Text(
                        materialCellText(row, col.key)?.take(14).orEmpty().ifEmpty { "—" },
                        fontSize = 10.sp,
                        color = Color(0xFF334155),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (canEdit || canDelete) {
            Row(modifier = Modifier.width(88.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (canEdit) {
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
                }
                if (canEdit && canDelete) {
                    Spacer(Modifier.width(4.dp))
                }
                if (canDelete) {
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
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
private fun MaterialTypeTag(type: String?) {
    val (bg, fg) = when (type) {
        "鋼材" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        "樹脂" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "アルミ" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        "鋼管" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        else -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
    }
    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(type.orEmpty().ifBlank { "—" }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SupplyTag(value: String?) {
    val isFree = value?.contains("無償") == true
    Surface(shape = RoundedCornerShape(4.dp), color = if (isFree) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)) {
        Text(value.orEmpty().ifBlank { "—" }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = if (isFree) Color(0xFF059669) else Color(0xFFD97706))
    }
}

@Composable
private fun UsageTag(value: String?) {
    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
        Text(value.orEmpty().ifBlank { "—" }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = Color(0xFF64748B))
    }
}
