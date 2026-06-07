package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.ChamferingListResponse
import com.example.smart_emap.data.model.PatchChamferingBody
import com.example.smart_emap.data.model.ReorderChamferingBody
import com.example.smart_emap.data.model.SplitChamferingToNextDayBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChamferingApiService {
    @GET("/api/plan/chamfering-management/list")
    suspend fun list(
        @Query("production_day") productionDay: String? = null,
        @Query("chamfering_machine") chamferingMachine: String? = null,
        @Query("limit") limit: Int? = null,
    ): ChamferingListResponse

    @PATCH("/api/plan/chamfering-management/{id}")
    suspend fun patch(
        @Path("id") id: Int,
        @Body body: PatchChamferingBody,
    ): ApiMessageResponse

    @POST("/api/plan/chamfering-management/reorder")
    suspend fun reorder(@Body body: ReorderChamferingBody): ApiMessageResponse

    @POST("/api/plan/chamfering-management/{id}/split-to-next-day")
    suspend fun splitToNextDay(
        @Path("id") id: Int,
        @Body body: SplitChamferingToNextDayBody,
    ): ApiMessageResponse
}
