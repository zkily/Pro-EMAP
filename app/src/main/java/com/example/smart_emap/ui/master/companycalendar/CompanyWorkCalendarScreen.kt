package com.example.smart_emap.ui.master.companycalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.CompanyWorkCalendarItemDto
import com.example.smart_emap.ui.erp.production.planning.ProductionBeautifulDatePickerDialog
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors
import com.example.smart_emap.ui.shell.LayoutColors
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CompanyWorkCalendarScreen(viewModel: CompanyWorkCalendarViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    if (showDatePicker) {
        ProductionBeautifulDatePickerDialog(
            value = uiState.pendingDates.lastOrNull().orEmpty(),
            title = "日付を追加",
            onDismiss = { showDatePicker = false },
            onConfirm = { viewModel.addPendingDate(it); showDatePicker = false },
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        ProductionPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CwcHeroBar(
                    monthLabel = uiState.monthLabel,
                    scheduled = uiState.scheduledCount,
                    total = uiState.totalDays,
                    count = uiState.items.size,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::loadMonth,
                )
                CwcToolbar(
                    monthYm = uiState.monthYm,
                    dayTypes = uiState.dayTypeOptions,
                    selectedDayType = uiState.selectedDayType,
                    pendingDates = uiState.pendingDates,
                    saving = uiState.isSaving,
                    onMonthChange = viewModel::setMonthYm,
                    onDayTypeChange = viewModel::setSelectedDayType,
                    onAddDate = { showDatePicker = true },
                    onRemoveDate = viewModel::removePendingDate,
                    onSubmit = viewModel::submitBatch,
                )
                CwcTableCard(items = uiState.items, loading = uiState.isLoading, onDelete = viewModel::deleteEntry)
                CwcNoteCard()
            }
        }
    }
}

@Composable
private fun CwcHeroBar(
    monthLabel: String,
    scheduled: Int,
    total: Int,
    count: Int,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFEFF6FF), Color(0xFFF8FAFC))))
            .border(1.dp, Color(0x332563EB), shape)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB)) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.padding(8.dp).size(18.dp))
            }
            Column {
                Text("マスタ · 会社稼働カレンダー", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Text("祝日・有給・会社休・臨時出勤", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("未登録日は月〜金=通常稼働日", fontSize = 9.sp, color = Color(0xFF64748B))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$monthLabel · $scheduled/$total · ${count}件", fontSize = 9.sp, color = Color(0xFF475569))
            IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
            }
            if (loading) CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF2563EB))
        }
    }
}

@Composable
private fun CwcToolbar(
    monthYm: String,
    dayTypes: List<Pair<String, String>>,
    selectedDayType: String,
    pendingDates: List<String>,
    saving: Boolean,
    onMonthChange: (String) -> Unit,
    onDayTypeChange: (String) -> Unit,
    onAddDate: () -> Unit,
    onRemoveDate: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val monthOptions = remember {
        val now = YearMonth.now()
        (0 until 24).map { offset ->
            val ym = now.minusMonths(offset.toLong())
            ym.format(DateTimeFormatter.ofPattern("yyyy-MM")) to "${ym.year}年${ym.monthValue}月"
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ProductionDropdownFilter("対象月", monthYm, monthOptions, onMonthChange, Modifier.fillMaxWidth())
        ProductionDropdownFilter("区分", selectedDayType, dayTypes, onDayTypeChange, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            pendingDates.forEach { d ->
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEFF6FF)) {
                    Row(modifier = Modifier.padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(d, fontSize = 9.sp, color = Color(0xFF2563EB))
                        IconButton(onClick = { onRemoveDate(d) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDBEAFE), modifier = Modifier.clickable(onClick = onAddDate)) {
                Text("+ 日付", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, color = Color(0xFF2563EB))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            val enabled = !saving && pendingDates.isNotEmpty()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (enabled) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                modifier = Modifier.clickable(enabled = enabled, onClick = onSubmit),
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (saving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    else Text("追加", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CwcTableCard(
    items: List<CompanyWorkCalendarItemDto>,
    loading: Boolean,
    onDelete: (Int?) -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(8.dp),
    ) {
        Text("登録一覧", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
        if (loading && items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(20.dp), strokeWidth = 2.dp)
        } else if (items.isEmpty()) {
            Text("登録がありません", fontSize = 11.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(8.dp))
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))))
                    .padding(vertical = 4.dp),
            ) {
                listOf("日付" to 88, "区分" to 64, "稼働" to 40, "名称" to 80).forEach { (h, w) ->
                    Text(h, modifier = Modifier.padding(horizontal = 4.dp).then(Modifier), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            items.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                Row(
                    modifier = Modifier.fillMaxWidth().background(bg).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.calendarDate.orEmpty(), modifier = Modifier.padding(horizontal = 4.dp), fontSize = 9.sp, color = Color(0xFF334155))
                    Text(item.dayTypeLabel ?: item.dayType.orEmpty(), modifier = Modifier.padding(horizontal = 4.dp), fontSize = 9.sp, color = tagColor(item.dayType))
                    Text(if (item.isScheduled == true) "○" else "—", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 9.sp)
                    Text(item.name.orEmpty().ifBlank { "—" }, modifier = Modifier.weight(1f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CwcNoteCard() {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(8.dp),
    ) {
        Text("反映先", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text("· MES 検査工程 稼働率分析（通常稼働日・カレンダー率）", fontSize = 9.sp, color = Color(0xFF64748B))
        Text("· 分析画面の臨時指定は会社カレンダーに上書き追加", fontSize = 9.sp, color = Color(0xFF64748B))
        Text("· 納入先休日は顧客交期用（別管理）", fontSize = 9.sp, color = Color(0xFF64748B))
    }
}

private fun tagColor(dayType: String?): Color = when (dayType) {
    "extra_workday" -> Color(0xFFEA580C)
    "national_holiday" -> Color(0xFFDC2626)
    "paid_leave" -> Color(0xFF7C3AED)
    else -> Color(0xFF64748B)
}
