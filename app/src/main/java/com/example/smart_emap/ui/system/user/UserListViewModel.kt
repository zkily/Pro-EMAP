package com.example.smart_emap.ui.system.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OrganizationDto
import com.example.smart_emap.data.model.RoleListItemDto
import com.example.smart_emap.data.model.UserCreateBodyDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.UserUpdateBodyDto
import com.example.smart_emap.data.repository.SystemUserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserFormState(
    val id: Int = 0,
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val departmentId: Int? = null,
    val roleId: Int? = null,
    val twoFactorEnabled: Boolean = false,
    val password: String = "",
)

data class ResetPasswordFormState(
    val userId: Int = 0,
    val newPassword: String = "",
    val confirmPassword: String = "",
)

data class UserListUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val users: List<UserListItemDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 10,
    val pages: Int = 1,
    val keyword: String = "",
    val departmentId: Int? = null,
    val statusFilter: String = "",
    val roles: List<RoleListItemDto> = emptyList(),
    val departments: List<OrganizationDto> = emptyList(),
    val showFormDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val form: UserFormState = UserFormState(),
    val showResetPasswordDialog: Boolean = false,
    val resetPasswordForm: ResetPasswordFormState = ResetPasswordFormState(),
    val pendingPrintHtml: String? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)

class UserListViewModel(
    private val repository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadOptions()
        refreshUsers()
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            repository.getUsers(
                keyword = state.keyword,
                departmentId = state.departmentId,
                status = state.statusFilter.takeIf { it.isNotBlank() },
                page = state.page,
                pageSize = state.pageSize,
            ).fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = res.items.orEmpty(),
                            total = res.total ?: 0,
                            page = res.page ?: state.page,
                            pageSize = res.pageSize ?: state.pageSize,
                            pages = res.pages ?: 1,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "ユーザー一覧の取得に失敗しました")
                    }
                },
            )
        }
    }

    fun loadOptions() {
        viewModelScope.launch {
            val roles = repository.getRoles().getOrElse { emptyList() }
            val orgs = repository.getOrganizations().getOrElse { emptyList() }
                .filter { it.type in setOf("company", "site", "department") }
            _uiState.update { it.copy(roles = roles, departments = orgs) }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value, page = 1) }
        debouncedRefresh()
    }

    fun setDepartmentFilter(departmentId: Int?) {
        _uiState.update { it.copy(departmentId = departmentId, page = 1) }
        refreshUsers()
    }

    fun setStatusFilter(status: String) {
        _uiState.update { it.copy(statusFilter = status, page = 1) }
        refreshUsers()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page.coerceAtLeast(1)) }
        refreshUsers()
    }

    fun setPageSize(pageSize: Int) {
        _uiState.update { it.copy(pageSize = pageSize, page = 1) }
        refreshUsers()
    }

    private fun debouncedRefresh() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refreshUsers()
        }
    }

    fun openCreate() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = false,
                form = UserFormState(),
            )
        }
        loadOptions()
    }

    fun openEdit(user: UserListItemDto) {
        val roleName = roleLabel(user.role)
        val matchedRole = _uiState.value.roles.find { it.name == roleName }
            ?: _uiState.value.roles.find { roleCodeFromJapaneseName(it.name) == user.role?.lowercase() }
        val dept = _uiState.value.departments.find { it.name == user.department }
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEditMode = true,
                form = UserFormState(
                    id = user.id ?: 0,
                    username = user.username.orEmpty(),
                    fullName = user.fullName.orEmpty(),
                    email = user.email.orEmpty(),
                    departmentId = dept?.id,
                    roleId = matchedRole?.id,
                    twoFactorEnabled = user.twoFactor == true,
                ),
            )
        }
        loadOptions()
    }

    fun updateForm(transform: (UserFormState) -> UserFormState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun dismissForm() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(showFormDialog = false) }
    }

    fun submitForm() {
        val state = _uiState.value
        val form = state.form
        if (form.email.isBlank() || form.fullName.isBlank() || form.roleId == null) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        if (!state.isEditMode) {
            if (form.username.isBlank() || form.password.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "ユーザー名とパスワードは必須です") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (state.isEditMode) {
                repository.updateUser(
                    form.id,
                    UserUpdateBodyDto(
                        email = form.email,
                        fullName = form.fullName,
                        departmentId = form.departmentId,
                        roleId = form.roleId,
                        twoFactorEnabled = form.twoFactorEnabled,
                    ),
                )
            } else {
                repository.createUser(
                    UserCreateBodyDto(
                        username = form.username,
                        email = form.email,
                        fullName = form.fullName,
                        departmentId = form.departmentId,
                        roleId = form.roleId!!,
                        twoFactorEnabled = form.twoFactorEnabled,
                        password = form.password,
                    ),
                )
            }
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showFormDialog = false,
                            snackbarMessage = if (state.isEditMode) "ユーザーを更新しました" else "ユーザーを登録しました",
                        )
                    }
                    refreshUsers()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, snackbarMessage = e.message ?: "保存に失敗しました")
                    }
                },
            )
        }
    }

    fun toggleLock(user: UserListItemDto, currentUserId: Int) {
        val userId = user.id ?: return
        if (user.status != "locked" && userId == currentUserId) {
            _uiState.update { it.copy(snackbarMessage = "自分自身をロックすることはできません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (user.status == "locked") {
                repository.unlockUser(userId)
            } else {
                repository.lockUser(userId)
            }
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = if (user.status == "locked") "ロックを解除しました" else "ユーザーをロックしました",
                        )
                    }
                    refreshUsers()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, snackbarMessage = e.message ?: "操作に失敗しました")
                    }
                },
            )
        }
    }

    fun openResetPassword(user: UserListItemDto) {
        _uiState.update {
            it.copy(
                showResetPasswordDialog = true,
                resetPasswordForm = ResetPasswordFormState(userId = user.id ?: 0),
            )
        }
    }

    fun updateResetPasswordForm(transform: (ResetPasswordFormState) -> ResetPasswordFormState) {
        _uiState.update { it.copy(resetPasswordForm = transform(it.resetPasswordForm)) }
    }

    fun dismissResetPassword() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(showResetPasswordDialog = false) }
    }

    fun submitResetPassword() {
        val form = _uiState.value.resetPasswordForm
        if (form.newPassword.length < 8) {
            _uiState.update { it.copy(snackbarMessage = "パスワードは8文字以上で入力してください") }
            return
        }
        if (form.newPassword != form.confirmPassword) {
            _uiState.update { it.copy(snackbarMessage = "パスワードが一致しません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            repository.resetPassword(form.userId, form.newPassword).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showResetPasswordDialog = false,
                            snackbarMessage = "パスワードを再設定しました",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, snackbarMessage = e.message ?: "パスワード再設定に失敗しました")
                    }
                },
            )
        }
    }

    fun printUsers() {
        val users = _uiState.value.users
        if (users.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        _uiState.update { it.copy(pendingPrintHtml = buildUserListPrintHtml(users)) }
    }

    fun clearPendingPrintHtml() {
        _uiState.update { it.copy(pendingPrintHtml = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    val activeCount: Int get() = _uiState.value.users.count { it.status == "active" }
    val lockedCount: Int get() = _uiState.value.users.count { it.status == "locked" }

    class Factory(
        private val repository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserListViewModel::class.java)) {
                return UserListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
