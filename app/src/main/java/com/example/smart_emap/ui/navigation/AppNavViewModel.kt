package com.example.smart_emap.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 跨屏幕旋转保持登录态，避免重建时误跳回登录页导致标签页丢失。 */
class AppNavViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _sessionReady = MutableStateFlow(false)
    val sessionReady: StateFlow<Boolean> = _sessionReady.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUser.value = authRepository.restoreSession()
            _sessionReady.value = true
        }
    }

    fun onLoginSuccess() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getSavedUser()
        }
    }

    fun onLogout() {
        _currentUser.value = null
    }

    class Factory(
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppNavViewModel::class.java)) {
                return AppNavViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
