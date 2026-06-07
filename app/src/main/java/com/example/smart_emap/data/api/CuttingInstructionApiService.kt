package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.ApiMessageResponse
import com.example.smart_emap.data.model.ConfirmActualResponse
import com.example.smart_emap.data.model.CreateChamferingManagementBody
import com.example.smart_emap.data.model.CreateChamferingPlanBody
import com.example.smart_emap.data.model.CreateInstructionPlanBody
import com.example.smart_emap.data.model.CreateNoteBody
import com.example.smart_emap.data.model.CuttingInstructionNoteDto
import com.example.smart_emap.data.model.CuttingInstructionNotesResponse
import com.example.smart_emap.data.model.EquipmentEfficiencyListResponse
import com.example.smart_emap.data.model.InstructionChamferingPlanRowDto
import com.example.smart_emap.data.model.InstructionChamferingRowDto
import com.example.smart_emap.data.model.InstructionCuttingRowDto
import com.example.smart_emap.data.model.InstructionPlanRowDto
import com.example.smart_emap.data.model.KanbanBatchIssueBody
import com.example.smart_emap.data.model.KanbanIssuanceRowDto
import com.example.smart_emap.data.model.KanbanProductNamesResponse
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import com.example.smart_emap.data.model.MaterialUsageCommitBody
import com.example.smart_emap.data.model.MaterialUsageReflectedCodesResponse
import com.example.smart_emap.data.model.MaterialUsageReflectedResponse
import com.example.smart_emap.data.model.MoveChamferingPlanBody
import com.example.smart_emap.data.model.MoveCuttingToBatchBody
import com.example.smart_emap.data.model.MoveFromBatchBody
import com.example.smart_emap.data.model.PagedListDto
import com.example.smart_emap.data.model.PatchChamferingBody
import com.example.smart_emap.data.model.PatchChamferingPlanBody
import com.example.smart_emap.data.model.PatchInstructionCuttingBody
import com.example.smart_emap.data.model.PatchInstructionPlanBody
import com.example.smart_emap.data.model.PatchKanbanBody
import com.example.smart_emap.data.model.PatchNoteBody
import com.example.smart_emap.data.model.PlanListResponse
import com.example.smart_emap.data.model.ProductBatchDetailResponse
import com.example.smart_emap.data.model.ReorderChamferingBody
import com.example.smart_emap.data.model.ReorderCuttingBody
import com.example.smart_emap.data.model.SplitChamferingToNextDayBody
import com.example.smart_emap.data.model.SplitCuttingToNextDayBody
import com.example.smart_emap.data.model.UpdateChamferingPlanContentBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CuttingInstructionApiService {
    @GET("/api/plan/batch/list")
    suspend fun listBatchPlans(
        @Query("equipment") equipment: String? = null,
        @Query("production_month") productionMonth: String? = null,
        @Query("limit") limit: Int = 5000,
    ): PlanListResponse<InstructionPlanRowDto>

    @POST("/api/plan/batch/create")
    suspend fun createBatchPlan(@Body body: CreateInstructionPlanBody): ApiMessageResponse

    @PATCH("/api/plan/batch/{id}")
    suspend fun patchBatchPlan(
        @Path("id") id: Int,
        @Body body: PatchInstructionPlanBody,
    ): ApiMessageResponse

    @DELETE("/api/plan/batch/{id}")
    suspend fun deleteBatchPlan(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/batch/sync-lengths-from-products")
    suspend fun syncLengthsFromProducts(): ApiMessageResponse

    @POST("/api/plan/batch/move-from-cutting")
    suspend fun moveFromCutting(@Body body: MoveCuttingToBatchBody): ApiMessageResponse

    @GET("/api/plan/cutting-management/list")
    suspend fun listCuttingManagement(
        @Query("production_day") productionDay: String? = null,
        @Query("cutting_machine") cuttingMachine: String? = null,
        @Query("limit") limit: Int = 2000,
    ): PlanListResponse<InstructionCuttingRowDto>

    @PATCH("/api/plan/cutting-management/{id}")
    suspend fun patchCuttingManagement(
        @Path("id") id: Int,
        @Body body: PatchInstructionCuttingBody,
    ): ApiMessageResponse

    @POST("/api/plan/cutting-management/move-from-batch")
    suspend fun moveFromBatch(@Body body: MoveFromBatchBody): ApiMessageResponse

    @POST("/api/plan/cutting-management/reorder")
    suspend fun reorderCutting(@Body body: ReorderCuttingBody): ApiMessageResponse

    @POST("/api/plan/cutting-management/{id}/duplicate")
    suspend fun duplicateCutting(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/cutting-management/{id}/split-to-next-day")
    suspend fun splitCuttingToNextDay(
        @Path("id") id: Int,
        @Body body: SplitCuttingToNextDayBody,
    ): ApiMessageResponse

    @DELETE("/api/plan/cutting-management/{id}")
    suspend fun deleteCutting(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/cutting-management/confirm-actual")
    suspend fun confirmCuttingActual(
        @Query("production_day") productionDay: String,
        @Query("cutting_machine") cuttingMachine: String? = null,
    ): ConfirmActualResponse

    @GET("/api/plan/chamfering-plans/list")
    suspend fun listChamferingPlans(
        @Query("production_month") productionMonth: String? = null,
        @Query("limit") limit: Int = 5000,
    ): PlanListResponse<InstructionChamferingPlanRowDto>

    @POST("/api/plan/chamfering-plans")
    suspend fun createChamferingPlan(@Body body: CreateChamferingPlanBody): ApiMessageResponse

    @POST("/api/plan/chamfering-plans/move-to-chamfering")
    suspend fun moveChamferingPlanToChamfering(@Body body: MoveChamferingPlanBody): ApiMessageResponse

    @PATCH("/api/plan/chamfering-plans/{id}")
    suspend fun patchChamferingPlan(
        @Path("id") id: Int,
        @Body body: PatchChamferingPlanBody,
    ): ApiMessageResponse

    @PUT("/api/plan/chamfering-plans/{id}/content")
    suspend fun updateChamferingPlanContent(
        @Path("id") id: Int,
        @Body body: UpdateChamferingPlanContentBody,
    ): ApiMessageResponse

    @POST("/api/plan/chamfering-plans/{id}/copy")
    suspend fun copyChamferingPlan(@Path("id") id: Int): ApiMessageResponse

    @DELETE("/api/plan/chamfering-plans/{id}")
    suspend fun deleteChamferingPlan(@Path("id") id: Int): ApiMessageResponse

    @GET("/api/plan/chamfering-management/list")
    suspend fun listChamferingManagement(
        @Query("production_day") productionDay: String? = null,
        @Query("chamfering_machine") chamferingMachine: String? = null,
        @Query("limit") limit: Int = 2000,
    ): PlanListResponse<InstructionChamferingRowDto>

    @POST("/api/plan/chamfering-management")
    suspend fun createChamferingManagement(@Body body: CreateChamferingManagementBody): ApiMessageResponse

    @PATCH("/api/plan/chamfering-management/{id}")
    suspend fun patchChamferingManagement(
        @Path("id") id: Int,
        @Body body: PatchChamferingBody,
    ): ApiMessageResponse

    @POST("/api/plan/chamfering-management/{id}/duplicate")
    suspend fun duplicateChamfering(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/chamfering-management/{id}/split-to-next-day")
    suspend fun splitChamferingToNextDay(
        @Path("id") id: Int,
        @Body body: SplitChamferingToNextDayBody,
    ): ApiMessageResponse

    @DELETE("/api/plan/chamfering-management/{id}")
    suspend fun deleteChamfering(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/chamfering-management/reorder")
    suspend fun reorderChamfering(@Body body: ReorderChamferingBody): ApiMessageResponse

    @POST("/api/plan/chamfering-management/confirm-actual")
    suspend fun confirmChamferingActual(
        @Query("production_day") productionDay: String,
        @Query("chamfering_machine") chamferingMachine: String? = null,
    ): ConfirmActualResponse

    @GET("/api/plan/kanban-issuance/list")
    suspend fun listKanbanIssuance(
        @Query("production_day") productionDay: String? = null,
        @Query("status") status: String? = null,
        @Query("product_name") productName: String? = null,
        @Query("limit") limit: Int = 2000,
    ): PlanListResponse<KanbanIssuanceRowDto>

    @GET("/api/plan/kanban-issuance/product-names")
    suspend fun listKanbanProductNames(
        @Query("limit") limit: Int = 500,
    ): KanbanProductNamesResponse

    @PATCH("/api/plan/kanban-issuance/{id}")
    suspend fun patchKanban(
        @Path("id") id: Int,
        @Body body: PatchKanbanBody,
    ): ApiMessageResponse

    @POST("/api/plan/kanban-issuance/{id}/issue")
    suspend fun issueKanban(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/kanban-issuance/{id}/reissue")
    suspend fun reissueKanban(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/plan/kanban-issuance/batch-issue")
    suspend fun batchIssueKanban(@Body body: KanbanBatchIssueBody): ApiMessageResponse

    @POST("/api/plan/kanban-issuance/sync-production-day")
    suspend fun syncKanbanProductionDay(): ApiMessageResponse

    @GET("/api/master/products/batch-detail/{productCd}")
    suspend fun getProductBatchDetail(
        @Path("productCd") productCd: String,
    ): ProductBatchDetailResponse

    @GET("/api/master/equipment-efficiency")
    suspend fun listEquipmentEfficiency(
        @Query("keyword") keyword: String? = null,
        @Query("limit") limit: Int = 9999,
    ): EquipmentEfficiencyListResponse

    @GET("/api/plan/cutting-instruction-notes")
    suspend fun listNotes(
        @Query("limit") limit: Int = 200,
    ): CuttingInstructionNotesResponse

    @POST("/api/plan/cutting-instruction-notes")
    suspend fun createNote(@Body body: CreateNoteBody): ApiMessageResponse

    @PATCH("/api/plan/cutting-instruction-notes/{id}")
    suspend fun patchNote(
        @Path("id") id: Int,
        @Body body: PatchNoteBody,
    ): ApiMessageResponse

    @DELETE("/api/plan/cutting-instruction-notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): ApiMessageResponse

    @POST("/api/material/usage/commit")
    suspend fun commitMaterialUsage(@Body body: MaterialUsageCommitBody): ApiMessageResponse

    @GET("/api/material/usage/reflected")
    suspend fun getUsageReflected(
        @Query("date") date: String,
    ): MaterialUsageReflectedResponse

    @GET("/api/material/usage/reflected-management-codes")
    suspend fun getReflectedManagementCodes(
        @Query("date") date: String,
    ): MaterialUsageReflectedCodesResponse

    @GET("/api/material/stock")
    suspend fun listMaterialStock(
        @Query("target_date") targetDate: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10000,
    ): ApiEnvelope<PagedListDto<MaterialStockItemDto>>

    @GET("/api/material/stock/sub")
    suspend fun listMaterialStockSub(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
    ): ApiEnvelope<PagedListDto<MaterialStockSubItemDto>>
}
