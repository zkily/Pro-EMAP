package com.example.smart_emap.ui.mes.cuttinginstruction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.CreateChamferingManagementBody
import com.example.smart_emap.data.model.CreateChamferingPlanBody
import com.example.smart_emap.data.model.CreateInstructionPlanBody
import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.InstructionChamferingPlanRowDto
import com.example.smart_emap.data.model.InstructionChamferingRowDto
import com.example.smart_emap.data.model.InstructionCuttingRowDto
import com.example.smart_emap.data.model.InstructionPlanRowDto
import com.example.smart_emap.data.model.KanbanIssuanceRowDto
import com.example.smart_emap.data.model.MoveChamferingPlanBody
import com.example.smart_emap.data.model.MoveCuttingToBatchBody
import com.example.smart_emap.data.model.MoveFromBatchBody
import com.example.smart_emap.data.model.PatchChamferingBody
import com.example.smart_emap.data.model.PatchChamferingPlanBody
import com.example.smart_emap.data.model.PatchInstructionCuttingBody
import com.example.smart_emap.data.model.PatchInstructionPlanBody
import com.example.smart_emap.data.model.PatchKanbanBody
import com.example.smart_emap.data.model.ProductBatchDetailDto
import com.example.smart_emap.data.model.ReorderChamferingBody
import com.example.smart_emap.data.model.ReorderCuttingBody
import com.example.smart_emap.data.model.SplitChamferingToNextDayBody
import com.example.smart_emap.data.model.SplitCuttingToNextDayBody
import com.example.smart_emap.data.model.UpdateChamferingPlanContentBody
import com.example.smart_emap.data.model.CuttingInstructionNoteDto
import com.example.smart_emap.data.repository.CuttingInstructionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.max
private val JST = ZoneId.of("Asia/Tokyo")
fun instructionToday(): String = LocalDate.now(JST).toString()
fun instructionTomorrow(): String = LocalDate.now(JST).plusDays(1).toString()
fun shiftInstructionDate(date: String, days: Long): String =
    runCatching { LocalDate.parse(date).plusDays(days).toString() }.getOrDefault(date)
fun formatInstructionDate(value: String?): String =
    value?.trim()?.take(10).orEmpty().ifBlank { "-" }
fun formatProductionMonth(value: String?): String =
    value?.trim()?.take(7).orEmpty().ifBlank { "-" }
fun chamferPlanCd(row: InstructionChamferingPlanRowDto): String {
    val mgmt = row.managementCode?.trim().orEmpty()
    return when {
        mgmt.length >= 5 -> mgmt.takeLast(5)
        mgmt.isNotEmpty() -> mgmt
        else -> "-"
    }
}
fun nextWeekdayFrom(dateStr: String): String {
    val base = runCatching { LocalDate.parse(dateStr.trim().take(10)) }.getOrNull() ?: return dateStr
    var day = base.plusDays(1)
    while (day.dayOfWeek == java.time.DayOfWeek.SATURDAY || day.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
        day = day.plusDays(1)
    }
    return day.toString()
}
fun isCuttingProductionDayDueOrOverdue(day: String): Boolean {
    if (day.isBlank() || day == "-") return false
    val today = LocalDate.now(JST).toString()
    return day <= nextWeekdayFrom(today)
}
fun formatUsageCount(value: Double?): String {
    val num = value ?: 1.0
    return String.format("%.1f", num)
}
private fun formatPlanDecimal(value: Double?): String {
    if (value == null) return ""
    return if (kotlin.math.abs(value % 1.0) < 0.0001) value.toLong().toString() else String.format("%.2f", value)
}
private const val OUTSOURCED_CUTTING_MACHINE = "外注切断"

sealed class CuttingInstructionDialog {
    data object None : CuttingInstructionDialog()
    data object MoldingPreInventory : CuttingInstructionDialog()
    data object CuttingDoneList : CuttingInstructionDialog()
    data object ChamferingDoneList : CuttingInstructionDialog()
    data object DataManagement : CuttingInstructionDialog()
    data object Notes : CuttingInstructionDialog()
    data class NewPlan(val isTrial: Boolean, val initial: PlanFormState) : CuttingInstructionDialog()
    data class EditPlan(val planId: Int, val initial: PlanFormState) : CuttingInstructionDialog()
    data class EditUsageCount(val rowId: Int, val isCutting: Boolean, val current: Double) : CuttingInstructionDialog()
    data object SpecifiedDateMaterial : CuttingInstructionDialog()
    data class MoveToCutting(val plan: InstructionPlanRowDto) : CuttingInstructionDialog()
    data class EditCutting(val row: InstructionCuttingRowDto) : CuttingInstructionDialog()
    data class SplitCutting(val row: InstructionCuttingRowDto) : CuttingInstructionDialog()
    data class EditChamfering(val row: InstructionChamferingRowDto) : CuttingInstructionDialog()
    data class SplitChamfering(val row: InstructionChamferingRowDto) : CuttingInstructionDialog()
    data class NewChamferingPlan(val initial: ChamferingPlanFormState) : CuttingInstructionDialog()
    data class NewChamferingInstruction(val initial: ChamferingInstructionFormState) : CuttingInstructionDialog()
    data class EditChamferingPlan(val planId: Int, val initial: ChamferingPlanFormState) : CuttingInstructionDialog()
    data class MoveChamferingPlan(val plan: InstructionChamferingPlanRowDto) : CuttingInstructionDialog()
    data class EditKanban(val row: KanbanIssuanceRowDto) : CuttingInstructionDialog()
    data object ConfirmCuttingActual : CuttingInstructionDialog()
    data object ConfirmChamferingActual : CuttingInstructionDialog()
    data object ConfirmUsageReflection : CuttingInstructionDialog()
    data class ConfirmActualResult(val inserted: Int, val totalQty: Int) : CuttingInstructionDialog()
    data class ConfirmDelete(val message: String, val onConfirm: () -> Unit) : CuttingInstructionDialog()
}
data class UsageSummaryCounts(val total: Int = 0, val reflected: Int = 0, val notReflected: Int = 0)
data class CuttingInstructionUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val activeDialog: CuttingInstructionDialog = CuttingInstructionDialog.None,
    val equipmentFilter: String = "",
    val productNameFilter: String = "",
    val materialNameFilter: String = "",
    val machineOptions: List<Pair<String, String>> = emptyList(),
    val chamferingMachineOptions: List<Pair<String, String>> = emptyList(),
    val allPlans: List<InstructionPlanRowDto> = emptyList(),
    val planPage: Int = 1,
    val planPageSize: Int = 50,
    val selectedPlanId: Int? = null,
    val selectedProductCd: String? = null,
    val productDetail: ProductBatchDetailDto? = null,
    val productDetailLoading: Boolean = false,
    val equipmentEfficiency: List<EquipmentEfficiencyRowDto> = emptyList(),
    val chamferingPlanEfficiency: List<EquipmentEfficiencyRowDto> = emptyList(),
    val chamferingPlanEfficiencyLoading: Boolean = false,
    val cuttingDateToday: String = instructionToday(),
    val cuttingDateTomorrow: String = instructionTomorrow(),
    val cuttingMachineFilter: String = "",
    val cuttingToday: List<InstructionCuttingRowDto> = emptyList(),
    val cuttingTomorrow: List<InstructionCuttingRowDto> = emptyList(),
    val cuttingLoading: Boolean = false,
    val usageSummaryDateToday: String = instructionToday(),
    val usageSummaryDateTomorrow: String = instructionTomorrow(),
    val usageSummaryToday: List<InstructionCuttingRowDto> = emptyList(),
    val usageSummaryTomorrow: List<InstructionCuttingRowDto> = emptyList(),
    val usageSummaryLoading: Boolean = false,
    val reflectedCodesToday: Set<String> = emptySet(),
    val chamferingPlans: List<InstructionChamferingPlanRowDto> = emptyList(),
    val chamferingPlansLoading: Boolean = false,
    val cuttingProductionDayByMgmtCode: Map<String, String> = emptyMap(),
    val cuttingFormingStartDateByMgmtCode: Map<String, String> = emptyMap(),
    val selectedChamferingPlanId: Int? = null,
    val chamferingDateToday: String = instructionToday(),
    val chamferingDateTomorrow: String = instructionTomorrow(),
    val chamferingMachineFilter: String = "",
    val chamferingToday: List<InstructionChamferingRowDto> = emptyList(),
    val chamferingTomorrow: List<InstructionChamferingRowDto> = emptyList(),
    val chamferingLoading: Boolean = false,
    val kanbanDate: String = instructionToday(),
    val kanbanStatus: String = "",
    val kanbanProductName: String = "",
    val kanbanProductOptions: List<String> = emptyList(),
    val kanbanRows: List<KanbanIssuanceRowDto> = emptyList(),
    val selectedKanbanIds: Set<Int> = emptySet(),
    val kanbanLoading: Boolean = false,
    val notes: List<CuttingInstructionNoteDto> = emptyList(),
    val notesCount: Int = 0,
    val notesLoading: Boolean = false,
    val moldingPreInventoryDate: String = instructionToday(),
    val moldingPreInventoryGroups: List<MoldingPreInventoryGroup> = emptyList(),
    val moldingPreInventoryLoading: Boolean = false,
    val cuttingDoneRaw: List<InstructionCuttingRowDto> = emptyList(),
    val cuttingDonePeriodStart: String = "",
    val cuttingDonePeriodEnd: String = "",
    val cuttingDoneProductFilter: String = "",
    val cuttingDoneManagementCodeFilter: String = "",
    val cuttingDoneOnlyCompleted: Boolean = false,
    val chamferingDoneRaw: List<InstructionChamferingRowDto> = emptyList(),
    val chamferingDonePeriodStart: String = "",
    val chamferingDonePeriodEnd: String = "",
    val chamferingDoneProductFilter: String = "",
    val chamferingDoneOnlyCompleted: Boolean = false,
    val doneListPage: Int = 1,
    val doneListPageSize: Int = 50,
    val chamferingProductOptions: List<Pair<String, String>> = emptyList(),
    val chamferingMaterialOptions: List<String> = emptyList(),
    val materialMasterOptions: List<Pair<String, String>> = emptyList(),
    val doneListLoading: Boolean = false,
    val dataManagementMonth: String = "",
    val dataManagementRows: List<InstructionPlanRowDto> = emptyList(),
    val dataManagementLoading: Boolean = false,
    val specifiedMaterialDate: String = instructionToday(),
    val specifiedMaterialRows: List<InstructionCuttingRowDto> = emptyList(),
    val specifiedMaterialLoading: Boolean = false,
)
class CuttingInstructionViewModel(
    private val repository: CuttingInstructionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CuttingInstructionUiState())
    val uiState: StateFlow<CuttingInstructionUiState> = _uiState.asStateFlow()
    val filteredPlans: List<InstructionPlanRowDto>
        get() {
            val state = _uiState.value
            val pn = state.productNameFilter.trim()
            val mn = state.materialNameFilter.trim()
            return state.allPlans.filter { row ->
                (pn.isBlank() || row.productName.orEmpty().trim() == pn) &&
                    (mn.isBlank() || row.materialName.orEmpty().trim() == mn)
            }.sortedWith(compareBy(nullsLast()) { it.startDate?.take(10) })
        }
    val pagedPlans: List<InstructionPlanRowDto>
        get() {
            val list = filteredPlans
            val start = (_uiState.value.planPage - 1) * _uiState.value.planPageSize
            return list.drop(start).take(_uiState.value.planPageSize)
        }
    val planTotalPages: Int
        get() {
            val size = filteredPlans.size
            return max(1, ceil(size.toDouble() / _uiState.value.planPageSize).toInt())
        }
    val productNameOptions: List<String>
        get() = _uiState.value.allPlans.mapNotNull { it.productName?.trim()?.takeIf { n -> n.isNotEmpty() } }.distinct().sorted()
    val materialNameOptions: List<String>
        get() = _uiState.value.allPlans.mapNotNull { it.materialName?.trim()?.takeIf { n -> n.isNotEmpty() } }.distinct().sorted()
    val usageSummaryTodayCounts: UsageSummaryCounts
        get() = computeUsageCounts(_uiState.value.usageSummaryToday, _uiState.value.reflectedCodesToday)
    val usageSummaryTomorrowCounts: UsageSummaryCounts
        get() = computeUsageCounts(_uiState.value.usageSummaryTomorrow, emptySet())
    val cuttingDoneFiltered: List<InstructionCuttingRowDto>
        get() = filterCuttingDoneList(
            _uiState.value.cuttingDoneRaw,
            _uiState.value.cuttingDonePeriodStart,
            _uiState.value.cuttingDonePeriodEnd,
            _uiState.value.cuttingDoneProductFilter,
            _uiState.value.cuttingDoneManagementCodeFilter,
            _uiState.value.cuttingDoneOnlyCompleted,
        )
    val cuttingDonePaged: List<InstructionCuttingRowDto>
        get() {
            val list = cuttingDoneFiltered
            val start = (_uiState.value.doneListPage - 1) * _uiState.value.doneListPageSize
            return list.drop(start).take(_uiState.value.doneListPageSize)
        }
    val cuttingDoneTotalPages: Int
        get() = max(1, ceil(cuttingDoneFiltered.size.toDouble() / _uiState.value.doneListPageSize).toInt())
    val cuttingDoneProductOptions: List<String>
        get() = _uiState.value.cuttingDoneRaw.mapNotNull { it.productName?.trim()?.takeIf { n -> n.isNotEmpty() } }.distinct().sorted()
    val chamferingDoneFiltered: List<InstructionChamferingRowDto>
        get() = filterChamferingDoneList(
            _uiState.value.chamferingDoneRaw,
            _uiState.value.chamferingDonePeriodStart,
            _uiState.value.chamferingDonePeriodEnd,
            _uiState.value.chamferingDoneProductFilter,
            _uiState.value.chamferingDoneOnlyCompleted,
        )
    val chamferingDonePaged: List<InstructionChamferingRowDto>
        get() {
            val list = chamferingDoneFiltered
            val start = (_uiState.value.doneListPage - 1) * _uiState.value.doneListPageSize
            return list.drop(start).take(_uiState.value.doneListPageSize)
        }
    val chamferingDoneTotalPages: Int
        get() = max(1, ceil(chamferingDoneFiltered.size.toDouble() / _uiState.value.doneListPageSize).toInt())
    val chamferingDoneProductOptions: List<String>
        get() = _uiState.value.chamferingDoneRaw.mapNotNull { it.productName?.trim()?.takeIf { n -> n.isNotEmpty() } }.distinct().sorted()
    val dataManagementMonthOptions: List<String>
        get() = _uiState.value.allPlans.mapNotNull { it.productionMonth?.take(7)?.takeIf { m -> m.isNotBlank() } }.distinct().sortedDescending()
    val cuttingMachineChipOptions: List<Pair<String, String>>
        get() = _uiState.value.machineOptions.filter { (name, _) -> name != OUTSOURCED_CUTTING_MACHINE }
    init { refreshAll() }
    fun dismissDialog() = _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None) }
    fun openDialog(dialog: CuttingInstructionDialog) = _uiState.update { it.copy(activeDialog = dialog) }
    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val machines = repository.loadCuttingMachines()
                val chamferMachines = repository.loadChamferingMachines()
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                val notes = repository.loadNotes()
                val kanbanProducts = repository.loadKanbanProductNames()
                val materialMasterOptions = repository.loadMaterialMasterOptions()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        machineOptions = machines,
                        chamferingMachineOptions = chamferMachines,
                        allPlans = plans,
                        notes = notes,
                        notesCount = notes.count { n -> n.isDone != true },
                        kanbanProductOptions = kanbanProducts,
                        materialMasterOptions = materialMasterOptions,
                        chamferingMaterialOptions = materialMasterOptions.map { pair -> pair.first },
                        cuttingMachineFilter = resolveCuttingMachineFilter(it.cuttingMachineFilter, machines),
                        chamferingMachineFilter = it.chamferingMachineFilter,
                        dataManagementMonth = it.dataManagementMonth.ifBlank { plans.firstOrNull()?.productionMonth?.take(7).orEmpty() },
                    )
                }
                loadCuttingLists()
                loadUsageSummary()
                loadChamferingLists()
                loadKanbanList()
                loadChamferingPlans()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }
    private fun computeUsageCounts(rows: List<InstructionCuttingRowDto>, reflectedCodes: Set<String>): UsageSummaryCounts {
        val target = rows.filter { it.useMaterialStockSub != 1 }
        val reflected = target.count { row ->
            row.materialUsageReflected == "反映済" ||
                reflectedCodes.contains(row.managementCode.orEmpty().trim())
        }
        return UsageSummaryCounts(total = target.size, reflected = reflected, notReflected = target.size - reflected)
    }
    private fun <T> filterDoneList(
        raw: List<T>,
        productFilter: String,
        onlyCompleted: Boolean,
        productName: (T) -> String?,
    ): List<T> = raw.filter { row ->
        (productFilter.isBlank() || productName(row).orEmpty() == productFilter) &&
            (!onlyCompleted || when (row) {
                is InstructionCuttingRowDto -> row.productionCompletedCheck == 1
                is InstructionChamferingRowDto -> row.productionCompletedCheck == 1
                else -> true
            })
    }
    private fun filterCuttingDoneList(
        raw: List<InstructionCuttingRowDto>,
        periodStart: String,
        periodEnd: String,
        productFilter: String,
        managementCodeFilter: String,
        onlyCompleted: Boolean,
    ): List<InstructionCuttingRowDto> {
        val mcQ = managementCodeFilter.trim().lowercase()
        return raw.filter { row ->
            val day = row.productionDay.orEmpty().trim().take(10)
            if (periodStart.isNotBlank()) {
                if (day.isBlank() || day < periodStart) return@filter false
            }
            if (periodEnd.isNotBlank() && day.isNotBlank() && day > periodEnd) return@filter false
            if (productFilter.isNotBlank() && row.productName.orEmpty() != productFilter) return@filter false
            if (mcQ.isNotBlank()) {
                val mc = row.managementCode.orEmpty().trim().lowercase()
                if (!mc.contains(mcQ)) return@filter false
            }
            !onlyCompleted || row.productionCompletedCheck == 1
        }
    }
    private fun filterChamferingDoneList(
        raw: List<InstructionChamferingRowDto>,
        periodStart: String,
        periodEnd: String,
        productFilter: String,
        onlyCompleted: Boolean,
    ): List<InstructionChamferingRowDto> = raw.filter { row ->
        val day = row.productionDay.orEmpty().trim().take(10)
        if (periodStart.isNotBlank()) {
            if (day.isBlank() || day < periodStart) return@filter false
        }
        if (periodEnd.isNotBlank() && day.isNotBlank() && day > periodEnd) return@filter false
        (productFilter.isBlank() || row.productName.orEmpty() == productFilter) &&
            (!onlyCompleted || row.productionCompletedCheck == 1)
    }
    private fun sortCuttingDoneList(list: List<InstructionCuttingRowDto>): List<InstructionCuttingRowDto> =
        list.sortedWith(compareByDescending<InstructionCuttingRowDto> { it.productionDay.orEmpty().take(10) }
            .thenByDescending { it.managementCode.orEmpty() })
    private fun sortChamferingDoneList(list: List<InstructionChamferingRowDto>): List<InstructionChamferingRowDto> =
        list.sortedWith(compareByDescending<InstructionChamferingRowDto> { it.productionDay.orEmpty().take(10) }
            .thenByDescending { it.managementCode.orEmpty() })
    fun setEquipmentFilter(value: String) {
        _uiState.update { it.copy(equipmentFilter = value, productNameFilter = "", materialNameFilter = "", planPage = 1) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val plans = repository.loadBatchPlans(value)
                _uiState.update { it.copy(isLoading = false, allPlans = plans) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }
    fun setProductNameFilter(value: String) = _uiState.update { it.copy(productNameFilter = value, planPage = 1) }
    fun setMaterialNameFilter(value: String) = _uiState.update { it.copy(materialNameFilter = value, planPage = 1) }
    fun setPlanPage(page: Int) = _uiState.update { it.copy(planPage = page.coerceIn(1, planTotalPages)) }
    fun selectPlan(row: InstructionPlanRowDto) {
        val cd = row.productCd.orEmpty()
        _uiState.update {
            it.copy(selectedPlanId = row.id, selectedProductCd = cd.ifBlank { null }, productDetail = null, equipmentEfficiency = emptyList(), productDetailLoading = cd.isNotBlank())
        }
        if (cd.isBlank()) return
        viewModelScope.launch {
            val detail = repository.loadProductDetail(cd)
            val efficiency = repository.loadEquipmentEfficiency(cd).filter { e -> e.machinesName.orEmpty().contains("切断") }
            _uiState.update { it.copy(productDetail = detail, equipmentEfficiency = efficiency, productDetailLoading = false) }
        }
    }
    fun selectChamferingPlan(row: InstructionChamferingPlanRowDto) {
        val cd = row.productCd.orEmpty()
        _uiState.update {
            it.copy(
                selectedChamferingPlanId = row.id,
                chamferingPlanEfficiency = emptyList(),
                chamferingPlanEfficiencyLoading = cd.isNotBlank(),
            )
        }
        if (cd.isBlank()) return
        viewModelScope.launch {
            val efficiency = repository.loadEquipmentEfficiency(cd, keyword = cd)
                .filter { e -> e.machinesName.orEmpty().contains("面取") }
            _uiState.update { it.copy(chamferingPlanEfficiency = efficiency, chamferingPlanEfficiencyLoading = false) }
        }
    }
    fun syncLengthsFromProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val msg = repository.syncLengthsFromProducts()
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                _uiState.update { it.copy(actionLoading = false, allPlans = plans, snackbarMessage = msg) }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "同期失敗") }
            }
        }
    }
    fun togglePlanStockSub(row: InstructionPlanRowDto, enabled: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchBatchPlan(id, PatchInstructionPlanBody(useMaterialStockSub = if (enabled) 1 else 0))
                refreshPlanRow(id) { it.copy(useMaterialStockSub = if (enabled) 1 else 0) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun openEditPlan(row: InstructionPlanRowDto) {
        loadMaterialMasterOptionsIfNeeded()
        openDialog(CuttingInstructionDialog.EditPlan(row.id ?: return, row.toPlanFormState()))
    }
    fun openNewPlan(isTrial: Boolean) {
        loadMaterialMasterOptionsIfNeeded()
        val month = LocalDate.now(JST).toString().take(7)
        openDialog(CuttingInstructionDialog.NewPlan(isTrial, PlanFormState(productionMonth = month, productionLine = _uiState.value.equipmentFilter)))
    }
    private fun loadMaterialMasterOptionsIfNeeded() {
        if (_uiState.value.materialMasterOptions.isNotEmpty()) return
        viewModelScope.launch {
            runCatching {
                val options = repository.loadMaterialMasterOptions()
                _uiState.update {
                    it.copy(
                        materialMasterOptions = options,
                        chamferingMaterialOptions = options.map { pair -> pair.first },
                    )
                }
            }
        }
    }
    fun copyPlan(row: InstructionPlanRowDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.createBatchPlan(row.toCreateBody())
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                _uiState.update { it.copy(actionLoading = false, allPlans = plans, snackbarMessage = "複製しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "複製失敗") }
            }
        }
    }
    fun saveNewPlan(isTrial: Boolean, form: PlanFormState) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.createBatchPlan(form.toCreateBody(isTrial))
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                _uiState.update { it.copy(actionLoading = false, allPlans = plans, activeDialog = CuttingInstructionDialog.None, snackbarMessage = "追加しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "追加失敗") }
            }
        }
    }
    fun saveEditPlan(planId: Int, form: PlanFormState) {
        viewModelScope.launch {
            runCatching {
                repository.patchBatchPlan(planId, form.toPatchBody())
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                _uiState.update { it.copy(allPlans = plans, activeDialog = CuttingInstructionDialog.None, snackbarMessage = "更新しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun deletePlan(row: InstructionPlanRowDto) {
        val id = row.id ?: return
        openDialog(CuttingInstructionDialog.ConfirmDelete("このロットを削除しますか？") {
            viewModelScope.launch {
                runCatching {
                    repository.deleteBatchPlan(id)
                    _uiState.update { state ->
                        state.copy(allPlans = state.allPlans.filterNot { it.id == id }, activeDialog = CuttingInstructionDialog.None, snackbarMessage = "削除しました")
                    }
                }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
            }
        })
    }
    fun openMoveToCutting(row: InstructionPlanRowDto) = openDialog(CuttingInstructionDialog.MoveToCutting(row))
    fun confirmMoveToCuttingAdvanced(productionDay: String, cuttingMachine: String) {
        val plan = (_uiState.value.activeDialog as? CuttingInstructionDialog.MoveToCutting)?.plan ?: return
        val id = plan.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.movePlanToCutting(
                    MoveFromBatchBody(
                        planId = id,
                        productionMonth = plan.productionMonth?.take(7),
                        productionLine = plan.productionLine,
                        productCd = plan.productCd,
                        productName = plan.productName,
                        actualProductionQuantity = plan.actualProductionQuantity,
                        materialName = plan.materialName,
                        productionDay = productionDay,
                        priorityOrder = plan.priorityOrder,
                        cuttingMachine = cuttingMachine.ifBlank { null },
                        hasChamferingProcess = plan.hasChamferingProcess,
                    ),
                )
                val plans = repository.loadBatchPlans(_uiState.value.equipmentFilter)
                _uiState.update { it.copy(actionLoading = false, allPlans = plans, activeDialog = CuttingInstructionDialog.None, snackbarMessage = "切断指示へ移行しました") }
                loadCuttingLists()
                loadChamferingPlans()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "移行失敗") }
            }
        }
    }
    fun setCuttingMachineFilter(machine: String) {
        _uiState.update { it.copy(cuttingMachineFilter = machine) }
        loadCuttingLists()
    }
    fun shiftCuttingDateToday(days: Long) {
        _uiState.update { it.copy(cuttingDateToday = shiftInstructionDate(it.cuttingDateToday, days)) }
        loadCuttingLists()
    }
    fun shiftCuttingDateTomorrow(days: Long) {
        _uiState.update { it.copy(cuttingDateTomorrow = shiftInstructionDate(it.cuttingDateTomorrow, days)) }
        loadCuttingLists()
    }
    private fun loadCuttingLists() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(cuttingLoading = true) }
            runCatching {
                val machine = state.cuttingMachineFilter.ifBlank { null }
                val today = repository.loadCuttingManagement(state.cuttingDateToday, machine)
                val tomorrow = repository.loadCuttingManagement(state.cuttingDateTomorrow, machine)
                _uiState.update { it.copy(cuttingToday = today, cuttingTomorrow = tomorrow, cuttingLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(cuttingLoading = false, snackbarMessage = e.message ?: "切断指示取得失敗") }
            }
        }
    }
    fun loadUsageSummary() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(usageSummaryLoading = true) }
            runCatching {
                val today = repository.loadCuttingManagement(state.usageSummaryDateToday, null)
                val tomorrow = repository.loadCuttingManagement(state.usageSummaryDateTomorrow, null)
                val codes = repository.loadReflectedManagementCodes(state.usageSummaryDateToday)
                _uiState.update { it.copy(usageSummaryToday = today, usageSummaryTomorrow = tomorrow, reflectedCodesToday = codes, usageSummaryLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(usageSummaryLoading = false, snackbarMessage = e.message ?: "使用数取得失敗") }
            }
        }
    }
    fun shiftUsageSummaryDateToday(days: Long) {
        _uiState.update { it.copy(usageSummaryDateToday = shiftInstructionDate(it.usageSummaryDateToday, days)) }
        loadUsageSummary()
    }
    fun shiftUsageSummaryDateTomorrow(days: Long) {
        _uiState.update { it.copy(usageSummaryDateTomorrow = shiftInstructionDate(it.usageSummaryDateTomorrow, days)) }
        loadUsageSummary()
    }
    fun toggleUsageSummaryStock(row: InstructionCuttingRowDto, enabled: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchCutting(id, PatchInstructionCuttingBody(useMaterialStockSub = if (enabled) 1 else 0))
                loadUsageSummary()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun openEditUsageCount(row: InstructionCuttingRowDto) {
        val id = row.id ?: return
        openDialog(CuttingInstructionDialog.EditUsageCount(id, isCutting = true, current = row.usageCount ?: 1.0))
    }
    fun saveUsageCount(rowId: Int, isCutting: Boolean, value: Double) {
        viewModelScope.launch {
            runCatching {
                if (isCutting) {
                    repository.patchCutting(rowId, PatchInstructionCuttingBody(usageCount = value))
                    loadUsageSummary()
                    loadCuttingLists()
                }
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "保存しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun toggleCuttingCompleted(row: InstructionCuttingRowDto, completed: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchCutting(id, PatchInstructionCuttingBody(productionCompletedCheck = completed))
                patchCuttingRow(id) { it.copy(productionCompletedCheck = if (completed) 1 else 0) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun openEditCutting(row: InstructionCuttingRowDto) = openDialog(CuttingInstructionDialog.EditCutting(row))
    fun saveCuttingEdit(row: InstructionCuttingRowDto, day: String, machine: String, qty: Int?, defect: Int?, remarks: String?) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchCutting(id, PatchInstructionCuttingBody(
                    productionDay = day.ifBlank { null },
                    cuttingMachine = machine.ifBlank { null },
                    actualProductionQuantity = qty,
                    defectQty = defect,
                    remarks = remarks,
                ))
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "保存しました") }
                loadCuttingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun splitCutting(row: InstructionCuttingRowDto, todayQty: Int) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.splitCuttingToNextDay(id, SplitCuttingToNextDayBody(todayQuantity = todayQty, nextDay = _uiState.value.cuttingDateTomorrow))
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "分割しました") }
                loadCuttingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "分割失敗") } }
        }
    }
    fun reorderCuttingRow(row: InstructionCuttingRowDto, direction: Int) {
        val list = if (row.productionDay?.take(10) == _uiState.value.cuttingDateTomorrow) _uiState.value.cuttingTomorrow else _uiState.value.cuttingToday
        val idx = list.indexOfFirst { it.id == row.id }
        if (idx < 0) return
        val newIdx = idx + direction
        if (newIdx !in list.indices) return
        val reordered = list.toMutableList()
        val item = reordered.removeAt(idx)
        reordered.add(newIdx, item)
        val ids = reordered.mapNotNull { it.id }
        val machine = _uiState.value.cuttingMachineFilter.ifBlank { row.cuttingMachine.orEmpty() }
        viewModelScope.launch {
            runCatching {
                repository.reorderCutting(ReorderCuttingBody(cuttingMachine = machine, orderedIds = ids))
                loadCuttingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "並べ替え失敗") } }
        }
    }
    fun moveCuttingBackToBatch(row: InstructionCuttingRowDto) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.moveCuttingToBatch(
                    MoveCuttingToBatchBody(
                        cuttingId = id,
                        productionMonth = row.productionDay?.take(7) ?: LocalDate.now(JST).toString().take(7),
                        productionLine = row.productionLine.orEmpty(),
                        productCd = row.productCd.orEmpty(),
                        productName = row.productName.orEmpty(),
                        actualProductionQuantity = row.actualProductionQuantity,
                        materialName = row.materialName,
                        managementCode = row.managementCode,
                        productionDay = row.productionDay?.take(10),
                    ),
                )
                _uiState.update { it.copy(snackbarMessage = "ロットへ戻しました") }
                refreshAll()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "戻し失敗") } }
        }
    }
    fun duplicateCuttingRow(row: InstructionCuttingRowDto) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.duplicateCutting(id)
                loadCuttingLists()
                _uiState.update { it.copy(snackbarMessage = "複製しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "複製失敗") } }
        }
    }
    fun deleteCuttingRow(row: InstructionCuttingRowDto) {
        val id = row.id ?: return
        openDialog(CuttingInstructionDialog.ConfirmDelete("切断指示を削除しますか？") {
            viewModelScope.launch {
                runCatching {
                    repository.deleteCutting(id)
                    loadCuttingLists()
                    _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "削除しました") }
                }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
            }
        })
    }
    fun openConfirmCuttingActual() = openDialog(CuttingInstructionDialog.ConfirmCuttingActual)
    fun confirmCuttingActualAction() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val res = repository.confirmCuttingActual(_uiState.value.cuttingDateToday, _uiState.value.cuttingMachineFilter)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        activeDialog = CuttingInstructionDialog.ConfirmActualResult(res.inserted ?: 0, res.totalQuantity ?: 0),
                    )
                }
                loadCuttingLists()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, activeDialog = CuttingInstructionDialog.None, snackbarMessage = e.message ?: "確定失敗") }
            }
        }
    }
    fun printCuttingPlan() = printCuttingPlanForDate(_uiState.value.cuttingDateToday, "切断計画")
    fun printCuttingInstructionSheet() = printCuttingSheetForDate(_uiState.value.cuttingDateToday)
    private fun printCuttingPlanForDate(date: String, subject: String) {
        viewModelScope.launch {
            runCatching {
                val rows = repository.loadCuttingManagement(date, null)
                if (rows.isEmpty()) throw IllegalStateException("該当日のデータがありません")
                val stock = repository.loadMaterialStockForPrint(date)
                val sub = repository.loadMaterialStockSubForPrint()
                val html = buildCuttingPlanPrintHtml(date, rows, stock, sub)
                _uiState.update { it.copy(pendingPrintHtml = html, pendingPrintSubject = subject) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "印刷失敗") } }
        }
    }
    private fun printCuttingSheetForDate(date: String) {
        viewModelScope.launch {
            runCatching {
                val rows = repository.loadCuttingManagement(date, null)
                if (rows.isEmpty()) throw IllegalStateException("該当日のデータがありません")
                _uiState.update { it.copy(pendingPrintHtml = buildCuttingInstructionSheetHtml(date, rows), pendingPrintSubject = "切断指示書") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "印刷失敗") } }
        }
    }
    fun setChamferingMachineFilter(machine: String) {
        _uiState.update { it.copy(chamferingMachineFilter = machine) }
        loadChamferingLists()
    }
    fun shiftChamferingDateToday(days: Long) {
        _uiState.update { it.copy(chamferingDateToday = shiftInstructionDate(it.chamferingDateToday, days)) }
        loadChamferingLists()
    }
    fun shiftChamferingDateTomorrow(days: Long) {
        _uiState.update { it.copy(chamferingDateTomorrow = shiftInstructionDate(it.chamferingDateTomorrow, days)) }
        loadChamferingLists()
    }
    private fun loadChamferingPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(chamferingPlansLoading = true) }
            runCatching {
                val plans = repository.loadChamferingPlans()
                val (productionDayMap, startDateMap) = repository.loadCuttingReferenceByMgmtCode()
                _uiState.update {
                    it.copy(
                        chamferingPlans = plans,
                        cuttingProductionDayByMgmtCode = productionDayMap,
                        cuttingFormingStartDateByMgmtCode = startDateMap,
                        chamferingPlansLoading = false,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(chamferingPlansLoading = false, snackbarMessage = e.message ?: "面取ロット取得失敗") }
            }
        }
    }
    private fun loadChamferingLists() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(chamferingLoading = true) }
            runCatching {
                val machine = state.chamferingMachineFilter.ifBlank { null }
                val today = repository.loadChamferingManagement(state.chamferingDateToday, machine)
                val tomorrow = repository.loadChamferingManagement(state.chamferingDateTomorrow, machine)
                _uiState.update { it.copy(chamferingToday = today, chamferingTomorrow = tomorrow, chamferingLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(chamferingLoading = false, snackbarMessage = e.message ?: "面取指示取得失敗") }
            }
        }
    }
    fun toggleChamferingPlanSw(row: InstructionChamferingPlanRowDto, enabled: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchChamferingPlan(id, PatchChamferingPlanBody(hasSwProcess = if (enabled) 1 else 0))
                loadChamferingPlans()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun copyChamferingPlan(row: InstructionChamferingPlanRowDto) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.copyChamferingPlan(id)
                loadChamferingPlans()
                _uiState.update { it.copy(snackbarMessage = "複製しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "複製失敗") } }
        }
    }
    fun deleteChamferingPlan(row: InstructionChamferingPlanRowDto) {
        val id = row.id ?: return
        openDialog(CuttingInstructionDialog.ConfirmDelete("面取ロットを削除しますか？") {
            viewModelScope.launch {
                runCatching {
                    repository.deleteChamferingPlan(id)
                    loadChamferingPlans()
                    _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "削除しました") }
                }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
            }
        })
    }
    fun openNewChamferingPlan() {
        openDialog(CuttingInstructionDialog.NewChamferingPlan(ChamferingPlanFormState(
            productionMonth = LocalDate.now(JST).toString().take(7),
            productionDay = instructionToday(),
            productionLine = _uiState.value.chamferingMachineFilter,
        )))
    }
    fun openEditChamferingPlan(row: InstructionChamferingPlanRowDto) {
        openDialog(CuttingInstructionDialog.EditChamferingPlan(row.id ?: return, row.toChamferingPlanFormState()))
    }
    fun saveNewChamferingPlan(form: ChamferingPlanFormState) {
        viewModelScope.launch {
            runCatching {
                repository.createChamferingPlan(form.toCreateBody())
                loadChamferingPlans()
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "登録しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "登録失敗") } }
        }
    }
    fun saveEditChamferingPlan(planId: Int, form: ChamferingPlanFormState) {
        viewModelScope.launch {
            runCatching {
                repository.updateChamferingPlanContent(planId, form.toUpdateBody())
                loadChamferingPlans()
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "更新しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun openMoveChamferingPlan(row: InstructionChamferingPlanRowDto) = openDialog(CuttingInstructionDialog.MoveChamferingPlan(row))
    fun confirmMoveChamferingPlan(day: String, machine: String, machine2: String?) {
        val plan = (_uiState.value.activeDialog as? CuttingInstructionDialog.MoveChamferingPlan)?.plan ?: return
        val id = plan.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.moveChamferingPlanToChamfering(MoveChamferingPlanBody(id, day, machine, machine2))
                loadChamferingPlans()
                loadChamferingLists()
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "面取指示へ移行しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "移行失敗") } }
        }
    }
    fun toggleChamferingCompleted(row: InstructionChamferingRowDto, completed: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchChamfering(id, PatchChamferingBody(productionCompletedCheck = completed))
                patchChamferingRow(id) { it.copy(productionCompletedCheck = if (completed) 1 else 0) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun toggleChamferingNoCount(row: InstructionChamferingRowDto, enabled: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchChamfering(id, PatchChamferingBody(noCount = enabled))
                patchChamferingRow(id) { it.copy(noCount = if (enabled) 1 else 0) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun openNewChamferingInstruction() {
        val state = _uiState.value
        openDialog(CuttingInstructionDialog.NewChamferingInstruction(
            ChamferingInstructionFormState(
                productionDay = state.chamferingDateToday,
                chamferingMachine = state.chamferingMachineFilter.ifBlank {
                    state.chamferingMachineOptions.firstOrNull()?.first.orEmpty()
                },
            ),
        ))
        viewModelScope.launch {
            runCatching {
                val products = repository.loadChamferingProductOptions()
                val materials = repository.loadChamferingMaterialOptions()
                _uiState.update {
                    it.copy(
                        chamferingProductOptions = products,
                        chamferingMaterialOptions = materials,
                    )
                }
            }
        }
    }
    fun saveNewChamferingInstruction(form: ChamferingInstructionFormState) {
        val day = form.productionDay.trim().take(10)
        val machine = form.chamferingMachine.trim()
        val productCd = form.productCd.trim()
        val productName = form.productName.trim()
        if (day.length < 10) {
            _uiState.update { it.copy(snackbarMessage = "生産日を入力してください（YYYY-MM-DD）") }
            return
        }
        if (machine.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "面取機を選択してください") }
            return
        }
        if (productCd.isBlank() || productName.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品CD・製品名は必須です") }
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.createChamferingManagement(CreateChamferingManagementBody(
                    productionDay = day,
                    productionLine = form.productionLine.trim(),
                    chamferingMachine = machine,
                    productCd = productCd,
                    productName = productName,
                    actualProductionQuantity = form.actualProductionQuantity.trim().toIntOrNull() ?: 0,
                    productionSequence = form.productionSequence.trim().toIntOrNull(),
                    materialName = form.materialName.trim().ifBlank { null },
                    managementCode = previewChamferingManagementCode(
                        day, form.productionLine, productCd, form.productionSequence,
                    ).ifBlank { null },
                ))
                loadChamferingLists()
                _uiState.update {
                    it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "面取指示を登録しました")
                }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "登録失敗") } }
        }
    }
    fun openEditChamfering(row: InstructionChamferingRowDto) = openDialog(CuttingInstructionDialog.EditChamfering(row))
    fun saveChamferingEdit(row: InstructionChamferingRowDto, day: String, machine: String, qty: Int?, defect: Int?, remarks: String?) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchChamfering(id, PatchChamferingBody(
                    productionDay = day.ifBlank { null },
                    chamferingMachine = machine.ifBlank { null },
                    actualProductionQuantity = qty,
                    defectQty = defect,
                    remarks = remarks,
                ))
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "保存しました") }
                loadChamferingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun duplicateChamferingRow(row: InstructionChamferingRowDto) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.duplicateChamfering(id)
                loadChamferingLists()
                _uiState.update { it.copy(snackbarMessage = "複製しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "複製失敗") } }
        }
    }
    fun splitChamfering(row: InstructionChamferingRowDto, todayQty: Int) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.splitChamferingToNextDay(id, SplitChamferingToNextDayBody(todayQuantity = todayQty, nextDay = _uiState.value.chamferingDateTomorrow))
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "分割しました") }
                loadChamferingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "分割失敗") } }
        }
    }
    fun reorderChamferingRow(row: InstructionChamferingRowDto, direction: Int) {
        val list = if (row.productionDay?.take(10) == _uiState.value.chamferingDateTomorrow) _uiState.value.chamferingTomorrow else _uiState.value.chamferingToday
        val idx = list.indexOfFirst { it.id == row.id }
        if (idx < 0) return
        val newIdx = idx + direction
        if (newIdx !in list.indices) return
        val reordered = list.toMutableList()
        reordered.add(newIdx, reordered.removeAt(idx))
        val machine = _uiState.value.chamferingMachineFilter.ifBlank { row.chamferingMachine.orEmpty() }
        val day = row.productionDay?.take(10) ?: _uiState.value.chamferingDateToday
        viewModelScope.launch {
            runCatching {
                repository.reorderChamfering(ReorderChamferingBody(machine, day, reordered.mapNotNull { it.id }))
                loadChamferingLists()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "並べ替え失敗") } }
        }
    }
    fun deleteChamferingRow(row: InstructionChamferingRowDto) {
        val id = row.id ?: return
        openDialog(CuttingInstructionDialog.ConfirmDelete("面取指示を削除しますか？") {
            viewModelScope.launch {
                runCatching {
                    repository.deleteChamfering(id)
                    loadChamferingLists()
                    _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "削除しました") }
                }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
            }
        })
    }
    fun openConfirmChamferingActual() = openDialog(CuttingInstructionDialog.ConfirmChamferingActual)
    fun confirmChamferingActualAction() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val res = repository.confirmChamferingActual(_uiState.value.chamferingDateToday, _uiState.value.chamferingMachineFilter)
                _uiState.update {
                    it.copy(actionLoading = false, activeDialog = CuttingInstructionDialog.ConfirmActualResult(res.inserted ?: 0, res.totalQuantity ?: 0))
                }
                loadChamferingLists()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, activeDialog = CuttingInstructionDialog.None, snackbarMessage = e.message ?: "確定失敗") }
            }
        }
    }
    fun printChamferingPlan() {
        viewModelScope.launch {
            runCatching {
                val rows = repository.loadChamferingManagement(_uiState.value.chamferingDateToday, _uiState.value.chamferingMachineFilter.ifBlank { null })
                if (rows.isEmpty()) throw IllegalStateException("該当日のデータがありません")
                _uiState.update { it.copy(pendingPrintHtml = buildChamferingPlanPrintHtml(_uiState.value.chamferingDateToday, rows), pendingPrintSubject = "面取計画") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "印刷失敗") } }
        }
    }
    fun printChamferingInstructionSheet() {
        viewModelScope.launch {
            runCatching {
                val rows = repository.loadChamferingManagement(_uiState.value.chamferingDateToday, null)
                if (rows.isEmpty()) throw IllegalStateException("該当日のデータがありません")
                _uiState.update { it.copy(pendingPrintHtml = buildChamferingInstructionSheetHtml(_uiState.value.chamferingDateToday, rows), pendingPrintSubject = "面取指示書") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "印刷失敗") } }
        }
    }
    fun setKanbanDate(date: String) {
        _uiState.update { it.copy(kanbanDate = date) }
        loadKanbanList()
    }
    fun setKanbanToday() = setKanbanDate(instructionToday())
    fun setKanbanStatus(status: String) {
        _uiState.update { it.copy(kanbanStatus = status) }
        loadKanbanList()
    }
    fun setKanbanProductName(name: String) {
        _uiState.update { it.copy(kanbanProductName = name) }
        loadKanbanList()
    }
    fun toggleKanbanSelection(id: Int, selected: Boolean) {
        _uiState.update { state ->
            val next = state.selectedKanbanIds.toMutableSet()
            if (selected) next.add(id) else next.remove(id)
            state.copy(selectedKanbanIds = next)
        }
    }
    private fun loadKanbanList() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(kanbanLoading = true) }
            runCatching {
                val rows = repository.loadKanbanList(state.kanbanDate, state.kanbanStatus, state.kanbanProductName)
                _uiState.update { it.copy(kanbanRows = rows, kanbanLoading = false, selectedKanbanIds = emptySet()) }
            }.onFailure { e ->
                _uiState.update { it.copy(kanbanLoading = false, snackbarMessage = e.message ?: "カンバン取得失敗") }
            }
        }
    }
    fun issueKanban(id: Int) {
        viewModelScope.launch {
            runCatching {
                repository.issueKanban(id)
                loadKanbanList()
                _uiState.update { it.copy(snackbarMessage = "カンバンを発行しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "発行失敗") } }
        }
    }
    fun reissueKanban(id: Int) {
        viewModelScope.launch {
            runCatching {
                repository.reissueKanban(id)
                loadKanbanList()
                _uiState.update { it.copy(snackbarMessage = "再発行しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "再発行失敗") } }
        }
    }
    fun batchIssueKanban() {
        val ids = _uiState.value.selectedKanbanIds.toList()
        if (ids.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "カンバンを選択してください") }
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.batchIssueKanban(ids)
                loadKanbanList()
                val issued = _uiState.value.kanbanRows.filter { it.id in ids }
                val items = issued.mapNotNull { row -> row.kanbanNo?.let { no -> row to no } }
                if (items.isNotEmpty()) {
                    _uiState.update { it.copy(pendingPrintHtml = buildKanbanBatchPrintHtml(items), pendingPrintSubject = "カンバン一括印刷") }
                }
                _uiState.update { it.copy(snackbarMessage = "一括発行しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "一括発行失敗") } }
        }
    }
    fun openEditKanban(row: KanbanIssuanceRowDto) = openDialog(CuttingInstructionDialog.EditKanban(row))
    fun saveKanbanEdit(row: KanbanIssuanceRowDto, productName: String?, qty: Int?, day: String?) {
        val id = row.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.patchKanban(id, PatchKanbanBody(productName = productName, actualProductionQuantity = qty, productionDay = day))
                loadKanbanList()
                _uiState.update { it.copy(activeDialog = CuttingInstructionDialog.None, snackbarMessage = "更新しました") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "保存失敗") } }
        }
    }
    fun syncKanbanProductionDay() {
        viewModelScope.launch {
            runCatching {
                val msg = repository.syncKanbanProductionDay()
                loadKanbanList()
                _uiState.update { it.copy(snackbarMessage = msg) }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "同期失敗") } }
        }
    }
    fun openConfirmUsageReflection() = openDialog(CuttingInstructionDialog.ConfirmUsageReflection)
    fun confirmUsageReflection() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val msg = repository.commitMaterialUsage(state.usageSummaryDateToday, state.usageSummaryDateTomorrow)
                _uiState.update { it.copy(actionLoading = false, activeDialog = CuttingInstructionDialog.None, snackbarMessage = msg) }
                loadUsageSummary()
                loadCuttingLists()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, activeDialog = CuttingInstructionDialog.None, snackbarMessage = e.message ?: "反映失敗") }
            }
        }
    }
    fun openMoldingPreInventory() {
        openDialog(CuttingInstructionDialog.MoldingPreInventory)
        loadMoldingPreInventory()
    }
    fun setMoldingPreInventoryDate(date: String) = _uiState.update { it.copy(moldingPreInventoryDate = date) }
    fun loadMoldingPreInventory() {
        viewModelScope.launch {
            _uiState.update { it.copy(moldingPreInventoryLoading = true) }
            runCatching {
                val groups = repository.loadMoldingPreInventoryGroups(_uiState.value.moldingPreInventoryDate)
                _uiState.update { it.copy(moldingPreInventoryGroups = groups, moldingPreInventoryLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(moldingPreInventoryLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }
    fun printMoldingPreInventory() {
        val groups = _uiState.value.moldingPreInventoryGroups
        if (groups.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        _uiState.update {
            it.copy(
                pendingPrintHtml = buildMoldingPreInventoryPrintHtml(it.moldingPreInventoryDate, groups),
                pendingPrintSubject = "成型前在庫・時間換算",
            )
        }
    }
    fun openCuttingDoneList() {
        val (start, end) = instructionCurrentMonthPeriod()
        _uiState.update {
            it.copy(
                activeDialog = CuttingInstructionDialog.CuttingDoneList,
                cuttingDonePeriodStart = start,
                cuttingDonePeriodEnd = end,
                cuttingDoneProductFilter = "",
                cuttingDoneManagementCodeFilter = "",
                cuttingDoneOnlyCompleted = false,
                doneListPage = 1,
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(doneListLoading = true) }
            runCatching {
                val list = sortCuttingDoneList(repository.loadAllCuttingManagement())
                _uiState.update { it.copy(cuttingDoneRaw = list, doneListLoading = false) }
            }.onFailure { e -> _uiState.update { it.copy(doneListLoading = false, snackbarMessage = e.message ?: "取得失敗") } }
        }
    }
    fun openChamferingDoneList() {
        _uiState.update {
            it.copy(
                activeDialog = CuttingInstructionDialog.ChamferingDoneList,
                chamferingDonePeriodStart = "",
                chamferingDonePeriodEnd = "",
                chamferingDoneProductFilter = "",
                chamferingDoneOnlyCompleted = false,
                doneListPage = 1,
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(doneListLoading = true) }
            runCatching {
                val list = sortChamferingDoneList(repository.loadAllChamferingManagement())
                _uiState.update { it.copy(chamferingDoneRaw = list, doneListLoading = false) }
            }.onFailure { e -> _uiState.update { it.copy(doneListLoading = false, snackbarMessage = e.message ?: "取得失敗") } }
        }
    }
    fun setCuttingDonePeriodStart(v: String) = _uiState.update { it.copy(cuttingDonePeriodStart = v, doneListPage = 1) }
    fun setCuttingDonePeriodEnd(v: String) = _uiState.update { it.copy(cuttingDonePeriodEnd = v, doneListPage = 1) }
    fun setCuttingDoneManagementCodeFilter(v: String) = _uiState.update { it.copy(cuttingDoneManagementCodeFilter = v, doneListPage = 1) }
    fun setChamferingDonePeriodStart(v: String) = _uiState.update { it.copy(chamferingDonePeriodStart = v, doneListPage = 1) }
    fun setChamferingDonePeriodEnd(v: String) = _uiState.update { it.copy(chamferingDonePeriodEnd = v, doneListPage = 1) }
    fun setDoneListPage(page: Int) {
        _uiState.update { state ->
            val maxPage = when (state.activeDialog) {
                CuttingInstructionDialog.CuttingDoneList -> cuttingDoneTotalPages
                CuttingInstructionDialog.ChamferingDoneList -> chamferingDoneTotalPages
                else -> 1
            }
            state.copy(doneListPage = page.coerceIn(1, maxPage))
        }
    }
    fun setCuttingDoneProductFilter(v: String) = _uiState.update { it.copy(cuttingDoneProductFilter = v) }
    fun setCuttingDoneOnlyCompleted(v: Boolean) = _uiState.update { it.copy(cuttingDoneOnlyCompleted = v) }
    fun setChamferingDoneProductFilter(v: String) = _uiState.update { it.copy(chamferingDoneProductFilter = v) }
    fun setChamferingDoneOnlyCompleted(v: Boolean) = _uiState.update { it.copy(chamferingDoneOnlyCompleted = v) }
    fun openDataManagement() {
        openDialog(CuttingInstructionDialog.DataManagement)
        loadDataManagement()
    }
    fun setDataManagementMonth(month: String) = _uiState.update { it.copy(dataManagementMonth = month) }
    fun loadDataManagement() {
        viewModelScope.launch {
            _uiState.update { it.copy(dataManagementLoading = true) }
            runCatching {
                val rows = repository.loadBatchPlans(null, _uiState.value.dataManagementMonth)
                _uiState.update { it.copy(dataManagementRows = rows, dataManagementLoading = false) }
            }.onFailure { e -> _uiState.update { it.copy(dataManagementLoading = false, snackbarMessage = e.message ?: "取得失敗") } }
        }
    }
    fun openNotes() {
        openDialog(CuttingInstructionDialog.Notes)
        refreshNotes()
    }
    fun refreshNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(notesLoading = true) }
            runCatching {
                val notes = repository.loadNotes()
                _uiState.update { it.copy(notes = notes, notesCount = notes.count { n -> n.isDone != true }, notesLoading = false) }
            }.onFailure { e -> _uiState.update { it.copy(notesLoading = false) } }
        }
    }
    fun createNote(content: String) {
        viewModelScope.launch {
            runCatching {
                repository.createNote(content)
                refreshNotes()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "追加失敗") } }
        }
    }
    fun toggleNoteDone(id: Int, done: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.patchNote(id, isDone = done)
                refreshNotes()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "更新失敗") } }
        }
    }
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            runCatching {
                repository.deleteNote(id)
                refreshNotes()
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "削除失敗") } }
        }
    }
    fun openSpecifiedDateMaterial() {
        openDialog(CuttingInstructionDialog.SpecifiedDateMaterial)
        loadSpecifiedMaterialRows()
    }
    fun setSpecifiedMaterialDate(date: String) = _uiState.update { it.copy(specifiedMaterialDate = date) }
    fun loadSpecifiedMaterialRows() {
        viewModelScope.launch {
            _uiState.update { it.copy(specifiedMaterialLoading = true) }
            runCatching {
                val rows = repository.loadCuttingManagement(_uiState.value.specifiedMaterialDate, null)
                _uiState.update { it.copy(specifiedMaterialRows = rows, specifiedMaterialLoading = false) }
            }.onFailure { e -> _uiState.update { it.copy(specifiedMaterialLoading = false) } }
        }
    }
    fun printSpecifiedDateMaterial() {
        val date = _uiState.value.specifiedMaterialDate
        val rows = _uiState.value.specifiedMaterialRows
        if (rows.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        viewModelScope.launch {
            runCatching {
                val stock = repository.loadMaterialStockForPrint(date)
                val sub = repository.loadMaterialStockSubForPrint()
                _uiState.update { it.copy(pendingPrintHtml = buildCuttingPlanPrintHtml(date, rows, stock, sub), pendingPrintSubject = "指定日材料数") }
            }.onFailure { e -> _uiState.update { it.copy(snackbarMessage = e.message ?: "印刷失敗") } }
        }
    }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null, pendingPrintSubject = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    private fun refreshPlanRow(id: Int, transform: (InstructionPlanRowDto) -> InstructionPlanRowDto) {
        _uiState.update { state -> state.copy(allPlans = state.allPlans.map { if (it.id == id) transform(it) else it }) }
    }
    private fun patchCuttingRow(id: Int, transform: (InstructionCuttingRowDto) -> InstructionCuttingRowDto) {
        _uiState.update { state ->
            state.copy(
                cuttingToday = state.cuttingToday.map { if (it.id == id) transform(it) else it },
                cuttingTomorrow = state.cuttingTomorrow.map { if (it.id == id) transform(it) else it },
            )
        }
    }
    private fun patchChamferingRow(id: Int, transform: (InstructionChamferingRowDto) -> InstructionChamferingRowDto) {
        _uiState.update { state ->
            state.copy(
                chamferingToday = state.chamferingToday.map { if (it.id == id) transform(it) else it },
                chamferingTomorrow = state.chamferingTomorrow.map { if (it.id == id) transform(it) else it },
            )
        }
    }
    class Factory(private val repository: CuttingInstructionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CuttingInstructionViewModel(repository) as T
    }
}
private fun InstructionPlanRowDto.toPlanFormState() = PlanFormState(
    productionMonth = productionMonth?.take(10).orEmpty(),
    productionLine = productionLine.orEmpty(),
    priorityOrder = priorityOrder?.toString() ?: "0",
    productCd = productCd.orEmpty(),
    productName = productName.orEmpty(),
    plannedQuantity = plannedQuantity?.toString() ?: "0",
    actualProductionQuantity = actualProductionQuantity?.toString() ?: "0",
    startDate = startDate?.take(10).orEmpty(),
    endDate = endDate?.take(10).orEmpty(),
    productionLotSize = productionLotSize?.toString().orEmpty(),
    lotNumber = lotNumber ?: productionLotSize?.toString().orEmpty(),
    isCuttingInstructed = isCuttingInstructed == 1,
    hasChamferingProcess = hasChamferingProcess == 1,
    isChamferingInstructed = isChamferingInstructed == 1,
    hasSwProcess = hasSwProcess == 1,
    isSwInstructed = isSwInstructed == 1,
    managementCode = managementCode.orEmpty(),
    takeCount = takeCount?.toString().orEmpty(),
    cuttingLength = formatPlanDecimal(cuttingLength),
    chamferingLength = formatPlanDecimal(chamferingLength),
    developedLength = formatPlanDecimal(developedLength),
    scrapLength = formatPlanDecimal(scrapLength),
    materialName = materialName.orEmpty(),
    materialManufacturer = materialManufacturer.orEmpty(),
    standardSpecification = standardSpecification.orEmpty(),
    useMaterialStockSub = useMaterialStockSub == 1,
    usageCount = formatUsageCount(usageCount),
)
private fun InstructionPlanRowDto.toCreateBody(isTrial: Boolean = false) = CreateInstructionPlanBody(
    productionMonth = productionMonth?.take(7).orEmpty(),
    productionLine = productionLine.orEmpty(),
    priorityOrder = priorityOrder,
    productCd = productCd.orEmpty(),
    productName = productName.orEmpty(),
    plannedQuantity = plannedQuantity,
    productionLotSize = productionLotSize,
    lotNumber = lotNumber,
    actualProductionQuantity = actualProductionQuantity,
    takeCount = takeCount,
    cuttingLength = cuttingLength,
    chamferingLength = chamferingLength,
    developedLength = developedLength,
    scrapLength = scrapLength,
    materialName = materialName,
    materialManufacturer = materialManufacturer,
    standardSpecification = standardSpecification,
    startDate = startDate?.take(10),
    endDate = endDate?.take(10),
    hasChamferingProcess = hasChamferingProcess,
    hasSwProcess = hasSwProcess,
    useMaterialStockSub = useMaterialStockSub,
    usageCount = usageCount,
)
private fun PlanFormState.toCreateBody(isTrial: Boolean) = CreateInstructionPlanBody(
    productionMonth = productionMonth,
    productionLine = productionLine,
    priorityOrder = priorityOrder.toIntOrNull(),
    productCd = productCd,
    productName = productName,
    plannedQuantity = plannedQuantity.toIntOrNull(),
    productionLotSize = productionLotSize.toIntOrNull(),
    lotNumber = lotNumber.ifBlank { null },
    actualProductionQuantity = actualProductionQuantity.toIntOrNull(),
    materialName = materialName.ifBlank { null },
    startDate = startDate.ifBlank { null },
    endDate = endDate.ifBlank { null },
    hasChamferingProcess = if (hasChamferingProcess) 1 else 0,
    useMaterialStockSub = if (useMaterialStockSub) 1 else 0,
    usageCount = usageCount.toDoubleOrNull(),
)
private fun PlanFormState.toPatchBody() = PatchInstructionPlanBody(
    productionMonth = productionMonth.ifBlank { null },
    productionLine = productionLine,
    priorityOrder = priorityOrder.toIntOrNull(),
    productCd = productCd,
    productName = productName,
    plannedQuantity = plannedQuantity.toIntOrNull(),
    actualProductionQuantity = actualProductionQuantity.toIntOrNull(),
    productionLotSize = productionLotSize.toIntOrNull(),
    lotNumber = lotNumber.ifBlank { null },
    materialName = materialName.ifBlank { null },
    materialManufacturer = materialManufacturer.ifBlank { null },
    standardSpecification = standardSpecification.ifBlank { null },
    startDate = startDate.ifBlank { null },
    endDate = endDate.ifBlank { null },
    takeCount = takeCount.toIntOrNull(),
    cuttingLength = cuttingLength.toDoubleOrNull(),
    chamferingLength = chamferingLength.toDoubleOrNull(),
    developedLength = developedLength.toDoubleOrNull(),
    scrapLength = scrapLength.toDoubleOrNull(),
    hasChamferingProcess = if (hasChamferingProcess) 1 else 0,
    hasSwProcess = if (hasSwProcess) 1 else 0,
    useMaterialStockSub = if (useMaterialStockSub) 1 else 0,
    usageCount = usageCount.toDoubleOrNull(),
)
private fun InstructionChamferingPlanRowDto.toChamferingPlanFormState() = ChamferingPlanFormState(
    productionMonth = productionMonth?.take(7).orEmpty(),
    productionDay = productionDay?.take(10).orEmpty(),
    productionLine = productionLine.orEmpty(),
    productCd = productCd.orEmpty(),
    productName = productName.orEmpty(),
    actualProductionQuantity = actualProductionQuantity?.toString() ?: "0",
    productionLotSize = productionLotSize?.toString().orEmpty(),
    lotNumber = lotNumber.orEmpty(),
    materialName = materialName.orEmpty(),
    hasSwProcess = hasSwProcess == 1,
)
private fun ChamferingPlanFormState.toCreateBody() = CreateChamferingPlanBody(
    productionMonth = productionMonth,
    productionDay = productionDay,
    productionLine = productionLine,
    productCd = productCd,
    productName = productName,
    actualProductionQuantity = actualProductionQuantity.toIntOrNull(),
    productionLotSize = productionLotSize.toIntOrNull(),
    lotNumber = lotNumber.ifBlank { null },
    materialName = materialName.ifBlank { null },
    hasSwProcess = if (hasSwProcess) 1 else 0,
)
private fun ChamferingPlanFormState.toUpdateBody() = UpdateChamferingPlanContentBody(
    productionMonth = productionMonth.ifBlank { null },
    productionDay = productionDay.ifBlank { null },
    productionLine = productionLine,
    productCd = productCd,
    productName = productName,
    actualProductionQuantity = actualProductionQuantity.toIntOrNull(),
    productionLotSize = productionLotSize.toIntOrNull(),
    lotNumber = lotNumber.ifBlank { null },
    materialName = materialName.ifBlank { null },
    hasSwProcess = if (hasSwProcess) 1 else 0,
)

private fun resolveCuttingMachineFilter(current: String, machines: List<Pair<String, String>>): String {
    val filtered = machines.filter { (name, _) -> name != OUTSOURCED_CUTTING_MACHINE }
    val trimmed = current.trim()
    if (trimmed.isBlank() || trimmed == OUTSOURCED_CUTTING_MACHINE) {
        return filtered.firstOrNull()?.first.orEmpty()
    }
    return trimmed
}