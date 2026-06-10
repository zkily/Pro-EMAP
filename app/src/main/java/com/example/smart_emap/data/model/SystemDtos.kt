package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class UserCreateBodyDto(
    val username: String,
    val email: String,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "department_id") val departmentId: Int? = null,
    @Json(name = "role_id") val roleId: Int,
    @Json(name = "two_factor_enabled") val twoFactorEnabled: Boolean = false,
    val password: String,
)

data class UserUpdateBodyDto(
    val email: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "department_id") val departmentId: Int? = null,
    @Json(name = "role_id") val roleId: Int? = null,
    @Json(name = "two_factor_enabled") val twoFactorEnabled: Boolean? = null,
)

data class ResetPasswordBodyDto(
    @Json(name = "new_password") val newPassword: String,
)

data class RoleListItemDto(
    val id: Int,
    val name: String,
    @Json(name = "is_system") val isSystem: Boolean = false,
    @Json(name = "user_count") val userCount: Int = 0,
)

data class MenuTreeNodeDto(
    val id: Int,
    val code: String,
    val label: String,
    val children: List<MenuTreeNodeDto> = emptyList(),
)

data class MenuItemDto(
    val id: Int,
    val code: String,
    val name: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    val path: String? = null,
    val icon: String? = null,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
)

data class RoleDetailDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    @Json(name = "is_system") val isSystem: Boolean = false,
    @Json(name = "data_scope") val dataScope: String = "department",
    @Json(name = "custom_departments") val customDepartments: List<String>? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "user_count") val userCount: Int = 0,
    @Json(name = "menu_permissions") val menuPermissions: List<Int> = emptyList(),
    @Json(name = "operation_permissions") val operationPermissions: List<OperationPermissionDto> = emptyList(),
)

data class RoleCreateBodyDto(
    val name: String,
    val description: String? = null,
)

data class RoleUpdateBodyDto(
    val name: String? = null,
    val description: String? = null,
    @Json(name = "data_scope") val dataScope: String? = null,
    @Json(name = "custom_departments") val customDepartments: List<String>? = null,
    @Json(name = "menu_permissions") val menuPermissions: List<Int>? = null,
    @Json(name = "operation_permissions") val operationPermissions: List<OperationPermissionDto>? = null,
)

data class OrganizationDto(
    val id: Int,
    val code: String,
    val name: String,
    val type: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
)

data class OrganizationTreeNodeDto(
    val id: Int,
    val code: String,
    val name: String,
    val type: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    val children: List<OrganizationTreeNodeDto> = emptyList(),
)

data class OrganizationDetailDto(
    val id: Int,
    val code: String,
    val name: String,
    val type: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    @Json(name = "manager_name") val managerName: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val description: String? = null,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    @Json(name = "is_active") val isActive: Boolean = true,
)

data class OrganizationCreateBodyDto(
    val code: String,
    val name: String,
    val type: String,
    @Json(name = "parent_id") val parentId: Int? = null,
    @Json(name = "manager_name") val managerName: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val description: String? = null,
)

data class OrganizationUpdateBodyDto(
    val name: String? = null,
    val type: String? = null,
    @Json(name = "parent_id") val parentId: Int? = null,
    @Json(name = "manager_name") val managerName: String? = null,
    val location: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val description: String? = null,
)
