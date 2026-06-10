package com.example.smart_emap.ui.master.materialinspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterInspectionDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaterialInspectionUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
    val items: List<MasterInspectionDto> = emptyList(),
    val total: Int = 0,
    val selectedIds: Set<Int> = emptySet(),
    val showForm: Boolean = false,
    val editingItem: MasterInspectionDto? = null,
    val inspectionCd: String = "",
    val inspectionStandard: String = "",
    val showDetail: Boolean = false,
    val detailItem: MasterInspectionDto? = null,
    val pendingDeleteIds: List<Int>? = null,
    val snackbarMessage: String? = null,
)

class MaterialInspectionMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialInspectionUiState())
    val uiState: StateFlow<MaterialInspectionUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadPage() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    private suspend fun loadPage() {
        val state = _uiState.value
        val (items, total) = repository.loadInspectionMasters(state.keyword, state.page, state.pageSize)
        _uiState.update {
            it.copy(
                isLoading = false,
                items = items,
                total = total,
                selectedIds = it.selectedIds.filter { id -> items.any { row -> row.id == id } }.toSet(),
            )
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value, page = 1) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refreshAll()
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", page = 1) }
        refreshAll()
    }

    fun hasActiveFilters(): Boolean = _uiState.value.keyword.isNotBlank()

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        refreshAll()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        refreshAll()
    }

    fun toggleSelection(id: Int) {
        _uiState.update { state ->
            val next = state.selectedIds.toMutableSet()
            if (id in next) next.remove(id) else next.add(id)
            state.copy(selectedIds = next)
        }
    }

    fun toggleSelectAll(checked: Boolean) {
        _uiState.update { state ->
            state.copy(
                selectedIds = if (checked) {
                    state.items.mapNotNull { it.id }.toSet()
                } else {
                    emptySet()
                },
            )
        }
    }

    fun openCreate() {
        _uiState.update {
            it.copy(
                showForm = true,
                editingItem = null,
                inspectionCd = "",
                inspectionStandard = "",
            )
        }
    }

    fun openEdit(item: MasterInspectionDto) {
        _uiState.update {
            it.copy(
                showForm = true,
                editingItem = item,
                inspectionCd = item.inspectionCd.orEmpty(),
                inspectionStandard = item.inspectionStandard.orEmpty(),
            )
        }
    }

    fun setInspectionCd(value: String) = _uiState.update { it.copy(inspectionCd = value) }

    fun setInspectionStandard(value: String) = _uiState.update { it.copy(inspectionStandard = value) }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingItem = null) }

    fun openDetail(item: MasterInspectionDto) = _uiState.update { it.copy(showDetail = true, detailItem = item) }

    fun closeDetail() = _uiState.update { it.copy(showDetail = false, detailItem = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteIds = listOf(id)) }

    fun requestBatchDelete() {
        val ids = _uiState.value.selectedIds.toList()
        if (ids.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "削除するレコードを選択してください") }
            return
        }
        _uiState.update { it.copy(pendingDeleteIds = ids) }
    }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteIds = null) }

    fun confirmDelete() {
        val ids = _uiState.value.pendingDeleteIds ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteIds = null) }
            runCatching {
                if (ids.size == 1) {
                    repository.deleteInspectionMaster(ids.first())
                } else {
                    repository.batchDeleteInspection(ids)
                }
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        selectedIds = emptySet(),
                        snackbarMessage = if (ids.size == 1) "削除しました" else "${ids.size} 件のレコードを削除しました",
                    )
                }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.inspectionCd.trim()
        val standard = state.inspectionStandard.trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "検品CDを入力してください") }
                return
            }
            cd.length > 50 -> {
                _uiState.update { it.copy(snackbarMessage = "検品CDは1〜50文字で入力してください") }
                return
            }
            standard.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "検品規格を入力してください") }
                return
            }
            standard.length > 500 -> {
                _uiState.update { it.copy(snackbarMessage = "検品規格は1〜500文字で入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveInspectionMaster(state.editingItem?.id, cd, standard)
                if (ok) {
                    val isEdit = state.editingItem != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingItem = null,
                            snackbarMessage = if (isEdit) "更新しました" else "作成しました",
                        )
                    }
                    refreshAll()
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存に失敗しました") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialInspectionMasterViewModel(repository) as T
    }
}
