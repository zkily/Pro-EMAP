package com.example.smart_emap.ui.mes.cuttinginstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
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

private val PlanBatchHeaders = listOf("→切断", "開始日", "ライン", "順位", "製品名", "計画数", "原材料", "ロット数", "No", "生産数", "材料区分", "材料使用数", "操作")
private val PlanBatchRowHeight = CuttingInstructionTheme.PlanBatchRowHeightDp.dp
private val PlanBatchBodyHeight = PlanBatchRowHeight * CuttingInstructionTheme.PlanBatchVisibleRows
private val LotCardToolbarButtonHeight = CuttingInstructionTheme.HeaderToolbarButtonHeightDp.dp
private val LotCardToolbarButtonShape = RoundedCornerShape(18.dp)

private fun planBatchColumnWidth(header: String) = when (header) {
    "→切断" -> 44.dp
    "製品名" -> 100.dp
    "操作" -> 56.dp
    else -> 56.dp
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlanLotFilterField(
                    label = "設備",
                    value = equipmentFilter,
                    emptyLabel = "設備を選択",
                    options = machineOptions,
                    onSelect = onEquipmentFilter,
                    modifier = Modifier.weight(1f),
                )
                PlanLotFilterField(
                    label = "製品",
                    value = productNameFilter,
                    emptyLabel = "全部",
                    options = productNameOptions.map { it to it },
                    onSelect = onProductNameFilter,
                    modifier = Modifier.weight(1f),
                )
                PlanLotFilterField(
                    label = "材料",
                    value = materialNameFilter,
                    emptyLabel = "全部",
                    options = materialNameOptions.map { it to it },
                    onSelect = onMaterialNameFilter,
                    modifier = Modifier.weight(1f),
                )
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
    value: String,
    emptyLabel: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CuttingInstructionTheme.Subtitle)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = CuttingInstructionTheme.Title),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        value.ifBlank { emptyLabel },
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text(emptyLabel) }, onClick = { onSelect(""); expanded = false })
                options.forEach { (cd, name) ->
                    DropdownMenuItem(text = { Text(name, fontSize = 12.sp) }, onClick = { onSelect(cd); expanded = false })
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
        BadgedBox(
            badge = {
                if (notesCount > 0) {
                    Badge(
                        modifier = Modifier.offset(x = (-2).dp, y = 2.dp),
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White,
                    ) {
                        Text(
                            if (notesCount > 99) "99+" else notesCount.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        ) {
            Icon(Icons.AutoMirrored.Filled.Note, contentDescription = "メモ（TODO）", modifier = Modifier.size(18.dp))
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
    content: @Composable () -> Unit,
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
            content()
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
) {
    val navHeight = CuttingInstructionTheme.InstructionDateNavHeightDp.dp
    val shape = RoundedCornerShape(6.dp)
    val backgroundBrush: Brush
    val borderColor: Color
    val textColor: Color
    when (style) {
        ChamferingHeaderActionStyle.New -> {
            backgroundBrush = Brush.verticalGradient(listOf(Color.White, CuttingInstructionTheme.ChamferHeaderBtnNewBg))
            borderColor = CuttingInstructionTheme.ChamferHeaderBtnNewBorder
            textColor = CuttingInstructionTheme.ChamferHeaderBtnNewText
        }
        ChamferingHeaderActionStyle.PlanPrint -> {
            backgroundBrush = Brush.horizontalGradient(listOf(CuttingInstructionTheme.ChamferHeaderBtnPlanStart, CuttingInstructionTheme.ChamferHeaderBtnPlanEnd))
            borderColor = CuttingInstructionTheme.ChamferHeaderBtnPlanEnd
            textColor = Color.White
        }
        ChamferingHeaderActionStyle.IssueSheet -> {
            backgroundBrush = Brush.horizontalGradient(listOf(CuttingInstructionTheme.ChamferHeaderBtnIssue, CuttingInstructionTheme.ChamferHeaderBtnIssueEnd))
            borderColor = CuttingInstructionTheme.ChamferHeaderBtnIssueEnd
            textColor = Color.White
        }
        ChamferingHeaderActionStyle.ConfirmActual -> {
            backgroundBrush = Brush.horizontalGradient(listOf(CuttingInstructionTheme.ChamferHeaderBtnConfirmStart, CuttingInstructionTheme.ChamferHeaderBtnConfirmEnd))
            borderColor = CuttingInstructionTheme.ChamferHeaderBtnConfirmEnd
            textColor = Color.White
        }
    }
    Box(
        modifier = modifier
            .height(navHeight)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
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
    val (containerColor, borderColor, contentColor) = when (style) {
        MachineChipStyle.Default -> Triple(
            if (active) Color.White else Color(0xFFF5F7FA),
            if (active) Color(0xFFC0C4CC) else CuttingInstructionTheme.ChipInactiveBorder,
            if (active) CuttingInstructionTheme.CuttingTitle else CuttingInstructionTheme.Subtitle,
        )
        MachineChipStyle.Chamfering -> Triple(
            if (active) Color.Transparent else Color.White,
            if (active) Color(0xFF059669) else Color(0xFFD1FAE5),
            if (active) Color.White else Color(0xFF047857),
        )
    }
    OutlinedButton(
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 2.dp),
        modifier = Modifier
            .height(height)
            .then(
                if (style == MachineChipStyle.Chamfering && active) {
                    Modifier.background(
                        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        shape,
                    )
                } else Modifier,
            ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Text(name, fontSize = fontSize, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun CuttingMgmtHeaderButton(text: String, bg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = Color.White),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
                    CuttingMgmtHeaderButton("実績確定", CuttingInstructionTheme.CuttingBtnConfirmSolid, onConfirmActual)
                }
            }
            InstructionMachineChips(
                machines = machineOptions,
                selected = selectedMachine,
                onSelect = onMachineSelect,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
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
            )
            CuttingMgmtSummaryFooter(rows)
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
private val CuttingTodayMinWidth = 839.dp
private val CuttingTomorrowMinWidth = 380.dp
private val CuttingMgmtBodyMinHeight = CuttingTodayRowHeight * CuttingInstructionTheme.CuttingTodayVisibleRows
private val CuttingMgmtBodyMaxHeight = (CuttingInstructionTheme.CuttingTableMaxHeightDp - CuttingInstructionTheme.CuttingMgmtRowHeightDp).dp
private val CuttingTodayBodyMinHeight = CuttingMgmtBodyMinHeight
private val CuttingTodayBodyMaxHeight = CuttingMgmtBodyMaxHeight

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
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tableShape)
            .border(1.dp, CuttingInstructionTheme.CuttingTableBorder, tableShape),
    ) {
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Column(Modifier.widthIn(min = CuttingTodayMinWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    listOf("CD", "ライン", "生産日", "切断機").forEach { h ->
                        CuttingTodayCellText(
                            h,
                            width = cuttingTodayColWidth(h),
                            fontWeight = FontWeight.ExtraBold,
                            align = CuttingCellAlign.Center,
                            color = CuttingInstructionTheme.CuttingTableHeaderText,
                        )
                    }
                    CuttingTodayCellText("製品名", weight = 1f, minWidth = 60.dp, fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Start, color = CuttingInstructionTheme.CuttingTableHeaderText)
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
                HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .heightIn(min = CuttingTodayBodyMinHeight, max = CuttingTodayBodyMaxHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingTodayBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.CuttingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingTodayBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> rows.forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = {}, onDoubleClick = { onEdit(row) })
                                .background(if (index % 2 == 1) Color(0xFFFAFAFF) else Color.White),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CuttingTodayCellText(row.cd ?: row.managementCode ?: "-", width = cuttingTodayColWidth("CD"))
                            CuttingTodayCellText(row.productionLine ?: "-", width = cuttingTodayColWidth("ライン"))
                            CuttingTodayCellText(formatInstructionDate(row.productionDay), width = cuttingTodayColWidth("生産日"))
                            CuttingTodayCellText(row.cuttingMachine ?: "-", width = cuttingTodayColWidth("切断機"))
                            CuttingTodayCellText(
                                row.productName ?: row.productCd ?: "-",
                                weight = 1f,
                                minWidth = 60.dp,
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
                                        IconButton(onClick = { onSplit(row) }, modifier = Modifier.size(22.dp)) {
                                            Icon(
                                                Icons.Default.KeyboardDoubleArrowRight,
                                                contentDescription = "順延",
                                                tint = Color(0xFFE6A23C),
                                                modifier = Modifier.size(15.dp),
                                            )
                                        }
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

private fun cuttingTodayColWidth(h: String) = when (h) {
    "CD" -> 42.dp
    "ライン" -> 46.dp
    "生産日" -> 75.dp
    "切断機" -> 46.dp
    "不良" -> 44.dp
    "完了" -> 45.dp
    "生産順" -> 46.dp
    "生産時間" -> 50.dp
    "備考" -> 75.dp
    "戻す" -> 44.dp
    else -> 46.dp
}

private fun cuttingTomorrowColWidth(h: String) = when (h) {
    "CD" -> 41.dp
    "生産日" -> 75.dp
    "切断機" -> 50.dp
    "生産数" -> 52.dp
    "生産順" -> 52.dp
    "操作" -> 44.dp
    else -> 46.dp
}

@Composable
private fun CuttingTomorrowTable(
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    onMoveBackToBatch: (InstructionCuttingRowDto) -> Unit,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tableShape)
            .border(1.dp, CuttingInstructionTheme.CuttingTableBorder, tableShape),
    ) {
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Column(Modifier.widthIn(min = CuttingTomorrowMinWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    listOf("CD", "生産日", "切断機").forEach { h ->
                        CuttingTodayCellText(
                            h,
                            width = cuttingTomorrowColWidth(h),
                            fontWeight = FontWeight.ExtraBold,
                            align = CuttingCellAlign.Center,
                            color = CuttingInstructionTheme.CuttingTableHeaderText,
                        )
                    }
                    CuttingTodayCellText("製品名", weight = 1f, minWidth = 90.dp, fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Start, color = CuttingInstructionTheme.CuttingTableHeaderText)
                    CuttingTodayCellText("生産数", width = cuttingTomorrowColWidth("生産数"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.End, color = CuttingInstructionTheme.CuttingTableHeaderText)
                    CuttingTodayCellText("生産順", width = cuttingTomorrowColWidth("生産順"), fontWeight = FontWeight.ExtraBold, align = CuttingCellAlign.Center, color = CuttingInstructionTheme.CuttingTableHeaderText)
                    CuttingTodayCellText("", width = cuttingTomorrowColWidth("操作"), fontWeight = FontWeight.ExtraBold, showRightBorder = false, color = CuttingInstructionTheme.CuttingTableHeaderText)
                }
                HorizontalDivider(color = CuttingInstructionTheme.CuttingTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .heightIn(min = CuttingMgmtBodyMinHeight, max = CuttingMgmtBodyMaxHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingMgmtBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.CuttingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingMgmtBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> rows.forEachIndexed { index, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 1) Color(0xFFFAFAFF) else Color.White),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CuttingTodayCellText(row.cd ?: row.managementCode ?: "-", width = cuttingTomorrowColWidth("CD"))
                                CuttingTodayCellText(formatInstructionDate(row.productionDay), width = cuttingTomorrowColWidth("生産日"))
                                CuttingTodayCellText(row.cuttingMachine ?: "-", width = cuttingTomorrowColWidth("切断機"))
                                CuttingTodayCellText(
                                    row.productName ?: row.productCd ?: "-",
                                    weight = 1f,
                                    minWidth = 90.dp,
                                    align = CuttingCellAlign.Start,
                                )
                                CuttingTodayCellText(row.actualProductionQuantity?.toString() ?: "-", width = cuttingTomorrowColWidth("生産数"), align = CuttingCellAlign.End)
                                CuttingTodayCellText(row.productionSequence?.toString() ?: "-", width = cuttingTomorrowColWidth("生産順"))
                                CuttingTodayCellBorder(width = cuttingTomorrowColWidth("操作"), showRightBorder = false) {
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
                            modifier = Modifier.width(planBatchColumnWidth(h)).padding(horizontal = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CuttingInstructionTheme.BatchTitle,
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
                                .combinedClickable(onClick = { onSelect(row) }, onLongClick = { onEdit(row) })
                                .background(
                                    when {
                                        selected -> CuttingInstructionTheme.RowSelected
                                        else -> CuttingInstructionTheme.planPriorityBg(row.priorityOrder)
                                    },
                                )
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.width(planBatchColumnWidth("→切断")),
                                contentAlignment = Alignment.Center,
                            ) {
                                TextButton(onClick = { onMoveToCutting(row) }, contentPadding = PaddingValues(0.dp)) {
                                    Text("→切断", fontSize = 9.sp, color = CuttingInstructionTheme.BatchAccent)
                                }
                            }
                            listOf(
                                formatInstructionDate(row.startDate),
                                row.productionLine.orEmpty().ifBlank { "-" },
                                row.priorityOrder?.toString() ?: "-",
                            ).forEach { t ->
                                Text(t, modifier = Modifier.width(56.dp).padding(horizontal = 2.dp), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(
                                row.productName ?: row.productCd ?: "-",
                                modifier = Modifier.width(100.dp).padding(horizontal = 2.dp),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            listOf(
                                row.plannedQuantity?.toString() ?: "-",
                                row.materialName ?: "-",
                                row.productionLotSize?.toString() ?: "-",
                                row.lotNumber ?: "-",
                                row.actualProductionQuantity?.toString() ?: "-",
                            ).forEach { t ->
                                Text(t, modifier = Modifier.width(56.dp).padding(horizontal = 2.dp), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Switch(
                                checked = row.useMaterialStockSub == 1,
                                onCheckedChange = { onToggleStock(row, it) },
                                modifier = Modifier.width(56.dp).padding(horizontal = 4.dp),
                            )
                            Text(formatUsageCount(row.usageCount), modifier = Modifier.width(56.dp).padding(horizontal = 2.dp), fontSize = 10.sp)
                            Row(Modifier.width(planBatchColumnWidth("操作")), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onEdit(row) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, "編集", tint = CuttingInstructionTheme.BatchAccent, modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { onDelete(row) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, "削除", tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
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
        )
        return
    }
    CuttingTomorrowTable(
        rows = rows,
        loading = loading,
        onMoveBackToBatch = onMoveBackToBatch,
    )
}

private val ChamferMgmtMinWidth = CuttingInstructionTheme.ChamferMgmtMinWidthDp.dp
private val ChamferMgmtTomorrowMinWidth = 504.dp
private val ChamferMgmtRowAltBg = Color(0x59ECFDF5)

private fun chamferTodayColWidth(h: String) = when (h) {
    "CD" -> 44.dp
    "ライン" -> 48.dp
    "成型予定日" -> 72.dp
    "生産日" -> 72.dp
    "面取機" -> 52.dp
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
    "生産数" -> 55.dp
    "不良" -> 44.dp
    "生産順" -> 45.dp
    "生産時間" -> 60.dp
    else -> 46.dp
}

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
fun ChamferMgmtSummaryFooter(rows: List<InstructionChamferingRowDto>) {
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
        ChamferMgmtFooterItem("不良合計", defect.toString())
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
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    val headerText = CuttingInstructionTheme.ChamferMgmtTableHeaderText
    Column(
        modifier = Modifier
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
                    listOf("生産日", "面取機").forEach { h ->
                        ChamferMgmtCellText(h, width = chamferTodayColWidth(h), fontWeight = FontWeight.ExtraBold, color = headerText)
                    }
                    ChamferMgmtCellText(
                        "製品名",
                        weight = 1f,
                        minWidth = 60.dp,
                        align = CuttingCellAlign.Start,
                        fontWeight = FontWeight.ExtraBold,
                        color = headerText,
                    )
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
                        .heightIn(min = CuttingTodayBodyMinHeight, max = CuttingTodayBodyMaxHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingTodayBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.ChamferingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingTodayBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("データなし", color = CuttingInstructionTheme.Subtitle, fontSize = 12.sp)
                        }
                        else -> rows.forEachIndexed { index, row ->
                            val mgmtCode = row.managementCode?.trim().orEmpty()
                            val formingDay = formatInstructionDate(formingStartDateByMgmtCode[mgmtCode])
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(onClick = {}, onDoubleClick = { onEdit(row) })
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
                                    row.productName ?: row.productCd ?: "-",
                                    weight = 1f,
                                    minWidth = 60.dp,
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
                                            IconButton(onClick = { onSplit(row) }, modifier = Modifier.size(22.dp)) {
                                                Icon(
                                                    Icons.Default.KeyboardDoubleArrowRight,
                                                    contentDescription = "順延",
                                                    tint = Color(0xFFE6A23C),
                                                    modifier = Modifier.size(15.dp),
                                                )
                                            }
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
            ChamferMgmtSummaryFooter(rows)
        }
    }
}

@Composable
private fun ChamferingTomorrowTable(
    rows: List<InstructionChamferingRowDto>,
    loading: Boolean,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val tableShape = RoundedCornerShape(6.dp)
    val headerText = CuttingInstructionTheme.ChamferMgmtTableHeaderText
    Column(
        modifier = Modifier
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
                    listOf("CD", "生産日", "面取機").forEach { h ->
                        ChamferMgmtCellText(h, width = chamferTomorrowColWidth(h), fontWeight = FontWeight.ExtraBold, color = headerText)
                    }
                    ChamferMgmtCellText(
                        "製品名",
                        weight = 1f,
                        minWidth = 90.dp,
                        align = CuttingCellAlign.Start,
                        fontWeight = FontWeight.ExtraBold,
                        color = headerText,
                    )
                    listOf("生産数", "不良", "生産順", "生産時間").forEach { h ->
                        ChamferMgmtCellText(
                            h,
                            width = chamferTomorrowColWidth(h),
                            fontWeight = FontWeight.ExtraBold,
                            color = headerText,
                            align = if (h == "生産数") CuttingCellAlign.End else CuttingCellAlign.Center,
                            showRightBorder = h != "生産時間",
                        )
                    }
                }
                HorizontalDivider(color = CuttingInstructionTheme.ChamferMgmtTableBorder, thickness = 1.dp)
                Column(
                    modifier = Modifier
                        .heightIn(min = CuttingMgmtBodyMinHeight, max = CuttingMgmtBodyMaxHeight)
                        .verticalScroll(verticalScroll),
                ) {
                    when {
                        loading -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingMgmtBodyMinHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = CuttingInstructionTheme.ChamferingAccent,
                            )
                        }
                        rows.isEmpty() -> Box(
                            Modifier
                                .fillMaxWidth()
                                .height(CuttingMgmtBodyMinHeight),
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
                                    row.productName ?: row.productCd ?: "-",
                                    weight = 1f,
                                    minWidth = 90.dp,
                                    align = CuttingCellAlign.Start,
                                )
                                ChamferMgmtCellText(
                                    row.actualProductionQuantity?.toString() ?: "-",
                                    width = chamferTomorrowColWidth("生産数"),
                                    align = CuttingCellAlign.End,
                                )
                                ChamferMgmtCellText(row.defectQty?.toString() ?: "-", width = chamferTomorrowColWidth("不良"))
                                ChamferMgmtCellText(row.productionSequence?.toString() ?: "-", width = chamferTomorrowColWidth("生産順"))
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
            ChamferMgmtSummaryFooter(rows)
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
    compact: Boolean = false,
) {
    if (compact) {
        ChamferingTomorrowTable(rows = rows, loading = loading)
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
        )
    }
}

private val ChamferPlanRowHeight = PlanBatchRowHeight
private val ChamferPlanBodyHeight = PlanBatchBodyHeight
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

@Composable
fun UsageSummaryTable(
    rows: List<InstructionCuttingRowDto>,
    loading: Boolean,
    reflectedCodes: Set<String>,
    onToggleStock: (InstructionCuttingRowDto, Boolean) -> Unit,
    onEditUsage: (InstructionCuttingRowDto) -> Unit,
) {
    if (loading) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
        return
    }
    if (rows.isEmpty()) {
        Text("該当日のデータがありません", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)
        return
    }
    val scroll = rememberScrollState()
    Column(Modifier.horizontalScroll(scroll)) {
        Row(Modifier.background(CuttingInstructionTheme.TableHeaderBg).padding(vertical = 6.dp)) {
            listOf("製品名", "材料", "在庫区分", "使用数", "反映").forEach { h ->
                Text(h, Modifier.width(if (h == "製品名") 88.dp else 56.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.TableHeaderText)
            }
        }
        rows.forEachIndexed { index, row ->
            val reflected = row.materialUsageReflected == "反映済" || reflectedCodes.contains(row.managementCode.orEmpty().trim())
            Row(
                Modifier
                    .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(row.productName ?: "-", Modifier.width(88.dp), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.materialName ?: "-", Modifier.width(56.dp), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Switch(checked = row.useMaterialStockSub == 1, onCheckedChange = { onToggleStock(row, it) }, modifier = Modifier.width(48.dp))
                Text(formatUsageCount(row.usageCount), Modifier.width(56.dp).clickable { onEditUsage(row) }, fontSize = 10.sp)
                Text(if (reflected) "済" else "未", Modifier.width(56.dp), fontSize = 10.sp, color = if (reflected) CuttingInstructionTheme.ChamferingAccent else Color(0xFFDC2626))
            }
            HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun KanbanTable(
    rows: List<KanbanIssuanceRowDto>,
    selectedIds: Set<Int>,
    loading: Boolean,
    onToggleSelect: (Int, Boolean) -> Unit,
    onIssue: (Int) -> Unit,
    onReissue: (Int) -> Unit = {},
    onEdit: (KanbanIssuanceRowDto) -> Unit = {},
) {
    if (loading) {
        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }
        return
    }
    if (rows.isEmpty()) {
        Text("データなし", modifier = Modifier.padding(12.dp), color = CuttingInstructionTheme.Subtitle, fontSize = 11.sp)
        return
    }
    val scroll = rememberScrollState()
    Column(Modifier.horizontalScroll(scroll)) {
        Row(Modifier.background(CuttingInstructionTheme.KanbanTableHeaderBg).padding(vertical = 6.dp)) {
            listOf("", "状態", "工程", "カンバンNo", "製品名", "ライン", "切断機", "材料", "管理CD", "生産数", "生産日", "操作").forEach { h ->
                Text(h, Modifier.width(wKanban(h)), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CuttingInstructionTheme.KanbanTitle)
            }
        }
        rows.forEachIndexed { index, row ->
            val id = row.id ?: return@forEachIndexed
            Row(
                Modifier
                    .background(if (index % 2 == 1) CuttingInstructionTheme.TableRowAlt else Color.White)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(checked = selectedIds.contains(id), onCheckedChange = { onToggleSelect(id, it) }, modifier = Modifier.width(36.dp))
                Text(row.status ?: "-", Modifier.width(wKanban("状態")), fontSize = 9.sp)
                Text(row.processType ?: "-", Modifier.width(wKanban("工程")), fontSize = 9.sp)
                Text(row.kanbanNo ?: "-", Modifier.width(wKanban("カンバンNo")), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.productName ?: "-", Modifier.width(wKanban("製品名")), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.productionLine ?: "-", Modifier.width(wKanban("ライン")), fontSize = 9.sp)
                Text(row.cuttingMachine ?: "-", Modifier.width(wKanban("切断機")), fontSize = 9.sp)
                Text(row.materialName ?: "-", Modifier.width(wKanban("材料")), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.managementCode ?: "-", Modifier.width(wKanban("管理CD")), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.actualProductionQuantity?.toString() ?: "-", Modifier.width(wKanban("生産数")), fontSize = 9.sp)
                Text(formatInstructionDate(row.productionDay), Modifier.width(wKanban("生産日")), fontSize = 9.sp)
                Row(Modifier.width(wKanban("操作"))) {
                    if (row.status == "pending") {
                        TextButton(onClick = { onIssue(id) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("発行", fontSize = 8.sp) }
                    } else {
                        TextButton(onClick = { onReissue(id) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("再発", fontSize = 8.sp) }
                    }
                    TextButton(onClick = { onEdit(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("編", fontSize = 8.sp) }
                }
            }
            HorizontalDivider(color = CuttingInstructionTheme.TableBorder.copy(alpha = 0.4f))
        }
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
                "生産ロット" to (detail.lotSize?.toString() ?: "-"),
                "材料" to (detail.materialName ?: detail.materialCd ?: "-"),
                "取数" to (detail.takeCount?.toString() ?: "-"),
                "切断長" to (detail.cutLength?.toString() ?: "-"),
                "面取長" to (detail.chamferLength?.toString()?.takeIf { it != "0.0" } ?: "--"),
                "展開長" to (detail.developedLength?.toString()?.takeIf { it != "0.0" } ?: "--"),
                "端材長" to (detail.scrapLength?.toString() ?: "-"),
            )
            items.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)
                    Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = CuttingInstructionTheme.Title, textAlign = TextAlign.End, modifier = Modifier.widthIn(max = 140.dp))
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

private fun wKanban(h: String) = when (h) {
    "製品名", "管理CD" -> 72.dp
    "カンバンNo" -> 64.dp
    "操作" -> 52.dp
    "" -> 36.dp
    else -> 48.dp
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
