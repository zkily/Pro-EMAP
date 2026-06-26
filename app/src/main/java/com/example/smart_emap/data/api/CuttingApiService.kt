package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CuttingListResponse
import com.example.smart_emap.data.model.CuttingProductionIndicatorLinesResponse
import com.example.smart_emap.data.model.CuttingProductivityAnalysisResponse
import com.example.smart_emap.data.model.PatchCuttingBody
import com.example.smart_emap.data.model.ReorderCuttingBody
import com.example.smart_emap.data.model.SplitCuttingToNextDayBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CuttingApiService {
    @GET("/api/plan/cutting-management/list")
    suspend fun list(
        @Query("production_day") productionDay: String? = null,
        @Query("cutting_machine") cuttingMachine: String? = null,
        @Query("limit") limit: Int? = null,
    ): CuttingListResponse

    @PATCH("/api/plan/cutting-management/{id}")
    suspend fun patch(
        @Path("id") id: Int,
        @Body body: PatchCuttingBody,
    ): ApiMessageResponse

    @POST("/api/plan/cutting-management/reorder")
    suspend fun reorder(@Body body: ReorderCuttingBody): ApiMessageResponse

    @POST("/api/plan/cutting-management/{id}/split-to-next-day")
    suspend fun splitToNextDay(
        @Path("id") id: Int,
        @Body body: SplitCuttingToNextDayBody,
    ): ApiMessageResponse

    @GET("/api/plan/cutting-production-indicator/lines")
    suspend fun productivityLines(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): CuttingProductionIndicatorLinesResponse

    @GET("/api/plan/cutting-production-indicator/productivity-analysis")
    suspend fun productivityAnalysis(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("production_line") productionLine: String? = null,
        @Query("product_cd") productCd: String? = null,
        @Query("include_incomplete") includeIncomplete: Boolean? = null,
        @Query("limit") limit: Int? = null,
    ): CuttingProductivityAnalysisResponse
}
