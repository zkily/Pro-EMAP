package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.OrganizationCreateBodyDto
import com.example.smart_emap.data.model.OrganizationDetailDto
import com.example.smart_emap.data.model.OrganizationTreeNodeDto
import com.example.smart_emap.data.model.OrganizationUpdateBodyDto
import com.example.smart_emap.data.model.UserListItemDto

class SystemOrganizationRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getOrganizationTree(): Result<List<OrganizationTreeNodeDto>> = runCatching {
        apiClient.systemApi().getOrganizationTree()
    }

    suspend fun getOrganization(orgId: Int): Result<OrganizationDetailDto> = runCatching {
        apiClient.systemApi().getOrganization(orgId)
    }

    suspend fun createOrganization(body: OrganizationCreateBodyDto): Result<OrganizationDetailDto> = runCatching {
        apiClient.systemApi().createOrganization(body)
    }

    suspend fun updateOrganization(orgId: Int, body: OrganizationUpdateBodyDto): Result<OrganizationDetailDto> = runCatching {
        apiClient.systemApi().updateOrganization(orgId, body)
    }

    suspend fun deleteOrganization(orgId: Int): Result<Unit> = runCatching {
        apiClient.systemApi().deleteOrganization(orgId)
    }

    suspend fun getOrgMembers(departmentId: Int): Result<List<UserListItemDto>> = runCatching {
        val res = apiClient.systemApi().getUsers(departmentId = departmentId, page = 1, pageSize = 100)
        res.items.orEmpty()
    }
}
