package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.BatchCreateMonthlyBodyDto
import com.example.smart_emap.data.model.BatchCreateProductItemDto
import com.example.smart_emap.data.model.BatchUpdateDailyBodyDto
import com.example.smart_emap.data.model.BatchUpdateDailyItemDto
import com.example.smart_emap.data.model.CheckCombinationExistsResponseDto
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.GenerateDailyOrdersBodyDto
import com.example.smart_emap.data.model.OrderDailyCreateBodyDto
import com.example.smart_emap.data.model.OrderDailyEditRowUi
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderMonthlyCreateDto
import com.example.smart_emap.data.model.OrderMonthlyItemDto
import com.example.smart_emap.data.model.OrderMonthlySummaryDto
import com.example.smart_emap.data.model.OrderProductItemDto
import com.example.smart_emap.data.model.UpdateOrderFieldsBodyDto
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class OrderMonthlyFilters(
    val year: Int? = null,
    val month: Int? = null,
    val destinationCd: String? = null,
    val keyword: String? = null,
)

data class ForecastUpdateResult(
    val step1Count: Int,
    val step2Count: Int,
    val step3Count: Int,
    val step4Count: Int,
    val totalReflected: Int,
) {
    val totalCount: Int get() = step1Count + step2Count + step3Count + step4Count
}

class OrderMonthlyRepository(
    private val apiClient: ApiClient,
) {
    private val japanZone = ZoneId.of("Asia/Tokyo")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun loadDestinationOptions(): List<DestinationOptionDto> = runCatching {
        apiClient.masterApi().destinationOptions()
            .sortedBy { it.name }
    }.getOrElse { emptyList() }

    suspend fun loadList(filters: OrderMonthlyFilters): List<OrderMonthlyItemDto> =
        apiClient.orderMonthlyApi().list(
            year = filters.year,
            month = filters.month,
            destinationCd = filters.destinationCd?.takeIf { it.isNotBlank() },
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
        )

    suspend fun loadSummary(filters: OrderMonthlyFilters): OrderMonthlySummaryDto =
        apiClient.orderMonthlyApi().summary(
            year = filters.year,
            month = filters.month,
            destinationCd = filters.destinationCd?.takeIf { it.isNotBlank() },
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
        )

    suspend fun createMonthly(body: OrderMonthlyCreateDto): OrderMonthlyItemDto =
        apiClient.orderMonthlyApi().create(body)

    suspend fun updateMonthly(id: Int, body: OrderMonthlyCreateDto): OrderMonthlyItemDto =
        apiClient.orderMonthlyApi().update(id, body)

    suspend fun deleteMonthly(id: Int) {
        apiClient.orderMonthlyApi().delete(id)
    }

    suspend fun generateDailyOrders(
        year: Int,
        month: Int,
        destinationCd: String?,
    ) = apiClient.orderBatchApi().generateDailyOrders(
        GenerateDailyOrdersBodyDto(
            year = year,
            month = month,
            productType = "量産品",
            destinationCd = destinationCd?.takeIf { it.isNotBlank() },
        ),
    )

    suspend fun updateOrderFields(startDate: String, updateProductInfo: Boolean) =
        apiClient.orderBatchApi().updateOrderFields(
            UpdateOrderFieldsBodyDto(startDate = startDate, updateProductInfo = updateProductInfo),
        )

    suspend fun loadOrderProducts(
        destinationCd: String,
        year: Int,
        month: Int,
    ): List<OrderProductItemDto> {
        val res = apiClient.orderBatchApi().getProducts(destinationCd, year, month)
        return res.data.orEmpty()
    }

    suspend fun checkCombinationExists(
        destinationName: String,
        productName: String,
        year: Int,
        month: Int,
    ): CheckCombinationExistsResponseDto =
        apiClient.orderBatchApi().checkCombinationExists(
            destinationName = destinationName,
            productName = productName,
            year = year,
            month = month,
        )

    suspend fun batchCreateMonthly(
        year: Int,
        month: Int,
        destinationCd: String,
        destinationName: String,
        products: List<BatchCreateProductItemDto>,
    ) = apiClient.orderBatchApi().batchCreateMonthly(
        BatchCreateMonthlyBodyDto(
            year = year,
            month = month,
            destinationCd = destinationCd,
            destinationName = destinationName,
            products = products,
        ),
    )

    suspend fun loadDailyByMonthlyOrderId(monthlyOrderId: String): List<OrderDailyItemDto> =
        apiClient.orderDailyApi().list(monthlyOrderId = monthlyOrderId)

    suspend fun loadDailyBatchRows(monthlyOrderId: String): List<OrderDailyEditRowUi> =
        OrderDailyUiMapper.processBatchRows(loadDailyByMonthlyOrderId(monthlyOrderId))

    suspend fun loadDailyManageRows(date: String, destinationCd: String?): List<OrderDailyEditRowUi> =
        OrderDailyUiMapper.processManageRows(
            loadDailyByDateRange(date, date, destinationCd),
        )

    suspend fun batchUpdateDailyItems(items: List<BatchUpdateDailyItemDto>): Int {
        if (items.isEmpty()) return 0
        val res = apiClient.orderDailyApi().batchUpdate(BatchUpdateDailyBodyDto(list = items))
        return res.updated ?: items.size
    }

    suspend fun loadDailyByDateRange(
        startDate: String,
        endDate: String,
        destinationCd: String?,
    ): List<OrderDailyItemDto> = apiClient.orderDailyApi().list(
        startDate = startDate,
        endDate = endDate,
        destinationCd = destinationCd?.takeIf { it.isNotBlank() },
    )

    suspend fun updateDailyForecast(id: Int, forecastUnits: Int, row: OrderDailyItemDto) {
        apiClient.orderDailyApi().update(
            id = id,
            body = OrderDailyCreateBodyDto(
                monthlyOrderId = row.monthlyOrderId,
                destinationCd = row.destinationCd,
                destinationName = row.destinationName,
                date = row.date.orEmpty(),
                weekday = row.weekday,
                productCd = row.productCd,
                productName = row.productName,
                productType = row.productType,
                forecastUnits = forecastUnits,
                confirmedBoxes = row.confirmedBoxes,
                confirmedUnits = row.confirmedUnits,
                unitPerBox = row.unitPerBox,
                status = row.status,
                remarks = row.remarks,
                deliveryDate = row.deliveryDate,
            ),
        )
    }

    suspend fun runForecastUpdate(
        destinationCd: String?,
        onProgress: (Int) -> Unit = {},
    ): ForecastUpdateResult {
        val today = LocalDate.now(japanZone)
        val todayStr = today.format(dateFormatter)
        val startDate = today.minusDays(31).format(dateFormatter)
        val endDate90 = today.plusDays(90).format(dateFormatter)

        val allRows = loadDailyByDateRange(startDate, endDate90, destinationCd)
        onProgress(10)

        var totalReflected = 0

        val step1List = allRows.filter { r ->
            val d = normDate(r.deliveryDate)
            val cu = r.confirmedUnits ?: 0
            val fu = r.forecastUnits
            d.isNotEmpty() && d >= startDate && d <= endDate90 && cu > 0 && fu != cu
        }
        val step1Payloads = step1List.map { r ->
            BatchUpdateDailyItemDto(
                id = r.id,
                forecastUnits = r.confirmedUnits ?: 0,
                confirmedBoxes = r.confirmedBoxes,
                confirmedUnits = r.confirmedUnits,
                status = r.status,
                remarks = r.remarks,
            )
        }
        totalReflected += batchUpdateChunks(step1Payloads)
        onProgress(30)

        val step2List = allRows.filter { r ->
            val d = normDate(r.deliveryDate)
            val cu = r.confirmedUnits ?: 0
            val fu = r.forecastUnits
            d.isNotEmpty() && d >= startDate && d <= todayStr && cu <= 0 && fu > 0
        }
        val step2Payloads = step2List.map { r ->
            BatchUpdateDailyItemDto(
                id = r.id,
                forecastUnits = 0,
                confirmedBoxes = r.confirmedBoxes,
                confirmedUnits = r.confirmedUnits,
                status = r.status,
                remarks = r.remarks,
            )
        }
        totalReflected += batchUpdateChunks(step2Payloads)
        onProgress(50)

        val step2Ids = step2List.map { it.id }.toSet()
        val byProductLastDate = mutableMapOf<String, String>()
        for (r in allRows) {
            val key = r.productCd.ifBlank { r.id.toString() }
            val d = normDate(r.deliveryDate)
            val cb = r.confirmedBoxes ?: 0
            if (cb > 0 && d.isNotEmpty() && d >= startDate && d <= endDate90) {
                val prev = byProductLastDate[key].orEmpty()
                if (d > prev) byProductLastDate[key] = d
            }
        }
        val step3List = allRows.filter { r ->
            if (r.id in step2Ids) return@filter false
            val key = r.productCd.ifBlank { r.id.toString() }
            val lastD = byProductLastDate[key] ?: return@filter false
            val d = normDate(r.deliveryDate)
            val cu = r.confirmedUnits ?: 0
            val fu = r.forecastUnits
            d.isNotEmpty() && d >= startDate && d <= lastD && cu <= 0 && fu > 0
        }
        val step3Payloads = step3List.map { r ->
            BatchUpdateDailyItemDto(
                id = r.id,
                forecastUnits = 0,
                confirmedBoxes = r.confirmedBoxes,
                confirmedUnits = r.confirmedUnits,
                status = r.status,
                remarks = r.remarks,
            )
        }
        totalReflected += batchUpdateChunks(step3Payloads)
        onProgress(70)

        val step2And3Ids = step2Ids + step3List.map { it.id }
        val lastConfirmedDateByProduct = mutableMapOf<String, String>()
        for (r in allRows) {
            val cu = r.confirmedUnits ?: 0
            if (cu <= 0) continue
            val key = r.productCd.ifBlank { r.id.toString() }
            val d = normDate(r.deliveryDate)
            if (d.isEmpty()) continue
            val prev = lastConfirmedDateByProduct[key].orEmpty()
            if (d > prev) lastConfirmedDateByProduct[key] = d
        }
        val step4List = allRows.filter { r ->
            if (r.id in step2And3Ids) return@filter false
            val d = normDate(r.deliveryDate)
            val cu = r.confirmedUnits ?: 0
            val fu = r.forecastUnits
            if (cu > 0 || fu <= 0) return@filter false
            val key = r.productCd.ifBlank { r.id.toString() }
            val lastDate = lastConfirmedDateByProduct[key]
            if (lastDate != null) return@filter d.isNotEmpty() && d < lastDate
            d.isNotEmpty() && d <= todayStr
        }
        val step4Payloads = step4List.map { r ->
            BatchUpdateDailyItemDto(
                id = r.id,
                forecastUnits = 0,
                confirmedBoxes = r.confirmedBoxes,
                confirmedUnits = r.confirmedUnits,
                status = r.status,
                remarks = r.remarks,
            )
        }
        totalReflected += batchUpdateChunks(step4Payloads)
        onProgress(100)

        return ForecastUpdateResult(
            step1Count = step1Payloads.size,
            step2Count = step2Payloads.size,
            step3Count = step3Payloads.size,
            step4Count = step4Payloads.size,
            totalReflected = totalReflected,
        )
    }

    private suspend fun batchUpdateChunks(payloads: List<BatchUpdateDailyItemDto>): Int {
        if (payloads.isEmpty()) return 0
        var reflected = 0
        payloads.chunked(BATCH_SIZE).forEach { batch ->
            val res = apiClient.orderDailyApi().batchUpdate(BatchUpdateDailyBodyDto(list = batch))
            reflected += res.updated ?: batch.size
        }
        return reflected
    }

    private fun normDate(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val part = value.trim().take(10)
        return if (part.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) part else ""
    }

    companion object {
        private const val BATCH_SIZE = 100
    }
}
