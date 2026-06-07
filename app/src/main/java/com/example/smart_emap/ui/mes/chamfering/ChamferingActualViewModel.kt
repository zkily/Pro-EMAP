package com.example.smart_emap.ui.mes.chamfering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.mes.ChamferingOfflineStore
import com.example.smart_emap.core.mes.ChamferingPlanSession
import com.example.smart_emap.core.mes.ChamferingSessionLogic
import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.core.mes.TimerPhase
import com.example.smart_emap.core.mes.applyTo
import com.example.smart_emap.core.mes.toPersisted
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.core.network.NetworkMonitor
import com.example.smart_emap.data.model.ChamferingManagementRowDto
import com.example.smart_emap.data.model.ChamferingMachineDto
import com.example.smart_emap.data.model.PatchChamferingBody
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.ChamferingPatchException
import com.example.smart_emap.data.repository.ChamferingRepository
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

data class ChamferingMachineUi(
    val id: Int,
    val label: String,
)

data class ChamferingPlanRowUi(
    val planId: Int,
    val dayKey: String,
    val productionSequence: Int?,
    val productName: String,
    val productCd: String,
    val materialName: String,
    val qtyLabel: String,
    val qtyValue: String,
    val managementCode: String,
    val mgmtCodeLabel: String,
    val remarks: String,
    val statusText: String,
    val isConfirmedDisplay: Boolean,
    val isConfirmed: Boolean,
    val canScan: Boolean,
    val hasScan: Boolean,
    val scannedCode: String,
    val canStart: Boolean,
    val canPause: Boolean,
    val canResume: Boolean,
    val canEnd: Boolean,
    val canChangeMachine: Boolean,
    val canEditConfirmed: Boolean,
    val canMoveUp: Boolean,
    val canMoveDown: Boolean,
    val canDragReorder: Boolean,
    val productionInProgress: Boolean,
    val showTimerPauseSide: Boolean,
    val timerDisplayFrozen: Boolean,
    val operatorUserId: Int?,
    val setupTimeMin: Int?,
    val timerPhase: TimerPhase,
    val timerPhaseLabel: String,
    val elapsedDisplay: String,
    val pausedDisplay: String,
    val wallStartDisplay: String,
    val wallEndDisplay: String,
)

data class ChamferingDayGroupUi(
    val dayKey: String,
    val dayLabel: String,
    val isAnchorDay: Boolean,
    val countLabel: String,
    val rows: List<ChamferingPlanRowUi>,
)

data class ChamferingUiState(
    val locale: ChamferLocale = ChamferLocale.Ja,
    val productionDay: String = MesCalendarUtils.jstToday(),
    val machines: List<ChamferingMachineUi> = emptyList(),
    val selectedMachineId: Int? = null,
    val hideCompleted: Boolean = true,
    val operators: List<UserListItemDto> = emptyList(),
    val dayGroups: List<ChamferingDayGroupUi> = emptyList(),
    val isLoadingMachines: Boolean = false,
    val isLoadingPlans: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isReorderSaving: Boolean = false,
    val snackbarMessage: String? = null,
    val isNetworkOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val isOfflineMode: Boolean = false,
    val scanDialogVisible: Boolean = false,
    val scanTargetPlanId: Int? = null,
    val scanTargetLabel: String = "",
    val endDialogVisible: Boolean = false,
    val endDialogPlanId: Int? = null,
    val endDialogProductLabel: String = "",
    val endDialogQty: String = "",
    val endDialogBaseline: Int = 0,
    val endDialogDeferDay: String = "",
    val endDialogSubmitting: Boolean = false,
    val endDialogMetaIncomplete: Boolean = false,
    val endDialogOperatorLabel: String = "",
    val endDialogHasOperator: Boolean = false,
    val endDialogSetupTimeMin: Int? = null,
    val endDialogHasSetupTime: Boolean = false,
    val endDialogSubsequentDeferCount: Int = 0,
    val endDialogDeferSubsequent: Boolean = false,
    val changeMachineVisible: Boolean = false,
    val changeMachinePlanId: Int? = null,
    val changeMachineCurrentName: String = "",
    val changeMachineTargetId: Int? = null,
    val changeMachineSubmitting: Boolean = false,
    val confirmedEditVisible: Boolean = false,
    val confirmedEditPlanId: Int? = null,
    val confirmedEditProductLabel: String = "",
    val confirmedEditQty: String = "",
    val confirmedEditOperatorUserId: Int? = null,
    val confirmedEditSetupTimeMin: Int? = null,
    val confirmedEditWallStartMs: Long? = null,
    val confirmedEditWallEndMs: Long? = null,
    val confirmedEditPausedSec: String = "",
    val confirmedEditSubmitting: Boolean = false,
    val deferQtyDialogVisible: Boolean = false,
    val deferQtyDialogQty: String = "",
)

class ChamferingActualViewModel(
    private val repository: ChamferingRepository,
    private val offlineStore: ChamferingOfflineStore,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val sessions = mutableMapOf<Int, ChamferingPlanSession>()
    private var managementRows = listOf<ChamferingManagementRowDto>()
    private var machinesRaw = listOf<ChamferingMachineDto>()
    private var tickJob: Job? = null
    private var flushInFlight = false
    private val mesMetaPatchJobs = mutableMapOf<Int, Job>()
    private val localMesEchoUntil = mutableMapOf<Int, Long>()
    private var confirmedEditSnapshot: ChamferingConfirmedEditSnapshot? = null

    private val _uiState = MutableStateFlow(ChamferingUiState())
    val uiState: StateFlow<ChamferingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hydrateFiltersFromStore()
            loadMachines()
            loadOperators()
            startTickLoop()
            if (_uiState.value.selectedMachineId != null) loadPlans(showLoading = true)
            if (networkMonitor.currentOnline()) flushOfflineQueue(reloadAfter = false)
        }
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isNetworkOnline = online, isOfflineMode = !online || it.pendingSyncCount > 0) }
                if (online) flushOfflineQueue(reloadAfter = true)
            }
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }

    fun refreshAll() {
        viewModelScope.launch {
            loadMachines()
            loadOperators()
            loadPlans(showLoading = true)
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun setLocale(locale: ChamferLocale) = _uiState.update { it.copy(locale = locale) }

    fun shiftProductionDay(delta: Int) {
        val day = _uiState.value.productionDay
        if (!day.matches(DAY_REGEX)) return
        viewModelScope.launch {
            flushPersist(scopeDay = day, scopeMachineId = _uiState.value.selectedMachineId)
            val next = MesCalendarUtils.shiftDateYmd(day, delta)
            _uiState.update { it.copy(productionDay = next) }
            offlineStore.saveFilter(next, _uiState.value.selectedMachineId, _uiState.value.hideCompleted)
            clearRowsAndSessions()
            if (_uiState.value.selectedMachineId != null) loadPlans(showLoading = true)
        }
    }

    fun setProductionDayToday() {
        viewModelScope.launch {
            flushPersist()
            val today = MesCalendarUtils.jstToday()
            _uiState.update { it.copy(productionDay = today) }
            offlineStore.saveFilter(today, _uiState.value.selectedMachineId, _uiState.value.hideCompleted)
            clearRowsAndSessions()
            if (_uiState.value.selectedMachineId != null) loadPlans(showLoading = true)
        }
    }

    fun setProductionDay(day: String) {
        if (!day.matches(DAY_REGEX)) {
            _uiState.update { it.copy(snackbarMessage = chamferStringsFor(it.locale).invalidProductionDay) }
            return
        }
        viewModelScope.launch {
            flushPersist()
            _uiState.update { it.copy(productionDay = day) }
            offlineStore.saveFilter(day, _uiState.value.selectedMachineId, _uiState.value.hideCompleted)
            clearRowsAndSessions()
            if (_uiState.value.selectedMachineId != null) loadPlans(showLoading = true)
        }
    }

    fun setSelectedMachine(machineId: Int?) {
        viewModelScope.launch {
            flushPersist()
            _uiState.update { it.copy(selectedMachineId = machineId) }
            offlineStore.saveFilter(_uiState.value.productionDay, machineId, _uiState.value.hideCompleted)
            clearRowsAndSessions()
            if (machineId != null) loadPlans(showLoading = true)
        }
    }

    fun setHideCompleted(hide: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(hideCompleted = hide) }
            offlineStore.saveFilter(_uiState.value.productionDay, _uiState.value.selectedMachineId, hide)
            publishUi()
            schedulePersist()
        }
    }

    fun loadPlans() = viewModelScope.launch { loadPlans(showLoading = true) }

    fun setOperator(planId: Int, userId: Int?) {
        ensureSession(planId).operatorUserId = userId
        publishUi()
        schedulePersist()
        scheduleMesMetaPatch(planId, MesMetaField.OPERATOR)
    }

    fun setSetupTime(planId: Int, value: Int?) {
        ensureSession(planId).setupTimeMin = value?.coerceAtLeast(0)
        publishUi()
        schedulePersist()
        scheduleMesMetaPatch(planId, MesMetaField.SETUP)
    }



    fun onStart(planId: Int) = viewModelScope.launch {
        val s = chamferStringsFor(_uiState.value.locale)
        val sess = ensureSession(planId)
        if (sess.wallEnd != null) return@launch
        val now = System.currentTimeMillis()
        if (sess.wallStart == null) {
            findOtherActiveRow(planId)?.let { other ->
                _uiState.update {
                    it.copy(snackbarMessage = s.format(s.singleMachineProductionOnly, "label" to rowShortLabel(other)))
                }
                return@launch
            }
            markLocalEcho(planId)
            val ok = patchWithOffline(planId, PatchChamferingBody(
                mesProductionStartedAt = Instant.ofEpochMilli(now).toString(),
                mesProductionIsPaused = 0,
            ), silent = false)
            if (!ok && _uiState.value.isNetworkOnline) return@launch
            sess.wallStart = now
            sess.activeAccumMs = 0
            sess.pausedAccumMs = 0
            sess.pauseSliceStart = null
            sess.runningSliceStart = now
            publishUi()
            schedulePersist()
            return@launch
        }
        if (sess.runningSliceStart == null) {
            sess.runningSliceStart = now
            markLocalEcho(planId)
            persistTimerCheckpoint(planId)
            publishUi()
            schedulePersist()
        }
    }

    fun onPause(planId: Int) = viewModelScope.launch {
        val sess = ensureSession(planId)
        if (sess.wallEnd != null || sess.runningSliceStart == null) return@launch
        val now = System.currentTimeMillis()
        ChamferingSessionLogic.flushRunningSlice(sess, now)
        sess.pauseSliceStart = now
        markLocalEcho(planId)
        persistTimerCheckpoint(planId)
        publishUi()
        schedulePersist()
    }

    fun onResume(planId: Int) = viewModelScope.launch {
        val sess = ensureSession(planId)
        if (sess.wallEnd != null || sess.wallStart == null || sess.runningSliceStart != null) return@launch
        val now = System.currentTimeMillis()
        ChamferingSessionLogic.flushPauseSlice(sess, now)
        sess.runningSliceStart = now
        markLocalEcho(planId)
        persistTimerCheckpoint(planId)
        publishUi()
        schedulePersist()
    }

    fun openEndDialog(planId: Int) {
        val row = managementRows.find { it.id == planId } ?: return
        val sess = ensureSession(planId)
        if (sess.wallStart == null || sess.wallEnd != null) return
        if (!ChamferingSessionLogic.isTimerRunning(sess)) return
        val baseline = splitBaseline(row)
        val daySrc = MesCalendarUtils.normalizeProductionDayKey(row.productionDay ?: _uiState.value.productionDay)
        val deferDay = if (daySrc.matches(DAY_REGEX)) MesCalendarUtils.nextWeekdayYmdJST(daySrc)
        else MesCalendarUtils.nextWeekdayYmdJST(_uiState.value.productionDay)
        val metaIncomplete = !hasOperator(sess) || sess.setupTimeMin == null
        val operatorLabel = operatorLabelFor(planId)
        val subsequentCount = countSubsequentDeferRows(row)
        _uiState.update {
            it.copy(
                endDialogVisible = true,
                endDialogPlanId = planId,
                endDialogProductLabel = "${row.productCd.orEmpty()} · ${row.productName.orEmpty()}",
                endDialogQty = if (baseline > 0) baseline.toString() else row.actualProductionQuantity?.toString().orEmpty(),
                endDialogBaseline = baseline,
                endDialogDeferDay = deferDay,
                endDialogMetaIncomplete = metaIncomplete,
                endDialogOperatorLabel = operatorLabel,
                endDialogHasOperator = operatorLabel.isNotEmpty(),
                endDialogSetupTimeMin = sess.setupTimeMin,
                endDialogHasSetupTime = sess.setupTimeMin != null,
                endDialogSubsequentDeferCount = subsequentCount,
                endDialogDeferSubsequent = subsequentCount > 0,
                endDialogSubmitting = false,
            )
        }
    }

    fun closeEndDialog() = _uiState.update {
        it.copy(
            endDialogVisible = false,
            endDialogPlanId = null,
            endDialogProductLabel = "",
            endDialogQty = "",
            endDialogBaseline = 0,
            endDialogDeferDay = "",
            endDialogMetaIncomplete = false,
            endDialogOperatorLabel = "",
            endDialogHasOperator = false,
            endDialogSetupTimeMin = null,
            endDialogHasSetupTime = false,
            endDialogSubsequentDeferCount = 0,
            endDialogDeferSubsequent = false,
            endDialogSubmitting = false,
            deferQtyDialogVisible = false,
            deferQtyDialogQty = "",
        )
    }

    fun onEndDialogQtyChange(qty: String) = _uiState.update { it.copy(endDialogQty = qty) }

    fun onEndDialogDeferDayChange(day: String) = _uiState.update { it.copy(endDialogDeferDay = day) }

    fun onEndDialogDeferSubsequentChange(checked: Boolean) =
        _uiState.update { it.copy(endDialogDeferSubsequent = checked) }

    fun submitProductionEndFull() = viewModelScope.launch { submitEndFull() }

    fun submitProductionEndWithInput() = viewModelScope.launch { submitEndZeroOrInput() }

    fun requestProductionEndDefer() {
        val s = chamferStringsFor(_uiState.value.locale)
        val baseline = _uiState.value.endDialogBaseline
        if (baseline <= 0) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return
        }
        val nextDay = _uiState.value.endDialogDeferDay.trim().take(10)
        if (!nextDay.matches(DAY_REGEX)) {
            _uiState.update { it.copy(snackbarMessage = s.deferNextDayRequired) }
            return
        }
        _uiState.update {
            it.copy(
                deferQtyDialogVisible = true,
                deferQtyDialogQty = it.endDialogQty.ifBlank { baseline.toString() },
            )
        }
    }

    fun closeDeferQtyDialog() = _uiState.update {
        it.copy(deferQtyDialogVisible = false, deferQtyDialogQty = "")
    }

    fun onDeferQtyDialogQtyChange(qty: String) = _uiState.update { it.copy(deferQtyDialogQty = qty) }

    fun submitProductionEndDefer() = viewModelScope.launch {
        val s = chamferStringsFor(_uiState.value.locale)
        if (!_uiState.value.isNetworkOnline) {
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForEnd, deferQtyDialogVisible = false) }
            return@launch
        }
        val planId = _uiState.value.endDialogPlanId ?: return@launch
        val row = managementRows.find { it.id == planId } ?: return@launch
        val baseline = _uiState.value.endDialogBaseline
        val qty = parseQty(_uiState.value.deferQtyDialogQty)
        if (qty == null) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return@launch
        }
        if (baseline <= 0 || qty >= baseline) {
            _uiState.update {
                it.copy(snackbarMessage = if (baseline <= 0) s.qtyInvalid else s.deferQtyMustBeLess)
            }
            return@launch
        }
        val nextDay = _uiState.value.endDialogDeferDay.trim().take(10)
        if (!nextDay.matches(DAY_REGEX)) {
            _uiState.update { it.copy(snackbarMessage = s.deferNextDayRequired) }
            return@launch
        }
        val subsequent = if (_uiState.value.endDialogDeferSubsequent) subsequentDeferRows(row) else emptyList()
        val subsequentCount = subsequent.size
        _uiState.update { it.copy(endDialogSubmitting = true, deferQtyDialogVisible = false, deferQtyDialogQty = "") }
        try {
            if (subsequent.isNotEmpty()) {
                moveSubsequentRowsProductionDay(subsequent, nextDay)
            }
            markLocalEcho(planId)
            finalizeTimer(planId)
            val mesPatch = PatchChamferingBody(
                defectQty = computedDefect(row, qty),
                mesProductionEndedAt = ensureSession(planId).wallEnd?.let { Instant.ofEpochMilli(it).toString() },
                mesNetProductionSec = ChamferingSessionLogic.netProductionSeconds(ensureSession(planId)),
                mesPausedAccumSec = ChamferingSessionLogic.pausedAccumSeconds(ensureSession(planId)),
                mesSetupTimeMin = ensureSession(planId).setupTimeMin,
                mesOperatorUserId = ensureSession(planId).operatorUserId,
            )
            val ok = patchWithOffline(planId, mesPatch, silent = false)
            if (!ok) return@launch
            repository.splitToNextDay(planId, qty, nextDay)
            repository.patchPlan(planId, PatchChamferingBody(productionCompletedCheck = true))
            reorderAfterProductionDefer(row, nextDay, subsequent, baseline - qty)
            val snackMessage = if (subsequentCount > 0) {
                s.format(s.deferSavedWithFollowing, "n" to (subsequentCount + 1), "date" to nextDay)
            } else {
                s.format(s.deferSavedWithDate, "date" to nextDay)
            }
            _uiState.update { it.copy(snackbarMessage = snackMessage) }
            closeEndDialog()
            loadPlans(showLoading = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        } finally {
            _uiState.update { it.copy(endDialogSubmitting = false) }
        }
    }

    fun openScanDialog(planId: Int) {
        val row = managementRows.find { it.id == planId } ?: return
        if (!canScan(row)) return
        _uiState.update {
            it.copy(
                scanDialogVisible = true,
                scanTargetPlanId = planId,
                scanTargetLabel = "${row.productName.orEmpty()} (${row.productCd.orEmpty()})",
            )
        }
    }

    fun closeScanDialog() = _uiState.update { it.copy(scanDialogVisible = false, scanTargetPlanId = null) }

    fun onBarcodeScanned(code: String) = viewModelScope.launch {
        val planId = _uiState.value.scanTargetPlanId ?: return@launch
        val trimmed = code.trim().take(512)
        if (trimmed.isEmpty()) return@launch
        val s = chamferStringsFor(_uiState.value.locale)
        val row = managementRows.find { it.id == planId }
        try {
            patchWithOffline(planId, PatchChamferingBody(mesScannedCode = trimmed), silent = false)
            managementRows = managementRows.map { r ->
                if (r.id == planId) r.copy(mesScannedCode = trimmed) else r
            }
            val matched = scanMatches(trimmed, row?.managementCode)
            _uiState.update {
                it.copy(
                    scanDialogVisible = false,
                    scanTargetPlanId = null,
                    snackbarMessage = if (matched) s.scanRegistered else s.scanSaved,
                )
            }
            publishUi()
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        }
    }

    fun movePlanUp(dayKey: String, planId: Int) = reorderInDay(dayKey, planId, -1)

    fun movePlanDown(dayKey: String, planId: Int) = reorderInDay(dayKey, planId, 1)

    fun commitDayReorder(dayKey: String, orderedPlanIds: List<Int>) {
        val previousOrder = visibleRowsForGroups().find { it.dayKey == dayKey }?.rows.orEmpty().map { it.planId }
        if (orderedPlanIds.isEmpty() || orderedPlanIds == previousOrder) return
        applyLocalProductionSequence(orderedPlanIds)
        publishUi()
        viewModelScope.launch { persistDayReorder(dayKey, orderedPlanIds, previousOrder) }
    }

    private suspend fun persistDayReorder(dayKey: String, orderedPlanIds: List<Int>, previousOrder: List<Int>) {
        if (_uiState.value.isReorderSaving) return
        val s = chamferStringsFor(_uiState.value.locale)
        if (!_uiState.value.isNetworkOnline) {
            applyLocalProductionSequence(previousOrder)
            publishUi()
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForEdit) }
            return
        }
        val machine = machineNameById(_uiState.value.selectedMachineId) ?: return
        _uiState.update { it.copy(isReorderSaving = true) }
        try {
            repository.reorderPlans(machine, dayKey, orderedPlanIds)
            _uiState.update { it.copy(snackbarMessage = s.reorderSaved) }
            publishUi()
        } catch (e: Exception) {
            applyLocalProductionSequence(previousOrder)
            publishUi()
            _uiState.update { it.copy(snackbarMessage = s.reorderFailed) }
            loadPlans(showLoading = false)
        } finally {
            _uiState.update { it.copy(isReorderSaving = false) }
        }
    }

    private fun applyLocalProductionSequence(orderedIds: List<Int>) {
        orderedIds.forEachIndexed { idx, id ->
            val seq = idx + 1
            managementRows = managementRows.map { row ->
                if (row.id == id) row.copy(productionSequence = seq) else row
            }
        }
    }

    fun openChangeMachine(planId: Int) {
        val row = managementRows.find { it.id == planId } ?: return
        if (!canChangeMachine(row)) return
        val currentName = row.chamferingMachine.orEmpty().trim()
        val matched = machinesRaw.find { (it.machineName ?: "").trim() == currentName }
        _uiState.update {
            it.copy(
                changeMachineVisible = true,
                changeMachinePlanId = planId,
                changeMachineCurrentName = currentName.ifEmpty { "—" },
                changeMachineTargetId = matched?.id ?: _uiState.value.selectedMachineId,
            )
        }
    }

    fun closeChangeMachine() = _uiState.update {
        it.copy(changeMachineVisible = false, changeMachinePlanId = null, changeMachineSubmitting = false)
    }

    fun setChangeMachineTarget(id: Int?) = _uiState.update { it.copy(changeMachineTargetId = id) }

    fun submitChangeMachine() = viewModelScope.launch {
        val s = chamferStringsFor(_uiState.value.locale)
        if (!_uiState.value.isNetworkOnline) {
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForEnd) }
            return@launch
        }
        val planId = _uiState.value.changeMachinePlanId ?: return@launch
        val row = managementRows.find { it.id == planId } ?: return@launch
        val targetName = machineNameById(_uiState.value.changeMachineTargetId)
        if (targetName.isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = s.emptyMachineName) }
            return@launch
        }
        val sourceName = row.chamferingMachine.orEmpty().trim().ifEmpty { machineNameById(_uiState.value.selectedMachineId).orEmpty() }
        if (sourceName.isBlank() || sourceName == targetName) return@launch
        _uiState.update { it.copy(changeMachineSubmitting = true) }
        try {
            patchWithOffline(planId, PatchChamferingBody(chamferingMachine = targetName), silent = false)
            _uiState.update { it.copy(snackbarMessage = s.format(s.changeMachineSaved, "machine" to targetName)) }
            closeChangeMachine()
            loadPlans(showLoading = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        } finally {
            _uiState.update { it.copy(changeMachineSubmitting = false) }
        }
    }

    fun openConfirmedEdit(planId: Int) {
        val row = managementRows.find { it.id == planId } ?: return
        if (!isRowConfirmed(row)) return
        val sess = ensureSession(planId)
        val wallStartMs = MesDateTime.parseToMillis(row.mesProductionStartedAt) ?: sess.wallStart
        val wallEndMs = MesDateTime.parseToMillis(row.mesProductionEndedAt) ?: sess.wallEnd
        confirmedEditSnapshot = ChamferingConfirmedEditSnapshot(
            operatorUserId = sess.operatorUserId,
            setupTimeMin = sess.setupTimeMin,
            wallStart = wallStartMs,
            wallEnd = wallEndMs,
            pausedAccumMs = sess.pausedAccumMs,
            activeAccumMs = sess.activeAccumMs,
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
                confirmedEditOperatorUserId = sess.operatorUserId,
                confirmedEditSetupTimeMin = sess.setupTimeMin,
                confirmedEditWallStartMs = wallStartMs,
                confirmedEditWallEndMs = wallEndMs,
                confirmedEditPausedSec = (
                    row.mesPausedAccumSec ?: ChamferingSessionLogic.pausedAccumSeconds(sess)
                ).coerceAtLeast(0).toString(),
                confirmedEditSubmitting = false,
            )
        }
    }

    fun closeConfirmedEdit() {
        val planId = _uiState.value.confirmedEditPlanId
        val snapshot = confirmedEditSnapshot
        if (planId != null && snapshot != null) {
            restoreConfirmedEditSession(planId, snapshot, sessions)
        }
        confirmedEditSnapshot = null
        _uiState.update { it.copy(confirmedEditVisible = false).withClearedConfirmedEditFields() }
    }

    fun onConfirmedEditQtyChange(value: String) {
        _uiState.update { it.copy(confirmedEditQty = value.filter { ch -> ch.isDigit() }) }
    }

    fun onConfirmedEditOperatorChange(userId: Int?) {
        _uiState.update { it.copy(confirmedEditOperatorUserId = userId) }
    }

    fun onConfirmedEditSetupChange(value: String) {
        val trimmed = value.filter { it.isDigit() }
        _uiState.update {
            it.copy(confirmedEditSetupTimeMin = if (trimmed.isEmpty()) null else trimmed.toIntOrNull()?.coerceAtLeast(0))
        }
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

    fun submitConfirmedEdit() = viewModelScope.launch {
        val state = _uiState.value
        val planId = state.confirmedEditPlanId ?: return@launch
        val row = managementRows.find { it.id == planId } ?: return@launch
        val s = chamferStringsFor(state.locale)
        if (!state.isNetworkOnline) {
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForConfirmedEdit) }
            return@launch
        }
        val qty = state.confirmedEditQty.toIntOrNull()
        if (qty == null || qty < 0) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return@launch
        }
        val ws = state.confirmedEditWallStartMs
        val we = state.confirmedEditWallEndMs
        if (ws == null || we == null) {
            _uiState.update { it.copy(snackbarMessage = s.editTimeRequired) }
            return@launch
        }
        if (we < ws) {
            _uiState.update { it.copy(snackbarMessage = s.editTimeOrder) }
            return@launch
        }
        val wallSec = ((we - ws) / 1000).coerceAtLeast(0)
        val pauseSec = state.confirmedEditPausedSec.toLongOrNull()?.coerceAtLeast(0) ?: 0L
        if (pauseSec > wallSec) {
            _uiState.update { it.copy(snackbarMessage = s.editPauseTooLong) }
            return@launch
        }
        val netSec = (wallSec - pauseSec).coerceAtLeast(0)
        _uiState.update { it.copy(confirmedEditSubmitting = true) }
        try {
            val sess = ensureSession(planId)
            sess.operatorUserId = state.confirmedEditOperatorUserId
            sess.setupTimeMin = state.confirmedEditSetupTimeMin
            sess.wallStart = ws
            sess.wallEnd = we
            sess.pausedAccumMs = pauseSec * 1000
            sess.activeAccumMs = netSec * 1000
            sess.runningSliceStart = null
            sess.pauseSliceStart = null

            var body = PatchChamferingBody(
                mesProductionStartedAt = Instant.ofEpochMilli(ws).toString(),
                mesProductionEndedAt = Instant.ofEpochMilli(we).toString(),
                mesPausedAccumSec = pauseSec.toInt(),
                mesNetProductionSec = netSec.toInt(),
                mesProductionIsPaused = 0,
                mesSetupTimeMin = state.confirmedEditSetupTimeMin,
                mesOperatorUserId = state.confirmedEditOperatorUserId?.takeIf { it > 0 },
            )
            val oldActual = row.actualProductionQuantity ?: 0
            if (qty != oldActual) {
                val oldDefect = row.defectQty ?: 0
                body = body.copy(
                    actualProductionQuantity = qty,
                    defectQty = (oldDefect + (qty - oldActual)).coerceAtLeast(0),
                )
            }
            markLocalEcho(planId)
            val ok = patchWithOffline(planId, body, silent = false)
            if (!ok) return@launch
            flushPersist()
            confirmedEditSnapshot = null
            _uiState.update {
                it.copy(
                    snackbarMessage = s.confirmedEditSaved,
                    confirmedEditVisible = false,
                ).withClearedConfirmedEditFields()
            }
            loadPlans(showLoading = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        } finally {
            _uiState.update { it.copy(confirmedEditSubmitting = false) }
        }
    }

    private suspend fun submitEndFull() {
        val s = chamferStringsFor(_uiState.value.locale)
        if (!_uiState.value.isNetworkOnline) {
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForEnd) }
            return
        }
        val planId = _uiState.value.endDialogPlanId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        val baseline = splitBaseline(row)
        if (baseline <= 0) {
            submitEndZeroOrInput()
            return
        }
        _uiState.update { it.copy(endDialogSubmitting = true) }
        try {
            markLocalEcho(planId)
            finalizeTimer(planId)
            val body = buildEndPatch(row, planId, baseline, complete = true)
            val ok = patchWithOffline(planId, body, silent = false)
            if (!ok) return
            flushPersist()
            _uiState.update { it.copy(snackbarMessage = s.completeSaved) }
            closeEndDialog()
            loadPlans(showLoading = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        } finally {
            _uiState.update { it.copy(endDialogSubmitting = false) }
        }
    }

    private suspend fun submitEndZeroOrInput() {
        val s = chamferStringsFor(_uiState.value.locale)
        if (!_uiState.value.isNetworkOnline) {
            _uiState.update { it.copy(snackbarMessage = s.needOnlineForEnd) }
            return
        }
        val planId = _uiState.value.endDialogPlanId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        val qty = parseQty(_uiState.value.endDialogQty)
        if (qty == null) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return
        }
        _uiState.update { it.copy(endDialogSubmitting = true) }
        try {
            markLocalEcho(planId)
            finalizeTimer(planId)
            val body = buildEndPatch(row, planId, qty, complete = true)
            val ok = patchWithOffline(planId, body, silent = false)
            if (!ok) return
            flushPersist()
            _uiState.update { it.copy(snackbarMessage = s.completeSaved) }
            closeEndDialog()
            loadPlans(showLoading = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, s.saveFailed)) }
        } finally {
            _uiState.update { it.copy(endDialogSubmitting = false) }
        }
    }

    private fun buildEndPatch(row: ChamferingManagementRowDto, planId: Int, qty: Int, complete: Boolean): PatchChamferingBody {
        val sess = ensureSession(planId)
        val operatorId = sess.operatorUserId
        return PatchChamferingBody(
            actualProductionQuantity = qty,
            productionCompletedCheck = complete,
            defectQty = computedDefect(row, qty),
            mesProductionEndedAt = sess.wallEnd?.let { Instant.ofEpochMilli(it).toString() },
            mesNetProductionSec = ChamferingSessionLogic.netProductionSeconds(sess),
            mesPausedAccumSec = ChamferingSessionLogic.pausedAccumSeconds(sess),
            mesProductionIsPaused = 0,
            mesSetupTimeMin = sess.setupTimeMin,
            mesOperatorUserId = if (operatorId != null && operatorId > 0) operatorId else null,
        )
    }

    private fun reorderInDay(dayKey: String, planId: Int, delta: Int) = viewModelScope.launch {
        val s = chamferStringsFor(_uiState.value.locale)
        val machine = machineNameById(_uiState.value.selectedMachineId) ?: return@launch
        val group = visibleRowsForGroups().find { it.dayKey == dayKey }?.rows.orEmpty()
        val idx = group.indexOfFirst { it.planId == planId }
        if (idx < 0) return@launch
        val newIdx = idx + delta
        if (newIdx !in group.indices) return@launch
        val reordered = group.toMutableList()
        val item = reordered.removeAt(idx)
        reordered.add(newIdx, item)
        val orderedIds = reordered.map { it.planId }
        try {
            repository.reorderPlans(machine, dayKey, orderedIds)
            managementRows = managementRows.map { row ->
                val pos = orderedIds.indexOf(row.id ?: -1)
                if (pos >= 0) row.copy(productionSequence = pos + 1) else row
            }
            _uiState.update { it.copy(snackbarMessage = s.reorderSaved) }
            publishUi()
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = s.reorderFailed) }
            loadPlans(showLoading = false)
        }
    }

    private suspend fun loadMachines() {
        val s = chamferStringsFor(_uiState.value.locale)
        _uiState.update { it.copy(isLoadingMachines = true) }
        try {
            machinesRaw = repository.loadMachines()
            _uiState.update {
                it.copy(
                    machines = machinesRaw.mapNotNull { m ->
                        val id = m.id ?: return@mapNotNull null
                        ChamferingMachineUi(id, (m.machineName ?: m.machineCd ?: "").trim().ifEmpty { "#$id" })
                    },
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = s.loadMachinesFailed) }
        } finally {
            _uiState.update { it.copy(isLoadingMachines = false) }
        }
    }

    private suspend fun loadOperators() {
        val s = chamferStringsFor(_uiState.value.locale)
        _uiState.update { it.copy(isLoadingUsers = true) }
        try {
            val ops = repository.loadOperators()
            _uiState.update { it.copy(operators = ops) }
        } catch (_: Exception) {
            _uiState.update { it.copy(snackbarMessage = s.loadUsersFailed) }
        } finally {
            _uiState.update { it.copy(isLoadingUsers = false) }
        }
    }

    private suspend fun loadPlans(showLoading: Boolean) {
        val s = chamferStringsFor(_uiState.value.locale)
        val machineId = _uiState.value.selectedMachineId
        if (machineId == null) {
            _uiState.update { it.copy(snackbarMessage = s.emptySelectMachine) }
            return
        }
        val machine = machineNameById(machineId)
        if (machine.isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = s.emptyMachineName) }
            return
        }
        val dayStr = _uiState.value.productionDay.trim()
        if (!dayStr.matches(DAY_REGEX)) {
            _uiState.update { it.copy(snackbarMessage = s.invalidProductionDay) }
            clearRowsAndSessions()
            return
        }
        if (showLoading) _uiState.update { it.copy(isLoadingPlans = true) }
        try {
            val rows = repository.loadPlans(dayStr, machine)
            offlineStore.saveCachedPlans(dayStr, machineId, rows)
            val restored = applyPlansFromRows(rows, dayStr, machineId)
            if (restored) _uiState.update { it.copy(snackbarMessage = s.stateRestored) }
            flushOfflineQueue(reloadAfter = false)
        } catch (e: Exception) {
            val cached = offlineStore.loadCachedPlans(dayStr, machineId)
            when {
                cached != null -> {
                    applyPlansFromRows(cached, dayStr, machineId)
                    _uiState.update { it.copy(snackbarMessage = s.offlineListCached) }
                    flushOfflineQueue(reloadAfter = false)
                }
                managementRows.isNotEmpty() -> {
                    applyPlansFromRows(managementRows, dayStr, machineId)
                    _uiState.update { it.copy(snackbarMessage = s.offlineListCached) }
                }
                else -> {
                    _uiState.update { it.copy(snackbarMessage = formatError(e, s.loadPlansFailed)) }
                    clearRowsAndSessions()
                }
            }
        } finally {
            if (showLoading) _uiState.update { it.copy(isLoadingPlans = false) }
        }
    }

    private suspend fun applyPlansFromRows(
        rows: List<ChamferingManagementRowDto>,
        dayStr: String,
        machineId: Int,
    ): Boolean {
        sessions.clear()
        managementRows = rows
        var restored = false
        val persisted = offlineStore.loadSnapshot(dayStr, machineId)
        for (r in rows) {
            val id = r.id ?: continue
            val sess = ChamferingPlanSession(
                operatorUserId = r.mesOperatorUserId?.takeIf { it > 0 },
                setupTimeMin = ChamferingSessionLogic.mesMinuteFromRow(r.mesSetupTimeMin),
            )
            ChamferingSessionLogic.hydrateFromRow(sess, r)
            if (sess.wallStart != null && sess.wallEnd == null) {
                ChamferingSessionLogic.reconcileInProgressTimer(sess)
            }
            persisted?.sessions?.get(id.toString())?.let { p ->
                p.applyTo(sess)
                restored = true
            }
            sessions[id] = sess
        }
        publishUi()
        schedulePersist()
        return restored
    }

    private suspend fun patchWithOffline(planId: Int, body: PatchChamferingBody, silent: Boolean): Boolean {
        val s = chamferStringsFor(_uiState.value.locale)
        val scopeKey = offlineStore.makeScopeKey(_uiState.value.productionDay, _uiState.value.selectedMachineId)
        if (!networkMonitor.currentOnline()) {
            offlineStore.enqueuePatch(scopeKey, planId, body)
            refreshPendingSync()
            if (!silent) _uiState.update { it.copy(snackbarMessage = s.offlineQueued) }
            return false
        }
        return try {
            repository.patchPlan(planId, body)
            true
        } catch (e: Exception) {
            if (NetworkErrors.isNetworkFailure(e)) {
                offlineStore.enqueuePatch(scopeKey, planId, body)
                refreshPendingSync()
                if (!silent) _uiState.update { it.copy(snackbarMessage = s.offlineQueued) }
                false
            } else {
                throw e
            }
        }
    }

    private suspend fun persistTimerCheckpoint(planId: Int) {
        val sess = ensureSession(planId)
        val now = System.currentTimeMillis()
        if (sess.runningSliceStart != null) {
            ChamferingSessionLogic.flushRunningSlice(sess, now)
            if (sess.wallEnd == null && sess.pauseSliceStart == null) sess.runningSliceStart = now
        } else if (sess.pauseSliceStart != null) {
            ChamferingSessionLogic.flushPauseSlice(sess, now)
            if (sess.wallEnd == null) sess.pauseSliceStart = now
        }
        patchWithOffline(
            planId,
            PatchChamferingBody(
                mesNetProductionSec = ChamferingSessionLogic.netProductionSeconds(sess),
                mesPausedAccumSec = ChamferingSessionLogic.pausedAccumSeconds(sess),
                mesProductionIsPaused = if (ChamferingSessionLogic.isTimerPaused(sess)) 1 else 0,
            ),
            silent = true,
        )
    }

    private suspend fun flushOfflineQueue(reloadAfter: Boolean) {
        if (!networkMonitor.currentOnline() || flushInFlight) return
        if (offlineStore.pendingCount() == 0) return
        flushInFlight = true
        try {
            val result = offlineStore.flush { id, body -> repository.patchPlan(id, body) }
            refreshPendingSync()
            if (result.syncedPatches > 0) {
                val s = chamferStringsFor(_uiState.value.locale)
                _uiState.update { it.copy(snackbarMessage = s.format(s.offlineSyncOk, "n" to result.syncedPatches)) }
                if (reloadAfter && _uiState.value.selectedMachineId != null) loadPlans(showLoading = false)
            }
        } finally {
            flushInFlight = false
        }
    }

    private suspend fun refreshPendingSync() {
        val count = offlineStore.pendingCount()
        _uiState.update { it.copy(pendingSyncCount = count, isOfflineMode = !it.isNetworkOnline || count > 0) }
    }

    private suspend fun hydrateFiltersFromStore() {
        val today = MesCalendarUtils.jstToday()
        val prefs = offlineStore.loadFilterPrefs()
        _uiState.update {
            it.copy(
                productionDay = today,
                selectedMachineId = prefs.selectedMachineId,
                hideCompleted = prefs.hideCompleted,
            )
        }
        offlineStore.saveFilter(today, prefs.selectedMachineId, prefs.hideCompleted)
    }

    private fun clearRowsAndSessions() {
        managementRows = emptyList()
        sessions.clear()
        publishUi()
    }

    private fun ensureSession(planId: Int): ChamferingPlanSession =
        sessions.getOrPut(planId) { ChamferingSessionLogic.emptySession() }

    private fun finalizeTimer(planId: Int) {
        val sess = ensureSession(planId)
        if (sess.wallEnd != null) return
        val now = System.currentTimeMillis()
        ChamferingSessionLogic.flushRunningSlice(sess, now)
        sess.wallEnd = now
        ChamferingSessionLogic.freezePausedAccumMs(sess, now)
    }

    private fun markLocalEcho(planId: Int) {
        localMesEchoUntil[planId] = System.currentTimeMillis() + 2800
    }

    private fun machineNameById(id: Int?): String? {
        if (id == null) return null
        return machinesRaw.find { it.id == id }?.machineName?.trim()?.ifEmpty { null }
    }

    private fun splitBaseline(row: ChamferingManagementRowDto): Int {
        val n = row.actualProductionQuantity ?: 0
        return n.coerceAtLeast(0)
    }

    private fun computedDefect(row: ChamferingManagementRowDto, confirmedQty: Int): Int =
        confirmedQty - splitBaseline(row)

    private fun hasOperator(sess: ChamferingPlanSession): Boolean {
        val id = sess.operatorUserId
        return id != null && id > 0
    }

    private fun operatorLabelFor(planId: Int): String {
        val uid = ensureSession(planId).operatorUserId ?: return ""
        return _uiState.value.operators.find { it.id == uid }?.displayLabel().orEmpty()
    }

    private fun countSubsequentDeferRows(row: ChamferingManagementRowDto): Int =
        subsequentDeferRows(row).size

    private fun subsequentDeferRows(row: ChamferingManagementRowDto): List<ChamferingManagementRowDto> {
        val id = row.id ?: return emptyList()
        val dayKey = MesCalendarUtils.normalizeProductionDayKey(row.productionDay ?: _uiState.value.productionDay)
        if (dayKey == "—") return emptyList()
        val machine = row.chamferingMachine?.trim().orEmpty()
        if (machine.isEmpty()) return emptyList()
        val currentSeq = row.productionSequence ?: 0
        return managementRows
            .filter { r ->
                r.id != null &&
                    r.id != id &&
                    MesCalendarUtils.normalizeProductionDayKey(r.productionDay) == dayKey &&
                    (r.chamferingMachine ?: "").trim() == machine &&
                    (r.productionSequence ?: 0) > currentSeq &&
                    (r.productionCompletedCheck ?: 0) != 1 &&
                    (r.actualProductionQuantity ?: 0) > 0
            }
            .sortedBy { it.productionSequence ?: 0 }
    }

    private suspend fun moveSubsequentRowsProductionDay(rows: List<ChamferingManagementRowDto>, targetDay: String) {
        for (row in rows) {
            val id = row.id ?: continue
            repository.patchPlan(id, PatchChamferingBody(productionDay = targetDay))
        }
    }

    private suspend fun reorderMachineRowsForDay(machine: String, productionDay: String) {
        if (!productionDay.matches(DAY_REGEX)) return
        val rows = repository.loadPlans(productionDay, machine)
        val ids = rows.sortedBy { it.productionSequence ?: 0 }.mapNotNull { it.id }
        if (ids.isEmpty()) return
        repository.reorderPlans(machine, productionDay, ids)
    }

    private suspend fun reorderTargetDayAfterDefer(
        machine: String,
        targetDay: String,
        currentRow: ChamferingManagementRowDto,
        subsequentIds: List<Int>,
        remainderQty: Int,
    ) {
        val all = repository.loadPlans(targetDay, machine)
        if (all.isEmpty()) return
        val currentId = currentRow.id
        val remainderId = findDeferRemainderRowId(all, currentRow, currentId, remainderQty)
        val subsequentOrdered = subsequentIds.filter { id -> all.any { it.id == id } }
        val subsequentSet = subsequentOrdered.toSet()
        val rest = all
            .filter { r ->
                r.id != null &&
                    r.id != remainderId &&
                    r.id !in subsequentSet
            }
            .sortedBy { it.productionSequence ?: 0 }
            .mapNotNull { it.id }
        val orderedIds = buildList {
            remainderId?.let { add(it) }
            addAll(subsequentOrdered)
            addAll(rest)
        }
        val allIds = all.mapNotNull { it.id }
        val missing = allIds.filter { it !in orderedIds.toSet() }
        var finalOrder = if (orderedIds.isEmpty()) {
            all.sortedBy { it.productionSequence ?: 0 }.mapNotNull { it.id }
        } else {
            orderedIds + missing
        }
        if (remainderId == null && finalOrder.isNotEmpty()) {
            val fallbackId = all
                .filter { r ->
                    r.id != null &&
                        r.id != currentId &&
                        (r.productionCompletedCheck ?: 0) == 0
                }
                .maxByOrNull { it.id ?: 0 }
                ?.id
            if (fallbackId != null && finalOrder.first() != fallbackId) {
                finalOrder = listOf(fallbackId) + finalOrder.filter { it != fallbackId }
            }
        }
        if (finalOrder.isEmpty()) return
        repository.reorderPlans(machine, targetDay, finalOrder)
    }

    private fun findDeferRemainderRowId(
        all: List<ChamferingManagementRowDto>,
        currentRow: ChamferingManagementRowDto,
        currentId: Int?,
        remainderQty: Int,
    ): Int? {
        if (remainderQty <= 0) return null
        val mgmt = currentRow.managementCode?.trim().orEmpty()
        fun sameProduct(r: ChamferingManagementRowDto): Boolean {
            if ((r.productCd ?: "").trim() != (currentRow.productCd ?: "").trim()) return false
            if (mgmt.isEmpty()) return true
            return (r.managementCode ?: "").trim() == mgmt
        }
        fun isRemainderCandidate(r: ChamferingManagementRowDto): Boolean =
            r.id != null &&
                r.id != currentId &&
                sameProduct(r) &&
                (r.productionCompletedCheck ?: 0) == 0

        val byQty = all.filter { r ->
            isRemainderCandidate(r) && (r.actualProductionQuantity ?: 0) == remainderQty
        }
        if (byQty.isNotEmpty()) {
            return byQty.maxByOrNull { it.id ?: 0 }?.id
        }
        return all
            .filter(::isRemainderCandidate)
            .maxByOrNull { it.id ?: 0 }
            ?.id
    }

    private suspend fun reorderAfterProductionDefer(
        currentRow: ChamferingManagementRowDto,
        targetDay: String,
        subsequentRows: List<ChamferingManagementRowDto>,
        remainderQty: Int,
    ) {
        val machine = currentRow.chamferingMachine?.trim().orEmpty()
        if (machine.isEmpty()) return
        val sourceDay = MesCalendarUtils.normalizeProductionDayKey(
            currentRow.productionDay ?: _uiState.value.productionDay,
        )
        val subsequentIds = subsequentRows.mapNotNull { it.id }
        if (sourceDay != "—" && sourceDay.matches(DAY_REGEX) && sourceDay != targetDay) {
            reorderMachineRowsForDay(machine, sourceDay)
        }
        if (subsequentIds.isEmpty()) {
            // split API 已在顺延日将剩余行以 production_sequence = 1 插入
            return
        }
        reorderTargetDayAfterDefer(machine, targetDay, currentRow, subsequentIds, remainderQty)
    }

    private fun parseQty(raw: String): Int? {
        val n = raw.trim().toIntOrNull() ?: return null
        return if (n >= 0) n else null
    }

    private fun isRowConfirmed(row: ChamferingManagementRowDto): Boolean =
        (row.productionCompletedCheck ?: 0) == 1

    private fun isRowMesEnded(row: ChamferingManagementRowDto): Boolean =
        !row.mesProductionEndedAt.isNullOrBlank()

    private fun isRowSessionEnded(planId: Int): Boolean =
        sessions[planId]?.wallEnd != null

    private fun isConfirmedForDisplay(row: ChamferingManagementRowDto): Boolean =
        isRowConfirmed(row) || isRowMesEnded(row) || (row.id?.let { isRowSessionEnded(it) } == true)

    private fun isRowActive(row: ChamferingManagementRowDto): Boolean {
        val id = row.id ?: return false
        sessions[id]?.let {
            if (it.wallStart != null && it.wallEnd == null) return true
        }
        return !row.mesProductionStartedAt.isNullOrBlank() && row.mesProductionEndedAt.isNullOrBlank()
    }

    private fun findOtherActiveRow(excludeId: Int): ChamferingManagementRowDto? =
        managementRows.firstOrNull { it.id != null && it.id != excludeId && isRowActive(it) }

    private fun canScan(row: ChamferingManagementRowDto): Boolean =
        !isConfirmedForDisplay(row) && row.id != null

    private fun canChangeMachine(row: ChamferingManagementRowDto): Boolean {
        if (row.id == null || isConfirmedForDisplay(row) || isRowActive(row)) return false
        return true
    }

    private fun scanMatches(scanned: String, mgmt: String?): Boolean {
        val a = scanned.trim()
        val b = mgmt?.trim().orEmpty()
        if (a.isEmpty() || b.isEmpty()) return false
        if (a == b || b.endsWith(a) || a.endsWith(b)) return true
        val tailA = if (a.length <= 5) a else a.takeLast(5)
        val tailB = if (b.length <= 5) b else b.takeLast(5)
        return tailA.length >= 3 && tailA == tailB
    }

    private fun rowShortLabel(row: ChamferingManagementRowDto): String {
        val seq = row.productionSequence?.let { "#$it" }.orEmpty()
        val name = row.productName?.trim().orEmpty().ifEmpty { row.productCd.orEmpty() }
        return listOf(seq, name).filter { it.isNotEmpty() }.joinToString(" ")
    }

    private fun formatMgmtShort(code: String?): String {
        val s = code?.trim().orEmpty()
        if (s.isEmpty()) return ""
        val tail = if (s.length <= 5) s else s.takeLast(5)
        return tail
    }

    private fun formatWall(ts: Long?): String {
        if (ts == null) return "—"
        val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
        return Instant.ofEpochMilli(ts).atZone(ZoneId.of("Asia/Tokyo")).format(fmt)
    }

    private fun timerPhaseLabel(phase: TimerPhase, s: ChamferStrings): String = when (phase) {
        TimerPhase.Idle -> s.timerIdle
        TimerPhase.Running -> s.timerRunning
        TimerPhase.Paused -> s.timerPaused
        TimerPhase.Ended -> s.timerEnded
    }

    private data class RowRef(val planId: Int, val dayKey: String)

    private data class DayGroupInternal(val dayKey: String, val rows: List<RowRef>)

    private fun visibleRowsForGroups(): List<DayGroupInternal> {
        var rows = managementRows.filter { it.id != null }
        if (_uiState.value.hideCompleted) {
            rows = rows.filter { (it.productionCompletedCheck ?: 0) != 1 }
        }
        rows = rows.sortedWith(compareBy({ MesCalendarUtils.normalizeProductionDayKey(it.productionDay) }, { it.productionSequence ?: 0 }))
        val groups = linkedMapOf<String, MutableList<RowRef>>()
        for (r in rows) {
            val k = MesCalendarUtils.normalizeProductionDayKey(r.productionDay)
            groups.getOrPut(k) { mutableListOf() }.add(RowRef(r.id!!, k))
        }
        return groups.map { (k, v) -> DayGroupInternal(k, v) }
    }

    private fun publishUi() {
        val s = chamferStringsFor(_uiState.value.locale)
        val anchor = _uiState.value.productionDay.take(10)
        val groups = visibleRowsForGroups().map { grp ->
            val draggableCount = grp.rows.count { ref ->
                val row = managementRows.find { it.id == ref.planId }
                row != null && !isConfirmedForDisplay(row)
            }
            val rowUis = grp.rows.mapIndexed { index, ref ->
                val row = managementRows.find { it.id == ref.planId } ?: return@mapIndexed null
                val sess = ensureSession(ref.planId)
                val confirmedDisplay = isConfirmedForDisplay(row)
                val phase = ChamferingSessionLogic.timerPhase(sess)
                val canStart = sess.wallEnd == null && sess.wallStart == null && findOtherActiveRow(ref.planId) == null
                val inProgress = ChamferingSessionLogic.isProductionInProgress(sess)
                val mgmtTail = formatMgmtShort(row.managementCode)
                ChamferingPlanRowUi(
                    planId = ref.planId,
                    dayKey = grp.dayKey,
                    productionSequence = row.productionSequence,
                    productName = row.productName.orEmpty().ifEmpty { "—" },
                    productCd = row.productCd.orEmpty(),
                    materialName = row.materialName.orEmpty().trim(),
                    qtyLabel = if (confirmedDisplay) s.actualQty else s.plannedQty,
                    qtyValue = row.actualProductionQuantity?.toString() ?: "—",
                    managementCode = row.managementCode.orEmpty().trim(),
                    mgmtCodeLabel = if (mgmtTail.isEmpty()) "" else s.format(s.mgmtCodeShort, "code" to mgmtTail),
                    remarks = row.remarks.orEmpty().trim(),
                    statusText = if (confirmedDisplay) s.cmConfirmed else s.cmPending,
                    isConfirmedDisplay = confirmedDisplay,
                    isConfirmed = isRowConfirmed(row),
                    canScan = canScan(row),
                    hasScan = !row.mesScannedCode.isNullOrBlank(),
                    scannedCode = row.mesScannedCode.orEmpty(),
                    canStart = canStart,
                    canPause = ChamferingSessionLogic.isTimerRunning(sess),
                    canResume = ChamferingSessionLogic.isTimerPaused(sess),
                    canEnd = ChamferingSessionLogic.isTimerRunning(sess),
                    canChangeMachine = canChangeMachine(row),
                    canEditConfirmed = isRowConfirmed(row),
                    canMoveUp = index > 0,
                    canMoveDown = index < grp.rows.lastIndex,
                    canDragReorder = !confirmedDisplay && grp.dayKey != "—" && draggableCount > 1,
                    productionInProgress = inProgress,
                    showTimerPauseSide = sess.wallStart != null,
                    timerDisplayFrozen = phase == TimerPhase.Paused,
                    operatorUserId = sess.operatorUserId,
                    setupTimeMin = sess.setupTimeMin,
                    timerPhase = phase,
                    timerPhaseLabel = timerPhaseLabel(phase, s),
                    elapsedDisplay = ChamferingSessionLogic.formatDurationMs(ChamferingSessionLogic.operationDisplayMs(sess)),
                    pausedDisplay = ChamferingSessionLogic.formatDurationMs(ChamferingSessionLogic.readPausedAccumMs(sess)),
                    wallStartDisplay = formatWall(sess.wallStart),
                    wallEndDisplay = formatWall(sess.wallEnd),
                )
            }.filterNotNull()
            ChamferingDayGroupUi(
                dayKey = grp.dayKey,
                dayLabel = if (grp.dayKey == "—") {
                    "—"
                } else {
                    MesCalendarUtils.formatDateWithWeekday(
                        grp.dayKey,
                        when (_uiState.value.locale) {
                            ChamferLocale.En -> "en"
                            ChamferLocale.Zh -> "zh"
                            ChamferLocale.Vi -> "vi"
                            ChamferLocale.Ja -> "ja"
                        },
                    )
                },
                isAnchorDay = grp.dayKey == anchor,
                countLabel = "${rowUis.size} ${s.planLinesSuffix}",
                rows = rowUis,
            )
        }
        _uiState.update { it.copy(dayGroups = groups) }
    }

    private fun schedulePersist() = viewModelScope.launch {
        delay(350)
        flushPersist()
    }

    private enum class MesMetaField { OPERATOR, SETUP }

    private fun scheduleMesMetaPatch(planId: Int, field: MesMetaField) {
        mesMetaPatchJobs[planId]?.cancel()
        mesMetaPatchJobs[planId] = viewModelScope.launch {
            delay(350)
            persistMesMetaFields(planId, field)
        }
    }

    private suspend fun persistMesMetaFields(planId: Int, field: MesMetaField) {
        val sess = sessions[planId] ?: return
        val row = managementRows.find { it.id == planId } ?: return
        if (isConfirmedForDisplay(row)) return
        markLocalEcho(planId)
        val body = when (field) {
            MesMetaField.OPERATOR -> PatchChamferingBody(
                mesOperatorUserId = sess.operatorUserId?.takeIf { it > 0 } ?: 0,
            )
            MesMetaField.SETUP -> PatchChamferingBody(
                mesSetupTimeMin = sess.setupTimeMin?.coerceAtLeast(0) ?: -1,
            )
        }
        try {
            patchWithOffline(planId, body, silent = true)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = formatError(e, chamferStringsFor(it.locale).saveFailed)) }
            return
        }
        managementRows = managementRows.map { r ->
            if (r.id != planId) {
                r
            } else {
                r.copy(
                    mesSetupTimeMin = sess.setupTimeMin,
                    mesOperatorUserId = sess.operatorUserId,
                )
            }
        }
    }

    private suspend fun flushPersist(
        scopeDay: String = _uiState.value.productionDay,
        scopeMachineId: Int? = _uiState.value.selectedMachineId,
    ) {
        if (scopeMachineId == null || !scopeDay.matches(DAY_REGEX)) return
        val map = sessions.mapKeys { it.key.toString() }.mapValues { it.value.toPersisted() }
        offlineStore.saveSnapshot(
            com.example.smart_emap.core.mes.ChamferingPagePersistSnapshot(
                productionDay = scopeDay,
                selectedMachineId = scopeMachineId,
                hideCompleted = _uiState.value.hideCompleted,
                sessions = map,
            ),
        )
    }

    private fun formatError(e: Exception, fallback: String): String {
        val s = chamferStringsFor(_uiState.value.locale)
        if (e is ChamferingPatchException) return e.message ?: fallback
        return NetworkErrors.formatError(e, fallback, s.networkErrorHints())
    }

    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (sessions.values.any { ChamferingSessionLogic.isProductionInProgress(it) }) {
                    publishUi()
                }
            }
        }
    }

    companion object {
        private val DAY_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    }
}

private data class ChamferingConfirmedEditSnapshot(
    val operatorUserId: Int?,
    val setupTimeMin: Int?,
    val wallStart: Long?,
    val wallEnd: Long?,
    val pausedAccumMs: Long,
    val activeAccumMs: Long,
)

private fun ChamferingUiState.withClearedConfirmedEditFields(): ChamferingUiState = copy(
    confirmedEditPlanId = null,
    confirmedEditProductLabel = "",
    confirmedEditQty = "",
    confirmedEditOperatorUserId = null,
    confirmedEditSetupTimeMin = null,
    confirmedEditWallStartMs = null,
    confirmedEditWallEndMs = null,
    confirmedEditPausedSec = "",
    confirmedEditSubmitting = false,
)

private fun restoreConfirmedEditSession(planId: Int, snapshot: ChamferingConfirmedEditSnapshot, sessions: MutableMap<Int, ChamferingPlanSession>) {
    val sess = sessions.getOrPut(planId) { ChamferingSessionLogic.emptySession() }
    sess.operatorUserId = snapshot.operatorUserId
    sess.setupTimeMin = snapshot.setupTimeMin
    sess.wallStart = snapshot.wallStart
    sess.wallEnd = snapshot.wallEnd
    sess.pausedAccumMs = snapshot.pausedAccumMs
    sess.activeAccumMs = snapshot.activeAccumMs
    sess.runningSliceStart = null
    sess.pauseSliceStart = null
}

class ChamferingActualViewModelFactory(
    private val repository: ChamferingRepository,
    private val offlineStore: ChamferingOfflineStore,
    private val networkMonitor: NetworkMonitor,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChamferingActualViewModel(repository, offlineStore, networkMonitor) as T
    }
}
