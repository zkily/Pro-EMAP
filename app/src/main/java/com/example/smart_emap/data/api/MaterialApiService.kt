package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.DataGenerationBodyDto
import com.example.smart_emap.data.model.DataGenerationResultDto
import com.example.smart_emap.data.model.MaterialForecastDetailDto
import com.example.smart_emap.data.model.MaterialForecastStatsDto
import com.example.smart_emap.data.model.MaterialForecastSummaryDto
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.data.model.MaterialMasterItemDto
import com.example.smart_emap.data.model.MaterialMasterUpdateBodyDto
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.MaterialStockSubCreateBodyDto
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import com.example.smart_emap.data.model.MaterialStockSubUpdateBodyDto
import com.example.smart_emap.data.model.MaterialStockUpdateBodyDto
import com.example.smart_emap.data.model.MaterialTransferToSubBodyDto
import com.example.smart_emap.data.model.PagedListDto
import com.example.smart_emap.data.model.StockActionResultDto
import com.example.smart_emap.data.model.SyncMasterResultDto
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MaterialApiService {
    @GET("/api/material/receiving")
    suspend fun listReceiving(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("keyword") keyword: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("supplier") supplier: String? = null,
    ): ApiEnvelope<PagedListDto<MaterialLogItemDto>>

    @GET("/api/material/receiving/suppliers")
    suspend fun receivingSuppliers(): ApiEnvelope<List<String>>

    @GET("/api/material/stock")
    suspend fun listStock(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("keyword") keyword: String? = null,
        @Query("suppliers") suppliers: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("target_date") targetDate: String? = null,
        @Query("order_only") orderOnly: Boolean? = null,
    ): ApiEnvelope<PagedListDto<MaterialStockItemDto>>

    @PUT("/api/material/stock/{id}")
    suspend fun updateStock(
        @Path("id") id: Int,
        @Body body: MaterialStockUpdateBodyDto,
    ): ApiEnvelope<MaterialStockItemDto>

    @GET("/api/material/stock/sub")
    suspend fun listSubStock(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("keyword") keyword: String? = null,
        @Query("suppliers") suppliers: String? = null,
        @Query("target_date") targetDate: String? = null,
    ): ApiEnvelope<PagedListDto<MaterialStockSubItemDto>>

    @POST("/api/material/stock/sub")
    suspend fun createSubStock(
        @Body body: MaterialStockSubCreateBodyDto,
    ): ApiEnvelope<MaterialStockSubItemDto>

    @PUT("/api/material/stock/sub/{id}")
    suspend fun updateSubStock(
        @Path("id") id: Int,
        @Body body: MaterialStockSubUpdateBodyDto,
    ): ApiEnvelope<MaterialStockSubItemDto>

    @DELETE("/api/material/stock/sub/{id}")
    suspend fun deleteSubStock(@Path("id") id: Int): ApiEnvelope<Any>

    @GET("/api/material/stock/supplier-names")
    suspend fun stockSupplierNames(): ApiEnvelope<List<String>>

    @POST("/api/material/stock/sync-material-master")
    suspend fun syncMaterialMaster(
        @Body body: Map<String, String?> = emptyMap(),
    ): ApiEnvelope<SyncMasterResultDto>

    @POST("/api/material/stock/calculate")
    suspend fun calculateStock(): ApiEnvelope<StockActionResultDto>

    @POST("/api/material-data-generation/generate")
    suspend fun generateStockData(@Body body: DataGenerationBodyDto): ApiEnvelope<DataGenerationResultDto>

    @GET("/api/material/forecast/list")
    suspend fun forecastList(
        @Query("target_year") targetYear: Int,
        @Query("target_month") targetMonth: Int,
        @Query("supplier_cd") supplierCd: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
    ): ApiEnvelope<PagedListDto<MaterialForecastDetailDto>>

    @GET("/api/material/forecast/summary")
    suspend fun forecastSummary(
        @Query("target_year") targetYear: Int,
        @Query("target_month") targetMonth: Int,
        @Query("supplier_cd") supplierCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): ApiEnvelope<List<MaterialForecastSummaryDto>>

    @GET("/api/material/forecast/stats")
    suspend fun forecastStats(
        @Query("target_year") targetYear: Int,
        @Query("target_month") targetMonth: Int,
        @Query("supplier_cd") supplierCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): ApiEnvelope<MaterialForecastStatsDto>

    @GET("/api/material/forecast/suppliers")
    suspend fun forecastSuppliers(
        @Query("target_year") targetYear: Int,
        @Query("target_month") targetMonth: Int,
    ): ApiEnvelope<List<Map<String, String>>>

    @GET("/api/master/materials")
    suspend fun listMasterMaterials(
        @Query("pageSize") pageSize: Int = 9999,
        @Query("keyword") keyword: String? = null,
    ): ApiEnvelope<PagedListDto<MaterialMasterItemDto>>

    @PUT("/api/master/materials/{id}")
    suspend fun updateMasterMaterial(
        @Path("id") id: Int,
        @Body body: MaterialMasterUpdateBodyDto,
    ): ApiEnvelope<MaterialMasterItemDto>

    @POST("/api/material/stock/transfer-to-sub")
    suspend fun transferToSub(@Body body: MaterialTransferToSubBodyDto): ApiEnvelope<Any>
}
