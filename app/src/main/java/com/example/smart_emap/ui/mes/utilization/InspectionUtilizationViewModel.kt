package com.example.smart_emap.ui.mes.utilization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.InspectionRepository
import com.example.smart_emap.data.repository.SystemUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspectionUtilizationUiState(
    val isLoading: Boolean = false,
    val startDate: String = InspectionUtilizationLogic.defaultDateRange().first,
    val endDate: String = InspectionUtilizationLogic.defaultDateRange().second,
    val filterInspectorId: Int? = null,
    val includeIncomplete: Boolean = false,
    val extraWorkdays: List<String> = emptyList(),
    val extraHolidays: List<String> = emptyList(),
    val inspectorOptions: List<UserListItemDto> = emptyList(),
    val analysisData: InspectionUtilizationAnalysisDataDto? = null,
    val snackbarMessage: String? = null,
) {
    val standardHours: Double get() = analysisData?.standardWorkdayHours ?: 7.6
    val kpiCards: List<IuaKpiCard> get() = InspectionUtilizationLogic.buildKpiCards(
        analysisData?.summary,
        analysisData?.calendarWorkdaysInRange,
    )
    val filteredDailyRows get() = InspectionUtilizationLogic.filterDailyRows(
        analysisData?.dailyByInspector.orEmpty(),
        filterInspectorId,
    )
    val rangeLabel: String? get() = InspectionUtilizationLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
}

class InspectionUtilizationViewModel(
    private val inspectionRepository: InspectionRepository,
    private val userRepository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionUtilizationUiState())
    val uiState: StateFlow<InspectionUtilizationUiState> = _uiState.asStateFlow()

    init {
        loadInspectors()
        loadAnalysis()
    }

    fun refreshAll() = loadAnalysis()

    private fun loadInspectors() {
        viewModelScope.launch {
            userRepository.getUsers(status = "active", page = 1, pageSize = 500)
                .onSuccess { res ->
                    _uiState.update { it.copy(inspectorOptions = res.items.orEmpty()) }
                }
        }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
    }

    fun setFilterInspectorId(id: Int?) {
        _uiState.update { it.copy(filterInspectorId = id) }
    }

    fun setIncludeIncomplete(value: Boolean) {
        _uiState.update { it.copy(includeIncomplete = value) }
    }

    fun addExtraWorkday(date: String) {
        val d = date.take(10)
        if (d.isBlank()) return
        _uiState.update {
            val next = (it.extraWorkdays + d).distinct().sorted()
            it.copy(extraWorkdays = next)
        }
    }

    fun removeExtraWorkday(date: String) {
        _uiState.update { it.copy(extraWorkdays = it.extraWorkdays.filter { d -> d != date }) }
    }

    fun addExtraHoliday(date: String) {
        val d = date.take(10)
        if (d.isBlank()) return
        _uiState.update {
            val next = (it.extraHolidays + d).distinct().sorted()
            it.copy(extraHolidays = next)
        }
    }

    fun removeExtraHoliday(date: String) {
        _uiState.update { it.copy(extraHolidays = it.extraHolidays.filter { d -> d != date }) }
    }

    fun loadAnalysis() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            inspectionRepository.loadUtilizationAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                inspectorUserId = state.filterInspectorId,
                includeIncomplete = state.includeIncomplete,
                extraWorkdays = state.extraWorkdays,
                extraHolidays = state.extraHolidays,
            ).onSuccess { data ->
                _uiState.update { it.copy(isLoading = false, analysisData = data) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        analysisData = null,
                        snackbarMessage = e.message ?: "分析データの取得に失敗しました",
                    )
                }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val inspectionRepository: InspectionRepository,
        private val userRepository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InspectionUtilizationViewModel(inspectionRepository, userRepository) as T
    }
}
