package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.CuttingPlanningMachineDto
import retrofit2.http.GET

interface CuttingPlanningApiService {
    @GET("/api/cutting-planning/machines")
    suspend fun machines(): List<CuttingPlanningMachineDto>
}
