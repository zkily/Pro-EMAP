package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.AbcAnalysisRowDto
import com.example.smart_emap.data.model.DestinationGroupDto
import com.example.smart_emap.data.model.InventoryKpiRowDto
import com.example.smart_emap.data.model.PickingHistoryRowDto
import com.example.smart_emap.data.model.PickingNewProgressDto
import com.example.smart_emap.data.model.ShippingItemDto
import com.example.smart_emap.data.model.ShippingOverviewRowDto
import com.example.smart_emap.data.model.WarehouseDailyRowDto
import com.example.smart_emap.data.model.WeldingShippingDataDto
import com.example.smart_emap.data.model.WeldingShippingProductDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShippingApiService {
    @GET("/api/shipping/items")
    suspend fun listItems(
        @Query("shipping_date") shippingDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("destination_cd") destinationCd: String? = null,
        @Query("product_name") productName: String? = null,
        @Query("status") status: String? = null,
        @Query("shipping_no") shippingNo: String? = null,
    ): List<ShippingItemDto>

    @GET("/api/shipping/overview")
    suspend fun overview(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("destination_cds") destinationCds: String? = null,
    ): List<ShippingOverviewRowDto>

    @GET("/api/shipping/picking/new-progress")
    suspend fun pickingNewProgress(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): PickingNewProgressDto

    @POST("/api/shipping/items/refresh-picking-log-matched/async")
    suspend fun refreshPickingAsync(): Map<String, Any?>

    @GET("/api/shipping/picking/history")
    suspend fun pickingHistory(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Map<String, Any?>

    @GET("/api/shipping/welding/products")
    suspend fun weldingProducts(): List<Map<String, Any?>>

    @POST("/api/shipping/welding/data")
    suspend fun weldingData(@Body body: Map<String, @JvmSuppressWildcards Any?>): Map<String, Any?>

    @POST("/api/shipping/welding/export")
    suspend fun weldingExport(@Body body: Map<String, @JvmSuppressWildcards Any?>): Map<String, Any?>

    @GET("/api/shipping/warehouse-daily/rows")
    suspend fun warehouseDailyRows(
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("product_cd") productCd: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("massProductOnly") massProductionOnly: Boolean? = null,
    ): Map<String, Any?>

    @GET("/api/shipping/warehouse-daily/product-options")
    suspend fun warehouseDailyProductOptions(): List<Map<String, Any?>>

    @POST("/api/shipping/warehouse-daily/sync-from-order-daily")
    suspend fun warehouseDailySync(): Map<String, Any?>

    @POST("/api/shipping/warehouse-daily/generate-data")
    suspend fun warehouseDailyGenerate(): Map<String, Any?>

    @GET("/api/database/inventory-kpi/turnover")
    suspend fun kpiTurnover(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Map<String, Any?>

    @GET("/api/database/inventory-kpi/avg-inventory-days")
    suspend fun kpiAvgInventoryDays(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Map<String, Any?>

    @GET("/api/database/inventory-kpi/shortage-alerts")
    suspend fun kpiShortageAlerts(
        @Query("as_of") asOf: String,
    ): Map<String, Any?>

    @GET("/api/database/inventory-kpi/overstock-alerts")
    suspend fun kpiOverstockAlerts(
        @Query("as_of") asOf: String,
    ): Map<String, Any?>

    @GET("/api/database/inventory-kpi/reorder-point")
    suspend fun kpiReorderPoint(
        @Query("as_of") asOf: String,
    ): Map<String, Any?>
}
