package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CreateInspectionBody
import com.example.smart_emap.data.model.CreateInspectionResponse
import com.example.smart_emap.data.model.InspectionListResponse
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
}
