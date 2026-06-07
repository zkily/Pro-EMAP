package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.DataGenerationBodyDto
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.PartStockUpdateBodyDto
import com.example.smart_emap.data.model.PagedListDto
import com.example.smart_emap.data.model.StockActionResultDto
import com.example.smart_emap.data.model.SyncMasterResultDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PartApiService {
    @GET("/api/part/stock")
    suspend fun listStock(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("keyword") keyword: String? = null,
        @Query("suppliers") suppliers: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("order_only") orderOnly: Boolean? = null,
    ): ApiEnvelope<PagedListDto<PartStockItemDto>>

    @PUT("/api/part/stock/{id}")
    suspend fun updateStock(
        @Path("id") id: Int,
        @Body body: PartStockUpdateBodyDto,
    ): ApiEnvelope<PartStockItemDto>

    @GET("/api/part/stock/supplier-names")
    suspend fun stockSupplierNames(): ApiEnvelope<List<String>>

    @POST("/api/part/stock/sync-part-master")
    suspend fun syncPartMaster(
        @Body body: Map<String, String?> = emptyMap(),
    ): ApiEnvelope<SyncMasterResultDto>

    @POST("/api/part/stock/calculate")
    suspend fun calculateStock(
        @Body body: Map<String, String?> = emptyMap(),
    ): ApiEnvelope<StockActionResultDto>

    @POST("/api/part-data-generation/generate")
    suspend fun generateStockData(@Body body: DataGenerationBodyDto): ApiEnvelope<Any>
}
