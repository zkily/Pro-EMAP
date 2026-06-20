package com.example.smart_emap.ui.erp.shipping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.ShippingItemDto
import com.example.smart_emap.data.model.ShippingOverviewRowDto
import com.example.smart_emap.data.repository.ShippingRepository
import com.example.smart_emap.data.repository.WeldingShippingMatrix
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── List ─────────────────────────────────────────────────────────────────────

data class ShippingListUiState(
    val isLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val keyword: String = "",
    val items: List<ShippingItemDto> = emptyList(),
    val snackbarMessage: String? = null,
)

class ShippingListViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingListUiState())
    val uiState: StateFlow<ShippingListUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun setKeyword(v: String) = _uiState.update { it.copy(keyword = v) }
    fun shiftDay(delta: Long) {
        val s = _uiState.value
        val start = repository.shiftDate(s.startDate.ifBlank { repository.todayStr() }, delta)
        val end = repository.shiftDate(s.endDate.ifBlank { repository.todayStr() }, delta)
        _uiState.update { it.copy(startDate = start, endDate = end) }
        search()
    }

    fun goToday() {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val items = repository.loadShippingItems(
                startDate = s.startDate,
                endDate = s.endDate,
                productName = s.keyword.takeIf { it.isNotBlank() },
            )
            _uiState.update { it.copy(isLoading = false, items = items) }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShippingListViewModel(repository) as T
    }
}

@Composable
fun ShippingListScreen(viewModel: ShippingListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("出荷構成表管理", "作成・編集・一覧", Icons.AutoMirrored.Filled.List, uiState.isLoading, viewModel::search, dark = false)
                ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, { viewModel.shiftDay(-1) }, viewModel::goToday, { viewModel.shiftDay(1) }, viewModel::search, uiState.isLoading) {
                    OutlinedTextField(uiState.keyword, viewModel::setKeyword, label = { Text("製品", fontSize = 10.sp) }, modifier = Modifier.width(120.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 11.sp))
                }
                ShippingStatCardsRow(listOf("件数" to uiState.items.size.toString(), "発行済" to uiState.items.count { it.status?.contains("発行") == true }.toString()), listOf(Color(0xFF409EFF), Color(0xFF67C23A)), dark = false)
                ShippingDataTable(
                    columns = listOf("出荷日", "出荷No", "納入先", "製品", "箱数", "状態"),
                    rows = uiState.items.map { listOf(it.shippingDate.orEmpty(), it.shippingNo.orEmpty(), it.destinationName.orEmpty(), it.productName.orEmpty(), (it.confirmedBoxes ?: 0).toString(), it.status.orEmpty()) },
                )
            }
        }
    }
}

// ── Document (report / overview / confirm) ─────────────────────────────────

data class ShippingDocumentUiState(
    val mode: ShippingDocumentMode = ShippingDocumentMode.REPORT,
    val isLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val rows: List<ShippingOverviewRowDto> = emptyList(),
)

class ShippingDocumentViewModel(
    private val repository: ShippingRepository,
    private val mode: ShippingDocumentMode,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingDocumentUiState(mode = mode))
    val uiState: StateFlow<ShippingDocumentUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun shiftDay(delta: Long) {
        val s = _uiState.value
        _uiState.update { it.copy(startDate = repository.shiftDate(s.startDate, delta), endDate = repository.shiftDate(s.endDate, delta)) }
        search()
    }

    fun goToday() {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val rows = repository.loadOverview(s.startDate, s.endDate)
            _uiState.update { it.copy(isLoading = false, rows = rows) }
        }
    }

    class Factory(private val repository: ShippingRepository, private val mode: ShippingDocumentMode) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShippingDocumentViewModel(repository, mode) as T
    }
}

@Composable
fun ShippingDocumentScreen(viewModel: ShippingDocumentViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()
    val icon = when (uiState.mode) {
        ShippingDocumentMode.REPORT -> Icons.Default.Description
        ShippingDocumentMode.OVERVIEW -> Icons.Default.CalendarMonth
        ShippingDocumentMode.CONFIRM -> Icons.Default.CheckCircle
    }
    val totalQty = uiState.rows.sumOf { it.quantity ?: 0 }
    val destCount = uiState.rows.map { it.destinationName.orEmpty() }.distinct().size

    Scaffold(containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader(uiState.mode.title, uiState.mode.subtitle, icon, uiState.isLoading, viewModel::search, dark = false)
                ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, { viewModel.shiftDay(-1) }, viewModel::goToday, { viewModel.shiftDay(1) }, viewModel::search, uiState.isLoading)
                ShippingStatCardsRow(listOf("件数" to uiState.rows.size.toString(), "納入先" to destCount.toString(), "箱数合計" to totalQty.toString()), listOf(Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B)), dark = false)
                ShippingDataTable(
                    columns = listOf("出荷日", "納入先", "出荷No", "製品", "箱数"),
                    rows = uiState.rows.map { listOf(it.shippingDate.orEmpty(), it.destinationName.orEmpty(), it.shippingNo.orEmpty(), it.productName.orEmpty(), (it.quantity ?: 0).toString()) },
                )
            }
        }
    }
}

// ── Picking ──────────────────────────────────────────────────────────────────

data class ShippingPickingUiState(
    val isLoading: Boolean = false,
    val tab: ShippingPickingTab = ShippingPickingTab.PROGRESS,
    val startDate: String = "",
    val endDate: String = "",
    val totalToday: Int = 0,
    val pendingToday: Int = 0,
    val completedToday: Int = 0,
    val completionRate: Float = 0f,
    val palletRows: List<List<String>> = emptyList(),
    val historyRows: List<List<String>> = emptyList(),
    val snackbarMessage: String? = null,
)

class ShippingPickingViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingPickingUiState())
    val uiState: StateFlow<ShippingPickingUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        refresh()
    }

    fun selectTab(tab: ShippingPickingTab) = _uiState.update { it.copy(tab = tab) }
    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val progress = repository.loadPickingProgress()
            val ov = progress?.todayOverview
            val pallets = progress?.palletList.orEmpty().map {
                listOf(it.shippingDate.orEmpty(), it.shippingNoP.orEmpty(), it.destinationName.orEmpty(), it.productName.orEmpty(), if ((it.pickingLogMatched ?: 0) == 1) "完了" else "未")
            }
            val s = _uiState.value
            val history = repository.loadPickingHistoryItems(s.startDate, s.endDate).map {
                listOf(it.shippingDate.orEmpty(), it.destinationName.orEmpty(), it.shippingNoP.orEmpty(), it.productName.orEmpty(), if ((it.pickingLogMatched ?: 0) == 1) "完了" else "未")
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalToday = ov?.totalToday ?: 0,
                    pendingToday = ov?.pendingToday ?: 0,
                    completedToday = ov?.completedToday ?: 0,
                    completionRate = (ov?.todayCompletionRate ?: 0.0).toFloat(),
                    palletRows = pallets,
                    historyRows = history,
                )
            }
        }
    }

    fun syncPicking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.refreshPickingAsync()
                .onSuccess { _uiState.update { s -> s.copy(isLoading = false, snackbarMessage = "同期を開始しました") } }
                .onFailure { e -> _uiState.update { s -> s.copy(isLoading = false, snackbarMessage = e.message) } }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShippingPickingViewModel(repository) as T
    }
}

@Composable
fun ShippingPickingScreen(viewModel: ShippingPickingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val tabs = ShippingPickingTab.entries.map { it.label }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("ピッキング管理", "進捗 · リスト · 履歴", Icons.Default.Inventory2, uiState.isLoading, viewModel::refresh, trailing = {
                    TextButton(onClick = viewModel::syncPicking, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("同期", fontSize = 11.sp) }
                }, dark = false)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ShippingStatCard("本日", uiState.totalToday.toString(), Color(0xFF667EEA), false)
                    ShippingStatCard("未", uiState.pendingToday.toString(), Color(0xFFE6A23C), false)
                    ShippingStatCard("完了", uiState.completedToday.toString(), Color(0xFF67C23A), false)
                    ShippingProgressRing(uiState.completionRate, "完了率")
                }
                ShippingTabRow(tabs, uiState.tab.ordinal, { viewModel.selectTab(ShippingPickingTab.entries[it]) })
                when (uiState.tab) {
                    ShippingPickingTab.PROGRESS -> ShippingDataTable(listOf("出荷日", "パレット", "納入先", "製品", "状態"), uiState.palletRows)
                    ShippingPickingTab.GENERATOR -> {
                        ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, {}, {}, {}, viewModel::refresh, uiState.isLoading)
                        ShippingDataTable(listOf("出荷日", "パレット", "納入先", "製品", "状態"), uiState.palletRows)
                    }
                    ShippingPickingTab.HISTORY -> {
                        ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, {}, {}, {}, viewModel::refresh, uiState.isLoading)
                        ShippingDataTable(listOf("出荷日", "納入先", "パレット", "製品", "状態"), uiState.historyRows)
                    }
                }
            }
        }
    }
}

// ── Welding ──────────────────────────────────────────────────────────────────

data class WeldingShippingUiState(
    val isLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val products: List<String> = emptyList(),
    val selectedProducts: Set<String> = emptySet(),
    val tableRows: List<List<String>> = emptyList(),
    val dateColumns: List<String> = emptyList(),
    val pendingPrintHtml: String? = null,
    val snackbarMessage: String? = null,
)

class WeldingShippingViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WeldingShippingUiState())
    val uiState: StateFlow<WeldingShippingUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = repository.shiftDate(today, 6)) }
        loadProducts()
    }

    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun toggleProduct(cd: String) = _uiState.update { s ->
        val next = s.selectedProducts.toMutableSet()
        if (cd in next) next.remove(cd) else next.add(cd)
        s.copy(selectedProducts = next)
    }

    fun loadProducts() {
        viewModelScope.launch {
            val products = repository.loadWeldingProductLabels()
            _uiState.update { it.copy(products = products) }
        }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val cds = s.selectedProducts.map { it.substringBefore(" ·").trim().substringBefore(" -").trim() }.filter { it.isNotBlank() }
            val matrix = repository.loadWeldingMatrix(s.startDate, s.endDate, cds) ?: WeldingShippingMatrix()
            val cols = matrix.dateColumns
            val rows = matrix.rows.map { row ->
                listOf(row.destinationName) + cols.map { d -> (row.cells[d] ?: 0).toString() } + listOf(row.total.toString())
            }
            _uiState.update { it.copy(isLoading = false, dateColumns = cols, tableRows = rows) }
        }
    }

    fun printReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val cds = s.selectedProducts.map { it.substringBefore(" ·").trim().substringBefore(" -").trim() }.filter { it.isNotBlank() }
            val html = repository.exportWeldingHtml(s.startDate, s.endDate, cds)
            _uiState.update { it.copy(isLoading = false, pendingPrintHtml = html, snackbarMessage = if (html == null) "印刷データ取得に失敗" else null) }
        }
    }

    fun clearPendingPrint() = _uiState.update { it.copy(pendingPrintHtml = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = WeldingShippingViewModel(repository) as T
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeldingShippingScreen(viewModel: WeldingShippingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.pendingPrintHtml) {
        uiState.pendingPrintHtml?.let { html ->
            HtmlPrintHelper.printHtml(context, html, "溶接出荷", PrintPageLayout.A4_LANDSCAPE_SINGLE)
            viewModel.clearPendingPrint()
        }
    }

    Scaffold(containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("溶接出荷管理", "納入先 × 日付マトリクス", Icons.Default.PrecisionManufacturing, uiState.isLoading, viewModel::search, trailing = {
                    TextButton(onClick = viewModel::printReport) { Text("印刷", fontSize = 11.sp) }
                }, dark = false)
                ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, {}, {}, {}, viewModel::search, uiState.isLoading)
                ShippingSectionTitle("溶接製品", "${uiState.selectedProducts.size} 選択")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.products.take(12).forEach { p ->
                        val selected = p in uiState.selectedProducts
                        FilterChip(selected, { viewModel.toggleProduct(p) }, label = { Text(p.take(20), fontSize = 9.sp) })
                    }
                }
                ShippingDataTable(listOf("納入先") + uiState.dateColumns + listOf("合計"), uiState.tableRows)
            }
        }
    }
}

// ── Inventory shortage ───────────────────────────────────────────────────────

data class InventoryShortageUiState(
    val isLoading: Boolean = false,
    val date: String = "",
    val keyword: String = "",
    val negativeOnly: Boolean = false,
    val rows: List<List<String>> = emptyList(),
    val totalCurrent: Int = 0,
    val negativeSum: Int = 0,
)

class InventoryShortageViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryShortageUiState())
    val uiState: StateFlow<InventoryShortageUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(date = repository.todayStr()) }
        search()
    }

    fun setDate(v: String) = _uiState.update { it.copy(date = v) }
    fun setKeyword(v: String) = _uiState.update { it.copy(keyword = v) }
    fun setNegativeOnly(v: Boolean) = _uiState.update { it.copy(negativeOnly = v) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            var rows = repository.loadProductionSummaryForDate(s.date)
            if (s.keyword.isNotBlank()) rows = rows.filter { (it.productName ?: "").contains(s.keyword, true) || (it.productCd ?: "").contains(s.keyword, true) }
            if (s.negativeOnly) rows = rows.filter { (it.warehouseInventory ?: 0) < 0 }
            val table = rows.map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), (it.warehouseInventory ?: 0).toString(), (it.safetyStock ?: 0).toString(), (it.forecastQuantity ?: 0).toString()) }
            val total = rows.sumOf { (it.warehouseInventory ?: 0) + (it.outsourcedWarehouseInventory ?: 0) }
            val neg = rows.sumOf { val v = it.warehouseInventory ?: 0; if (v < 0) v else 0 }
            _uiState.update { it.copy(isLoading = false, rows = table, totalCurrent = total, negativeSum = neg) }
        }
    }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InventoryShortageViewModel(repository) as T
    }
}

@Composable
fun InventoryShortageScreen(viewModel: InventoryShortageViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()
    Scaffold(containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("倉庫在庫管理", "在庫不足 · 倉庫在庫", Icons.Default.Warehouse, uiState.isLoading, viewModel::search, dark = false)
                ShippingFilterBar(uiState.date, uiState.date, viewModel::setDate, viewModel::setDate, {}, {}, {}, viewModel::search, uiState.isLoading) {
                    OutlinedTextField(uiState.keyword, viewModel::setKeyword, label = { Text("製品", fontSize = 10.sp) }, modifier = Modifier.width(100.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 11.sp))
                    FilterChip(uiState.negativeOnly, { viewModel.setNegativeOnly(!uiState.negativeOnly) }, label = { Text("マイナスのみ", fontSize = 9.sp) })
                }
                ShippingStatCardsRow(listOf("現在在庫" to formatShippingNum(uiState.totalCurrent), "不足合計" to formatShippingNum(uiState.negativeSum), "件数" to uiState.rows.size.toString()), listOf(Color(0xFF409EFF), Color(0xFFF56C6C), Color(0xFF9254DE)), dark = false)
                ShippingDataTable(listOf("製品CD", "製品名", "倉庫在庫", "安全在庫", "内示"), uiState.rows)
            }
        }
    }
}

// ── KPI ──────────────────────────────────────────────────────────────────────

data class InventoryKpiUiState(
    val isLoading: Boolean = false,
    val tab: ShippingKpiTab = ShippingKpiTab.TURNOVER,
    val startDate: String = "",
    val endDate: String = "",
    val asOf: String = "",
    val rows: List<List<String>> = emptyList(),
)

class InventoryKpiViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryKpiUiState())
    val uiState: StateFlow<InventoryKpiUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = repository.shiftDate(today, -30), endDate = today, asOf = today) }
        search()
    }

    fun selectTab(tab: ShippingKpiTab) = _uiState.update { it.copy(tab = tab) }
    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun setAsOf(v: String) = _uiState.update { it.copy(asOf = v) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val data = when (s.tab) {
                ShippingKpiTab.TURNOVER -> repository.loadKpiTurnover(s.startDate, s.endDate).map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), String.format("%.2f", it.turnoverRate ?: 0.0), String.format("%.1f", it.turnoverDays ?: 0.0)) }
                ShippingKpiTab.AVG_DAYS -> repository.loadKpiAvgDays(s.startDate, s.endDate).map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), String.format("%.1f", it.avgInventoryDays ?: 0.0), (it.closingInventory ?: it.warehouseInventory ?: 0).toString()) }
                ShippingKpiTab.SHORTAGE -> repository.loadKpiShortage(s.asOf).map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), (it.warehouseInventory ?: 0).toString(), (it.safetyStock ?: 0).toString()) }
                ShippingKpiTab.OVERSTOCK -> repository.loadKpiOverstock(s.asOf).map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), (it.overstockQty ?: 0).toString(), String.format("%.1f", it.turnoverDays ?: 0.0)) }
                ShippingKpiTab.REORDER -> repository.loadKpiReorder(s.asOf).map { listOf(it.productCd.orEmpty(), it.productName.orEmpty(), (it.warehouseInventory ?: 0).toString(), (it.reorderPoint ?: 0).toString()) }
            }
            _uiState.update { it.copy(isLoading = false, rows = data) }
        }
    }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InventoryKpiViewModel(repository) as T
    }
}

@Composable
fun InventoryKpiScreen(viewModel: InventoryKpiViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()
    val columns = when (uiState.tab) {
        ShippingKpiTab.TURNOVER -> listOf("製品CD", "製品名", "回転率", "回転日数")
        ShippingKpiTab.AVG_DAYS -> listOf("製品CD", "製品名", "平均在庫日数", "在庫")
        ShippingKpiTab.SHORTAGE -> listOf("製品CD", "製品名", "在庫", "安全在庫")
        ShippingKpiTab.OVERSTOCK -> listOf("製品CD", "製品名", "過剰数", "回転日数")
        ShippingKpiTab.REORDER -> listOf("製品CD", "製品名", "在庫", "発注点")
    }

    Scaffold(containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("在庫KPI・アラート", "回転率 · 欠品 · 発注点", Icons.Default.Analytics, uiState.isLoading, viewModel::search, dark = false)
                ShippingTabRow(ShippingKpiTab.entries.map { it.label }, uiState.tab.ordinal) { viewModel.selectTab(ShippingKpiTab.entries[it]); viewModel.search() }
                if (uiState.tab == ShippingKpiTab.TURNOVER || uiState.tab == ShippingKpiTab.AVG_DAYS) {
                    ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, {}, {}, {}, viewModel::search, uiState.isLoading)
                } else {
                    ShippingFilterBar(uiState.asOf, uiState.asOf, viewModel::setAsOf, viewModel::setAsOf, {}, {}, {}, viewModel::search, uiState.isLoading)
                }
                ShippingDataTable(columns, uiState.rows)
            }
        }
    }
}

// ── Warehouse daily ──────────────────────────────────────────────────────────

data class WarehouseDailyUiState(
    val isLoading: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val rows: List<List<String>> = emptyList(),
    val snackbarMessage: String? = null,
)

class WarehouseDailyViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WarehouseDailyUiState())
    val uiState: StateFlow<WarehouseDailyUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(dateFrom = today, dateTo = today) }
    }

    fun setDateFrom(v: String) = _uiState.update { it.copy(dateFrom = v) }
    fun setDateTo(v: String) = _uiState.update { it.copy(dateTo = v) }

    fun search() {
        viewModelScope.launch {
            val s = _uiState.value
            val dateFrom = s.dateFrom.ifBlank { repository.todayStr() }
            val dateTo = s.dateTo.ifBlank { dateFrom }
            _uiState.update { it.copy(isLoading = true, dateFrom = dateFrom, dateTo = dateTo) }
            val rows = repository.loadWarehouseDailyRows(dateFrom, dateTo).map {
                listOf(
                    it.productCd.orEmpty(),
                    it.productName.orEmpty(),
                    formatWarehouseQty(it.orderQty),
                    formatWarehouseQty(it.forecastQty),
                    formatWarehouseQty(it.warehouseStock),
                    formatWarehouseQty(it.warehouseActual),
                )
            }
            _uiState.update { it.copy(isLoading = false, rows = rows) }
        }
    }

    fun syncFromOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.syncWarehouseDaily().onSuccess { search(); _uiState.update { it.copy(snackbarMessage = "受注同期完了") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message) } }
        }
    }

    fun generateData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.generateWarehouseDaily().onSuccess { search(); _uiState.update { it.copy(snackbarMessage = "データ生成完了") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message) } }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = WarehouseDailyViewModel(repository) as T
    }
}

@Composable
fun WarehouseDailyScreen(viewModel: WarehouseDailyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) { viewModel.search() }
    LaunchedEffect(uiState.snackbarMessage) { uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("倉庫日次在庫", "日次受注 · 在庫推移", Icons.Default.Assessment, uiState.isLoading, viewModel::search, trailing = {
                    Row {
                        TextButton(onClick = viewModel::syncFromOrder) { Text("受注同期", fontSize = 10.sp) }
                        TextButton(onClick = viewModel::generateData) { Text("生成", fontSize = 10.sp) }
                    }
                }, dark = false)
                ShippingFilterBar(uiState.dateFrom, uiState.dateTo, viewModel::setDateFrom, viewModel::setDateTo, {}, {}, {}, viewModel::search, uiState.isLoading)
                ShippingStatCardsRow(listOf("件数" to uiState.rows.size.toString()), listOf(Color(0xFF7C3AED)), dark = false)
                ShippingDataTable(listOf("製品CD", "製品名", "受注", "内示", "在庫", "実績"), uiState.rows)
            }
        }
    }
}

private fun formatWarehouseQty(value: Double?): String {
    val v = value ?: 0.0
    return if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}

// ── ABC ──────────────────────────────────────────────────────────────────────

data class AbcAnalysisUiState(
    val isLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val rows: List<List<String>> = emptyList(),
)

class AbcAnalysisViewModel(private val repository: ShippingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AbcAnalysisUiState())
    val uiState: StateFlow<AbcAnalysisUiState> = _uiState.asStateFlow()

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = repository.shiftDate(today, -90), endDate = today) }
        search()
    }

    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value
            val items = repository.loadShippingItems(startDate = s.startDate, endDate = s.endDate)
            val grouped = items.groupBy { "${it.productCd.orEmpty()}|${it.destinationName.orEmpty()}" }
                .map { (_, list) ->
                    val first = list.first()
                    val qty = list.sumOf { it.confirmedBoxes ?: 0 }
                    Triple(first.productCd.orEmpty(), first.productName.orEmpty() + " / " + first.destinationName.orEmpty(), qty)
                }
                .sortedByDescending { it.third }
            val total = grouped.sumOf { it.third }.coerceAtLeast(1)
            var cum = 0
            val rows = grouped.map { (cd, name, qty) ->
                cum += qty
                val pct = cum * 100.0 / total
                val rank = when { pct <= 80 -> "A"; pct <= 95 -> "B"; else -> "C" }
                listOf(cd, name, qty.toString(), rank, String.format("%.1f%%", pct))
            }
            _uiState.update { it.copy(isLoading = false, rows = rows) }
        }
    }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AbcAnalysisViewModel(repository) as T
    }
}

@Composable
fun AbcAnalysisScreen(viewModel: AbcAnalysisViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()
    Scaffold(containerColor = LayoutColors.ShellBg, contentWindowInsets = PurchaseShellWindowInsets) { padding ->
        ShippingLightPageBackground {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShippingGlassHeader("ABC分析", "出荷データによる重要度分析", Icons.Default.LocalShipping, uiState.isLoading, viewModel::search, dark = false)
                ShippingFilterBar(uiState.startDate, uiState.endDate, viewModel::setStartDate, viewModel::setEndDate, {}, {}, {}, viewModel::search, uiState.isLoading)
                ShippingDataTable(listOf("製品CD", "製品/納入先", "出荷数", "ランク", "累計%"), uiState.rows)
            }
        }
    }
}
