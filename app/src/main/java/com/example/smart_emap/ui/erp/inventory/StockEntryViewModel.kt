package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InventoryStockTransactionLogBodyDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class StockEntryFormUi(
    val stockType: String = "",
    val transactionType: String = "入庫",
    val targetCd: String = "",
    val locationCd: String = "",
    val processCd: String = "",
    val quantity: String = "",
    val unit: String = "本",
    val transactionTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
    val remarks: String = "",
)

data class StockEntryUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val form: StockEntryFormUi = StockEntryFormUi(),
    val processOptions: List<Pair<String, String>> = emptyList(),
    val locationOptions: List<Pair<String, String>> = emptyList(),
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    val snackbarMessage: String? = null,
)

class StockEntryViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockEntryUiState())
    val uiState: StateFlow<StockEntryUiState> = _uiState.asStateFlow()

    init {
        refreshOptions()
    }

    fun refreshOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val processes = repository.loadProcessOptions()
                val locations = repository.loadLocationOptions()
                val products = repository.loadProductOptions()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        processOptions = processes,
                        locationOptions = locations,
                        productOptions = products,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, snackbarMessage = e.message ?: "オプションの取得に失敗しました")
                }
            }
        }
    }

    fun setStockType(value: String) {
        _uiState.update {
            val unit = when (value) {
                "材料" -> "kg"
                "部品" -> "個"
                else -> "本"
            }
            it.copy(form = it.form.copy(stockType = value, targetCd = "", processCd = "", unit = unit))
        }
    }

    fun updateForm(transform: (StockEntryFormUi) -> StockEntryFormUi) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun submit() {
        val form = _uiState.value.form
        if (form.stockType.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "在庫種別を選択してください") }
            return
        }
        if (form.targetCd.isBlank() || form.locationCd.isBlank() || form.transactionType.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        if (form.stockType == "仕掛品" && form.processCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "仕掛品の場合は工程を選択してください") }
            return
        }
        val qty = form.quantity.toDoubleOrNull()
        if (qty == null || qty == 0.0) {
            _uiState.update { it.copy(snackbarMessage = "数量を正しく入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val body = InventoryStockTransactionLogBodyDto(
                stockType = form.stockType,
                transactionType = form.transactionType,
                targetCd = form.targetCd.trim(),
                locationCd = form.locationCd,
                quantity = qty,
                unit = form.unit.ifBlank { "本" },
                processCd = form.processCd.takeIf { it.isNotBlank() },
                transactionTime = form.transactionTime,
                remarks = form.remarks.trim().ifBlank { null },
            )
            runCatching {
                repository.createStockTransactionLog(body)
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = "在庫取引を登録しました",
                        form = StockEntryFormUi(
                            stockType = form.stockType,
                            transactionType = form.transactionType,
                            locationCd = form.locationCd,
                            unit = form.unit,
                        ),
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "登録に失敗しました")
                }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StockEntryViewModel(repository) as T
    }
}

val STOCK_ENTRY_UNIT_OPTIONS = listOf("本", "個", "kg", "束", "箱")
