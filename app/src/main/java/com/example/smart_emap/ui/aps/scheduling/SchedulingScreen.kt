package com.example.smart_emap.ui.aps.scheduling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun SchedulingScreen(viewModel: SchedulingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val ok = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "生産スケジューリングボード",
            layout = PrintPageLayout.A3_LANDSCAPE_SINGLE,
        )
        if (!ok) {
            snackbarHostState.showSnackbar("印刷を開始できませんでした")
        }
        viewModel.clearPendingPrintHtml()
    }

    val lineNameById = remember(uiState.lines) {
        uiState.lines.associate { line ->
            line.id to line.lineName?.trim().orEmpty().ifEmpty { line.lineCode }
        }
    }
    val dates = uiState.grid?.dates.orEmpty()
    val visibleBlocks = remember(uiState.grid, lineNameById) {
        SchedulingMatrixLogic.filterVisibleBlocks(uiState.grid?.blocks.orEmpty(), lineNameById)
    }
    val productOptions = remember(visibleBlocks) {
        SchedulingMatrixLogic.productOptions(visibleBlocks)
    }
    val displayBlocks = remember(visibleBlocks, dates, uiState.itemName, uiState.matrixPlanExtendMode) {
        SchedulingMatrixLogic.filterBlocksByProduct(
            visibleBlocks,
            dates,
            uiState.itemName,
            uiState.matrixPlanExtendMode,
        )
    }
    val matrixRows = remember(displayBlocks, dates, lineNameById, uiState.matrixPlanExtendMode) {
        SchedulingMatrixLogic.buildMatrixRows(
            displayBlocks,
            dates,
            lineNameById,
            uiState.matrixPlanExtendMode,
        )
    }
    val matrixSections = remember(matrixRows) {
        SchedulingMatrixLogic.buildMatrixSections(matrixRows)
    }
    val lineCount = displayBlocks.size
    val overallTotal = SchedulingMatrixLogic.overallPlannedOutputTotal(displayBlocks, dates)
    val avgEfficiency = SchedulingMatrixLogic.avgEfficiencyRate(displayBlocks)
    val requiredHours = SchedulingMatrixLogic.requiredProductionHours(overallTotal, avgEfficiency)
    val dailyTotals = SchedulingMatrixLogic.overallDailyTotals(displayBlocks, dates, uiState.matrixPlanExtendMode)
    val featureLabel = SchedulingTheme.processLabel(uiState.processCd)
    val dateRangeText = "${uiState.startDate} ~ ${uiState.endDate}"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.linearGradient(
                        listOf(
                            SchedulingTheme.PageBgStart,
                            SchedulingTheme.PageBgEnd,
                        ),
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SchedulingPageHeader()
            SchedulingFilterCard(
                processCd = uiState.processCd,
                lineId = uiState.lineId,
                lines = uiState.lines,
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                itemName = uiState.itemName,
                productOptions = productOptions,
                onProcessChange = viewModel::setProcessCd,
                onLineChange = viewModel::setLineId,
                onStartDateChange = viewModel::setStartDate,
                onEndDateChange = viewModel::setEndDate,
                onItemChange = viewModel::setItemName,
            )
            SchedulingStatGrid(
                lineCount = lineCount,
                plannedTotal = SchedulingMatrixLogic.formatQty(overallTotal),
                avgEfficiency = SchedulingMatrixLogic.formatEfficiency(avgEfficiency),
                requiredHours = SchedulingMatrixLogic.formatHours(requiredHours),
            )
            SchedulingMatrixCard(
                featureLabel = featureLabel,
                dateRangeText = dateRangeText,
                planExtendMode = uiState.matrixPlanExtendMode,
                isLoading = uiState.isLoading,
                dates = dates,
                sections = matrixSections,
                overallTotal = SchedulingMatrixLogic.formatQty(overallTotal),
                dailyTotals = dailyTotals,
                planExtendModeValue = uiState.matrixPlanExtendMode,
                onPlanExtendModeChange = viewModel::setMatrixPlanExtendMode,
                onPrint = viewModel::preparePrint,
            )
        }
    }
}
