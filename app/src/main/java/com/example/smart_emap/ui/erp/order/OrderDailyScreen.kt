package com.example.smart_emap.ui.erp.order

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.shell.LayoutColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDailyScreen(viewModel: OrderDailyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingCsvShare) {
        val csv = uiState.pendingCsvShare ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "order_daily.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "CSVエクスポート"))
        viewModel.clearPendingCsvShare()
    }

    OrderDailyDialogs(state = uiState, viewModel = viewModel)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshAll,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OrderMonthlyColors.pageBackground),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        OrderDailyPageHero(
                            lastFetchedText = uiState.lastFetchedText,
                            startDate = uiState.startDate,
                            endDate = uiState.endDate,
                            destinationCd = uiState.destinationCd,
                            keyword = uiState.keyword,
                            destinationOptions = uiState.destinationOptions,
                            productOptions = uiState.allProductOptions,
                            actionLoading = uiState.isLoading || uiState.isRefreshing,
                            exportEnabled = uiState.fullList.isNotEmpty(),
                            onRefresh = viewModel::refreshAll,
                            onExportCsv = viewModel::exportCsv,
                            onCreate = viewModel::openCreateDialog,
                            onQuickRange = viewModel::applyQuickRange,
                            onStartDateChange = viewModel::setStartDate,
                            onEndDateChange = viewModel::setEndDate,
                            onDestinationChange = viewModel::setDestinationCd,
                            onKeywordChange = viewModel::setKeyword,
                        )
                    }
                    item { OrderDailySummaryStrip(summary = uiState.summary) }
                    item {
                        OrderDailyTableSection(
                            isLoading = uiState.isLoading,
                            isEmpty = uiState.pageItems.isEmpty(),
                            rows = uiState.pageItems,
                            pageRangeText = uiState.pageRangeText,
                            page = uiState.page,
                            pageSize = uiState.pageSize,
                            total = uiState.total,
                            onEdit = viewModel::openEditDialog,
                            onDelete = viewModel::openDeleteDialog,
                            onPageChange = viewModel::setPage,
                            onPageSizeChange = viewModel::setPageSize,
                        )
                    }
                }
            }
        }
    }
}
