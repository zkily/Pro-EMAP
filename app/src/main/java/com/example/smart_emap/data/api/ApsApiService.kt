package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.data.model.EquipmentEfficiencyProductDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApsApiService {
    @GET("/api/aps/equipment-efficiency-products")
    suspend fun getEquipmentEfficiencyProducts(
        @Query("machineId") machineId: Int,
    ): List<EquipmentEfficiencyProductDto>

    @GET("/api/aps/lines")
    suspend fun listLines(
        @Query("processCd") processCd: String? = null,
    ): List<ApsProductionLineDto>

    @GET("/api/aps/scheduling/grid")
    suspend fun getSchedulingGrid(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("lineId") lineId: Int? = null,
        @Query("processCd") processCd: String? = null,
    ): SchedulingGridResponseDto
}
