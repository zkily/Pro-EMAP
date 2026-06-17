package com.example.smart_emap.ui.erp.order

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.shell.LayoutColors
import kotlinx.coroutines.delay

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshAll,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF1F4FB), Color(0xFFEEF2FF), Color(0xFFECFEFF)),
                        ),
                    ),
            ) {
                OrderDailyAnimatedBackground()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        OrderDailyStaggeredReveal(index = 0) {
                            OrderDailyPageHero(
                                lastFetchedText = uiState.lastFetchedText,
                                startDate = uiState.startDate,
                                endDate = uiState.endDate,
                                destinationCd = uiState.destinationCd,
                                keyword = uiState.keyword,
                                destinationOptions = uiState.destinationOptions,
                                productOptions = uiState.heroProductOptions,
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
                    }
                    uiState.listError?.let { error ->
                        item {
                            OrderDailyStaggeredReveal(index = 1) {
                                OrderDailyErrorBanner(message = error)
                            }
                        }
                    }
                    item {
                        OrderDailyStaggeredReveal(index = 2) {
                            OrderDailySummaryStrip(summary = uiState.summary)
                        }
                    }
                    item {
                        OrderDailyStaggeredReveal(index = 3) {
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
}

@Composable
private fun OrderDailyStaggeredReveal(
    index: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it / 6 } +
            scaleIn(initialScale = 0.98f, animationSpec = tween(400, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}

@Composable
private fun OrderDailyAnimatedBackground() {
    val transition = rememberInfiniteTransition(label = "daily-bg")
    val orb1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8500), RepeatMode.Reverse),
        label = "orb1",
    )
    val orb2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse),
        label = "orb2",
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-40).dp, y = (-60).dp + (orb1 * 18).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x336366F1), Color.Transparent),
                        radius = 320f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (orb2 * 24).dp, y = 120.dp)
                .align(androidx.compose.ui.Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x280EA5E9), Color.Transparent),
                        radius = 260f,
                    ),
                ),
        )
    }
}

@Composable
private fun OrderDailyErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33DC2626))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2)),
                ),
            )
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            color = Color(0xFFB91C1C),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
        )
    }
}
