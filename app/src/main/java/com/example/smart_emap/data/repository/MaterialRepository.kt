package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.DataGenerationResultDto
import com.example.smart_emap.data.model.MaterialForecastDetailDto
import com.example.smart_emap.data.model.MaterialForecastStatsDto
import com.example.smart_emap.data.model.MaterialForecastSummaryDto
import com.example.smart_emap.data.model.MaterialLogItemDto
import com.example.smart_emap.data.model.MaterialMasterItemDto
import com.example.smart_emap.data.model.MaterialMasterUpdateBodyDto
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.MaterialOrderPrintRow
import com.example.smart_emap.data.model.MaterialStockSubCreateBodyDto
import com.example.smart_emap.data.model.toPrintRow
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import com.example.smart_emap.data.model.MaterialStockSubUpdateBodyDto
import com.example.smart_emap.data.model.MaterialStockUpdateBodyDto

data class MaterialReceivingFilters(
    val keyword: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val suppliers: List<String> = emptyList(),
)

data class MaterialStockFilters(
    val keyword: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val targetDate: String? = null,
    val suppliers: List<String> = emptyList(),
    val orderOnly: Boolean = false,
)

data class MaterialForecastFilters(
    val year: Int,
    val month: Int,
    val supplierCd: String? = null,
    val keyword: String? = null,
)

data class MaterialStockStatsUi(
    val totalMaterials: Int = 0,
    val totalCurrentStock: Int = 0,
    val averageUnitPrice: Double = 0.0,
    val totalUsageQuantity: Int = 0,
    val totalOrderQuantity: Int = 0,
    val totalOrderBundleQuantity: Int = 0,
    val totalBundleWeight: Double = 0.0,
    val totalOrderAmount: Double = 0.0,
)

class MaterialRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadReceivingSuppliers(): List<String> = runCatching {
        apiClient.materialApi().receivingSuppliers().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadReceivingList(filters: MaterialReceivingFilters): Pair<List<MaterialLogItemDto>, Int> =
        runCatching {
            val supplierParam = filters.suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
            val resp = apiClient.materialApi().listReceiving(
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplier = supplierParam,
            )
            val page = resp.data
            (page?.list.orEmpty()) to (page?.total ?: 0)
        }.getOrElse { emptyList<MaterialLogItemDto>() to 0 }

    suspend fun loadStockSuppliers(): List<String> = runCatching {
        apiClient.materialApi().stockSupplierNames().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadStockList(filters: MaterialStockFilters): List<MaterialStockItemDto> = runCatching {
        val supplierParam = filters.suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
        apiClient.materialApi().listStock(
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
            suppliers = supplierParam,
            startDate = filters.startDate?.takeIf { it.isNotBlank() },
            endDate = filters.endDate?.takeIf { it.isNotBlank() },
            targetDate = filters.targetDate?.takeIf { it.isNotBlank() },
            orderOnly = filters.orderOnly.takeIf { it },
        ).data?.list.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun updateSubStock(id: Int, body: MaterialStockSubUpdateBodyDto) {
        apiClient.materialApi().updateSubStock(id, body)
    }

    suspend fun deleteSubStock(id: Int) {
        apiClient.materialApi().deleteSubStock(id)
    }

    suspend fun loadSubStockList(keyword: String?, suppliers: List<String>): List<MaterialStockSubItemDto> =
        runCatching {
            val supplierParam = suppliers.takeIf { it.isNotEmpty() }?.joinToString(",")
            apiClient.materialApi().listSubStock(
                keyword = keyword?.takeIf { it.isNotBlank() },
                suppliers = supplierParam,
            ).data?.list.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadSubStockByDate(targetDate: String): List<MaterialStockSubItemDto> = runCatching {
        apiClient.materialApi().listSubStock(
            targetDate = targetDate,
        ).data?.list.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createSubStock(body: MaterialStockSubCreateBodyDto) {
        apiClient.materialApi().createSubStock(body)
    }

    suspend fun loadMergedPrintOrderRows(
        startDate: String,
        stockItems: List<MaterialStockItemDto>,
    ): List<MaterialOrderPrintRow> {
        val printSuppliers = setOf("丸一NST", "丸一ﾒﾀﾙｱｸﾄ")
        val mainRows = stockItems
            .filter { (it.orderQuantity ?: 0) > 0 && it.date == startDate && it.supplierName in printSuppliers }
            .map { it.toPrintRow() }
        val subRows = loadSubStockByDate(startDate)
            .filter { (it.orderQuantity?.toInt() ?: 0) > 0 && it.date == startDate && it.supplierName in printSuppliers }
            .map { it.toPrintRow() }
        return mainRows + subRows
    }

    suspend fun updateStock(id: Int, body: MaterialStockUpdateBodyDto) {
        apiClient.materialApi().updateStock(id, body)
    }

    suspend fun transferToSub(stockId: Int, quantity: Int = 1) {
        apiClient.materialApi().transferToSub(
            com.example.smart_emap.data.model.MaterialTransferToSubBodyDto(
                stockId = stockId,
                quantity = quantity,
            ),
        )
    }

    suspend fun syncMaterialMaster(startDate: String, endDate: String): Int {
        val response = apiClient.materialApi().syncMaterialMaster(
            mapOf(
                "start_date" to startDate,
                "end_date" to endDate,
            ),
        )
        return response.data?.updated_count ?: 0
    }

    suspend fun calculateStock() = apiClient.materialApi().calculateStock()

    suspend fun generateStockData(startDate: String, endDate: String): DataGenerationResultDto {
        val response = apiClient.materialApiLong().generateStockData(
            com.example.smart_emap.data.model.DataGenerationBodyDto(
                start_date = startDate,
                end_date = endDate,
                overwrite_existing = false,
            ),
        )
        return response.data ?: DataGenerationResultDto()
    }

    suspend fun loadForecastStats(filters: MaterialForecastFilters): MaterialForecastStatsDto =
        runCatching {
            apiClient.materialApi().forecastStats(
                targetYear = filters.year,
                targetMonth = filters.month,
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
            ).data ?: MaterialForecastStatsDto()
        }.getOrElse { MaterialForecastStatsDto() }

    suspend fun loadForecastDetails(filters: MaterialForecastFilters): List<MaterialForecastDetailDto> =
        runCatching {
            apiClient.materialApi().forecastList(
                targetYear = filters.year,
                targetMonth = filters.month,
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
            ).data?.list.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadForecastSummary(filters: MaterialForecastFilters): List<MaterialForecastSummaryDto> =
        runCatching {
            apiClient.materialApi().forecastSummary(
                targetYear = filters.year,
                targetMonth = filters.month,
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
            ).data.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadForecastSuppliers(year: Int, month: Int): List<Pair<String, String>> = runCatching {
        apiClient.materialApi().forecastSuppliers(year, month).data.orEmpty().mapNotNull { row ->
            val cd = row["supplier_cd"] ?: row["cd"] ?: return@mapNotNull null
            val name = row["supplier_name"] ?: row["name"] ?: cd
            cd to name
        }
    }.getOrElse { emptyList() }

    suspend fun loadMasterMaterials(keyword: String? = null): List<MaterialMasterItemDto> = runCatching {
        apiClient.materialApi().listMasterMaterials(
            keyword = keyword?.takeIf { it.isNotBlank() },
        ).data?.list.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun updateMasterMaterial(id: Int, body: MaterialMasterUpdateBodyDto) {
        apiClient.materialApi().updateMasterMaterial(id, body)
    }

    fun summarizeStock(rows: List<MaterialStockItemDto>): MaterialStockStatsUi {
        val distinctMaterials = rows.map { it.materialCd.orEmpty() }.distinct().count { it.isNotBlank() }
        val orderRows = rows.filter { (it.orderQuantity ?: 0) > 0 }
        val avgPrice = if (rows.isEmpty()) 0.0 else rows.map { it.unitPrice ?: 0.0 }.average()
        return MaterialStockStatsUi(
            totalMaterials = if (distinctMaterials > 0) distinctMaterials else rows.size,
            totalCurrentStock = rows.sumOf { it.currentStock ?: 0 },
            averageUnitPrice = avgPrice,
            totalUsageQuantity = rows.sumOf { it.plannedUsage ?: 0 },
            totalOrderQuantity = orderRows.sumOf { it.orderQuantity ?: 0 },
            totalOrderBundleQuantity = orderRows.sumOf { it.orderBundleQuantity ?: 0 },
            totalBundleWeight = orderRows.sumOf { it.bundleWeight ?: 0.0 },
            totalOrderAmount = orderRows.sumOf { it.orderAmount ?: 0.0 },
        )
    }
}

data class MaterialOrderPrintRow(
    val materialName: String?,
    val standardSpec: String?,
    val orderQuantity: Int,
    val orderBundleQuantity: Int,
    val bundleWeight: Double,
    val remarks: String?,
)

fun MaterialStockItemDto.toPrintRow(): MaterialOrderPrintRow = MaterialOrderPrintRow(
    materialName = materialName,
    standardSpec = standardSpec,
    orderQuantity = orderQuantity ?: 0,
    orderBundleQuantity = orderBundleQuantity ?: 0,
    bundleWeight = bundleWeight ?: 0.0,
    remarks = remarks,
)

fun MaterialStockSubItemDto.toPrintRow(): MaterialOrderPrintRow = MaterialOrderPrintRow(
    materialName = materialName,
    standardSpec = standardSpec,
    orderQuantity = orderQuantity?.toInt() ?: 0,
    orderBundleQuantity = orderBundleQuantity?.toInt() ?: 0,
    bundleWeight = bundleWeight ?: 0.0,
    remarks = remarks,
)
