package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.UserListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SystemUsersApiService {
    @GET("/api/system/users")
    suspend fun listUsers(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("status") status: String? = null,
    ): UserListResponse
}
