package com.example.smart_emap.ui.mes.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.ui.shell.LayoutColors
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val JST = ZoneId.of("Asia/Tokyo")
private val ymd = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun ProcessMonitorScreen(
    viewModel: ProcessMonitorViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.onPageEnter()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MonitorHeader(
                    processKey = uiState.processKey,
                    clockText = uiState.clockText,
                    productionDay = uiState.productionDay,
                    autoRefresh = uiState.autoRefresh,
                    onPrevDay = { shiftDay(uiState.productionDay, -1, viewModel::setProductionDay) },
                    onNextDay = { shiftDay(uiState.productionDay, 1, viewModel::setProductionDay) },
                    onToday = { viewModel.setProductionDay(MesCalendarUtils.jstToday()) },
                    onAutoRefreshChange = viewModel::setAutoRefresh,
                    onRefresh = viewModel::refreshAll,
                )

                MonitorKpiRow(
                    processKey = uiState.processKey,
                    stats = uiState.overallStats,
                )

                if (uiState.processKey == MonitorProcessKey.INSPECTION) {
                    MonitorSectionTitle("稼働中検査", uiState.summary.machines.size)
                    MonitorMachineGrid(uiState.summary.machines)

                    MonitorSectionTitle("不良（項目別）", uiState.summary.defectListRows.size)
                    MonitorDefectList(uiState.summary.defectListRows)
                } else {
                    MonitorSectionTitle("設備稼働", uiState.summary.machines.size)
                    MonitorMachineGrid(uiState.summary.machines)
                }

                MonitorSectionTitle("完了履歴", uiState.summary.historyRows.size)
                MonitorHistoryList(uiState.summary.historyRows)
            }

            if (uiState.isLoading && !uiState.hasInitialData) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun shiftDay(current: String, delta: Int, onSet: (String) -> Unit) {
    runCatching {
        val next = LocalDate.parse(current, ymd).plusDays(delta.toLong()).format(ymd)
        onSet(next)
    }
}

@Composable
private fun MonitorHeader(
    processKey: MonitorProcessKey,
    clockText: String,
    productionDay: String,
    autoRefresh: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAutoRefreshChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = processKey.pageTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = clockText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4338CA),
                )
            }
            Text(
                text = processKey.processLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前日")
                    }
                    Text(productionDay, fontWeight = FontWeight.Medium)
                    IconButton(onClick = onNextDay) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "翌日")
                    }
                    TextButton(onClick = onToday) { Text("今日") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("自動更新", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.width(4.dp))
                    Switch(checked = autoRefresh, onCheckedChange = onAutoRefreshChange)
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitorKpiRow(processKey: MonitorProcessKey, stats: MonitorOverallStats) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KpiCard("稼働中", stats.totalRunning.toString(), Icons.Default.PlayArrow, Color(0xFF16A34A), Color(0xFFDCFCE7))
        KpiCard("一時停止", stats.totalPaused.toString(), Icons.Default.Pause, Color(0xFFF59E0B), Color(0xFFFEF3C7))
        KpiCard("完了", stats.totalCompleted.toString(), Icons.Default.CheckCircle, Color(0xFF2563EB), Color(0xFFDBEAFE))
        if (processKey == MonitorProcessKey.INSPECTION) {
            KpiCard("休憩中", stats.totalBreak.toString(), Icons.Default.Coffee, Color(0xFF7C3AED), Color(0xFFEDE9FE))
        } else {
            KpiCard("待機中", stats.totalWaiting.toString(), Icons.Default.Schedule, Color(0xFF64748B), Color(0xFFF1F5F9))
        }
        KpiCard(
            label = "設備稼働",
            value = "${stats.runningMachines}/${stats.totalMachines}",
            icon = Icons.Default.Timeline,
            iconColor = Color(0xFF0D9488),
            bgColor = Color(0xFFCCFBF1),
        )
        KpiCard(
            label = "完了率",
            value = "${stats.completionRate}%",
            icon = Icons.Default.Speed,
            iconColor = Color(0xFF4F46E5),
            bgColor = Color(0xFFE0E7FF),
        )
        if (stats.totalDefect > 0) {
            KpiCard(
                label = "不良数",
                value = stats.totalDefect.toString(),
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFDC2626),
                bgColor = Color(0xFFFEE2E2),
            )
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(120.dp),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun MonitorSectionTitle(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.width(6.dp))
        Text("($count)", fontSize = 12.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun MonitorMachineGrid(machines: List<MonitorMachineCard>) {
    if (machines.isEmpty()) {
        MonitorEmptyHint("稼働中のデータはありません")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        machines.forEach { card -> MonitorMachineCardItem(card) }
    }
}

@Composable
private fun MonitorMachineCardItem(card: MonitorMachineCard) {
    val (statusColor, statusBg) = statusColors(card.status)
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    card.name,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    MonitorLogic.statusLabel(card.status),
                    fontSize = 11.sp,
                    color = statusColor,
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            if (card.operatorName.isNotBlank()) {
                Text("担当: ${card.operatorName}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            if (card.currentProduct.isNotBlank()) {
                Text(card.currentProduct, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                "経過 ${MonitorLogic.formatDuration(card.elapsedSec)}",
                fontSize = 12.sp,
                color = Color(0xFF475569),
            )
            if (card.pausedSec > 0) {
                Text("停止 ${MonitorLogic.formatDuration(card.pausedSec)}", fontSize = 11.sp, color = Color(0xFFF59E0B))
            }
            if (card.breakSec > 0) {
                Text("休憩 ${MonitorLogic.formatDuration(card.breakSec)}", fontSize = 11.sp, color = Color(0xFF7C3AED))
            }
        }
    }
}

@Composable
private fun MonitorDefectList(rows: List<MonitorDefectListRow>) {
    if (rows.isEmpty()) {
        MonitorEmptyHint("不良データはありません")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row {
                        Text(row.inspectorName, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(
                            MonitorLogic.statusLabel(row.status),
                            fontSize = 10.sp,
                            color = statusColors(row.status).first,
                        )
                    }
                    Text(row.productName, fontSize = 12.sp, color = Color(0xFF475569), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row {
                        Text(row.defectItemLabel, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("×${row.defectQty}", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                    val atLabel = row.defectOccurredAt?.let { MonitorLogic.formatHistoryTime(it) } ?: "—"
                    Text("発生 $atLabel", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun MonitorHistoryList(rows: List<MonitorHistoryRow>) {
    if (rows.isEmpty()) {
        MonitorEmptyHint("完了履歴はありません")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.take(30).forEach { row ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(row.title, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("担当: ${row.operatorName}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Row {
                        Text("実績 ${row.actualQty}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                        if (row.defectQty > 0) {
                            Text("不良 ${row.defectQty}", fontSize = 12.sp, color = Color(0xFFDC2626))
                        }
                    }
                    val start = MonitorLogic.formatHistoryTime(row.startedAt)
                    val end = MonitorLogic.formatHistoryTime(row.endedAt)
                    Text("$start → $end · ${MonitorLogic.formatDuration(row.elapsedSec)}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun MonitorEmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color(0xFF94A3B8), fontSize = 13.sp)
    }
}

private fun statusColors(status: MonitorRowStatus): Pair<Color, Color> = when (status) {
    MonitorRowStatus.Running -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
    MonitorRowStatus.Paused -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
    MonitorRowStatus.Break -> Color(0xFF7C3AED) to Color(0xFFEDE9FE)
    MonitorRowStatus.Completed -> Color(0xFF2563EB) to Color(0xFFDBEAFE)
    MonitorRowStatus.Waiting, MonitorRowStatus.Idle -> Color(0xFF64748B) to Color(0xFFF1F5F9)
}
