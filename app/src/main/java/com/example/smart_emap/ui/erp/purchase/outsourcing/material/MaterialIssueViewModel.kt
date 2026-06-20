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

data class MaterialIssueFormState(
    val supplierId: String = "",
    val orderNo: String = "",
    val issueDate: String = LocalDate.now().toString(),
    val operator: String = "",
    val materialCode: String = "",
    val materialName: String = "",
    val spec: String = "",
    val stockQty: Int = 0,
    val quantity: Int = 0,
    val unit: String = "",
    val unitWeight: Double = 0.0,
    val remarks: String = "",
)

data class MaterialIssueUiState(
    val isLoading: Boolean = false,
    val submitLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val supplierId: String = "",
    val orderNo: String = "",
    val materialCode: String = "",
    val status: String = "",
    val issueList: List<MaterialIssueItem> = OutsourcingMaterialMockData.issueList,
    val filteredList: List<MaterialIssueItem> = OutsourcingMaterialMockData.issueList,
    val selectedIds: Set<Int> = emptySet(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Int = OutsourcingMaterialMockData.issueList.size,
    val supplierOptions: List<OutsourcingMaterialOption> = OutsourcingMaterialMockData.supplierOptions,
    val orderOptions: List<OutsourcingOrderOption> = OutsourcingMaterialMockData.orderOptions,
    val materialOptions: List<OutsourcingMaterialSelectOption> = OutsourcingMaterialMockData.materialOptions,
    val showFormDialog: Boolean = false,
    val isEdit: Boolean = false,
    val editingId: Int? = null,
    val form: MaterialIssueFormState = MaterialIssueFormState(),
    val detailItem: MaterialIssueItem? = null,
    val snackbarMessage: String? = null,
) {
    val today: String = LocalDate.now().toString()
    val todayIssueCount: Int
        get() = filteredList.count { it.issueDate == today && it.status == "issued" }
    val pendingCount: Int get() = filteredList.count { it.status == "preparing" }
    val todayQuantity: Int
        get() = filteredList.filter { it.issueDate == today }.sumOf { it.totalWeight }
}

class MaterialIssueViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialIssueUiState())
    val uiState: StateFlow<MaterialIssueUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    fun setStartDate(value: String) = _uiState.update { it.copy(startDate = value) }
    fun setEndDate(value: String) = _uiState.update { it.copy(endDate = value) }
    fun setSupplierId(value: String) = _uiState.update { it.copy(supplierId = value) }
    fun setOrderNo(value: String) = _uiState.update { it.copy(orderNo = value) }
    fun setMaterialCode(value: String) = _uiState.update { it.copy(materialCode = value) }
    fun setStatus(value: String) = _uiState.update { it.copy(status = value) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(300)
            val state = _uiState.value
            var list = state.issueList
            if (state.startDate.isNotBlank()) list = list.filter { it.issueDate >= state.startDate }
            if (state.endDate.isNotBlank()) list = list.filter { it.issueDate <= state.endDate }
            if (state.supplierId.isNotBlank()) list = list.filter { it.supplierId.toString() == state.supplierId }
            if (state.orderNo.isNotBlank()) list = list.filter { it.orderNo.contains(state.orderNo, ignoreCase = true) }
            if (state.materialCode.isNotBlank()) list = list.filter { it.materialCode.contains(state.materialCode, ignoreCase = true) }
            if (state.status.isNotBlank()) list = list.filter { it.status == state.status }
            _uiState.update { it.copy(isLoading = false, filteredList = list, totalCount = list.size) }
        }
    }

    fun resetFilters() {
        _uiState.update { it.copy(startDate = "", endDate = "", supplierId = "", orderNo = "", materialCode = "", status = "", page = 1) }
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
                form = MaterialIssueFormState(issueDate = LocalDate.now().toString()),
            )
        }
    }

    fun openEditDialog(item: MaterialIssueItem) {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEdit = true,
                editingId = item.id,
                form = MaterialIssueFormState(
                    supplierId = item.supplierId.toString(),
                    orderNo = item.orderNo,
                    issueDate = item.issueDate,
                    operator = item.operator,
                    materialCode = item.materialCode,
                    materialName = item.materialName,
                    spec = item.spec,
                    quantity = item.quantity,
                    unit = item.unit,
                    unitWeight = item.unitWeight,
                    remarks = item.remarks,
                ),
            )
        }
    }

    fun updateForm(transform: (MaterialIssueFormState) -> MaterialIssueFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun onMaterialChange(code: String) {
        val material = _uiState.value.materialOptions.find { it.code == code }
        if (material != null) {
            _uiState.update {
                it.copy(
                    form = it.form.copy(
                        materialCode = material.code,
                        materialName = material.name,
                        spec = material.spec,
                        unit = material.unit,
                        unitWeight = material.unitWeight,
                        stockQty = material.stockQty,
                        quantity = minOf(100, material.stockQty),
                    ),
                )
            }
        }
    }

    fun hideFormDialog() = _uiState.update { it.copy(showFormDialog = false) }
    fun showDetail(item: MaterialIssueItem) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }

    fun submitForm() {
        viewModelScope.launch {
            val form = _uiState.value.form
            if (form.supplierId.isBlank() || form.issueDate.isBlank() || form.materialCode.isBlank() ||
                form.quantity <= 0 || form.operator.isBlank()
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

    fun issueItem(item: MaterialIssueItem) {
        _uiState.update { state ->
            val updated = state.issueList.map {
                if (it.id == item.id) it.copy(status = "issued") else it
            }
            state.copy(issueList = updated, snackbarMessage = "${item.issueNo} の出庫完了しました")
        }
        search()
    }

    fun deleteIssue(item: MaterialIssueItem) {
        _uiState.update { it.copy(snackbarMessage = "${item.issueNo} を削除しました") }
        search()
    }

    fun batchIssue() {
        val count = _uiState.value.selectedIds.size
        if (count == 0) return
        _uiState.update { it.copy(snackbarMessage = "${count}件の一括出庫を行います") }
    }

    fun exportData() {
        _uiState.update { it.copy(snackbarMessage = "Excel出力機能は準備中です") }
    }

    fun printDeliveryNote() {
        val count = _uiState.value.selectedIds.size
        if (count == 0) return
        _uiState.update { it.copy(snackbarMessage = "支給伝票印刷機能は準備中です") }
    }

    fun refreshAll() = search()

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialIssueViewModel() as T
    }
}
