package com.example.smart_emap.ui.erp.shipping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.repository.ShippingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShippingHomeUiState(
    val isLoading: Boolean = false,
    val todayStr: String = "",
    val statValues: Map<String, String> = emptyMap(),
    val snackbarMessage: String? = null,
)

class ShippingHomeViewModel(
    private val repository: ShippingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingHomeUiState())
    val uiState: StateFlow<ShippingHomeUiState> = _uiState.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "取得に失敗しました") }
                }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private suspend fun loadInternal() = coroutineScope {
        val today = repository.todayStr()
        val pickingDeferred = async { repository.loadPickingProgress() }
        val summaryDeferred = async { repository.loadProductionSummaryForDate(today) }

        val picking = pickingDeferred.await()
        val summary = summaryDeferred.await()

        val ov = picking?.todayOverview
        val totalCurrent = summary.sumOf { (it.warehouseInventory ?: 0) + (it.outsourcedWarehouseInventory ?: 0) }
        val negativeCount = summary.sumOf { row ->
            val v = row.warehouseInventory ?: 0
            if (v < 0) v else 0
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                todayStr = today,
                statValues = mapOf(
                    "pallets" to (ov?.totalToday?.toString() ?: "0"),
                    "pending" to (ov?.pendingToday?.toString() ?: "0"),
                    "completed" to (ov?.completedToday?.toString() ?: "0"),
                    "inventory" to formatShippingNum(totalCurrent),
                    "shortage" to formatShippingNum(negativeCount),
                ),
            )
        }
    }

    class Factory(private val repository: ShippingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShippingHomeViewModel(repository) as T
    }
}
