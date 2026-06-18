package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun OrderDestinationHistoryScreen(viewModel: OrderDestinationHistoryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "納入先別受注履歴",
            layout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
        )
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        // MainShell 内嵌：不再叠加状态栏 inset，避免页头上方留白
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(OrderMonthlyColors.pageBackground),
        ) {
            DestHistoryAnimatedBackground()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DestHistoryStaggeredReveal(index = 0) {
                        DestHistoryHeroPanel(
                            resultCount = uiState.detailItems.size,
                            showResultBadge = uiState.detailItems.isNotEmpty(),
                            destinationCd = uiState.destinationCd,
                            startDate = uiState.startDate,
                            endDate = uiState.endDate,
                            destinationOptions = uiState.destinationOptions,
                            isLoading = uiState.isLoading,
                            onDestinationChange = viewModel::setDestinationCd,
                            onStartDateChange = viewModel::setStartDate,
                            onEndDateChange = viewModel::setEndDate,
                            onSearch = viewModel::search,
                        )
                    }
                }
                item {
                    DestHistoryStaggeredReveal(index = 1) {
                        DestHistorySummarySection(summary = uiState.summaryItems)
                    }
                }
                item {
                    DestHistoryStaggeredReveal(index = 2) {
                        DestHistoryDetailsSection(
                            items = uiState.detailItems,
                            isLoading = uiState.isLoading,
                            hasSearched = uiState.hasSearched,
                            onPrint = viewModel::preparePrint,
                        )
                    }
                }
            }
        }
    }
}
