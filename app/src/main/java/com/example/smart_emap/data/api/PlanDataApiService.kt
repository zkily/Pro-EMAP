package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.PlanDataListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlanDataApiService {
    @GET("/api/excel-monitor/plan-data")
    suspend fun listPlanData(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("productNameExact") productNameExact: String? = null,
        @Query("processName") processName: String? = null,
        @Query("limit") limit: Int = 10000,
        @Query("page") page: Int = 1,
    ): PlanDataListResponse
}
