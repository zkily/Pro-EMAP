package com.example.smart_emap.ui.master.productmachineconfig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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

@Composable
fun PmcPageBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(PmcTheme.PageBg)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Brush.radialGradient(listOf(Color(0x226366F1), Color.Transparent), radius = 480f)),
        )
        content()
    }
}

@Composable
fun PmcWorkspace(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, PmcTheme.PanelShape, ambientColor = Color(0x0F0F172A), spotColor = Color(0x180F172A)),
        shape = PmcTheme.PanelShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x3894A3B8)),
    ) {
        content()
    }
}

@Composable
fun ProductMachineConfigHeroBar(total: Int, filtered: Int, layout: PmcLayoutMode) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(300)) + slideInVertically(tween(340)) { it / 4 }) {
        Card(
            modifier = Modifier.fillMaxWidth().shadow(10.dp, PmcTheme.CardShape, spotColor = Color(0x406366F1)),
            shape = PmcTheme.CardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(3.dp).background(PmcTheme.HeroAccentBar))
                if (layout == PmcLayoutMode.Compact) {
                    Column(
                        Modifier.fillMaxWidth().background(PmcTheme.HeroBg).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PmcHeroBrand()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PmcHeroStat("$total", "登録数", PmcTheme.Cyan300, Modifier.weight(1f))
                            PmcHeroStat("$filtered", "表示中", PmcTheme.Sky400, Modifier.weight(1f))
                        }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().background(PmcTheme.HeroBg).padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PmcHeroBrand(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            PmcHeroStat("$total", "登録数", PmcTheme.Cyan300)
                            PmcHeroStat("$filtered", "表示中", PmcTheme.Sky400)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PmcHeroBrand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.size(34.dp).clip(PmcTheme.ChipShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), PmcTheme.ChipShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Column {
            Text("製品加工設備設定", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White, lineHeight = 17.sp)
            Text("製品ごとの機器設定を一覧管理", fontSize = 9.sp, color = Color(0xD1E2E8F0), lineHeight = 11.sp)
        }
    }
}

@Composable
private fun PmcHeroStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(shape = PmcTheme.ChipShape, color = Color.White.copy(alpha = 0.08f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), modifier = modifier) {
        Row(Modifier.padding(start = 0.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(30.dp).background(accent, PmcTheme.ChipShape))
            Column(Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White, lineHeight = 14.sp)
                Text(label, fontSize = 8.sp, color = Color(0xB3E2E8F0), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProductMachineConfigActionSection(
    keyword: String,
    filteredCount: Int,
    totalCount: Int,
    loading: Boolean,
    syncing: Boolean,
    canEdit: Boolean,
    canCreate: Boolean,
    layout: PmcLayoutMode,
    onKeywordChange: (String) -> Unit,
    onClear: () -> Unit,
    onSync: () -> Unit,
    onCreate: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(PmcTheme.FilterBg)) {
        if (layout == PmcLayoutMode.Compact) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FilterList, null, tint = PmcTheme.Indigo500, modifier = Modifier.size(14.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PmcTheme.Slate700)
                    PmcCountBadge(filteredCount, totalCount)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PmcTextChip("クリア", Icons.Default.Refresh, !loading, onClear)
                    if (canEdit) PmcSyncChip(if (syncing) "同期中" else "製品同期", syncing, !loading && !syncing, onSync)
                    if (canCreate) PmcPrimaryChip("新規登録", Icons.Default.Add, !loading, onCreate)
                }
                PmcSearchField(keyword, onKeywordChange)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.FilterList, null, tint = PmcTheme.Indigo500, modifier = Modifier.size(14.dp))
                Text("検索・絞り込み", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PmcTheme.Slate700)
                PmcCountBadge(filteredCount, totalCount)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PmcTextChip("クリア", Icons.Default.Refresh, !loading, onClear)
                    if (canEdit) {
                        PmcSyncChip(
                            label = if (syncing) "同期中" else if (layout == PmcLayoutMode.Medium) "同期" else "製品情報同期",
                            loading = syncing,
                            enabled = !loading && !syncing,
                            onClick = onSync,
                        )
                    }
                    if (canCreate) PmcPrimaryChip(if (layout == PmcLayoutMode.Wide) "新規登録" else "新規", Icons.Default.Add, !loading, onCreate)
                }
            }
            HorizontalDivider(color = PmcTheme.Slate200, thickness = 0.5.dp)
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp)) {
                PmcSearchField(keyword, onKeywordChange, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PmcCountBadge(filteredCount: Int, totalCount: Int) {
    Surface(shape = CircleShape, color = Color(0x146366F1), border = BorderStroke(1.dp, Color(0x1F6366F1))) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Default.Info, null, tint = PmcTheme.Indigo500, modifier = Modifier.size(11.dp))
            Text("表示 $filteredCount / $totalCount", fontSize = 9.sp, color = PmcTheme.Slate500, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PmcSearchField(keyword: String, onKeywordChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(PmcTheme.FieldShape).background(Color.White)
            .border(1.dp, Color(0x4D94A3B8), PmcTheme.FieldShape)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Surface(shape = PmcTheme.PillShape, color = Color(0xFFEEF2FF), border = BorderStroke(1.dp, Color(0x386366F1))) {
            Text("検索", Modifier.padding(horizontal = 7.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PmcTheme.Indigo600)
        }
        Icon(Icons.Default.Search, null, tint = PmcTheme.Slate400, modifier = Modifier.size(14.dp))
        BasicTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = PmcTheme.Slate900),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (keyword.isEmpty()) Text("製品CD・製品名", fontSize = 12.sp, color = PmcTheme.Slate400)
                inner()
            },
        )
    }
}

@Composable
private fun PmcTextChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(80), label = "chip")
    Surface(onClick = onClick, enabled = enabled, interactionSource = interaction, shape = PmcTheme.ChipShape, color = PmcTheme.Slate100, border = BorderStroke(1.dp, PmcTheme.Slate200), modifier = Modifier.scale(scale)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PmcTheme.Slate600, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, color = PmcTheme.Slate600, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PmcPrimaryChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(80), label = "primary")
    Surface(onClick = onClick, enabled = enabled, interactionSource = interaction, shape = PmcTheme.ChipShape, color = Color.Transparent, modifier = Modifier.scale(scale)) {
        Row(Modifier.background(PmcTheme.PrimaryBtn).padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PmcSyncChip(label: String, loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(80), label = "sync")
    Surface(onClick = onClick, enabled = enabled, interactionSource = interaction, shape = PmcTheme.ChipShape, color = Color(0xFFECFDF5), border = BorderStroke(1.dp, PmcTheme.Emerald), modifier = Modifier.scale(scale)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            if (loading) CircularProgressIndicator(Modifier.size(11.dp), strokeWidth = 2.dp, color = PmcTheme.Emerald)
            else Icon(Icons.Default.Sync, null, tint = PmcTheme.Emerald, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, color = PmcTheme.Emerald, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProductMachineConfigTable(
    rows: List<ProductMachineConfigUiRow>,
    loading: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    layout: PmcLayoutMode,
    modifier: Modifier = Modifier,
    onEdit: (ProductMachineConfigUiRow) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val showActions = canEdit || canDelete
    Column(modifier) {
        PmcTableCap(layout)
        HorizontalDivider(color = PmcTheme.Slate200, thickness = 0.5.dp)
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = when {
                    loading && rows.isEmpty() -> "loading"
                    rows.isEmpty() -> "empty"
                    layout == PmcLayoutMode.Compact -> "cards"
                    else -> "table"
                },
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(160)) },
                label = "pmc-table",
                modifier = Modifier.fillMaxSize(),
            ) { state ->
            when (state) {
                "loading" -> Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PmcTheme.Indigo500, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
                }
                "empty" -> Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = PmcTheme.Slate400, fontSize = 11.sp)
                }
                "cards" -> PmcCompactCardList(rows, showActions, canEdit, canDelete, onEdit, onDelete)
                else -> PmcWideTable(rows, showActions, canEdit, canDelete, layout, onEdit, onDelete)
            }
            }
        }
    }
}

@Composable
private fun PmcTableCap(layout: PmcLayoutMode) {
    Row(
        Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(PmcTheme.Slate50, PmcTheme.Slate100)))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(Brush.linearGradient(listOf(PmcTheme.Indigo500, PmcTheme.Sky400))))
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Default.Memory, null, tint = PmcTheme.Indigo500, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text("設備一覧", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PmcTheme.Slate900)
        if (layout != PmcLayoutMode.Compact) {
            Spacer(Modifier.width(6.dp))
            Surface(shape = CircleShape, color = Color(0x146366F1), border = BorderStroke(1.dp, Color(0x1F6366F1))) {
                Text("横スクロール", Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 8.sp, color = PmcTheme.Slate500)
            }
        }
    }
}

@Composable
private fun PmcCompactCardList(
    rows: List<ProductMachineConfigUiRow>,
    showActions: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: (ProductMachineConfigUiRow) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 6.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { index, row ->
            var visible by remember(row.id) { mutableStateOf(false) }
            LaunchedEffect(row.id) { visible = true }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(160, delayMillis = (index % 6) * 30)) + slideInVertically(tween(200, delayMillis = (index % 6) * 30)) { it / 6 },
            ) {
                PmcConfigCard(row, showActions, canEdit, canDelete, onEdit, onDelete)
            }
        }
    }
}

@Composable
private fun PmcConfigCard(
    row: ProductMachineConfigUiRow,
    showActions: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: (ProductMachineConfigUiRow) -> Unit,
    onDelete: (Int) -> Unit,
) {
    var expanded by remember(row.id) { mutableStateOf(false) }
    Surface(
        shape = PmcTheme.PanelShape,
        color = Color.White,
        border = BorderStroke(1.dp, PmcTheme.Slate200),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(row.productCd, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PmcTheme.Indigo600)
                    Text(row.productName, fontSize = 10.sp, color = PmcTheme.Slate700, maxLines = if (expanded) 3 else 1, overflow = TextOverflow.Ellipsis)
                }
                if (showActions) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (canEdit) PmcIconBtn(Icons.Default.Edit, Color(0xFFEEF2FF), PmcTheme.Indigo600) { onEdit(row) }
                        if (canDelete) PmcIconBtn(Icons.Default.Delete, Color(0xFFFEF2F2), PmcTheme.Rose) { onDelete(row.id) }
                    }
                }
            }
            AnimatedVisibility(visible = expanded, enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
                Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider(color = PmcTheme.Slate100)
                    PmcCardFieldGrid(row)
                }
            }
            if (!expanded) {
                val preview = listOf(row.cuttingMachine, row.moldingMachine, row.weldingMachine).filter { it.isNotBlank() }.take(2)
                if (preview.isNotEmpty()) {
                    Text(
                        preview.joinToString(" · "),
                        fontSize = 9.sp,
                        color = PmcTheme.Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text("タップで詳細", fontSize = 8.sp, color = PmcTheme.Slate400, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun PmcCardFieldGrid(row: ProductMachineConfigUiRow) {
    val fields = listOf(
        "切断機" to row.cuttingMachine,
        "面取機" to row.chamferingMachine,
        "SW機" to row.swMachine,
        "成型機" to row.moldingMachine,
        "メッキ治具" to row.platingMachine,
        "溶接機" to row.weldingMachine,
        "検査員" to row.inspectorMachine,
        "外注メッキ" to row.outsourcedPlatingMachine,
        "外注溶接" to row.outsourcedWeldingMachine,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        fields.forEach { (label, value) ->
            Surface(shape = PmcTheme.ChipShape, color = PmcTheme.Slate50, border = BorderStroke(1.dp, PmcTheme.Slate200)) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 100.dp, max = 160.dp)) {
                    Text(label, fontSize = 8.sp, color = PmcTheme.Slate500, fontWeight = FontWeight.SemiBold)
                    Text(value.ifBlank { "—" }, fontSize = 9.sp, color = PmcTheme.Slate700, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun PmcWideTable(
    rows: List<ProductMachineConfigUiRow>,
    showActions: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    layout: PmcLayoutMode,
    onEdit: (ProductMachineConfigUiRow) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val compactCols = layout == PmcLayoutMode.Medium
    Column(Modifier.fillMaxSize().verticalScroll(vScroll)) {
        Row(Modifier.horizontalScroll(hScroll).padding(vertical = 1.dp)) {
            PmcHeaderCells(showActions, compactCols)
        }
        rows.forEachIndexed { index, row ->
            var rowVisible by remember(row.id) { mutableStateOf(false) }
            LaunchedEffect(row.id) { rowVisible = true }
            AnimatedVisibility(
                visible = rowVisible,
                enter = fadeIn(tween(160, delayMillis = (index % 8) * 25)) + slideInVertically(tween(200, delayMillis = (index % 8) * 25)) { it / 5 },
            ) {
                Column {
                    Row(
                        Modifier.horizontalScroll(hScroll)
                            .background(if (index % 2 == 0) Color.White else Color(0xFFFAFCFF))
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PmcDataCells(row, showActions, canEdit, canDelete, compactCols, onEdit, onDelete)
                    }
                    HorizontalDivider(color = PmcTheme.Slate100, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun RowScope.PmcHeaderCells(showActions: Boolean, compact: Boolean) {
    @Composable fun H(label: String, w: Int, group: PmcColGroup) {
        Box(Modifier.width(w.dp).background(group.bg()).padding(vertical = 3.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = PmcTheme.Slate600, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 8.sp)
        }
    }
    H("CD", 56, PmcColGroup.Meta)
    H("製品名", if (compact) 80 else 88, PmcColGroup.Meta)
    H("切断", 72, PmcColGroup.Cut)
    H("面取", 72, PmcColGroup.Cut)
    if (!compact) {
        H("SW", 64, PmcColGroup.Form)
    }
    H("成型", 72, PmcColGroup.Form)
    H("メッキ", 76, PmcColGroup.Plate)
    H("溶接", 72, PmcColGroup.Weld)
    if (!compact) {
        H("検査", 72, PmcColGroup.Weld)
    }
    if (!compact) {
        H("外注メ", 80, PmcColGroup.Out)
        H("外注溶", 80, PmcColGroup.Out)
    }
    if (showActions) H("操作", 72, PmcColGroup.Act)
}

@Composable
private fun RowScope.PmcDataCells(
    row: ProductMachineConfigUiRow,
    showActions: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    compact: Boolean,
    onEdit: (ProductMachineConfigUiRow) -> Unit,
    onDelete: (Int) -> Unit,
) {
    @Composable fun C(w: Int, text: String, group: PmcColGroup) {
        Box(Modifier.width(w.dp).background(group.bg().copy(alpha = 0.4f)).padding(horizontal = 2.dp, vertical = 3.dp), contentAlignment = Alignment.Center) {
            Text(text.ifBlank { "—" }, fontSize = 8.sp, color = PmcTheme.Slate700, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, lineHeight = 9.sp)
        }
    }
    C(56, row.productCd, PmcColGroup.Meta)
    C(if (compact) 80 else 88, row.productName, PmcColGroup.Meta)
    C(72, row.cuttingMachine, PmcColGroup.Cut)
    C(72, row.chamferingMachine, PmcColGroup.Cut)
    if (!compact) C(64, row.swMachine, PmcColGroup.Form)
    C(72, row.moldingMachine, PmcColGroup.Form)
    C(76, row.platingMachine, PmcColGroup.Plate)
    C(72, row.weldingMachine, PmcColGroup.Weld)
    if (!compact) C(72, row.inspectorMachine, PmcColGroup.Weld)
    if (!compact) {
        C(80, row.outsourcedPlatingMachine, PmcColGroup.Out)
        C(80, row.outsourcedWeldingMachine, PmcColGroup.Out)
    }
    if (showActions) {
        Box(Modifier.width(72.dp).background(PmcColGroup.Act.bg().copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (canEdit) PmcIconBtn(Icons.Default.Edit, Color(0xFFEEF2FF), PmcTheme.Indigo600) { onEdit(row) }
                if (canDelete) PmcIconBtn(Icons.Default.Delete, Color(0xFFFEF2F2), PmcTheme.Rose) { onDelete(row.id) }
            }
        }
    }
}

@Composable
private fun PmcIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, tint: Color, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "icon")
    Surface(onClick = onClick, interactionSource = interaction, shape = CircleShape, color = bg, modifier = Modifier.scale(scale).size(24.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp)) }
    }
}
