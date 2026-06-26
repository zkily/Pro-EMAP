package com.example.smart_emap.ui.mes.inspectionregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.PatchInspectionBody
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.InspectionPatchException
import com.example.smart_emap.data.repository.InspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class InspectionManualRegistrationUiState(
    val productionDay: String = MesCalendarUtils.jstToday(),
    val inspectorUserId: Int? = null,
    val inspectorFilterId: Int? = null,
    val listPage: Int = 1,
    val productCd: String = "",
    val productName: String = "",
    val boxQtyText: String = "",
    val pieceQtyText: String = "",
    val qtyInputSource: String? = null,
    val startedAtText: String = "",
    val endedAtText: String = "",
    val breakMinText: String = "",
    val stopMinText: String = "",
    val registrationNote: String = "",
    val defects: Map<String, Int> = emptyMap(),
    val editingRowId: Int? = null,
    val rows: List<InspectionManagementRowDto> = emptyList(),
    val products: List<ErpProductDto> = emptyList(),
    val inspectors: List<UserListItemDto> = emptyList(),
    val defectItems: List<ProcessDefectItemDto> = emptyList(),
    val defectGroups: List<InspectionManualRegistrationLogic.DefectGroup> = emptyList(),
    val isLoadingRows: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val isLoadingInspectors: Boolean = false,
    val isLoadingDefects: Boolean = false,
    val isSaving: Boolean = false,
    val deletingRowId: Int? = null,
    val snackbarMessage: String? = null,
    val confirmDeleteRow: InspectionManagementRowDto? = null,
    val confirmQtyMismatch: Boolean = false,
    val productionSequence: Int? = null,
) {
    companion object {
        const val LIST_PAGE_SIZE = 10
    }

    val isEdit: Boolean get() = editingRowId != null
    val inspectorSelected: Boolean get() = (inspectorUserId ?: 0) > 0
    val productSelected: Boolean get() = isEdit || productCd.isNotBlank()
    val filteredRows: List<InspectionManagementRowDto>
        get() {
            val fid = inspectorFilterId ?: return rows
            return rows.filter { it.mesInspectorUserId == fid }
        }
    val listTotalPages: Int
        get() {
            val total = filteredRows.size
            return maxOf(1, (total + LIST_PAGE_SIZE - 1) / LIST_PAGE_SIZE)
        }
    val pagedFilteredRows: List<InspectionManagementRowDto>
        get() {
            val page = listPage.coerceIn(1, listTotalPages)
            val start = (page - 1) * LIST_PAGE_SIZE
            return filteredRows.drop(start).take(LIST_PAGE_SIZE)
        }
    val listSummary: InspectionManualRegistrationLogic.ListSummary
        get() = InspectionManualRegistrationLogic.buildListSummary(filteredRows)
    val unitPerBox: Int
        get() {
            val code = productCd.trim()
            if (code.isEmpty()) return 0
            return products.firstOrNull { it.normalizedCode() == code }?.unitPerBox?.coerceAtLeast(0) ?: 0
        }
    val totalDefects: Int get() = defects.values.sumOf { it.coerceAtLeast(0) }
    val breakMin: Int
        get() = InspectionManualRegistrationLogic.parseQtyInput(breakMinText)?.coerceIn(0, 999) ?: 0
    val stopMin: Int
        get() = InspectionManualRegistrationLogic.parseQtyInput(stopMinText)?.coerceIn(0, 999) ?: 0
    val timeSummary: InspectionManualRegistrationLogic.TimeSummary
        get() = InspectionManualRegistrationLogic.buildTimeSummary(
            productionDay = productionDay,
            startedAtText = startedAtText,
            endedAtText = endedAtText,
            breakMin = breakMin,
            stopMin = stopMin,
        )
    val qtyMismatch: InspectionManualRegistrationLogic.QtyMismatchInfo?
        get() = InspectionManualRegistrationLogic.buildQtyMismatch(pieceQtyText, unitPerBox)
}

class InspectionManualRegistrationViewModel(
    private val repository: InspectionRepository,
    private val loggedInUserId: Int?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionManualRegistrationUiState())
    val uiState: StateFlow<InspectionManualRegistrationUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            loadProducts()
            loadInspectors()
            loadDefectItems()
            loadRows()
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun setProductionDay(day: String) {
        val normalized = day.trim().take(10)
        _uiState.update { it.copy(productionDay = normalized, listPage = 1) }
        loadRows()
    }

    fun shiftProductionDay(delta: Int) {
        val current = _uiState.value.productionDay
        val next = if (MesCalendarUtils.normalizeProductionDayKey(current) == "—") {
            MesCalendarUtils.jstToday()
        } else {
            MesCalendarUtils.shiftDateYmd(current, delta)
        }
        setProductionDay(next)
    }

    fun goProductionDayToday() = setProductionDay(MesCalendarUtils.jstToday())

    fun setInspectorUserId(id: Int?) {
        val state = _uiState.value
        if (state.editingRowId != null) {
            _uiState.update { it.copy(inspectorUserId = id) }
            return
        }
        val clearedProduct = if (id == null || id <= 0) {
            state.copy(
                inspectorUserId = id,
                productCd = "",
                productName = "",
                boxQtyText = "",
                pieceQtyText = "",
                qtyInputSource = null,
            )
        } else {
            state.copy(inspectorUserId = id)
        }
        _uiState.update { clearedProduct }
    }

    fun setInspectorFilterId(id: Int?) {
        _uiState.update { it.copy(inspectorFilterId = id, listPage = 1) }
    }

    fun setListPage(page: Int) {
        _uiState.update { state ->
            val p = page.coerceIn(1, state.listTotalPages)
            state.copy(listPage = p)
        }
    }

    fun prevListPage() = setListPage(_uiState.value.listPage - 1)

    fun nextListPage() = setListPage(_uiState.value.listPage + 1)

    fun onProductSelected(productCd: String) {
        val product = _uiState.value.products.firstOrNull { it.normalizedCode() == productCd.trim() }
        _uiState.update {
            it.copy(
                productCd = productCd.trim(),
                productName = product?.normalizedName().orEmpty(),
                boxQtyText = "",
                pieceQtyText = "",
                qtyInputSource = null,
            )
        }
    }

    fun onBoxQtyInput(raw: String) {
        val qty = InspectionManualRegistrationLogic.parseQtyInput(raw)
        val upb = _uiState.value.unitPerBox
        _uiState.update {
            if (qty == null) {
                it.copy(boxQtyText = "", pieceQtyText = "", qtyInputSource = null)
            } else {
                it.copy(
                    boxQtyText = qty.toString(),
                    pieceQtyText = if (upb > 0) InspectionManualRegistrationLogic.pieceQtyFromBoxes(qty, upb).toString() else "",
                    qtyInputSource = "box",
                )
            }
        }
    }

    fun onPieceQtyInput(raw: String) {
        val qty = InspectionManualRegistrationLogic.parseQtyInput(raw)
        val upb = _uiState.value.unitPerBox
        _uiState.update {
            if (qty == null) {
                it.copy(pieceQtyText = "", boxQtyText = "", qtyInputSource = null)
            } else {
                it.copy(
                    pieceQtyText = qty.toString(),
                    boxQtyText = if (upb > 0) InspectionManualRegistrationLogic.boxQtyFromPieces(qty, upb).toString() else "",
                    qtyInputSource = "piece",
                )
            }
        }
    }

    fun onStartedAtInput(raw: String) {
        _uiState.update { it.copy(startedAtText = InspectionManualRegistrationLogic.sanitizeTimeDraft(raw)) }
    }

    fun onEndedAtInput(raw: String) {
        _uiState.update { it.copy(endedAtText = InspectionManualRegistrationLogic.sanitizeTimeDraft(raw)) }
    }

    fun onStartedAtBlur() {
        val parsed = InspectionManualRegistrationLogic.parseTimeInput(_uiState.value.startedAtText)
        _uiState.update {
            it.copy(startedAtText = InspectionManualRegistrationLogic.formatTimeDisplay(parsed))
        }
    }

    fun onEndedAtBlur() {
        val parsed = InspectionManualRegistrationLogic.parseTimeInput(_uiState.value.endedAtText)
        _uiState.update {
            it.copy(endedAtText = InspectionManualRegistrationLogic.formatTimeDisplay(parsed))
        }
    }

    fun onBreakMinInput(raw: String) {
        _uiState.update { it.copy(breakMinText = raw.filter { it.isDigit() }.take(3)) }
    }

    fun onStopMinInput(raw: String) {
        _uiState.update { it.copy(stopMinText = raw.filter { it.isDigit() }.take(3)) }
    }

    fun setRegistrationNote(value: String) {
        _uiState.update { it.copy(registrationNote = value.take(500)) }
    }

    fun bumpDefect(defectCd: String, delta: Int) {
        val current = _uiState.value.defects.toMutableMap()
        val next = ((current[defectCd] ?: 0) + delta).coerceAtLeast(0)
        if (next == 0) current.remove(defectCd) else current[defectCd] = next
        _uiState.update { it.copy(defects = current) }
    }

    fun onDefectQtyInput(defectCd: String, raw: String) {
        val qty = InspectionManualRegistrationLogic.parseQtyInput(raw)
        val current = _uiState.value.defects.toMutableMap()
        if (qty == null || qty <= 0) current.remove(defectCd) else current[defectCd] = qty
        _uiState.update { it.copy(defects = current) }
    }

    fun resetForm(preserveInspector: Boolean = false) {
        val inspectorId = if (preserveInspector) _uiState.value.inspectorUserId else null
        _uiState.update {
            InspectionManualRegistrationUiState(
                productionDay = it.productionDay,
                inspectorUserId = inspectorId,
                inspectorFilterId = it.inspectorFilterId,
                products = it.products,
                inspectors = it.inspectors,
                defectItems = it.defectItems,
                defectGroups = it.defectGroups,
                rows = it.rows,
            )
        }
    }

    fun loadRowIntoForm(row: InspectionManagementRowDto) {
        if (InspectionManualRegistrationLogic.isRowMesInProgress(row)) {
            showMessage("MES 生産中の行は手動編集できません")
            return
        }
        val day = (row.productionDay ?: _uiState.value.productionDay).trim().take(10)
        val breakMin = ((row.mesBreakSec ?: 0) / 60.0).let { kotlin.math.round(it).toInt() }.coerceAtLeast(0)
        val stopFromSec = ((row.mesStopSec ?: 0) / 60.0).let { kotlin.math.round(it).toInt() }.coerceAtLeast(0)
        val pausedMin = ((row.mesPausedAccumSec ?: 0) / 60.0).let { kotlin.math.round(it).toInt() }.coerceAtLeast(0)
        val stopMin = if (row.mesStopSec != null) stopFromSec else (pausedMin - breakMin).coerceAtLeast(0)
        val productCd = row.productCd?.trim().orEmpty()
        val pieceQty = row.actualProductionQuantity
        val upb = _uiState.value.products.firstOrNull { it.normalizedCode() == productCd }?.unitPerBox ?: 0
        val boxQty = if (pieceQty != null && upb > 0) {
            InspectionManualRegistrationLogic.boxQtyFromPieces(pieceQty, upb)
        } else null
        val defects = row.mesDefectByItem?.mapValues { it.value.coerceAtLeast(0) }.orEmpty()
        _uiState.update {
            it.copy(
                editingRowId = row.id,
                productionSequence = row.productionSequence,
                productionDay = day,
                inspectorUserId = row.mesInspectorUserId,
                productCd = productCd,
                productName = row.productName?.trim().orEmpty(),
                boxQtyText = boxQty?.toString().orEmpty(),
                pieceQtyText = pieceQty?.toString().orEmpty(),
                qtyInputSource = if (pieceQty != null) "piece" else null,
                defects = defects,
                registrationNote = row.manualRegistrationNote?.trim().orEmpty(),
                startedAtText = InspectionManualRegistrationLogic.formatTimeDisplay(
                    InspectionManualRegistrationLogic.timeOnlyFromIso(row.mesProductionStartedAt),
                ),
                endedAtText = InspectionManualRegistrationLogic.formatTimeDisplay(
                    InspectionManualRegistrationLogic.timeOnlyFromIso(row.mesProductionEndedAt),
                ),
                breakMinText = if (breakMin > 0) breakMin.toString() else "",
                stopMinText = if (stopMin > 0) stopMin.toString() else "",
            )
        }
        setProductionDay(day)
    }

    fun requestDeleteRow(row: InspectionManagementRowDto) {
        _uiState.update { it.copy(confirmDeleteRow = row) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(confirmDeleteRow = null) }
    }

    fun confirmDeleteRow() {
        val row = _uiState.value.confirmDeleteRow ?: return
        _uiState.update { it.copy(confirmDeleteRow = null, deletingRowId = row.id) }
        viewModelScope.launch {
            try {
                repository.deletePlan(row.id ?: return@launch)
                if (_uiState.value.editingRowId == row.id) resetForm()
                showMessage("削除しました")
                loadRows()
            } catch (e: Exception) {
                showMessage(e.message ?: "削除に失敗しました")
            } finally {
                _uiState.update { it.copy(deletingRowId = null) }
            }
        }
    }

    fun submitForm(canSave: Boolean) {
        if (!canSave) {
            showMessage("製造実行の操作権限がありません")
            return
        }
        if (loggedInUserId == null) {
            showMessage("ログイン後に登録してください")
            return
        }
        val state = _uiState.value
        val day = state.productionDay.trim().take(10)
        if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(day)) {
            showMessage("① 生産日を選択してください")
            return
        }
        if ((state.inspectorUserId ?: 0) <= 0) {
            showMessage("② 検査員を選択してください")
            return
        }
        if (!state.isEdit && state.productCd.isBlank()) {
            showMessage("③ 製品名を選択してください")
            return
        }
        val upb = state.unitPerBox
        if (state.qtyInputSource == "box" && upb <= 0) {
            showMessage("この製品の入数が未設定のため、本数を入力してください")
            return
        }
        val pieceQty = InspectionManualRegistrationLogic.parseQtyInput(state.pieceQtyText)
        if (pieceQty == null) {
            showMessage("④ 生産数（本数）を入力してください")
            return
        }
        val startedParsed = InspectionManualRegistrationLogic.parseTimeInput(state.startedAtText)
        val endedParsed = InspectionManualRegistrationLogic.parseTimeInput(state.endedAtText)
        val window = InspectionManualRegistrationLogic.resolveProductionWindow(day, startedParsed, endedParsed)
        val ws = window.startedMs
        val we = window.endedMs
        if (ws == null || we == null) {
            showMessage("⑤ 生産開始・終了時刻を入力してください")
            return
        }
        val breakMin = state.breakMin.coerceAtLeast(0)
        val stopMin = state.stopMin.coerceAtLeast(0)
        val pauseMin = breakMin + stopMin
        val shiftMin = ((we - ws) / 60000.0).toInt()
        if (pauseMin > shiftMin) {
            showMessage("休憩＋停止時間が生産時間を超えています")
            return
        }
        if (upb > 0 && InspectionManualRegistrationLogic.hasPieceBoxQtyMismatch(pieceQty, upb)) {
            _uiState.update { it.copy(confirmQtyMismatch = true) }
            return
        }
        performSave(pieceQty, ws, we, breakMin, stopMin)
    }

    fun dismissQtyMismatch() {
        _uiState.update { it.copy(confirmQtyMismatch = false) }
    }

    fun confirmQtyMismatchSave() {
        _uiState.update { it.copy(confirmQtyMismatch = false) }
        val state = _uiState.value
        val pieceQty = InspectionManualRegistrationLogic.parseQtyInput(state.pieceQtyText) ?: return
        val day = state.productionDay.trim().take(10)
        val startedParsed = InspectionManualRegistrationLogic.parseTimeInput(state.startedAtText)
        val endedParsed = InspectionManualRegistrationLogic.parseTimeInput(state.endedAtText)
        val window = InspectionManualRegistrationLogic.resolveProductionWindow(day, startedParsed, endedParsed)
        val ws = window.startedMs ?: return
        val we = window.endedMs ?: return
        performSave(pieceQty, ws, we, state.breakMin, state.stopMin)
    }

    private fun performSave(pieceQty: Int, ws: Long, we: Long, breakMin: Int, stopMin: Int) {
        val state = _uiState.value
        val day = state.productionDay.trim().take(10)
        val breakSec = breakMin * 60
        val stopSec = stopMin * 60
        val pauseSec = breakSec + stopSec
        val netSec = ((we - ws) / 1000 - pauseSec).toInt().coerceAtLeast(0)
        val defects = InspectionManualRegistrationLogic.mergeDefects(state.defects)
        val note = state.registrationNote.trim().ifEmpty { null }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                var rowId = state.editingRowId
                if (rowId == null) {
                    rowId = repository.createPlan(
                        productionDay = day,
                        productCd = state.productCd.trim(),
                        productName = state.productName.trim().ifEmpty { state.productCd.trim() },
                        inspectorUserId = state.inspectorUserId ?: return@launch,
                        manualRegistrationNote = note,
                        manualRegistration = true,
                    )
                }
                repository.patchPlan(
                    rowId,
                    PatchInspectionBody(
                        productionDay = day,
                        productionSequence = state.productionSequence?.takeIf { state.isEdit },
                        mesInspectorUserId = state.inspectorUserId,
                        productionCompletedCheck = true,
                        manualRegistration = true,
                        manualRegistrationNote = note,
                        mesDefectByItem = defects,
                        actualProductionQuantity = pieceQty,
                        mesProductionStartedAt = Instant.ofEpochMilli(ws).toString(),
                        mesProductionEndedAt = Instant.ofEpochMilli(we).toString(),
                        mesBreakSec = breakSec,
                        mesStopSec = stopSec,
                        mesPausedAccumSec = pauseSec,
                        mesNetProductionSec = netSec,
                        mesProductionIsPaused = 0,
                    ),
                )
                showMessage(if (state.isEdit) "更新しました" else "登録しました")
                setProductionDay(day)
                resetForm(preserveInspector = true)
                loadRows()
            } catch (e: InspectionPatchException) {
                showMessage(e.message ?: "保存に失敗しました")
            } catch (e: Exception) {
                showMessage(e.message ?: "保存に失敗しました")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun loadRows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRows = true) }
            runCatching { repository.loadPlans(_uiState.value.productionDay) }
                .onSuccess { list ->
                    _uiState.update { state ->
                        val sorted = list.sortedWith(rowCompare)
                        val withRows = state.copy(isLoadingRows = false, rows = sorted)
                        withRows.copy(listPage = withRows.listPage.coerceIn(1, withRows.listTotalPages))
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingRows = false) }
                    showMessage(it.message ?: "一覧の取得に失敗しました")
                }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true) }
            runCatching { repository.loadProducts() }
                .onSuccess { list -> _uiState.update { it.copy(isLoadingProducts = false, products = list) } }
                .onFailure { _uiState.update { it.copy(isLoadingProducts = false) } }
        }
    }

    private fun loadInspectors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInspectors = true) }
            runCatching { repository.loadShiageSectionInspectors() }
                .onSuccess { list -> _uiState.update { it.copy(isLoadingInspectors = false, inspectors = list) } }
                .onFailure { _uiState.update { it.copy(isLoadingInspectors = false) } }
        }
    }

    private fun loadDefectItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDefects = true) }
            runCatching { repository.loadDefectItems() }
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoadingDefects = false,
                            defectItems = items,
                            defectGroups = InspectionManualRegistrationLogic.groupDefectItems(items),
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoadingDefects = false) } }
        }
    }

    fun inspectorLabel(userId: Int?): String {
        if (userId == null) return "未選択"
        return _uiState.value.inspectors.find { it.id == userId }?.displayLabel().orEmpty().ifBlank { userId.toString() }
    }

    fun canEditRow(row: InspectionManagementRowDto): Boolean =
        !InspectionManualRegistrationLogic.isRowMesInProgress(row)

    fun canDeleteRow(row: InspectionManagementRowDto): Boolean =
        !InspectionManualRegistrationLogic.isRowMesInProgress(row)

    private fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private val rowCompare =
        Comparator<InspectionManagementRowDto> { a, b ->
            val idA = a.id ?: 0
            val idB = b.id ?: 0
            when {
                idA != idB -> idB.compareTo(idA)
                else -> (b.updatedAt.orEmpty()).compareTo(a.updatedAt.orEmpty())
            }
        }

    class Factory(
        private val repository: InspectionRepository,
        private val loggedInUserId: Int?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InspectionManualRegistrationViewModel::class.java)) {
                return InspectionManualRegistrationViewModel(repository, loggedInUserId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
