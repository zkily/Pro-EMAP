package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ActiveProductCountDto
import com.example.smart_emap.data.model.DailyConfirmedSeriesDto
import com.example.smart_emap.data.model.InventoryStatsDto
import com.example.smart_emap.data.model.SalesStatsDto

data class DashboardData(
    val sales: SalesStatsDto = SalesStatsDto(),
    val inventory: InventoryStatsDto = InventoryStatsDto(),
    val activeProducts: ActiveProductCountDto = ActiveProductCountDto(),
    val dailySeries: DailyConfirmedSeriesDto? = null,
)

class DashboardRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadDashboard(): Result<DashboardData> = runCatching {
        val api = apiClient.dashboardApi()
        val daily = runCatching { api.getDailyConfirmedSeries() }.getOrNull()
        DashboardData(
            sales = api.getSalesStats(),
            inventory = api.getInventoryStats(),
            activeProducts = api.getActiveProductCount(),
            dailySeries = daily,
        )
    }
}
