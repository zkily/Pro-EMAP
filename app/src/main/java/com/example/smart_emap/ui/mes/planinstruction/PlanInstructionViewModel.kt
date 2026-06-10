package com.example.smart_emap.ui.mes.planinstruction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.MachineWorkTimeConfigBody
import com.example.smart_emap.data.model.MachineWorkTimeConfigDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.PlanInstructionNoteDto
import com.example.smart_emap.data.model.PlanInstructionRecordDto
import com.example.smart_emap.data.repository.PlanInstructionRepository
import com.example.smart_emap.data.repository.MasterRepository
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanCreateLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlanInstructionDialog {
    None,
    Notes,
    SetupPreview,
    EfficiencyUpdate,
    WorkTimeConfig,
    PrintPreview,
}

data class SetupSchedulePreviewMeta(
    val productionDate: String,
    val totalQuantity: Int,
    val currentDateTime: String,
)

data class PlanInstructionUiState(
    val isLoading: Boolean = false,
    val planLoading: Boolean = false,
    val chartLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val selectedDate: String = PlanInstructionLogic.todayIso(),
    val machineFilter: String = "",
    val keyword: String = "",
    val machines: List<MasterMachineFullDto> = emptyList(),
    val planRows: List<PlanInstructionRecordDto> = emptyList(),
    val allPlanRows: List<PlanInstructionRecordDto> = emptyList(),
    val planStats: PlanInstructionStats = PlanInstructionStats(),
    val chartStartDate: String = "",
    val chartEndDate: String = "",
    val chartPoints: List<PlanQtyChartPoint> = emptyList(),
    val notes: List<PlanInstructionNoteDto> = emptyList(),
    val notesCount: Int = 0,
    val notesLoading: Boolean = false,
    val notesSaving: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A5_LANDSCAPE_SINGLE,
    val activeDialog: PlanInstructionDialog = PlanInstructionDialog.None,
    val setupPreviewRows: List<SetupScheduleRow> = emptyList(),
    val setupPreviewMeta: SetupSchedulePreviewMeta? = null,
    val specifiedWorkingDays: Int? = null,
    val efficiencyUpdateStartDate: String = PlanInstructionLogic.todayIso(),
    val workTimeConfigs: List<MachineWorkTimeConfigDto> = emptyList(),
    val remarksDrafts: Map<String, String> = emptyMap(),
    val printPreviewHtml: String? = null,
)

class PlanInstructionViewModel(
    private val repository: PlanInstructionRepository,
    private val masterRepository: MasterRepository,
    val config: PlanInstructionConfig,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanInstructionUiState())
    val uiState: StateFlow<PlanInstructionUiState> = _uiState.asStateFlow()

    private var planRefreshJob: Job? = null
    private val remarksSaveJobs = mutableMapOf<String, Job>()
    private var efficiencyCache: Map<String, Double> = emptyMap()

    init {
        val (chartStart, chartEnd) = PlanInstructionLogic.chartDateRangeDefault()
        val today = PlanInstructionLogic.todayIso()
        _uiState.update {
            it.copy(
                chartStartDate = chartStart,
                chartEndDate = chartEnd,
                specifiedWorkingDays = if (config.showSpecifiedWorkingDays) {
                    ProductionPlanCreateLogic.calcWorkingDaysFallbackFromDate(today)
                } else {
                    null
                },
            )
        }
        if (config.showSpecifiedWorkingDays) {
            refreshSpecifiedWorkingDays(today)
        }
        viewModelScope.launch {
            loadMachines()
            refreshAll()
        }
    }

    private fun refreshSpecifiedWorkingDays(dateIso: String) {
        viewModelScope.launch {
            val days = withContext(Dispatchers.IO) {
                masterRepository.loadScheduledWorkdaysForDateIso(dateIso)
            }
            _uiState.update { state ->
                if (state.selectedDate.take(10) != dateIso.take(10)) return@update state
                state.copy(specifiedWorkingDays = days)
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            coroutineScope {
                val planJob = async { loadPlanData() }
                val chartJob = async { loadChart() }
                val notesJob = async { refreshNotesCount() }
                planJob.await()
                chartJob.await()
                notesJob.await()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadPlanData() {
        viewModelScope.launch { loadPlanDataInternal() }
    }

    fun loadMachines() {
        viewModelScope.launch {
            val machines = repository.loadMachines(config.machineType)
            _uiState.update { it.copy(machines = machines) }
        }
    }

    fun loadChart() {
        viewModelScope.launch { loadChartInternal() }
    }

    fun setDate(value: String) {
        val date = value.trim().take(10).ifBlank { PlanInstructionLogic.todayIso() }
        _uiState.update {
            it.copy(
                selectedDate = date,
                specifiedWorkingDays = if (config.showSpecifiedWorkingDays) {
                    ProductionPlanCreateLogic.calcWorkingDaysFallbackFromDate(date)
                } else {
                    it.specifiedWorkingDays
                },
            )
        }
        if (config.showSpecifiedWorkingDays) {
            refreshSpecifiedWorkingDays(date)
        }
        refreshPlanData()
    }

    fun shiftDate(days: Int) {
        setDate(PlanInstructionLogic.shiftDate(_uiState.value.selectedDate, days))
    }

    fun setMachine(value: String?) {
        _uiState.update { it.copy(machineFilter = value?.trim().orEmpty()) }
        schedulePlanRefresh()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value.trim()) }
        schedulePlanRefresh()
    }

    fun refreshPlanData() {
        viewModelScope.launch { loadPlanDataInternal() }
    }

    fun setChartDateRange(startDate: String, endDate: String) {
        _uiState.update {
            it.copy(
                chartStartDate = startDate.trim().take(10),
                chartEndDate = endDate.trim().take(10),
            )
        }
        loadChart()
    }

    fun setChartMonth(monthOffset: Int) {
        val (start, end) = PlanInstructionLogic.chartMonthRange(monthOffset)
        setChartDateRange(start, end)
    }

    fun setSpecifiedWorkingDays(days: Int?) {
        _uiState.update {
            it.copy(specifiedWorkingDays = days?.coerceIn(1, 31))
        }
    }

    fun stepSpecifiedWorkingDays(delta: Int) {
        _uiState.update { state ->
            val base = state.specifiedWorkingDays
                ?: ProductionPlanCreateLogic.calcWorkingDaysFallbackFromDate(state.selectedDate)
            val next = (base + delta).coerceIn(1, 31)
            state.copy(specifiedWorkingDays = next)
        }
    }

    fun setEfficiencyUpdateStartDate(value: String) {
        _uiState.update { it.copy(efficiencyUpdateStartDate = value.trim().take(10)) }
    }

    fun printInstructions() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val machineNames = resolveMachineNamesForPrint()
                if (machineNames.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "印刷対象の設備がありません",
                        )
                    }
                    return@launch
                }
                val fullData = loadFullPlanDataForPrint()
                val currentDateDisplay = PlanInstructionLogic.formatDisplayDate(PlanInstructionLogic.todayIso())
                val sheets = machineNames.map { machineName ->
                    PlanInstructionSheetParams(
                        machineName = machineName,
                        machineCd = repository.resolveMachineCd(machineName),
                        planRows = fullData,
                    )
                }
                val html = buildCombinedInstructionPrintHtml(
                    config = config,
                    baseDateIso = _uiState.value.selectedDate,
                    currentDateDisplay = currentDateDisplay,
                    sheets = sheets,
                )
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = config.instructionPrintTitle,
                        pendingPrintLayout = PrintPageLayout.A5_LANDSCAPE_SINGLE,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "印刷に失敗しました",
                    )
                }
            }
        }
    }

    fun openPrintPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val machineNames = resolveMachineNamesForPrint()
                if (machineNames.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "プレビュー対象の設備がありません",
                        )
                    }
                    return@launch
                }
                val fullData = loadFullPlanDataForPrint()
                val currentDateDisplay = PlanInstructionLogic.formatDisplayDate(PlanInstructionLogic.todayIso())
                val sheets = machineNames.map { machineName ->
                    PlanInstructionSheetParams(
                        machineName = machineName,
                        machineCd = repository.resolveMachineCd(machineName),
                        planRows = fullData,
                    )
                }
                val html = buildCombinedInstructionPrintHtml(
                    config = config,
                    baseDateIso = _uiState.value.selectedDate,
                    currentDateDisplay = currentDateDisplay,
                    sheets = sheets,
                )
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        printPreviewHtml = html,
                        activeDialog = PlanInstructionDialog.PrintPreview,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "プレビューの生成に失敗しました",
                    )
                }
            }
        }
    }

    fun printSetupSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val data = buildSetupScheduleData()
                if (data.tableRows.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "印刷する計画データがありません",
                        )
                    }
                    return@launch
                }
                val html = buildSetupSchedulePrintHtmlForConfig(data)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = config.setupSchedulePrintTitle,
                        pendingPrintLayout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "段取予定表の印刷に失敗しました",
                    )
                }
            }
        }
    }

    fun openSetupPreview() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val data = buildSetupScheduleData()
                if (data.tableRows.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            snackbarMessage = "表示する計画データがありません",
                        )
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        setupPreviewRows = data.tableRows,
                        setupPreviewMeta = SetupSchedulePreviewMeta(
                            productionDate = data.productionDate,
                            totalQuantity = data.totalQuantity,
                            currentDateTime = data.currentDateTime,
                        ),
                        activeDialog = PlanInstructionDialog.SetupPreview,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "段取予定表の生成に失敗しました",
                    )
                }
            }
        }
    }

    fun printFromSetupPreview(rows: List<SetupScheduleRow>? = null) {
        val meta = _uiState.value.setupPreviewMeta
        val tableRows = rows ?: _uiState.value.setupPreviewRows
        if (meta == null || tableRows.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        val data = SetupScheduleData(
            tableRows = tableRows,
            productionDate = meta.productionDate,
            totalQuantity = meta.totalQuantity,
            currentDateTime = meta.currentDateTime,
        )
        if (config.useWeldingSetupPrint) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        pendingPrintHtml = buildSetupSchedulePrintHtmlForConfig(data),
                        pendingPrintSubject = config.setupSchedulePrintTitle,
                        pendingPrintLayout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    pendingPrintHtml = buildSetupSchedulePrintHtml(config, data),
                    pendingPrintSubject = config.setupSchedulePrintTitle,
                    pendingPrintLayout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
                )
            }
        }
    }

    fun openNotes() {
        _uiState.update { it.copy(activeDialog = PlanInstructionDialog.Notes) }
        refreshNotes()
    }

    fun refreshNotes(showSuccessMessage: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(notesLoading = true) }
            runCatching {
                val notes = repository.loadNotes(config)
                _uiState.update {
                    it.copy(
                        notes = notes,
                        notesCount = notes.size,
                        notesLoading = false,
                        notesSaving = false,
                        snackbarMessage = if (showSuccessMessage) "追加しました" else it.snackbarMessage,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        notesLoading = false,
                        notesSaving = false,
                        snackbarMessage = error.message ?: "メモ（TODO）の取得に失敗しました",
                    )
                }
            }
        }
    }

    fun createNote(content: String, onSuccess: () -> Unit = {}) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "内容を入力してください") }
            return
        }
        if (trimmed.length > 200) {
            _uiState.update { it.copy(snackbarMessage = "content は200文字以内で指定してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(notesSaving = true) }
            runCatching {
                repository.createNote(config, trimmed)
                refreshNotes(showSuccessMessage = true)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        notesSaving = false,
                        snackbarMessage = error.message ?: "追加に失敗しました",
                    )
                }
            }
        }
    }

    fun toggleNoteDone(noteId: Int, done: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(notesSaving = true) }
            runCatching {
                repository.toggleNoteDone(config, noteId, done)
                _uiState.update { state ->
                    state.copy(
                        notes = state.notes.map { note ->
                            if (note.id == noteId) note.copy(isDone = if (done) 1 else 0) else note
                        },
                        notesSaving = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        notesSaving = false,
                        snackbarMessage = error.message ?: "更新に失敗しました",
                    )
                }
                refreshNotes()
            }
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(notesSaving = true) }
            runCatching {
                repository.deleteNote(config, noteId)
                _uiState.update { state ->
                    val updated = state.notes.filter { it.id != noteId }
                    state.copy(
                        notes = updated,
                        notesCount = updated.size,
                        notesSaving = false,
                    )
                }
                refreshNotes()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        notesSaving = false,
                        snackbarMessage = error.message ?: "削除に失敗しました",
                    )
                }
                refreshNotes()
            }
        }
    }

    fun onRemarksChange(row: PlanInstructionRecordDto, value: String) {
        val key = PlanInstructionLogic.rowKey(row)
        _uiState.update { state ->
            state.copy(remarksDrafts = state.remarksDrafts + (key to value))
        }
        remarksSaveJobs[key]?.cancel()
        remarksSaveJobs[key] = viewModelScope.launch {
            delay(1_500)
            saveRemarks(row, showSuccessMessage = false)
        }
    }

    fun saveRemarks(row: PlanInstructionRecordDto, showSuccessMessage: Boolean = true) {
        val key = PlanInstructionLogic.rowKey(row)
        remarksSaveJobs[key]?.cancel()
        remarksSaveJobs.remove(key)

        val remarks = _uiState.value.remarksDrafts[key]
            ?: row.remarks.orEmpty()

        if (!config.persistRemarksToApi) {
            patchPlanRowRemarks(row, remarks)
            if (showSuccessMessage) {
                _uiState.update { it.copy(snackbarMessage = "備考を保存しました") }
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.saveRemarks(config, row.copy(remarks = remarks), remarks)
                patchPlanRowRemarks(row, remarks)
                _uiState.update { state ->
                    state.copy(
                        remarksDrafts = state.remarksDrafts - key,
                        snackbarMessage = if (showSuccessMessage) "備考を保存しました" else state.snackbarMessage,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(snackbarMessage = error.message ?: "備考の保存に失敗しました")
                }
            }
        }
    }

    fun openEfficiencyUpdate() {
        _uiState.update {
            it.copy(
                activeDialog = PlanInstructionDialog.EfficiencyUpdate,
                efficiencyUpdateStartDate = it.selectedDate,
            )
        }
    }

    fun updateEfficiency() {
        val startDate = _uiState.value.efficiencyUpdateStartDate.trim()
        if (startDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "開始日を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.updateEfficiencyAndSetupTime(startDate)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        activeDialog = PlanInstructionDialog.None,
                        snackbarMessage = "能率・段取時間の更新が完了しました",
                    )
                }
                refreshAll()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "更新に失敗しました",
                    )
                }
            }
        }
    }

    fun openWorkTimeConfig() {
        _uiState.update { it.copy(activeDialog = PlanInstructionDialog.WorkTimeConfig) }
        loadWorkTimeConfigs()
    }

    fun loadWorkTimeConfigs(machineCd: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val configs = repository.loadMachineWorkTimeConfigs(machineCd)
                _uiState.update { it.copy(workTimeConfigs = configs, actionLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "設備運行時間の取得に失敗しました",
                    )
                }
            }
        }
    }

    fun createWorkTimeConfig(machineCd: String, machineName: String, timeSlot: String) {
        createWorkTimeConfig(
            MachineWorkTimeConfigBody(
                machineCd = machineCd.trim(),
                machineName = machineName.trim(),
                timeSlot = timeSlot.trim(),
            ),
        )
    }

    fun createWorkTimeConfig(body: MachineWorkTimeConfigBody) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.createMachineWorkTimeConfig(body)
                loadWorkTimeConfigs()
                _uiState.update { it.copy(snackbarMessage = "設備運行時間を保存しました") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "保存に失敗しました",
                    )
                }
            }
        }
    }

    fun deleteWorkTimeConfig(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.deleteMachineWorkTimeConfig(id)
                loadWorkTimeConfigs()
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = error.message ?: "削除に失敗しました",
                    )
                }
            }
        }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                activeDialog = PlanInstructionDialog.None,
                printPreviewHtml = null,
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearPendingPrint() {
        _uiState.update {
            it.copy(
                pendingPrintHtml = null,
                pendingPrintSubject = null,
                pendingPrintLayout = PrintPageLayout.A5_LANDSCAPE_SINGLE,
            )
        }
    }

    fun remarksForRow(row: PlanInstructionRecordDto): String {
        val key = PlanInstructionLogic.rowKey(row)
        return _uiState.value.remarksDrafts[key]
            ?: row.remarks?.trim().orEmpty().ifBlank {
                PlanInstructionLogic.defaultRemarksPlaceholder(row.productName)
            }
    }

    private fun schedulePlanRefresh() {
        planRefreshJob?.cancel()
        planRefreshJob = viewModelScope.launch {
            delay(240)
            loadPlanDataInternal()
        }
    }

    private suspend fun loadPlanDataInternal() {
        val state = _uiState.value
        val date = state.selectedDate
        if (date.isBlank()) return
        _uiState.update { it.copy(planLoading = true) }
        try {
            val records = repository.loadPlanData(
                config = config,
                startDate = date,
                endDate = date,
                machineName = state.machineFilter.ifBlank { null },
                keyword = state.keyword.ifBlank { null },
            )
            val filtered = PlanInstructionLogic.filterPlanRows(
                records = records,
                machineName = state.machineFilter.ifBlank { null },
                keyword = state.keyword.ifBlank { null },
            )
            _uiState.update {
                it.copy(
                    planRows = filtered,
                    planStats = PlanInstructionLogic.calculateStats(filtered),
                    planLoading = false,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    planLoading = false,
                    snackbarMessage = error.message ?: "計画データの取得に失敗しました",
                )
            }
        }
    }

    private suspend fun loadChartInternal() {
        val state = _uiState.value
        if (state.chartStartDate.isBlank() || state.chartEndDate.isBlank()) return
        _uiState.update { it.copy(chartLoading = true) }
        try {
            val records = repository.loadPlanData(
                config = config,
                startDate = state.chartStartDate,
                endDate = state.chartEndDate,
                machineName = state.machineFilter.ifBlank { null },
                keyword = null,
            )
            val filtered = records.filter { !it.productName.isNullOrBlank() }
            val dates = PlanInstructionLogic.enumerateChartDates(state.chartStartDate, state.chartEndDate)
            val points = PlanInstructionLogic.aggregateChartPoints(filtered, dates)
            _uiState.update {
                it.copy(
                    allPlanRows = filtered,
                    chartPoints = points,
                    chartLoading = false,
                )
            }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    chartLoading = false,
                    snackbarMessage = error.message ?: "チャートデータの取得に失敗しました",
                )
            }
        }
    }

    private suspend fun refreshNotesCount() {
        runCatching {
            val notes = repository.loadNotes(config)
            _uiState.update { it.copy(notesCount = notes.size) }
        }
    }

    private suspend fun loadFullPlanDataForPrint(): List<PlanInstructionRecordDto> {
        val state = _uiState.value
        val (startDate, endDate) = PlanInstructionLogic.calculateSmartDateRange(
            baseDateIso = state.selectedDate,
            processName = config.processName,
            machineName = state.machineFilter.ifBlank { null },
            hasProductionOnDate = { date ->
                repository.loadPlanData(
                    config = config,
                    startDate = date,
                    endDate = date,
                    machineName = state.machineFilter.ifBlank { null },
                    keyword = null,
                ).any { (it.quantity ?: 0) > 0 }
            },
        )
        if (startDate.isBlank() || endDate.isBlank()) return emptyList()
        return repository.loadPlanData(
            config = config,
            startDate = startDate,
            endDate = endDate,
            machineName = state.machineFilter.ifBlank { null },
            keyword = state.keyword.ifBlank { null },
        )
    }

    private suspend fun buildSetupScheduleData(): SetupScheduleData {
        ensureEfficiencyCache()
        val fullData = loadFullPlanDataForPrint()
        val state = _uiState.value
        val productionDate = state.selectedDate
        val utilizationMap = loadUtilizationDiffHoursByLineName(repository, config, productionDate)
        val plannedHoursMap = loadPlannedWorkHoursByMachine(repository, state.machines, productionDate)
        return generateSetupScheduleData(
            planData = fullData,
            productionDateIso = productionDate,
            efficiencyCache = efficiencyCache,
            operationVarianceByMachine = { machine ->
                utilizationMap[machine]?.let(::formatOperationVarianceForRow)
            },
            plannedWorkingHoursByMachine = { machine -> plannedHoursMap[machine] },
            kind = config.kind,
        )
    }

    private suspend fun buildSetupSchedulePrintHtmlForConfig(data: SetupScheduleData): String {
        if (!config.useWeldingSetupPrint) {
            return buildSetupSchedulePrintHtml(config, data)
        }
        val state = _uiState.value
        val baselineMonth = PlanInstructionLogic.baselineMonthFromDate(state.selectedDate).orEmpty()
        val (summary, items) = if (baselineMonth.isNotBlank()) {
            repository.loadPlanBaselineComparison(config, baselineMonth, state.specifiedWorkingDays)
        } else {
            null to emptyList()
        }
        val comparison = buildPlanComparisonSummary(summary, items, state.specifiedWorkingDays)
        val varianceRows = loadOperationVarianceRows(repository, config, state.selectedDate)
        return buildWeldingSetupSchedulePrintHtml(config, data, varianceRows, comparison)
    }

    private suspend fun ensureEfficiencyCache() {
        if (efficiencyCache.isNotEmpty()) return
        efficiencyCache = repository.loadEquipmentEfficiency()
            .mapNotNull { row ->
                val machine = row.machinesName?.trim().orEmpty()
                val product = row.productName?.trim().orEmpty()
                if (machine.isBlank() || product.isBlank()) return@mapNotNull null
                "$machine|$product" to (row.efficiencyRate ?: 0.0)
            }
            .toMap()
    }

    private suspend fun resolveMachineNamesForPrint(): List<String> {
        val state = _uiState.value
        val allMachineNames = state.machines
            .mapNotNull { it.machineName?.trim()?.ifEmpty { null } }
            .ifEmpty {
                repository.loadMachines(config.machineType)
                    .mapNotNull { it.machineName?.trim()?.ifEmpty { null } }
            }
        return PlanInstructionLogic.machinesForPrint(
            selectedMachine = state.machineFilter.ifBlank { null },
            allMachines = allMachineNames,
            excluded = config.excludePrintMachines,
        )
    }

    private fun patchPlanRowRemarks(row: PlanInstructionRecordDto, remarks: String) {
        val key = PlanInstructionLogic.rowKey(row)
        val transform: (PlanInstructionRecordDto) -> PlanInstructionRecordDto = { item ->
            if (PlanInstructionLogic.rowKey(item) == key) item.copy(remarks = remarks) else item
        }
        _uiState.update { state ->
            state.copy(
                planRows = state.planRows.map(transform),
                remarksDrafts = state.remarksDrafts - key,
            )
        }
    }

    companion object {
        const val VIEW_MODEL_KEY_FORMING = "plan_instruction_forming"
        const val VIEW_MODEL_KEY_WELDING = "plan_instruction_welding"
    }

    class Factory(
        private val repository: PlanInstructionRepository,
        private val masterRepository: MasterRepository,
        private val config: PlanInstructionConfig,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlanInstructionViewModel::class.java)) {
                return PlanInstructionViewModel(repository, masterRepository, config) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
