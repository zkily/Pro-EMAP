package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ProductionActualLogsResponse
import com.example.smart_emap.data.model.ProductionActualUpdateBody
import com.example.smart_emap.data.model.SimpleMessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductionActualLogsApiService {
    @GET("/api/erp/production-actual-logs")
    suspend fun list(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10000,
        @Query("transaction_type") transactionType: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("process_cd") processCd: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("target_name") targetName: String? = null,
        @Query("machine_name") machineName: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): ProductionActualLogsResponse

    @PUT("/api/erp/stock-transaction-logs/{id}")
    suspend fun updateLog(
        @Path("id") id: Int,
        @Body body: ProductionActualUpdateBody,
    ): SimpleMessageResponse

    @DELETE("/api/erp/stock-transaction-logs/{id}")
    suspend fun deleteLog(@Path("id") id: Int): SimpleMessageResponse
}
