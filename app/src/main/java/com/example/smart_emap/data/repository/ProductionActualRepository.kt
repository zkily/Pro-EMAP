package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.MaterialUsageChartDataDto
import com.example.smart_emap.data.model.MaterialUsageRecordDto
import com.example.smart_emap.data.model.MaterialUsageRecordsDataDto
import com.example.smart_emap.data.model.ProductionActualDataDto
import com.example.smart_emap.data.model.ProductionActualLogDto
import com.example.smart_emap.data.model.ProductionActualUpdateBody
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.model.QualityRateByProcessDataDto
import com.example.smart_emap.data.model.QualityRateByProductDataDto

/** 工程マスタ選択肢 */
data class ProcessOption(
    val processCd: String,
    val processName: String,
)

class ProductionActualRepository(
    private val apiClient: ApiClient,
) {
    // ── 材料消費実績 ──
    suspend fun loadMaterialUsageRecords(
        page: Int,
        pageSize: Int,
        materialCd: String?,
        dateFrom: String?,
        dateTo: String?,
    ): MaterialUsageRecordsDataDto {
        val res = apiClient.materialApiLong().usageRecords(
            page = page,
            pageSize = pageSize,
            materialCd = materialCd?.takeIf { it.isNotBlank() },
            dateFrom = dateFrom?.takeIf { it.isNotBlank() },
            dateTo = dateTo?.takeIf { it.isNotBlank() },
        )
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "一覧の取得に失敗しました")
        }
        return res.data ?: MaterialUsageRecordsDataDto(emptyList(), 0)
    }

    suspend fun loadMaterialUsageChart(
        materialCd: String?,
        dateFrom: String?,
        dateTo: String?,
    ): MaterialUsageChartDataDto {
        val res = apiClient.materialApiLong().usageRecordsChartSummary(
            materialCd = materialCd?.takeIf { it.isNotBlank() },
            dateFrom = dateFrom?.takeIf { it.isNotBlank() },
            dateTo = dateTo?.takeIf { it.isNotBlank() },
        )
        return res.data ?: MaterialUsageChartDataDto(emptyList(), emptyList())
    }

    suspend fun loadMaterialOptions(): List<ProcessOption> {
        val res = apiClient.materialApi().listMasterMaterials(pageSize = 9999)
        return res.data?.list.orEmpty().mapNotNull { m ->
            val cd = m.materialCd ?: return@mapNotNull null
            ProcessOption(cd, m.materialName ?: cd)
        }
    }

    // ── 工程別実績 ──
    suspend fun loadProductionActualLogs(
        dateFrom: String,
        dateTo: String,
        processCd: String?,
    ): List<ProductionActualLogDto> {
        val res = apiClient.productionActualLogsApi().list(
            page = 1,
            limit = 10000,
            transactionType = "実績",
            dateFrom = dateFrom,
            dateTo = dateTo,
            processCd = processCd?.takeIf { it.isNotBlank() },
        )
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "実績データの取得に失敗しました")
        }
        return res.data?.list.orEmpty()
    }

    suspend fun loadProcessMaster(): List<ProcessOption> {
        val res = apiClient.masterApi().listProcesses(pageSize = 5000)
        return res.items().mapNotNull { p ->
            val cd = p.processCd ?: return@mapNotNull null
            ProcessOption(cd, p.processName ?: cd)
        }
    }

    // ── 廃棄率分析 ──
    suspend fun loadQualityRateByProcess(
        startDate: String,
        endDate: String,
        process: String?,
        includeAmounts: Boolean,
    ): QualityRateByProcessDataDto {
        val res = apiClient.databaseApiLong().qualityRateByProcess(
            startDate = startDate,
            endDate = endDate,
            process = process?.takeIf { it.isNotBlank() },
            includeAmounts = includeAmounts,
        )
        return res.data ?: QualityRateByProcessDataDto()
    }

    suspend fun loadQualityRateByProduct(
        startDate: String,
        endDate: String,
        page: Int,
        limit: Int,
        productCd: String?,
        sortBy: String,
        sortOrder: String,
    ): QualityRateByProductDataDto {
        val res = apiClient.databaseApiLong().qualityRateByProduct(
            startDate = startDate,
            endDate = endDate,
            page = page,
            limit = limit,
            productCd = productCd?.takeIf { it.isNotBlank() },
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
        return res.data ?: QualityRateByProductDataDto()
    }

    suspend fun loadProductionSummaryProducts(): List<ProductionSummaryProductOptionDto> {
        val res = apiClient.productionSummaryApi().listProducts()
        return res.data.orEmpty()
    }

    // ── 生産実績管理（在庫取引ログ：一覧＋集計、更新、削除） ──
    suspend fun loadStockActualLogs(
        page: Int,
        limit: Int,
        processCd: String?,
        transactionType: String?,
        targetName: String?,
        machineName: String?,
        dateFrom: String?,
        dateTo: String?,
        sortBy: String?,
        sortOrder: String?,
    ): ProductionActualDataDto {
        val res = apiClient.productionActualLogsApi().list(
            page = page,
            limit = limit,
            transactionType = transactionType?.takeIf { it.isNotBlank() },
            dateFrom = dateFrom?.takeIf { it.isNotBlank() },
            dateTo = dateTo?.takeIf { it.isNotBlank() },
            processCd = processCd?.takeIf { it.isNotBlank() },
            targetName = targetName?.takeIf { it.isNotBlank() },
            machineName = machineName?.takeIf { it.isNotBlank() },
            sortBy = sortBy?.takeIf { it.isNotBlank() },
            sortOrder = sortOrder?.takeIf { it.isNotBlank() },
        )
        if (res.success == false) {
            throw IllegalStateException(res.message ?: "実績データの取得に失敗しました")
        }
        return res.data ?: ProductionActualDataDto()
    }

    suspend fun updateStockActualLog(id: Int, body: ProductionActualUpdateBody) {
        apiClient.productionActualLogsApi().updateLog(id, body)
    }

    suspend fun deleteStockActualLog(id: Int) {
        apiClient.productionActualLogsApi().deleteLog(id)
    }
}
