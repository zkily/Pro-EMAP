package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.OrganizationDto
import com.example.smart_emap.data.model.ResetPasswordBodyDto
import com.example.smart_emap.data.model.RoleListItemDto
import com.example.smart_emap.data.model.UserCreateBodyDto
import com.example.smart_emap.data.model.UserListResponse
import com.example.smart_emap.data.model.UserUpdateBodyDto

class SystemUserRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getUsers(
        keyword: String? = null,
        departmentId: Int? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 10,
    ): Result<UserListResponse> = runCatching {
        apiClient.systemApi().getUsers(
            keyword = keyword?.trim()?.takeIf { it.isNotEmpty() },
            departmentId = departmentId,
            status = status?.takeIf { it.isNotEmpty() },
            page = page,
            pageSize = pageSize,
        )
    }

    suspend fun createUser(body: UserCreateBodyDto): Result<Unit> = runCatching {
        apiClient.systemApi().createUser(body)
    }

    suspend fun updateUser(userId: Int, body: UserUpdateBodyDto): Result<Unit> = runCatching {
        apiClient.systemApi().updateUser(userId, body)
    }

    suspend fun lockUser(userId: Int): Result<Unit> = runCatching {
        apiClient.systemApi().lockUser(userId)
    }

    suspend fun unlockUser(userId: Int): Result<Unit> = runCatching {
        apiClient.systemApi().unlockUser(userId)
    }

    suspend fun resetPassword(userId: Int, newPassword: String): Result<Unit> = runCatching {
        apiClient.systemApi().resetPassword(userId, ResetPasswordBodyDto(newPassword))
    }

    suspend fun getRoles(): Result<List<RoleListItemDto>> = runCatching {
        apiClient.systemApi().getRoles()
    }

    suspend fun getOrganizations(): Result<List<OrganizationDto>> = runCatching {
        apiClient.systemApi().getOrganizations()
    }
}
