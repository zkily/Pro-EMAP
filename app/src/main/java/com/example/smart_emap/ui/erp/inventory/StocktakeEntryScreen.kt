package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StocktakeEntryScreen(viewModel: StocktakeEntryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val form = uiState.form

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
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InventoryGlassHeader(
                    title = "棚卸登録",
                    subtitle = "材料・部品・製品棚卸データの手入力",
                    icon = Icons.Default.Edit,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshOptions,
                    dark = false,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.98f),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("項目", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            STOCKTAKE_ITEMS.forEach { item ->
                                FilterChip(
                                    selected = form.item == item,
                                    onClick = { viewModel.setItem(item) },
                                    label = { Text(item, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF667EEA),
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }

                        if (form.item.isNotBlank()) {
                            InventoryDropdownField(
                                label = "製品CD",
                                value = form.productCd,
                                options = uiState.productOptions.map {
                                    it.productCd to "${it.productCd} | ${it.productName.orEmpty()}"
                                },
                                onSelect = viewModel::selectProduct,
                            )
                            InventoryTextField(
                                label = "製品名",
                                value = form.productName,
                                onValueChange = { viewModel.updateForm { f -> f.copy(productName = it) } },
                            )
                            InventoryDropdownField(
                                label = "工程",
                                value = form.processCd,
                                options = uiState.processOptions,
                                onSelect = { viewModel.updateForm { f -> f.copy(processCd = it) } },
                                allowEmpty = false,
                            )
                            InventoryTextField(
                                label = "日付 (YYYY-MM-DD)",
                                value = form.logDate,
                                onValueChange = { viewModel.updateForm { f -> f.copy(logDate = it) } },
                            )
                            InventoryTextField(
                                label = "時間 (HH:mm:ss)",
                                value = form.logTime,
                                onValueChange = { viewModel.updateForm { f -> f.copy(logTime = it) } },
                            )
                            InventoryTextField(
                                label = "HD番号",
                                value = form.hdNo,
                                onValueChange = { viewModel.updateForm { f -> f.copy(hdNo = it) } },
                            )
                            InventoryTextField(
                                label = "入数",
                                value = form.packQty,
                                onValueChange = { viewModel.updateForm { f -> f.copy(packQty = it) } },
                            )
                            InventoryTextField(
                                label = "ケース数",
                                value = form.caseQty,
                                onValueChange = { viewModel.updateForm { f -> f.copy(caseQty = it) } },
                            )
                            InventoryTextField(
                                label = "数量",
                                value = form.quantity,
                                onValueChange = { viewModel.updateForm { f -> f.copy(quantity = it) } },
                            )
                            InventoryTextField(
                                label = "備考",
                                value = form.remarks,
                                singleLine = false,
                                onValueChange = { viewModel.updateForm { f -> f.copy(remarks = it) } },
                            )
                            Button(
                                onClick = viewModel::submit,
                                enabled = !uiState.actionLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                            ) {
                                if (uiState.actionLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(end = 8.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                    )
                                }
                                Text("登録")
                            }
                        }
                    }
                }
            }
        }
    }
}
