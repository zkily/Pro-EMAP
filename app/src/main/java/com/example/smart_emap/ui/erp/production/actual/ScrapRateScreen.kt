package com.example.smart_emap.ui.erp.production.actual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.QualityProductRowDto
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDataTable
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiCard
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiStrip
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard
import com.example.smart_emap.ui.erp.production.requirements.RequirementSearchButton

@Composable
fun ScrapRateScreen(viewModel: ScrapRateViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.fetchAll() }
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }
    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "廃棄率分析_製品別集計",
            layout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
        )
        if (!opened) snackbarHostState.showSnackbar("印刷を開始できませんでした")
        viewModel.clearPendingPrintHtml()
    }

    val processFilterOptions = remember { listOf("" to "全工程") + SCRAP_PROCESS_OPTIONS }
    val productFilterOptions = remember(uiState.productOptions) {
        listOf("" to "（すべて）") + uiState.productOptions.map { p ->
            val cd = p.productCd
            cd to (if (!p.productName.isNullOrBlank()) "$cd · ${p.productName}" else cd)
        }
    }
    val sortOptions = remember(uiState.mainLineLabels) {
        buildList {
            add("product_name" to "製品名")
            add("all_processes_defect_scrap" to "不良＋廃棄")
            uiState.mainLineLabels.forEach { add((it.key ?: "") to "${it.label}（％）") }
            add("rty_loss" to "廃棄率")
            add("rty" to "合格率")
        }
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
                    title = "廃棄率分析",
                    subtitle = "工程別／製品別",
                    actionLoading = uiState.loadingProcessAgg || uiState.loadingProductMatrix,
                    onRefresh = viewModel::fetchAll,
                )

                ProductionFilterSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        ProductionCompactDateRangeField(
                            startDate = uiState.dateFrom,
                            endDate = uiState.dateTo,
                            onStartChange = { viewModel.setRange(it, uiState.dateTo) },
                            onEndChange = { viewModel.setRange(uiState.dateFrom, it) },
                            modifier = Modifier.weight(1f),
                        )
                        RequirementSearchButton(
                            loading = uiState.loadingProcessAgg || uiState.loadingProductMatrix,
                            onClick = viewModel::fetchAll,
                        )
                    }
                    ProductionDropdownFilter(
                        label = "工程",
                        value = uiState.process,
                        options = processFilterOptions,
                        onSelect = viewModel::setProcess,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ProductionDropdownFilter(
                        label = "製品",
                        value = uiState.filterProductCd,
                        options = productFilterOptions,
                        onSelect = viewModel::setProductFilter,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::reset, modifier = Modifier.height(34.dp)) {
                            Text("リセット", fontSize = 12.sp)
                        }
                    }
                }

                ScrapKpiStrip(uiState)

                ProductionTableCard(title = "工程別 比率（不良＋廃棄÷実績）", isLoading = uiState.loadingProcessAgg) {
                    if (uiState.processRows.isEmpty()) {
                        ScrapHint()
                    } else {
                        ActualHorizontalBarChart(
                            items = uiState.processRows.map {
                                ChartBarItem(it.label ?: it.key ?: "", it.ratePercent ?: 0.0, Color(0xFFF43F5E))
                            },
                            modifier = Modifier.fillMaxWidth().height((uiState.processRows.size * 30 + 16).dp),
                        )
                    }
                }

                ProductionTableCard(title = "工程別 不良・廃棄（数量）", isLoading = uiState.loadingProcessAgg) {
                    if (uiState.processRows.isEmpty()) {
                        ScrapHint()
                    } else {
                        Column {
                            ActualStackedBarChart(
                                labels = uiState.processRows.map { it.label ?: it.key ?: "" },
                                series = listOf(
                                    StackSeries("不良", Color(0xFFFB923C), uiState.processRows.map { it.sumDefect ?: 0.0 }),
                                    StackSeries("廃棄", Color(0xFFF43F5E), uiState.processRows.map { it.sumScrap ?: 0.0 }),
                                ),
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                            )
                            ChartLegend(
                                listOf(
                                    ChartBarItem("不良", 0.0, Color(0xFFFB923C)),
                                    ChartBarItem("廃棄", 0.0, Color(0xFFF43F5E)),
                                ),
                            )
                        }
                    }
                }

                ProductionTableCard(
                    title = "製品別 不良＋廃棄（本）と 廃棄率（1−RTY）（％）",
                    subtitle = productChartSubLine(uiState),
                    isLoading = uiState.productChartLoading,
                ) {
                    val chartRows = uiState.productChartRows
                    if (chartRows.isEmpty()) {
                        ScrapHint(if (uiState.productChartLoading) "全件を読込中…" else "該当データなし")
                    } else {
                        ActualComboBarLineChart(
                            labels = chartRows.map { productShortLabel(it) },
                            barValues = chartRows.map { it.allProcessesDefectScrap ?: 0.0 },
                            lineValues = chartRows.map { computeProductMainLineRty(it)?.second },
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                        )
                    }
                }

                ProductionTableCard(title = "工程別集計", isLoading = uiState.loadingProcessAgg) {
                    if (uiState.processRows.isEmpty()) {
                        ScrapHint()
                    } else {
                        val totals = computeProcessTotals(uiState.processRows)
                        ProductionDataTable(
                            headers = listOf(
                                "工程", "実績", "不良", "廃棄", "不良＋廃棄",
                                "不良金額", "廃棄金額", "不良＋廃棄金額", "廃棄率",
                            ),
                            rows = uiState.processRows.map { r ->
                                listOf(
                                    r.label ?: r.key ?: "—",
                                    fmtInt(r.sumActual),
                                    fmtInt(r.sumDefect),
                                    fmtInt(r.sumScrap),
                                    fmtInt(r.sumDefectAndScrap),
                                    fmtYen(r.sumDefectAmount),
                                    fmtYen(r.sumScrapAmount),
                                    fmtYen(r.sumDefectAndScrapAmount),
                                    fmtPct(r.ratePercent),
                                )
                            } + listOf(totals),
                            columnWidths = listOf(120, 90, 80, 80, 100, 110, 110, 130, 90),
                            columnKeys = listOf("product_name", "count", "count", "count", "count", "count", "count", "count", "count"),
                        )
                        if (uiState.processAmountsSyncing) {
                            Text("金額列を読込中…", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                ProductionTableCard(title = "製品別集計（${uiState.productTotal} 件）", isLoading = uiState.loadingProductMatrix) {
                    ProductionDropdownFilter(
                        label = "並び替え（${if (uiState.productSortOrder == "desc") "降順" else "昇順"}）",
                        value = uiState.productSortProp,
                        options = sortOptions,
                        onSelect = viewModel::setProductSort,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                    if (uiState.productRows.isEmpty()) {
                        ScrapHint()
                    } else {
                        val labels = uiState.mainLineLabels
                        ProductionDataTable(
                            headers = listOf("製品CD", "製品名", "不良＋廃棄") +
                                labels.map { "${it.label}（％）" } + listOf("廃棄率", "合格率"),
                            rows = uiState.productRows.map { row ->
                                listOf(
                                    row.productCd ?: "—",
                                    row.productName ?: "",
                                    fmtInt(row.allProcessesDefectScrap),
                                ) + labels.map { formatProcCell(row, it.key ?: "") } +
                                    listOf(formatProductRtyLoss(row), formatProductRty(row))
                            },
                            columnWidths = listOf(90, 130, 110) + labels.map { 90 } + listOf(90, 90),
                            columnKeys = listOf("", "product_name", "count") +
                                labels.map { "count" } + listOf("count", "count"),
                        )
                        ScrapPaginationBar(
                            page = uiState.productPage,
                            pageCount = uiState.productPageCount,
                            onPrev = { viewModel.goToProductPage(uiState.productPage - 1) },
                            onNext = { viewModel.goToProductPage(uiState.productPage + 1) },
                        )
                        OutlinedButton(
                            onClick = viewModel::printProductTable,
                            enabled = !uiState.printLoading,
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(if (uiState.printLoading) "印刷準備中…" else "印刷", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrapKpiStrip(uiState: ScrapRateUiState) {
    val s = uiState.summary
    val basisRolled = s?.basis == "rolled_main_line"
    val firstCard = ProductionKpiCard(
        label = if (basisRolled) "連乘廃棄率 1−RTY" else "選択工程比率",
        value = fmtPct(s?.ratePercent),
        description = if (basisRolled) "主ライン・プール集計" else "${s?.referenceProcessLabel ?: ""} ÷ 実績",
        accent = solid(Color(0xFFF43F5E)),
    )
    val secondCard = if (basisRolled) {
        ProductionKpiCard(
            label = "連乘合格率 RTY",
            value = fmtPct(s?.rolledYieldPercent),
            description = "RTY＝Π(1−r)",
            accent = solid(Color(0xFF10B981)),
        )
    } else {
        ProductionKpiCard(
            label = "不良＋廃棄（選択工程）",
            value = fmtInt(s?.sumDefectAndScrap),
            description = "期間内合計",
            accent = solid(Color(0xFFD97706)),
        )
    }
    val thirdCard = ProductionKpiCard(
        label = "不良＋廃棄（全工程合計）",
        value = fmtInt(uiState.allProcessesDefectScrapTotal),
        description = "全工程キー合計 · フィルタ無関係",
        accent = solid(Color(0xFFCA8A04)),
    )
    ProductionKpiStrip(cards = listOf(firstCard, secondCard, thirdCard))
}

@Composable
private fun ScrapPaginationBar(page: Int, pageCount: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, enabled = page > 1) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "前へ")
        }
        Text("$page / $pageCount", fontSize = 12.sp, color = Color(0xFF475569))
        IconButton(onClick = onNext, enabled = page < pageCount) {
            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "次へ")
        }
    }
}

@Composable
private fun ScrapHint(text: String = "データがありません") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

private fun solid(color: Color): Brush = Brush.linearGradient(listOf(color, color))

private fun productChartSubLine(uiState: ScrapRateUiState): String {
    val n = uiState.productChartRows.size
    val t = uiState.productChartTotal
    if (uiState.productChartLoading && n == 0) return "全件を読込中…"
    if (n == 0) return "該当データなし"
    return if (t > n) "全 $t 件中 廃棄率高い順で上位 $n 件を表示" else "全 $t 件 · 廃棄率（1−RTY）降順"
}

private fun productShortLabel(row: QualityProductRowDto): String {
    val base = row.productName?.trim()?.takeIf { it.isNotEmpty() } ?: row.productCd?.trim() ?: "—"
    return if (base.length > 10) base.substring(0, 9) + "…" else base
}

private fun computeProcessTotals(rows: List<com.example.smart_emap.data.model.QualityProcessRowDto>): List<String> {
    val tActual = rows.sumOf { it.sumActual ?: 0.0 }
    val tDef = rows.sumOf { it.sumDefect ?: 0.0 }
    val tScr = rows.sumOf { it.sumScrap ?: 0.0 }
    val tDefAmt = rows.sumOf { it.sumDefectAmount ?: 0.0 }
    val tScrAmt = rows.sumOf { it.sumScrapAmount ?: 0.0 }
    val tBad = tDef + tScr
    val tBadAmt = tDefAmt + tScrAmt
    val rateStr = if (tActual > 0) "%.2f %%".format(tBad / tActual * 100.0) else "—"
    return listOf(
        "合計", fmtInt(tActual), fmtInt(tDef), fmtInt(tScr), fmtInt(tBad),
        fmtYen(tDefAmt), fmtYen(tScrAmt), fmtYen(tBadAmt), rateStr,
    )
}

private fun formatProcCell(row: QualityProductRowDto, key: String): String {
    val p = row.processes?.firstOrNull { it.key == key }
    val rp = p?.ratePercent ?: return "—"
    return "%.2f %%".format(rp)
}

private fun formatProductRty(row: QualityProductRowDto): String {
    val x = computeProductMainLineRty(row) ?: return "—"
    return "%.2f %%".format(x.first)
}

private fun formatProductRtyLoss(row: QualityProductRowDto): String {
    val x = computeProductMainLineRty(row) ?: return "—"
    return "%.2f %%".format(x.second)
}

private fun fmtInt(n: Double?): String {
    if (n == null || n.isNaN()) return "—"
    return formatActualNum(n)
}

private fun fmtYen(n: Double?): String {
    if (n == null || n.isNaN()) return "—"
    return "¥" + formatActualNum(kotlin.math.round(n))
}

private fun fmtPct(p: Double?): String {
    if (p == null || p.isNaN()) return "—"
    return "%.2f %%".format(p)
}
