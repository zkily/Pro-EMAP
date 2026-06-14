package com.example.smart_emap.ui.master.equipmentefficiency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.EquipmentEfficiencyTabCountsDto
import kotlin.math.ceil

@Composable
fun EePageBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(EeTheme.PageBg)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Brush.radialGradient(listOf(Color(0x265B5EA6), Color.Transparent), radius = 500f)),
        )
        content()
    }
}

@Composable
fun EeWorkspace(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, EeTheme.PanelShape, ambientColor = Color(0x0F0F172A), spotColor = Color(0x180F172A)),
        shape = EeTheme.PanelShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x0F000000)),
    ) {
        content()
    }
}

@Composable
fun EquipmentEfficiencyHeroBar(
    tabCountsAll: Int,
    machineCount: Int,
    productCount: Int,
    layout: EeLayoutMode,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(300)) + slideInVertically(tween(340)) { it / 4 }) {
        Card(
            modifier = Modifier.fillMaxWidth().shadow(10.dp, EeTheme.CardShape, spotColor = Color(0x405B5EA6)),
            shape = EeTheme.CardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(3.dp).background(EeTheme.HeroAccentBar))
                if (layout == EeLayoutMode.Compact) {
                    Column(
                        Modifier.fillMaxWidth().background(EeTheme.HeroBg).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EeHeroBrand()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EeHeroStat("$tabCountsAll", "設定数", Modifier.weight(1f))
                            EeHeroStat("$machineCount", "設備数", Modifier.weight(1f))
                            EeHeroStat("$productCount", "製品数", Modifier.weight(1f))
                        }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().background(EeTheme.HeroBg).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        EeHeroBrand(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EeHeroStat("$tabCountsAll", "設定数")
                            EeHeroStat("$machineCount", "設備数")
                            EeHeroStat("$productCount", "製品数")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EeHeroBrand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.size(34.dp).clip(EeTheme.ChipShape)
                .background(Color.White.copy(alpha = 0.18f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), EeTheme.ChipShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Build, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Column {
            Text("設備能率管理", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, lineHeight = 18.sp)
            Text("設備ごとの加工製品別能率設定・管理", fontSize = 9.sp, color = Color.White.copy(alpha = 0.82f), lineHeight = 11.sp)
        }
    }
}

@Composable
private fun EeHeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = EeTheme.TabShape,
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, lineHeight = 16.sp)
            Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.85f), letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun EquipmentEfficiencyToolbar(
    keyword: String,
    loading: Boolean,
    canCreate: Boolean,
    layout: EeLayoutMode,
    onKeywordChange: (String) -> Unit,
    onClear: () -> Unit,
    onCreate: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(EeTheme.FilterBg)) {
        val pad = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        if (layout == EeLayoutMode.Compact) {
            Column(pad, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EeSearchField(keyword, onKeywordChange)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EeActionChip("クリア", Icons.Default.Refresh, !loading, onClear)
                    if (canCreate) EePrimaryChip("新規登録", Icons.Default.Add, !loading, onCreate)
                }
            }
        } else {
            Row(pad, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EeSearchField(keyword, onKeywordChange, Modifier.weight(1f))
                EeActionChip("クリア", Icons.Default.Refresh, !loading, onClear)
                if (canCreate) EePrimaryChip("新規登録", Icons.Default.Add, !loading, onCreate)
            }
        }
        HorizontalDivider(color = EeTheme.Slate200, thickness = 0.5.dp)
    }
}

@Composable
private fun EeSearchField(keyword: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = EeTheme.FieldShape,
        color = Color.White,
        border = BorderStroke(1.dp, EeTheme.Slate200),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = EeTheme.Slate400, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = keyword,
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = EeTheme.Slate700),
                decorationBox = { inner ->
                    Box {
                        if (keyword.isEmpty()) {
                            Text("製品名・設備名で検索…", fontSize = 11.sp, color = EeTheme.Slate400)
                        }
                        inner()
                    }
                },
            )
        }
    }
}

@Composable
private fun EeActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "eeChip")
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = EeTheme.ChipShape,
        color = Color.White,
        border = BorderStroke(1.dp, EeTheme.Slate200),
        modifier = Modifier.scale(scale),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = EeTheme.Slate600, modifier = Modifier.size(12.dp))
            Text(label, fontSize = 10.sp, color = EeTheme.Slate700, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EePrimaryChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "eePrimary")
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = EeTheme.ChipShape,
        color = EeTheme.Violet600,
        modifier = Modifier.scale(scale),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EquipmentEfficiencyProcessTabs(
    activeTab: String,
    tabCounts: EquipmentEfficiencyTabCountsDto,
    onTabSelected: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .background(Color(0xFFF0F2F8))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EquipmentEfficiencyMasterLogic.processTabs.forEach { tab ->
            val count = EquipmentEfficiencyMasterLogic.tabCount(tabCounts, tab.value)
            val selected = activeTab == tab.value
            Surface(
                onClick = { onTabSelected(tab.value) },
                shape = EeTheme.PillShape,
                color = if (selected) EeTheme.Violet600 else Color.White,
                border = BorderStroke(1.dp, if (selected) EeTheme.Violet600 else EeTheme.Slate200),
            ) {
                Text(
                    "${tab.label} ($count)",
                    fontSize = 9.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else EeTheme.Slate600,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
fun EquipmentEfficiencyTableSection(
    rows: List<EquipmentEfficiencyUiRow>,
    loading: Boolean,
    total: Int,
    currentPage: Int,
    pageSize: Int,
    activeTab: String,
    statusUpdatingId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    layout: EeLayoutMode,
    modifier: Modifier = Modifier,
    onEdit: (EquipmentEfficiencyUiRow) -> Unit,
    onDelete: (Int) -> Unit,
    onStatusChange: (EquipmentEfficiencyUiRow, Boolean) -> Unit,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val rowOffset = (currentPage - 1) * pageSize
    Column(modifier) {
        Box(Modifier.weight(1f)) {
            when {
                loading && rows.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EeTheme.Violet600, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
                layout == EeLayoutMode.Compact -> EeCompactCardList(rowOffset, rows, canEdit, canDelete, statusUpdatingId, onEdit, onDelete, onStatusChange)
                else -> EeWideTable(rowOffset, rows, canEdit, canDelete, layout, statusUpdatingId, onEdit, onDelete, onStatusChange)
            }
            if (loading && rows.isNotEmpty()) {
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EeTheme.Violet600, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
            }
        }
        EeResultBar(
            shown = rows.size,
            total = total,
            activeTab = activeTab,
            currentPage = currentPage,
            pageSize = pageSize,
            onPageChange = onPageChange,
            onPageSizeChange = onPageSizeChange,
        )
    }
}

@Composable
private fun EeCompactCardList(
    rowOffset: Int,
    rows: List<EquipmentEfficiencyUiRow>,
    canEdit: Boolean,
    canDelete: Boolean,
    statusUpdatingId: Int?,
    onEdit: (EquipmentEfficiencyUiRow) -> Unit,
    onDelete: (Int) -> Unit,
    onStatusChange: (EquipmentEfficiencyUiRow, Boolean) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { index, row ->
            EeEfficiencyCard(rowOffset + index + 1, row, canEdit, canDelete, statusUpdatingId == row.id, onEdit, onDelete, onStatusChange)
        }
        if (rows.isEmpty()) {
            Text("データがありません", fontSize = 11.sp, color = EeTheme.Slate500, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun EeEfficiencyCard(
    displayIndex: Int,
    row: EquipmentEfficiencyUiRow,
    canEdit: Boolean,
    canDelete: Boolean,
    statusLoading: Boolean,
    onEdit: (EquipmentEfficiencyUiRow) -> Unit,
    onDelete: (Int) -> Unit,
    onStatusChange: (EquipmentEfficiencyUiRow, Boolean) -> Unit,
) {
    Surface(shape = EeTheme.PanelShape, color = Color.White, border = BorderStroke(1.dp, EeTheme.Slate200), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#$displayIndex", fontSize = 8.sp, color = EeTheme.Slate400, modifier = Modifier.padding(end = 6.dp))
                Column(Modifier.weight(1f)) {
                    Text(row.machinesName.ifBlank { row.machineCd }, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EeTheme.Violet600)
                    Text(row.productName.ifBlank { row.productCd }, fontSize = 10.sp, color = EeTheme.Slate700, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (canEdit) EeIconBtn(Icons.Default.Edit, Color(0xFFF5F3FF), EeTheme.Violet600) { onEdit(row) }
                    if (canDelete) EeIconBtn(Icons.Default.Delete, Color(0xFFFEF2F2), EeTheme.Rose) { onDelete(row.id) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EeMiniField("能率", EquipmentEfficiencyMasterLogic.formatEfficiency(row), EeTheme.ColEff)
                EeMiniField("段取", EquipmentEfficiencyMasterLogic.formatStepTime(row.stepTime), EeTheme.ColStep)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (statusLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = EeTheme.Violet600)
                } else {
                    Switch(
                        checked = row.status == 1,
                        onCheckedChange = { if (canEdit) onStatusChange(row, it) },
                        enabled = canEdit,
                        modifier = Modifier.scale(0.75f),
                        colors = SwitchDefaults.colors(checkedTrackColor = EeTheme.Violet600, checkedThumbColor = Color.White),
                    )
                }
                Text(
                    EquipmentEfficiencyMasterLogic.statusLabel(row.status),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (row.status == 1) EeTheme.Emerald else EeTheme.Slate500,
                )
                if (row.remarks.isNotBlank()) {
                    Text(row.remarks, fontSize = 8.sp, color = EeTheme.Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RowScope.EeMiniField(label: String, value: String, bg: Color) {
    Surface(shape = EeTheme.ChipShape, color = bg, border = BorderStroke(1.dp, EeTheme.Slate200), modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, fontSize = 7.sp, color = EeTheme.Slate500, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 10.sp, color = EeTheme.Slate700, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EeWideTable(
    rowOffset: Int,
    rows: List<EquipmentEfficiencyUiRow>,
    canEdit: Boolean,
    canDelete: Boolean,
    layout: EeLayoutMode,
    statusUpdatingId: Int?,
    onEdit: (EquipmentEfficiencyUiRow) -> Unit,
    onDelete: (Int) -> Unit,
    onStatusChange: (EquipmentEfficiencyUiRow, Boolean) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val compact = layout == EeLayoutMode.Medium
    val showActions = canEdit || canDelete
    Column(Modifier.fillMaxSize().verticalScroll(vScroll)) {
        Row(Modifier.horizontalScroll(hScroll).background(Color(0xFFF0F2F8)).padding(vertical = 2.dp)) {
            EeHeaderCells(showActions, compact)
        }
        if (rows.isEmpty()) {
            Text("データがありません", fontSize = 11.sp, color = EeTheme.Slate500, modifier = Modifier.padding(16.dp))
        }
        rows.forEachIndexed { index, row ->
            Column {
                Row(
                    Modifier.horizontalScroll(hScroll)
                        .background(if (index % 2 == 0) Color.White else Color(0xFFFAFCFF))
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EeDataCells(rowOffset + index + 1, row, showActions, canEdit, canDelete, compact, statusUpdatingId == row.id, onEdit, onDelete, onStatusChange)
                }
                HorizontalDivider(color = EeTheme.Slate100, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun RowScope.EeHeaderCells(showActions: Boolean, compact: Boolean) {
    @Composable fun H(label: String, w: Int, group: EeColGroup) {
        Box(Modifier.width(w.dp).background(group.bg()).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = EeTheme.Slate700, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 8.sp)
        }
    }
    H("#", 32, EeColGroup.Meta)
    H("設備CD", 52, EeColGroup.Meta)
    H("設備名", if (compact) 72 else 84, EeColGroup.Meta)
    H("製品CD", 52, EeColGroup.Meta)
    H("製品名", if (compact) 76 else 88, EeColGroup.Meta)
    H("能率", 56, EeColGroup.Eff)
    H("段取", 44, EeColGroup.Step)
    H("状態", 72, EeColGroup.Status)
    if (!compact) H("備考", 80, EeColGroup.Meta)
    if (showActions) H("操作", 72, EeColGroup.Act)
}

@Composable
private fun RowScope.EeDataCells(
    displayIndex: Int,
    row: EquipmentEfficiencyUiRow,
    showActions: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    compact: Boolean,
    statusLoading: Boolean,
    onEdit: (EquipmentEfficiencyUiRow) -> Unit,
    onDelete: (Int) -> Unit,
    onStatusChange: (EquipmentEfficiencyUiRow, Boolean) -> Unit,
) {
    @Composable fun C(w: Int, text: String, group: EeColGroup, bold: Boolean = false, color: Color = EeTheme.Slate700) {
        Box(Modifier.width(w.dp).background(group.bg().copy(alpha = 0.45f)).padding(horizontal = 2.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 8.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, lineHeight = 9.sp)
        }
    }
    C(32, "$displayIndex", EeColGroup.Meta)
    C(52, row.machineCd, EeColGroup.Meta)
    C(if (compact) 72 else 84, row.machinesName, EeColGroup.Meta)
    C(52, row.productCd, EeColGroup.Meta)
    C(if (compact) 76 else 88, row.productName, EeColGroup.Meta)
    C(56, EquipmentEfficiencyMasterLogic.formatEfficiency(row), EeColGroup.Eff, bold = true, color = EeTheme.Indigo600)
    C(44, EquipmentEfficiencyMasterLogic.formatStepTime(row.stepTime), EeColGroup.Step)
    Box(Modifier.width(72.dp).background(EeColGroup.Status.bg().copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (statusLoading) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.5.dp, color = EeTheme.Violet600)
            } else {
                Switch(
                    checked = row.status == 1,
                    onCheckedChange = { if (canEdit) onStatusChange(row, it) },
                    enabled = canEdit,
                    modifier = Modifier.scale(0.68f),
                    colors = SwitchDefaults.colors(checkedTrackColor = EeTheme.Violet600, checkedThumbColor = Color.White),
                )
            }
            Text(
                EquipmentEfficiencyMasterLogic.statusLabel(row.status),
                fontSize = 7.sp,
                color = if (row.status == 1) EeTheme.Emerald else EeTheme.Slate500,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    if (!compact) C(80, row.remarks.ifBlank { "—" }, EeColGroup.Meta)
    if (showActions) {
        Box(Modifier.width(72.dp).background(EeColGroup.Act.bg().copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (canEdit) EeIconBtn(Icons.Default.Edit, Color(0xFFF5F3FF), EeTheme.Violet600) { onEdit(row) }
                if (canDelete) EeIconBtn(Icons.Default.Delete, Color(0xFFFEF2F2), EeTheme.Rose) { onDelete(row.id) }
            }
        }
    }
}

@Composable
private fun EeResultBar(
    shown: Int,
    total: Int,
    activeTab: String,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val totalPages = ceil(total.toDouble() / pageSize.coerceAtLeast(1)).toInt().coerceAtLeast(1)
    val tabLabel = EquipmentEfficiencyMasterLogic.processTabs.find { it.value == activeTab }?.label ?: "全て"
    Column(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFFAFBFC), Color.White)))
            .border(BorderStroke(0.5.dp, EeTheme.Slate200))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("表示: $shown / $total 件", fontSize = 9.sp, color = EeTheme.Slate600)
            if (activeTab != "all") {
                Surface(shape = CircleShape, color = Color(0x146366F1), border = BorderStroke(1.dp, Color(0x1F6366F1))) {
                    Text("${tabLabel}工程", fontSize = 8.sp, color = EeTheme.Violet600, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(20, 50, 100).forEach { size ->
                    val selected = pageSize == size
                    Surface(
                        onClick = { onPageSizeChange(size) },
                        shape = EeTheme.PillShape,
                        color = if (selected) EeTheme.Violet600 else Color.White,
                        border = BorderStroke(1.dp, if (selected) EeTheme.Violet600 else EeTheme.Slate200),
                    ) {
                        Text("$size", fontSize = 8.sp, color = if (selected) Color.White else EeTheme.Slate600, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                EePageBtn(Icons.Default.ChevronLeft, currentPage > 1) { onPageChange(currentPage - 1) }
                Text("$currentPage / $totalPages", fontSize = 9.sp, color = EeTheme.Slate700, modifier = Modifier.padding(horizontal = 6.dp))
                EePageBtn(Icons.Default.ChevronRight, currentPage < totalPages) { onPageChange(currentPage + 1) }
            }
        }
    }
}

@Composable
private fun EePageBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) Color.White else EeTheme.Slate100,
        border = BorderStroke(1.dp, EeTheme.Slate200),
        modifier = Modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (enabled) EeTheme.Slate600 else EeTheme.Slate400, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun EeIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, tint: Color, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "eeIcon")
    Surface(onClick = onClick, interactionSource = interaction, shape = CircleShape, color = bg, modifier = Modifier.scale(scale).size(24.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp))
        }
    }
}
