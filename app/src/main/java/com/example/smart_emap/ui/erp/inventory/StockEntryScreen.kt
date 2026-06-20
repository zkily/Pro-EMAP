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
import androidx.compose.material.icons.filled.Inventory2
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
fun StockEntryScreen(viewModel: StockEntryViewModel) {
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
                    title = "在庫取引登録",
                    subtitle = "製品・材料・部品・仕掛品の入出庫一元管理",
                    icon = Icons.Default.Inventory2,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshOptions,
                    dark = false,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("在庫種別", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            STOCK_TYPES.forEach { type ->
                                FilterChip(
                                    selected = form.stockType == type,
                                    onClick = { viewModel.setStockType(type) },
                                    label = { Text(type, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF667EEA),
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }
                }

                if (form.stockType.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.98f),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${form.stockType}登録",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF667EEA),
                            )

                            val targetOptions = if (form.stockType == "製品" || form.stockType == "仕掛品") {
                                uiState.productOptions.map { it.productCd to "${it.productCd} | ${it.productName.orEmpty()}" }
                            } else {
                                emptyList()
                            }
                            if (targetOptions.isNotEmpty()) {
                                InventoryDropdownField(
                                    label = "対象CD",
                                    value = form.targetCd,
                                    options = targetOptions,
                                    onSelect = { viewModel.updateForm { f -> f.copy(targetCd = it) } },
                                )
                            } else {
                                InventoryTextField(
                                    label = "対象CD",
                                    value = form.targetCd,
                                    onValueChange = { viewModel.updateForm { f -> f.copy(targetCd = it) } },
                                )
                            }

                            InventoryDropdownField(
                                label = "保管場所",
                                value = form.locationCd,
                                options = uiState.locationOptions,
                                onSelect = { viewModel.updateForm { f -> f.copy(locationCd = it) } },
                                allowEmpty = false,
                            )

                            if (form.stockType == "仕掛品") {
                                InventoryDropdownField(
                                    label = "工程",
                                    value = form.processCd,
                                    options = uiState.processOptions,
                                    onSelect = { viewModel.updateForm { f -> f.copy(processCd = it) } },
                                    allowEmpty = false,
                                )
                            }

                            Text("操作種別", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                TRANSACTION_TYPES.forEach { type ->
                                    FilterChip(
                                        selected = form.transactionType == type,
                                        onClick = { viewModel.updateForm { f -> f.copy(transactionType = type) } },
                                        label = { Text(type, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF667EEA),
                                            selectedLabelColor = Color.White,
                                        ),
                                    )
                                }
                            }

                            InventoryTextField(
                                label = "数量",
                                value = form.quantity,
                                onValueChange = { viewModel.updateForm { f -> f.copy(quantity = it) } },
                            )

                            InventoryDropdownField(
                                label = "単位",
                                value = form.unit,
                                options = STOCK_ENTRY_UNIT_OPTIONS.map { it to it },
                                onSelect = { viewModel.updateForm { f -> f.copy(unit = it) } },
                                allowEmpty = false,
                            )

                            InventoryTextField(
                                label = "操作日時",
                                value = form.transactionTime,
                                onValueChange = { viewModel.updateForm { f -> f.copy(transactionTime = it) } },
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
