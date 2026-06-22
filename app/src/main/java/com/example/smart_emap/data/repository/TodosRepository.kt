package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.UserTodoCreateBodyDto
import com.example.smart_emap.data.model.UserTodoListResponseDto
import com.example.smart_emap.data.model.UserTodoItemDto
import com.example.smart_emap.data.model.UserTodoUpdateBodyDto

class TodosRepository(
    private val apiClient: ApiClient,
) {
    suspend fun list(limit: Int = 200): UserTodoListResponseDto =
        apiClient.todosApi().list(limit)

    suspend fun create(content: String): UserTodoItemDto =
        apiClient.todosApi().create(UserTodoCreateBodyDto(content))

    suspend fun update(id: Int, content: String? = null, isDone: Int? = null): UserTodoItemDto =
        apiClient.todosApi().update(id, UserTodoUpdateBodyDto(content = content, isDone = isDone))

    suspend fun delete(id: Int) {
        apiClient.todosApi().delete(id)
    }

    suspend fun clearCompleted(): Int =
        apiClient.todosApi().clearCompleted().deletedCount
}
