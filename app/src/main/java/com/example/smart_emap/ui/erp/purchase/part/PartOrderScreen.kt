package com.example.smart_emap.ui.erp.purchase.part

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.order.OrderDailyCalendarRangeField
import com.example.smart_emap.ui.erp.purchase.PurchaseDataRowCard
import com.example.smart_emap.ui.erp.purchase.PurchaseEmptyHint
import com.example.smart_emap.ui.erp.purchase.PurchaseHeroHeader
import com.example.smart_emap.ui.erp.purchase.PurchaseLoadingOverlay
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseStatGrid
import com.example.smart_emap.ui.erp.purchase.PurchaseTabChipRow
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PartOrderScreen(viewModel: PartOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = listOf(
        PartOrderTab.Daily to "日別在庫",
        PartOrderTab.Usage to "使用管理",
        PartOrderTab.Order to "注文",
        PartOrderTab.OrderHistory to "注文履歴",
    )
    val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.tab }.coerceAtLeast(0)

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    uiState.editingItem?.let {
        AlertDialog(
            onDismissRequest = viewModel::closeEdit,
            title = { Text("注文数量編集") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it.partName.orEmpty())
                    OutlinedTextField(
                        value = uiState.editOrderQty,
                        onValueChange = viewModel::setEditOrderQty,
                        label = { Text("注文数量") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = uiState.editRemarks,
                        onValueChange = viewModel::setEditRemarks,
                        label = { Text("備考") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::saveEdit) { Text("保存") } },
            dismissButton = { TextButton(onClick = viewModel::closeEdit) { Text("取消") } },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        PurchasePageBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    PurchaseHeroHeader(
                        title = "部品在庫管理(発注・使用)",
                        subtitle = "Part Stock & Order",
                    )
                }
                item {
                    PurchaseStatGrid(
                        listOf(
                            "部品种类" to uiState.stats.totalParts.toString(),
                            "在庫合計" to uiState.stats.totalCurrentStock.toString(),
                            "注文数量" to uiState.stats.totalOrderQuantity.toString(),
                            "注文金額" to "%.0f".format(uiState.stats.totalOrderAmount),
                        ),
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = viewModel::syncMaster, enabled = !uiState.actionLoading, modifier = Modifier.weight(1f)) {
                            Text("マスタ更新", maxLines = 1)
                        }
                        OutlinedButton(onClick = viewModel::generateData, enabled = !uiState.actionLoading, modifier = Modifier.weight(1f)) {
                            Text("データ生成", maxLines = 1)
                        }
                        OutlinedButton(onClick = viewModel::calculateStock, enabled = !uiState.actionLoading, modifier = Modifier.weight(1f)) {
                            Text("在庫計算", maxLines = 1)
                        }
                    }
                }
                item {
                    PurchaseTabChipRow(
                        tabs = tabs.map { it.second },
                        selectedIndex = selectedTabIndex,
                        onSelect = { index -> viewModel.setTab(tabs[index].first) },
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.keyword,
                            onValueChange = viewModel::setKeyword,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("キーワード") },
                            singleLine = true,
                        )
                        OrderDailyCalendarRangeField(
                            startDate = uiState.startDate,
                            endDate = uiState.endDate,
                            accent = androidx.compose.ui.graphics.Color(0xFF667EEA),
                            onStartChange = viewModel::setStartDate,
                            onEndChange = viewModel::setEndDate,
                        )
                        Button(onClick = viewModel::search, modifier = Modifier.fillMaxWidth()) {
                            Text("検索")
                        }
                    }
                }
                if (uiState.isLoading || uiState.actionLoading) item { PurchaseLoadingOverlay(true) }
                if (!uiState.isLoading && uiState.items.isEmpty()) {
                    item { PurchaseEmptyHint("在庫データがありません") }
                }
                items(uiState.items, key = { it.id ?: it.hashCode() }) { row ->
                    PurchaseDataRowCard(
                        title = row.partName.orEmpty(),
                        subtitle = "${row.date.orEmpty()} · ${row.supplierName.orEmpty()}",
                        chips = listOf(
                            "在庫 ${row.currentStock ?: 0}",
                            "使用 ${row.plannedUsage ?: 0}",
                            "注文 ${row.orderQuantity ?: 0}",
                        ),
                        modifier = Modifier.clickable { viewModel.openEdit(row) },
                    )
                }
            }
        }
    }
}
