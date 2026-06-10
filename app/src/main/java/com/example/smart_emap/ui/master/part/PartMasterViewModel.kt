package com.example.smart_emap.ui.master.part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.MasterPartDto
import com.example.smart_emap.data.model.PartCsvExportItemDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val PART_SETTLEMENT_TYPES = listOf("有償支給", "無償支給", "自給", "その他")
val PART_KINDS = listOf("T", "N", "F")
val PART_CURRENCIES = listOf("JPY", "USD", "EUR", "CNY", "VND")

fun partDefaultFormValues(): Map<String, String> = mapOf(
    "part_cd" to "",
    "part_name" to "",
    "category" to "",
    "kind" to "N",
    "settlement_type" to "有償支給",
    "uom" to "個",
    "unit_price" to "0",
    "material_unit_price" to "0",
    "currency" to "JPY",
    "exchange_rate" to "1",
    "supplier_cd" to "",
    "status" to "1",
    "remarks" to "",
)

fun MasterPartDto.toPartFormValues(): Map<String, String> = mapOf(
    "part_cd" to partCd.orEmpty(),
    "part_name" to partName.orEmpty(),
    "category" to category.orEmpty(),
    "kind" to kind.orEmpty().ifBlank { "N" },
    "settlement_type" to settlementType.orEmpty().ifBlank { "有償支給" },
    "uom" to uom.orEmpty().ifBlank { "個" },
    "unit_price" to (unitPrice ?: 0.0).toString(),
    "material_unit_price" to (materialUnitPrice ?: 0.0).toString(),
    "currency" to currency.orEmpty().ifBlank { "JPY" },
    "exchange_rate" to (exchangeRate ?: 1.0).toString(),
    "supplier_cd" to supplierCd.orEmpty(),
    "status" to (status ?: 1).toString(),
    "remarks" to remarks.orEmpty(),
)

fun calcPartPreviewJpy(unitPrice: String, materialUnitPrice: String, exchangeRate: String): Double {
    val u = unitPrice.toDoubleOrNull() ?: 0.0
    val m = materialUnitPrice.toDoubleOrNull() ?: 0.0
    val ex = exchangeRate.toDoubleOrNull() ?: 0.0
    return if (ex > 0) u * ex + m else u + m
}

data class PartMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val statusFilter: String = "",
    val page: Int = 1,
    val total: Int = 0,
    val parts: List<MasterPartDto> = emptyList(),
    val supplierOptions: List<Pair<String, String>> = emptyList(),
    val showForm: Boolean = false,
    val editingPart: MasterPartDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    val pendingCsvContent: String? = null,
)

class PartMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    companion object {
        const val PAGE_SIZE = 30
    }

    private val _uiState = MutableStateFlow(PartMasterUiState())
    val uiState: StateFlow<PartMasterUiState> = _uiState.asStateFlow()
    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = if (_uiState.value.supplierOptions.isEmpty()) {
                    repository.loadSupplierOptionsForMaterial()
                } else {
                    _uiState.value.supplierOptions
                }
                val state = _uiState.value
                val status = state.statusFilter.toIntOrNull()
                val (parts, total) = repository.loadParts(state.keyword, status, state.page, PAGE_SIZE)
                _uiState.update {
                    it.copy(isLoading = false, parts = parts, total = total, supplierOptions = suppliers)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
    }

    fun search() {
        _uiState.update { it.copy(page = 1) }
        refreshAll()
    }

    fun setStatusFilter(value: String) {
        _uiState.update { it.copy(statusFilter = value, page = 1) }
        refreshAll()
    }

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", statusFilter = "", page = 1) }
        refreshAll()
    }

    fun hasActiveFilters(): Boolean {
        val s = _uiState.value
        return s.keyword.isNotBlank() || s.statusFilter.isNotBlank()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        refreshAll()
    }

    fun openCreate() {
        _uiState.update {
            it.copy(showForm = true, editingPart = null, formValues = partDefaultFormValues())
        }
    }

    fun openEdit(part: MasterPartDto) {
        val id = part.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val detail = repository.loadPartById(id) ?: part
            _uiState.update {
                it.copy(actionLoading = false, showForm = true, editingPart = detail, formValues = detail.toPartFormValues())
            }
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { state ->
            var values = state.formValues + (key to value)
            if (key == "currency" && value == "JPY") {
                values = values + ("exchange_rate" to "1")
            }
            state.copy(formValues = values)
        }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingPart = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching {
                repository.deletePartMaster(id)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.formValues["part_cd"].orEmpty().trim()
        val name = state.formValues["part_name"].orEmpty().trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "部品CDを入力してください") }
                return
            }
            name.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "部品名を入力してください") }
                return
            }
            state.formValues["kind"].isNullOrBlank() -> {
                _uiState.update { it.copy(snackbarMessage = "区分を選択してください") }
                return
            }
            state.formValues["settlement_type"].isNullOrBlank() -> {
                _uiState.update { it.copy(snackbarMessage = "決済種類を選択してください") }
                return
            }
            (state.formValues["exchange_rate"]?.toDoubleOrNull() ?: 0.0) <= 0 -> {
                _uiState.update { it.copy(snackbarMessage = "為替レートを入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.savePartMaster(state.editingPart?.id, state.formValues)
                if (ok) {
                    val isEdit = state.editingPart != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingPart = null,
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

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val status = _uiState.value.statusFilter.toIntOrNull()
                val (all, _) = repository.loadParts(_uiState.value.keyword, status, 1, 10000)
                if (all.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "出力する部品がありません") }
                    return@runCatching
                }
                val items = all.map { PartCsvExportItemDto(partCd = it.partCd, partName = it.partName) }
                val csv = repository.exportPartMasterCsv(items)
                _uiState.update {
                    it.copy(actionLoading = false, pendingCsvContent = csv, snackbarMessage = "${all.size}件のCSVを生成しました")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "CSV出力に失敗しました") }
            }
        }
    }

    fun printQrCodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val parts = repository.loadAllPartsForQr()
                if (parts.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "印刷する部品がありません") }
                    return@runCatching
                }
                val qrItems = buildPartQrPrintItems(parts)
                if (qrItems.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "QRコードの生成に失敗しました") }
                    return@runCatching
                }
                val html = buildPartQrPrintHtml(qrItems)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = "部品QRコード印刷",
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

    fun clearPendingCsv() = _uiState.update { it.copy(pendingCsvContent = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PartMasterViewModel(repository) as T
    }
}
