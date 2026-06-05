package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.LoginRequest
import com.example.smart_emap.data.model.LoginResponse
import com.example.smart_emap.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("/api/auth/logout")
    suspend fun logout()

    @GET("/api/auth/me")
    suspend fun me(): UserDto
}
