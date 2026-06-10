package com.example.smart_emap.ui.mes.cuttinginstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.smart_emap.data.model.InstructionChamferingPlanRowDto
import com.example.smart_emap.data.model.InstructionChamferingRowDto
import com.example.smart_emap.data.model.InstructionCuttingRowDto
import com.example.smart_emap.data.model.InstructionPlanRowDto
import com.example.smart_emap.data.model.KanbanIssuanceRowDto
import android.os.Handler
import android.os.Looper

private const val PlanBatchEditLongPressMillis = 2_000L
private const val MgmtTableReorderLongPressMs = 1_000L

private fun applyCuttingRowReorderFromOffset(
    rows: MutableList<InstructionCuttingRowDto>,
    startIndex: Int,
    totalOffsetY: Float,
    rowHeights: Map<Int, Int>,
    rowHeightPx: Int,
    dividerPx: Float,
) {
    if (startIndex !in rows.indices || totalOffsetY == 0f) return
    val dragMachine = rows[startIndex].cuttingMachine.orEmpty().trim()
    fun sameMachineAt(index: Int): Boolean =
        index in rows.indices && rows[index].cuttingMachine.orEmpty().trim() == dragMachine
    var index = startIndex
    var offset = totalOffsetY
    val rowId = rows[index].id ?: return
    val fallbackHeight = rowHeights[rowId] ?: rowHeightPx
    val step = fallbackHeight + dividerPx
    val threshold = step / 2f
    while (offset > threshold && index < rows.lastIndex) {
        val target = index + 1
        if (!sameMachineAt(target)) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset -= step
        index = target
    }
    while (offset < -threshold && index > 0) {
        val target = index - 1
        if (!sameMachineAt(target)) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset += step
        index = target
    }
}

private fun applyChamferingRowReorderFromOffset(
    rows: MutableList<InstructionChamferingRowDto>,
    startIndex: Int,
    totalOffsetY: Float,
    rowHeights: Map<Int, Int>,
    rowHeightPx: Int,
    dividerPx: Float,
) {
    if (startIndex !in rows.indices || totalOffsetY == 0f) return
    val dragMachine = rows[startIndex].chamferingMachine.orEmpty().trim()
    fun sameMachineAt(index: Int): Boolean =
        index in rows.indices && rows[index].chamferingMachine.orEmpty().trim() == dragMachine
    var index = startIndex
    var offset = totalOffsetY
    val rowId = rows[index].id ?: return
    val fallbackHeight = rowHeights[rowId] ?: rowHeightPx
    val step = fallbackHeight + dividerPx
    val threshold = step / 2f
    while (offset > threshold && index < rows.lastIndex) {
        val target = index + 1
        if (!sameMachineAt(target)) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset -= step
        index = target
    }
    while (offset < -threshold && index > 0) {
        val target = index - 1
        if (!sameMachineAt(target)) break
        val item = rows.removeAt(index)
        rows.add(target, item)
        offset += step
        index = target
    }
}

private suspend fun PointerInputScope.detectMgmtTableLongPressDragGestures(
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

private fun Modifier.planBatchRowGestures(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier = pointerInput(onClick, onLongClick) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var longPressTriggered = false
        var canceled = false
        val handler = Handler(Looper.getMainLooper())
        val longPressRunnable = Runnable {
            if (!canceled) {
                longPressTriggered = true
                onLongClick()
            }
        }
        handler.postDelayed(longPressRunnable, PlanBatchEditLongPressMillis)
        try {
            val up = waitForUpOrCancellation()
            if (up != null && !longPressTriggered) {
                onClick()
            }
        } finally {
            canceled = true
            handler.removeCallbacks(longPressRunnable)
        }
    }
}

@Composable
private fun SplitToNextDayLinkButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(24.dp),
    ) {
        Icon(
            Icons.Default.KeyboardDoubleArrowRight,
            contentDescription = "未完了分を翌日へ順延",
            tint = Color(0xFFE6A23C),
            modifier = Modifier.size(16.dp),
        )
    }
}

private val PlanBatchHeaders = listOf("→切断", "開始日", "ライン", "順位", "製品名", "計画数", "原材料", "ロット数", "No", "生産数", "材料区分", "操作")
private val PlanBatchRowHeight = CuttingInstructionTheme.PlanBatchRowHeightDp.dp
private val PlanBatchBodyHeight = PlanBatchRowHeight * CuttingInstructionTheme.PlanBatchVisibleRows
private val LotCardToolbarButtonHeight = CuttingInstructionTheme.HeaderToolbarButtonHeightDp.dp
private val LotCardToolbarButtonShape = RoundedCornerShape(18.dp)

private val PlanBatchStartAlignHeaders = setOf("製品名", "原材料")

private fun planBatchColumnMinWidth(header: String) = when (header) {
    "→切断" -> 44.dp
    "開始日" -> 72.dp
    "ライン" -> 52.dp
    "順位" -> 40.dp
    "製品名" -> 100.dp
    "計画数" -> 55.dp
    "原材料" -> 80.dp
    "ロット数" -> 55.dp
    "No" -> 40.dp
    "生産数" -> 55.dp
    "材料区分" -> 70.dp
    "操作" -> 67.2.dp
    else -> 56.dp
}

@Composable
private fun rememberPlanBatchColumnWidths(rows: List<InstructionPlanRowDto>): Map<String, Dp> {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val cellStyle = remember { TextStyle(fontSize = 10.sp) }
    val headerStyle = remember { TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    val cellPadding = 8.dp

    return remember(rows, density) {
        fun measureWidth(text: String, style: TextStyle = cellStyle): Dp =
            with(density) {
                if (text.isBlank()) {
                    0.dp
                } else {
                    textMeasurer.measure(
                        text = text,
                        style = style,
                        maxLines = 1,
                        softWrap = false,
                    ).size.width.toDp()
                }
            }

        fun columnWidth(header: String, cellTexts: List<String>, measureStyle: TextStyle = headerStyle): Dp {
            val measured = maxOf(
                measureWidth(header, measureStyle),
                cellTexts.maxOfOrNull { measureWidth(it) } ?: 0.dp,
            )
            return maxOf(planBatchColumnMinWidth(header), measured + cellPadding)
        }

        mapOf(
            "→切断" to columnWidth("→切断", listOf("→切断"), cellStyle.copy(fontSize = 9.sp)),
            "開始日" to columnWidth("開始日", rows.map { formatInstructionDate(it.startDate) }),
            "ライン" to columnWidth("ライン", rows.map { it.productionLine.orEmpty().ifBlank { "-" } }),
            "順位" to columnWidth("順位", rows.map { it.priorityOrder?.toString() ?: "-" }),
            "製品名" to columnWidth("製品名", rows.map { it.productName ?: it.productCd ?: "-" }),
            "計画数" to columnWidth("計画数", rows.map { it.plannedQuantity?.toString() ?: "-" }),
            "原材料" to columnWidth("原材料", rows.map { it.materialName ?: "-" }),
            "ロット数" to columnWidth("ロット数", rows.map { it.productionLotSize?.toString() ?: "-" }),
            "No" to columnWidth("No", rows.map { it.lotNumber ?: "-" }),
            "生産数" to columnWidth("生産数", rows.map { it.actualProductionQuantity?.toString() ?: "-" }),
            "材料区分" to planBatchColumnMinWidth("材料区分"),
            "操作" to planBatchColumnMinWidth("操作"),
        )
    }
}

@Composable
private fun PlanBatchCellText(
    text: String,
    width: Dp,
    align: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
        fontSize = 10.sp,
        textAlign = align,
        softWrap = false,
        maxLines = 1,
    )
}

private fun planBatchRowCellText(header: String, row: InstructionPlanRowDto): String = when (header) {
    "開始日" -> formatInstructionDate(row.startDate)
    "ライン" -> row.productionLine.orEmpty().ifBlank { "-" }
    "順位" -> row.priorityOrder?.toString() ?: "-"
    "製品名" -> row.productName ?: row.productCd ?: "-"
    "計画数" -> row.plannedQuantity?.toString() ?: "-"
    "原材料" -> row.materialName ?: "-"
    "ロット数" -> row.productionLotSize?.toString() ?: "-"
    "No" -> row.lotNumber ?: "-"
    "生産数" -> row.actualProductionQuantity?.toString() ?: "-"
    else -> ""
}

@Composable
fun ProductionLotListCard(
    notesCount: Int,
    actionLoading: Boolean,
    equipmentFilter: String,
    machineOptions: List<Pair<String, String>>,
    productNameFilter: String,
    productNameOptions: List<String>,
    materialNameFilter: String,
    materialNameOptions: List<String>,
    rows: List<InstructionPlanRowDto>,
    selectedPlanId: Int?,
    loading: Boolean,
    planPage: Int,
    planTotalPages: Int,
    planTotal: Int,
    onEquipmentFilter: (String) -> Unit,
    onProductNameFilter: (String) -> Unit,
    onMaterialNameFilter: (String) -> Unit,
    onOpenNotes: () -> Unit,
    onSyncLengths: () -> Unit,
    onNewPlan: (Boolean) -> Unit,
    onSelectPlan: (InstructionPlanRowDto) -> Unit,
    onToggleStock: (InstructionPlanRowDto, Boolean) -> Unit,
    onDeletePlan: (InstructionPlanRowDto) -> Unit,
    onMoveToCutting: (InstructionPlanRowDto) -> Unit,
    onEditPlan: (InstructionPlanRowDto) -> Unit,
    onPlanPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = CuttingInstructionTheme.CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.85f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CuttingInstructionTheme.BatchAccent, modifier = Modifier.size(20.dp))
                    Text("生産ロット一覧", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CuttingInstructionTheme.BatchTitle)
                    LotCardHeaderButton(
                        text = "寸法マスタ同期",
                        onClick = onSyncLengths,
                        containerColor = CuttingInstructionTheme.BatchSyncBtnBg,
                        contentColor = CuttingInstructionTheme.BatchSyncBtnText,
                        borderColor = CuttingInstructionTheme.BatchSyncBtnBorder,
                        loading = actionLoading,
                        leadingIcon = {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                    )
                    LotCardGradientButton(
                        text = "+ 新規追加",
                        colors = listOf(CuttingInstructionTheme.BtnNew, CuttingInstructionTheme.BtnNewEnd),
                        onClick = { onNewPlan(false) },
                    )
                    LotCardGradientButton(
                        text = "試作追加",
                        colors = listOf(CuttingInstructionTheme.BtnTrial, CuttingInstructionTheme.BtnTrialEnd),
                        onClick = { onNewPlan(true) },
                    )
                }
                LotCardNotesIconButton(notesCount = notesCount, onClick = onOpenNotes)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.75f)),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlanLotFilterField(
                        label = "設備",
                        accent = CuttingInstructionTheme.BatchAccent,
                        value = equipmentFilter,
                        emptyLabel = "設備を選択",
                        options = machineOptions,
                        onSelect = onEquipmentFilter,
                        clearable = true,
                        modifier = Modifier.weight(1f),
                    )
                    PlanLotFilterField(
                        label = "製品",
                        accent = Color(0xFF6366F1),
                        value = productNameFilter,
                        emptyLabel = "全部",
                        options = productNameOptions.map { it to it },
                        onSelect = onProductNameFilter,
                        clearable = true,
                        modifier = Modifier.weight(1f),
                    )
                    PlanLotFilterField(
                        label = "材料",
                        accent = Color(0xFF059669),
                        value = materialNameFilter,
                        emptyLabel = "全部",
                        options = materialNameOptions.map { it to it },
                        onSelect = onMaterialNameFilter,
                        clearable = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            PlanBatchTable(
                rows = rows,
                selectedId = selectedPlanId,
                loading = loading,
                onSelect = onSelectPlan,
                onToggleStock = onToggleStock,
                onDelete = onDeletePlan,
                onMoveToCutting = onMoveToCutting,
                onEdit = onEditPlan,
            )
            InstructionPaginationBar(planPage, planTotalPages, planTotal, onPlanPageChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanLotFilterField(
    label: String,
    accent: Color,
    value: String,
    emptyLabel: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    clearable: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val hasValue = value.isNotBlank()
    val borderColor = if (hasValue) accent.copy(alpha = 0.45f) else CuttingInstructionTheme.FilterBorder.copy(alpha = 0.9f)
    val fieldBackground = if (hasValue) accent.copy(alpha = 0.04f) else Color.White

    Row(
        modifier = modifier.height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(7.dp),
            color = accent.copy(alpha = 0.10f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                onClick = { expanded = true },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                color = fieldBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                shadowElevation = if (expanded) 2.dp else 0.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        value.ifBlank { emptyLabel },
                        fontSize = 11.sp,
                        fontWeight = if (hasValue) FontWeight.Medium else FontWeight.Normal,
                        color = if (hasValue) CuttingInstructionTheme.Title else CuttingInstructionTheme.Subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (clearable && hasValue) {
                        IconButton(
                            onClick = { onSelect("") },
                            modifier = Modifier.size(22.dp),
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "クリア",
                                tint = CuttingInstructionTheme.Subtitle.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = accent.copy(alpha = if (expanded) 1f else 0.65f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.8f), RoundedCornerShape(10.dp)),
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            emptyLabel,
                            fontSize = 12.sp,
                            color = CuttingInstructionTheme.Subtitle,
                        )
                    },
                    onClick = { onSelect(""); expanded = false },
                )
                HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.5f))
                options.forEach { (cd, name) ->
                    val selected = value == cd
                    DropdownMenuItem(
                        text = {
                            Text(
                                name,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) accent else CuttingInstructionTheme.Title,
                            )
                        },
                        onClick = { onSelect(cd); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun LotCardHeaderButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color = CuttingInstructionTheme.FilterBorder,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        shape = LotCardToolbarButtonShape,
        modifier = Modifier.height(LotCardToolbarButtonHeight),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = contentColor)
        } else {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LotCardGradientButton(
    text: String,
    colors: List<Color>,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(LotCardToolbarButtonHeight)
            .clip(LotCardToolbarButtonShape)
            .background(Brush.horizontalGradient(colors), LotCardToolbarButtonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LotCardNotesIconButton(
    notesCount: Int,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = LotCardToolbarButtonShape,
        modifier = Modifier.height(LotCardToolbarButtonHeight),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFF5F3FF),
            contentColor = Color(0xFF5B21B6),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.Note, contentDescription = "メモ（TODO）", modifier = Modifier.size(18.dp))
            if (notesCount > 0) {
                Text(
                    if (notesCount > 99) "99+" else notesCount.toString(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp),
                    color = CuttingInstructionTheme.NotesBadgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun InstructionSectionCard(
    accent: Color,
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = CuttingInstructionTheme.Title,
    titleExtras: @Composable () -> Unit = {},
    titleSubRow: @Composable () -> Unit = {},
    headerActions: @Composable () -> Unit = {},
    fillHeight: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = CuttingInstructionTheme.CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.85f)),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accent),
                    )
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = titleColor)
                    titleExtras()
                }
                headerActions()
            }
            titleSubRow()
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f),
            )
            if (fillHeight) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

@Composable
fun InstructionDateNav(
    date: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val navHeight = CuttingInstructionTheme.InstructionDateNavHeightDp.dp
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(navHeight)
            .clip(shape)
            .border(1.dp, CuttingInstructionTheme.FilterBorder, shape)
            .background(CuttingInstructionTheme.FilterBg)
            .padding(horizontal = 2.dp),
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(navHeight)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "前日", modifier = Modifier.size(16.dp), tint = CuttingInstructionTheme.Subtitle)
        }
        Text(
            date,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuttingInstructionTheme.Title,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        IconButton(onClick = onNext, modifier = Modifier.size(navHeight)) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "翌日", modifier = Modifier.size(16.dp), tint = CuttingInstructionTheme.Subtitle)
        }
    }
}

enum class UsageSummaryActionStyle {
    Reflect,
    SpecifiedDate,
}

@Composable
fun UsageSummaryActionButton(
    text: String,
    style: UsageSummaryActionStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navHeight = CuttingInstructionTheme.InstructionDateNavHeightDp.dp
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "usageSummaryBtnScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 0.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "usageSummaryBtnElev",
    )
    val backgroundBrush: Brush
    val borderColor: Color
    val textColor: Color
    val shadowColor: Color
    when (style) {
        UsageSummaryActionStyle.Reflect -> {
            backgroundBrush = Brush.horizontalGradient(listOf(CuttingInstructionTheme.UsageReflectBtnStart, CuttingInstructionTheme.UsageReflectBtnEnd))
            borderColor = CuttingInstructionTheme.UsageReflectBtnBorder
            textColor = Color.White
            shadowColor = CuttingInstructionTheme.UsageReflectBtnShadow
        }
        UsageSummaryActionStyle.SpecifiedDate -> {
            backgroundBrush = Brush.verticalGradient(listOf(Color.White, CuttingInstructionTheme.UsageSpecifiedBtnBg))
            borderColor = CuttingInstructionTheme.UsageSpecifiedBtnBorder
            textColor = CuttingInstructionTheme.UsageSpecifiedBtnText
            shadowColor = CuttingInstructionTheme.UsageSpecifiedBtnShadow
        }
    }
    Box(
        modifier = modifier
            .height(navHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, shape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor.copy(alpha = if (pressed) 0.75f else 1f), shape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (style == UsageSummaryActionStyle.Reflect) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = if (pressed) 0.1f else 0.22f), Color.Transparent),
                            startY = 0f,
                            endY = navHeight.value,
                        ),
                    ),
            )
        }
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionFilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    includeAll: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .widthIn(min = 100.dp, max = 160.dp)
                .height(36.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CuttingInstructionTheme.FilterBg,
                contentColor = CuttingInstructionTheme.Title,
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        ) {
            Text(
                "$label: ${value.ifBlank { "全部" }}",
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (includeAll) {
                DropdownMenuItem(text = { Text("（全部）") }, onClick = { onSelect(""); expanded = false })
            }
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 12.sp) },
                    onClick = { onSelect(cd); expanded = false },
                )
            }
        }
    }
}

enum class ChamferingHeaderActionStyle {
    New,
    PlanPrint,
    IssueSheet,
    ConfirmActual,
}

@Composable
fun ChamferingHeaderActionButton(
    text: String,
    style: ChamferingHeaderActionStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val btnHeight = CuttingInstructionTheme.ChamferMgmtHeaderBtnHeightDp.dp
    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chamferHeaderBtnScale",
    )
    val elevation by animateDpAsState(
        targetValue = when {
            pressed -> 0.dp
            style == ChamferingHeaderActionStyle.New -> 1.dp
            else -> 3.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chamferHeaderBtnElev",
    )
    val backgroundBrush: Brush
    val borderColor: Color
    val textColor: Color
    val shadowColor: Color
    when (style) {
        ChamferingHeaderActionStyle.New -> {
            backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color.White,
                    CuttingInstructionTheme.ChamferMgmtBtnDefaultBg,
                    Color(0xFFF8FAFC),
                ),
            )
            borderColor = CuttingInstructionTheme.ChamferMgmtBtnDefaultBorder
            textColor = CuttingInstructionTheme.ChamferMgmtBtnDefaultText
            shadowColor = Color(0x18000000)
        }
        ChamferingHeaderActionStyle.PlanPrint -> {
            backgroundBrush = Brush.horizontalGradient(
                listOf(CuttingInstructionTheme.ChamferHeaderBtnPlanStart, CuttingInstructionTheme.ChamferHeaderBtnPlanEnd),
            )
            borderColor = CuttingInstructionTheme.ChamferHeaderBtnPlanEnd
            textColor = Color.White
            shadowColor = Color(0x40E49604)
        }
        ChamferingHeaderActionStyle.IssueSheet -> {
            backgroundBrush = Brush.horizontalGradient(
                listOf(CuttingInstructionTheme.ChamferMgmtBtnPrimaryBg, Color(0xFF2563EB)),
            )
            borderColor = CuttingInstructionTheme.ChamferMgmtBtnPrimaryBorder
            textColor = Color.White
            shadowColor = Color(0x40409EFF)
        }
        ChamferingHeaderActionStyle.ConfirmActual -> {
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFF85CE61), CuttingInstructionTheme.ChamferMgmtBtnSuccessBg),
            )
            borderColor = CuttingInstructionTheme.ChamferMgmtBtnSuccessBorder
            textColor = Color.White
            shadowColor = Color(0x4067C23A)
        }
    }
    Box(
        modifier = modifier
            .height(btnHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, shape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .background(backgroundBrush)
            .drawBehind {
                if (style == ChamferingHeaderActionStyle.New) {
                    drawLine(
                        color = Color.White.copy(alpha = if (pressed) 0.35f else 0.75f),
                        start = Offset(8.dp.toPx(), 1.dp.toPx()),
                        end = Offset(size.width - 8.dp.toPx(), 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .border(1.dp, borderColor.copy(alpha = if (pressed) 0.75f else 1f), shape)
            .clickable(interactionSource = interactionSource, indication = null, enabled = !loading, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (style != ChamferingHeaderActionStyle.New) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = if (pressed) 0.1f else 0.22f), Color.Transparent),
                            startY = 0f,
                            endY = btnHeight.value,
                        ),
                    ),
            )
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = textColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = if (style == ChamferingHeaderActionStyle.ConfirmActual) FontWeight.Medium else FontWeight.Normal,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

enum class MachineChipStyle {
    Default,
    Chamfering,
}

@Composable
fun InstructionMachineChips(
    machines: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    includeAll: Boolean = false,
    allLabel: String = "全部",
    modifier: Modifier = Modifier,
    chipStyle: MachineChipStyle = MachineChipStyle.Default,
    compact: Boolean = false,
) {
    val scroll = rememberScrollState()
    val horizontalPadding = if (compact) 6.dp else 8.dp
    val chipHeight = if (compact) 22.dp else 24.dp
    val fontSize = if (compact) 9.sp else 11.sp
    val chipSpacing = if (compact) 5.dp else 6.dp
    Row(modifier = modifier.horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(chipSpacing)) {
        if (includeAll) {
            MachineTabChip(
                name = allLabel,
                active = selected.isBlank(),
                onClick = { onSelect("") },
                style = chipStyle,
                horizontalPadding = horizontalPadding,
                height = chipHeight,
                fontSize = fontSize,
            )
        }
        machines.forEach { (name, _) ->
            MachineTabChip(
                name = name,
                active = selected == name,
                onClick = { onSelect(name) },
                style = chipStyle,
                horizontalPadding = horizontalPadding,
                height = chipHeight,
                fontSize = fontSize,
            )
        }
    }
}

@Composable
private fun MachineTabChip(
    name: String,
    active: Boolean,
    onClick: () -> Unit,
    style: MachineChipStyle = MachineChipStyle.Default,
    horizontalPadding: Dp = 8.dp,
    height: Dp = 24.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
) {
    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val backgroundBrush: Brush?
    val solidBackground: Color
    val borderColor: Color
    val contentColor: Color
    when (style) {
        MachineChipStyle.Default -> if (active) {
            backgroundBrush = null
            solidBackground = CuttingInstructionTheme.ChamferMgmtBtnPrimaryBg
            borderColor = CuttingInstructionTheme.ChamferMgmtBtnPrimaryBorder
            contentColor = Color.White
        } else {
            backgroundBrush = null
            solidBackground = Color(0xFFF5F7FA)
            borderColor = CuttingInstructionTheme.ChipInactiveBorder
            contentColor = CuttingInstructionTheme.Subtitle
        }
        MachineChipStyle.Chamfering -> if (active) {
            backgroundBrush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
            solidBackground = Color.Transparent
            borderColor = Color(0xFF059669)
            contentColor = Color.White
        } else {
            backgroundBrush = null
            solidBackground = Color.White
            borderColor = Color(0xFFD1FAE5)
            contentColor = Color(0xFF047857)
        }
    }
    Box(
        modifier = Modifier
            .height(height)
            .graphicsLayer { alpha = if (pressed) 0.88f else 1f }
            .clip(shape)
            .then(
                if (backgroundBrush != null) Modifier.background(backgroundBrush, shape)
                else Modifier.background(solidBackground, shape),
            )
            .border(1.dp, borderColor, shape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name,
            fontSize = fontSize,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CuttingMgmtHeaderButton(
    text: String,
    bg: Color,
    onClick: () -> Unit,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = Color.White),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CuttingInstructionTodayCard(
    date: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    machineOptions: List<Pair<String, String>>,
    selectedMachine: String,
    onMachineSelect: (String) -> Unit,
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    onToggleCompleted: (InstructionCuttingRowDto, Boolean) -> Unit,
    onDuplicate: (InstructionCuttingRowDto) -> Unit,
    onDelete: (InstructionCuttingRowDto) -> Unit,
    onEdit: (InstructionCuttingRowDto) -> Unit,
    onSplit: (InstructionCuttingRowDto) -> Unit,
    onPrintPlan: () -> Unit,
    onPrintSheet: () -> Unit,
    onConfirmActual: () -> Unit,
    confirmActualLoading: Boolean = false,
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit,
    machineFilter: String = "",
    onCommitReorder: (List<Int>, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = CuttingInstructionTheme.CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.85f)),
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "切断指示-今日",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CuttingInstructionTheme.CuttingTitle,
                    )
                    InstructionDateNav(date, onPrevDate, onNextDate)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CuttingMgmtHeaderButton("計画印刷", CuttingInstructionTheme.CuttingBtnPlanSolid, onPrintPlan)
                    CuttingMgmtHeaderButton("指示書発行", CuttingInstructionTheme.CuttingBtnIssueSolid, onPrintSheet)
                    CuttingMgmtHeaderButton(
                        "実績確定",
                        CuttingInstructionTheme.CuttingBtnConfirmSolid,
                        onConfirmActual,
                        loading = confirmActualLoading,
                    )
                }
            }
            InstructionMachineChips(
                machines = machineOptions,
                selected = selectedMachine,
                onSelect = onMachineSelect,
                modifier = Modifier.padding(top = CuttingMgmtMachineChipsTopPadding),
            )
            Spacer(Modifier.height(CuttingMgmtTableSpacerAfterChips))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                CuttingManagementTable(
                    rows = rows,
                    compact = false,
                    loading = loading,
                    onToggleCompleted = onToggleCompleted,
                    onDuplicate = onDuplicate,
                    onDelete = onDelete,
                    onEdit = onEdit,
                    onSplit = onSplit,
                    onMoveBackToBatch = onMoveBackToBatch,
                    machineFilter = machineFilter,
                    onCommitReorder = onCommitReorder,
                    reorderEnabled = !loading,
                    modifier = Modifier.fillMaxSize(),
                    expandVertically = true,
                )
            }
            CuttingMgmtSummaryFooter(rows)
        }
    }
}

@Composable
fun CuttingInstructionTomorrowCard(
    date: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = CuttingInstructionTheme.CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.85f)),
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "切断指示-翌日",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CuttingInstructionTheme.CuttingTitle,
                    )
                    InstructionDateNav(date, onPrevDate, onNextDate)
                }
                Spacer(Modifier.height(28.dp))
            }
            Spacer(Modifier.height(CuttingMgmtTableTopSpacerHeight))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                CuttingManagementTable(
                    rows = rows,
                    compact = true,
                    loading = loading,
                    onToggleCompleted = { _, _ -> },
                    onDuplicate = {},
                    onDelete = {},
                    onMoveBackToBatch = onMoveBackToBatch,
                    modifier = Modifier.fillMaxSize(),
                    expandVertically = true,
                )
            }
            CuttingMgmtSummaryFooter(rows, showDefect = false, showUsage = false)
        }
    }
}

@Composable
fun CuttingMgmtSummaryFooter(
    rows: List<InstructionCuttingRowDto>,
    showDefect: Boolean = true,
    showUsage: Boolean = true,
) {
    if (rows.isEmpty()) return
    val qty = rows.sumOf { it.actualProductionQuantity ?: 0 }
    val defect = rows.sumOf { it.defectQty ?: 0 }
    val timeTotal = rows.sumOf { row -> row.productionTime?.toDoubleOrNull() ?: 0.0 }
    val timeText = if (timeTotal == 0.0) "-" else {
        val rounded = kotlin.math.round(timeTotal * 10) / 10.0
        if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
    val usage = rows.sumOf { (it.usageCount ?: 1.0).toInt() }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(CuttingInstructionTheme.CuttingTableFooterBg)
            .border(1.dp, CuttingInstructionTheme.CuttingTableBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CuttingMgmtFooterItem("生産数合計", qty.toString())
        if (showDefect) {
            CuttingMgmtFooterItem("不良合計", defect.toString())
        }
        CuttingMgmtFooterItem("生産時間合計", timeText)
        if (showUsage) {
            CuttingMgmtFooterItem("使用材料数", "$usage 束")
        }
    }
}

@Composable
private fun CuttingMgmtFooterItem(label: String, value: String) {
    Text(
        "$label：$value",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = CuttingInstructionTheme.CuttingTableHeaderText,
    )
}

private enum class CuttingCellAlign { Start, Center, End }

private val CuttingTodayRowHeight = CuttingInstructionTheme.CuttingMgmtRowHeightDp.dp
private val CuttingTodayMinWidth = 863.dp
private val CuttingTomorrowMinWidth = 420.dp

private fun cuttingProductNameDisplay(row: InstructionCuttingRowDto): String =
    row.productName?.trim()?.takeIf { it.isNotEmpty() }
        ?: row.productCd?.trim()?.takeIf { it.isNotEmpty() }
        ?: "-"
private val CuttingMgmtMachineChipsTopPadding = 8.dp
private val CuttingMgmtMachineChipsHeight = 24.dp
private val CuttingMgmtTableSpacerAfterChips = 8.dp
private val CuttingMgmtTableTopSpacerHeight =
    CuttingMgmtMachineChipsTopPadding + CuttingMgmtMachineChipsHeight + CuttingMgmtTableSpacerAfterChips
private val CuttingMgmtBodyMinHeight = CuttingTodayRowHeight * CuttingInstructionTheme.CuttingTodayVisibleRows
private val CuttingMgmtBodyMaxHeight = (CuttingInstructionTheme.CuttingTableMaxHeightDp - CuttingInstructionTheme.CuttingMgmtRowHeightDp).dp
private val CuttingTodayBodyMinHeight = CuttingMgmtBodyMinHeight
private val CuttingTodayBodyMaxHeight = CuttingMgmtBodyMaxHeight
private val ChamferTodayBodyHeight = CuttingTodayRowHeight * CuttingInstructionTheme.ChamferTodayVisibleRows

private fun Modifier.cuttingMgmtTableBodyHeight(expandVertically: Boolean): Modifier =
    if (expandVertically) {
        fillMaxHeight().heightIn(min = CuttingTodayBodyMinHeight)
    } else {
        heightIn(min = CuttingTodayBodyMinHeight, max = CuttingTodayBodyMaxHeight)
    }

private fun Modifier.chamferTodayTableBodyHeight(): Modifier = height(ChamferTodayBodyHeight)

private fun Modifier.cuttingMgmtTablePlaceholderHeight(expandVertically: Boolean): Modifier =
    if (expandVertically) {
        fillMaxWidth().fillMaxHeight()
    } else {
        fillMaxWidth().height(CuttingTodayBodyMinHeight)
    }

private fun Modifier.chamferTodayTablePlaceholderHeight(): Modifier =
    fillMaxWidth().height(ChamferTodayBodyHeight)

@Composable
private fun RowScope.CuttingTodayCellBorder(
    width: Dp? = null,
    weight: Float = 0f,
    minWidth: Dp = 0.dp,
    align: CuttingCellAlign = CuttingCellAlign.Center,
    showRightBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val mod = when {
        weight > 0f -> Modifier.weight(weight).defaultMinSize(minWidth = minWidth)
        width != null -> Modifier.width(width)
        else -> Modifier
    }
    Box(
        modifier = mod
            .height(CuttingTodayRowHeight)
            .then(
                if (showRightBorder) {
                    Modifier.border(
                        width = 0.dp,
                        color = Color.Transparent,
                    )
                } else Modifier,
            )
            .padding(horizontal = 4.dp),
        contentAlignment = when (align) {
            CuttingCellAlign.Start -> Alignment.CenterStart
            CuttingCellAlign.Center -> Alignment.Center
            CuttingCellAlign.End -> Alignment.CenterEnd
        },
        content = content,
    )
}

@Composable
private fun RowScope.CuttingTodayCellText(
    text: String,
    width: Dp? = null,
    weight: Float = 0f,
    minWidth: Dp = 0.dp,
    align: CuttingCellAlign = CuttingCellAlign.Center,
    fontWeight: FontWeight = FontWeight.Normal,
    showRightBorder: Boolean = true,
    color: Color = CuttingInstructionTheme.Title,
) {
    CuttingTodayCellBorder(width, weight, minWidth, align, showRightBorder) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = when (align) {
                CuttingCellAlign.Start -> TextAlign.Start
                CuttingCellAlign.Center -> TextAlign.Center
                CuttingCellAlign.End -> TextAlign.End
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RowScope.CuttingManagementTableHeaderRow() {
    listOf("CD", "ライン", "生産日", "切断機").forEach { h ->
        CuttingTodayCellText(
            h,
            width = cuttingTodayColWidth(h),
            fontWeight = FontWeight.ExtraBold,
            align = CuttingCellAlign.Center,
            color = CuttingInstructionTheme.CuttingTableHeaderText,
        )
    }
    CuttingTodayCellText("製品名", width = cuttingTodayColWidth("製品名"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Start, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("原材料", width = 100.dp, fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Start, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("生産数", width = 50.dp, fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.End, color = CuttingInstructionTheme.CuttingTableHeaderText)
    listOf("不良", "完了", "生産順", "生産時間", "備考").forEach { h ->
        CuttingTodayCellText(
            h,
            width = cuttingTodayColWidth(h),
            fontWeight = FontWeight.ExtraBold,
            align = CuttingCellAlign.Center,
            color = CuttingInstructionTheme.CuttingTableHeaderText,
        )
    }
    CuttingTodayCellText("操作", width = 90.dp, fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("", width = cuttingTodayColWidth("戻す"), fontWeight = FontWeight.ExtraBold, showRightBorder = false, color = CuttingInstructionTheme.CuttingTableHeaderText)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CuttingTodayTable(
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    onToggleCompleted: (InstructionCuttingRowDto, Boolean) -> Unit,
    onDuplicate: (InstructionCuttingRowDto) -> Unit,
    onDelete: (InstructionCuttingRowDto) -> Unit,
    onEdit: (InstructionCuttingRowDto) -> Unit,
    onSplit: (InstructionCuttingRowDto) -> Unit,
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit,
    machineFilter: String = "",
    onCommitReorder: (List<Int>, String) -> Unit = { _, _ -> },
    reorderEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    expandVertically: Boolean = false,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val density = LocalDensity.current
    val rowHeightPx = with(density) { CuttingTodayRowHeight.roundToPx() }
    val dividerPx = with(density) { 1.dp.toPx() }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }
    val displayRows = remember { mutableStateListOf<InstructionCuttingRowDto>() }
    var draggingRowId by remember { mutableIntStateOf(-1) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(rows) {
        if (draggingRowId < 0) {
            displayRows.clear()
            displayRows.addAll(rows)
        }
    }

    val tableShape = RoundedCornerShape(6.dp)
    val tableModifier = modifier
        .fillMaxWidth()
        .then(if (expandVertically) Modifier.fillMaxHeight() else Modifier)
        .clip(tableShape)
        .border(1.dp, CuttingInstructionTheme.CuttingTableBorder, tableShape)
    Column(modifier = tableModifier) {
        Column(
            Modifier
                .then(if (expandVertically) Modifier.weight(1f).fillMaxWidth() else Modifier)
                .horizontalScroll(horizontalScroll),
        ) {
            Column(Modifier.width(CuttingTodayMinWidth)) {
                Row(
                    modifier = Modifier
                        .width(CuttingTodayMinWidth)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CuttingInstructionTheme.CuttingTableHeaderStart,
                                    CuttingInstructionTheme.CuttingTableHeaderEnd,
                                ),
                            ),
                        )
                        .border(
                            width = 0.dp,
                            color = Color.Transparent,
                            shape = tableShape,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CuttingManagementTableHeaderRow()
                }
                HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .cuttingMgmtTableBodyHeight(expandVertically)
                        .verticalScroll(verticalScroll, enabled = !isDragging),
                ) {
                    when {
                        loading -> Box(
                            Modifier.cuttingMgmtTablePlaceholderHeight(expandVertically),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.CuttingAccent,
                            )
                        }
                        displayRows.isEmpty() -> Box(
                            Modifier.cuttingMgmtTablePlaceholderHeight(expandVertically),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> displayRows.forEachIndexed { index, row ->
                            val rowId = row.id ?: 0
                            key(rowId) {
                                val isRowDragging = rowId > 0 && rowId == draggingRowId
                                val rowMachine = row.cuttingMachine.orEmpty().trim()
                                val sameMachineCount = displayRows.count {
                                    it.cuttingMachine.orEmpty().trim() == rowMachine && rowMachine.isNotBlank()
                                }
                                val canDrag = reorderEnabled && rowId > 0 && sameMachineCount > 1 && rowMachine.isNotBlank()
                                val dragModifier = if (canDrag) {
                                    Modifier.pointerInput(rowId, reorderEnabled, displayRows.size) {
                                        detectMgmtTableLongPressDragGestures(
                                            longPressTimeoutMillis = MgmtTableReorderLongPressMs,
                                            onDragStart = {
                                                draggingRowId = rowId
                                                dragStartIndex = displayRows.indexOfFirst { it.id == rowId }
                                                dragOffsetY = 0f
                                                isDragging = true
                                            },
                                            onDrag = { delta -> dragOffsetY += delta.y },
                                            onDragEnd = {
                                                if (draggingRowId >= 0 && dragStartIndex >= 0) {
                                                    applyCuttingRowReorderFromOffset(
                                                        rows = displayRows,
                                                        startIndex = dragStartIndex,
                                                        totalOffsetY = dragOffsetY,
                                                        rowHeights = rowHeights,
                                                        rowHeightPx = rowHeightPx,
                                                        dividerPx = dividerPx,
                                                    )
                                                    val draggedRow = displayRows.find { it.id == draggingRowId }
                                                    if (draggedRow != null) {
                                                        val cm = machineFilter.trim().ifBlank {
                                                            draggedRow.cuttingMachine.orEmpty().trim()
                                                        }
                                                        val sameMachine = displayRows.filter {
                                                            it.cuttingMachine.orEmpty().trim() == cm
                                                        }
                                                        onCommitReorder(sameMachine.mapNotNull { it.id }, cm)
                                                    }
                                                }
                                                draggingRowId = -1
                                                dragStartIndex = -1
                                                dragOffsetY = 0f
                                                isDragging = false
                                            },
                                            onDragCancel = {
                                                draggingRowId = -1
                                                dragStartIndex = -1
                                                dragOffsetY = 0f
                                                isDragging = false
                                                displayRows.clear()
                                                displayRows.addAll(rows)
                                            },
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                                Row(
                                    modifier = Modifier
                                        .width(CuttingTodayMinWidth)
                                        .then(dragModifier)
                                        .combinedClickable(onClick = {}, onDoubleClick = { onEdit(row) })
                                        .zIndex(if (isRowDragging) 1f else 0f)
                                        .offset { IntOffset(0, if (isRowDragging) dragOffsetY.roundToInt() else 0) }
                                        .alpha(if (isRowDragging) 0.88f else 1f)
                                        .shadow(if (isRowDragging) 6.dp else 0.dp, RoundedCornerShape(4.dp))
                                        .onSizeChanged { if (rowId > 0) rowHeights[rowId] = it.height }
                                        .background(if (index % 2 == 1) Color(0xFFFAFAFF) else Color.White),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                            CuttingTodayCellText(row.cd ?: row.managementCode ?: "-", width = cuttingTodayColWidth("CD"))
                            CuttingTodayCellText(row.productionLine ?: "-", width = cuttingTodayColWidth("ライン"))
                            CuttingTodayCellText(formatInstructionDate(row.productionDay), width = cuttingTodayColWidth("生産日"))
                            CuttingTodayCellText(row.cuttingMachine ?: "-", width = cuttingTodayColWidth("切断機"))
                            CuttingTodayCellText(
                                cuttingProductNameDisplay(row),
                                width = cuttingTodayColWidth("製品名"),
                                align = CuttingCellAlign.Start,
                            )
                            CuttingTodayCellText(row.materialName ?: "-", width = 100.dp, align = CuttingCellAlign.Start)
                            CuttingTodayCellText(row.actualProductionQuantity?.toString() ?: "-", width = 50.dp, align = CuttingCellAlign.End)
                            CuttingTodayCellText(row.defectQty?.toString() ?: "-", width = cuttingTodayColWidth("不良"))
                            CuttingTodayCellBorder(width = cuttingTodayColWidth("完了")) {
                                Switch(
                                    checked = row.productionCompletedCheck == 1,
                                    onCheckedChange = { onToggleCompleted(row, it) },
                                    modifier = Modifier.scale(CuttingInstructionTheme.CuttingCompletedSwitchScale),
                                )
                            }
                            CuttingTodayCellText(row.productionSequence?.toString() ?: "-", width = cuttingTodayColWidth("生産順"))
                            CuttingTodayCellText(row.productionTime ?: "-", width = cuttingTodayColWidth("生産時間"))
                            CuttingTodayCellText(row.remarks ?: "-", width = cuttingTodayColWidth("備考"))
                            CuttingTodayCellBorder(width = 90.dp) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if ((row.actualProductionQuantity ?: 0) > 0) {
                                        SplitToNextDayLinkButton(onClick = { onSplit(row) })
                                    }
                                    IconButton(onClick = { onDuplicate(row) }, modifier = Modifier.size(22.dp)) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "複製",
                                            tint = CuttingInstructionTheme.CuttingBtnIssueSolid,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(22.dp)) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "削除",
                                            tint = Color(0xFFF56C6C),
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                            CuttingTodayCellBorder(width = cuttingTodayColWidth("戻す"), showRightBorder = false) {
                                TextButton(onClick = { onMoveBackToBatch(row) }, contentPadding = PaddingValues(0.dp)) {
                                    Text("戻す", fontSize = 9.sp, color = CuttingInstructionTheme.BatchAccent)
                                }
                            }
                        }
                        if (index < displayRows.lastIndex) {
                            HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder.copy(alpha = 0.45f), thickness = 1.dp)
                        }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun cuttingTodayColWidth(h: String) = when (h) {
    "CD" -> 42.dp
    "ライン" -> 46.dp
    "生産日" -> 75.dp
    "切断機" -> 46.dp
    "製品名" -> 110.dp
    "不良" -> 44.dp
    "完了" -> 45.dp
    "生産順" -> 46.dp
    "生産時間" -> 50.dp
    "備考" -> 75.dp
    "戻す" -> 44.dp
    else -> 46.dp
}

@Composable
private fun RowScope.CuttingTomorrowTableHeaderRow() {
    listOf("CD", "生産日", "切断機").forEach { h ->
        CuttingTodayCellText(
            h,
            width = cuttingTomorrowColWidth(h),
            fontWeight = FontWeight.ExtraBold,
            align = CuttingCellAlign.Center,
            color = CuttingInstructionTheme.CuttingTableHeaderText,
        )
    }
    CuttingTodayCellText("製品名", width = cuttingTomorrowColWidth("製品名"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Start, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("生産数", width = cuttingTomorrowColWidth("生産数"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.End, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("生産順", width = cuttingTomorrowColWidth("生産順"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Center, color = CuttingInstructionTheme.CuttingTableHeaderText)
    CuttingTodayCellText("", width = cuttingTomorrowColWidth("戻す"), fontWeight = FontWeight.ExtraBold, showRightBorder = false, color = CuttingInstructionTheme.CuttingTableHeaderText)
}

private fun cuttingTomorrowColWidth(h: String) = when (h) {
    "CD" -> 42.dp
    "生産日" -> 75.dp
    "切断機" -> 46.dp
    "製品名" -> 100.dp
    "生産数" -> 50.dp
    "生産順" -> 46.dp
    "戻す" -> 44.dp
    else -> 46.dp
}

@Composable
private fun CuttingTomorrowTable(
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit,
    modifier: Modifier = Modifier,
    expandVertically: Boolean = false,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    val tableModifier = modifier
        .fillMaxWidth()
        .then(if (expandVertically) Modifier.fillMaxHeight() else Modifier)
        .clip(tableShape)
        .border(1.dp, CuttingInstructionTheme.CuttingTableBorder, tableShape)
    Column(modifier = tableModifier) {
        Column(
            Modifier
                .then(if (expandVertically) Modifier.weight(1f).fillMaxWidth() else Modifier)
                .horizontalScroll(horizontalScroll),
        ) {
            Column(Modifier.width(CuttingTomorrowMinWidth)) {
                Row(
                    modifier = Modifier
                        .width(CuttingTomorrowMinWidth)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CuttingInstructionTheme.CuttingTableHeaderStart,
                                    CuttingInstructionTheme.CuttingTableHeaderEnd,
                                ),
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CuttingTomorrowTableHeaderRow()
                }
                HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .cuttingMgmtTableBodyHeight(expandVertically)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier.cuttingMgmtTablePlaceholderHeight(expandVertically),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.CuttingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier.cuttingMgmtTablePlaceholderHeight(expandVertically),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> rows.forEachIndexed { index, row ->
                            Row(
                                modifier = Modifier
                                    .width(CuttingTomorrowMinWidth)
                                    .background(if (index % 2 == 1) Color(0xFFFAFAFF) else Color.White),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CuttingTodayCellText(row.cd ?: row.managementCode ?: "-", width = cuttingTomorrowColWidth("CD"))
                                CuttingTodayCellText(formatInstructionDate(row.productionDay), width = cuttingTomorrowColWidth("生産日"))
                                CuttingTodayCellText(row.cuttingMachine ?: "-", width = cuttingTomorrowColWidth("切断機"))
                                CuttingTodayCellText(
                                    cuttingProductNameDisplay(row),
                                    width = cuttingTomorrowColWidth("製品名"),
                                    align = CuttingCellAlign.Start,
                                )
                                CuttingTodayCellText(row.actualProductionQuantity?.toString() ?: "-", width = cuttingTomorrowColWidth("生産数"), align = CuttingCellAlign.End)
                                CuttingTodayCellText(row.productionSequence?.toString() ?: "-", width = cuttingTomorrowColWidth("生産順"))
                                CuttingTodayCellBorder(width = cuttingTomorrowColWidth("戻す"), showRightBorder = false) {
                                    TextButton(onClick = { onMoveBackToBatch(row) }, contentPadding = PaddingValues(0.dp)) {
                                        Text("戻す", fontSize = 9.sp, color = CuttingInstructionTheme.BatchAccent)
                                    }
                                }
                            }
                            if (index < rows.lastIndex) {
                                HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder.copy(alpha = 0.45f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanBatchTable(
    rows: List<InstructionPlanRowDto>,
    selectedId: Int?,
    loading: Boolean,
    onSelect: (InstructionPlanRowDto) -> Unit,
    onToggleStock: (InstructionPlanRowDto, Boolean) -> Unit,
    onDelete: (InstructionPlanRowDto) -> Unit,
    onMoveToCutting: (InstructionPlanRowDto) -> Unit,
    onEdit: (InstructionPlanRowDto) -> Unit = {},
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val columnWidths = rememberPlanBatchColumnWidths(rows)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CuttingInstructionTheme.TableBorder, RoundedCornerShape(8.dp)),
    ) {
        when {
            loading -> Box(
                Modifier.fillMaxWidth().height(PlanBatchBodyHeight + PlanBatchRowHeight),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = CuttingInstructionTheme.BatchAccent)
            }
            rows.isEmpty() -> Box(
                Modifier.fillMaxWidth().height(PlanBatchBodyHeight + PlanBatchRowHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
            }
            else -> Column(Modifier.horizontalScroll(horizontalScroll)) {
                Row(
                    modifier = Modifier
                        .background(CuttingInstructionTheme.BatchTableHeaderBg)
                        .height(PlanBatchRowHeight)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlanBatchHeaders.forEach { h ->
                        Text(
                            h,
                            modifier = Modifier.width(columnWidths.getValue(h)).padding(horizontal = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CuttingInstructionTheme.BatchTitle,
                            textAlign = if (h in PlanBatchStartAlignHeaders) TextAlign.Start else TextAlign.Center,
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }
                HorizontalDivider(color = CuttingInstructionTheme.TableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .height(PlanBatchBodyHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    rows.forEachIndexed { index, row ->
                        val selected = row.id == selectedId
                        Row(
                            modifier = Modifier
                                .height(PlanBatchRowHeight)
                                .planBatchRowGestures(
                                    onClick = { onSelect(row) },
                                    onLongClick = { onEdit(row) },
                                )
                                .background(
                                    when {
                                        selected -> CuttingInstructionTheme.RowSelected
                                        else -> CuttingInstructionTheme.planPriorityBg(row.priorityOrder)
                                    },
                                )
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PlanBatchHeaders.forEach { header ->
                                when (header) {
                                    "→切断" -> Box(
                                        modifier = Modifier.width(columnWidths.getValue(header)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        TextButton(onClick = { onMoveToCutting(row) }, contentPadding = PaddingValues(0.dp)) {
                                            Text("→切断", fontSize = 9.sp, color = CuttingInstructionTheme.BatchAccent)
                                        }
                                    }
                                    "材料区分" -> Switch(
                                        checked = row.useMaterialStockSub == 1,
                                        onCheckedChange = { onToggleStock(row, it) },
                                        modifier = Modifier
                                            .width(columnWidths.getValue(header))
                                            .padding(horizontal = 4.dp)
                                            .scale(CuttingInstructionTheme.PlanBatchMaterialSwitchScale),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = CuttingInstructionTheme.PlanBatchMaterialSwitchTrackChecked,
                                            checkedBorderColor = Color.Transparent,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = CuttingInstructionTheme.PlanBatchMaterialSwitchTrackUnchecked,
                                            uncheckedBorderColor = Color.Transparent,
                                            disabledCheckedBorderColor = Color.Transparent,
                                            disabledUncheckedBorderColor = Color.Transparent,
                                        ),
                                    )
                                    "操作" -> Row(
                                        Modifier.width(columnWidths.getValue(header)),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        IconButton(onClick = { onEdit(row) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, "編集", tint = CuttingInstructionTheme.BatchAccent, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, "削除", tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    else -> PlanBatchCellText(
                                        text = planBatchRowCellText(header, row),
                                        width = columnWidths.getValue(header),
                                        align = if (header in PlanBatchStartAlignHeaders) TextAlign.Start else TextAlign.Center,
                                    )
                                }
                            }
                        }
                        if (index < rows.lastIndex) {
                            HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CuttingManagementTable(
    rows: List<InstructionCuttingRowDto>,
    compact: Boolean,
    loading: Boolean,
    onToggleCompleted: (InstructionCuttingRowDto, Boolean) -> Unit,
    onDuplicate: (InstructionCuttingRowDto) -> Unit,
    onDelete: (InstructionCuttingRowDto) -> Unit,
    onEdit: (InstructionCuttingRowDto) -> Unit = {},
    onSplit: (InstructionCuttingRowDto) -> Unit = {},
    onMoveUp: (InstructionCuttingRowDto) -> Unit = {},
    onMoveDown: (InstructionCuttingRowDto) -> Unit = {},
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit = {},
    machineFilter: String = "",
    onCommitReorder: (List<Int>, String) -> Unit = { _, _ -> },
    reorderEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    expandVertically: Boolean = false,
) {
    if (!compact) {
        CuttingTodayTable(
            rows = rows,
            loading = loading,
            onToggleCompleted = onToggleCompleted,
            onDuplicate = onDuplicate,
            onDelete = onDelete,
            onEdit = onEdit,
            onSplit = onSplit,
            onMoveBackToBatch = onMoveBackToBatch,
            machineFilter = machineFilter,
            onCommitReorder = onCommitReorder,
            reorderEnabled = reorderEnabled,
            modifier = modifier,
            expandVertically = expandVertically,
        )
        return
    }
    CuttingTomorrowTable(
        rows = rows,
        loading = loading,
        onMoveBackToBatch = onMoveBackToBatch,
        modifier = modifier,
        expandVertically = expandVertically,
    )
}

private val ChamferMgmtMinWidth = CuttingInstructionTheme.ChamferMgmtMinWidthDp.dp
private val ChamferMgmtTomorrowMinWidth = 400.dp
private val ChamferMgmtRowAltBg = Color(0x59ECFDF5)
private val ChamferMgmtCompactChipHeight = 22.dp
private val ChamferMgmtChipRowTopPadding = 8.dp
private val ChamferMgmtHeaderButtonHeight = CuttingInstructionTheme.ChamferMgmtHeaderBtnHeightDp.dp

@Composable
fun ChamferMgmtHeaderPlaceholderSubRow() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .padding(top = ChamferMgmtChipRowTopPadding)
            .height(ChamferMgmtCompactChipHeight),
    )
}

@Composable
fun ChamferMgmtHeaderPlaceholderActions() {
    Spacer(Modifier.height(ChamferMgmtHeaderButtonHeight))
}

private fun chamferTodayColWidth(h: String) = when (h) {
    "CD" -> 44.dp
    "ライン" -> 48.dp
    "成型予定日" -> 72.dp
    "生産日" -> 72.dp
    "面取機" -> 52.dp
    "製品名" -> 110.dp
    "生産数" -> 52.dp
    "不良" -> 44.dp
    "完了" -> 42.dp
    "カ無" -> 42.dp
    "生産順" -> 44.dp
    "生産時間" -> 56.dp
    "操作" -> 110.dp
    else -> 46.dp
}

private fun chamferTomorrowColWidth(h: String) = when (h) {
    "CD" -> 45.dp
    "生産日" -> 75.dp
    "面取機" -> 55.dp
    "製品名" -> 100.dp
    "生産数" -> 55.dp
    "不良" -> 44.dp
    "生産順" -> 45.dp
    "生産時間" -> 60.dp
    else -> 46.dp
}

private fun chamferProductNameDisplay(row: InstructionChamferingRowDto): String =
    row.productName?.trim()?.takeIf { it.isNotEmpty() }
        ?: row.productCd?.trim()?.takeIf { it.isNotEmpty() }
        ?: "-"

private fun chamferMgmtRowBackground(index: Int): Color =
    if (index % 2 == 1) ChamferMgmtRowAltBg else Color.White

@Composable
private fun RowScope.ChamferMgmtCellText(
    text: String,
    width: Dp? = null,
    weight: Float = 0f,
    minWidth: Dp = 0.dp,
    align: CuttingCellAlign = CuttingCellAlign.Center,
    fontWeight: FontWeight = FontWeight.Normal,
    showRightBorder: Boolean = true,
    color: Color = CuttingInstructionTheme.Title,
    backgroundColor: Color = Color.Unspecified,
) {
    CuttingTodayCellBorder(width, weight, minWidth, align, showRightBorder) {
        Box(
            modifier = if (backgroundColor != Color.Unspecified) {
                Modifier.fillMaxWidth().background(backgroundColor)
            } else {
                Modifier
            },
            contentAlignment = when (align) {
                CuttingCellAlign.Start -> Alignment.CenterStart
                CuttingCellAlign.Center -> Alignment.Center
                CuttingCellAlign.End -> Alignment.CenterEnd
            },
        ) {
            Text(
                text,
                fontSize = 10.sp,
                fontWeight = fontWeight,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = when (align) {
                    CuttingCellAlign.Start -> TextAlign.Start
                    CuttingCellAlign.Center -> TextAlign.Center
                    CuttingCellAlign.End -> TextAlign.End
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ChamferMgmtSummaryFooter(
    rows: List<InstructionChamferingRowDto>,
    showDefectTotal: Boolean = true,
) {
    if (rows.isEmpty()) return
    val qty = rows.sumOf { it.actualProductionQuantity ?: 0 }
    val defect = rows.sumOf { it.defectQty ?: 0 }
    val timeTotal = rows.sumOf { row -> row.productionTime?.toDoubleOrNull() ?: 0.0 }
    val timeText = if (timeTotal == 0.0) {
        "-"
    } else {
        val rounded = kotlin.math.round(timeTotal * 10) / 10.0
        if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        CuttingInstructionTheme.ChamferMgmtTableFooterStart,
                        CuttingInstructionTheme.ChamferMgmtTableFooterEnd,
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ChamferMgmtFooterItem("生産数合計", qty.toString())
        if (showDefectTotal) {
            ChamferMgmtFooterItem("不良合計", defect.toString())
        }
        ChamferMgmtFooterItem("生産時間合計", timeText)
    }
}

@Composable
private fun ChamferMgmtFooterItem(label: String, value: String) {
    Text(
        "$label：$value",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CuttingInstructionTheme.ChamferMgmtTableFooterText,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChamferingTodayTable(
    rows: List<InstructionChamferingRowDto>,
    loading: Boolean,
    formingStartDateByMgmtCode: Map<String, String>,
    onToggleCompleted: (InstructionChamferingRowDto, Boolean) -> Unit,
    onToggleNoCount: (InstructionChamferingRowDto, Boolean) -> Unit,
    onEdit: (InstructionChamferingRowDto) -> Unit,
    onDuplicate: (InstructionChamferingRowDto) -> Unit,
    onDelete: (InstructionChamferingRowDto) -> Unit,
    onSplit: (InstructionChamferingRowDto) -> Unit,
    onCommitReorder: (List<Int>, String) -> Unit = { _, _ -> },
    reorderEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val density = LocalDensity.current
    val rowHeightPx = with(density) { CuttingTodayRowHeight.roundToPx() }
    val dividerPx = with(density) { 1.dp.toPx() }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }
    val displayRows = remember { mutableStateListOf<InstructionChamferingRowDto>() }
    var draggingRowId by remember { mutableIntStateOf(-1) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(rows) {
        if (draggingRowId < 0) {
            displayRows.clear()
            displayRows.addAll(rows)
        }
    }

    val tableShape = RoundedCornerShape(6.dp)
    val headerText = CuttingInstructionTheme.ChamferMgmtTableHeaderText
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(tableShape)
            .border(1.dp, CuttingInstructionTheme.ChamferMgmtTableBorder, tableShape),
    ) {
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Column(Modifier.widthIn(min = ChamferMgmtMinWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CuttingInstructionTheme.ChamferMgmtTableHeaderStart,
                                    CuttingInstructionTheme.ChamferMgmtTableHeaderEnd,
                                ),
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("CD", "ライン").forEach { h ->
                        ChamferMgmtCellText(h, width = chamferTodayColWidth(h), fontWeight = FontWeight.ExtraBold, color = headerText)
                    }
                    ChamferMgmtCellText(
                        "成型予定日",
                        width = chamferTodayColWidth("成型予定日"),
                        fontWeight = FontWeight.SemiBold,
                        color = CuttingInstructionTheme.ChamferMgmtFormingDayHeaderText,
                        backgroundColor = CuttingInstructionTheme.ChamferMgmtFormingDayHeaderBg,
                    )
                    listOf("生産日", "面取機", "製品名").forEach { h ->
                        ChamferMgmtCellText(
                            h,
                            width = chamferTodayColWidth(h),
                            fontWeight = FontWeight.ExtraBold,
                            color = headerText,
                            align = if (h == "製品名") CuttingCellAlign.Start else CuttingCellAlign.Center,
                        )
                    }
                    listOf("生産数", "不良", "完了", "カ無", "生産順", "生産時間").forEach { h ->
                        ChamferMgmtCellText(h, width = chamferTodayColWidth(h), fontWeight = FontWeight.ExtraBold, color = headerText)
                    }
                    ChamferMgmtCellText(
                        "操作",
                        width = chamferTodayColWidth("操作"),
                        fontWeight = FontWeight.ExtraBold,
                        showRightBorder = false,
                        color = headerText,
                    )
                }
                HorizontalDivider(color = CuttingInstructionTheme.ChamferMgmtTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .chamferTodayTableBodyHeight()
                        .verticalScroll(verticalScroll, enabled = !isDragging),
                ) {
                    when {
                        loading -> Box(
                            Modifier.chamferTodayTablePlaceholderHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.ChamferingAccent,
                            )
                        }
                        displayRows.isEmpty() -> Box(
                            Modifier.chamferTodayTablePlaceholderHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> displayRows.forEachIndexed { index, row ->
                            val rowId = row.id ?: 0
                            key(rowId) {
                                val isRowDragging = rowId > 0 && rowId == draggingRowId
                                val rowMachine = row.chamferingMachine.orEmpty().trim()
                                val sameMachineCount = displayRows.count {
                                    it.chamferingMachine.orEmpty().trim() == rowMachine && rowMachine.isNotBlank()
                                }
                                val canDrag = reorderEnabled && rowId > 0 && sameMachineCount > 1 && rowMachine.isNotBlank()
                                val dragModifier = if (canDrag) {
                                    Modifier.pointerInput(rowId, reorderEnabled, displayRows.size) {
                                        detectMgmtTableLongPressDragGestures(
                                            longPressTimeoutMillis = MgmtTableReorderLongPressMs,
                                            onDragStart = {
                                                draggingRowId = rowId
                                                dragStartIndex = displayRows.indexOfFirst { it.id == rowId }
                                                dragOffsetY = 0f
                                                isDragging = true
                                            },
                                            onDrag = { delta -> dragOffsetY += delta.y },
                                            onDragEnd = {
                                                if (draggingRowId >= 0 && dragStartIndex >= 0) {
                                                    applyChamferingRowReorderFromOffset(
                                                        rows = displayRows,
                                                        startIndex = dragStartIndex,
                                                        totalOffsetY = dragOffsetY,
                                                        rowHeights = rowHeights,
                                                        rowHeightPx = rowHeightPx,
                                                        dividerPx = dividerPx,
                                                    )
                                                    val draggedRow = displayRows.find { it.id == draggingRowId }
                                                    if (draggedRow != null) {
                                                        val cm = draggedRow.chamferingMachine.orEmpty().trim()
                                                        val sameMachine = displayRows.filter {
                                                            it.chamferingMachine.orEmpty().trim() == cm
                                                        }
                                                        onCommitReorder(sameMachine.mapNotNull { it.id }, cm)
                                                    }
                                                }
                                                draggingRowId = -1
                                                dragStartIndex = -1
                                                dragOffsetY = 0f
                                                isDragging = false
                                            },
                                            onDragCancel = {
                                                draggingRowId = -1
                                                dragStartIndex = -1
                                                dragOffsetY = 0f
                                                isDragging = false
                                                displayRows.clear()
                                                displayRows.addAll(rows)
                                            },
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                                val mgmtCode = row.managementCode?.trim().orEmpty()
                                val formingDay = formatInstructionDate(formingStartDateByMgmtCode[mgmtCode])
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(dragModifier)
                                        .combinedClickable(onClick = {}, onDoubleClick = { onEdit(row) })
                                        .zIndex(if (isRowDragging) 1f else 0f)
                                        .offset { IntOffset(0, if (isRowDragging) dragOffsetY.roundToInt() else 0) }
                                        .alpha(if (isRowDragging) 0.88f else 1f)
                                        .shadow(if (isRowDragging) 6.dp else 0.dp, RoundedCornerShape(4.dp))
                                        .onSizeChanged { if (rowId > 0) rowHeights[rowId] = it.height }
                                        .background(chamferMgmtRowBackground(index)),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                ChamferMgmtCellText(row.cd ?: row.managementCode ?: "-", width = chamferTodayColWidth("CD"))
                                ChamferMgmtCellText(row.productionLine ?: "-", width = chamferTodayColWidth("ライン"))
                                ChamferMgmtCellText(
                                    formingDay,
                                    width = chamferTodayColWidth("成型予定日"),
                                    fontWeight = FontWeight.Medium,
                                    color = CuttingInstructionTheme.ChamferMgmtFormingDayCellText,
                                    backgroundColor = CuttingInstructionTheme.ChamferMgmtFormingDayCellBg,
                                )
                                ChamferMgmtCellText(formatInstructionDate(row.productionDay), width = chamferTodayColWidth("生産日"))
                                ChamferMgmtCellText(row.chamferingMachine ?: "-", width = chamferTodayColWidth("面取機"))
                                ChamferMgmtCellText(
                                    chamferProductNameDisplay(row),
                                    width = chamferTodayColWidth("製品名"),
                                    align = CuttingCellAlign.Start,
                                )
                                ChamferMgmtCellText(
                                    row.actualProductionQuantity?.toString() ?: "-",
                                    width = chamferTodayColWidth("生産数"),
                                    align = CuttingCellAlign.End,
                                )
                                ChamferMgmtCellText(row.defectQty?.toString() ?: "-", width = chamferTodayColWidth("不良"))
                                CuttingTodayCellBorder(width = chamferTodayColWidth("完了")) {
                                    Switch(
                                        checked = row.productionCompletedCheck == 1,
                                        onCheckedChange = { onToggleCompleted(row, it) },
                                        modifier = Modifier.scale(CuttingInstructionTheme.CuttingCompletedSwitchScale),
                                    )
                                }
                                CuttingTodayCellBorder(width = chamferTodayColWidth("カ無")) {
                                    Switch(
                                        checked = row.noCount == 1,
                                        onCheckedChange = { onToggleNoCount(row, it) },
                                        modifier = Modifier.scale(CuttingInstructionTheme.CuttingCompletedSwitchScale),
                                    )
                                }
                                ChamferMgmtCellText(row.productionSequence?.toString() ?: "-", width = chamferTodayColWidth("生産順"))
                                ChamferMgmtCellText(row.productionTime ?: "-", width = chamferTodayColWidth("生産時間"))
                                CuttingTodayCellBorder(width = chamferTodayColWidth("操作"), showRightBorder = false) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if ((row.actualProductionQuantity ?: 0) > 0) {
                                            SplitToNextDayLinkButton(onClick = { onSplit(row) })
                                        }
                                        IconButton(onClick = { onDuplicate(row) }, modifier = Modifier.size(22.dp)) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "複製",
                                                tint = CuttingInstructionTheme.ChamferHeaderBtnIssue,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                        IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(22.dp)) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "削除",
                                                tint = Color(0xFFF56C6C),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            if (index < displayRows.lastIndex) {
                                HorizontalDivider(
                                    color = CuttingInstructionTheme.ChamferMgmtTableBorder.copy(alpha = 0.45f),
                                    thickness = 1.dp,
                                )
                            }
                            }
                        }
                    }
                }
            }
        }
        if (rows.isNotEmpty()) {
            HorizontalDivider(color = CuttingInstructionTheme.ChamferMgmtTableBorder, thickness = 2.dp)
            ChamferMgmtSummaryFooter(rows)
        }
    }
}

@Composable
private fun ChamferingTomorrowTable(
    rows: List<InstructionChamferingRowDto>,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    val headerText = CuttingInstructionTheme.ChamferMgmtTableHeaderText
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(tableShape)
            .border(1.dp, CuttingInstructionTheme.ChamferMgmtTableBorder, tableShape),
    ) {
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Column(Modifier.widthIn(min = ChamferMgmtTomorrowMinWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CuttingInstructionTheme.ChamferMgmtTableHeaderStart,
                                    CuttingInstructionTheme.ChamferMgmtTableHeaderEnd,
                                ),
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("CD", "生産日", "面取機", "製品名", "生産数", "生産時間").forEach { h ->
                        ChamferMgmtCellText(
                            h,
                            width = chamferTomorrowColWidth(h),
                            fontWeight = FontWeight.ExtraBold,
                            color = headerText,
                            align = when (h) {
                                "製品名" -> CuttingCellAlign.Start
                                "生産数" -> CuttingCellAlign.End
                                else -> CuttingCellAlign.Center
                            },
                            showRightBorder = h != "生産時間",
                        )
                    }
                }
                HorizontalDivider(color = CuttingInstructionTheme.ChamferMgmtTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .chamferTodayTableBodyHeight()
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier.chamferTodayTablePlaceholderHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.ChamferingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier.chamferTodayTablePlaceholderHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> rows.forEachIndexed { index, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(chamferMgmtRowBackground(index)),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ChamferMgmtCellText(row.cd ?: row.managementCode ?: "-", width = chamferTomorrowColWidth("CD"))
                                ChamferMgmtCellText(formatInstructionDate(row.productionDay), width = chamferTomorrowColWidth("生産日"))
                                ChamferMgmtCellText(row.chamferingMachine ?: "-", width = chamferTomorrowColWidth("面取機"))
                                ChamferMgmtCellText(
                                    chamferProductNameDisplay(row),
                                    width = chamferTomorrowColWidth("製品名"),
                                    align = CuttingCellAlign.Start,
                                )
                                ChamferMgmtCellText(
                                    row.actualProductionQuantity?.toString() ?: "-",
                                    width = chamferTomorrowColWidth("生産数"),
                                    align = CuttingCellAlign.End,
                                )
                                ChamferMgmtCellText(
                                    row.productionTime ?: "-",
                                    width = chamferTomorrowColWidth("生産時間"),
                                    showRightBorder = false,
                                )
                            }
                            if (index < rows.lastIndex) {
                                HorizontalDivider(
                                    color = CuttingInstructionTheme.ChamferMgmtTableBorder.copy(alpha = 0.45f),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (rows.isNotEmpty()) {
            HorizontalDivider(color = CuttingInstructionTheme.ChamferMgmtTableBorder, thickness = 2.dp)
            ChamferMgmtSummaryFooter(rows, showDefectTotal = false)
        }
    }
}

@Composable
fun ChamferingManagementTable(
    rows: List<InstructionChamferingRowDto>,
    loading: Boolean,
    formingStartDateByMgmtCode: Map<String, String> = emptyMap(),
    onToggleCompleted: (InstructionChamferingRowDto, Boolean) -> Unit,
    onToggleNoCount: (InstructionChamferingRowDto, Boolean) -> Unit = { _, _ -> },
    onEdit: (InstructionChamferingRowDto) -> Unit = {},
    onDuplicate: (InstructionChamferingRowDto) -> Unit = {},
    onDelete: (InstructionChamferingRowDto) -> Unit = {},
    onSplit: (InstructionChamferingRowDto) -> Unit = {},
    onMoveUp: (InstructionChamferingRowDto) -> Unit = {},
    onMoveDown: (InstructionChamferingRowDto) -> Unit = {},
    onCommitReorder: (List<Int>, String) -> Unit = { _, _ -> },
    reorderEnabled: Boolean = true,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        ChamferingTomorrowTable(
            rows = rows,
            loading = loading,
            modifier = modifier,
        )
    } else {
        ChamferingTodayTable(
            rows = rows,
            loading = loading,
            formingStartDateByMgmtCode = formingStartDateByMgmtCode,
            onToggleCompleted = onToggleCompleted,
            onToggleNoCount = onToggleNoCount,
            onEdit = onEdit,
            onDuplicate = onDuplicate,
            onDelete = onDelete,
            onSplit = onSplit,
            onCommitReorder = onCommitReorder,
            reorderEnabled = reorderEnabled,
            modifier = modifier,
        )
    }
}

private val ChamferPlanRowHeight = PlanBatchRowHeight
private val ChamferPlanBodyHeight = ChamferPlanRowHeight * CuttingInstructionTheme.ChamferPlanVisibleRows
private val ChamferPlanMinWidth = CuttingInstructionTheme.ChamferPlanMinWidthDp.dp
private val ChamferPlanGridBorder = Color(0xFFE2E8F0)

private fun chamferPlanColumnWidth(header: String) = when (header) {
    "→面取" -> 44.dp
    "CD" -> 56.dp
    "生産月" -> 72.dp
    "ライン" -> 52.dp
    "切断生産日", "成型予定日" -> 76.dp
    "原材料" -> 110.dp
    "生産数", "ロット数" -> 55.dp
    "ロットNo" -> 48.dp
    "SW" -> 36.dp
    "操作" -> 90.dp
    else -> 56.dp
}

@Composable
private fun RowScope.ChamferPlanCell(
    width: Dp? = null,
    weight: Float = 0f,
    minWidth: Dp = 0.dp,
    align: CuttingCellAlign = CuttingCellAlign.Center,
    backgroundColor: Color = Color.Unspecified,
    showRightBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val mod = when {
        weight > 0f -> Modifier.weight(weight).defaultMinSize(minWidth = minWidth)
        width != null -> Modifier.width(width)
        else -> Modifier
    }
    Box(
        modifier = mod
            .height(ChamferPlanRowHeight)
            .then(if (backgroundColor != Color.Unspecified) Modifier.background(backgroundColor) else Modifier)
            .drawBehind {
                if (showRightBorder) {
                    drawLine(
                        color = ChamferPlanGridBorder,
                        start = Offset(size.width - 0.5f, 0f),
                        end = Offset(size.width - 0.5f, size.height),
                        strokeWidth = 1f,
                    )
                }
            }
            .padding(horizontal = 4.dp),
        contentAlignment = when (align) {
            CuttingCellAlign.Start -> Alignment.CenterStart
            CuttingCellAlign.Center -> Alignment.Center
            CuttingCellAlign.End -> Alignment.CenterEnd
        },
        content = content,
    )
}

@Composable
private fun RowScope.ChamferPlanCellText(
    text: String,
    width: Dp? = null,
    weight: Float = 0f,
    minWidth: Dp = 0.dp,
    align: CuttingCellAlign = CuttingCellAlign.Center,
    fontWeight: FontWeight = FontWeight.Normal,
    showRightBorder: Boolean = true,
    color: Color = CuttingInstructionTheme.Title,
    backgroundColor: Color = Color.Unspecified,
) {
    ChamferPlanCell(width, weight, minWidth, align, backgroundColor, showRightBorder) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = when (align) {
                CuttingCellAlign.Start -> TextAlign.Start
                CuttingCellAlign.Center -> TextAlign.Center
                CuttingCellAlign.End -> TextAlign.End
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChamferingPlanTable(
    rows: List<InstructionChamferingPlanRowDto>,
    selectedId: Int?,
    loading: Boolean,
    cuttingProductionDayByMgmtCode: Map<String, String>,
    cuttingFormingStartDateByMgmtCode: Map<String, String>,
    onSelect: (InstructionChamferingPlanRowDto) -> Unit,
    onToggleSw: (InstructionChamferingPlanRowDto, Boolean) -> Unit,
    onCopy: (InstructionChamferingPlanRowDto) -> Unit,
    onDelete: (InstructionChamferingPlanRowDto) -> Unit,
    onMove: (InstructionChamferingPlanRowDto) -> Unit,
    onEdit: (InstructionChamferingPlanRowDto) -> Unit,
) {
    val sortedRows = remember(rows) {
        rows.sortedWith(
            compareBy<InstructionChamferingPlanRowDto>(
                { it.productName.orEmpty() },
                { chamferPlanCd(it) },
            ),
        )
    }
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tableShape)
            .border(1.dp, CuttingInstructionTheme.ChamferPlanTableBorder, tableShape)
            .background(Color.White),
    ) {
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Column(Modifier.widthIn(min = ChamferPlanMinWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChamferPlanRowHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CuttingInstructionTheme.ChamferPlanTableHeaderStart,
                                    CuttingInstructionTheme.ChamferPlanTableHeaderEnd,
                                ),
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChamferPlanCellText("→面取", width = chamferPlanColumnWidth("→面取"), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText("CD", width = chamferPlanColumnWidth("CD"), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText("生産月", width = chamferPlanColumnWidth("生産月"), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText("ライン", width = chamferPlanColumnWidth("ライン"), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText(
                        "切断生産日",
                        width = chamferPlanColumnWidth("切断生産日"),
                        fontWeight = FontWeight.ExtraBold,
                        color = CuttingInstructionTheme.ChamferPlanCuttingDayHeaderText,
                        backgroundColor = CuttingInstructionTheme.ChamferPlanCuttingDayHeaderBg,
                    )
                    ChamferPlanCellText(
                        "成型予定日",
                        width = chamferPlanColumnWidth("成型予定日"),
                        fontWeight = FontWeight.ExtraBold,
                        color = CuttingInstructionTheme.ChamferPlanFormingDayHeaderText,
                        backgroundColor = CuttingInstructionTheme.ChamferPlanFormingDayHeaderBg,
                    )
                    ChamferPlanCellText("製品名", weight = 1f, minWidth = 110.dp, align = CuttingCellAlign.Start, fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText("原材料", width = chamferPlanColumnWidth("原材料"), align = CuttingCellAlign.Start, fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    listOf("生産数", "ロット数", "ロットNo").forEach { h ->
                        ChamferPlanCellText(h, width = chamferPlanColumnWidth(h), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    }
                    ChamferPlanCellText("SW", width = chamferPlanColumnWidth("SW"), fontWeight = FontWeight.ExtraBold, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                    ChamferPlanCellText("操作", width = chamferPlanColumnWidth("操作"), fontWeight = FontWeight.ExtraBold, showRightBorder = false, color = CuttingInstructionTheme.ChamferPlanTableHeaderText)
                }
                HorizontalDivider(color = CuttingInstructionTheme.ChamferPlanTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .height(ChamferPlanBodyHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(ChamferPlanBodyHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.ChamferingAccent,
                            )
                        }
                        sortedRows.isEmpty() -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(ChamferPlanBodyHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> sortedRows.forEachIndexed { index, row ->
                            val mgmtCode = row.managementCode?.trim().orEmpty()
                            val cuttingDayRaw = cuttingProductionDayByMgmtCode[mgmtCode]
                            val cuttingDay = formatInstructionDate(cuttingDayRaw)
                            val formingDay = formatInstructionDate(cuttingFormingStartDateByMgmtCode[mgmtCode])
                            val cuttingDue = isCuttingProductionDayDueOrOverdue(cuttingDay)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ChamferPlanRowHeight)
                                    .combinedClickable(onClick = { onSelect(row) }, onDoubleClick = { onEdit(row) })
                                    .background(
                                        when {
                                            row.id == selectedId -> CuttingInstructionTheme.RowSelected
                                            index % 2 == 1 -> Color(0xFFFAFAFF)
                                            else -> Color.White
                                        },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ChamferPlanCell(width = chamferPlanColumnWidth("→面取")) {
                                    TextButton(onClick = { onMove(row) }, contentPadding = PaddingValues(0.dp)) {
                                        Text("→面取", fontSize = 9.sp, color = CuttingInstructionTheme.ChamferingAccent)
                                    }
                                }
                                ChamferPlanCellText(chamferPlanCd(row), width = chamferPlanColumnWidth("CD"))
                                ChamferPlanCellText(formatProductionMonth(row.productionMonth), width = chamferPlanColumnWidth("生産月"))
                                ChamferPlanCellText(row.productionLine ?: "-", width = chamferPlanColumnWidth("ライン"))
                                ChamferPlanCellText(
                                    cuttingDay,
                                    width = chamferPlanColumnWidth("切断生産日"),
                                    fontWeight = if (cuttingDue) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (cuttingDue) CuttingInstructionTheme.ChamferPlanCuttingDayDueText else CuttingInstructionTheme.ChamferPlanCuttingDayCellText,
                                    backgroundColor = if (cuttingDue) CuttingInstructionTheme.ChamferPlanCuttingDayDueBg else CuttingInstructionTheme.ChamferPlanCuttingDayCellBg,
                                )
                                ChamferPlanCellText(
                                    formingDay,
                                    width = chamferPlanColumnWidth("成型予定日"),
                                    fontWeight = FontWeight.Medium,
                                    color = CuttingInstructionTheme.ChamferPlanFormingDayCellText,
                                    backgroundColor = CuttingInstructionTheme.ChamferPlanFormingDayCellBg,
                                )
                                ChamferPlanCellText(
                                    row.productName ?: row.productCd ?: "-",
                                    weight = 1f,
                                    minWidth = 110.dp,
                                    align = CuttingCellAlign.Start,
                                )
                                ChamferPlanCellText(row.materialName ?: "-", width = chamferPlanColumnWidth("原材料"), align = CuttingCellAlign.Start)
                                ChamferPlanCellText(row.actualProductionQuantity?.toString() ?: "-", width = chamferPlanColumnWidth("生産数"))
                                ChamferPlanCellText(row.productionLotSize?.toString() ?: "-", width = chamferPlanColumnWidth("ロット数"))
                                ChamferPlanCellText(row.lotNumber ?: "-", width = chamferPlanColumnWidth("ロットNo"))
                                ChamferPlanCell(width = chamferPlanColumnWidth("SW")) {
                                    Switch(
                                        checked = row.hasSwProcess == 1,
                                        onCheckedChange = { onToggleSw(row, it) },
                                        modifier = Modifier.scale(0.75f),
                                    )
                                }
                                ChamferPlanCell(width = chamferPlanColumnWidth("操作"), showRightBorder = false) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        IconButton(onClick = { onCopy(row) }, modifier = Modifier.size(22.dp)) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "複製",
                                                tint = CuttingInstructionTheme.ChamferingAccent,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                        IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(22.dp)) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "削除",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            if (index < sortedRows.lastIndex) {
                                HorizontalDivider(color = ChamferPlanGridBorder.copy(alpha = 0.85f), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val UsageSummaryRowHeight = CuttingInstructionTheme.UsageSummaryRowHeightDp.dp
private val UsageSummaryHeaderHeight = CuttingInstructionTheme.UsageSummaryHeaderHeightDp.dp
private val UsageSummaryBodyHeight =
    UsageSummaryRowHeight * CuttingInstructionTheme.UsageSummaryVisibleRows

private val UsageSummaryColumnWeights = listOf(
    "製品名" to 1.35f,
    "材料" to 1.15f,
    "在庫区分" to 0.85f,
    "使用数" to 0.85f,
    "反映" to 0.8f,
)

@Composable
private fun RowScope.UsageSummaryHeaderCell(text: String, weight: Float) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CuttingInstructionTheme.TableHeaderText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RowScope.UsageSummaryTextCell(
    text: String,
    weight: Float,
    align: CuttingCellAlign = CuttingCellAlign.Start,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val resolvedColor = if (color == Color.Unspecified) Color(0xFF303133) else color
    Box(
        modifier = modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        contentAlignment = when (align) {
            CuttingCellAlign.Start -> Alignment.CenterStart
            CuttingCellAlign.Center -> Alignment.Center
            CuttingCellAlign.End -> Alignment.CenterEnd
        },
    ) {
        Text(
            text,
            fontSize = 10.sp,
            color = resolvedColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = when (align) {
                CuttingCellAlign.Start -> TextAlign.Start
                CuttingCellAlign.Center -> TextAlign.Center
                CuttingCellAlign.End -> TextAlign.End
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UsageSummaryStockSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.scale(CuttingInstructionTheme.UsageSummaryStockSwitchScale),
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = CuttingInstructionTheme.UsageSummaryStockSwitchTrackChecked,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = CuttingInstructionTheme.UsageSummaryStockSwitchTrackUnchecked,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
private fun UsageSummaryTableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .height(UsageSummaryHeaderHeight)
            .background(CuttingInstructionTheme.TableHeaderBg)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UsageSummaryColumnWeights.forEach { (header, weight) ->
            UsageSummaryHeaderCell(header, weight)
        }
    }
}

@Composable
private fun UsageSummaryTableRow(
    row: InstructionCuttingRowDto,
    index: Int,
    reflected: Boolean,
    onToggleStock: (InstructionCuttingRowDto, Boolean) -> Unit,
    onEditUsage: (InstructionCuttingRowDto) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(UsageSummaryRowHeight),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UsageSummaryTextCell(
                text = row.productName ?: "-",
                weight = UsageSummaryColumnWeights[0].second,
                align = CuttingCellAlign.Start,
            )
            UsageSummaryTextCell(
                text = row.materialName ?: "-",
                weight = UsageSummaryColumnWeights[1].second,
                align = CuttingCellAlign.Start,
            )
            Box(
                modifier = Modifier.weight(UsageSummaryColumnWeights[2].second),
                contentAlignment = Alignment.Center,
            ) {
                UsageSummaryStockSwitch(
                    checked = row.useMaterialStockSub == 1,
                    onCheckedChange = { onToggleStock(row, it) },
                )
            }
            UsageSummaryTextCell(
                text = formatUsageCount(row.usageCount),
                weight = UsageSummaryColumnWeights[3].second,
                align = CuttingCellAlign.Center,
                modifier = Modifier.clickable { onEditUsage(row) },
            )
            UsageSummaryTextCell(
                text = if (reflected) "済" else "未",
                weight = UsageSummaryColumnWeights[4].second,
                align = CuttingCellAlign.Center,
                color = if (reflected) CuttingInstructionTheme.ChamferingAccent else Color(0xFFDC2626),
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.4f),
        )
    }
}

@Composable
fun UsageSummaryTable(
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    reflectedCodes: Set<String>,
    onToggleStock: (InstructionCuttingRowDto, Boolean) -> Unit,
    onEditUsage: (InstructionCuttingRowDto) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        UsageSummaryTableHeader()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(UsageSummaryBodyHeight),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                rows.isEmpty() -> Text(
                    "該当日のデータがありません",
                    fontSize = 11.sp,
                    color = CuttingInstructionTheme.Subtitle,
                )
                else -> {
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UsageSummaryBodyHeight)
                            .verticalScroll(scroll),
                    ) {
                        rows.forEachIndexed { index, row ->
                            val reflected = row.materialUsageReflected == "反映済" ||
                                reflectedCodes.contains(row.managementCode.orEmpty().trim())
                            UsageSummaryTableRow(
                                row = row,
                                index = index,
                                reflected = reflected,
                                onToggleStock = onToggleStock,
                                onEditUsage = onEditUsage,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val KanbanRowHeight = CuttingInstructionTheme.KanbanRowHeightDp.dp
private val KanbanBodyHeight = KanbanRowHeight * CuttingInstructionTheme.KanbanVisibleRows
private val KanbanHeaderBtnHeight = CuttingInstructionTheme.InstructionDateNavHeightDp.dp

@Composable
fun KanbanIssuanceSection(
    date: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    productName: String,
    productOptions: List<String>,
    onProductChange: (String) -> Unit,
    rows: List<KanbanIssuanceRowDto>,
    selectedIds: Set<Int>,
    loading: Boolean,
    batchIssueLoading: Boolean,
    syncLoading: Boolean,
    page: Int,
    totalPages: Int,
    totalCount: Int,
    onPageChange: (Int) -> Unit,
    onToggleSelect: (Int, Boolean) -> Unit,
    onTogglePageSelect: (Boolean) -> Unit,
    onBatchIssue: () -> Unit,
    onSync: () -> Unit,
    onIssue: (Int) -> Unit,
    onReissue: (Int) -> Unit,
    onEdit: (KanbanIssuanceRowDto) -> Unit,
    issueLoadingId: Int? = null,
    reissueLoadingId: Int? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = CuttingInstructionTheme.CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder.copy(alpha = 0.85f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "カンバン発行",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CuttingInstructionTheme.KanbanTitle,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InstructionDateNav(date, onPrevDate, onNextDate)
                    KanbanHeaderOutlinedButton("今日", onToday)
                    InstructionFilterDropdown(
                        label = "状態",
                        value = status,
                        options = listOf("" to "（全部）", "pending" to "待発行", "issued" to "発行済", "completed" to "完了"),
                        onSelect = onStatusChange,
                    )
                    InstructionFilterDropdown(
                        label = "製品名",
                        value = productName,
                        options = productOptions.map { it to it },
                        onSelect = onProductChange,
                    )
                    KanbanHeaderPrimaryButton("一括発行", onBatchIssue, loading = batchIssueLoading)
                    KanbanHeaderOutlinedButton("更新", onSync, loading = syncLoading)
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
                color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f),
            )
            KanbanTable(
                rows = rows,
                selectedIds = selectedIds,
                loading = loading,
                onToggleSelect = onToggleSelect,
                onTogglePageSelect = onTogglePageSelect,
                onIssue = onIssue,
                onReissue = onReissue,
                onEdit = onEdit,
                issueLoadingId = issueLoadingId,
                reissueLoadingId = reissueLoadingId,
            )
            if (totalCount > 0) {
                InstructionPaginationBar(page, totalPages, totalCount, onPageChange)
            }
        }
    }
}

@Composable
private fun KanbanHeaderOutlinedButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.height(KanbanHeaderBtnHeight),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CuttingInstructionTheme.FilterBg,
            contentColor = CuttingInstructionTheme.Title,
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 11.sp)
        }
    }
}

@Composable
private fun KanbanHeaderPrimaryButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.height(KanbanHeaderBtnHeight),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CuttingInstructionTheme.BtnIssue),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KanbanTable(
    rows: List<KanbanIssuanceRowDto>,
    selectedIds: Set<Int>,
    loading: Boolean,
    onToggleSelect: (Int, Boolean) -> Unit,
    onTogglePageSelect: (Boolean) -> Unit,
    onIssue: (Int) -> Unit,
    onReissue: (Int) -> Unit = {},
    onEdit: (KanbanIssuanceRowDto) -> Unit = {},
    issueLoadingId: Int? = null,
    reissueLoadingId: Int? = null,
) {
    val selectableIds = rows.filter { it.status == "pending" || it.status == "issued" }.mapNotNull { it.id }
    val pageAllSelected = selectableIds.isNotEmpty() && selectableIds.all { it in selectedIds }
    Column(Modifier.fillMaxWidth()) {
        KanbanTableHeaderRow(
            pageAllSelected = pageAllSelected,
            hasSelectable = selectableIds.isNotEmpty(),
            onTogglePageSelect = onTogglePageSelect,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(KanbanBodyHeight),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.size(22.dp), color = CuttingInstructionTheme.KanbanAccent)
                rows.isEmpty() -> Text("データなし", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)
                else -> {
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(KanbanBodyHeight)
                            .verticalScroll(scroll),
                    ) {
                        rows.forEachIndexed { index, row ->
                            KanbanTableDataRow(
                                row = row,
                                index = index,
                                selectedIds = selectedIds,
                                onToggleSelect = onToggleSelect,
                                onIssue = onIssue,
                                onReissue = onReissue,
                                onEdit = onEdit,
                                issueLoadingId = issueLoadingId,
                                reissueLoadingId = reissueLoadingId,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanTableHeaderRow(
    pageAllSelected: Boolean,
    hasSelectable: Boolean,
    onTogglePageSelect: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(KanbanRowHeight)
            .background(CuttingInstructionTheme.KanbanTableHeaderBg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KanbanCheckboxCell {
            Checkbox(
                checked = pageAllSelected,
                onCheckedChange = onTogglePageSelect,
                enabled = hasSelectable,
                modifier = Modifier.size(18.dp).scale(0.85f),
                colors = CheckboxDefaults.colors(checkedColor = CuttingInstructionTheme.BtnIssue),
            )
        }
        KanbanHeaderCell("状態", 56.dp)
        KanbanHeaderCell("発行日", 72.dp)
        KanbanHeaderCell("生産日", 72.dp)
        KanbanHeaderCell("製品名", weight = 1.3f, align = TextAlign.Start)
        KanbanHeaderCell("ライン", 52.dp)
        KanbanHeaderCell("切断機", 56.dp)
        KanbanHeaderCell("管理コード", weight = 1f, align = TextAlign.Start)
        KanbanHeaderCell("ロットNo.", 56.dp)
        KanbanHeaderCell("ロット本数", 64.dp, align = TextAlign.End)
        KanbanHeaderCell("操作", 56.dp)
    }
    HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.5f))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KanbanTableDataRow(
    row: KanbanIssuanceRowDto,
    index: Int,
    selectedIds: Set<Int>,
    onToggleSelect: (Int, Boolean) -> Unit,
    onIssue: (Int) -> Unit,
    onReissue: (Int) -> Unit,
    onEdit: (KanbanIssuanceRowDto) -> Unit,
    issueLoadingId: Int? = null,
    reissueLoadingId: Int? = null,
) {
    val id = row.id
    val selectable = row.status == "pending" || row.status == "issued"
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
            .then(
                if (id != null) {
                    Modifier.combinedClickable(onClick = {}, onDoubleClick = { onEdit(row) })
                } else Modifier,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(KanbanRowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KanbanCheckboxCell {
                if (id != null) {
                    Checkbox(
                        checked = selectedIds.contains(id),
                        onCheckedChange = { onToggleSelect(id, it) },
                        enabled = selectable,
                        modifier = Modifier.size(18.dp).scale(0.85f),
                        colors = CheckboxDefaults.colors(checkedColor = CuttingInstructionTheme.BtnIssue),
                    )
                }
            }
            KanbanDataCell(width = 56.dp) { KanbanStatusBadge(row.status) }
            KanbanDataCell(72.dp) { KanbanCellText(formatInstructionDate(row.issueDate).ifBlank { "-" }) }
            KanbanDataCell(72.dp) { KanbanCellText(formatInstructionDate(row.productionDay).ifBlank { "-" }) }
            KanbanDataCell(weight = 1.3f, align = TextAlign.Start) { KanbanCellText(row.productName ?: "-", maxLines = 1) }
            KanbanDataCell(52.dp) { KanbanCellText(row.productionLine ?: "-") }
            KanbanDataCell(56.dp) { KanbanCellText(row.cuttingMachine ?: "-") }
            KanbanDataCell(weight = 1f, align = TextAlign.Start) { KanbanCellText(row.managementCode ?: "-", maxLines = 1) }
            KanbanDataCell(56.dp) { KanbanCellText(row.lotNumber ?: "-") }
            KanbanDataCell(64.dp, align = TextAlign.End) { KanbanCellText(row.actualProductionQuantity?.toString() ?: "-") }
            KanbanDataCell(56.dp) {
                when (row.status) {
                    "pending" -> {
                        if (id != null) {
                            if (issueLoadingId == id) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = CuttingInstructionTheme.BtnIssue,
                                )
                            } else {
                                TextButton(
                                    onClick = { onIssue(id) },
                                    enabled = issueLoadingId == null,
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text("発行", fontSize = 11.sp, color = CuttingInstructionTheme.BtnIssue)
                                }
                            }
                        }
                    }
                    "issued" -> {
                        if (id != null) {
                            if (reissueLoadingId == id) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = CuttingInstructionTheme.KanbanAccent,
                                )
                            } else {
                                TextButton(
                                    onClick = { onReissue(id) },
                                    enabled = reissueLoadingId == null,
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text("再発行", fontSize = 11.sp, color = CuttingInstructionTheme.KanbanAccent)
                                }
                            }
                        }
                    }
                    else -> KanbanCellText("-")
                }
            }
        }
        HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.4f))
    }
}

@Composable
private fun RowScope.KanbanHeaderCell(
    text: String,
    width: Dp = Dp.Unspecified,
    weight: Float = 0f,
    align: TextAlign = TextAlign.Center,
) {
    val modifier = when {
        weight > 0f -> Modifier.weight(weight).padding(horizontal = 4.dp)
        else -> Modifier.width(width).padding(horizontal = 2.dp)
    }
    Text(
        text,
        modifier = modifier,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = CuttingInstructionTheme.KanbanTitle,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.KanbanDataCell(
    width: Dp = Dp.Unspecified,
    weight: Float = 0f,
    align: TextAlign = TextAlign.Center,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = when {
            weight > 0f -> Modifier.weight(weight).padding(horizontal = 4.dp)
            else -> Modifier.width(width).padding(horizontal = 2.dp)
        },
        contentAlignment = when (align) {
            TextAlign.End -> Alignment.CenterEnd
            TextAlign.Start -> Alignment.CenterStart
            else -> Alignment.Center
        },
    ) {
        content()
    }
}

@Composable
private fun RowScope.KanbanCheckboxCell(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.width(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun KanbanCellText(text: String, maxLines: Int = 1) {
    Text(
        text,
        fontSize = 10.sp,
        color = CuttingInstructionTheme.Title,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun KanbanStatusBadge(status: String?) {
    when (status) {
        "pending" -> Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFEF3C7),
        ) {
            Text(
                "待発行",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp,
                color = Color(0xFFD97706),
                fontWeight = FontWeight.Medium,
            )
        }
        "issued" -> Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEFF6FF),
        ) {
            Text(
                "発行済",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Medium,
            )
        }
        "completed" -> Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFECFDF5),
        ) {
            Text(
                "完了",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.Medium,
            )
        }
        else -> KanbanCellText(status ?: "-")
    }
}

@Composable
fun CuttingEquipmentEfficiencyPanel(
    productCd: String?,
    efficiency: List<com.example.smart_emap.data.model.EquipmentEfficiencyRowDto>,
    loading: Boolean,
    accent: Color = CuttingInstructionTheme.ChamferingAccent,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("設備能率（面取）", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accent)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f))
            when {
                productCd.isNullOrBlank() -> Box(
                    Modifier.fillMaxWidth().heightIn(min = ChamferPlanBodyHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("一覧で製品をクリック", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)
                }
                loading -> Box(
                    Modifier.fillMaxWidth().height(ChamferPlanBodyHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = accent)
                }
                efficiency.isEmpty() -> Box(
                    Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("該当データなし", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)
                }
                else -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CuttingInstructionTheme.TableHeaderBg)
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                    ) {
                        Text("設備", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.TableHeaderText, modifier = Modifier.weight(1f))
                        Text("能率", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.TableHeaderText)
                    }
                    efficiency.take(12).forEachIndexed { index, e ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(e.machinesName ?: "-", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text(e.efficiencyRate?.toString() ?: "-", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

private fun formatProductDetailNumber(value: Number?): String {
    if (value == null) return "-"
    return when (value) {
        is Double, is Float -> {
            val text = value.toString()
            if (text.endsWith(".0")) text.dropLast(2) else text
        }
        else -> value.toString()
    }
}

private fun formatProductDetailOptionalLength(value: Double?): String {
    if (value == null || value == 0.0) return "--"
    return formatProductDetailNumber(value)
}

@Composable
fun ProductDetailPanel(
    productCd: String?,
    detail: com.example.smart_emap.data.model.ProductBatchDetailDto?,
    efficiency: List<com.example.smart_emap.data.model.EquipmentEfficiencyRowDto>,
    loading: Boolean,
    accent: Color = CuttingInstructionTheme.CuttingAccent,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(10.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.TableBorder),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("製品情報", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accent)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f))
            if (productCd.isNullOrBlank()) {
                Box(Modifier.fillMaxWidth().heightIn(min = 120.dp), contentAlignment = Alignment.Center) {
                    Text("一覧で製品をクリック", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)
                }
                return@Column
            }
            if (loading) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = accent)
                }
                return@Column
            }
            if (detail == null) {
                Text("該当製品なし", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)
                return@Column
            }
            val items = listOf(
                "製品CD" to (detail.productCd ?: "-"),
                "製品名" to (detail.productName ?: "-"),
                "生産ロット" to formatProductDetailNumber(detail.lotSize),
                "材料" to (detail.materialName ?: detail.materialCd ?: "-"),
                "取数" to formatProductDetailNumber(detail.takeCount),
                "切断長" to formatProductDetailNumber(detail.cutLength),
                "面取長" to formatProductDetailOptionalLength(detail.chamferLength),
                "展開長" to formatProductDetailOptionalLength(detail.developedLength),
                "端材長" to formatProductDetailNumber(detail.scrapLength),
            )
            items.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle, modifier = Modifier.widthIn(max = 72.dp))
                    Text(
                        value,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CuttingInstructionTheme.Title,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.6f))
            Text("設備能率（切断）", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accent)
            Spacer(Modifier.height(6.dp))
            if (efficiency.isEmpty()) {
                Text("該当データなし", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CuttingInstructionTheme.TableHeaderBg)
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                ) {
                    Text("設備", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.TableHeaderText, modifier = Modifier.weight(1f))
                    Text("能率", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.TableHeaderText)
                }
                efficiency.take(8).forEachIndexed { index, e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(e.machinesName ?: "-", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(e.efficiencyRate?.toString() ?: "-", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun InstructionPaginationBar(page: Int, totalPages: Int, total: Int, onPageChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("全 $total 件", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onPageChange(page - 1) }, enabled = page > 1, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text("前", fontSize = 11.sp)
            }
            Text("$page / $totalPages", fontSize = 11.sp)
            OutlinedButton(onClick = { onPageChange(page + 1) }, enabled = page < totalPages, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text("次", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ChamferPlanNewAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val height = CuttingInstructionTheme.SectionCardTitleHeightDp.dp
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chamferPlanNewBtnScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 0.dp else 3.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chamferPlanNewBtnElev",
    )
    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation,
                shape,
                ambientColor = CuttingInstructionTheme.ChamferPlanGlassBtnShadow,
                spotColor = CuttingInstructionTheme.ChamferPlanGlassBtnShadow,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        CuttingInstructionTheme.ChamferPlanGlassBtnTop,
                        CuttingInstructionTheme.ChamferPlanGlassBtnMid,
                        CuttingInstructionTheme.ChamferPlanGlassBtnBottom,
                    ),
                ),
            )
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = if (pressed) 0.28f else 0.52f),
                    start = Offset(8.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 8.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
                drawLine(
                    color = CuttingInstructionTheme.ChamferPlanGlassBtnEdge.copy(alpha = 0.35f),
                    start = Offset(6.dp.toPx(), size.height - 1.dp.toPx()),
                    end = Offset(size.width - 6.dp.toPx(), size.height - 1.dp.toPx()),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }
            .border(1.dp, CuttingInstructionTheme.ChamferPlanGlassBtnBorder, shape)
            .border(0.5.dp, CuttingInstructionTheme.ChamferPlanGlassBtnEdge, shape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (pressed) 0.08f else 0.18f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (pressed) 0.04f else 0.1f),
                        ),
                    ),
                ),
        )
        Text(
            "新規追加",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CuttingInstructionTheme.ChamferPlanGlassBtnText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun GradientActionButton(
    text: String,
    colors: List<Color>,
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = contentColor, disabledContainerColor = Color.Transparent),
        modifier = modifier.background(
            if (enabled) Brush.horizontalGradient(colors) else Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))),
            shape,
        ),
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

enum class HeaderToolbarButtonStyle {
    Molding,
    CuttingDone,
    ChamferingDone,
}

private val HeaderToolbarButtonShape = RoundedCornerShape(18.dp)
private val HeaderToolbarButtonHeight = CuttingInstructionTheme.HeaderToolbarButtonHeightDp.dp

@Composable
fun HeaderToolbarButton(
    text: String,
    style: HeaderToolbarButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "headerToolbarScale",
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 1.dp else 5.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "headerToolbarElev",
    )
    val (topColor, bottomColor, textColor, borderColor, shadowColor) = when (style) {
        HeaderToolbarButtonStyle.Molding -> listOf(
            CuttingInstructionTheme.HeaderBtnMoldingTop,
            CuttingInstructionTheme.HeaderBtnMoldingBottom,
            CuttingInstructionTheme.BtnMoldingSoftText,
            CuttingInstructionTheme.HeaderBtnMoldingBorder,
            CuttingInstructionTheme.HeaderBtnMoldingShadow,
        )
        HeaderToolbarButtonStyle.CuttingDone -> listOf(
            CuttingInstructionTheme.HeaderBtnCuttingTop,
            CuttingInstructionTheme.HeaderBtnCuttingBottom,
            Color.White,
            CuttingInstructionTheme.HeaderBtnCuttingBorder,
            CuttingInstructionTheme.HeaderBtnCuttingShadow,
        )
        HeaderToolbarButtonStyle.ChamferingDone -> listOf(
            CuttingInstructionTheme.HeaderBtnChamferTop,
            CuttingInstructionTheme.HeaderBtnChamferBottom,
            Color.White,
            CuttingInstructionTheme.HeaderBtnChamferBorder,
            CuttingInstructionTheme.HeaderBtnChamferShadow,
        )
    }
    Box(
        modifier = modifier
            .height(HeaderToolbarButtonHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, HeaderToolbarButtonShape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(HeaderToolbarButtonShape)
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .border(1.dp, borderColor.copy(alpha = if (pressed) 0.7f else 1f), HeaderToolbarButtonShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = if (pressed) 0.12f else 0.28f), Color.Transparent),
                        startY = 0f,
                        endY = 80f,
                    ),
                ),
        )
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SoftActionButton(text: String, onClick: () -> Unit) {
    HeaderToolbarButton(text, HeaderToolbarButtonStyle.Molding, onClick)
}

@Composable
fun ToolbarLinkButton(text: String, onClick: () -> Unit, accent: Color = CuttingInstructionTheme.BatchAccent) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, fontSize = 11.sp, color = accent, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SyncOutlineButton(label: String, loading: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = CuttingInstructionTheme.BatchAccent),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp)
    }
}
