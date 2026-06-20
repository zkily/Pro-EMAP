package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class InventoryLogRowDto(
    val id: Int = 0,
    val item: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "log_date") val logDate: String? = null,
    @Json(name = "log_time") val logTime: String? = null,
    @Json(name = "hd_no") val hdNo: String? = null,
    @Json(name = "pack_qty") val packQty: Int? = null,
    @Json(name = "case_qty") val caseQty: Int? = null,
    val quantity: Int? = null,
    val remarks: String? = null,
    @Json(name = "worker_name") val workerName: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class InventoryLogListDataDto(
    val list: List<InventoryLogRowDto>? = null,
    val total: Int = 0,
    @Json(name = "totalQuantity") val totalQuantity: Int = 0,
)

data class InventoryLogCreateBodyDto(
    val item: String,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "process_cd") val processCd: String,
    @Json(name = "log_date") val logDate: String,
    @Json(name = "log_time") val logTime: String,
    @Json(name = "hd_no") val hdNo: String? = null,
    @Json(name = "pack_qty") val packQty: Int? = null,
    @Json(name = "case_qty") val caseQty: Int? = null,
    val quantity: Int,
    val remarks: String? = null,
)

data class StockAlertDto(
    val id: Int = 0,
    @Json(name = "product_code") val productCode: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "warehouse_code") val warehouseCode: String? = null,
    @Json(name = "warehouse_name") val warehouseName: String? = null,
    @Json(name = "alert_type") val alertType: String? = null,
    @Json(name = "alert_type_name") val alertTypeName: String? = null,
    @Json(name = "current_quantity") val currentQuantity: Double? = null,
    @Json(name = "threshold_quantity") val thresholdQuantity: Double? = null,
    val status: String? = null,
    @Json(name = "status_name") val statusName: String? = null,
)

data class StockAlertListResponse(
    val items: List<StockAlertDto>? = null,
    val total: Int = 0,
    val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 50,
)

data class InventoryStockTransactionLogRowDto(
    val id: Int = 0,
    @Json(name = "stock_type") val stockType: String? = null,
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "target_cd") val targetCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "location_cd") val locationCd: String? = null,
    @Json(name = "lot_no") val lotNo: String? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "related_log_id") val relatedLogId: Int? = null,
    @Json(name = "operator_id") val operatorId: Int? = null,
    @Json(name = "operator_name") val operatorName: String? = null,
    @Json(name = "transaction_time") val transactionTime: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "source_file") val sourceFile: String? = null,
    val remarks: String? = null,
)

data class InventoryStockTransactionLogListResponse(
    val list: List<InventoryStockTransactionLogRowDto>? = null,
    val total: Int = 0,
    @Json(name = "totalQuantity") val totalQuantity: Double? = null,
    @Json(name = "inboundQuantity") val inboundQuantity: Double? = null,
    @Json(name = "outboundQuantity") val outboundQuantity: Double? = null,
)

data class InventoryStockTransactionLogBodyDto(
    @Json(name = "stock_type") val stockType: String,
    @Json(name = "transaction_type") val transactionType: String,
    @Json(name = "target_cd") val targetCd: String,
    @Json(name = "location_cd") val locationCd: String,
    val quantity: Double,
    val unit: String = "本",
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "transaction_time") val transactionTime: String,
    @Json(name = "source_file") val sourceFile: String = "手入力",
    val remarks: String? = null,
    @Json(name = "lot_no") val lotNo: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "order_no") val orderNo: String? = null,
)

fun StockTransactionLogBody.toInventoryBody(): InventoryStockTransactionLogBodyDto =
    InventoryStockTransactionLogBodyDto(
        stockType = stockType,
        transactionType = transactionType,
        targetCd = targetCd,
        locationCd = locationCd,
        quantity = quantity.toDouble(),
        unit = unit,
        processCd = processCd,
        transactionTime = transactionTime,
        sourceFile = sourceFile,
    )
