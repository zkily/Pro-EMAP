package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ComponentRequirementsBundleDataDto
import com.example.smart_emap.data.model.MaterialRequirementsDataDto

class ProductionRequirementsRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadMaterialRequirements(
        dateStart: String,
        dateEnd: String,
    ): MaterialRequirementsDataDto {
        val res = apiClient.productionRequirementsApi()
            .materialRequirementsSummary(dateStart, dateEnd)
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "集計に失敗しました")
        }
        return res.data ?: MaterialRequirementsDataDto()
    }

    suspend fun loadComponentRequirements(
        dateStart: String,
        dateEnd: String,
        planColumn: String,
    ): ComponentRequirementsBundleDataDto {
        val res = apiClient.productionRequirementsApi()
            .componentRequirementsBundle(dateStart, dateEnd, planColumn = planColumn)
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "集計に失敗しました")
        }
        return res.data ?: ComponentRequirementsBundleDataDto()
    }
}
