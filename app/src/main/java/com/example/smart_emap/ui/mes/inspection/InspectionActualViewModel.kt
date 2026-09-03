package com.example.smart_emap.ui.mes.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.auth.SessionStore
import com.example.smart_emap.core.mes.InspectionOfflineStore
import com.example.smart_emap.core.mes.InspectionRowSnapshot
import com.example.smart_emap.core.mes.InspectionSessionLogic
import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.core.mes.MesInspectionWebSocket
import com.example.smart_emap.core.mes.PendingCreatePlan
import com.example.smart_emap.core.mes.PlanSession
import com.example.smart_emap.core.mes.TimerPhase
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.core.network.NetworkMonitor
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.defectCdKeys
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.InspectionNextAssignmentDto
import com.example.smart_emap.data.model.InspectionQrScanDto
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MesLockOwner { Unclaimed, Mine, Other }

enum class EndDialogQtyInputSource { Box, Piece }

data class EndDialogQtyMismatch(val piece: Int, val upb: Int)

enum class InspRetryAction {
    RefreshAll,
    ReloadProducts,
    ReloadPlans,
    ReloadDefects,
    ReloadSync,
}

private const val RECONNECT_SYNC_DELAY_MS = 500L

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

data class QrScanSuccessInfo(
    val productCd: String,
    val productName: String,
    val timeDisplay: String,
)

enum class QrScanNoticeKind { Mismatch, Cooldown }

data class QrScanNoticeInfo(
    val kind: QrScanNoticeKind,
    val title: String,
    val detail: String,
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
    val endDialogBoxes: String = "",
    val endDialogPieceQty: String = "",
    val endDialogUnitPerBox: Int = 0,
    val endDialogQtyInputSource: EndDialogQtyInputSource? = null,
    val endDialogQtyMismatch: EndDialogQtyMismatch? = null,
    val endDialogQtyMismatchConfirm: EndDialogQtyMismatch? = null,
    val endDialogCanSubmit: Boolean = false,
    val endDialogSubmitting: Boolean = false,
    val endDialogWallEndDisplay: String = "—",
    val endDialogScanCount: Int = 0,
    val endDialogScanBoxTotal: Int = 0,
    val endDialogScanPieceTotal: Int = 0,
    val qrScanHistoryVisible: Boolean = false,
    val qrScanHistoryLoading: Boolean = false,
    val qrScanHistoryItems: List<InspectionQrScanDto> = emptyList(),
    val qrScanHistoryBoxTotal: Int = 0,
    val qrScanHistoryPieceTotal: Int = 0,
    val qrScanHistoryScanCount: Int = 0,
    val timerPhase: TimerPhase = TimerPhase.Idle,
    val timerPhaseLabel: String = "未開始",
    val elapsedDisplay: String = "00:00:00",
    val pausedDisplay: String = "00:00:00",
    val breakDisplay: String = "00:00:00",
    val wallStartDisplay: String = "—",
    val wallStartClockDisplay: String = "—",
    val wallEndDisplay: String = "—",
    val defectTotal: Int = 0,
    /** Snapshot for Compose invalidation (session map is not observed directly). */
    val defectCounts: Map<String, Int> = emptyMap(),
    val canStart: Boolean = false,
    val canPause: Boolean = false,
    val canResume: Boolean = false,
    val canBreak: Boolean = false,
    val canResumeBreak: Boolean = false,
    val canEnd: Boolean = false,
    val canCancelProduction: Boolean = false,
    val cancelProductionConfirmVisible: Boolean = false,
    val cancelProductionSubmitting: Boolean = false,
    val showPlanCard: Boolean = false,
    val productSelectionLocked: Boolean = false,
    val canEditDefects: Boolean = false,
    val completedQtyTotal: Int = 0,
    val showSessionRecoveryAlert: Boolean = false,
    val showOtherTerminalLockBanner: Boolean = false,
    val canReclaimFromOtherTerminal: Boolean = false,
    val canForceReleaseLock: Boolean = false,
    val reclaimConfirmRowId: Int? = null,
    val forceReleaseConfirmRowId: Int? = null,
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
    val showNextAssignmentStrip: Boolean = false,
    val nextAssignmentProductLabel: String = "",
    val nextAssignmentProductTitle: String = "",
    val canApplyNextAssignmentProduct: Boolean = false,
    val inProgressPanelVisible: Boolean = false,
    val helpDialogVisible: Boolean = false,
    val persistentScannerVisible: Boolean = false,
    val persistentScannerLastCode: String = "",
    val persistentScannerLastAtDisplay: String = "",
    /** 一時停止 / 休憩中の離席バナー */
    val showAwayBanner: Boolean = false,
    val qrScanSuccessBanner: QrScanSuccessInfo? = null,
    val qrScanNoticeBanner: QrScanNoticeInfo? = null,
)

class InspectionActualViewModel(
    private val repository: InspectionRepository,
    private val offlineStore: InspectionOfflineStore,
    private val networkMonitor: NetworkMonitor,
    private val sessionStore: SessionStore,
    private val defaultApiBaseUrl: String,
    private val canMesEdit: Boolean,
    private val userId: Int,
    private val inspectorLabel: String,
) : ViewModel() {
    private val sessions = mutableMapOf<Int, PlanSession>()
    private val locallyOperated = mutableSetOf<Int>()
    private var managementRows = listOf<InspectionManagementRowDto>()
    private var defectItems = listOf<ProcessDefectItemDto>()
    private var tickJob: Job? = null
    private var syncJob: Job? = null
    private var checkpointJob: Job? = null
    private val planSyncMutex = Mutex()
    private var flushInFlight = false
    private var reconnectSyncJob: Job? = null
    private var clientInstanceId: String = ""
    private var confirmedEditSnapshot: ConfirmedEditSnapshot? = null
    private var mesInspectionWebSocket: MesInspectionWebSocket? = null

    private data class ConfirmedEditSnapshot(
        val wallStart: Long?,
        val wallEnd: Long?,
        val pausedAccumMs: Long,
        val activeAccumMs: Long,
        val defects: Map<String, Int>,
    )

    private val _uiState = MutableStateFlow(InspectionUiState(inspectorLabel = inspectorLabel))
    val uiState: StateFlow<InspectionUiState> = _uiState.asStateFlow()

    private var started = false
    private var networkJob: Job? = null
    /** loadInitial の初回一覧取得完了前はネットワーク復帰同期を走らせない */
    private var initialPlansLoadCompleted = false
    /** 直近の成功同期以降にオフラインになった場合のみ stale バナーを許可 */
    private var wasOfflineSinceLastSuccessfulSync = false
    private var myNextAssignment: InspectionNextAssignmentDto? = null
    private var nextAssignmentAutoClearInFlight = false
    /** 「選ぶ」後、サーバ削除完了まで同期で再表示しない */
    private var suppressNextAssignmentSync = false
    private var persistentScannerLastPatchAtMs: Long = 0L
    private var persistentScannerLastPatchedCode: String = ""
    private var qrScanSuccessDismissJob: Job? = null
    private var qrScanNoticeDismissJob: Job? = null
    private var persistentScannerLastNoticeShownAtMs: Long = 0L
    /** カメラ側の誤検出に加え、ViewModel でも 2 秒以内の再処理を黙って無視 */
    private var persistentScannerLastHandledAtMs: Long = 0L
    private var sessionScanBoxTotal: Int = 0
    private var sessionScanPieceTotal: Int = 0
    private var sessionScanCount: Int = 0

    /** 进入検査画面后再拉取计划与轮询，避免主界面挂载时触发 /api/plan/inspection-management/list。 */
    fun ensureStarted() {
        if (started) return
        started = true
        viewModelScope.launch {
            clientInstanceId = repository.getClientInstanceId(userId)
            hydrateFromOfflineCache()
            refreshPendingSyncState()
            loadInitial()
            startTickLoop()
            startSyncLoop()
            startCheckpointLoop()
            startMesInspectionWebSocket()
            if (networkMonitor.currentOnline()) {
                flushOfflineQueue()
            }
        }
        networkJob = viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isNetworkOnline = online, isOfflineMode = !online || it.pendingSyncCount > 0) }
                if (online) {
                    scheduleReconnectSync()
                } else {
                    wasOfflineSinceLastSuccessfulSync = true
                    reconnectSyncJob?.cancel()
                }
            }
        }
    }

    override fun onCleared() {
        mesInspectionWebSocket?.stop()
        mesInspectionWebSocket = null
        tickJob?.cancel()
        syncJob?.cancel()
        checkpointJob?.cancel()
        networkJob?.cancel()
        reconnectSyncJob?.cancel()
        qrScanSuccessDismissJob?.cancel()
        qrScanNoticeDismissJob?.cancel()
        super.onCleared()
    }

    fun dismissQrScanSuccessBanner() {
        qrScanSuccessDismissJob?.cancel()
        _uiState.update { it.copy(qrScanSuccessBanner = null) }
    }

    fun dismissQrScanNoticeBanner() {
        qrScanNoticeDismissJob?.cancel()
        _uiState.update { it.copy(qrScanNoticeBanner = null) }
    }

    fun openQrScanHistoryPanel() {
        val code = _uiState.value.selectedProductCode
        if (code.isNullOrBlank()) {
            _uiState.update {
                it.copy(snackbarMessage = inspStringsFor(it.locale).qrScanHistoryNeedProduct)
            }
            return
        }
        _uiState.update { it.copy(qrScanHistoryVisible = true) }
        viewModelScope.launch { refreshQrScanHistory(silent = false) }
    }

    fun closeQrScanHistoryPanel() {
        _uiState.update {
            it.copy(
                qrScanHistoryVisible = false,
                qrScanHistoryLoading = false,
            )
        }
    }

    private suspend fun refreshQrScanHistory(silent: Boolean) {
        val state = _uiState.value
        val productCd = state.selectedProductCode?.trim().orEmpty()
        if (productCd.isEmpty()) return
        if (!silent) {
            _uiState.update { it.copy(qrScanHistoryLoading = true) }
        }
        if (!networkMonitor.checkOnline()) {
            _uiState.update {
                it.copy(
                    qrScanHistoryLoading = false,
                    snackbarMessage = if (silent) {
                        it.snackbarMessage
                    } else {
                        inspStringsFor(it.locale).qrScanHistoryLoadFailed
                    },
                )
            }
            return
        }
        runCatching {
            val scope = buildQrScanQueryScope() ?: return
            if (scope.inspectionId == null || scope.startedAtIso == null) {
                _uiState.update {
                    it.copy(
                        qrScanHistoryLoading = false,
                        qrScanHistoryItems = emptyList(),
                        qrScanHistoryScanCount = 0,
                        qrScanHistoryBoxTotal = 0,
                        qrScanHistoryPieceTotal = 0,
                    )
                }
                return
            }
            repository.loadQrScanList(
                productionDay = scope.productionDay,
                productCd = scope.productCd,
                inspectionId = scope.inspectionId,
                startedAt = scope.startedAtIso,
                endedAt = scope.endedAtIso,
            )
        }.onSuccess { data ->
            val items = data.items.orEmpty()
            _uiState.update {
                it.copy(
                    qrScanHistoryLoading = false,
                    qrScanHistoryItems = items,
                    qrScanHistoryScanCount = (data.scanCount ?: items.size).coerceAtLeast(0),
                    qrScanHistoryBoxTotal = (data.boxQtyTotal ?: items.sumOf { row -> row.boxQty ?: 0 }).coerceAtLeast(0),
                    qrScanHistoryPieceTotal = (data.pieceQtyTotal ?: items.sumOf { row -> row.pieceQty ?: 0 }).coerceAtLeast(0),
                )
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    qrScanHistoryLoading = false,
                    snackbarMessage = if (silent) it.snackbarMessage
                    else formatNetworkError(e, inspStringsFor(it.locale).qrScanHistoryLoadFailed),
                )
            }
        }
    }

    private fun showQrScanSuccessBanner(productCd: String, productName: String, timeDisplay: String) {
        qrScanSuccessDismissJob?.cancel()
        _uiState.update {
            it.copy(
                qrScanSuccessBanner = QrScanSuccessInfo(
                    productCd = productCd,
                    productName = productName,
                    timeDisplay = timeDisplay,
                ),
                qrScanNoticeBanner = null,
            )
        }
        qrScanSuccessDismissJob = viewModelScope.launch {
            delay(QR_SCAN_SUCCESS_BANNER_MS)
            _uiState.update { state ->
                if (state.qrScanSuccessBanner != null) state.copy(qrScanSuccessBanner = null) else state
            }
        }
    }

    private fun showQrScanNoticeBanner(kind: QrScanNoticeKind, title: String, detail: String) {
        qrScanNoticeDismissJob?.cancel()
        _uiState.update {
            it.copy(
                qrScanNoticeBanner = QrScanNoticeInfo(
                    kind = kind,
                    title = title,
                    detail = detail,
                ),
                qrScanSuccessBanner = null,
            )
        }
        qrScanNoticeDismissJob = viewModelScope.launch {
            val dismissMs = if (kind == QrScanNoticeKind.Cooldown) {
                QR_SCAN_COOLDOWN_NOTICE_MS
            } else {
                QR_SCAN_SUCCESS_BANNER_MS
            }
            delay(dismissMs)
            _uiState.update { state ->
                if (state.qrScanNoticeBanner != null) state.copy(qrScanNoticeBanner = null) else state
            }
        }
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

    fun onPersistentScannerCodeScanned(code: String) {
        val trimmed = code.trim().take(512)
        if (trimmed.isEmpty()) return
        if (!Regex("^\\d{5}$").matches(trimmed)) return
        val planId = _uiState.value.activePlanId ?: return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isProductionInProgress(session)) return
        // 一時停止・休憩中はカメラ停止のため読取も無視
        if (!InspectionSessionLogic.isTimerRunning(session)) return
        val now = System.currentTimeMillis()
        // 移開時の再検出など、2秒以内の連続コールは無音で無視
        if (persistentScannerLastHandledAtMs > 0L &&
            now - persistentScannerLastHandledAtMs < PERSISTENT_SCAN_DEBOUNCE_MS
        ) {
            return
        }
        persistentScannerLastHandledAtMs = now
        val state = _uiState.value
        val currentProductCd = state.selectedProductCode

        // 1) 冷却チェック：成功讀取后，未满 60 秒再次读到要提示
        val cooldownLeftMs = if (persistentScannerLastPatchAtMs > 0L) {
            PERSISTENT_SCAN_COOLDOWN_MS - (now - persistentScannerLastPatchAtMs)
        } else {
            0L
        }
        if (cooldownLeftMs > 0L) {
            val remainSec = ((cooldownLeftMs + 999) / 1000).toInt().coerceAtLeast(1)
            if ((now - persistentScannerLastNoticeShownAtMs) < 1_500L) return
            persistentScannerLastNoticeShownAtMs = now
            val s = inspStringsFor(state.locale)
            showQrScanNoticeBanner(
                kind = QrScanNoticeKind.Cooldown,
                title = s.qrScanCooldownTitle,
                detail = s.qrScanCooldownDetail.replace("{sec}", remainSec.toString()),
            )
            return
        }

        // 2) 製品CD検証：読取QRから解決した製品CDが、現在生産中の製品と一致すること
        if (currentProductCd.isNullOrBlank()) {
            if ((now - persistentScannerLastNoticeShownAtMs) < 1_500L) return
            persistentScannerLastNoticeShownAtMs = now
            val s = inspStringsFor(state.locale)
            showQrScanNoticeBanner(
                kind = QrScanNoticeKind.Mismatch,
                title = s.qrScanProductUnsetTitle,
                detail = s.qrScanProductUnsetDetail,
            )
            return
        }
        val resolvedCd = resolveProductCodeFromScan(trimmed, state.products)
        if (resolvedCd == null) {
            if ((now - persistentScannerLastNoticeShownAtMs) < 1_500L) return
            persistentScannerLastNoticeShownAtMs = now
            val s = inspStringsFor(state.locale)
            showQrScanNoticeBanner(
                kind = QrScanNoticeKind.Mismatch,
                title = s.qrScanProductMismatchTitle,
                detail = s.qrScanProductNotFoundDetail
                    .replace("{code}", trimmed)
                    .replace("{current}", currentProductCd),
            )
            return
        }
        if (resolvedCd != currentProductCd) {
            if ((now - persistentScannerLastNoticeShownAtMs) < 1_500L) return
            persistentScannerLastNoticeShownAtMs = now
            val s = inspStringsFor(state.locale)
            showQrScanNoticeBanner(
                kind = QrScanNoticeKind.Mismatch,
                title = s.qrScanProductMismatchTitle,
                detail = s.qrScanProductMismatchDetail
                    .replace("{current}", currentProductCd)
                    .replace("{resolved}", resolvedCd)
                    .replace("{raw}", trimmed),
            )
            return
        }

        // 一致：成功提示 + 独立表へ登録
        persistentScannerLastPatchAtMs = now
        persistentScannerLastPatchedCode = trimmed

        val timeDisplay = formatWall(now)
        val isoTimestamp = Instant.ofEpochMilli(now).toString()
        val unitPerBox = resolveUnitPerBox(currentProductCd, state.products)
        val boxQty = 1
        val pieceQty = if (unitPerBox > 0) unitPerBox * boxQty else 0

        val productName = state.products
            .find { it.productCode == currentProductCd }
            ?.productName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: state.displayProductName

        // 楽観更新（オフライン時も表示）。成功後は DB 合計で上書きする
        sessionScanCount += 1
        sessionScanBoxTotal += boxQty
        sessionScanPieceTotal += pieceQty

        _uiState.update {
            it.copy(
                persistentScannerLastCode = trimmed,
                persistentScannerLastAtDisplay = formatWallClock(now),
                endDialogScanCount = sessionScanCount,
                endDialogScanBoxTotal = sessionScanBoxTotal,
                endDialogScanPieceTotal = sessionScanPieceTotal,
            )
        }

        showQrScanSuccessBanner(
            productCd = currentProductCd,
            productName = productName,
            timeDisplay = timeDisplay,
        )

        viewModelScope.launch {
            runCatching {
                repository.createQrScan(
                    productionDay = state.productionDay,
                    productCd = currentProductCd,
                    productName = productName,
                    unitPerBox = unitPerBox,
                    boxQty = boxQty,
                    pieceQty = pieceQty,
                    scannedCode = trimmed,
                    inspectionId = planId.takeIf { it > 0 && !offlineStore.isLocalPlanId(it) },
                    inspectorUserId = userId,
                    registeredAt = isoTimestamp,
                )
            }.onSuccess {
                // 本製品の DB 合計を再取得
                hydrateSessionScanTotals(currentProductCd)
                if (_uiState.value.qrScanHistoryVisible) {
                    refreshQrScanHistory(silent = true)
                }
            }.onFailure { e ->
                sessionScanCount = (sessionScanCount - 1).coerceAtLeast(0)
                sessionScanBoxTotal = (sessionScanBoxTotal - boxQty).coerceAtLeast(0)
                sessionScanPieceTotal = (sessionScanPieceTotal - pieceQty).coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        endDialogScanCount = sessionScanCount,
                        endDialogScanBoxTotal = sessionScanBoxTotal,
                        endDialogScanPieceTotal = sessionScanPieceTotal,
                        snackbarMessage = formatNetworkError(e, inspStringsFor(it.locale).saveFailed),
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { loadPlans(showLoading = true) }
    }

    fun refreshAll() {
        ensureStarted()
        viewModelScope.launch { loadInitial() }
    }

    fun retryLoad(action: InspRetryAction) {
        viewModelScope.launch {
            when (action) {
                InspRetryAction.RefreshAll -> loadInitial()
                InspRetryAction.ReloadProducts -> fetchProducts(showSnackbarOnError = false)
                InspRetryAction.ReloadPlans -> loadPlans(showLoading = true)
                InspRetryAction.ReloadDefects -> loadDefectItems(showLoading = true)
                InspRetryAction.ReloadSync -> recoverServerSync(userInitiated = true)
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
        viewModelScope.launch {
            loadPlans(showLoading = true)
            if (_uiState.value.qrScanHistoryVisible) {
                refreshQrScanHistory(silent = true)
            }
        }
    }

    fun setProductionDayToday() {
        _uiState.update { it.copy(productionDay = jstToday()) }
        viewModelScope.launch {
            loadPlans(showLoading = true)
            if (_uiState.value.qrScanHistoryVisible) {
                refreshQrScanHistory(silent = true)
            }
        }
    }

    fun onProductSelected(code: String?) {
        if (_uiState.value.productSelectionLocked && code != _uiState.value.selectedProductCode) {
            _uiState.update {
                it.copy(snackbarMessage = inspStringsFor(it.locale).switchProductBlocked)
            }
            return
        }
        _uiState.update { it.copy(selectedProductCode = code) }
        bindActivePlanFromSelection()
        publishUi()
        code?.let { selected ->
            viewModelScope.launch {
                clearMyNextAssignmentIfSelectedProductMatches(selected)
                if (_uiState.value.qrScanHistoryVisible) {
                    refreshQrScanHistory(silent = true)
                }
                publishUi()
            }
        } ?: run {
            if (_uiState.value.qrScanHistoryVisible) {
                _uiState.update {
                    it.copy(
                        qrScanHistoryItems = emptyList(),
                        qrScanHistoryBoxTotal = 0,
                        qrScanHistoryPieceTotal = 0,
                        qrScanHistoryScanCount = 0,
                    )
                }
            }
        }
    }

    fun applyNextAssignmentProductSelection() {
        val s = inspStringsFor(_uiState.value.locale)
        if (_uiState.value.productSelectionLocked) {
            _uiState.update { it.copy(snackbarMessage = s.switchProductBlocked) }
            return
        }
        val code = resolveProductCodeFromNextAssignment(_uiState.value.products) ?: run {
            _uiState.update { it.copy(snackbarMessage = s.nextAssignmentProductNotFound) }
            return
        }
        // 「選ぶ」直後に次製品表示を消す（サーバ削除は後続）
        suppressNextAssignmentSync = true
        myNextAssignment = null
        _uiState.update { it.copy(selectedProductCode = code) }
        bindActivePlanFromSelection()
        publishUi()
        viewModelScope.launch {
            try {
                deleteMyNextAssignmentOnServer()
            } finally {
                suppressNextAssignmentSync = false
                myNextAssignment = null
                publishUi()
            }
        }
    }

    fun openInProgressPanel() {
        _uiState.update { it.copy(inProgressPanelVisible = true) }
    }

    fun closeInProgressPanel() {
        _uiState.update { it.copy(inProgressPanelVisible = false) }
    }

    fun onInProgressPanelRowClick(row: InspectionManagementRowDto) {
        focusInProgressRow(row)
        closeInProgressPanel()
    }

    fun requestForceReleaseActiveRow() {
        val planId = _uiState.value.activePlanId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        requestForceReleaseSession(row)
    }

    fun onInProgressPanelResume(row: InspectionManagementRowDto) {
        requestResumeSession(row)
        closeInProgressPanel()
    }

    fun openHelpDialog() {
        _uiState.update { it.copy(helpDialogVisible = true) }
    }

    fun closeHelpDialog() {
        _uiState.update { it.copy(helpDialogVisible = false) }
    }

    fun inspectorNameForInProgressRow(row: InspectionManagementRowDto): String {
        val s = inspStringsFor(_uiState.value.locale)
        return row.mesInspectorName?.trim()?.takeIf { it.isNotEmpty() }
            ?: row.mesInspectorUsername?.trim()?.takeIf { it.isNotEmpty() }
            ?: if (row.mesInspectorUserId == userId) inspectorLabel
            else row.mesInspectorUserId?.toString() ?: s.inspectorMissing
    }

    fun inProgressRowStatusLabel(row: InspectionManagementRowDto): String {
        val s = inspStringsFor(_uiState.value.locale)
        if (rowMesLockOwner(row) == MesLockOwner.Other) return s.sessionLockedByOtherTerminalShort
        return when (row.mesProductionIsPaused) {
            1 -> s.timerPaused
            2 -> s.timerBreak
            else -> s.timerRunning
        }
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
        if (rowMesLockOwner(row) == MesLockOwner.Other) return canInspectorReclaimRow(row)
        return true
    }

    fun canForceReleaseSession(row: InspectionManagementRowDto): Boolean {
        if (!canMesEdit) return false
        if (row.id == null || !isRowMesActive(row)) return false
        return rowMesLockOwner(row) == MesLockOwner.Other
    }

    fun resumeSessionButtonLabel(row: InspectionManagementRowDto): String {
        val s = inspStringsFor(_uiState.value.locale)
        return if (rowMesLockOwner(row) == MesLockOwner.Other && canInspectorReclaimRow(row)) {
            s.btnReclaimSession
        } else {
            s.btnResumeSession
        }
    }

    private fun canInspectorReclaimRow(row: InspectionManagementRowDto): Boolean {
        if (row.id == null || !isRowMesActive(row)) return false
        val ri = row.mesInspectorUserId ?: return false
        return ri == userId
    }

    fun requestResumeSession(row: InspectionManagementRowDto) {
        if (rowMesLockOwner(row) == MesLockOwner.Other && canInspectorReclaimRow(row)) {
            _uiState.update { it.copy(reclaimConfirmRowId = row.id) }
            return
        }
        resumeInProgressSession(row)
    }

    fun dismissReclaimConfirm() {
        _uiState.update { it.copy(reclaimConfirmRowId = null) }
    }

    fun confirmReclaimSession() {
        val planId = _uiState.value.reclaimConfirmRowId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        _uiState.update { it.copy(reclaimConfirmRowId = null) }
        resumeInProgressSession(row)
    }

    fun requestForceReleaseSession(row: InspectionManagementRowDto) {
        if (!canForceReleaseSession(row)) return
        _uiState.update { it.copy(forceReleaseConfirmRowId = row.id) }
    }

    fun dismissForceReleaseConfirm() {
        _uiState.update { it.copy(forceReleaseConfirmRowId = null) }
    }

    fun confirmForceReleaseSession() {
        val planId = _uiState.value.forceReleaseConfirmRowId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        _uiState.update { it.copy(forceReleaseConfirmRowId = null) }
        forceReleaseMesClientLock(row)
    }

    fun resumeActiveSession() {
        val planId = _uiState.value.activePlanId ?: return
        val row = managementRows.find { it.id == planId } ?: return
        requestResumeSession(row)
    }

    /** 切回当前检验员正在生产的计划并恢复操作 */
    fun resumeMyActiveProduction() {
        val row = findMyActiveProductionRow() ?: return
        requestResumeSession(row)
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
            val owner = rowMesLockOwner(row)
            if (owner == MesLockOwner.Other && !canInspectorReclaimRow(row)) {
                _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                return@launch
            }
            if (owner != MesLockOwner.Mine) {
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
            alignSessionElapsedFromWallClock(planId, row)
            hydrateSessionScanTotals(row.productCd)
            publishUi()
            _uiState.update { it.copy(snackbarMessage = s.sessionResumed) }
        }
    }

    private fun forceReleaseMesClientLock(row: InspectionManagementRowDto) {
        viewModelScope.launch {
            val s = inspStringsFor(_uiState.value.locale)
            val planId = row.id ?: return@launch
            if (!canForceReleaseSession(row)) return@launch
            val ok = patchWithConflictHandling(
                planId,
                PatchInspectionBody(
                    mesForceRelease = true,
                    mesReleaseClientLock = true,
                ),
            )
            if (!ok) return@launch
            if (networkMonitor.currentOnline()) loadPlans()
            publishUi()
            _uiState.update { it.copy(snackbarMessage = s.forceReleaseLockSuccess) }
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
                            if (canInspectorReclaimRow(existingRow)) {
                                requestResumeSession(existingRow)
                            } else {
                                _uiState.update { it.copy(snackbarMessage = s.sessionLockedByOtherTerminal) }
                            }
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
                        upsertLocalPlanRow(
                            planId = planId,
                            productionDay = state.productionDay,
                            productCd = code,
                            productName = product.productName.trim().ifEmpty { code },
                            startedAt = null,
                        )
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
                    session = InspectionSessionLogic.emptySession(defectItems.defectCdKeys())
                    sessions[planId] = session
                }
                val now = System.currentTimeMillis()
                val iso = Instant.ofEpochMilli(now).toString()
                locallyOperated.add(planId)
                persistentScannerLastPatchAtMs = 0L
                persistentScannerLastPatchedCode = ""
                persistentScannerLastHandledAtMs = 0L
                sessionScanCount = 0
                sessionScanBoxTotal = 0
                sessionScanPieceTotal = 0
                session.wallStart = now
                session.activeAccumMs = 0
                session.pausedAccumMs = 0
                session.pauseSliceStart = null
                session.breakAccumMs = 0
                session.breakSliceStart = null
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
                    return@launch
                }
                upsertLocalPlanRow(
                    planId = planId,
                    productionDay = state.productionDay,
                    productCd = code,
                    productName = product.productName.trim().ifEmpty { code },
                    startedAt = iso,
                )
                updateLocalRowAfterMesStart(planId, iso)
                _uiState.update {
                    it.copy(
                        snackbarMessage = inspStringsFor(it.locale).started,
                        persistentScannerLastCode = "",
                        persistentScannerLastAtDisplay = "",
                        qrScanSuccessBanner = null,
                        qrScanNoticeBanner = null,
                        endDialogScanCount = 0,
                        endDialogScanBoxTotal = 0,
                        endDialogScanPieceTotal = 0,
                    )
                }
                // 生産開始後：当該製品の QR 合計を DB から取得
                hydrateSessionScanTotals(code)
                publishUi()
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
        val ws = resolveSessionWallStartMs(session, planId)
        if (ws != null) {
            InspectionSessionLogic.correctNetProductionFromWallClock(session, ws, now)
        } else {
            session.runningSliceStart = now
            session.pauseSliceStart = null
        }
        viewModelScope.launch {
            persistTimerCheckpoint(planId, session)
            publishUi()
        }
    }

    fun onBreakProduction() {
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isTimerRunning(session)) return
        val now = System.currentTimeMillis()
        InspectionSessionLogic.flushRunningSlice(session, now)
        session.breakSliceStart = now
        viewModelScope.launch {
            persistTimerCheckpoint(planId, session)
            publishUi()
        }
    }

    fun onResumeBreakProduction() {
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isTimerOnBreak(session)) return
        val now = System.currentTimeMillis()
        InspectionSessionLogic.flushBreakSlice(session, now)
        val ws = resolveSessionWallStartMs(session, planId)
        if (ws != null) {
            InspectionSessionLogic.correctNetProductionFromWallClock(session, ws, now)
        } else {
            session.runningSliceStart = now
            session.breakSliceStart = null
        }
        viewModelScope.launch {
            persistTimerCheckpoint(planId, session)
            publishUi()
        }
    }

    fun requestCancelProduction() {
        if (!_uiState.value.canCancelProduction) return
        _uiState.update {
            it.copy(cancelProductionConfirmVisible = true, cancelProductionSubmitting = false)
        }
    }

    fun dismissCancelProductionConfirm() {
        if (_uiState.value.cancelProductionSubmitting) return
        _uiState.update {
            it.copy(cancelProductionConfirmVisible = false, cancelProductionSubmitting = false)
        }
    }

    fun confirmCancelProduction() {
        if (_uiState.value.cancelProductionSubmitting) return
        val planId = _uiState.value.activePlanId ?: return
        if (!locallyOperated.contains(planId)) return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isProductionInProgress(session)) return
        viewModelScope.launch {
            val s = inspStringsFor(_uiState.value.locale)
            _uiState.update { it.copy(cancelProductionSubmitting = true) }
            val resolvedId = offlineStore.resolvePlanId(planId)
            val ok = when {
                resolvedId < 0 -> true
                !networkMonitor.checkOnline() -> {
                    _uiState.update {
                        it.copy(
                            cancelProductionSubmitting = false,
                            snackbarMessage = s.networkNoConnectionError,
                        )
                    }
                    false
                }
                else -> try {
                    repository.patchPlan(
                        resolvedId,
                        PatchInspectionBody(
                            mesForceRelease = true,
                            mesAbandonInProgress = true,
                        ),
                    )
                    true
                } catch (e: InspectionPatchException) {
                    if (e.statusCode == 409) {
                        true
                    } else {
                        _uiState.update {
                            it.copy(cancelProductionSubmitting = false, snackbarMessage = e.message)
                        }
                        false
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            cancelProductionSubmitting = false,
                            snackbarMessage = formatNetworkError(e, s.saveFailed),
                        )
                    }
                    false
                }
            }
            if (!ok) return@launch
            offlineStore.removePendingSyncForPlan(planId)
            clearLocalAbandonedSession(planId, dropUnsyncedLocalRow = resolvedId < 0)
            refreshPendingSyncState()
            _uiState.update {
                it.copy(
                    activePlanId = null,
                    selectedProductCode = null,
                    cancelProductionConfirmVisible = false,
                    cancelProductionSubmitting = false,
                    snackbarMessage = s.cancelProductionSuccess,
                    persistentScannerLastCode = "",
                    persistentScannerLastAtDisplay = "",
                    qrScanSuccessBanner = null,
                    qrScanNoticeBanner = null,
                    endDialogVisible = false,
                    endDialogScanCount = 0,
                    endDialogScanBoxTotal = 0,
                    endDialogScanPieceTotal = 0,
                )
            }
            loadPlans(showLoading = false, rebindSelection = false)
            syncMyNextAssignment()
            publishUi()
        }
    }

    private suspend fun clearLocalAbandonedSession(planId: Int, dropUnsyncedLocalRow: Boolean) {
        locallyOperated.remove(planId)
        sessions[planId] = InspectionSessionLogic.emptySession(defectItems.defectCdKeys())
        persistentScannerLastPatchAtMs = 0L
        persistentScannerLastPatchedCode = ""
        persistentScannerLastHandledAtMs = 0L
        sessionScanCount = 0
        sessionScanBoxTotal = 0
        sessionScanPieceTotal = 0
        qrScanSuccessDismissJob?.cancel()
        qrScanNoticeDismissJob?.cancel()
        managementRows = if (dropUnsyncedLocalRow && offlineStore.isLocalPlanId(planId)) {
            managementRows.filter { it.id != planId }
        } else {
            managementRows.map { row ->
                if (row.id != planId) {
                    row
                } else {
                    row.copy(
                        mesProductionStartedAt = null,
                        mesProductionEndedAt = null,
                        mesNetProductionSec = null,
                        mesPausedAccumSec = null,
                        mesBreakSec = null,
                        mesStopSec = null,
                        mesProductionIsPaused = null,
                        mesInspectorUserId = null,
                        mesInspectorName = null,
                        mesInspectorUsername = null,
                        mesDefectByItem = null,
                        defectQty = 0,
                        productionCompletedCheck = 0,
                        mesClientInstanceId = null,
                        mesScannedCode = null,
                        actualProductionQuantity = null,
                    )
                }
            }
        }
        offlineStore.savePlans(_uiState.value.productionDay, managementRows)
    }

    fun openEndDialog() {
        val planId = _uiState.value.activePlanId ?: return
        if (sessions[planId] == null) return
        if (!_uiState.value.canEnd) return
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                endDialogVisible = true,
                endDialogBoxes = "",
                endDialogPieceQty = "",
                endDialogQtyInputSource = null,
                endDialogQtyMismatch = null,
                endDialogQtyMismatchConfirm = null,
                endDialogWallEndDisplay = formatWall(now),
                endDialogScanCount = sessionScanCount,
                endDialogScanBoxTotal = sessionScanBoxTotal,
                endDialogScanPieceTotal = sessionScanPieceTotal,
            )
        }
        publishEndDialogQtyState()
        viewModelScope.launch { refreshEndDialogScanSummary() }
    }

    fun closeEndDialog() {
        resumeProductionAfterEndDialogCancel()
        _uiState.update {
            it.copy(
                endDialogVisible = false,
                endDialogQtyMismatchConfirm = null,
            )
        }
        publishUi()
    }

    private fun resumeProductionAfterEndDialogCancel() {
        val planId = _uiState.value.activePlanId ?: return
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isProductionInProgress(session)) return
        if (InspectionSessionLogic.isTimerPaused(session) ||
            InspectionSessionLogic.isTimerOnBreak(session) ||
            InspectionSessionLogic.isTimerRunning(session)
        ) {
            return
        }
        session.runningSliceStart = System.currentTimeMillis()
        viewModelScope.launch { persistTimerCheckpoint(planId, session) }
    }

    fun onEndDialogBoxesChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        val state = _uiState.value
        val upb = resolveUnitPerBox(state.selectedProductCode, state.products)
        val syncedPiece = when {
            filtered.isEmpty() -> ""
            upb > 0 -> {
                val boxes = filtered.toIntOrNull()
                if (boxes != null && boxes >= 0) pieceQtyFromBoxes(boxes, upb).toString() else state.endDialogPieceQty
            }
            else -> state.endDialogPieceQty
        }
        _uiState.update {
            it.copy(
                endDialogBoxes = filtered,
                endDialogPieceQty = syncedPiece,
                endDialogQtyInputSource = EndDialogQtyInputSource.Box,
            )
        }
        publishEndDialogQtyState()
    }

    fun onEndDialogPieceQtyChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        val state = _uiState.value
        val upb = resolveUnitPerBox(state.selectedProductCode, state.products)
        val syncedBoxes = when {
            filtered.isEmpty() -> ""
            upb > 0 -> {
                val piece = filtered.toIntOrNull()
                if (piece != null && piece >= 0) boxQtyFromPieces(piece, upb).toString() else state.endDialogBoxes
            }
            else -> state.endDialogBoxes
        }
        _uiState.update {
            it.copy(
                endDialogPieceQty = filtered,
                endDialogBoxes = syncedBoxes,
                endDialogQtyInputSource = EndDialogQtyInputSource.Piece,
            )
        }
        publishEndDialogQtyState()
    }

    private fun publishEndDialogQtyState() {
        val state = _uiState.value
        val unitPerBox = resolveUnitPerBox(state.selectedProductCode, state.products)
        val mismatch = resolveEndDialogQtyMismatch(state.endDialogPieceQty, unitPerBox)
        val canSubmit = endDialogCanSubmit(state.endDialogBoxes, state.endDialogPieceQty, unitPerBox)
        _uiState.update {
            it.copy(
                endDialogUnitPerBox = unitPerBox,
                endDialogQtyMismatch = mismatch,
                endDialogCanSubmit = canSubmit,
            )
        }
    }

    fun dismissProductionEndQtyMismatch() {
        _uiState.update { it.copy(endDialogQtyMismatchConfirm = null) }
    }

    fun confirmProductionEndQtyMismatch() {
        val confirm = _uiState.value.endDialogQtyMismatchConfirm ?: return
        _uiState.update { it.copy(endDialogQtyMismatchConfirm = null) }
        performProductionEnd(confirm.piece)
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
        if (sessions[planId] == null) return
        val state = _uiState.value
        val unitPerBox = resolveUnitPerBox(state.selectedProductCode, state.products)
        val s = inspStringsFor(state.locale)
        val qty = if (unitPerBox > 0) {
            state.endDialogPieceQty.trim().toIntOrNull() ?: -1
        } else {
            state.endDialogPieceQty.trim().toIntOrNull() ?: -1
        }
        if (qty < 0) {
            _uiState.update { it.copy(snackbarMessage = s.qtyInvalid) }
            return
        }
        if (unitPerBox > 0 && hasPieceBoxQtyMismatch(qty, unitPerBox)) {
            _uiState.update {
                it.copy(endDialogQtyMismatchConfirm = EndDialogQtyMismatch(qty, unitPerBox))
            }
            return
        }
        performProductionEnd(qty)
    }

    private fun performProductionEnd(qty: Int) {
        val planId = _uiState.value.activePlanId ?: return
        val session = sessions[planId] ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(endDialogSubmitting = true) }
            try {
                val now = System.currentTimeMillis()
                if (InspectionSessionLogic.isTimerRunning(session)) InspectionSessionLogic.flushRunningSlice(session, now)
                if (InspectionSessionLogic.isTimerPaused(session)) InspectionSessionLogic.flushPauseSlice(session, now)
                if (InspectionSessionLogic.isTimerOnBreak(session)) InspectionSessionLogic.flushBreakSlice(session, now)
                session.wallEnd = now
                val defectTotal = session.defects.values.sum()
                val breakSec = (InspectionSessionLogic.readBreakAccumMs(session, now) / 1000).toInt()
                val stopSec = (InspectionSessionLogic.readPausedAccumMs(session, now) / 1000).toInt()
                val ok = patchWithConflictHandling(
                    planId,
                    PatchInspectionBody(
                        productionDay = productionDayFromMillis(session.wallStart ?: now),
                        mesProductionEndedAt = Instant.ofEpochMilli(now).toString(),
                        mesNetProductionSec = (InspectionSessionLogic.readNetProductionMs(session, now) / 1000).toInt(),
                        mesBreakSec = breakSec,
                        mesStopSec = stopSec,
                        mesPausedAccumSec = breakSec + stopSec,
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
                sessions[planId] = InspectionSessionLogic.emptySession(defectItems.defectCdKeys())
                persistentScannerLastPatchAtMs = 0L
                persistentScannerLastPatchedCode = ""
                persistentScannerLastHandledAtMs = 0L
                sessionScanCount = 0
                sessionScanBoxTotal = 0
                sessionScanPieceTotal = 0
                qrScanSuccessDismissJob?.cancel()
                qrScanNoticeDismissJob?.cancel()
                val savedMsg = inspStringsFor(_uiState.value.locale).endProductionSaved
                _uiState.update {
                    it.copy(
                        activePlanId = null,
                        selectedProductCode = null,
                        endDialogVisible = false,
                        endDialogSubmitting = false,
                        snackbarMessage = savedMsg,
                        persistentScannerLastCode = "",
                        persistentScannerLastAtDisplay = "",
                        qrScanSuccessBanner = null,
                        qrScanNoticeBanner = null,
                        endDialogScanCount = 0,
                        endDialogScanBoxTotal = 0,
                        endDialogScanPieceTotal = 0,
                    )
                }
                loadPlans(showLoading = false, rebindSelection = false)
                syncMyNextAssignment()
                publishUi()
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
        if (!networkMonitor.checkOnline()) return
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
        val breakSec = (InspectionSessionLogic.readBreakAccumMs(session, endMs) / 1000).toInt()
        val stopSec = (InspectionSessionLogic.readPausedAccumMs(session, endMs) / 1000).toInt()
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
                    mesBreakSec = breakSec,
                    mesStopSec = stopSec,
                    mesPausedAccumSec = breakSec + stopSec,
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
                publishEndDialogQtyState()
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
        initialPlansLoadCompleted = false
        _uiState.update { it.copy(isLoadingDefects = true) }
        try {
            fetchProducts(showSnackbarOnError = false)
            loadDefectItems(showLoading = true)
            loadPlans(showLoading = true)
        } finally {
            initialPlansLoadCompleted = true
        }
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

    private suspend fun loadPlans(
        showLoading: Boolean = false,
        rebindSelection: Boolean = true,
        allowStaleBanner: Boolean = false,
    ) {
        applyPlansFromServer(
            showLoading = showLoading,
            rebindSelection = rebindSelection,
            allowStaleBanner = allowStaleBanner,
        )
    }

    /** Web の syncMesStateFromServer：バックグラウンド同期は失敗しても stale バナーを出さない */
    private suspend fun syncPlansFromServer() {
        applyPlansFromServer(showLoading = false, rebindSelection = false, allowStaleBanner = false)
    }

    private fun scheduleReconnectSync() {
        reconnectSyncJob?.cancel()
        reconnectSyncJob = viewModelScope.launch {
            delay(RECONNECT_SYNC_DELAY_MS)
            if (!networkMonitor.checkOnline()) return@launch
            if (!initialPlansLoadCompleted) return@launch
            recoverServerSync(userInitiated = false)
        }
    }

    /** オフライン復帰・「再読込」：キュー送信後にサーバー一覧を再取得 */
    private suspend fun recoverServerSync(userInitiated: Boolean) {
        if (userInitiated) {
            _uiState.update { it.copy(syncStaleMessage = null) }
        }
        if (!networkMonitor.checkOnline()) {
            if (userInitiated) {
                val s = inspStringsFor(_uiState.value.locale)
                _uiState.update {
                    it.copy(syncStaleMessage = s.networkErrorHints().noConnection)
                }
            }
            return
        }
        flushOfflineQueue()
        val allowStaleBanner = userInitiated || wasOfflineSinceLastSuccessfulSync
        applyPlansFromServer(
            showLoading = false,
            rebindSelection = true,
            allowStaleBanner = allowStaleBanner,
        )
    }

    private suspend fun applyPlansFromServer(
        showLoading: Boolean,
        rebindSelection: Boolean,
        allowStaleBanner: Boolean = false,
    ) {
        planSyncMutex.withLock {
            applyPlansFromServerLocked(showLoading, rebindSelection, allowStaleBanner)
        }
    }

    private suspend fun applyPlansFromServerLocked(
        showLoading: Boolean,
        rebindSelection: Boolean,
        allowStaleBanner: Boolean,
    ) {
        if (showLoading) {
            _uiState.update { it.copy(isLoadingPlans = true) }
        }
        val s = inspStringsFor(_uiState.value.locale)
        runCatching { repository.loadPlans(_uiState.value.productionDay) }
            .onSuccess { rows ->
                wasOfflineSinceLastSuccessfulSync = false
                managementRows = mergeServerPlansPreservingLocalMes(rows)
                val wasStale = _uiState.value.syncStaleMessage != null
                _uiState.update { state ->
                    state.copy(
                        isLoadingPlans = if (showLoading) false else state.isLoadingPlans,
                        plansLoadError = if (showLoading) null else state.plansLoadError,
                        syncStaleMessage = null,
                        snackbarMessage = if (wasStale && !showLoading) s.syncRecovered else state.snackbarMessage,
                    )
                }
                runCatching {
                    rows.forEach { row ->
                        val id = row.id ?: return@forEach
                        if (id !in sessions) {
                            sessions[id] = InspectionSessionLogic.emptySession(defectItems.defectCdKeys())
                        }
                        if (shouldHydrateSessionFromServer(id)) {
                            syncSessionFromRow(id, row)
                        }
                    }
                }
                if (rebindSelection) {
                    bindActivePlanFromSelection()
                }
                tryReclaimOperatedPlansOnLoad()
                detachFromRemoteInProgressContext()
                syncMyNextAssignment()
                publishUi()
                runCatching { offlineStore.savePlans(_uiState.value.productionDay, managementRows) }
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
                } else if (allowStaleBanner) {
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
                mesBreakSec = r.mesBreakSec,
                mesStopSec = r.mesStopSec,
                mesProductionIsPaused = r.mesProductionIsPaused,
                mesDefectByItem = r.mesDefectByItem,
            ),
        )
        if (InspectionSessionLogic.isProductionInProgress(session)) {
            InspectionSessionLogic.reconcileInProgressTimer(session)
        }
    }

    /** 作業再開・サーバー同期後：稼働時間を生産開始時刻基準で補正（Web correctNetProductionFromWallClock） */
    private fun alignSessionElapsedFromWallClock(planId: Int, row: InspectionManagementRowDto? = null) {
        val session = sessions[planId] ?: return
        if (!InspectionSessionLogic.isProductionInProgress(session)) return
        val ws = resolveSessionWallStartMs(session, planId, row) ?: return
        val now = System.currentTimeMillis()
        when {
            InspectionSessionLogic.isTimerPaused(session) || InspectionSessionLogic.isTimerOnBreak(session) -> {
                val serverNet = row?.mesNetProductionSec ?: managementRows.find { it.id == planId }?.mesNetProductionSec
                if ((serverNet ?: 0) == 0 && session.activeAccumMs == 0L) {
                    val pauseMs = InspectionSessionLogic.readExplicitPausedAccumMs(session, now)
                    val breakMs = InspectionSessionLogic.readExplicitBreakAccumMs(session, now)
                    session.activeAccumMs = (now - ws - pauseMs - breakMs).coerceAtLeast(0)
                }
            }
            else -> InspectionSessionLogic.correctNetProductionFromWallClock(session, ws, now)
        }
    }

    private fun resolveSessionWallStartMs(
        session: PlanSession,
        planId: Int,
        row: InspectionManagementRowDto? = null,
    ): Long? {
        session.wallStart?.let { return it }
        val startedAt = row?.mesProductionStartedAt
            ?: managementRows.find { it.id == planId }?.mesProductionStartedAt
        return MesDateTime.parseToMillis(startedAt)
    }

    private fun rowMesLockOwner(row: InspectionManagementRowDto?): MesLockOwner {
        if (row == null) return MesLockOwner.Unclaimed
        val lock = row.mesClientInstanceId?.trim().orEmpty()
        if (lock.isEmpty()) return MesLockOwner.Unclaimed
        return if (lock == clientInstanceId) MesLockOwner.Mine else MesLockOwner.Other
    }

    private fun canServerPatchPlan(planId: Int): Boolean {
        if (offlineStore.isLocalPlanId(planId) && locallyOperated.contains(planId)) return true
        if (!networkMonitor.checkOnline() && locallyOperated.contains(planId)) return true
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
        if (!networkMonitor.checkOnline()) {
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
                syncPlansFromServer()
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

    /** 本端末計測中はサーバー一覧でローカル MES 状態を上書きしない */
    private fun mergeServerPlansPreservingLocalMes(
        fresh: List<InspectionManagementRowDto>,
    ): List<InspectionManagementRowDto> {
        val localById = managementRows.associateBy { it.id }
        return fresh.map { serverRow ->
            val id = serverRow.id ?: return@map serverRow
            if (!locallyOperated.contains(id)) return@map serverRow
            val session = sessions[id] ?: return@map serverRow
            if (!InspectionSessionLogic.isProductionInProgress(session)) return@map serverRow
            val local = localById[id] ?: return@map serverRow
            serverRow.copy(
                mesProductionStartedAt = local.mesProductionStartedAt ?: serverRow.mesProductionStartedAt,
                mesProductionEndedAt = local.mesProductionEndedAt,
                mesProductionIsPaused = local.mesProductionIsPaused ?: serverRow.mesProductionIsPaused,
                mesClientInstanceId = local.mesClientInstanceId ?: serverRow.mesClientInstanceId,
                mesInspectorUserId = local.mesInspectorUserId ?: serverRow.mesInspectorUserId,
                mesNetProductionSec = local.mesNetProductionSec ?: serverRow.mesNetProductionSec,
                mesBreakSec = local.mesBreakSec ?: serverRow.mesBreakSec,
                mesStopSec = local.mesStopSec ?: serverRow.mesStopSec,
                mesPausedAccumSec = local.mesPausedAccumSec ?: serverRow.mesPausedAccumSec,
                mesDefectByItem = local.mesDefectByItem ?: serverRow.mesDefectByItem,
            )
        }
    }

    /** Web onStartProduction 同様：一覧再取得せずローカル行を更新 */
    private fun updateLocalRowAfterMesStart(planId: Int, startedAtIso: String) {
        managementRows = managementRows.map { row ->
            if (row.id != planId) {
                row
            } else {
                row.copy(
                    mesProductionStartedAt = startedAtIso,
                    mesProductionEndedAt = null,
                    mesProductionIsPaused = 0,
                    mesInspectorUserId = userId,
                    mesClientInstanceId = clientInstanceId,
                )
            }
        }
        viewModelScope.launch {
            offlineStore.savePlans(_uiState.value.productionDay, managementRows)
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
            InspectionSessionLogic.emptySession(defectItems.defectCdKeys())
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
        val breakSec = (InspectionSessionLogic.readBreakAccumMs(session, now) / 1000).toInt()
        val stopSec = (InspectionSessionLogic.readPausedAccumMs(session, now) / 1000).toInt()
        return patchWithConflictHandling(
            planId,
            PatchInspectionBody(
                mesNetProductionSec = (InspectionSessionLogic.readNetProductionMs(session, now) / 1000).toInt(),
                mesBreakSec = breakSec,
                mesStopSec = stopSec,
                mesPausedAccumSec = breakSec + stopSec,
                mesProductionIsPaused = when {
                    InspectionSessionLogic.isTimerOnBreak(session) -> 2
                    InspectionSessionLogic.isTimerPaused(session) -> 1
                    else -> 0
                },
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
        val unitPerBox = resolveUnitPerBox(state.selectedProductCode, state.products)
        val assignment = myNextAssignment
        val showNextStrip = assignment?.let {
            it.nextProductName?.trim()?.isNotEmpty() == true ||
                it.nextProductCd?.trim()?.isNotEmpty() == true
        } == true
        val nextLabel = assignment?.let {
            it.nextProductName?.trim()?.takeIf { name -> name.isNotEmpty() }
                ?: it.nextProductCd?.trim().orEmpty()
        }.orEmpty()
        val nextTitle = assignment?.let {
            val cd = it.nextProductCd?.trim().orEmpty()
            val name = it.nextProductName?.trim().orEmpty()
            when {
                cd.isNotEmpty() && name.isNotEmpty() -> "$cd · $name"
                name.isNotEmpty() -> name
                else -> cd
            }
        }.orEmpty()

        val next = state.copy(
            inProgressRows = inProgress,
            completedRows = completed,
            completedQtyTotal = completed.sumOf { r -> r.actualProductionQuantity ?: 0 },
            displayProductCd = rowForDisplay?.productCd ?: product?.productCode ?: "—",
            displayProductName = rowForDisplay?.productName?.trim()?.takeIf { it.isNotEmpty() }
                ?: product?.productName?.trim()?.takeIf { it.isNotEmpty() }
                ?: "—",
            timerPhase = phase,
            timerPhaseLabel = timerPhaseLabel(phase, inspStringsFor(state.locale)),
            elapsedDisplay = InspectionSessionLogic.formatDurationMs(
                session?.let { s -> InspectionSessionLogic.readNetProductionMs(s, now) } ?: 0,
            ),
            pausedDisplay = InspectionSessionLogic.formatDurationMs(
                session?.let { s -> InspectionSessionLogic.readPausedAccumMs(s, now) } ?: 0,
            ),
            breakDisplay = InspectionSessionLogic.formatDurationMs(
                session?.let { s -> InspectionSessionLogic.readBreakAccumMs(s, now) } ?: 0,
            ),
            wallStartDisplay = formatWall(session?.wallStart),
            wallStartClockDisplay = formatWallClock(session?.wallStart),
            wallEndDisplay = formatWall(session?.wallEnd),
            defectTotal = session?.defects?.values?.sum() ?: 0,
            defectCounts = session?.defects?.toMap() ?: emptyMap(),
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
            canBreak = editableSession?.let { InspectionSessionLogic.isTimerRunning(it) } == true,
            canResumeBreak = editableSession?.let { InspectionSessionLogic.isTimerOnBreak(it) } == true,
            canEnd = editableSession?.let {
                InspectionSessionLogic.isProductionInProgress(it) &&
                    !InspectionSessionLogic.isTimerPaused(it) &&
                    !InspectionSessionLogic.isTimerOnBreak(it)
            } == true,
            canCancelProduction = editableSession?.let {
                InspectionSessionLogic.isProductionInProgress(it)
            } == true,
            showPlanCard = state.selectedProductCode != null,
            productSelectionLocked = locked,
            canEditDefects = canEdit,
            showSessionRecoveryAlert = activeRow?.let { row ->
                row.id != null && isRowMesActive(row) &&
                    rowMesLockOwner(row) != MesLockOwner.Other &&
                    !locallyOperated.contains(row.id)
            } == true,
            showOtherTerminalLockBanner = activeRow?.let { row ->
                row.id != null && isRowMesActive(row) && rowMesLockOwner(row) == MesLockOwner.Other
            } == true,
            canReclaimFromOtherTerminal = activeRow?.let { row ->
                rowMesLockOwner(row) == MesLockOwner.Other && canInspectorReclaimRow(row)
            } == true,
            canForceReleaseLock = activeRow?.let { row -> canForceReleaseSession(row) } == true,
            showActiveProductionSwitchBanner = showActiveProductionSwitchBanner,
            activeProductionSwitchLabel = myActiveRow?.let { rowShortLabel(it) }.orEmpty(),
            showNextAssignmentStrip = showNextStrip,
            nextAssignmentProductLabel = nextLabel,
            nextAssignmentProductTitle = nextTitle,
            canApplyNextAssignmentProduct = !locked && resolveProductCodeFromNextAssignment(state.products) != null,
            // 計測中のみカメラ表示（一時停止・休憩中は閉じる）
            persistentScannerVisible = inProgressLocal && locked && phase == TimerPhase.Running,
            showAwayBanner = locked && (phase == TimerPhase.Paused || phase == TimerPhase.Break),
            endDialogUnitPerBox = unitPerBox,
            endDialogCanSubmit = endDialogCanSubmit(
                state.endDialogBoxes,
                state.endDialogPieceQty,
                unitPerBox,
            ),
            endDialogQtyMismatch = resolveEndDialogQtyMismatch(state.endDialogPieceQty, unitPerBox),
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
        val breakTime = InspectionSessionLogic.formatDurationMs(
            InspectionSessionLogic.readBreakAccumMs(session, now),
        )
        val phaseLabel = timerPhaseLabel(phase, inspStringsFor(state.locale))
        if (elapsed == state.elapsedDisplay &&
            paused == state.pausedDisplay &&
            breakTime == state.breakDisplay &&
            phase == state.timerPhase &&
            phaseLabel == state.timerPhaseLabel
        ) {
            return
        }
        _uiState.update {
            it.copy(
                elapsedDisplay = elapsed,
                pausedDisplay = paused,
                breakDisplay = breakTime,
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

    /** Web の runningPersistTimer（5s）と同等：計測中はサーバーへ checkpoint を送信し updated_at を更新 */
    private fun startCheckpointLoop() {
        checkpointJob?.cancel()
        checkpointJob = viewModelScope.launch {
            while (isActive) {
                delay(MES_CHECKPOINT_INTERVAL_MS)
                flushInProgressTimerCheckpoints()
            }
        }
    }

    private fun startMesInspectionWebSocket() {
        mesInspectionWebSocket?.stop()
        mesInspectionWebSocket = MesInspectionWebSocket(
            sessionStore = sessionStore,
            defaultApiBaseUrl = defaultApiBaseUrl,
            scope = viewModelScope,
        ) { productionDay, _ ->
            if (productionDay == _uiState.value.productionDay.trim()) {
                viewModelScope.launch { syncPlansFromServer() }
            }
        }.also { it.start() }
    }

    private suspend fun flushInProgressTimerCheckpoints() {
        val now = System.currentTimeMillis()
        for ((planId, session) in sessions) {
            if (!InspectionSessionLogic.isProductionInProgress(session)) continue
            if (!locallyOperated.contains(planId) && !canServerPatchPlan(planId)) continue
            when {
                InspectionSessionLogic.isTimerRunning(session) -> {
                    InspectionSessionLogic.flushRunningSlice(session, now)
                    if (session.wallEnd == null && session.pauseSliceStart == null && session.breakSliceStart == null) {
                        session.runningSliceStart = now
                    }
                }
                InspectionSessionLogic.isTimerPaused(session) -> {
                    InspectionSessionLogic.flushPauseSlice(session, now)
                    if (session.wallEnd == null) session.pauseSliceStart = now
                }
                InspectionSessionLogic.isTimerOnBreak(session) -> {
                    InspectionSessionLogic.flushBreakSlice(session, now)
                    if (session.wallEnd == null) session.breakSliceStart = now
                }
            }
            persistTimerCheckpoint(planId, session)
        }
    }

    private fun rowProductionDay(row: InspectionManagementRowDto): String {
        val stored = row.productionDay?.trim()?.take(10).orEmpty()
        if (stored.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return stored
        return _uiState.value.productionDay
    }

    private fun timerPhaseLabel(phase: TimerPhase, s: InspStrings): String = when (phase) {
        TimerPhase.Idle -> s.timerIdle
        TimerPhase.Running -> s.timerRunning
        TimerPhase.Paused -> s.timerPaused
        TimerPhase.Break -> s.timerBreak
        TimerPhase.Ended -> s.timerEnded
    }

    private fun formatWall(ts: Long?): String {
        if (ts == null) return "—"
        val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
        return Instant.ofEpochMilli(ts).atZone(ZoneId.of("Asia/Tokyo")).format(fmt)
    }

    private fun formatWallClock(ts: Long?): String {
        if (ts == null) return "—"
        val fmt = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.JAPAN)
        return Instant.ofEpochMilli(ts).atZone(ZoneId.of("Asia/Tokyo")).format(fmt)
    }

    private fun resolveUnitPerBox(code: String?, products: List<ErpProductDto>): Int {
        val hit = products.find { it.productCode == code } ?: return 0
        return (hit.unitPerBox ?: 0).coerceAtLeast(0)
    }

    private suspend fun syncMyNextAssignment() {
        if (suppressNextAssignmentSync || nextAssignmentAutoClearInFlight) return
        val day = _uiState.value.productionDay.trim()
        if (!day.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            myNextAssignment = null
            return
        }
        if (!networkMonitor.checkOnline()) return
        runCatching {
            myNextAssignment = repository.loadMyNextAssignment(day)
        }
    }

    private fun resolveSelectedProductName(code: String, products: List<ErpProductDto>): String {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return ""
        val fromMaster = products.find { it.productCode == trimmed }?.productName?.trim()
        if (!fromMaster.isNullOrEmpty()) return fromMaster
        return managementRows.find { (it.productCd ?: "").trim() == trimmed }
            ?.productName?.trim().orEmpty()
    }

    private suspend fun clearMyNextAssignmentIfSelectedProductMatches(code: String) {
        val assignment = myNextAssignment ?: return
        if (nextAssignmentAutoClearInFlight) return
        val nextCd = assignment.nextProductCd?.trim().orEmpty()
        val nextName = assignment.nextProductName?.trim().orEmpty()
        val selectedCd = code.trim()
        val selectedName = resolveSelectedProductName(selectedCd, _uiState.value.products)
        val matched = when {
            nextCd.isNotEmpty() && selectedCd.isNotEmpty() && nextCd == selectedCd -> true
            nextName.isNotEmpty() && selectedName.isNotEmpty() && nextName == selectedName -> true
            else -> false
        }
        if (!matched) return
        clearMyNextAssignmentAlways()
    }

    private suspend fun clearMyNextAssignmentAlways() {
        // 表示は即クリア。サーバ削除に失敗しても画面上は消す
        myNextAssignment = null
        deleteMyNextAssignmentOnServer()
    }

    private suspend fun deleteMyNextAssignmentOnServer() {
        if (nextAssignmentAutoClearInFlight) return
        val day = _uiState.value.productionDay.trim()
        if (!day.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) return
        if (!networkMonitor.checkOnline()) return
        nextAssignmentAutoClearInFlight = true
        try {
            runCatching {
                repository.deleteMyNextAssignment(day)
            }
        } finally {
            nextAssignmentAutoClearInFlight = false
            // 削除後もローカルは空のまま維持
            myNextAssignment = null
        }
    }

    private data class QrScanQueryScope(
        val productionDay: String,
        val productCd: String,
        val inspectionId: Int?,
        val startedAtIso: String?,
        val endedAtIso: String?,
    )

    /**
     * QR合計/履歴の絞り込み条件：
     * - inspection_id（サーバ計画ID）
     * - registered_at が生産開始〜終了（未終了時は現在時刻まで）
     */
    private fun buildQrScanQueryScope(endMsOverride: Long? = null): QrScanQueryScope? {
        val state = _uiState.value
        val productCd = state.selectedProductCode?.trim().orEmpty()
        if (productCd.isEmpty()) return null
        val planId = state.activePlanId
        val inspectionId = planId?.takeIf { it > 0 && !offlineStore.isLocalPlanId(it) }
        val session = planId?.let { sessions[it] }
        val startMs = when {
            session != null -> resolveSessionWallStartMs(session, planId)
            else -> null
        } ?: session?.wallStart
        val endMs = endMsOverride
            ?: session?.wallEnd
            ?: System.currentTimeMillis()
        return QrScanQueryScope(
            productionDay = state.productionDay,
            productCd = productCd,
            inspectionId = inspectionId,
            startedAtIso = startMs?.let { Instant.ofEpochMilli(it).toString() },
            endedAtIso = if (startMs != null) Instant.ofEpochMilli(endMs).toString() else null,
        )
    }

    private suspend fun refreshEndDialogScanSummary() {
        val state = _uiState.value
        val productCd = state.selectedProductCode ?: return
        if (!networkMonitor.checkOnline()) {
            _uiState.update {
                it.copy(
                    endDialogScanCount = sessionScanCount,
                    endDialogScanBoxTotal = sessionScanBoxTotal,
                    endDialogScanPieceTotal = sessionScanPieceTotal,
                )
            }
            return
        }
        val scope = buildQrScanQueryScope(endMsOverride = System.currentTimeMillis()) ?: return
        // サーバ計画が未確定の場合はローカル累計を表示
        if (scope.inspectionId == null || scope.startedAtIso == null) {
            _uiState.update {
                it.copy(
                    endDialogScanCount = sessionScanCount,
                    endDialogScanBoxTotal = sessionScanBoxTotal,
                    endDialogScanPieceTotal = sessionScanPieceTotal,
                )
            }
            return
        }
        runCatching {
            repository.loadQrScanSummary(
                productionDay = scope.productionDay,
                productCd = scope.productCd,
                inspectionId = scope.inspectionId,
                startedAt = scope.startedAtIso,
                endedAt = scope.endedAtIso,
            )
        }.onSuccess { summary ->
            val boxTotal = (summary.boxQtyTotal ?: 0).coerceAtLeast(0)
            val pieceTotal = (summary.pieceQtyTotal ?: 0).coerceAtLeast(0)
            val scanCount = (summary.scanCount ?: 0).coerceAtLeast(0)
            sessionScanBoxTotal = boxTotal
            sessionScanPieceTotal = pieceTotal
            sessionScanCount = scanCount
            _uiState.update { ui ->
                val nextBoxes = if (ui.endDialogBoxes.isBlank() && boxTotal > 0) boxTotal.toString() else ui.endDialogBoxes
                val nextPiece = if (ui.endDialogPieceQty.isBlank() && pieceTotal > 0) pieceTotal.toString() else ui.endDialogPieceQty
                ui.copy(
                    endDialogScanCount = scanCount,
                    endDialogScanBoxTotal = boxTotal,
                    endDialogScanPieceTotal = pieceTotal,
                    endDialogBoxes = nextBoxes,
                    endDialogPieceQty = nextPiece,
                    endDialogQtyInputSource = when {
                        nextBoxes.isNotBlank() && ui.endDialogBoxes.isBlank() -> EndDialogQtyInputSource.Box
                        nextPiece.isNotBlank() && ui.endDialogPieceQty.isBlank() -> EndDialogQtyInputSource.Piece
                        else -> ui.endDialogQtyInputSource
                    },
                )
            }
            publishEndDialogQtyState()
        }
    }

    private suspend fun hydrateSessionScanTotals(productCd: String?) {
        val code = productCd?.trim().orEmpty()
        if (code.isEmpty()) return
        if (!networkMonitor.checkOnline()) return
        val scope = buildQrScanQueryScope() ?: return
        if (scope.inspectionId == null || scope.startedAtIso == null) return
        runCatching {
            repository.loadQrScanSummary(
                productionDay = scope.productionDay,
                productCd = code,
                inspectionId = scope.inspectionId,
                startedAt = scope.startedAtIso,
                endedAt = scope.endedAtIso,
            )
        }.onSuccess { summary ->
            sessionScanCount = (summary.scanCount ?: 0).coerceAtLeast(0)
            sessionScanBoxTotal = (summary.boxQtyTotal ?: 0).coerceAtLeast(0)
            sessionScanPieceTotal = (summary.pieceQtyTotal ?: 0).coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    endDialogScanCount = sessionScanCount,
                    endDialogScanBoxTotal = sessionScanBoxTotal,
                    endDialogScanPieceTotal = sessionScanPieceTotal,
                )
            }
        }
    }

    private fun resolveProductCodeFromNextAssignment(products: List<ErpProductDto>): String? {
        val assignment = myNextAssignment ?: return null
        val cd = assignment.nextProductCd?.trim().orEmpty()
        val name = assignment.nextProductName?.trim().orEmpty()
        if (cd.isNotEmpty()) {
            val exact = products.find { (it.productCode ?: "").trim() == cd }
            if (!exact?.productCode.isNullOrBlank()) return exact.productCode.trim()
        }
        if (name.isNotEmpty()) {
            val byName = products.filter { (it.productName ?: "").trim() == name }
            if (byName.size == 1 && !byName[0].productCode.isNullOrBlank()) {
                return byName[0].productCode.trim()
            }
            if (byName.size > 1 && cd.isNotEmpty()) {
                val withCd = byName.find { (it.productCode ?: "").trim() == cd }
                if (!withCd?.productCode.isNullOrBlank()) return withCd.productCode.trim()
            }
        }
        return null
    }

    private fun pieceQtyFromBoxes(boxes: Int, unitPerBox: Int): Int = boxes * unitPerBox

    private fun boxQtyFromPieces(pieces: Int, unitPerBox: Int): Int =
        kotlin.math.round(pieces.toDouble() / unitPerBox).toInt()

    private fun hasPieceBoxQtyMismatch(pieceQty: Int, unitPerBox: Int): Boolean =
        unitPerBox > 0 && pieceQty % unitPerBox != 0

    private fun resolveEndDialogQtyMismatch(pieceQtyRaw: String, unitPerBox: Int): EndDialogQtyMismatch? {
        if (unitPerBox <= 0) return null
        val trimmed = pieceQtyRaw.trim()
        if (trimmed.isEmpty()) return null
        val piece = trimmed.toIntOrNull() ?: return null
        if (piece < 0) return null
        if (!hasPieceBoxQtyMismatch(piece, unitPerBox)) return null
        return EndDialogQtyMismatch(piece, unitPerBox)
    }

    private fun endDialogCanSubmit(boxesRaw: String, pieceQtyRaw: String, unitPerBox: Int): Boolean {
        if (unitPerBox > 0) {
            val piece = pieceQtyRaw.trim().toIntOrNull() ?: return false
            if (piece < 0) return false
            return boxesRaw.trim().isNotEmpty() || pieceQtyRaw.trim().isNotEmpty()
        }
        val raw = pieceQtyRaw.trim()
        if (raw.isEmpty()) return false
        val qty = raw.toIntOrNull() ?: return false
        return qty >= 0
    }

    companion object {
        private const val MES_CHECKPOINT_INTERVAL_MS = 5_000L
        private const val PERSISTENT_SCAN_COOLDOWN_MS = 60_000L
        private const val PERSISTENT_SCAN_DEBOUNCE_MS = 2_000L
        private const val QR_SCAN_SUCCESS_BANNER_MS = 15_000L
        private const val QR_SCAN_COOLDOWN_NOTICE_MS = 5_000L
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
                        items = list.mapNotNull { item ->
                            val id = item.defectCd?.trim().orEmpty()
                            if (id.isEmpty()) return@mapNotNull null
                            DefectItemUi(
                                id = id,
                                label = item.defectName?.trim().orEmpty().ifEmpty { id },
                            )
                        },
                    )
                }
        }
    }

    class Factory(
        private val repository: InspectionRepository,
        private val offlineStore: InspectionOfflineStore,
        private val networkMonitor: NetworkMonitor,
        private val sessionStore: SessionStore,
        private val defaultApiBaseUrl: String,
        private val canMesEdit: Boolean,
        private val userId: Int,
        private val inspectorLabel: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InspectionActualViewModel(
                repository,
                offlineStore,
                networkMonitor,
                sessionStore,
                defaultApiBaseUrl,
                canMesEdit,
                userId,
                inspectorLabel,
            ) as T
        }
    }
}
