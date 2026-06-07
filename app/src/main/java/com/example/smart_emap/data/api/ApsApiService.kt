package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.EquipmentEfficiencyProductDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApsApiService {
    @GET("/api/aps/equipment-efficiency-products")
    suspend fun getEquipmentEfficiencyProducts(
        @Query("machineId") machineId: Int,
    ): List<EquipmentEfficiencyProductDto>
}
