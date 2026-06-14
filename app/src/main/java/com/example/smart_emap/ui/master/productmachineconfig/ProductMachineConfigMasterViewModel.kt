package com.example.smart_emap.ui.master.productmachineconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.AvailableProductDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductMachineConfigUiState(
    val isLoading: Boolean = false,
    val syncing: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val rows: List<ProductMachineConfigUiRow> = emptyList(),
    val machineOptions: List<MachineOption> = emptyList(),
    val productOptions: List<Pair<String, String>> = emptyList(),
    val availableProducts: List<AvailableProductDto> = emptyList(),
    val showForm: Boolean = false,
    val isEdit: Boolean = false,
    val formRow: ProductMachineConfigUiRow? = null,
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
) {
    val filteredRows: List<ProductMachineConfigUiRow> =
        ProductMachineConfigMasterLogic.filterRows(rows, keyword)
}

class ProductMachineConfigMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductMachineConfigUiState())
    val uiState: StateFlow<ProductMachineConfigUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val configs = repository.loadProductMachineConfigList()
                val products = repository.loadAvailableProductsForMachineConfig()
                val machines = repository.loadMachineOptions()
                Triple(configs, products, machines)
            }.onSuccess { (configs, products, machines) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rows = configs.mapNotNull(ProductMachineConfigMasterLogic::fromDto),
                        machineOptions = ProductMachineConfigMasterLogic.machineOptions(machines),
                        productOptions = ProductMachineConfigMasterLogic.productOptions(products),
                        availableProducts = products,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗")
                }
            }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun clearFilters() = _uiState.update { it.copy(keyword = "") }

    fun openCreate(canCreate: Boolean) {
        if (!canCreate) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update {
            it.copy(showForm = true, isEdit = false, formRow = ProductMachineConfigMasterLogic.emptyFormRow())
        }
    }

    fun openEdit(row: ProductMachineConfigUiRow, canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update { it.copy(showForm = true, isEdit = true, formRow = row) }
    }

    fun updateFormRow(transform: (ProductMachineConfigUiRow) -> ProductMachineConfigUiRow) {
        _uiState.update { state ->
            val current = state.formRow ?: return@update state
            state.copy(formRow = transform(current))
        }
    }

    fun onProductSelected(productCd: String) {
        val product = _uiState.value.availableProducts.find { it.productCd == productCd }
        updateFormRow {
            it.copy(productCd = productCd, productName = product?.productName.orEmpty())
        }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, formRow = null, isEdit = false) }

    fun saveForm(canCreate: Boolean, canEdit: Boolean) {
        val state = _uiState.value
        val row = state.formRow ?: return
        if (row.productCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品を選択してください") }
            return
        }
        if (state.isEdit) {
            if (!canEdit) {
                _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(actionLoading = true) }
                runCatching {
                    repository.updateProductMachineConfigFull(
                        row.id,
                        ProductMachineConfigMasterLogic.toUpdateBody(row),
                    )
                }.onSuccess {
                    _uiState.update {
                        it.copy(actionLoading = false, showForm = false, formRow = null, snackbarMessage = "機器設定を更新しました")
                    }
                    refreshAll()
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました")
                    }
                }
            }
        } else {
            if (!canCreate) {
                _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(actionLoading = true) }
                runCatching {
                    repository.createProductMachineConfig(ProductMachineConfigMasterLogic.toCreateBody(row))
                }.onSuccess {
                    _uiState.update {
                        it.copy(actionLoading = false, showForm = false, formRow = null, snackbarMessage = "機器設定を登録しました")
                    }
                    refreshAll()
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました")
                    }
                }
            }
        }
    }

    fun syncProducts(canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(syncing = true) }
            runCatching { repository.syncProductMachineConfig() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(syncing = false, snackbarMessage = ProductMachineConfigMasterLogic.syncMessage(data))
                    }
                    refreshAll()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(syncing = false, snackbarMessage = e.message ?: "同期に失敗しました")
                    }
                }
        }
    }

    fun requestDelete(id: Int, canDelete: Boolean) {
        if (!canDelete) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update { it.copy(pendingDeleteId = id) }
    }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching { repository.deleteProductMachineConfig(id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = "機器設定を削除しました")
                    }
                    refreshAll()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました")
                    }
                }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductMachineConfigMasterViewModel(repository) as T
    }
}
