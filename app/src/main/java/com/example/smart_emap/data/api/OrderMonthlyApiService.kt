package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.OrderMonthlyCreateDto
import com.example.smart_emap.data.model.OrderMonthlyItemDto
import com.example.smart_emap.data.model.OrderMonthlySummaryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderMonthlyApiService {
    @GET("/api/erp/orders/monthly")
    suspend fun list(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("destination_cd") destinationCd: String? = null,
        @Query("product_cd") productCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): List<OrderMonthlyItemDto>

    @GET("/api/erp/orders/monthly/{id}")
    suspend fun getById(@Path("id") id: Int): OrderMonthlyItemDto

    @POST("/api/erp/orders/monthly")
    suspend fun create(@Body body: OrderMonthlyCreateDto): OrderMonthlyItemDto

    @PUT("/api/erp/orders/monthly/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: OrderMonthlyCreateDto,
    ): OrderMonthlyItemDto

    @DELETE("/api/erp/orders/monthly/{id}")
    suspend fun delete(@Path("id") id: Int)

    @GET("/api/order/monthly/summary")
    suspend fun summary(
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("destination_cd") destinationCd: String? = null,
        @Query("keyword") keyword: String? = null,
    ): OrderMonthlySummaryDto
}
