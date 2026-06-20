package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.InventoryLogCreateBodyDto
import com.example.smart_emap.data.model.InventoryLogRowDto
import com.example.smart_emap.data.model.InventoryStockTransactionLogBodyDto
import com.example.smart_emap.data.model.InventoryStockTransactionLogRowDto
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.model.StartDateBody
import com.example.smart_emap.data.model.StockAlertDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class InventoryLogFilters(
    val item: String? = null,
    val keyword: String = "",
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val monthPicker: String? = null,
    val stageType: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "log_date",
    val sortOrder: String = "desc",
)

data class InventoryLogPageResult(
    val list: List<InventoryLogRowDto> = emptyList(),
    val total: Int = 0,
    val totalQuantity: Int = 0,
)

data class StockTransactionLogFilters(
    val stockType: String? = null,
    val keyword: String? = null,
    val targetCd: String? = null,
    val locationCd: String? = null,
    val transactionType: String? = null,
    val processCd: String? = null,
    val sourceFile: String? = null,
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class StockTransactionLogPageResult(
    val list: List<InventoryStockTransactionLogRowDto> = emptyList(),
    val total: Int = 0,
    val totalQuantity: Double = 0.0,
    val inboundQuantity: Double = 0.0,
    val outboundQuantity: Double = 0.0,
)

class InventoryRepository(
    private val apiClient: ApiClient,
    private val productionSummaryRepository: ProductionSummaryRepository,
    private val materialRepository: MaterialRepository,
    private val partRepository: PartRepository,
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayStr(): String = LocalDate.now().format(dateFmt)

    suspend fun loadProductSummaryForDate(date: String): List<ProductionSummaryFullRowDto> =
        productionSummaryRepository.loadList(
            ProductionSummaryFilters(
                startDate = date,
                endDate = date,
                limit = 5000,
            ),
        ).first

    suspend fun loadProductSummaryRange(startDate: String, endDate: String, productCd: String = ""): Pair<List<ProductionSummaryFullRowDto>, Int> =
        productionSummaryRepository.loadList(
            ProductionSummaryFilters(
                startDate = startDate,
                endDate = endDate,
                productCd = productCd,
                limit = 5000,
            ),
        )

    suspend fun loadProductOptions(): List<ProductionSummaryProductOptionDto> =
        productionSummaryRepository.loadProducts()

    suspend fun updateProductInventory(startDate: String? = null): String = runCatching {
        val api = apiClient.productionSummaryApiLong()
        api.updateInventory(startDate?.let { StartDateBody(it) }).message ?: "在庫更新が完了しました"
    }.getOrElse { throw it }

    suspend fun loadMaterialStockForDate(date: String): List<MaterialStockItemDto> =
        materialRepository.loadStockList(
            MaterialStockFilters(targetDate = date),
        )

    suspend fun loadMaterialStockRange(startDate: String?, endDate: String?, keyword: String? = null): List<MaterialStockItemDto> =
        materialRepository.loadStockList(
            MaterialStockFilters(
                keyword = keyword,
                startDate = startDate,
                endDate = endDate,
            ),
        )

    suspend fun loadPartStockForDate(date: String): List<PartStockItemDto> =
        partRepository.loadStockList(
            PartStockFilters(targetDate = date),
        )

    suspend fun loadPartStockRange(startDate: String?, endDate: String?, keyword: String? = null): List<PartStockItemDto> =
        partRepository.loadStockList(
            PartStockFilters(
                keyword = keyword,
                startDate = startDate,
                endDate = endDate,
            ),
        )

    suspend fun loadStockAlerts(page: Int = 1, pageSize: Int = 50): Pair<List<StockAlertDto>, Int> = runCatching {
        val resp = apiClient.inventoryApi().listStockAlerts(page = page, pageSize = pageSize)
        (resp.items.orEmpty()) to resp.total
    }.getOrElse { emptyList<StockAlertDto>() to 0 }

    suspend fun loadInventoryLogs(filters: InventoryLogFilters): InventoryLogPageResult = runCatching {
        val dateRange = if (!filters.dateStart.isNullOrBlank() && !filters.dateEnd.isNullOrBlank()) {
            listOf(filters.dateStart, filters.dateEnd)
        } else {
            null
        }
        val resp = apiClient.inventoryApi().listInventoryLogs(
            item = filters.item?.takeIf { it.isNotBlank() },
            keyword = filters.keyword.takeIf { it.isNotBlank() },
            dateRange = dateRange,
            monthPicker = filters.monthPicker?.takeIf { it.isNotBlank() },
            stageType = filters.stageType?.takeIf { it.isNotBlank() && it != "all" },
            page = filters.page,
            pageSize = filters.pageSize,
            sortBy = filters.sortBy,
            sortOrder = filters.sortOrder,
        )
        val data = resp.data
        InventoryLogPageResult(
            list = data?.list.orEmpty(),
            total = data?.total ?: 0,
            totalQuantity = data?.totalQuantity ?: 0,
        )
    }.getOrElse { InventoryLogPageResult() }

    suspend fun createInventoryLog(body: InventoryLogCreateBodyDto) {
        apiClient.inventoryApi().createInventoryLog(body)
    }

    suspend fun deleteInventoryLog(id: Int) {
        apiClient.inventoryApi().deleteInventoryLog(id)
    }

    suspend fun importInventoryLogs(): String = runCatching {
        apiClient.inventoryApiLong().importInventoryLogs().message ?: "棚卸データ取込が完了しました"
    }.getOrElse { throw it }

    suspend fun loadStockTransactionLogs(filters: StockTransactionLogFilters): StockTransactionLogPageResult = runCatching {
        val resp = apiClient.stockTransactionLogApi().listStockLogs(
            stockType = filters.stockType?.takeIf { it.isNotBlank() },
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
            targetCd = filters.targetCd?.takeIf { it.isNotBlank() },
            locationCd = filters.locationCd?.takeIf { it.isNotBlank() },
            transactionType = filters.transactionType?.takeIf { it.isNotBlank() },
            processCd = filters.processCd?.takeIf { it.isNotBlank() },
            sourceFile = filters.sourceFile?.takeIf { it.isNotBlank() },
            dateStart = filters.dateStart?.takeIf { it.isNotBlank() },
            dateEnd = filters.dateEnd?.takeIf { it.isNotBlank() },
            page = filters.page,
            pageSize = filters.pageSize,
        )
        StockTransactionLogPageResult(
            list = resp.list.orEmpty(),
            total = resp.total,
            totalQuantity = resp.totalQuantity ?: 0.0,
            inboundQuantity = resp.inboundQuantity ?: 0.0,
            outboundQuantity = resp.outboundQuantity ?: 0.0,
        )
    }.getOrElse { StockTransactionLogPageResult() }

    suspend fun createStockTransactionLog(body: InventoryStockTransactionLogBodyDto) {
        apiClient.stockTransactionLogApi().createStockLog(body)
    }

    suspend fun updateStockTransactionLog(id: Int, body: InventoryStockTransactionLogBodyDto) {
        apiClient.stockTransactionLogApi().updateStockLog(id, body)
    }

    suspend fun deleteStockTransactionLog(id: Int) {
        apiClient.stockTransactionLogApi().deleteStockLog(id)
    }

    suspend fun loadProcessOptions(): List<Pair<String, String>> =
        productionSummaryRepository.loadProcessOptions()

    fun loadLocationOptions(): List<Pair<String, String>> = listOf(
        "製品倉庫" to "製品倉庫",
        "外注倉庫" to "外注倉庫",
        "工程中間在庫" to "工程中間在庫",
        "材料倉庫" to "材料倉庫",
        "部品倉庫" to "部品倉庫",
    )
}
