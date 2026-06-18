package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import com.example.smart_emap.data.repository.MaterialStockStatsUi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.util.Locale

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

private data class KpiCardSpec(
    val label: String,
    val value: String,
    val unit: String = "",
    val accent: Brush,
    val icon: ImageVector,
)

@Composable
fun MaterialOrderHeroBar(
    actionLoading: Boolean,
    onSyncMaster: () -> Unit,
    onGenerateData: () -> Unit,
    onCalculateStock: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                "材料在庫管理(発注・使用)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MaterialOrderActionBtn(
                text = "材料マスタ更新",
                icon = Icons.Default.Refresh,
                container = Color.White.copy(alpha = 0.18f),
                content = Color.White,
                enabled = !actionLoading,
                onClick = onSyncMaster,
            )
            MaterialOrderActionBtn(
                text = "データ生成",
                icon = Icons.Default.Inventory2,
                container = Color(0xE643E97B),
                content = Color.White,
                enabled = !actionLoading,
                onClick = onGenerateData,
            )
            MaterialOrderActionBtn(
                text = "在庫計算",
                icon = Icons.Default.TrendingUp,
                container = Color(0xE6FAAD14),
                content = Color.White,
                enabled = !actionLoading,
                onClick = onCalculateStock,
            )
        }
    }
}

@Composable
private fun MaterialOrderActionBtn(
    text: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun MaterialOrderKpiStrip(stats: MaterialStockStatsUi) {
    val cards = listOf(
        KpiCardSpec("総材料種類数", jpNumber.format(stats.totalMaterials), "", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Inventory2),
        KpiCardSpec("在庫数合計", jpNumber.format(stats.totalCurrentStock), "束", Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))), Icons.Default.Inventory2),
        KpiCardSpec("平均kg単価", "¥${jpNumber.format(stats.averageUnitPrice.toLong())}", "", Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))), Icons.Default.AttachMoney),
        KpiCardSpec("使用数合計", jpNumber.format(stats.totalUsageQuantity), "", Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))), Icons.Default.TrendingUp),
        KpiCardSpec("注文束数", jpNumber.format(stats.totalOrderQuantity), "束", Brush.linearGradient(listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3))), Icons.Default.ShoppingCart),
        KpiCardSpec("注文本数", jpNumber.format(stats.totalOrderBundleQuantity), "本", Brush.linearGradient(listOf(Color(0xFFD299C2), Color(0xFFFEF9D7))), Icons.Default.Inventory2),
        KpiCardSpec("注文総重量", jpNumber.format(stats.totalBundleWeight.toLong()), "kg", Brush.linearGradient(listOf(Color(0xFFFFECD2), Color(0xFFFCB69F))), Icons.Default.TrendingUp),
        KpiCardSpec("参考注文金額", "¥${jpNumber.format(stats.totalOrderAmount.toLong())}", "", Brush.linearGradient(listOf(Color(0xFFA8CABA), Color(0xFF5D4E75))), Icons.Default.AttachMoney),
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(cards) { card ->
            MaterialOrderKpiCard(card)
        }
    }
}

@Composable
private fun MaterialOrderKpiCard(spec: KpiCardSpec) {
    Surface(
        modifier = Modifier.width(118.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(spec.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(spec.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(spec.value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    if (spec.unit.isNotBlank()) {
                        Text(spec.unit, fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 2.dp, bottom = 1.dp))
                    }
                }
                Text(spec.label, fontSize = 9.sp, color = Color(0xFF64748B), maxLines = 2, lineHeight = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialOrderFilterBar(
    showDateFilter: Boolean,
    startDate: String,
    endDate: String,
    keyword: String,
    supplierOptions: List<String>,
    selectedSuppliers: List<String>,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onToday: () -> Unit,
    onKeywordChange: (String) -> Unit,
    onSupplierChange: (List<String>) -> Unit,
    onSearch: () -> Unit,
    keywordPlaceholder: String = "材料名 / 材料CD",
) {
    val filterAccent = Color(0xFF667EEA)
    val filterShape = RoundedCornerShape(10.dp)
    val scroll = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, filterShape, spotColor = Color(0x12000000))
            .clip(filterShape)
            .background(Color.White)
            .border(1.dp, Color(0x1A667EEA), filterShape)
            .horizontalScroll(scroll)
            .height(36.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDateFilter) {
            MaterialFilterSection(isFirst = true) {
                MaterialFilterLabel(Icons.Default.CalendarMonth, "期間", filterAccent)
                MaterialFilterDateRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    accent = filterAccent,
                    onStartChange = {
                        onStartChange(it)
                        onSearch()
                    },
                    onEndChange = {
                        onEndChange(it)
                        onSearch()
                    },
                )
                MaterialFilterDateNavGroup(
                    accent = filterAccent,
                    onPrev = { onShiftDate(-1) },
                    onToday = onToday,
                    onNext = { onShiftDate(1) },
                )
            }
            MaterialFilterDivider()
        }

        MaterialFilterSection(isFirst = !showDateFilter) {
            MaterialFilterLabel(Icons.Default.Search, "キーワード", filterAccent)
            MaterialFilterKeywordField(
                value = keyword,
                placeholder = keywordPlaceholder,
                onValueChange = onKeywordChange,
            )
        }

        MaterialFilterDivider()

        MaterialFilterSection(isLast = true) {
            MaterialFilterLabel(Icons.Default.Person, "仕入先", filterAccent)
            MaterialFilterSupplierField(
                selected = selectedSuppliers,
                options = supplierOptions,
                onSelectionChange = {
                    onSupplierChange(it)
                    onSearch()
                },
            )
        }
    }
}

@Composable
private fun MaterialFilterSection(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(
            start = if (isFirst) 0.dp else 12.dp,
            end = if (isLast) 0.dp else 12.dp,
        ),
    ) {
        content()
    }
}

@Composable
private fun MaterialFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(Color(0xFFE5E7EB)),
    )
}

@Composable
private fun MaterialFilterLabel(
    icon: ImageVector,
    text: String,
    accent: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B),
            maxLines = 1,
        )
    }
}

@Composable
private fun MaterialFilterDateRangeField(
    startDate: String,
    endDate: String,
    accent: Color,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = accent,
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
            accent = accent,
            onDismiss = { pickEnd = false },
            onConfirm = {
                onEndChange(it)
                pickEnd = false
            },
        )
    }
    val displayText = when {
        startDate.isNotBlank() && endDate.isNotBlank() -> "$startDate ~ $endDate"
        startDate.isNotBlank() -> startDate
        endDate.isNotBlank() -> endDate
        else -> "開始日 ~ 終了日"
    }
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .clickable { pickStart = true }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
        Text(
            displayText,
            fontSize = 11.sp,
            color = if (startDate.isBlank() && endDate.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MaterialFilterDateNavGroup(
    accent: Color,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaterialFilterNavButton(onClick = onPrev) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前日",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp),
            )
        }
        MaterialFilterTodayButton(onClick = onToday, accent = accent)
        MaterialFilterNavButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "翌日",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun MaterialFilterNavButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 28.dp)
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MaterialFilterTodayButton(onClick: () -> Unit, accent: Color) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(accent, Color(0xFF764BA2))))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("今日", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun MaterialFilterKeywordField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .widthIn(min = 140.dp, max = 200.dp)
            .height(32.dp),
        placeholder = { Text(placeholder, fontSize = 11.sp, maxLines = 1) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF667EEA),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedTextColor = Color(0xFF334155),
            unfocusedTextColor = Color(0xFF334155),
            cursorColor = Color(0xFF667EEA),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialFilterSupplierField(
    selected: List<String>,
    options: List<String>,
    onSelectionChange: (List<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val displayText = when {
        selected.isEmpty() -> "全て"
        selected.size == 1 -> selected.first()
        else -> "${selected.first()} +${selected.size - 1}"
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 140.dp, max = 220.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                displayText,
                fontSize = 11.sp,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (selected.isNotEmpty()) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "選択をクリア",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectionChange(emptyList()) },
                        ),
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 220.dp, max = 320.dp)
                .heightIn(max = 280.dp),
        ) {
            DropdownMenuItem(
                text = { Text("全て", fontSize = 12.sp) },
                onClick = {
                    onSelectionChange(emptyList())
                    expanded = false
                },
            )
            options.forEach { name ->
                val checked = name in selected
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(name, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    onClick = {
                        val next = if (checked) selected - name else selected + name
                        onSelectionChange(next)
                    },
                )
            }
        }
    }
}

@Composable
fun MaterialOrderTablePanel(
    selectedTab: MaterialOrderTab,
    onTabSelect: (MaterialOrderTab) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    headerActions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val outerShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, outerShape, spotColor = Color(0x18000000))
            .clip(outerShape)
            .background(Color.White)
            .border(1.dp, Color(0x0F667EEA), outerShape),
    ) {
        MaterialOrderTabStrip(
            selected = selectedTab,
            onSelect = onTabSelect,
            embedded = true,
            trailingActions = headerActions,
        )
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
            }
        } else {
            content()
        }
    }
}

private data class MaterialTabTheme(
    val icon: ImageVector,
    val activeGradient: Brush,
    val idleIcon: Color,
    val idleText: Color = Color(0xFF6B7280),
)

private fun materialTabTheme(tab: MaterialOrderTab): MaterialTabTheme = when (tab) {
    MaterialOrderTab.Initial -> MaterialTabTheme(
        icon = Icons.Default.Inventory2,
        activeGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
        idleIcon = Color(0xFFD97706),
    )
    MaterialOrderTab.Daily -> MaterialTabTheme(
        icon = Icons.Default.CalendarMonth,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
        idleIcon = Color(0xFF667EEA),
    )
    MaterialOrderTab.Sub -> MaterialTabTheme(
        icon = Icons.Default.Description,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
        idleIcon = Color(0xFF6366F1),
    )
    MaterialOrderTab.Usage -> MaterialTabTheme(
        icon = Icons.Default.Build,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
        idleIcon = Color(0xFF3B82F6),
    )
    MaterialOrderTab.Order -> MaterialTabTheme(
        icon = Icons.Default.ShoppingCart,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
        idleIcon = Color(0xFF10B981),
    )
    MaterialOrderTab.OrderHistory -> MaterialTabTheme(
        icon = Icons.AutoMirrored.Filled.List,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
        idleIcon = Color(0xFF64748B),
    )
    MaterialOrderTab.UnusedReceiving -> MaterialTabTheme(
        icon = Icons.Default.ContentCopy,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF10B981))),
        idleIcon = Color(0xFF0D9488),
    )
}

@Composable
fun MaterialOrderTabStrip(
    selected: MaterialOrderTab,
    onSelect: (MaterialOrderTab) -> Unit,
    embedded: Boolean = false,
    trailingActions: @Composable (() -> Unit)? = null,
) {
    val scroll = rememberScrollState()
    val barShape = if (embedded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp) else RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!embedded) Modifier.shadow(2.dp, barShape).clip(barShape).background(Color.White) else Modifier)
            .background(
                Brush.linearGradient(listOf(Color(0xFFF8F9FA), Color(0xFFEEF2F7))),
                barShape,
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scroll)
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MaterialOrderTab.entries.forEach { tab ->
                MaterialOrderTabChip(
                    tab = tab,
                    active = tab == selected,
                    onSelect = onSelect,
                )
            }
        }
        trailingActions?.let { actions ->
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        }
    }
}

@Composable
private fun MaterialOrderTabChip(
    tab: MaterialOrderTab,
    active: Boolean,
    onSelect: (MaterialOrderTab) -> Unit,
) {
    val theme = materialTabTheme(tab)
    val tabShape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(tabShape)
            .then(
                if (active) {
                    Modifier
                        .background(theme.activeGradient)
                        .shadow(3.dp, tabShape, spotColor = Color(0x40667EEA))
                } else {
                    Modifier.background(Color.Transparent)
                },
            )
            .clickable { onSelect(tab) }
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                theme.icon,
                contentDescription = null,
                tint = if (active) Color.White else theme.idleIcon,
                modifier = Modifier.size(14.dp),
            )
            Text(
                tab.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) Color.White else theme.idleText,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun MaterialOrderTabActionButtons(
    actionLoading: Boolean,
    onAddManualOrder: () -> Unit,
    onPrintOrder: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MaterialOrderActionBtn(
            text = "材料注文追加",
            icon = Icons.Default.Add,
            container = Color(0xE643E97B),
            content = Color.White,
            enabled = !actionLoading,
            onClick = onAddManualOrder,
        )
        MaterialOrderActionBtn(
            text = "注文書発行",
            icon = Icons.Default.Print,
            container = Color(0xFF2563EB),
            content = Color.White,
            enabled = !actionLoading,
            onClick = onPrintOrder,
        )
    }
}

@Composable
fun MaterialOrderPurchaseTable(
    items: List<MaterialStockItemDto>,
    onOrderChange: (MaterialStockItemDto, Int) -> Unit,
    onRemarksChange: (MaterialStockItemDto, String) -> Unit,
) {
    val cols = MoCols.purchaseOrder
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("日付", cols[0])
            MoHeaderCell("材料CD", cols[1])
            MoHeaderCell("材料名", cols[2], align = TextAlign.Start)
            MoHeaderCell("仕入先", cols[3])
            MoHeaderCell("規格", cols[4])
            MoHeaderCell("現在在庫", cols[5])
            MoHeaderCell("注文束数", cols[6], Color(0xFFFDE68A))
            MoHeaderCell("注文本数", cols[7])
            MoHeaderCell("重量", cols[8])
            MoHeaderCell("注文金額", cols[9])
            MoHeaderCell("備考", cols[10], align = TextAlign.Start)
        }
        items.forEachIndexed { index, row ->
            MaterialOrderPurchaseRow(
                row = row,
                cols = cols,
                striped = index % 2 == 1,
                onOrderChange = { onOrderChange(row, it) },
                onRemarksChange = onRemarksChange,
            )
        }
    }
}

@Composable
private fun MaterialOrderPurchaseRow(
    row: MaterialStockItemDto,
    cols: List<MoColumnSpec>,
    striped: Boolean,
    onOrderChange: (Int) -> Unit,
    onRemarksChange: (MaterialStockItemDto, String) -> Unit,
) {
    val stock = row.currentStock ?: 0
    val bundleQty = row.orderBundleQuantity ?: 0
    val weight = row.bundleWeight ?: 0.0
    val amount = row.orderAmount ?: 0.0
    Row(
        modifier = Modifier.moDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.date.orEmpty(), cols[0])
        MoBodyCell(row.materialCd.orEmpty(), cols[1], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[2], Color(0xFF2563EB), align = TextAlign.Start)
        MoBodyCell(row.supplierName.orEmpty(), cols[3], Color(0xFF64748B))
        MoBodyCell(row.standardSpec.orEmpty(), cols[4], Color(0xFF64748B), align = TextAlign.Start)
        MoBodyCell(
            formatStockDisplay(stock),
            cols[5],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        MoStepperCell(
            value = row.orderQuantity ?: 0,
            spec = cols[6],
            bg = Color(0xFFFDE68A),
            blankWhenZero = true,
            onChange = onOrderChange,
        )
        MoBodyCell(formatStockDisplay(bundleQty), cols[7])
        MoBodyCell(
            if (bundleQty > 0) "${weight.roundToInt()}kg" else "",
            cols[8],
        )
        MoBodyCell(
            if (amount > 0.0) "¥${jpNumber.format(amount.roundToInt())}" else "",
            cols[9],
            Color(0xFF1E293B),
            bold = true,
        )
        MaterialStockRemarksCell(row = row, spec = cols[10], onRemarksChange = onRemarksChange)
    }
}

@Composable
fun MaterialOrderHistoryTable(items: List<MaterialStockItemDto>) {
    val cols = MoCols.purchaseOrder
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("日付", cols[0])
            MoHeaderCell("材料CD", cols[1])
            MoHeaderCell("材料名", cols[2], align = TextAlign.Start)
            MoHeaderCell("仕入先", cols[3])
            MoHeaderCell("規格", cols[4])
            MoHeaderCell("現在在庫", cols[5])
            MoHeaderCell("注文束数", cols[6], Color(0xFFFDE68A))
            MoHeaderCell("注文本数", cols[7])
            MoHeaderCell("重量(kg)", cols[8])
            MoHeaderCell("注文金額", cols[9])
            MoHeaderCell("備考", cols[10], align = TextAlign.Start)
        }
        items.forEachIndexed { index, row ->
            MaterialOrderHistoryRow(row = row, cols = cols, striped = index % 2 == 1)
        }
        if (items.isNotEmpty()) {
            MaterialOrderHistorySummaryRow(items = items, cols = cols)
        }
    }
}

@Composable
private fun MaterialOrderHistoryRow(
    row: MaterialStockItemDto,
    cols: List<MoColumnSpec>,
    striped: Boolean,
) {
    val stock = row.currentStock ?: 0
    val orderQty = row.orderQuantity ?: 0
    val bundleQty = row.orderBundleQuantity ?: 0
    val weight = row.bundleWeight ?: 0.0
    val amount = row.orderAmount ?: 0.0
    Row(
        modifier = Modifier.moDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.date.orEmpty(), cols[0])
        MoBodyCell(row.materialCd.orEmpty(), cols[1], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[2], Color(0xFF2563EB), align = TextAlign.Start)
        MoBodyCell(row.supplierName.orEmpty(), cols[3], Color(0xFF64748B))
        MoBodyCell(row.standardSpec.orEmpty(), cols[4], Color(0xFF64748B), align = TextAlign.Start)
        MoBodyCell(
            formatStockDisplay(stock),
            cols[5],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell(formatStockDisplay(orderQty), cols[6])
        MoBodyCell(formatStockDisplay(bundleQty), cols[7])
        MoBodyCell(
            if (orderQty > 0 || bundleQty > 0) "${weight.roundToInt()}" else "",
            cols[8],
            if (weight < 0) Color(0xFFEF4444) else Color(0xFF334155),
        )
        MoBodyCell(
            if (amount > 0.0) "¥${jpNumber.format(amount.roundToInt())}" else "",
            cols[9],
            if (amount < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell(row.remarks.orEmpty(), cols[10], Color(0xFF64748B), align = TextAlign.Start)
    }
}

@Composable
private fun MaterialOrderHistorySummaryRow(
    items: List<MaterialStockItemDto>,
    cols: List<MoColumnSpec>,
) {
    val totalOrderQty = items.sumOf { it.orderQuantity ?: 0 }
    val totalBundleQty = items.sumOf { it.orderBundleQuantity ?: 0 }
    val totalWeight = items.sumOf { (it.bundleWeight ?: 0.0).roundToInt() }
    val totalAmount = items.sumOf { (it.orderAmount ?: 0.0).roundToInt() }
    Row(
        modifier = Modifier.moSummaryRow(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell("合計", cols[0], Color(0xFF475569), bold = true)
        MoBodyCell("${items.size}件", cols[1], Color(0xFF475569), bold = true)
        MoBodyCell("", cols[2])
        MoBodyCell("", cols[3])
        MoBodyCell("", cols[4])
        MoBodyCell("", cols[5])
        MoBodyCell(
            if (totalOrderQty > 0) totalOrderQty.toString() else "",
            cols[6],
            Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell(
            if (totalBundleQty > 0) totalBundleQty.toString() else "",
            cols[7],
            Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell(
            if (totalWeight > 0) totalWeight.toString() else "",
            cols[8],
            Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell(
            if (totalAmount > 0) "¥${jpNumber.format(totalAmount)}" else "",
            cols[9],
            Color(0xFF1E293B),
            bold = true,
        )
        MoBodyCell("", cols[10])
    }
}

@Composable
private fun RowScope.MaterialStockRemarksCell(
    row: MaterialStockItemDto,
    spec: MoColumnSpec,
    onRemarksChange: (MaterialStockItemDto, String) -> Unit,
) {
    var text by remember(row.id) { mutableStateOf(row.remarks.orEmpty()) }
    LaunchedEffect(row.remarks) { text = row.remarks.orEmpty() }
    var isFocused by remember(row.id) { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth)
            .height(26.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (isFocused && !focus.isFocused && text != row.remarks.orEmpty()) {
                        onRemarksChange(row, text)
                    }
                    isFocused = focus.isFocused
                },
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text("備考を入力", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
                inner()
            },
        )
    }
}

private fun formatStockDisplay(value: Int): String =
    if (value == 0) "" else value.toString()

@Composable
fun MaterialOrderInitialStockTable(
    items: List<MaterialStockItemDto>,
    onInitialStockChange: (MaterialStockItemDto, Int) -> Unit,
    onAdjustmentChange: (MaterialStockItemDto, Int) -> Unit,
) {
    val cols = MoCols.initialStock
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("日付", cols[0])
            MoHeaderCell("仕入先", cols[1])
            MoHeaderCell("材料CD", cols[2])
            MoHeaderCell("材料名", cols[3], align = TextAlign.Start)
            MoHeaderCell("初期在庫", cols[4], Color(0xFFBBF7D0))
            MoHeaderCell("調整数", cols[5], Color(0xFFC7D2FE))
        }
        items.forEachIndexed { index, row ->
            MaterialOrderInitialStockRow(
                row = row,
                cols = cols,
                striped = index % 2 == 1,
                onInitialStockChange = { onInitialStockChange(row, it) },
                onAdjustmentChange = { onAdjustmentChange(row, it) },
            )
        }
    }
}

@Composable
private fun MaterialOrderInitialStockRow(
    row: MaterialStockItemDto,
    cols: List<MoColumnSpec>,
    striped: Boolean,
    onInitialStockChange: (Int) -> Unit,
    onAdjustmentChange: (Int) -> Unit,
) {
    val initial = row.initialStock ?: 0
    val initialBg = if (initial > 0) Color(0xFFD1FAE5) else Color(0xFFECFDF5)
    Row(
        modifier = Modifier.moDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.date.orEmpty().takeLast(5), cols[0])
        MoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        MoBodyCell(row.materialCd.orEmpty(), cols[2], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        MoStepperCell(
            value = initial,
            spec = cols[4],
            bg = initialBg,
            minValue = 0,
            onChange = onInitialStockChange,
        )
        MoStepperCell(
            value = row.adjustmentQuantity ?: 0,
            spec = cols[5],
            bg = Color(0xFFE0E7FF),
            minValue = null,
            onChange = onAdjustmentChange,
        )
    }
}

@Composable
fun MaterialOrderUsageTable(
    items: List<MaterialStockItemDto>,
    onUsageChange: (MaterialStockItemDto, Int) -> Unit,
) {
    val cols = MoCols.usage
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("日付", cols[0])
            MoHeaderCell("仕入先", cols[1])
            MoHeaderCell("材料CD", cols[2])
            MoHeaderCell("材料名", cols[3], align = TextAlign.Start)
            MoHeaderCell("現在在庫", cols[4])
            MoHeaderCell("使用数", cols[5], Color(0xFFBAE6FD))
        }
        items.forEachIndexed { index, row ->
            MaterialOrderUsageRow(
                row = row,
                cols = cols,
                striped = index % 2 == 1,
                onUsageChange = { onUsageChange(row, it) },
            )
        }
    }
}

@Composable
private fun MaterialOrderUsageRow(
    row: MaterialStockItemDto,
    cols: List<MoColumnSpec>,
    striped: Boolean,
    onUsageChange: (Int) -> Unit,
) {
    val stock = row.currentStock ?: 0
    Row(
        modifier = Modifier.moDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.date.orEmpty(), cols[0])
        MoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        MoBodyCell(row.materialCd.orEmpty(), cols[2], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        MoBodyCell(
            formatStockDisplay(stock),
            cols[4],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        MoStepperCell(
            value = row.plannedUsage ?: 0,
            spec = cols[5],
            bg = Color(0xFFBAE6FD),
            blankWhenZero = true,
            onChange = onUsageChange,
        )
    }
}

@Composable
fun MaterialOrderStockTable(
    items: List<MaterialStockItemDto>,
    showTransfer: Boolean,
    onUsageChange: (MaterialStockItemDto, Int) -> Unit,
    onOrderChange: (MaterialStockItemDto, Int) -> Unit,
    onTransfer: (MaterialStockItemDto) -> Unit,
) {
    val cols = if (showTransfer) MoCols.dailyStockTransfer else MoCols.dailyStock
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("日付", cols[0])
            MoHeaderCell("仕入先", cols[1])
            MoHeaderCell("CD", cols[2])
            MoHeaderCell("材料名", cols[3], align = TextAlign.Start)
            MoHeaderCell("安全", cols[4])
            MoHeaderCell("在庫", cols[5])
            MoHeaderCell("使用", cols[6], Color(0xFFBAE6FD))
            MoHeaderCell("束数", cols[7], Color(0xFFFDE68A))
            if (showTransfer) {
                MoHeaderCell("本数", cols[8])
                MoHeaderCell("重量", cols[9])
                MoHeaderCell("操作", cols[10])
            }
        }
        items.forEachIndexed { index, row ->
            MaterialOrderStockRow(
                row = row,
                cols = cols,
                showTransfer = showTransfer,
                striped = index % 2 == 1,
                onUsageChange = { onUsageChange(row, it) },
                onOrderChange = { onOrderChange(row, it) },
                onTransfer = { onTransfer(row) },
            )
        }
    }
}

@Composable
private fun MaterialOrderStockRow(
    row: MaterialStockItemDto,
    cols: List<MoColumnSpec>,
    showTransfer: Boolean,
    striped: Boolean = false,
    onUsageChange: (Int) -> Unit,
    onOrderChange: (Int) -> Unit,
    onTransfer: () -> Unit,
) {
    Row(
        modifier = Modifier.moDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.date.orEmpty().takeLast(5), cols[0])
        MoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        MoBodyCell(row.materialCd.orEmpty(), cols[2], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        MoBodyCell((row.safetyStock ?: 0).toString(), cols[4])
        MoBodyCell(
            (row.currentStock ?: 0).toString(),
            cols[5],
            if ((row.currentStock ?: 0) < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        MoStepperCell(
            value = row.plannedUsage ?: 0,
            spec = cols[6],
            bg = Color(0xFFE0F2FE),
            onChange = onUsageChange,
        )
        MoStepperCell(
            value = row.orderQuantity ?: 0,
            spec = cols[7],
            bg = Color(0xFFFEF9C3),
            onChange = onOrderChange,
        )
        if (showTransfer) {
            val bundleQty = row.orderBundleQuantity ?: 0
            MoBodyCell(if (bundleQty > 0) bundleQty.toString() else "", cols[8])
            MoBodyCell(
                if (bundleQty > 0) "${row.bundleWeight?.toInt() ?: 0}" else "",
                cols[9],
            )
            Box(
                modifier = Modifier
                    .weight(cols[10].weight)
                    .widthIn(min = cols[10].minWidth),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))))
                        .clickable(onClick = onTransfer)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("転送", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MaterialOrderSubTable(
    items: List<MaterialStockSubItemDto>,
    onUsageChange: (MaterialStockSubItemDto, Int) -> Unit,
    onRemarksChange: (MaterialStockSubItemDto, String) -> Unit,
    onLabelColorChange: (MaterialStockSubItemDto, String?) -> Unit,
    onDelete: (MaterialStockSubItemDto) -> Unit,
) {
    val cols = MoCols.subStock
    MoTableShell(columns = cols) {
        Row(
            modifier = Modifier.moTableHeaderRow(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            MoHeaderCell("材料CD", cols[0])
            MoHeaderCell("材料名", cols[1], align = TextAlign.Start)
            MoHeaderCell("仕入先", cols[2])
            MoHeaderCell("規格", cols[3])
            MoHeaderCell("注文束数", cols[4], Color(0xFFFDE68A))
            MoHeaderCell("注文本数", cols[5])
            MoHeaderCell("使用数", cols[6], Color(0xFFBAE6FD))
            MoHeaderCell("使用状態", cols[7])
            MoHeaderCell("ラベル色", cols[8])
            MoHeaderCell("備考", cols[9], align = TextAlign.Start)
            MoHeaderCell("操作", cols[10])
        }
        items.forEachIndexed { index, row ->
            MaterialOrderSubRow(
                row = row,
                cols = cols,
                striped = index % 2 == 1,
                onUsageChange = onUsageChange,
                onRemarksChange = onRemarksChange,
                onLabelColorChange = onLabelColorChange,
                onDelete = onDelete,
            )
        }
    }
}

private fun formatSubQty(value: Double?): String {
    val n = value?.toInt() ?: 0
    return if (n == 0) "" else n.toString()
}

private fun isSubItemUsed(row: MaterialStockSubItemDto): Boolean {
    val usage = row.plannedUsage?.toInt() ?: 0
    val order = row.orderQuantity?.toInt() ?: 0
    return usage == order
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialOrderSubRow(
    row: MaterialStockSubItemDto,
    cols: List<MoColumnSpec>,
    striped: Boolean,
    onUsageChange: (MaterialStockSubItemDto, Int) -> Unit,
    onRemarksChange: (MaterialStockSubItemDto, String) -> Unit,
    onLabelColorChange: (MaterialStockSubItemDto, String?) -> Unit,
    onDelete: (MaterialStockSubItemDto) -> Unit,
) {
    val usage = row.plannedUsage?.toInt() ?: 0
    val used = isSubItemUsed(row)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MoCols.subRowHeight)
            .background(if (striped) Color(0xFFFAFBFC) else Color.White)
            .drawBehindRowDivider(Color(0xFFE2E8F0))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        MoBodyCell(row.materialCd.orEmpty(), cols[0], Color(0xFF475569))
        MoBodyCell(row.materialName.orEmpty(), cols[1], Color(0xFF2563EB), align = TextAlign.Start)
        MoBodyCell(row.supplierName.orEmpty(), cols[2], Color(0xFF64748B))
        MoBodyCell(row.standardSpec.orEmpty(), cols[3], Color(0xFF64748B), align = TextAlign.Start)
        MoBodyCell(formatSubQty(row.orderQuantity), cols[4])
        MoBodyCell(formatSubQty(row.orderBundleQuantity), cols[5])
        MoStepperCell(
            value = usage,
            spec = cols[6],
            bg = Color(0xFFBAE6FD),
            onChange = { onUsageChange(row, it) },
        )
        Box(
            modifier = Modifier
                .weight(cols[7].weight)
                .widthIn(min = cols[7].minWidth),
            contentAlignment = Alignment.Center,
        ) {
            MaterialSubUsageStatusBadge(used = used)
        }
        MaterialSubLabelColorSelect(
            value = row.labelColor,
            spec = cols[8],
            onChange = { onLabelColorChange(row, it) },
        )
        MaterialSubRemarksCell(
            row = row,
            spec = cols[9],
            onRemarksChange = onRemarksChange,
        )
        MaterialSubDeleteButton(spec = cols[10], onClick = { onDelete(row) })
    }
}

@Composable
private fun MaterialSubUsageStatusBadge(used: Boolean) {
    val textColor = if (used) Color(0xFF16A34A) else Color(0xFFD97706)
    val bg = if (used) Color(0xFFDCFCE7) else Color(0xFFFFEDD5)
    val border = if (used) Color(0xFF86EFAC) else Color(0xFFFDBA74)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            if (used) "使用済" else "未使用",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.MaterialSubLabelColorSelect(
    value: String?,
    spec: MoColumnSpec,
    onChange: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val display = value?.takeIf { it.isNotBlank() } ?: "選択"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                display,
                fontSize = 9.sp,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("選択", fontSize = 11.sp) },
                onClick = {
                    onChange(null)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("白", fontSize = 11.sp) },
                onClick = {
                    onChange("白")
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("緑", fontSize = 11.sp) },
                onClick = {
                    onChange("緑")
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun RowScope.MaterialSubRemarksCell(
    row: MaterialStockSubItemDto,
    spec: MoColumnSpec,
    onRemarksChange: (MaterialStockSubItemDto, String) -> Unit,
) {
    var text by remember(row.id) { mutableStateOf(row.remarks.orEmpty()) }
    LaunchedEffect(row.remarks) {
        text = row.remarks.orEmpty()
    }
    val shape = RoundedCornerShape(6.dp)
    var isFocused by remember(row.id) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth)
            .height(26.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (isFocused && !focus.isFocused && text != row.remarks.orEmpty()) {
                        onRemarksChange(row, text)
                    }
                    isFocused = focus.isFocused
                },
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text("備考", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
                inner()
            },
        )
    }
}

@Composable
private fun RowScope.MaterialSubDeleteButton(
    spec: MoColumnSpec,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
                .clickable(onClick = onClick)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
            Text("削除", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MaterialOrderTableContainer(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, shape)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.98f)),
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
            }
        } else {
            content()
        }
    }
}
