package com.example.smart_emap.ui.master.productprocessbom

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.ProductProcessBomStatsDto
import kotlin.math.max

@Composable
internal fun PpbDotToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = PpbTheme.Indigo500,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ppbDotPress",
    )
    val dotScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "ppbDotInner",
    )
    val ringColor = when {
        !enabled -> PpbTheme.Slate200
        checked -> accent
        else -> PpbTheme.Slate400
    }
    val glowAlpha by animateFloatAsState(
        targetValue = if (checked && enabled) 0.14f else 0f,
        animationSpec = tween(180),
        label = "ppbDotGlow",
    )

    Box(
        modifier = modifier
            .size(20.dp)
            .scale(pressScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (glowAlpha > 0f) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = glowAlpha)),
            )
        }
        Box(
            Modifier
                .size(18.dp)
                .border(2.dp, ringColor, CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (dotScale > 0.02f) {
                Box(
                    Modifier
                        .size((8 * dotScale).dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) {
                                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.82f)))
                            } else {
                                Brush.linearGradient(listOf(PpbTheme.Slate400, PpbTheme.Slate400))
                            },
                        ),
                )
            }
        }
    }
}

@Composable
fun PpbPageBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(PpbTheme.PageBg)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x1F6366F1), Color.Transparent),
                        radius = 500f,
                    ),
                ),
        )
        content()
    }
}

@Composable
fun PpbWorkspace(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, PpbTheme.PanelShape, ambientColor = Color(0x0F0F172A), spotColor = Color(0x140F172A)),
        shape = PpbTheme.PanelShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x3894A3B8)),
    ) {
        content()
    }
}

@Composable
fun ProductProcessBomHeroBar(stats: ProductProcessBomStatsDto) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)) + slideInVertically(tween(360)) { it / 3 },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, PpbTheme.CardShape, ambientColor = Color(0x450F172A), spotColor = Color(0x550F172A)),
            shape = PpbTheme.CardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PpbTheme.HeroAccentBar),
                )
                Box(Modifier.fillMaxWidth().background(PpbTheme.HeroBg)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(PpbTheme.PanelShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), PpbTheme.PanelShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Build, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    "製品工程BOM管理",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp,
                                )
                                Text(
                                    "工程フラグ・LT を一覧編集（セル変更は自動保存）",
                                    fontSize = 10.sp,
                                    color = Color(0xD1E2E8F0),
                                    lineHeight = 12.sp,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PpbHeroStat("${stats.total}", "登録数", PpbTheme.Sky300, Icons.Default.FormatListBulleted)
                            PpbHeroStat("${stats.activeCount}", "現行", PpbTheme.Green400, Icons.Default.CheckCircle)
                            PpbHeroStat("${stats.discontinuedCount}", "終息", PpbTheme.Orange400, Icons.Default.Cancel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PpbHeroStat(
    value: String,
    label: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        shape = PpbTheme.ChipShape,
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Row(
            Modifier.padding(start = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(accent, PpbTheme.ChipShape),
            )
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(14.dp))
                Column {
                    Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.White, lineHeight = 15.sp)
                    Text(label, fontSize = 9.sp, color = Color(0xB3E2E8F0), fontWeight = FontWeight.SemiBold, lineHeight = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ProductProcessBomToolbar(
    keyword: String,
    loading: Boolean,
    syncing: Boolean,
    canEdit: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onSync: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFFAFBFF), Color.White)))
            .border(BorderStroke(0.dp, Color.Transparent))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = PpbTheme.PillShape,
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0x386366F1)),
                ) {
                    Text(
                        "検索",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PpbTheme.Indigo600,
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PpbTheme.FieldShape)
                        .background(Color.White)
                        .border(1.dp, Color(0x4D94A3B8), PpbTheme.FieldShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Search, null, tint = PpbTheme.Slate400, modifier = Modifier.size(15.dp))
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = PpbTheme.Slate900),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (keyword.isEmpty()) {
                                Text("製品コード・製品名", fontSize = 12.sp, color = PpbTheme.Slate400)
                            }
                            inner()
                        },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                PpbActionChip("クリア", Icons.Default.Refresh, PpbTheme.Slate600, !loading, onClear)
                PpbGradientChip("検索", Icons.Default.Search, PpbTheme.SearchBtn, !loading, onClick = onSearch)
                if (canEdit) {
                    PpbSyncChip(
                        label = if (syncing) "同期中" else "同期",
                        loading = syncing,
                        enabled = !loading && !syncing,
                        onClick = onSync,
                    )
                }
            }
        }
    }
}

@Composable
private fun PpbSyncChip(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90), label = "sync-scale")
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = PpbTheme.ChipShape,
        color = PpbTheme.SyncBtnBg,
        border = BorderStroke(1.dp, PpbTheme.SyncBtnBorder),
        modifier = Modifier.scale(scale),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(color = PpbTheme.SyncBtnText, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
            } else {
                Icon(Icons.Default.Sync, null, tint = PpbTheme.SyncBtnText, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = PpbTheme.SyncBtnText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PpbActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90), label = "chip-scale")
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = PpbTheme.ChipShape,
        color = PpbTheme.Slate100,
        border = BorderStroke(1.dp, PpbTheme.Slate200),
        modifier = Modifier.scale(scale),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PpbGradientChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    brush: Brush,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90), label = "grad-scale")
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = PpbTheme.ChipShape,
        color = Color.Transparent,
        modifier = Modifier.scale(scale),
    ) {
        Row(
            Modifier.background(brush).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
            } else {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProductProcessBomTable(
    rows: List<ProductProcessBomUiRow>,
    loading: Boolean,
    savingProductCd: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    onRowChange: (Int, (ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit,
    onAutoSave: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val showActions = canEdit || canDelete

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PpbTheme.Indigo500, Color(0xFF0EA5E9)))),
            )
            Spacer(Modifier.width(8.dp))
            Text("登録一覧", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PpbTheme.Slate900)
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color(0x146366F1),
                border = BorderStroke(1.dp, Color(0x1F6366F1)),
            ) {
                Text(
                    "自動保存 · デバウンス",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = PpbTheme.Slate500,
                )
            }
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(visible = savingProductCd != null, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CircularProgressIndicator(Modifier.size(11.dp), strokeWidth = 2.dp, color = PpbTheme.Indigo500)
                    Text("保存中…", fontSize = 9.sp, color = PpbTheme.Indigo600)
                }
            }
        }
        HorizontalDivider(color = PpbTheme.Slate200, thickness = 0.5.dp)
        AnimatedContent(
                targetState = when {
                    loading && rows.isEmpty() -> "loading"
                    rows.isEmpty() -> "empty"
                    else -> "data"
                },
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "table-state",
            ) { state ->
                when (state) {
                    "loading" -> Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PpbTheme.Indigo500, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                    "empty" -> Text(
                        "データがありません",
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = PpbTheme.Slate400,
                        fontSize = 12.sp,
                    )
                    else -> Column(Modifier.heightIn(max = 480.dp).verticalScroll(vScroll)) {
                        Row(Modifier.horizontalScroll(hScroll).padding(vertical = 2.dp)) {
                            PpbHeaderCells(showActions)
                        }
                        rows.forEachIndexed { index, row ->
                            var rowVisible by remember(row.productCd) { mutableStateOf(false) }
                            LaunchedEffect(row.productCd) { rowVisible = true }
                            AnimatedVisibility(
                                visible = rowVisible,
                                enter = fadeIn(tween(180, delayMillis = (index % 8) * 30)) +
                                    slideInVertically(tween(220, delayMillis = (index % 8) * 30)) { it / 4 },
                            ) {
                                Column {
                                    Row(
                                        Modifier
                                            .horizontalScroll(hScroll)
                                            .background(
                                                if (row.isDiscontinued) Color(0xFFFFF1F2)
                                                else if (index % 2 == 0) Color.White
                                                else Color(0xFFFAFCFF),
                                            )
                                            .padding(vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        PpbDataCells(
                                            row = row,
                                            canEdit = canEdit,
                                            showActions = showActions,
                                            canDelete = canDelete,
                                            saving = savingProductCd == row.productCd,
                                            onChange = { transform ->
                                                onRowChange(row.productCd, transform)
                                                onAutoSave(row.productCd)
                                            },
                                            onEdit = { onEdit(row.productCd) },
                                            onDelete = { onDelete(row.productCd) },
                                        )
                                    }
                                    HorizontalDivider(color = PpbTheme.Slate100, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
private fun RowScope.PpbHeaderCells(showActions: Boolean) {
    @Composable
    fun H(label: String, w: Int, group: PpbColGroup) {
        Box(
            Modifier
                .width(w.dp)
                .background(group.bg())
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = PpbTheme.Slate600, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 9.sp)
        }
    }
    H("終息", 36, PpbColGroup.Meta)
    H("CD", 48, PpbColGroup.Meta)
    H("製品名", 92, PpbColGroup.Meta)
    H("最低", 44, PpbColGroup.Stock)
    H("安全", 44, PpbColGroup.Stock)
    H("材料", 32, PpbColGroup.G0); H("LT", 40, PpbColGroup.G0)
    H("切断", 32, PpbColGroup.G1); H("LT", 40, PpbColGroup.G1)
    H("面取", 32, PpbColGroup.G2); H("LT", 40, PpbColGroup.G2)
    H("SW", 28, PpbColGroup.G3); H("LT", 38, PpbColGroup.G3)
    H("成型", 32, PpbColGroup.G0); H("LT", 40, PpbColGroup.G0)
    H("メッキ", 36, PpbColGroup.G1); H("LT", 42, PpbColGroup.G1)
    H("外注メ", 44, PpbColGroup.G2); H("LT", 48, PpbColGroup.G2)
    H("溶接", 32, PpbColGroup.G3); H("LT", 40, PpbColGroup.G3)
    H("外注溶", 44, PpbColGroup.G0); H("LT", 48, PpbColGroup.G0)
    H("検査", 32, PpbColGroup.G1); H("LT", 40, PpbColGroup.G1)
    H("外注倉", 44, PpbColGroup.G2); H("LT", 48, PpbColGroup.G2)
    H("前溶接", 44, PpbColGroup.G3); H("後溶接", 44, PpbColGroup.G3); H("後LT", 48, PpbColGroup.G3)
    if (showActions) H("操作", 64, PpbColGroup.Act)
}

@Composable
private fun RowScope.PpbDataCells(
    row: ProductProcessBomUiRow,
    canEdit: Boolean,
    showActions: Boolean,
    canDelete: Boolean,
    saving: Boolean,
    onChange: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    @Composable
    fun C(w: Int, group: PpbColGroup = PpbColGroup.Meta, content: @Composable () -> Unit) {
        Box(
            Modifier.width(w.dp).background(group.bg().copy(alpha = 0.35f)).padding(horizontal = 1.dp),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
    @Composable
    fun lt(w: Int, value: Int, enabled: Boolean, group: PpbColGroup, set: (ProductProcessBomUiRow, Int) -> ProductProcessBomUiRow) {
        C(w, group) { PpbLtField(value, enabled && canEdit) { v -> onChange { set(it, v) } } }
    }
    @Composable
    fun flag(w: Int, checked: Boolean, group: PpbColGroup, toggle: (ProductProcessBomUiRow) -> ProductProcessBomUiRow) {
        C(w, group) {
            PpbDotToggle(
                checked = checked,
                onCheckedChange = { if (canEdit) onChange { toggle(it) } },
                enabled = canEdit,
            )
        }
    }

    C(36, PpbColGroup.Meta) {
        PpbDotToggle(
            checked = row.isDiscontinued,
            onCheckedChange = { if (canEdit) onChange { it.copy(isDiscontinued = !it.isDiscontinued) } },
            enabled = canEdit,
            accent = PpbTheme.Rose,
        )
    }
    C(48, PpbColGroup.Meta) {
        Text("${row.productCd}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = PpbTheme.Slate900)
    }
    C(92, PpbColGroup.Meta) {
        Text(
            row.productName,
            fontSize = 8.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = PpbTheme.Slate600,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
    C(44, PpbColGroup.Stock) { PpbLtField(row.minStockDays, canEdit) { v -> onChange { it.copy(minStockDays = v) } } }
    C(44, PpbColGroup.Stock) { PpbLtField(row.safetyStockDays, canEdit) { v -> onChange { it.copy(safetyStockDays = v) } } }

    flag(32, row.materialProcess, PpbColGroup.G0) { it.copy(materialProcess = !it.materialProcess) }
    lt(40, row.materialProcessLt, row.materialProcess, PpbColGroup.G0) { r, v -> r.copy(materialProcessLt = v) }
    flag(32, row.cutingProcess, PpbColGroup.G1) { it.copy(cutingProcess = !it.cutingProcess) }
    lt(40, row.cutingProcessLt, row.cutingProcess, PpbColGroup.G1) { r, v -> r.copy(cutingProcessLt = v) }
    flag(32, row.chamferingProcess, PpbColGroup.G2) { it.copy(chamferingProcess = !it.chamferingProcess) }
    lt(40, row.chamferingProcessLt, row.chamferingProcess, PpbColGroup.G2) { r, v -> r.copy(chamferingProcessLt = v) }
    flag(28, row.swagingProcess, PpbColGroup.G3) { it.copy(swagingProcess = !it.swagingProcess) }
    lt(38, row.swagingProcessLt, row.swagingProcess, PpbColGroup.G3) { r, v -> r.copy(swagingProcessLt = v) }
    flag(32, row.formingProcess, PpbColGroup.G0) { it.copy(formingProcess = !it.formingProcess) }
    lt(40, row.formingProcessLt, row.formingProcess, PpbColGroup.G0) { r, v -> r.copy(formingProcessLt = v) }
    flag(36, row.platingProcess, PpbColGroup.G1) { it.copy(platingProcess = !it.platingProcess) }
    lt(42, row.platingProcessLt, row.platingProcess, PpbColGroup.G1) { r, v -> r.copy(platingProcessLt = v) }
    flag(44, row.outsourcedPlatingProcess, PpbColGroup.G2) { it.copy(outsourcedPlatingProcess = !it.outsourcedPlatingProcess) }
    lt(48, row.outsourcedPlatingProcessLt, row.outsourcedPlatingProcess, PpbColGroup.G2) { r, v -> r.copy(outsourcedPlatingProcessLt = v) }
    flag(32, row.weldingProcess, PpbColGroup.G3) { it.copy(weldingProcess = !it.weldingProcess) }
    lt(40, row.weldingProcessLt, row.weldingProcess, PpbColGroup.G3) { r, v -> r.copy(weldingProcessLt = v) }
    flag(44, row.outsourcedWeldingProcess, PpbColGroup.G0) { it.copy(outsourcedWeldingProcess = !it.outsourcedWeldingProcess) }
    lt(48, row.outsourcedWeldingProcessLt, row.outsourcedWeldingProcess, PpbColGroup.G0) { r, v -> r.copy(outsourcedWeldingProcessLt = v) }
    flag(32, row.inspectionProcess, PpbColGroup.G1) { it.copy(inspectionProcess = !it.inspectionProcess) }
    lt(40, row.inspectionProcessLt, row.inspectionProcess, PpbColGroup.G1) { r, v -> r.copy(inspectionProcessLt = v) }
    flag(44, row.outsourcedWarehouseProcess, PpbColGroup.G2) { it.copy(outsourcedWarehouseProcess = !it.outsourcedWarehouseProcess) }
    lt(48, row.outsourcedWarehouseProcessLt, row.outsourcedWarehouseProcess, PpbColGroup.G2) { r, v -> r.copy(outsourcedWarehouseProcessLt = v) }
    flag(44, row.prePlatingWelding, PpbColGroup.G3) { it.copy(prePlatingWelding = !it.prePlatingWelding) }
    flag(44, row.postInspectionWelding, PpbColGroup.G3) { it.copy(postInspectionWelding = !it.postInspectionWelding) }
    lt(48, row.postInspectionWeldingLt, row.postInspectionWelding, PpbColGroup.G3) { r, v -> r.copy(postInspectionWeldingLt = v) }

    if (showActions) {
        C(64, PpbColGroup.Act) {
            Row(horizontalArrangement = Arrangement.Center) {
                if (canEdit) {
                    PpbIconAction(Icons.Default.Edit, Color(0xFFEEF2FF), PpbTheme.Indigo600, onEdit)
                }
                if (canDelete) {
                    PpbIconAction(Icons.Default.Delete, Color(0xFFFEF2F2), PpbTheme.Rose, onDelete)
                }
                if (saving) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = PpbTheme.Indigo500)
                }
            }
        }
    }
}

@Composable
private fun PpbIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bg: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "icon-scale")
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = CircleShape,
        color = bg,
        modifier = Modifier.scale(scale).size(26.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PpbLtField(value: Int, enabled: Boolean, onValue: (Int) -> Unit) {
    BasicTextField(
        value = value.toString(),
        onValueChange = { onValue(ProductProcessBomMasterLogic.parseLt(it)) },
        enabled = enabled,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            color = if (enabled) PpbTheme.Slate900 else PpbTheme.Slate400,
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(36.dp)
            .clip(PpbTheme.FieldShape)
            .background(if (enabled) Color.White else PpbTheme.Slate50)
            .border(1.dp, if (enabled) PpbTheme.Slate200 else Color.Transparent, PpbTheme.FieldShape)
            .padding(vertical = 3.dp, horizontal = 2.dp),
    )
}

@Composable
fun ProductProcessBomPaginationBar(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val maxPage = max(1, (total + pageSize - 1) / pageSize)
    val sizes = listOf(10, 20, 50, 100)
    HorizontalDivider(color = PpbTheme.Slate200, thickness = 0.5.dp)
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
            val compact = maxWidth < 520.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("全 $total 件 · $page / $maxPage", fontSize = 10.sp, color = PpbTheme.Slate500)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        sizes.forEach { size -> PpbPageSizeChip(size, size == pageSize, onPageSizeChange) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PpbNavBtn("前へ", page > 1) { onPageChange(page - 1) }
                        PpbNavBtn("次へ", page < maxPage) { onPageChange(page + 1) }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("全 $total 件", fontSize = 10.sp, color = PpbTheme.Slate500, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        sizes.forEach { size -> PpbPageSizeChip(size, size == pageSize, onPageSizeChange) }
                        PpbNavBtn("前へ", page > 1) { onPageChange(page - 1) }
                        Surface(shape = PpbTheme.ChipShape, color = PpbTheme.Slate100) {
                            Text("$page / $maxPage", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        PpbNavBtn("次へ", page < maxPage) { onPageChange(page + 1) }
                    }
                }
            }
    }
}

@Composable
private fun PpbPageSizeChip(size: Int, active: Boolean, onClick: (Int) -> Unit) {
    val scale by animateFloatAsState(if (active) 1.04f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "page-chip")
    Surface(
        onClick = { onClick(size) },
        shape = PpbTheme.ChipShape,
        color = if (active) PpbTheme.Indigo600 else PpbTheme.Slate100,
        modifier = Modifier.scale(scale),
    ) {
        Text(
            "$size",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) Color.White else PpbTheme.Slate600,
        )
    }
}

@Composable
private fun PpbNavBtn(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = PpbTheme.ChipShape,
        color = if (enabled) Color(0xFFEEF2FF) else PpbTheme.Slate100,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = if (enabled) PpbTheme.Indigo600 else PpbTheme.Slate400,
            fontWeight = FontWeight.Medium,
        )
    }
}
