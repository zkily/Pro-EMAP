package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import com.example.smart_emap.ui.shell.LayoutColors
import androidx.compose.material3.Button

@Composable
fun MaterialReceivingHistoryScreen(viewModel: MaterialReceivingHistoryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    uiState.detailItem?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::hideDetail,
            title = { Text(item.materialName.orEmpty().ifBlank { "受入詳細" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("日付: ${item.logDate.orEmpty()} ${item.logTime.orEmpty()}")
                    Text("材料CD: ${item.materialCd.orEmpty()}")
                    Text("製造番号: ${item.manufactureNo.orEmpty()}")
                    Text("数量: ${item.quantity ?: 0}")
                    Text("外径1/2: ${item.outerDiameter1 ?: "-"} / ${item.outerDiameter2 ?: "-"}")
                    Text("仕入先: ${item.supplier.orEmpty()}")
                    Text("材質: ${item.materialQuality.orEmpty()}")
                    Text("備考: ${item.remarks.orEmpty()}")
                }
            },
            confirmButton = { TextButton(onClick = viewModel::hideDetail) { Text("閉じる") } },
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
                    PurchaseHeroHeader(title = "材料受入履歴", subtitle = "材料の受入・検品を管理します")
                }
                item {
                    PurchaseStatGrid(
                        listOf(
                            "総件数" to uiState.totalCount.toString(),
                            "表示件数" to uiState.items.size.toString(),
                        ),
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
                            accent = androidx.compose.ui.graphics.Color(0xFFE6A23C),
                            onStartChange = viewModel::setStartDate,
                            onEndChange = viewModel::setEndDate,
                        )
                        Button(onClick = viewModel::search, modifier = Modifier.fillMaxWidth()) {
                            Text("検索")
                        }
                    }
                }
                if (uiState.isLoading) item { PurchaseLoadingOverlay(true) }
                if (!uiState.isLoading && uiState.items.isEmpty()) {
                    item { PurchaseEmptyHint("該当する受入履歴がありません") }
                }
                items(uiState.items, key = { it.id ?: it.hashCode() }) { row ->
                    PurchaseDataRowCard(
                        title = row.materialName.orEmpty().ifBlank { row.materialCd.orEmpty() },
                        subtitle = "${row.logDate.orEmpty()} · ${row.supplier.orEmpty()}",
                        chips = listOf(
                            "製造番号 ${row.manufactureNo.orEmpty()}",
                            "数量 ${row.quantity ?: 0}",
                            "外径 ${row.outerDiameter1 ?: "-"}",
                        ),
                        modifier = Modifier.clickable { viewModel.showDetail(row) },
                    )
                }
            }
        }
    }
}
