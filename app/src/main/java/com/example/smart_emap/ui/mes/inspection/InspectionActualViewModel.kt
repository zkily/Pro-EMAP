package com.example.smart_emap.ui.mes.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.mes.InspectionOfflineStore
import com.example.smart_emap.core.mes.InspectionRowSnapshot
import com.example.smart_emap.core.mes.InspectionSessionLogic
import com.example.smart_emap.core.mes.PendingCreatePlan
import com.example.smart_emap.core.mes.PlanSession
import com.example.smart_emap.core.mes.TimerPhase
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.core.network.NetworkMonitor
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.PatchInspectionBody
import com.example.smart_emap.data.repository.InspectionPatchException
import com.example.smart_emap.data.repository.InspectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MesLockOwner { Unclaimed, Mine, Other }

enum class InspRetryAction {
    RefreshAll,
    ReloadProducts,
    ReloadPlans,
    ReloadDefects,
    ReloadSync,
}

private fun jstToday(): String {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return java.time.LocalDate.now(ZoneId.of("Asia/Tokyo")).format(fmt)
}

data class DefectItemUi(
    val id: String,
    val label: String,
)

data class DefectGroupUi(
    val processCd: String,
    val processName: String,
    val items: List<DefectItemUi>,
)

data class InspectionUiState(
    val locale: InspLocale = InspLocale.Ja,
    val productionDay: String = jstToday(),
    val inspectorLabel: String = "",
    val products: List<ErpProductDto> = emptyList(),
    val selectedProductCode: String? = null,
    val inProgressRows: List<InspectionManagementRowDto> = emptyList(),
    val completedRows: List<InspectionManagementRowDto> = emptyList(),
    val defectGroups: List<DefectGroupUi> = emptyList(),
    val activePlanId: Int? = null,
    val displayProductCd: String = "—",
    val displayProductName: String = "—",
    val isLoadingPlans: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val productsLoadError: String? = null,
    val plansLoadError: String? = null,
    val defectsLoadError: String? = null,
    val syncStaleMessage: String? = null,
    val isLoadingDefects: Boolean = false,
    val snackbarMessage: String? = null,
    val scanDialogVisible: Boolean = false,
    val endDialogVisible: Boolean = false,
    val endDialogQty: String = "",
    val endDialogSubmitting: Boolean = false,
    val timerPhase: TimerPhase = TimerPhase.Idle,
    val timerPhaseLabel: String = "未開始",
    val elapsedDisplay: String = "00:00",
    val pausedDisplay: String = "00:00",
    val wallStartDisplay: String = "—",
    val wallEndDisplay: String = "—",
    val defectTotal: Int = 0,
    val canStart: Boolean = false,
    val canPause: Boolean = false,
    val canResume: Boolean = false,
    val canEnd: Boolean = false,
    val showPlanCard: Boolean = false,
    val productSelectionLocked: Boolean = false,
    val canEditDefects: Boolean = false,
    val completedQtyTotal: Int = 0,
    val showSessionRecoveryAlert: Boolean = false,
    val confirmedEditVisible: Boolean = false,
    val confirmedEditPlanId: Int? = null,
    val confirmedEditProductLabel: String = "",
    val confirmedEditQty: String = "",
    val confirmedEditWallStartMs: Long? = null,
    val confirmedEditWallEndMs: Long? = null,
    val confirmedEditPausedSec: String = "",
    val confirmedEditRemarks: String = "",
    val confirmedEditDefects: Map<String, Int> = emptyMap(),
    val confirmedEditSubmitting: Boolean = false,
    val isNetworkOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val isOfflineMode: Boolean = false,
    val showActiveProductionSwitchBanner: Boolean = false,
    val activeProductionSwitchLabel: String = "",
)

class InspectionActualViewModel(
    private val repository: InspectionRepository,
    private val offlineStore: InspectionOfflineStore,
    private val networkMonitor: NetworkMonitor,
    private val userId: Int,
    private val inspectorLabel: String,
) : ViewModel() {
    private val sessions = mutableMapOf<Int, PlanSession>()
    private val locallyOperated = mutableSetOf<Int>()
    private var managementRows = listOf<InspectionManagementRowDto>()
    private var defectItems = listOf<ProcessDefectItemDto>()
    private var tickJob: Job? = null
    private var syncJob: Job? = null
    private var syncInFlight = false
    private var flushInFlight = false
    private var clientInstanceId: String = ""
    private var confirmedEditSnapshot: ConfirmedEditSnapshot? = null

    private data class ConfirmedEditSnapshot(
        val wallStart: Long?,
        val wallEnd: Long?,
        val pausedAccumMs: Long,
        val activeAccumMs: Long,
        val defects: Map<String, Int>,
    )

    private val _uiState = MutableStateFlow(InspectionUiState(inspectorLabel = inspectorLabel))
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            clientInstanceId = repository.getClientInstanceId()
            hydrateFromOfflineCache()
            refreshPendingSyncState()
            loadInitial()
            startTickLoop()
            startSyncLoop()
            if (networkMonitor.currentOnline()) {
                flushOfflineQueue()
            }
        }
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isNetworkOnline = online, isOfflineMode = !online || it.pendingSyncCount > 0) }
                if (online) {
                    flushOfflineQueue()
                    syncPlansFromServer(force = true)
                }
            }
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        syncJob?.cancel()
        super.onCleared()
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun setLocale(locale: InspLocale) {
        _uiState.update { it.copy(locale = locale) }
    }

    fun openScanDialog() {
        val s = inspStringsFor(_uiState.value.locale)
        if (_uiState.value.productSelectionLocked) {
            _uiState.update { it.copy(snackbarMessage = s.switchProductBlocked) }
            return
        }
        _uiState.update { it.copy(scanDialogVisible = true) }
    }

    fun closeScanDialog() {
        _uiState.update { it.copy(scanDialogVisible = false) }
    }

    fun onProductBarcodeScanned(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return
        val s = inspStringsFor(_uiState.value.locale)
        if (_uiState.value.productSelectionLocked) {
            _uiState.update { it.copy(snackbarMessage = s.switchProductBlocked, scanDialogVisible = false) }
            return
        }
        if (!Regex("^\\d{5}$").matches(trimmed)) {
            _uiState.update { it.copy(snackbarMessage = s.scanProductInvalidDigits) }
            return
        }
        val productCd = resolveProductCodeFromScan(trimmed, _uiState.value.products)
        if (productCd == null) {
            _uiState.update { it.copy(snackbarMessage = "${s.scanProductNotFound} ($trimmed)") }
            return
        }
        val product = _uiState.value.products.find { it.productCode == productCd }
        val label = product?.productName?.trim()?.takeIf { it.isNotEmpty() } ?: productCd
        onProductSelected(productCd)
        _uiState.update {
            it.copy(
                scanDialogVisible = false,
                snackbarMessage = "${s.scanProductSelected} ($label)",
            )
        }
    }

    fun refresh() {
        viewModelScope.launch { loadPlans(showLoading = true) }
    }

    fun refreshAll() {
        viewModelScope.launch { loadInitial() }
    }

    fun retryLoad(action: InspRetryAction) {
        viewModelScope.launch {
            when (action) {
                InspRetryAction.RefreshAll -> loadInitial()
                InspRetryAction.ReloadProducts -> fetchProducts(showSnackbarOnError = false)
                InspRetryAction.ReloadPlans -> loadPlans(showLoading = true)
                InspRetryAction.ReloadDefects -> loadDefectItems(showLoading = true)
                InspRetryAction.ReloadSync -> {
                    flushOfflineQueue()
                    syncPlansFromServer(force = true)
                }
            }
        }
    }

    fun dismissLoadError(action: InspRetryAction) {
        _uiState.update { state ->
            when (action) {
                InspRetryAction.ReloadProducts -> state.copy(productsLoadError = null)
                InspRetryAction.ReloadPlans -> state.copy(plansLoadError = null)
                InspRetryAction.ReloadDefects -> state.copy(defectsLoadError = null)
                InspRetryAction.ReloadSync -> state.copy(syncStaleMessage = null)
                InspRetryAction.RefreshAll -> state.copy(
                    productsLoadError = null,
                    plansLoadError = null,
                    defectsLoadError = null,
                )
            }
        }
    }

    fun shiftProductionDay(delta: Int) {
        val day = shiftDateYmd(_uiState.value.productionDay, delta)
        _uiState.update { it.copy(productionDay = day) }
        viewModelScope.launch { loadPlans(showLoading = true) }
    }

    fun setProductionDayToday() {
        _uiState.update { it.copy(productionDay = jstToday()) }
        viewModelScope.launch { loadPlans(showLoading = true) }
    }

    fun onProductSelected(code: String?) {
        if (_uiState.value.productSelectionLocked && code != _uiState.value.selectedProductCode) {
            _uiState.update { it.copy(snackbarMessage = "生産中は製品を変更できません。先に生産終了してください。") }
            return
        }
        _uiState.update { it.copy(selectedProductCode = code) }
        bindActivePlanFromSelection()
        publishUi()
    }

    fun focusInProgressRow(row: InspectionManagementRowDto) {
        val code = row.productCd ?: return
        _uiState.update { it.copy(selectedProductCode = code, activePlanId = row.id) }
        row.id?.let { id ->
            if (shouldHydrateSessionFromServer(id)) syncSessionFromRow(id)
        }
        publishUi()
    }

    fun canResumeSession(row: InspectionManagementRowDto): Boolean {
        if (row.id == null || !isRowMesActive(row)) return false
        return rowMesLockOwner(row) != MesLockOwner.Other
    }

    fun resumeActiveSession() {
        val planId = _uiState.value.activePlanId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        resumeInProgressSession(row)
    }

    /** 切回当前检验员正在生产的计划并恢复操作 */
    fun resumeMyActiveProduction() {
        val row = findMyActiveProductionRow() ?: return
        resumeInProgressSession(row)
    }

    private fun findMyActiveProductionRow(): InspectionManagementRowDto? =
        managementRows.firstOrNull { row ->
            isRowMesActive(row) && row.mesInspectorUserId == userId
        }

    fun resumeInProgressSession(row: InspectionManagementRowDto) {
        viewModelScope.launch {
            val s = inspStringsFor(_uiState.value.locale)
            val planId = row.id ?: return@launch
            if (!isRowMesActive(row)) return@launch
            if (row.mesInspectorUserId != null && row.mesInspectorUserId != userId) {
                _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                return@launch
            }
            if (rowMesLockOwner(row) == MesLockOwner.Other) {
                _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                return@launch
            }
            if (rowMesLockOwner(row) != MesLockOwner.Mine) {
                val ok = patchWithConflictHandling(
                    planId,
                    PatchInspectionBody(
                        mesClaimClientLock = true,
                        mesInspectorUserId = userId,
                    ),
                )
                if (!ok) return@launch
                if (networkMonitor.currentOnline()) {
                    loadPlans()
                    val fresh = managementRows.find { it.id == planId }
                    if (fresh == null || rowMesLockOwner(fresh) == MesLockOwner.Other) {
                        _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                        return@launch
                    }
                }
            }
            locallyOperated.add(planId)
            row.productCd?.let { code ->
                _uiState.update { it.copy(selectedProductCode = code, activePlanId = planId) }
            }
            syncSessionFromRow(planId)
            publishUi()
            _uiState.update { it.copy(snackbarMessage = s.sessionResumed) }
        }
    }

    fun onStartProduction() {
        viewModelScope.launch {
            val state = _uiState.value
            val s = inspStringsFor(state.locale)
            val code = state.selectedProductCode ?: return@launch
            val product = state.products.find { it.productCode == code } ?: return@launch
            try {
                val existingRow = findOpenRow(code)
                var planId = existingRow?.id
                if (existingRow != null && isRowMesActive(existingRow)) {
                    when (rowMesLockOwner(existingRow)) {
                        MesLockOwner.Other -> {
                            _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                            return@launch
                        }
                        MesLockOwner.Unclaimed, MesLockOwner.Mine -> {
                            resumeInProgressSession(existingRow)
                            return@launch
                        }
                    }
                }
                if (planId == null) {
                    planId = createPlanResilient(
                        productionDay = state.productionDay,
                        productCd = code,
                        productName = product.productName.trim().ifEmpty { code },
                    )
                    if (planId > 0) {
                        loadPlans(rebindSelection = false)
                    }
                }
                findOtherActiveRowForInspector(userId, planId)?.let { other ->
                    when (rowMesLockOwner(other)) {
                        MesLockOwner.Other -> {
                            _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    snackbarMessage = s.switchedToActiveProduction.replace(
                                        "{label}",
                                        rowShortLabel(other),
                                    ),
                                )
                            }
                            resumeInProgressSession(other)
                        }
                    }
                    return@launch
                }
                _uiState.update { it.copy(activePlanId = planId) }
                var session = ensureSession(planId)
                if (InspectionSessionLogic.isProductionInProgress(session)) {
                    _uiState.update { it.copy(snackbarMessage = s.sessionRecoveryHint) }
                    publishUi()
                    return@launch
                }
                if (session.wallStart != null) {
                    session = InspectionSessionLogic.emptySession(defectItems.map { it.defectCd })
                    sessions[planId] = session
                }
                val now = System.currentTimeMillis()
                val iso = Instant.ofEpochMilli(now).toString()
                locallyOperated.add(planId)
                session.wallStart = now
                session.activeAccumMs = 0
                session.pausedAccumMs = 0
                session.pauseSliceStart = null
                session.runningSliceStart = now
                session.wallEnd = null
                publishUi()
                val ok = patchWithConflictHandling(
                    planId,
                    PatchInspectionBody(
                        productionDay = state.productionDay,
                        mesProductionStartedAt = iso,
                        mesProductionIsPaused = 0,
                        mesInspectorUserId = userId,
                    ),
                )
                if (!ok) {
                    locallyOperated.remove(planId)
                    session.wallStart = null
                    session.runningSliceStart = null
                    publishUi()
                    if (networkMonitor.currentOnline()) {
                        applyPlansFromServer(showLoading = false, rebindSelection = false)
                    }
                    return@launch
                }
                upsertLocalPlanRow(
                    planId = planId,
                    productionDay = state.productionDay,
                    productCd = code,
                    productName = product.productName.trim().ifEmpty { code },
                    startedAt = iso,
                )
                _uiState.update {
                    it.copy(snackbarMessage = inspStringsFor(it.locale).started)
                }
                publishUi()
                if (networkMonitor.currentOnline()) {
                    applyPlansFromServer(showLoading = false, rebindSelection = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = formatNetworkError(e, inspStringsFor(it.locale).saveFailed))
                }
                loadPlans()
            }
        }
    }

    fun onPauseProduction() {
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isTimerRunning(session)) return
        val now = System.currentTimeMillis()
        InspectionSessionLogic.flushRunningSlice(session, now)
        session.pauseSliceStart = now
        viewModelScope.launch {
            persistTimerCheckpoint(planId, session)
            publishUi()
        }
    }

    fun onResumeProduction() {
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isTimerPaused(session)) return
        val now = System.currentTimeMillis()
        InspectionSessionLogic.flushPauseSlice(session, now)
        session.runningSliceStart = now
        session.pauseSliceStart = null
        viewModelScope.launch {
            persistTimerCheckpoint(planId, session)
            publishUi()
        }
    }

    fun openEndDialog() {
        val planId = _uiState.value.activePlanId ?: return
        val session = sessions[planId] ?: return
        if (!_uiState.value.canEnd) return
        val now = System.currentTimeMillis()
        if (InspectionSessionLogic.isTimerRunning(session)) InspectionSessionLogic.flushRunningSlice(session, now)
        if (InspectionSessionLogic.isTimerPaused(session)) InspectionSessionLogic.flushPauseSlice(session, now)
        _uiState.update { it.copy(endDialogVisible = true, endDialogQty = "") }
        publishUi()
    }

    fun closeEndDialog() {
        _uiState.update { it.copy(endDialogVisible = false) }
    }

    fun onEndDialogQtyChange(value: String) {
        _uiState.update { it.copy(endDialogQty = value.filter { it.isDigit() }) }
    }

    fun canEditConfirmedHistoryRow(row: InspectionManagementRowDto): Boolean {
        val inspectorId = row.mesInspectorUserId
        return inspectorId == null || inspectorId == userId
    }

    fun inspectorLabelForHistoryRow(row: InspectionManagementRowDto): String =
        InspectionManagementRowExt.inspectorLabel(row, userId, _uiState.value.inspectorLabel)

    fun openConfirmedHistoryEdit(row: InspectionManagementRowDto) {
        val planId = row.id ?: return
        if ((row.productionCompletedCheck ?: 0) != 1) return
        val s = inspStringsFor(_uiState.value.locale)
        if (!canEditConfirmedHistoryRow(row)) {
            _uiState.update { it.copy(snackbarMessage = s.cannotEditOthersRecord) }
            return
        }
        val session = ensureSession(planId)
        syncSessionFromRow(planId, row)
        val wallStartMs = HistoryRowFormat.parseIsoToMillis(row.mesProductionStartedAt)
        val wallEndMs = HistoryRowFormat.parseIsoToMillis(row.mesProductionEndedAt)
        confirmedEditSnapshot = ConfirmedEditSnapshot(
            wallStart = wallStartMs,
            wallEnd = wallEndMs,
            pausedAccumMs = session.pausedAccumMs,
            activeAccumMs = session.activeAccumMs,
            defects = session.defects.toMap(),
        )
        val productLabel = listOfNotNull(
            row.productCd?.trim()?.takeIf { it.isNotEmpty() },
            row.productName?.trim()?.takeIf { it.isNotEmpty() },
        ).joinToString(" · ").ifEmpty { "—" }
        _uiState.update {
            it.copy(
                confirmedEditVisible = true,
                confirmedEditPlanId = planId,
                confirmedEditProductLabel = productLabel,
                confirmedEditQty = (row.actualProductionQuantity ?: 0).toString(),
                confirmedEditWallStartMs = wallStartMs,
                confirmedEditWallEndMs = wallEndMs,
                confirmedEditPausedSec = (
                    row.mesPausedAccumSec ?: HistoryRowFormat.rowPausedAccumSec(row)
                ).coerceAtLeast(0).toString(),
                confirmedEditRemarks = row.remarks.orEmpty(),
                confirmedEditDefects = session.defects.filter { entry -> entry.value > 0 },
                confirmedEditSubmitting = false,
            )
        }
    }

    fun closeConfirmedHistoryEdit() {
        val planId = _uiState.value.confirmedEditPlanId
        val snapshot = confirmedEditSnapshot
        if (planId != null && snapshot != null) {
            val session = ensureSession(planId)
            session.wallStart = snapshot.wallStart
            session.wallEnd = snapshot.wallEnd
            session.pausedAccumMs = snapshot.pausedAccumMs
            session.activeAccumMs = snapshot.activeAccumMs
            session.defects.clear()
            session.defects.putAll(snapshot.defects)
            session.runningSliceStart = null
            session.pauseSliceStart = null
        }
        confirmedEditSnapshot = null
        _uiState.update {
            it.copy(
                confirmedEditVisible = false,
                confirmedEditPlanId = null,
                confirmedEditProductLabel = "",
                confirmedEditQty = "",
                confirmedEditWallStartMs = null,
                confirmedEditWallEndMs = null,
                confirmedEditPausedSec = "",
                confirmedEditRemarks = "",
                confirmedEditDefects = emptyMap(),
                confirmedEditSubmitting = false,
            )
        }
    }

    fun onConfirmedEditQtyChange(value: String) {
        _uiState.update { it.copy(confirmedEditQty = value.filter { it.isDigit() }) }
    }

    fun onConfirmedEditWallStartChange(ms: Long) {
        _uiState.update { it.copy(confirmedEditWallStartMs = ms) }
    }

    fun onConfirmedEditWallEndChange(ms: Long) {
        _uiState.update { it.copy(confirmedEditWallEndMs = ms) }
    }

    fun onConfirmedEditPausedSecChange(value: String) {
        _uiState.update { it.copy(confirmedEditPausedSec = value.filter { it.isDigit() }) }
    }

    fun onConfirmedEditRemarksChange(value: String) {
        _uiState.update { it.copy(confirmedEditRemarks = value) }
    }

    fun bumpConfirmedEditDefect(itemId: String, delta: Int) {
        _uiState.update { state ->
            val current = state.confirmedEditDefects[itemId] ?: 0
            val next = (current + delta).coerceAtLeast(0)
            val updated = state.confirmedEditDefects.toMutableMap()
            if (next == 0) updated.remove(itemId) else updated[itemId] = next
            state.copy(confirmedEditDefects = updated)
        }
    }

    fun confirmedEditDefectCount(itemId: String): Int =
        _uiState.value.confirmedEditDefects[itemId] ?: 0

    fun submitConfirmedHistoryEdit() {
        val state = _uiState.value
        val planId = state.confirmedEditPlanId ?: return
        val s = inspStringsFor(state.locale)
        val qty = state.confirmedEditQty.toIntOrNull()
        if (qty == null || qty < 0) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return
        }
        val ws = state.confirmedEditWallStartMs
        val we = state.confirmedEditWallEndMs
        if (ws == null || we == null) {
            _uiState.update { it.copy(snackbarMessage = s.editTimeRequired) }
            return
        }
        if (we < ws) {
            _uiState.update { it.copy(snackbarMessage = s.editTimeOrder) }
            return
        }
        val wallSec = ((we - ws) / 1000).coerceAtLeast(0)
        val pauseSec = state.confirmedEditPausedSec.toLongOrNull()?.coerceAtLeast(0) ?: 0L
        if (pauseSec > wallSec) {
            _uiState.update { it.copy(snackbarMessage = s.editPauseTooLong) }
            return
        }
        val netSec = (wallSec - pauseSec).coerceAtLeast(0)
        val defects = state.confirmedEditDefects.filter { it.value > 0 }
        val defectTotal = defects.values.sum()
        viewModelScope.launch {
            _uiState.update { it.copy(confirmedEditSubmitting = true) }
            val session = ensureSession(planId)
            session.wallStart = ws
            session.wallEnd = we
            session.pausedAccumMs = pauseSec * 1000
            session.activeAccumMs = netSec * 1000
            session.runningSliceStart = null
            session.pauseSliceStart = null
            session.defects.keys.forEach { k -> session.defects[k] = defects[k] ?: 0 }
            try {
                val ok = patchWithConflictHandling(
                    planId,
                    PatchInspectionBody(
                        productionDay = productionDayFromMillis(ws),
                        mesProductionStartedAt = Instant.ofEpochMilli(ws).toString(),
                        mesProductionEndedAt = Instant.ofEpochMilli(we).toString(),
                        mesPausedAccumSec = pauseSec.toInt(),
                        mesNetProductionSec = netSec.toInt(),
                        mesProductionIsPaused = 0,
                        mesInspectorUserId = userId,
                        mesDefectByItem = defects,
                        actualProductionQuantity = qty,
                        productionCompletedCheck = true,
                        defectQty = defectTotal,
                        remarks = state.confirmedEditRemarks.trim().ifEmpty { null },
                        mesForceRelease = true,
                    ),
                )
                if (!ok) {
                    _uiState.update { it.copy(confirmedEditSubmitting = false) }
                    return@launch
                }
                confirmedEditSnapshot = null
                updateLocalRowFromConfirmedEdit(planId, state, ws, we, qty, defects, defectTotal, pauseSec, netSec)
                _uiState.update {
                    it.copy(
                        confirmedEditVisible = false,
                        confirmedEditPlanId = null,
                        confirmedEditProductLabel = "",
                        confirmedEditQty = "",
                        confirmedEditWallStartMs = null,
                        confirmedEditWallEndMs = null,
                        confirmedEditPausedSec = "",
                        confirmedEditRemarks = "",
                        confirmedEditDefects = emptyMap(),
                        confirmedEditSubmitting = false,
                        snackbarMessage = s.confirmedEditSaved,
                    )
                }
                loadPlans()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        confirmedEditSubmitting = false,
                        snackbarMessage = formatNetworkError(e, inspStringsFor(it.locale).saveFailed),
                    )
                }
            }
        }
    }

    fun submitProductionEnd() {
        val planId = _uiState.value.activePlanId ?: return
        val session = sessions[planId] ?: return
        val qty = _uiState.value.endDialogQty.toIntOrNull() ?: -1
        if (qty < 0) {
            _uiState.update { it.copy(snackbarMessage = "生産数を正しく入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(endDialogSubmitting = true) }
            try {
                val now = System.currentTimeMillis()
                if (InspectionSessionLogic.isTimerRunning(session)) InspectionSessionLogic.flushRunningSlice(session, now)
                if (InspectionSessionLogic.isTimerPaused(session)) InspectionSessionLogic.flushPauseSlice(session, now)
                session.wallEnd = now
                val defectTotal = session.defects.values.sum()
                val ok = patchWithConflictHandling(
                    planId,
                    PatchInspectionBody(
                        productionDay = productionDayFromMillis(session.wallStart ?: now),
                        mesProductionEndedAt = Instant.ofEpochMilli(now).toString(),
                        mesNetProductionSec = (InspectionSessionLogic.readNetProductionMs(session, now) / 1000).toInt(),
                        mesPausedAccumSec = (InspectionSessionLogic.readPausedAccumMs(session, now) / 1000).toInt(),
                        mesProductionIsPaused = 0,
                        mesInspectorUserId = userId,
                        mesDefectByItem = session.defects.filter { it.value > 0 },
                        actualProductionQuantity = qty,
                        productionCompletedCheck = true,
                        defectQty = defectTotal,
                    ),
                )
                if (!ok) {
                    session.wallEnd = null
                    _uiState.update { it.copy(endDialogSubmitting = false) }
                    loadPlans()
                    return@launch
                }
                updateLocalRowCompleted(planId, session, qty, now)
                locallyOperated.remove(planId)
                sessions[planId] = InspectionSessionLogic.emptySession(defectItems.map { it.defectCd })
                val savedMsg = inspStringsFor(_uiState.value.locale).endProductionSaved
                _uiState.update {
                    it.copy(
                        activePlanId = null,
                        endDialogVisible = false,
                        endDialogSubmitting = false,
                        snackbarMessage = savedMsg,
                    )
                }
                if (networkMonitor.currentOnline()) {
                    loadPlans()
                } else {
                    publishUi()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        endDialogSubmitting = false,
                        snackbarMessage = formatNetworkError(e, inspStringsFor(it.locale).saveFailed),
                    )
                }
            }
        }
    }

    fun bumpDefect(itemId: String, delta: Int) {
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        val current = session.defects[itemId] ?: 0
        session.defects[itemId] = (current + delta).coerceAtLeast(0)
        publishUi()
        viewModelScope.launch {
            if (!locallyOperated.contains(planId) && !canServerPatchPlan(planId)) return@launch
            patchWithConflictHandling(
                planId,
                PatchInspectionBody(
                    mesDefectByItem = session.defects.filter { it.value > 0 },
                ),
            )
        }
    }

    fun defectCount(itemId: String): Int {
        val planId = _uiState.value.activePlanId ?: return 0
        return sessions[planId]?.defects?.get(itemId) ?: 0
    }

    private fun formatNetworkError(e: Throwable, fallback: String): String {
        val s = inspStringsFor(_uiState.value.locale)
        return NetworkErrors.formatError(e, fallback, s.networkErrorHints())
    }

    private suspend fun hydrateFromOfflineCache() {
        val cache = offlineStore.loadCache()
        val day = _uiState.value.productionDay
        if (cache.products.isNotEmpty()) {
            _uiState.update { it.copy(products = cache.products) }
        }
        if (cache.defectItems.isNotEmpty()) {
            defectItems = cache.defectItems
            _uiState.update { it.copy(defectGroups = groupDefectItems(cache.defectItems)) }
        }
        cache.plansByDay[day]?.let { rows ->
            managementRows = rows
            publishUi()
        }
    }

    private suspend fun refreshPendingSyncState() {
        val count = offlineStore.pendingCount()
        _uiState.update {
            it.copy(
                pendingSyncCount = count,
                isOfflineMode = !it.isNetworkOnline || count > 0,
            )
        }
    }

    private suspend fun flushOfflineQueue() {
        if (!networkMonitor.currentOnline()) return
        if (flushInFlight) return
        if (offlineStore.pendingCount() == 0) return
        flushInFlight = true
        try {
            val result = offlineStore.flush(
                createPlan = { entry ->
                    val serverId = repository.createPlan(
                        productionDay = entry.productionDay,
                        productCd = entry.productCd,
                        productName = entry.productName,
                        inspectorUserId = entry.inspectorUserId,
                    )
                    remapPlanId(entry.localPlanId, serverId)
                    serverId
                },
                patchPlan = { id, body -> repository.patchPlan(id, body) },
            )
            refreshPendingSyncState()
            if (result.syncedCreates + result.syncedPatches > 0) {
                _uiState.update {
                    it.copy(snackbarMessage = inspStringsFor(it.locale).syncUploadedSuccess)
                }
                loadPlans(showLoading = false)
            }
        } catch (e: Exception) {
            if (!NetworkErrors.isNetworkFailure(e)) {
                _uiState.update {
                    it.copy(snackbarMessage = formatNetworkError(e, inspStringsFor(it.locale).saveFailed))
                }
            }
        } finally {
            flushInFlight = false
        }
    }

    private fun remapPlanId(oldId: Int, newId: Int) {
        sessions.remove(oldId)?.let { sessions[newId] = it }
        if (locallyOperated.remove(oldId)) locallyOperated.add(newId)
        if (_uiState.value.activePlanId == oldId) {
            _uiState.update { it.copy(activePlanId = newId) }
        }
        if (_uiState.value.confirmedEditPlanId == oldId) {
            _uiState.update { it.copy(confirmedEditPlanId = newId) }
        }
        managementRows = managementRows.map { row ->
            if (row.id == oldId) row.copy(id = newId) else row
        }
    }

    private suspend fun createPlanResilient(
        productionDay: String,
        productCd: String,
        productName: String,
    ): Int {
        if (!networkMonitor.currentOnline()) {
            return createLocalPlan(productionDay, productCd, productName)
        }
        return try {
            repository.createPlan(
                productionDay = productionDay,
                productCd = productCd,
                productName = productName,
                inspectorUserId = userId,
            )
        } catch (e: Exception) {
            if (NetworkErrors.isNetworkFailure(e)) {
                createLocalPlan(productionDay, productCd, productName)
            } else {
                throw e
            }
        }
    }

    private suspend fun createLocalPlan(
        productionDay: String,
        productCd: String,
        productName: String,
    ): Int {
        val localId = offlineStore.allocateLocalPlanId()
        offlineStore.enqueueCreate(
            PendingCreatePlan(
                localPlanId = localId,
                productionDay = productionDay,
                productCd = productCd,
                productName = productName,
                inspectorUserId = userId,
            ),
        )
        upsertLocalPlanRow(
            planId = localId,
            productionDay = productionDay,
            productCd = productCd,
            productName = productName,
            startedAt = null,
        )
        refreshPendingSyncState()
        return localId
    }

    private suspend fun upsertLocalPlanRow(
        planId: Int,
        productionDay: String,
        productCd: String,
        productName: String,
        startedAt: String?,
    ) {
        val row = InspectionManagementRowDto(
            id = planId,
            productionDay = productionDay,
            productCd = productCd,
            productName = productName,
            mesInspectorUserId = userId,
            mesClientInstanceId = clientInstanceId,
            mesProductionStartedAt = startedAt,
        )
        managementRows = managementRows.filter { it.id != planId } + row
        offlineStore.savePlans(_uiState.value.productionDay, managementRows)
    }

    private suspend fun queuePatch(planId: Int, body: PatchInspectionBody) {
        offlineStore.enqueuePatch(planId, body)
        refreshPendingSyncState()
    }

    private fun updateLocalRowCompleted(planId: Int, session: PlanSession, qty: Int, endMs: Long) {
        val startIso = session.wallStart?.let { Instant.ofEpochMilli(it).toString() }
        val endIso = Instant.ofEpochMilli(endMs).toString()
        val netSec = (InspectionSessionLogic.readNetProductionMs(session, endMs) / 1000).toInt()
        val pauseSec = (InspectionSessionLogic.readPausedAccumMs(session, endMs) / 1000).toInt()
        managementRows = managementRows.map { row ->
            if (row.id != planId) {
                row
            } else {
                row.copy(
                    productionCompletedCheck = 1,
                    actualProductionQuantity = qty,
                    defectQty = session.defects.values.sum(),
                    mesDefectByItem = session.defects.filter { it.value > 0 },
                    mesProductionStartedAt = startIso ?: row.mesProductionStartedAt,
                    mesProductionEndedAt = endIso,
                    mesNetProductionSec = netSec,
                    mesPausedAccumSec = pauseSec,
                    mesProductionIsPaused = 0,
                )
            }
        }
        viewModelScope.launch {
            offlineStore.savePlans(_uiState.value.productionDay, managementRows)
        }
    }

    private fun updateLocalRowFromConfirmedEdit(
        planId: Int,
        state: InspectionUiState,
        wallStartMs: Long,
        wallEndMs: Long,
        qty: Int,
        defects: Map<String, Int>,
        defectTotal: Int,
        pauseSec: Long,
        netSec: Long,
    ) {
        val startIso = Instant.ofEpochMilli(wallStartMs).toString()
        val endIso = Instant.ofEpochMilli(wallEndMs).toString()
        managementRows = managementRows.map { row ->
            if (row.id != planId) {
                row
            } else {
                row.copy(
                    productionCompletedCheck = 1,
                    actualProductionQuantity = qty,
                    defectQty = defectTotal,
                    mesDefectByItem = defects,
                    mesProductionStartedAt = startIso,
                    mesProductionEndedAt = endIso,
                    mesPausedAccumSec = pauseSec.toInt(),
                    mesNetProductionSec = netSec.toInt(),
                    mesProductionIsPaused = 0,
                    remarks = state.confirmedEditRemarks.trim().ifEmpty { null },
                )
            }
        }
        viewModelScope.launch {
            offlineStore.savePlans(_uiState.value.productionDay, managementRows)
        }
    }

    private suspend fun fetchProducts(showSnackbarOnError: Boolean = false) {
        _uiState.update { it.copy(isLoadingProducts = true) }
        val s = inspStringsFor(_uiState.value.locale)
        runCatching { repository.loadProducts() }
            .onSuccess { list ->
                _uiState.update {
                    it.copy(
                        products = list,
                        isLoadingProducts = false,
                        productsLoadError = null,
                    )
                }
                offlineStore.saveProducts(list)
            }
            .onFailure { e ->
                val msg = formatNetworkError(e, s.loadProductsFailed)
                _uiState.update {
                    it.copy(
                        isLoadingProducts = false,
                        productsLoadError = msg,
                        snackbarMessage = if (showSnackbarOnError) msg else it.snackbarMessage,
                    )
                }
            }
    }

    fun reloadProducts() {
        viewModelScope.launch { fetchProducts(showSnackbarOnError = true) }
    }

    private suspend fun loadInitial() {
        _uiState.update { it.copy(isLoadingDefects = true) }
        fetchProducts(showSnackbarOnError = false)
        loadDefectItems(showLoading = true)
        loadPlans(showLoading = true)
    }

    private suspend fun loadDefectItems(showLoading: Boolean = false) {
        if (showLoading) {
            _uiState.update { it.copy(isLoadingDefects = true) }
        }
        val s = inspStringsFor(_uiState.value.locale)
        runCatching { repository.loadDefectItems() }
            .onSuccess { items ->
                defectItems = items
                _uiState.update {
                    it.copy(
                        defectGroups = groupDefectItems(items),
                        isLoadingDefects = false,
                        defectsLoadError = null,
                    )
                }
                offlineStore.saveDefectItems(items)
            }
            .onFailure { e ->
                val msg = formatNetworkError(e, s.loadDefectsFailed)
                _uiState.update {
                    it.copy(
                        isLoadingDefects = false,
                        defectsLoadError = msg,
                    )
                }
            }
    }

    private suspend fun loadPlans(showLoading: Boolean = false, rebindSelection: Boolean = true) {
        applyPlansFromServer(showLoading = showLoading, rebindSelection = rebindSelection)
    }

    /** Web の syncMesStateFromServer：バックグラウンド同期はローディング表示しない */
    private suspend fun syncPlansFromServer(force: Boolean = false) {
        if (!force && syncInFlight) return
        syncInFlight = true
        try {
            applyPlansFromServer(showLoading = false, rebindSelection = false)
        } finally {
            syncInFlight = false
        }
    }

    private suspend fun applyPlansFromServer(showLoading: Boolean, rebindSelection: Boolean) {
        if (showLoading) {
            _uiState.update { it.copy(isLoadingPlans = true) }
        }
        val s = inspStringsFor(_uiState.value.locale)
        runCatching { repository.loadPlans(_uiState.value.productionDay) }
            .onSuccess { rows ->
                managementRows = rows
                rows.forEach { row ->
                    val id = row.id ?: return@forEach
                    if (id !in sessions) {
                        sessions[id] = InspectionSessionLogic.emptySession(defectItems.map { it.defectCd })
                    }
                    if (shouldHydrateSessionFromServer(id)) {
                        syncSessionFromRow(id, row)
                    }
                }
                val wasStale = _uiState.value.syncStaleMessage != null
                _uiState.update { state ->
                    state.copy(
                        isLoadingPlans = if (showLoading) false else state.isLoadingPlans,
                        plansLoadError = if (showLoading) null else state.plansLoadError,
                        syncStaleMessage = null,
                        snackbarMessage = if (wasStale && !showLoading) s.syncRecovered else state.snackbarMessage,
                    )
                }
                if (rebindSelection) {
                    bindActivePlanFromSelection()
                }
                tryReclaimOperatedPlansOnLoad()
                detachFromRemoteInProgressContext()
                publishUi()
                offlineStore.savePlans(_uiState.value.productionDay, managementRows)
            }
            .onFailure { e ->
                val msg = formatNetworkError(e, s.loadPlansFailed)
                val cached = offlineStore.loadCache().plansByDay[_uiState.value.productionDay]
                if (!cached.isNullOrEmpty()) {
                    managementRows = cached
                    if (rebindSelection) bindActivePlanFromSelection()
                    publishUi()
                }
                if (showLoading) {
                    _uiState.update { it.copy(isLoadingPlans = false, plansLoadError = msg) }
                } else {
                    _uiState.update { it.copy(syncStaleMessage = msg) }
                }
            }
    }

    private fun bindActivePlanFromSelection() {
        val code = _uiState.value.selectedProductCode
        if (code == null) {
            _uiState.update { it.copy(activePlanId = null, showPlanCard = false) }
            return
        }
        val currentId = _uiState.value.activePlanId
        val planId = when {
            currentId != null &&
                locallyOperated.contains(currentId) &&
                managementRows.find { it.id == currentId }?.productCd == code -> currentId
            else -> findOpenRow(code)?.id
        }
        _uiState.update { it.copy(activePlanId = planId, showPlanCard = true) }
        planId?.let { id ->
            if (shouldHydrateSessionFromServer(id)) syncSessionFromRow(id)
        }
    }

    /** 本端末で計測中のセッションはサーバー同期で上書きしない */
    private fun shouldHydrateSessionFromServer(planId: Int): Boolean {
        if (!locallyOperated.contains(planId)) return true
        val session = sessions[planId] ?: return true
        return !InspectionSessionLogic.isProductionInProgress(session)
    }

    private fun isRowMesEnded(row: InspectionManagementRowDto): Boolean =
        !row.mesProductionEndedAt.isNullOrBlank()

    private fun findOpenRow(code: String): InspectionManagementRowDto? {
        val operatedId = _uiState.value.activePlanId
        if (operatedId != null && locallyOperated.contains(operatedId)) {
            managementRows.find { it.id == operatedId && it.productCd == code }?.let { return it }
        }
        return managementRows.firstOrNull { row ->
            row.productCd == code &&
                (row.productionCompletedCheck ?: 0) != 1 &&
                row.mesInspectorUserId == userId &&
                !isRowMesEnded(row)
        }
    }

    /** 同一検査員が別製品を同時に生産中か（Web findOtherActiveRowForInspector） */
    private fun findOtherActiveRowForInspector(
        inspectorId: Int,
        excludeId: Int,
    ): InspectionManagementRowDto? {
        return managementRows.firstOrNull { row ->
            row.id != excludeId &&
                isRowMesActive(row) &&
                row.mesInspectorUserId == inspectorId
        }
    }

    private fun rowShortLabel(row: InspectionManagementRowDto): String {
        val name = (row.productName ?: row.productCd ?: "").trim()
        return name.ifEmpty { "#${row.id}" }
    }

    private fun syncSessionFromRow(planId: Int, row: InspectionManagementRowDto? = managementRows.find { it.id == planId }) {
        val r = row ?: return
        val session = ensureSession(planId)
        InspectionSessionLogic.hydrateFromRow(
            session,
            InspectionRowSnapshot(
                mesProductionStartedAt = r.mesProductionStartedAt,
                mesProductionEndedAt = r.mesProductionEndedAt,
                mesNetProductionSec = r.mesNetProductionSec,
                mesPausedAccumSec = r.mesPausedAccumSec,
                mesProductionIsPaused = r.mesProductionIsPaused,
                mesDefectByItem = r.mesDefectByItem,
            ),
        )
    }

    private fun rowMesLockOwner(row: InspectionManagementRowDto?): MesLockOwner {
        if (row == null) return MesLockOwner.Unclaimed
        val lock = row.mesClientInstanceId?.trim().orEmpty()
        if (lock.isEmpty()) return MesLockOwner.Unclaimed
        return if (lock == clientInstanceId) MesLockOwner.Mine else MesLockOwner.Other
    }

    private fun canServerPatchPlan(planId: Int): Boolean {
        if (offlineStore.isLocalPlanId(planId) && locallyOperated.contains(planId)) return true
        if (!networkMonitor.currentOnline() && locallyOperated.contains(planId)) return true
        val row = managementRows.find { it.id == planId } ?: return locallyOperated.contains(planId)
        if (!isRowMesActive(row)) return false
        return when (rowMesLockOwner(row)) {
            MesLockOwner.Other -> false
            MesLockOwner.Mine -> true
            MesLockOwner.Unclaimed -> locallyOperated.contains(planId)
        }
    }

    private suspend fun patchWithConflictHandling(planId: Int, body: PatchInspectionBody): Boolean {
        val s = inspStringsFor(_uiState.value.locale)
        val hints = s.networkErrorHints()
        if (!networkMonitor.currentOnline()) {
            queuePatch(planId, body)
            return true
        }
        val resolvedId = offlineStore.resolvePlanId(planId)
        if (resolvedId < 0) {
            queuePatch(planId, body)
            return true
        }
        return try {
            repository.patchPlan(resolvedId, body)
            true
        } catch (e: InspectionPatchException) {
            val msg = if (e.statusCode in 500..599) {
                NetworkErrors.formatHttpError(e.statusCode, e.message, s.saveFailed, hints)
            } else {
                e.message
            }
            if (e.statusCode == 409) {
                _uiState.update { it.copy(snackbarMessage = msg) }
                syncPlansFromServer(force = true)
                detachFromRemoteInProgressContext()
                publishUi()
            } else if (NetworkErrors.isNetworkFailure(e)) {
                queuePatch(planId, body)
                return true
            } else {
                _uiState.update { it.copy(snackbarMessage = msg) }
            }
            false
        } catch (e: Exception) {
            if (NetworkErrors.isNetworkFailure(e)) {
                queuePatch(planId, body)
                return true
            }
            _uiState.update { it.copy(snackbarMessage = formatNetworkError(e, s.saveFailed)) }
            false
        }
    }

    private fun tryReclaimOperatedPlansOnLoad() {
        val row = managementRows.firstOrNull { r ->
            r.mesInspectorUserId == userId && isRowMesActive(r)
        } ?: return
        val id = row.id ?: return
        if (rowMesLockOwner(row) == MesLockOwner.Mine) {
            locallyOperated.add(id)
            if (shouldHydrateSessionFromServer(id)) {
                syncSessionFromRow(id, row)
            }
        }
    }

    private fun detachFromRemoteInProgressContext() {
        val planId = _uiState.value.activePlanId ?: return
        if (locallyOperated.contains(planId) &&
            sessions[planId]?.let { InspectionSessionLogic.isProductionInProgress(it) } == true
        ) {
            return
        }
        val row = managementRows.find { it.id == planId } ?: return
        if (!isRowMesActive(row)) return
        when (rowMesLockOwner(row)) {
            MesLockOwner.Mine -> {
                if (!locallyOperated.contains(planId)) locallyOperated.add(planId)
            }
            MesLockOwner.Other -> {
                locallyOperated.remove(planId)
                _uiState.update { it.copy(selectedProductCode = null, activePlanId = null) }
            }
            MesLockOwner.Unclaimed -> Unit
        }
    }

    private fun ensureSession(planId: Int): PlanSession {
        return sessions.getOrPut(planId) {
            InspectionSessionLogic.emptySession(defectItems.map { it.defectCd })
        }
    }

    private fun isRowMesActive(row: InspectionManagementRowDto): Boolean {
        val started = row.mesProductionStartedAt?.trim().orEmpty()
        if (started.isEmpty()) return false
        val ended = row.mesProductionEndedAt?.trim().orEmpty()
        return ended.isEmpty()
    }

    private suspend fun persistTimerCheckpoint(planId: Int, session: PlanSession): Boolean {
        if (!locallyOperated.contains(planId) && !canServerPatchPlan(planId)) return false
        val now = System.currentTimeMillis()
        return patchWithConflictHandling(
            planId,
            PatchInspectionBody(
                mesNetProductionSec = (InspectionSessionLogic.readNetProductionMs(session, now) / 1000).toInt(),
                mesPausedAccumSec = (InspectionSessionLogic.readPausedAccumMs(session, now) / 1000).toInt(),
                mesProductionIsPaused = if (InspectionSessionLogic.isTimerPaused(session)) 1 else 0,
            ),
        )
    }

    private fun publishUi() {
        val state = _uiState.value
        val planId = state.activePlanId
        val session = planId?.let { sessions[it] }
        val activeRow = planId?.let { id -> managementRows.find { it.id == id } }
        val product = state.selectedProductCode?.let { code -> state.products.find { it.productCode == code } }
        val rowForDisplay = activeRow?.takeIf { it.productCd == state.selectedProductCode }
        val now = System.currentTimeMillis()

        val inProgress = managementRows.filter { isRowMesActive(it) }
        val completed = managementRows.filter { row ->
            (row.productionCompletedCheck ?: 0) == 1 &&
                row.mesInspectorUserId == userId &&
                rowProductionDay(row) == state.productionDay
        }.sortedWith { a, b -> InspectionManagementRowExt.compareForHistory(a, b) }
        val phase = session?.let { InspectionSessionLogic.timerPhase(it) } ?: TimerPhase.Idle
        val inProgressLocal = session != null && InspectionSessionLogic.isProductionInProgress(session)
        val locked = inProgressLocal && planId?.let { locallyOperated.contains(it) } == true
        val canEdit = locked
        val editableSession = if (canEdit) session else null
        val myActiveRow = findMyActiveProductionRow()
        val showActiveProductionSwitchBanner = myActiveRow?.productCd?.let { activeCode ->
            val activeId = myActiveRow.id
            activeCode != state.selectedProductCode ||
                (activeId != null && !locallyOperated.contains(activeId))
        } == true

        val next = state.copy(
            inProgressRows = inProgress,
            completedRows = completed,
            completedQtyTotal = completed.sumOf { r -> r.actualProductionQuantity ?: 0 },
            displayProductCd = rowForDisplay?.productCd ?: product?.productCode ?: "—",
            displayProductName = rowForDisplay?.productName?.trim()?.takeIf { it.isNotEmpty() }
                ?: product?.productName?.trim()?.takeIf { it.isNotEmpty() }
                ?: "—",
            timerPhase = phase,
            timerPhaseLabel = timerPhaseLabel(phase),
            elapsedDisplay = InspectionSessionLogic.formatDurationMs(
                session?.let { s -> InspectionSessionLogic.readNetProductionMs(s, now) } ?: 0,
            ),
            pausedDisplay = InspectionSessionLogic.formatDurationMs(
                session?.let { s -> InspectionSessionLogic.readPausedAccumMs(s, now) } ?: 0,
            ),
            wallStartDisplay = formatWall(session?.wallStart),
            wallEndDisplay = formatWall(session?.wallEnd),
            defectTotal = session?.defects?.values?.sum() ?: 0,
            canStart = state.selectedProductCode != null && run {
                val rowId = activeRow?.id
                if (activeRow != null && rowId != null && isRowMesActive(activeRow) &&
                    !locallyOperated.contains(rowId)
                ) {
                    return@run false
                }
                session == null || !InspectionSessionLogic.isProductionInProgress(session)
            },
            canPause = editableSession?.let { InspectionSessionLogic.isTimerRunning(it) } == true,
            canResume = editableSession?.let { InspectionSessionLogic.isTimerPaused(it) } == true,
            canEnd = editableSession?.let {
                InspectionSessionLogic.isProductionInProgress(it) &&
                    !InspectionSessionLogic.isTimerPaused(it)
            } == true,
            showPlanCard = state.selectedProductCode != null,
            productSelectionLocked = locked,
            canEditDefects = canEdit,
            showSessionRecoveryAlert = activeRow?.let { row ->
                row.id != null && isRowMesActive(row) &&
                    rowMesLockOwner(row) != MesLockOwner.Other &&
                    !locallyOperated.contains(row.id)
            } == true,
            showActiveProductionSwitchBanner = showActiveProductionSwitchBanner,
            activeProductionSwitchLabel = myActiveRow?.let { rowShortLabel(it) }.orEmpty(),
        )
        if (next != state) {
            _uiState.value = next
        }
    }

    private fun startTickLoop() {
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                publishTimerTick()
            }
        }
    }

    /** 計測中のみタイマー表示を更新（毎秒の全体再描画を避ける） */
    private fun publishTimerTick() {
        val state = _uiState.value
        val planId = state.activePlanId ?: return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isProductionInProgress(session)) return
        val now = System.currentTimeMillis()
        val phase = InspectionSessionLogic.timerPhase(session)
        val elapsed = InspectionSessionLogic.formatDurationMs(
            InspectionSessionLogic.readNetProductionMs(session, now),
        )
        val paused = InspectionSessionLogic.formatDurationMs(
            InspectionSessionLogic.readPausedAccumMs(session, now),
        )
        val phaseLabel = timerPhaseLabel(phase)
        if (elapsed == state.elapsedDisplay &&
            paused == state.pausedDisplay &&
            phase == state.timerPhase
        ) {
            return
        }
        _uiState.update {
            it.copy(
                elapsedDisplay = elapsed,
                pausedDisplay = paused,
                timerPhase = phase,
                timerPhaseLabel = phaseLabel,
            )
        }
    }

    private fun startSyncLoop() {
        syncJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                syncPlansFromServer()
            }
        }
    }

    private fun rowProductionDay(row: InspectionManagementRowDto): String {
        val stored = row.productionDay?.trim()?.take(10).orEmpty()
        if (stored.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return stored
        return _uiState.value.productionDay
    }

    private fun timerPhaseLabel(phase: TimerPhase): String = when (phase) {
        TimerPhase.Idle -> "未開始"
        TimerPhase.Running -> "計測中"
        TimerPhase.Paused -> "一時停止中"
        TimerPhase.Ended -> "終了済"
    }

    private fun formatWall(ts: Long?): String {
        if (ts == null) return "—"
        val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
        return Instant.ofEpochMilli(ts).atZone(ZoneId.of("Asia/Tokyo")).format(fmt)
    }

    companion object {
        private val PROCESS_ORDER = listOf("KT01", "KT02", "KT04", "KT07", "KT05", "KT09")

        fun shiftDateYmd(ymd: String, deltaDays: Int): String {
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = java.time.LocalDate.parse(ymd, fmt)
            return date.plusDays(deltaDays.toLong()).format(fmt)
        }

        fun productionDayFromMillis(ms: Long): String {
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return Instant.ofEpochMilli(ms).atZone(ZoneId.of("Asia/Tokyo")).toLocalDate().format(fmt)
        }

        /** Web resolveProductCodeFromScan と同一 */
        fun resolveProductCodeFromScan(scanned: String, products: List<ErpProductDto>): String? {
            val key = scanned.trim()
            if (!Regex("^\\d{5}$").matches(key)) return null
            val matches = products.filter { p ->
                val cd = p.productCode.trim()
                cd.isNotEmpty() && (cd.endsWith("${key}1") || cd.endsWith(key))
            }
            if (matches.isEmpty()) return null
            val strict = matches.filter { (it.productCode).trim().endsWith("${key}1") }
            if (strict.size == 1) return strict[0].productCode
            if (strict.size > 1) {
                return strict.minByOrNull { it.productCode.length }?.productCode
            }
            if (matches.size == 1) return matches[0].productCode
            return matches.minByOrNull { it.productCode.length }?.productCode
        }

        fun groupDefectItems(items: List<ProcessDefectItemDto>): List<DefectGroupUi> {
            val grouped = items.groupBy { (it.attributableProcessCd ?: "").trim().ifEmpty { "—" } }
            return grouped.entries
                .sortedBy { (cd, _) ->
                    val idx = PROCESS_ORDER.indexOf(cd.uppercase())
                    if (idx == -1) PROCESS_ORDER.size else idx
                }
                .map { (cd, list) ->
                    DefectGroupUi(
                        processCd = cd,
                        processName = list.firstOrNull()?.attributableProcessName?.trim().orEmpty().ifEmpty { cd },
                        items = list.map { DefectItemUi(id = it.defectCd, label = it.defectName) },
                    )
                }
        }
    }

    class Factory(
        private val repository: InspectionRepository,
        private val offlineStore: InspectionOfflineStore,
        private val networkMonitor: NetworkMonitor,
        private val userId: Int,
        private val inspectorLabel: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InspectionActualViewModel(
                repository,
                offlineStore,
                networkMonitor,
                userId,
                inspectorLabel,
            ) as T
        }
    }
}
