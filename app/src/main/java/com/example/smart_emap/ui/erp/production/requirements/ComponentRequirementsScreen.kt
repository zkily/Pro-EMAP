package com.example.smart_emap.ui.erp.production.requirements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDataTable
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard

@Composable
fun ComponentRequirementsScreen(viewModel: ComponentRequirementsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.runSummary() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val ok = HtmlPrintHelper.printHtml(context, html, "部品需要量", PrintPageLayout.A4_LANDSCAPE_SINGLE)
        if (!ok) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
        viewModel.clearPendingPrintHtml()
    }

    val planOptions = listOf(
        "molding_actual_plan" to "molding_actual_plan（成型実績計画）",
        "molding_plan" to "molding_plan（成型計画）",
    )

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
                    title = "部品需要量",
                    subtitle = "成型計画ベースの部品所要量・使用量を集計",
                    actionLoading = uiState.loading,
                    onPrint = if (uiState.dailyDates.isNotEmpty()) viewModel::printDailyMatrix else null,
                )

                ProductionFilterSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        MonthQuickBar(enabled = !uiState.loading, onQuick = viewModel::applyQuickMonth)
                        ProductionCompactDateRangeField(
                            startDate = uiState.dateStart,
                            endDate = uiState.dateEnd,
                            onStartChange = { viewModel.setRange(it, uiState.dateEnd) },
                            onEndChange = { viewModel.setRange(uiState.dateStart, it) },
                            modifier = Modifier.weight(1f),
                        )
                        RequirementSearchButton(loading = uiState.loading, onClick = viewModel::runSummary)
                    }
                    ProductionDropdownFilter(
                        label = "需要量の駆動列",
                        value = uiState.planColumn,
                        options = planOptions,
                        onSelect = viewModel::setPlanColumn,
                        modifier = Modifier.width(260.dp),
                    )
                }

                uiState.summary?.let { s ->
                    RequirementChips(
                        listOf(
                            Triple("期間", "${s.dateStart} — ${s.dateEnd}", Color(0xFF2563EB)),
                            Triple("部品種類", (s.totalComponentKinds ?: 0).toString(), Color(0xFF0D9488)),
                            Triple("所要量合計", formatQty(s.totalRequiredQty), Color(0xFF7C3AED)),
                        ),
                    )
                }
                uiState.summaryUse?.let { u ->
                    RequirementChips(
                        listOf(
                            Triple("使用合計(溶接実績+不良×BOM)", formatQty(u.totalRequiredQty), Color(0xFFEA580C)),
                        ),
                    )
                }

                ProductionTableCard(title = "部品需要量", isLoading = uiState.loading) {
                    if (uiState.items.isEmpty()) {
                        EmptyHint(if (uiState.hasSearched) "データがありません" else "条件を指定して集計してください")
                    } else {
                        ProductionDataTable(
                            headers = listOf("部品CD", "部品名", "単位", "サマリー行数", "所要量"),
                            rows = uiState.items.map {
                                listOf(
                                    it.componentCd.orEmpty(),
                                    it.componentName.orEmpty(),
                                    it.componentUom.orEmpty(),
                                    formatInt(it.sourceLotCount),
                                    formatQty(it.requiredQty),
                                )
                            },
                            columnWidths = listOf(90, 150, 60, 100, 90),
                            columnKeys = listOf("product_cd", "product_name", "product_cd", "count", "count"),
                        )
                    }
                }

                if (uiState.hasSearched && uiState.summary?.dailyMatrixOmitted == true) {
                    RequirementWarningBar(
                        "期間が ${uiState.summary?.dailyMatrixMaxDays ?: 186} 日を超えるため、日別の二次元表は表示しません。期間を絞って再集計してください。",
                    )
                }

                if (uiState.hasSearched && uiState.dailyDates.isNotEmpty()) {
                    val dates = uiState.dailyDates
                    ProductionTableCard(title = "日別・部品別需要", isLoading = uiState.loading) {
                        ProductionDataTable(
                            headers = listOf("部品CD", "部品名", "単位") +
                                dates.map { shortDateLabel(it) } + listOf("所要量合計"),
                            rows = uiState.matrixRows.map { row -> matrixRowCells(row, dates) },
                            columnWidths = listOf(90, 140, 60) + dates.map { 70 } + listOf(100),
                            columnKeys = listOf("product_cd", "product_name", "product_cd") +
                                dates.map { "" } + listOf(""),
                        )
                    }
                    ProductionTableCard(title = "日別・部品別使用量", isLoading = uiState.loading) {
                        ProductionDataTable(
                            headers = listOf("部品CD", "部品名", "単位") +
                                dates.map { shortDateLabel(it) } + listOf("使用量合計"),
                            rows = uiState.matrixRowsUse.map { row -> matrixRowCells(row, dates) },
                            columnWidths = listOf(90, 140, 60) + dates.map { 70 } + listOf(100),
                            columnKeys = listOf("product_cd", "product_name", "product_cd") +
                                dates.map { "" } + listOf(""),
                        )
                    }
                }
            }
        }
    }
}

private fun matrixRowCells(
    row: com.example.smart_emap.data.model.ComponentRequirementsDailyMatrixRowDto,
    dates: List<String>,
): List<String> {
    return listOf(
        row.componentCd.orEmpty(),
        row.componentName.orEmpty(),
        row.componentUom.orEmpty(),
    ) + dates.map { d ->
        val v = row.byDate?.get(d) ?: 0.0
        if (v == 0.0) "" else formatQty(v)
    } + listOf(formatQty(row.rowTotal))
}
