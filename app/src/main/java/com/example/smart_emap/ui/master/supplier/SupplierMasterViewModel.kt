package com.example.smart_emap.ui.master.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterSupplierDto
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun supplierDefaultFormValues(): Map<String, String> = mapOf(
    "supplier_cd" to "",
    "supplier_name" to "",
    "supplier_kana" to "",
    "contact_person" to "",
    "phone" to "",
    "fax" to "",
    "email" to "",
    "postal_code" to "",
    "address1" to "",
    "address2" to "",
    "payment_terms" to "",
    "currency" to "JPY",
    "remarks" to "",
)

fun MasterSupplierDto.toSupplierFormValues(): Map<String, String> = mapOf(
    "supplier_cd" to supplierCd.orEmpty(),
    "supplier_name" to supplierName.orEmpty(),
    "supplier_kana" to supplierKana.orEmpty(),
    "contact_person" to contactPerson.orEmpty(),
    "phone" to phone.orEmpty(),
    "fax" to fax.orEmpty(),
    "email" to email.orEmpty(),
    "postal_code" to postalCode.orEmpty(),
    "address1" to address1.orEmpty(),
    "address2" to address2.orEmpty(),
    "payment_terms" to paymentTerms.orEmpty(),
    "currency" to currency.orEmpty().ifBlank { "JPY" },
    "remarks" to remarks.orEmpty(),
)

data class SupplierMasterUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val keyword: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val suppliers: List<MasterSupplierDto> = emptyList(),
    val showForm: Boolean = false,
    val editingSupplier: MasterSupplierDto? = null,
    val formValues: Map<String, String> = emptyMap(),
    val pendingDeleteId: Int? = null,
    val snackbarMessage: String? = null,
)

class SupplierMasterViewModel(
    private val repository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierMasterUiState())
    val uiState: StateFlow<SupplierMasterUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val (suppliers, total) = repository.loadSuppliers(state.keyword, state.page, state.pageSize)
                _uiState.update { it.copy(isLoading = false, suppliers = suppliers, total = total) }
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

    fun clearFilters() {
        _uiState.update { it.copy(keyword = "", page = 1) }
        refreshAll()
    }

    fun hasActiveFilters(): Boolean = _uiState.value.keyword.isNotBlank()

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        refreshAll()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        refreshAll()
    }

    fun openCreate() {
        _uiState.update {
            it.copy(showForm = true, editingSupplier = null, formValues = supplierDefaultFormValues())
        }
    }

    fun openEdit(supplier: MasterSupplierDto) {
        _uiState.update {
            it.copy(showForm = true, editingSupplier = supplier, formValues = supplier.toSupplierFormValues())
        }
    }

    fun setFormValue(key: String, value: String) {
        _uiState.update { it.copy(formValues = it.formValues + (key to value)) }
    }

    fun closeForm() = _uiState.update { it.copy(showForm = false, editingSupplier = null) }

    fun requestDelete(id: Int) = _uiState.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, pendingDeleteId = null) }
            runCatching {
                repository.deleteSupplierMaster(id)
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "削除しました") }
                refreshAll()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val cd = state.formValues["supplier_cd"].orEmpty().trim()
        val name = state.formValues["supplier_name"].orEmpty().trim()
        when {
            cd.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "仕入先CDを入力してください") }
                return
            }
            name.isEmpty() -> {
                _uiState.update { it.copy(snackbarMessage = "仕入先名を入力してください") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val ok = repository.saveSupplierMaster(state.editingSupplier?.id, state.formValues)
                if (ok) {
                    val isEdit = state.editingSupplier != null
                    _uiState.update {
                        it.copy(
                            actionLoading = false,
                            showForm = false,
                            editingSupplier = null,
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SupplierMasterViewModel(repository) as T
    }
}
