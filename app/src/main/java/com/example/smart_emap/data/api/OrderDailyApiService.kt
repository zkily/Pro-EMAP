package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.BatchUpdateDailyBodyDto
import com.example.smart_emap.data.model.BatchUpdateDailyResponseDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderDailyCreateBodyDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderDailyApiService {
    @GET("/api/erp/orders/daily")
    suspend fun list(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("monthly_order_id") monthlyOrderId: String? = null,
        @Query("destination_cd") destinationCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): List<OrderDailyItemDto>

    @POST("/api/erp/orders/daily/batch-update")
    suspend fun batchUpdate(@Body body: BatchUpdateDailyBodyDto): BatchUpdateDailyResponseDto

    @PUT("/api/erp/orders/daily/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: OrderDailyCreateBodyDto,
    ): OrderDailyItemDto

    @POST("/api/erp/orders/daily")
    suspend fun create(@Body body: OrderDailyCreateBodyDto): OrderDailyItemDto

    @DELETE("/api/erp/orders/daily/{id}")
    suspend fun delete(@Path("id") id: Int)
}
