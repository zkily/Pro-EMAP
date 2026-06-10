package com.example.smart_emap.data.api

import com.example.smart_emap.data.model.OrganizationCreateBodyDto
import com.example.smart_emap.data.model.OrganizationDetailDto
import com.example.smart_emap.data.model.OrganizationDto
import com.example.smart_emap.data.model.OrganizationTreeNodeDto
import com.example.smart_emap.data.model.OrganizationUpdateBodyDto
import com.example.smart_emap.data.model.MenuItemDto
import com.example.smart_emap.data.model.MenuTreeNodeDto
import com.example.smart_emap.data.model.ResetPasswordBodyDto
import com.example.smart_emap.data.model.RoleCreateBodyDto
import com.example.smart_emap.data.model.RoleDetailDto
import com.example.smart_emap.data.model.RoleListItemDto
import com.example.smart_emap.data.model.RoleUpdateBodyDto
import com.example.smart_emap.data.model.UserCreateBodyDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.UserListResponse
import com.example.smart_emap.data.model.UserUpdateBodyDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SystemApiService {
    @GET("/api/system/users")
    suspend fun getUsers(
        @Query("keyword") keyword: String? = null,
        @Query("department_id") departmentId: Int? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
    ): UserListResponse

    @POST("/api/system/users")
    suspend fun createUser(@Body body: UserCreateBodyDto): UserListItemDto

    @PUT("/api/system/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Int,
        @Body body: UserUpdateBodyDto,
    ): UserListItemDto

    @POST("/api/system/users/{userId}/lock")
    suspend fun lockUser(@Path("userId") userId: Int)

    @POST("/api/system/users/{userId}/unlock")
    suspend fun unlockUser(@Path("userId") userId: Int)

    @POST("/api/system/users/{userId}/reset-password")
    suspend fun resetPassword(
        @Path("userId") userId: Int,
        @Body body: ResetPasswordBodyDto,
    )

    @GET("/api/system/roles")
    suspend fun getRoles(): List<RoleListItemDto>

    @GET("/api/system/roles/{roleId}")
    suspend fun getRole(@Path("roleId") roleId: Int): RoleDetailDto

    @POST("/api/system/roles")
    suspend fun createRole(@Body body: RoleCreateBodyDto): RoleDetailDto

    @PUT("/api/system/roles/{roleId}")
    suspend fun updateRole(
        @Path("roleId") roleId: Int,
        @Body body: RoleUpdateBodyDto,
    ): RoleDetailDto

    @DELETE("/api/system/roles/{roleId}")
    suspend fun deleteRole(@Path("roleId") roleId: Int)

    @GET("/api/system/menus")
    suspend fun getMenus(
        @Query("include_inactive") includeInactive: Boolean = false,
    ): List<MenuItemDto>

    @GET("/api/system/menus/tree")
    suspend fun getMenuTree(): List<MenuTreeNodeDto>

    @GET("/api/system/organizations")
    suspend fun getOrganizations(): List<OrganizationDto>

    @GET("/api/system/organizations/tree")
    suspend fun getOrganizationTree(): List<OrganizationTreeNodeDto>

    @GET("/api/system/organizations/{orgId}")
    suspend fun getOrganization(@Path("orgId") orgId: Int): OrganizationDetailDto

    @POST("/api/system/organizations")
    suspend fun createOrganization(@Body body: OrganizationCreateBodyDto): OrganizationDetailDto

    @PUT("/api/system/organizations/{orgId}")
    suspend fun updateOrganization(
        @Path("orgId") orgId: Int,
        @Body body: OrganizationUpdateBodyDto,
    ): OrganizationDetailDto

    @DELETE("/api/system/organizations/{orgId}")
    suspend fun deleteOrganization(@Path("orgId") orgId: Int)
}

/** @deprecated use [SystemApiService] */
typealias SystemUsersApiService = SystemApiService
