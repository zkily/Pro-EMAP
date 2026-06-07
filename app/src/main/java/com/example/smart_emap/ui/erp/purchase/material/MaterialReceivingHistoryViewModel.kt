package com.example.smart_emap.ui.erp.purchase.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.data.repository.MaterialReceivingFilters
import com.example.smart_emap.data.repository.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MaterialReceivingHistoryUiState(
    val isLoading: Boolean = false,
    val items: List<MaterialLogItemDto> = emptyList(),
    val totalCount: Int = 0,
    val keyword: String = "",
    val startDate: String = LocalDate.now().minusMonths(1).toString(),
    val endDate: String = LocalDate.now().toString(),
    val supplierOptions: List<String> = emptyList(),
    val selectedSuppliers: List<String> = emptyList(),
    val snackbarMessage: String? = null,
    val detailItem: MaterialLogItemDto? = null,
)

class MaterialReceivingHistoryViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialReceivingHistoryUiState())
    val uiState: StateFlow<MaterialReceivingHistoryUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadReceivingSuppliers()
                _uiState.update { it.copy(supplierOptions = suppliers) }
                searchInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }
    fun setStartDate(value: String) = _uiState.update { it.copy(startDate = value) }
    fun setEndDate(value: String) = _uiState.update { it.copy(endDate = value) }
    fun setSelectedSuppliers(value: List<String>) = _uiState.update { it.copy(selectedSuppliers = value) }
    fun showDetail(item: MaterialLogItemDto) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { searchInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") }
                }
        }
    }

    private suspend fun searchInternal() {
        val state = _uiState.value
        val (items, total) = repository.loadReceivingList(
            MaterialReceivingFilters(
                keyword = state.keyword,
                startDate = state.startDate,
                endDate = state.endDate,
                suppliers = state.selectedSuppliers,
            ),
        )
        _uiState.update { it.copy(isLoading = false, items = items, totalCount = total) }
    }

    class Factory(private val repository: MaterialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialReceivingHistoryViewModel(repository) as T
    }
}
