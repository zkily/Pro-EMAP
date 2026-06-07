package com.example.smart_emap.ui.mes.cutting

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import android.os.Handler
import android.os.Looper
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.toMutableStateList
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.mes.TimerPhase
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.core.mes.CuttingSessionLogic
import com.example.smart_emap.ui.mes.welding.ConfirmedEditDateTimeField
import com.example.smart_emap.ui.mes.welding.ConfirmedEditDateTimeTarget
import com.example.smart_emap.ui.mes.welding.DateTimeCalendarTimePanel
import com.example.smart_emap.ui.mes.welding.WeldingHistoryRowFormat
import com.example.smart_emap.ui.mes.welding.MesBarcodeScanDialog
import com.example.smart_emap.ui.mes.welding.WeldLocale
import com.example.smart_emap.ui.mes.welding.weldStringsFor

private val PlanRunBlockHeight = 84.dp
private val PlanActBtnWidth = 100.dp
private val MetaChipHeight = 42.dp
private val OperatorFieldWidth = 107.dp
private val OperatorContainerMinWidth = 184.dp
private const val ReorderLongPressMs = 2000L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CuttingActualScreen(viewModel: CuttingActualViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val s = cutStringsFor(uiState.locale)
    val snackbarHostState = remember { SnackbarHostState() }
    var planReorderDragging by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }
    val scanStrings = weldStringsFor(
        when (uiState.locale) {
            CutLocale.Ja -> WeldLocale.Ja
            CutLocale.En -> WeldLocale.En
            CutLocale.Zh -> WeldLocale.Zh
            CutLocale.Vi -> WeldLocale.Vi
        },
    )
    MesBarcodeScanDialog(
        visible = uiState.scanDialogVisible,
        s = scanStrings,
        productLabel = uiState.scanTargetLabel.ifEmpty { null },
        onDismiss = viewModel::closeScanDialog,
        onScanned = viewModel::onBarcodeScanned,
    )
    if (uiState.endDialogVisible) EndCuttingDialog(uiState, s, viewModel)
    if (uiState.deferQtyDialogVisible) DeferQtyDialog(uiState, s, viewModel)
    if (uiState.changeMachineVisible) ChangeMachineDialog(uiState, s, viewModel)
    if (uiState.confirmedEditVisible) {
        ConfirmedCuttingEditDialog(uiState, s, uiState.operators, viewModel)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CuttingActualColors.PageBg,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState, enabled = !planReorderDragging).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PageHeader(s, uiState.locale, viewModel::setLocale)
            ToolbarCard(uiState, s, viewModel)
            if (!uiState.isNetworkOnline || uiState.pendingSyncCount > 0) {
                OfflineStrip(if (!uiState.isNetworkOnline) s.offlineBanner else s.format(s.offlinePendingSync, "n" to uiState.pendingSyncCount))
            }
            when {
                uiState.isLoadingPlans -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CuttingActualColors.Primary)
                }
                uiState.selectedMachineId == null -> EmptyHint(s.emptySelectMachine)
                uiState.dayGroups.isEmpty() -> EmptyHint(s.emptyNoPlans)
                else -> uiState.dayGroups.forEach {
                    DayGroupSection(
                        group = it,
                        s = s,
                        operators = uiState.operators,
                        viewModel = viewModel,
                        reorderEnabled = !uiState.isReorderSaving && !uiState.isLoadingPlans,
                        onDragActiveChange = { active -> planReorderDragging = active },
                    )
                }
            }
        }
    }
}

@Composable
private fun PageHeader(s: CutStrings, locale: CutLocale, onLocale: (CutLocale) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(38.dp).shadow(4.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))
                    .background(CuttingActualColors.TitleIconGradient),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Text(s.title, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = CuttingActualColors.TextPrimary)
        }
        LocaleGlyphRow(locale, onLocale)
    }
}

@Composable
private fun LocaleGlyphRow(current: CutLocale, onSelect: (CutLocale) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CutLocale.entries.forEach { loc ->
            val active = loc == current
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (active) CuttingActualColors.PrimaryLight9 else Color.White)
                    .border(1.dp, if (active) CuttingActualColors.Primary else CuttingActualColors.Border, RoundedCornerShape(8.dp))
                    .clickable { onSelect(loc) },
                contentAlignment = Alignment.Center,
            ) {
                Text(loc.glyph, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (active) CuttingActualColors.Primary else CuttingActualColors.TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ToolbarCard(uiState: CuttingUiState, s: CutStrings, viewModel: CuttingActualViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CuttingActualColors.CardBg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, CuttingActualColors.BorderLight, RoundedCornerShape(10.dp)).shadow(1.dp, RoundedCornerShape(10.dp)),
    ) {
        FlowRow(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DayToolbarRow(uiState.productionDay, s, viewModel)
            MachineToolbarRow(uiState, s, viewModel)
            FilterSwitchRow(uiState.hideCompleted, s, viewModel::setHideCompleted)
            Button(
                onClick = viewModel::loadPlans,
                enabled = uiState.selectedMachineId != null && !uiState.isLoadingPlans,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = CuttingActualColors.BtnDisabledBg),
            ) {
                Row(
                    Modifier.background(Brush.verticalGradient(listOf(CuttingActualColors.LoadBtnStart, CuttingActualColors.LoadBtnEnd)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.loadPlans, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DayToolbarRow(productionDay: String, s: CutStrings, viewModel: CuttingActualViewModel) {
    Row(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(CuttingActualColors.PrimaryLight9, Color.White, CuttingActualColors.DayGroupHeadBg)))
            .border(1.dp, CuttingActualColors.ToolbarDayBorder, RoundedCornerShape(10.dp)).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(Brush.linearGradient(listOf(CuttingActualColors.Primary, Color(0xFF38BDF8))), RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(s.productionDay, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CuttingActualColors.PrimaryDark, modifier = Modifier.padding(horizontal = 6.dp))
        ToolbarDayField(
            value = productionDay,
            onValueChange = viewModel::setProductionDay,
        )
        IconButton(onClick = { viewModel.shiftProductionDay(-1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.dayPrev, modifier = Modifier.size(16.dp)) }
        TextButton(onClick = viewModel::setProductionDayToday, contentPadding = PaddingValues(horizontal = 6.dp), modifier = Modifier.height(28.dp)) {
            Text(s.dayToday, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { viewModel.shiftProductionDay(1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowForward, s.dayNext, modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun ToolbarDayField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = CuttingActualColors.TextPrimary,
        ),
        cursorBrush = SolidColor(CuttingActualColors.Primary),
        modifier = Modifier
            .width(130.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, CuttingActualColors.BorderLight, RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun ToolbarMachineField(
    text: String,
    placeholder: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(115.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFFFFFBEB) else Color.White)
            .border(1.dp, CuttingActualColors.WarningLight5, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.ifBlank { placeholder },
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) CuttingActualColors.ToolbarMachineLabel else CuttingActualColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MachineToolbarRow(uiState: CuttingUiState, s: CutStrings, viewModel: CuttingActualViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(CuttingActualColors.WarningLight9, Color.White, Color(0xFFFFF7ED))))
            .border(1.dp, CuttingActualColors.ToolbarMachineBorder, RoundedCornerShape(10.dp)).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))), RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(s.machine, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CuttingActualColors.ToolbarMachineLabel, modifier = Modifier.padding(horizontal = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            val selectedLabel = uiState.machines.find { it.id == uiState.selectedMachineId }?.label.orEmpty()
            ToolbarMachineField(
                text = selectedLabel,
                placeholder = s.machinePlaceholder,
                selected = uiState.selectedMachineId != null,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.machines.forEach { m -> DropdownMenuItem(text = { Text(m.label) }, onClick = { viewModel.setSelectedMachine(m.id); expanded = false }) }
            }
        }
    }
}

@Composable
private fun FilterSwitchRow(hideCompleted: Boolean, s: CutStrings, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.height(36.dp).clip(RoundedCornerShape(999.dp))
            .background(if (hideCompleted) Brush.verticalGradient(listOf(CuttingActualColors.SuccessLight9, Color.White)) else Brush.verticalGradient(listOf(Color.White, Color.White)))
            .border(1.dp, if (hideCompleted) CuttingActualColors.SuccessLight5 else CuttingActualColors.BorderLight, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(s.hideCompleted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (hideCompleted) CuttingActualColors.SuccessDark else CuttingActualColors.TextSecondary)
        Switch(checked = hideCompleted, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedTrackColor = CuttingActualColors.Success, uncheckedTrackColor = CuttingActualColors.Border))
        Text(if (hideCompleted) s.filterSwitchOn else s.filterSwitchOff, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun OfflineStrip(text: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(CuttingActualColors.OfflineStrip).border(1.dp, CuttingActualColors.OfflineStripBorder, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(7.dp).background(CuttingActualColors.Warning, RoundedCornerShape(50)))
        Text(text, fontSize = 12.sp, color = CuttingActualColors.WarningDark)
    }
}

@Composable private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text(text, color = CuttingActualColors.TextMuted, fontSize = 14.sp) }
}

@Composable
private fun DayGroupSection(
    group: CuttingDayGroupUi,
    s: CutStrings,
    operators: List<com.example.smart_emap.data.model.UserListItemDto>,
    viewModel: CuttingActualViewModel,
    reorderEnabled: Boolean,
    onDragActiveChange: (Boolean) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White)
            .border(1.dp, if (group.isAnchorDay) CuttingActualColors.AnchorDayBorder else CuttingActualColors.BorderLight, RoundedCornerShape(10.dp))
            .shadow(1.dp, RoundedCornerShape(10.dp)),
    ) {
        Row(
            Modifier.fillMaxWidth().background(if (group.isAnchorDay) Brush.verticalGradient(listOf(CuttingActualColors.PrimaryLight9, Color.White)) else Brush.verticalGradient(listOf(CuttingActualColors.DayGroupHeadBg, Color.White))).padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(group.dayLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (group.isAnchorDay) Text(s.anchorDayBadge, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.background(CuttingActualColors.Primary, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text(group.countLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CuttingActualColors.TextSecondary)
        }
        HorizontalDivider(color = CuttingActualColors.BorderLight)
        ReorderableDayRows(
            group = group,
            s = s,
            operators = operators,
            viewModel = viewModel,
            reorderEnabled = reorderEnabled,
            onDragActiveChange = onDragActiveChange,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun ReorderableDayRows(
    group: CuttingDayGroupUi,
    s: CutStrings,
    operators: List<com.example.smart_emap.data.model.UserListItemDto>,
    viewModel: CuttingActualViewModel,
    reorderEnabled: Boolean,
    onDragActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val rowGapPx = with(density) { 8.dp.toPx() }
    val rows = remember(group.dayKey) { group.rows.toMutableStateList() }
    var draggingPlanId by remember(group.dayKey) { mutableIntStateOf(-1) }
    var dragStartIndex by remember(group.dayKey) { mutableIntStateOf(-1) }
    var dragOffsetY by remember(group.dayKey) { mutableFloatStateOf(0f) }
    val itemHeights = remember(group.dayKey) { mutableMapOf<Int, Int>() }

    LaunchedEffect(group.rows) {
        if (draggingPlanId < 0) {
            rows.clear()
            rows.addAll(group.rows)
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            key(row.planId) {
                val isDragging = row.planId == draggingPlanId
                val cardDragModifier = if (row.canDragReorder && reorderEnabled) {
                    Modifier.pointerInput(row.planId, reorderEnabled) {
                        detectLongPressDragGestures(
                            longPressTimeoutMillis = ReorderLongPressMs,
                            onDragStart = {
                                draggingPlanId = row.planId
                                dragStartIndex = rows.indexOfFirst { it.planId == row.planId }
                                dragOffsetY = 0f
                                onDragActiveChange(true)
                            },
                            onDrag = { delta -> dragOffsetY += delta.y },
                            onDragEnd = {
                                if (draggingPlanId >= 0 && dragStartIndex >= 0) {
                                    applyPlanReorderFromOffset(
                                        rows = rows,
                                        startIndex = dragStartIndex,
                                        totalOffsetY = dragOffsetY,
                                        itemHeights = itemHeights,
                                        rowGapPx = rowGapPx,
                                    )
                                    viewModel.commitDayReorder(group.dayKey, rows.map { it.planId })
                                }
                                draggingPlanId = -1
                                dragStartIndex = -1
                                dragOffsetY = 0f
                                onDragActiveChange(false)
                            },
                            onDragCancel = {
                                draggingPlanId = -1
                                dragStartIndex = -1
                                dragOffsetY = 0f
                                onDragActiveChange(false)
                            },
                        )
                    }
                } else {
                    Modifier
                }
                PlanRowCard(
                    row = row,
                    s = s,
                    operators = operators,
                    viewModel = viewModel,
                    cardModifier = Modifier
                        .then(cardDragModifier)
                        .zIndex(if (isDragging) 1f else 0f)
                        .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                        .alpha(if (isDragging) 0.88f else 1f)
                        .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(8.dp))
                        .onSizeChanged { itemHeights[row.planId] = it.height },
                )
            }
        }
    }
}

private fun applyPlanReorderFromOffset(
    rows: MutableList<CuttingPlanRowUi>,
    startIndex: Int,
    totalOffsetY: Float,
    itemHeights: Map<Int, Int>,
    rowGapPx: Float,
) {
    if (startIndex !in rows.indices || totalOffsetY == 0f) return
    var index = startIndex
    var offset = totalOffsetY
    val fallbackHeight = itemHeights[rows[index].planId]
        ?: itemHeights.values.maxOrNull()
        ?: 280
    val step = fallbackHeight + rowGapPx
    val threshold = step / 2f
    var moved = false

    while (offset > threshold && index < rows.lastIndex) {
        val target = index + 1
        if (!rows[target].canDragReorder) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset -= step
        index = target
        moved = true
    }
    while (offset < -threshold && index > 0) {
        val target = index - 1
        if (!rows[target].canDragReorder) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset += step
        index = target
        moved = true
    }
    if (moved) refreshPlanRowSequences(rows)
}

private fun refreshPlanRowSequences(rows: MutableList<CuttingPlanRowUi>) {
    rows.forEachIndexed { idx, row ->
        val seq = idx + 1
        if (row.productionSequence != seq) {
            rows[idx] = row.copy(productionSequence = seq)
        }
    }
}

private suspend fun PointerInputScope.detectLongPressDragGestures(
    longPressTimeoutMillis: Long,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val pointerId = down.id
        var dragStarted = false
        var canceled = false
        val handler = Handler(Looper.getMainLooper())
        val longPressRunnable = Runnable {
            if (!canceled) {
                dragStarted = true
                onDragStart()
            }
        }
        handler.postDelayed(longPressRunnable, longPressTimeoutMillis)
        try {
            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == pointerId }
                if (change == null) {
                    if (dragStarted) continue
                    break
                }
                if (!change.pressed) {
                    if (dragStarted) onDragEnd()
                    break
                }
                if (!dragStarted) {
                    if (change.positionChange().getDistance() > viewConfiguration.touchSlop) break
                    continue
                }
                val delta = change.positionChange()
                change.consume()
                onDrag(delta)
            }
        } catch (e: CancellationException) {
            if (dragStarted) onDragCancel()
            throw e
        } finally {
            canceled = true
            handler.removeCallbacks(longPressRunnable)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlanRowCard(
    row: CuttingPlanRowUi,
    s: CutStrings,
    operators: List<com.example.smart_emap.data.model.UserListItemDto>,
    viewModel: CuttingActualViewModel,
    cardModifier: Modifier = Modifier,
) {
    val confirmed = row.isConfirmedDisplay
    val leftBorder = if (confirmed) CuttingActualColors.PlanCardBorderLeftConfirmed else CuttingActualColors.PlanCardBorderLeft
    Card(
        colors = CardDefaults.cardColors(containerColor = if (confirmed) CuttingActualColors.ConfirmedCardBg else Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = cardModifier
            .fillMaxWidth()
            .alpha(if (confirmed) 0.92f else 1f)
            .border(1.dp, if (confirmed) CuttingActualColors.ConfirmedStatusBorder else CuttingActualColors.BorderLight, RoundedCornerShape(8.dp))
            .border(3.dp, leftBorder, RoundedCornerShape(8.dp)),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(end = if (row.canEditConfirmed) 52.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlanRowHead(row, s, viewModel)
                PlanRowMeta(row, s, operators, viewModel)
                PlanRowOps(row, s, viewModel)
                PlanRowActions(row, s, viewModel)
            }
            if (row.canEditConfirmed) {
                TextButton(
                    onClick = { viewModel.openConfirmedEdit(row.planId) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = s.btnEditConfirmed,
                        tint = CuttingActualColors.Primary,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        s.btnEditConfirmed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CuttingActualColors.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanRowHead(row: CuttingPlanRowUi, s: CutStrings, viewModel: CuttingActualViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = s.dragToReorder,
            tint = if (row.canDragReorder) CuttingActualColors.TextMuted else CuttingActualColors.TextMuted.copy(alpha = 0.35f),
            modifier = Modifier.size(20.dp),
        )
        Text("${s.seq} ${row.productionSequence ?: "—"}", fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.background(
                if (row.isConfirmedDisplay) CuttingActualColors.ConfirmedStatusBg else CuttingActualColors.DayGroupHeadBg,
                RoundedCornerShape(999.dp),
            ).border(
                1.dp,
                if (row.isConfirmedDisplay) CuttingActualColors.ConfirmedStatusBorder else CuttingActualColors.BorderLight,
                RoundedCornerShape(999.dp),
            ).padding(horizontal = 10.dp, vertical = 2.dp))
        StatusTag(row.statusText, row.isConfirmedDisplay)
        if (row.canScan) TextButton(onClick = { viewModel.openScanDialog(row.planId) }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(22.dp)) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(2.dp)); Text(s.btnScanCode, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        if (row.hasScan) Text(row.scannedCode.take(8), fontSize = 11.sp, color = CuttingActualColors.Primary,
            modifier = Modifier.background(CuttingActualColors.PrimaryLight9, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        if (row.remarks.isNotEmpty()) Text(row.remarks, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.Danger, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp).background(CuttingActualColors.DangerLight9, RoundedCornerShape(6.dp)).border(1.dp, CuttingActualColors.DangerLight5, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
private fun StatusTag(text: String, confirmed: Boolean) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (confirmed) CuttingActualColors.ConfirmedStatusText else CuttingActualColors.WarningDark,
        modifier = Modifier
            .background(
                if (confirmed) CuttingActualColors.ConfirmedStatusBg else CuttingActualColors.WarningLight9,
                RoundedCornerShape(4.dp),
            )
            .border(
                1.dp,
                if (confirmed) CuttingActualColors.ConfirmedStatusBorder else CuttingActualColors.WarningLight5,
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlanRowMeta(row: CuttingPlanRowUi, s: CutStrings, operators: List<com.example.smart_emap.data.model.UserListItemDto>, viewModel: CuttingActualViewModel) {
    val confirmed = row.isConfirmedDisplay
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(
                if (confirmed) {
                    Brush.verticalGradient(
                        listOf(
                            CuttingActualColors.ConfirmedMetaGradientStart,
                            CuttingActualColors.ConfirmedMetaGradientMid,
                            CuttingActualColors.ConfirmedMetaGradientEnd,
                        ),
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            CuttingActualColors.MetaGradientStart,
                            CuttingActualColors.MetaGradientMid,
                            CuttingActualColors.MetaGradientEnd,
                        ),
                    )
                },
            )
            .border(
                1.dp,
                if (confirmed) CuttingActualColors.ConfirmedMetaBorder else CuttingActualColors.MetaBorder,
                RoundedCornerShape(10.dp),
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductNameChip(row.productName, confirmed)
        if (row.materialName.isNotEmpty()) {
            MaterialNameChip(row.materialName)
        }
        QtyChip(row.qtyLabel, row.qtyValue, row.isConfirmedDisplay)
        if (row.mgmtCodeLabel.isNotEmpty()) CodeChip(row.mgmtCodeLabel)
        OperatorField(row, s, operators, viewModel)
    }
}

@Composable
private fun ProductNameChip(name: String, confirmed: Boolean) {
    Box(
        Modifier
            .height(MetaChipHeight)
            .widthIn(max = 140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (confirmed) {
                    Brush.linearGradient(
                        listOf(
                            CuttingActualColors.ConfirmedProductBgStart,
                            CuttingActualColors.ConfirmedProductBgEnd,
                        ),
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            CuttingActualColors.ProductPrimaryBgStart,
                            CuttingActualColors.ProductPrimaryBgEnd,
                        ),
                    )
                },
            )
            .border(
                1.dp,
                if (confirmed) CuttingActualColors.ConfirmedProductBorder else CuttingActualColors.ProductPrimaryBorder,
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = name.ifBlank { "—" },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (confirmed) CuttingActualColors.ConfirmedProductName else CuttingActualColors.ProductName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MaterialNameChip(name: String) {
    Box(
        Modifier
            .height(MetaChipHeight)
            .widthIn(min = 96.dp, max = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, CuttingActualColors.MaterialBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuttingActualColors.MaterialText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable private fun QtyChip(label: String, value: String, isActual: Boolean) {
    val bg = if (isActual) Brush.verticalGradient(listOf(CuttingActualColors.QtyActualBgStart, CuttingActualColors.QtyActualBgEnd))
    else Brush.verticalGradient(listOf(CuttingActualColors.QtyChipBgStart, CuttingActualColors.QtyChipBgEnd))
    Row(
        Modifier
            .height(MetaChipHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (isActual) CuttingActualColors.QtyActualBorder else CuttingActualColors.QtyChipBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.QtyChipLabel)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CuttingActualColors.QtyChipValue)
    }
}

@Composable private fun CodeChip(text: String) {
    Box(
        Modifier
            .height(MetaChipHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(CuttingActualColors.CodeChipBgStart, CuttingActualColors.CodeChipBgEnd)))
            .border(1.dp, CuttingActualColors.CodeChipBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.CodeChipText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperatorField(row: CuttingPlanRowUi, s: CutStrings, operators: List<com.example.smart_emap.data.model.UserListItemDto>, viewModel: CuttingActualViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val label = operators.find { it.id == row.operatorUserId }?.displayLabel().orEmpty()
    val selected = row.operatorUserId != null
    Row(
        Modifier
            .height(MetaChipHeight)
            .widthIn(min = OperatorContainerMinWidth)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(CuttingActualColors.OperatorBgStart, CuttingActualColors.OperatorBgEnd)))
            .border(1.dp, CuttingActualColors.OperatorBorder, RoundedCornerShape(8.dp))
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Person, null, tint = CuttingActualColors.OperatorLabel, modifier = Modifier.size(14.dp))
        Text(s.operator, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.OperatorLabel, modifier = Modifier.padding(horizontal = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (!row.isConfirmedDisplay) expanded = it }) {
            ToolbarOperatorField(
                text = label,
                placeholder = s.operatorPlaceholder,
                selected = selected,
                enabled = !row.isConfirmedDisplay,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("—") }, onClick = { viewModel.setOperator(row.planId, null); expanded = false })
                operators.forEach { u -> val id = u.id ?: return@forEach; DropdownMenuItem(text = { Text(u.displayLabel()) }, onClick = { viewModel.setOperator(row.planId, id); expanded = false }) }
            }
        }
    }
}

@Composable private fun PlanRowOps(row: CuttingPlanRowUi, s: CutStrings, viewModel: CuttingActualViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MinuteStepperField(s.setupTime, s.setupTimeUnit, row.setupTimeMin, !row.isConfirmedDisplay, CuttingActualColors.SetupBgStart, CuttingActualColors.SetupBgEnd, CuttingActualColors.SetupBorder, CuttingActualColors.SetupLabel) { viewModel.setSetupTime(row.planId, it) }
        MinuteStepperField(s.sawBladeExchange, s.setupTimeUnit, row.sawBladeExchangeMin, !row.isConfirmedDisplay, CuttingActualColors.BladeBgStart, CuttingActualColors.BladeBgEnd, CuttingActualColors.BladeBorder, CuttingActualColors.BladeLabel) { viewModel.setSawBlade(row.planId, it) }
        MinuteStepperField(s.repairTime, s.setupTimeUnit, row.repairMin, !row.isConfirmedDisplay, CuttingActualColors.RepairBgStart, CuttingActualColors.RepairBgEnd, CuttingActualColors.RepairBorder, CuttingActualColors.RepairLabel) { viewModel.setRepair(row.planId, it) }
    }
}

@Composable
private fun MinuteStepperField(label: String, unit: String, value: Int?, enabled: Boolean, bgStart: Color, bgEnd: Color, border: Color, labelColor: Color, onChange: (Int?) -> Unit) {
    Row(Modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(bgStart, bgEnd))).border(1.dp, border, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(Modifier.widthIn(min = 52.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, tint = labelColor, modifier = Modifier.size(12.dp)); Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = labelColor) }
            Text("（$unit）", fontSize = 9.sp, color = labelColor.copy(alpha = 0.85f))
        }
        IconButton(onClick = { onChange(((value ?: 0) - 1).coerceAtLeast(0).let { if (it == 0 && value == null) null else it }) }, enabled = enabled, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = labelColor, modifier = Modifier.size(16.dp)) }
        MinuteStepperValueField(value = value, enabled = enabled, border = border, onChange = onChange)
        IconButton(onClick = { onChange((value ?: 0) + 1) }, enabled = enabled, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = labelColor, modifier = Modifier.size(16.dp)) }
    }
}

@Composable
private fun ToolbarOperatorField(
    text: String,
    placeholder: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(OperatorFieldWidth)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, CuttingActualColors.OperatorBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.ifBlank { placeholder },
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                !enabled -> CuttingActualColors.TextMuted
                selected -> CuttingActualColors.TextPrimary
                else -> CuttingActualColors.TextSecondary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MinuteStepperValueField(
    value: Int?,
    enabled: Boolean,
    border: Color,
    onChange: (Int?) -> Unit,
) {
    BasicTextField(
        value = value?.toString().orEmpty(),
        onValueChange = { raw ->
            val trimmed = raw.trim()
            onChange(if (trimmed.isEmpty()) null else trimmed.toIntOrNull())
        },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = CuttingActualColors.TextPrimary,
        ),
        cursorBrush = SolidColor(border),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(48.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, border, RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            }
        },
    )
}

@Composable private fun PlanRowActions(row: CuttingPlanRowUi, s: CutStrings, viewModel: CuttingActualViewModel) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        TimerCompact(row, s, Modifier.height(PlanRunBlockHeight))
        PlanActionButton(s.btnStart, Icons.Default.PlayArrow, row.canStart, row.productionInProgress, Brush.verticalGradient(listOf(CuttingActualColors.BtnStartStart, CuttingActualColors.BtnStartEnd)), Color.White) { viewModel.onStart(row.planId) }
        when {
            row.canResume -> PlanActionButton(s.btnResume, Icons.Default.PlayArrow, true, false, Brush.verticalGradient(listOf(CuttingActualColors.BtnResumeStart, CuttingActualColors.BtnResumeEnd)), Color.White) { viewModel.onResume(row.planId) }
            row.canPause -> PlanActionButton(s.btnPause, Icons.Default.Pause, true, false, Brush.verticalGradient(listOf(CuttingActualColors.BtnPauseStart, CuttingActualColors.BtnPauseEnd)), CuttingActualColors.BtnPauseText) { viewModel.onPause(row.planId) }
            else -> PlanActionButton(s.btnPause, Icons.Default.Pause, false, false, Brush.verticalGradient(listOf(CuttingActualColors.BtnDisabledBg, CuttingActualColors.BtnDisabledBg)), CuttingActualColors.BtnDisabledText) {}
        }
        PlanActionButton(s.btnEnd, Icons.Default.CheckCircle, row.canEnd, false, Brush.verticalGradient(listOf(CuttingActualColors.BtnEndStart, CuttingActualColors.BtnEndEnd)), Color.White) { viewModel.openEndDialog(row.planId) }
        PlanActionButton(s.btnChangeMachine, Icons.Default.Build, row.canChangeMachine, false, Brush.verticalGradient(listOf(CuttingActualColors.BtnMachineStart, CuttingActualColors.BtnMachineEnd)), Color.White) { viewModel.openChangeMachine(row.planId) }
    }
}

@Composable private fun TimerCompact(row: CuttingPlanRowUi, s: CutStrings, modifier: Modifier = Modifier) {
    val tc = timerColors(row.timerPhase)
    Column(modifier.width(155.dp).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(listOf(tc.start, tc.end))).border(1.dp, tc.border, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.Center) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccessTime, null, tint = tc.accent, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(3.dp)); Text(s.elapsed, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tc.accent) }
            Text(row.timerPhaseLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tc.accent, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(999.dp)).border(1.dp, tc.border, RoundedCornerShape(999.dp)).padding(horizontal = 7.dp, vertical = 1.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(row.elapsedDisplay, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (row.timerDisplayFrozen) CuttingActualColors.TimerPausedText else tc.accent)
            if (row.showTimerPauseSide) Column(horizontalAlignment = Alignment.End) { Text(s.pausedAccum, fontSize = 8.sp, color = CuttingActualColors.TextMuted); Text(row.pausedDisplay, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.TimerPausedText) }
        }
        HorizontalDivider(Modifier.padding(top = 4.dp), color = tc.border.copy(alpha = 0.35f), thickness = 1.dp)
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(row.wallStartDisplay, fontSize = 10.sp, color = tc.accent, fontFamily = FontFamily.Monospace); Text(" → ", fontSize = 10.sp, color = tc.accent.copy(alpha = 0.5f)); Text(row.wallEndDisplay, fontSize = 10.sp, color = tc.accent, fontFamily = FontFamily.Monospace)
        }
    }
}

private data class TimerColorSet(val start: Color, val end: Color, val border: Color, val accent: Color)
private fun timerColors(phase: TimerPhase) = when (phase) {
    TimerPhase.Running -> TimerColorSet(CuttingActualColors.TimerRunningBgStart, CuttingActualColors.TimerRunningBgEnd, CuttingActualColors.TimerRunningBorder, CuttingActualColors.TimerRunningText)
    TimerPhase.Paused -> TimerColorSet(CuttingActualColors.TimerPausedBgStart, CuttingActualColors.TimerPausedBgEnd, CuttingActualColors.TimerPausedBorder, CuttingActualColors.TimerPausedText)
    TimerPhase.Ended -> TimerColorSet(CuttingActualColors.TimerEndedBgStart, CuttingActualColors.TimerEndedBgEnd, CuttingActualColors.TimerEndedBorder, CuttingActualColors.TimerEndedText)
    TimerPhase.Idle -> TimerColorSet(CuttingActualColors.TimerIdleBgStart, CuttingActualColors.TimerIdleBgEnd, CuttingActualColors.TimerIdleBorder, CuttingActualColors.TextMuted)
}

@Composable
private fun PlanActionButton(text: String, icon: ImageVector, enabled: Boolean, locked: Boolean, gradient: Brush, textColor: Color, onClick: () -> Unit) {
    val bg = if (locked || !enabled) Brush.verticalGradient(listOf(CuttingActualColors.BtnDisabledBg, CuttingActualColors.BtnDisabledBg)) else gradient
    val fg = if (locked || !enabled) CuttingActualColors.BtnDisabledText else textColor
    Column(Modifier.width(PlanActBtnWidth).height(PlanRunBlockHeight).clip(RoundedCornerShape(8.dp)).background(bg).clickable(enabled = enabled && !locked, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp)); Spacer(Modifier.height(4.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg, maxLines = 2, lineHeight = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable private fun EndCuttingDialog(uiState: CuttingUiState, s: CutStrings, viewModel: CuttingActualViewModel) {
    val submitting = uiState.endDialogSubmitting
    Dialog(onDismissRequest = { if (!submitting) viewModel.closeEndDialog() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        s.endDialogTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CuttingActualColors.TextPrimary,
                    )
                    IconButton(
                        onClick = { if (!submitting) viewModel.closeEndDialog() },
                        modifier = Modifier.size(32.dp),
                        enabled = !submitting,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.btnCancel, tint = CuttingActualColors.TextSecondary)
                    }
                }
                Text(
                    uiState.endDialogProductLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CuttingActualColors.TextPrimary,
                    lineHeight = 19.sp,
                )
                if (uiState.endDialogMetaIncomplete) {
                    EndDialogWarningBanner(s.endDialogMetaMissingHint)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EndDialogMetaItem(
                        label = s.operator,
                        value = if (uiState.endDialogHasOperator) {
                            uiState.endDialogOperatorLabel
                        } else {
                            s.endDialogOperatorMissing
                        },
                        missing = !uiState.endDialogHasOperator,
                        modifier = Modifier.weight(1f),
                    )
                    EndDialogMetaItem(
                        label = s.setupTime,
                        value = if (uiState.endDialogHasSetupTime) {
                            "${uiState.endDialogSetupTimeMin} ${s.setupTimeUnit}"
                        } else {
                            s.endDialogSetupTimeMissing
                        },
                        missing = !uiState.endDialogHasSetupTime,
                        modifier = Modifier.weight(1f),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        s.endDialogQtyLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CuttingActualColors.TextSecondary,
                    )
                    OutlinedTextField(
                        value = uiState.endDialogQty,
                        onValueChange = viewModel::onEndDialogQtyChange,
                        placeholder = { Text(s.endDialogQtyPlaceholder, fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CuttingActualColors.Primary,
                            unfocusedBorderColor = CuttingActualColors.BorderLight,
                            cursorColor = CuttingActualColors.Primary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
                if (uiState.endDialogBaseline > 0) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(CuttingActualColors.SetupBgEnd, CuttingActualColors.SetupBgStart),
                                ),
                            )
                            .border(1.dp, CuttingActualColors.SetupBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            s.deferNextDayLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = CuttingActualColors.TextSecondary,
                        )
                        OutlinedTextField(
                            value = uiState.endDialogDeferDay,
                            onValueChange = viewModel::onEndDialogDeferDayChange,
                            singleLine = true,
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 14.sp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = CuttingActualColors.SetupLabel,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CuttingActualColors.SetupBorder,
                                unfocusedBorderColor = CuttingActualColors.BorderLight,
                                cursorColor = CuttingActualColors.Primary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        )
                        if (uiState.endDialogSubsequentDeferCount > 0) {
                            Row(verticalAlignment = Alignment.Top) {
                                Checkbox(
                                    checked = uiState.endDialogDeferSubsequent,
                                    onCheckedChange = {
                                        if (!submitting) viewModel.onEndDialogDeferSubsequentChange(it)
                                    },
                                    enabled = !submitting,
                                )
                                Text(
                                    s.format(s.deferSubsequentLabel, "n" to uiState.endDialogSubsequentDeferCount),
                                    fontSize = 14.sp,
                                    color = CuttingActualColors.TextSecondary,
                                    modifier = Modifier.padding(top = 12.dp),
                                )
                            }
                        }
                    }
                }
                if (uiState.endDialogBaseline > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EndDialogActionButton(
                            text = s.format(s.btnCompleteFull, "n" to uiState.endDialogBaseline),
                            gradient = Brush.verticalGradient(listOf(Color(0xFF3ECF7A), CuttingActualColors.Success)),
                            textColor = Color.White,
                            enabled = !submitting,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::submitProductionEndFull,
                        )
                        EndDialogActionButton(
                            text = s.btnDeferConfirm,
                            gradient = Brush.verticalGradient(listOf(Color(0xFFFFC857), CuttingActualColors.Warning)),
                            textColor = CuttingActualColors.BtnPauseText,
                            enabled = !submitting,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::requestProductionEndDefer,
                        )
                    }
                } else {
                    EndDialogActionButton(
                        text = s.btnCompleteWithInput,
                        gradient = Brush.verticalGradient(listOf(CuttingActualColors.LoadBtnStart, CuttingActualColors.Primary)),
                        textColor = Color.White,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::submitProductionEndWithInput,
                    )
                }
                TextButton(
                    onClick = { if (!submitting) viewModel.closeEndDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !submitting,
                ) {
                    Text(s.btnCancel, fontSize = 14.sp, color = CuttingActualColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun EndDialogWarningBanner(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CuttingActualColors.WarningLight9)
            .border(1.dp, CuttingActualColors.WarningLight5, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = CuttingActualColors.Warning, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 14.sp, color = CuttingActualColors.WarningDark, lineHeight = 18.sp)
    }
}

@Composable
private fun EndDialogMetaItem(label: String, value: String, missing: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CuttingActualColors.DayGroupHeadBg)
            .border(1.dp, CuttingActualColors.BorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CuttingActualColors.TextSecondary)
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = if (missing) FontWeight.SemiBold else FontWeight.Bold,
            color = if (missing) CuttingActualColors.WarningDark else CuttingActualColors.TextPrimary,
        )
    }
}

@Composable
private fun EndDialogActionButton(
    text: String,
    gradient: Brush,
    textColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (enabled) {
        gradient
    } else {
        Brush.verticalGradient(listOf(CuttingActualColors.BtnDisabledBg, CuttingActualColors.BtnDisabledBg))
    }
    val fg = if (enabled) textColor else CuttingActualColors.BtnDisabledText
    Box(
        modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun DeferQtyDialog(uiState: CuttingUiState, s: CutStrings, viewModel: CuttingActualViewModel) {
    val submitting = uiState.endDialogSubmitting
    val baseline = uiState.endDialogBaseline
    val actualQty = uiState.deferQtyDialogQty.trim().toIntOrNull()
    val remainder = if (actualQty != null && baseline > 0 && actualQty < baseline) baseline - actualQty else null
    val canSubmit = !submitting && remainder != null && remainder > 0
    Dialog(onDismissRequest = { if (!submitting) viewModel.closeDeferQtyDialog() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        s.deferQtyDialogTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CuttingActualColors.TextPrimary,
                    )
                    IconButton(
                        onClick = { if (!submitting) viewModel.closeDeferQtyDialog() },
                        modifier = Modifier.size(32.dp),
                        enabled = !submitting,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.btnCancel, tint = CuttingActualColors.TextSecondary)
                    }
                }
                Text(
                    uiState.endDialogProductLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CuttingActualColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    s.format(s.deferQtyDialogDeferToLabel, "date" to uiState.endDialogDeferDay.trim().take(10)),
                    fontSize = 13.sp,
                    color = CuttingActualColors.TextSecondary,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CuttingActualColors.PrimaryLight9)
                        .border(1.dp, CuttingActualColors.PrimaryLight8, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(s.deferQtyDialogBaselineLabel, fontSize = 13.sp, color = CuttingActualColors.TextSecondary)
                    Text(
                        "$baseline",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CuttingActualColors.TextPrimary,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        s.deferQtyDialogActualLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CuttingActualColors.TextSecondary,
                    )
                    OutlinedTextField(
                        value = uiState.deferQtyDialogQty,
                        onValueChange = viewModel::onDeferQtyDialogQtyChange,
                        placeholder = { Text(s.endDialogQtyPlaceholder, fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !submitting,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CuttingActualColors.Primary,
                            unfocusedBorderColor = CuttingActualColors.BorderLight,
                            cursorColor = CuttingActualColors.Primary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CuttingActualColors.SetupBgEnd, CuttingActualColors.SetupBgStart),
                            ),
                        )
                        .border(1.dp, CuttingActualColors.SetupBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(s.deferQtyDialogRemainderLabel, fontSize = 13.sp, color = CuttingActualColors.SetupLabel)
                    Text(
                        if (remainder != null) "$remainder" else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (remainder != null && remainder > 0) {
                            CuttingActualColors.WarningDark
                        } else {
                            CuttingActualColors.TextMuted
                        },
                    )
                }
                if (actualQty != null && baseline > 0 && actualQty >= baseline) {
                    Text(
                        s.deferQtyMustBeLess,
                        fontSize = 12.sp,
                        color = CuttingActualColors.Danger,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EndDialogActionButton(
                        text = s.btnDeferConfirm,
                        gradient = Brush.verticalGradient(listOf(Color(0xFFFFC857), CuttingActualColors.Warning)),
                        textColor = CuttingActualColors.BtnPauseText,
                        enabled = canSubmit,
                        modifier = Modifier.weight(1f),
                        onClick = viewModel::submitProductionEndDefer,
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CuttingActualColors.BorderLight, RoundedCornerShape(8.dp))
                            .clickable(enabled = !submitting) { viewModel.closeDeferQtyDialog() }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            s.btnCancel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CuttingActualColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ChangeMachineDialog(uiState: CuttingUiState, s: CutStrings, viewModel: CuttingActualViewModel) {
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = viewModel::closeChangeMachine, title = { Text(s.changeMachineDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${s.machine}: ${uiState.changeMachineCurrentName}")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(uiState.machines.find { it.id == uiState.changeMachineTargetId }?.label.orEmpty(), {}, readOnly = true, label = { Text(s.machine) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        uiState.machines.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.label) },
                                onClick = { viewModel.setChangeMachineTarget(m.id); expanded = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = viewModel::submitChangeMachine, enabled = !uiState.changeMachineSubmitting) { Text(s.btnChangeMachineSave) } },
        dismissButton = { TextButton(onClick = viewModel::closeChangeMachine) { Text(s.btnCancel) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ConfirmedCuttingEditDialog(
    uiState: CuttingUiState,
    s: CutStrings,
    operators: List<com.example.smart_emap.data.model.UserListItemDto>,
    viewModel: CuttingActualViewModel,
) {
    val submitting = uiState.confirmedEditSubmitting
    val scrollState = rememberScrollState()
    var operatorExpanded by remember { mutableStateOf(false) }
    var expandedDateTime by remember { mutableStateOf<ConfirmedEditDateTimeTarget?>(null) }
    val weldLocale = cutLocaleToWeld(uiState.locale)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CuttingActualColors.Primary,
        unfocusedBorderColor = CuttingActualColors.BorderLight,
        cursorColor = CuttingActualColors.Primary,
    )
    val elapsedPreview = run {
        val ws = uiState.confirmedEditWallStartMs
        val we = uiState.confirmedEditWallEndMs
        val pauseMs = (uiState.confirmedEditPausedSec.toLongOrNull() ?: 0L) * 1000L
        if (ws == null || we == null) {
            "00:00:00"
        } else {
            CuttingSessionLogic.formatDurationMs((we - ws - pauseMs).coerceAtLeast(0))
        }
    }
    val operatorLabel = operators.find { it.id == uiState.confirmedEditOperatorUserId }?.displayLabel().orEmpty()

    Dialog(onDismissRequest = { if (!submitting) viewModel.closeConfirmedEdit() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(CuttingActualColors.PrimaryLight9, Color.White)))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Edit, null, tint = CuttingActualColors.Primary, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.confirmedEditDialogTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CuttingActualColors.TextPrimary)
                        Text(uiState.confirmedEditProductLabel, fontSize = 11.sp, color = CuttingActualColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                HorizontalDivider(color = CuttingActualColors.BorderLight)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExposedDropdownMenuBox(
                        expanded = operatorExpanded,
                        onExpandedChange = { if (!submitting) operatorExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = operatorLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.operator) },
                            placeholder = { Text(s.operatorPlaceholder) },
                            enabled = !submitting,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operatorExpanded) },
                            colors = fieldColors,
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = operatorExpanded, onDismissRequest = { operatorExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("—") },
                                onClick = {
                                    viewModel.onConfirmedEditOperatorChange(null)
                                    operatorExpanded = false
                                },
                            )
                            operators.forEach { user ->
                                val id = user.id ?: return@forEach
                                DropdownMenuItem(
                                    text = { Text(user.displayLabel()) },
                                    onClick = {
                                        viewModel.onConfirmedEditOperatorChange(id)
                                        operatorExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.confirmedEditQty,
                            onValueChange = viewModel::onConfirmedEditQtyChange,
                            label = { Text(s.actualQty) },
                            enabled = !submitting,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.confirmedEditPausedSec,
                            onValueChange = viewModel::onConfirmedEditPausedSecChange,
                            label = { Text(s.pausedAccum) },
                            enabled = !submitting,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.confirmedEditSetupTimeMin?.toString().orEmpty(),
                            onValueChange = viewModel::onConfirmedEditSetupChange,
                            label = { Text(s.setupTime) },
                            enabled = !submitting,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.widthIn(min = 100.dp),
                        )
                        OutlinedTextField(
                            value = uiState.confirmedEditSawBladeExchangeMin?.toString().orEmpty(),
                            onValueChange = viewModel::onConfirmedEditSawBladeChange,
                            label = { Text(s.sawBladeExchange) },
                            enabled = !submitting,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.widthIn(min = 100.dp),
                        )
                        OutlinedTextField(
                            value = uiState.confirmedEditRepairMin?.toString().orEmpty(),
                            onValueChange = viewModel::onConfirmedEditRepairChange,
                            label = { Text(s.repairTime) },
                            enabled = !submitting,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = fieldColors,
                            modifier = Modifier.widthIn(min = 100.dp),
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            locale = weldLocale,
                            enabled = !submitting,
                            onEpochMillisChange = viewModel::onConfirmedEditWallStartChange,
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
                            locale = weldLocale,
                            enabled = !submitting,
                            onEpochMillisChange = viewModel::onConfirmedEditWallEndChange,
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CuttingActualColors.PrimaryLight9)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s.elapsed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CuttingActualColors.TextSecondary)
                        Text(elapsedPreview, fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CuttingActualColors.Primary)
                    }
                    Text(s.confirmedEditPauseHint, fontSize = 11.sp, color = CuttingActualColors.TextMuted, lineHeight = 14.sp)
                }
                HorizontalDivider(color = CuttingActualColors.BorderLight)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = viewModel::closeConfirmedEdit, enabled = !submitting) { Text(s.btnCancel) }
                    Button(onClick = viewModel::submitConfirmedEdit, enabled = !submitting) {
                        if (submitting) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text(s.btnSaveConfirmed)
                        }
                    }
                }
            }
        }
    }
}

private fun cutLocaleToWeld(locale: CutLocale): WeldLocale = when (locale) {
    CutLocale.Ja -> WeldLocale.Ja
    CutLocale.En -> WeldLocale.En
    CutLocale.Zh -> WeldLocale.Zh
    CutLocale.Vi -> WeldLocale.Vi
}
