package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ProcessDefectOptionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProcessDefectApiService {
    @GET("/api/master/process-defect-items/options")
    suspend fun getOptions(
        @Query("detectionProcessCd") detectionProcessCd: String,
        @Query("attributableProcessCd") attributableProcessCd: String? = null,
    ): ProcessDefectOptionsResponse
}
