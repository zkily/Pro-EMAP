package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CreateInspectionBody
import com.example.smart_emap.data.model.CreateInspectionResponse
import com.example.smart_emap.data.model.CreateInspectionQrScanBody
import com.example.smart_emap.data.model.CreateInspectionQrScanResponse
import com.example.smart_emap.data.model.InspectionListResponse
import com.example.smart_emap.data.model.DeleteInspectionNextAssignmentBody
import com.example.smart_emap.data.model.InspectionMonitorSummaryResponse
import com.example.smart_emap.data.model.InspectionNextAssignmentResponse
import com.example.smart_emap.data.model.InspectionNextAssignmentsResponse
import com.example.smart_emap.data.model.InspectionQrScanListResponse
import com.example.smart_emap.data.model.InspectionQrScanSummaryResponse
import com.example.smart_emap.data.model.UpsertInspectionNextAssignmentBody
import com.example.smart_emap.data.model.InspectionProductivityAnalysisResponse
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisResponse
import com.example.smart_emap.data.model.PatchInspectionBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InspectionApiService {
    @GET("/api/plan/inspection-management/list")
    suspend fun list(
        @Query("production_day") productionDay: String? = null,
        @Query("hide_completed") hideCompleted: Boolean? = null,
        @Query("limit") limit: Int? = null,
    ): InspectionListResponse

    @GET("/api/plan/inspection-management/monitor-summary")
    suspend fun monitorSummary(
        @Query("production_day") productionDay: String,
        @Query("limit") limit: Int? = null,
    ): InspectionMonitorSummaryResponse

    @GET("/api/plan/inspection-management/next-assignments")
    suspend fun nextAssignments(
        @Query("production_day") productionDay: String,
    ): InspectionNextAssignmentsResponse

    @GET("/api/plan/inspection-management/next-assignment/me")
    suspend fun myNextAssignment(
        @Query("production_day") productionDay: String,
    ): InspectionNextAssignmentResponse

    @DELETE("/api/plan/inspection-management/next-assignment/me")
    suspend fun deleteMyNextAssignment(
        @Query("production_day") productionDay: String,
    ): ApiMessageResponse

    @PUT("/api/plan/inspection-management/next-assignment")
    suspend fun upsertNextAssignment(
        @Body body: UpsertInspectionNextAssignmentBody,
    ): InspectionNextAssignmentResponse

    @HTTP(method = "DELETE", path = "/api/plan/inspection-management/next-assignment", hasBody = true)
    suspend fun deleteNextAssignment(
        @Body body: DeleteInspectionNextAssignmentBody,
    ): ApiMessageResponse

    @POST("/api/plan/inspection-management/qr-scans")
    suspend fun createQrScan(@Body body: CreateInspectionQrScanBody): CreateInspectionQrScanResponse

    @GET("/api/plan/inspection-management/qr-scans")
    suspend fun listQrScans(
        @Query("production_day") productionDay: String,
        @Query("product_cd") productCd: String,
        @Query("inspection_id") inspectionId: Int? = null,
        @Query("started_at") startedAt: String? = null,
        @Query("ended_at") endedAt: String? = null,
        @Query("limit") limit: Int? = null,
    ): InspectionQrScanListResponse

    @GET("/api/plan/inspection-management/qr-scans/summary")
    suspend fun qrScanSummary(
        @Query("production_day") productionDay: String,
        @Query("product_cd") productCd: String? = null,
        @Query("inspection_id") inspectionId: Int? = null,
        @Query("inspector_user_id") inspectorUserId: Int? = null,
        @Query("started_at") startedAt: String? = null,
        @Query("ended_at") endedAt: String? = null,
    ): InspectionQrScanSummaryResponse

    @POST("/api/plan/inspection-management")
    suspend fun create(@Body body: CreateInspectionBody): CreateInspectionResponse

    @PATCH("/api/plan/inspection-management/{id}")
    suspend fun patch(
        @Path("id") id: Int,
        @Body body: PatchInspectionBody,
    ): ApiMessageResponse

    @DELETE("/api/plan/inspection-management/{id}")
    suspend fun delete(@Path("id") id: Int): ApiMessageResponse

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
