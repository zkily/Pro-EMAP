package com.example.smart_emap.ui.erp.inventory

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialInventoryListScreen(viewModel: MaterialInventoryListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        InventoryLightPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                InventoryGlassHeader(
                    title = "材料在庫照会",
                    subtitle = "全 ${uiState.rows.size} 件の在庫データ",
                    icon = Icons.Default.Category,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshAll,
                    dark = false,
                    trailing = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (showFilters) Color(0xFF6366F1) else Color(0xFF64748B)
                            )
                        }
                    }
                )

                InventoryStatCardsGrid(
                    fields = MATERIAL_STAT_FIELDS,
                    values = uiState.statValues,
                    dark = false,
                )

                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MaterialInventoryFilterPanel(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        keyword = uiState.keyword,
                        onStartChange = viewModel::setStartDate,
                        onEndChange = viewModel::setEndDate,
                        onKeywordChange = viewModel::setKeyword,
                        onPrev = viewModel::setQuickDatePrev,
                        onToday = viewModel::setQuickDateToday,
                        onNext = viewModel::setQuickDateNext,
                        onReset = viewModel::resetFilters,
                    )
                }

                InventoryModernTable(
                    title = "材料在庫一覧",
                    countLabel = "表示中: ${uiState.rows.size} 件",
                    loading = uiState.isLoading,
                    dark = false,
                    header = { MaterialInventoryTableHeader() },
                ) {
                    MaterialInventoryTableBody(rows = uiState.rows)
                }
            }
        }
    }
}

@Composable
private fun MaterialInventoryFilterPanel(
    startDate: String,
    endDate: String,
    keyword: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
) {
    InventoryFilterPanel(onReset = onReset) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InventoryListDateFilter(
                startDate = startDate,
                endDate = endDate,
                onStartChange = onStartChange,
                onEndChange = onEndChange,
                onPrev = onPrev,
                onToday = onToday,
                onNext = onNext,
                dark = false,
            )

            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                placeholder = { Text("材料名 / 仕入先 / コード で検索", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                ),
            )
        }
    }
}

@Composable
private fun MaterialInventoryTableHeader() {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .background(Color.Black.copy(alpha = 0.03f))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InventoryTableHeaderCell("材料CD", 80)
        InventoryTableHeaderCell("材料名", 140)
        InventoryTableHeaderCell("現在在庫", 72)
        InventoryTableHeaderCell("安全在庫", 72)
        InventoryTableHeaderCell("使用数", 64)
        InventoryTableHeaderCell("注文数", 64)
        InventoryTableHeaderCell("仕入先", 100)
    }
}

@Composable
private fun MaterialInventoryTableBody(rows: List<MaterialStockItemDto>) {
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Text("データが見つかりません", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        return
    }
    val scroll = rememberScrollState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        itemsIndexed(rows) { index, row ->
            val bg = if (index % 2 == 0) Color.Transparent else Color.Black.copy(alpha = 0.015f)
            val isLowStock = (row.currentStock ?: 0) < (row.safetyStock ?: 0)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .background(bg)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InventoryTableCell(row.materialCd.orEmpty(), 80)
                InventoryTableCell(row.materialName.orEmpty(), 140, textAlign = TextAlign.Start, bold = true)
                InventoryTableCell(
                    text = formatInventoryNum(row.currentStock),
                    width = 72,
                    bold = true,
                    color = if (isLowStock) Color(0xFFEF4444) else null
                )
                InventoryTableCell(formatInventoryNum(row.safetyStock), 72)
                InventoryTableCell(formatInventoryNum(row.plannedUsage), 64)
                InventoryTableCell(formatInventoryNum(row.orderQuantity), 64)
                InventoryTableCell(row.supplierName.orEmpty(), 100, textAlign = TextAlign.Start, fontSize = 11)
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color(0xFFF1F5F9))
        }
    }
}

