package com.example.smart_emap.data.repository



import com.example.smart_emap.core.network.ApiClient

import com.example.smart_emap.data.model.ConfirmActualResponse

import com.example.smart_emap.data.model.CreateChamferingManagementBody

import com.example.smart_emap.data.model.CreateChamferingPlanBody

import com.example.smart_emap.data.model.CreateInstructionPlanBody

import com.example.smart_emap.data.model.CreateNoteBody

import com.example.smart_emap.data.model.CuttingInstructionNoteDto

import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto

import com.example.smart_emap.data.model.InstructionChamferingPlanRowDto

import com.example.smart_emap.data.model.InstructionChamferingRowDto

import com.example.smart_emap.data.model.InstructionCuttingRowDto

import com.example.smart_emap.data.model.InstructionPlanRowDto

import com.example.smart_emap.data.model.KanbanIssuanceRowDto

import com.example.smart_emap.data.model.MaterialStockItemDto

import com.example.smart_emap.data.model.MaterialStockSubItemDto

import com.example.smart_emap.data.model.MaterialUsageCommitBody

import com.example.smart_emap.data.model.MoveChamferingPlanBody

import com.example.smart_emap.data.model.MoveCuttingToBatchBody

import com.example.smart_emap.data.model.MoveFromBatchBody

import com.example.smart_emap.data.model.PatchChamferingBody

import com.example.smart_emap.data.model.PatchChamferingPlanBody

import com.example.smart_emap.data.model.PatchInstructionCuttingBody

import com.example.smart_emap.data.model.PatchInstructionPlanBody

import com.example.smart_emap.data.model.PatchKanbanBody

import com.example.smart_emap.data.model.PatchNoteBody

import com.example.smart_emap.data.model.ProductBatchDetailDto

import com.example.smart_emap.data.model.ReorderChamferingBody

import com.example.smart_emap.data.model.ReorderCuttingBody

import com.example.smart_emap.data.model.SplitChamferingToNextDayBody

import com.example.smart_emap.data.model.SplitCuttingToNextDayBody

import com.example.smart_emap.data.model.UpdateChamferingPlanContentBody
import com.example.smart_emap.ui.mes.cuttinginstruction.MoldingPreInventoryGroup
import com.example.smart_emap.ui.mes.cuttinginstruction.buildMoldingPreInventoryGroups



class CuttingInstructionRepository(

    private val apiClient: ApiClient,

) {

    private suspend fun api() = apiClient.cuttingInstructionApi()

    private suspend fun master() = apiClient.masterApi()



    suspend fun loadCuttingMachines(): List<Pair<String, String>> = runCatching {

        master().listMachines(keyword = "切断", pageSize = 9999).items()

            .mapNotNull { m ->

                val name = m.machineName.orEmpty()

                if (name.isBlank()) null else name to name

            }

            .distinctBy { it.first }

    }.getOrElse { emptyList() }



    suspend fun loadChamferingMachines(): List<Pair<String, String>> = runCatching {

        master().listMachines(keyword = "面取", pageSize = 9999).items()

            .filter { m ->

                val name = m.machineName.orEmpty()

                name.contains("面取") && !name.contains("外注")

            }

            .mapNotNull { m ->

                val name = m.machineName.orEmpty()

                if (name.isBlank()) null else name to name

            }

            .distinctBy { it.first }

    }.getOrElse { emptyList() }



    suspend fun loadBatchPlans(

        equipment: String?,

        productionMonth: String? = null,

    ): List<InstructionPlanRowDto> = runCatching {

        val res = api().listBatchPlans(

            equipment = equipment?.takeIf { it.isNotBlank() },

            productionMonth = productionMonth?.takeIf { it.isNotBlank() },

        )

        if (res.success == false) throw IllegalStateException(res.message ?: "計画データの読み込みに失敗しました")

        res.data.orEmpty()

    }.getOrElse { throw it }



    suspend fun createBatchPlan(body: CreateInstructionPlanBody) {

        val res = api().createBatchPlan(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "追加に失敗しました")

    }



    suspend fun syncLengthsFromProducts(): String = runCatching {

        val res = api().syncLengthsFromProducts()

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "同期に失敗しました")

        res.message ?: "寸法マスタを同期しました"

    }.getOrElse { throw it }



    suspend fun patchBatchPlan(id: Int, body: PatchInstructionPlanBody) {

        val res = api().patchBatchPlan(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun deleteBatchPlan(id: Int) {

        val res = api().deleteBatchPlan(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")

    }



    suspend fun loadCuttingManagement(

        productionDay: String?,

        cuttingMachine: String?,

    ): List<InstructionCuttingRowDto> = runCatching {

        val res = api().listCuttingManagement(

            productionDay = productionDay?.takeIf { it.isNotBlank() },

            cuttingMachine = cuttingMachine?.takeIf { it.isNotBlank() },

        )

        if (res.success == false) throw IllegalStateException(res.message ?: "切断指示の取得に失敗しました")

        res.data.orEmpty()

    }.getOrElse { throw it }



    suspend fun loadCuttingReferenceByMgmtCode(): Pair<Map<String, String>, Map<String, String>> = runCatching {
        val res = api().listCuttingManagement(productionDay = null, cuttingMachine = null, limit = 5000)
        if (res.success == false) throw IllegalStateException(res.message ?: "切断指示の取得に失敗しました")
        val productionDayMap = linkedMapOf<String, String>()
        val startDateMap = linkedMapOf<String, String>()
        for (row in res.data.orEmpty()) {
            val code = row.managementCode?.trim().orEmpty()
            if (code.isEmpty()) continue
            if (!startDateMap.containsKey(code)) {
                row.startDate?.trim()?.take(10)?.takeIf { it.isNotEmpty() }?.let { startDateMap[code] = it }
            }
            if (!productionDayMap.containsKey(code)) {
                row.productionDay?.trim()?.take(10)?.takeIf { it.isNotEmpty() }?.let { productionDayMap[code] = it }
            }
        }
        productionDayMap to startDateMap
    }.getOrElse { throw it }



    suspend fun movePlanToCutting(body: MoveFromBatchBody) {

        val res = api().moveFromBatch(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "移行に失敗しました")

    }



    suspend fun moveCuttingToBatch(body: MoveCuttingToBatchBody) {

        val res = api().moveFromCutting(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "ロットへ戻すのに失敗しました")

    }



    suspend fun patchCutting(id: Int, body: PatchInstructionCuttingBody) {

        val res = api().patchCuttingManagement(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun reorderCutting(body: ReorderCuttingBody) {

        val res = api().reorderCutting(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "並べ替えに失敗しました")

    }



    suspend fun splitCuttingToNextDay(id: Int, body: SplitCuttingToNextDayBody) {

        val res = api().splitCuttingToNextDay(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "分割に失敗しました")

    }



    suspend fun duplicateCutting(id: Int) {

        val res = api().duplicateCutting(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "複製に失敗しました")

    }



    suspend fun deleteCutting(id: Int) {

        val res = api().deleteCutting(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")

    }



    suspend fun confirmCuttingActual(productionDay: String, cuttingMachine: String?): ConfirmActualResponse {

        val res = api().confirmCuttingActual(productionDay, cuttingMachine?.takeIf { it.isNotBlank() })

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "実績確定に失敗しました")

        return res

    }



    suspend fun loadChamferingPlans(productionMonth: String? = null): List<InstructionChamferingPlanRowDto> = runCatching {

        api().listChamferingPlans(productionMonth = productionMonth?.takeIf { it.isNotBlank() }).data.orEmpty()

    }.getOrElse { emptyList() }



    suspend fun createChamferingPlan(body: CreateChamferingPlanBody) {

        val res = api().createChamferingPlan(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "登録に失敗しました")

    }



    suspend fun moveChamferingPlanToChamfering(body: MoveChamferingPlanBody) {

        val res = api().moveChamferingPlanToChamfering(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "面取指示へ移行に失敗しました")

    }



    suspend fun patchChamferingPlan(id: Int, body: PatchChamferingPlanBody) {

        val res = api().patchChamferingPlan(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun updateChamferingPlanContent(id: Int, body: UpdateChamferingPlanContentBody) {

        val res = api().updateChamferingPlanContent(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun copyChamferingPlan(id: Int) {

        val res = api().copyChamferingPlan(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "複製に失敗しました")

    }



    suspend fun deleteChamferingPlan(id: Int) {

        val res = api().deleteChamferingPlan(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")

    }



    suspend fun loadChamferingManagement(

        productionDay: String,

        chamferingMachine: String?,

    ): List<InstructionChamferingRowDto> = runCatching {

        val res = api().listChamferingManagement(

            productionDay = productionDay,

            chamferingMachine = chamferingMachine?.takeIf { it.isNotBlank() },

        )

        if (res.success == false) throw IllegalStateException(res.message ?: "面取指示の取得に失敗しました")

        res.data.orEmpty()

    }.getOrElse { throw it }



    suspend fun createChamferingManagement(body: CreateChamferingManagementBody) {

        val res = api().createChamferingManagement(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "登録に失敗しました")

    }



    suspend fun patchChamfering(id: Int, body: PatchChamferingBody) {

        val res = api().patchChamferingManagement(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun duplicateChamfering(id: Int) {

        val res = api().duplicateChamfering(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "複製に失敗しました")

    }



    suspend fun splitChamferingToNextDay(id: Int, body: SplitChamferingToNextDayBody) {

        val res = api().splitChamferingToNextDay(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "分割に失敗しました")

    }



    suspend fun deleteChamfering(id: Int) {

        val res = api().deleteChamfering(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")

    }



    suspend fun reorderChamfering(body: ReorderChamferingBody) {

        val res = api().reorderChamfering(body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "並べ替えに失敗しました")

    }



    suspend fun confirmChamferingActual(productionDay: String, chamferingMachine: String?): ConfirmActualResponse {

        val res = api().confirmChamferingActual(productionDay, chamferingMachine?.takeIf { it.isNotBlank() })

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "実績確定に失敗しました")

        return res

    }



    suspend fun loadKanbanList(

        productionDay: String?,

        status: String?,

        productName: String?,

    ): List<KanbanIssuanceRowDto> = runCatching {

        api().listKanbanIssuance(

            productionDay = productionDay?.takeIf { it.isNotBlank() },

            status = status?.takeIf { it.isNotBlank() },

            productName = productName?.takeIf { it.isNotBlank() },

        ).data.orEmpty()

    }.getOrElse { emptyList() }



    suspend fun loadKanbanProductNames(): List<String> = runCatching {

        api().listKanbanProductNames().data.orEmpty()

    }.getOrElse { emptyList() }



    suspend fun patchKanban(id: Int, body: PatchKanbanBody) {

        val res = api().patchKanban(id, body)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun issueKanban(id: Int) {

        val res = api().issueKanban(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "発行に失敗しました")

    }



    suspend fun reissueKanban(id: Int) {

        val res = api().reissueKanban(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "再発行に失敗しました")

    }



    suspend fun batchIssueKanban(ids: List<Int>) {

        val res = api().batchIssueKanban(com.example.smart_emap.data.model.KanbanBatchIssueBody(ids))

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "一括発行に失敗しました")

    }



    suspend fun syncKanbanProductionDay(): String {

        val res = api().syncKanbanProductionDay()

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "同期に失敗しました")

        return res.message ?: "生産日を同期しました"

    }



    suspend fun loadProductDetail(productCd: String): ProductBatchDetailDto? = runCatching {

        api().getProductBatchDetail(productCd).data

    }.getOrNull()



    suspend fun loadEquipmentEfficiency(productCd: String, keyword: String? = null): List<EquipmentEfficiencyRowDto> = runCatching {

        val res = api().listEquipmentEfficiency(keyword = keyword ?: productCd)

        res.items()

    }.getOrElse { emptyList() }

    suspend fun loadAllCuttingManagement(limit: Int = 10000): List<InstructionCuttingRowDto> = runCatching {
        val res = api().listCuttingManagement(productionDay = null, cuttingMachine = null, limit = limit)
        if (res.success == false) throw IllegalStateException(res.message ?: "切断指示の取得に失敗しました")
        res.data.orEmpty()
    }.getOrElse { throw it }

    suspend fun loadAllChamferingManagement(limit: Int = 10000): List<InstructionChamferingRowDto> = runCatching {
        val res = api().listChamferingManagement(productionDay = null, chamferingMachine = null, limit = limit)
        if (res.success == false) throw IllegalStateException(res.message ?: "面取指示の取得に失敗しました")
        res.data.orEmpty()
    }.getOrElse { throw it }

    suspend fun loadChamferingProductOptions(): List<Pair<String, String>> = runCatching {
        master().listProductsByProcess("KT02")
            .sortedBy { it.productName.orEmpty() }
            .map { it.productCd to (it.productName.orEmpty().ifBlank { it.productCd }) }
    }.getOrElse { emptyList() }

    suspend fun loadChamferingMaterialOptions(): List<String> =
        loadMaterialMasterOptions().map { it.first }

    suspend fun loadMaterialMasterOptions(): List<Pair<String, String>> = runCatching {
        master().listMaterials(pageSize = 500).items()
            .mapNotNull { m ->
                val name = m.materialName?.trim()?.takeIf { n -> n.isNotEmpty() } ?: return@mapNotNull null
                name to name
            }
            .distinctBy { it.first }
            .sortedBy { it.first }
    }.getOrElse { emptyList() }

    suspend fun loadMoldingPreInventoryGroups(refDate: String): List<MoldingPreInventoryGroup> {
        val date = refDate.trim().take(10)
        val summaryRows = apiClient.databaseApi().listProductionSummarys(
            page = 1,
            limit = 50000,
            startDate = date,
            endDate = date,
            excludeInactiveProducts = true,
        ).data?.list.orEmpty()
        val pmcRows = master().listProductMachineConfig().items()
        val moldingMachineByProduct = pmcRows.mapNotNull { row ->
            val cd = row.productCd.orEmpty().trim()
            if (cd.isBlank()) null else cd to row.moldingMachine.orEmpty().trim()
        }.toMap()
        val machines = master().listMachines(pageSize = 9999).items()
        val formingEff = master().listEquipmentEfficiencyMaster(processType = "forming", limit = 99999).items()
        val cuttingRows = loadAllCuttingManagement()
        return buildMoldingPreInventoryGroups(
            summaryRows = summaryRows,
            moldingMachineByProduct = moldingMachineByProduct,
            machines = machines,
            formingEfficiency = formingEff,
            cuttingRows = cuttingRows,
            refDate = date,
        )
    }



    suspend fun loadNotes(): List<CuttingInstructionNoteDto> = runCatching {

        api().listNotes().data.orEmpty()

    }.getOrElse { emptyList() }



    suspend fun createNote(content: String) {

        val res = api().createNote(CreateNoteBody(content))

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "追加に失敗しました")

    }



    suspend fun patchNote(id: Int, isDone: Boolean? = null, content: String? = null) {

        val res = api().patchNote(id, PatchNoteBody(isDone = isDone, content = content))

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "保存に失敗しました")

    }



    suspend fun deleteNote(id: Int) {

        val res = api().deleteNote(id)

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")

    }



    suspend fun commitMaterialUsage(todayDate: String, tomorrowDate: String): String {

        val res = api().commitMaterialUsage(

            MaterialUsageCommitBody(todayDate = todayDate, tomorrowDate = tomorrowDate),

        )

        if (res.success == false) throw IllegalStateException(res.message ?: res.detail ?: "反映に失敗しました")

        return res.message ?: "使用数を反映しました"

    }



    suspend fun isUsageReflected(date: String): Boolean = runCatching {

        api().getUsageReflected(date).reflected == true

    }.getOrDefault(false)



    suspend fun loadReflectedManagementCodes(date: String): Set<String> = runCatching {

        val res = api().getReflectedManagementCodes(date)

        (res.data ?: res.codes.orEmpty()).toSet()

    }.getOrDefault(emptySet())



    suspend fun loadMaterialStockForPrint(targetDate: String): List<MaterialStockItemDto> = runCatching {

        api().listMaterialStock(targetDate = targetDate).data?.list.orEmpty()

    }.getOrElse { emptyList() }



    suspend fun loadMaterialStockSubForPrint(): List<MaterialStockSubItemDto> {

        val all = mutableListOf<MaterialStockSubItemDto>()

        var page = 1

        repeat(20) {

            val res = api().listMaterialStockSub(page = page, pageSize = 500)

            val chunk = res.data?.list.orEmpty()

            all.addAll(chunk)

            val total = res.data?.total ?: chunk.size

            if (chunk.isEmpty() || all.size >= total) return all

            page++

        }

        return all

    }

}


