package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.ClearCompletedTodosResponseDto
import com.example.smart_emap.data.model.UserTodoCreateBodyDto
import com.example.smart_emap.data.model.UserTodoItemDto
import com.example.smart_emap.data.model.UserTodoListResponseDto
import com.example.smart_emap.data.model.UserTodoUpdateBodyDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TodosApiService {
    @GET("/api/auth/todos")
    suspend fun list(@Query("limit") limit: Int = 200): UserTodoListResponseDto

    @POST("/api/auth/todos")
    suspend fun create(@Body body: UserTodoCreateBodyDto): UserTodoItemDto

    @PATCH("/api/auth/todos/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body body: UserTodoUpdateBodyDto,
    ): UserTodoItemDto

    @DELETE("/api/auth/todos/{id}")
    suspend fun delete(@Path("id") id: Int)

    @DELETE("/api/auth/todos/completed")
    suspend fun clearCompleted(): ClearCompletedTodosResponseDto
}
