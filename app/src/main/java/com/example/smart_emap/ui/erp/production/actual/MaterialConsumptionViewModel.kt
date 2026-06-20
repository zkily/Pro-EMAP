package com.example.smart_emap.ui.erp.production.actual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialUsageByDateDto
import com.example.smart_emap.data.model.MaterialUsageByMaterialDto
import com.example.smart_emap.data.model.MaterialUsageRecordDto
import com.example.smart_emap.data.repository.ProcessOption
import com.example.smart_emap.data.repository.ProductionActualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val MC_PAGE_SIZE = 50

data class MaterialConsumptionUiState(
    val dateFrom: String = "",
    val dateTo: String = "",
    val materialCd: String = "",
    val materialOptions: List<ProcessOption> = emptyList(),
    val loading: Boolean = false,
    val records: List<MaterialUsageRecordDto> = emptyList(),
    val page: Int = 1,
    val total: Int = 0,
    val byDate: List<MaterialUsageByDateDto> = emptyList(),
    val byMaterial: List<MaterialUsageByMaterialDto> = emptyList(),
    val snackbarMessage: String? = null,
) {
    val pageCount: Int get() = if (total <= 0) 1 else (total + MC_PAGE_SIZE - 1) / MC_PAGE_SIZE
}

class MaterialConsumptionViewModel(
    private val repository: ProductionActualRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialConsumptionUiState())
    val uiState: StateFlow<MaterialConsumptionUiState> = _uiState.asStateFlow()

    init {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        _uiState.update { it.copy(dateFrom = start.toString(), dateTo = end.toString()) }
        loadMaterialOptions()
    }

    private fun loadMaterialOptions() {
        viewModelScope.launch {
            runCatching { repository.loadMaterialOptions() }
                .onSuccess { opts -> _uiState.update { it.copy(materialOptions = opts) } }
        }
    }

    fun setRange(start: String, end: String) = _uiState.update { it.copy(dateFrom = start, dateTo = end) }

    fun setMaterial(cd: String) {
        _uiState.update { it.copy(materialCd = cd) }
        search()
    }

    fun reset() {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        _uiState.update { it.copy(dateFrom = start.toString(), dateTo = end.toString(), materialCd = "") }
        search()
    }

    fun search() {
        _uiState.update { it.copy(page = 1) }
        fetchCharts()
        fetchList()
    }

    fun goToPage(page: Int) {
        val s = _uiState.value
        val target = page.coerceIn(1, s.pageCount)
        if (target == s.page) return
        _uiState.update { it.copy(page = target) }
        fetchList()
    }

    private fun fetchCharts() {
        val s = _uiState.value
        viewModelScope.launch {
            runCatching {
                repository.loadMaterialUsageChart(s.materialCd, s.dateFrom, s.dateTo)
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        byDate = data.byDate.orEmpty(),
                        byMaterial = data.byMaterial.orEmpty().take(10),
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(byDate = emptyList(), byMaterial = emptyList()) }
            }
        }
    }

    private fun fetchList() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            runCatching {
                repository.loadMaterialUsageRecords(
                    page = s.page,
                    pageSize = MC_PAGE_SIZE,
                    materialCd = s.materialCd,
                    dateFrom = s.dateFrom,
                    dateTo = s.dateTo,
                )
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(loading = false, records = data.list.orEmpty(), total = data.total ?: 0)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(loading = false, records = emptyList(), total = 0, snackbarMessage = e.message ?: "一覧の取得に失敗しました")
                }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ProductionActualRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialConsumptionViewModel(repository) as T
    }
}
