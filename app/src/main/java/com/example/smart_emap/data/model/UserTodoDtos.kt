package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class UserTodoItemDto(
    val id: Int,
    val content: String,
    @Json(name = "is_done") val isDone: Int,
    @Json(name = "created_by") val createdBy: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "completed_at") val completedAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String,
)

data class UserTodoListResponseDto(
    val list: List<UserTodoItemDto> = emptyList(),
    @Json(name = "pending_count") val pendingCount: Int = 0,
)

data class UserTodoCreateBodyDto(
    val content: String,
)

data class UserTodoUpdateBodyDto(
    val content: String? = null,
    @Json(name = "is_done") val isDone: Int? = null,
)

data class ClearCompletedTodosResponseDto(
    @Json(name = "deleted_count") val deletedCount: Int = 0,
)
