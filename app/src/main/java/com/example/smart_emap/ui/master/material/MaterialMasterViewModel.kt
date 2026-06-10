package com.example.smart_emap.ui.master.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.MasterMaterialDto
import com.example.smart_emap.data.model.MaterialCsvExportItemDto
import com.example.smart_emap.data.model.MaterialMasterStatsDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun materialDefaultFormValues(): Map<String, String> = mapOf(
    "material_cd" to "",
    "material_name" to "",
    "material_type" to "",
    "standard_spec" to "",
    "usegae" to "",
    "representative_model" to "",
    "unit" to "",
    "diameter" to "",
    "thickness" to "",
    "length" to "",
    "pieces_per_bundle" to "",
    "long_weight" to "",
    "supply_classification" to "",
    "supplier_cd" to "",
    "unit_price" to "",
    "single_price" to "",
    "safety_stock" to "",
    "lead_time" to "",
    "storage_location" to "",
    "status" to "1",
    "tolerance_range" to "",
    "tolerance_1" to "",
    "tolerance_2" to "",
    "range_value" to "",
    "min_value" to "",
    "max_value" to "",
    "actual_value_1" to "",
    "actual_value_2" to "",
    "actual_value_3" to "",
    "note" to "",
)

fun MasterMaterialDto.toFormValues(): Map<String, String> = mapOf(
    "material_cd" to materialCd.orEmpty(),
    "material_name" to materialName.orEmpty(),
    "material_type" to materialType.orEmpty(),
    "standard_spec" to standardSpec.orEmpty(),
    "usegae" to usegae.orEmpty(),
    "representative_model" to representativeModel.orEmpty(),
    "unit" to unit.orEmpty(),
    "diameter" to diameter?.toString().orEmpty(),
    "thickness" to thickness?.toString().orEmpty(),
    "length" to length?.toString().orEmpty(),
    "pieces_per_bundle" to piecesPerBundle?.toString().orEmpty(),
    "long_weight" to longWeight?.toString().orEmpty(),
    "supply_classification" to supplyClassification.orEmpty(),
    "supplier_cd" to supplierCd.orEmpty(),
    "unit_price" to unitPrice?.toString().orEmpty(),
    "single_price" to singlePrice?.toString().orEmpty(),
    "safety_stock" to safetyStock?.toString().orEmpty(),
    "lead_time" to leadTime?.toString().orEmpty(),
    "storage_location" to storageLocation.orEmpty(),
    "status" to (status ?: 1).toString(),
    "tolerance_range" to toleranceRange.orEmpty(),
    "tolerance_1" to tolerance1?.toString().orEmpty(),
    "tolerance_2" to tolerance2?.toString().orEmpty(),
    "range_value" to rangeValue.orEmpty(),
    "min_value" to minValue?.toString().orEmpty(),
    "max_value" to maxValue?.toString().orEmpty(),
    "actual_value_1" to actualValue1?.toString().orEmpty(),
    "actual_value_2" to actualValue2?.toString().orEmpty(),
    "actual_value_3" to actualValue3?.toString().orEmpty(),
    "note" to note.orEmpty(),
)

data class MaterialMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val statusFilter: String = "",
    val materialType: String = "",
    val supplyClassification: String = "",
    val usage: String = "",
    val storageLocation: String = "",
    val allMaterials: List<MasterMaterialDto> = emptyList(),
    val filteredMaterials: List<MasterMaterialDto> = emptyList(),
    val stats: MaterialMasterStatsDto = MaterialMasterStatsDto(),
    val supplierOptions: List<Pair<String, String>> = emptyList(),
    val snackbarMessage: String? = null,
    val showForm: Boolean = false,
    val editingMaterial: MasterMaterialDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val visibleColumns: Map<String, Boolean> = defaultMaterialVisibleColumns(),
    val showColumnSettings: Boolean = false,
    val columnSettingsDraft: Map<String, Boolean> = defaultMaterialVisibleColumns(),
    val showPrintSettings: Boolean = false,
    val printSettingsDraft: MaterialPrintSettings = MaterialPrintSettings(),
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    val pendingCsvContent: String? = null,
    val statusUpdatingIds: Set<Int> = emptySet(),
)

class MaterialMasterViewModel(
    private val repository: MasterRepository,
    private val columnSettingsStore: MaterialColumnSettingsStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialMasterUiState())
    val uiState: StateFlow<MaterialMasterUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        loadColumnSettings()
        refreshAll()
    }

    private fun loadColumnSettings() {
        viewModelScope.launch {
            runCatching {
                val saved = columnSettingsStore.load()
                _uiState.update { it.copy(visibleColumns = saved, columnSettingsDraft = saved) }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    private suspend fun loadInternal() {
        val suppliers = if (_uiState.value.supplierOptions.isEmpty()) {
            repository.loadSupplierOptionsForMaterial()
        } else _uiState.value.supplierOptions
        val all = repository.loadAllMaterials()
        val stats = MaterialMasterStatsDto(
            total = all.size,
            active = all.count { it.status == 1 },
            inactive = all.count { it.status != 1 },
        )
        _uiState.update { state ->
            val filtered = applyFilters(all, state)
            state.copy(
                isLoading = false,
                allMaterials = all,
                filteredMaterials = filtered,
                stats = stats,
                supplierOptions = suppliers,
            )
        }
    }

    private fun applyFilters(
        all: List<MasterMaterialDto>,
        state: MaterialMasterUiState,
    ): List<MasterMaterialDto> {
        var result = all
        if (state.keyword.isNotBlank()) {
            val keyword = state.keyword.lowercase()
            result = result.filter { row ->
                row.materialCd.orEmpty().lowercase().contains(keyword) ||
                    row.materialName.orEmpty().lowercase().contains(keyword) ||
                    row.standardSpec.orEmpty().lowercase().contains(keyword) ||
                    row.supplierName.orEmpty().lowercase().contains(keyword)
            }
        }
        if (state.statusFilter.isNotBlank()) {
            val status = state.statusFilter.toIntOrNull()
            if (status != null) result = result.filter { it.status == status }
        }
        if (state.materialType.isNotBlank()) {
            result = result.filter { it.materialType == state.materialType }
        }
        if (state.supplyClassification.isNotBlank()) {
            result = result.filter { it.supplyClassification == state.supplyClassification }
        }
        if (state.usage.isNotBlank()) {
            result = result.filter { it.usegae == state.usage }
        }
        if (state.storageLocation.isNotBlank()) {
            val loc = state.storageLocation.lowercase()
            result = result.filter { it.storageLocation.orEmpty().lowercase().contains(loc) }
        }
        return result
    }

    private fun recomputeFiltered() {
        _uiState.update { state ->
            state.copy(filteredMaterials = applyFilters(state.allMaterials, state))
        }
    }

    private fun debounceFilter() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
            recomputeFiltered()
        }
    }

    fun setKeyword(v: String) {
        _uiState.update { it.copy(keyword = v) }
        debounceFilter()
    }

    fun setStatusFilter(v: String) {
        _uiState.update { it.copy(statusFilter = v) }
        recomputeFiltered()
    }

    fun setMaterialType(v: String) {
        _uiState.update { it.copy(materialType = v) }
        recomputeFiltered()
    }

    fun setSupplyClassification(v: String) {
        _uiState.update { it.copy(supplyClassification = v) }
        recomputeFiltered()
    }

    fun setUsage(v: String) {
        _uiState.update { it.copy(usage = v) }
        recomputeFiltered()
    }

    fun setStorageLocation(v: String) {
        _uiState.update { it.copy(storageLocation = v) }
        debounceFilter()
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                keyword = "",
                statusFilter = "",
                materialType = "",
                supplyClassification = "",
                usage = "",
                storageLocation = "",
            )
        }
        recomputeFiltered()
    }

    fun hasActiveFilters(): Boolean {
        val s = _uiState.value
        return s.keyword.isNotBlank() || s.statusFilter.isNotBlank() || s.materialType.isNotBlank() ||
            s.supplyClassification.isNotBlank() || s.usage.isNotBlank() || s.storageLocation.isNotBlank()
    }

    fun openCreate() {
        viewModelScope.launch {
            val nextCd = repository.loadNextMaterialCd()
            _uiState.update {
                it.copy(
                    showForm = true,
                    editingMaterial = null,
                    formValues = materialDefaultFormValues() + ("material_cd" to nextCd),
                )
            }
        }
    }

    fun openEdit(material: MasterMaterialDto) {
        _uiState.update {
            it.copy(
                showForm = true,
                editingMaterial = material,
                formValues = material.toFormValues(),
            )
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingMaterial = null) }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.formValues["material_cd"].orEmpty().trim()
        val name = state.formValues["material_name"].orEmpty().trim()
        val validationError = when {
            cd.isEmpty() -> "材料コードを入力してください"
            cd.length !in 1..20 -> "材料コードは1-20文字で入力してください"
            name.isEmpty() -> "材料名を入力してください"
            name.length !in 1..100 -> "材料名は1-100文字で入力してください"
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(snackbarMessage = validationError) }
            return
        }
        val isEdit = state.editingMaterial != null
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveMaterial(state.editingMaterial?.id, state.formValues)
                if (ok) {
                    val message = if (isEdit) "材料情報を更新しました" else "材料情報を登録しました"
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = isEdit,
                            editingMaterial = if (isEdit) it.editingMaterial else null,
                            snackbarMessage = message,
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

    fun deleteMaterial(material: MasterMaterialDto) {
        val id = material.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.deleteMaterial(id)
                _uiState.update { it.copy(snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun toggleStatus(material: MasterMaterialDto, active: Boolean) {
        val id = material.id ?: return
        val newStatus = if (active) 1 else 0
        viewModelScope.launch {
            _uiState.update { it.copy(statusUpdatingIds = it.statusUpdatingIds + id) }
            runCatching {
                val ok = repository.updateMaterialStatus(material, newStatus)
                if (ok) {
                    _uiState.update { it.copy(snackbarMessage = "状態を更新しました") }
                    refreshAll()
                } else {
                    _uiState.update { it.copy(snackbarMessage = "状態の更新に失敗しました") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "状態の更新に失敗しました") }
            }
            _uiState.update { it.copy(statusUpdatingIds = it.statusUpdatingIds - id) }
        }
    }

    fun openColumnSettings() {
        _uiState.update { it.copy(showColumnSettings = true, columnSettingsDraft = it.visibleColumns) }
    }

    fun closeColumnSettings() {
        _uiState.update { it.copy(showColumnSettings = false) }
    }

    fun toggleColumnSettingsDraft(key: String, visible: Boolean) {
        _uiState.update { it.copy(columnSettingsDraft = it.columnSettingsDraft + (key to visible)) }
    }

    fun selectAllColumnSettingsDraft(select: Boolean) {
        _uiState.update { state ->
            state.copy(columnSettingsDraft = materialOptionalColumnDefinitions.associate { it.key to select })
        }
    }

    fun saveColumnSettings() {
        val draft = _uiState.value.columnSettingsDraft
        viewModelScope.launch {
            runCatching {
                columnSettingsStore.save(draft)
                _uiState.update {
                    it.copy(visibleColumns = draft, showColumnSettings = false, snackbarMessage = "列表示設定を保存しました")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "列設定の保存に失敗しました") }
            }
        }
    }

    fun openPrintSettings() {
        val state = _uiState.value
        if (state.filteredMaterials.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        _uiState.update { it.copy(showPrintSettings = true) }
    }

    fun closePrintSettings() {
        _uiState.update { it.copy(showPrintSettings = false) }
    }

    fun updatePrintSettingsDraft(settings: MaterialPrintSettings) {
        _uiState.update { it.copy(printSettingsDraft = settings) }
    }

    fun confirmPrint() {
        val state = _uiState.value
        if (state.filteredMaterials.isEmpty()) {
            _uiState.update { it.copy(showPrintSettings = false, snackbarMessage = "印刷するデータがありません") }
            return
        }
        val html = buildMaterialListPrintHtml(state.filteredMaterials, state.printSettingsDraft)
        _uiState.update {
            it.copy(
                showPrintSettings = false,
                pendingPrintHtml = html,
                pendingPrintSubject = "材料マスタ一覧",
                pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
            )
        }
    }

    fun printQrCodes() {
        val materials = _uiState.value.filteredMaterials.ifEmpty { _uiState.value.allMaterials }
        if (materials.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷する材料がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val qrItems = buildMaterialQrPrintItems(materials)
                if (qrItems.isEmpty()) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "QRコードの生成に失敗しました") }
                    return@runCatching
                }
                val html = buildMaterialQrPrintHtml(qrItems)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingPrintHtml = html,
                        pendingPrintSubject = "材料QRコード印刷",
                        pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
                        snackbarMessage = "${qrItems.size}件のQRコードを生成しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "QRコードの生成に失敗しました")
                }
            }
        }
    }

    fun calcSinglePrice() {
        val list = _uiState.value.allMaterials
        if (list.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "材料データがありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            var ok = 0
            var err = 0
            runCatching {
                list.forEach { row ->
                    val id = row.id ?: return@forEach
                    val dia = row.diameter ?: 0.0
                    val thick = row.thickness ?: 0.0
                    val len = row.length ?: 0.0
                    val unitPrice = row.unitPrice ?: 0.0
                    if (dia <= 0 || len <= 0) return@forEach
                    val (longWeight, singlePrice) = calcMaterialSinglePrice(dia, thick, len, unitPrice)
                    val fields = row.toFormValues() + mapOf(
                        "long_weight" to longWeight.toString(),
                        "single_price" to singlePrice.toString(),
                    )
                    if (repository.saveMaterial(id, fields)) ok++ else err++
                }
                val msg = if (err > 0) "単価計算: ${ok}件更新、${err}件失敗" else "${ok}件の単価を計算・更新しました"
                if (ok == 0 && err == 0) {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "直径・厚さ・長さが入力された材料がありません") }
                } else {
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = msg) }
                    refreshAll()
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "単価計算に失敗しました") }
            }
        }
    }

    fun exportCsv() {
        val filtered = _uiState.value.filteredMaterials
        if (filtered.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "出力する材料がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val items = filtered.map {
                    MaterialCsvExportItemDto(materialCd = it.materialCd, materialName = it.materialName)
                }
                val csv = repository.exportMaterialMasterCsv(items)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        pendingCsvContent = csv,
                        snackbarMessage = "${filtered.size}件のCSVを生成しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "CSVファイルの出力に失敗しました")
                }
            }
        }
    }

    fun clearPendingPrintHtml() {
        _uiState.update {
            it.copy(
                pendingPrintHtml = null,
                pendingPrintSubject = null,
                pendingPrintLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
            )
        }
    }

    fun clearPendingCsv() = _uiState.update { it.copy(pendingCsvContent = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: MasterRepository,
        private val columnSettingsStore: MaterialColumnSettingsStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialMasterViewModel(repository, columnSettingsStore) as T
    }
}
