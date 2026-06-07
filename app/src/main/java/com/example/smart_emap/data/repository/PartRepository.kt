package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.PartStockUpdateBodyDto

data class PartStockFilters(
    val keyword: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val suppliers: List<String> = emptyList(),
    val orderOnly: Boolean = false,
)

data class PartStockStatsUi(
    val totalParts: Int = 0,
    val totalCurrentStock: Int = 0,
    val totalOrderQuantity: Int = 0,
    val totalOrderAmount: Double = 0.0,
)

class PartRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadStockSuppliers(): List<String> = runCatching {
        apiClient.partApi().stockSupplierNames().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadStockList(filters: PartStockFilters): List<PartStockItemDto> = runCatching {
        val supplierParam = filters.suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
        apiClient.partApi().listStock(
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
            suppliers = supplierParam,
            startDate = filters.startDate?.takeIf { it.isNotBlank() },
            endDate = filters.endDate?.takeIf { it.isNotBlank() },
            orderOnly = filters.orderOnly.takeIf { it },
        ).data?.list.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun updateStock(id: Int, body: PartStockUpdateBodyDto) {
        apiClient.partApi().updateStock(id, body)
    }

    suspend fun syncPartMaster() = apiClient.partApi().syncPartMaster()

    suspend fun calculateStock(startDate: String? = null, endDate: String? = null) =
        apiClient.partApi().calculateStock(
            buildMap {
                startDate?.takeIf { it.isNotBlank() }?.let { put("start_date", it) }
                endDate?.takeIf { it.isNotBlank() }?.let { put("end_date", it) }
            },
        )

    suspend fun generateStockData(startDate: String, endDate: String) =
        apiClient.partApiLong().generateStockData(
            com.example.smart_emap.data.model.DataGenerationBodyDto(
                start_date = startDate,
                end_date = endDate,
            ),
        )

    fun summarizeStock(rows: List<PartStockItemDto>): PartStockStatsUi {
        val distinctParts = rows.map { it.partCd.orEmpty() }.distinct().count { it.isNotBlank() }
        return PartStockStatsUi(
            totalParts = distinctParts,
            totalCurrentStock = rows.sumOf { it.currentStock ?: 0 },
            totalOrderQuantity = rows.sumOf { it.orderQuantity ?: 0 },
            totalOrderAmount = rows.sumOf { it.orderAmount ?: 0.0 },
        )
    }
}
