package com.example.smart_emap.ui.erp.production.actual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.production.planning.ProductionDataTable
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiCard
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiStrip
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionSingleDatePickerField
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard
import com.example.smart_emap.ui.erp.production.planning.ProductionTabStrip

@Composable
fun ProcessActualScreen(viewModel: ProcessActualViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadData() }
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    val processOptions = remember(uiState.processOptions) {
        listOf("" to "全工程") + uiState.processOptions.map { it.processCd to it.processName }
    }

    ProductionScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ProductionPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .productionPageScaffoldPadding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProductionHeroBar(
                    title = "工程別実績",
                    subtitle = "日・月単位で各工程の一日合計実績を集計・可視化",
                    badge = uiState.periodLabel,
                    actionLoading = uiState.loading,
                    onRefresh = viewModel::loadData,
                )

                ProductionFilterSurface {
                    ProductionTabStrip(
                        tabs = listOf("day" to "日", "month" to "月"),
                        selected = uiState.mode,
                        onSelect = viewModel::setMode,
                    )
                    if (uiState.mode == "day") {
                        ProductionSingleDatePickerField(
                            value = uiState.selectedDay,
                            onChange = viewModel::setDay,
                            label = "対象日",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        ProductionSingleDatePickerField(
                            value = "${uiState.selectedMonth}-01",
                            onChange = { v -> if (v.length >= 7) viewModel.setMonth(v.substring(0, 7)) },
                            label = "対象月",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickChip(if (uiState.mode == "day") "前日" else "前月") { viewModel.shift(-1) }
                        QuickChip(if (uiState.mode == "day") "今日" else "今月", primary = true) { viewModel.setCurrent() }
                        QuickChip(if (uiState.mode == "day") "翌日" else "翌月") { viewModel.shift(1) }
                    }
                    ProductionDropdownFilter(
                        label = "工程",
                        value = uiState.selectedProcessCd,
                        options = processOptions,
                        onSelect = viewModel::setProcess,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProductionKpiStrip(
                    cards = listOf(
                        kpi("対象工程数", "${uiState.processCount} 工程", Color(0xFF3B82F6)),
                        kpi("実績合計", "${formatActualNum(uiState.totalQuantity)} 本", Color(0xFF10B981)),
                        kpi("稼働日数", "${uiState.activeDays} 日", Color(0xFF06B6D4)),
                        kpi(
                            "最多工程",
                            uiState.topProcessName,
                            Color(0xFFF59E0B),
                            desc = if (uiState.topProcessTotal > 0) "${formatActualNum(uiState.topProcessTotal)} 本" else null,
                        ),
                    ),
                )

                ProductionTableCard(title = "${uiState.selectedProcessName} — 日次実績") {
                    if (uiState.selectedProcessCd.isBlank()) {
                        Hint("上の「工程」から対象工程を選択してください")
                    } else if (uiState.selectedDailyValues.isEmpty() || uiState.selectedDailyValues.all { it == 0.0 }) {
                        Hint("該当データがありません")
                    } else {
                        ActualVerticalBarChart(
                            labels = uiState.selectedDailyLabels,
                            values = uiState.selectedDailyValues,
                            modifier = Modifier.fillMaxWidth().height(240.dp),
                            barColor = ActualPalette[
                                uiState.summary.firstOrNull { it.processCd == uiState.selectedProcessCd }?.colorIndex?.rem(ActualPalette.size) ?: 0
                            ],
                        )
                    }
                }

                ProductionTableCard(title = "工程別合計") {
                    if (uiState.summary.isEmpty()) {
                        Hint("該当データがありません")
                    } else {
                        ActualHorizontalBarChart(
                            items = uiState.summary.map {
                                ChartBarItem(it.processName, it.total, ActualPalette[it.colorIndex % ActualPalette.size])
                            },
                            modifier = Modifier.fillMaxWidth().height((uiState.summary.size * 30 + 16).dp),
                        )
                    }
                }

                ProductionTableCard(title = "工程別構成比") {
                    if (uiState.summary.isEmpty()) {
                        Hint("該当データがありません")
                    } else {
                        val items = uiState.summary.filter { it.total > 0 }.map {
                            ChartBarItem(it.processName, it.total, ActualPalette[it.colorIndex % ActualPalette.size])
                        }
                        Column {
                            ActualDonutChart(items = items, modifier = Modifier.fillMaxWidth().height(200.dp))
                            ChartLegend(items)
                        }
                    }
                }

                ProductionTableCard(title = "工程別実績サマリー", isLoading = uiState.loading) {
                    if (uiState.summary.isEmpty()) {
                        Hint(if (uiState.hasSearched) "該当データがありません" else "条件を指定してください")
                    } else {
                        ProductionDataTable(
                            headers = listOf("工程", "合計数量", "稼働日数", "平均/稼働日", "構成比"),
                            rows = uiState.summary.map {
                                listOf(
                                    it.processName,
                                    formatActualNum(it.total),
                                    "${it.activeDays} 日",
                                    formatActualNum(it.avgPerDay, 1),
                                    "%.1f%%".format(it.ratio),
                                )
                            },
                            columnWidths = listOf(120, 110, 90, 110, 90),
                            columnKeys = listOf("product_name", "count", "", "count", ""),
                        )
                    }
                }

                if (uiState.dateColumns.isNotEmpty() && uiState.matrixRows.isNotEmpty()) {
                    val dates = uiState.dateColumns
                    ProductionTableCard(title = "工程別 一日合計マトリクス（${dates.size} 日）", isLoading = uiState.loading) {
                        ProductionDataTable(
                            headers = listOf("工程") + dates.map { shortDate(it) } + listOf("合計"),
                            rows = uiState.matrixRows.map { row ->
                                listOf(row.processName) +
                                    dates.map { d ->
                                        val v = row.byDate[d] ?: 0.0
                                        if (v == 0.0) "" else formatActualNum(v)
                                    } + listOf(formatActualNum(row.total))
                            },
                            columnWidths = listOf(110) + dates.map { 56 } + listOf(90),
                            columnKeys = listOf("product_name") + dates.map { "" } + listOf("count"),
                        )
                    }
                }
            }
        }
    }
}

private fun kpi(label: String, value: String, color: Color, desc: String? = null) = ProductionKpiCard(
    label = label,
    value = value,
    description = desc,
    accent = Brush.linearGradient(listOf(color, color)),
)

@Composable
private fun QuickChip(text: String, primary: Boolean = false, onClick: () -> Unit) {
    val bg = if (primary) Color(0xFF2563EB) else Color(0xFFF1F5F9)
    val fg = if (primary) Color.White else Color(0xFF475569)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun Hint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}
