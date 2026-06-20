package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.InventoryLogRowDto
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDateFilterRow
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun StocktakeListScreen(viewModel: StocktakeListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        InventoryLightPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InventoryGlassHeader(
                    title = "棚卸リスト一覧",
                    subtitle = "材料・部品・製品棚卸データ",
                    icon = Icons.Default.Inventory2,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshAll,
                    dark = false,
                    trailing = {
                        Button(
                            onClick = viewModel::importLogs,
                            enabled = !uiState.importLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        ) {
                            if (uiState.importLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 6.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            }
                            Text("棚卸データ取込", fontSize = 10.sp)
                        }
                    },
                )

                InventoryFilterPanel(onReset = viewModel::resetFilters) {
                    InventoryTextField(
                        label = "キーワード",
                        value = uiState.keyword,
                        onValueChange = viewModel::setKeyword,
                    )
                    Text("項目", fontSize = 10.sp, color = Color(0xFF64748B))
                    InventoryChipRow(
                        options = STOCKTAKE_ITEMS,
                        selected = uiState.item,
                        onSelect = viewModel::setItem,
                        dark = false,
                    )
                    InventoryDropdownField(
                        label = "ステー種別",
                        value = uiState.stageType,
                        options = uiState.processOptions,
                        onSelect = viewModel::setStageType,
                    )
                    InventoryTextField(
                        label = "月選択 (YYYY-MM)",
                        value = uiState.monthPicker,
                        onValueChange = viewModel::setMonthPicker,
                    )
                    OutsourcingDateFilterRow(
                        startDate = uiState.dateStart,
                        endDate = uiState.dateEnd,
                        onStartChange = viewModel::setDateStart,
                        onEndChange = viewModel::setDateEnd,
                        onShiftDate = { days ->
                            val start = java.time.LocalDate.parse(uiState.dateStart).plusDays(days.toLong())
                            val end = java.time.LocalDate.parse(uiState.dateEnd).plusDays(days.toLong())
                            viewModel.setDateStart(start.toString())
                            viewModel.setDateEnd(end.toString())
                        },
                        onToday = {
                            val today = java.time.LocalDate.now().toString()
                            viewModel.setDateStart(today)
                            viewModel.setDateEnd(today)
                        },
                        onThisMonth = {
                            val today = java.time.LocalDate.now()
                            viewModel.setDateStart(today.withDayOfMonth(1).toString())
                            viewModel.setDateEnd(today.toString())
                        },
                    )
                    Button(
                        onClick = {
                            if (uiState.monthPicker.isNotBlank()) {
                                viewModel.applyMonthPicker(uiState.monthPicker)
                            } else {
                                viewModel.search()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                    ) {
                        Text("検索")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${uiState.total}件", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("計 ${formatInventoryNum(uiState.totalQuantity)}", fontSize = 11.sp, color = Color(0xFF667EEA))
                }

                InventoryTablePanel(
                    title = "棚卸データ",
                    countLabel = "${uiState.rows.size}件表示",
                    loading = uiState.isLoading,
                    dark = false,
                ) {
                    StocktakeListTable(rows = uiState.rows, onDelete = viewModel::requestDelete)
                }

                InventoryPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    total = uiState.total,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                    dark = false,
                )
            }
        }
    }

    if (uiState.showDeleteConfirm) {
        InventoryConfirmDialog(
            title = "削除確認",
            message = "「${uiState.pendingDelete?.productName.orEmpty()}」の棚卸記録を削除しますか？",
            confirmText = "削除",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteConfirm,
        )
    }
}

@Composable
private fun StocktakeListTable(
    rows: List<InventoryLogRowDto>,
    onDelete: (InventoryLogRowDto) -> Unit,
) {
    val scroll = rememberScrollState()
    Column {
        Row(
            modifier = Modifier
                .horizontalScroll(scroll)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            InventoryTableHeaderCell("項目", 72)
            InventoryTableHeaderCell("製品CD", 72)
            InventoryTableHeaderCell("製品名", 120)
            InventoryTableHeaderCell("工程", 72)
            InventoryTableHeaderCell("日付", 80)
            InventoryTableHeaderCell("時間", 56)
            InventoryTableHeaderCell("数量", 56)
            InventoryTableHeaderCell("備考", 80)
            InventoryTableHeaderCell("操作", 48)
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .horizontalScroll(scroll)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                InventoryTableCell(row.item.orEmpty(), 72)
                InventoryTableCell(row.productCd.orEmpty(), 72)
                InventoryTableCell(row.productName.orEmpty(), 120)
                InventoryTableCell(row.processName ?: row.processCd.orEmpty(), 72)
                InventoryTableCell(row.logDate.orEmpty(), 80)
                InventoryTableCell(row.logTime.orEmpty(), 56)
                InventoryTableCell(formatInventoryNum(row.quantity), 56, bold = true)
                InventoryTableCell(row.remarks.orEmpty(), 80)
                TextButton(onClick = { onDelete(row) }) {
                    Text("削除", fontSize = 9.sp, color = Color(0xFFEF4444))
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}
