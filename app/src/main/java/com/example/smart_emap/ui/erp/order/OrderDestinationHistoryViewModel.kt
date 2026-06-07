package com.example.smart_emap.ui.erp.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.repository.OrderDailyListFilters
import com.example.smart_emap.data.repository.OrderDailyRepository
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DestinationHistoryItemUi(
    val date: String,
    val destinationCd: String,
    val destinationName: String,
    val productCd: String,
    val productName: String,
    val quantity: Int,
    val status: String,
    val deliveryDate: String,
    val ym: String,
)

data class DestinationHistorySummaryUi(
    val ym: String,
    val totalQuantity: Int,
)

data class DestinationHistoryUiState(
    val isLoading: Boolean = false,
    val destinationOptions: List<DestinationOptionDto> = emptyList(),
    val destinationCd: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val allItems: List<DestinationHistoryItemUi> = emptyList(),
    val detailItems: List<DestinationHistoryItemUi> = emptyList(),
    val summaryItems: List<DestinationHistorySummaryUi> = emptyList(),
    val hasSearched: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class OrderDestinationHistoryViewModel(
    private val repository: OrderDailyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DestinationHistoryUiState())
    val uiState: StateFlow<DestinationHistoryUiState> = _uiState.asStateFlow()

    private val numberFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

    init {
        loadDestinationOptions()
    }

    fun loadDestinationOptions() {
        viewModelScope.launch {
            runCatching { repository.loadDestinationOptions() }
                .onSuccess { options ->
                    _uiState.update { it.copy(destinationOptions = options) }
                }
                .onFailure { showMessage("納入先一覧の取得に失敗しました") }
        }
    }

    fun setDestinationCd(value: String) {
        _uiState.update { it.copy(destinationCd = value) }
    }

    fun setStartDate(value: String) {
        _uiState.update { it.copy(startDate = value) }
    }

    fun setEndDate(value: String) {
        _uiState.update { it.copy(endDate = value) }
    }

    fun search() {
        val state = _uiState.value
        if (state.destinationCd.isBlank()) {
            showMessage("納入先を選択してください")
            return
        }
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            showMessage("期間を選択してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.loadList(
                    OrderDailyListFilters(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        destinationCd = state.destinationCd,
                    ),
                ).map { it.toHistoryItem() }
            }.onSuccess { items ->
                val details = items.filter { it.quantity > 0 }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSearched = true,
                        allItems = items,
                        detailItems = details,
                        summaryItems = buildSummary(details),
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(isLoading = false, hasSearched = true, allItems = emptyList(), detailItems = emptyList(), summaryItems = emptyList())
                }
                showMessage("データ取得に失敗しました")
            }
        }
    }

    fun preparePrint() {
        val state = _uiState.value
        if (state.detailItems.isEmpty()) {
            showMessage("印刷するデータがありません")
            return
        }
        val destLabel = state.destinationOptions.find { it.cd == state.destinationCd }?.let { "${it.cd} - ${it.name}" }
            ?: state.destinationCd
        _uiState.update {
            it.copy(
                pendingPrintHtml = buildPrintHtml(
                    items = state.detailItems,
                    summary = state.summaryItems,
                    destinationLabel = destLabel,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    numberFormat = numberFormat,
                ),
            )
        }
    }

    fun clearPendingPrintHtml() {
        _uiState.update { it.copy(pendingPrintHtml = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun buildSummary(items: List<DestinationHistoryItemUi>): List<DestinationHistorySummaryUi> {
        return items
            .groupBy { it.ym }
            .map { (ym, rows) -> DestinationHistorySummaryUi(ym = ym, totalQuantity = rows.sumOf { it.quantity }) }
            .sortedBy { it.ym }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    class Factory(
        private val repository: OrderDailyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderDestinationHistoryViewModel::class.java)) {
                return OrderDestinationHistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private fun OrderDailyItemDto.toHistoryItem(): DestinationHistoryItemUi {
    val d = date.orEmpty()
    return DestinationHistoryItemUi(
        date = d,
        destinationCd = destinationCd,
        destinationName = destinationName.orEmpty().ifBlank { destinationCd },
        productCd = productCd,
        productName = productName.orEmpty().ifBlank { productCd },
        quantity = confirmedUnits ?: 0,
        status = status.orEmpty(),
        deliveryDate = deliveryDate.orEmpty(),
        ym = if (d.length >= 7) d.take(7) else "",
    )
}

private fun buildPrintHtml(
    items: List<DestinationHistoryItemUi>,
    summary: List<DestinationHistorySummaryUi>,
    destinationLabel: String,
    startDate: String,
    endDate: String,
    numberFormat: NumberFormat,
): String {
    fun esc(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    val filterInfo = "検索条件: 期間: $startDate ~ $endDate / 納入先: ${esc(destinationLabel)}"
    val summaryRows = summary.joinToString("") { row ->
        """<tr><td class="center">${esc(row.ym)}</td><td class="number">${numberFormat.format(row.totalQuantity)}</td></tr>"""
    }
    val detailRows = items.joinToString("") { row ->
        """<tr>
            <td class="center">${esc(row.date)}</td>
            <td>${esc(row.destinationName)}</td>
            <td>${esc(row.productName)}</td>
            <td class="number">${numberFormat.format(row.quantity)}</td>
            <td class="center">${esc(row.status.ifBlank { "-" })}</td>
            <td class="center">${esc(row.deliveryDate.ifBlank { "-" })}</td>
        </tr>"""
    }
    return """
        <!DOCTYPE html><html><head><meta charset="utf-8"/>
        <title>納入先別受注履歴</title>
        <style>
          body{font-family:sans-serif;padding:16px;color:#0f172a}
          h2{color:#4f46e5;margin:0 0 8px}
          .filter{font-size:12px;color:#64748b;margin-bottom:12px}
          table{width:100%;border-collapse:collapse;margin-bottom:16px;font-size:12px}
          th,td{border:1px solid #e2e8f0;padding:6px 8px}
          th{background:#f8fafc;font-weight:700}
          .center{text-align:center}.number{text-align:right;font-family:monospace}
        </style></head><body>
        <h2>納入先別受注履歴</h2>
        <div class="filter">$filterInfo</div>
        <h3>月別集計</h3>
        <table><thead><tr><th>年月</th><th>受注数量合計</th></tr></thead><tbody>$summaryRows</tbody></table>
        <h3>受注明細</h3>
        <table><thead><tr><th>出荷日</th><th>納入先名</th><th>製品名</th><th>数量</th><th>状態</th><th>納入日</th></tr></thead>
        <tbody>$detailRows</tbody></table>
        </body></html>
    """.trimIndent()
}
