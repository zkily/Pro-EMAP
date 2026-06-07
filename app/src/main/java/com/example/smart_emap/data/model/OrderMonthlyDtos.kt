package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class OrderMonthlyItemDto(
    val id: Int = 0,
    @Json(name = "order_id") val orderId: String = "",
    @Json(name = "destination_cd") val destinationCd: String = "",
    @Json(name = "destination_name") val destinationName: String = "",
    val year: Int = 0,
    val month: Int = 0,
    @Json(name = "product_cd") val productCd: String = "",
    @Json(name = "product_name") val productName: String = "",
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "product_type") val productType: String = "",
    @Json(name = "forecast_units") val forecastUnits: Int = 0,
    @Json(name = "forecast_total_units") val forecastTotalUnits: Int = 0,
    @Json(name = "forecast_diff") val forecastDiff: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class OrderMonthlyCreateDto(
    @Json(name = "destination_cd") val destinationCd: String,
    @Json(name = "destination_name") val destinationName: String,
    val year: Int,
    val month: Int,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
    @Json(name = "forecast_total_units") val forecastTotalUnits: Int? = null,
    @Json(name = "forecast_diff") val forecastDiff: Int? = null,
)

data class OrderMonthlySummaryDto(
    @Json(name = "forecast_units") val forecastUnits: Int = 0,
    @Json(name = "forecast_total_units") val forecastTotalUnits: Int = 0,
    @Json(name = "forecast_diff") val forecastDiff: Int = 0,
    @Json(name = "plating_count") val platingCount: Int = 0,
    @Json(name = "external_plating_count") val externalPlatingCount: Int = 0,
    @Json(name = "internal_welding_count") val internalWeldingCount: Int = 0,
    @Json(name = "external_welding_count") val externalWeldingCount: Int = 0,
    @Json(name = "internal_inspection_count") val internalInspectionCount: Int = 0,
    @Json(name = "external_inspection_count") val externalInspectionCount: Int = 0,
)

data class DestinationOptionDto(
    val cd: String = "",
    val name: String = "",
)

data class OrderProductItemDto(
    @Json(name = "product_cd") val productCd: String = "",
    @Json(name = "product_name") val productName: String = "",
    @Json(name = "product_type") val productType: String = "",
    @Json(name = "forecast_units") val forecastUnits: Int = 0,
)

data class OrderProductsResponseDto(
    val success: Boolean? = null,
    val data: List<OrderProductItemDto>? = null,
)

data class CheckCombinationExistsResponseDto(
    val exists: Boolean = false,
    val id: Int? = null,
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
)

data class BatchCreateProductItemDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "forecast_units") val forecastUnits: Int,
)

data class BatchCreateMonthlyBodyDto(
    val year: Int,
    val month: Int,
    @Json(name = "destination_cd") val destinationCd: String,
    @Json(name = "destination_name") val destinationName: String,
    val products: List<BatchCreateProductItemDto>,
)

data class BatchCreateMonthlyResponseDto(
    val inserted: Int = 0,
    val total: Int = 0,
    val skipped: Int = 0,
    val message: String = "",
)

data class GenerateDailyOrdersBodyDto(
    val year: Int,
    val month: Int,
    @Json(name = "productType") val productType: String = "量産品",
    @Json(name = "destination_cd") val destinationCd: String? = null,
)

data class GenerateDailyOrdersResponseDto(
    val success: Boolean = false,
    @Json(name = "insertedCount") val insertedCount: Int? = null,
    @Json(name = "updatedCount") val updatedCount: Int? = null,
    val total: Int? = null,
)

data class UpdateOrderFieldsBodyDto(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "updateProductInfo") val updateProductInfo: Boolean,
)

data class UpdateOrderFieldsResponseDto(
    @Json(name = "updatedCount") val updatedCount: Int = 0,
    val message: String = "",
)

data class OrderDailyItemDto(
    val id: Int = 0,
    @Json(name = "monthly_order_id") val monthlyOrderId: String? = null,
    @Json(name = "destination_cd") val destinationCd: String = "",
    @Json(name = "destination_name") val destinationName: String? = null,
    val date: String? = null,
    @Json(name = "product_cd") val productCd: String = "",
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
    val weekday: String? = null,
    @Json(name = "forecast_units") val forecastUnits: Int = 0,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "confirmed_units") val confirmedUnits: Int? = null,
    val status: String? = null,
    val remarks: String? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
)

data class BatchUpdateDailyItemDto(
    val id: Int,
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "confirmed_units") val confirmedUnits: Int? = null,
    val status: String? = null,
    val remarks: String? = null,
)

data class BatchUpdateDailyBodyDto(
    val list: List<BatchUpdateDailyItemDto>,
)

data class BatchUpdateDailyResponseDto(
    val success: Boolean? = null,
    val updated: Int? = null,
)

data class OrderDailyUpdateBodyDto(
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "confirmed_units") val confirmedUnits: Int? = null,
    val status: String? = null,
    val remarks: String? = null,
)

data class OrderMonthlyListWrapperDto(
    val data: List<OrderMonthlyItemDto>? = null,
)

data class OrderDailyEditRowUi(
    val id: Int,
    val destinationName: String = "",
    val productName: String = "",
    val productType: String = "",
    val unitPerBox: Int = 0,
    val shipDate: String? = null,
    val weekday: String? = null,
    val confirmedBoxes: String = "",
    val confirmedUnits: String = "",
    val forecastUnits: String = "",
    val deliveryDate: String? = null,
    val status: String? = null,
    val remarks: String? = null,
)

data class OrderDailyCreateBodyDto(
    @Json(name = "monthly_order_id") val monthlyOrderId: String? = null,
    @Json(name = "destination_cd") val destinationCd: String,
    @Json(name = "destination_name") val destinationName: String? = null,
    val date: String,
    val weekday: String? = null,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "confirmed_units") val confirmedUnits: Int? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
    val status: String? = null,
    val remarks: String? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
)

data class CheckMonthlyOrderExistsResponseDto(
    val exists: Boolean = false,
    val id: Int? = null,
    @Json(name = "order_id") val orderId: String? = null,
)

data class AddMonthlyOrderBodyDto(
    @Json(name = "order_id") val orderId: String,
    @Json(name = "destination_cd") val destinationCd: String,
    @Json(name = "destination_name") val destinationName: String,
    val year: Int,
    val month: Int,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "product_type") val productType: String,
    @Json(name = "forecast_units") val forecastUnits: Int,
    @Json(name = "forecast_total_units") val forecastTotalUnits: Int,
)

data class AddMonthlyOrderResponseDto(
    val ok: Boolean = false,
    @Json(name = "order_id") val orderId: String? = null,
    val created: Boolean? = null,
)

data class MasterProductItemDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
) {
    fun normalized(): MasterProductItemDto = MasterProductItemDto(
        productCd = productCd.orEmpty(),
        productName = productName.orEmpty(),
        productType = productType?.takeIf { it.isNotBlank() } ?: "量産品",
        unitPerBox = unitPerBox ?: 0,
        productAlias = productAlias.orEmpty(),
        destinationCd = destinationCd.orEmpty(),
    )
}

data class MasterProductListDataDto(
    val list: List<MasterProductItemDto>? = null,
)

data class MasterProductListResponseDto(
    val success: Boolean? = null,
    val data: MasterProductListDataDto? = null,
    val list: List<MasterProductItemDto>? = null,
) {
    fun items(): List<MasterProductItemDto> =
        (data?.list ?: list.orEmpty()).map { it.normalized() }
}

data class OrderDailySummaryUi(
    val count: Int = 0,
    val confirmedUnits: Int = 0,
    val confirmedBoxes: Int = 0,
    val forecastUnits: Int = 0,
)
