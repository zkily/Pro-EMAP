package com.example.smart_emap.ui.erp.purchase.material

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseEmptyHint
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialForecastScreen(viewModel: MaterialForecastViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val useStatsForSummary = uiState.supplierCd.isBlank() && uiState.keyword.isBlank()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, "材料内示管理")
            putExtra(Intent.EXTRA_TEXT, html)
        }
        context.startActivity(Intent.createChooser(intent, "印刷 / 共有"))
        viewModel.clearPendingPrintHtml()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        PurchasePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MaterialForecastHeroBar(
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    onRefresh = viewModel::refreshData,
                    onPrint = viewModel::preparePrint,
                )
                MaterialForecastKpiStrip(stats = uiState.stats)
                MaterialForecastFilterBar(
                    year = uiState.year,
                    month = uiState.month,
                    keyword = uiState.keyword,
                    supplierCd = uiState.supplierCd,
                    supplierOptions = uiState.supplierOptions,
                    onYearChange = viewModel::setYear,
                    onMonthChange = viewModel::setMonth,
                    onPrevMonth = { viewModel.shiftMonth(-1) },
                    onCurrentMonth = viewModel::setCurrentMonth,
                    onNextMonth = { viewModel.shiftMonth(1) },
                    onKeywordChange = viewModel::setKeyword,
                    onSupplierChange = viewModel::setSupplierCd,
                    onReset = viewModel::resetFilters,
                )
                MaterialForecastTablePanel(
                    selectedTab = uiState.tab,
                    onTabSelect = viewModel::setTab,
                    isLoading = uiState.isLoading,
                ) {
                    when (uiState.tab) {
                        MaterialForecastTab.Detail -> {
                            if (uiState.details.isEmpty()) {
                                Text(
                                    "内示データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialForecastDetailTable(
                                    items = uiState.details,
                                    stats = uiState.stats,
                                    useStatsForSummary = useStatsForSummary,
                                )
                            }
                        }
                        MaterialForecastTab.Summary -> {
                            if (uiState.summary.isEmpty()) {
                                PurchaseEmptyHint("集計データがありません")
                            } else {
                                MaterialForecastSummaryTable(items = uiState.summary)
                            }
                        }
                    }
                }
            }
        }
    }
}
