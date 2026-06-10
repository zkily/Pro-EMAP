package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.LineCapacityDto
import com.example.smart_emap.data.model.MachineWorkTimeConfigBody
import com.example.smart_emap.data.model.MachineWorkTimeConfigDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.data.model.PlanBaselineComparisonDto
import com.example.smart_emap.data.model.PlanBaselineComparisonItemDto
import com.example.smart_emap.data.model.PlanInstructionNoteDto
import com.example.smart_emap.data.model.PlanInstructionRecordDto
import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto
import com.example.smart_emap.ui.mes.planinstruction.PlanInstructionConfig

class PlanInstructionRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadPlanData(
        config: PlanInstructionConfig,
        startDate: String,
        endDate: String,
        machineName: String? = null,
        keyword: String? = null,
    ): List<PlanInstructionRecordDto> = runCatching {
        val response = apiClient.planInstructionApi().loadPlanData(
            startDate = startDate,
            endDate = endDate,
            processName = config.processName,
            machineName = machineName?.trim()?.ifEmpty { null },
            keyword = keyword?.trim()?.ifEmpty { null },
        )
        response.data?.records.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadMachines(machineType: String): List<MasterMachineFullDto> = runCatching {
        apiClient.masterApi().listMachines(machineType = machineType, pageSize = 9999).items()
    }.getOrElse { emptyList() }

    suspend fun resolveMachineCd(machineName: String): String {
        if (machineName.isBlank()) return "未指定"
        return runCatching {
            val list = apiClient.masterApi().listMachines(keyword = machineName, pageSize = 100).items()
            list.firstOrNull { it.machineName == machineName }?.machineCd
                ?: list.firstOrNull()?.machineCd
                ?: "未指定"
        }.getOrDefault("未指定")
    }

    suspend fun loadEquipmentEfficiency(): List<EquipmentEfficiencyRowDto> = runCatching {
        apiClient.cuttingInstructionApi().listEquipmentEfficiency(limit = 10000).items()
    }.getOrElse { emptyList() }

    suspend fun loadNotes(config: PlanInstructionConfig): List<PlanInstructionNoteDto> = runCatching {
        apiClient.planInstructionApi().loadNotes(config.notesApiPath).data?.list.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createNote(config: PlanInstructionConfig, content: String) {
        apiClient.planInstructionApi().createNote(config.notesApiPath, com.example.smart_emap.data.model.PlanInstructionNoteBody(content))
    }

    suspend fun toggleNoteDone(config: PlanInstructionConfig, noteId: Int, isDone: Boolean) {
        apiClient.planInstructionApi().patchNote(
            config.notesApiPath,
            noteId,
            com.example.smart_emap.data.model.PlanInstructionNotePatchBody(isDone),
        )
    }

    suspend fun deleteNote(config: PlanInstructionConfig, noteId: Int) {
        val res = apiClient.planInstructionApi().deleteNote(config.notesApiPath, noteId)
        if (res.success == false) {
            throw IllegalStateException(res.message ?: res.detail ?: "削除に失敗しました")
        }
    }

    suspend fun saveRemarks(
        config: PlanInstructionConfig,
        row: PlanInstructionRecordDto,
        remarks: String,
    ) {
        if (!config.persistRemarksToApi) return
        apiClient.planInstructionApi().saveRemarks(
            com.example.smart_emap.data.model.PlanInstructionRemarksBody(
                remarks = remarks,
                id = row.id,
                planDate = row.planDate,
                machineName = row.machineName,
                productCd = row.productCd,
                processName = row.processName ?: config.processName,
            ),
        )
    }

    suspend fun updateEfficiencyAndSetupTime(startDate: String) {
        apiClient.planInstructionApi().updateEfficiencyAndSetupTime(
            com.example.smart_emap.data.model.PlanInstructionEfficiencyUpdateBody(startDate),
        )
    }

    suspend fun loadLineCapacities(lineId: Int, date: String): List<LineCapacityDto> = runCatching {
        apiClient.planInstructionApi().listLineCapacities(lineId, date, date)
    }.getOrElse { emptyList() }

    suspend fun loadSchedulingGrid(
        config: PlanInstructionConfig,
        startDate: String,
        endDate: String,
    ): SchedulingGridResponseDto = runCatching {
        apiClient.apsApiLong().getSchedulingGrid(
            startDate = startDate,
            endDate = endDate,
            processCd = config.apsProcessCd,
        )
    }.getOrElse { SchedulingGridResponseDto() }

    suspend fun loadPlanBaselineComparison(
        config: PlanInstructionConfig,
        baselineMonth: String,
        workingDays: Int?,
    ): Pair<PlanBaselineComparisonDto?, List<PlanBaselineComparisonItemDto>> = runCatching {
        val res = apiClient.planInstructionApi()
            .loadPlanBaselineComparison(baselineMonth, config.processName, workingDays)
        val summary = res.summary ?: res.data
        summary to res.items.orEmpty()
    }.getOrElse { null to emptyList() }

    suspend fun loadApsLines(processCd: String): List<ApsProductionLineDto> = runCatching {
        apiClient.apsApi().listLines(processCd)
    }.getOrElse { emptyList() }

    suspend fun loadMachineWorkTimeConfigs(machineCd: String? = null): List<MachineWorkTimeConfigDto> =
        runCatching {
            apiClient.planInstructionApi().listMachineWorkTimeConfigs(machineCd)
        }.getOrElse { emptyList() }

    suspend fun createMachineWorkTimeConfig(body: MachineWorkTimeConfigBody) {
        apiClient.planInstructionApi().createMachineWorkTimeConfig(body)
    }

    suspend fun deleteMachineWorkTimeConfig(id: Int) {
        apiClient.planInstructionApi().deleteMachineWorkTimeConfig(id)
    }
}
