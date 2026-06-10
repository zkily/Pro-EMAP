package com.example.smart_emap.ui.mes.productivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.WeldingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.WeldingProductivityProductRankingDto
import com.example.smart_emap.data.repository.SystemUserRepository
import com.example.smart_emap.data.repository.WeldingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeldingProductivityUiState(
    val isLoading: Boolean = false,
    val startDate: String = WeldingProductivityLogic.defaultDateRange().first,
    val endDate: String = WeldingProductivityLogic.defaultDateRange().second,
    val filterOperatorId: Int? = null,
    val filterProductCd: String = "",
    val includeIncomplete: Boolean = false,
    val operatorOptions: List<UserListItemDto> = emptyList(),
    val productOptions: List<ErpProductDto> = emptyList(),
    val loadingProducts: Boolean = false,
    val defectLabelMap: Map<String, String> = emptyMap(),
    val analysisData: WeldingProductivityAnalysisDataDto? = null,
    val rankViewProductCd: String = "",
    val snackbarMessage: String? = null,
) {
    val kpiCards: List<IpaKpiCard> get() = WeldingProductivityLogic.buildKpiCards(analysisData?.summary)
    val productRankList: List<WeldingProductivityProductRankingDto> get() =
        WeldingProductivityLogic.resolveProductRankList(analysisData)
    val selectedProductRanking: WeldingProductivityProductRankingDto? get() {
        val list = productRankList
        if (list.isEmpty()) return null
        return list.find { it.productCd == rankViewProductCd } ?: list.first()
    }
    val podiumOperators get() = WeldingProductivityLogic.podiumOperators(selectedProductRanking)
    val productRankTopOverview get() = productRankList.filter { it.topEfficiencyPerHour != null }
    val rangeLabel: String? get() = WeldingProductivityLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
}

class WeldingProductivityViewModel(
    private val weldingRepository: WeldingRepository,
    private val userRepository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeldingProductivityUiState())
    val uiState: StateFlow<WeldingProductivityUiState> = _uiState.asStateFlow()

    init {
        loadOperators()
        loadProductOptions()
        loadDefectLabels()
        loadAnalysis()
    }

    fun refreshAll() = loadAnalysis()

    private fun loadOperators() {
        viewModelScope.launch {
            userRepository.getUsers(status = "active", page = 1, pageSize = 500)
                .onSuccess { res ->
                    _uiState.update { it.copy(operatorOptions = res.items.orEmpty()) }
                }
        }
    }

    private fun loadProductOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProducts = true) }
            runCatching { weldingRepository.loadProducts() }
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
            runCatching { weldingRepository.loadDefectItems() }
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

    fun setFilterOperatorId(id: Int?) {
        _uiState.update { it.copy(filterOperatorId = id) }
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
            weldingRepository.loadProductivityAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                operatorUserId = state.filterOperatorId,
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

    private fun syncRankProductSelection(data: WeldingProductivityAnalysisDataDto) {
        val list = WeldingProductivityLogic.resolveProductRankList(data)
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
        private val weldingRepository: WeldingRepository,
        private val userRepository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WeldingProductivityViewModel(weldingRepository, userRepository) as T
    }
}
