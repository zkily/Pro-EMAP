package com.example.smart_emap.ui.erp.production.actual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.ProductionActualLogDto
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterSurface
import com.example.smart_emap.ui.erp.production.planning.ProductionHeroBar
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiCard
import com.example.smart_emap.ui.erp.production.planning.ProductionKpiStrip
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding
import com.example.smart_emap.ui.erp.production.planning.ProductionTableCard
import com.example.smart_emap.ui.erp.production.planning.ProductionTabStrip

@Composable
fun ProductionActualManagementScreen(viewModel: ProductionActualManagementViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteTarget by remember { mutableStateOf<ProductionActualLogDto?>(null) }

    LaunchedEffect(Unit) { viewModel.loadData() }
    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }
    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "生産実績_取引ログ",
            layout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
        )
        if (!opened) snackbarHostState.showSnackbar("印刷を開始できませんでした")
        viewModel.clearPendingPrintHtml()
    }

    val processTabs = remember(uiState.processOptions) {
        listOf("ALL" to "全工程") + uiState.processOptions.map { it.processCd to it.processName }
    }
    val productFilterOptions = remember(uiState.productOptions) {
        listOf("" to "（すべて）") + uiState.productOptions.map { it to it }
    }
    val machineFilterOptions = remember(uiState.machineOptions) {
        listOf("" to "（すべて）") + uiState.machineOptions.map { it to it }
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
                    title = "生産実績管理",
                    subtitle = "工程別に在庫取引ログ（実績）を横断分析",
                    actionLoading = uiState.loading,
                    onRefresh = viewModel::loadData,
                )

                ProductionFilterSurface {
                    ProductionCompactDateRangeField(
                        startDate = uiState.dateFrom,
                        endDate = uiState.dateTo,
                        onStartChange = { viewModel.setRange(it, uiState.dateTo) },
                        onEndChange = { viewModel.setRange(uiState.dateFrom, it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PamChip("前日") { viewModel.shiftDay(-1) }
                        PamChip("今日", primary = true) { viewModel.setToday() }
                        PamChip("翌日") { viewModel.shiftDay(1) }
                    }
                    ProductionDropdownFilter(
                        label = "製品名",
                        value = uiState.targetName,
                        options = productFilterOptions,
                        onSelect = viewModel::setTargetName,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ProductionDropdownFilter(
                        label = "設備",
                        value = uiState.machineName,
                        options = machineFilterOptions,
                        onSelect = viewModel::setMachineName,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ProductionTabStrip(
                        tabs = processTabs,
                        selected = uiState.activeProcessCd,
                        onSelect = viewModel::setProcessTab,
                    )
                }

                if (uiState.typeSummary.isNotEmpty()) {
                    TypeSummaryRow(uiState)
                }

                PamStatStrip(uiState)

                ProductionTableCard(title = "取引ログ一覧（${uiState.total} 件）", isLoading = uiState.loading) {
                    if (uiState.records.isEmpty()) {
                        PamHint("該当データがありません")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.records.forEach { r ->
                                RecordCard(
                                    record = r,
                                    onEdit = { viewModel.openEdit(r) },
                                    onDelete = { deleteTarget = r },
                                )
                            }
                        }
                        PamPaginationBar(
                            page = uiState.page,
                            pageCount = uiState.pageCount,
                            onPrev = { viewModel.goToPage(uiState.page - 1) },
                            onNext = { viewModel.goToPage(uiState.page + 1) },
                        )
                        TextButton(onClick = viewModel::printTable) {
                            Text("取引ログ印刷", fontSize = 12.sp)
                        }
                    }
                }

                ProductionTableCard(title = "日別生産量推移") {
                    if (uiState.dailyValues.all { it == 0.0 }) {
                        PamHint("該当データがありません")
                    } else {
                        ActualVerticalBarChart(
                            labels = uiState.dailyLabels,
                            values = uiState.dailyValues,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            barColor = Color(0xFF3B82F6),
                        )
                    }
                }

                ProductionTableCard(title = "工程別生産量") {
                    if (uiState.processAgg.isEmpty()) {
                        PamHint("該当データがありません")
                    } else {
                        ActualHorizontalBarChart(
                            items = uiState.processAgg.mapIndexed { i, p ->
                                ChartBarItem(p.name, p.total, ActualPalette[i % ActualPalette.size])
                            },
                            modifier = Modifier.fillMaxWidth().height((uiState.processAgg.size * 30 + 16).dp),
                        )
                    }
                }

                ProductionTableCard(title = "取引タイプ分布") {
                    if (uiState.typeDist.isEmpty()) {
                        PamHint("該当データがありません")
                    } else {
                        val items = uiState.typeDist.mapIndexed { i, p ->
                            ChartBarItem(p.name, p.total, ActualPalette[i % ActualPalette.size])
                        }
                        Column {
                            ActualDonutChart(items = items, modifier = Modifier.fillMaxWidth().height(200.dp))
                            ChartLegend(items)
                        }
                    }
                }

                ProductionTableCard(title = "製品生産量 TOP10") {
                    if (uiState.productTop.isEmpty()) {
                        PamHint("該当データがありません")
                    } else {
                        ActualHorizontalBarChart(
                            items = uiState.productTop.mapIndexed { i, p ->
                                ChartBarItem(p.name, p.total, ActualPalette[i % ActualPalette.size])
                            },
                            modifier = Modifier.fillMaxWidth().height((uiState.productTop.size * 30 + 16).dp),
                        )
                    }
                }
            }
        }
    }

    if (uiState.editVisible) {
        EditDialog(
            uiState = uiState,
            onQuantityChange = viewModel::setEditQuantity,
            onConfirm = viewModel::saveEdit,
            onDismiss = viewModel::closeEdit,
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("削除確認") },
            text = { Text("取引ログ（ID: ${target.id}）を削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    target.id?.let { viewModel.deleteRecord(it) }
                    deleteTarget = null
                }) { Text("削除", color = Color(0xFFDC2626)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("キャンセル") }
            },
        )
    }
}

@Composable
private fun EditDialog(
    uiState: ProductionActualManagementUiState,
    onQuantityChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val e = uiState.edit
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("取引ログ編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReadonlyLine("取引日時", pamDateTime(e.transactionTime))
                ReadonlyLine("取引タイプ", e.transactionType.ifBlank { "-" })
                ReadonlyLine("区分", e.stockType.ifBlank { "-" })
                ReadonlyLine("製品コード", e.targetCd.ifBlank { "-" })
                ReadonlyLine("製品名", e.targetName.ifBlank { "-" })
                ReadonlyLine("設備名", e.machineName.ifBlank { "-" })
                OutlinedTextField(
                    value = e.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("数量") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !uiState.saving) {
                Text(if (uiState.saving) "保存中…" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )
}

@Composable
private fun ReadonlyLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(84.dp))
        Text(value, fontSize = 12.sp, color = Color(0xFF334155))
    }
}

@Composable
private fun RecordCard(record: ProductionActualLogDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(pamDateTime(record.transactionTime), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    TypeTag(record.transactionType ?: "-")
                    Text(record.processName ?: "-", fontSize = 11.sp, color = Color(0xFF6366F1))
                }
                Text(
                    buildString {
                        append(record.targetCd ?: "-")
                        if (!record.targetName.isNullOrBlank()) append("  ${record.targetName}")
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("数量 ${formatActualNum(record.quantity ?: 0.0)}", fontSize = 11.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                    if (!record.machineName.isNullOrBlank()) {
                        Text(record.machineName!!, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "編集", tint = Color(0xFF2563EB))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
private fun TypeTag(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(text, fontSize = 10.sp, color = Color(0xFF475569))
    }
}

@Composable
private fun TypeSummaryRow(uiState: ProductionActualManagementUiState) {
    ProductionTableCard(title = "取引タイプ内訳") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            uiState.typeSummary.forEach { t ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.transactionType ?: "-", fontSize = 12.sp, color = Color(0xFF334155))
                    Text(
                        "${formatActualNum((t.recordCount ?: 0).toDouble())} 件 / ${formatActualNum(t.totalQuantity ?: 0.0)}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                    )
                }
            }
        }
    }
}

@Composable
private fun PamStatStrip(uiState: ProductionActualManagementUiState) {
    val s = uiState.stats
    ProductionKpiStrip(
        cards = listOf(
            pamKpi("総レコード", "${formatActualNum((s.totalRecords ?: 0).toDouble())} 件", Color(0xFF3B82F6)),
            pamKpi("数量合計", "${formatActualNum(s.totalQuantity ?: 0.0)} 本", Color(0xFF10B981)),
            pamKpi("平均数量/件", "${formatActualNum(s.avgQuantity ?: 0.0, 1)} 本/件", Color(0xFFF59E0B)),
            pamKpi("対象製品数", "${formatActualNum((s.productCount ?: 0).toDouble())} 品目", Color(0xFF8B5CF6)),
            pamKpi("稼働日数", "${formatActualNum((s.activeDays ?: 0).toDouble())} 日", Color(0xFF06B6D4)),
        ),
    )
}

private fun pamKpi(label: String, value: String, color: Color) = ProductionKpiCard(
    label = label,
    value = value,
    accent = Brush.linearGradient(listOf(color, color)),
)

@Composable
private fun PamPaginationBar(page: Int, pageCount: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, enabled = page > 1) {
            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "前へ")
        }
        Text("$page / $pageCount", fontSize = 12.sp, color = Color(0xFF475569))
        IconButton(onClick = onNext, enabled = page < pageCount) {
            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "次へ")
        }
    }
}

@Composable
private fun PamChip(text: String, primary: Boolean = false, onClick: () -> Unit) {
    val bg = if (primary) Color(0xFF2563EB) else Color(0xFFF1F5F9)
    val fg = if (primary) Color.White else Color(0xFF475569)
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun PamHint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

private fun pamDateTime(iso: String?): String {
    val s = iso.orEmpty()
    if (s.length < 16) return s
    return s.substring(0, 16).replace('T', ' ')
}
