package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.data.model.MasterProcessDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto

class ApsSchedulingRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadProcesses(): List<MasterProcessDto> = runCatching {
        apiClient.masterApi().listProcesses(pageSize = 500).items()
    }.getOrElse { emptyList() }

    suspend fun loadLines(processCd: String?): List<ApsProductionLineDto> = runCatching {
        val pc = processCd?.trim().orEmpty().ifBlank { null }
        apiClient.apsApiLong().listLines(processCd = pc)
    }.getOrElse { emptyList() }

    suspend fun loadSchedulingGrid(
        startDate: String,
        endDate: String,
        lineId: Int?,
        processCd: String?,
    ): SchedulingGridResponseDto = runCatching {
        val pc = processCd?.trim().orEmpty().ifBlank { null }
        apiClient.apsApiLong().getSchedulingGrid(
            startDate = startDate,
            endDate = endDate,
            lineId = lineId,
            processCd = pc,
        )
    }.getOrElse {
        SchedulingGridResponseDto()
    }
}
