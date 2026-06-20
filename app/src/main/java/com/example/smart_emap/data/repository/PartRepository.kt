package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.DataGenerationResultDto
import com.example.smart_emap.data.model.MasterPartDto
import com.example.smart_emap.data.model.PartLogItemDto
import com.example.smart_emap.data.model.PartOrderPrintRow
import com.example.smart_emap.data.model.PartStockCreateBodyDto
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.PartStockUpdateBodyDto
import com.example.smart_emap.data.model.toPrintRow

data class PartStockFilters(
    val keyword: String? = null,
    val partCd: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val targetDate: String? = null,
    val suppliers: List<String> = emptyList(),
    val orderOnly: Boolean = false,
)

data class PartReceivingFilters(
    val keyword: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val suppliers: List<String> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class PartStockStatsUi(
    val totalParts: Int = 0,
    val totalCurrentStock: Int = 0,
    val averageUnitPrice: Double = 0.0,
    val totalUsageQuantity: Int = 0,
    val totalOrderQuantity: Int = 0,
    val totalOrderValue: Double = 0.0,
)

class PartRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadStockSuppliers(): List<String> = runCatching {
        apiClient.partApi().stockSupplierNames().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadReceivingSuppliers(): List<String> = runCatching {
        apiClient.partApi().receivingSuppliers().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadReceivingList(filters: PartReceivingFilters): Pair<List<PartLogItemDto>, Int> =
        runCatching {
            val supplierParam = filters.suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
            val resp = apiClient.partApi().listReceiving(
                page = filters.page,
                pageSize = filters.pageSize,
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplier = supplierParam,
            )
            val page = resp.data
            (page?.list.orEmpty()) to (page?.total ?: 0)
        }.getOrElse { emptyList<PartLogItemDto>() to 0 }

    suspend fun importReceivingCsv(): String = runCatching {
        val resp = apiClient.partApi().importReceivingCsv(emptyList())
        resp.message ?: "データ読取が完了しました"
    }.getOrElse { throw it }

    suspend fun loadReceivingListAll(filters: PartReceivingFilters, totalCount: Int): List<PartLogItemDto> =
        loadReceivingList(filters.copy(page = 1, pageSize = maxOf(totalCount, 100))).first

    suspend fun loadStockList(filters: PartStockFilters): List<PartStockItemDto> = runCatching {
        val supplierParam = filters.suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
        apiClient.partApi().listStock(
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
            partCd = filters.partCd?.takeIf { it.isNotBlank() },
            suppliers = supplierParam,
            startDate = filters.startDate?.takeIf { it.isNotBlank() },
            endDate = filters.endDate?.takeIf { it.isNotBlank() },
            targetDate = filters.targetDate?.takeIf { it.isNotBlank() },
            orderOnly = filters.orderOnly.takeIf { it },
        ).data?.list.orEmpty()
            .filter { (it.partMasterStatus ?: 1) != 0 }
    }.getOrElse { emptyList() }

    suspend fun updateStock(id: Int, body: PartStockUpdateBodyDto) {
        apiClient.partApi().updateStock(id, body)
    }

    suspend fun createStock(body: PartStockCreateBodyDto) {
        apiClient.partApi().createStock(body)
    }

    suspend fun syncPartMaster(startDate: String, endDate: String): Int {
        val response = apiClient.partApi().syncPartMaster(
            mapOf(
                "start_date" to startDate,
                "end_date" to endDate,
            ),
        )
        return response.data?.updated_count ?: 0
    }

    suspend fun calculateStock() = apiClient.partApi().calculateStock()

    suspend fun generateStockData(startDate: String, endDate: String): DataGenerationResultDto {
        val response = apiClient.partApiLong().generateStockData(
            com.example.smart_emap.data.model.DataGenerationBodyDto(
                start_date = startDate,
                end_date = endDate,
                overwrite_existing = false,
            ),
        )
        return response.data ?: DataGenerationResultDto()
    }

    suspend fun loadMasterParts(keyword: String? = null): List<MasterPartDto> = runCatching {
        apiClient.masterApi().listParts(
            keyword = keyword?.takeIf { it.isNotBlank() },
            status = 1,
            pageSize = 9999,
        ).items()
    }.getOrElse { emptyList() }

    suspend fun loadPrintOrderRows(
        startDate: String,
        stockItems: List<PartStockItemDto>,
    ): List<PartOrderPrintRow> {
        val printSuppliers = setOf("丸一NST", "丸一ﾒﾀﾙｱｸﾄ")
        return stockItems
            .filter { (it.orderQuantity ?: 0) > 0 && it.date == startDate && it.supplierName in printSuppliers }
            .map { it.toPrintRow() }
    }

    fun summarizeStock(rows: List<PartStockItemDto>): PartStockStatsUi {
        val distinctParts = rows.map { it.partCd.orEmpty() }.distinct().count { it.isNotBlank() }
        val orderRows = rows.filter { (it.orderQuantity ?: 0) > 0 }
        val avgPrice = if (rows.isEmpty()) 0.0 else rows.map { it.unitPrice ?: 0.0 }.average()
        return PartStockStatsUi(
            totalParts = if (distinctParts > 0) distinctParts else rows.size,
            totalCurrentStock = rows.sumOf { it.currentStock ?: 0 },
            averageUnitPrice = avgPrice,
            totalUsageQuantity = rows.sumOf { it.plannedUsage ?: 0 },
            totalOrderQuantity = orderRows.sumOf { it.orderQuantity ?: 0 },
            totalOrderValue = orderRows.sumOf { it.orderAmount ?: 0.0 },
        )
    }
}
