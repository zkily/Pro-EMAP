package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ErpProductDto
import retrofit2.http.GET

interface ErpOptionsApiService {
    @GET("/api/erp/products")
    suspend fun getProducts(): List<ErpProductDto>
}
