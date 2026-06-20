package com.example.smart_emap.ui.erp.purchase.outsourcing.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.resolveOutsourcingStockStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuppliedMaterialStockUiState(
    val isLoading: Boolean = false,
    val supplierId: String = "",
    val materialCode: String = "",
    val stockStatus: String = "",
    val supplierOptions: List<OutsourcingMaterialOption> = OutsourcingMaterialMockData.supplierOptions,
    val allSuppliers: List<SuppliedMaterialSupplierStock> = OutsourcingMaterialMockData.supplierStocks,
    val filteredSuppliers: List<SuppliedMaterialSupplierStock> = OutsourcingMaterialMockData.supplierStocks,
    val historyTitle: String = "",
    val historyItems: List<SuppliedMaterialHistoryItem> = emptyList(),
    val showHistoryDialog: Boolean = false,
    val snackbarMessage: String? = null,
) {
    val supplierCount: Int get() = allSuppliers.size
    val materialCount: Int get() = allSuppliers.sumOf { it.totalItems }
    val lowStockCount: Int get() = allSuppliers.sumOf { it.lowStockItems }
}

class SuppliedMaterialStockViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SuppliedMaterialStockUiState())
    val uiState: StateFlow<SuppliedMaterialStockUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    fun setSupplierId(value: String) = _uiState.update { it.copy(supplierId = value) }
    fun setMaterialCode(value: String) = _uiState.update { it.copy(materialCode = value) }
    fun setStockStatus(value: String) = _uiState.update { it.copy(stockStatus = value) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(300)
            val state = _uiState.value
            var result = state.allSuppliers
            if (state.supplierId.isNotBlank()) {
                result = result.filter { it.id.toString() == state.supplierId }
            }
            if (state.materialCode.isNotBlank()) {
                val code = state.materialCode.trim().lowercase()
                result = result.map { supplier ->
                    supplier.copy(materials = supplier.materials.filter {
                        it.materialCode.lowercase().contains(code)
                    })
                }.filter { it.materials.isNotEmpty() }
            }
            if (state.stockStatus.isNotBlank()) {
                result = result.map { supplier ->
                    supplier.copy(
                        materials = supplier.materials.filter { material ->
                            val status = resolveOutsourcingStockStatus(material.stockQty, material.minStock)
                            when (state.stockStatus) {
                                "low" -> status == OutsourcingStockStatus.Low
                                "empty" -> status == OutsourcingStockStatus.Empty
                                "normal" -> status == OutsourcingStockStatus.Normal
                                else -> true
                            }
                        },
                    )
                }.filter { it.materials.isNotEmpty() }
            }
            _uiState.update { it.copy(isLoading = false, filteredSuppliers = result) }
        }
    }

    fun resetFilters() {
        _uiState.update { it.copy(supplierId = "", materialCode = "", stockStatus = "") }
        search()
    }

    fun refreshStock() {
        _uiState.update { it.copy(snackbarMessage = "在庫情報を更新しています...") }
        search()
    }

    fun exportData() {
        _uiState.update { it.copy(snackbarMessage = "Excel出力機能は準備中です") }
    }

    fun viewHistory(supplier: SuppliedMaterialSupplierStock, material: SuppliedMaterialStockItem) {
        _uiState.update {
            it.copy(
                historyTitle = "${supplier.name} - ${material.materialCode} 履歴",
                historyItems = OutsourcingMaterialMockData.sampleHistory(),
                showHistoryDialog = true,
            )
        }
    }

    fun hideHistory() = _uiState.update { it.copy(showHistoryDialog = false, historyTitle = "", historyItems = emptyList()) }

    fun refreshAll() = search()

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SuppliedMaterialStockViewModel() as T
    }
}
