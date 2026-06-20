package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.StockAlertDto
import com.example.smart_emap.data.repository.InventoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryHomeUiState(
    val isLoading: Boolean = false,
    val todayStr: String = "",
    val productStats: Map<String, Int> = emptyMap(),
    val materialStats: Map<String, Int> = emptyMap(),
    val partStats: Map<String, Int> = emptyMap(),
    val alerts: List<StockAlertDto> = emptyList(),
    val alertsTotal: Int = 0,
    val snackbarMessage: String? = null,
)

class InventoryHomeViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryHomeUiState())
    val uiState: StateFlow<InventoryHomeUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = e.message ?: "データの取得に失敗しました",
                        )
                    }
                }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private suspend fun loadInternal() = coroutineScope {
        val today = repository.todayStr()
        val productDeferred = async { repository.loadProductSummaryForDate(today) }
        val materialDeferred = async { repository.loadMaterialStockForDate(today) }
        val partDeferred = async { repository.loadPartStockForDate(today) }
        val alertsDeferred = async { repository.loadStockAlerts() }

        val productRows = productDeferred.await()
        val materialRows = materialDeferred.await()
        val partRows = partDeferred.await()
        val (alerts, alertsTotal) = alertsDeferred.await()

        _uiState.update {
            it.copy(
                isLoading = false,
                todayStr = today,
                productStats = PRODUCT_INVENTORY_FIELDS.associate { field ->
                    field.key to sumProductInventory(productRows, field.key)
                },
                materialStats = MATERIAL_STAT_FIELDS.associate { field ->
                    field.key to sumMaterialStat(materialRows, field.key)
                },
                partStats = PART_STAT_FIELDS.associate { field ->
                    field.key to sumPartStat(partRows, field.key)
                },
                alerts = alerts,
                alertsTotal = alertsTotal,
            )
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InventoryHomeViewModel(repository) as T
    }
}
