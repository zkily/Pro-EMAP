package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.data.model.MaterialMasterItemDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog

private val InspAccent = Color(0xFF667EEA)
private val InspAccentSoft = Color(0xFFEEF2FF)
private val InspGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val CardShape = RoundedCornerShape(12.dp)

private data class InspCol(
    val label: String,
    val weight: Float,
    val minWidth: Dp,
    val alignStart: Boolean = false,
    val maxLines: Int = 1,
)

@Composable
fun MaterialInspectionPageHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape, spotColor = Color(0x12000000)),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F2F5)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(InspGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("材料受入検品管理", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))
                Text("材料の品質検査・検品作業を管理します", fontSize = 11.sp, color = Color(0xFF7F8C8D))
            }
        }
    }
}

@Composable
fun MaterialInspectionTabRow(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf(
        Icons.Default.Description to "検品履歴",
        Icons.Default.Settings to "品質基準設定",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tabs.forEachIndexed { index, (icon, label) ->
                val selected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(index) },
                    color = if (selected) InspAccentSoft else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) InspAccent.copy(alpha = 0.35f) else Color(0xFFE5E7EB),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, null, tint = if (selected) InspAccent else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) InspAccent else Color(0xFF64748B),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialInspectionHistoryCard(
    supplierOptions: List<String>,
    selectedSuppliers: List<String>,
    startDate: String,
    endDate: String,
    printLoading: Boolean,
    isLoading: Boolean,
    items: List<MaterialLogItemDto>,
    totalCount: Int,
    page: Int,
    pageSize: Int,
    onSupplierChange: (List<String>) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onPrint: () -> Unit,
    onDetail: (MaterialLogItemDto) -> Unit,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val scroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFBFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("📊 検品履歴", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1F2937))
                Row(
                    modifier = Modifier.horizontalScroll(scroll),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    InspSupplierSelect(selectedSuppliers, supplierOptions, onSupplierChange)
                    InspDateRangeField(startDate, endDate, onStartChange, onEndChange)
                    InspPrintButton(printLoading, totalCount > 0, onPrint)
                }
            }
            HorizontalDivider(color = Color(0xFFE5E7EB))
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "insp-history-loading",
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InspAccent, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                    }
                } else if (items.isEmpty()) {
                    Text(
                        "検品履歴がありません",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                    )
                } else {
                    InspectionHistoryTable(items, onDetail)
                }
            }
            if (totalCount > 0) {
                HorizontalDivider(color = Color(0xFFE5E7EB))
                MaterialInspectionPaginationBar(page, pageSize, totalCount, onPageChange, onPageSizeChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialInspectionStandardsCard(
    materialNameOptions: List<String>,
    selectedMaterialName: String,
    isLoading: Boolean,
    items: List<MaterialMasterItemDto>,
    onMaterialNameChange: (String) -> Unit,
    onEdit: (MaterialMasterItemDto) -> Unit,
    onStatusChange: (MaterialMasterItemDto, Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFBFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("⚖️ 品質基準設定", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1F2937))
                InspMaterialNameSelect(selectedMaterialName, materialNameOptions, onMaterialNameChange)
            }
            HorizontalDivider(color = Color(0xFFE5E7EB))
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "insp-standards-loading",
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InspAccent, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                    }
                } else if (items.isEmpty()) {
                    Text(
                        "品質基準データがありません",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                    )
                } else {
                    InspectionStandardsTable(items, onEdit, onStatusChange)
                }
            }
        }
    }
}

private val historyCols = listOf(
    InspCol("検品日", 0.9f, 72.dp),
    InspCol("時間", 0.75f, 56.dp),
    InspCol("材料名", 1.5f, 88.dp, alignStart = true, maxLines = 2),
    InspCol("仕入先", 1.1f, 72.dp, alignStart = true, maxLines = 2),
    InspCol("製造番号", 1.2f, 88.dp),
    InspCol("数量", 0.7f, 48.dp),
    InspCol("外径1", 0.75f, 52.dp),
    InspCol("外径2", 0.75f, 52.dp),
    InspCol("検品者", 1.1f, 72.dp, alignStart = true, maxLines = 2),
    InspCol("操作", 0.8f, 52.dp),
)

private val standardsCols = listOf(
    InspCol("材料名", 1.4f, 80.dp, alignStart = true, maxLines = 2),
    InspCol("材料規格", 1.0f, 64.dp, alignStart = true),
    InspCol("仕入先", 1.1f, 72.dp, alignStart = true, maxLines = 2),
    InspCol("許容範囲", 0.9f, 60.dp),
    InspCol("許容値1", 0.8f, 56.dp),
    InspCol("許容値2", 0.8f, 56.dp),
    InspCol("範囲値", 1.0f, 64.dp),
    InspCol("最小値", 0.8f, 52.dp),
    InspCol("最大値", 0.8f, 52.dp),
    InspCol("実測値1", 0.8f, 52.dp),
    InspCol("実測値2", 0.8f, 52.dp),
    InspCol("実測値3", 0.8f, 52.dp),
    InspCol("代表品種", 1.0f, 64.dp, alignStart = true),
    InspCol("状態", 0.8f, 56.dp),
    InspCol("操作", 0.7f, 48.dp),
)

private fun minTableWidth(cols: List<InspCol>): Dp =
    cols.fold(0.dp) { acc, c -> acc + c.minWidth } + 3.dp * (cols.size - 1).coerceAtLeast(0) + 12.dp

@Composable
private fun InspectionHistoryTable(items: List<MaterialLogItemDto>, onDetail: (MaterialLogItemDto) -> Unit) {
    val hScroll = rememberScrollState()
    val minW = minTableWidth(historyCols)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val needsScroll = minW > maxWidth
        val mod = if (needsScroll) Modifier.horizontalScroll(hScroll).widthIn(min = minW) else Modifier.fillMaxWidth()
        Column(mod.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)).padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                historyCols.forEach { InspHeaderCell(it) }
            }
            items.forEachIndexed { i, row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 34.dp)
                        .background(if (i % 2 == 1) Color(0xFFFAFBFC) else Color.White)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InspBodyCell(historyCols[0], row.logDate.orEmpty().ifBlank { "—" })
                    InspBodyCell(historyCols[1], row.logTime.orEmpty().ifBlank { "—" })
                    InspBodyCell(historyCols[2], row.materialName.orEmpty().ifBlank { "—" }, Color(0xFF2563EB))
                    InspBodyCell(historyCols[3], row.supplier.orEmpty().ifBlank { "—" })
                    InspBodyCell(historyCols[4], row.manufactureNo.orEmpty().ifBlank { "—" })
                    InspBodyCell(historyCols[5], row.quantity?.toString() ?: "—")
                    InspBodyCell(historyCols[6], row.outerDiameter1?.toString() ?: "—")
                    InspBodyCell(historyCols[7], row.outerDiameter2?.toString() ?: "—")
                    InspBodyCell(historyCols[8], row.inspectorDisplayName())
                    Box(Modifier.weight(historyCols[9].weight).widthIn(min = historyCols[9].minWidth), contentAlignment = Alignment.Center) {
                        TextButton(onClick = { onDetail(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                            Text("詳細", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun InspectionStandardsTable(
    items: List<MaterialMasterItemDto>,
    onEdit: (MaterialMasterItemDto) -> Unit,
    onStatusChange: (MaterialMasterItemDto, Boolean) -> Unit,
) {
    val hScroll = rememberScrollState()
    val minW = minTableWidth(standardsCols)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val needsScroll = minW > maxWidth
        val mod = if (needsScroll) Modifier.horizontalScroll(hScroll).widthIn(min = minW) else Modifier.fillMaxWidth()
        Column(mod.padding(horizontal = 6.dp, vertical = 6.dp)) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)).padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                standardsCols.forEach { InspHeaderCell(it) }
            }
            items.forEachIndexed { i, row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 36.dp)
                        .background(if (i % 2 == 1) Color(0xFFFAFBFC) else Color.White)
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InspBodyCell(standardsCols[0], row.materialName.orEmpty().ifBlank { "—" })
                    InspBodyCell(standardsCols[1], row.standardSpec.orEmpty().ifBlank { "—" })
                    InspBodyCell(standardsCols[2], row.supplierName.orEmpty().ifBlank { "—" })
                    InspBodyCell(standardsCols[3], row.toleranceRange.orEmpty().ifBlank { "—" })
                    InspBodyCell(standardsCols[4], row.tolerance1?.toString() ?: "—")
                    InspBodyCell(standardsCols[5], row.tolerance2?.toString() ?: "—")
                    InspBodyCell(standardsCols[6], row.rangeValue.orEmpty().ifBlank { "—" })
                    InspBodyCell(standardsCols[7], row.minValue?.toString() ?: "—")
                    InspBodyCell(standardsCols[8], row.maxValue?.toString() ?: "—")
                    InspBodyCell(standardsCols[9], row.actualValue1?.toString() ?: "—")
                    InspBodyCell(standardsCols[10], row.actualValue2?.toString() ?: "—")
                    InspBodyCell(standardsCols[11], row.actualValue3?.toString() ?: "—")
                    InspBodyCell(standardsCols[12], row.representativeModel.orEmpty().ifBlank { "—" })
                    Box(Modifier.weight(standardsCols[13].weight).widthIn(min = standardsCols[13].minWidth), contentAlignment = Alignment.Center) {
                        Switch(
                            checked = (row.status ?: 1) == 1,
                            onCheckedChange = { onStatusChange(row, it) },
                            modifier = Modifier.height(24.dp),
                            colors = SwitchDefaults.colors(checkedTrackColor = InspAccent, checkedThumbColor = Color.White),
                        )
                    }
                    Box(Modifier.weight(standardsCols[14].weight).widthIn(min = standardsCols[14].minWidth), contentAlignment = Alignment.Center) {
                        TextButton(onClick = { onEdit(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)) {
                            Text("編集", fontSize = 10.sp, color = InspAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun RowScope.InspHeaderCell(col: InspCol) {
    Text(
        col.label,
        modifier = Modifier.weight(col.weight).widthIn(min = col.minWidth).padding(horizontal = 2.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF475569),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (col.alignStart) TextAlign.Start else TextAlign.Center,
        lineHeight = 10.sp,
    )
}

@Composable
private fun RowScope.InspBodyCell(col: InspCol, text: String, color: Color = Color(0xFF334155)) {
    Text(
        text,
        modifier = Modifier.weight(col.weight).widthIn(min = col.minWidth).padding(horizontal = 2.dp),
        fontSize = 9.sp,
        color = color,
        maxLines = col.maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (col.alignStart) TextAlign.Start else TextAlign.Center,
        lineHeight = 11.sp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspSupplierSelect(selected: List<String>, options: List<String>, onChange: (List<String>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val display = when {
        selected.isEmpty() -> "仕入先を選択"
        selected.size == 1 -> selected.first()
        else -> "${selected.first()} +${selected.size - 1}"
    }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .widthIn(min = 120.dp, max = 160.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.FilterList, null, tint = InspAccent, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(display, fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (selected.isNotEmpty()) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).clickable { onChange(emptyList()) })
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
            DropdownMenuItem(text = { Text("全て", fontSize = 11.sp) }, onClick = { onChange(emptyList()); expanded = false })
            options.forEach { name ->
                val checked = name in selected
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.size(18.dp))
                            Text(name, fontSize = 11.sp, maxLines = 2)
                        }
                    },
                    onClick = { onChange(if (checked) selected - name else selected + name) },
                )
            }
        }
    }
}

@Composable
private fun InspDateRangeField(startDate: String, endDate: String, onStartChange: (String) -> Unit, onEndChange: (String) -> Unit) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = InspAccent,
            onDismiss = { pickStart = false },
            onConfirm = { date ->
                onStartChange(date)
                pickStart = false
                pickEnd = true
            },
        )
    }
    if (pickEnd) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = InspAccent,
            onDismiss = { pickEnd = false },
            onConfirm = { date ->
                onEndChange(date)
                pickEnd = false
            },
        )
    }
    val display = when {
        startDate.isNotBlank() && endDate.isNotBlank() -> "$startDate ~ $endDate"
        startDate.isNotBlank() -> startDate
        else -> "開始日 ~ 終了日"
    }
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .widthIn(min = 150.dp, max = 200.dp)
            .height(28.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .clickable { pickStart = true }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.CalendarMonth, null, tint = InspAccent, modifier = Modifier.size(12.dp))
        Text(display, fontSize = 10.sp, color = if (startDate.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun InspPrintButton(loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(if (enabled) InspGradient else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))))
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color.White)
            } else {
                Icon(Icons.Default.Print, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Text("印刷", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspMaterialNameSelect(selected: String, options: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .widthIn(min = 140.dp, max = 200.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                selected.ifBlank { "材料名を検索・選択" },
                fontSize = 10.sp,
                color = if (selected.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected.isNotBlank()) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).clickable { onChange("") })
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 260.dp)) {
            DropdownMenuItem(text = { Text("全て", fontSize = 11.sp) }, onClick = { onChange(""); expanded = false })
            options.forEach { name ->
                DropdownMenuItem(text = { Text(name, fontSize = 11.sp, maxLines = 2) }, onClick = { onChange(name); expanded = false })
            }
        }
    }
}

@Composable
fun MaterialInspectionPaginationBar(
    page: Int,
    pageSize: Int,
    totalCount: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val totalPages = ((totalCount + pageSize - 1) / pageSize).coerceAtLeast(1)
    val start = pageSize * (page - 1) + 1
    val end = minOf(pageSize * page, totalCount)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("$start - $end 件 / 全 $totalCount 件", fontSize = 10.sp, color = Color(0xFF64748B))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(10, 20, 50).forEach { size ->
                val active = pageSize == size
                Surface(shape = RoundedCornerShape(6.dp), color = if (active) InspAccent else Color(0xFFF1F5F9), modifier = Modifier.clickable { onPageSizeChange(size) }) {
                    Text("$size", modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else Color(0xFF64748B))
                }
            }
            IconButton(onClick = { onPageChange(page - 1) }, enabled = page > 1, modifier = Modifier.size(26.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(16.dp))
            }
            Text("$page / $totalPages", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { onPageChange(page + 1) }, enabled = page < totalPages, modifier = Modifier.size(26.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MaterialInspectionDetailDialog(item: MaterialLogItemDto, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
        ) {
            Box(Modifier.fillMaxWidth().background(InspGradient).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("検品詳細情報", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                inspDetailRow("検品日", item.logDate)
                inspDetailRow("時間", item.logTime)
                inspDetailRow("材料名", item.materialName)
                inspDetailRow("仕入先", item.supplier)
                inspDetailRow("製造番号", item.manufactureNo)
                inspDetailRow("数量", item.quantity?.toString())
                inspDetailRow("外径1/2", "${item.outerDiameter1 ?: "-"} / ${item.outerDiameter2 ?: "-"}")
                inspDetailRow("材料規格", item.materialQuality)
                inspDetailRow("検品者", item.inspectorDisplayName())
                inspDetailRow("備考", item.remarks)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 4.dp)) {
                Text("閉じる", color = InspAccent)
            }
        }
    }
}

@Composable
private fun inspDetailRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.width(72.dp))
        Text(value.orEmpty().ifBlank { "—" }, fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
    }
}

@Composable
fun MaterialInspectionQualityEditDialog(
    item: MaterialMasterItemDto,
    form: MaterialQualityEditForm,
    saving: Boolean,
    onFormChange: (MaterialQualityEditForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
        ) {
            Row(
                Modifier.fillMaxWidth().background(InspGradient).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("品質基準編集", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(item.materialName.orEmpty(), color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("材料情報", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            inspBadge("材料名", item.materialName.orEmpty())
                            inspBadge("標準仕様", item.standardSpec.orEmpty().ifBlank { "—" })
                            inspBadge("仕入先", item.supplierName.orEmpty().ifBlank { "—" })
                        }
                    }
                }
                Text("許容・範囲", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    inspField("許容範囲", form.toleranceRange, Modifier.weight(1f)) { onFormChange(form.copy(toleranceRange = it)) }
                    inspField("範囲値", form.rangeValue, Modifier.weight(1f)) { onFormChange(form.copy(rangeValue = it)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    inspField("許容値1", form.tolerance1, Modifier.weight(1f)) { onFormChange(form.copy(tolerance1 = it)) }
                    inspField("許容値2", form.tolerance2, Modifier.weight(1f)) { onFormChange(form.copy(tolerance2 = it)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    inspField("最小値", form.minValue, Modifier.weight(1f)) { onFormChange(form.copy(minValue = it)) }
                    inspField("最大値", form.maxValue, Modifier.weight(1f)) { onFormChange(form.copy(maxValue = it)) }
                }
                Text("実測値", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    inspField("実測値1", form.actualValue1, Modifier.weight(1f)) { onFormChange(form.copy(actualValue1 = it)) }
                    inspField("実測値2", form.actualValue2, Modifier.weight(1f)) { onFormChange(form.copy(actualValue2 = it)) }
                    inspField("実測値3", form.actualValue3, Modifier.weight(1f)) { onFormChange(form.copy(actualValue3 = it)) }
                }
                inspField("代表モデル", form.representativeModel, Modifier.fillMaxWidth()) { onFormChange(form.copy(representativeModel = it)) }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("キャンセル", color = Color(0xFF64748B)) }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(InspGradient)
                        .clickable(enabled = !saving, onClick = onSave)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    if (saving) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("保存", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun inspBadge(key: String, value: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(key, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
            Text(value, fontSize = 10.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun inspField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = InspAccent, unfocusedBorderColor = Color(0xFFE2E8F0)),
    )
}
