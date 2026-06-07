package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.OrderMonthlyItemDto
import com.example.smart_emap.data.model.OrderMonthlySummaryDto
import java.text.NumberFormat
import java.util.Locale

private val numberFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

@Composable
fun OrderMonthlyToolbar(
    onGenerateDaily: () -> Unit,
    onUpdateForecast: () -> Unit,
    onUpdateProduct: () -> Unit,
    onDailyManage: () -> Unit,
    onBatchRegister: () -> Unit,
    actionLoading: Boolean,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x474F46E5),
                spotColor = Color(0x596D28D9),
            )
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(18.dp))
            .background(OrderMonthlyColors.toolbarBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                        radius = 500f,
                    ),
                ),
        )
        val wide = maxWidth >= 720.dp
        if (wide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarTitle()
                ToolbarButtons(
                    actionLoading = actionLoading,
                    onGenerateDaily = onGenerateDaily,
                    onUpdateForecast = onUpdateForecast,
                    onUpdateProduct = onUpdateProduct,
                    onDailyManage = onDailyManage,
                    onBatchRegister = onBatchRegister,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolbarTitle()
                ToolbarButtons(
                    actionLoading = actionLoading,
                    onGenerateDaily = onGenerateDaily,
                    onUpdateForecast = onUpdateForecast,
                    onUpdateProduct = onUpdateProduct,
                    onDailyManage = onDailyManage,
                    onBatchRegister = onBatchRegister,
                )
            }
        }
    }
}

@Composable
private fun ToolbarTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x400F172A))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.08f)),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "月別受注管理",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.95f), Color(0x80C4B5FD), Color.Transparent),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun ToolbarButtons(
    actionLoading: Boolean,
    onGenerateDaily: () -> Unit,
    onUpdateForecast: () -> Unit,
    onUpdateProduct: () -> Unit,
    onDailyManage: () -> Unit,
    onBatchRegister: () -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassToolbarButton("日受注生成", Icons.AutoMirrored.Filled.NoteAdd, GlassButtonStyle.Blue, !actionLoading, onGenerateDaily)
        GlassToolbarButton("内示更新", Icons.Default.Refresh, GlassButtonStyle.Teal, !actionLoading, onUpdateForecast)
        GlassToolbarButton("製品更新", Icons.Default.Inventory2, GlassButtonStyle.Amber, !actionLoading, onUpdateProduct)
        GlassToolbarButton("日受注管理", Icons.Default.CalendarMonth, GlassButtonStyle.Indigo, !actionLoading, onDailyManage)
        GlassToolbarButton("一括登録", Icons.Default.FileCopy, GlassButtonStyle.Green, !actionLoading, onBatchRegister)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderMonthlyFilterBar(
    year: Int,
    month: Int,
    destinationCd: String,
    keyword: String,
    destinationOptions: List<DestinationOptionDto>,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDestinationChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    onPrevPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onCurrentMonth: () -> Unit,
) {
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .orderMonthlyFilterGlass(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
            FilterDropdown("${year}年", yearExpanded, { yearExpanded = it }) {
                OrderMonthlyViewModel.yearOptions().forEach { y ->
                    DropdownMenuItem(text = { Text("${y}年") }, onClick = { onYearChange(y); yearExpanded = false })
                }
            }
            FilterDropdown("${month}月", monthExpanded, { monthExpanded = it }) {
                (1..12).forEach { m ->
                    DropdownMenuItem(text = { Text("${m}月") }, onClick = { onMonthChange(m); monthExpanded = false })
                }
            }
            GlassIconButton(onClick = onPrevPeriod) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrderMonthlyColors.TextMuted)
            }
            GlassPillButton(text = "今月", onClick = onCurrentMonth, filled = true)
            GlassIconButton(onClick = onNextPeriod) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrderMonthlyColors.TextMuted)
            }
            Box(modifier = Modifier.width(1.dp).height(22.dp).background(Color(0x14000000)))
            FilterDropdown(
                label = destinationOptions.find { it.cd == destinationCd }?.let { "${it.cd} | ${it.name}" }
                    ?: if (destinationCd.isBlank()) "納入先" else destinationCd,
                expanded = destExpanded,
                onExpandedChange = { destExpanded = it },
                minWidth = 182.dp,
                showClearIcon = destinationCd.isNotBlank(),
                onClear = { onDestinationChange("") },
            ) {
                DropdownMenuItem(text = { Text("すべて") }, onClick = { onDestinationChange(""); destExpanded = false })
                destinationOptions.forEach { d ->
                    DropdownMenuItem(
                        text = { Text("${d.cd} | ${d.name}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onDestinationChange(d.cd); destExpanded = false },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = OrderMonthlyColors.TextMuted, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (keyword.isEmpty()) Text("製品検索...", color = OrderMonthlyColors.TextMuted, fontSize = 12.sp)
                        inner()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    minWidth: androidx.compose.ui.unit.Dp = 88.dp,
    showClearIcon: Boolean = false,
    onClear: (() -> Unit)? = null,
    menuContent: @Composable () -> Unit,
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier
                .widthIn(min = minWidth)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = if (showClearIcon && onClear != null) 2.dp else 10.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        ) {
            Row(
                modifier = Modifier.widthIn(min = minWidth - 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = true),
                )
                if (showClearIcon && onClear != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onClear()
                                    onExpandedChange(false)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "クリア",
                            tint = OrderMonthlyColors.TextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            menuContent()
        }
    }
}

@Composable
fun OrderMonthlyProgressBar(visible: Boolean, percent: Int) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .orderMonthlyFilterGlass(RoundedCornerShape(10.dp))
            .padding(10.dp),
    ) {
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = Color(0xFF3B82F6),
            trackColor = Color(0xFFE2E8F0),
        )
    }
}

@Composable
fun OrderMonthlySummaryCards(summary: OrderMonthlySummaryDto) {
    data class CardDef(
        val title: String,
        val value: Int,
        val icon: ImageVector,
        val iconGradient: Brush,
        val valueColor: Color = OrderMonthlyColors.TextPrimary,
    )

    val cards = listOf(
        CardDef("内示本数", summary.forecastUnits, Icons.Default.Description, Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))),
        CardDef("確定本数", summary.forecastTotalUnits, Icons.Default.Check, Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))),
        CardDef(
            "内示差異", summary.forecastDiff, Icons.AutoMirrored.Filled.TrendingUp,
            Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))),
            when {
                summary.forecastDiff < 0 -> OrderMonthlyColors.DiffNegative
                summary.forecastDiff > 0 -> OrderMonthlyColors.DiffPositive
                else -> OrderMonthlyColors.TextPrimary
            },
        ),
        CardDef("社内メッキ", summary.platingCount, Icons.Default.Refresh, Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
        CardDef("外注メッキ", summary.externalPlatingCount, Icons.Default.Description, Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFDB2777)))),
        CardDef("社内溶接", summary.internalWeldingCount, Icons.Default.CalendarMonth, Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2)))),
        CardDef("外注溶接", summary.externalWeldingCount, Icons.Default.FileCopy, Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))),
        CardDef("社内検査", summary.internalInspectionCount, Icons.Default.Check, Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)))),
        CardDef("外注検査", summary.externalInspectionCount, Icons.Default.Search, Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF9333EA)))),
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(cards) { card ->
            SummaryCard(
                title = card.title,
                value = numberFormat.format(card.value),
                icon = card.icon,
                iconGradient = card.iconGradient,
                valueColor = card.valueColor,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconGradient: Brush,
    valueColor: Color,
) {
    Box(
        modifier = Modifier
            .width(148.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0D000000))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0x146366F1), Color.Transparent)),
                    shape = RoundedCornerShape(99.dp),
                ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(3.dp, RoundedCornerShape(9.dp))
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 10.5.sp, color = OrderMonthlyColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private val TableRowHeight = 42.dp
private val TableHeaderHeight = 38.dp
private val TableCellHPadding = 10.dp

private enum class TableAlign {
    Start,
    Center,
    End,
    ;

    val boxAlignment: Alignment
        get() = when (this) {
            Start -> Alignment.CenterStart
            Center -> Alignment.Center
            End -> Alignment.CenterEnd
        }

    val textAlign: TextAlign
        get() = when (this) {
            Start -> TextAlign.Start
            Center -> TextAlign.Center
            End -> TextAlign.End
        }
}

private data class OrderMonthlyTableWidths(
    val destination: Dp,
    val year: Dp,
    val month: Dp,
    val productCd: Dp,
    val productName: Dp,
    val productType: Dp,
    val forecastUnits: Dp,
    val forecastTotal: Dp,
    val forecastDiff: Dp,
    val actions: Dp,
) {
    val total: Dp
        get() = destination + year + month + productCd + productName + productType +
            forecastUnits + forecastTotal + forecastDiff + actions
}

private fun resolveOrderMonthlyTableWidths(available: Dp): OrderMonthlyTableWidths {
    val fixed = 52.dp + 44.dp + 88.dp + 80.dp + 88.dp + 96.dp + 88.dp + 196.dp
    val destBase = 120.dp
    val nameBase = 128.dp
    val minTotal = fixed + destBase + nameBase
    val extra = (available - minTotal).coerceAtLeast(0.dp)
    return OrderMonthlyTableWidths(
        destination = destBase + extra * 0.42f,
        year = 52.dp,
        month = 44.dp,
        productCd = 88.dp,
        productName = nameBase + extra * 0.58f,
        productType = 80.dp,
        forecastUnits = 88.dp,
        forecastTotal = 96.dp,
        forecastDiff = 88.dp,
        actions = 196.dp,
    )
}

@Composable
fun OrderMonthlyDataTable(
    items: List<OrderMonthlyItemDto>,
    onDailyOrder: (OrderMonthlyItemDto) -> Unit,
    onEdit: (OrderMonthlyItemDto) -> Unit,
    onDelete: (OrderMonthlyItemDto) -> Unit,
) {
    val scroll = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .orderMonthlyGlassSurface(RoundedCornerShape(14.dp), elevation = 8.dp)
            .padding(12.dp),
    ) {
        val widths = resolveOrderMonthlyTableWidths(maxWidth)
        val tableWidth = widths.total.coerceAtLeast(maxWidth)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll),
        ) {
            Column(modifier = Modifier.width(tableWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TableHeaderHeight)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(OrderMonthlyColors.tableHeaderBackground)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x336366F1), Color(0x006366F1)),
                            ),
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableHeaderCell("納入先名", widths.destination, TableAlign.Start)
                    TableHeaderCell("年", widths.year, TableAlign.Center)
                    TableHeaderCell("月", widths.month, TableAlign.Center)
                    TableHeaderCell("製品CD", widths.productCd, TableAlign.Start)
                    TableHeaderCell("製品名", widths.productName, TableAlign.Start)
                    TableHeaderCell("種別", widths.productType, TableAlign.Center)
                    TableHeaderCell("内示本数", widths.forecastUnits, TableAlign.End)
                    TableHeaderCell("確定本数", widths.forecastTotal, TableAlign.End)
                    TableHeaderCell("内示差異", widths.forecastDiff, TableAlign.End)
                    TableHeaderCell("操作", widths.actions, TableAlign.Center)
                }
                HorizontalDivider(color = Color(0x336366F1), thickness = 1.5.dp)

                items.forEachIndexed { index, item ->
                    val rowBg = if (index % 2 == 0) Color.White.copy(alpha = 0.55f) else OrderMonthlyColors.TableRowStripe
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = TableRowHeight)
                            .background(rowBg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TableBodyCell(item.destinationName, widths.destination, TableAlign.Start)
                        TableBodyCell(item.year.toString(), widths.year, TableAlign.Center)
                        TableBodyCell(item.month.toString(), widths.month, TableAlign.Center)
                        TableBodyCell(item.productCd, widths.productCd, TableAlign.Start, monospace = true)
                        TableBodyCell(item.productName, widths.productName, TableAlign.Start)
                        TableBodyCell(item.productType, widths.productType, TableAlign.Center)
                        TableBodyCell(
                            numberFormat.format(item.forecastUnits),
                            widths.forecastUnits,
                            TableAlign.End,
                            monospace = true,
                        )
                        TableBodyCell(
                            numberFormat.format(item.forecastTotalUnits),
                            widths.forecastTotal,
                            TableAlign.End,
                            monospace = true,
                        )
                        TableDiffCell(
                            text = numberFormat.format(item.forecastDiff),
                            width = widths.forecastDiff,
                            value = item.forecastDiff,
                        )
                        TableActionsCell(
                            width = widths.actions,
                            onDailyOrder = { onDailyOrder(item) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) },
                        )
                    }
                    HorizontalDivider(color = OrderMonthlyColors.TableRowDivider, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp, align: TableAlign) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = TableCellHPadding),
        contentAlignment = align.boxAlignment,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OrderMonthlyColors.TextPrimary,
            letterSpacing = 0.3.sp,
            textAlign = align.textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TableBodyCell(
    text: String,
    width: Dp,
    align: TableAlign,
    color: Color = OrderMonthlyColors.TextPrimary,
    monospace: Boolean = false,
) {
    Box(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = TableRowHeight)
            .padding(horizontal = TableCellHPadding, vertical = 6.dp),
        contentAlignment = align.boxAlignment,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = align.textAlign,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun TableDiffCell(text: String, width: Dp, value: Int) {
    val color = when {
        value < 0 -> OrderMonthlyColors.DiffNegative
        value > 0 -> OrderMonthlyColors.DiffPositive
        else -> OrderMonthlyColors.TextPrimary
    }
    val highlight = value != 0
    Box(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = TableRowHeight)
            .padding(horizontal = TableCellHPadding, vertical = 6.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (highlight) {
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (value < 0) Color(0x0DDC2626) else Color(0x0D16A34A),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                ),
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = color,
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TableActionsCell(
    width: Dp,
    onDailyOrder: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = TableRowHeight)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableActionLink("日別受注", OrderMonthlyColors.ActionBlue, onDailyOrder)
        TableActionLink("編集", OrderMonthlyColors.ActionEdit, onEdit)
        TableActionLink("削除", OrderMonthlyColors.DiffNegative, onDelete)
    }
}

@Composable
private fun TableActionLink(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 6.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1,
    )
}

@Composable
fun OrderMonthlyPagination(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
) {
    val totalPages = ((total + pageSize - 1) / pageSize).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("全 $total 件", fontSize = 12.sp, color = OrderMonthlyColors.TextMuted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GlassPillButton("前へ", onClick = { if (page > 1) onPageChange(page - 1) })
            Text("$page / $totalPages", fontSize = 12.sp)
            GlassPillButton("次へ", onClick = { if (page < totalPages) onPageChange(page + 1) })
        }
    }
}
