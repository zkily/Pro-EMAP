package com.example.smart_emap.ui.system.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OperationPermissionDto
import com.example.smart_emap.data.model.RoleCreateBodyDto
import com.example.smart_emap.data.model.RoleDetailDto
import com.example.smart_emap.data.model.RoleListItemDto
import com.example.smart_emap.data.model.RoleUpdateBodyDto
import com.example.smart_emap.data.repository.SystemRoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoleFormState(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
)

data class RolePermissionUiState(
    val isRolesLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isMenuLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSubmitting: Boolean = false,
    val roleList: List<RoleListItemDto> = emptyList(),
    val selectedRoleId: Int? = null,
    val selectedRole: RoleDetailDto? = null,
    val permissionMenuTree: List<PermissionMenuNode> = emptyList(),
    val menuCodeToId: Map<String, Int> = emptyMap(),
    val expandedMenuCodes: Set<String> = emptySet(),
    val checkedMenuCodes: Set<String> = emptySet(),
    val operationPermissions: List<OperationPermissionDto> = emptyList(),
    val dataScope: String = "department",
    val customDepartments: List<String> = emptyList(),
    val departmentOptions: List<Pair<Int, String>> = emptyList(),
    val activeTab: Int = 0,
    val showFormDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val form: RoleFormState = RoleFormState(),
    val showDeleteConfirm: RoleListItemDto? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)

class RolePermissionViewModel(
    private val repository: SystemRoleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RolePermissionUiState())
    val uiState: StateFlow<RolePermissionUiState> = _uiState.asStateFlow()

    val totalUserCount: Int get() = _uiState.value.roleList.sumOf { it.userCount }

    init {
        refreshAll()
    }

    fun refreshAll() {
        refreshRoles()
        refreshPermissionMenuTree()
        refreshDepartments()
    }

    fun refreshRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRolesLoading = true, errorMessage = null) }
            repository.getRoles()
                .onSuccess { list ->
                    _uiState.update { it.copy(roleList = list, isRolesLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isRolesLoading = false,
                            errorMessage = e.message ?: "ロール一覧の取得に失敗しました",
                        )
                    }
                }
        }
    }

    private fun refreshPermissionMenuTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMenuLoading = true) }
            repository.getMenus()
                .onSuccess { menus ->
                    val codeToId = buildMenuCodeToIdMap(menus.map { it.code to it.id })
                    val tree = buildPermissionMenuTree(codeToId)
                    _uiState.update {
                        it.copy(
                            permissionMenuTree = tree,
                            menuCodeToId = codeToId,
                            expandedMenuCodes = collectAllPermissionMenuCodes(tree),
                            isMenuLoading = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isMenuLoading = false) }
                }
        }
    }

    private fun refreshDepartments() {
        viewModelScope.launch {
            repository.getDepartmentOptions()
                .onSuccess { options ->
                    _uiState.update { it.copy(departmentOptions = options) }
                }
        }
    }

    fun selectRole(role: RoleListItemDto) {
        if (_uiState.value.selectedRoleId == role.id) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedRoleId = role.id,
                    isDetailLoading = true,
                    selectedRole = null,
                )
            }
            repository.getRole(role.id)
                .onSuccess { detail ->
                    val idToCode = buildIdToCodeMap(_uiState.value.menuCodeToId)
                    _uiState.update {
                        it.copy(
                            selectedRole = detail,
                            checkedMenuCodes = menuIdsToCheckedCodes(
                                detail.menuPermissions,
                                idToCode,
                                it.permissionMenuTree,
                            ),
                            operationPermissions = buildOperationPermissionsFromRole(detail),
                            dataScope = detail.dataScope,
                            customDepartments = detail.customDepartments.orEmpty(),
                            isDetailLoading = false,
                            activeTab = 0,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            selectedRoleId = null,
                            isDetailLoading = false,
                            snackbarMessage = e.message ?: "ロール詳細の取得に失敗しました",
                        )
                    }
                }
        }
    }

    fun toggleMenuExpand(code: String) {
        _uiState.update { state ->
            val expanded = state.expandedMenuCodes.toMutableSet()
            if (code in expanded) expanded.remove(code) else expanded.add(code)
            state.copy(expandedMenuCodes = expanded)
        }
    }

    fun toggleMenuCheck(code: String, checked: Boolean) {
        _uiState.update { state ->
            state.copy(
                checkedMenuCodes = toggleMenuCheckByCode(
                    state.permissionMenuTree,
                    state.checkedMenuCodes,
                    code,
                    checked,
                ),
            )
        }
    }

    fun updateOperationPermission(module: String, field: String, value: Boolean) {
        _uiState.update { state ->
            state.copy(
                operationPermissions = state.operationPermissions.map { op ->
                    if (op.module != module) op
                    else when (field) {
                        "create" -> op.copy(canCreate = value)
                        "edit" -> op.copy(canEdit = value)
                        "delete" -> op.copy(canDelete = value)
                        "export" -> op.copy(canExport = value)
                        "approve" -> op.copy(canApprove = value)
                        else -> op
                    }
                },
            )
        }
    }

    fun setDataScope(scope: String) {
        _uiState.update { it.copy(dataScope = scope) }
    }

    fun toggleCustomDepartment(name: String) {
        _uiState.update { state ->
            val current = state.customDepartments.toMutableList()
            if (name in current) current.remove(name) else current.add(name)
            state.copy(customDepartments = current)
        }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun openAddRole() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = false,
                form = RoleFormState(),
            )
        }
    }

    fun openEditRole(role: RoleListItemDto) {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = true,
                form = RoleFormState(id = role.id, name = role.name, description = ""),
            )
        }
    }

    fun dismissForm() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun updateForm(transform: (RoleFormState) -> RoleFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun submitRole() {
        val form = _uiState.value.form
        if (form.name.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "ロール名を入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (form.id > 0) {
                repository.updateRole(
                    form.id,
                    RoleUpdateBodyDto(
                        name = form.name,
                        description = form.description.ifBlank { null },
                    ),
                )
            } else {
                repository.createRole(
                    RoleCreateBodyDto(
                        name = form.name,
                        description = form.description.ifBlank { null },
                    ),
                )
            }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showFormDialog = false,
                            snackbarMessage = if (form.id > 0) "保存しました" else "ロールを作成しました",
                        )
                    }
                    refreshRoles()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = e.message ?: "保存に失敗しました",
                        )
                    }
                }
        }
    }

    fun requestDeleteRole(role: RoleListItemDto) {
        if (role.isSystem) return
        _uiState.update { it.copy(showDeleteConfirm = role) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = null) }
    }

    fun confirmDeleteRole() {
        val role = _uiState.value.showDeleteConfirm ?: return
        viewModelScope.launch {
            repository.deleteRole(role.id)
                .onSuccess {
                    val clearSelection = _uiState.value.selectedRoleId == role.id
                    _uiState.update {
                        it.copy(
                            showDeleteConfirm = null,
                            snackbarMessage = "削除しました",
                            selectedRoleId = if (clearSelection) null else it.selectedRoleId,
                            selectedRole = if (clearSelection) null else it.selectedRole,
                        )
                    }
                    refreshRoles()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            showDeleteConfirm = null,
                            snackbarMessage = e.message ?: "削除に失敗しました",
                        )
                    }
                }
        }
    }

    fun savePermissions() {
        val role = _uiState.value.selectedRole ?: return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val menuPermissions = menuCodesToMenuIdsForSave(
                state.checkedMenuCodes,
                state.menuCodeToId,
            )
            repository.updateRole(
                role.id,
                RoleUpdateBodyDto(
                    menuPermissions = menuPermissions,
                    operationPermissions = state.operationPermissions,
                    dataScope = state.dataScope,
                    customDepartments = if (state.dataScope == "custom") state.customDepartments else null,
                ),
            )
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            selectedRole = updated,
                            checkedMenuCodes = state.checkedMenuCodes,
                            operationPermissions = buildOperationPermissionsFromRole(updated),
                            dataScope = updated.dataScope,
                            customDepartments = updated.customDepartments.orEmpty(),
                            snackbarMessage = "権限を保存しました",
                        )
                    }
                    refreshRoles()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = e.message ?: "保存に失敗しました",
                        )
                    }
                }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val repository: SystemRoleRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RolePermissionViewModel::class.java)) {
                return RolePermissionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
