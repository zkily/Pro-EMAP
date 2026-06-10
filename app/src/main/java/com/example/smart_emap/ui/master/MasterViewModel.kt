package com.example.smart_emap.ui.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterDestinationHolidayDto
import com.example.smart_emap.data.model.MasterDestinationWorkdayDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto
import com.example.smart_emap.data.model.MasterProductRouteStepDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MasterListUiState(
    val path: String = "",
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val secondaryFilter: String = "",
    val rows: List<MasterTableRow> = emptyList(),
    val totalCount: Int = 0,
    val processOptions: List<Pair<String, String>> = emptyList(),
    val snackbarMessage: String? = null,
    val showForm: Boolean = false,
    val editingId: Int? = null,
    val formValues: Map<String, String> = emptyMap(),
    // product route
    val productKeyword: String = "",
    val productOptions: List<Pair<String, String>> = emptyList(),
    val selectedProductCd: String = "",
    val routeInfo: MasterProductRouteInfoDto? = null,
    val routeSteps: List<MasterProductRouteStepDto> = emptyList(),
    // destination holiday
    val destinationOptions: List<Pair<String, String>> = emptyList(),
    val selectedDestinationCd: String = "",
    val holidays: List<MasterDestinationHolidayDto> = emptyList(),
    val workdays: List<MasterDestinationWorkdayDto> = emptyList(),
    val newHolidayDate: String = "",
    val newWorkdayDate: String = "",
    val newWorkdayReason: String = "",
    // process route steps
    val routeStepsDialogRouteCd: String? = null,
    val routeStepsList: List<com.example.smart_emap.data.model.MasterRouteStepDto> = emptyList(),
)

class MasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MasterListUiState())
    val uiState: StateFlow<MasterListUiState> = _uiState.asStateFlow()
    private var keywordJob: Job? = null

    fun setPath(path: String) {
        if (_uiState.value.path == path) return
        _uiState.update {
            it.copy(
                path = path,
                keyword = "",
                secondaryFilter = "",
                rows = emptyList(),
                selectedProductCd = "",
                routeInfo = null,
                routeSteps = emptyList(),
            )
        }
        refreshAll()
    }

    fun refreshAll() {
        val path = _uiState.value.path
        if (path.isBlank() || path == "/master") return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadForPath(path) }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") } }
        }
    }

    private suspend fun loadForPath(path: String) {
        when (MasterPageRegistry.kindForPath(path)) {
            MasterPageKind.ProductProcessRoute -> loadProductRouteScreen()
            MasterPageKind.DestinationHoliday -> loadHolidayScreen()
            MasterPageKind.ProcessingFee -> {
                val options = repository.loadProcessOptions()
                _uiState.update { it.copy(processOptions = options) }
                loadList(path)
            }
            else -> loadList(path)
        }
    }

    private suspend fun loadList(path: String) {
        val def = MasterPageRegistry.pageForPath(path) ?: return
        val state = _uiState.value
        val (rows, total) = repository.loadList(def.kind, state.keyword, state.secondaryFilter)
        _uiState.update { it.copy(isLoading = false, rows = rows, totalCount = total) }
    }

    private suspend fun loadProductRouteScreen() {
        val state = _uiState.value
        val (products, _) = repository.loadProductsForRoute(state.productKeyword)
        val info = state.selectedProductCd.takeIf { it.isNotBlank() }?.let { repository.loadProductRouteInfo(it) }
        val steps = if (info != null && !info.routeCd.isNullOrBlank()) {
            repository.loadProductRouteSteps(state.selectedProductCd, info.routeCd.orEmpty())
        } else emptyList()
        _uiState.update {
            it.copy(
                isLoading = false,
                productOptions = products,
                routeInfo = info,
                routeSteps = steps,
            )
        }
    }

    private suspend fun loadHolidayScreen() {
        val options = repository.loadDestinationOptions().map { it.cd to it.name }
        val dest = _uiState.value.selectedDestinationCd
        val holidays = if (dest.isNotBlank()) repository.loadHolidays(dest) else emptyList()
        val workdays = if (dest.isNotBlank()) repository.loadWorkdays(dest) else emptyList()
        _uiState.update {
            it.copy(
                isLoading = false,
                destinationOptions = options,
                holidays = holidays,
                workdays = workdays,
            )
        }
    }

    fun setKeyword(v: String) {
        _uiState.update { it.copy(keyword = v) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(400)
            refreshAll()
        }
    }

    fun setSecondaryFilter(v: String) {
        _uiState.update { it.copy(secondaryFilter = v) }
        refreshAll()
    }

    fun resetFilters() {
        _uiState.update { it.copy(keyword = "", secondaryFilter = "") }
        refreshAll()
    }

    fun openCreateForm() {
        val def = MasterPageRegistry.pageForPath(_uiState.value.path) ?: return
        _uiState.update {
            it.copy(showForm = true, editingId = null, formValues = def.formFields.associate { f -> f.key to "" })
        }
    }

    fun openEditForm(row: MasterTableRow) {
        val def = MasterPageRegistry.pageForPath(_uiState.value.path) ?: return
        val values = def.formFields.mapIndexed { i, field ->
            field.key to (row.cells.getOrNull(i).orEmpty())
        }.toMap()
        _uiState.update { it.copy(showForm = true, editingId = row.id, formValues = values) }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingId = null) }

    fun saveForm() {
        val state = _uiState.value
        val def = MasterPageRegistry.pageForPath(state.path) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveForm(def.kind, state.editingId, state.formValues)
                if (ok) {
                    _uiState.update { it.copy(actionLoading = false, showForm = false, snackbarMessage = "保存しました") }
                    refreshAll()
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存失敗") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存失敗") }
            }
        }
    }

    fun deleteRow(row: MasterTableRow) {
        val id = row.id ?: return
        val def = MasterPageRegistry.pageForPath(_uiState.value.path) ?: return
        viewModelScope.launch {
            runCatching {
                repository.deleteItem(def.kind, id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun setProductKeyword(v: String) {
        _uiState.update { it.copy(productKeyword = v) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch { delay(400); loadProductRouteScreen(); _uiState.update { it.copy(isLoading = false) } }
    }

    fun selectProduct(cd: String) {
        _uiState.update { it.copy(selectedProductCd = cd, isLoading = true) }
        viewModelScope.launch { loadProductRouteScreen() }
    }

    fun setDestination(v: String) {
        _uiState.update { it.copy(selectedDestinationCd = v) }
    }

    fun loadHolidayData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadHolidayScreen()
        }
    }

    fun setNewHolidayDate(v: String) = _uiState.update { it.copy(newHolidayDate = v) }
    fun setNewWorkdayDate(v: String) = _uiState.update { it.copy(newWorkdayDate = v) }
    fun setNewWorkdayReason(v: String) = _uiState.update { it.copy(newWorkdayReason = v) }

    fun addHoliday() {
        val state = _uiState.value
        if (state.selectedDestinationCd.isBlank() || state.newHolidayDate.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addHoliday(state.selectedDestinationCd, state.newHolidayDate)
                _uiState.update { it.copy(snackbarMessage = "休日を追加しました", newHolidayDate = "") }
                loadHolidayScreen()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "追加失敗") } }
        }
    }

    fun deleteHolidayItem(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            runCatching {
                repository.deleteHoliday(id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                loadHolidayScreen()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
        }
    }

    fun addWorkday() {
        val state = _uiState.value
        if (state.selectedDestinationCd.isBlank() || state.newWorkdayDate.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addWorkday(state.selectedDestinationCd, state.newWorkdayDate, state.newWorkdayReason.takeIf { it.isNotBlank() })
                _uiState.update { it.copy(snackbarMessage = "臨時出勤を追加しました", newWorkdayDate = "", newWorkdayReason = "") }
                loadHolidayScreen()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "追加失敗") } }
        }
    }

    fun deleteWorkdayItem(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            runCatching {
                repository.deleteWorkday(id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                loadHolidayScreen()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
        }
    }

    fun openRouteSteps(routeCd: String) {
        viewModelScope.launch {
            val steps = repository.loadRouteSteps(routeCd)
            _uiState.update { it.copy(routeStepsDialogRouteCd = routeCd, routeStepsList = steps) }
        }
    }

    fun closeRouteSteps() = _uiState.update { it.copy(routeStepsDialogRouteCd = null, routeStepsList = emptyList()) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MasterViewModel(repository) as T
    }
}
