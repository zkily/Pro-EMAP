package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CreateWeldingBody
import com.example.smart_emap.data.model.CreateWeldingResponse
import com.example.smart_emap.data.model.PatchWeldingBody
import com.example.smart_emap.data.model.WeldingListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WeldingApiService {
    @GET("/api/plan/welding-management/list")
    suspend fun list(
        @Query("production_day") productionDay: String? = null,
        @Query("hide_completed") hideCompleted: Boolean? = null,
        @Query("limit") limit: Int? = null,
    ): WeldingListResponse

    @POST("/api/plan/welding-management")
    suspend fun create(@Body body: CreateWeldingBody): CreateWeldingResponse

    @PATCH("/api/plan/welding-management/{id}")
    suspend fun patch(
        @Path("id") id: Int,
        @Body body: PatchWeldingBody,
    ): ApiMessageResponse
}
