package com.example.smart_emap.ui.master.productprocessbom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ProductProcessBomStatsDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductProcessBomUiState(
    val isLoading: Boolean = false,
    val syncing: Boolean = false,
    val savingProductCd: Int? = null,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortOrder: String = "asc",
    val rows: List<ProductProcessBomUiRow> = emptyList(),
    val stats: ProductProcessBomStatsDto = ProductProcessBomStatsDto(),
    val showForm: Boolean = false,
    val formRow: ProductProcessBomUiRow? = null,
    val pendingDeleteProductCd: Int? = null,
    val snackbarMessage: String? = null,
)

class ProductProcessBomMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductProcessBomUiState())
    val uiState: StateFlow<ProductProcessBomUiState> = _uiState.asStateFlow()
    private val saveJobs = mutableMapOf<Int, Job>()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadPage() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗")
                    }
                }
        }
    }

    private suspend fun loadPage() {
        val state = _uiState.value
        val result = repository.loadProductProcessBomPage(
            keyword = state.keyword,
            page = state.page,
            limit = state.pageSize,
            sortBy = "product_name",
            sortOrder = state.sortOrder,
        )
        _uiState.update {
            it.copy(
                isLoading = false,
                rows = result.items.mapNotNull(ProductProcessBomMasterLogic::fromDto),
                stats = result.stats,
            )
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun search() {
        _uiState.update { it.copy(page = 1) }
        refreshAll()
    }

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", page = 1) }
        refreshAll()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        refreshAll()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        refreshAll()
    }

    fun toggleSort() {
        _uiState.update {
            it.copy(
                sortOrder = if (it.sortOrder == "asc") "desc" else "asc",
                page = 1,
            )
        }
        refreshAll()
    }

    fun updateRow(productCd: Int, transform: (ProductProcessBomUiRow) -> ProductProcessBomUiRow) {
        _uiState.update { state ->
            state.copy(
                rows = state.rows.map { row ->
                    if (row.productCd == productCd) transform(row) else row
                },
            )
        }
    }

    fun scheduleAutoSave(productCd: Int, canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        saveJobs[productCd]?.cancel()
        saveJobs[productCd] = viewModelScope.launch {
            delay(800)
            saveRow(productCd)
        }
    }

    private suspend fun saveRow(productCd: Int) {
        val row = _uiState.value.rows.find { it.productCd == productCd } ?: return
        _uiState.update { it.copy(savingProductCd = productCd) }
        runCatching {
            repository.updateProductProcessBom(productCd, ProductProcessBomMasterLogic.toUpdateBody(row))
        }.onSuccess {
            _uiState.update { it.copy(savingProductCd = null, snackbarMessage = "保存しました") }
        }.onFailure { e ->
            _uiState.update {
                it.copy(savingProductCd = null, snackbarMessage = e.message ?: "保存に失敗しました")
            }
        }
        saveJobs.remove(productCd)
    }

    fun syncProducts(canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(syncing = true) }
            runCatching { repository.syncProductProcessBom() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            syncing = false,
                            snackbarMessage = ProductProcessBomMasterLogic.syncMessage(data),
                        )
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

    fun openEdit(productCd: Int, canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching { repository.getProductProcessBom(productCd) }
                .onSuccess { dto ->
                    val row = ProductProcessBomMasterLogic.fromDto(dto)
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = row != null,
                            formRow = row,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました")
                    }
                }
        }
    }

    fun updateFormRow(transform: (ProductProcessBomUiRow) -> ProductProcessBomUiRow) {
        _uiState.update { state ->
            val current = state.formRow ?: return@update state
            state.copy(formRow = transform(current))
        }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, formRow = null) }

    fun saveForm(canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        val row = _uiState.value.formRow ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.updateProductProcessBom(row.productCd, ProductProcessBomMasterLogic.toUpdateBody(row))
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showForm = false,
                        formRow = null,
                        snackbarMessage = "製品工程BOMを更新しました",
                    )
                }
                refreshAll()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました")
                }
            }
        }
    }

    fun requestDelete(productCd: Int, canDelete: Boolean) {
        if (!canDelete) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update { it.copy(pendingDeleteProductCd = productCd) }
    }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteProductCd = null) }

    fun confirmDelete() {
        val cd = _uiState.value.pendingDeleteProductCd ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteProductCd = null) }
            runCatching {
                repository.deleteProductProcessBom(cd)
            }.onSuccess {
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = "製品工程BOMを削除しました")
                }
                refreshAll()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました")
                }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: MasterRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductProcessBomMasterViewModel(repository) as T
    }
}
