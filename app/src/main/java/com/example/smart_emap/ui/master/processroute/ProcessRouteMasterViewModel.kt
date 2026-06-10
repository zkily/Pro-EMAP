package com.example.smart_emap.ui.master.processroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterProcessRouteDto
import com.example.smart_emap.data.model.MasterRouteStepDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProcessRouteScreenMode { List, StepEditor }

const val PROCESS_ROUTE_PAGE_SIZE = 50

fun routeDefaultFormValues(): Map<String, String> = mapOf(
    "route_cd" to "",
    "route_name" to "",
    "description" to "",
    "is_active" to "true",
    "is_default" to "false",
)

fun MasterProcessRouteDto.toRouteFormValues(): Map<String, String> = mapOf(
    "route_cd" to routeCd.orEmpty(),
    "route_name" to routeName.orEmpty(),
    "description" to description.orEmpty(),
    "is_active" to if (isActive != false) "true" else "false",
    "is_default" to if (isDefault == true) "true" else "false",
)

fun stepDefaultFormValues(routeCd: String, nextStepNo: Int = 1): Map<String, String> = mapOf(
    "step_no" to nextStepNo.toString(),
    "process_cd" to "",
    "yield_percent" to "100",
    "cycle_sec" to "0",
    "remarks" to "",
    "route_cd" to routeCd,
)

fun MasterRouteStepDto.toStepFormValues(): Map<String, String> = mapOf(
    "step_no" to (stepNo?.toString() ?: "1"),
    "process_cd" to processCd.orEmpty(),
    "yield_percent" to (yieldPercent?.toString() ?: "100"),
    "cycle_sec" to (cycleSec?.toString() ?: "0"),
    "remarks" to remarks.orEmpty(),
    "route_cd" to routeCd.orEmpty(),
)

data class ProcessRouteMasterUiState(
    val screenMode: ProcessRouteScreenMode = ProcessRouteScreenMode.List,
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val page: Int = 1,
    val pageSize: Int = PROCESS_ROUTE_PAGE_SIZE,
    val total: Int = 0,
    val routes: List<MasterProcessRouteDto> = emptyList(),
    val showRouteForm: Boolean = false,
    val editingRoute: MasterProcessRouteDto? = null,
    val routeFormValues: Map<String, String> = emptyMap(),
    val pendingDeleteRouteId: Int? = null,
    val editingRouteCd: String? = null,
    val editingRouteName: String? = null,
    val steps: List<MasterRouteStepDto> = emptyList(),
    val processOptions: List<Pair<String, String>> = emptyList(),
    val showStepForm: Boolean = false,
    val editingStep: MasterRouteStepDto? = null,
    val stepFormValues: Map<String, String> = emptyMap(),
    val pendingDeleteStepId: Int? = null,
    val snackbarMessage: String? = null,
)

class ProcessRouteMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProcessRouteMasterUiState())
    val uiState: StateFlow<ProcessRouteMasterUiState> = _uiState.asStateFlow()

    init {
        refreshRoutes()
    }

    fun refreshAll() {
        when (_uiState.value.screenMode) {
            ProcessRouteScreenMode.List -> refreshRoutes()
            ProcessRouteScreenMode.StepEditor -> viewModelScope.launch { refreshSteps() }
        }
    }

    fun refreshRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val (routes, total) = repository.loadProcessRoutes(state.keyword, state.page, state.pageSize)
                _uiState.update { it.copy(isLoading = false, routes = routes, total = total) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    private suspend fun refreshSteps() {
        val routeCd = _uiState.value.editingRouteCd ?: return
        _uiState.update { it.copy(isLoading = true) }
        runCatching {
            val route = repository.getProcessRouteByCd(routeCd)
            val steps = repository.loadRouteSteps(routeCd)
            val options = _uiState.value.processOptions.ifEmpty { repository.loadProcessOptions() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    editingRouteName = route?.routeName,
                    steps = steps.sortedBy { s -> s.stepNo ?: 0 },
                    processOptions = options,
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun search() {
        _uiState.update { it.copy(page = 1) }
        refreshRoutes()
    }

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", page = 1) }
        refreshRoutes()
    }

    fun hasActiveFilters(): Boolean = _uiState.value.keyword.isNotBlank()

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        refreshRoutes()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        refreshRoutes()
    }

    fun openCreateRoute() {
        _uiState.update {
            it.copy(showRouteForm = true, editingRoute = null, routeFormValues = routeDefaultFormValues())
        }
    }

    fun openEditRoute(route: MasterProcessRouteDto) {
        _uiState.update {
            it.copy(showRouteForm = true, editingRoute = route, routeFormValues = route.toRouteFormValues())
        }
    }

    fun setRouteFormValue(key: String, value: String) {
        _uiState.update { it.copy(routeFormValues = it.routeFormValues + (key to value)) }
    }

    fun closeRouteForm() = _uiState.update { it.copy(showRouteForm = false, editingRoute = null) }

    fun saveRouteForm() {
        val state = _uiState.value
        if (state.routeFormValues["route_cd"].isNullOrBlank() || state.routeFormValues["route_name"].isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = "ルートCD・名称は必須です") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveProcessRouteMaster(state.editingRoute?.id, state.routeFormValues)
                if (ok) {
                    _uiState.update {
                        it.copy(actionLoading = false, showRouteForm = false, snackbarMessage = "保存しました")
                    }
                    refreshRoutes()
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存失敗") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    fun requestDeleteRoute(id: Int?) {
        if (id != null) _uiState.update { it.copy(pendingDeleteRouteId = id) }
    }

    fun cancelDeleteRoute() = _uiState.update { it.copy(pendingDeleteRouteId = null) }

    fun confirmDeleteRoute() {
        val id = _uiState.value.pendingDeleteRouteId ?: return
        viewModelScope.launch {
            runCatching {
                repository.deleteProcessRouteMaster(id)
                _uiState.update { it.copy(pendingDeleteRouteId = null, snackbarMessage = "削除しました") }
                refreshRoutes()
            }.onFailure { e ->
                _uiState.update { it.copy(pendingDeleteRouteId = null, snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun openStepEditor(route: MasterProcessRouteDto) {
        val cd = route.routeCd ?: return
        _uiState.update {
            it.copy(
                screenMode = ProcessRouteScreenMode.StepEditor,
                editingRouteCd = cd,
                editingRouteName = route.routeName,
                steps = emptyList(),
            )
        }
        viewModelScope.launch { refreshSteps() }
    }

    fun closeStepEditor() {
        _uiState.update {
            it.copy(
                screenMode = ProcessRouteScreenMode.List,
                editingRouteCd = null,
                editingRouteName = null,
                steps = emptyList(),
                showStepForm = false,
                editingStep = null,
            )
        }
        refreshRoutes()
    }

    fun openCreateStep() {
        val routeCd = _uiState.value.editingRouteCd ?: return
        val nextNo = (_uiState.value.steps.maxOfOrNull { it.stepNo ?: 0 } ?: 0) + 1
        _uiState.update {
            it.copy(
                showStepForm = true,
                editingStep = null,
                stepFormValues = stepDefaultFormValues(routeCd, nextNo),
            )
        }
    }

    fun openEditStep(step: MasterRouteStepDto) {
        _uiState.update {
            it.copy(showStepForm = true, editingStep = step, stepFormValues = step.toStepFormValues())
        }
    }

    fun setStepFormValue(key: String, value: String) {
        val updated = _uiState.value.stepFormValues + (key to value)
        _uiState.update { it.copy(stepFormValues = updated) }
        if (key == "process_cd" && _uiState.value.editingStep == null && value.isNotBlank()) {
            viewModelScope.launch {
                val details = repository.loadProcessDetailsMap()
                val cycle = details[value]?.defaultCycleSec
                if (cycle != null) {
                    _uiState.update { state ->
                        state.copy(stepFormValues = state.stepFormValues + ("cycle_sec" to cycle.toString()))
                    }
                }
            }
        }
    }

    fun closeStepForm() = _uiState.update { it.copy(showStepForm = false, editingStep = null) }

    fun saveStepForm() {
        val state = _uiState.value
        val routeCd = state.editingRouteCd ?: return
        if (state.stepFormValues["process_cd"].isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = "工程を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val stepId = state.editingStep?.id
                if (stepId == null) repository.createRouteStep(routeCd, state.stepFormValues)
                else repository.updateRouteStep(stepId, state.stepFormValues)
                _uiState.update {
                    it.copy(actionLoading = false, showStepForm = false, editingStep = null, snackbarMessage = "保存しました")
                }
                refreshSteps()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    fun requestDeleteStep(id: Int?) {
        if (id != null) _uiState.update { it.copy(pendingDeleteStepId = id) }
    }

    fun cancelDeleteStep() = _uiState.update { it.copy(pendingDeleteStepId = null) }

    fun confirmDeleteStep() {
        val state = _uiState.value
        val routeCd = state.editingRouteCd ?: return
        val stepId = state.pendingDeleteStepId ?: return
        viewModelScope.launch {
            runCatching {
                repository.deleteRouteStep(routeCd, stepId)
                _uiState.update { it.copy(pendingDeleteStepId = null, snackbarMessage = "削除しました") }
                refreshSteps()
            }.onFailure { e ->
                _uiState.update { it.copy(pendingDeleteStepId = null, snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun saveStepOrder() {
        val routeCd = _uiState.value.editingRouteCd ?: return
        val steps = _uiState.value.steps
        if (steps.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.updateRouteStepOrder(routeCd, steps)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "順序を保存しました") }
                refreshSteps()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProcessRouteMasterViewModel(repository) as T
    }
}
