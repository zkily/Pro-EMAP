package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets

@Composable
fun ProductInventoryListScreen(viewModel: ProductInventoryListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(value = true) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    if (uiState.showUpdateConfirm) {
        InventoryConfirmDialog(
            title = "在庫更新確認",
            message = "全製品の現在在庫情報を最新状態に更新しますか？\n(処理に数十秒かかる場合があります)",
            confirmText = "更新実行",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmUpdateInventory,
            onDismiss = viewModel::dismissUpdateConfirm,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A),
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProductInventoryCompactHeader(
                    count = uiState.totalCount,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshAll,
                    onToggleFilters = { showFilters = !showFilters },
                    onUpdateInventory = viewModel::requestUpdateInventory,
                )

                ProductInventoryStatRow(uiState.statValues)

                if (showFilters) {
                    ProductInventoryFilterRow(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        productCd = uiState.productCd,
                        keyword = uiState.keyword,
                        productOptions = uiState.productOptions,
                        onStartDateChange = viewModel::setStartDate,
                        onEndDateChange = viewModel::setEndDate,
                        onProductCdChange = viewModel::setProductCd,
                        onKeywordChange = viewModel::setKeyword,
                        onReset = viewModel::resetFilters,
                        onPrevDate = viewModel::setQuickDatePrev,
                        onNextDate = viewModel::setQuickDateNext,
                    )
                }

                ProductInventoryModernTable(
                    rows = uiState.rows,
                    isLoading = uiState.isLoading,
                )
            }
        }
    }
}

@Composable
private fun ProductInventoryCompactHeader(
    count: Int,
    loading: Boolean,
    onRefresh: () -> Unit,
    onToggleFilters: () -> Unit,
    onUpdateInventory: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("製品在庫照会", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${count}件の製品データ", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
            }

            IconButton(onClick = onToggleFilters, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.size(32.dp)) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF60A5FA))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            Button(
                onClick = onUpdateInventory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("在庫更新", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProductInventoryStatRow(stats: Map<String, Int>) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PRODUCT_INVENTORY_FIELDS.take(8).forEachIndexed { index, field ->
            val color = when(index % 4) {
                0 -> Color(0xFF3B82F6) to Color(0xFF2563EB)
                1 -> Color(0xFF10B981) to Color(0xFF059669)
                2 -> Color(0xFFF59E0B) to Color(0xFFD97706)
                else -> Color(0xFF6366F1) to Color(0xFF4F46E5)
            }
            ModernStatCard(
                label = field.label,
                value = formatInventoryNum(stats[field.key]),
                color1 = color.first,
                color2 = color.second,
            )
        }
    }
}

@Composable
private fun ProductInventoryFilterRow(
    startDate: String,
    endDate: String,
    productCd: String,
    keyword: String,
    productOptions: List<com.example.smart_emap.data.model.ProductionSummaryProductOptionDto>,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onProductCdChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    onReset: () -> Unit,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.padding(horizontal = 8.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.08f)),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Date Range Section
            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    onClick = onPrevDate,
                    modifier = Modifier.size(32.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("<", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                       TextButton(onClick = { showStartPicker = true }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(36.dp)) {
                           Text(startDate.takeLast(5), fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                       }
                       Text("〜", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                       TextButton(onClick = { showEndPicker = true }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(36.dp)) {
                           Text(endDate.takeLast(5), fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                       }
                    }
                }

                Surface(
                    onClick = onNextDate,
                    modifier = Modifier.size(32.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(">", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Product Name Dropdown (Now centered, label changed from 品番 to 製品名)
            InventoryDropdownField(
                label = "製品名",
                value = productCd,
                options = productOptions.map { it.productCd to (it.productName ?: it.productCd) },
                onSelect = onProductCdChange,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).height(56.dp),
            )

            // Keyword Search Section (Now centered)
            TextField(
                value = keyword,
                onValueChange = onKeywordChange,
                placeholder = { 
                    Text(
                        "キーワード検索...", 
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp, 
                        color = Color.White.copy(alpha = 0.4f)
                    ) 
                },
                modifier = Modifier.weight(1.5f).height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                    focusedIndicatorColor = Color(0xFF4F46E5),
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF4F46E5),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center)
            )
            
            Surface(
                onClick = onReset,
                modifier = Modifier.size(40.dp),
                color = Color(0xFF4F46E5).copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showStartPicker) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = Color(0xFF4F46E5),
            onDismiss = { showStartPicker = false },
            onConfirm = { onStartDateChange(it); showStartPicker = false },
        )
    }
    if (showEndPicker) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = Color(0xFF4F46E5),
            onDismiss = { showEndPicker = false },
            onConfirm = { onEndDateChange(it); showEndPicker = false },
        )
    }
}

@Composable
private fun ProductInventoryModernTable(
    rows: List<ProductionSummaryFullRowDto>,
    isLoading: Boolean,
) {
    val scroll = rememberScrollState()
    
    Surface(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        color = Color.White.copy(alpha = 0.02f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InventoryTableHeaderCell("日付", 80, dark = true)
                InventoryTableHeaderCell("品番", 80, dark = true)
                InventoryTableHeaderCell("品名", 120, dark = true)
                PRODUCT_INVENTORY_FIELDS.forEach { field ->
                    InventoryTableHeaderCell(field.label, 60, dark = true)
                }
            }

            if (isLoading && rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF60A5FA))
                }
            } else if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                    Text("データが見つかりません", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    itemsIndexed(rows) { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scroll)
                                .background(if ((index % 2) == 0) Color.Transparent else Color.White.copy(alpha = 0.02f))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            InventoryTableCell(row.date.orEmpty(), 80, dark = true)
                            InventoryTableCell(row.productCd.orEmpty(), 80, dark = true, bold = true)
                            InventoryTableCell(row.productName.orEmpty(), 120, dark = true)
                            PRODUCT_INVENTORY_FIELDS.forEach { field ->
                                val value = productInventoryValue(row, field.key)
                                InventoryTableCell(
                                    text = formatInventoryNum(value),
                                    width = 60,
                                    dark = true,
                                    bold = value != 0,
                                    fontSize = 11, // Increased font size for numbers
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}
