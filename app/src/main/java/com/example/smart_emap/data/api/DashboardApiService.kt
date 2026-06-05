package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ActiveProductCountDto
import com.example.smart_emap.data.model.DailyConfirmedSeriesDto
import com.example.smart_emap.data.model.InventoryStatsDto
import com.example.smart_emap.data.model.SalesStatsDto
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApiService {
    @GET("/api/erp/sales/orders/stats")
    suspend fun getSalesStats(): SalesStatsDto

    @GET("/api/erp/sales/orders/daily-confirmed-series")
    suspend fun getDailyConfirmedSeries(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): DailyConfirmedSeriesDto

    @GET("/api/erp/inventory/stats")
    suspend fun getInventoryStats(): InventoryStatsDto

    @GET("/api/master/products/stats/active-count")
    suspend fun getActiveProductCount(): ActiveProductCountDto
}
