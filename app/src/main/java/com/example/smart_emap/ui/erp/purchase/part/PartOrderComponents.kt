package com.example.smart_emap.ui.erp.purchase.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.repository.PartStockStatsUi
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
fun PartOrderHeroBar(
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
                "部品在庫管理(発注・使用)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PartOrderActionBtn(
                text = "部品マスタ更新",
                icon = Icons.Default.Refresh,
                container = Color.White.copy(alpha = 0.18f),
                content = Color.White,
                enabled = !actionLoading,
                onClick = onSyncMaster,
            )
            PartOrderActionBtn(
                text = "データ生成",
                icon = Icons.Default.Inventory2,
                container = Color(0xE643E97B),
                content = Color.White,
                enabled = !actionLoading,
                onClick = onGenerateData,
            )
            PartOrderActionBtn(
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
private fun PartOrderActionBtn(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartOrderKpiStrip(stats: PartStockStatsUi) {
    val cards = listOf(
        KpiCardSpec("総部品種類数", jpNumber.format(stats.totalParts), "", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Inventory2),
        KpiCardSpec("在庫数合計", jpNumber.format(stats.totalCurrentStock), "", Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))), Icons.Default.Inventory2),
        KpiCardSpec("平均単価", "¥${jpNumber.format(stats.averageUnitPrice.toLong())}", "", Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))), Icons.Default.AttachMoney),
        KpiCardSpec("使用数合計", jpNumber.format(stats.totalUsageQuantity), "", Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))), Icons.Default.TrendingUp),
        KpiCardSpec("注文本数", jpNumber.format(stats.totalOrderQuantity), "", Brush.linearGradient(listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3))), Icons.Default.ShoppingCart),
        KpiCardSpec("参考注文金額", "¥${jpNumber.format(stats.totalOrderValue.toLong())}", "", Brush.linearGradient(listOf(Color(0xFFA8CABA), Color(0xFF5D4E75))), Icons.Default.AttachMoney),
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = when {
            maxWidth < 480.dp -> 5.dp
            maxWidth < 768.dp -> 6.dp
            else -> 8.dp
        }
        val columns = when {
            maxWidth >= 1200.dp -> 8
            maxWidth < 480.dp -> 2
            else -> 4
        }
        val hideIcon = maxWidth < 640.dp && maxWidth >= 480.dp
        val compact = maxWidth < 768.dp
        val cardWidth = ((maxWidth - gap * (columns - 1)) / columns).coerceAtLeast(72.dp)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
            maxItemsInEachRow = columns,
        ) {
            cards.forEach { card ->
                PartOrderKpiCard(
                    spec = card,
                    modifier = Modifier.width(cardWidth),
                    compact = compact,
                    hideIcon = hideIcon,
                )
            }
        }
    }
}

@Composable
private fun PartOrderKpiCard(
    spec: KpiCardSpec,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    hideIcon: Boolean = false,
) {
    val shape = RoundedCornerShape(if (compact && hideIcon) 6.dp else 8.dp)
    val iconSize = when {
        hideIcon -> 0.dp
        compact -> 26.dp
        else -> 32.dp
    }
    val valueFontSize = when {
        compact && hideIcon -> 12.sp
        compact -> 14.sp
        else -> 16.sp
    }
    val labelFontSize = when {
        compact && hideIcon -> 8.sp
        compact -> 9.sp
        else -> 10.sp
    }
    Surface(
        modifier = modifier.heightIn(min = if (compact) 44.dp else 56.dp),
        shape = shape,
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x0D000000)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 44.dp else 56.dp)
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(if (compact && hideIcon) 2.dp else 3.dp)
                    .fillMaxHeight()
                    .background(spec.accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = if (compact) 6.dp else 10.dp,
                        vertical = if (compact) 5.dp else 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
            ) {
                if (!hideIcon) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(if (compact) 6.dp else 8.dp))
                            .background(spec.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            spec.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (compact) 12.dp else 14.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            spec.value,
                            fontWeight = FontWeight.Bold,
                            fontSize = valueFontSize,
                            color = Color(0xFF2D3748),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (spec.unit.isNotBlank()) {
                            Text(
                                spec.unit,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF718096),
                                modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                            )
                        }
                    }
                    Text(
                        spec.label,
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartOrderFilterBar(
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
    keywordPlaceholder: String = "部品名 / 部品CD",
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
fun PartOrderTablePanel(
    selectedTab: PartOrderTab,
    onTabSelect: (PartOrderTab) -> Unit,
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
        PartOrderTabStrip(
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

private fun partTabTheme(tab: PartOrderTab): MaterialTabTheme = when (tab) {
    PartOrderTab.Initial -> MaterialTabTheme(
        icon = Icons.Default.Inventory2,
        activeGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
        idleIcon = Color(0xFFD97706),
    )
    PartOrderTab.Daily -> MaterialTabTheme(
        icon = Icons.Default.CalendarMonth,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
        idleIcon = Color(0xFF667EEA),
    )
    PartOrderTab.Usage -> MaterialTabTheme(
        icon = Icons.Default.Build,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
        idleIcon = Color(0xFF3B82F6),
    )
    PartOrderTab.Order -> MaterialTabTheme(
        icon = Icons.Default.ShoppingCart,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
        idleIcon = Color(0xFF10B981),
    )
    PartOrderTab.OrderHistory -> MaterialTabTheme(
        icon = Icons.AutoMirrored.Filled.List,
        activeGradient = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569))),
        idleIcon = Color(0xFF64748B),
    )
}

@Composable
fun PartOrderTabStrip(
    selected: PartOrderTab,
    onSelect: (PartOrderTab) -> Unit,
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
            PartOrderTab.entries.forEach { tab ->
                PartOrderTabChip(
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
private fun PartOrderTabChip(
    tab: PartOrderTab,
    active: Boolean,
    onSelect: (PartOrderTab) -> Unit,
) {
    val theme = partTabTheme(tab)
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
fun PartOrderTabActionButtons(
    actionLoading: Boolean,
    onAddManualOrder: () -> Unit,
    onPrintOrder: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PartOrderActionBtn(
            text = "部品注文追加",
            icon = Icons.Default.Add,
            container = Color(0xE643E97B),
            content = Color.White,
            enabled = !actionLoading,
            onClick = onAddManualOrder,
        )
        PartOrderActionBtn(
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
fun PartOrderPurchaseTable(
    items: List<PartStockItemDto>,
    onOrderChange: (PartStockItemDto, Int) -> Unit,
    onRemarksChange: (PartStockItemDto, String) -> Unit,
) {
    val cols = PoCols.purchaseOrder
    PoTableShell(columns = cols) {
        Row(
            modifier = Modifier.poTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PoHeaderCell("日付", cols[0])
            PoHeaderCell("部品CD", cols[1])
            PoHeaderCell("部品名", cols[2], align = TextAlign.Start)
            PoHeaderCell("仕入先", cols[3])
            PoHeaderCell("規格", cols[4])
            PoHeaderCell("現在在庫", cols[5])
            PoHeaderCell("注文本数", cols[6], Color(0xFFFDE68A))
            PoHeaderCell("注文金額", cols[7])
            PoHeaderCell("備考", cols[8], align = TextAlign.Start)
        }
        items.forEachIndexed { index, row ->
            PartOrderPurchaseRow(
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
private fun PartOrderPurchaseRow(
    row: PartStockItemDto,
    cols: List<PoColumnSpec>,
    striped: Boolean,
    onOrderChange: (Int) -> Unit,
    onRemarksChange: (PartStockItemDto, String) -> Unit,
) {
    val stock = row.currentStock ?: 0
    val amount = row.orderAmount ?: 0.0
    Row(
        modifier = Modifier.poDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell(row.date.orEmpty(), cols[0])
        PoBodyCell(row.partCd.orEmpty(), cols[1], Color(0xFF475569))
        PoBodyCell(row.partName.orEmpty(), cols[2], Color(0xFF2563EB), align = TextAlign.Start)
        PoBodyCell(row.supplierName.orEmpty(), cols[3], Color(0xFF64748B))
        PoBodyCell(row.standardSpec.orEmpty(), cols[4], Color(0xFF64748B), align = TextAlign.Start)
        PoBodyCell(
            formatStockDisplay(stock),
            cols[5],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        PoStepperCell(
            value = row.orderQuantity ?: 0,
            spec = cols[6],
            bg = Color(0xFFFDE68A),
            blankWhenZero = true,
            onChange = onOrderChange,
        )
        PoBodyCell(
            if (amount > 0.0) "¥${jpNumber.format(amount.roundToInt())}" else "",
            cols[7],
            Color(0xFF1E293B),
            bold = true,
        )
        PartStockRemarksCell(row = row, spec = cols[8], onRemarksChange = onRemarksChange)
    }
}

@Composable
fun PartOrderHistoryTable(items: List<PartStockItemDto>) {
    val cols = PoCols.purchaseOrder
    PoTableShell(columns = cols) {
        Row(
            modifier = Modifier.poTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PoHeaderCell("日付", cols[0])
            PoHeaderCell("部品CD", cols[1])
            PoHeaderCell("部品名", cols[2], align = TextAlign.Start)
            PoHeaderCell("仕入先", cols[3])
            PoHeaderCell("規格", cols[4])
            PoHeaderCell("現在在庫", cols[5])
            PoHeaderCell("注文本数", cols[6], Color(0xFFFDE68A))
            PoHeaderCell("注文金額", cols[7])
            PoHeaderCell("備考", cols[8], align = TextAlign.Start)
        }
        items.forEachIndexed { index, row ->
            PartOrderHistoryRow(row = row, cols = cols, striped = index % 2 == 1)
        }
        if (items.isNotEmpty()) {
            PartOrderHistorySummaryRow(items = items, cols = cols)
        }
    }
}

@Composable
private fun PartOrderHistoryRow(
    row: PartStockItemDto,
    cols: List<PoColumnSpec>,
    striped: Boolean,
) {
    val stock = row.currentStock ?: 0
    val orderQty = row.orderQuantity ?: 0
    val amount = row.orderAmount ?: 0.0
    Row(
        modifier = Modifier.poDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell(row.date.orEmpty(), cols[0])
        PoBodyCell(row.partCd.orEmpty(), cols[1], Color(0xFF475569))
        PoBodyCell(row.partName.orEmpty(), cols[2], Color(0xFF2563EB), align = TextAlign.Start)
        PoBodyCell(row.supplierName.orEmpty(), cols[3], Color(0xFF64748B))
        PoBodyCell(row.standardSpec.orEmpty(), cols[4], Color(0xFF64748B), align = TextAlign.Start)
        PoBodyCell(
            formatStockDisplay(stock),
            cols[5],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell(formatStockDisplay(orderQty), cols[6])
        PoBodyCell(
            if (amount > 0.0) "¥${jpNumber.format(amount.roundToInt())}" else "",
            cols[7],
            if (amount < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell(row.remarks.orEmpty(), cols[8], Color(0xFF64748B), align = TextAlign.Start)
    }
}

@Composable
private fun PartOrderHistorySummaryRow(
    items: List<PartStockItemDto>,
    cols: List<PoColumnSpec>,
) {
    val totalOrderQty = items.sumOf { it.orderQuantity ?: 0 }
    val totalAmount = items.sumOf { (it.orderAmount ?: 0.0).roundToInt() }
    Row(
        modifier = Modifier.poSummaryRow(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell("合計", cols[0], Color(0xFF475569), bold = true)
        PoBodyCell("${items.size}件", cols[1], Color(0xFF475569), bold = true)
        PoBodyCell("", cols[2])
        PoBodyCell("", cols[3])
        PoBodyCell("", cols[4])
        PoBodyCell("", cols[5])
        PoBodyCell(
            if (totalOrderQty > 0) totalOrderQty.toString() else "",
            cols[6],
            Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell(
            if (totalAmount > 0) "¥${jpNumber.format(totalAmount)}" else "",
            cols[7],
            Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell("", cols[8])
    }
}

@Composable
private fun RowScope.PartStockRemarksCell(
    row: PartStockItemDto,
    spec: PoColumnSpec,
    onRemarksChange: (PartStockItemDto, String) -> Unit,
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
fun PartOrderInitialStockTable(
    items: List<PartStockItemDto>,
    onInitialStockChange: (PartStockItemDto, Int) -> Unit,
    onAdjustmentChange: (PartStockItemDto, Int) -> Unit,
) {
    val cols = PoCols.initialStock
    PoTableShell(columns = cols) {
        Row(
            modifier = Modifier.poTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PoHeaderCell("日付", cols[0])
            PoHeaderCell("仕入先", cols[1])
            PoHeaderCell("部品CD", cols[2])
            PoHeaderCell("部品名", cols[3], align = TextAlign.Start)
            PoHeaderCell("初期在庫", cols[4], Color(0xFFBBF7D0))
            PoHeaderCell("調整数", cols[5], Color(0xFFC7D2FE))
        }
        items.forEachIndexed { index, row ->
            PartOrderInitialStockRow(
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
private fun PartOrderInitialStockRow(
    row: PartStockItemDto,
    cols: List<PoColumnSpec>,
    striped: Boolean,
    onInitialStockChange: (Int) -> Unit,
    onAdjustmentChange: (Int) -> Unit,
) {
    val initial = row.initialStock ?: 0
    val initialBg = if (initial > 0) Color(0xFFD1FAE5) else Color(0xFFECFDF5)
    Row(
        modifier = Modifier.poDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell(row.date.orEmpty().takeLast(5), cols[0])
        PoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        PoBodyCell(row.partCd.orEmpty(), cols[2], Color(0xFF475569))
        PoBodyCell(row.partName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        PoStepperCell(
            value = initial,
            spec = cols[4],
            bg = initialBg,
            minValue = 0,
            onChange = onInitialStockChange,
        )
        PoStepperCell(
            value = row.adjustmentQuantity ?: 0,
            spec = cols[5],
            bg = Color(0xFFE0E7FF),
            minValue = null,
            onChange = onAdjustmentChange,
        )
    }
}

@Composable
fun PartOrderUsageTable(items: List<PartStockItemDto>) {
    val cols = PoCols.usage
    PoTableShell(columns = cols) {
        Row(
            modifier = Modifier.poTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PoHeaderCell("日付", cols[0])
            PoHeaderCell("仕入先", cols[1])
            PoHeaderCell("部品CD", cols[2])
            PoHeaderCell("部品名", cols[3], align = TextAlign.Start)
            PoHeaderCell("現在在庫", cols[4])
            PoHeaderCell("使用数", cols[5], Color(0xFFBAE6FD))
            PoHeaderCell("使用計画", cols[6])
            PoHeaderCell("在庫推移", cols[7])
        }
        items.forEachIndexed { index, row ->
            PartOrderUsageRow(row = row, cols = cols, striped = index % 2 == 1)
        }
    }
}

@Composable
private fun PartOrderUsageRow(
    row: PartStockItemDto,
    cols: List<PoColumnSpec>,
    striped: Boolean,
) {
    val stock = row.currentStock ?: 0
    Row(
        modifier = Modifier.poDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell(row.date.orEmpty(), cols[0])
        PoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        PoBodyCell(row.partCd.orEmpty(), cols[2], Color(0xFF475569))
        PoBodyCell(row.partName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        PoBodyCell(
            formatStockDisplay(stock),
            cols[4],
            if (stock < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell(formatStockDisplay(row.plannedUsage ?: 0), cols[5], Color(0xFF64748B))
        PoBodyCell(formatStockDisplay(row.usagePlanQty ?: 0), cols[6], Color(0xFF64748B))
        PoBodyCell(formatStockDisplay(row.stockTrend ?: 0), cols[7], Color(0xFF64748B))
    }
}

@Composable
fun PartOrderStockTable(
    items: List<PartStockItemDto>,
    onOrderChange: (PartStockItemDto, Int) -> Unit,
) {
    val cols = PoCols.dailyStock
    PoTableShell(columns = cols) {
        Row(
            modifier = Modifier.poTableHeaderRow(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            PoHeaderCell("日付", cols[0])
            PoHeaderCell("仕入先", cols[1])
            PoHeaderCell("部品CD", cols[2])
            PoHeaderCell("部品名", cols[3], align = TextAlign.Start)
            PoHeaderCell("在庫", cols[4])
            PoHeaderCell("使用数", cols[5], Color(0xFFBAE6FD))
            PoHeaderCell("使用計画", cols[6])
            PoHeaderCell("在庫推移", cols[7])
            PoHeaderCell("注文本数", cols[8], Color(0xFFFDE68A))
        }
        items.forEachIndexed { index, row ->
            PartOrderStockRow(
                row = row,
                cols = cols,
                striped = index % 2 == 1,
                onOrderChange = { onOrderChange(row, it) },
            )
        }
    }
}

@Composable
private fun PartOrderStockRow(
    row: PartStockItemDto,
    cols: List<PoColumnSpec>,
    striped: Boolean = false,
    onOrderChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.poDataRow(striped),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PoBodyCell(row.date.orEmpty().takeLast(5), cols[0])
        PoBodyCell(row.supplierName.orEmpty(), cols[1], Color(0xFF64748B))
        PoBodyCell(row.partCd.orEmpty(), cols[2], Color(0xFF475569))
        PoBodyCell(row.partName.orEmpty(), cols[3], Color(0xFF2563EB), align = TextAlign.Start)
        PoBodyCell(
            formatStockDisplay(row.currentStock ?: 0),
            cols[4],
            if ((row.currentStock ?: 0) < 0) Color(0xFFEF4444) else Color(0xFF1E293B),
            bold = true,
        )
        PoBodyCell(formatStockDisplay(row.plannedUsage ?: 0), cols[5], Color(0xFF64748B))
        PoBodyCell(formatStockDisplay(row.usagePlanQty ?: 0), cols[6], Color(0xFF64748B))
        PoBodyCell(formatStockDisplay(row.stockTrend ?: 0), cols[7], Color(0xFF64748B))
        PoStepperCell(
            value = row.orderQuantity ?: 0,
            spec = cols[8],
            bg = Color(0xFFFEF9C3),
            blankWhenZero = true,
            onChange = onOrderChange,
        )
    }
}

@Composable
fun PartOrderTableContainer(
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
