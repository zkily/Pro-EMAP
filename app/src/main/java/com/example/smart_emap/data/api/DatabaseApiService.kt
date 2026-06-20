package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ProductionSummaryListResponse
import com.example.smart_emap.data.model.QualityRateByProcessResponse
import com.example.smart_emap.data.model.QualityRateByProductResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DatabaseApiService {
    @GET("/api/database/production-summarys")
    suspend fun listProductionSummarys(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50000,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("excludeInactiveProducts") excludeInactiveProducts: Boolean? = null,
    ): ProductionSummaryListResponse

    @GET("/api/database/production-summarys/quality-rate-by-process")
    suspend fun qualityRateByProcess(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("process") process: String? = null,
        @Query("includeAmounts") includeAmounts: Boolean = false,
    ): QualityRateByProcessResponse

    @GET("/api/database/production-summarys/quality-rate-by-product")
    suspend fun qualityRateByProduct(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("productCd") productCd: String? = null,
        @Query("sortBy") sortBy: String = "product_name",
        @Query("sortOrder") sortOrder: String = "asc",
    ): QualityRateByProductResponse
}
