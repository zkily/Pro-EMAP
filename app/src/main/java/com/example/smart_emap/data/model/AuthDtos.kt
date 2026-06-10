package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class LoginRequest(
    val username: String,
    val password: String,
)

data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    val user: UserDto,
)

data class OperationPermissionDto(
    val module: String,
    @Json(name = "can_create") val canCreate: Boolean = false,
    @Json(name = "can_edit") val canEdit: Boolean = false,
    @Json(name = "can_delete") val canDelete: Boolean = false,
    @Json(name = "can_export") val canExport: Boolean = false,
    @Json(name = "can_approve") val canApprove: Boolean = false,
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @Json(name = "full_name") val fullName: String? = null,
    val role: String,
    @Json(name = "is_active") val isActive: Boolean = true,
    val permissions: List<String> = emptyList(),
    @Json(name = "menu_codes") val menuCodes: List<String> = emptyList(),
    @Json(name = "operation_permissions") val operationPermissions: List<OperationPermissionDto> = emptyList(),
    @Json(name = "department_id") val departmentId: Int? = null,
    @Json(name = "department_name") val departmentName: String? = null,
)

data class ApiErrorBody(
    val success: Boolean? = null,
    val error: ApiErrorDetail? = null,
    val detail: String? = null,
)

data class ApiErrorDetail(
    val code: String? = null,
    val message: String? = null,
)
