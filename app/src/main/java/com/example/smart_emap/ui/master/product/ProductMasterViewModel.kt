package com.example.smart_emap.ui.master.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductMasterStatsDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val productLocationOptions: List<Pair<String, String>> = listOf(
    "製品倉庫" to "製品倉庫",
    "外注倉庫" to "外注倉庫",
    "仮設倉庫" to "仮設倉庫",
    "部品倉庫" to "部品倉庫",
    "材料置場" to "材料置場",
    "仕上倉庫" to "仕上倉庫",
    "工程中間在庫" to "工程中間在庫",
    "メッキ倉庫" to "メッキ倉庫",
)

fun productDefaultFormValues(): Map<String, String> = mapOf(
    "product_cd" to "",
    "product_name" to "",
    "part_number" to "",
    "product_alias" to "",
    "product_type" to "",
    "category" to "",
    "kind" to "",
    "priority" to "2",
    "status" to "active",
    "unit_price" to "0",
    "process_count" to "1",
    "is_multistage" to "true",
    "lead_time" to "0",
    "safety_days" to "0",
    "lot_size" to "1",
    "route_cd" to "",
    "box_type" to "",
    "unit_per_box" to "0",
    "dimensions" to "",
    "weight" to "0",
    "destination_cd" to "",
    "vehicle_model" to "",
    "location_cd" to "",
    "start_use_date" to "",
    "material_cd" to "",
    "cut_length" to "0",
    "chamfer_length" to "0",
    "developed_length" to "0",
    "scrap_length" to "0",
    "take_count" to "0",
    "note" to "",
)

fun MasterProductDto.toFormValues(): Map<String, String> = mapOf(
    "product_cd" to productCd.orEmpty(),
    "product_name" to productName.orEmpty(),
    "part_number" to partNumber.orEmpty(),
    "product_alias" to productAlias.orEmpty(),
    "product_type" to productType.orEmpty(),
    "category" to category.orEmpty(),
    "kind" to kind.orEmpty(),
    "priority" to (priority ?: 2).toString(),
    "status" to status.orEmpty().ifBlank { "active" },
    "unit_price" to (unitPrice ?: 0.0).toString(),
    "process_count" to (processCount ?: 1).toString(),
    "is_multistage" to (isMultistage != false).toString(),
    "lead_time" to (leadTime ?: 0).toString(),
    "safety_days" to (safetyDays ?: 0).toString(),
    "lot_size" to (lotSize ?: 1).toString(),
    "route_cd" to routeCd.orEmpty(),
    "box_type" to boxType.orEmpty(),
    "unit_per_box" to (unitPerBox ?: 0).toString(),
    "dimensions" to dimensions.orEmpty(),
    "weight" to (weight ?: 0.0).toString(),
    "destination_cd" to destinationCd.orEmpty(),
    "vehicle_model" to vehicleModel.orEmpty(),
    "location_cd" to locationCd.orEmpty(),
    "start_use_date" to startUseDate?.take(10).orEmpty(),
    "material_cd" to materialCd.orEmpty(),
    "cut_length" to (cutLength ?: 0.0).toString(),
    "chamfer_length" to (chamferLength ?: 0.0).toString(),
    "developed_length" to (developedLength ?: 0.0).toString(),
    "scrap_length" to (scrapLength ?: 0.0).toString(),
    "take_count" to (takeCount ?: 0).toString(),
    "note" to note.orEmpty(),
)

data class ProductMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val category: String = "",
    val kind: String = "",
    val materialCd: String = "",
    val products: List<MasterProductDto> = emptyList(),
    val totalCount: Int = 0,
    val page: Int = 1,
    val stats: ProductMasterStatsDto = ProductMasterStatsDto(),
    val materialOptions: List<Pair<String, String>> = emptyList(),
    val formDestinationOptions: List<Pair<String, String>> = emptyList(),
    val formRouteOptions: List<Pair<String, String>> = emptyList(),
    val snackbarMessage: String? = null,
    val showForm: Boolean = false,
    val editingProduct: MasterProductDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val visibleColumns: Map<String, Boolean> = defaultProductVisibleColumns(),
    val showColumnSettings: Boolean = false,
    val columnSettingsDraft: Map<String, Boolean> = defaultProductVisibleColumns(),
    val showProductTypeSelector: Boolean = false,
    val selectedProductTypes: Set<String> = emptySet(),
    val showScrapConfirm: Boolean = false,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
)

class ProductMasterViewModel(
    private val repository: MasterRepository,
    private val columnSettingsStore: ProductColumnSettingsStore,
) : ViewModel() {
    companion object {
        val PAGE_SIZE: Int get() = PRODUCT_MASTER_PAGE_SIZE
    }

    private val _uiState = MutableStateFlow(ProductMasterUiState())
    val uiState: StateFlow<ProductMasterUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        loadColumnSettings()
        refreshAll()
    }

    private fun loadColumnSettings() {
        viewModelScope.launch {
            runCatching {
                val saved = columnSettingsStore.load()
                _uiState.update { it.copy(visibleColumns = saved, columnSettingsDraft = saved) }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val filters = MasterRepository.ProductMasterFilters(
            keyword = state.keyword,
            category = state.category,
            kind = state.kind,
            materialCd = state.materialCd,
            page = state.page,
            pageSize = PAGE_SIZE,
        )
        val materials = if (state.materialOptions.isEmpty()) repository.loadMaterialOptionsForProduct() else state.materialOptions
        val stats = repository.loadProductMasterStats()
        var products: List<MasterProductDto>
        var total: Int
        val firstLoad = repository.loadProductMasterList(filters)
        products = firstLoad.first
        total = firstLoad.second
        val maxPage = maxOf(1, (total + PAGE_SIZE - 1) / PAGE_SIZE)
        val adjustedPage = state.page.coerceIn(1, maxPage)
        if (adjustedPage != state.page) {
            val reload = repository.loadProductMasterList(filters.copy(page = adjustedPage))
            products = reload.first
            total = reload.second
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                products = products,
                totalCount = total,
                page = adjustedPage,
                stats = stats,
                materialOptions = materials,
            )
        }
    }

    private fun debounceLoad(resetPage: Boolean = true) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            if (resetPage) _uiState.update { it.copy(page = 1) }
            _uiState.update { it.copy(isLoading = true) }
            delay(400)
            loadInternal()
        }
    }

    fun setKeyword(v: String) { _uiState.update { it.copy(keyword = v) }; debounceLoad() }
    fun setCategory(v: String) { _uiState.update { it.copy(category = v, page = 1) }; refreshAll() }
    fun setKind(v: String) { _uiState.update { it.copy(kind = v, page = 1) }; refreshAll() }
    fun setMaterialCd(v: String) { _uiState.update { it.copy(materialCd = v, page = 1) }; refreshAll() }

    fun resetFilters() {
        _uiState.update { it.copy(keyword = "", category = "", kind = "", materialCd = "", page = 1) }
        refreshAll()
    }

    fun setPage(page: Int) {
        val maxPage = maxOf(1, (_uiState.value.totalCount + PAGE_SIZE - 1) / PAGE_SIZE)
        _uiState.update { it.copy(page = page.coerceIn(1, maxPage)) }
        refreshAll()
    }

    private fun loadFormOptions(forCreate: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val destinations = if (state.formDestinationOptions.isEmpty()) {
                repository.loadDestinationOptions().map { it.cd to it.name }.filter { it.first.isNotBlank() }
            } else state.formDestinationOptions
            val routes = if (state.formRouteOptions.isEmpty()) {
                repository.loadRouteOptionsForProduct()
            } else state.formRouteOptions
            val nextProductCd = if (forCreate) {
                repository.loadNextProductCd(state.products)
            } else null
            _uiState.update { current ->
                var updated = current.copy(formDestinationOptions = destinations, formRouteOptions = routes)
                if (forCreate && current.showForm && current.editingProduct == null && nextProductCd != null) {
                    updated = updated.copy(formValues = updated.formValues + ("product_cd" to nextProductCd))
                }
                updated
            }
        }
    }

    fun openCreate() {
        val initialCd = repository.nextProductCdFromList(_uiState.value.products)
        _uiState.update {
            it.copy(
                showForm = true,
                editingProduct = null,
                formValues = productDefaultFormValues() + ("product_cd" to initialCd),
            )
        }
        loadFormOptions(forCreate = true)
    }

    fun openEdit(product: MasterProductDto) {
        _uiState.update {
            it.copy(
                showForm = true,
                editingProduct = product,
                formValues = product.toFormValues(),
            )
        }
        loadFormOptions()
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingProduct = null) }

    fun saveForm() {
        val state = _uiState.value
        if (state.formValues["product_cd"].isNullOrBlank() || state.formValues["product_name"].isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品CDと製品名称は必須です") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveProduct(state.editingProduct?.id, state.formValues)
                if (ok) {
                    _uiState.update {
                        it.copy(actionLoading = false, showForm = false, editingProduct = null, snackbarMessage = "保存しました")
                    }
                    refreshAll()
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存失敗") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    fun deleteProduct(product: MasterProductDto) {
        val id = product.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.deleteProduct(id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun recalculateScrapLength() {
        openScrapCalcConfirm()
    }

    fun openScrapCalcConfirm() {
        _uiState.update { it.copy(showScrapConfirm = true) }
    }

    fun dismissScrapCalcConfirm() {
        _uiState.update { it.copy(showScrapConfirm = false) }
    }

    fun confirmScrapCalc() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val result = repository.recalculateProductScrapLength()
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showScrapConfirm = false,
                        snackbarMessage = "端材長を更新しました（更新: ${result.updated} 件 / スキップ: ${result.skipped} 件 / 全 ${result.total} 件）",
                    )
                }
                refreshAll()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showScrapConfirm = false,
                        snackbarMessage = e.message ?: "端材長の一括計算に失敗しました",
                    )
                }
            }
        }
    }

    fun openQrPrint() {
        if (_uiState.value.stats.total <= 0) {
            _uiState.update { it.copy(snackbarMessage = "印刷する製品がありません") }
            return
        }
        _uiState.update {
            it.copy(
                showProductTypeSelector = true,
                selectedProductTypes = emptySet(),
            )
        }
    }

    fun dismissProductTypeSelector() {
        _uiState.update { it.copy(showProductTypeSelector = false) }
    }

    fun toggleProductType(type: String, selected: Boolean) {
        _uiState.update { state ->
            val next = state.selectedProductTypes.toMutableSet()
            if (selected) next.add(type) else next.remove(type)
            state.copy(selectedProductTypes = next)
        }
    }

    fun confirmQrPrint() {
        val state = _uiState.value
        if (state.selectedProductTypes.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "製品種別を選択してください") }
            return
        }
        _uiState.update { it.copy(showProductTypeSelector = false, actionLoading = true) }
        viewModelScope.launch {
            runCatching {
                val allProducts = repository.loadAllProducts()
                if (allProducts.isEmpty()) {
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = "印刷する製品がありません")
                    }
                    return@runCatching
                }
                val qrItems = buildProductQrPrintItems(allProducts, state.selectedProductTypes)
                if (qrItems.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "選択した製品種別に該当する製品がありません",
                        )
                    }
                    return@runCatching
                }
                val html = buildProductQrPrintHtml(qrItems)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = "製品QRコード印刷",
                        pendingPrintLayout = PrintPageLayout.A3_LANDSCAPE_SINGLE,
                        snackbarMessage = "${qrItems.size}件のQRコードを生成しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "QRコードの生成に失敗しました",
                    )
                }
            }
        }
    }

    fun printCuttingLength() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val state = _uiState.value
            val filters = MasterRepository.ProductMasterFilters(
                keyword = state.keyword,
                category = state.category,
                kind = state.kind,
                materialCd = state.materialCd,
            )
            runCatching {
                val products = repository.loadAllProducts(filters)
                if (products.isEmpty()) {
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = "印刷するデータがありません")
                    }
                    return@runCatching
                }
                val materialNameMap = state.materialOptions.associate { (cd, name) -> cd to name }
                val html = buildProductCuttingLengthPrintHtml(products, materialNameMap)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = "切断長印刷",
                        pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
                        snackbarMessage = "印刷プレビューを開きます",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "切断長印刷に失敗しました",
                    )
                }
            }
        }
    }

    fun clearPendingPrintHtml() {
        _uiState.update {
            it.copy(
                pendingPrintHtml = null,
                pendingPrintSubject = null,
                pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
            )
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val state = _uiState.value
            val filters = MasterRepository.ProductMasterFilters(
                keyword = state.keyword,
                category = state.category,
                kind = state.kind,
                materialCd = state.materialCd,
            )
            runCatching {
                val result = repository.exportProductMasterCsv(filters)
                if (result.success == true) {
                    val count = result.rowCount ?: 0
                    val fileName = result.fileName ?: "ProductMaster.csv"
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "${count}件を${fileName}として共有フォルダに保存しました",
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = result.message ?: "CSVファイルの保存に失敗しました",
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "CSVファイルの出力に失敗しました",
                    )
                }
            }
        }
    }

    fun openColumnSettings() {
        _uiState.update {
            it.copy(
                showColumnSettings = true,
                columnSettingsDraft = it.visibleColumns,
            )
        }
    }

    fun closeColumnSettings() {
        _uiState.update { it.copy(showColumnSettings = false) }
    }

    fun toggleColumnSettingsDraft(key: String, visible: Boolean) {
        _uiState.update {
            it.copy(columnSettingsDraft = it.columnSettingsDraft + (key to visible))
        }
    }

    fun selectAllColumnSettingsDraft(select: Boolean) {
        _uiState.update { state ->
            state.copy(
                columnSettingsDraft = productOptionalColumnDefinitions.associate { it.key to select },
            )
        }
    }

    fun saveColumnSettings() {
        val draft = _uiState.value.columnSettingsDraft
        viewModelScope.launch {
            runCatching {
                columnSettingsStore.save(draft)
                _uiState.update {
                    it.copy(
                        visibleColumns = draft,
                        showColumnSettings = false,
                        snackbarMessage = "列表示設定を保存しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "列設定の保存に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: MasterRepository,
        private val columnSettingsStore: ProductColumnSettingsStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductMasterViewModel(repository, columnSettingsStore) as T
    }
}
