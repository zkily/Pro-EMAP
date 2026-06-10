package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.BatchUpdateLockBody
import com.example.smart_emap.data.model.BatchUpdateLockResponse
import com.example.smart_emap.data.model.GenerateProductionSummaryBody
import com.example.smart_emap.data.model.InventoryStagnationResponse
import com.example.smart_emap.data.model.ClearPlanFieldsResponse
import com.example.smart_emap.data.model.InventoryTrendCalcStartDateResponse
import com.example.smart_emap.data.model.PrevCarryBreakdownResponse
import com.example.smart_emap.data.model.PrevCarryWipTotalResponse
import com.example.smart_emap.data.model.ProcessMachinePlanResponse
import com.example.smart_emap.data.model.ProcessMachineProductsResponse
import com.example.smart_emap.data.model.ProductionSummaryListPageResponse
import com.example.smart_emap.data.model.ProductionSummaryProductsResponse
import com.example.smart_emap.data.model.SimpleMessageResponse
import com.example.smart_emap.data.model.StartDateBody
import com.example.smart_emap.data.model.UpdateFromOrderDailyBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProductionSummaryApiService {
    @GET("/api/database/production-summarys")
    suspend fun listProductionSummarys(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50000,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("productCd") productCd: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("excludeInactiveProducts") excludeInactiveProducts: Boolean? = null,
    ): ProductionSummaryListPageResponse

    @GET("/api/database/production-summarys/products")
    suspend fun listProducts(): ProductionSummaryProductsResponse

    @POST("/api/database/production-summarys/generate")
    suspend fun generate(@Body body: GenerateProductionSummaryBody): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-from-order-daily")
    suspend fun updateFromOrderDaily(@Body body: UpdateFromOrderDailyBody = UpdateFromOrderDailyBody()): SimpleMessageResponse

    @POST("/api/database/production-summarys/batch-update-lock/acquire")
    suspend fun acquireBatchUpdateLock(@Body body: BatchUpdateLockBody): BatchUpdateLockResponse

    @POST("/api/database/production-summarys/batch-update-lock/release")
    suspend fun releaseBatchUpdateLock(@Body body: BatchUpdateLockBody): BatchUpdateLockResponse

    @POST("/api/database/production-summarys/clear-carry-over")
    suspend fun clearCarryOver(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-carry-over")
    suspend fun updateCarryOver(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-actual")
    suspend fun updateActual(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-defect")
    suspend fun updateDefect(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-scrap")
    suspend fun updateScrap(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-on-hold")
    suspend fun updateOnHold(): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-production-dates")
    suspend fun updateProductionDates(): SimpleMessageResponse

    @POST("/api/database/production-summarys/clear-calculated-fields")
    suspend fun clearCalculatedFields(@Body body: StartDateBody): SimpleMessageResponse

    @POST("/api/database/production-summarys/clear-plan-fields")
    suspend fun clearPlanFields(@Body body: StartDateBody): SimpleMessageResponse

    @POST("/api/database/production-summarys/clear-molding-plan")
    suspend fun clearMoldingPlan(@Body body: StartDateBody): ClearPlanFieldsResponse

    @POST("/api/database/production-summarys/clear-welding-plan")
    suspend fun clearWeldingPlan(@Body body: StartDateBody): ClearPlanFieldsResponse

    @POST("/api/database/production-summarys/update-plan")
    suspend fun updatePlan(@Body body: StartDateBody? = null): SimpleMessageResponse

    @GET("/api/database/production-summarys/inventory-trend-calc-start-date")
    suspend fun getInventoryTrendCalcStartDate(): InventoryTrendCalcStartDateResponse

    @POST("/api/database/production-summarys/update-inventory")
    suspend fun updateInventory(@Body body: StartDateBody? = null): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-trend")
    suspend fun updateTrend(@Body body: StartDateBody? = null): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-safety-stock")
    suspend fun updateSafetyStock(@Body body: StartDateBody? = null): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-product-master")
    suspend fun updateProductMaster(@Body body: GenerateProductionSummaryBody): SimpleMessageResponse

    @POST("/api/database/production-summarys/update-machine")
    suspend fun updateMachine(@Body body: GenerateProductionSummaryBody): SimpleMessageResponse

    @GET("/api/database/production-summarys/prev-carry-pre-plating-wip-total")
    suspend fun getPrevCarryWipTotal(@Query("month") month: String): PrevCarryWipTotalResponse

    @GET("/api/database/production-summarys/prev-carry-breakdown")
    suspend fun getPrevCarryBreakdown(
        @Query("month") month: String,
        @Query("column") column: String,
    ): PrevCarryBreakdownResponse

    @GET("/api/database/production-summarys/process-machine-plan")
    suspend fun getProcessMachinePlan(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("processes") processes: String? = null,
        @Query("productCd") productCd: String? = null,
    ): ProcessMachinePlanResponse

    @GET("/api/database/production-summarys/process-machine-plan/products")
    suspend fun getProcessMachinePlanProducts(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("process") process: String,
        @Query("machine") machine: String,
    ): ProcessMachineProductsResponse

    @GET("/api/database/production-summarys/inventory-stagnation")
    suspend fun getInventoryStagnation(
        @Query("as_of") asOf: String? = null,
        @Query("min_quantity") minQuantity: Int? = null,
        @Query("stable_calendar_days") stableCalendarDays: Int? = null,
        @Query("productCd") productCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): InventoryStagnationResponse
}
