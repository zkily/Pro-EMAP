package com.example.smart_emap.ui.erp.order

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, "納入先別受注履歴")
            putExtra(Intent.EXTRA_TEXT, html)
        }
        context.startActivity(Intent.createChooser(intent, "印刷 / 共有"))
        viewModel.clearPendingPrintHtml()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFFEEF2FF),
                            androidx.compose.ui.graphics.Color(0xFFF8FAFC),
                            androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                        ),
                    ),
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
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
                item {
                    DestHistorySummarySection(summary = uiState.summaryItems)
                }
                item {
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
