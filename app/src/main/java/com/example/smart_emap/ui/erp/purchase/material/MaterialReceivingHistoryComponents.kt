package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog

private val ReceivingAccent = Color(0xFF4F46E5)
private val ReceivingAccentSoft = Color(0xFFEEF2FF)
private val ReceivingGradient = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)))
private val CardShape = RoundedCornerShape(10.dp)

private data class ReceivingColumnDef(
    val key: String,
    val label: String,
    val weight: Float,
    val minWidth: Dp = 56.dp,
    val alignStart: Boolean = false,
    val maxLines: Int = 1,
    val visible: (MaterialReceivingColumnVisibility) -> Boolean,
    val value: (MaterialLogItemDto) -> String,
)

private fun receivingColumns(cols: MaterialReceivingColumnVisibility): List<ReceivingColumnDef> {
    val all = listOf(
        ReceivingColumnDef("logDate", "日付", 0.9f, 72.dp, visible = { it.logDate }) { it.logDate.orEmpty() },
        ReceivingColumnDef("logTime", "時間", 0.75f, 60.dp, visible = { it.logTime }) { it.logTime.orEmpty() },
        ReceivingColumnDef("item", "項目", 0.85f, 72.dp, visible = { it.item }) { it.item.orEmpty() },
        ReceivingColumnDef("materialCd", "材料CD", 0.9f, 76.dp, visible = { it.materialCd }) { it.materialCd.orEmpty() },
        ReceivingColumnDef("materialName", "材料名", 1.6f, 100.dp, alignStart = true, maxLines = 2, visible = { it.materialName }) { it.materialName.orEmpty() },
        ReceivingColumnDef("processCd", "工程CD", 0.85f, 72.dp, visible = { it.processCd }) { it.processCd.orEmpty() },
        ReceivingColumnDef("manufactureNo", "製造番号", 1.1f, 88.dp, visible = { it.manufactureNo }) { it.manufactureNo.orEmpty() },
        ReceivingColumnDef("manufactureDate", "製造日", 0.9f, 72.dp, visible = { it.manufactureDate }) { it.manufactureDate.orEmpty() },
        ReceivingColumnDef("piecesPerBundle", "束当り枚数", 0.9f, 72.dp, visible = { it.piecesPerBundle }) { it.piecesPerBundle?.toString().orEmpty() },
        ReceivingColumnDef("length", "長さ", 0.7f, 56.dp, visible = { it.length }) { it.length?.toString().orEmpty() },
        ReceivingColumnDef("quantity", "数量", 0.7f, 56.dp, visible = { it.quantity }) { it.quantity?.toString().orEmpty() },
        ReceivingColumnDef("bundleQuantity", "束数", 0.7f, 56.dp, visible = { it.bundleQuantity }) { it.bundleQuantity?.toString().orEmpty() },
        ReceivingColumnDef("outerDiameter1", "外径1", 0.75f, 60.dp, visible = { it.outerDiameter1 }) { it.outerDiameter1?.toString().orEmpty() },
        ReceivingColumnDef("outerDiameter2", "外径2", 0.75f, 60.dp, visible = { it.outerDiameter2 }) { it.outerDiameter2?.toString().orEmpty() },
        ReceivingColumnDef("supplier", "仕入先", 1.3f, 88.dp, alignStart = true, maxLines = 2, visible = { it.supplier }) { it.supplier.orEmpty() },
        ReceivingColumnDef("materialQuality", "材料規格", 1.1f, 80.dp, alignStart = true, maxLines = 2, visible = { it.materialQuality }) { it.materialQuality.orEmpty() },
        ReceivingColumnDef("magnetic", "磁気", 0.6f, 48.dp, visible = { it.magnetic }) { formatMagneticLabel(it.magnetic) },
        ReceivingColumnDef("appearance", "外観", 0.6f, 48.dp, visible = { it.appearance }) { formatAppearanceLabel(it.appearance) },
        ReceivingColumnDef("hdNo", "HD番号", 1.1f, 88.dp, visible = { it.hdNo }) { it.hdNo.orEmpty() },
        ReceivingColumnDef("remarks", "作業員", 1.2f, 80.dp, alignStart = true, maxLines = 2, visible = { it.remarks }) { it.remarks.orEmpty() },
        ReceivingColumnDef("note", "ノート", 1.2f, 80.dp, alignStart = true, maxLines = 2, visible = { it.note }) { it.note.orEmpty() },
        ReceivingColumnDef("createdAt", "作成日時", 1.2f, 96.dp, visible = { it.createdAt }) { it.createdAt.orEmpty() },
        ReceivingColumnDef("updatedAt", "更新日時", 1.2f, 96.dp, visible = { it.updatedAt }) { it.updatedAt.orEmpty() },
    )
    return all.filter { it.visible(cols) }
}

private fun formatMagneticLabel(value: String?) = when {
    value.isNullOrBlank() -> "無"
    value == "1" || value.equals("true", true) || value == "有" -> "有"
    else -> value
}

private fun formatAppearanceLabel(value: String?) = when {
    value.isNullOrBlank() -> "不良"
    value == "1" || value.equals("true", true) || value == "良" -> "良"
    else -> value
}

@Composable
fun MaterialReceivingHeroBar(totalCount: Int, displayCount: Int) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = Color(0x404F46E5))
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape),
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(56.dp)
                .background(ReceivingGradient)
                .align(Alignment.CenterStart),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ReceivingGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Description, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("材料受入履歴", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text("材料の受入・検品を管理します", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReceivingStatPill(Icons.Default.Description, totalCount.toString(), "総件数", Color(0xFF4F46E5))
                ReceivingStatPill(Icons.Default.Visibility, displayCount.toString(), "表示件数", Color(0xFF0EA5E9))
            }
        }
    }
}

@Composable
private fun ReceivingStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
) {
    Surface(shape = RoundedCornerShape(8.dp), color = tint.copy(alpha = 0.08f), border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.15f))) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B), lineHeight = 14.sp)
                Text(label, fontSize = 8.sp, color = Color(0xFF64748B))
            }
        }
    }
}

private fun receivingMinTableWidth(columns: List<ReceivingColumnDef>): Dp =
    columns.fold(0.dp) { acc, col -> acc + col.minWidth } +
        3.dp * (columns.size - 1).coerceAtLeast(0) + 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialReceivingSearchFilterBar(
    keyword: String,
    startDate: String,
    endDate: String,
    supplierOptions: List<String>,
    selectedSuppliers: List<String>,
    sortField: MaterialReceivingSortField?,
    sortOrder: MaterialReceivingSortOrder,
    actionLoading: Boolean,
    printLoading: Boolean,
    importLoading: Boolean,
    canPrint: Boolean,
    onKeywordChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onSupplierChange: (List<String>) -> Unit,
    onSortFieldChange: (MaterialReceivingSortField?) -> Unit,
    onSortOrderChange: (MaterialReceivingSortOrder) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onColumnSettings: () -> Unit,
    onPrint: () -> Unit,
    onImport: () -> Unit,
) {
    val scroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                .horizontalScroll(scroll)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            ReceivingFilterSection(isFirst = true) {
                Icon(Icons.Default.Search, null, tint = ReceivingAccent, modifier = Modifier.size(14.dp))
                Text("検索・フィルター", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF334155))
            }
            ReceivingFilterDivider()
            ReceivingFilterSection {
                ReceivingFieldLabel(Icons.Default.Search, "キーワード")
                ReceivingInlineKeywordField(keyword, onKeywordChange, onSearch)
            }
            ReceivingFilterDivider()
            ReceivingFilterSection {
                ReceivingFieldLabel(Icons.Default.CalendarMonth, "期間")
                ReceivingInlineDateRangeField(startDate, endDate, onStartChange, onEndChange)
            }
            ReceivingFilterDivider()
            ReceivingFilterSection {
                ReceivingFieldLabel(Icons.Default.FilterList, "仕入先")
                ReceivingInlineSupplierSelect(selectedSuppliers, supplierOptions, onSupplierChange)
            }
            ReceivingFilterDivider()
            ReceivingFilterSection {
                ReceivingFieldLabel(Icons.Default.Sort, "並び順")
                ReceivingSortFieldSelect(sortField, onSortFieldChange, Modifier.widthIn(min = 100.dp, max = 140.dp))
                Spacer(Modifier.width(4.dp))
                ReceivingSortOrderSelect(sortOrder, sortField != null, onSortOrderChange, Modifier.width(72.dp))
            }
            ReceivingFilterDivider()
            ReceivingFilterSection {
                val shape = RoundedCornerShape(6.dp)
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(shape)
                        .background(ReceivingGradient)
                        .clickable(onClick = onSearch)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("検索", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
            ReceivingFilterDivider()
            ReceivingFilterSection(isLast = true) {
                ReceivingMiniBtn("クリア", Icons.Default.Refresh, enabled = !actionLoading, onClick = onClear)
                ReceivingMiniBtn("列設定", Icons.Default.Settings, enabled = !actionLoading, onClick = onColumnSettings)
                ReceivingMiniBtn("印刷", Icons.Default.Print, enabled = canPrint && !printLoading && !actionLoading, loading = printLoading, onClick = onPrint)
                ReceivingMiniBtn("データ読取", Icons.Default.Upload, accent = true, enabled = !importLoading && !actionLoading, loading = importLoading, onClick = onImport)
            }
        }
    }
}

@Composable
private fun ReceivingFilterSection(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(
            start = if (isFirst) 0.dp else 10.dp,
            end = if (isLast) 0.dp else 10.dp,
        ),
        content = content,
    )
}

@Composable
private fun ReceivingFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(Color(0xFFE5E7EB)),
    )
}

@Composable
private fun ReceivingInlineKeywordField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .widthIn(min = 120.dp, max = 180.dp)
            .height(32.dp),
        placeholder = { Text("材料名・仕入先・製造番号", fontSize = 10.sp, maxLines = 1) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ReceivingAccent,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8FAFC),
        ),
    )
}

@Composable
private fun ReceivingInlineDateRangeField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = ReceivingAccent,
            onDismiss = { pickStart = false },
            onConfirm = {
                onStartChange(it)
                pickStart = false
                pickEnd = true
            },
        )
    }
    if (pickEnd) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = ReceivingAccent,
            onDismiss = { pickEnd = false },
            onConfirm = {
                onEndChange(it)
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
            .widthIn(min = 160.dp, max = 220.dp)
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .clickable { pickStart = true }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.CalendarMonth, null, tint = ReceivingAccent, modifier = Modifier.size(12.dp))
        Text(
            display,
            fontSize = 11.sp,
            color = if (startDate.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivingInlineSupplierSelect(
    selected: List<String>,
    options: List<String>,
    onChange: (List<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val display = when {
        selected.isEmpty() -> "全て"
        selected.size == 1 -> selected.first()
        else -> "${selected.first()} +${selected.size - 1}"
    }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .widthIn(min = 110.dp, max = 160.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(display, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (selected.isNotEmpty()) {
                Icon(Icons.Default.Close, "クリア", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).clickable { onChange(emptyList()) })
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 260.dp)) {
            DropdownMenuItem(text = { Text("全て", fontSize = 12.sp) }, onClick = { onChange(emptyList()); expanded = false })
            options.forEach { name ->
                val checked = name in selected
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.size(20.dp))
                            Text(name, fontSize = 12.sp, maxLines = 2)
                        }
                    },
                    onClick = { onChange(if (checked) selected - name else selected + name) },
                )
            }
        }
    }
}

@Composable
private fun ReceivingMiniBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    loading: Boolean = false,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val bg = if (accent) ReceivingGradient else Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = if (accent) Color.White else ReceivingAccent)
            } else {
                Icon(icon, null, tint = if (accent) Color.White else Color(0xFF64748B), modifier = Modifier.size(12.dp))
            }
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (accent) Color.White else Color(0xFF475569))
        }
    }
}

@Composable
private fun ReceivingFieldLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = ReceivingAccent, modifier = Modifier.size(12.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivingSortFieldSelect(
    value: MaterialReceivingSortField?,
    onChange: (MaterialReceivingSortField?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value?.label ?: "項目", fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("なし", fontSize = 12.sp) }, onClick = { onChange(null); expanded = false })
            MaterialReceivingSortField.entries.forEach { field ->
                DropdownMenuItem(text = { Text(field.label, fontSize = 12.sp) }, onClick = { onChange(field); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivingSortOrderSelect(
    value: MaterialReceivingSortOrder,
    enabled: Boolean,
    onChange: (MaterialReceivingSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(if (enabled) Color(0xFFF8FAFC) else Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (value == MaterialReceivingSortOrder.ASC) "昇順" else "降順", fontSize = 11.sp, color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8))
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("昇順", fontSize = 12.sp) }, onClick = { onChange(MaterialReceivingSortOrder.ASC); expanded = false })
            DropdownMenuItem(text = { Text("降順", fontSize = 12.sp) }, onClick = { onChange(MaterialReceivingSortOrder.DESC); expanded = false })
        }
    }
}

@Composable
fun MaterialReceivingHistoryTable(
    items: List<MaterialLogItemDto>,
    visibleColumns: MaterialReceivingColumnVisibility,
    isLoading: Boolean,
    totalCount: Int,
    onRowClick: (MaterialLogItemDto) -> Unit,
) {
    val columns = remember(visibleColumns) { receivingColumns(visibleColumns) }
    val hScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.FilterList, null, tint = ReceivingAccent, modifier = Modifier.size(14.dp))
                Text("材料ログ一覧", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF334155))
                if (totalCount > 0) {
                    Surface(shape = RoundedCornerShape(10.dp), color = ReceivingAccentSoft) {
                        Text("$totalCount 件", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ReceivingAccent)
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "receiving-table-loading",
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ReceivingAccent, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                } else if (items.isEmpty()) {
                    Text(
                        "該当する受入履歴がありません",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                    )
                } else {
                    val minWidth = receivingMinTableWidth(columns)
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val needsScroll = minWidth > maxWidth
                        val tableModifier = if (needsScroll) {
                            Modifier.horizontalScroll(hScroll).widthIn(min = minWidth)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                        Column(tableModifier) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                columns.forEach { col ->
                                    ReceivingHeaderCell(col)
                                }
                            }
                            items.forEachIndexed { index, row ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(180, delayMillis = (index.coerceAtMost(8)) * 30)) +
                                        scaleIn(initialScale = 0.98f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = if (columns.any { it.maxLines > 1 }) 40.dp else 32.dp)
                                            .clickable { onRowClick(row) }
                                            .background(if (index % 2 == 1) Color(0xFFFAFBFC) else Color.White)
                                            .padding(horizontal = 6.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        columns.forEach { col ->
                                            val text = col.value(row)
                                            ReceivingBodyCell(
                                                col = col,
                                                text = text,
                                                tint = when (col.key) {
                                                    "item" -> itemTypeColor(row.item)
                                                    "materialName" -> Color(0xFF2563EB)
                                                    else -> Color(0xFF334155)
                                                },
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ReceivingHeaderCell(col: ReceivingColumnDef) {
    val align = if (col.alignStart) TextAlign.Start else TextAlign.Center
    Text(
        text = col.label,
        modifier = Modifier
            .weight(col.weight)
            .widthIn(min = col.minWidth)
            .padding(horizontal = 2.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
        lineHeight = 11.sp,
    )
}

@Composable
private fun RowScope.ReceivingBodyCell(
    col: ReceivingColumnDef,
    text: String,
    tint: Color = Color(0xFF334155),
) {
    val align = if (col.alignStart) TextAlign.Start else TextAlign.Center
    Text(
        text = text.ifBlank { "—" },
        modifier = Modifier
            .weight(col.weight)
            .widthIn(min = col.minWidth)
            .padding(horizontal = 2.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.Normal,
        color = tint,
        maxLines = col.maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
        lineHeight = 11.sp,
    )
}

private fun itemTypeColor(item: String?): Color = when (item) {
    "材料受入" -> Color(0xFF16A34A)
    "材料検品" -> Color(0xFFD97706)
    "材料出庫" -> Color(0xFF2563EB)
    "材料返品" -> Color(0xFFDC2626)
    else -> Color(0xFF4F46E5)
}

@Composable
fun MaterialReceivingPaginationBar(
    page: Int,
    pageSize: Int,
    totalCount: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    if (totalCount <= 0) return
    val totalPages = ((totalCount + pageSize - 1) / pageSize).coerceAtLeast(1)
    val start = pageSize * (page - 1) + 1
    val end = minOf(pageSize * page, totalCount)
    val scroll = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("$start - $end 件 / 全 $totalCount 件", fontSize = 10.sp, color = Color(0xFF64748B))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(10, 20, 50).forEach { size ->
                    val active = pageSize == size
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (active) ReceivingAccent else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { onPageSizeChange(size) },
                    ) {
                        Text("$size", modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else Color(0xFF64748B))
                    }
                }
                IconButton(onClick = { onPageChange(page - 1) }, enabled = page > 1, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                }
                Text("$page / $totalPages", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                IconButton(onClick = { onPageChange(page + 1) }, enabled = page < totalPages, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun MaterialReceivingDetailDialog(item: MaterialLogItemDto, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(ReceivingGradient)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("材料ログ詳細", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                detailRow("項目", item.item)
                detailRow("材料CD", item.materialCd)
                detailRow("材料名", item.materialName)
                detailRow("工程CD", item.processCd)
                detailRow("日時", "${item.logDate.orEmpty()} ${item.logTime.orEmpty()}")
                detailRow("製造番号", item.manufactureNo)
                detailRow("数量", item.quantity?.toString())
                detailRow("外径1/2", "${item.outerDiameter1 ?: "-"} / ${item.outerDiameter2 ?: "-"}")
                detailRow("仕入先", item.supplier)
                detailRow("材料規格", item.materialQuality)
                detailRow("HD番号", item.hdNo)
                detailRow("作業員", item.remarks)
                detailRow("ノート", item.note)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 4.dp)) {
                Text("閉じる", color = ReceivingAccent)
            }
        }
    }
}

@Composable
private fun detailRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, modifier = Modifier.width(72.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        Text(value.orEmpty().ifBlank { "—" }, fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialReceivingColumnSettingsDialog(
    draft: MaterialReceivingColumnVisibility,
    onToggle: (String, Boolean) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val groups = listOf(
        "基本情報" to listOf("logDate" to "日付", "logTime" to "時間", "item" to "項目", "materialCd" to "材料CD", "materialName" to "材料名", "processCd" to "工程CD"),
        "製造情報" to listOf("manufactureNo" to "製造番号", "manufactureDate" to "製造日", "piecesPerBundle" to "束当り枚数", "length" to "長さ"),
        "数量・品質" to listOf("quantity" to "数量", "bundleQuantity" to "束数", "outerDiameter1" to "外径1", "outerDiameter2" to "外径2", "magnetic" to "磁気", "appearance" to "外観"),
        "仕入先・規格" to listOf("supplier" to "仕入先", "materialQuality" to "材料規格"),
        "その他" to listOf("hdNo" to "HD番号", "remarks" to "作業員", "note" to "ノート", "createdAt" to "作成日時", "updatedAt" to "更新日時"),
    )
    fun isChecked(key: String) = when (key) {
        "logDate" -> draft.logDate
        "logTime" -> draft.logTime
        "item" -> draft.item
        "materialCd" -> draft.materialCd
        "materialName" -> draft.materialName
        "processCd" -> draft.processCd
        "manufactureNo" -> draft.manufactureNo
        "manufactureDate" -> draft.manufactureDate
        "piecesPerBundle" -> draft.piecesPerBundle
        "length" -> draft.length
        "quantity" -> draft.quantity
        "bundleQuantity" -> draft.bundleQuantity
        "outerDiameter1" -> draft.outerDiameter1
        "outerDiameter2" -> draft.outerDiameter2
        "supplier" -> draft.supplier
        "materialQuality" -> draft.materialQuality
        "magnetic" -> draft.magnetic
        "appearance" -> draft.appearance
        "hdNo" -> draft.hdNo
        "remarks" -> draft.remarks
        "note" -> draft.note
        "createdAt" -> draft.createdAt
        "updatedAt" -> draft.updatedAt
        else -> false
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 480.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
        ) {
            Text("列表示設定", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                groups.forEach { (title, cols) ->
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ReceivingAccent)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        cols.forEach { (key, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .clickable { onToggle(key, !isChecked(key)) }
                                    .padding(end = 6.dp),
                            ) {
                                Checkbox(checked = isChecked(key), onCheckedChange = { onToggle(key, it) }, modifier = Modifier.size(28.dp))
                                Text(label, fontSize = 11.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onReset) { Text("リセット", fontSize = 11.sp) }
                TextButton(onClick = onDismiss) { Text("キャンセル", fontSize = 11.sp) }
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = ReceivingAccent), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("保存", fontSize = 11.sp)
                }
            }
        }
    }
}
