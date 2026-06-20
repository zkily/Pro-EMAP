package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.data.repository.OutsourcingRepository
import com.example.smart_emap.data.repository.OutsourcingSupplierFilters
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OutsourcingSupplierFormUi(
    val id: Int? = null,
    val supplierCd: String = "",
    val supplierName: String = "",
    val supplierType: String = "",
    val address: String = "",
    val phone: String = "",
    val fax: String = "",
    val contactPerson: String = "",
    val email: String = "",
    val paymentTerms: String = "",
    val leadTimeDays: Int = 7,
    val remarks: String = "",
    val isActive: Boolean = true,
)

data class OutsourcingSuppliersUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val filterType: String = "",
    val filterIsActive: Boolean? = true,
    val keyword: String = "",
    val suppliers: List<OutsourcingSupplierDto> = emptyList(),
    val showFormDialog: Boolean = false,
    val isEdit: Boolean = false,
    val form: OutsourcingSupplierFormUi = OutsourcingSupplierFormUi(),
    val duplicateCodeError: String = "",
    val showDeleteConfirm: Boolean = false,
    val pendingDelete: OutsourcingSupplierDto? = null,
    val togglingId: Int? = null,
    val snackbarMessage: String? = null,
)

class OutsourcingSuppliersViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutsourcingSuppliersUiState())
    val uiState: StateFlow<OutsourcingSuppliersUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました") }
                }
        }
    }

    fun setFilterType(value: String) {
        _uiState.update { it.copy(filterType = value) }
        debounceLoad()
    }

    fun setFilterIsActive(value: Boolean?) {
        _uiState.update { it.copy(filterIsActive = value) }
        debounceLoad()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
        debounceLoad()
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEdit = false,
                form = OutsourcingSupplierFormUi(),
                duplicateCodeError = "",
            )
        }
    }

    fun openEditDialog(row: OutsourcingSupplierDto) {
        _uiState.update {
            it.copy(
                showFormDialog = true,
                isEdit = true,
                duplicateCodeError = "",
                form = OutsourcingSupplierFormUi(
                    id = row.id,
                    supplierCd = row.supplierCd.orEmpty(),
                    supplierName = row.supplierName.orEmpty(),
                    supplierType = row.supplierType.orEmpty(),
                    address = row.address.orEmpty(),
                    phone = row.phone.orEmpty(),
                    fax = row.fax.orEmpty(),
                    contactPerson = row.contactPerson.orEmpty(),
                    email = row.email.orEmpty(),
                    paymentTerms = row.paymentTerms.orEmpty(),
                    leadTimeDays = row.leadTimeDays ?: 7,
                    remarks = row.remarks.orEmpty(),
                    isActive = row.isActive ?: true,
                ),
            )
        }
    }

    fun dismissFormDialog() = _uiState.update { it.copy(showFormDialog = false, duplicateCodeError = "") }

    fun updateForm(transform: (OutsourcingSupplierFormUi) -> OutsourcingSupplierFormUi) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun checkSupplierCode() {
        val state = _uiState.value
        if (state.isEdit || state.form.supplierCd.length < 2) {
            _uiState.update { it.copy(duplicateCodeError = "") }
            return
        }
        viewModelScope.launch {
            runCatching {
                val exists = repository.loadSuppliers(OutsourcingSupplierFilters())
                    .any { it.supplierCd.equals(state.form.supplierCd, ignoreCase = true) }
                _uiState.update {
                    it.copy(
                        duplicateCodeError = if (exists) {
                            "外注先コード「${state.form.supplierCd}」は既に使用されています"
                        } else {
                            ""
                        },
                    )
                }
            }
        }
    }

    fun submitForm() {
        val state = _uiState.value
        val form = state.form
        if (!state.isEdit && state.duplicateCodeError.isNotBlank()) {
            _uiState.update { it.copy(snackbarMessage = state.duplicateCodeError) }
            return
        }
        if (form.supplierCd.isBlank() || form.supplierName.isBlank() || form.supplierType.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val body = mapOf(
                "supplier_cd" to form.supplierCd.trim(),
                "supplier_name" to form.supplierName.trim(),
                "supplier_type" to form.supplierType,
                "address" to form.address.trim().ifBlank { null },
                "phone" to form.phone.trim().ifBlank { null },
                "fax" to form.fax.trim().ifBlank { null },
                "contact_person" to form.contactPerson.trim().ifBlank { null },
                "email" to form.email.trim().ifBlank { null },
                "payment_terms" to form.paymentTerms.trim().ifBlank { null },
                "lead_time_days" to form.leadTimeDays,
                "remarks" to form.remarks.trim().ifBlank { null },
                "is_active" to form.isActive,
            )
            runCatching {
                if (state.isEdit && form.id != null) {
                    repository.updateSupplier(form.id, body)
                    _uiState.update { it.copy(actionLoading = false, showFormDialog = false, snackbarMessage = "更新しました") }
                } else {
                    repository.createSupplier(body)
                    _uiState.update { it.copy(actionLoading = false, showFormDialog = false, snackbarMessage = "登録しました") }
                }
                loadInternal()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: if (state.isEdit) "更新に失敗しました" else "登録に失敗しました")
                }
            }
        }
    }

    fun requestDelete(row: OutsourcingSupplierDto) {
        _uiState.update { it.copy(showDeleteConfirm = true, pendingDelete = row) }
    }

    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false, pendingDelete = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDelete?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showDeleteConfirm = false) }
            runCatching {
                repository.deleteSupplier(id)
                _uiState.update { it.copy(actionLoading = false, pendingDelete = null, snackbarMessage = "削除しました") }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun toggleStatus(row: OutsourcingSupplierDto, active: Boolean) {
        val id = row.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(togglingId = id) }
            val body = mapOf(
                "supplier_cd" to row.supplierCd,
                "supplier_name" to row.supplierName,
                "supplier_type" to row.supplierType,
                "address" to row.address,
                "phone" to row.phone,
                "fax" to row.fax,
                "contact_person" to row.contactPerson,
                "email" to row.email,
                "payment_terms" to row.paymentTerms,
                "lead_time_days" to row.leadTimeDays,
                "remarks" to row.remarks,
                "is_active" to active,
            )
            runCatching {
                repository.updateSupplier(id, body)
                _uiState.update {
                    it.copy(
                        togglingId = null,
                        snackbarMessage = if (active) "有効にしました" else "無効にしました",
                    )
                }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(togglingId = null, snackbarMessage = e.message ?: "状態の更新に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun resetFilters() {
        _uiState.update {
            it.copy(filterType = "", filterIsActive = true, keyword = "")
        }
        debounceLoad()
    }

    private fun debounceLoad() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
            loadInternal()
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val list = repository.loadSuppliers(
            OutsourcingSupplierFilters(
                type = state.filterType.takeIf { it.isNotBlank() },
                isActive = state.filterIsActive,
                keyword = state.keyword,
            ),
        )
        _uiState.update { it.copy(isLoading = false, suppliers = list) }
    }

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OutsourcingSuppliersViewModel(repository) as T
    }
}

val OUTSOURCING_SUPPLIER_TYPE_OPTIONS = listOf(
    "" to "すべて",
    "plating" to "メッキ",
    "welding" to "溶接",
    "cutting" to "切断",
    "forming" to "成型",
    "parts_processing" to "部品加工",
)

val OUTSOURCING_SUPPLIER_FORM_TYPE_OPTIONS = OUTSOURCING_SUPPLIER_TYPE_OPTIONS.filter { it.first.isNotBlank() }
