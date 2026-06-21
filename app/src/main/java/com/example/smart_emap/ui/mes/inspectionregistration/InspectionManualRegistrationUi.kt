package com.example.smart_emap.ui.mes.inspectionregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.mes.inspection.InspectionManagementRowExt
import java.text.NumberFormat
import java.util.Locale

internal object ImrTheme {
    val PageBg = Color(0xFFF8FAFC)
    val Surface = Color(0xE0FFFFFF)
    val Border = Color(0x3894A3B8)
    val TextPrimary = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val Label = Color(0xFF334155)
    val C1 = Color(0xFF3B82F6)
    val C2 = Color(0xFF8B5CF6)
    val C3 = Color(0xFF14B8A6)
    val C4 = Color(0xFFF59E0B)
    val C5 = Color(0xFF6366F1)
    val C6 = Color(0xFFF43F5E)
    val C7 = Color(0xFF64748B)
    val Indigo = Color(0xFF4F46E5)
    val Rose = Color(0xFFE11D48)
    val EditBorder = Color(0xFFF59E0B)
}

/** 画面全体で統一する寸法（コンパクト・高さ揃え） */
private object ImrMetrics {
    val PagePadH = 8.dp
    val PagePadV = 6.dp
    val SectionGap = 8.dp
    val InnerGap = 6.dp
    val ControlHeight = 38.dp
    val ControlHeightMulti = 56.dp
    val IconBtn = 32.dp
    val Radius = 10.dp
    val RadiusSm = 8.dp
    val SectionPadH = 10.dp
    val SectionPadV = 8.dp
    val TripleMinH = 78.dp
    val TimeCellH = 54.dp
    val DefectCardW = 78.dp
    val StepBadge = 16.dp
    val TableCompactBreakpoint = 600.dp
    val TablePanelHeadCompactBreakpoint = 480.dp
    val MinuteFieldCellW = 58.dp
    val MinuteInputW = 44.dp
    val TableOpWidth = 52.dp
}

private data class ImrTableColumn(val key: String, val label: String, val weight: Float)

private val ImrTableColumns = listOf(
    ImrTableColumn("day", "生産日", 1.05f),
    ImrTableColumn("inspector", "検査員", 1f),
    ImrTableColumn("cd", "CD", 0.75f),
    ImrTableColumn("name", "製品名", 1.55f),
    ImrTableColumn("qty", "生産数", 0.7f),
    ImrTableColumn("defect", "不良", 0.55f),
    ImrTableColumn("eff", "能率", 0.55f),
    ImrTableColumn("start", "開始", 1.15f),
    ImrTableColumn("end", "終了", 1.15f),
    ImrTableColumn("break", "休憩", 0.55f),
    ImrTableColumn("stop", "停止", 0.55f),
    ImrTableColumn("source", "取得元", 0.7f),
    ImrTableColumn("note", "備考", 0.95f),
)

private val ImrFieldTextStyle = TextStyle(
    fontSize = 13.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.Center,
)

@Composable
private fun ImrCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 2,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    textAlign: TextAlign = if (singleLine) TextAlign.Center else TextAlign.Start,
) {
    val fieldHeight = if (singleLine) ImrMetrics.ControlHeight else ImrMetrics.ControlHeightMulti
    val borderColor = when {
        !enabled -> Color(0xFFE8EDF3)
        readOnly -> Color(0xFFE2E8F0)
        else -> Color(0xFFE2E8F0)
    }
    val bgColor = when {
        !enabled && !readOnly -> Color(0xFFF8FAFC)
        readOnly -> Color(0xFFFCFCFD)
        else -> Color.White
    }
    val textColor = if (enabled || readOnly) ImrTheme.TextPrimary else ImrTheme.TextMuted
    val fieldTextStyle = ImrFieldTextStyle.copy(
        textAlign = textAlign,
        color = textColor,
    )
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(fieldHeight)
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .border(1.dp, borderColor, RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(bgColor)
            .then(
                if (onFocusChanged != null) {
                    Modifier.onFocusChanged { onFocusChanged(it.isFocused) }
                } else {
                    Modifier
                },
            ),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = fieldTextStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(ImrTheme.Indigo),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(Modifier.width(4.dp))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (singleLine) Alignment.Center else Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                        Text(
                            text = placeholder,
                            fontSize = 12.sp,
                            color = ImrTheme.TextMuted,
                            textAlign = textAlign,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (singleLine) Alignment.Center else Alignment.TopStart,
                    ) {
                        innerTextField()
                    }
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(2.dp))
                    trailingIcon()
                }
            }
        },
    )
}

private data class IarChipStyle(val bg: Color, val fg: Color, val border: Color)

private fun iarChipStyle(step: Int): IarChipStyle = when (step) {
    1 -> IarChipStyle(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Color(0xFFBFDBFE))
    2 -> IarChipStyle(Color(0xFFF5F3FF), Color(0xFF6D28D9), Color(0xFFDDD6FE))
    3 -> IarChipStyle(Color(0xFFF0FDFA), Color(0xFF0F766E), Color(0xFF99F6E4))
    4 -> IarChipStyle(Color(0xFFFFFBEB), Color(0xFFB45309), Color(0xFFFDE68A))
    5 -> IarChipStyle(Color(0xFFEEF2FF), Color(0xFF4338CA), Color(0xFFC7D2FE))
    6 -> IarChipStyle(Color(0xFFFFF1F2), Color(0xFFBE123C), Color(0xFFFECDD3))
    else -> IarChipStyle(Color(0xFFF8FAFC), Color(0xFF475569), Color(0xFFE2E8F0))
}

private data class DefectGroupStyle(val bar: Color, val headBg: Brush, val nameColor: Color)

private fun defectGroupStyle(index: Int): DefectGroupStyle = when (index % 3) {
    0 -> DefectGroupStyle(
        bar = Color(0xFFF59E0B),
        headBg = Brush.horizontalGradient(listOf(Color(0xD9FFFBEB), Color(0x80FFFFFF))),
        nameColor = Color(0xFFB45309),
    )
    1 -> DefectGroupStyle(
        bar = Color(0xFF6366F1),
        headBg = Brush.horizontalGradient(listOf(Color(0xD9EEF2FF), Color(0x80FFFFFF))),
        nameColor = Color(0xFF4338CA),
    )
    else -> DefectGroupStyle(
        bar = Color(0xFF14B8A6),
        headBg = Brush.horizontalGradient(listOf(Color(0xD9F0FDFA), Color(0x80FFFFFF))),
        nameColor = Color(0xFF0F766E),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun InspectionManualRegistrationBody(
    uiState: InspectionManualRegistrationUiState,
    canSave: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onRefresh: () -> Unit,
    onInspectorSelected: (Int?) -> Unit,
    onProductSelected: (String) -> Unit,
    onBoxQty: (String) -> Unit,
    onPieceQty: (String) -> Unit,
    onStartedAt: (String) -> Unit,
    onEndedAt: (String) -> Unit,
    onStartedBlur: () -> Unit,
    onEndedBlur: () -> Unit,
    onBreakMin: (String) -> Unit,
    onStopMin: (String) -> Unit,
    onNote: (String) -> Unit,
    onBumpDefect: (String, Int) -> Unit,
    onDefectQty: (String, String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onInspectorFilter: (Int?) -> Unit,
    onListPagePrev: () -> Unit,
    onListPageNext: () -> Unit,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IarBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ImrMetrics.PagePadH, vertical = ImrMetrics.PagePadV),
            verticalArrangement = Arrangement.spacedBy(ImrMetrics.SectionGap),
        ) {
            IarHeroSection()
            IarFormPanel(
                uiState = uiState,
                canSave = canSave,
                onPrevDay = onPrevDay,
                onNextDay = onNextDay,
                onToday = onToday,
                onInspectorSelected = onInspectorSelected,
                onProductSelected = onProductSelected,
                onBoxQty = onBoxQty,
                onPieceQty = onPieceQty,
                onStartedAt = onStartedAt,
                onEndedAt = onEndedAt,
                onStartedBlur = onStartedBlur,
                onEndedBlur = onEndedBlur,
                onBreakMin = onBreakMin,
                onStopMin = onStopMin,
                onNote = onNote,
                onBumpDefect = onBumpDefect,
                onDefectQty = onDefectQty,
                onSave = onSave,
                onClear = onClear,
            )
            IarTablePanel(
                uiState = uiState,
                canEdit = canEdit,
                canDelete = canDelete,
                onRefresh = onRefresh,
                onInspectorFilter = onInspectorFilter,
                onListPagePrev = onListPagePrev,
                onListPageNext = onListPageNext,
                onEditRow = onEditRow,
                onDeleteRow = onDeleteRow,
                inspectorLabel = inspectorLabel,
                canEditRow = canEditRow,
            )
        }
    }
}

@Composable
private fun IarBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = 20.dp)
                .background(Color(0x663B82F6), CircleShape)
                .alpha(0.35f),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 80.dp)
                .background(Color(0x668B5CF6), CircleShape)
                .alpha(0.28f),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-20).dp, y = 120.dp)
                .background(Color(0x6614B8A6), CircleShape)
                .alpha(0.22f),
        )
    }
}

@Composable
private fun IarHeroSection() {
    val chipScroll = rememberScrollState()
    Surface(
        shape = RoundedCornerShape(ImrMetrics.Radius),
        color = Color(0xF8FFFFFF),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2E94A3B8)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = ImrMetrics.SectionPadH, vertical = ImrMetrics.InnerGap),
            verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.ShowChart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "MES · 実績収集登録",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6366F1),
                        letterSpacing = 0.4.sp,
                    )
                    Text(
                        "検査実績収集登録",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImrTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScroll),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IarStepChip("①", "生産日", 1)
                IarStepChip("②", "検査員", 2)
                IarStepChip("③", "製品", 3)
                IarStepChip("④", "生産数", 4)
                IarStepChip("⑤", "時間", 5)
                IarStepChip("⑥", "不良", 6)
                IarStepChip("⑦", "備考", 7)
            }
        }
    }
}

@Composable
private fun IarStepChip(step: String, label: String, stepIndex: Int) {
    val style = iarChipStyle(stepIndex)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(style.bg)
            .border(1.dp, style.border, RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(step, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = style.fg)
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = style.fg)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun IarFormPanel(
    uiState: InspectionManualRegistrationUiState,
    canSave: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onInspectorSelected: (Int?) -> Unit,
    onProductSelected: (String) -> Unit,
    onBoxQty: (String) -> Unit,
    onPieceQty: (String) -> Unit,
    onStartedAt: (String) -> Unit,
    onEndedAt: (String) -> Unit,
    onStartedBlur: () -> Unit,
    onEndedBlur: () -> Unit,
    onBreakMin: (String) -> Unit,
    onStopMin: (String) -> Unit,
    onNote: (String) -> Unit,
    onBumpDefect: (String, Int) -> Unit,
    onDefectQty: (String, String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    val borderColor = if (uiState.isEdit) ImrTheme.EditBorder else ImrTheme.Border
    Surface(
        shape = RoundedCornerShape(ImrMetrics.Radius),
        color = ImrTheme.Surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(if (uiState.isEdit) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFCFFFFFF), Color(0xEEF1F5F9))))
                    .padding(horizontal = ImrMetrics.SectionPadH, vertical = ImrMetrics.InnerGap),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (uiState.isEdit) "編集中 #${uiState.editingRowId}" else "実績入力",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ImrTheme.TextPrimary,
                    )
                    if (uiState.isEdit) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "編集",
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
                uiState.timeSummary.workMin?.let { work ->
                    Text(
                        "作業 ${InspectionManualRegistrationLogic.formatMinutesLabel(work)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4338CA),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))))
                            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            HorizontalDivider(color = ImrTheme.Border.copy(alpha = 0.5f))

            Column(
                modifier = Modifier.padding(horizontal = ImrMetrics.SectionPadH, vertical = ImrMetrics.SectionPadV),
                verticalArrangement = Arrangement.spacedBy(ImrMetrics.SectionGap),
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val tripleWide = maxWidth >= 480.dp
                    if (tripleWide) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
                        ) {
                            IarTripleField(stepIndex = 1, label = "生産日", accent = ImrTheme.C1, modifier = Modifier.weight(1f).fillMaxHeight()) {
                                IarProductionDayNav(uiState.productionDay, onPrevDay, onNextDay, onToday)
                            }
                            IarTripleField(stepIndex = 2, label = "検査員", accent = ImrTheme.C2, modifier = Modifier.weight(1f).fillMaxHeight()) {
                                ImrInspectorDropdown(
                                    inspectors = uiState.inspectors,
                                    selectedId = uiState.inspectorUserId,
                                    enabled = !uiState.isLoadingInspectors,
                                    onSelected = onInspectorSelected,
                                )
                            }
                            IarTripleField(stepIndex = 3, label = "製品名", accent = ImrTheme.C3, modifier = Modifier.weight(1f).fillMaxHeight()) {
                                if (uiState.isEdit) {
                                    ImrCompactField(
                                        value = uiState.productName.ifBlank { uiState.productCd },
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = true,
                                    )
                                } else {
                                    ImrProductDropdown(
                                        products = uiState.products,
                                        selectedCd = uiState.productCd,
                                        enabled = uiState.inspectorSelected && !uiState.isLoadingProducts,
                                        onSelected = onProductSelected,
                                    )
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap)) {
                            IarTripleField(stepIndex = 1, label = "生産日", accent = ImrTheme.C1) {
                                IarProductionDayNav(uiState.productionDay, onPrevDay, onNextDay, onToday)
                            }
                            IarTripleField(stepIndex = 2, label = "検査員", accent = ImrTheme.C2) {
                                ImrInspectorDropdown(
                                    inspectors = uiState.inspectors,
                                    selectedId = uiState.inspectorUserId,
                                    enabled = !uiState.isLoadingInspectors,
                                    onSelected = onInspectorSelected,
                                )
                            }
                            IarTripleField(stepIndex = 3, label = "製品名", accent = ImrTheme.C3) {
                                if (uiState.isEdit) {
                                    ImrCompactField(
                                        value = uiState.productName.ifBlank { uiState.productCd },
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = true,
                                    )
                                } else {
                                    ImrProductDropdown(
                                        products = uiState.products,
                                        selectedCd = uiState.productCd,
                                        enabled = uiState.inspectorSelected && !uiState.isLoadingProducts,
                                        onSelected = onProductSelected,
                                    )
                                }
                            }
                        }
                    }
                }

                if (!uiState.isEdit && !uiState.inspectorSelected) {
                    IarLockHint("② 検査員を選択すると、③ 製品名を入力できます")
                } else if (!uiState.productSelected) {
                    IarLockHint("③ 製品名を選択すると、生産数・時間・不良・備考を入力できます")
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (uiState.productSelected) 1f else 0.48f),
                    verticalArrangement = Arrangement.spacedBy(ImrMetrics.SectionGap),
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val qtyTimeWide = maxWidth >= 440.dp
                        if (qtyTimeWide) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
                            ) {
                                IarQtyBlock(uiState, onBoxQty, onPieceQty, Modifier.weight(0.4f).fillMaxHeight())
                                IarTimeBlock(
                                    uiState, onStartedAt, onEndedAt, onStartedBlur, onEndedBlur,
                                    onBreakMin, onStopMin, Modifier.weight(0.6f).fillMaxHeight(),
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap)) {
                                IarQtyBlock(uiState, onBoxQty, onPieceQty)
                                IarTimeBlock(
                                    uiState, onStartedAt, onEndedAt, onStartedBlur, onEndedBlur,
                                    onBreakMin, onStopMin,
                                )
                            }
                        }
                    }

                    if (uiState.defectGroups.isNotEmpty()) {
                        IarDefectsBlock(uiState, onBumpDefect, onDefectQty)
                    }

                    IarRemarksFooter(
                        note = uiState.registrationNote,
                        enabled = uiState.productSelected,
                        canSave = canSave,
                        isSaving = uiState.isSaving,
                        isEdit = uiState.isEdit,
                        onNote = onNote,
                        onSave = onSave,
                        onClear = onClear,
                    )
                }
            }
        }
    }
}

@Composable
private fun IarProductionDayNav(
    productionDay: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(ImrMetrics.ControlHeight),
    ) {
        ImrCompactField(
            value = productionDay,
            onValueChange = {},
            readOnly = true,
            enabled = true,
            modifier = Modifier.weight(1f),
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = ImrTheme.C1, modifier = Modifier.size(16.dp)) },
        )
        ImrIconCircleBtn(Icons.AutoMirrored.Filled.ArrowBack, "前日", onPrevDay)
        Box(
            modifier = Modifier
                .height(ImrMetrics.ControlHeight)
                .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(ImrMetrics.RadiusSm))
                .background(Color(0xFFEFF6FF))
                .clickable(onClick = onToday)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("今日", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
        }
        ImrIconCircleBtn(Icons.AutoMirrored.Filled.ArrowForward, "翌日", onNextDay)
    }
}

@Composable
private fun ImrIconCircleBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(ImrMetrics.IconBtn)
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, desc, modifier = Modifier.size(16.dp), tint = ImrTheme.TextMuted)
    }
}

@Composable
private fun IarTripleField(
    stepIndex: Int,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ImrMetrics.TripleMinH)
            .clip(RoundedCornerShape(ImrMetrics.Radius))
            .background(Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xE8F1F5F9))))
            .border(1.dp, Color(0x2494A3B8), RoundedCornerShape(ImrMetrics.Radius)),
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.4f)))))
        Column(
            Modifier
                .padding(horizontal = ImrMetrics.InnerGap, vertical = ImrMetrics.InnerGap)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IarFieldLabel(stepIndex = stepIndex, label = label)
            content()
        }
    }
}

@Composable
private fun IarFieldLabel(stepIndex: Int, label: String, trailing: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IarStepBadge(stepIndex)
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.Label)
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}

@Composable
private fun IarStepBadge(stepIndex: Int) {
    val accent = when (stepIndex) {
        1 -> ImrTheme.C1
        2 -> ImrTheme.C2
        3 -> ImrTheme.C3
        4 -> ImrTheme.C4
        5 -> ImrTheme.C5
        6 -> ImrTheme.C6
        else -> ImrTheme.C7
    }
    val step = listOf("①", "②", "③", "④", "⑤", "⑥", "⑦")[stepIndex - 1]
    Box(
        modifier = Modifier
            .size(ImrMetrics.StepBadge)
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.82f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(step, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun IarQtyBlock(
    uiState: InspectionManualRegistrationUiState,
    onBoxQty: (String) -> Unit,
    onPieceQty: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val derivedBox = uiState.qtyInputSource == "piece"
    val derivedPiece = uiState.qtyInputSource == "box"
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ImrMetrics.Radius))
            .background(Brush.linearGradient(listOf(Color(0xF5FFFBEB), Color(0x66FEF3C7))))
            .border(1.dp, Color(0x40F59E0B), RoundedCornerShape(ImrMetrics.Radius))
            .padding(horizontal = ImrMetrics.InnerGap, vertical = ImrMetrics.InnerGap),
        verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
    ) {
        IarFieldLabel(
            stepIndex = 4,
            label = "生産数",
            trailing = {
                if (uiState.productCd.isNotBlank()) {
                    Text(
                        if (uiState.unitPerBox > 0) "入数 ${uiState.unitPerBox} 本/箱" else "入数未設定（本数を入力）",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.unitPerBox > 0) Color(0xFFB45309) else Color(0xFFDC2626),
                    )
                }
            },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
            modifier = Modifier.fillMaxWidth().height(ImrMetrics.ControlHeight + 14.dp),
        ) {
            ImrQtyCell(
                label = "箱数",
                value = uiState.boxQtyText,
                enabled = uiState.unitPerBox > 0 && uiState.productSelected,
                derived = derivedBox,
                onChange = onBoxQty,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp),
            ) {
                Text("×", color = ImrTheme.TextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(
                    if (uiState.unitPerBox > 0) uiState.unitPerBox.toString() else "—",
                    fontWeight = FontWeight.ExtraBold,
                    color = ImrTheme.C4,
                    fontSize = 13.sp,
                )
                Text("=", color = ImrTheme.TextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            ImrQtyCell(
                label = "本数",
                value = uiState.pieceQtyText,
                enabled = uiState.productSelected,
                derived = derivedPiece,
                onChange = onPieceQty,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        uiState.qtyMismatch?.let { mm ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "本数 ${mm.piece} は入数 ${mm.upb} で割り切れません。登録時に確認します。",
                    fontSize = 10.sp,
                    color = Color(0xFFB45309),
                    lineHeight = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun IarTimeBlock(
    uiState: InspectionManualRegistrationUiState,
    onStartedAt: (String) -> Unit,
    onEndedAt: (String) -> Unit,
    onStartedBlur: () -> Unit,
    onEndedBlur: () -> Unit,
    onBreakMin: (String) -> Unit,
    onStopMin: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ImrMetrics.Radius))
            .background(Brush.linearGradient(listOf(Color(0xF0EEF2FF), Color(0x66E0E7FF))))
            .border(1.dp, Color(0x336366F1), RoundedCornerShape(ImrMetrics.Radius))
            .padding(horizontal = ImrMetrics.InnerGap, vertical = ImrMetrics.InnerGap),
        verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IarStepBadge(5)
            Spacer(Modifier.width(5.dp))
            Text("生産時間", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.Label)
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Default.AccessTime, null, tint = ImrTheme.C5, modifier = Modifier.size(13.dp))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().height(ImrMetrics.TimeCellH),
        ) {
            ImrTimeField("開始", uiState.startedAtText, uiState.productSelected, onStartedAt, onStartedBlur, Modifier.weight(1.15f).fillMaxHeight(), Color(0xFF2563EB))
            ImrTimeField("終了", uiState.endedAtText, uiState.productSelected, onEndedAt, onEndedBlur, Modifier.weight(1.15f).fillMaxHeight(), Color(0xFF7C3AED))
            ImrMinuteField("休憩", uiState.breakMinText, uiState.productSelected, onBreakMin, Modifier.width(ImrMetrics.MinuteFieldCellW).fillMaxHeight(), Color(0xFF0D9488))
            ImrMinuteField("停止", uiState.stopMinText, uiState.productSelected, onStopMin, Modifier.width(ImrMetrics.MinuteFieldCellW).fillMaxHeight(), Color(0xFFE11D48))
        }
        uiState.timeSummary.shiftMin?.let { shift ->
            Text(
                buildString {
                    append("シフト ${InspectionManualRegistrationLogic.formatMinutesLabel(shift)}")
                    if (uiState.timeSummary.endsNextDay) append("（終了は翌日）")
                    append(" · 休憩 ${uiState.timeSummary.breakMin}分 · 停止 ${uiState.timeSummary.stopMin}分")
                },
                fontSize = 10.sp,
                color = Color(0xFF4338CA),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
                    .background(Brush.horizontalGradient(listOf(Color(0xE6EEF2FF), Color(0x80E0E7FF))))
                    .border(1.dp, Color(0x99C7D2FE), RoundedCornerShape(ImrMetrics.RadiusSm))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun IarDefectsBlock(
    uiState: InspectionManualRegistrationUiState,
    onBumpDefect: (String, Int) -> Unit,
    onDefectQty: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ImrMetrics.Radius))
            .background(Brush.linearGradient(listOf(Color(0x40FFF1F2), Color(0xE6F8FAFC))))
            .border(1.dp, Color(0x20F43F5E), RoundedCornerShape(ImrMetrics.Radius))
            .padding(horizontal = ImrMetrics.InnerGap, vertical = ImrMetrics.InnerGap),
        verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IarStepBadge(6)
            Spacer(Modifier.width(6.dp))
            Text("不良（項目別）", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.Label)
            if (uiState.totalDefects > 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    uiState.totalDefects.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48))))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
                .background(Brush.verticalGradient(listOf(Color(0xE0FFFFFF), Color(0xF3F4F6))))
                .border(1.dp, Color(0x99CBD5E1), RoundedCornerShape(ImrMetrics.RadiusSm))
                .padding(horizontal = ImrMetrics.InnerGap, vertical = ImrMetrics.InnerGap),
            verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
        ) {
            uiState.defectGroups.forEachIndexed { index, group ->
                val style = defectGroupStyle(index)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(style.headBg)
                        .padding(start = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(24.dp)
                            .background(style.bar),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("帰属工程", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ImrTheme.TextMuted)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        group.processName.ifBlank { group.processCd.ifBlank { "—" } },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = style.nameColor,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    group.items.forEach { item ->
                        val defectCd = item.defectCd?.trim().orEmpty()
                        if (defectCd.isEmpty()) return@forEach
                        ImrDefectCard(
                            label = item.defectName.orEmpty().ifEmpty { defectCd },
                            qty = uiState.defects[defectCd] ?: 0,
                            enabled = uiState.productSelected,
                            onBump = { onBumpDefect(defectCd, it) },
                            onQty = { onDefectQty(defectCd, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IarRemarksFooter(
    note: String,
    enabled: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    isEdit: Boolean,
    onNote: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 420.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IarStepBadge(7)
                    Spacer(Modifier.width(6.dp))
                    Text("備考", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.Label)
                }
                ImrCompactField(
                    value = note,
                    onValueChange = onNote,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "備考（任意）",
                    singleLine = false,
                    maxLines = 2,
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    IarFormActionButtons(canSave, enabled, isSaving, isEdit, onSave, onClear)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.widthIn(min = 64.dp)) {
                    IarStepBadge(7)
                    Spacer(Modifier.width(6.dp))
                    Text("備考", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.Label)
                }
                ImrCompactField(
                    value = note,
                    onValueChange = onNote,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    placeholder = "備考（任意）",
                    singleLine = false,
                    maxLines = 2,
                )
                IarFormActionButtons(canSave, enabled, isSaving, isEdit, onSave, onClear)
            }
        }
    }
}

@Composable
private fun IarFormActionButtons(
    canSave: Boolean,
    enabled: Boolean,
    isSaving: Boolean,
    isEdit: Boolean,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    if (canSave) {
        Button(
            onClick = onSave,
            enabled = enabled && !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = ImrTheme.Indigo),
            modifier = Modifier.height(ImrMetrics.ControlHeight),
            contentPadding = PaddingValues(horizontal = 14.dp),
            shape = RoundedCornerShape(ImrMetrics.RadiusSm),
        ) {
            if (isSaving) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(if (isEdit) "更新" else "保存", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
    OutlinedButton(
        onClick = onClear,
        modifier = Modifier.height(ImrMetrics.ControlHeight),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(ImrMetrics.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
    ) {
        Text("クリア", color = ImrTheme.TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun IarLockHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(Brush.linearGradient(listOf(Color(0xF2F8FAFC), Color(0xE0F1F5F9))))
            .border(1.dp, Color(0x5594A3B8), RoundedCornerShape(ImrMetrics.RadiusSm))
            .padding(horizontal = ImrMetrics.InnerGap, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Info, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), lineHeight = 14.sp)
    }
}

@Composable
private fun ImrQtyCell(
    label: String,
    value: String,
    enabled: Boolean,
    derived: Boolean,
    onChange: (String) -> Unit,
    modifier: Modifier,
) {
    val bg = if (derived) Color(0xE6F8FAFC) else Color(0xEBFFFFFF)
    val border = if (derived) Color(0x7394A3B8) else Color(0x47F59E0B)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(ImrMetrics.RadiusSm))
            .padding(horizontal = ImrMetrics.InnerGap, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
        ImrCompactField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImrInspectorDropdown(
    inspectors: List<UserListItemDto>,
    selectedId: Int?,
    enabled: Boolean,
    onSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = inspectors.find { it.id == selectedId }?.displayLabel().orEmpty()
    val unselected = label.isBlank()
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        ImrCompactField(
            value = if (unselected) "" else label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = if (unselected) "選択" else null,
            leadingIcon = { Icon(Icons.Default.Person, null, tint = ImrTheme.C2, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("（未選択）") }, onClick = { onSelected(null); expanded = false })
            inspectors.forEach { insp ->
                val id = insp.id ?: return@forEach
                DropdownMenuItem(text = { Text(insp.displayLabel()) }, onClick = { onSelected(id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImrProductDropdown(
    products: List<ErpProductDto>,
    selectedCd: String,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = products.find { it.normalizedCode() == selectedCd }
    val label = selected?.normalizedName().orEmpty()
    val unselected = label.isBlank()
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        ImrCompactField(
            value = if (unselected) "" else label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = if (unselected) "選択" else null,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            products.forEach { p ->
                val cd = p.normalizedCode()
                DropdownMenuItem(
                    text = {
                        Text(p.normalizedName(), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    },
                    onClick = { onSelected(cd); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ImrTimeField(
    label: String,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
    onBlur: () -> Unit,
    modifier: Modifier,
    labelColor: Color,
) {
    var wasFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xBFF8FAFC))))
            .border(1.dp, Color(0x2E94A3B8), RoundedCornerShape(ImrMetrics.RadiusSm))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.2.sp)
        ImrCompactField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            placeholder = "HH:MM",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onFocusChanged = { focused ->
                if (wasFocused && !focused) onBlur()
                wasFocused = focused
            },
        )
    }
}

@Composable
private fun ImrMinuteField(
    label: String,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
    modifier: Modifier,
    labelColor: Color,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xBFF8FAFC))))
            .border(1.dp, Color(0x2E94A3B8), RoundedCornerShape(ImrMetrics.RadiusSm))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = labelColor, letterSpacing = 0.2.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ImrCompactField(
                value = value,
                onValueChange = onChange,
                enabled = enabled,
                modifier = Modifier.width(ImrMetrics.MinuteInputW),
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Text("分", fontSize = 10.sp, color = ImrTheme.TextMuted, modifier = Modifier.padding(start = 2.dp))
        }
    }
}

@Composable
private fun ImrDefectCircleBtn(
    minus: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val size = 24.dp
    val shape = CircleShape
    val modifier = if (minus) {
        Modifier
            .size(size)
            .shadow(2.dp, shape, ambientColor = Color(0x140F172A), spotColor = Color(0x140F172A))
            .clip(shape)
            .background(Color.White, shape)
            .border(1.dp, Color(0xFFCBD5E1), shape)
    } else {
        Modifier
            .size(size)
            .shadow(4.dp, shape, ambientColor = Color(0x590EA5E9), spotColor = Color(0x590EA5E9))
            .clip(shape)
            .background(Color(0xFF38BDF8), shape)
    }
    Box(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (minus) Icons.Default.Remove else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (minus) Color(0xFF475569) else Color.White,
        )
    }
}

@Composable
private fun ImrDefectQtyField(
    qty: Int,
    enabled: Boolean,
    onQty: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = if (qty > 0) qty.toString() else ""
    val textColor = if (qty > 0) Color(0xFF111827) else Color(0xFF9CA3AF)
    BasicTextField(
        value = display,
        onValueChange = { onQty(it.filter { ch -> ch.isDigit() }) },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = textColor,
        ),
        cursorBrush = SolidColor(Color(0xFF38BDF8)),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .height(24.dp)
            .widthIn(min = 22.dp),
        decorationBox = { innerTextField ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (display.isEmpty()) {
                    Text(
                        "0",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                    )
                }
                innerTextField()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImrDefectCard(
    label: String,
    qty: Int,
    enabled: Boolean,
    onBump: (Int) -> Unit,
    onQty: (String) -> Unit,
) {
    val active = qty > 0
    Column(
        modifier = Modifier
            .width(ImrMetrics.DefectCardW)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (active) Color(0xFFF59E0B) else Color(0xD9CBD5E1),
                RoundedCornerShape(8.dp),
            )
            .background(if (active) Color(0xFFFFFBEB) else Color.White)
            .padding(horizontal = 5.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp,
            color = Color(0xFF1F2937),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ImrDefectCircleBtn(minus = true, enabled = enabled && qty > 0) { onBump(-1) }
            ImrDefectQtyField(
                qty = qty,
                enabled = enabled,
                onQty = onQty,
                modifier = Modifier.weight(1f),
            )
            ImrDefectCircleBtn(minus = false, enabled = enabled) { onBump(1) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IarTablePanel(
    uiState: InspectionManualRegistrationUiState,
    canEdit: Boolean,
    canDelete: Boolean,
    onRefresh: () -> Unit,
    onInspectorFilter: (Int?) -> Unit,
    onListPagePrev: () -> Unit,
    onListPageNext: () -> Unit,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tableCompact = maxWidth < ImrMetrics.TableCompactBreakpoint
        val headCompact = maxWidth < ImrMetrics.TablePanelHeadCompactBreakpoint
        Surface(
            shape = RoundedCornerShape(ImrMetrics.Radius),
            color = ImrTheme.Surface,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImrTheme.Border),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                if (headCompact) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color(0xFAFFFFFF), Color(0xBFF1F5F9))))
                            .padding(horizontal = ImrMetrics.SectionPadH, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ImrTablePanelTitle(uiState)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ImrInspectorFilter(
                                inspectors = uiState.inspectors,
                                selectedId = uiState.inspectorFilterId,
                                onSelected = onInspectorFilter,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedButton(
                                onClick = onRefresh,
                                modifier = Modifier.height(ImrMetrics.ControlHeight),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                shape = RoundedCornerShape(ImrMetrics.RadiusSm),
                            ) {
                                Icon(Icons.Default.Refresh, "更新", modifier = Modifier.size(14.dp), tint = ImrTheme.Indigo)
                                Spacer(Modifier.width(4.dp))
                                Text("更新", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color(0xFAFFFFFF), Color(0xBFF1F5F9))))
                            .padding(horizontal = ImrMetrics.SectionPadH, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ImrTablePanelTitle(uiState, modifier = Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ImrInspectorFilter(
                                inspectors = uiState.inspectors,
                                selectedId = uiState.inspectorFilterId,
                                onSelected = onInspectorFilter,
                                modifier = Modifier.widthIn(max = 160.dp),
                            )
                            OutlinedButton(
                                onClick = onRefresh,
                                modifier = Modifier.height(ImrMetrics.ControlHeight),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                shape = RoundedCornerShape(ImrMetrics.RadiusSm),
                            ) {
                                Icon(Icons.Default.Refresh, "更新", modifier = Modifier.size(14.dp), tint = ImrTheme.Indigo)
                                Spacer(Modifier.width(4.dp))
                                Text("更新", fontSize = 11.sp)
                            }
                        }
                    }
                }
                HorizontalDivider(color = ImrTheme.Border.copy(alpha = 0.5f))
                Column(Modifier.padding(horizontal = ImrMetrics.SectionPadH, vertical = ImrMetrics.InnerGap)) {
                    if (uiState.isLoadingRows) {
                        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ImrTheme.C3, modifier = Modifier.size(24.dp))
                        }
                    } else if (uiState.filteredRows.isEmpty()) {
                        Text("データがありません", color = ImrTheme.TextMuted, fontSize = 12.sp)
                    } else {
                        if (tableCompact) {
                            ImrRegistrationCardList(
                                rows = uiState.pagedFilteredRows,
                                editingRowId = uiState.editingRowId,
                                deletingRowId = uiState.deletingRowId,
                                canEdit = canEdit,
                                canDelete = canDelete,
                                inspectorLabel = inspectorLabel,
                                canEditRow = canEditRow,
                                onEditRow = onEditRow,
                                onDeleteRow = onDeleteRow,
                            )
                        } else {
                            ImrRegistrationTable(
                                rows = uiState.pagedFilteredRows,
                                editingRowId = uiState.editingRowId,
                                deletingRowId = uiState.deletingRowId,
                                canEdit = canEdit,
                                canDelete = canDelete,
                                inspectorLabel = inspectorLabel,
                                canEditRow = canEditRow,
                                onEditRow = onEditRow,
                                onDeleteRow = onDeleteRow,
                            )
                        }
                        ImrListPagination(
                            page = uiState.listPage,
                            totalPages = uiState.listTotalPages,
                            totalItems = uiState.filteredRows.size,
                            pageSize = InspectionManualRegistrationUiState.LIST_PAGE_SIZE,
                            onPrev = onListPagePrev,
                            onNext = onListPageNext,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImrTablePanelTitle(
    uiState: InspectionManualRegistrationUiState,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF0D9488)))),
        )
        Spacer(Modifier.width(8.dp))
        Text("登録一覧", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ImrTheme.TextPrimary)
        Spacer(Modifier.width(6.dp))
        Text(
            uiState.productionDay,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ImrTheme.TextMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "${uiState.filteredRows.size}件",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F766E),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFCCFBF1))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ImrListPagination(
    page: Int,
    totalPages: Int,
    totalItems: Int,
    pageSize: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    if (totalItems <= 0) return
    val start = (page - 1) * pageSize + 1
    val end = minOf(page * pageSize, totalItems)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ImrMetrics.InnerGap),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onPrev,
            enabled = page > 1,
            modifier = Modifier.height(30.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            shape = RoundedCornerShape(ImrMetrics.RadiusSm),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "前へ", modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "$start–$end / $totalItems 件 · $page / $totalPages ページ",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ImrTheme.TextMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
            onClick = onNext,
            enabled = page < totalPages,
            modifier = Modifier.height(30.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            shape = RoundedCornerShape(ImrMetrics.RadiusSm),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "次へ", modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImrInspectorFilter(
    inspectors: List<UserListItemDto>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedId?.let { id -> inspectors.find { it.id == id }?.displayLabel() } ?: "全検査員"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        ImrCompactField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("全検査員") }, onClick = { onSelected(null); expanded = false })
            inspectors.forEach { insp ->
                val id = insp.id ?: return@forEach
                DropdownMenuItem(text = { Text(insp.displayLabel()) }, onClick = { onSelected(id); expanded = false })
            }
        }
    }
}

@Composable
private fun ImrRegistrationTable(
    rows: List<InspectionManagementRowDto>,
    editingRowId: Int?,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.JAPAN) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImrTableColumns.forEach { col ->
                ImrTableCell(
                    text = col.label,
                    weight = col.weight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImrTheme.TextMuted,
                )
            }
            Spacer(Modifier.width(ImrMetrics.TableOpWidth))
        }
        HorizontalDivider()
        rows.forEach { row ->
            ImrRegistrationTableRow(
                row = row,
                nf = nf,
                editingRowId = editingRowId,
                deletingRowId = deletingRowId,
                canEdit = canEdit,
                canDelete = canDelete,
                inspectorLabel = inspectorLabel,
                canEditRow = canEditRow,
                onEditRow = onEditRow,
                onDeleteRow = onDeleteRow,
            )
            HorizontalDivider(color = ImrTheme.Border)
        }
    }
}

@Composable
private fun ImrRegistrationTableRow(
    row: InspectionManagementRowDto,
    nf: NumberFormat,
    editingRowId: Int?,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    val editable = canEdit && canEditRow(row)
    val active = row.id == editingRowId
    val inProgress = InspectionManualRegistrationLogic.isRowMesInProgress(row)
    val efficiency = InspectionManualRegistrationLogic.resolveEfficiencyRate(row)
    val effOut = InspectionManualRegistrationLogic.isEfficiencyOutOfRange(efficiency)
    val ds = InspectionManualRegistrationLogic.resolveDataSource(row)
    val dsLabel = InspectionManualRegistrationLogic.dataSourceLabel(ds)
    val (dsBg, dsFg) = dataSourceColors(ds)
    val cells = imrRowCellValues(row, nf, inspectorLabel, inProgress, efficiency)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) Color(0xFFEEF2FF) else Color.Transparent)
            .clickable(enabled = editable) { onEditRow(row) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ImrTableColumns.forEach { col ->
            when (col.key) {
                "source" -> {
                    Text(
                        dsLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dsFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(col.weight)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(dsBg)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
                "defect" -> ImrTableCell(
                    cells.defect,
                    col.weight,
                    color = if (InspectionManagementRowExt.historyDefectQty(row) > 0) ImrTheme.Rose else Color.Unspecified,
                )
                "eff" -> ImrTableCell(
                    cells.efficiency,
                    col.weight,
                    color = if (effOut) ImrTheme.Rose else Color.Unspecified,
                    fontWeight = if (effOut) FontWeight.Bold else FontWeight.Normal,
                )
                "name" -> ImrTableCell(cells.productName, col.weight, maxLines = 2)
                else -> ImrTableCell(
                    text = cells.valueFor(col.key),
                    weight = col.weight,
                    fontSize = if (col.key == "start" || col.key == "end") 9.sp else 10.sp,
                    maxLines = if (col.key == "inspector" || col.key == "note") 1 else 1,
                )
            }
        }
        ImrRowActions(
            editable = editable,
            inProgress = inProgress,
            canDelete = canDelete && canEditRow(row),
            deleting = deletingRowId == row.id,
            onEdit = { onEditRow(row) },
            onDelete = { onDeleteRow(row) },
        )
    }
}

@Composable
private fun ImrRegistrationCardList(
    rows: List<InspectionManagementRowDto>,
    editingRowId: Int?,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.JAPAN) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ImrMetrics.InnerGap),
    ) {
        rows.forEach { row ->
            ImrRegistrationCard(
                row = row,
                nf = nf,
                editingRowId = editingRowId,
                deletingRowId = deletingRowId,
                canEdit = canEdit,
                canDelete = canDelete,
                inspectorLabel = inspectorLabel,
                canEditRow = canEditRow,
                onEditRow = onEditRow,
                onDeleteRow = onDeleteRow,
            )
        }
    }
}

@Composable
private fun ImrRegistrationCard(
    row: InspectionManagementRowDto,
    nf: NumberFormat,
    editingRowId: Int?,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    inspectorLabel: (Int?) -> String,
    canEditRow: (InspectionManagementRowDto) -> Boolean,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    val editable = canEdit && canEditRow(row)
    val active = row.id == editingRowId
    val inProgress = InspectionManualRegistrationLogic.isRowMesInProgress(row)
    val efficiency = InspectionManualRegistrationLogic.resolveEfficiencyRate(row)
    val effOut = InspectionManualRegistrationLogic.isEfficiencyOutOfRange(efficiency)
    val ds = InspectionManualRegistrationLogic.resolveDataSource(row)
    val dsLabel = InspectionManualRegistrationLogic.dataSourceLabel(ds)
    val (dsBg, dsFg) = dataSourceColors(ds)
    val cells = imrRowCellValues(row, nf, inspectorLabel, inProgress, efficiency)
    val borderColor = if (active) ImrTheme.Indigo.copy(alpha = 0.45f) else ImrTheme.Border

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ImrMetrics.RadiusSm))
            .background(if (active) Color(0xFFEEF2FF) else Color(0xFAFCFD))
            .border(1.dp, borderColor, RoundedCornerShape(ImrMetrics.RadiusSm))
            .clickable(enabled = editable) { onEditRow(row) }
            .padding(ImrMetrics.InnerGap),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(cells.productionDay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImrTheme.TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text(cells.inspector, fontSize = 11.sp, color = ImrTheme.Label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ImrRowActions(
                editable = editable,
                inProgress = inProgress,
                canDelete = canDelete && canEditRow(row),
                deleting = deletingRowId == row.id,
                onEdit = { onEditRow(row) },
                onDelete = { onDeleteRow(row) },
            )
        }
        Text(
            "${cells.productCd} · ${cells.productName}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ImrTheme.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ImrCardMetric("生産", cells.qty, Modifier.weight(1f))
            ImrCardMetric(
                "不良",
                cells.defect,
                Modifier.weight(1f),
                valueColor = if (InspectionManagementRowExt.historyDefectQty(row) > 0) ImrTheme.Rose else ImrTheme.TextPrimary,
            )
            ImrCardMetric(
                "能率",
                cells.efficiency,
                Modifier.weight(1f),
                valueColor = if (effOut) ImrTheme.Rose else ImrTheme.TextPrimary,
                valueBold = effOut,
            )
        }
        Text(
            "${cells.started} → ${cells.ended} · 休憩 ${cells.breakMin} · 停止 ${cells.stopMin}",
            fontSize = 10.sp,
            color = ImrTheme.TextMuted,
            maxLines = 2,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                dsLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = dsFg,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(dsBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            if (cells.note != "—") {
                Text(
                    cells.note,
                    fontSize = 10.sp,
                    color = ImrTheme.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ImrCardMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = ImrTheme.TextPrimary,
    valueBold: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 9.sp, color = ImrTheme.TextMuted, fontWeight = FontWeight.SemiBold)
        Text(
            value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun ImrRowActions(
    editable: Boolean,
    inProgress: Boolean,
    canDelete: Boolean,
    deleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.width(ImrMetrics.TableOpWidth),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editable) {
            IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = ImrTheme.Indigo)
            }
        } else if (inProgress) {
            Text("—", fontSize = 10.sp, color = ImrTheme.TextMuted)
        }
        if (canDelete) {
            IconButton(onClick = onDelete, enabled = !deleting, modifier = Modifier.size(26.dp)) {
                if (deleting) {
                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp), tint = ImrTheme.Rose)
                }
            }
        }
    }
}

private data class ImrRowCells(
    val productionDay: String,
    val inspector: String,
    val productCd: String,
    val productName: String,
    val qty: String,
    val defect: String,
    val efficiency: String,
    val started: String,
    val ended: String,
    val breakMin: String,
    val stopMin: String,
    val note: String,
) {
    fun valueFor(key: String): String = when (key) {
        "day" -> productionDay
        "inspector" -> inspector
        "cd" -> productCd
        "name" -> productName
        "qty" -> qty
        "defect" -> defect
        "eff" -> efficiency
        "start" -> started
        "end" -> ended
        "break" -> breakMin
        "stop" -> stopMin
        "note" -> note
        else -> "—"
    }
}

private fun imrRowCellValues(
    row: InspectionManagementRowDto,
    nf: NumberFormat,
    inspectorLabel: (Int?) -> String,
    inProgress: Boolean,
    efficiency: Int?,
): ImrRowCells = ImrRowCells(
    productionDay = (row.productionDay ?: "—").take(10),
    inspector = inspectorLabel(row.mesInspectorUserId),
    productCd = row.productCd.orEmpty().ifBlank { "—" },
    productName = row.productName?.trim().orEmpty().ifBlank { "—" },
    qty = row.actualProductionQuantity?.let { nf.format(it) } ?: "—",
    defect = InspectionManagementRowExt.historyDefectQty(row).toString(),
    efficiency = if (inProgress) "—" else efficiency?.toString() ?: "—",
    started = InspectionManualRegistrationLogic.formatDateTimeShort(row.mesProductionStartedAt),
    ended = InspectionManualRegistrationLogic.formatDateTimeShort(row.mesProductionEndedAt),
    breakMin = InspectionManualRegistrationLogic.formatBreakMin(row),
    stopMin = InspectionManualRegistrationLogic.formatStopMin(row),
    note = row.manualRegistrationNote?.trim().orEmpty().ifBlank { "—" },
)

@Composable
private fun RowScope.ImrTableCell(
    text: String,
    weight: Float,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 2.dp),
    )
}

private fun dataSourceColors(source: String): Pair<Color, Color> = when (source) {
    "excel" -> Color(0xFFDCFCE7) to Color(0xFF166534)
    "csv" -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
    else -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
}
