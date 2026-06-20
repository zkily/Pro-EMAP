package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ComponentRequirementsBundleResponse
import com.example.smart_emap.data.model.MaterialRequirementsSummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductionRequirementsApiService {
    @GET("/api/plan/batch/material-requirements-summary")
    suspend fun materialRequirementsSummary(
        @Query("date_start") dateStart: String,
        @Query("date_end") dateEnd: String,
        @Query("production_month") productionMonth: String? = null,
    ): MaterialRequirementsSummaryResponse

    @GET("/api/plan/batch/component-requirements-bundle")
    suspend fun componentRequirementsBundle(
        @Query("date_start") dateStart: String,
        @Query("date_end") dateEnd: String,
        @Query("production_month") productionMonth: String? = null,
        @Query("plan_column") planColumn: String? = null,
    ): ComponentRequirementsBundleResponse
}
