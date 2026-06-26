package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class CuttingProductionIndicatorLinesResponse(
    val success: Boolean? = null,
    val data: List<CuttingProductionIndicatorLineOption>? = null,
    val message: String? = null,
)

data class CuttingProductionIndicatorLineOption(
    @Json(name = "line_name") val lineName: String,
)

/** cutting API は welding と同じ集計 JSON 構造を返す */
typealias CuttingProductivityAnalysisResponse = WeldingProductivityAnalysisResponse
typealias CuttingProductivityAnalysisDataDto = WeldingProductivityAnalysisDataDto
typealias CuttingProductivityBucketDto = WeldingProductivityBucketDto
typealias CuttingProductivityDailyRowDto = WeldingProductivityDailyRowDto
typealias CuttingProductivityOperatorRowDto = WeldingProductivityOperatorRowDto
typealias CuttingProductivityProductRowDto = WeldingProductivityProductRowDto
typealias CuttingProductivityProductRankingDto = WeldingProductivityProductRankingDto
typealias CuttingProductivityDefectRowDto = WeldingProductivityDefectRowDto
typealias CuttingProductivitySessionRowDto = WeldingProductivitySessionRowDto
