package com.example.smart_emap.ui.mes.productivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.InspectionRepository
import com.example.smart_emap.data.repository.SystemUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspectionProductivityUiState(
    val isLoading: Boolean = false,
    val startDate: String = InspectionProductivityLogic.defaultDateRange().first,
    val endDate: String = InspectionProductivityLogic.defaultDateRange().second,
    val filterInspectorId: Int? = null,
    val filterProductCd: String = "",
    val includeIncomplete: Boolean = false,
    val inspectorOptions: List<UserListItemDto> = emptyList(),
    val productOptions: List<ErpProductDto> = emptyList(),
    val loadingProducts: Boolean = false,
    val defectLabelMap: Map<String, String> = emptyMap(),
    val analysisData: InspectionProductivityAnalysisDataDto? = null,
    val rankViewProductCd: String = "",
    val snackbarMessage: String? = null,
) {
    val kpiCards: List<IpaKpiCard> get() = InspectionProductivityLogic.buildKpiCards(analysisData?.summary)
    val productRankList: List<InspectionProductivityProductRankingDto> get() =
        InspectionProductivityLogic.resolveProductRankList(analysisData)
    val selectedProductRanking: InspectionProductivityProductRankingDto? get() {
        val list = productRankList
        if (list.isEmpty()) return null
        return list.find { it.productCd == rankViewProductCd } ?: list.first()
    }
    val podiumInspectors get() = InspectionProductivityLogic.podiumInspectors(selectedProductRanking)
    val productRankTopOverview get() = productRankList.filter { it.topEfficiencyPerHour != null }
    val rangeLabel: String? get() = InspectionProductivityLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
}

class InspectionProductivityViewModel(
    private val inspectionRepository: InspectionRepository,
    private val userRepository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionProductivityUiState())
    val uiState: StateFlow<InspectionProductivityUiState> = _uiState.asStateFlow()

    init {
        loadInspectors()
        loadProductOptions()
        loadDefectLabels()
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

    private fun loadProductOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProducts = true) }
            runCatching { inspectionRepository.loadProducts() }
                .onSuccess { list ->
                    _uiState.update { it.copy(loadingProducts = false, productOptions = list) }
                }
                .onFailure {
                    _uiState.update { it.copy(loadingProducts = false, productOptions = emptyList()) }
                }
        }
    }

    private fun loadDefectLabels() {
        viewModelScope.launch {
            runCatching { inspectionRepository.loadDefectItems() }
                .onSuccess { items ->
                    val map = items.associate { item ->
                        val key = item.defectCd.trim().ifBlank { item.id?.toString().orEmpty() }
                        key to item.defectName
                    }
                    _uiState.update { it.copy(defectLabelMap = map) }
                }
        }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
    }

    fun setFilterInspectorId(id: Int?) {
        _uiState.update { it.copy(filterInspectorId = id) }
    }

    fun setFilterProductCd(cd: String) {
        _uiState.update { it.copy(filterProductCd = cd) }
    }

    fun setIncludeIncomplete(value: Boolean) {
        _uiState.update { it.copy(includeIncomplete = value) }
    }

    fun setRankViewProductCd(cd: String) {
        _uiState.update { it.copy(rankViewProductCd = cd) }
    }

    fun loadAnalysis() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            inspectionRepository.loadProductivityAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                inspectorUserId = state.filterInspectorId,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = state.includeIncomplete,
            ).onSuccess { data ->
                syncRankProductSelection(data)
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

    private fun syncRankProductSelection(data: InspectionProductivityAnalysisDataDto) {
        val list = InspectionProductivityLogic.resolveProductRankList(data)
        val current = _uiState.value.rankViewProductCd
        val next = when {
            list.isEmpty() -> ""
            list.any { it.productCd == current } -> current
            else -> list.first().productCd
        }
        _uiState.update { it.copy(rankViewProductCd = next) }
    }

    fun defectLabel(defectCd: String): String {
        val cd = defectCd.trim()
        return _uiState.value.defectLabelMap[cd] ?: cd
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val inspectionRepository: InspectionRepository,
        private val userRepository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InspectionProductivityViewModel(inspectionRepository, userRepository) as T
    }
}
