package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.core.network.NetworkErrorHints
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.data.model.AddMonthlyOrderBodyDto
import com.example.smart_emap.data.model.ApiErrorBody
import com.example.smart_emap.data.model.CheckMonthlyOrderExistsResponseDto
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OrderDailyCreateBodyDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderDailySummaryUi
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import retrofit2.HttpException

data class OrderDailyListFilters(
    val startDate: String? = null,
    val endDate: String? = null,
    val destinationCd: String? = null,
    val keyword: String? = null,
)

class OrderDailyRepository(
    private val apiClient: ApiClient,
) {
    private val errorAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiErrorBody::class.java)

    private val networkHints = NetworkErrorHints(
        ssl = "SSL接続に失敗しました。APIアドレスを HTTP（例: http://192.168.x.x:8010/）にしてください。",
        connection = "サーバーに接続できません。アドレス・ポート・起動状態を確認してください。",
        timeout = "接続がタイムアウトしました。しばらくしてから再試行してください。",
        server = "サーバーが一時的に利用できません。しばらくしてから再試行してください。",
        noConnection = "ネットワークに接続できません。Wi‑Fi 等を確認してください。",
    )

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
                    productAlias = it.productAlias,
                ).normalized()
            }
            .sortedBy { it.productName.orEmpty().ifBlank { it.productCd.orEmpty() } }
    }

    suspend fun loadList(filters: OrderDailyListFilters): List<OrderDailyItemDto> {
        try {
            return apiClient.orderDailyApi()
                .list(
                    startDate = filters.startDate?.takeIf { it.isNotBlank() },
                    endDate = filters.endDate?.takeIf { it.isNotBlank() },
                    destinationCd = filters.destinationCd?.takeIf { it.isNotBlank() },
                    keyword = filters.keyword?.takeIf { it.isNotBlank() },
                )
                .map { it.normalized() }
        } catch (e: HttpException) {
            throw mapHttpError(e, "一覧の取得に失敗しました")
        } catch (e: JsonDataException) {
            throw Exception("一覧データの解析に失敗しました")
        } catch (e: IOException) {
            throw Exception(
                NetworkErrors.formatError(e, "一覧の取得に失敗しました", networkHints),
            )
        }
    }

    suspend fun createDaily(body: OrderDailyCreateBodyDto): OrderDailyItemDto =
        apiClient.orderDailyApi().create(body).normalized()

    suspend fun updateDaily(id: Int, body: OrderDailyCreateBodyDto): OrderDailyItemDto =
        apiClient.orderDailyApi().update(id, body).normalized()

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
            forecastUnits += row.forecastUnits ?: 0
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
                        r.destinationCd.orEmpty(),
                        r.destinationName.orEmpty(),
                        r.productCd.orEmpty(),
                        r.productName.orEmpty(),
                        r.productType.orEmpty(),
                        (r.forecastUnits ?: 0).toString(),
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

    private fun mapHttpError(e: HttpException, fallback: String): Exception {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            val parsed = runCatching { errorAdapter.fromJson(body) }.getOrNull()
            val message = parsed?.error?.message?.trim()
                ?: parsed?.detail?.trim()
            if (!message.isNullOrBlank()) {
                return Exception(message)
            }
        }
        return Exception(
            NetworkErrors.formatHttpError(e.code(), e.message(), fallback, networkHints),
        )
    }

    private fun escapeCsvCell(value: String): String {
        if (value.any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
