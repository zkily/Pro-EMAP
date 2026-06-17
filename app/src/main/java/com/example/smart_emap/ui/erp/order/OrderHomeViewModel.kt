package com.example.smart_emap.ui.erp.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderMonthlySummaryDto
import com.example.smart_emap.data.repository.OrderDailyListFilters
import com.example.smart_emap.data.repository.OrderDailyRepository
import com.example.smart_emap.data.repository.OrderMonthlyFilters
import com.example.smart_emap.data.repository.OrderMonthlyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class OrderHomeMonthlyPointUi(
    val label: String,
    val forecastUnits: Int,
    val forecastTotalUnits: Int,
    val isFuture: Boolean,
)

data class OrderHomeDailyRowUi(
    val date: String,
    val weekday: String,
    val count: Int,
    val confirmedUnits: Int,
)

data class OrderHomeProductRankUi(
    val productCd: String,
    val productName: String,
    val confirmedUnits: Int,
    val lineCount: Int,
)

data class OrderHomeUiState(
    val isLoadingSummary: Boolean = true,
    val isLoadingAnalytics: Boolean = false,
    val summary: OrderMonthlySummaryDto = OrderMonthlySummaryDto(),
    val monthlyPoints: List<OrderHomeMonthlyPointUi> = emptyList(),
    val dailyRows: List<OrderHomeDailyRowUi> = emptyList(),
    val productRank: List<OrderHomeProductRankUi> = emptyList(),
    val asOfDate: String = "",
    val errorMessage: String? = null,
)

class OrderHomeViewModel(
    private val repository: OrderMonthlyRepository,
    private val dailyRepository: OrderDailyRepository,
) : ViewModel() {

    private val japanZone = ZoneId.of("Asia/Tokyo")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _uiState = MutableStateFlow(OrderHomeUiState())
    val uiState: StateFlow<OrderHomeUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingSummary = true,
                    isLoadingAnalytics = true,
                    errorMessage = null,
                )
            }
            val today = LocalDate.now(japanZone)
            val asOf = today.format(dateFormatter)
            val currentYear = today.year
            val currentMonth = today.monthValue

            runCatching {
                val summary = repository.loadSummary(
                    OrderMonthlyFilters(year = currentYear, month = currentMonth),
                )
                _uiState.update {
                    it.copy(summary = summary, isLoadingSummary = false, asOfDate = asOf)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoadingSummary = false,
                        errorMessage = e.message ?: "サマリの取得に失敗しました",
                    )
                }
            }

            runCatching {
                val monthly = loadMonthlySeries(today)
                val dailyList = dailyRepository.loadList(
                    OrderDailyListFilters(
                        startDate = YearMonth.from(today).atDay(1).format(dateFormatter),
                        endDate = YearMonth.from(today).atEndOfMonth().format(dateFormatter),
                    ),
                )
                val dailyRows = buildDailyRows(today, dailyList)
                val productRank = aggregateProductRank(dailyList)
                _uiState.update {
                    it.copy(
                        monthlyPoints = monthly,
                        dailyRows = dailyRows,
                        productRank = productRank,
                        isLoadingAnalytics = false,
                        asOfDate = asOf,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoadingAnalytics = false,
                        errorMessage = it.errorMessage ?: (e.message ?: "分析データの取得に失敗しました"),
                    )
                }
            }
        }
    }

    private suspend fun loadMonthlySeries(today: LocalDate): List<OrderHomeMonthlyPointUi> = coroutineScope {
        val months = past6Future2Months(today)
        val summaries = months.map { ym ->
            async {
                runCatching {
                    repository.loadSummary(
                        OrderMonthlyFilters(year = ym.year, month = ym.monthValue),
                    )
                }.getOrElse { OrderMonthlySummaryDto() }
            }
        }.awaitAll()
        months.mapIndexed { index, ym ->
            val summary = summaries.getOrElse(index) { OrderMonthlySummaryDto() }
            OrderHomeMonthlyPointUi(
                label = "${ym.year}/${ym.monthValue.toString().padStart(2, '0')}",
                forecastUnits = summary.forecastUnits,
                forecastTotalUnits = summary.forecastTotalUnits,
                isFuture = index >= FUTURE_MONTH_START_INDEX,
            )
        }
    }

    private fun past6Future2Months(today: LocalDate): List<YearMonth> {
        val start = YearMonth.from(today).minusMonths(5)
        return (0 until 8).map { start.plusMonths(it.toLong()) }
    }

    private fun buildDailyRows(today: LocalDate, items: List<OrderDailyItemDto>): List<OrderHomeDailyRowUi> {
        val ym = YearMonth.from(today)
        val agg = mutableMapOf<String, Pair<Int, Int>>()
        for (row in items) {
            val date = row.date?.trim()?.take(10).orEmpty()
            if (date.length != 10) continue
            val units = row.confirmedUnits ?: 0
            val prev = agg[date] ?: (0 to 0)
            agg[date] = (prev.first + 1) to (prev.second + units)
        }
        val locale = Locale.JAPAN
        return (1..ym.lengthOfMonth()).map { day ->
            val date = ym.atDay(day)
            val dateStr = date.format(dateFormatter)
            val bucket = agg[dateStr] ?: (0 to 0)
            OrderHomeDailyRowUi(
                date = dateStr,
                weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                count = bucket.first,
                confirmedUnits = bucket.second,
            )
        }
    }

    private fun aggregateProductRank(items: List<OrderDailyItemDto>): List<OrderHomeProductRankUi> {
        val map = linkedMapOf<String, Triple<String, Int, Int>>()
        for (row in items) {
            val cd = row.productCd.orEmpty().trim()
            if (cd.isEmpty()) continue
            val displayName = row.productName.orEmpty()
                .ifBlank { row.productAlias.orEmpty() }
                .ifBlank { row.productCd.orEmpty() }
                .trim()
            if (displayName.contains("加工")) continue
            val units = row.confirmedUnits ?: 0
            val current = map[cd]
            if (current == null) {
                map[cd] = Triple(displayName.ifEmpty { cd }, units, 1)
            } else {
                val name = if (displayName.length > current.first.length) displayName else current.first
                map[cd] = Triple(name, current.second + units, current.third + 1)
            }
        }
        return map.entries
            .map { (cd, triple) ->
                OrderHomeProductRankUi(
                    productCd = cd,
                    productName = triple.first,
                    confirmedUnits = triple.second,
                    lineCount = triple.third,
                )
            }
            .sortedWith(
                compareByDescending<OrderHomeProductRankUi> { it.confirmedUnits }
                    .thenByDescending { it.lineCount },
            )
            .take(PRODUCT_RANK_LIMIT)
    }

    class Factory(
        private val repository: OrderMonthlyRepository,
        private val dailyRepository: OrderDailyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderHomeViewModel(repository, dailyRepository) as T
    }

    companion object {
        private const val FUTURE_MONTH_START_INDEX = 6
        private const val PRODUCT_RANK_LIMIT = 12
    }
}
