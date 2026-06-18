package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ShortcutPinsUpdateRequestDto
import com.example.smart_emap.data.model.ShortcutVisitRequestDto
import com.example.smart_emap.data.model.ShortcutsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ShortcutsApiService {
    @GET("/api/auth/shortcuts")
    suspend fun listShortcuts(): ShortcutsResponseDto

    @PUT("/api/auth/shortcuts/pins")
    suspend fun updatePins(@Body body: ShortcutPinsUpdateRequestDto): ShortcutsResponseDto

    @POST("/api/auth/shortcuts/visit")
    suspend fun recordVisit(@Body body: ShortcutVisitRequestDto)
}
