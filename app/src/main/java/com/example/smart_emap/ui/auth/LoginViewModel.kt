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
    val apiBaseUrl: String = "",
    val rememberMe: Boolean = false,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val apiBaseUrlError: String? = null,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUrl = authRepository.getApiBaseUrl(ApiDefaults.displayBaseUrl)
            val remembered = authRepository.getRememberedCredentials()
            _uiState.update {
                it.copy(
                    apiBaseUrl = ApiDefaults.resolvePresetSelection(savedUrl),
                    rememberMe = remembered.rememberMe,
                    username = remembered.username,
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

    fun onApiBaseUrlChange(value: String) {
        val selected = ApiDefaults.matchPreset(value) ?: ApiDefaults.ensureTrailingSlash(value)
        _uiState.update { it.copy(apiBaseUrl = selected, apiBaseUrlError = null, errorMessage = null) }
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
        val apiBaseUrlError = validateApiBaseUrl(state.apiBaseUrl)
        if (usernameError != null || passwordError != null || apiBaseUrlError != null) {
            valid = false
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    passwordError = passwordError,
                    apiBaseUrlError = apiBaseUrlError,
                )
            }
        }
        if (!valid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val apiBaseUrl = ApiDefaults.ensureTrailingSlash(
                ApiDefaults.migrateDevApiUrl(state.apiBaseUrl.trim().trimEnd('/')),
            )
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

    fun canAttemptQrLogin(): Boolean {
        val apiBaseUrlError = validateApiBaseUrl(_uiState.value.apiBaseUrl)
        if (apiBaseUrlError != null) {
            _uiState.update { it.copy(apiBaseUrlError = apiBaseUrlError, errorMessage = apiBaseUrlError) }
            return false
        }
        return true
    }

    fun loginWithScannedQr(code: String, onSuccess: () -> Unit) {
        if (!canAttemptQrLogin()) return
        when (val parsed = LoginQrPayload.parse(code)) {
            is LoginQrPayload.Parsed.TokenLogin -> {
                _uiState.update {
                    it.copy(username = parsed.username, usernameError = null, errorMessage = null)
                }
                performQrLogin(code, onSuccess)
            }
            is LoginQrPayload.Parsed.PasswordLogin -> {
                _uiState.update {
                    it.copy(
                        username = parsed.username,
                        password = parsed.password,
                        usernameError = null,
                        passwordError = null,
                        errorMessage = null,
                    )
                }
                login(onSuccess)
            }
            is LoginQrPayload.Parsed.UsernameOnly -> {
                _uiState.update {
                    it.copy(
                        username = parsed.username,
                        usernameError = null,
                        errorMessage = "ユーザー名を読み取りました。パスワードを入力してください",
                    )
                }
            }
            LoginQrPayload.Parsed.Invalid -> {
                _uiState.update {
                    it.copy(errorMessage = "ログイン用のQRコードではありません")
                }
            }
        }
    }

    private fun performQrLogin(code: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val apiBaseUrl = ApiDefaults.ensureTrailingSlash(
                ApiDefaults.migrateDevApiUrl(state.apiBaseUrl.trim().trimEnd('/')),
            )
            val result = authRepository.qrLogin(
                code = code,
                apiBaseUrl = apiBaseUrl,
                rememberMe = state.rememberMe,
            )
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "QRログインに失敗しました")
                    }
                },
            )
        }
    }

    private fun validateApiBaseUrl(url: String): String? = when {
        url.isBlank() -> "API サーバーアドレスを入力してください"
        !url.trim().matches(Regex("^https?://\\S+$", RegexOption.IGNORE_CASE)) ->
            "http:// または https:// で始まる URL を入力してください"
        else -> null
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
