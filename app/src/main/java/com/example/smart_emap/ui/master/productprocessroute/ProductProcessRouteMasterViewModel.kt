package com.example.smart_emap.ui.master.productprocessroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterProcessOptionDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto
import com.example.smart_emap.data.model.MasterProductRouteMachineBodyDto
import com.example.smart_emap.data.model.MasterProductRouteStepBulkItemDto
import com.example.smart_emap.data.model.MasterProductRouteStepBulkMachineDto
import com.example.smart_emap.data.model.MasterProductRouteStepDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ProductRouteMachineUi(
    val localId: String = UUID.randomUUID().toString(),
    val id: Int? = null,
    val machineCd: String = "",
    val machineName: String = "",
    val processTimeSec: Int = 0,
    val setupTime: Int = 0,
)

data class ProductRouteStepUi(
    val localId: String = UUID.randomUUID().toString(),
    val id: Int? = null,
    val productCd: String = "",
    val routeCd: String = "",
    val stepNo: Int = 0,
    val processCd: String = "",
    val processName: String = "",
    val machines: List<ProductRouteMachineUi> = emptyList(),
)

data class ProductProcessRouteMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val productKeyword: String = "",
    val productPage: Int = 1,
    val productPageSize: Int = 20,
    val productTotal: Int = 0,
    val products: List<Pair<String, String>> = emptyList(),
    val selectedProductCd: String = "",
    val routeInfo: MasterProductRouteInfoDto? = null,
    val steps: List<ProductRouteStepUi> = emptyList(),
    val dataLoaded: Boolean = false,
    val allMachines: List<MasterMachineFullDto> = emptyList(),
    val processOptions: List<MasterProcessOptionDto> = emptyList(),
    val showProcessDialog: Boolean = false,
    val pendingDeleteStepIndex: Int? = null,
    val pendingDeleteMachine: Pair<String, Int>? = null,
    val pendingReset: Boolean = false,
    val snackbarMessage: String? = null,
)

class ProductProcessRouteMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductProcessRouteMasterUiState())
    val uiState: StateFlow<ProductProcessRouteMasterUiState> = _uiState.asStateFlow()
    private var keywordJob: Job? = null

    init {
        viewModelScope.launch {
            val machines = repository.loadAllMachinesForRoute()
            _uiState.update { it.copy(allMachines = machines) }
        }
        refreshProducts()
    }

    fun refreshAll() {
        refreshProducts()
        val cd = _uiState.value.selectedProductCd
        if (cd.isNotBlank()) loadProductRoute(cd)
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val (products, total) = repository.loadProductsForRoute(
                    keyword = state.productKeyword,
                    page = state.productPage,
                    pageSize = state.productPageSize,
                )
                _uiState.update { it.copy(isLoading = false, products = products, productTotal = total) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "製品一覧の読込に失敗") }
            }
        }
    }

    fun setProductKeyword(value: String) {
        _uiState.update { it.copy(productKeyword = value, productPage = 1) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(320)
            refreshProducts()
        }
    }

    fun setProductPage(page: Int) {
        _uiState.update { it.copy(productPage = page) }
        refreshProducts()
    }

    fun selectProduct(productCd: String) {
        _uiState.update { it.copy(selectedProductCd = productCd, isLoading = true) }
        loadProductRoute(productCd)
    }

    private fun loadProductRoute(productCd: String) {
        viewModelScope.launch {
            runCatching {
                val info = repository.loadProductRouteInfo(productCd)
                val routeCd = info?.routeCd.orEmpty()
                val steps = if (info != null && routeCd.isNotBlank()) {
                    repository.loadProductRouteSteps(productCd, routeCd).map { it.toUi() }
                } else {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        routeInfo = info,
                        steps = steps,
                        dataLoaded = true,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        routeInfo = null,
                        steps = emptyList(),
                        dataLoaded = false,
                        snackbarMessage = e.message ?: "データ読み込み失敗",
                    )
                }
            }
        }
    }

    fun openProcessDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(showProcessDialog = true, actionLoading = true) }
            runCatching {
                val options = repository.loadProcessesForProductRoute()
                _uiState.update { it.copy(actionLoading = false, processOptions = options) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, showProcessDialog = false, snackbarMessage = e.message ?: "工程一覧の読込に失敗")
                }
            }
        }
    }

    fun closeProcessDialog() = _uiState.update { it.copy(showProcessDialog = false) }

    fun addProcess(process: MasterProcessOptionDto) {
        val state = _uiState.value
        val productCd = state.selectedProductCd
        val routeCd = state.routeInfo?.routeCd?.takeIf { it.isNotBlank() }
            ?: state.steps.firstOrNull()?.routeCd?.takeIf { it.isNotBlank() }
        if (routeCd.isNullOrBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品に工程ルートが設定されていません") }
            return
        }
        val nextNo = (state.steps.maxOfOrNull { it.stepNo } ?: 0) + 1
        val step = ProductRouteStepUi(
            productCd = productCd,
            routeCd = routeCd,
            stepNo = nextNo,
            processCd = process.processCd.orEmpty(),
            processName = process.processName.orEmpty(),
        )
        _uiState.update { it.copy(steps = state.steps + step, showProcessDialog = false) }
    }

    fun requestRemoveStep(index: Int) {
        val step = _uiState.value.steps.getOrNull(index) ?: return
        if (step.id != null) {
            _uiState.update { it.copy(pendingDeleteStepIndex = index) }
        } else {
            removeStepAt(index)
        }
    }

    fun cancelRemoveStep() = _uiState.update { it.copy(pendingDeleteStepIndex = null) }

    fun confirmRemoveStep() {
        val index = _uiState.value.pendingDeleteStepIndex ?: return
        _uiState.update { it.copy(pendingDeleteStepIndex = null) }
        removeStepAt(index)
    }

    private fun removeStepAt(index: Int) {
        val steps = _uiState.value.steps.toMutableList()
        if (index !in steps.indices) return
        steps.removeAt(index)
        renumberSteps(steps)
        _uiState.update { it.copy(steps = steps) }
    }

    fun moveStepUp(index: Int) {
        if (index <= 0) return
        val steps = _uiState.value.steps.toMutableList()
        val item = steps.removeAt(index)
        steps.add(index - 1, item)
        renumberSteps(steps)
        _uiState.update { it.copy(steps = steps, snackbarMessage = "ステップ順序が更新されました") }
    }

    fun moveStepDown(index: Int) {
        val steps = _uiState.value.steps.toMutableList()
        if (index >= steps.lastIndex) return
        val item = steps.removeAt(index)
        steps.add(index + 1, item)
        renumberSteps(steps)
        _uiState.update { it.copy(steps = steps, snackbarMessage = "ステップ順序が更新されました") }
    }

    private fun renumberSteps(steps: MutableList<ProductRouteStepUi>) {
        steps.forEachIndexed { i, step -> steps[i] = step.copy(stepNo = i + 1) }
    }

    fun addMachine(stepLocalId: String) {
        updateStep(stepLocalId) { step ->
            step.copy(machines = step.machines + ProductRouteMachineUi())
        }
    }

    fun updateMachineCd(stepLocalId: String, machineLocalId: String, machineCd: String) {
        val machine = _uiState.value.allMachines.find { it.machineCd == machineCd }
        updateMachine(stepLocalId, machineLocalId) {
            it.copy(machineCd = machineCd, machineName = machine?.machineName.orEmpty())
        }
    }

    fun updateMachineProcessTime(stepLocalId: String, machineLocalId: String, value: Int) {
        updateMachine(stepLocalId, machineLocalId) { it.copy(processTimeSec = value.coerceAtLeast(0)) }
    }

    fun updateMachineSetupTime(stepLocalId: String, machineLocalId: String, value: Int) {
        updateMachine(stepLocalId, machineLocalId) { it.copy(setupTime = value.coerceAtLeast(0)) }
    }

    private fun updateMachine(
        stepLocalId: String,
        machineLocalId: String,
        transform: (ProductRouteMachineUi) -> ProductRouteMachineUi,
    ) {
        updateStep(stepLocalId) { step ->
            step.copy(
                machines = step.machines.map { m ->
                    if (m.localId == machineLocalId) transform(m) else m
                },
            )
        }
    }

    private fun updateStep(stepLocalId: String, transform: (ProductRouteStepUi) -> ProductRouteStepUi) {
        _uiState.update { state ->
            state.copy(
                steps = state.steps.map { step ->
                    if (step.localId == stepLocalId) transform(step) else step
                },
            )
        }
    }

    fun requestRemoveMachine(stepLocalId: String, machineIndex: Int) {
        val step = _uiState.value.steps.find { it.localId == stepLocalId } ?: return
        val machine = step.machines.getOrNull(machineIndex) ?: return
        if (machine.id != null) {
            _uiState.update { it.copy(pendingDeleteMachine = stepLocalId to machineIndex) }
        } else {
            removeMachineAt(stepLocalId, machineIndex)
        }
    }

    fun cancelRemoveMachine() = _uiState.update { it.copy(pendingDeleteMachine = null) }

    fun confirmRemoveMachine() {
        val pending = _uiState.value.pendingDeleteMachine ?: return
        _uiState.update { it.copy(pendingDeleteMachine = null, actionLoading = true) }
        viewModelScope.launch {
            runCatching {
                val step = _uiState.value.steps.find { it.localId == pending.first }
                val machine = step?.machines?.getOrNull(pending.second)
                if (machine?.id != null) {
                    repository.deleteProductRouteMachine(machine.id)
                }
                removeMachineAt(pending.first, pending.second)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "設備削除成功") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "設備削除に失敗しました") }
            }
        }
    }

    private fun removeMachineAt(stepLocalId: String, machineIndex: Int) {
        updateStep(stepLocalId) { step ->
            step.copy(machines = step.machines.filterIndexed { i, _ -> i != machineIndex })
        }
    }

    fun saveMachine(stepLocalId: String, machineLocalId: String) {
        val state = _uiState.value
        val step = state.steps.find { it.localId == stepLocalId } ?: return
        val machine = step.machines.find { it.localId == machineLocalId } ?: return
        if (machine.machineCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "先に設備を選択してください") }
            return
        }
        val body = MasterProductRouteMachineBodyDto(
            productCd = step.productCd,
            routeCd = step.routeCd,
            stepNo = step.stepNo,
            machineCd = machine.machineCd,
            machineName = machine.machineName,
            processTimeSec = machine.processTimeSec.toDouble(),
            setupTime = machine.setupTime.toDouble(),
        )
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                if (machine.id != null) {
                    repository.updateProductRouteMachine(machine.id, body)
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "設備更新成功") }
                } else {
                    val newId = repository.createProductRouteMachine(body)
                    updateMachine(stepLocalId, machineLocalId) { it.copy(id = newId) }
                    _uiState.update { it.copy(actionLoading = false, snackbarMessage = "設備追加成功") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "設備操作に失敗しました") }
            }
        }
    }

    fun saveSteps() {
        val state = _uiState.value
        if (!state.dataLoaded) {
            _uiState.update { it.copy(snackbarMessage = "データの読み込みが完了していません") }
            return
        }
        if (state.steps.any { it.processCd.isBlank() }) {
            _uiState.update { it.copy(snackbarMessage = "無効な工程ステップが存在します") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val payload = state.steps.map { it.toBulkDto() }
                repository.saveProductRouteStepsBulk(payload)
                loadProductRoute(state.selectedProductCd)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "保存成功！") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "保存に失敗しました") }
            }
        }
    }

    fun requestReset() = _uiState.update { it.copy(pendingReset = true) }

    fun cancelReset() = _uiState.update { it.copy(pendingReset = false) }

    fun confirmReset() {
        _uiState.update { it.copy(pendingReset = false) }
        val cd = _uiState.value.selectedProductCd
        if (cd.isNotBlank()) loadProductRoute(cd)
        _uiState.update { it.copy(snackbarMessage = "データがリセットされました") }
    }

    fun machinesForProcess(processName: String): List<MasterMachineFullDto> {
        val all = _uiState.value.allMachines
        val filtered = all.filter { it.machineType == processName }
        return if (filtered.isNotEmpty()) filtered else all
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductProcessRouteMasterViewModel(repository) as T
    }
}

private fun MasterProductRouteStepDto.toUi(): ProductRouteStepUi = ProductRouteStepUi(
    id = id,
    productCd = productCd.orEmpty(),
    routeCd = routeCd.orEmpty(),
    stepNo = stepNo ?: 0,
    processCd = processCd.orEmpty(),
    processName = processName.orEmpty(),
    machines = machines.orEmpty().map {
        ProductRouteMachineUi(
            id = it.id,
            machineCd = it.machineCd.orEmpty(),
            machineName = it.machineName.orEmpty(),
            processTimeSec = it.processTimeSec?.toInt() ?: 0,
            setupTime = it.setupTime ?: 0,
        )
    },
)

private fun ProductRouteStepUi.toBulkDto(): MasterProductRouteStepBulkItemDto = MasterProductRouteStepBulkItemDto(
    id = id,
    productCd = productCd,
    routeCd = routeCd,
    stepNo = stepNo,
    processCd = processCd,
    processName = processName,
    machines = machines
        .filter { it.machineCd.isNotBlank() }
        .map {
            MasterProductRouteStepBulkMachineDto(
                machineCd = it.machineCd,
                machineName = it.machineName,
                processTimeSec = it.processTimeSec.toDouble(),
                setupTime = it.setupTime.toDouble(),
            )
        },
)
