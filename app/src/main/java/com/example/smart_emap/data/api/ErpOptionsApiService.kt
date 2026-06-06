package com.example.smart_emap.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET

interface ErpOptionsApiService {
    @GET("/api/erp/products")
    suspend fun getProductsRaw(): ResponseBody
}
