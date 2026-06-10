package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.PlanBaselineFullComparisonResultDto
import com.example.smart_emap.data.model.PlanBaselineGenerateBody
import com.example.smart_emap.data.model.PlanBaselinePlanQuantityBody
import com.example.smart_emap.data.model.PlanBaselineRecordDto
import com.example.smart_emap.data.model.PlanOperationRateRowDto
import com.example.smart_emap.data.model.PlanUpdateRecordDto

class PlanBaselineRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadComparison(baselineMonth: String, processName: String?): PlanBaselineFullComparisonResultDto? =
        runCatching {
            apiClient.planBaselineApi().loadComparison(
                baselineMonth = baselineMonth,
                processName = processName?.trim()?.ifBlank { null },
            )
        }.getOrNull()

    suspend fun generate(
        baselineMonth: String,
        processName: String?,
        weekdayBaseline: Double? = null,
        saturdayBaseline: Double? = null,
        sundayBaseline: Double? = null,
    ) {
        apiClient.planBaselineApi().generate(
            PlanBaselineGenerateBody(
                baselineMonth = baselineMonth,
                processName = processName?.trim()?.ifBlank { null },
                weekdayBaseline = weekdayBaseline,
                saturdayBaseline = saturdayBaseline,
                sundayBaseline = sundayBaseline,
            ),
        )
    }

    suspend fun delete(baselineMonth: String, processName: String?) {
        apiClient.planBaselineApi().delete(
            baselineMonth = baselineMonth,
            processName = processName?.trim()?.ifBlank { null },
        )
    }

    suspend fun loadRecords(baselineMonth: String, processName: String?): List<PlanBaselineRecordDto> =
        runCatching {
            apiClient.planBaselineApi().loadRecords(
                baselineMonth = baselineMonth,
                processName = processName?.trim()?.ifBlank { null },
            )
        }.getOrElse { emptyList() }

    suspend fun updatePlanQuantity(
        baselineMonth: String,
        planDate: String,
        processName: String?,
        planQuantity: Double,
    ) {
        apiClient.planBaselineApi().updatePlanQuantity(
            PlanBaselinePlanQuantityBody(
                baselineMonth = baselineMonth,
                planDate = planDate,
                processName = processName?.trim()?.ifBlank { null },
                planQuantity = planQuantity,
            ),
        )
    }

    suspend fun deleteRecord(baselineMonth: String, planDate: String, processName: String?) {
        apiClient.planBaselineApi().deleteRecord(
            baselineMonth = baselineMonth,
            planDate = planDate,
            processName = processName?.trim()?.ifBlank { null },
        )
    }

    suspend fun loadPlanOperationRate(monthNum: Int?, processName: String?): List<PlanOperationRateRowDto> =
        runCatching {
            apiClient.planBaselineApi().loadPlanOperationRate(monthNum, processName?.trim()?.ifBlank { null }).items.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadPlanData(
        startDate: String,
        endDate: String,
        productNameExact: String,
        processName: String?,
    ): List<PlanUpdateRecordDto> = runCatching {
        val res = apiClient.planDataApi().listPlanData(
            startDate = startDate,
            endDate = endDate,
            productNameExact = productNameExact.trim().ifBlank { null },
            processName = processName?.trim()?.ifBlank { null },
        )
        res.records()
    }.getOrElse { emptyList() }
}
