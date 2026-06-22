package com.example.smart_emap.ui.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.UserTodoItemDto
import com.example.smart_emap.data.repository.TodosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HeaderTodoUiState(
    val items: List<UserTodoItemDto> = emptyList(),
    val draft: String = "",
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val pendingCount: Int
        get() = items.count { it.isDone != 1 }

    val doneCount: Int
        get() = items.count { it.isDone == 1 }

    val sortedItems: List<UserTodoItemDto>
        get() = items.sortedWith(
            compareBy<UserTodoItemDto> { it.isDone }
                .thenByDescending { it.createdAt },
        )
}

class HeaderTodoViewModel(
    private val repository: TodosRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HeaderTodoUiState())
    val uiState: StateFlow<HeaderTodoUiState> = _uiState.asStateFlow()

    fun setDraft(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            runCatching { repository.list() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            items = data.list,
                            loading = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            errorMessage = "連絡事項の読み込みに失敗しました",
                        )
                    }
                }
        }
    }

    fun add() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || _uiState.value.submitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true, errorMessage = null) }
            runCatching { repository.create(text) }
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(
                            items = listOf(created) + it.items,
                            draft = "",
                            submitting = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            submitting = false,
                            errorMessage = "連絡事項の追加に失敗しました",
                        )
                    }
                }
        }
    }

    fun toggle(id: Int) {
        val item = _uiState.value.items.firstOrNull { it.id == id } ?: return
        val nextDone = if (item.isDone == 1) 0 else 1
        viewModelScope.launch {
            runCatching { repository.update(id, isDone = nextDone) }
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(items = state.items.map { if (it.id == id) updated else it })
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "連絡事項の更新に失敗しました") }
                }
        }
    }

    fun updateContent(id: Int, content: String) {
        val text = content.trim()
        if (text.isEmpty()) return
        val item = _uiState.value.items.firstOrNull { it.id == id } ?: return
        if (item.content == text) return

        viewModelScope.launch {
            runCatching { repository.update(id, content = text) }
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(items = state.items.map { if (it.id == id) updated else it })
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "連絡事項の更新に失敗しました") }
                }
        }
    }

    fun remove(id: Int) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(items = state.items.filterNot { it.id == id })
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "連絡事項の削除に失敗しました") }
                }
        }
    }

    fun clearDone() {
        if (_uiState.value.doneCount == 0) return
        viewModelScope.launch {
            runCatching { repository.clearCompleted() }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(items = state.items.filter { it.isDone != 1 })
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "完了した連絡事項の削除に失敗しました") }
                }
        }
    }

    fun reset() {
        _uiState.value = HeaderTodoUiState()
    }

    class Factory(
        private val repository: TodosRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HeaderTodoViewModel::class.java)) {
                return HeaderTodoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
