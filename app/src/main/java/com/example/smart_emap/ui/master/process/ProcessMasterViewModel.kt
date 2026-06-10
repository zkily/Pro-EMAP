package com.example.smart_emap.ui.master.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.MasterProcessDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

val PROCESS_CATEGORIES = listOf(
    "cut" to "切断",
    "chamfer" to "面取",
    "swaging" to "SW",
    "forming" to "成型",
    "plating" to "メッキ",
    "weld" to "溶接",
    "inspect" to "検査",
    "warehouse" to "倉庫",
)

val PROCESS_CAPACITY_UNITS = listOf("pcs", "kg", "m")

fun processCategoryLabel(value: String?): String =
    PROCESS_CATEGORIES.find { it.first == value }?.second ?: value.orEmpty().ifBlank { "—" }

fun processDefaultFormValues(): Map<String, String> = mapOf(
    "process_cd" to "",
    "process_name" to "",
    "short_name" to "",
    "category" to "",
    "is_outsource" to "false",
    "default_cycle_sec" to "0",
    "default_yield_percent" to "100",
    "capacity_unit" to "pcs",
    "remark" to "",
)

fun MasterProcessDto.toProcessFormValues(): Map<String, String> = mapOf(
    "process_cd" to processCd.orEmpty(),
    "process_name" to processName.orEmpty(),
    "short_name" to shortName.orEmpty(),
    "category" to category.orEmpty(),
    "is_outsource" to if (isOutsource == true) "true" else "false",
    "default_cycle_sec" to (defaultCycleSec ?: 0.0).toString(),
    "default_yield_percent" to String.format(
        Locale.JAPAN,
        "%.1f",
        (defaultYield ?: 1.0) * 100.0,
    ),
    "capacity_unit" to capacityUnit.orEmpty().ifBlank { "pcs" },
    "remark" to remark.orEmpty(),
)

fun formatProcessYieldPercent(value: Double?): String {
    val pct = (value ?: 1.0) * 100.0
    return String.format(Locale.JAPAN, "%.1f", pct)
}

fun formatProcessCycleSec(value: Double?): String {
    if (value == null) return "0"
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(Locale.JAPAN, "%.2f", value)
}

data class ProcessMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val total: Int = 0,
    val processes: List<MasterProcessDto> = emptyList(),
    val showForm: Boolean = false,
    val editingProcess: MasterProcessDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
)

class ProcessMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProcessMasterUiState())
    val uiState: StateFlow<ProcessMasterUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val (processes, total) = repository.loadProcesses(_uiState.value.keyword)
                _uiState.update { it.copy(isLoading = false, processes = processes, total = total) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
    }

    fun search() = refreshAll()

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "") }
        refreshAll()
    }

    fun hasActiveFilters(): Boolean = _uiState.value.keyword.isNotBlank()

    fun openCreate() {
        _uiState.update {
            it.copy(showForm = true, editingProcess = null, formValues = processDefaultFormValues())
        }
    }

    fun openEdit(process: MasterProcessDto) {
        _uiState.update {
            it.copy(showForm = true, editingProcess = process, formValues = process.toProcessFormValues())
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingProcess = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching {
                repository.deleteProcessMaster(id)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.formValues["process_cd"].orEmpty().trim()
        val name = state.formValues["process_name"].orEmpty().trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "工程コードを入力してください") }
                return
            }
            name.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "工程名称を入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveProcessMaster(state.editingProcess?.id, state.formValues)
                if (ok) {
                    val isEdit = state.editingProcess != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingProcess = null,
                            snackbarMessage = if (isEdit) "更新しました" else "登録しました",
                        )
                    }
                    refreshAll()
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存に失敗しました") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました") }
            }
        }
    }

    fun printQrCodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val processes = repository.loadAllProcessesForQr()
                if (processes.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "印刷する工程がありません") }
                    return@runCatching
                }
                val qrItems = buildProcessQrPrintItems(processes)
                if (qrItems.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "QRコードの生成に失敗しました") }
                    return@runCatching
                }
                val html = buildProcessQrPrintHtml(qrItems)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = "工程QRコード印刷",
                        pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
                        snackbarMessage = "${qrItems.size}件のQRコードを生成しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "QRコードの生成に失敗しました") }
            }
        }
    }

    fun clearPendingPrintHtml() {
        _uiState.update {
            it.copy(pendingPrintHtml = null, pendingPrintSubject = null, pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE)
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProcessMasterViewModel(repository) as T
    }
}
