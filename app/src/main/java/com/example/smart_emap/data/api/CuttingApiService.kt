package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.CuttingListResponse
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
}
