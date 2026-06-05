package com.example.smart_emap.ui.mes.inspection

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.smart_emap.core.mes.TimerPhase
import com.example.smart_emap.data.model.InspectionManagementRowDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectionActualScreen(
    viewModel: InspectionActualViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val s = inspStringsFor(uiState.locale)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = InspectionActualColors.PageBg,
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
                ToolbarCard(
                    uiState = uiState,
                    s = s,
                    onPrevDay = { viewModel.shiftProductionDay(-1) },
                    onNextDay = { viewModel.shiftProductionDay(1) },
                    onToday = viewModel::setProductionDayToday,
                    onProductSelected = viewModel::onProductSelected,
                    onScan = viewModel::openScanDialog,
                )
                when {
                    uiState.isLoadingPlans -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = InspectionActualColors.Teal,
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
    s: InspStrings,
    locale: InspLocale,
    onLocale: (InspLocale) -> Unit,
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
                Icon(Icons.Default.DataUsage, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = s.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InspectionActualColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onHelp, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.HelpOutline,
                    contentDescription = s.helpOpen,
                    tint = InspectionActualColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InspLocale.entries.forEach { opt ->
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
            .background(if (active) InspectionActualColors.Primary else Color.White)
            .border(
                1.dp,
                if (active) InspectionActualColors.Primary else InspectionActualColors.Border,
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else InspectionActualColors.TextSecondary,
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
    val ProductSelectMinWidth = 160.dp // 200dp の 80%
    val ProductSelectMaxWidth = 224.dp // 280dp の 80%
}

private val ToolbarTextStyle = TextStyle(
    fontSize = ToolbarMetrics.FontSize,
    fontWeight = FontWeight.Medium,
    color = InspectionActualColors.TextPrimary,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ToolbarCard(
    uiState: InspectionUiState,
    s: InspStrings,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onProductSelected: (String?) -> Unit,
    onScan: () -> Unit,
) {
    var productExpanded by remember { mutableStateOf(false) }
    val products = uiState.products
    val selectedLabel = products.find { it.productCode == uiState.selectedProductCode }
        ?.let { "${it.productCode} · ${it.productName}" } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = InspectionActualColors.GlassShadow,
                spotColor = InspectionActualColors.GlassShadow,
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        InspectionActualColors.GlassCardTop.copy(alpha = 0.92f),
                        InspectionActualColors.GlassCardMid.copy(alpha = 0.78f),
                        InspectionActualColors.GlassCardBottom.copy(alpha = 0.85f),
                    ),
                ),
            )
            .border(1.dp, InspectionActualColors.GlassBorder, RoundedCornerShape(14.dp))
            .border(0.5.dp, InspectionActualColors.GlassBorderOuter.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            InspectionActualColors.GlassHighlight,
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
                    text = uiState.inspectorLabel.ifBlank { "—" },
                    modifier = Modifier.widthIn(min = 88.dp, max = 120.dp),
                    muted = true,
                )
            }

            ToolbarFieldGroup(
                icon = Icons.Default.DataUsage,
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
                            .menuAnchor()
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
                                        "${p.productCode} · ${p.productName}",
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
            color = InspectionActualColors.TextSecondary,
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
            .shadow(3.dp, CircleShape, spotColor = InspectionActualColors.GlassShadow.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        InspectionActualColors.TealLight.copy(alpha = 0.9f),
                    ),
                ),
            )
            .border(1.dp, InspectionActualColors.GlassBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = InspectionActualColors.Teal, modifier = Modifier.size(17.dp))
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
            .border(1.dp, InspectionActualColors.GlassBorderOuter.copy(alpha = 0.5f), RoundedCornerShape(ToolbarMetrics.CornerRadius))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ToolbarTextStyle.copy(
                color = if (muted) InspectionActualColors.TextSecondary else InspectionActualColors.TextPrimary,
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
            .border(1.dp, InspectionActualColors.GlassBorderOuter.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(17.dp), tint = InspectionActualColors.TextSecondary)
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
                InspectionActualColors.GlassInputBgDisabled,
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
                if (expanded) InspectionActualColors.Teal.copy(alpha = 0.5f) else InspectionActualColors.GlassBorderOuter.copy(alpha = 0.5f),
                RoundedCornerShape(ToolbarMetrics.CornerRadius),
            )
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = ToolbarTextStyle.copy(
                color = if (isPlaceholder) InspectionActualColors.TextMuted else InspectionActualColors.TextPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (enabled) InspectionActualColors.TextSecondary else InspectionActualColors.TextMuted,
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
            .border(1.dp, InspectionActualColors.GlassBorderOuter.copy(alpha = 0.5f), RoundedCornerShape(ToolbarMetrics.CornerRadius))
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
                if (enabled) InspectionActualColors.AmberBorder.copy(alpha = 0.75f) else InspectionActualColors.Border,
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
                tint = if (enabled) InspectionActualColors.AmberBtn else InspectionActualColors.TextMuted,
            )
            Text(
                label,
                style = ToolbarTextStyle.copy(
                    color = if (enabled) InspectionActualColors.AmberBtn else InspectionActualColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun SessionRecoveryAlert(s: InspStrings, onResume: () -> Unit) {
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
                Text(s.sessionRecoveryHint, fontSize = 11.sp, color = InspectionActualColors.TextMuted)
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
            .border(1.dp, InspectionActualColors.Border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = InspectionActualColors.TextMuted, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanProductionCard(
    uiState: InspectionUiState,
    s: InspStrings,
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
            // 製品CD タグ
            StatusChip(uiState.displayProductCd, Color(0xFFEFF6FF), InspectionActualColors.Primary, outlined = true)

            // 製品名ボックス + 不良合計 + 検査員
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = uiState.displayProductName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InspectionActualColors.TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, InspectionActualColors.Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(s.defectTotal, fontSize = 12.sp, color = InspectionActualColors.TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${uiState.defectTotal}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.defectTotal > 0) Color(0xFFDC2626) else InspectionActualColors.TextPrimary,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, InspectionActualColors.Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Person, null, tint = InspectionActualColors.Teal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.inspector, fontSize = 11.sp, color = InspectionActualColors.TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(uiState.inspectorLabel.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // タイマー + 操作ボタン
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TimerPanel(uiState, s, phaseLabel)
                ActionButton(s.btnStart, Icons.Default.PlayArrow, InspectionActualColors.GreenBtn, enabled = uiState.canStart, onClick = onStart)
                if (uiState.canPause) {
                    ActionButton(s.btnPause, Icons.Default.Pause, InspectionActualColors.AmberBtn, enabled = true, onClick = onPause)
                } else if (uiState.canResume) {
                    ActionButton(s.btnResume, Icons.Default.PlayArrow, InspectionActualColors.BlueBtn, enabled = true, onClick = onResume)
                } else {
                    ActionButton(s.btnPause, Icons.Default.Pause, Color(0xFFCBD5E1), enabled = false, onClick = {})
                }
                ActionButton(s.btnEnd, Icons.Default.CheckCircle, InspectionActualColors.Teal, enabled = uiState.canEnd, onClick = onEnd)
            }

            HorizontalDivider(color = InspectionActualColors.Border)

            // 不良（項目別）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(s.defectByItem, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(s.defectHint, fontSize = 11.sp, color = InspectionActualColors.TextMuted)
            }
            if (uiState.isLoadingDefects) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            } else if (uiState.defectGroups.isEmpty()) {
                Text(s.defectItemsEmpty, fontSize = 12.sp, color = InspectionActualColors.TextMuted)
            } else {
                uiState.defectGroups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                s.attributableProcess,
                                fontSize = 10.sp,
                                color = InspectionActualColors.TextMuted,
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
private fun TimerPanel(uiState: InspectionUiState, s: InspStrings, phaseLabel: String) {
    val (bg, border) = when (uiState.timerPhase) {
        TimerPhase.Running -> Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))) to InspectionActualColors.TealBorder
        TimerPhase.Paused -> Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))) to InspectionActualColors.AmberBorder
        TimerPhase.Ended -> Brush.linearGradient(listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))) to Color(0xFF93C5FD)
        TimerPhase.Idle -> Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))) to Color(0xFFCBD5E1)
    }
    Column(
        modifier = Modifier
            .widthIn(min = 160.dp)
            .fillMaxWidth(0.45f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = InspectionActualColors.TextMuted)
                Spacer(modifier = Modifier.width(4.dp))
                Text(s.elapsed, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InspectionActualColors.TextMuted)
            }
            Text(
                phaseLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        Text(
            text = uiState.elapsedDisplay,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = when (uiState.timerPhase) {
                TimerPhase.Running -> Color(0xFF047857)
                TimerPhase.Paused -> Color(0xFFB45309)
                else -> InspectionActualColors.TextPrimary
            },
            modifier = Modifier.padding(vertical = 2.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(s.pausedAccum, fontSize = 10.sp, color = InspectionActualColors.TextMuted)
            Text(uiState.pausedDisplay, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "${uiState.wallStartDisplay} → ${uiState.wallEndDisplay}",
            fontSize = 9.sp,
            color = InspectionActualColors.TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.widthIn(min = 108.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = Color(0xFFE2E8F0)),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    val bg = if (active) InspectionActualColors.DefectActive else Color(0xFFF8FAFC)
    val border = if (active) InspectionActualColors.DefectActiveBorder else InspectionActualColors.Border
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
private fun StepperButton(icon: ImageVector, enabled: Boolean, primary: Boolean, onClick: () -> Unit) {
    val bg = when {
        !enabled -> Color(0xFFE2E8F0)
        primary -> InspectionActualColors.Primary
        else -> Color.White
    }
    val fg = when {
        !enabled -> Color(0xFF94A3B8)
        primary -> Color.White
        else -> InspectionActualColors.TextSecondary
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (primary) bg else InspectionActualColors.Border, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = fg)
    }
}

private data class HistoryColumnSpec(
    val header: String,
    val width: Dp,
    val align: TextAlign = TextAlign.Start,
)

private val HistoryTableColumns: (InspStrings) -> List<HistoryColumnSpec> = { s ->
    listOf(
        HistoryColumnSpec(s.productionDay, 76.dp),
        HistoryColumnSpec(s.productName, 108.dp),
        HistoryColumnSpec(s.productionQty, 52.dp, TextAlign.End),
        HistoryColumnSpec(s.defectQty, 44.dp, TextAlign.End),
        HistoryColumnSpec(s.defectRate, 50.dp, TextAlign.End),
        HistoryColumnSpec(s.efficiencyRate, 44.dp, TextAlign.End),
    )
}

@Composable
private fun CompletedHistorySection(
    rows: List<InspectionManagementRowDto>,
    totalQty: Int,
    s: InspStrings,
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.JAPAN) }
    val columns = remember(s) { HistoryTableColumns(s) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = InspectionActualColors.GlassShadow.copy(alpha = 0.5f),
                spotColor = InspectionActualColors.GlassShadow.copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        Color(0xFFF0FDFA).copy(alpha = 0.88f),
                        Color(0xFFECFDF5).copy(alpha = 0.75f),
                    ),
                ),
            )
            .border(1.dp, InspectionActualColors.TealBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .border(0.5.dp, InspectionActualColors.GlassBorder, RoundedCornerShape(12.dp)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HistoryTableHead(
                title = s.historyTitle,
                totalLabel = s.historyProductionQtyTotal,
                totalQty = nf.format(totalQty),
                rowCount = rows.size,
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(bottom = 4.dp),
                ) {
                    HistoryTableHeaderRow(columns)
                    rows.forEachIndexed { index, row ->
                        HistoryTableDataRow(row = row, index = index, columns = columns, nf = nf)
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
                        Color(0xFFECFDF5).copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.7f),
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4).copy(alpha = 0.6f)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = InspectionActualColors.Teal, modifier = Modifier.size(15.dp))
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InspectionActualColors.TextPrimary)
            HistoryBadge(
                label = totalLabel,
                value = totalQty,
                valueColor = Color(0xFF047857),
                bg = Color(0xFFD1FAE5).copy(alpha = 0.85f),
                border = Color(0xFF6EE7B7).copy(alpha = 0.6f),
            )
        }
        HistoryBadge(
            label = "",
            value = "$rowCount",
            valueColor = Color(0xFF047857),
            bg = Color.White.copy(alpha = 0.65f),
            border = InspectionActualColors.TealBorder.copy(alpha = 0.4f),
        )
    }
    HorizontalDivider(color = InspectionActualColors.TealBorder.copy(alpha = 0.25f), thickness = 1.dp)
}

@Composable
private fun HistoryBadge(
    label: String,
    value: String,
    valueColor: Color,
    bg: Color,
    border: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = InspectionActualColors.TextMuted)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun HistoryTableHeaderRow(columns: List<HistoryColumnSpec>) {
    Row(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFECFDF5), Color(0xFFF0FDFA)),
                ),
            )
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { col ->
            Text(
                text = col.header,
                modifier = Modifier.width(col.width),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF115E59),
                textAlign = col.align,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HistoryTableDataRow(
    row: InspectionManagementRowDto,
    index: Int,
    columns: List<HistoryColumnSpec>,
    nf: NumberFormat,
) {
    val prod = row.actualProductionQuantity ?: 0
    val defects = row.defectQty ?: 0
    val defectRateStr = if (prod > 0) String.format(Locale.JAPAN, "%.1f%%", defects * 100.0 / prod) else "—"
    val netSec = row.mesNetProductionSec ?: 0
    val efficiencyStr = if (netSec > 0 && prod > 0) (prod / (netSec / 3600.0)).toInt().toString() else "—"
    val striped = index % 2 == 1
    val rowBg = if (striped) Color(0xFFF8FAFC).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.45f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(
                width = 0.5.dp,
                color = InspectionActualColors.Border.copy(alpha = 0.35f),
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HistoryDayCell((row.productionDay ?: "—").take(10), columns[0].width)
        HistoryNameCell(row.productName ?: "—", columns[1].width)
        HistoryQtyCell(nf.format(prod), columns[2].width, positive = true)
        HistoryQtyCell(if (defects > 0) nf.format(defects) else "—", columns[3].width, positive = defects > 0, warn = true)
        HistoryRateCell(defectRateStr, columns[4].width, warn = defects > 0 && prod > 0)
        HistoryRateCell(efficiencyStr, columns[5].width, efficiency = true)
    }
}

@Composable
private fun HistoryDayCell(text: String, width: Dp) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = if (width > 60.dp) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFECFDF5).copy(alpha = 0.9f))
                .border(0.5.dp, Color(0xFF99F6E4).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp),
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
        fontWeight = FontWeight.SemiBold,
        color = InspectionActualColors.TextPrimary,
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
            Text(
                text = text,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (warn) Color(0xFFFFF7ED).copy(alpha = 0.95f) else Color(0xFFD1FAE5).copy(alpha = 0.85f),
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (warn) Color(0xFFB45309) else Color(0xFF047857),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
            )
        } else {
            Text(
                text = text,
                fontSize = 11.sp,
                color = InspectionActualColors.TextMuted,
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
        text == "—" -> InspectionActualColors.TextMuted
        efficiency -> Color(0xFF0369A1)
        warn -> Color(0xFFB45309)
        else -> InspectionActualColors.TextSecondary
    }
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun EndProductionDialog(
    uiState: InspectionUiState,
    s: InspStrings,
    onQtyChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(s.endDialogTitle, fontWeight = FontWeight.Bold)
                Text(s.endDialogIntro, fontSize = 12.sp, color = InspectionActualColors.TextMuted)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${uiState.displayProductCd} · ${uiState.displayProductName}", fontWeight = FontWeight.SemiBold)
                Text("${s.elapsed}: ${uiState.elapsedDisplay}", fontSize = 12.sp)
                Text("${s.defectTotal}: ${uiState.defectTotal}", fontSize = 12.sp)
                OutlinedTextField(
                    value = uiState.endDialogQty,
                    onValueChange = onQtyChange,
                    label = { Text(s.productionQty) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !uiState.endDialogSubmitting) {
                if (uiState.endDialogSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(s.btnConfirmEnd)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.endDialogSubmitting) { Text(s.cancel) }
        },
    )
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

private fun phaseLabel(phase: TimerPhase, s: InspStrings): String = when (phase) {
    TimerPhase.Idle -> s.timerIdle
    TimerPhase.Running -> s.timerRunning
    TimerPhase.Paused -> s.timerPaused
    TimerPhase.Ended -> s.timerEnded
}
