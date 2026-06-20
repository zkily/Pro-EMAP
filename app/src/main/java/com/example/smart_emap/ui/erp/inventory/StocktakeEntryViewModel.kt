package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InventoryLogCreateBodyDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class StocktakeEntryFormUi(
    val item: String = "",
    val productCd: String = "",
    val productName: String = "",
    val processCd: String = "",
    val logDate: String = LocalDate.now().toString(),
    val logTime: String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
    val hdNo: String = "",
    val packQty: String = "",
    val caseQty: String = "",
    val quantity: String = "",
    val remarks: String = "",
)

data class StocktakeEntryUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val form: StocktakeEntryFormUi = StocktakeEntryFormUi(),
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    val processOptions: List<Pair<String, String>> = emptyList(),
    val snackbarMessage: String? = null,
)

class StocktakeEntryViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StocktakeEntryUiState())
    val uiState: StateFlow<StocktakeEntryUiState> = _uiState.asStateFlow()

    init {
        refreshOptions()
    }

    fun refreshOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val products = repository.loadProductOptions()
                val processes = repository.loadProcessOptions()
                _uiState.update {
                    it.copy(isLoading = false, productOptions = products, processOptions = processes)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "オプションの取得に失敗しました") }
            }
        }
    }

    fun setItem(value: String) {
        _uiState.update { it.copy(form = it.form.copy(item = value, productCd = "", productName = "")) }
    }

    fun updateForm(transform: (StocktakeEntryFormUi) -> StocktakeEntryFormUi) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun selectProduct(productCd: String) {
        val product = _uiState.value.productOptions.find { it.productCd == productCd }
        _uiState.update {
            it.copy(
                form = it.form.copy(
                    productCd = productCd,
                    productName = product?.productName.orEmpty(),
                ),
            )
        }
    }

    fun submit() {
        val form = _uiState.value.form
        if (form.item.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "項目を選択してください") }
            return
        }
        if (form.productCd.isBlank() || form.productName.isBlank() || form.processCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        val qty = form.quantity.toIntOrNull()
        if (qty == null) {
            _uiState.update { it.copy(snackbarMessage = "数量を正しく入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val body = InventoryLogCreateBodyDto(
                item = form.item,
                productCd = form.productCd.trim(),
                productName = form.productName.trim(),
                processCd = form.processCd,
                logDate = form.logDate,
                logTime = form.logTime,
                hdNo = form.hdNo.trim().ifBlank { null },
                packQty = form.packQty.toIntOrNull(),
                caseQty = form.caseQty.toIntOrNull(),
                quantity = qty,
                remarks = form.remarks.trim().ifBlank { null },
            )
            runCatching {
                repository.createInventoryLog(body)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = "棚卸を登録しました",
                        form = StocktakeEntryFormUi(item = form.item),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "登録に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StocktakeEntryViewModel(repository) as T
    }
}
