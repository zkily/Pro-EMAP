package com.example.smart_emap.ui.mes.welding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.mes.InspectionSessionLogic
import com.example.smart_emap.core.mes.TimerPhase
import com.example.smart_emap.data.model.WeldingManagementRowDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeldingActualScreen(
    viewModel: WeldingActualViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val s = weldStringsFor(uiState.locale)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    MesBarcodeScanDialog(
        visible = uiState.scanDialogVisible,
        s = s,
        productLabel = uiState.selectedProductCode?.let { code ->
            uiState.products.find { it.productCode == code }?.let { "${it.productCode} · ${it.productName}" }
        },
        onDismiss = viewModel::closeScanDialog,
        onScanned = viewModel::onProductBarcodeScanned,
    )

    if (uiState.endDialogVisible) {
        EndProductionDialog(
            uiState = uiState,
            s = s,
            onQtyChange = viewModel::onEndDialogQtyChange,
            onDismiss = viewModel::closeEndDialog,
            onConfirm = viewModel::submitProductionEnd,
        )
    }

    if (uiState.confirmedEditVisible) {
        ConfirmedHistoryEditDialog(
            uiState = uiState,
            s = s,
            defectGroups = uiState.defectGroups,
            defectCount = viewModel::confirmedEditDefectCount,
            onQtyChange = viewModel::onConfirmedEditQtyChange,
            onWallStartChange = viewModel::onConfirmedEditWallStartChange,
            onWallEndChange = viewModel::onConfirmedEditWallEndChange,
            onPausedSecChange = viewModel::onConfirmedEditPausedSecChange,
            onRemarksChange = viewModel::onConfirmedEditRemarksChange,
            onBumpDefect = viewModel::bumpConfirmedEditDefect,
            onDismiss = viewModel::closeConfirmedHistoryEdit,
            onSave = viewModel::submitConfirmedHistoryEdit,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WeldingActualColors.PageBg,
        // MainShell 内嵌：不再叠加状态栏 inset，避免标题上方大块留白
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        val scrollState = rememberScrollState()
        val titleTopPad = with(LocalDensity.current) { (5f / density).dp }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = 8.dp, end = 14.dp, top = titleTopPad, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PageHeader(
                    s = s,
                    locale = uiState.locale,
                    onLocale = viewModel::setLocale,
                    onHelp = {},
                )
                WeldingNetworkBanners(
                    uiState = uiState,
                    s = s,
                    onRetry = viewModel::retryLoad,
                    onDismiss = viewModel::dismissLoadError,
                )
                ToolbarCard(
                    uiState = uiState,
                    s = s,
                    onPrevDay = { viewModel.shiftProductionDay(-1) },
                    onNextDay = { viewModel.shiftProductionDay(1) },
                    onToday = viewModel::setProductionDayToday,
                    onProductSelected = viewModel::onProductSelected,
                    onScan = viewModel::openScanDialog,
                )
                if (uiState.showActiveProductionSwitchBanner) {
                    ActiveProductionSwitchBanner(
                        productLabel = uiState.activeProductionSwitchLabel,
                        s = s,
                        onResume = viewModel::resumeMyActiveProduction,
                    )
                }
                when {
                    uiState.isLoadingPlans -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = WeldingActualColors.Teal,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                    uiState.selectedProductCode.isNullOrBlank() -> {
                        EmptyProductHint(s.emptySelectProduct)
                    }
                    uiState.showPlanCard -> {
                        if (uiState.showSessionRecoveryAlert) {
                            SessionRecoveryAlert(
                                s = s,
                                onResume = viewModel::resumeActiveSession,
                            )
                        }
                        PlanProductionCard(
                            uiState = uiState,
                            s = s,
                            defectCount = viewModel::defectCount,
                            onStart = viewModel::onStartProduction,
                            onPause = viewModel::onPauseProduction,
                            onResume = viewModel::onResumeProduction,
                            onEnd = viewModel::openEndDialog,
                            onBumpDefect = viewModel::bumpDefect,
                        )
                    }
                }
                if (uiState.completedRows.isNotEmpty()) {
                    CompletedHistorySection(
                        rows = uiState.completedRows,
                        totalQty = uiState.completedQtyTotal,
                        s = s,
                        operatorLabelForRow = viewModel::operatorLabelForHistoryRow,
                        canEditRow = viewModel::canEditConfirmedHistoryRow,
                        onEditRow = viewModel::openConfirmedHistoryEdit,
                    )
                }
            }
            PageScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun WeldingNetworkBanners(
    uiState: WeldingUiState,
    s: WeldStrings,
    onRetry: (WeldRetryAction) -> Unit,
    onDismiss: (WeldRetryAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (uiState.isOfflineMode) {
            OfflineModeBanner(
                isNetworkOnline = uiState.isNetworkOnline,
                pendingCount = uiState.pendingSyncCount,
                offlineText = s.offlineModeBanner,
                pendingText = s.pendingSyncBanner.replace("{count}", uiState.pendingSyncCount.toString()),
                retryLabel = s.btnRetry,
                onRetry = { onRetry(WeldRetryAction.ReloadSync) },
            )
        }
        uiState.syncStaleMessage?.let { message ->
            SyncStaleBanner(
                title = s.syncStaleBanner,
                detail = message,
                retryLabel = s.btnRetry,
                onRetry = { onRetry(WeldRetryAction.ReloadSync) },
            )
        }
        uiState.plansLoadError?.let { message ->
            LoadErrorBanner(
                message = message,
                retryLabel = s.btnRetry,
                dismissLabel = s.btnDismiss,
                onRetry = { onRetry(WeldRetryAction.ReloadPlans) },
                onDismiss = { onDismiss(WeldRetryAction.ReloadPlans) },
            )
        }
        uiState.productsLoadError?.let { message ->
            LoadErrorBanner(
                message = message,
                retryLabel = s.btnRetry,
                dismissLabel = s.btnDismiss,
                onRetry = { onRetry(WeldRetryAction.ReloadProducts) },
                onDismiss = { onDismiss(WeldRetryAction.ReloadProducts) },
            )
        }
        uiState.defectsLoadError?.let { message ->
            LoadErrorBanner(
                message = message,
                retryLabel = s.btnRetry,
                dismissLabel = s.btnDismiss,
                onRetry = { onRetry(WeldRetryAction.ReloadDefects) },
                onDismiss = { onDismiss(WeldRetryAction.ReloadDefects) },
            )
        }
    }
}

@Composable
private fun OfflineModeBanner(
    isNetworkOnline: Boolean,
    pendingCount: Int,
    offlineText: String,
    pendingText: String,
    retryLabel: String,
    onRetry: () -> Unit,
) {
    val bg = if (isNetworkOnline) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
    val border = if (isNetworkOnline) Color(0xFF93C5FD) else Color(0xFF86EFAC)
    val tint = if (isNetworkOnline) Color(0xFF2563EB) else Color(0xFF059669)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            if (isNetworkOnline) Icons.Default.CloudUpload else Icons.Default.CloudOff,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = if (!isNetworkOnline) offlineText else pendingText,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = tint,
            lineHeight = 16.sp,
        )
        if (isNetworkOnline && pendingCount > 0) {
            TextButton(
                onClick = onRetry,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(retryLabel, fontSize = 12.sp, color = tint)
            }
        }
    }
}

@Composable
private fun SyncStaleBanner(
    title: String,
    detail: String,
    retryLabel: String,
    onRetry: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF92400E),
                lineHeight = 16.sp,
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = Color(0xFFB45309),
                lineHeight = 14.sp,
            )
        }
        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(retryLabel, fontSize = 12.sp, color = Color(0xFFB45309))
        }
    }
}

@Composable
private fun LoadErrorBanner(
    message: String,
    retryLabel: String,
    dismissLabel: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = Color(0xFF991B1B),
            lineHeight = 16.sp,
        )
        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        ) {
            Text(retryLabel, fontSize = 12.sp, color = Color(0xFFDC2626))
        }
        TextButton(
            onClick = onDismiss,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        ) {
            Text(dismissLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
private fun PageScrollbar(scrollState: ScrollState, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.width(5.dp),
    ) {
        val trackHeight = maxHeight.value
        val maxScroll = scrollState.maxValue.toFloat()
        val thumbHeight = if (maxScroll > 0f) {
            (trackHeight * trackHeight / (trackHeight + maxScroll)).coerceIn(28f, trackHeight)
        } else {
            trackHeight
        }
        val thumbOffset = if (maxScroll > 0f) {
            (scrollState.value / maxScroll) * (trackHeight - thumbHeight)
        } else {
            0f
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE2E8F0)),
        )
        if (maxScroll > 0f) {
            Box(
                modifier = Modifier
                    .offset(y = thumbOffset.dp)
                    .fillMaxWidth()
                    .height(thumbHeight.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF94A3B8)),
            )
        }
    }
}

@Composable
private fun PageHeader(
    s: WeldStrings,
    locale: WeldLocale,
    onLocale: (WeldLocale) -> Unit,
    onHelp: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF14B8A6)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = s.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WeldingActualColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onHelp, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = s.helpOpen,
                    tint = WeldingActualColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WeldLocale.entries.forEach { opt ->
                LocaleGlyphButton(
                    glyph = opt.glyph,
                    active = opt == locale,
                    onClick = { onLocale(opt) },
                )
            }
        }
    }
}

@Composable
private fun LocaleGlyphButton(glyph: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) WeldingActualColors.Primary else Color.White)
            .border(
                1.dp,
                if (active) WeldingActualColors.Primary else WeldingActualColors.Border,
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else WeldingActualColors.TextSecondary,
        )
    }
}

/** ツールバー内コントロールの統一サイズ（生産日基準） */
private object ToolbarMetrics {
    val ControlHeight = 38.dp
    val IconSize = 32.dp
    val FontSize = 13.sp
    val CornerRadius = 10.dp
    val LabelFontSize = 13.sp
    val ProductSelectMinWidth = 96.dp // 160dp の 60%
    val ProductSelectMaxWidth = 134.dp // 224dp の 60%
}

/** 稼働タイマー + 生産操作ボタン行（Web plan-row__ops と同一高さ） */
private object PlanOpsMetrics {
    val BlockHeight = 84.dp
    val ButtonWidth = 100.dp
    val TimerWidth = 248.dp
    val CornerRadius = 10.dp
    val Gap = 8.dp
}

private enum class PlanActionVariant { Start, Pause, Resume, End, Disabled }

private data class TimerPhaseStyle(
    val background: Brush,
    val borderColor: Color,
    val shadowColor: Color,
    val labelColor: Color,
    val readoutColor: Color,
    val phaseBg: Color,
    val phaseBorder: Color,
    val phaseText: Color,
    val wallsColor: Color,
)

private fun timerPhaseStyle(phase: TimerPhase): TimerPhaseStyle = when (phase) {
    TimerPhase.Running -> TimerPhaseStyle(
        background = Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5), Color(0xFFF0FDF4))),
        borderColor = Color(0xFF6EE7B7),
        shadowColor = Color(0x3310B981),
        labelColor = Color(0xFF047857),
        readoutColor = Color(0xFF047857),
        phaseBg = Color.White.copy(alpha = 0.8f),
        phaseBorder = Color(0xFF6EE7B7),
        phaseText = Color(0xFF065F46),
        wallsColor = Color(0xFF047857).copy(alpha = 0.75f),
    )
    TimerPhase.Paused -> TimerPhaseStyle(
        background = Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFFF7ED))),
        borderColor = Color(0xFFFBBF24),
        shadowColor = Color(0x33F59E0B),
        labelColor = Color(0xFFB45309),
        readoutColor = Color(0xFFB45309),
        phaseBg = Color.White.copy(alpha = 0.82f),
        phaseBorder = Color(0xFFFCD34D),
        phaseText = Color(0xFF92400E),
        wallsColor = Color(0xFFB45309).copy(alpha = 0.75f),
    )
    TimerPhase.Ended -> TimerPhaseStyle(
        background = Brush.linearGradient(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE), Color(0xFFF8FAFC))),
        borderColor = Color(0xFF93C5FD),
        shadowColor = Color(0x333B82F6),
        labelColor = Color(0xFF1D4ED8),
        readoutColor = Color(0xFF1E3A8A),
        phaseBg = Color.White.copy(alpha = 0.82f),
        phaseBorder = Color(0xFF93C5FD),
        phaseText = Color(0xFF1E40AF),
        wallsColor = Color(0xFF1D4ED8).copy(alpha = 0.7f),
    )
    TimerPhase.Idle -> TimerPhaseStyle(
        background = Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))),
        borderColor = Color(0xFFCBD5E1),
        shadowColor = Color(0x1A64748B),
        labelColor = WeldingActualColors.TextMuted,
        readoutColor = WeldingActualColors.TextPrimary,
        phaseBg = Color.White.copy(alpha = 0.75f),
        phaseBorder = Color(0xFFCBD5E1),
        phaseText = Color(0xFF475569),
        wallsColor = WeldingActualColors.TextMuted,
    )
}

private data class PlanActionStyle(
    val top: Color,
    val bottom: Color,
    val border: Color,
    val content: Color,
    val shadow: Color,
)

private fun planActionStyle(variant: PlanActionVariant, enabled: Boolean): PlanActionStyle {
    if (!enabled || variant == PlanActionVariant.Disabled) {
        return PlanActionStyle(
            top = Color(0xFFF8FAFC),
            bottom = Color(0xFFE2E8F0),
            border = Color(0xFFCBD5E1),
            content = Color(0xFF94A3B8),
            shadow = Color(0x0A000000),
        )
    }
    return when (variant) {
        PlanActionVariant.Start -> PlanActionStyle(
            top = Color(0xFF4CD787),
            bottom = Color(0xFF10B981),
            border = Color(0xFF059669),
            content = Color.White,
            shadow = Color(0x4010B981),
        )
        PlanActionVariant.Pause -> PlanActionStyle(
            top = Color(0xFFFFD06A),
            bottom = Color(0xFFF59E0B),
            border = Color(0xFFD97706),
            content = Color(0xFF5C3D00),
            shadow = Color(0x40F59E0B),
        )
        PlanActionVariant.Resume -> PlanActionStyle(
            top = Color(0xFF79BBFF),
            bottom = Color(0xFF3B82F6),
            border = Color(0xFF2563EB),
            content = Color.White,
            shadow = Color(0x403B82F6),
        )
        PlanActionVariant.End -> PlanActionStyle(
            top = Color(0xFFF89898),
            bottom = Color(0xFFEF4444),
            border = Color(0xFFDC2626),
            content = Color.White,
            shadow = Color(0x40EF4444),
        )
        PlanActionVariant.Disabled -> PlanActionStyle(
            top = Color(0xFFF8FAFC),
            bottom = Color(0xFFE2E8F0),
            border = Color(0xFFCBD5E1),
            content = Color(0xFF94A3B8),
            shadow = Color(0x0A000000),
        )
    }
}

private val ToolbarTextStyle = TextStyle(
    fontSize = ToolbarMetrics.FontSize,
    fontWeight = FontWeight.Medium,
    color = WeldingActualColors.TextPrimary,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ToolbarCard(
    uiState: WeldingUiState,
    s: WeldStrings,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onProductSelected: (String?) -> Unit,
    onScan: () -> Unit,
) {
    var productExpanded by remember { mutableStateOf(false) }
    val products = uiState.products
    val selectedLabel = products.find { it.productCode == uiState.selectedProductCode }
        ?.let { it.productName.trim().ifEmpty { it.productCode } } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = WeldingActualColors.GlassShadow,
                spotColor = WeldingActualColors.GlassShadow,
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        WeldingActualColors.GlassCardTop.copy(alpha = 0.92f),
                        WeldingActualColors.GlassCardMid.copy(alpha = 0.78f),
                        WeldingActualColors.GlassCardBottom.copy(alpha = 0.85f),
                    ),
                ),
            )
            .border(1.dp, WeldingActualColors.GlassBorder, RoundedCornerShape(14.dp))
            .border(0.5.dp, WeldingActualColors.GlassBorderOuter.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WeldingActualColors.GlassHighlight,
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 120f,
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ToolbarFieldGroup(icon = Icons.Default.CalendarMonth, label = s.productionDay) {
                GlassValueChip(text = uiState.productionDay)
                GlassCircleButton(Icons.AutoMirrored.Filled.ArrowBack, s.dayPrev, onPrevDay)
                GlassPillButton(text = s.dayToday, onClick = onToday)
                GlassCircleButton(Icons.AutoMirrored.Filled.ArrowForward, s.dayNext, onNextDay)
            }

            ToolbarFieldGroup(icon = Icons.Default.Person, label = s.inspector) {
                GlassValueChip(
                    text = uiState.operatorLabel.ifBlank { "—" },
                    modifier = Modifier.widthIn(min = 88.dp, max = 120.dp),
                    muted = true,
                )
            }

            ToolbarFieldGroup(
                icon = Icons.Default.Inventory2,
                label = s.selectProduct,
                labelMinWidth = 56.dp,
            ) {
                val productEnabled = !uiState.productSelectionLocked && !uiState.isLoadingProducts
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { if (productEnabled) productExpanded = it },
                ) {
                    GlassProductSelect(
                        text = selectedLabel.ifBlank { s.productPlaceholder },
                        isPlaceholder = selectedLabel.isBlank(),
                        expanded = productExpanded,
                        enabled = productEnabled,
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = productEnabled,
                            )
                            .widthIn(
                                min = ToolbarMetrics.ProductSelectMinWidth,
                                max = ToolbarMetrics.ProductSelectMaxWidth,
                            ),
                    )
                    ExposedDropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        p.productName.trim().ifEmpty { p.productCode },
                                        style = ToolbarTextStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = {
                                    productExpanded = false
                                    onProductSelected(p.productCode)
                                },
                            )
                        }
                    }
                }
                GlassScanButton(
                    label = s.btnScanCode,
                    enabled = productEnabled,
                    onClick = onScan,
                )
            }
        }
    }
}

@Composable
private fun ToolbarFieldGroup(
    icon: ImageVector,
    label: String,
    labelMinWidth: Dp = 52.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GlassFieldIcon(icon)
        Text(
            text = label,
            fontSize = ToolbarMetrics.LabelFontSize,
            fontWeight = FontWeight.SemiBold,
            color = WeldingActualColors.TextSecondary,
            modifier = Modifier.widthIn(min = labelMinWidth),
            maxLines = 1,
        )
        Row(
            modifier = Modifier.height(ToolbarMetrics.ControlHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun GlassFieldIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(ToolbarMetrics.IconSize)
            .shadow(3.dp, CircleShape, spotColor = WeldingActualColors.GlassShadow.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        WeldingActualColors.TealLight.copy(alpha = 0.9f),
                    ),
                ),
            )
            .border(1.dp, WeldingActualColors.GlassBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = WeldingActualColors.Teal, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun GlassValueChip(
    text: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    Box(
        modifier = modifier
            .height(ToolbarMetrics.ControlHeight)
            .shadow(2.dp, RoundedCornerShape(ToolbarMetrics.CornerRadius), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .background(
                if (muted) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC).copy(alpha = 0.95f),
                            Color(0xFFEEF2F7).copy(alpha = 0.9f),
                        ),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFF8FAFC).copy(alpha = 0.88f),
                        ),
                    )
                },
            )
            .border(1.dp, WeldingActualColors.GlassBorderOuter.copy(alpha = 0.5f), RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ToolbarTextStyle.copy(
                color = if (muted) WeldingActualColors.TextSecondary else WeldingActualColors.TextPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GlassCircleButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(ToolbarMetrics.ControlHeight)
            .shadow(2.dp, CircleShape, spotColor = Color(0x22000000))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.98f), Color(0xFFE2E8F0).copy(alpha = 0.85f)),
                ),
            )
            .border(1.dp, WeldingActualColors.GlassBorderOuter.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(17.dp), tint = WeldingActualColors.TextSecondary)
    }
}

@Composable
private fun GlassProductSelect(
    text: String,
    isPlaceholder: Boolean,
    expanded: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg = if (enabled) {
        Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.95f), Color(0xFFF8FAFC).copy(alpha = 0.88f)),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                WeldingActualColors.GlassInputBgDisabled,
                Color(0xFFE2E8F0).copy(alpha = 0.7f),
            ),
        )
    }
    Row(
        modifier = modifier
            .height(ToolbarMetrics.ControlHeight)
            .shadow(2.dp, RoundedCornerShape(ToolbarMetrics.CornerRadius), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .background(bg)
            .border(
                1.dp,
                if (expanded) WeldingActualColors.Teal.copy(alpha = 0.5f) else WeldingActualColors.GlassBorderOuter.copy(alpha = 0.5f),
                RoundedCornerShape(ToolbarMetrics.CornerRadius),
            )
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = ToolbarTextStyle.copy(
                color = if (isPlaceholder) WeldingActualColors.TextMuted else WeldingActualColors.TextPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (enabled) WeldingActualColors.TextSecondary else WeldingActualColors.TextMuted,
        )
    }
}

@Composable
private fun GlassPillButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(ToolbarMetrics.ControlHeight)
            .shadow(2.dp, RoundedCornerShape(ToolbarMetrics.CornerRadius), spotColor = Color(0x22000000))
            .clip(RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.96f), Color(0xFFF1F5F9).copy(alpha = 0.9f)),
                ),
            )
            .border(1.dp, WeldingActualColors.GlassBorderOuter.copy(alpha = 0.5f), RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = ToolbarTextStyle)
    }
}

@Composable
private fun GlassScanButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFBEB).copy(alpha = 0.95f),
                Color(0xFFFEF3C7).copy(alpha = 0.88f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
        )
    }
    Box(
        modifier = Modifier
            .height(ToolbarMetrics.ControlHeight)
            .shadow(if (enabled) 3.dp else 0.dp, RoundedCornerShape(ToolbarMetrics.CornerRadius), spotColor = Color(0x33F59E0B))
            .clip(RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .background(bg)
            .border(
                1.dp,
                if (enabled) WeldingActualColors.AmberBorder.copy(alpha = 0.75f) else WeldingActualColors.Border,
                RoundedCornerShape(ToolbarMetrics.CornerRadius),
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = if (enabled) WeldingActualColors.AmberBtn else WeldingActualColors.TextMuted,
            )
            Text(
                label,
                style = ToolbarTextStyle.copy(
                    color = if (enabled) WeldingActualColors.AmberBtn else WeldingActualColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun ActiveProductionSwitchBanner(
    productLabel: String,
    s: WeldStrings,
    onResume: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${s.inProgressStripTitle}：$productLabel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF9A3412),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = s.inProgressStripHint,
                    fontSize = 11.sp,
                    color = Color(0xFFC2410C),
                    lineHeight = 14.sp,
                )
            }
            Button(
                onClick = onResume,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA580C),
                    contentColor = Color.White,
                ),
            ) {
                Text(s.btnResumeSession, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SessionRecoveryAlert(s: WeldStrings, onResume: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(s.sessionRecoveryTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(s.sessionRecoveryHint, fontSize = 11.sp, color = WeldingActualColors.TextMuted)
            }
            Button(
                onClick = onResume,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(s.btnResumeSession, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyProductHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, WeldingActualColors.Border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = WeldingActualColors.TextMuted, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanProductionMetaRow(
    uiState: WeldingUiState,
    s: WeldStrings,
) {
    val blockHeight = 44.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0x33409EFF),
                spotColor = Color(0x22409EFF),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.96f),
                        Color(0xFFECF5FF).copy(alpha = 0.88f),
                        Color(0xFFF8FAFC).copy(alpha = 0.94f),
                    ),
                ),
            )
            .border(1.dp, Color(0xFFB3D8FF).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.72f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 48f,
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlanMetaGlassChip(
                label = s.productCd,
                value = uiState.displayProductCd,
                variant = PlanMetaChipVariant.ProductCd,
                height = blockHeight,
                modifier = Modifier.widthIn(min = 84.dp, max = 108.dp),
            )
            PlanMetaGlassChip(
                label = s.productName,
                value = uiState.displayProductName,
                variant = PlanMetaChipVariant.ProductName,
                height = blockHeight,
                compactWidth = true,
                modifier = Modifier
                    .widthIn(max = 136.dp)
                    .wrapContentWidth(),
            )
            PlanMetaGlassChip(
                label = s.defectTotal,
                value = uiState.defectTotal.toString(),
                variant = PlanMetaChipVariant.DefectTotal,
                height = blockHeight,
                valueColorOverride = if (uiState.defectTotal > 0) Color(0xFFDC2626) else null,
                modifier = Modifier.widthIn(min = 88.dp, max = 112.dp),
            )
            PlanMetaGlassChip(
                label = s.inspector,
                value = uiState.operatorLabel.ifBlank { "—" },
                variant = PlanMetaChipVariant.Welder,
                height = blockHeight,
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.widthIn(min = 100.dp, max = 148.dp),
            )
        }
    }
}

private enum class PlanMetaChipVariant {
    ProductCd,
    ProductName,
    DefectTotal,
    Welder,
}

private data class PlanMetaChipStyle(
    val gradientTop: Color,
    val gradientBottom: Color,
    val border: Color,
    val labelColor: Color,
    val valueColor: Color,
    val shadow: Color,
)

private fun planMetaChipStyle(variant: PlanMetaChipVariant): PlanMetaChipStyle = when (variant) {
    PlanMetaChipVariant.ProductCd -> PlanMetaChipStyle(
        gradientTop = Color(0xFFF3E8FF),
        gradientBottom = Color(0xFFE9D5FF),
        border = Color(0xFFC084FC),
        labelColor = Color(0xFF9333EA),
        valueColor = Color(0xFF6B21A8),
        shadow = Color(0x33A855F7),
    )
    PlanMetaChipVariant.ProductName -> PlanMetaChipStyle(
        gradientTop = Color(0xFFEFF6FF),
        gradientBottom = Color(0xFFDBEAFE),
        border = Color(0xFF93C5FD),
        labelColor = Color(0xFF2563EB),
        valueColor = Color(0xFF1E3A8A),
        shadow = Color(0x333B82F6),
    )
    PlanMetaChipVariant.DefectTotal -> PlanMetaChipStyle(
        gradientTop = Color(0xFFDBEAFE),
        gradientBottom = Color(0xFFBFDBFE),
        border = Color(0xFF60A5FA),
        labelColor = Color(0xFF1D4ED8),
        valueColor = Color(0xFF1E3A8A),
        shadow = Color(0x332563EB),
    )
    PlanMetaChipVariant.Welder -> PlanMetaChipStyle(
        gradientTop = Color(0xFFF5F3FF),
        gradientBottom = Color(0xFFEDE9FE),
        border = Color(0xFFC4B5FD),
        labelColor = Color(0xFF7C3AED),
        valueColor = Color(0xFF5B21B6),
        shadow = Color(0x337C3AED),
    )
}

@Composable
private fun PlanMetaGlassChip(
    label: String,
    value: String,
    variant: PlanMetaChipVariant,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    valueColorOverride: Color? = null,
    leadingIcon: ImageVector? = null,
    compactWidth: Boolean = false,
) {
    val style = planMetaChipStyle(variant)
    val valueColor = valueColorOverride ?: style.valueColor
    val valueFont = if (variant == PlanMetaChipVariant.ProductCd || variant == PlanMetaChipVariant.DefectTotal) {
        FontFamily.Monospace
    } else {
        FontFamily.Default
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .height(height)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(style.gradientTop, style.gradientBottom)))
            .border(1.dp, style.border.copy(alpha = 0.92f), RoundedCornerShape(10.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                        startY = 0f,
                        endY = 26f,
                    ),
                ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = if (compactWidth) Modifier else Modifier.fillMaxWidth(),
        ) {
            if (leadingIcon != null) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = style.labelColor,
                )
            }
            Column(
                modifier = if (compactWidth) {
                    Modifier.widthIn(max = 116.dp)
                } else {
                    Modifier.weight(1f, fill = false)
                },
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.labelColor,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    fontFamily = valueFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanProductionCard(
    uiState: WeldingUiState,
    s: WeldStrings,
    defectCount: (String) -> Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onBumpDefect: (String, Int) -> Unit,
) {
    val phaseLabel = phaseLabel(uiState.timerPhase, s)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PlanProductionMetaRow(uiState = uiState, s = s)

            // タイマー + 操作ボタン
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PlanOpsMetrics.Gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimerPanel(uiState, s, phaseLabel)
                GlassPlanActionButton(
                    label = s.btnStart,
                    icon = Icons.Default.PlayArrow,
                    variant = PlanActionVariant.Start,
                    enabled = uiState.canStart,
                    onClick = onStart,
                )
                when {
                    uiState.canPause -> GlassPlanActionButton(
                        label = s.btnPause,
                        icon = Icons.Default.Pause,
                        variant = PlanActionVariant.Pause,
                        enabled = true,
                        onClick = onPause,
                    )
                    uiState.canResume -> GlassPlanActionButton(
                        label = s.btnResume,
                        icon = Icons.Default.PlayArrow,
                        variant = PlanActionVariant.Resume,
                        enabled = true,
                        onClick = onResume,
                    )
                    else -> GlassPlanActionButton(
                        label = s.btnPause,
                        icon = Icons.Default.Pause,
                        variant = PlanActionVariant.Disabled,
                        enabled = false,
                        onClick = {},
                    )
                }
                GlassPlanActionButton(
                    label = s.btnEnd,
                    icon = Icons.Default.CheckCircle,
                    variant = PlanActionVariant.End,
                    enabled = uiState.canEnd,
                    onClick = onEnd,
                )
            }

            HorizontalDivider(color = WeldingActualColors.Border)

            // 不良（項目別）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(s.defectByItem, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(s.defectHint, fontSize = 11.sp, color = WeldingActualColors.TextMuted)
            }
            if (uiState.isLoadingDefects) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            } else if (uiState.defectGroups.isEmpty()) {
                Text(s.defectItemsEmpty, fontSize = 12.sp, color = WeldingActualColors.TextMuted)
            } else {
                uiState.defectGroups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                s.attributableProcess,
                                fontSize = 10.sp,
                                color = WeldingActualColors.TextMuted,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(group.processName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            group.items.forEach { item ->
                                DefectCell(
                                    label = item.label,
                                    count = defectCount(item.id),
                                    active = defectCount(item.id) > 0,
                                    enabled = uiState.canEditDefects,
                                    onMinus = { onBumpDefect(item.id, -1) },
                                    onPlus = { onBumpDefect(item.id, 1) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerPanel(uiState: WeldingUiState, s: WeldStrings, phaseLabel: String) {
    val style = timerPhaseStyle(uiState.timerPhase)
    Box(
        modifier = Modifier
            .width(PlanOpsMetrics.TimerWidth)
            .height(PlanOpsMetrics.BlockHeight)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(PlanOpsMetrics.CornerRadius),
                ambientColor = style.shadowColor,
                spotColor = style.shadowColor,
            )
            .clip(RoundedCornerShape(PlanOpsMetrics.CornerRadius))
            .background(style.background)
            .border(1.dp, style.borderColor, RoundedCornerShape(PlanOpsMetrics.CornerRadius)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.72f), Color.Transparent),
                        startY = 0f,
                        endY = 72f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = style.labelColor,
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        s.elapsed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = style.labelColor,
                    )
                }
                Text(
                    phaseLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.phaseText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(style.phaseBg)
                        .border(0.5.dp, style.phaseBorder, RoundedCornerShape(999.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = uiState.elapsedDisplay,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = style.readoutColor,
                    maxLines = 1,
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(s.pausedAccum, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = WeldingActualColors.TextMuted)
                    Text(uiState.pausedDisplay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = style.readoutColor)
                }
            }
            Text(
                text = "${uiState.wallStartDisplay} → ${uiState.wallEndDisplay}",
                fontSize = 8.sp,
                color = style.wallsColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GlassPlanActionButton(
    label: String,
    icon: ImageVector,
    variant: PlanActionVariant,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val style = planActionStyle(variant, enabled)
    val shape = RoundedCornerShape(PlanOpsMetrics.CornerRadius)
    Box(
        modifier = Modifier
            .width(PlanOpsMetrics.ButtonWidth)
            .height(PlanOpsMetrics.BlockHeight)
            .shadow(
                elevation = if (enabled) 5.dp else 1.dp,
                shape = shape,
                ambientColor = style.shadow,
                spotColor = style.shadow,
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(style.top, style.bottom)))
            .border(1.dp, style.border, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = if (enabled) 0.35f else 0.15f), Color.Transparent),
                        startY = 0f,
                        endY = 48f,
                    ),
                ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = style.content)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = style.content,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DefectCell(
    label: String,
    count: Int,
    active: Boolean,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val bg = if (active) WeldingActualColors.DefectActive else Color(0xFFF8FAFC)
    val border = if (active) WeldingActualColors.DefectActiveBorder else WeldingActualColors.Border
    Column(
        modifier = Modifier
            .width(138.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StepperButton(Icons.Default.Remove, enabled = enabled && count > 0, primary = false, onClick = onMinus)
            Text("$count", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.widthIn(min = 22.dp), textAlign = TextAlign.Center)
            StepperButton(Icons.Default.Add, enabled = enabled, primary = true, onClick = onPlus)
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    enabled: Boolean,
    primary: Boolean,
    onClick: () -> Unit,
    size: Dp = 34.dp,
) {
    val bg = when {
        !enabled -> Color(0xFFE2E8F0)
        primary -> WeldingActualColors.Primary
        else -> Color.White
    }
    val fg = when {
        !enabled -> Color(0xFF94A3B8)
        primary -> Color.White
        else -> WeldingActualColors.TextSecondary
    }
    val iconSize = if (size <= 28.dp) 14.dp else 16.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (primary) bg else WeldingActualColors.Border, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize), tint = fg)
    }
}

private enum class HistoryColumnGroup {
    Product, Metrics, Time, Meta, Action,
}

private data class HistoryColumnSpec(
    val header: String,
    val width: Dp,
    val align: TextAlign = TextAlign.Start,
    val group: HistoryColumnGroup = HistoryColumnGroup.Product,
)

/** 製品 → 実績数値 → 時間 → 付帯 → 操作 */
private val HistoryTableColumns: (WeldStrings) -> List<HistoryColumnSpec> = { s ->
    listOf(
        HistoryColumnSpec(s.productCd, 66.dp, group = HistoryColumnGroup.Product),
        HistoryColumnSpec(s.productName, 132.dp, group = HistoryColumnGroup.Product),
        HistoryColumnSpec(s.productionQty, 54.dp, TextAlign.End, HistoryColumnGroup.Metrics),
        HistoryColumnSpec(s.defectQty, 48.dp, TextAlign.End, HistoryColumnGroup.Metrics),
        HistoryColumnSpec(s.defectRate, 50.dp, TextAlign.End, HistoryColumnGroup.Metrics),
        HistoryColumnSpec(s.efficiencyRate, 58.dp, TextAlign.End, HistoryColumnGroup.Metrics),
        HistoryColumnSpec(s.productionStart, 90.dp, group = HistoryColumnGroup.Time),
        HistoryColumnSpec(s.productionEnd, 90.dp, group = HistoryColumnGroup.Time),
        HistoryColumnSpec(s.elapsedMinutes, 58.dp, TextAlign.End, HistoryColumnGroup.Time),
        HistoryColumnSpec(s.pausedAccumMinutes, 58.dp, TextAlign.End, HistoryColumnGroup.Time),
        HistoryColumnSpec(s.productionDay, 76.dp, group = HistoryColumnGroup.Meta),
        HistoryColumnSpec(s.inspector, 64.dp, group = HistoryColumnGroup.Meta),
        HistoryColumnSpec(s.historyActions, 52.dp, TextAlign.Center, HistoryColumnGroup.Action),
    )
}

@Composable
private fun CompletedHistorySection(
    rows: List<WeldingManagementRowDto>,
    totalQty: Int,
    s: WeldStrings,
    operatorLabelForRow: (WeldingManagementRowDto) -> String,
    canEditRow: (WeldingManagementRowDto) -> Boolean,
    onEditRow: (WeldingManagementRowDto) -> Unit,
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.JAPAN) }
    val columns = remember(s) { HistoryTableColumns(s) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x400D9488),
                spotColor = Color(0x300D9488),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF8FFFE),
                        Color(0xFFF0FDFA).copy(alpha = 0.92f),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
            .border(1.dp, WeldingActualColors.TealBorder.copy(alpha = 0.28f), RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HistoryTableHead(
                title = s.historyTitle,
                totalLabel = s.historyProductionQtyTotal,
                totalQty = nf.format(totalQty),
                rowCount = rows.size,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .horizontalScroll(scrollState),
                ) {
                    HistoryTableHeaderRow(columns)
                    rows.forEachIndexed { index, row ->
                        HistoryTableDataRow(
                            row = row,
                            index = index,
                            columns = columns,
                            nf = nf,
                            operatorLabel = operatorLabelForRow(row),
                            canEdit = canEditRow(row),
                            onEdit = { onEditRow(row) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTableHead(
    title: String,
    totalLabel: String,
    totalQty: String,
    rowCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFECFDF5),
                        Color(0xFFF0FDFA).copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.6f),
                    ),
                ),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp), ambientColor = Color(0x400D9488))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF134E4A),
                    letterSpacing = 0.2.sp,
                )
                Text(
                    "$totalLabel $totalQty",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF047857),
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        HistoryBadge(
            label = "",
            value = rowCount.toString(),
            valueColor = Color(0xFF0F766E),
            bg = Brush.linearGradient(listOf(Color.White, Color(0xFFECFDF5))),
            border = Color(0xFF99F6E4).copy(alpha = 0.8f),
        )
    }
    HorizontalDivider(color = Color(0xFFCCFBF1).copy(alpha = 0.9f), thickness = 1.dp)
}

@Composable
private fun HistoryBadge(
    label: String,
    value: String,
    valueColor: Color,
    bg: Brush,
    border: Color,
) {
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(999.dp), ambientColor = Color(0x200D9488))
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = WeldingActualColors.TextMuted)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun HistoryTableHeaderRow(columns: List<HistoryColumnSpec>) {
    Row(
        modifier = Modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0F766E), Color(0xFF115E59), Color(0xFF134E4A)),
                ),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { index, col ->
            if (index > 0 && columns[index - 1].group != col.group) {
                HistoryGroupDivider(isHeader = true)
            }
            Text(
                text = col.header,
                modifier = Modifier.width(col.width),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFECFDF5),
                textAlign = col.align,
                maxLines = 2,
                lineHeight = 13.sp,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.15.sp,
            )
        }
    }
}

@Composable
private fun HistoryGroupDivider(isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(if (isHeader) 28.dp else 24.dp)
            .background(
                if (isHeader) Color.White.copy(alpha = 0.22f) else Color(0xFFCBD5E1).copy(alpha = 0.65f),
            ),
    )
}

@Composable
private fun HistoryTableDataRow(
    row: WeldingManagementRowDto,
    index: Int,
    columns: List<HistoryColumnSpec>,
    nf: NumberFormat,
    operatorLabel: String,
    canEdit: Boolean,
    onEdit: () -> Unit,
) {
    val prod = row.actualProductionQuantity ?: 0
    val defects = WeldingManagementRowExt.historyDefectQty(row)
    val defectRateStr = WeldingManagementRowExt.formatDefectRate(row)
    val wallSec = WeldingHistoryRowFormat.rowWallElapsedSec(row)
    val pauseSec = WeldingHistoryRowFormat.rowPausedAccumSec(row)
    val efficiencyStr = WeldingManagementRowExt.formatEfficiencyRate(row)
    val striped = index % 2 == 1
    val rowBg = if (striped) {
        Brush.horizontalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9).copy(alpha = 0.55f)))
    } else {
        Brush.horizontalGradient(listOf(Color.White, Color(0xFFFAFFFE)))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(
                width = 0.5.dp,
                color = Color(0xFFE2E8F0).copy(alpha = if (striped) 0.5f else 0.35f),
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { colIndex, col ->
            if (colIndex > 0 && columns[colIndex - 1].group != col.group) {
                HistoryGroupDivider()
            }
            when (colIndex) {
                0 -> HistoryCodeCell(row.productCd ?: "—", col.width)
                1 -> HistoryProductNameCell(row.productName ?: "—", col.width)
                2 -> HistoryQtyCell(nf.format(prod), col.width, positive = true)
                3 -> HistoryQtyCell(
                    if (defects > 0) nf.format(defects) else "—",
                    col.width,
                    positive = defects > 0,
                    warn = true,
                )
                4 -> HistoryRateCell(defectRateStr, col.width, warn = defects > 0 && prod > 0)
                5 -> HistoryRateCell(efficiencyStr, col.width, efficiency = true)
                6 -> HistoryTimeCell(WeldingHistoryRowFormat.formatProductionStart(row), col.width)
                7 -> HistoryTimeCell(WeldingHistoryRowFormat.formatProductionEnd(row), col.width)
                8 -> HistoryDurationCell(
                    WeldingHistoryRowFormat.formatSecondsAsMinutes(wallSec),
                    col.width,
                    active = wallSec > 0,
                )
                9 -> HistoryDurationCell(
                    WeldingHistoryRowFormat.formatSecondsAsMinutes(pauseSec),
                    col.width,
                    active = pauseSec > 0,
                    muted = pauseSec <= 0,
                )
                10 -> HistoryDayCell(WeldingManagementRowExt.formatHistoryProductionDay(row), col.width)
                11 -> HistoryNameCell(operatorLabel, col.width)
                12 -> HistoryActionCell(col.width, canEdit, col.header, onEdit)
            }
        }
    }
}

@Composable
private fun HistoryProductNameCell(text: String, width: Dp) {
    Row(
        modifier = Modifier.width(width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
                ),
        )
        Text(
            text = text,
            modifier = Modifier.width(width - 7.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HistoryCodeCell(text: String, width: Dp) {
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE)),
                    ),
                )
                .border(0.5.dp, Color(0xFFC4B5FD).copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D28D9),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HistoryDayCell(text: String, width: Dp) {
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFECFDF5).copy(alpha = 0.95f))
                .border(0.5.dp, Color(0xFF5EEAD4).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F766E),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
    }
}

@Composable
private fun HistoryNameCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF475569),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun HistoryQtyCell(
    text: String,
    width: Dp,
    positive: Boolean = false,
    warn: Boolean = false,
) {
    val showChip = positive && text != "—"
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.CenterEnd) {
        if (showChip) {
            val bgColors = if (warn) {
                listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))
            } else {
                listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0).copy(alpha = 0.85f))
            }
            Text(
                text = text,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.linearGradient(bgColors))
                    .border(
                        0.5.dp,
                        if (warn) Color(0xFFFDBA74).copy(alpha = 0.5f) else Color(0xFF6EE7B7).copy(alpha = 0.45f),
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (warn) Color(0xFFC2410C) else Color(0xFF047857),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
            )
        } else {
            Text(
                text = text,
                fontSize = 11.sp,
                color = WeldingActualColors.TextMuted,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HistoryRateCell(
    text: String,
    width: Dp,
    warn: Boolean = false,
    efficiency: Boolean = false,
) {
    val color = when {
        text == "—" -> WeldingActualColors.TextMuted
        efficiency -> Color(0xFF0369A1)
        warn -> Color(0xFFC2410C)
        else -> WeldingActualColors.TextSecondary
    }
    val bg = when {
        text == "—" -> null
        efficiency -> Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD).copy(alpha = 0.5f)))
        warn -> Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5).copy(alpha = 0.6f)))
        else -> null
    }
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.CenterEnd) {
        if (bg != null) {
            Text(
                text = text,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
            )
        } else {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HistoryTimeCell(text: String, width: Dp) {
    val empty = text == "—"
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.CenterStart) {
        if (empty) {
            Text(text, fontSize = 10.sp, color = WeldingActualColors.TextMuted)
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9).copy(alpha = 0.9f))
                    .border(0.5.dp, Color(0xFFCBD5E1).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color(0xFF64748B),
                )
                Text(
                    text = text,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HistoryDurationCell(
    text: String,
    width: Dp,
    active: Boolean = false,
    muted: Boolean = false,
) {
    val color = when {
        muted || text == "0" -> WeldingActualColors.TextMuted
        active -> Color(0xFF0369A1)
        else -> WeldingActualColors.TextSecondary
    }
    Text(
        text = if (text == "0" && muted) "—" else text,
        modifier = Modifier.width(width),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun HistoryActionCell(
    width: Dp,
    canEdit: Boolean,
    editLabel: String,
    onEdit: () -> Unit,
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center,
    ) {
        if (canEdit) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(3.dp, RoundedCornerShape(10.dp), ambientColor = Color(0x403B82F6))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))),
                    )
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = editLabel,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White,
                )
            }
        } else {
            Text("—", fontSize = 11.sp, color = WeldingActualColors.TextMuted.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfirmedHistoryEditDialog(
    uiState: WeldingUiState,
    s: WeldStrings,
    defectGroups: List<DefectGroupUi>,
    defectCount: (String) -> Int,
    onQtyChange: (String) -> Unit,
    onWallStartChange: (Long) -> Unit,
    onWallEndChange: (Long) -> Unit,
    onPausedSecChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onBumpDefect: (String, Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val submitting = uiState.confirmedEditSubmitting
    val scrollState = rememberScrollState()
    var expandedDateTime by remember { mutableStateOf<ConfirmedEditDateTimeTarget?>(null) }
    val elapsedPreview = run {
        val ws = uiState.confirmedEditWallStartMs
        val we = uiState.confirmedEditWallEndMs
        val pauseMs = (uiState.confirmedEditPausedSec.toLongOrNull() ?: 0L) * 1000L
        if (ws == null || we == null) {
            "00:00:00"
        } else {
            InspectionSessionLogic.formatDurationMs((we - ws - pauseMs).coerceAtLeast(0))
        }
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = WeldingActualColors.Teal.copy(alpha = 0.75f),
        unfocusedBorderColor = Color(0xFFE2E8F0),
        disabledBorderColor = Color(0xFFE2E8F0).copy(alpha = 0.6f),
        focusedContainerColor = Color(0xFFF0FDFA).copy(alpha = 0.55f),
        unfocusedContainerColor = Color(0xFFF8FAFC),
        disabledContainerColor = Color(0xFFF1F5F9),
        cursorColor = WeldingActualColors.Teal,
        focusedLabelColor = WeldingActualColors.Teal,
        unfocusedLabelColor = WeldingActualColors.TextMuted,
    )

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .heightIn(max = 580.dp)
                .shadow(20.dp, RoundedCornerShape(18.dp), spotColor = Color(0x330D9488)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFECFDF5), Color(0xFFF0FDFA), Color.White),
                            ),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = Color(0x400D9488))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            s.confirmedEditDialogTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF134E4A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            uiState.confirmedEditProductLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFCCFBF1).copy(alpha = 0.85f), thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ConfirmedEditCompactField(
                            value = uiState.confirmedEditQty,
                            onValueChange = onQtyChange,
                            label = s.productionQty,
                            enabled = !submitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.weight(0.42f),
                        )
                        ConfirmedEditCompactField(
                            value = uiState.confirmedEditPausedSec,
                            onValueChange = onPausedSecChange,
                            label = s.pausedAccum,
                            enabled = !submitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.weight(0.58f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ConfirmedEditDateTimeField(
                            title = s.productionStart,
                            summary = WeldingHistoryRowFormat.formatWallInput(uiState.confirmedEditWallStartMs),
                            enabled = !submitting,
                            expanded = expandedDateTime == ConfirmedEditDateTimeTarget.Start,
                            onExpandedChange = { open ->
                                expandedDateTime = when {
                                    open -> ConfirmedEditDateTimeTarget.Start
                                    expandedDateTime == ConfirmedEditDateTimeTarget.Start -> null
                                    else -> expandedDateTime
                                }
                            },
                            fieldColors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                        ConfirmedEditDateTimeField(
                            title = s.productionEnd,
                            summary = WeldingHistoryRowFormat.formatWallInput(uiState.confirmedEditWallEndMs),
                            enabled = !submitting,
                            expanded = expandedDateTime == ConfirmedEditDateTimeTarget.End,
                            onExpandedChange = { open ->
                                expandedDateTime = when {
                                    open -> ConfirmedEditDateTimeTarget.End
                                    expandedDateTime == ConfirmedEditDateTimeTarget.End -> null
                                    else -> expandedDateTime
                                }
                            },
                            fieldColors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AnimatedVisibility(
                        visible = expandedDateTime == ConfirmedEditDateTimeTarget.Start && !submitting,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        DateTimeCalendarTimePanel(
                            epochMillis = uiState.confirmedEditWallStartMs,
                            productionDay = uiState.productionDay,
                            locale = uiState.locale,
                            enabled = !submitting,
                            onEpochMillisChange = onWallStartChange,
                        )
                    }
                    AnimatedVisibility(
                        visible = expandedDateTime == ConfirmedEditDateTimeTarget.End && !submitting,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    ) {
                        DateTimeCalendarTimePanel(
                            epochMillis = uiState.confirmedEditWallEndMs,
                            productionDay = uiState.productionDay,
                            locale = uiState.locale,
                            enabled = !submitting,
                            onEpochMillisChange = onWallEndChange,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEFF6FF), Color(0xFFECFDF5).copy(alpha = 0.65f)),
                                ),
                            )
                            .border(0.5.dp, Color(0xFFBFDBFE).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.AccessTime, null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            Text(s.elapsed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                        }
                        Text(
                            elapsedPreview,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1D4ED8),
                        )
                    }

                    ConfirmedEditCompactField(
                        value = uiState.confirmedEditRemarks,
                        onValueChange = onRemarksChange,
                        label = s.remarks,
                        enabled = !submitting,
                        singleLine = false,
                        minLines = 1,
                        maxLines = 2,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            s.defectByItem,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF134E4A),
                        )
                        Text(
                            s.confirmedEditPauseHint,
                            fontSize = 9.sp,
                            color = WeldingActualColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                            textAlign = TextAlign.End,
                        )
                    }

                    if (defectGroups.isEmpty()) {
                        Text(s.defectItemsEmpty, fontSize = 10.sp, color = WeldingActualColors.TextMuted)
                    } else {
                        defectGroups.forEach { group ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    group.processName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    group.items.forEach { item ->
                                        ConfirmedEditDefectChip(
                                            label = item.label,
                                            count = defectCount(item.id),
                                            active = defectCount(item.id) > 0,
                                            enabled = !submitting,
                                            onMinus = { onBumpDefect(item.id, -1) },
                                            onPlus = { onBumpDefect(item.id, 1) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0).copy(alpha = 0.9f), thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !submitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                    ) {
                        Text(s.cancel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = onSave,
                        enabled = !submitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = Color(0x400D9488)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(s.btnSaveConfirmed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmedEditCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: androidx.compose.material3.TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = placeholder?.let { { Text(it, fontSize = 10.sp, maxLines = 1) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontFamily = FontFamily.Monospace),
        shape = RoundedCornerShape(10.dp),
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (singleLine) 48.dp else 52.dp),
    )
}

@Composable
private fun ConfirmedEditDefectChip(
    label: String,
    count: Int,
    active: Boolean,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val bg = if (active) Color(0xFFFFF1F2) else Color(0xFFF8FAFC)
    val border = if (active) Color(0xFFFECACA) else Color(0xFFE2E8F0)
    Row(
        modifier = Modifier
            .widthIn(min = 108.dp, max = 148.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(8.dp))
            .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            label,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = WeldingActualColors.TextPrimary,
        )
        StepperButton(Icons.Default.Remove, enabled = enabled && count > 0, primary = false, onClick = onMinus, size = 26.dp)
        Text(
            "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.widthIn(min = 16.dp),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
        )
        StepperButton(Icons.Default.Add, enabled = enabled, primary = true, onClick = onPlus, size = 26.dp)
    }
}

@Composable
private fun EndProductionDialog(
    uiState: WeldingUiState,
    s: WeldStrings,
    onQtyChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val qtyFocusRequester = remember { FocusRequester() }
    val qtyBorderColor = Color(0xFFDC2626)
    val qtyBorderMuted = Color(0xFFEF4444)
    val submitting = uiState.endDialogSubmitting

    LaunchedEffect(Unit) {
        qtyFocusRequester.requestFocus()
    }

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color(0x40EF4444),
                    spotColor = Color(0x40EF4444),
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFF1F2),
                                    Color(0xFFFFF7ED),
                                    Color.White,
                                ),
                            ),
                        )
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0x40EF4444))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFECACA), Color(0xFFFEE2E2)),
                                    ),
                                )
                                .border(1.dp, Color(0xFFFCA5A5), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Color(0xFFDC2626),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                s.endDialogTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = WeldingActualColors.TextPrimary,
                            )
                            Text(
                                s.endDialogIntro,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = WeldingActualColors.TextMuted,
                            )
                        }
                    }
                }

                HorizontalDivider(color = WeldingActualColors.Border.copy(alpha = 0.6f))

                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EndDialogSummaryCard(
                        icon = Icons.Default.Inventory2,
                        iconTint = Color(0xFF0D9488),
                        iconBg = WeldingActualColors.TealLight,
                        label = s.productName,
                        value = "${uiState.displayProductCd} · ${uiState.displayProductName}",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        EndDialogSummaryCard(
                            icon = Icons.Default.AccessTime,
                            iconTint = Color(0xFF2563EB),
                            iconBg = Color(0xFFEFF6FF),
                            label = s.elapsed,
                            value = uiState.elapsedDisplay,
                            modifier = Modifier.weight(1f),
                        )
                        EndDialogSummaryCard(
                            icon = Icons.Default.ErrorOutline,
                            iconTint = Color(0xFFB45309),
                            iconBg = WeldingActualColors.AmberLight,
                            label = s.defectTotal,
                            value = uiState.defectTotal.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            s.productionQty,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = qtyBorderColor,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, qtyBorderMuted, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp)),
                        ) {
                            OutlinedTextField(
                                value = uiState.endDialogQty,
                                onValueChange = onQtyChange,
                                singleLine = true,
                                enabled = !submitting,
                                placeholder = {
                                    Text("0", color = Color(0xFFFCA5A5))
                                },
                                textStyle = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = WeldingActualColors.TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    focusedContainerColor = Color(0xFFFFF5F5),
                                    unfocusedContainerColor = Color(0xFFFFFBFB),
                                    disabledContainerColor = Color(0xFFF8FAFC),
                                    cursorColor = qtyBorderColor,
                                    focusedTextColor = WeldingActualColors.TextPrimary,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(qtyFocusRequester),
                            )
                        }
                    }
                }

                HorizontalDivider(color = WeldingActualColors.Border.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !submitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WeldingActualColors.Border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WeldingActualColors.TextSecondary,
                        ),
                    ) {
                        Text(s.cancel, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !submitting && uiState.endDialogQty.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0x40EF4444)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = Color(0xFFFCA5A5),
                        ),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(s.btnConfirmEnd, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndDialogSummaryCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, WeldingActualColors.Border.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = WeldingActualColors.TextMuted)
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WeldingActualColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color, outlined: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(if (outlined) Modifier.border(1.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(6.dp)) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun phaseLabel(phase: TimerPhase, s: WeldStrings): String = when (phase) {
    TimerPhase.Idle -> s.timerIdle
    TimerPhase.Running -> s.timerRunning
    TimerPhase.Paused -> s.timerPaused
    TimerPhase.Ended -> s.timerEnded
}
