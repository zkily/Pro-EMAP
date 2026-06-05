package com.example.smart_emap.ui.mes.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.mes.InspectionRowSnapshot
import com.example.smart_emap.core.mes.InspectionSessionLogic
import com.example.smart_emap.core.mes.PlanSession
import com.example.smart_emap.core.mes.TimerPhase
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
    val isLoadingDefects: Boolean = false,
    val errorMessage: String? = null,
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
)

class InspectionActualViewModel(
    private val repository: InspectionRepository,
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
    private var clientInstanceId: String = ""

    private val _uiState = MutableStateFlow(InspectionUiState(inspectorLabel = inspectorLabel))
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            clientInstanceId = repository.getClientInstanceId()
            loadInitial()
            startTickLoop()
            startSyncLoop()
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
        val label = product?.let { "${it.productCode} · ${it.productName}" } ?: productCd
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
                loadPlans()
                val fresh = managementRows.find { it.id == planId }
                if (fresh == null || rowMesLockOwner(fresh) == MesLockOwner.Other) {
                    _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                    return@launch
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
            val code = state.selectedProductCode ?: return@launch
            val product = state.products.find { it.productCode == code } ?: return@launch
            try {
                var planId = findOpenRow(code)?.id
                if (planId == null) {
                    planId = repository.createPlan(
                        productionDay = state.productionDay,
                        productCd = code,
                        productName = product.productName.trim().ifEmpty { code },
                        inspectorUserId = userId,
                    )
                    loadPlans()
                }
                val session = ensureSession(planId)
                if (session.wallStart != null) return@launch
                val now = System.currentTimeMillis()
                val iso = Instant.ofEpochMilli(now).toString()
                locallyOperated.add(planId)
                session.wallStart = now
                session.activeAccumMs = 0
                session.pausedAccumMs = 0
                session.pauseSliceStart = null
                session.runningSliceStart = now
                session.wallEnd = null
                _uiState.update { it.copy(activePlanId = planId) }
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
                    applyPlansFromServer(showLoading = false, rebindSelection = false)
                    return@launch
                }
                _uiState.update {
                    it.copy(snackbarMessage = inspStringsFor(it.locale).started)
                }
                publishUi()
                applyPlansFromServer(showLoading = false, rebindSelection = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = e.message ?: "保存に失敗しました") }
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
            if (!persistTimerCheckpoint(planId, session)) return@launch
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
            if (!persistTimerCheckpoint(planId, session)) return@launch
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
                locallyOperated.remove(planId)
                sessions[planId] = InspectionSessionLogic.emptySession(defectItems.map { it.defectCd })
                _uiState.update {
                    it.copy(
                        activePlanId = null,
                        endDialogVisible = false,
                        endDialogSubmitting = false,
                        snackbarMessage = "実績を保存しました",
                    )
                }
                loadPlans()
                publishUi()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(endDialogSubmitting = false, snackbarMessage = e.message ?: "保存に失敗しました")
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
            if (!canServerPatchPlan(planId)) return@launch
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

    private suspend fun loadInitial() {
        _uiState.update { it.copy(isLoadingProducts = true, isLoadingDefects = true) }
        runCatching { repository.loadProducts() }
            .onSuccess { list -> _uiState.update { it.copy(products = list, isLoadingProducts = false) } }
            .onFailure { e -> _uiState.update { it.copy(isLoadingProducts = false, errorMessage = e.message) } }
        runCatching { repository.loadDefectItems() }
            .onSuccess { items ->
                defectItems = items
                _uiState.update { it.copy(defectGroups = groupDefectItems(items), isLoadingDefects = false) }
            }
            .onFailure { e -> _uiState.update { it.copy(isLoadingDefects = false, errorMessage = e.message) } }
        loadPlans(showLoading = true)
    }

    private suspend fun loadPlans(showLoading: Boolean = false, rebindSelection: Boolean = true) {
        applyPlansFromServer(showLoading = showLoading, rebindSelection = rebindSelection)
    }

    /** Web の syncMesStateFromServer：バックグラウンド同期はローディング表示しない */
    private suspend fun syncPlansFromServer() {
        if (syncInFlight) return
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
                if (showLoading) {
                    _uiState.update { it.copy(isLoadingPlans = false) }
                }
                if (rebindSelection) {
                    bindActivePlanFromSelection()
                }
                tryReclaimOperatedPlansOnLoad()
                detachFromRemoteInProgressContext()
                publishUi()
            }
            .onFailure { e ->
                if (showLoading) {
                    _uiState.update { it.copy(isLoadingPlans = false, errorMessage = e.message ?: "一覧取得に失敗しました") }
                }
            }
    }

    private fun bindActivePlanFromSelection() {
        val code = _uiState.value.selectedProductCode ?: return
        val currentId = _uiState.value.activePlanId
        val planId = when {
            currentId != null && locallyOperated.contains(currentId) -> currentId
            else -> findOpenRow(code)?.id ?: currentId
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

    private fun findOpenRow(code: String): InspectionManagementRowDto? {
        val operatedId = _uiState.value.activePlanId
        if (operatedId != null && locallyOperated.contains(operatedId)) {
            managementRows.find { it.id == operatedId && it.productCd == code }?.let { return it }
        }
        return managementRows.firstOrNull { row ->
            row.productCd == code &&
                (row.productionCompletedCheck ?: 0) != 1 &&
                (row.mesInspectorUserId == null || row.mesInspectorUserId == userId)
        }
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
        val row = managementRows.find { it.id == planId } ?: return false
        if (!isRowMesActive(row)) return false
        return when (rowMesLockOwner(row)) {
            MesLockOwner.Other -> false
            MesLockOwner.Mine -> true
            MesLockOwner.Unclaimed -> locallyOperated.contains(planId)
        }
    }

    private suspend fun patchWithConflictHandling(planId: Int, body: PatchInspectionBody): Boolean {
        return try {
            repository.patchPlan(planId, body)
            true
        } catch (e: InspectionPatchException) {
            if (e.statusCode == 409) {
                _uiState.update { it.copy(snackbarMessage = e.message) }
                syncPlansFromServer()
                detachFromRemoteInProgressContext()
                publishUi()
            } else {
                _uiState.update { it.copy(snackbarMessage = e.message) }
            }
            false
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = e.message ?: "保存に失敗しました") }
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
        if (!canServerPatchPlan(planId)) return false
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
        val now = System.currentTimeMillis()

        val inProgress = managementRows.filter { isRowMesActive(it) }
        val completed = managementRows.filter { row ->
            (row.productionCompletedCheck ?: 0) == 1 &&
                row.mesInspectorUserId == userId &&
                rowProductionDay(row) == state.productionDay
        }
        val phase = session?.let { InspectionSessionLogic.timerPhase(it) } ?: TimerPhase.Idle
        val inProgressLocal = session != null && InspectionSessionLogic.isProductionInProgress(session)
        val locked = inProgressLocal && locallyOperated.contains(planId)
        val canEdit = locked && locallyOperated.contains(planId ?: -1)

        val next = state.copy(
            inProgressRows = inProgress,
            completedRows = completed,
            completedQtyTotal = completed.sumOf { r -> r.actualProductionQuantity ?: 0 },
            displayProductCd = activeRow?.productCd ?: product?.productCode ?: "—",
            displayProductName = activeRow?.productName ?: product?.productName ?: "—",
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
                if (activeRow != null && planId != null && isRowMesActive(activeRow) &&
                    !locallyOperated.contains(planId)
                ) {
                    return@run false
                }
                session == null || session.wallStart == null
            },
            canPause = canEdit && session != null && InspectionSessionLogic.isTimerRunning(session),
            canResume = canEdit && session != null && InspectionSessionLogic.isTimerPaused(session),
            canEnd = canEdit && session != null && InspectionSessionLogic.isProductionInProgress(session) &&
                !InspectionSessionLogic.isTimerPaused(session),
            showPlanCard = state.selectedProductCode != null,
            productSelectionLocked = locked,
            canEditDefects = canEdit,
            showSessionRecoveryAlert = activeRow != null && planId != null && isRowMesActive(activeRow) &&
                rowMesLockOwner(activeRow) != MesLockOwner.Other &&
                !locallyOperated.contains(planId),
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
        private val userId: Int,
        private val inspectorLabel: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InspectionActualViewModel(repository, userId, inspectorLabel) as T
        }
    }
}
