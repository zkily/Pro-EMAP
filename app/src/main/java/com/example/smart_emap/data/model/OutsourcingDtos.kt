package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class OutsourcingSupplierDto(
    val id: Int? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "supplier_type") val supplierType: String? = null,
    @Json(name = "postal_code") val postalCode: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    @Json(name = "contact_person") val contactPerson: String? = null,
    val email: String? = null,
    @Json(name = "payment_terms") val paymentTerms: String? = null,
    @Json(name = "lead_time_days") val leadTimeDays: Int? = null,
    val remarks: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
)

data class OutsourcingSupplierSummaryDto(
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "supplier_type") val supplierType: String? = null,
    @Json(name = "plating_order_count") val platingOrderCount: Int? = null,
    @Json(name = "welding_order_count") val weldingOrderCount: Int? = null,
    @Json(name = "supplied_material_stock") val materialStockCount: Int? = null,
)

data class OutsourcingUpcomingDeliveryDto(
    val type: String? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    val quantity: Int? = null,
    @Json(name = "received_qty") val receivedQty: Int? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
    @Json(name = "days_remaining") val daysRemaining: Int? = null,
)

data class OutsourcingDashboardDto(
    @Json(name = "todayOrders") val todayOrders: Map<String, Int>? = null,
    @Json(name = "pendingOrders") val pendingOrders: Map<String, Int>? = null,
    @Json(name = "todayReceivings") val todayReceivings: Map<String, Int>? = null,
    @Json(name = "stockAlerts") val stockAlerts: Map<String, Int>? = null,
    @Json(name = "overdueOrders") val overdueOrders: Map<String, Int>? = null,
)

data class OutsourcingProcessProductDto(
    val id: Int? = null,
    @Json(name = "process_type") val processType: String? = null,
    @Json(name = "process_type_name") val processTypeName: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    val specification: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "delivery_lead_time") val deliveryLeadTime: Int? = null,
    @Json(name = "delivery_location") val deliveryLocation: String? = null,
    val category: String? = null,
    val content: String? = null,
    val remarks: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
)

data class OutsourcingProcessProductStatsDto(
    val total: OutsourcingProcessProductTotalStatsDto? = null,
    @Json(name = "byProcessType") val byProcessType: List<OutsourcingProcessTypeCountDto>? = null,
)

data class OutsourcingProcessProductTotalStatsDto(
    @Json(name = "total_count") val totalCount: Int? = null,
    @Json(name = "active_count") val activeCount: Int? = null,
    @Json(name = "supplier_count") val supplierCount: Int? = null,
)

data class OutsourcingProcessTypeCountDto(
    @Json(name = "process_type") val processType: String? = null,
    @Json(name = "total_count") val totalCount: Int? = null,
)

data class OutsourcingPlatingOrderDto(
    val id: Int? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "order_date") val orderDate: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "plating_type") val platingType: String? = null,
    val quantity: Int? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
    @Json(name = "delivery_location") val deliveryLocation: String? = null,
    val category: String? = null,
    val content: String? = null,
    val specification: String? = null,
    val status: String? = null,
    @Json(name = "received_qty") val receivedQty: Int? = null,
    @Json(name = "total_receiving_qty") val totalReceivingQty: Int? = null,
)

data class OutsourcingPlatingReceivingDto(
    val id: Int? = null,
    @Json(name = "receiving_no") val receivingNo: String? = null,
    @Json(name = "receiving_date") val receivingDate: String? = null,
    @Json(name = "order_id") val orderId: Int? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "plating_type") val platingType: String? = null,
    @Json(name = "order_qty") val orderQty: Int? = null,
    @Json(name = "receiving_qty") val receivingQty: Int? = null,
    @Json(name = "good_qty") val goodQty: Int? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    val status: String? = null,
    @Json(name = "defect_reason") val defectReason: String? = null,
    val inspector: String? = null,
    val remarks: String? = null,
)

data class OutsourcingReceivingListResponseDto(
    val success: Boolean? = null,
    val data: List<OutsourcingPlatingReceivingDto>? = null,
    val total: Int? = null,
)

data class OutsourcingWeldingOrderDto(
    val id: Int? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "order_date") val orderDate: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "welding_type") val weldingType: String? = null,
    val quantity: Int? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
    @Json(name = "delivery_location") val deliveryLocation: String? = null,
    val category: String? = null,
    val content: String? = null,
    val specification: String? = null,
    val status: String? = null,
    @Json(name = "received_qty") val receivedQty: Int? = null,
    @Json(name = "total_receiving_qty") val totalReceivingQty: Int? = null,
)

data class OutsourcingWeldingReceivingDto(
    val id: Int? = null,
    @Json(name = "receiving_no") val receivingNo: String? = null,
    @Json(name = "receiving_date") val receivingDate: String? = null,
    @Json(name = "order_id") val orderId: Int? = null,
    @Json(name = "order_no") val orderNo: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "welding_type") val weldingType: String? = null,
    @Json(name = "order_qty") val orderQty: Int? = null,
    @Json(name = "receiving_qty") val receivingQty: Int? = null,
    @Json(name = "good_qty") val goodQty: Int? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    val status: String? = null,
    @Json(name = "defect_reason") val defectReason: String? = null,
    val inspector: String? = null,
    val remarks: String? = null,
)

data class OutsourcingWeldingReceivingListResponseDto(
    val success: Boolean? = null,
    val data: List<OutsourcingWeldingReceivingDto>? = null,
    val total: Int? = null,
)

data class OutsourcingStockItemDto(
    val id: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "supplier_id") val supplierId: Int? = null,
    @Json(name = "plating_type") val platingType: String? = null,
    @Json(name = "welding_type") val weldingType: String? = null,
    @Json(name = "ordered_qty") val orderedQty: Int? = null,
    @Json(name = "received_qty") val receivedQty: Int? = null,
    @Json(name = "used_qty") val usedQty: Int? = null,
    @Json(name = "stock_qty") val stockQty: Int? = null,
    @Json(name = "pending_qty") val pendingQty: Int? = null,
    @Json(name = "min_stock") val minStock: Int? = null,
    @Json(name = "last_receive_date") val lastReceiveDate: String? = null,
    @Json(name = "last_issue_date") val lastIssueDate: String? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
    @Json(name = "pending_receiving") val pendingReceiving: Int? = null,
    @Json(name = "last_receiving_date") val lastReceivingDate: String? = null,
    @Json(name = "stock_status") val stockStatus: String? = null,
)

data class OutsourcingStockHistoryItemDto(
    val id: Int? = null,
    @Json(name = "transaction_date") val transactionDate: String? = null,
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "log_date") val logDate: String? = null,
    val type: String? = null,
    @Json(name = "related_no") val relatedNo: String? = null,
    val quantity: Int? = null,
    @Json(name = "stock_after") val stockAfter: Int? = null,
    val operator: String? = null,
    val remarks: String? = null,
)

data class OutsourcingStockListResponseDto(
    val success: Boolean? = null,
    val data: List<OutsourcingStockItemDto>? = null,
    val total: Int? = null,
)

data class OutsourcingPaginationDto(
    val page: Int? = null,
    @Json(name = "pageSize") val pageSize: Int? = null,
    val total: Int? = null,
)

data class OutsourcingProcessProductsResponseDto(
    val success: Boolean? = null,
    val data: List<OutsourcingProcessProductDto>? = null,
    val pagination: OutsourcingPaginationDto? = null,
)
