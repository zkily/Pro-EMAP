package com.example.smart_emap.ui.erp.purchase.outsourcing.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UsageFormState(
    val supplierId: String = "",
    val usageDate: String = LocalDate.now().toString(),
    val orderNo: String = "",
    val reporter: String = "",
    val materialCode: String = "",
    val materialName: String = "",
    val availableQty: Int = 0,
    val usageQty: Int = 0,
    val unit: String = "",
    val unitWeight: Double = 0.0,
    val productQty: Int = 0,
    val remarks: String = "",
)

data class UsageManagementUiState(
    val isLoading: Boolean = false,
    val submitLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val supplierId: String = "",
    val orderNo: String = "",
    val materialCode: String = "",
    val usageList: List<UsageManagementItem> = OutsourcingMaterialMockData.usageList,
    val filteredList: List<UsageManagementItem> = OutsourcingMaterialMockData.usageList,
    val selectedIds: Set<Int> = emptySet(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Int = OutsourcingMaterialMockData.usageList.size,
    val supplierOptions: List<OutsourcingMaterialOption> = OutsourcingMaterialMockData.supplierOptions,
    val orderOptions: List<OutsourcingOrderOption> = OutsourcingMaterialMockData.orderOptions,
    val availableMaterials: List<OutsourcingMaterialSelectOption> = OutsourcingMaterialMockData.availableUsageMaterials,
    val showFormDialog: Boolean = false,
    val isEdit: Boolean = false,
    val editingId: Int? = null,
    val form: UsageFormState = UsageFormState(),
    val detailItem: UsageManagementItem? = null,
    val snackbarMessage: String? = null,
) {
    val monthlyUsageQty: Int get() = filteredList.sumOf { it.usageQty }
    val monthlyUsageWeight: Int get() = filteredList.sumOf { it.usageWeight }
    val pendingReportCount: Int = 2
    val totalUsageQty: Int get() = filteredList.sumOf { it.usageQty }
    val totalProductQty: Int get() = filteredList.sumOf { it.productQty }
    val avgYieldRate: Double
        get() = if (filteredList.isEmpty()) 0.0 else filteredList.sumOf { it.yieldRate } / filteredList.size
}

class UsageManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UsageManagementUiState())
    val uiState: StateFlow<UsageManagementUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    fun setStartDate(value: String) = _uiState.update { it.copy(startDate = value) }
    fun setEndDate(value: String) = _uiState.update { it.copy(endDate = value) }
    fun setSupplierId(value: String) = _uiState.update { it.copy(supplierId = value) }
    fun setOrderNo(value: String) = _uiState.update { it.copy(orderNo = value) }
    fun setMaterialCode(value: String) = _uiState.update { it.copy(materialCode = value) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(300)
            val state = _uiState.value
            var list = state.usageList
            if (state.startDate.isNotBlank()) list = list.filter { it.usageDate >= state.startDate }
            if (state.endDate.isNotBlank()) list = list.filter { it.usageDate <= state.endDate }
            if (state.supplierId.isNotBlank()) list = list.filter { it.supplierId.toString() == state.supplierId }
            if (state.orderNo.isNotBlank()) list = list.filter { it.orderNo.contains(state.orderNo, ignoreCase = true) }
            if (state.materialCode.isNotBlank()) list = list.filter { it.materialCode.contains(state.materialCode, ignoreCase = true) }
            _uiState.update { it.copy(isLoading = false, filteredList = list, totalCount = list.size) }
        }
    }

    fun resetFilters() {
        _uiState.update { it.copy(startDate = "", endDate = "", supplierId = "", orderNo = "", materialCode = "", page = 1) }
        search()
    }

    fun toggleSelection(id: Int) {
        _uiState.update { state ->
            val next = state.selectedIds.toMutableSet()
            if (next.contains(id)) next.remove(id) else next.add(id)
            state.copy(selectedIds = next)
        }
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEdit = false,
                editingId = null,
                form = UsageFormState(usageDate = LocalDate.now().toString()),
            )
        }
    }

    fun openEditDialog(item: UsageManagementItem) {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEdit = true,
                editingId = item.id,
                form = UsageFormState(
                    supplierId = item.supplierId.toString(),
                    usageDate = item.usageDate,
                    orderNo = item.orderNo,
                    reporter = item.reporter,
                    materialCode = item.materialCode,
                    materialName = item.materialName,
                    usageQty = item.usageQty,
                    unit = item.unit,
                    productQty = item.productQty,
                    remarks = item.remarks,
                ),
            )
        }
    }

    fun updateForm(transform: (UsageFormState) -> UsageFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun onSupplierChange(supplierId: String) {
        _uiState.update { it.copy(form = it.form.copy(supplierId = supplierId, materialCode = "", materialName = "")) }
    }

    fun onMaterialChange(code: String) {
        val material = _uiState.value.availableMaterials.find { it.code == code }
        if (material != null) {
            _uiState.update {
                it.copy(
                    form = it.form.copy(
                        materialCode = material.code,
                        materialName = material.name,
                        unit = material.unit,
                        unitWeight = material.unitWeight,
                        availableQty = material.stockQty,
                    ),
                )
            }
        }
    }

    fun hideFormDialog() = _uiState.update { it.copy(showFormDialog = false) }
    fun showDetail(item: UsageManagementItem) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }

    fun submitForm() {
        viewModelScope.launch {
            val form = _uiState.value.form
            if (form.supplierId.isBlank() || form.usageDate.isBlank() || form.orderNo.isBlank() ||
                form.materialCode.isBlank() || form.usageQty <= 0 || form.reporter.isBlank()
            ) {
                _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
                return@launch
            }
            _uiState.update { it.copy(submitLoading = true) }
            delay(400)
            _uiState.update {
                it.copy(
                    submitLoading = false,
                    showFormDialog = false,
                    snackbarMessage = if (it.isEdit) "更新しました" else "登録しました",
                )
            }
            search()
        }
    }

    fun deleteUsage(item: UsageManagementItem) {
        _uiState.update { it.copy(snackbarMessage = "${item.usageNo} を削除しました") }
        search()
    }

    fun batchReport() {
        val count = _uiState.value.selectedIds.size
        if (count == 0) return
        _uiState.update { it.copy(snackbarMessage = "${count}件の一括登録を行います") }
    }

    fun exportData() {
        _uiState.update { it.copy(snackbarMessage = "Excel出力機能は準備中です") }
    }

    fun refreshAll() = search()

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            UsageManagementViewModel() as T
    }
}
