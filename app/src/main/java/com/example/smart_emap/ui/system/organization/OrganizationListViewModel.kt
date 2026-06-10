package com.example.smart_emap.ui.system.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OrganizationCreateBodyDto
import com.example.smart_emap.data.model.OrganizationDetailDto
import com.example.smart_emap.data.model.OrganizationTreeNodeDto
import com.example.smart_emap.data.model.OrganizationUpdateBodyDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.SystemOrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizationFormState(
    val id: Int = 0,
    val code: String = "",
    val name: String = "",
    val type: String = "department",
    val parentId: Int? = null,
    val managerName: String = "",
    val location: String = "",
    val phone: String = "",
    val email: String = "",
    val description: String = "",
)

data class OrganizationListUiState(
    val isTreeLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val orgTree: List<OrganizationTreeNodeDto> = emptyList(),
    val expandedIds: Set<Int> = emptySet(),
    val selectedId: Int? = null,
    val selectedOrg: OrganizationDetailDto? = null,
    val orgUsers: List<UserListItemDto> = emptyList(),
    val showFormDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val form: OrganizationFormState = OrganizationFormState(),
    val showDeleteConfirm: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)

class OrganizationListViewModel(
    private val repository: SystemOrganizationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizationListUiState())
    val uiState: StateFlow<OrganizationListUiState> = _uiState.asStateFlow()

    val orgCount: Int get() = countOrgNodes(_uiState.value.orgTree)
    val deptCount: Int get() = countDeptNodes(_uiState.value.orgTree)

    init {
        refreshTree()
    }

    fun refreshTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTreeLoading = true, errorMessage = null) }
            repository.getOrganizationTree()
                .onSuccess { tree ->
                    val expanded = collectAllNodeIds(tree)
                    _uiState.update {
                        it.copy(
                            orgTree = tree,
                            expandedIds = expanded,
                            isTreeLoading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isTreeLoading = false,
                            errorMessage = e.message ?: "組織ツリーの取得に失敗しました",
                        )
                    }
                }
        }
    }

    fun toggleExpand(nodeId: Int) {
        _uiState.update { state ->
            val next = state.expandedIds.toMutableSet()
            if (next.contains(nodeId)) next.remove(nodeId) else next.add(nodeId)
            state.copy(expandedIds = next)
        }
    }

    fun selectOrg(node: OrganizationTreeNodeDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedId = node.id, isDetailLoading = true) }
            repository.getOrganization(node.id)
                .onSuccess { org ->
                    _uiState.update { it.copy(selectedOrg = org, isDetailLoading = false) }
                    loadMembers(org.id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            snackbarMessage = e.message ?: "組織詳細の取得に失敗しました",
                        )
                    }
                }
        }
    }

    private fun loadMembers(departmentId: Int) {
        viewModelScope.launch {
            repository.getOrgMembers(departmentId)
                .onSuccess { users -> _uiState.update { it.copy(orgUsers = users) } }
                .onFailure { _uiState.update { it.copy(orgUsers = emptyList()) } }
        }
    }

    fun parentName(parentId: Int?): String? {
        if (parentId == null) return null
        return findOrgName(_uiState.value.orgTree, parentId)
    }

    fun openCreate() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = false,
                form = OrganizationFormState(),
            )
        }
    }

    fun openEdit() {
        val org = _uiState.value.selectedOrg ?: return
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = true,
                form = org.toFormState(),
            )
        }
    }

    private fun OrganizationDetailDto.toFormState() = OrganizationFormState(
        id = id,
        code = code,
        name = name,
        type = type,
        parentId = parentId,
        managerName = managerName.orEmpty(),
        location = location.orEmpty(),
        phone = phone.orEmpty(),
        email = email.orEmpty(),
        description = description.orEmpty(),
    )

    fun openEditFromNode(node: OrganizationTreeNodeDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedId = node.id, isDetailLoading = true) }
            repository.getOrganization(node.id)
                .onSuccess { org ->
                    _uiState.update {
                        it.copy(
                            selectedOrg = org,
                            isDetailLoading = false,
                            showFormDialog = true,
                            isEditMode = true,
                            form = org.toFormState(),
                        )
                    }
                    loadMembers(org.id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            snackbarMessage = e.message ?: "組織詳細の取得に失敗しました",
                        )
                    }
                }
        }
    }

    fun dismissForm() {
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun updateForm(transform: (OrganizationFormState) -> OrganizationFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun submitForm() {
        val state = _uiState.value
        val form = state.form
        if (form.code.isBlank() && !state.isEditMode) {
            _uiState.update { it.copy(snackbarMessage = "組織コードを入力してください") }
            return
        }
        if (form.name.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "組織名を入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val parentId = form.parentId?.takeIf { it > 0 }
            val result = if (state.isEditMode) {
                repository.updateOrganization(
                    form.id,
                    OrganizationUpdateBodyDto(
                        name = form.name,
                        type = form.type,
                        parentId = parentId,
                        managerName = form.managerName.takeIf { it.isNotBlank() },
                        location = form.location.takeIf { it.isNotBlank() },
                        phone = form.phone.takeIf { it.isNotBlank() },
                        email = form.email.takeIf { it.isNotBlank() },
                        description = form.description.takeIf { it.isNotBlank() },
                    ),
                )
            } else {
                repository.createOrganization(
                    OrganizationCreateBodyDto(
                        code = form.code.trim(),
                        name = form.name.trim(),
                        type = form.type,
                        parentId = parentId,
                        managerName = form.managerName.takeIf { it.isNotBlank() },
                        location = form.location.takeIf { it.isNotBlank() },
                        phone = form.phone.takeIf { it.isNotBlank() },
                        email = form.email.takeIf { it.isNotBlank() },
                        description = form.description.takeIf { it.isNotBlank() },
                    ),
                )
            }
            result
                .onSuccess { saved ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showFormDialog = false,
                            selectedId = saved.id,
                            selectedOrg = saved,
                            snackbarMessage = if (state.isEditMode) "更新しました" else "登録しました",
                        )
                    }
                    refreshTree()
                    loadMembers(saved.id)
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

    fun requestDelete() {
        if (_uiState.value.selectedOrg != null) {
            _uiState.update { it.copy(showDeleteConfirm = true) }
        }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDelete() {
        val orgId = _uiState.value.selectedOrg?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showDeleteConfirm = false) }
            repository.deleteOrganization(orgId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            selectedOrg = null,
                            selectedId = null,
                            orgUsers = emptyList(),
                            snackbarMessage = "削除しました",
                        )
                    }
                    refreshTree()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = e.message ?: "削除に失敗しました",
                        )
                    }
                }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val repository: SystemOrganizationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrganizationListViewModel(repository) as T
        }
    }
}
