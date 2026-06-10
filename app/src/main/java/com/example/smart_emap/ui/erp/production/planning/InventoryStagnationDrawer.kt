package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.InventoryStagnationDataDto
import com.example.smart_emap.data.model.InventoryStagnationRowDto

private object StagnationUi {
    val accent = Color(0xFFEF4444)
    val accentSoft = Color(0xFFFEE2E2)
    val panelBorder = Color(0xFFE8ECF0)
    val panelBg = Color(0xFFF8FAFC)
    val headerGrad = listOf(Color(0xFFFFF1F2), Color(0xFFF8FAFC), Color.White)
    val tableHeaderBg = Color(0xFFF1F5F9)
    val rowAltBg = Color(0xFFFAFBFC)
    val printBtn = Color(0xFF475569)
    val actionBlue = Color(0xFF2563EB)
}

@Composable
fun InventoryStagnationDrawer(
    asOfDate: String,
    minQuantity: Int,
    stableDays: Int,
    isLoading: Boolean,
    rows: List<InventoryStagnationRowDto>,
    meta: InventoryStagnationDataDto?,
    onAsOfDateChange: (String) -> Unit,
    onMinQuantityChange: (Int) -> Unit,
    onStableDaysChange: (Int) -> Unit,
    onFilterProduct: (String) -> Unit,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .background(Color.Black.copy(alpha = 0.28f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                color = StagnationUi.panelBg,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 300.dp, max = 680.dp)
                    .fillMaxWidth(0.86f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .shadow(12.dp, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    StagnationDrawerHeader(count = rows.size, onDismiss = onDismiss)
                    StagnationControlPanel(
                        asOfDate = asOfDate,
                        minQuantity = minQuantity,
                        stableDays = stableDays,
                        count = rows.size,
                        meta = meta,
                        onAsOfDateChange = onAsOfDateChange,
                        onMinQuantityChange = onMinQuantityChange,
                        onStableDaysChange = onStableDaysChange,
                        onPrint = onPrint,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
                    ) {
                        StagnationResultPanel(
                            isLoading = isLoading,
                            rows = rows,
                            onFilterProduct = onFilterProduct,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StagnationDrawerHeader(count: Int, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(StagnationUi.headerGrad))
            .border(1.dp, StagnationUi.panelBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(StagnationUi.accent, Color(0xFFF87171)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("在庫停滞監視", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProductionPlanningColors.TextPrimary)
            Text("同一在庫値が連続する製品を検出", fontSize = 10.sp, color = ProductionPlanningColors.TextSecondary)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StagnationUi.accentSoft,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
        ) {
            Text(
                "$count 件",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFB91C1C),
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun StagnationControlPanel(
    asOfDate: String,
    minQuantity: Int,
    stableDays: Int,
    count: Int,
    meta: InventoryStagnationDataDto?,
    onAsOfDateChange: (String) -> Unit,
    onMinQuantityChange: (Int) -> Unit,
    onStableDaysChange: (Int) -> Unit,
    onPrint: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        ProductionBeautifulDatePickerDialog(
            value = asOfDate,
            title = "基準日",
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onAsOfDateChange(it)
                showDatePicker = false
            },
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, StagnationUi.panelBorder),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StagnationInlineFilter(
                    label = "基準日",
                    modifier = Modifier.weight(1.15f),
                ) {
                    StagnationCompactDateChip(
                        value = asOfDate,
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                StagnationInlineFilter(
                    label = "閾値(>)",
                    modifier = Modifier.weight(0.85f),
                ) {
                    StagnationCompactStepper(
                        value = minQuantity,
                        onValueChange = onMinQuantityChange,
                        min = 0,
                        max = 999_999,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                StagnationInlineFilter(
                    label = "連続暦日",
                    modifier = Modifier.weight(0.85f),
                ) {
                    StagnationCompactStepper(
                        value = stableDays,
                        onValueChange = onStableDaysChange,
                        min = 2,
                        max = 60,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp), color = Color(0xFFF1F5F9), thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEFF6FF)) {
                    Text(
                        "検出 $count",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProductionPlanningColors.AccentBlue,
                    )
                }
                if (meta != null) {
                    Text(
                        "${meta.periodStart.orEmpty()}～${meta.periodEnd.orEmpty()}（${meta.stableCalendarDays ?: 0}日）",
                        fontSize = 10.sp,
                        color = ProductionPlanningColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Surface(
                    onClick = onPrint,
                    shape = RoundedCornerShape(6.dp),
                    color = StagnationUi.printBtn.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StagnationUi.printBtn.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = StagnationUi.printBtn, modifier = Modifier.size(12.dp))
                        Text("印刷", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = StagnationUi.printBtn)
                    }
                }
            }
        }
    }
}

@Composable
private fun StagnationInlineFilter(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
        content()
    }
}

@Composable
private fun StagnationCompactDateChip(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    val display = value.take(10).ifBlank { "—" }
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFFAFBFC))
            .border(1.dp, StagnationUi.panelBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
        Text(
            display,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = ProductionPlanningColors.TextPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun StagnationCompactStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFFAFBFC))
            .border(1.dp, StagnationUi.panelBorder, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            enabled = value > min,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "減らす", modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
        }
        Text(
            value.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = ProductionPlanningColors.TextPrimary,
        )
        IconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            enabled = value < max,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "増やす", modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
        }
    }
}

@Composable
private fun StagnationResultPanel(
    isLoading: Boolean,
    rows: List<InventoryStagnationRowDto>,
    onFilterProduct: (String) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = shape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, StagnationUi.panelBorder),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StagnationTableHeader()
            HorizontalDivider(color = StagnationUi.panelBorder, thickness = 1.dp)
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = StagnationUi.accent,
                            )
                        }
                    }
                    rows.isEmpty() -> StagnationEmptyState()
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(
                                items = rows,
                                key = { _, row -> "${row.productCd}_${row.inventoryColumn}_${row.periodStart}" },
                            ) { index, row ->
                                StagnationResultRow(
                                    row = row,
                                    striped = index % 2 == 1,
                                    onFilterProduct = onFilterProduct,
                                )
                                if (index < rows.lastIndex) {
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
private fun StagnationTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StagnationUi.tableHeaderBg)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("在庫列", Modifier.width(68.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        Text("製品名", Modifier.weight(1f), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        Text("在庫数", Modifier.width(52.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.End)
        Text("期間", Modifier.width(128.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.Center)
        Text("操作", Modifier.width(72.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.Center)
    }
}

@Composable
private fun StagnationEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "該当する停滞在庫は見つかりませんでした",
            fontSize = 11.sp,
            color = ProductionPlanningColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StagnationResultRow(
    row: InventoryStagnationRowDto,
    striped: Boolean,
    onFilterProduct: (String) -> Unit,
) {
    val (chipBg, chipFg) = InventoryStagnationLogic.inventoryChipColors(row.inventoryColumn)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(if (striped) StagnationUi.rowAltBg else Color.White)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(68.dp), contentAlignment = Alignment.CenterStart) {
            Surface(shape = RoundedCornerShape(10.dp), color = chipBg) {
                Text(
                    InventoryStagnationLogic.inventoryLabel(row.inventoryColumn),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = chipFg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            row.productName.orEmpty(),
            modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
            fontSize = 10.sp,
            color = ProductionPlanningColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            formatProductionNumber(row.stableQuantity),
            modifier = Modifier.width(52.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B5AD9),
            textAlign = TextAlign.End,
        )
        Text(
            "${row.periodStart}～${row.periodEnd}",
            modifier = Modifier.width(128.dp),
            fontSize = 9.sp,
            color = ProductionPlanningColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { onFilterProduct(row.productCd) },
                shape = RoundedCornerShape(5.dp),
                color = Color(0xFFEFF6FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = null, tint = StagnationUi.actionBlue, modifier = Modifier.size(10.dp))
                    Text("絞込", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = StagnationUi.actionBlue)
                }
            }
        }
    }
}
