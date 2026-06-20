package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OutsourcingProcessProductDto
import com.example.smart_emap.data.repository.OutsourcingProcessProductFilters
import com.example.smart_emap.data.repository.OutsourcingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OutsourcingProcessTab(val key: String, val label: String) {
    All("all", "全て"),
    Cutting("cutting", "外注切断"),
    Forming("forming", "外注成型"),
    Plating("plating", "外注メッキ"),
    Welding("welding", "外注溶接"),
    Inspection("inspection", "外注検査"),
    Processing("processing", "外注加工"),
}

data class OutsourcingProcessProductFormUi(
    val id: Int? = null,
    val processType: String = "",
    val supplierCd: String = "",
    val supplierName: String = "",
    val productCd: String = "",
    val productName: String = "",
    val specification: String = "",
    val unitPrice: Double = 0.0,
    val deliveryLeadTime: Int = 3,
    val deliveryLocation: String = "仕上倉庫ヤード下",
    val category: String = "",
    val content: String = "",
    val remarks: String = "",
)

data class OutsourcingProcessProductsUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val tab: OutsourcingProcessTab = OutsourcingProcessTab.All,
    val keyword: String = "",
    val supplierCd: String = "",
    val isActiveFilter: String = "all",
    val page: Int = 1,
    val pageSize: Int = 50,
    val total: Int = 0,
    val items: List<OutsourcingProcessProductDto> = emptyList(),
    val statsTotal: Int = 0,
    val statsActive: Int = 0,
    val statsSuppliers: Int = 0,
    val processCounts: Map<String, Int> = emptyMap(),
    val supplierOptions: List<Pair<String, String>> = emptyList(),
    val productOptions: List<MasterProductItemDto> = emptyList(),
    val showFormDialog: Boolean = false,
    val isEdit: Boolean = false,
    val form: OutsourcingProcessProductFormUi = OutsourcingProcessProductFormUi(),
    val showDeleteConfirm: Boolean = false,
    val showToggleConfirm: Boolean = false,
    val pendingActionRow: OutsourcingProcessProductDto? = null,
    val snackbarMessage: String? = null,
)

class OutsourcingProcessProductsViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutsourcingProcessProductsUiState())
    val uiState: StateFlow<OutsourcingProcessProductsUiState> = _uiState.asStateFlow()
    private var keywordJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadActiveSupplierOptions()
                loadInternal()
                loadStats()
                _uiState.update { it.copy(supplierOptions = suppliers) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました") }
            }
        }
    }

    fun setTab(tab: OutsourcingProcessTab) {
        _uiState.update { it.copy(tab = tab, page = 1) }
        loadDataOnly()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value, page = 1) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(350)
            loadDataOnly()
        }
    }

    fun setSupplierCd(value: String) {
        _uiState.update { it.copy(supplierCd = value, page = 1) }
        loadDataOnly()
    }

    fun setIsActiveFilter(value: String) {
        _uiState.update { it.copy(isActiveFilter = value, page = 1) }
        loadDataOnly()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        loadDataOnly()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        loadDataOnly()
    }

    fun openCreateDialog() {
        val tab = _uiState.value.tab
        viewModelScope.launch {
            val products = repository.loadProductOptions()
            _uiState.update {
                it.copy(
                    showFormDialog = true,
                    isEdit = false,
                    productOptions = products,
                    form = OutsourcingProcessProductFormUi(
                        processType = if (tab == OutsourcingProcessTab.All) "" else tab.key,
                    ),
                )
            }
        }
    }

    fun openEditDialog(row: OutsourcingProcessProductDto) {
        viewModelScope.launch {
            val products = repository.loadProductOptions()
            _uiState.update {
                it.copy(
                    showFormDialog = true,
                    isEdit = true,
                    productOptions = products,
                    form = OutsourcingProcessProductFormUi(
                        id = row.id,
                        processType = row.processType.orEmpty(),
                        supplierCd = row.supplierCd.orEmpty(),
                        supplierName = row.supplierName.orEmpty(),
                        productCd = row.productCd.orEmpty(),
                        productName = row.productName.orEmpty(),
                        specification = row.specification.orEmpty(),
                        unitPrice = row.unitPrice ?: 0.0,
                        deliveryLeadTime = row.deliveryLeadTime ?: 3,
                        deliveryLocation = row.deliveryLocation.orEmpty().ifBlank { "仕上倉庫ヤード下" },
                        category = row.category.orEmpty(),
                        content = row.content.orEmpty(),
                        remarks = row.remarks.orEmpty(),
                    ),
                )
            }
        }
    }

    fun dismissFormDialog() = _uiState.update { it.copy(showFormDialog = false) }

    fun updateForm(transform: (OutsourcingProcessProductFormUi) -> OutsourcingProcessProductFormUi) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun onSupplierSelected(cd: String) {
        val name = _uiState.value.supplierOptions.find { it.first == cd }?.second.orEmpty()
        _uiState.update { it.copy(form = it.form.copy(supplierCd = cd, supplierName = name)) }
    }

    fun onProductSelected(cd: String) {
        val name = _uiState.value.productOptions.find { it.productCd == cd }?.productName.orEmpty()
        _uiState.update { it.copy(form = it.form.copy(productCd = cd, productName = name)) }
    }

    fun submitForm() {
        val state = _uiState.value
        val form = state.form
        if (form.processType.isBlank() || form.supplierCd.isBlank() || form.productCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val body = mapOf(
                "process_type" to form.processType,
                "supplier_cd" to form.supplierCd.trim(),
                "supplier_name" to form.supplierName.trim().ifBlank { null },
                "product_cd" to form.productCd.trim(),
                "product_name" to form.productName.trim().ifBlank { null },
                "specification" to form.specification.trim().ifBlank { null },
                "unit_price" to form.unitPrice,
                "delivery_lead_time" to form.deliveryLeadTime,
                "delivery_location" to form.deliveryLocation.trim().ifBlank { null },
                "category" to form.category.trim().ifBlank { null },
                "content" to form.content.trim().ifBlank { null },
                "remarks" to form.remarks.trim().ifBlank { null },
            )
            runCatching {
                if (state.isEdit && form.id != null) {
                    repository.updateProcessProduct(form.id, body)
                    _uiState.update { it.copy(actionLoading = false, showFormDialog = false, snackbarMessage = "更新しました") }
                } else {
                    repository.createProcessProduct(body)
                    _uiState.update { it.copy(actionLoading = false, showFormDialog = false, snackbarMessage = "登録しました") }
                }
                loadInternal()
                loadStats()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: if (state.isEdit) "更新に失敗しました" else "登録に失敗しました")
                }
            }
        }
    }

    fun requestToggle(row: OutsourcingProcessProductDto) {
        _uiState.update { it.copy(showToggleConfirm = true, pendingActionRow = row) }
    }

    fun dismissToggleConfirm() = _uiState.update { it.copy(showToggleConfirm = false, pendingActionRow = null) }

    fun confirmToggle() {
        val id = _uiState.value.pendingActionRow?.id ?: return
        val active = _uiState.value.pendingActionRow?.isActive == true
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showToggleConfirm = false) }
            runCatching {
                repository.toggleProcessProductStatus(id)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingActionRow = null,
                        snackbarMessage = if (active) "無効化しました" else "有効化しました",
                    )
                }
                loadInternal()
                loadStats()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "状態の更新に失敗しました") }
            }
        }
    }

    fun requestDelete(row: OutsourcingProcessProductDto) {
        _uiState.update { it.copy(showDeleteConfirm = true, pendingActionRow = row) }
    }

    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false, pendingActionRow = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingActionRow?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showDeleteConfirm = false) }
            runCatching {
                repository.deleteProcessProduct(id)
                _uiState.update { it.copy(actionLoading = false, pendingActionRow = null, snackbarMessage = "削除しました") }
                loadInternal()
                loadStats()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun processCount(tab: OutsourcingProcessTab): Int {
        val state = _uiState.value
        return if (tab == OutsourcingProcessTab.All) state.statsTotal else state.processCounts[tab.key] ?: 0
    }

    private fun loadDataOnly() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました") }
                }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val page = repository.loadProcessProductsPage(
            OutsourcingProcessProductFilters(
                processType = state.tab.key,
                keyword = state.keyword,
                supplierCd = state.supplierCd,
                isActive = state.isActiveFilter,
                page = state.page,
                pageSize = state.pageSize,
            ),
        )
        _uiState.update {
            it.copy(
                isLoading = false,
                items = page.items,
                total = page.total,
                page = page.page,
                pageSize = page.pageSize,
            )
        }
    }

    private suspend fun loadStats() {
        val stats = repository.loadProcessProductStats()
        val counts = stats?.byProcessType.orEmpty().associate { it.processType.orEmpty() to (it.totalCount ?: 0) }
        val total = stats?.total
        _uiState.update {
            it.copy(
                statsTotal = total?.totalCount ?: it.items.size,
                statsActive = total?.activeCount ?: it.items.count { row -> row.isActive == true },
                statsSuppliers = total?.supplierCount ?: it.items.map { row -> row.supplierCd }.distinct().size,
                processCounts = counts,
            )
        }
    }

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OutsourcingProcessProductsViewModel(repository) as T
    }
}

fun outsourcingProcessTypeLabel(type: String?): String = when (type) {
    "cutting" -> "外注切断"
    "forming" -> "外注成型"
    "plating" -> "外注メッキ"
    "welding" -> "外注溶接"
    "inspection" -> "外注検査"
    "processing" -> "外注加工"
    else -> type.orEmpty().ifBlank { "—" }
}

fun outsourcingProcessTypeColor(type: String?): androidx.compose.ui.graphics.Color = when (type) {
    "cutting" -> androidx.compose.ui.graphics.Color(0xFF409EFF)
    "forming" -> androidx.compose.ui.graphics.Color(0xFF67C23A)
    "plating" -> androidx.compose.ui.graphics.Color(0xFFE6A23C)
    "welding" -> androidx.compose.ui.graphics.Color(0xFFF56C6C)
    "inspection" -> androidx.compose.ui.graphics.Color(0xFF909399)
    "processing" -> androidx.compose.ui.graphics.Color(0xFF6366F1)
    else -> androidx.compose.ui.graphics.Color(0xFF909399)
}

fun formatOutsourcingPrice(price: Double?): String {
    if (price == null) return "—"
    return "¥%,.2f".format(java.util.Locale.JAPAN, price)
}

val OUTSOURCING_DELIVERY_LOCATIONS = listOf("仕上倉庫ヤード下", "引き取り", "その他")
val OUTSOURCING_CATEGORIES = listOf("ステー無償支給", "ステー有償支給", "材料無償支給", "材料有償支給")
val OUTSOURCING_CONTENTS = listOf("メッキ塗装", "ブラケット溶接", "ステー加工", "部品加工")
val OUTSOURCING_PROCESS_FORM_TYPES = OutsourcingProcessTab.entries.filter { it != OutsourcingProcessTab.All }
