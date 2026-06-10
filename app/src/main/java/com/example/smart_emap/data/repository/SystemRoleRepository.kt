package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.MenuItemDto
import com.example.smart_emap.data.model.RoleCreateBodyDto
import com.example.smart_emap.data.model.RoleDetailDto
import com.example.smart_emap.data.model.RoleListItemDto
import com.example.smart_emap.data.model.RoleUpdateBodyDto

class SystemRoleRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getRoles(): Result<List<RoleListItemDto>> = runCatching {
        apiClient.systemApi().getRoles()
    }

    suspend fun getRole(roleId: Int): Result<RoleDetailDto> = runCatching {
        apiClient.systemApi().getRole(roleId)
    }

    suspend fun createRole(body: RoleCreateBodyDto): Result<RoleDetailDto> = runCatching {
        apiClient.systemApi().createRole(body)
    }

    suspend fun updateRole(roleId: Int, body: RoleUpdateBodyDto): Result<RoleDetailDto> = runCatching {
        apiClient.systemApi().updateRole(roleId, body)
    }

    suspend fun deleteRole(roleId: Int): Result<Unit> = runCatching {
        apiClient.systemApi().deleteRole(roleId)
    }

    suspend fun getMenus(): Result<List<MenuItemDto>> = runCatching {
        apiClient.systemApi().getMenus()
    }

    suspend fun getDepartmentOptions(): Result<List<Pair<Int, String>>> = runCatching {
        apiClient.systemApi().getOrganizations()
            .filter { it.type == "department" || it.type == "site" }
            .map { it.id to it.name }
    }
}
