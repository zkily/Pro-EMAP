package com.example.smart_emap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.network.ApiDefaults
import com.example.smart_emap.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val apiBaseUrl: String = ApiDefaults.displayBaseUrl,
    val rememberMe: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUrl = authRepository.getApiBaseUrl(ApiDefaults.displayBaseUrl)
            val apiBaseUrl = ApiDefaults.resolveApiBaseUrl(savedUrl)
            if (apiBaseUrl != savedUrl) {
                authRepository.saveApiBaseUrl(apiBaseUrl)
            }
            val remembered = authRepository.getRememberedCredentials()
            _uiState.update {
                it.copy(
                    apiBaseUrl = apiBaseUrl,
                    rememberMe = remembered.rememberMe,
                    username = remembered.username,
                    password = remembered.password,
                )
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onRememberMeChange(checked: Boolean) {
        _uiState.update { it.copy(rememberMe = checked) }
        if (!checked) {
            viewModelScope.launch { authRepository.clearRememberedCredentials() }
        }
    }

    fun togglePasswordVisible() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        var valid = true

        val usernameError = when {
            state.username.isBlank() -> "ユーザー名またはメールアドレスを入力してください"
            state.username.trim().length < 3 -> "ユーザー名は3文字以上である必要があります"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "パスワードを入力してください"
            state.password.length < 6 -> "パスワードは6文字以上である必要があります"
            else -> null
        }
        if (usernameError != null || passwordError != null) {
            valid = false
            _uiState.update {
                it.copy(usernameError = usernameError, passwordError = passwordError)
            }
        }
        if (!valid) return

        val apiBaseUrl = ApiDefaults.resolveApiBaseUrl(
            state.apiBaseUrl.takeIf { it.isNotBlank() } ?: ApiDefaults.displayBaseUrl,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                username = state.username,
                password = state.password,
                apiBaseUrl = apiBaseUrl,
                rememberMe = state.rememberMe,
            )
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "ログインに失敗しました")
                    }
                },
            )
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    }
}
