package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class SalesStatsDto(
    @Json(name = "monthly_order_count") val monthlyOrderCount: Int = 0,
    @Json(name = "monthly_order_amount") val monthlyOrderAmount: Double = 0.0,
    @Json(name = "monthly_confirmed_units") val monthlyConfirmedUnits: Int = 0,
)

data class InventoryStatsDto(
    @Json(name = "summary_stock_qty_today") val summaryStockQtyToday: Int? = null,
)

data class ActiveProductCountDto(
    @Json(name = "active_count") val activeCount: Int = 0,
)

data class DailyConfirmedSeriesItemDto(
    val date: String,
    @Json(name = "confirmed_units") val confirmedUnits: Int = 0,
)

data class DailyConfirmedSeriesDto(
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = "",
    @Json(name = "as_of_date") val asOfDate: String = "",
    val items: List<DailyConfirmedSeriesItemDto> = emptyList(),
)

data class ApiDataWrapper<T>(
    val data: T? = null,
)
