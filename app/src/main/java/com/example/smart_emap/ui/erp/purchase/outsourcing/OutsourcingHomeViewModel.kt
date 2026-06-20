package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.example.smart_emap.data.model.OutsourcingDashboardDto
import com.example.smart_emap.data.model.OutsourcingSupplierSummaryDto
import com.example.smart_emap.data.model.OutsourcingUpcomingDeliveryDto
import com.example.smart_emap.data.repository.OutsourcingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OutsourcingHomeStatsUi(
    val todayOrders: Int = 0,
    val pendingOrders: Int = 0,
    val todayReceivings: Int = 0,
    val stockAlerts: Int = 0,
    val overdueOrders: Int = 0,
)

data class OutsourcingHomeUiState(
    val isLoading: Boolean = false,
    val dashboard: OutsourcingDashboardDto? = null,
    val stats: OutsourcingHomeStatsUi = OutsourcingHomeStatsUi(),
    val upcomingDeliveries: List<OutsourcingUpcomingDeliveryDto> = emptyList(),
    val supplierSummary: List<OutsourcingSupplierSummaryDto> = emptyList(),
    val snackbarMessage: String? = null,
)

class OutsourcingHomeViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutsourcingHomeUiState())
    val uiState: StateFlow<OutsourcingHomeUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val dashboard = repository.loadDashboard()
                val deliveries = repository.loadUpcomingDeliveries(7)
                val summary = repository.loadSupplierSummary()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dashboard = dashboard,
                        upcomingDeliveries = deliveries,
                        supplierSummary = summary,
                        stats = buildStats(dashboard),
                    )
                }
            }.onFailure { e ->
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

    private fun buildStats(dashboard: OutsourcingDashboardDto?): OutsourcingHomeStatsUi {
        val today = dashboard?.todayOrders.orEmpty()
        val pending = dashboard?.pendingOrders.orEmpty()
        val receivings = dashboard?.todayReceivings.orEmpty()
        val alerts = dashboard?.stockAlerts.orEmpty()
        val overdue = dashboard?.overdueOrders.orEmpty()
        return OutsourcingHomeStatsUi(
            todayOrders = (today["plating_orders"] ?: 0) + (today["welding_orders"] ?: 0),
            pendingOrders = (pending["plating_pending"] ?: 0) + (pending["welding_pending"] ?: 0),
            todayReceivings = (receivings["plating_receivings"] ?: 0) + (receivings["welding_receivings"] ?: 0),
            stockAlerts = (alerts["plating_alerts"] ?: 0) + (alerts["welding_alerts"] ?: 0) + (alerts["material_alerts"] ?: 0),
            overdueOrders = (overdue["plating_overdue"] ?: 0) + (overdue["welding_overdue"] ?: 0),
        )
    }

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OutsourcingHomeViewModel(repository) as T
    }
}

fun outsourcingSupplierTypeLabel(type: String?): String = when (type) {
    "plating" -> "メッキ"
    "welding" -> "溶接"
    "cutting" -> "切断"
    "forming" -> "成型"
    "parts_processing" -> "部品加工"
    "both" -> "両方"
    else -> type.orEmpty().ifBlank { "—" }
}

fun outsourcingSupplierTypeColor(type: String?): Color = when (type) {
    "plating" -> Color(0xFFE6A23C)
    "welding" -> Color(0xFFF56C6C)
    "cutting" -> Color(0xFF67C23A)
    "forming" -> Color(0xFF409EFF)
    "parts_processing" -> Color(0xFF909399)
    "both" -> Color(0xFF6366F1)
    else -> Color(0xFF909399)
}

fun outsourcingDeliveryTypeLabel(type: String?): String = when (type) {
    "plating" -> "メッキ"
    "welding" -> "溶接"
    else -> type.orEmpty().ifBlank { "—" }
}

fun outsourcingDeliveryTypeColor(type: String?): Color = when (type) {
    "plating" -> Color(0xFFE6A23C)
    "welding" -> Color(0xFFF56C6C)
    else -> Color(0xFF909399)
}

fun outsourcingDaysRemainingColor(days: Int?): Color = when {
    days == null -> Color(0xFF67C23A)
    days <= 1 -> Color(0xFFF56C6C)
    days <= 3 -> Color(0xFFE6A23C)
    else -> Color(0xFF67C23A)
}
