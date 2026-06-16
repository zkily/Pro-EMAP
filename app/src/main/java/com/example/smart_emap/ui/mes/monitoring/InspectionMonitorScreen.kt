package com.example.smart_emap.ui.mes.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smart_emap.core.mes.MesCalendarUtils
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonitorTextPrimary = Color(0xFF303133)
private val MonitorTextMuted = Color(0xFF909399)
private val MonitorTextSecondary = Color(0xFF64748B)
private val MonitorHeaderChipHeight = 32.dp
/** 不良表：7文字分の固定列幅（12sp想定） */
private val MonitorDefectChar7ColWidth = 84.dp
private val ymdFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val qtyFormatter = NumberFormat.getNumberInstance(Locale.JAPAN)

@Composable
fun InspectionMonitorScreen(
    viewModel: ProcessMonitorViewModel,
    onNavigate: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.onPageEnter()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setPageVisible(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.setPageVisible(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setPageVisible(false)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    val navigateRegistration = {
        onNavigate("/mes/actualCollectionRegistration/inspection?production_day=${uiState.productionDay}")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC)),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x4010B981), Color.Transparent),
                            radius = 900f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x266366F1), Color.Transparent),
                            radius = 700f,
                        ),
                    ),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                val wideLayout = maxWidth >= 720.dp
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InspectionMonitorHeader(
                        clockText = uiState.clockText,
                        productionDay = uiState.productionDay,
                        autoRefresh = uiState.autoRefresh,
                        lastFetchError = uiState.lastFetchError,
                        onPrevDay = { shiftProductionDay(uiState.productionDay, -1, viewModel::setProductionDay) },
                        onNextDay = { shiftProductionDay(uiState.productionDay, 1, viewModel::setProductionDay) },
                        onToday = { viewModel.setProductionDay(MesCalendarUtils.jstToday()) },
                        onAutoRefreshChange = viewModel::setAutoRefresh,
                        onRefresh = viewModel::refreshAll,
                    )

                    InspectionKpiStrip(stats = uiState.overallStats)

                    if (wideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                InspectionRuntimeSection(
                                    summary = uiState.summary,
                                    nextAssignmentCount = uiState.nextAssignments.size,
                                    onOpenNextAssignPanel = viewModel::openNextAssignPanel,
                                )
                                InspectionEfficiencyPanel(
                                    rows = uiState.summary.inspectorEfficiencyRows,
                                    avgEfficiency = uiState.overallStats.avgEfficiency,
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1.25f),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                InspectionDefectPanel(rows = uiState.summary.defectListRows)
                                if (uiState.summary.historyRows.isNotEmpty()) {
                                    InspectionHistoryPanel(
                                        rows = uiState.summary.historyRows,
                                        onNavigateRegistration = navigateRegistration,
                                    )
                                }
                            }
                        }
                    } else {
                        InspectionRuntimeSection(
                            summary = uiState.summary,
                            nextAssignmentCount = uiState.nextAssignments.size,
                            onOpenNextAssignPanel = viewModel::openNextAssignPanel,
                        )
                        InspectionDefectPanel(rows = uiState.summary.defectListRows)
                        InspectionEfficiencyPanel(
                            rows = uiState.summary.inspectorEfficiencyRows,
                            avgEfficiency = uiState.overallStats.avgEfficiency,
                        )
                        if (uiState.summary.historyRows.isNotEmpty()) {
                            InspectionHistoryPanel(
                                rows = uiState.summary.historyRows,
                                onNavigateRegistration = navigateRegistration,
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading && !uiState.hasInitialData) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFF059669))
                }
            }
        }
    }

    InspectionNextAssignPanelSheet(
        visible = uiState.nextAssignPanelVisible,
        rows = uiState.nextAssignPanelRows,
        assignmentCount = uiState.nextAssignments.size,
        loading = uiState.loadingNextAssignData,
        onDismiss = viewModel::closeNextAssignPanel,
        onAddInspector = viewModel::openNextAssignCreateDialog,
        onOpenRow = viewModel::openNextAssignDialogForRow,
    )

    InspectionNextAssignDialog(
        dialog = uiState.nextAssignDialog,
        inspectors = uiState.shiageInspectors,
        products = uiState.monitorProducts,
        loading = uiState.loadingNextAssignData,
        hasExistingAssignment = viewModel.hasNextAssignmentForInspector(uiState.nextAssignDialog.inspectorUserId),
        onDismiss = viewModel::closeNextAssignDialog,
        onInspectorSelected = viewModel::setNextAssignInspectorUserId,
        onProductSelected = viewModel::setNextAssignProductCd,
        onSave = viewModel::saveNextAssignment,
        onClear = viewModel::clearNextAssignment,
    )
}

private fun shiftProductionDay(current: String, delta: Int, onSet: (String) -> Unit) {
    runCatching {
        onSet(LocalDate.parse(current, ymdFormatter).plusDays(delta.toLong()).format(ymdFormatter))
    }
}

@Composable
private fun InspectionMonitorHeader(
    clockText: String,
    productionDay: String,
    autoRefresh: Boolean,
    lastFetchError: String?,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAutoRefreshChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Monitor, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("検査モニタ", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                Spacer(Modifier.width(12.dp))
                MonitorClockBlock(clockText = clockText, lastFetchError = lastFetchError)
            }
            MonitorProductionDayInline(
                productionDay = productionDay,
                onPrevDay = onPrevDay,
                onNextDay = onNextDay,
                onToday = onToday,
            )
            MonitorToolbarActions(
                autoRefresh = autoRefresh,
                onAutoRefreshChange = onAutoRefreshChange,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun MonitorProductionDayInline(
    productionDay: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(MonitorHeaderChipHeight)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("生産日", fontSize = 11.sp, color = MonitorTextMuted, lineHeight = 12.sp)
            Spacer(Modifier.width(6.dp))
            Text(productionDay, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPrevDay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前日", modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNextDay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "翌日", modifier = Modifier.size(18.dp))
            }
            Text(
                "今日",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF047857),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onToday)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun MonitorClockBlock(clockText: String, lastFetchError: String?) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .height(MonitorHeaderChipHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFD1FAE5))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                clockText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF047857),
                lineHeight = 16.sp,
            )
        }
        if (lastFetchError != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "取得失敗",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFDC2626),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFEE2E2))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

private val MonitorAutoRefreshSwitchScale = 0.6f
private val MonitorAutoRefreshSwitchWidth = 31.dp
private val MonitorAutoRefreshSwitchHeight = 19.dp

@Composable
private fun MonitorToolbarActions(
    autoRefresh: Boolean,
    onAutoRefreshChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text("自動更新", fontSize = 11.sp, color = MonitorTextMuted)
        Box(
            modifier = Modifier.size(MonitorAutoRefreshSwitchWidth, MonitorAutoRefreshSwitchHeight),
            contentAlignment = Alignment.Center,
        ) {
            Switch(
                checked = autoRefresh,
                onCheckedChange = onAutoRefreshChange,
                modifier = Modifier.scale(MonitorAutoRefreshSwitchScale),
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981)),
            )
        }
        MonitorToolbarIconButton(onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "更新", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MonitorToolbarIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .size(34.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun InspectionKpiStrip(stats: MonitorOverallStats) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val cardMod = Modifier.widthIn(min = 118.dp)
        InspectionKpiCard("稼働中", stats.totalRunning.toString(), Icons.Default.PlayArrow, Color(0xFF16A34A), Color(0xFF4ADE80), cardMod)
        InspectionKpiCard("一時停止", stats.totalPaused.toString(), Icons.Default.Pause, Color(0xFFD97706), Color(0xFFFBBF24), cardMod)
        InspectionKpiCard("休憩中", stats.totalBreak.toString(), Icons.Default.Coffee, Color(0xFF0284C7), Color(0xFF38BDF8), cardMod)
        if (stats.totalCommStale > 0) {
            InspectionKpiCard(
                label = "通信断",
                value = stats.totalCommStale.toString(),
                icon = Icons.Default.Warning,
                accent = Color(0xFFDC2626),
                accentLight = Color(0xFFF87171),
                modifier = cardMod,
            )
        }
        InspectionKpiCard(
            label = "平均能率",
            value = MonitorLogic.fmtMonitorEfficiency(stats.avgEfficiency),
            suffix = if (stats.avgEfficiency != null) "本/時" else null,
            icon = Icons.Default.ShowChart,
            accent = Color(0xFF4F46E5),
            accentLight = Color(0xFF818CF8),
            alert = MonitorLogic.isMonitorEfficiencyOutOfRange(stats.avgEfficiency),
            modifier = cardMod,
        )
        InspectionKpiCard(
            label = "生産数",
            value = qtyFormatter.format(stats.totalActual),
            icon = Icons.Default.Inventory2,
            accent = Color(0xFF059669),
            accentLight = Color(0xFF34D399),
            modifier = cardMod,
        )
        if (stats.totalDefect > 0) {
            InspectionKpiCard(
                label = "不良数",
                value = stats.totalDefect.toString(),
                icon = Icons.Default.Warning,
                accent = Color(0xFFDC2626),
                accentLight = Color(0xFFF87171),
                modifier = cardMod,
            )
        }
        InspectionKpiCard(
            label = "不良率",
            value = MonitorLogic.fmtMonitorDefectRate(stats.defectRatePercent),
            icon = Icons.Default.Warning,
            accent = Color(0xFFEA580C),
            accentLight = Color(0xFFFB923C),
            modifier = cardMod,
        )
    }
}

@Composable
private fun InspectionKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    accentLight: Color,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    alert: Boolean = false,
) {
    val borderColor = if (alert) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
    val valueColor = if (alert) Color(0xFFDC2626) else MonitorTextPrimary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(modifier = Modifier.heightIn(min = 56.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(accentLight, accent))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor, lineHeight = 20.sp)
                    if (suffix != null) {
                        Text(suffix, fontSize = 11.sp, color = MonitorTextMuted, modifier = Modifier.padding(start = 2.dp, bottom = 1.dp))
                    }
                }
                Text(label, fontSize = 11.sp, color = MonitorTextMuted, lineHeight = 13.sp)
            }
            }
        }
    }
}

@Composable
private fun InspectionRuntimeSection(
    summary: MonitorProcessSummary,
    nextAssignmentCount: Int,
    onOpenNextAssignPanel: () -> Unit,
) {
    MonitorPanelShell(accentColor = Color(0xFF10B981)) {
        PanelHeader(
            icon = Icons.Default.Monitor,
            iconTint = Color(0xFF059669),
            iconBg = Color(0x1E10B981),
            title = "検査工程",
            hint = "本日",
            count = summary.machines.size,
            accentColor = Color(0xFF059669),
            titleTrailingContent = {
                NextAssignHeaderIconButton(
                    assignmentCount = nextAssignmentCount,
                    onClick = onOpenNextAssignPanel,
                )
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 386.dp, max = 386.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (summary.machines.isEmpty()) {
                MonitorEmptyBox(
                    if (summary.totalPlans > 0) "稼働中の検査はありません" else "本日の計画はありません",
                    minHeight = 360.dp,
                )
            } else {
                summary.machines.forEach { machine ->
                    InspectionMachineCard(machine = machine)
                }
            }
        }
    }
}

@Composable
private fun InspectionMachineCard(
    machine: MonitorMachineCard,
) {
    val (borderColor, bgBrush) = machineCardColors(machine.status, machine.commStale)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (machine.commStale) 58.dp else 50.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            machine.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (machine.elapsedSec > 0 || machine.status == MonitorRowStatus.Paused || machine.status == MonitorRowStatus.Break) {
                            Spacer(Modifier.width(8.dp))
                            if (machine.elapsedSec > 0) {
                                MetricChip("経過", MonitorLogic.formatDuration(machine.elapsedSec), Color(0xFFE6A23C))
                            }
                            if (machine.status == MonitorRowStatus.Paused) {
                                Spacer(Modifier.width(6.dp))
                                MetricChip(
                                    "一時停止",
                                    if (machine.pausedSec > 0) MonitorLogic.formatDuration(machine.pausedSec) else "—",
                                    Color(0xFFE6A23C),
                                )
                            }
                            if (machine.status == MonitorRowStatus.Break) {
                                Spacer(Modifier.width(6.dp))
                                MetricChip(
                                    "休憩",
                                    if (machine.breakSec > 0) MonitorLogic.formatDuration(machine.breakSec) else "—",
                                    Color(0xFF409EFF),
                                )
                            }
                            if (machine.commStale && !machine.lastCommAt.isNullOrBlank()) {
                                Spacer(Modifier.width(6.dp))
                                MetricChip(
                                    "最終通信",
                                    MonitorLogic.formatHistoryTimeOnly(machine.lastCommAt),
                                    Color(0xFFDC2626),
                                )
                            }
                        }
                    }
                }
                if (machine.operatorName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp),
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = MonitorTextPrimary)
                        Spacer(Modifier.width(2.dp))
                        Text(
                            machine.operatorName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 72.dp),
                        )
                    }
                }
                if (machine.commStale) {
                    CommStaleChip()
                    Spacer(Modifier.width(4.dp))
                }
                StatusChip(status = machine.status)
            }
        }
    }
}

@Composable
private fun CommStaleChip() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFFEE2E2),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
    ) {
        Text(
            "通信断",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            color = Color(0xFFDC2626),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MetricChip(label: String, value: String, valueColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = MonitorTextMuted)
        Spacer(Modifier.width(3.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun StatusChip(status: MonitorRowStatus) {
    val (textColor, bgColor) = statusColors(status)
    Surface(shape = RoundedCornerShape(999.dp), color = bgColor, border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.35f))) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status == MonitorRowStatus.Running) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF67C23A)),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(MonitorLogic.statusLabel(status), fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InspectionDefectPanel(rows: List<MonitorDefectListRow>) {
    MonitorPanelShell(accentColor = Color(0xFFEF4444)) {
        PanelHeader(
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFDC2626),
            iconBg = Color(0x1EF87171),
            title = "不良（項目別）",
            hint = "本日",
            count = rows.size,
            accentColor = Color(0xFFDC2626),
        )
        if (rows.isEmpty()) {
            MonitorEmptyBox("本日の不良はありません", minHeight = 386.dp)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 386.dp, max = 386.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                InspectionDefectTable(rows)
            }
        }
    }
}

@Composable
private fun InspectionEfficiencyPanel(
    rows: List<MonitorInspectorEfficiencyRow>,
    avgEfficiency: Int?,
) {
    MonitorPanelShell(accentColor = Color(0xFF6366F1)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1E6366F1)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("検査員能率", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("本日", fontSize = 11.sp, color = MonitorTextMuted)
            if (avgEfficiency != null) {
                Spacer(Modifier.width(6.dp))
                val alert = MonitorLogic.isMonitorEfficiencyOutOfRange(avgEfficiency)
                Text(
                    "平均 ${MonitorLogic.fmtMonitorEfficiency(avgEfficiency)} 本/時",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (alert) Color(0xFFDC2626) else Color(0xFF4F46E5),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("(${rows.size})", fontSize = 11.sp, color = MonitorTextSecondary)
        }
        if (rows.isEmpty()) {
            MonitorEmptyBox("本日の能率データはありません", minHeight = 352.dp)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 352.dp, max = 352.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                InspectionEfficiencyTable(rows)
            }
        }
    }
}

@Composable
private fun InspectionHistoryPanel(
    rows: List<MonitorHistoryRow>,
    onNavigateRegistration: () -> Unit,
) {
    MonitorPanelShell(accentColor = Color(0xFF0EA5E9)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1E0EA5E9)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("本日の確定実績", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onNavigateRegistration, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("実績登録", fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("生産数合計", fontSize = 10.sp, color = MonitorTextMuted)
                Text(qtyFormatter.format(MonitorLogic.historyProductionQtyTotal(rows)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text("(${rows.size})", fontSize = 11.sp, color = MonitorTextSecondary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 352.dp, max = 352.dp)
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rows.forEach { row ->
                InspectionHistoryCard(row = row, onClick = onNavigateRegistration)
            }
        }
    }
}

@Composable
private fun InspectionHistoryCard(
    row: MonitorHistoryRow,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        row.title.ifBlank { "—" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (row.elapsedSec > 0) {
                        Spacer(Modifier.width(8.dp))
                        MetricChip("経過", MonitorLogic.formatDuration(row.elapsedSec), Color(0xFFE6A23C))
                    }
                }
                if (row.operatorName.isNotBlank() && row.operatorName != "—") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(row.operatorName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                StatusChip(status = MonitorRowStatus.Completed)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HistoryStat("生産数", qtyFormatter.format(row.actualQty), false)
                HistoryStat("不良数", if (row.defectQty > 0) row.defectQty.toString() else "—", row.defectQty > 0)
                HistoryStat("開始", MonitorLogic.formatHistoryTime(row.startedAt), false)
                HistoryStat("終了", MonitorLogic.formatHistoryTime(row.endedAt), false)
            }
        }
    }
}

@Composable
private fun HistoryStat(label: String, value: String, defect: Boolean) {
    Column {
        Text(label, fontSize = 10.sp, color = MonitorTextMuted)
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (defect) Color(0xFFDC2626) else MonitorTextPrimary,
        )
    }
}

@Composable
private fun InspectionDefectTable(rows: List<MonitorDefectListRow>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DefectTableHeader()
        rows.forEach { row ->
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableCell(row.inspectorName, MonitorDefectChar7ColWidth)
                Box(Modifier.width(92.dp), contentAlignment = Alignment.Center) {
                    StatusChip(status = row.status)
                }
                TableCellFlex(row.productName)
                TableCell(row.defectItemLabel, MonitorDefectChar7ColWidth)
                Text(
                    qtyFormatter.format(row.defectQty),
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                )
                Text(
                    MonitorLogic.formatHistoryTimeOnly(row.defectOccurredAt),
                    modifier = Modifier.width(88.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MonitorTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun InspectionEfficiencyTable(rows: List<MonitorInspectorEfficiencyRow>) {
    val hScroll = rememberScrollState()
    Column(modifier = Modifier.horizontalScroll(hScroll)) {
        Row(
            modifier = Modifier
                .background(Color(0xFFEEF2FF))
                .padding(vertical = 6.dp, horizontal = 8.dp),
        ) {
            TableHeaderCell("順位", 52.dp, TextAlign.Center)
            TableHeaderCell("検査員", 96.dp)
            TableHeaderCell("件数", 52.dp, TextAlign.End)
            TableHeaderCell("生産数", 72.dp, TextAlign.End)
            TableHeaderCell("平均能率", 100.dp, TextAlign.End)
        }
        rows.forEach { row ->
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(52.dp), contentAlignment = Alignment.Center) {
                    EfficiencyRankBadge(row.rank)
                }
                TableCell(row.inspectorName, 96.dp)
                Text(
                    row.sessionCount.toString(),
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.End,
                    fontSize = 12.sp,
                )
                Text(
                    qtyFormatter.format(row.sumActualQty),
                    modifier = Modifier.width(72.dp),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
                Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.End) {
                    val alert = MonitorLogic.isMonitorEfficiencyOutOfRange(row.efficiencyPerHour)
                    Text(
                        MonitorLogic.fmtMonitorEfficiency(row.efficiencyPerHour),
                        fontWeight = FontWeight.Bold,
                        color = if (alert) Color(0xFFDC2626) else Color(0xFF4F46E5),
                        fontSize = 12.sp,
                    )
                    Text("本/時", fontSize = 10.sp, color = MonitorTextMuted, modifier = Modifier.padding(start = 2.dp, top = 1.dp))
                }
            }
        }
    }
}

@Composable
private fun DefectTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF2F2))
            .padding(vertical = 6.dp, horizontal = 8.dp),
    ) {
        TableHeaderCell("検査員", MonitorDefectChar7ColWidth)
        TableHeaderCell("稼働状態", 92.dp, TextAlign.Center)
        TableHeaderCellFlex("製品名")
        TableHeaderCell("不良項目", MonitorDefectChar7ColWidth)
        TableHeaderCell("不良数", 68.dp, TextAlign.End)
        TableHeaderCell("不良発生時刻", 88.dp, TextAlign.Center)
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp, align: TextAlign = TextAlign.Start) {
    Text(
        text,
        modifier = Modifier.width(width),
        textAlign = align,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MonitorTextSecondary,
    )
}

@Composable
private fun RowScope.TableHeaderCellFlex(text: String, align: TextAlign = TextAlign.Start) {
    Text(
        text,
        modifier = Modifier.weight(1f),
        textAlign = align,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MonitorTextSecondary,
    )
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.TableCellFlex(text: String) {
    Text(
        text,
        modifier = Modifier
            .weight(1f)
            .padding(end = 4.dp),
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EfficiencyRankBadge(rank: Int) {
    val bg = when (rank) {
        1 -> Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))
        2 -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
        3 -> Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)))
        else -> Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
    }
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(rank.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PanelHeader(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    hint: String,
    count: Int,
    accentColor: Color,
    titleTrailingContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MonitorTextPrimary)
            if (titleTrailingContent != null) {
                Spacer(Modifier.width(6.dp))
                titleTrailingContent()
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accentColor.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            ) {
                Text(
                    hint,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accentColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
    }
}

@Composable
private fun MonitorPanelShell(
    accentColor: Color? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(accentColor),
                )
            }
            content()
        }
    }
}

@Composable
private fun MonitorEmptyBox(text: String, minHeight: androidx.compose.ui.unit.Dp = 96.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(minHeight)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MonitorTextSecondary, fontSize = 13.sp)
    }
}

private fun machineCardColors(status: MonitorRowStatus, commStale: Boolean = false): Pair<Color, Brush> {
    if (commStale) {
        return Color(0xFFFCA5A5) to Brush.linearGradient(
            listOf(Color(0x14EF4444), Color(0x04EF4444)),
        )
    }
    return when (status) {
    MonitorRowStatus.Running -> Color(0xFFB3E19D) to Brush.linearGradient(
        listOf(Color(0x0A22C55E), Color(0x0222C55E)),
    )
    MonitorRowStatus.Paused -> Color(0xFFF3D19E) to Brush.linearGradient(
        listOf(Color(0x0AF59E0B), Color(0x02F59E0B)),
    )
    MonitorRowStatus.Break -> Color(0xFFB3D8FF) to Brush.linearGradient(
        listOf(Color(0x0D38BDF8), Color(0x0238BDF8)),
    )
    else -> Color(0xFFE2E8F0) to Brush.linearGradient(listOf(Color.White, Color.White))
    }
}

private fun statusColors(status: MonitorRowStatus): Pair<Color, Color> = when (status) {
    MonitorRowStatus.Running -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
    MonitorRowStatus.Paused -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
    MonitorRowStatus.Break -> Color(0xFF409EFF) to Color(0xFFDBEAFE)
    MonitorRowStatus.Completed -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
    MonitorRowStatus.Waiting, MonitorRowStatus.Idle -> Color(0xFF64748B) to Color(0xFFF1F5F9)
}
