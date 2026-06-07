package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.theme.LoginColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderMonthlyScreen(
    viewModel: OrderMonthlyViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingShareText) {
        val text = uiState.pendingShareText ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "日別受注編集")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "印刷・共有"))
        viewModel.clearPendingShareText()
    }

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
                        OrderMonthlyToolbar(
                            onGenerateDaily = viewModel::openGenerateDailyDialog,
                            onUpdateForecast = viewModel::openForecastUpdateDialog,
                            onUpdateProduct = viewModel::openUpdateFieldsDialog,
                            onDailyManage = viewModel::openDailyManageDialog,
                            onBatchRegister = viewModel::openBatchDialog,
                            actionLoading = uiState.actionLoading,
                        )
                    }
                    item {
                        OrderMonthlyProgressBar(
                            visible = uiState.progressVisible,
                            percent = uiState.progressPercent,
                        )
                    }
                    item {
                        OrderMonthlyFilterBar(
                            year = uiState.year,
                            month = uiState.month,
                            destinationCd = uiState.destinationCd,
                            keyword = uiState.keyword,
                            destinationOptions = uiState.destinationOptions,
                            onYearChange = viewModel::setYear,
                            onMonthChange = viewModel::setMonth,
                            onDestinationChange = viewModel::setDestinationCd,
                            onKeywordChange = viewModel::setKeyword,
                            onPrevPeriod = viewModel::goPrevPeriod,
                            onNextPeriod = viewModel::goNextPeriod,
                            onCurrentMonth = viewModel::goCurrentMonth,
                        )
                    }
                    item {
                        OrderMonthlySummaryCards(summary = uiState.summary)
                    }
                    if (uiState.isLoading && uiState.pageItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = LoginColors.Primary)
                            }
                        }
                    } else if (uiState.pageItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("データがありません", color = OrderMonthlyColors.TextMuted)
                            }
                        }
                    } else {
                        item {
                            OrderMonthlyDataTable(
                                items = uiState.pageItems,
                                onDailyOrder = viewModel::openDailyBatchDialog,
                                onEdit = viewModel::openEditDialog,
                                onDelete = viewModel::openDeleteDialog,
                            )
                        }
                        item {
                            OrderMonthlyPagination(
                                page = uiState.page,
                                pageSize = uiState.pageSize,
                                total = uiState.total,
                                onPageChange = viewModel::setPage,
                            )
                        }
                    }
                }
            }
        }
    }

    OrderMonthlyDialogs(state = uiState, viewModel = viewModel)
}
