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

data class MaterialReceivingInspectionUiState(
    val isLoading: Boolean = false,
    val tabIndex: Int = 0,
    val historyItems: List<MaterialLogItemDto> = emptyList(),
    val masterItems: List<MaterialMasterItemDto> = emptyList(),
    val keyword: String = "",
    val startDate: String = LocalDate.now().minusMonths(1).toString(),
    val endDate: String = LocalDate.now().toString(),
    val masterKeyword: String = "",
    val snackbarMessage: String? = null,
    val editingMaster: MaterialMasterItemDto? = null,
    val editTolerance1: String = "",
    val editTolerance2: String = "",
)

class MaterialReceivingInspectionViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialReceivingInspectionUiState())
    val uiState: StateFlow<MaterialReceivingInspectionUiState> = _uiState.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    fun setTab(index: Int) = _uiState.update { it.copy(tabIndex = index) }
    fun setKeyword(v: String) = _uiState.update { it.copy(keyword = v) }
    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun setMasterKeyword(v: String) = _uiState.update { it.copy(masterKeyword = v) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun searchHistory() = refreshAll()
    fun searchMaster() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val items = repository.loadMasterMaterials(_uiState.value.masterKeyword)
                _uiState.update { it.copy(isLoading = false, masterItems = items) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") }
            }
        }
    }

    fun openMasterEdit(item: MaterialMasterItemDto) {
        _uiState.update {
            it.copy(
                editingMaster = item,
                editTolerance1 = item.tolerance1?.toString().orEmpty(),
                editTolerance2 = item.tolerance2?.toString().orEmpty(),
            )
        }
    }

    fun setEditTolerance1(v: String) = _uiState.update { it.copy(editTolerance1 = v) }
    fun setEditTolerance2(v: String) = _uiState.update { it.copy(editTolerance2 = v) }
    fun closeMasterEdit() = _uiState.update { it.copy(editingMaster = null) }

    fun saveMasterEdit() {
        val state = _uiState.value
        val item = state.editingMaster ?: return
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateMasterMaterial(
                    id,
                    MaterialMasterUpdateBodyDto(
                        tolerance1 = state.editTolerance1.toDoubleOrNull(),
                        tolerance2 = state.editTolerance2.toDoubleOrNull(),
                    ),
                )
                closeMasterEdit()
                searchMaster()
                _uiState.update { it.copy(snackbarMessage = "保存しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val (history, _) = repository.loadReceivingList(
            MaterialReceivingFilters(
                keyword = state.keyword,
                startDate = state.startDate,
                endDate = state.endDate,
            ),
        )
        val masters = repository.loadMasterMaterials(state.masterKeyword)
        _uiState.update { it.copy(isLoading = false, historyItems = history, masterItems = masters) }
    }

    class Factory(private val repository: MaterialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialReceivingInspectionViewModel(repository) as T
    }
}
