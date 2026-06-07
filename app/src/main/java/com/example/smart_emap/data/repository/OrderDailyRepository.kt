package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.AddMonthlyOrderBodyDto
import com.example.smart_emap.data.model.CheckMonthlyOrderExistsResponseDto
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OrderDailyCreateBodyDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderDailySummaryUi

data class OrderDailyListFilters(
    val startDate: String? = null,
    val endDate: String? = null,
    val destinationCd: String? = null,
    val keyword: String? = null,
)

class OrderDailyRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadDestinationOptions(): List<DestinationOptionDto> = runCatching {
        apiClient.masterApi().destinationOptions().sortedBy { it.name }
    }.getOrElse { emptyList() }

    suspend fun loadProductOptions(destinationCd: String? = null): List<MasterProductItemDto> {
        val filteredDest = destinationCd?.trim()?.takeIf { it.isNotEmpty() }
        return apiClient.masterApi()
            .listProducts(pageSize = 9999, destinationCd = filteredDest)
            .items()
            .map {
                MasterProductItemDto(
                    productCd = it.productCd,
                    productName = it.productName,
                    productType = it.productType,
                    unitPerBox = it.unitPerBox,
                    destinationCd = it.destinationCd,
                ).normalized()
            }
            .sortedBy { it.productName.orEmpty().ifBlank { it.productCd.orEmpty() } }
    }

    suspend fun loadList(filters: OrderDailyListFilters): List<OrderDailyItemDto> =
        apiClient.orderDailyApi().list(
            startDate = filters.startDate?.takeIf { it.isNotBlank() },
            endDate = filters.endDate?.takeIf { it.isNotBlank() },
            destinationCd = filters.destinationCd?.takeIf { it.isNotBlank() },
            keyword = filters.keyword?.takeIf { it.isNotBlank() },
        )

    suspend fun createDaily(body: OrderDailyCreateBodyDto): OrderDailyItemDto =
        apiClient.orderDailyApi().create(body)

    suspend fun updateDaily(id: Int, body: OrderDailyCreateBodyDto): OrderDailyItemDto =
        apiClient.orderDailyApi().update(id, body)

    suspend fun deleteDaily(id: Int) {
        apiClient.orderDailyApi().delete(id)
    }

    suspend fun checkMonthlyOrderExists(orderId: String): CheckMonthlyOrderExistsResponseDto =
        apiClient.orderBatchApi().checkMonthlyOrderExists(orderId)

    suspend fun addMonthlyOrder(body: AddMonthlyOrderBodyDto) =
        apiClient.orderBatchApi().addMonthlyOrder(body)

    fun summarize(rows: List<OrderDailyItemDto>): OrderDailySummaryUi {
        var confirmedUnits = 0
        var confirmedBoxes = 0
        var forecastUnits = 0
        rows.forEach { row ->
            confirmedUnits += row.confirmedUnits ?: 0
            confirmedBoxes += row.confirmedBoxes ?: 0
            forecastUnits += row.forecastUnits
        }
        return OrderDailySummaryUi(
            count = rows.size,
            confirmedUnits = confirmedUnits,
            confirmedBoxes = confirmedBoxes,
            forecastUnits = forecastUnits,
        )
    }

    fun buildCsv(rows: List<OrderDailyItemDto>): String {
        val headers = listOf(
            "日付", "曜日", "月受注ID", "納入先CD", "納入先名", "製品CD", "製品名", "種別",
            "内示本数", "確定箱数", "確定本数", "ステータス", "納入日",
        )
        val lines = buildList {
            add(headers.joinToString(","))
            rows.forEach { r ->
                add(
                    listOf(
                        r.date.orEmpty(),
                        r.weekday.orEmpty(),
                        r.monthlyOrderId.orEmpty(),
                        r.destinationCd,
                        r.destinationName.orEmpty(),
                        r.productCd,
                        r.productName.orEmpty(),
                        r.productType.orEmpty(),
                        r.forecastUnits.toString(),
                        (r.confirmedBoxes ?: 0).toString(),
                        (r.confirmedUnits ?: 0).toString(),
                        r.status.orEmpty(),
                        r.deliveryDate.orEmpty(),
                    ).joinToString(",") { escapeCsvCell(it) },
                )
            }
        }
        return "\uFEFF${lines.joinToString("\r\n")}"
    }

    private fun escapeCsvCell(value: String): String {
        if (value.any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
