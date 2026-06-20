package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.InventoryLogCreateBodyDto
import com.example.smart_emap.data.model.InventoryLogListDataDto
import com.example.smart_emap.data.model.InventoryLogRowDto
import com.example.smart_emap.data.model.SimpleMessageResponse
import com.example.smart_emap.data.model.StockAlertListResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApiService {
    @GET("/api/erp/inventory/alerts")
    suspend fun listStockAlerts(
        @Query("alert_type") alertType: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): StockAlertListResponse

    @GET("/api/erp/inventory-logs")
    suspend fun listInventoryLogs(
        @Query("item") item: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("dateRange") dateRange: List<String>? = null,
        @Query("monthPicker") monthPicker: String? = null,
        @Query("stageType") stageType: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "log_date",
        @Query("sortOrder") sortOrder: String = "desc",
    ): ApiEnvelope<InventoryLogListDataDto>

    @GET("/api/erp/inventory-logs/recent")
    suspend fun listRecentInventoryLogs(
        @Query("limit") limit: Int = 50,
        @Query("hd_no") hdNo: String? = null,
    ): ApiEnvelope<List<InventoryLogRowDto>>

    @POST("/api/erp/inventory-logs")
    suspend fun createInventoryLog(
        @Body body: InventoryLogCreateBodyDto,
    ): ApiEnvelope<InventoryLogRowDto>

    @DELETE("/api/erp/inventory-logs/{id}")
    suspend fun deleteInventoryLog(@Path("id") id: Int): SimpleMessageResponse

    @POST("/api/erp/inventory-logs/import")
    suspend fun importInventoryLogs(): ApiEnvelope<Map<String, Any>>
}
