package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.BatchActualBody
import com.example.smart_emap.data.model.SimpleMessageResponse
import com.example.smart_emap.data.model.StockTransactionLogBody
import com.example.smart_emap.data.model.StockTransactionLogListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StockTransactionLogApiService {
    @GET("/api/erp/stock-transaction-logs")
    suspend fun listStockLogs(
        @Query("transaction_type") transactionType: String? = null,
        @Query("process_cd") processCd: String? = null,
        @Query("date_start") dateStart: String? = null,
        @Query("date_end") dateEnd: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5000,
    ): StockTransactionLogListResponse

    @POST("/api/erp/stock-transaction-logs")
    suspend fun createStockLog(@Body body: StockTransactionLogBody): SimpleMessageResponse

    @PUT("/api/erp/stock-transaction-logs/{id}")
    suspend fun updateStockLog(
        @Path("id") id: Int,
        @Body body: StockTransactionLogBody,
    ): SimpleMessageResponse

    @POST("/api/erp/stock-transaction-logs/batch-actual")
    suspend fun batchActual(@Body body: BatchActualBody): SimpleMessageResponse
}
