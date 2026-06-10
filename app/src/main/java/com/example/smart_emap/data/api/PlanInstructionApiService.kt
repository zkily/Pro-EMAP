package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.LineCapacityDto
import com.example.smart_emap.data.model.MachineWorkTimeConfigBody
import com.example.smart_emap.data.model.MachineWorkTimeConfigDto
import com.example.smart_emap.data.model.PlanBaselineComparisonResultDto
import com.example.smart_emap.data.model.PlanInstructionEfficiencyUpdateBody
import com.example.smart_emap.data.model.PlanInstructionListResponse
import com.example.smart_emap.data.model.PlanInstructionNoteBody
import com.example.smart_emap.data.model.PlanInstructionNoteDto
import com.example.smart_emap.data.model.PlanInstructionNotePatchBody
import com.example.smart_emap.data.model.PlanInstructionNotesResponse
import com.example.smart_emap.data.model.PlanInstructionRemarksBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PlanInstructionApiService {
    @GET("/api/mes/forming-plan-data")
    suspend fun loadPlanData(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("processName") processName: String,
        @Query("machineName") machineName: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10000,
    ): PlanInstructionListResponse

    @GET("/api/plan/{notesPath}")
    suspend fun loadNotes(
        @Path("notesPath") notesPath: String,
        @Query("limit") limit: Int = 200,
    ): PlanInstructionNotesResponse

    @POST("/api/plan/{notesPath}")
    suspend fun createNote(
        @Path("notesPath") notesPath: String,
        @Body body: PlanInstructionNoteBody,
    ): ApiEnvelope<PlanInstructionNoteDto>

    @PATCH("/api/plan/{notesPath}/{noteId}")
    suspend fun patchNote(
        @Path("notesPath") notesPath: String,
        @Path("noteId") noteId: Int,
        @Body body: PlanInstructionNotePatchBody,
    ): ApiEnvelope<PlanInstructionNoteDto>

    @DELETE("/api/plan/{notesPath}/{noteId}")
    suspend fun deleteNote(
        @Path("notesPath") notesPath: String,
        @Path("noteId") noteId: Int,
    ): ApiMessageResponse

    @PUT("/api/excel-monitor/plan-data/remarks")
    suspend fun saveRemarks(@Body body: PlanInstructionRemarksBody): ApiEnvelope<Unit>

    @POST("/api/excel-monitor/update-efficiency-and-setup-time")
    suspend fun updateEfficiencyAndSetupTime(
        @Body body: PlanInstructionEfficiencyUpdateBody,
    ): ApiEnvelope<Unit>

    @GET("/api/aps/line-capacities")
    suspend fun listLineCapacities(
        @Query("lineId") lineId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): List<LineCapacityDto>

    @GET("/api/plan-baseline/comparison")
    suspend fun loadPlanBaselineComparison(
        @Query("baselineMonth") baselineMonth: String,
        @Query("processName") processName: String,
        @Query("workingDays") workingDays: Int? = null,
    ): PlanBaselineComparisonResultDto

    @GET("/api/machine-work-time-config")
    suspend fun listMachineWorkTimeConfigs(
        @Query("machine_cd") machineCd: String? = null,
    ): List<MachineWorkTimeConfigDto>

    @POST("/api/machine-work-time-config")
    suspend fun createMachineWorkTimeConfig(
        @Body body: MachineWorkTimeConfigBody,
    ): ApiEnvelope<MachineWorkTimeConfigDto>

    @DELETE("/api/machine-work-time-config/{id}")
    suspend fun deleteMachineWorkTimeConfig(@Path("id") id: Int): ApiEnvelope<Unit>
}
