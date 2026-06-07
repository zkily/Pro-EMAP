package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.example.smart_emap.ui.erp.purchase.PurchaseTabChipRow
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialReceivingInspectionScreen(viewModel: MaterialReceivingInspectionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    uiState.editingMaster?.let {
        AlertDialog(
            onDismissRequest = viewModel::closeMasterEdit,
            title = { Text("品質基準設定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(it.materialName.orEmpty())
                    OutlinedTextField(
                        value = uiState.editTolerance1,
                        onValueChange = viewModel::setEditTolerance1,
                        label = { Text("外径1公差") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = uiState.editTolerance2,
                        onValueChange = viewModel::setEditTolerance2,
                        label = { Text("外径2公差") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::saveMasterEdit) { Text("保存") } },
            dismissButton = { TextButton(onClick = viewModel::closeMasterEdit) { Text("取消") } },
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
                        title = "材料受入検品管理",
                        subtitle = "材料の品質検査・検品作業を管理します",
                    )
                }
                item {
                    PurchaseTabChipRow(
                        tabs = listOf("検品履歴", "品質基準設定"),
                        selectedIndex = uiState.tabIndex,
                        onSelect = viewModel::setTab,
                    )
                }
                if (uiState.tabIndex == 0) {
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
                                accent = androidx.compose.ui.graphics.Color(0xFF67C23A),
                                onStartChange = viewModel::setStartDate,
                                onEndChange = viewModel::setEndDate,
                            )
                            Button(onClick = viewModel::searchHistory, modifier = Modifier.fillMaxWidth()) {
                                Text("検索")
                            }
                        }
                    }
                    if (uiState.isLoading) item { PurchaseLoadingOverlay(true) }
                    if (!uiState.isLoading && uiState.historyItems.isEmpty()) {
                        item { PurchaseEmptyHint("検品履歴がありません") }
                    }
                    items(uiState.historyItems, key = { it.id ?: it.hashCode() }) { row ->
                        PurchaseDataRowCard(
                            title = row.materialName.orEmpty(),
                            subtitle = "${row.logDate.orEmpty()} · ${row.supplier.orEmpty()}",
                            chips = listOf(
                                "外径 ${row.outerDiameter1 ?: "-"} / ${row.outerDiameter2 ?: "-"}",
                                "製造番号 ${row.manufactureNo.orEmpty()}",
                            ),
                        )
                    }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.masterKeyword,
                                onValueChange = viewModel::setMasterKeyword,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("材料名検索") },
                                singleLine = true,
                            )
                            Button(onClick = viewModel::searchMaster, modifier = Modifier.fillMaxWidth()) {
                                Text("検索")
                            }
                        }
                    }
                    if (uiState.isLoading) item { PurchaseLoadingOverlay(true) }
                    if (!uiState.isLoading && uiState.masterItems.isEmpty()) {
                        item { PurchaseEmptyHint("材料マスタがありません") }
                    }
                    items(uiState.masterItems, key = { it.id ?: it.materialCd.orEmpty() }) { row ->
                        PurchaseDataRowCard(
                            title = row.materialName.orEmpty(),
                            subtitle = row.supplierName.orEmpty(),
                            chips = listOf(
                                "外径1 ${row.tolerance1 ?: "-"}",
                                "外径2 ${row.tolerance2 ?: "-"}",
                            ),
                            modifier = Modifier.clickable { viewModel.openMasterEdit(row) },
                        )
                    }
                }
            }
        }
    }
}
