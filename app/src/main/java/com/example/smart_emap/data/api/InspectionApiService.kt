package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CreateInspectionBody
import com.example.smart_emap.data.model.CreateInspectionResponse
import com.example.smart_emap.data.model.InspectionListResponse
import com.example.smart_emap.data.model.InspectionProductivityAnalysisResponse
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisResponse
import com.example.smart_emap.data.model.PatchInspectionBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InspectionApiService {
    @GET("/api/plan/inspection-management/list")
    suspend fun list(
        @Query("production_day") productionDay: String? = null,
        @Query("hide_completed") hideCompleted: Boolean? = null,
        @Query("limit") limit: Int? = null,
    ): InspectionListResponse

    @POST("/api/plan/inspection-management")
    suspend fun create(@Body body: CreateInspectionBody): CreateInspectionResponse

    @PATCH("/api/plan/inspection-management/{id}")
    suspend fun patch(
        @Path("id") id: Int,
        @Body body: PatchInspectionBody,
    ): ApiMessageResponse

    @GET("/api/plan/inspection-management/productivity-analysis")
    suspend fun productivityAnalysis(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("mes_inspector_user_id") mesInspectorUserId: Int? = null,
        @Query("product_cd") productCd: String? = null,
        @Query("include_incomplete") includeIncomplete: Boolean? = null,
        @Query("limit") limit: Int? = null,
    ): InspectionProductivityAnalysisResponse

    @GET("/api/plan/inspection-management/utilization-analysis")
    suspend fun utilizationAnalysis(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("mes_inspector_user_id") mesInspectorUserId: Int? = null,
        @Query("include_incomplete") includeIncomplete: Boolean? = null,
        @Query("extra_workdays") extraWorkdays: String? = null,
        @Query("extra_holidays") extraHolidays: String? = null,
        @Query("use_company_calendar") useCompanyCalendar: Boolean? = true,
        @Query("limit") limit: Int? = null,
    ): InspectionUtilizationAnalysisResponse
}
