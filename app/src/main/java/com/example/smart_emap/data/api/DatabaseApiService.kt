package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ProductionSummaryListResponse
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
}
