package com.example.smart_emap.ui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ShortcutItemDto
import com.example.smart_emap.data.repository.SidebarShortcutsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SidebarShortcutsUiState(
    val pinned: List<ShortcutItemDto> = emptyList(),
    val frequent: List<ShortcutItemDto> = emptyList(),
    val loaded: Boolean = false,
) {
    val hasShortcuts: Boolean
        get() = pinned.isNotEmpty() || frequent.isNotEmpty()
}

class SidebarShortcutsViewModel(
    private val repository: SidebarShortcutsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SidebarShortcutsUiState())
    val uiState: StateFlow<SidebarShortcutsUiState> = _uiState.asStateFlow()

    private val lastVisitSent = mutableMapOf<String, Long>()

    fun load() {
        viewModelScope.launch {
            runCatching { repository.getShortcuts() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            pinned = data.pinned,
                            frequent = data.frequent,
                            loaded = true,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(pinned = emptyList(), frequent = emptyList(), loaded = false)
                    }
                }
        }
    }

    fun reset() {
        lastVisitSent.clear()
        _uiState.value = SidebarShortcutsUiState()
    }

    fun recordVisit(path: String) {
        val normalized = path.trim()
        if (normalized.isEmpty() || normalized in EXCLUDED_VISIT_PATHS) return

        val now = System.currentTimeMillis()
        val last = lastVisitSent[normalized] ?: 0L
        if (now - last < VISIT_DEBOUNCE_MS) return

        lastVisitSent[normalized] = now
        viewModelScope.launch {
            runCatching { repository.recordVisit(normalized) }
                .onSuccess {
                    if (_uiState.value.loaded) {
                        runCatching { repository.getShortcuts() }
                            .onSuccess { data ->
                                _uiState.update {
                                    it.copy(
                                        pinned = data.pinned,
                                        frequent = data.frequent,
                                        loaded = true,
                                    )
                                }
                            }
                    }
                }
                .onFailure {
                    lastVisitSent.remove(normalized)
                }
        }
    }

    class Factory(
        private val repository: SidebarShortcutsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SidebarShortcutsViewModel::class.java)) {
                return SidebarShortcutsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        private val EXCLUDED_VISIT_PATHS = setOf("/login", "/dashboard")
        private const val VISIT_DEBOUNCE_MS = 5 * 60 * 1000L
    }
}
