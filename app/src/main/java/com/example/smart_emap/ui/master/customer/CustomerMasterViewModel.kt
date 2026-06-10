package com.example.smart_emap.ui.master.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterCustomerDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun customerDefaultFormValues(): Map<String, String> = mapOf(
    "customer_cd" to "",
    "customer_name" to "",
    "customer_type" to "",
    "phone" to "",
    "address" to "",
    "status" to "1",
)

fun MasterCustomerDto.toCustomerFormValues(): Map<String, String> = mapOf(
    "customer_cd" to customerCd.orEmpty(),
    "customer_name" to customerName.orEmpty(),
    "customer_type" to customerType.orEmpty(),
    "phone" to phone.orEmpty(),
    "address" to address.orEmpty(),
    "status" to (status?.toString() ?: "1"),
)

data class CustomerMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val statusFilter: String = "",
    val typeFilter: String = "",
    val customers: List<MasterCustomerDto> = emptyList(),
    val total: Int = 0,
    val showForm: Boolean = false,
    val editingCustomer: MasterCustomerDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val statusUpdatingIds: Set<Int> = emptySet(),
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
)

class CustomerMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomerMasterUiState())
    val uiState: StateFlow<CustomerMasterUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val (customers, total) = repository.loadCustomers(
                    keyword = state.keyword,
                    status = state.statusFilter.toIntOrNull(),
                    customerType = state.typeFilter.takeIf { it.isNotBlank() },
                )
                _uiState.update { it.copy(isLoading = false, customers = customers, total = total) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun setStatusFilter(value: String) = _uiState.update { it.copy(statusFilter = value) }

    fun setTypeFilter(value: String) = _uiState.update { it.copy(typeFilter = value) }

    fun search() = refreshAll()

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", statusFilter = "", typeFilter = "") }
        refreshAll()
    }

    fun hasActiveFilters(): Boolean {
        val s = _uiState.value
        return s.keyword.isNotBlank() || s.statusFilter.isNotBlank() || s.typeFilter.isNotBlank()
    }

    fun displayedCustomers(state: CustomerMasterUiState): List<MasterCustomerDto> {
        var list = state.customers
        if (state.keyword.isNotBlank()) {
            val k = state.keyword.lowercase()
            list = list.filter {
                it.customerCd.orEmpty().lowercase().contains(k) ||
                    it.customerName.orEmpty().lowercase().contains(k) ||
                    it.phone.orEmpty().lowercase().contains(k)
            }
        }
        if (state.statusFilter.isNotBlank()) {
            val status = state.statusFilter.toIntOrNull()
            if (status != null) list = list.filter { it.status == status }
        }
        if (state.typeFilter.isNotBlank()) {
            list = list.filter { it.customerType == state.typeFilter }
        }
        return list
    }

    fun openCreate() {
        _uiState.update {
            it.copy(showForm = true, editingCustomer = null, formValues = customerDefaultFormValues())
        }
    }

    fun openEdit(customer: MasterCustomerDto) {
        _uiState.update {
            it.copy(showForm = true, editingCustomer = customer, formValues = customer.toCustomerFormValues())
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingCustomer = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching {
                repository.deleteCustomerMaster(id)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun toggleStatus(customer: MasterCustomerDto, active: Boolean) {
        val id = customer.id ?: return
        val next = if (active) 1 else 0
        viewModelScope.launch {
            _uiState.update { it.copy(statusUpdatingIds = it.statusUpdatingIds + id) }
            runCatching {
                repository.updateCustomerStatus(id, next)
                _uiState.update { state ->
                    state.copy(
                        statusUpdatingIds = state.statusUpdatingIds - id,
                        customers = state.customers.map { if (it.id == id) it.copy(status = next) else it },
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
        val cd = state.formValues["customer_cd"].orEmpty().trim()
        val name = state.formValues["customer_name"].orEmpty().trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "顧客CDを入力してください") }
                return
            }
            name.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "顧客名を入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveCustomerMaster(state.editingCustomer?.id, state.formValues)
                if (ok) {
                    val isEdit = state.editingCustomer != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingCustomer = null,
                            snackbarMessage = if (isEdit) "更新しました" else "作成しました",
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CustomerMasterViewModel(repository) as T
    }
}
