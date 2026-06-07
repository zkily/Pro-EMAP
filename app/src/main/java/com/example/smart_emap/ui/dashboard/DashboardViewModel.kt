package com.example.smart_emap.ui.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.DailyConfirmedSeriesDto
import com.example.smart_emap.data.repository.DashboardRepository
import com.example.smart_emap.ui.theme.LoginColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class StatCardUi(
    val key: String,
    val label: String,
    val value: String,
    val gradientStart: Color,
    val gradientEnd: Color,
)

data class QuickAccessUi(
    val route: String,
    val title: String,
    val description: String,
    val gradientStart: Color,
    val gradientEnd: Color,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentDateTime: String = "",
    val statCards: List<StatCardUi> = emptyList(),
    val dailySeries: DailyConfirmedSeriesDto? = null,
    val quickAccessItems: List<QuickAccessUi> = defaultQuickAccess(),
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val username: String,
) : ViewModel() {
    private val jstFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    private val numberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshDateTime()
        loadDashboard()
        viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                refreshDateTime()
            }
        }
    }

    fun refreshDateTime() {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        _uiState.update { it.copy(currentDateTime = now.format(jstFormatter)) }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            dashboardRepository.loadDashboard().fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statCards = buildStatCards(data),
                            dailySeries = data.dailySeries,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "データの取得に失敗しました",
                            statCards = buildStatCardsEmpty(),
                        )
                    }
                },
            )
        }
    }

    fun welcomeTitle(): String = "おかえりなさい、${username}さん"

    private fun buildStatCards(data: com.example.smart_emap.data.repository.DashboardData): List<StatCardUi> {
        val sales = data.sales
        val inventory = data.inventory
        val products = data.activeProducts
        return listOf(
            StatCardUi(
                key = "sales",
                label = "今月売上",
                value = "¥${numberFormat.format(sales.monthlyOrderAmount.toLong())}",
                gradientStart = LoginColors.Primary,
                gradientEnd = LoginColors.PrimaryDark,
            ),
            StatCardUi(
                key = "orders",
                label = "今月受注（確定本数）",
                value = numberFormat.format(sales.monthlyConfirmedUnits),
                gradientStart = Color(0xFFF43F5E),
                gradientEnd = Color(0xFFE11D48),
            ),
            StatCardUi(
                key = "inventory",
                label = "在庫数量",
                value = numberFormat.format(inventory.summaryStockQtyToday ?: 0),
                gradientStart = LoginColors.MesBadgeStart,
                gradientEnd = LoginColors.MesBadgeEnd,
            ),
            StatCardUi(
                key = "products",
                label = "アクティブ製品",
                value = numberFormat.format(products.activeCount),
                gradientStart = Color(0xFF10B981),
                gradientEnd = Color(0xFF059669),
            ),
        )
    }

    private fun buildStatCardsEmpty(): List<StatCardUi> = listOf(
        StatCardUi("sales", "今月売上", "¥0", LoginColors.Primary, LoginColors.PrimaryDark),
        StatCardUi("orders", "今月受注（確定本数）", "0", Color(0xFFF43F5E), Color(0xFFE11D48)),
        StatCardUi("inventory", "在庫数量", "0", LoginColors.MesBadgeStart, LoginColors.MesBadgeEnd),
        StatCardUi("products", "アクティブ製品", "0", Color(0xFF10B981), Color(0xFF059669)),
    )

    class Factory(
        private val dashboardRepository: DashboardRepository,
        private val username: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(dashboardRepository, username) as T
        }
    }
}

private fun defaultQuickAccess(): List<QuickAccessUi> = listOf(
    QuickAccessUi("/erp/order/monthly", "月別受注", "月次受注の登録・照会", Color(0xFF667EEA), Color(0xFF764BA2)),
    QuickAccessUi("/erp/order/daily", "日別受注", "日別受注", Color(0xFF0EA5E9), Color(0xFF0284C7)),
    QuickAccessUi("/erp/shipping/list", "出荷構成表管理", "出荷構成表の作成・編集", Color(0xFFF43F5E), Color(0xFFE11D48)),
    QuickAccessUi("/erp/shipping/report", "出荷報告書管理", "出荷報告の登録・一覧", Color(0xFFFB7185), Color(0xFFDB2777)),
    QuickAccessUi("/erp/production/data-management", "生産データ管理", "実績・計画データの更新", Color(0xFF10B981), Color(0xFF059669)),
    QuickAccessUi("/erp/production/plan-schedules", "スケジューリング", "生産スケジュールの確認", Color(0xFF8B5CF6), Color(0xFF7C3AED)),
    QuickAccessUi("/aps/planning-list", "成型計画一覧", "APS 成型計画の一覧・照会", Color(0xFFA855F7), Color(0xFF9333EA)),
    QuickAccessUi("/aps/welding-planning-list", "溶接計画一覧", "APS 溶接計画の一覧・照会", Color(0xFF7C3AED), Color(0xFF6D28D9)),
    QuickAccessUi("/mes/productionInstruction/cutting", "切断・面取指示", "切断・面取の作業指示", Color(0xFFF59E0B), Color(0xFFD97706)),
    QuickAccessUi("/mes/productionInstruction/forming", "成型指示", "成型の作業指示", Color(0xFF06B6D4), Color(0xFF0891B2)),
    QuickAccessUi("/mes/productionInstruction/welding", "溶接指示", "溶接の作業指示", Color(0xFF6366F1), Color(0xFF4F46E5)),
    QuickAccessUi("/mes/actualDataCollection/cutting", "切断実績収集", "切断実績の登録・照会", Color(0xFFEA580C), Color(0xFFC2410C)),
    QuickAccessUi("/mes/actualDataCollection/chamfering", "面取実績収集", "面取実績の登録・照会", Color(0xFF14B8A6), Color(0xFF0D9488)),
    QuickAccessUi("/mes/actualDataCollection/welding", "溶接実績収集", "溶接実績の登録・照会", Color(0xFF4F46E5), Color(0xFF4338CA)),
    QuickAccessUi("/mes/actualDataCollection/inspection", "検査実績収集", "検査実績の登録・照会", Color(0xFFEC4899), Color(0xFFDB2777)),
)
