package com.example.smart_emap.ui.erp.purchase.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.data.model.MaterialMasterItemDto
import com.example.smart_emap.data.model.MaterialMasterUpdateBodyDto
import com.example.smart_emap.data.repository.MaterialReceivingFilters
import com.example.smart_emap.data.repository.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MaterialQualityEditForm(
    val toleranceRange: String = "",
    val tolerance1: String = "",
    val tolerance2: String = "",
    val rangeValue: String = "",
    val minValue: String = "",
    val maxValue: String = "",
    val actualValue1: String = "",
    val actualValue2: String = "",
    val actualValue3: String = "",
    val representativeModel: String = "",
)

data class MaterialReceivingInspectionUiState(
    val isLoading: Boolean = false,
    val printLoading: Boolean = false,
    val savingMaster: Boolean = false,
    val tabIndex: Int = 0,
    val historyItems: List<MaterialLogItemDto> = emptyList(),
    val cachedHistoryAll: List<MaterialLogItemDto> = emptyList(),
    val totalCount: Int = 0,
    val masterItems: List<MaterialMasterItemDto> = emptyList(),
    val supplierOptions: List<String> = emptyList(),
    val selectedSuppliers: List<String> = emptyList(),
    val selectedMaterialName: String = "",
    val startDate: String = LocalDate.now().minusMonths(1).toString(),
    val endDate: String = LocalDate.now().toString(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val snackbarMessage: String? = null,
    val detailItem: MaterialLogItemDto? = null,
    val pendingPrintHtml: String? = null,
    val editingMaster: MaterialMasterItemDto? = null,
    val editForm: MaterialQualityEditForm = MaterialQualityEditForm(),
) {
    val materialNameOptions: List<String>
        get() = masterItems.mapNotNull { it.materialName?.takeIf { n -> n.isNotBlank() } }.distinct().sorted()

    val filteredMasterItems: List<MaterialMasterItemDto>
        get() = if (selectedMaterialName.isBlank()) {
            masterItems
        } else {
            masterItems.filter { it.materialName == selectedMaterialName }
        }
}

class MaterialReceivingInspectionViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialReceivingInspectionUiState())
    val uiState: StateFlow<MaterialReceivingInspectionUiState> = _uiState.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadReceivingSuppliers()
                _uiState.update { it.copy(supplierOptions = suppliers) }
                loadMasters()
                loadHistory()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.update {
            it.copy(tabIndex = index, selectedMaterialName = if (index == 1) it.selectedMaterialName else "")
        }
    }

    fun setStartDate(v: String) {
        _uiState.update { it.copy(startDate = v, page = 1) }
        reloadHistory()
    }

    fun setEndDate(v: String) {
        _uiState.update { it.copy(endDate = v, page = 1) }
        reloadHistory()
    }

    fun setSelectedSuppliers(v: List<String>) {
        _uiState.update { it.copy(selectedSuppliers = v, page = 1) }
        reloadHistory()
    }

    fun setSelectedMaterialName(v: String) = _uiState.update { it.copy(selectedMaterialName = v) }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        if (_uiState.value.selectedSuppliers.isNotEmpty()) {
            applyClientPagination()
        } else {
            reloadHistory()
        }
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        if (_uiState.value.selectedSuppliers.isNotEmpty()) {
            applyClientPagination()
        } else {
            reloadHistory()
        }
    }

    fun showDetail(item: MaterialLogItemDto) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun printHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val state = _uiState.value
                val rows = if (state.selectedSuppliers.isNotEmpty()) {
                    filterBySuppliers(state.cachedHistoryAll, state.selectedSuppliers)
                } else {
                    repository.loadReceivingListAll(historyFilters(state, page = 1, pageSize = maxOf(state.totalCount, 100)), state.totalCount)
                }
                if (rows.isEmpty()) {
                    _uiState.update { it.copy(printLoading = false, snackbarMessage = "印刷するデータがありません") }
                    return@launch
                }
                val supplierLabel = when {
                    state.selectedSuppliers.isEmpty() -> "全仕入先"
                    state.selectedSuppliers.size == 1 -> state.selectedSuppliers.first()
                    else -> state.selectedSuppliers.joinToString(", ")
                }
                val html = buildInspectionHistoryPrintHtml(rows, supplierLabel)
                _uiState.update { it.copy(printLoading = false, pendingPrintHtml = html) }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "印刷失敗") }
            }
        }
    }

    fun openMasterEdit(item: MaterialMasterItemDto) {
        _uiState.update {
            it.copy(
                editingMaster = item,
                editForm = MaterialQualityEditForm(
                    toleranceRange = item.toleranceRange.orEmpty(),
                    tolerance1 = item.tolerance1?.toString().orEmpty(),
                    tolerance2 = item.tolerance2?.toString().orEmpty(),
                    rangeValue = item.rangeValue.orEmpty(),
                    minValue = item.minValue?.toString().orEmpty(),
                    maxValue = item.maxValue?.toString().orEmpty(),
                    actualValue1 = item.actualValue1?.toString().orEmpty(),
                    actualValue2 = item.actualValue2?.toString().orEmpty(),
                    actualValue3 = item.actualValue3?.toString().orEmpty(),
                    representativeModel = item.representativeModel.orEmpty(),
                ),
            )
        }
    }

    fun closeMasterEdit() = _uiState.update { it.copy(editingMaster = null) }

    fun setEditForm(form: MaterialQualityEditForm) = _uiState.update { it.copy(editForm = form) }

    fun updateEditForm(block: (MaterialQualityEditForm) -> MaterialQualityEditForm) {
        _uiState.update { it.copy(editForm = block(it.editForm)) }
    }

    fun toggleMasterStatus(item: MaterialMasterItemDto, enabled: Boolean) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateMasterMaterial(id, MaterialMasterUpdateBodyDto(status = if (enabled) 1 else 0))
                loadMasters()
                _uiState.update { it.copy(snackbarMessage = "${item.materialName} の状態を更新しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "状態更新失敗") }
            }
        }
    }

    fun saveMasterEdit() {
        val state = _uiState.value
        val item = state.editingMaster ?: return
        val id = item.id ?: return
        val form = state.editForm
        viewModelScope.launch {
            _uiState.update { it.copy(savingMaster = true) }
            runCatching {
                repository.updateMasterMaterial(
                    id,
                    MaterialMasterUpdateBodyDto(
                        toleranceRange = form.toleranceRange.ifBlank { null },
                        tolerance1 = form.tolerance1.toDoubleOrNull(),
                        tolerance2 = form.tolerance2.toDoubleOrNull(),
                        rangeValue = form.rangeValue.ifBlank { null },
                        minValue = form.minValue.toDoubleOrNull(),
                        maxValue = form.maxValue.toDoubleOrNull(),
                        actualValue1 = form.actualValue1.ifBlank { null },
                        actualValue2 = form.actualValue2.ifBlank { null },
                        actualValue3 = form.actualValue3.ifBlank { null },
                        representativeModel = form.representativeModel.ifBlank { null },
                    ),
                )
                closeMasterEdit()
                loadMasters()
                _uiState.update { it.copy(savingMaster = false, snackbarMessage = "品質基準を保存しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(savingMaster = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    private fun reloadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadHistory() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    private suspend fun loadMasters() {
        val items = repository.loadMasterMaterials()
        _uiState.update { it.copy(masterItems = items) }
    }

    private suspend fun loadHistory() {
        val state = _uiState.value
        if (state.selectedSuppliers.isNotEmpty()) {
            val (all, _) = repository.loadReceivingList(
                historyFilters(state, page = 1, pageSize = 10000),
            )
            val filtered = filterBySuppliers(all, state.selectedSuppliers)
            val start = (state.page - 1) * state.pageSize
            val pageItems = filtered.drop(start).take(state.pageSize)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cachedHistoryAll = all,
                    historyItems = pageItems,
                    totalCount = filtered.size,
                )
            }
        } else {
            val (items, total) = repository.loadReceivingList(historyFilters(state))
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cachedHistoryAll = items,
                    historyItems = items,
                    totalCount = total,
                )
            }
        }
    }

    private fun applyClientPagination() {
        val state = _uiState.value
        val filtered = filterBySuppliers(state.cachedHistoryAll, state.selectedSuppliers)
        val start = (state.page - 1) * state.pageSize
        _uiState.update {
            it.copy(
                historyItems = filtered.drop(start).take(state.pageSize),
                totalCount = filtered.size,
            )
        }
    }

    private fun historyFilters(
        state: MaterialReceivingInspectionUiState,
        page: Int = state.page,
        pageSize: Int = state.pageSize,
    ) = MaterialReceivingFilters(
        startDate = state.startDate,
        endDate = state.endDate,
        page = page,
        pageSize = pageSize,
    )

    private fun filterBySuppliers(rows: List<MaterialLogItemDto>, suppliers: List<String>): List<MaterialLogItemDto> {
        if (suppliers.isEmpty()) return rows
        return rows.filter { row ->
            val name = row.supplier.orEmpty()
            name.isNotBlank() && suppliers.any { it == name }
        }
    }

    class Factory(private val repository: MaterialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialReceivingInspectionViewModel(repository) as T
    }
}
