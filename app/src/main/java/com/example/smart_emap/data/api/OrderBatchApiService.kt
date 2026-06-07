package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.BatchCreateMonthlyBodyDto
import com.example.smart_emap.data.model.BatchCreateMonthlyResponseDto
import com.example.smart_emap.data.model.CheckCombinationExistsResponseDto
import com.example.smart_emap.data.model.GenerateDailyOrdersBodyDto
import com.example.smart_emap.data.model.GenerateDailyOrdersResponseDto
import com.example.smart_emap.data.model.OrderProductsResponseDto
import com.example.smart_emap.data.model.UpdateOrderFieldsBodyDto
import com.example.smart_emap.data.model.UpdateOrderFieldsResponseDto
import com.example.smart_emap.data.model.CheckMonthlyOrderExistsResponseDto
import com.example.smart_emap.data.model.AddMonthlyOrderBodyDto
import com.example.smart_emap.data.model.AddMonthlyOrderResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderBatchApiService {
    @GET("/api/order/products")
    suspend fun getProducts(
        @Query("destination_cd") destinationCd: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): OrderProductsResponseDto

    @GET("/api/order/check-combination-exists")
    suspend fun checkCombinationExists(
        @Query("destination_name") destinationName: String,
        @Query("product_name") productName: String,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): CheckCombinationExistsResponseDto

    @POST("/api/order/batch-create-monthly")
    suspend fun batchCreateMonthly(@Body body: BatchCreateMonthlyBodyDto): BatchCreateMonthlyResponseDto

    @POST("/api/order/generate-daily")
    suspend fun generateDailyOrders(@Body body: GenerateDailyOrdersBodyDto): GenerateDailyOrdersResponseDto

    @POST("/api/order/monthly/update-fields")
    suspend fun updateOrderFields(@Body body: UpdateOrderFieldsBodyDto): UpdateOrderFieldsResponseDto

    @GET("/api/order/check-exists")
    suspend fun checkMonthlyOrderExists(
        @Query("order_id") orderId: String,
    ): CheckMonthlyOrderExistsResponseDto

    @POST("/api/order/monthly/add")
    suspend fun addMonthlyOrder(@Body body: AddMonthlyOrderBodyDto): AddMonthlyOrderResponseDto
}
