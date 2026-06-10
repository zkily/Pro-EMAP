package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto

@Composable
fun DataMgmtPageHeader(
    total: Int,
    actionLoading: Boolean,
    onToggleUpdateMenu: () -> Unit,
    onToggleRecommendedPrint: () -> Unit,
    onToggleProductionPlan: () -> Unit,
    onProcessPrint: () -> Unit,
    onInventoryStagnation: () -> Unit,
    onColumnSettings: () -> Unit,
    columnSettingsEnabled: Boolean = false,
    updateMenuExpanded: Boolean,
    recommendedMenuExpanded: Boolean,
    productionPlanMenuExpanded: Boolean,
    onCloseUpdateMenu: () -> Unit,
    onCloseRecommendedMenu: () -> Unit,
    onCloseProductionPlanMenu: () -> Unit,
    onUpdateAction: (String) -> Unit,
    onPrintAction: (String) -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("生産データ管理", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ProductionPlanningColors.TextPrimary)
                    Text("受注・実績・在庫を一元管理", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
                }
                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        "${total} 件",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = ProductionPlanningColors.AccentBlue,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (actionLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DataMgmtMenuButton(
                label = "各種更新機能",
                color = Color(0xFF7C3AED),
                icon = Icons.Default.Refresh,
                expanded = updateMenuExpanded,
                onClick = onToggleUpdateMenu,
                onDismiss = onCloseUpdateMenu,
                items = listOf(
                    "generate" to "データ生成",
                    "all-update" to "全部一括更新",
                    "update-order" to "受注データ更新",
                    "batch-initial" to "初期在庫一括登録",
                    "carry-over" to "繰越データ更新",
                    "actual" to "実績データ更新",
                    "defect" to "不良データ更新",
                    "scrap" to "廃棄データ更新",
                    "on-hold" to "保留データ更新",
                    "production-dates" to "生産計画日更新",
                    "plan" to "計画データ更新",
                    "inventory-trend" to "在庫・推移更新",
                    "product-master" to "製品マスタ更新",
                    "machine" to "設備フィールド更新",
                    "batch-actual" to "実績一括登録",
                ),
                onSelect = onUpdateAction,
                enabled = !actionLoading,
            )
            DataMgmtActionChip("工程別計画確認印刷", Color(0xFF059669), Icons.Default.Print, onProcessPrint, !actionLoading)
            DataMgmtMenuButton(
                label = "推奨生産日印刷",
                color = Color(0xFFF59E0B),
                icon = Icons.Default.Print,
                expanded = recommendedMenuExpanded,
                onClick = onToggleRecommendedPrint,
                onDismiss = onCloseRecommendedMenu,
                items = listOf(
                    "print-rec-plating" to "メッキ推奨生産日",
                    "print-rec-welding" to "溶接推奨生産日",
                    "print-rec-welding-plan" to "溶接計画推奨生産日",
                    "print-rec-molding" to "成型推奨生産日",
                    "print-rec-molding-plan" to "成型計画推奨生産日",
                ),
                onSelect = onPrintAction,
                enabled = !actionLoading,
            )
            DataMgmtMenuButton(
                label = "生産計画印刷",
                color = Color(0xFF7C3AED),
                icon = Icons.Default.Print,
                expanded = productionPlanMenuExpanded,
                onClick = onToggleProductionPlan,
                onDismiss = onCloseProductionPlanMenu,
                items = listOf(
                    "molding-plan-create" to "成型計画作成",
                    "welding-plan-create" to "溶接計画作成",
                ),
                onSelect = onPrintAction,
                enabled = !actionLoading,
            )
            DataMgmtActionChip("在庫停滞監視", Color(0xFFEF4444), Icons.Default.Warning, onInventoryStagnation, true)
            DataMgmtActionChip(
                label = "列設定",
                color = if (columnSettingsEnabled) Color(0xFF64748B) else Color(0xFFCBD5E1),
                icon = Icons.Default.Settings,
                onClick = onColumnSettings,
                enabled = columnSettingsEnabled,
            )
        }
    }
}

@Composable
private fun DataMgmtActionChip(
    label: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier.height(34.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun DataMgmtMenuButton(
    label: String,
    color: Color,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    items: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    enabled: Boolean,
) {
    Box {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.height(34.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, maxLines = 1)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            items.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = {
                        onDismiss()
                        onSelect(key)
                    },
                )
            }
        }
    }
}

@Composable
fun DataMgmtFilterCard(
    startDate: String,
    endDate: String,
    productCd: String,
    productOptions: List<ProductionSummaryProductOptionDto>,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onProductSelect: (String) -> Unit,
    onProductClear: () -> Unit,
    onPrevDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DataMgmtInlineFilterRow(
                label = "期間",
                modifier = Modifier.weight(1.5f),
            ) {
                ProductionCompactDateRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    onStartChange = onStartDateChange,
                    onEndChange = onEndDateChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DataMgmtQuickDateButton("前日", onPrevDay)
                DataMgmtQuickDateButton("今日", onToday, primary = true)
                DataMgmtQuickDateButton("翌日", onNextDay)
            }
            DataMgmtInlineFilterRow(
                label = "製品",
                modifier = Modifier.weight(1.2f),
            ) {
                DataMgmtProductFilterField(
                    productCd = productCd,
                    productOptions = productOptions,
                    onSelect = onProductSelect,
                    onClear = onProductClear,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DataMgmtInlineFilterRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = ProductionPlanningColors.TextSecondary,
            fontWeight = FontWeight.Medium,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataMgmtProductFilterField(
    productCd: String,
    productOptions: List<ProductionSummaryProductOptionDto>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = productOptions.map {
        it.productCd to "${it.productCd} - ${it.productName.orEmpty()}"
    }
    val display = options.find { it.first == productCd }?.second
        ?: if (productCd.isBlank()) "製品名を選択" else productCd
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
            Surface(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, ProductionPlanningColors.CardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (productCd.isBlank()) ProductionPlanningColors.TextSecondary else ProductionPlanningColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (productCd.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .clickable {
                                    onClear()
                                    expanded = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "クリア",
                                tint = ProductionPlanningColors.TextSecondary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = ProductionPlanningColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DataMgmtQuickDateButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = if (primary) ProductionPlanningColors.AccentBlue else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (primary) ProductionPlanningColors.AccentBlue else ProductionPlanningColors.CardBorder,
        ),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            fontSize = 11.sp,
            color = if (primary) Color.White else ProductionPlanningColors.TextPrimary,
            fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
fun DataMgmtTabRow(
    tabs: List<ProductionDataTab>,
    selected: ProductionDataTab,
    onSelect: (ProductionDataTab) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .background(Color.White)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        tabs.forEach { tab ->
            val active = tab == selected
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .clickable { onSelect(tab) }
                    .background(if (active) Color(0xFFF8FAFC) else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    tab.label,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) ProductionPlanningColors.AccentBlue else ProductionPlanningColors.TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(if (active) ProductionPlanningColors.AccentBlue else Color.Transparent),
                )
            }
        }
    }
    HorizontalDivider(color = ProductionPlanningColors.CardBorder)
}

@Composable
fun ProductionDataManagementTable(
    data: ProductionTableData,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val fixedCount = ProductionDataManagementTableLayout.fixedColumnCount(data.keys)
    val scrollStart = fixedCount.coerceAtMost(data.headers.size)

    fun columnWidth(index: Int) = (data.widths.getOrNull(index) ?: 80).dp

    fun headerAlign() = TextAlign.Center

    fun cellAlign(key: String?): TextAlign = when (key) {
        "product_name" -> TextAlign.Start
        else -> TextAlign.Center
    }

    @Composable
    fun HeaderCell(index: Int, text: String) {
        Box(
            modifier = Modifier
                .width(columnWidth(index))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = headerAlign(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    fun BodyCell(index: Int, text: String, isSummary: Boolean = false) {
        val align = cellAlign(data.keys.getOrNull(index))
        val textColor = when {
            isNegativeDisplayNumber(text) -> ProductionPlanningColors.AccentRed
            isSummary -> ProductionPlanningColors.TextPrimary
            else -> ProductionPlanningColors.TextPrimary
        }
        Box(
            modifier = Modifier
                .width(columnWidth(index))
                .padding(horizontal = 4.dp),
            contentAlignment = when (align) {
                TextAlign.Start -> Alignment.CenterStart
                else -> Alignment.Center
            },
        ) {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                fontWeight = if (isSummary) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = align,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    fun SummaryRow() {
        if (data.summaryRow.isEmpty()) return
        HorizontalDivider(color = ProductionPlanningColors.CardBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                data.summaryRow.take(scrollStart).forEachIndexed { i, cell ->
                    BodyCell(i, cell, isSummary = true)
                }
            }
            if (scrollStart < data.summaryRow.size) {
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = ProductionPlanningColors.CardBorder,
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState),
                ) {
                    data.summaryRow.drop(scrollStart).forEachIndexed { offset, cell ->
                        BodyCell(scrollStart + offset, cell, isSummary = true)
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProductionPlanningColors.TableHeaderBg)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                data.headers.take(scrollStart).forEachIndexed { i, h -> HeaderCell(i, h) }
            }
            if (scrollStart < data.headers.size) {
                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = ProductionPlanningColors.CardBorder,
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState),
                ) {
                    data.headers.drop(scrollStart).forEachIndexed { offset, h ->
                        HeaderCell(scrollStart + offset, h)
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(data.rows, key = { index, _ -> index }) { rowIndex, row ->
                val bg = if (rowIndex % 2 == 0) Color.White else ProductionPlanningColors.TableStripe
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        row.take(scrollStart).forEachIndexed { i, cell -> BodyCell(i, cell) }
                    }
                    if (scrollStart < row.size) {
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            color = ProductionPlanningColors.CardBorder,
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(scrollState),
                        ) {
                            row.drop(scrollStart).forEachIndexed { offset, cell ->
                                BodyCell(scrollStart + offset, cell)
                            }
                        }
                    }
                }
            }
        }
        SummaryRow()
    }
}

@Composable
fun DataMgmtTableCard(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .shadow(1.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape),
    ) {
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
        Box(modifier = Modifier.padding(8.dp).fillMaxWidth().weight(1f).fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun DataMgmtGenerateConfirmDialog(
    startDate: String,
    endDate: String,
    loading: Boolean,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "データ生成確認",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "データ生成を実行しますか？",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("期間:", fontSize = 13.sp, color = ProductionPlanningColors.TextSecondary, modifier = Modifier.width(40.dp))
                        ProductionDateRangePickerField(
                            startDate = startDate,
                            endDate = endDate,
                            onStartChange = onStartDateChange,
                            onEndChange = onEndDateChange,
                            modifier = Modifier.weight(1f),
                            startLabel = "開始日",
                            endLabel = "終了日",
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("説明:", fontSize = 13.sp, color = ProductionPlanningColors.TextSecondary, modifier = Modifier.width(40.dp))
                        Text(
                            "既存のデータはスキップされます",
                            fontSize = 13.sp,
                            color = ProductionPlanningColors.TextPrimary,
                        )
                    }
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedCancelButton(onClick = onDismiss, enabled = !loading)
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("生成開始", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DataMgmtProcessPrintDateDialog(
    targetDate: String,
    loading: Boolean,
    onTargetDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.88f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFECFDF5), Color.White)))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF34D399)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "工程別計画確認印刷",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "印刷対象の日付を選択してください。",
                    fontSize = 13.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                ProductionSingleDatePickerField(
                    value = targetDate,
                    onChange = onTargetDateChange,
                    label = "対象日",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedCancelButton(onClick = onDismiss, enabled = !loading)
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading && targetDate.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("印刷", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DataMgmtProgressDialog(
    title: String,
    progressText: String,
    progressPercentage: Float,
    status: DataMgmtProgressStatus,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage.coerceIn(0f, 100f) / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "progress",
    )
    val percentText = "${progressPercentage.coerceIn(0f, 100f).toInt()}%"
    val isSuccess = status == DataMgmtProgressStatus.Success
    val isError = status == DataMgmtProgressStatus.Error
    val barBrush = when {
        isSuccess -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
        isError -> Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFF87171)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7)))
    }
    val percentColor = when {
        isSuccess -> Color(0xFF059669)
        isError -> Color(0xFFDC2626)
        else -> Color(0xFF6366F1)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "progressAnim")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "iconSpin",
    )
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "shine",
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        val shape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.9f)
                .shadow(12.dp, shape, spotColor = Color(0x406366F1))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF8B5CF6)),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (status == DataMgmtProgressStatus.Running) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(iconRotation),
                                strokeWidth = 2.5.dp,
                                color = Color(0xFF6366F1),
                            )
                        } else {
                            Icon(
                                if (isSuccess) Icons.Default.Info else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Text(
                        progressText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProductionPlanningColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1F5F9)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(999.dp))
                            .background(barBrush),
                    ) {
                        if (status == DataMgmtProgressStatus.Running) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.6f)
                                    .offset(x = (shineOffset * 120).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.35f),
                                                Color.Transparent,
                                            ),
                                        ),
                                    ),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("進捗", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                    Text(
                        percentText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = percentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlinedCancelButton(onClick: () -> Unit, enabled: Boolean) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, ProductionPlanningColors.CardBorder),
    ) {
        Text(
            "キャンセル",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = ProductionPlanningColors.TextPrimary,
        )
    }
}

private val columnSettingsPrimary = Color(0xFF6366F1)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataMgmtColumnSettingsDialog(
    visibleColumns: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val totalColumns = ProductionDataManagementLogic.columnDefinitions.size
    val selectedCount = visibleColumns.count { it.value }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = columnSettingsPrimary.copy(alpha = 0.3f)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF5F3FF), Color.White)))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(columnSettingsPrimary, Color(0xFF8B5CF6)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("列設定", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ProductionPlanningColors.TextPrimary)
                        Text("カスタムタブに表示する列を選択", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEEF2FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    ) {
                        Text(
                            "$selectedCount / $totalColumns",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = columnSettingsPrimary,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("表示列を素早く切り替え", fontSize = 11.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DataMgmtColumnSettingsQuickButton("全選択", columnSettingsPrimary, onSelectAll)
                        DataMgmtColumnSettingsQuickButton("初期化", Color(0xFF64748B), onReset)
                    }
                }

                Column(
                    modifier = Modifier
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ProductionDataManagementLogic.columnGroups.forEach { (group, keys) ->
                        val groupSelected = keys.count { visibleColumns[it] == true }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFAFBFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8ECF0)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEEF2FF),
                                    ) {
                                        Text(
                                            group,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = columnSettingsPrimary,
                                        )
                                    }
                                    Text(
                                        "$groupSelected/${keys.size}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    keys.forEach { key ->
                                        val def = ProductionDataManagementLogic.columnDefinitions[key] ?: return@forEach
                                        val checked = visibleColumns[key] == true
                                        DataMgmtColumnToggleItem(
                                            label = def.label,
                                            checked = checked,
                                            onToggle = { onToggle(key, !checked) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.ContentPadding,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDFE6)),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text("キャンセル", color = Color(0xFF606266), fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = columnSettingsPrimary),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text("保存", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DataMgmtColumnSettingsQuickButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun DataMgmtColumnToggleItem(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val bg = if (checked) Color(0xFFEEF2FF) else Color.Transparent
    val border = if (checked) Color(0xFFC7D2FE) else Color(0xFFE2E8F0)
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier
            .widthIn(min = 148.dp, max = 168.dp)
            .padding(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 2.dp, end = 6.dp, top = 1.dp, bottom = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(32.dp),
                colors = CheckboxDefaults.colors(checkedColor = columnSettingsPrimary),
            )
            Text(
                label,
                fontSize = 11.sp,
                color = if (checked) Color(0xFF334155) else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

