package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.PlanBaselineFullComparisonResultDto
import com.example.smart_emap.data.model.PlanBaselineGenerateBody
import com.example.smart_emap.data.model.PlanBaselinePlanQuantityBody
import com.example.smart_emap.data.model.PlanBaselineRecordDto
import com.example.smart_emap.data.model.PlanOperationRateResponse
import com.example.smart_emap.data.model.SimpleMessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PlanBaselineApiService {
    @GET("/api/plan-baseline/comparison")
    suspend fun loadComparison(
        @Query("baselineMonth") baselineMonth: String,
        @Query("processName") processName: String? = null,
    ): PlanBaselineFullComparisonResultDto

    @POST("/api/plan-baseline/generate")
    suspend fun generate(@Body body: PlanBaselineGenerateBody): SimpleMessageResponse

    @DELETE("/api/plan-baseline/delete")
    suspend fun delete(
        @Query("baselineMonth") baselineMonth: String,
        @Query("processName") processName: String? = null,
    ): SimpleMessageResponse

    @GET("/api/plan-baseline/records")
    suspend fun loadRecords(
        @Query("baselineMonth") baselineMonth: String,
        @Query("processName") processName: String? = null,
    ): List<PlanBaselineRecordDto>

    @PUT("/api/plan-baseline/plan-quantity")
    suspend fun updatePlanQuantity(@Body body: PlanBaselinePlanQuantityBody): SimpleMessageResponse

    @DELETE("/api/plan-baseline/record")
    suspend fun deleteRecord(
        @Query("baselineMonth") baselineMonth: String,
        @Query("planDate") planDate: String,
        @Query("processName") processName: String? = null,
    ): SimpleMessageResponse

    @GET("/api/plan-baseline/plan-operation-rate")
    suspend fun loadPlanOperationRate(
        @Query("monthNum") monthNum: Int? = null,
        @Query("processName") processName: String? = null,
    ): PlanOperationRateResponse
}
