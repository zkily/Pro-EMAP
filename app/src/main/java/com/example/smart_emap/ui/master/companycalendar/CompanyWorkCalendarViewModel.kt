package com.example.smart_emap.ui.master.companycalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.CompanyWorkCalendarItemDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CompanyWorkCalendarUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val monthYm: String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")),
    val items: List<CompanyWorkCalendarItemDto> = emptyList(),
    val scheduledCount: Int = 0,
    val totalDays: Int = 0,
    val dayTypeOptions: List<Pair<String, String>> = emptyList(),
    val selectedDayType: String = "company_holiday",
    val pendingDates: List<String> = emptyList(),
    val entryName: String = "",
    val snackbarMessage: String? = null,
) {
    val monthLabel: String
        get() = runCatching {
            val ym = YearMonth.parse(monthYm)
            "${ym.year}年${ym.monthValue}月"
        }.getOrElse { monthYm }
}

class CompanyWorkCalendarViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyWorkCalendarUiState())
    val uiState: StateFlow<CompanyWorkCalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val types = repository.loadCompanyWorkCalendarDayTypes()
                .mapNotNull { (it.value ?: return@mapNotNull null) to (it.label ?: it.value) }
            _uiState.update {
                it.copy(
                    dayTypeOptions = types,
                    selectedDayType = types.firstOrNull()?.first ?: "company_holiday",
                )
            }
        }
        loadMonth()
    }

    fun refreshAll() = loadMonth()

    fun setMonthYm(value: String) {
        _uiState.update { it.copy(monthYm = value) }
        loadMonth()
    }

    fun setSelectedDayType(value: String) = _uiState.update { it.copy(selectedDayType = value) }
    fun setEntryName(value: String) = _uiState.update { it.copy(entryName = value) }
    fun addPendingDate(date: String) {
        val d = date.take(10)
        if (d.isBlank()) return
        _uiState.update { state ->
            state.copy(pendingDates = (state.pendingDates + d).distinct().sorted())
        }
    }
    fun removePendingDate(date: String) = _uiState.update { it.copy(pendingDates = it.pendingDates.filter { d -> d != date }) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun loadMonth() {
        val ym = _uiState.value.monthYm
        val yearMonth = runCatching { YearMonth.parse(ym) }.getOrNull() ?: return
        val start = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val end = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.loadCompanyWorkCalendar(start, end)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    items = res?.data?.items.orEmpty(),
                    scheduledCount = res?.data?.scheduledWorkdayCount ?: 0,
                    totalDays = res?.data?.totalDays ?: 0,
                    snackbarMessage = if (res == null) "読み込みに失敗しました" else null,
                )
            }
        }
    }

    fun submitBatch() {
        val state = _uiState.value
        if (state.pendingDates.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "日付を追加してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                val res = repository.batchCreateCompanyWorkCalendar(
                    state.pendingDates,
                    state.selectedDayType,
                    state.entryName,
                )
                if (res == null) throw IllegalStateException("追加に失敗しました")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        pendingDates = emptyList(),
                        entryName = "",
                        snackbarMessage = "追加 ${res.created ?: 0} 件（スキップ ${res.skipped ?: 0}）",
                    )
                }
                loadMonth()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, snackbarMessage = e.message ?: "追加に失敗しました") }
            }
        }
    }

    fun deleteEntry(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            runCatching {
                repository.deleteCompanyWorkCalendarEntry(id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                loadMonth()
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CompanyWorkCalendarViewModel(repository) as T
    }
}
