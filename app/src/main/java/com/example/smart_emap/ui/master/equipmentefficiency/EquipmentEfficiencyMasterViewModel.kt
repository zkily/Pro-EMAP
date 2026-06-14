package com.example.smart_emap.ui.master.equipmentefficiency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.EquipmentEfficiencyTabCountsDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EquipmentEfficiencyUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val activeProcessTab: String = "all",
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val rows: List<EquipmentEfficiencyUiRow> = emptyList(),
    val total: Int = 0,
    val tabCounts: EquipmentEfficiencyTabCountsDto = EquipmentEfficiencyTabCountsDto(),
    val machineDistinctCount: Int = 0,
    val productDistinctCount: Int = 0,
    val machineOptions: List<EeMachineOption> = emptyList(),
    val productOptions: List<Pair<String, String>> = emptyList(),
    val showForm: Boolean = false,
    val isEdit: Boolean = false,
    val formRow: EquipmentEfficiencyUiRow? = null,
    val pendingDeleteId: Int? = null,
    val statusUpdatingId: Int? = null,
    val snackbarMessage: String? = null,
) {
    val tabCountsAll: Int get() = tabCounts.all ?: 0
}

class EquipmentEfficiencyMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EquipmentEfficiencyUiState())
    val uiState: StateFlow<EquipmentEfficiencyUiState> = _uiState.asStateFlow()
    private var keywordJob: Job? = null

    init {
        refreshOptions()
        loadData()
    }

    fun refreshAll() {
        refreshOptions()
        loadData()
    }

    private fun refreshOptions() {
        viewModelScope.launch {
            runCatching {
                val machines = repository.loadMachineOptions()
                val products = repository.loadProductsForEquipmentEfficiency()
                machines to products
            }.onSuccess { (machines, products) ->
                _uiState.update {
                    it.copy(
                        machineOptions = EquipmentEfficiencyMasterLogic.machineOptions(machines),
                        productOptions = products,
                    )
                }
            }
        }
    }

    fun loadData() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.loadEquipmentEfficiencyPage(
                    keyword = state.keyword,
                    processType = state.activeProcessTab,
                    page = state.currentPage,
                    pageSize = state.pageSize,
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rows = result.rows.mapNotNull(EquipmentEfficiencyMasterLogic::fromDto),
                        total = result.total,
                        tabCounts = result.tabCounts,
                        machineDistinctCount = result.machineDistinctCount,
                        productDistinctCount = result.productDistinctCount,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, snackbarMessage = e.message ?: "能率データの読み込みに失敗しました")
                }
            }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value, currentPage = 1) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(350)
            loadData()
        }
    }

    fun clearFilters() {
        keywordJob?.cancel()
        _uiState.update { it.copy(keyword = "", currentPage = 1) }
        loadData()
    }

    fun setProcessTab(tab: String) {
        _uiState.update { it.copy(activeProcessTab = tab, currentPage = 1) }
        loadData()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceAtLeast(1)) }
        loadData()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, currentPage = 1) }
        loadData()
    }

    fun openCreate(canCreate: Boolean) {
        if (!canCreate) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update {
            it.copy(showForm = true, isEdit = false, formRow = EquipmentEfficiencyMasterLogic.emptyFormRow())
        }
    }

    fun openEdit(row: EquipmentEfficiencyUiRow, canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        _uiState.update { it.copy(showForm = true, isEdit = true, formRow = row) }
    }

    fun updateFormRow(transform: (EquipmentEfficiencyUiRow) -> EquipmentEfficiencyUiRow) {
        _uiState.update { state ->
            val current = state.formRow ?: return@update state
            state.copy(formRow = transform(current))
        }
    }

    fun onMachineSelected(machineCd: String) {
        val machine = _uiState.value.machineOptions.find { it.value == machineCd }
        val label = machine?.label.orEmpty()
        val name = label.substringBefore(" (").ifBlank { label }
        updateFormRow { it.copy(machineCd = machineCd, machinesName = name) }
    }

    fun onProductSelected(productCd: String) {
        val product = _uiState.value.productOptions.find { it.first == productCd }
        val label = product?.second.orEmpty()
        val name = label.substringBefore(" (").ifBlank { label }
        updateFormRow { it.copy(productCd = productCd, productName = name) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, formRow = null, isEdit = false) }

    fun saveForm(canCreate: Boolean, canEdit: Boolean) {
        val state = _uiState.value
        val row = state.formRow ?: return
        if (row.machineCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "設備を選択してください") }
            return
        }
        if (row.productCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品を選択してください") }
            return
        }
        if (row.efficiencyRate < 0) {
            _uiState.update { it.copy(snackbarMessage = "能率は0以上である必要があります") }
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
                    repository.updateEquipmentEfficiency(row.id, EquipmentEfficiencyMasterLogic.toUpdateBody(row))
                }.onSuccess {
                    _uiState.update {
                        it.copy(actionLoading = false, showForm = false, formRow = null, snackbarMessage = "能率設定を更新しました")
                    }
                    loadData()
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
                    repository.createEquipmentEfficiency(EquipmentEfficiencyMasterLogic.toCreateBody(row))
                }.onSuccess {
                    _uiState.update {
                        it.copy(actionLoading = false, showForm = false, formRow = null, snackbarMessage = "能率設定を登録しました")
                    }
                    loadData()
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました")
                    }
                }
            }
        }
    }

    fun toggleStatus(row: EquipmentEfficiencyUiRow, enabled: Boolean, canEdit: Boolean) {
        if (!canEdit) {
            _uiState.update { it.copy(snackbarMessage = "マスタ管理の操作権限がありません") }
            return
        }
        val newStatus = if (enabled) 1 else 0
        val previous = row.status
        _uiState.update { state ->
            state.copy(
                statusUpdatingId = row.id,
                rows = state.rows.map { if (it.id == row.id) it.copy(status = newStatus) else it },
            )
        }
        viewModelScope.launch {
            runCatching {
                repository.updateEquipmentEfficiency(row.id, EquipmentEfficiencyMasterLogic.toStatusBody(newStatus))
            }.onSuccess {
                _uiState.update { it.copy(statusUpdatingId = null, snackbarMessage = "状態を更新しました") }
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        statusUpdatingId = null,
                        snackbarMessage = e.message ?: "状態の更新に失敗しました",
                        rows = state.rows.map { if (it.id == row.id) it.copy(status = previous) else it },
                    )
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
            runCatching { repository.deleteEquipmentEfficiency(id) }
                .onSuccess {
                    val state = _uiState.value
                    val nextPage = if (state.rows.size <= 1 && state.currentPage > 1) state.currentPage - 1 else state.currentPage
                    _uiState.update {
                        it.copy(actionLoading = false, currentPage = nextPage, snackbarMessage = "能率設定を削除しました")
                    }
                    loadData()
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
            EquipmentEfficiencyMasterViewModel(repository) as T
    }
}
