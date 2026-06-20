package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApiEnvelope
import com.example.smart_emap.data.model.OutsourcingDashboardDto
import com.example.smart_emap.data.model.OutsourcingPlatingOrderDto
import com.example.smart_emap.data.model.OutsourcingPlatingReceivingDto
import com.example.smart_emap.data.model.OutsourcingProcessProductDto
import com.example.smart_emap.data.model.OutsourcingProcessProductStatsDto
import com.example.smart_emap.data.model.OutsourcingProcessProductsResponseDto
import com.example.smart_emap.data.model.OutsourcingStockHistoryItemDto
import com.example.smart_emap.data.model.OutsourcingStockItemDto
import com.example.smart_emap.data.model.OutsourcingStockListResponseDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.data.model.OutsourcingSupplierSummaryDto
import com.example.smart_emap.data.model.OutsourcingUpcomingDeliveryDto
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OutsourcingApiService {
    @GET("/api/outsourcing/dashboard")
    suspend fun dashboard(): ApiEnvelope<OutsourcingDashboardDto>

    @GET("/api/outsourcing/upcoming-deliveries")
    suspend fun upcomingDeliveries(@Query("days") days: Int = 7): ApiEnvelope<List<OutsourcingUpcomingDeliveryDto>>

    @GET("/api/outsourcing/suppliers/summary")
    suspend fun supplierSummary(): ApiEnvelope<List<OutsourcingSupplierSummaryDto>>

    @GET("/api/outsourcing/suppliers")
    suspend fun listSuppliers(
        @Query("type") type: String? = null,
        @Query("isActive") isActive: Boolean? = null,
    ): ApiEnvelope<List<OutsourcingSupplierDto>>

    @POST("/api/outsourcing/suppliers")
    suspend fun createSupplier(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingSupplierDto>

    @PUT("/api/outsourcing/suppliers/{id}")
    suspend fun updateSupplier(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingSupplierDto>

    @DELETE("/api/outsourcing/suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") id: Int): ApiEnvelope<Any>

    @GET("/api/outsourcing/process-products")
    suspend fun listProcessProducts(
        @Query("processType") processType: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("supplierCd") supplierCd: String? = null,
        @Query("productCd") productCd: String? = null,
        @Query("isActive") isActive: String? = null,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
    ): OutsourcingProcessProductsResponseDto

    @GET("/api/outsourcing/process-products/stats")
    suspend fun processProductStats(): ApiEnvelope<OutsourcingProcessProductStatsDto>

    @GET("/api/outsourcing/process-products/by-keys")
    suspend fun processProductsByKeys(
        @Query("processType") processType: String,
        @Query("supplierCd") supplierCd: String,
        @Query("isActive") isActive: Boolean? = true,
    ): ApiEnvelope<List<OutsourcingProcessProductDto>>

    @POST("/api/outsourcing/process-products")
    suspend fun createProcessProduct(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingProcessProductDto>

    @PUT("/api/outsourcing/process-products/{id}")
    suspend fun updateProcessProduct(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingProcessProductDto>

    @PATCH("/api/outsourcing/process-products/{id}/toggle")
    suspend fun toggleProcessProduct(@Path("id") id: Int): ApiEnvelope<OutsourcingProcessProductDto>

    @DELETE("/api/outsourcing/process-products/{id}")
    suspend fun deleteProcessProduct(@Path("id") id: Int): ApiEnvelope<Any>

    @GET("/api/outsourcing/plating/orders")
    suspend fun listPlatingOrders(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("supplierCd") supplierCd: String? = null,
        @Query("productName") productName: String? = null,
        @Query("status") status: String? = null,
    ): ApiEnvelope<List<OutsourcingPlatingOrderDto>>

    @GET("/api/outsourcing/plating/orders/pending")
    suspend fun pendingPlatingOrders(): ApiEnvelope<List<OutsourcingPlatingOrderDto>>

    @GET("/api/outsourcing/plating/orders/by-order-no")
    suspend fun platingOrdersByOrderNo(@Query("order_no") orderNo: String): ApiEnvelope<List<OutsourcingPlatingOrderDto>>

    @POST("/api/outsourcing/plating/orders")
    suspend fun createPlatingOrder(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingPlatingOrderDto>

    @PUT("/api/outsourcing/plating/orders/{id}")
    suspend fun updatePlatingOrder(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingPlatingOrderDto>

    @DELETE("/api/outsourcing/plating/orders/{id}")
    suspend fun deletePlatingOrder(@Path("id") id: Int): ApiEnvelope<Any>

    @POST("/api/outsourcing/plating/orders/batch-order")
    suspend fun batchOrderPlating(@Body body: Map<String, List<Int>>): ApiEnvelope<Any>

    @GET("/api/outsourcing/plating/receivings")
    suspend fun listPlatingReceivings(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("supplierCd") supplierCd: String? = null,
        @Query("supplierId") supplierId: Int? = null,
        @Query("productName") productName: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null,
    ): com.example.smart_emap.data.model.OutsourcingReceivingListResponseDto

    @GET("/api/outsourcing/plating/receivings/products")
    suspend fun platingReceivingProducts(): ApiEnvelope<List<String>>

    @POST("/api/outsourcing/plating/receivings")
    suspend fun createPlatingReceiving(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingPlatingReceivingDto>

    @PUT("/api/outsourcing/plating/receivings/{id}")
    suspend fun updatePlatingReceiving(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingPlatingReceivingDto>

    @GET("/api/outsourcing/plating/stock")
    suspend fun platingStock(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("supplierId") supplierId: Int? = null,
        @Query("productCode") productCode: String? = null,
        @Query("stockStatus") stockStatus: String? = null,
    ): OutsourcingStockListResponseDto

    @GET("/api/outsourcing/welding/orders")
    suspend fun listWeldingOrders(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("supplierCd") supplierCd: String? = null,
        @Query("productName") productName: String? = null,
        @Query("status") status: String? = null,
    ): ApiEnvelope<List<OutsourcingWeldingOrderDto>>

    @GET("/api/outsourcing/welding/orders/pending")
    suspend fun pendingWeldingOrders(): ApiEnvelope<List<OutsourcingWeldingOrderDto>>

    @GET("/api/outsourcing/welding/orders/by-order-no")
    suspend fun weldingOrdersByOrderNo(@Query("order_no") orderNo: String): ApiEnvelope<List<OutsourcingWeldingOrderDto>>

    @POST("/api/outsourcing/welding/orders")
    suspend fun createWeldingOrder(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingWeldingOrderDto>

    @POST("/api/outsourcing/welding/orders/batch")
    suspend fun createWeldingOrdersBatch(@Body body: Map<String, List<Map<String, @JvmSuppressWildcards Any?>>>): ApiEnvelope<List<OutsourcingWeldingOrderDto>>

    @PUT("/api/outsourcing/welding/orders/{id}")
    suspend fun updateWeldingOrder(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingWeldingOrderDto>

    @DELETE("/api/outsourcing/welding/orders/{id}")
    suspend fun deleteWeldingOrder(@Path("id") id: Int): ApiEnvelope<Any>

    @POST("/api/outsourcing/welding/orders/batch-order")
    suspend fun batchOrderWelding(@Body body: Map<String, List<Int>>): ApiEnvelope<Any>

    @GET("/api/outsourcing/welding/receivings")
    suspend fun listWeldingReceivings(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("supplierId") supplierId: Int? = null,
        @Query("productName") productName: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null,
    ): com.example.smart_emap.data.model.OutsourcingWeldingReceivingListResponseDto

    @GET("/api/outsourcing/welding/receivings/products")
    suspend fun weldingReceivingProducts(): ApiEnvelope<List<String>>

    @POST("/api/outsourcing/welding/receivings")
    suspend fun createWeldingReceiving(@Body body: Map<String, @JvmSuppressWildcards Any?>): ApiEnvelope<OutsourcingWeldingReceivingDto>

    @PUT("/api/outsourcing/welding/receivings/{id}")
    suspend fun updateWeldingReceiving(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): ApiEnvelope<OutsourcingWeldingReceivingDto>

    @DELETE("/api/outsourcing/welding/receivings/{id}")
    suspend fun deleteWeldingReceiving(@Path("id") id: Int): ApiEnvelope<Any>

    @GET("/api/outsourcing/welding/stock")
    suspend fun weldingStock(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("supplierId") supplierId: Int? = null,
        @Query("productCode") productCode: String? = null,
        @Query("stockStatus") stockStatus: String? = null,
    ): OutsourcingStockListResponseDto

    @GET("/api/outsourcing/stock/history")
    suspend fun stockHistory(
        @Query("processType") processType: String,
        @Query("productCd") productCd: String,
        @Query("supplierCd") supplierCd: String,
        @Query("weldingType") weldingType: String? = null,
    ): ApiEnvelope<List<OutsourcingStockHistoryItemDto>>
}
