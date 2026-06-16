package com.example.smart_emap.ui.mes.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.InspectionNextAssignmentDto
import com.example.smart_emap.data.model.MachineDto
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.WeldingManagementRowDto
import com.example.smart_emap.data.repository.InspectionRepository
import com.example.smart_emap.data.repository.SystemUserRepository
import com.example.smart_emap.data.repository.WeldingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val WELDING_AUTO_REFRESH_MS = 8_000L
private const val INSPECTION_AUTO_REFRESH_MS = 15_000L
private const val CLOCK_TICK_MS = 1_000L
private const val FETCH_ERROR_TOAST_COOLDOWN_MS = 30_000L

data class NextAssignDialogState(
    val visible: Boolean = false,
    val pickInspector: Boolean = false,
    val inspectorUserId: Int? = null,
    val inspectorName: String = "",
    val currentProductLabel: String = "—",
    val isFirstProduct: Boolean = false,
    val productCd: String = "",
    val submitting: Boolean = false,
)

data class MonitorProductOption(
    val productCode: String,
    val productName: String,
)

data class ProcessMonitorUiState(
    val processKey: MonitorProcessKey,
    val isLoading: Boolean = false,
    val hasInitialData: Boolean = false,
    val productionDay: String = MesCalendarUtils.jstToday(),
    val autoRefresh: Boolean = true,
    val pageVisible: Boolean = true,
    val clockText: String = MonitorLogic.formatClock(),
    val lastUpdatedLabel: String = "",
    val summary: MonitorProcessSummary = MonitorProcessSummary(),
    val overallStats: MonitorOverallStats = MonitorOverallStats(),
    val snackbarMessage: String? = null,
    val lastFetchError: String? = null,
    val nextAssignPanelVisible: Boolean = false,
    val nextAssignments: List<InspectionNextAssignmentDto> = emptyList(),
    val nextAssignPanelRows: List<NextAssignPanelRow> = emptyList(),
    val nextAssignDialog: NextAssignDialogState = NextAssignDialogState(),
    val monitorProducts: List<MonitorProductOption> = emptyList(),
    val shiageInspectors: List<UserListItemDto> = emptyList(),
    val loadingNextAssignData: Boolean = false,
)

class ProcessMonitorViewModel(
    private val processKey: MonitorProcessKey,
    private val inspectionRepository: InspectionRepository,
    private val weldingRepository: WeldingRepository,
    private val systemUserRepository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProcessMonitorUiState(processKey = processKey))
    val uiState: StateFlow<ProcessMonitorUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var clockJob: Job? = null
    private var refreshJob: Job? = null
    private var fetchSeq = 0
    private var lastErrorToastAt = 0L
    private var operatorsLoaded = false

    private var usersById: Map<Int, UserListItemDto> = emptyMap()
    private var cachedInspectionRows: List<InspectionManagementRowDto> = emptyList()
    private var cachedWeldingRows: List<WeldingManagementRowDto> = emptyList()
    private var cachedWeldingMachines: List<MachineDto> = emptyList()
    private var cachedDefectItems: List<ProcessDefectItemDto> = emptyList()
    private var cachedKpiAvgEfficiency: Int? = null
    private var cachedNextAssignments: List<InspectionNextAssignmentDto> = emptyList()
    private var cachedMonitorProducts: List<MonitorProductOption> = emptyList()
    private var cachedShiageInspectors: List<UserListItemDto> = emptyList()

    private val refreshIntervalMs =
        if (processKey == MonitorProcessKey.INSPECTION) INSPECTION_AUTO_REFRESH_MS else WELDING_AUTO_REFRESH_MS

    init {
        startClockTicker()
    }

    fun refreshAll() = fetchAll()

    fun onPageEnter() {
        val state = _uiState.value
        if (!state.hasInitialData && !state.isLoading) {
            fetchAll()
        }
    }

    fun setPageVisible(visible: Boolean) {
        _uiState.update { it.copy(pageVisible = visible) }
        if (processKey != MonitorProcessKey.INSPECTION) return
        if (visible) {
            fetchAll(silent = true)
            scheduleAutoRefresh()
        } else {
            refreshJob?.cancel()
        }
    }

    fun setProductionDay(day: String) {
        if (day.isBlank() || day == _uiState.value.productionDay) return
        if (processKey == MonitorProcessKey.INSPECTION) {
            cachedKpiAvgEfficiency = null
        }
        _uiState.update { it.copy(productionDay = day) }
        fetchAll()
    }

    fun setAutoRefresh(enabled: Boolean) {
        _uiState.update { it.copy(autoRefresh = enabled) }
        if (enabled) scheduleAutoRefresh() else refreshJob?.cancel()
    }

    fun openNextAssignPanel() {
        if (processKey != MonitorProcessKey.INSPECTION) return
        _uiState.update { it.copy(nextAssignPanelVisible = true) }
        viewModelScope.launch {
            loadNextAssignSupportData()
            publishNextAssignRows()
        }
    }

    fun closeNextAssignPanel() {
        _uiState.update { it.copy(nextAssignPanelVisible = false) }
    }

    fun openNextAssignDialogForRow(row: NextAssignPanelRow) {
        if (processKey != MonitorProcessKey.INSPECTION) return
        viewModelScope.launch {
            loadNextAssignSupportData()
            val existing = cachedNextAssignments.firstOrNull { it.inspectorUserId == row.inspectorUserId }
            _uiState.update {
                it.copy(
                    nextAssignDialog = NextAssignDialogState(
                        visible = true,
                        pickInspector = false,
                        inspectorUserId = row.inspectorUserId,
                        inspectorName = row.inspectorName,
                        currentProductLabel = row.currentProductLabel,
                        isFirstProduct = row.isFirstProduct,
                        productCd = existing?.nextProductCd?.trim().orEmpty(),
                    ),
                )
            }
        }
    }

    fun openNextAssignCreateDialog() {
        if (processKey != MonitorProcessKey.INSPECTION) return
        viewModelScope.launch {
            loadNextAssignSupportData()
            _uiState.update {
                it.copy(
                    nextAssignDialog = NextAssignDialogState(
                        visible = true,
                        pickInspector = true,
                        isFirstProduct = true,
                        currentProductLabel = "—",
                    ),
                )
            }
        }
    }

    fun closeNextAssignDialog() {
        _uiState.update { it.copy(nextAssignDialog = NextAssignDialogState()) }
    }

    fun setNextAssignInspectorUserId(userId: Int?) {
        val existing = userId?.let { id -> cachedNextAssignments.firstOrNull { it.inspectorUserId == id } }
        _uiState.update {
            it.copy(
                nextAssignDialog = it.nextAssignDialog.copy(
                    inspectorUserId = userId,
                    productCd = existing?.nextProductCd?.trim().orEmpty(),
                ),
            )
        }
    }

    fun setNextAssignProductCd(productCd: String) {
        _uiState.update {
            it.copy(nextAssignDialog = it.nextAssignDialog.copy(productCd = productCd))
        }
    }

    fun saveNextAssignment() {
        if (processKey != MonitorProcessKey.INSPECTION) return
        val dialog = _uiState.value.nextAssignDialog
        val inspectorUserId = dialog.inspectorUserId
        val productCd = dialog.productCd.trim()
        if (inspectorUserId == null || inspectorUserId <= 0) {
            _uiState.update { it.copy(snackbarMessage = "検査員を選択してください") }
            return
        }
        if (productCd.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "次製品を選択してください") }
            return
        }
        val product = cachedMonitorProducts.firstOrNull { it.productCode == productCd }
        if (product == null) {
            _uiState.update { it.copy(snackbarMessage = "検査対象外の製品です") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(nextAssignDialog = it.nextAssignDialog.copy(submitting = true)) }
            try {
                inspectionRepository.upsertNextAssignment(
                    productionDay = _uiState.value.productionDay,
                    inspectorUserId = inspectorUserId,
                    productCd = product.productCode,
                    productName = product.productName,
                )
                closeNextAssignDialog()
                _uiState.update { it.copy(snackbarMessage = "次製品を指定しました") }
                fetchAll(silent = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = NetworkErrors.formatLoadError(e, "次製品の指定に失敗しました"),
                        nextAssignDialog = it.nextAssignDialog.copy(submitting = false),
                    )
                }
            }
        }
    }

    fun clearNextAssignment() {
        if (processKey != MonitorProcessKey.INSPECTION) return
        val inspectorUserId = _uiState.value.nextAssignDialog.inspectorUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(nextAssignDialog = it.nextAssignDialog.copy(submitting = true)) }
            try {
                inspectionRepository.deleteNextAssignment(
                    productionDay = _uiState.value.productionDay,
                    inspectorUserId = inspectorUserId,
                )
                closeNextAssignDialog()
                _uiState.update { it.copy(snackbarMessage = "次製品指定を解除しました") }
                fetchAll(silent = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = NetworkErrors.formatLoadError(e, "解除に失敗しました"),
                        nextAssignDialog = it.nextAssignDialog.copy(submitting = false),
                    )
                }
            }
        }
    }

    fun hasNextAssignmentForInspector(inspectorUserId: Int?): Boolean {
        if (inspectorUserId == null) return false
        return cachedNextAssignments.any { it.inspectorUserId == inspectorUserId }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun startClockTicker() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                runCatching {
                    publishTickSummary(System.currentTimeMillis())
                }
                delay(CLOCK_TICK_MS)
            }
        }
    }

    private fun scheduleAutoRefresh() {
        refreshJob?.cancel()
        if (!_uiState.value.autoRefresh) return
        if (processKey == MonitorProcessKey.INSPECTION && !_uiState.value.pageVisible) return
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(refreshIntervalMs)
                if (_uiState.value.autoRefresh &&
                    (processKey != MonitorProcessKey.INSPECTION || _uiState.value.pageVisible)
                ) {
                    fetchAll(silent = true)
                }
            }
        }
    }

    fun fetchAll(silent: Boolean = false) {
        if (processKey == MonitorProcessKey.INSPECTION && !_uiState.value.pageVisible && silent) return

        val seq = ++fetchSeq
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isLoading = true, lastFetchError = null) }
            }
            try {
                val day = _uiState.value.productionDay
                val users = loadUsers()
                usersById = users
                mergeUsersFromInspectionRows(cachedInspectionRows)

                if (seq != fetchSeq) return@launch

                when (processKey) {
                    MonitorProcessKey.INSPECTION -> {
                        if (cachedDefectItems.isEmpty()) {
                            cachedDefectItems = inspectionRepository.loadDefectItems()
                        }
                        if (seq != fetchSeq) return@launch
                        val (summaryPair, nextList) = coroutineScope {
                            val summaryDeferred = async { inspectionRepository.loadMonitorSummary(day) }
                            val nextDeferred = async {
                                runCatching { inspectionRepository.loadNextAssignments(day) }
                                    .getOrDefault(emptyList())
                            }
                            summaryDeferred.await() to nextDeferred.await()
                        }
                        if (seq != fetchSeq) return@launch
                        cachedInspectionRows = summaryPair.first
                        cachedNextAssignments = nextList
                        val fetchedAt = summaryPair.second
                        mergeUsersFromInspectionRows(summaryPair.first)
                        cachedKpiAvgEfficiency = MonitorLogic.computeCompletedInspectorAvgEfficiency(summaryPair.first, usersById)
                        val updatedLabel = formatFetchedAtLabel(fetchedAt)
                        publishSummary(
                            seq = seq,
                            silent = silent,
                            lastFetchError = null,
                            lastUpdatedLabel = updatedLabel,
                        )
                    }
                    MonitorProcessKey.WELDING -> {
                        cachedWeldingRows = weldingRepository.loadMonitorPlans(day)
                        if (cachedWeldingMachines.isEmpty()) {
                            cachedWeldingMachines = weldingRepository.loadWeldingMesMachines()
                        }
                        if (seq != fetchSeq) return@launch
                        publishSummary(
                            seq = seq,
                            silent = silent,
                            lastFetchError = null,
                            lastUpdatedLabel = MonitorLogic.formatClock(),
                        )
                    }
                }
                if (_uiState.value.autoRefresh && refreshJob?.isActive != true) {
                    scheduleAutoRefresh()
                }
            } catch (e: Exception) {
                if (seq != fetchSeq) return@launch
                val message = formatFetchError(e)
                val now = System.currentTimeMillis()
                val showToast = !silent || now - lastErrorToastAt >= FETCH_ERROR_TOAST_COOLDOWN_MS
                if (showToast) lastErrorToastAt = now
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastFetchError = message,
                        snackbarMessage = if (showToast) message else it.snackbarMessage,
                    )
                }
            }
        }
    }

    private fun publishSummary(
        seq: Int,
        silent: Boolean,
        lastFetchError: String?,
        lastUpdatedLabel: String,
    ) {
        if (seq != fetchSeq) return
        val now = System.currentTimeMillis()
        val summary = buildSummary(now)
        val panelRows = if (processKey == MonitorProcessKey.INSPECTION) {
            buildNextAssignPanelRows(summary)
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                hasInitialData = true,
                summary = summary,
                overallStats = MonitorLogic.buildOverallStats(
                    processKey,
                    summary,
                    cachedKpiAvgEfficiency,
                ),
                lastFetchError = lastFetchError,
                lastUpdatedLabel = lastUpdatedLabel,
                snackbarMessage = if (!silent && lastFetchError != null) lastFetchError else it.snackbarMessage,
                nextAssignments = if (processKey == MonitorProcessKey.INSPECTION) cachedNextAssignments else emptyList(),
                nextAssignPanelRows = panelRows,
                monitorProducts = cachedMonitorProducts,
                shiageInspectors = cachedShiageInspectors,
            )
        }
    }

    private fun publishTickSummary(tickNow: Long) {
        if (!_uiState.value.hasInitialData) {
            _uiState.update {
                it.copy(
                    clockText = if (processKey == MonitorProcessKey.INSPECTION) {
                        MonitorLogic.formatMonitorDateTime(tickNow)
                    } else {
                        MonitorLogic.formatClock(tickNow)
                    },
                )
            }
            return
        }
        val summary = buildSummary(tickNow)
        val panelRows = if (processKey == MonitorProcessKey.INSPECTION) {
            buildNextAssignPanelRows(summary)
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                clockText = if (processKey == MonitorProcessKey.INSPECTION) {
                    MonitorLogic.formatMonitorDateTime(tickNow)
                } else {
                    MonitorLogic.formatClock(tickNow)
                },
                summary = summary,
                overallStats = MonitorLogic.buildOverallStats(
                    processKey,
                    summary,
                    cachedKpiAvgEfficiency,
                ),
                nextAssignPanelRows = panelRows,
            )
        }
    }

    private suspend fun loadNextAssignSupportData() {
        if (processKey != MonitorProcessKey.INSPECTION) return
        _uiState.update { it.copy(loadingNextAssignData = true) }
        try {
            if (cachedMonitorProducts.isEmpty()) {
                cachedMonitorProducts = inspectionRepository.loadProducts().map {
                    MonitorProductOption(
                        productCode = it.normalizedCode(),
                        productName = it.normalizedName(),
                    )
                }
            }
            if (cachedShiageInspectors.isEmpty()) {
                cachedShiageInspectors = inspectionRepository.loadShiageSectionInspectors()
            }
            _uiState.update {
                it.copy(
                    monitorProducts = cachedMonitorProducts,
                    shiageInspectors = cachedShiageInspectors,
                    loadingNextAssignData = false,
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(loadingNextAssignData = false) }
        }
    }

    private fun publishNextAssignRows() {
        val summary = _uiState.value.summary
        _uiState.update {
            it.copy(nextAssignPanelRows = buildNextAssignPanelRows(summary))
        }
    }

    private fun buildNextAssignPanelRows(summary: MonitorProcessSummary): List<NextAssignPanelRow> {
        val nameById = usersById.mapValues { (_, user) ->
            user.fullName?.trim().orEmpty().ifBlank { user.username?.trim().orEmpty() }
        }
        return MonitorLogic.buildNextAssignPanelRows(
            machines = summary.machines,
            nextAssignments = cachedNextAssignments,
            inspectorNameById = nameById,
        )
    }

    private suspend fun loadUsers(): Map<Int, UserListItemDto> {
        if (operatorsLoaded && usersById.isNotEmpty()) return usersById
        val res = systemUserRepository.getUsers(status = "active", page = 1, pageSize = 500)
        val map = res.getOrNull()?.items.orEmpty()
            .filter { it.id != null }
            .associateBy { it.id!! }
        operatorsLoaded = map.isNotEmpty()
        return map
    }

    private fun mergeUsersFromInspectionRows(rows: List<InspectionManagementRowDto>) {
        if (rows.isEmpty()) return
        val merged = usersById.toMutableMap()
        for (row in rows) {
            val id = row.mesInspectorUserId ?: continue
            if (merged.containsKey(id)) continue
            val fullName = row.mesInspectorName?.trim().orEmpty()
            val username = row.mesInspectorUsername?.trim().orEmpty()
            if (fullName.isEmpty() && username.isEmpty()) continue
            merged[id] = UserListItemDto(
                id = id,
                fullName = fullName.ifEmpty { null },
                username = username.ifEmpty { fullName },
            )
        }
        usersById = merged
    }

    private fun buildSummary(tickNow: Long): MonitorProcessSummary = when (processKey) {
        MonitorProcessKey.INSPECTION -> MonitorLogic.buildInspectionSummary(
            cachedInspectionRows,
            usersById,
            cachedDefectItems,
            tickNow,
            cachedNextAssignments,
        )
        MonitorProcessKey.WELDING -> MonitorLogic.buildWeldingSummary(
            cachedWeldingRows,
            cachedWeldingMachines,
            usersById,
            tickNow,
        )
    }

    private fun formatFetchedAtLabel(fetchedAt: String?): String {
        val ms = MesDateTime.parseToMillis(fetchedAt)
        return when {
            processKey == MonitorProcessKey.INSPECTION && ms != null ->
                MonitorLogic.formatMonitorDateTime(fetchedAt)
            ms != null -> MonitorLogic.formatHistoryTime(fetchedAt)
            else -> MonitorLogic.formatClock()
        }
    }

    private fun formatFetchError(e: Exception): String {
        val msg = NetworkErrors.formatLoadError(e, "データの取得に失敗しました")
        return if (msg.contains("403") || msg.contains("アクセス権")) {
            "検査モニタへのアクセス権がありません"
        } else {
            msg
        }
    }

    class Factory(
        private val processKey: MonitorProcessKey,
        private val inspectionRepository: InspectionRepository,
        private val weldingRepository: WeldingRepository,
        private val systemUserRepository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProcessMonitorViewModel(
                processKey,
                inspectionRepository,
                weldingRepository,
                systemUserRepository,
            ) as T
        }
    }
}
