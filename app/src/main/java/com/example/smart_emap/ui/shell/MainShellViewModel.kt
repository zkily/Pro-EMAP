package com.example.smart_emap.ui.shell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smart_emap.core.auth.canAccessPath
import com.example.smart_emap.core.auth.resolveAccessiblePath
import com.example.smart_emap.data.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val KEY_ACTIVE_PATH = "shell_active_path"
private const val KEY_TABS = "shell_tabs"

private val TabRecordSeparator = "\u001E"
private val TabFieldSeparator = "\u001F"

private fun encodeTabs(tabs: List<ShellTab>): String =
    tabs.joinToString(TabRecordSeparator) { tab ->
        "${tab.path}$TabFieldSeparator${tab.title}$TabFieldSeparator${tab.closable}"
    }

private fun decodeTabs(raw: String?): List<ShellTab>? {
    if (raw.isNullOrBlank()) return null
    return raw.split(TabRecordSeparator).mapNotNull { part ->
        val fields = part.split(TabFieldSeparator, limit = 3)
        if (fields.size != 3) return@mapNotNull null
        ShellTab(
            path = fields[0],
            title = fields[1],
            closable = fields[2].toBooleanStrictOrNull() ?: true,
        )
    }.takeIf { it.isNotEmpty() }
}

data class MainShellUiState(
    val isSidebarCollapsed: Boolean = true,
    val activePath: String = "/dashboard",
    val tabs: List<ShellTab> = listOf(
        ShellTab(path = "/dashboard", title = "ダッシュボード", closable = false),
    ),
)

class MainShellViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<MainShellUiState> = _uiState.asStateFlow()

    private fun restoreState(): MainShellUiState {
        val activePath = savedStateHandle.get<String>(KEY_ACTIVE_PATH) ?: "/dashboard"
        val tabs = decodeTabs(savedStateHandle.get<String>(KEY_TABS))
            ?: listOf(ShellTab(path = "/dashboard", title = "ダッシュボード", closable = false))
        return MainShellUiState(
            activePath = activePath,
            tabs = tabs,
        )
    }

    private fun persist(state: MainShellUiState) {
        savedStateHandle[KEY_ACTIVE_PATH] = state.activePath
        savedStateHandle[KEY_TABS] = encodeTabs(state.tabs)
    }

    private fun updateState(transform: (MainShellUiState) -> MainShellUiState) {
        _uiState.update { current ->
            val next = transform(current)
            persist(next)
            next
        }
    }

    fun setSidebarCollapsed(collapsed: Boolean) {
        _uiState.update { it.copy(isSidebarCollapsed = collapsed) }
    }

    fun navigateTo(path: String, user: UserDto) {
        val normalizedPath = user.resolveAccessiblePath(path)
        val title = titleForAccessiblePath(normalizedPath)
        updateState { state ->
            val newTabs = if (state.tabs.none { it.path == normalizedPath }) {
                state.tabs + ShellTab(path = normalizedPath, title = title)
            } else {
                state.tabs
            }
            state.copy(activePath = normalizedPath, tabs = newTabs)
        }
    }

    fun enforceUserAccess(user: UserDto) {
        updateState { state ->
            val sanitizedTabs = state.tabs.filter { tab ->
                user.canAccessPath(tab.path) || tab.path == "/access-denied"
            }
            val tabs = if (sanitizedTabs.isEmpty()) {
                listOf(ShellTab(path = "/dashboard", title = "ダッシュボード", closable = false))
            } else {
                sanitizedTabs
            }
            val activePath = if (user.canAccessPath(state.activePath)) {
                state.activePath
            } else {
                tabs.first().path
            }
            state.copy(activePath = activePath, tabs = tabs)
        }
    }

    private fun titleForAccessiblePath(path: String): String = when (path) {
        "/access-denied" -> "アクセス拒否"
        else -> AppMenuConfig.titleForPath(path)
    }

    fun selectTab(path: String) {
        updateState { it.copy(activePath = path) }
    }

    fun closeTab(path: String) {
        updateState { state ->
            val closingIndex = state.tabs.indexOfFirst { it.path == path }
            if (closingIndex < 0) return@updateState state
            val newTabs = state.tabs.filterNot { it.path == path }
            val newActivePath = if (state.activePath == path) {
                newTabs.getOrNull(closingIndex - 1)?.path ?: newTabs.first().path
            } else {
                state.activePath
            }
            state.copy(tabs = newTabs, activePath = newActivePath)
        }
    }

    fun closeOtherTabs() {
        updateState { state ->
            state.copy(tabs = state.tabs.filter { !it.closable || it.path == state.activePath })
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(MainShellViewModel::class.java)) {
                return MainShellViewModel(extras.createSavedStateHandle()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
