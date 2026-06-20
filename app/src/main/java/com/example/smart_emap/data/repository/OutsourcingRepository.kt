package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OutsourcingDashboardDto
import com.example.smart_emap.data.model.OutsourcingPlatingOrderDto
import com.example.smart_emap.data.model.OutsourcingPlatingReceivingDto
import com.example.smart_emap.data.model.OutsourcingProcessProductDto
import com.example.smart_emap.data.model.OutsourcingProcessProductStatsDto
import com.example.smart_emap.data.model.OutsourcingStockHistoryItemDto
import com.example.smart_emap.data.model.OutsourcingStockItemDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.data.model.OutsourcingSupplierSummaryDto
import com.example.smart_emap.data.model.OutsourcingUpcomingDeliveryDto
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto

data class OutsourcingOrderFilters(
    val startDate: String? = null,
    val endDate: String? = null,
    val supplierCd: String? = null,
    val productName: String? = null,
)

data class OutsourcingReceivingFilters(
    val startDate: String? = null,
    val endDate: String? = null,
    val supplierCd: String? = null,
    val productName: String? = null,
    val keyword: String? = null,
    val supplierId: Int? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class OutsourcingReceivingPage(
    val items: List<OutsourcingPlatingReceivingDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class WeldingReceivingPage(
    val items: List<OutsourcingWeldingReceivingDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class OutsourcingStockFilters(
    val supplierId: Int? = null,
    val productCode: String? = null,
    val stockStatus: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class OutsourcingSupplierFilters(
    val type: String? = null,
    val isActive: Boolean? = null,
    val keyword: String = "",
)

data class OutsourcingProcessProductFilters(
    val processType: String? = null,
    val keyword: String = "",
    val supplierCd: String = "",
    val isActive: String = "all",
    val page: Int = 1,
    val pageSize: Int = 50,
)

data class OutsourcingProcessProductsPage(
    val items: List<OutsourcingProcessProductDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 50,
)

data class OutsourcingCreateOrderBody(
    val supplierCd: String,
    val orderDate: String,
    val productCd: String,
    val productName: String,
    val processTypeLabel: String,
    val quantity: Int,
    val unitPrice: Double,
    val deliveryDate: String? = null,
    val deliveryLocation: String? = null,
    val category: String? = null,
    val content: String? = null,
    val specification: String? = null,
    val remarks: String? = null,
)

class OutsourcingRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadDashboard(): OutsourcingDashboardDto? = runCatching {
        apiClient.outsourcingApi().dashboard().data
    }.getOrNull()

    suspend fun loadUpcomingDeliveries(days: Int = 7): List<OutsourcingUpcomingDeliveryDto> = runCatching {
        apiClient.outsourcingApi().upcomingDeliveries(days).data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadSupplierSummary(): List<OutsourcingSupplierSummaryDto> = runCatching {
        apiClient.outsourcingApi().supplierSummary().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadSuppliers(processType: String? = null, isActive: Boolean? = true): List<OutsourcingSupplierDto> = runCatching {
        apiClient.outsourcingApi().listSuppliers(type = processType?.takeIf { it.isNotBlank() }, isActive = isActive).data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadSuppliers(filters: OutsourcingSupplierFilters): List<OutsourcingSupplierDto> = runCatching {
        var list = apiClient.outsourcingApi().listSuppliers(
            type = filters.type?.takeIf { it.isNotBlank() },
            isActive = filters.isActive,
        ).data.orEmpty()
        val keyword = filters.keyword.trim().lowercase()
        if (keyword.isNotEmpty()) {
            list = list.filter {
                it.supplierCd.orEmpty().lowercase().contains(keyword) ||
                    it.supplierName.orEmpty().lowercase().contains(keyword)
            }
        }
        list
    }.getOrElse { emptyList() }

    suspend fun createSupplier(body: Map<String, Any?>): OutsourcingSupplierDto? = runCatching {
        apiClient.outsourcingApi().createSupplier(body).data
    }.getOrNull()

    suspend fun updateSupplier(id: Int, body: Map<String, Any?>): OutsourcingSupplierDto? = runCatching {
        apiClient.outsourcingApi().updateSupplier(id, body).data
    }.getOrNull()

    suspend fun deleteSupplier(id: Int) {
        apiClient.outsourcingApi().deleteSupplier(id)
    }

    suspend fun loadActiveSupplierOptions(): List<Pair<String, String>> = runCatching {
        loadSuppliers(isActive = true).map { it.supplierCd.orEmpty() to it.supplierName.orEmpty() }
            .filter { it.first.isNotBlank() }
    }.getOrElse { emptyList() }

    suspend fun loadProductOptions(): List<MasterProductItemDto> = runCatching {
        apiClient.masterApi().listProducts(pageSize = 9999).items().map {
            MasterProductItemDto(
                productCd = it.productCd,
                productName = it.productName,
                productType = it.productType,
                unitPerBox = it.unitPerBox,
                destinationCd = it.destinationCd,
            ).normalized()
        }
    }.getOrElse { emptyList() }

    suspend fun loadProcessProductsPage(filters: OutsourcingProcessProductFilters): OutsourcingProcessProductsPage =
        runCatching {
            val resp = apiClient.outsourcingApi().listProcessProducts(
                processType = filters.processType?.takeIf { it.isNotBlank() && it != "all" },
                keyword = filters.keyword.takeIf { it.isNotBlank() },
                supplierCd = filters.supplierCd.takeIf { it.isNotBlank() },
                isActive = filters.isActive.takeIf { it.isNotBlank() && it != "all" },
                page = filters.page,
                pageSize = filters.pageSize,
            )
            OutsourcingProcessProductsPage(
                items = resp.data.orEmpty(),
                total = resp.pagination?.total ?: resp.data?.size ?: 0,
                page = resp.pagination?.page ?: filters.page,
                pageSize = resp.pagination?.pageSize ?: filters.pageSize,
            )
        }.getOrElse { OutsourcingProcessProductsPage() }

    suspend fun loadProcessProductStats(): OutsourcingProcessProductStatsDto? = runCatching {
        apiClient.outsourcingApi().processProductStats().data
    }.getOrNull()

    suspend fun createProcessProduct(body: Map<String, Any?>): OutsourcingProcessProductDto? = runCatching {
        apiClient.outsourcingApi().createProcessProduct(body).data
    }.getOrNull()

    suspend fun updateProcessProduct(id: Int, body: Map<String, Any?>): OutsourcingProcessProductDto? = runCatching {
        apiClient.outsourcingApi().updateProcessProduct(id, body).data
    }.getOrNull()

    suspend fun toggleProcessProductStatus(id: Int): OutsourcingProcessProductDto? = runCatching {
        apiClient.outsourcingApi().toggleProcessProduct(id).data
    }.getOrNull()

    suspend fun deleteProcessProduct(id: Int) {
        apiClient.outsourcingApi().deleteProcessProduct(id)
    }

    suspend fun getPlatingStock(
        filters: OutsourcingStockFilters = OutsourcingStockFilters(),
    ): Pair<List<OutsourcingStockItemDto>, Int> = runCatching {
        val resp = apiClient.outsourcingApi().platingStock(
            page = filters.page,
            pageSize = filters.pageSize,
            supplierId = filters.supplierId,
            productCode = filters.productCode?.takeIf { it.isNotBlank() },
            stockStatus = filters.stockStatus?.takeIf { it.isNotBlank() },
        )
        resp.data.orEmpty() to (resp.total ?: resp.data?.size ?: 0)
    }.getOrElse { emptyList<OutsourcingStockItemDto>() to 0 }

    suspend fun getWeldingStock(
        filters: OutsourcingStockFilters = OutsourcingStockFilters(),
    ): Pair<List<OutsourcingStockItemDto>, Int> = runCatching {
        val resp = apiClient.outsourcingApi().weldingStock(
            page = filters.page,
            pageSize = filters.pageSize,
            supplierId = filters.supplierId,
            productCode = filters.productCode?.takeIf { it.isNotBlank() },
            stockStatus = filters.stockStatus?.takeIf { it.isNotBlank() },
        )
        resp.data.orEmpty() to (resp.total ?: resp.data?.size ?: 0)
    }.getOrElse { emptyList<OutsourcingStockItemDto>() to 0 }

    suspend fun getOutsourcingStockHistory(
        processType: String,
        productCd: String,
        supplierCd: String,
        weldingType: String? = null,
    ): List<OutsourcingStockHistoryItemDto> = runCatching {
        apiClient.outsourcingApi().stockHistory(
            processType = processType,
            productCd = productCd,
            supplierCd = supplierCd,
            weldingType = weldingType?.takeIf { it.isNotBlank() },
        ).data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadProcessProducts(
        processType: String,
        supplierCd: String,
    ): List<OutsourcingProcessProductDto> = runCatching {
        val resp = apiClient.outsourcingApi().listProcessProducts(
            processType = processType,
            supplierCd = supplierCd,
            isActive = "true",
            page = 1,
            pageSize = 1000,
        )
        resp.data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadPlatingOrders(filters: OutsourcingOrderFilters): List<OutsourcingPlatingOrderDto> =
        runCatching {
            apiClient.outsourcingApi().listPlatingOrders(
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                productName = filters.productName?.takeIf { it.isNotBlank() },
            ).data.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadPendingPlatingOrders(): List<OutsourcingPlatingOrderDto> = runCatching {
        apiClient.outsourcingApi().pendingPlatingOrders().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadPlatingOrdersByOrderNo(orderNo: String): List<OutsourcingPlatingOrderDto> = runCatching {
        apiClient.outsourcingApi().platingOrdersByOrderNo(orderNo).data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createPlatingOrder(body: OutsourcingCreateOrderBody): OutsourcingPlatingOrderDto {
        val resp = apiClient.outsourcingApi().createPlatingOrder(body.toPlatingMap())
        return resp.data ?: throw IllegalStateException(resp.message ?: "登録に失敗しました")
    }

    suspend fun updatePlatingOrder(id: Int, body: Map<String, Any?>): OutsourcingPlatingOrderDto {
        val resp = apiClient.outsourcingApi().updatePlatingOrder(id, body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "更新に失敗しました")
    }

    suspend fun deletePlatingOrder(id: Int): String = runCatching {
        apiClient.outsourcingApi().deletePlatingOrder(id).message ?: "削除しました"
    }.getOrElse { throw it }

    suspend fun batchOrderPlating(orderIds: List<Int>): String = runCatching {
        apiClient.outsourcingApi().batchOrderPlating(mapOf("order_ids" to orderIds)).message
            ?: "${orderIds.size}件の注文を発注しました"
    }.getOrElse { throw it }

    suspend fun loadPlatingReceivings(filters: OutsourcingReceivingFilters): List<OutsourcingPlatingReceivingDto> =
        loadPlatingReceivingsPage(filters).items

    suspend fun loadPlatingReceivingsPage(filters: OutsourcingReceivingFilters): OutsourcingReceivingPage =
        runCatching {
            val resp = apiClient.outsourcingApi().listPlatingReceivings(
                page = filters.page,
                pageSize = filters.pageSize,
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                supplierId = filters.supplierId,
                productName = filters.productName?.takeIf { it.isNotBlank() },
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
            )
            OutsourcingReceivingPage(
                items = resp.data.orEmpty(),
                total = resp.total ?: resp.data?.size ?: 0,
                page = filters.page,
                pageSize = filters.pageSize,
            )
        }.getOrElse { OutsourcingReceivingPage(page = filters.page, pageSize = filters.pageSize) }

    suspend fun loadPlatingReceivingProducts(): List<String> = runCatching {
        apiClient.outsourcingApi().platingReceivingProducts().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createPlatingReceiving(body: Map<String, Any?>): OutsourcingPlatingReceivingDto {
        val resp = apiClient.outsourcingApi().createPlatingReceiving(body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "登録に失敗しました")
    }

    suspend fun updatePlatingReceiving(id: Int, body: Map<String, Any?>): OutsourcingPlatingReceivingDto {
        val resp = apiClient.outsourcingApi().updatePlatingReceiving(id, body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "更新に失敗しました")
    }

    suspend fun loadWeldingOrders(filters: OutsourcingOrderFilters): List<OutsourcingWeldingOrderDto> =
        runCatching {
            apiClient.outsourcingApi().listWeldingOrders(
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplierCd = filters.supplierCd?.takeIf { it.isNotBlank() },
                productName = filters.productName?.takeIf { it.isNotBlank() },
            ).data.orEmpty()
        }.getOrElse { emptyList() }

    suspend fun loadPendingWeldingOrders(): List<OutsourcingWeldingOrderDto> = runCatching {
        apiClient.outsourcingApi().pendingWeldingOrders().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadWeldingOrdersByOrderNo(orderNo: String): List<OutsourcingWeldingOrderDto> = runCatching {
        apiClient.outsourcingApi().weldingOrdersByOrderNo(orderNo).data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createWeldingOrder(body: OutsourcingCreateOrderBody): OutsourcingWeldingOrderDto {
        val resp = apiClient.outsourcingApi().createWeldingOrder(body.toWeldingMap())
        return resp.data ?: throw IllegalStateException(resp.message ?: "登録に失敗しました")
    }

    suspend fun updateWeldingOrder(id: Int, body: Map<String, Any?>): OutsourcingWeldingOrderDto {
        val resp = apiClient.outsourcingApi().updateWeldingOrder(id, body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "更新に失敗しました")
    }

    suspend fun deleteWeldingOrder(id: Int): String = runCatching {
        apiClient.outsourcingApi().deleteWeldingOrder(id).message ?: "削除しました"
    }.getOrElse { throw it }

    suspend fun batchOrderWelding(orderIds: List<Int>): String = runCatching {
        apiClient.outsourcingApi().batchOrderWelding(mapOf("order_ids" to orderIds)).message
            ?: "${orderIds.size}件の注文を発注しました"
    }.getOrElse { throw it }

    suspend fun loadWeldingReceivings(filters: OutsourcingReceivingFilters): List<OutsourcingWeldingReceivingDto> =
        loadWeldingReceivingsPage(filters).items

    suspend fun loadWeldingReceivingsPage(filters: OutsourcingReceivingFilters): WeldingReceivingPage =
        runCatching {
            val resp = apiClient.outsourcingApi().listWeldingReceivings(
                page = filters.page,
                pageSize = filters.pageSize,
                startDate = filters.startDate?.takeIf { it.isNotBlank() },
                endDate = filters.endDate?.takeIf { it.isNotBlank() },
                supplierId = filters.supplierId,
                productName = filters.productName?.takeIf { it.isNotBlank() },
                keyword = filters.keyword?.takeIf { it.isNotBlank() },
            )
            WeldingReceivingPage(
                items = resp.data.orEmpty(),
                total = resp.total ?: resp.data?.size ?: 0,
                page = filters.page,
                pageSize = filters.pageSize,
            )
        }.getOrElse { WeldingReceivingPage(page = filters.page, pageSize = filters.pageSize) }

    suspend fun loadWeldingReceivingProducts(): List<String> = runCatching {
        apiClient.outsourcingApi().weldingReceivingProducts().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun createWeldingReceiving(body: Map<String, Any?>): OutsourcingWeldingReceivingDto {
        val resp = apiClient.outsourcingApi().createWeldingReceiving(body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "登録に失敗しました")
    }

    suspend fun updateWeldingReceiving(id: Int, body: Map<String, Any?>): OutsourcingWeldingReceivingDto {
        val resp = apiClient.outsourcingApi().updateWeldingReceiving(id, body)
        return resp.data ?: throw IllegalStateException(resp.message ?: "更新に失敗しました")
    }

    private fun OutsourcingCreateOrderBody.toPlatingMap(): Map<String, Any?> = mapOf(
        "supplier_cd" to supplierCd,
        "order_date" to orderDate,
        "product_cd" to productCd,
        "product_name" to productName,
        "plating_type" to processTypeLabel,
        "quantity" to quantity,
        "unit_price" to unitPrice,
        "delivery_date" to deliveryDate,
        "delivery_location" to deliveryLocation,
        "category" to category,
        "content" to content,
        "specification" to specification,
        "remarks" to remarks,
    )

    private fun OutsourcingCreateOrderBody.toWeldingMap(): Map<String, Any?> = mapOf(
        "supplier_cd" to supplierCd,
        "order_date" to orderDate,
        "product_cd" to productCd,
        "product_name" to productName,
        "welding_type" to processTypeLabel,
        "quantity" to quantity,
        "unit_price" to unitPrice,
        "delivery_date" to deliveryDate,
        "delivery_location" to deliveryLocation,
        "category" to category,
        "content" to content,
        "specification" to specification,
        "remarks" to remarks,
    )
}
