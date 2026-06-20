package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.InventoryKpiRowDto
import com.example.smart_emap.data.model.PickingNewProgressDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.data.model.ShippingItemDto
import com.example.smart_emap.data.model.ShippingOverviewRowDto
import com.example.smart_emap.data.model.WarehouseDailyRowDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeldingShippingMatrix(
    val dateColumns: List<String> = emptyList(),
    val rows: List<WeldingShippingMatrixRow> = emptyList(),
)

data class WeldingShippingMatrixRow(
    val destinationName: String,
    val cells: Map<String, Int>,
    val total: Int,
)

class ShippingRepository(
    private val apiClient: ApiClient,
    private val productionSummaryRepository: ProductionSummaryRepository,
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val moshi = apiClient.moshi

    fun todayStr(): String = LocalDate.now().format(dateFmt)

    fun shiftDate(isoDay: String, deltaDays: Long): String =
        LocalDate.parse(isoDay).plusDays(deltaDays).format(dateFmt)

    suspend fun loadPickingProgress(): PickingNewProgressDto? = runCatching {
        apiClient.shippingApi().pickingNewProgress()
    }.getOrNull()

    suspend fun loadShippingItems(
        startDate: String? = null,
        endDate: String? = null,
        destinationCd: String? = null,
        productName: String? = null,
        status: String? = null,
        shippingNo: String? = null,
    ): List<ShippingItemDto> = runCatching {
        apiClient.shippingApi().listItems(
            shippingDate = startDate,
            endDate = endDate,
            destinationCd = destinationCd,
            productName = productName,
            status = status,
            shippingNo = shippingNo,
        )
    }.getOrDefault(emptyList())

    suspend fun loadOverview(
        dateFrom: String?,
        dateTo: String?,
        destinationCds: String? = null,
    ): List<ShippingOverviewRowDto> = runCatching {
        apiClient.shippingApi().overview(dateFrom, dateTo, destinationCds)
    }.getOrDefault(emptyList())

    suspend fun loadProductionSummaryForDate(date: String): List<ProductionSummaryFullRowDto> =
        productionSummaryRepository.loadAllSummaryInRange(date, date)

    suspend fun loadWeldingProductLabels(): List<String> = runCatching {
        apiClient.shippingApi().weldingProducts().map { row ->
            (row["label"] as? String)?.takeIf { it.isNotBlank() }
                ?: (row["value"] as? String).orEmpty()
        }.filter { it.isNotBlank() }
    }.getOrDefault(emptyList())

    suspend fun loadWeldingMatrix(
        startDate: String,
        endDate: String,
        productCds: List<String>,
    ): WeldingShippingMatrix? = runCatching {
        if (productCds.isEmpty()) return@runCatching WeldingShippingMatrix()
        val raw = apiClient.shippingApi().weldingData(
            mapOf(
                "start_date" to startDate,
                "end_date" to endDate,
                "products" to productCds,
            ),
        )
        val dates = (raw["dates"] as? List<*>)?.map { it.toString() }.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val nested = raw["data"] as? Map<String, Map<String, Map<String, List<Map<String, Any?>>>>> ?: emptyMap()
        val destinations = (raw["destinations"] as? List<*>)?.map { it.toString() }.orEmpty()
            .ifEmpty { nested.values.flatMap { it.keys }.distinct().sorted() }
        val rows = destinations.map { dest ->
            var total = 0
            val cells = dates.associateWith { date ->
                val productMap = nested[date]?.get(dest).orEmpty()
                val qty = productMap.values.flatten().sumOf { (it["boxes"] as? Number)?.toInt() ?: 0 }
                total += qty
                qty
            }
            WeldingShippingMatrixRow(dest, cells, total)
        }
        WeldingShippingMatrix(dates, rows)
    }.getOrNull()

    suspend fun exportWeldingHtml(
        startDate: String,
        endDate: String,
        productCds: List<String>,
    ): String? = runCatching {
        val res = apiClient.shippingApi().weldingExport(
            mapOf(
                "start_date" to startDate,
                "end_date" to endDate,
                "products" to productCds,
            ),
        )
        (res["html"] as? String)?.takeIf { it.isNotBlank() }
    }.getOrNull()

    suspend fun loadWarehouseDailyRows(
        dateFrom: String,
        dateTo: String,
        productCd: String? = null,
        massProductionOnly: Boolean? = null,
    ): List<WarehouseDailyRowDto> = runCatching {
        val raw = apiClient.shippingApi().warehouseDailyRows(
            dateFrom = dateFrom,
            dateTo = dateTo,
            productCd = productCd?.takeIf { it.isNotBlank() },
            massProductionOnly = massProductionOnly,
        )
        @Suppress("UNCHECKED_CAST")
        val data = raw["data"] as? Map<String, Any?> ?: raw
        parseList(data, "list", WarehouseDailyRowDto::class.java)
    }.getOrDefault(emptyList())

    suspend fun syncWarehouseDaily() = runCatching { apiClient.shippingApi().warehouseDailySync() }

    suspend fun generateWarehouseDaily() = runCatching { apiClient.shippingApi().warehouseDailyGenerate() }

    suspend fun refreshPickingAsync() = runCatching { apiClient.shippingApi().refreshPickingAsync() }

    suspend fun loadPickingHistoryItems(startDate: String, endDate: String): List<ShippingItemDto> = runCatching {
        val raw = apiClient.shippingApi().pickingHistory(startDate, endDate)
        parseList(raw, "items", ShippingItemDto::class.java)
    }.getOrDefault(emptyList())

    suspend fun loadKpiTurnover(start: String, end: String): List<InventoryKpiRowDto> = loadKpiList {
        apiClient.shippingApi().kpiTurnover(start, end)
    }

    suspend fun loadKpiAvgDays(start: String, end: String): List<InventoryKpiRowDto> = loadKpiList {
        apiClient.shippingApi().kpiAvgInventoryDays(start, end)
    }

    suspend fun loadKpiShortage(asOf: String): List<InventoryKpiRowDto> = loadKpiList {
        apiClient.shippingApi().kpiShortageAlerts(asOf)
    }

    suspend fun loadKpiOverstock(asOf: String): List<InventoryKpiRowDto> = loadKpiList {
        apiClient.shippingApi().kpiOverstockAlerts(asOf)
    }

    suspend fun loadKpiReorder(asOf: String): List<InventoryKpiRowDto> = loadKpiList {
        apiClient.shippingApi().kpiReorderPoint(asOf)
    }

    private suspend fun loadKpiList(fetch: suspend () -> Map<String, Any?>): List<InventoryKpiRowDto> = runCatching {
        val raw = fetch()
        parseList(raw, "data", InventoryKpiRowDto::class.java).ifEmpty {
            @Suppress("UNCHECKED_CAST")
            val data = raw["data"] as? Map<String, Any?>
            parseList(data ?: emptyMap(), "list", InventoryKpiRowDto::class.java)
        }
    }.getOrDefault(emptyList())

    private fun <T> parseList(raw: Map<String, Any?>, key: String, clazz: Class<T>): List<T> {
        return try {
            val listType = Types.newParameterizedType(List::class.java, clazz)
            val adapter = moshi.adapter<List<T>>(listType)
            val target = raw[key] ?: return emptyList()
            val json = moshi.adapter(Any::class.java).toJson(target)
            adapter.fromJson(json).orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
