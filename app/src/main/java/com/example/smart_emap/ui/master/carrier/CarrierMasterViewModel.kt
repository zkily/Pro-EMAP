package com.example.smart_emap.ui.master.carrier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterCarrierDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun carrierDefaultFormValues(): Map<String, String> = mapOf(
    "carrier_cd" to "",
    "carrier_name" to "",
    "contact_person" to "",
    "phone" to "",
    "shipping_time" to "",
    "report_no" to "",
    "note" to "",
    "status" to "1",
)

fun MasterCarrierDto.toCarrierFormValues(): Map<String, String> = mapOf(
    "carrier_cd" to carrierCd.orEmpty(),
    "carrier_name" to carrierName.orEmpty(),
    "contact_person" to contactPerson.orEmpty(),
    "phone" to phone.orEmpty(),
    "shipping_time" to formatCarrierShippingTime(shippingTime),
    "report_no" to reportNo.orEmpty(),
    "note" to note.orEmpty(),
    "status" to (status?.toString() ?: "1"),
)

fun formatCarrierShippingTime(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return ""
    return if (value.length >= 5) value.take(5) else value
}

data class CarrierMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val statusFilter: String = "",
    val carriers: List<MasterCarrierDto> = emptyList(),
    val total: Int = 0,
    val showForm: Boolean = false,
    val editingCarrier: MasterCarrierDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val statusUpdatingIds: Set<Int> = emptySet(),
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
)

class CarrierMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CarrierMasterUiState())
    val uiState: StateFlow<CarrierMasterUiState> = _uiState.asStateFlow()
    private var keywordJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val (carriers, total) = repository.loadCarriers(
                    keyword = state.keyword,
                    status = state.statusFilter.toIntOrNull(),
                )
                _uiState.update { it.copy(isLoading = false, carriers = carriers, total = total) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(320)
            refreshAll()
        }
    }

    fun setStatusFilter(value: String) {
        _uiState.update { it.copy(statusFilter = value) }
        refreshAll()
    }

    fun search() = refreshAll()

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", statusFilter = "") }
        refreshAll()
    }

    fun displayedCarriers(state: CarrierMasterUiState): List<MasterCarrierDto> = state.carriers

    fun activeCount(state: CarrierMasterUiState): Int = state.carriers.count { it.status == 1 }

    fun openCreate() {
        _uiState.update {
            it.copy(showForm = true, editingCarrier = null, formValues = carrierDefaultFormValues())
        }
    }

    fun openEdit(carrier: MasterCarrierDto) {
        _uiState.update {
            it.copy(showForm = true, editingCarrier = carrier, formValues = carrier.toCarrierFormValues())
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingCarrier = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching {
                repository.deleteCarrierMaster(id)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun toggleStatus(carrier: MasterCarrierDto, active: Boolean) {
        val id = carrier.id ?: return
        val next = if (active) 1 else 0
        viewModelScope.launch {
            _uiState.update { it.copy(statusUpdatingIds = it.statusUpdatingIds + id) }
            runCatching {
                repository.updateCarrierStatus(id, next)
                _uiState.update { state ->
                    state.copy(
                        statusUpdatingIds = state.statusUpdatingIds - id,
                        carriers = state.carriers.map { if (it.id == id) it.copy(status = next) else it },
                        snackbarMessage = "更新しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(statusUpdatingIds = it.statusUpdatingIds - id, snackbarMessage = e.message ?: "更新に失敗しました")
                }
            }
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.formValues["carrier_cd"].orEmpty().trim()
        val name = state.formValues["carrier_name"].orEmpty().trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "運送便CDを入力してください") }
                return
            }
            name.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "運送便名称を入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val shipping = state.formValues["shipping_time"].orEmpty().trim()
                val normalized = state.formValues.toMutableMap().apply {
                    if (shipping.matches(Regex("^\\d{2}:\\d{2}$"))) {
                        this["shipping_time"] = "$shipping:00"
                    }
                }
                val ok = repository.saveCarrierMaster(state.editingCarrier?.id, normalized)
                if (ok) {
                    val isEdit = state.editingCarrier != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingCarrier = null,
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

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: MasterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CarrierMasterViewModel(repository) as T
    }
}
