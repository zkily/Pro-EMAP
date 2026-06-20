package com.example.smart_emap.ui.erp.production.requirements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDataTable
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard

@Composable
fun MaterialRequirementsScreen(viewModel: MaterialRequirementsViewModel) {
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
        val ok = HtmlPrintHelper.printHtml(context, html, "材料需要量", PrintPageLayout.A4_LANDSCAPE_SINGLE)
        if (!ok) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
        viewModel.clearPendingPrintHtml()
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
                    title = "材料需要量",
                    subtitle = "start_date がある行を期間で絞り材料別の件数を集計",
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
                }

                uiState.summary?.let { s ->
                    RequirementChips(
                        listOf(
                            Triple("期間", "${s.dateStart} — ${s.dateEnd}", Color(0xFF2563EB)),
                            Triple("材料種類", (s.totalMaterialKinds ?: 0).toString(), Color(0xFF0D9488)),
                            Triple("合計件数", formatInt(s.totalPieceCount), Color(0xFF7C3AED)),
                        ),
                    )
                }

                ProductionTableCard(title = "材料需要量", isLoading = uiState.loading) {
                    if (uiState.items.isEmpty()) {
                        com.example.smart_emap.ui.erp.production.requirements.EmptyHint(
                            if (uiState.hasSearched) "データがありません" else "条件を指定して集計してください",
                        )
                    } else {
                        ProductionDataTable(
                            headers = listOf("メーカー", "材料名", "規格", "件数"),
                            rows = uiState.items.map {
                                listOf(
                                    it.materialManufacturer.orEmpty(),
                                    it.materialName.orEmpty(),
                                    it.standardSpecification.orEmpty(),
                                    formatInt(it.pieceCount),
                                )
                            },
                            columnWidths = listOf(110, 140, 110, 80),
                            columnKeys = listOf("product_name", "product_name", "product_name", "count"),
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
                    ProductionTableCard(title = "日別・材料別需要", isLoading = uiState.loading) {
                        ProductionDataTable(
                            headers = listOf("メーカー", "材料名", "規格") +
                                dates.map { shortDateLabel(it) } + listOf("期間合計件数"),
                            rows = uiState.matrixRows.map { row ->
                                listOf(
                                    row.materialManufacturer.orEmpty(),
                                    row.materialName.orEmpty(),
                                    row.standardSpecification.orEmpty(),
                                ) + dates.map { d ->
                                    val v = row.byDate?.get(d) ?: 0.0
                                    if (v == 0.0) "" else formatQty(v)
                                } + listOf(formatInt(row.rowTotal))
                            },
                            columnWidths = listOf(110, 120, 92) + dates.map { 55 } + listOf(100),
                            columnKeys = listOf("product_name", "product_name", "product_name") +
                                dates.map { "" } + listOf(""),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EmptyHint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material3.Text(
            text,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
        )
    }
}
