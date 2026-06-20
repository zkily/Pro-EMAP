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
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDataTable
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard
import com.example.smart_emap.ui.erp.production.requirements.RequirementSearchButton

@Composable
fun MaterialConsumptionScreen(viewModel: MaterialConsumptionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.search() }
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    val materialOptions = remember(uiState.materialOptions) {
        listOf("" to "全材料") + uiState.materialOptions.map { opt ->
            opt.processCd to "${opt.processCd}　${opt.processName}"
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
                    title = "材料消費実績",
                    subtitle = "日別・材料別の使用数を集計・可視化",
                    actionLoading = uiState.loading,
                    onRefresh = viewModel::search,
                )

                ProductionFilterSurface {
                    ProductionDropdownFilter(
                        label = "材料",
                        value = uiState.materialCd,
                        options = materialOptions,
                        onSelect = viewModel::setMaterial,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                        RequirementSearchButton(loading = uiState.loading, onClick = viewModel::search)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::reset, modifier = Modifier.height(34.dp)) {
                            Text("リセット", fontSize = 12.sp)
                        }
                    }
                }

                ProductionTableCard(title = "日別使用数") {
                    if (uiState.byDate.isEmpty()) {
                        ChartHint()
                    } else {
                        ActualVerticalBarChart(
                            labels = uiState.byDate.map { shortDate(it.usageDate) },
                            values = uiState.byDate.map { it.total ?: 0.0 },
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            barColor = Color(0xFF3B82F6),
                        )
                    }
                }

                ProductionTableCard(title = "材料別（上位）") {
                    if (uiState.byMaterial.isEmpty()) {
                        ChartHint()
                    } else {
                        ActualHorizontalBarChart(
                            items = uiState.byMaterial.mapIndexed { i, m ->
                                ChartBarItem(
                                    label = m.materialName ?: m.materialCd ?: "",
                                    value = m.total ?: 0.0,
                                    color = Color(0xFF10B981),
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height((uiState.byMaterial.size * 30 + 24).dp),
                        )
                    }
                }

                ProductionTableCard(title = "レコード（${uiState.total} 件）", isLoading = uiState.loading) {
                    if (uiState.records.isEmpty()) {
                        ChartHint("データがありません")
                    } else {
                        ProductionDataTable(
                            headers = listOf("使用日", "材料CD", "材料名", "使用数", "来源", "反映"),
                            rows = uiState.records.map { r ->
                                listOf(
                                    r.usageDate ?: "—",
                                    r.materialCd.orEmpty(),
                                    r.materialName.orEmpty(),
                                    formatActualNum(r.usageCount ?: 0.0, if ((r.usageCount ?: 0.0) % 1.0 == 0.0) 0 else 2),
                                    r.source.orEmpty(),
                                    if (r.reflected == true) "済" else "未",
                                )
                            },
                            columnWidths = listOf(96, 100, 150, 80, 120, 56),
                            columnKeys = listOf("", "", "product_name", "count", "", ""),
                        )
                        PaginationBar(
                            page = uiState.page,
                            pageCount = uiState.pageCount,
                            onPrev = { viewModel.goToPage(uiState.page - 1) },
                            onNext = { viewModel.goToPage(uiState.page + 1) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationBar(page: Int, pageCount: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, enabled = page > 1) {
            Icon(Icons.Default.NavigateBefore, contentDescription = "前へ")
        }
        Text("$page / $pageCount", fontSize = 12.sp, color = Color(0xFF475569))
        IconButton(onClick = onNext, enabled = page < pageCount) {
            Icon(Icons.Default.NavigateNext, contentDescription = "次へ")
        }
    }
}

@Composable
private fun ChartHint(text: String = "データがありません") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

internal fun shortDate(iso: String?): String {
    if (iso == null || iso.length < 10) return iso ?: ""
    return iso.substring(5, 10)
}
