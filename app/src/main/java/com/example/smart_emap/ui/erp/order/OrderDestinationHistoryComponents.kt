package com.example.smart_emap.ui.erp.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.DestinationOptionDto
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private val destHistNumberFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

private val destHistFilterFieldHeight = 36.dp

@Composable
fun DestHistoryStaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it / 6 } +
            scaleIn(initialScale = 0.98f, animationSpec = tween(400, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}

@Composable
fun DestHistoryAnimatedBackground() {
    val transition = rememberInfiniteTransition(label = "dest-hist-bg")
    val orb1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "orb1",
    )
    val orb2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(11000), RepeatMode.Reverse),
        label = "orb2",
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = (orb1 * 20 - 10).dp, y = (-60).dp + (orb2 * 12).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2E6366F1), Color(0x148B5CF6), Color.Transparent),
                        radius = 320f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = (orb2 * 24).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2406B6D4), Color.Transparent),
                        radius = 260f,
                    ),
                ),
        )
    }
}

@Composable
fun DestHistoryHeroPanel(
    resultCount: Int,
    showResultBadge: Boolean,
    destinationCd: String,
    startDate: String,
    endDate: String,
    destinationOptions: List<DestinationOptionDto>,
    isLoading: Boolean,
    onDestinationChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    var destExpanded by remember { mutableStateOf(false) }
    val heroShape = RoundedCornerShape(16.dp)
    val heroGradient = Brush.linearGradient(
        listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF6D28D9)),
    )
    val iconPulse = rememberInfiniteTransition(label = "dest-hero-icon")
    val iconGlow by iconPulse.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "iconGlow",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, heroShape, spotColor = Color(0x554F46E5))
            .clip(heroShape)
            .background(heroGradient)
            .border(1.dp, Color.White.copy(alpha = 0.22f), heroShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(iconGlow)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(
                    "納入先別受注履歴",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.3).sp,
                )
                Text(
                    "納入先ごとの受注データ分析・履歴管理",
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            AnimatedVisibility(
                visible = showResultBadge,
                enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.85f, animationSpec = tween(300)),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("結果", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.78f))
                    Text("${resultCount}件", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.16f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.07f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DestHistoryConditionBadge()
            DestHistoryFilterGroup(
                icon = Icons.Default.LocalShipping,
                label = "納入先",
                accent = Color(0xFFF59E0B),
            ) {
                DestHistoryDestDropdown(
                    destinationCd = destinationCd,
                    options = destinationOptions,
                    expanded = destExpanded,
                    onExpandedChange = { destExpanded = it },
                    onSelect = onDestinationChange,
                )
            }
            DestHistoryFilterGroup(
                icon = Icons.Default.CalendarMonth,
                label = "期間",
                accent = Color(0xFF38BDF8),
            ) {
                OrderDailyCalendarRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    accent = Color(0xFF4F46E5),
                    onStartChange = onStartDateChange,
                    onEndChange = onEndDateChange,
                    fieldHeight = destHistFilterFieldHeight,
                    fieldMinWidth = 124.dp,
                    elevated = true,
                    separatorColor = Color.White.copy(alpha = 0.78f),
                )
            }
            DestHistorySearchButton(isLoading = isLoading, onClick = onSearch)
        }
    }
}

@Composable
private fun DestHistoryConditionBadge() {
    Row(
        modifier = Modifier
            .height(destHistFilterFieldHeight)
            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = Color(0x40000000))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x1F0F172A))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(15.dp))
        Text("条件", color = Color.White.copy(alpha = 0.92f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DestHistoryFilterGroup(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(accent.copy(alpha = 0.28f))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
            }
            Text(
                label,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        content()
    }
}

@Composable
private fun DestHistorySearchButton(isLoading: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "searchBtnScale",
    )
    Box(
        modifier = Modifier
            .height(destHistFilterFieldHeight)
            .scale(scale)
            .shadow(8.dp, shape, spotColor = Color(0x55000000))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF0F172A))))
            .border(1.dp, Color.White.copy(alpha = 0.24f), shape)
            .clickable(enabled = !isLoading, interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Text("検索", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestHistoryDestDropdown(
    destinationCd: String,
    options: List<DestinationOptionDto>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
) {
    val label = options.find { it.cd == destinationCd }?.let { "${it.cd} - ${it.name}" } ?: "納入先を選択"
    val shape = RoundedCornerShape(10.dp)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        Row(
            modifier = Modifier
                .widthIn(min = 220.dp)
                .shadow(5.dp, shape, spotColor = Color(0x40000000))
                .height(destHistFilterFieldHeight)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC))))
                .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (destinationCd.isBlank()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { d ->
                DropdownMenuItem(
                    text = { Text("${d.cd} - ${d.name}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = { onSelect(d.cd); onExpandedChange(false) },
                )
            }
        }
    }
}

@Composable
fun DestHistorySummarySection(summary: List<DestinationHistorySummaryUi>) {
    DestHistoryPanelCard(
        title = "月別集計",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        trailing = {
            if (summary.isNotEmpty()) {
                DestHistoryStatChip("期間", "${summary.size}ヶ月")
            }
        },
    ) {
        if (summary.isEmpty()) {
            DestHistoryEmptyHint("検索後に月別集計が表示されます")
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(OrderMonthlyColors.tableHeaderBackground),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DestHistoryHeaderCell("年月", 140.dp, TextAlign.Center)
                    DestHistoryHeaderCell("受注数量合計", Modifier.weight(1f), TextAlign.End)
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                summary.forEachIndexed { index, row ->
                    DestHistoryAnimatedRow(index = index, key = row.ym) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(if (index % 2 == 1) Color(0xFFFAFBFC) else Color.White)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DestHistoryDateCell(row.ym, 140.dp)
                            Box(modifier = Modifier.weight(1f).padding(end = 12.dp), contentAlignment = Alignment.CenterEnd) {
                                DestHistoryQuantityCell(row.totalQuantity)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DestHistoryDetailsSection(
    items: List<DestinationHistoryItemUi>,
    isLoading: Boolean,
    hasSearched: Boolean,
    onPrint: () -> Unit,
) {
    DestHistoryPanelCard(
        title = "受注明細",
        icon = Icons.Default.LocalShipping,
        trailing = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (items.isNotEmpty()) {
                    DestHistoryStatChip("明細", "${items.size}件")
                }
                DestHistoryPrintButton(onClick = onPrint)
            }
        },
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFF6366F1),
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                    )
                }
            }
            !hasSearched -> DestHistoryEmptyHint("納入先と期間を選択して検索してください")
            items.isEmpty() -> DestHistoryEmptyHint("該当データがありません")
            else -> {
                val scroll = rememberScrollState()
                Column(modifier = Modifier.horizontalScroll(scroll)) {
                    val tableWidth = 780.dp
                    Column(modifier = Modifier.width(tableWidth)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(OrderMonthlyColors.tableHeaderBackground),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DestHistoryHeaderCell("出荷日", 108.dp, TextAlign.Center)
                            DestHistoryHeaderCell("納入先名", 160.dp, TextAlign.Center)
                            DestHistoryHeaderCell("製品名", 160.dp, TextAlign.Center)
                            DestHistoryHeaderCell("数量", 120.dp, TextAlign.End)
                            DestHistoryHeaderCell("状態", 100.dp, TextAlign.Center)
                            DestHistoryHeaderCell("納入日", 108.dp, TextAlign.Center)
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        items.forEachIndexed { index, row ->
                            DestHistoryAnimatedRow(index = index, key = "${row.date}-${row.productName}-$index") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                        .background(if (index % 2 == 1) Color(0xFFFAFBFC) else Color.White)
                                        .padding(vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    DestHistoryDateCell(row.date, 108.dp)
                                    DestHistoryNameCell(row.destinationName, 160.dp)
                                    DestHistoryNameCell(row.productName, 160.dp)
                                    Box(Modifier.width(120.dp).padding(end = 8.dp), contentAlignment = Alignment.CenterEnd) {
                                        DestHistoryQuantityCell(row.quantity)
                                    }
                                    Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                                        DestHistoryStatusTag(row.status)
                                    }
                                    DestHistoryDateCell(row.deliveryDate.ifBlank { "-" }, 108.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestHistoryPanelCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val cardShape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, spotColor = Color(0x1A6366F1))
            .clip(cardShape)
            .background(Color.White)
            .border(1.dp, Color(0x1A6366F1), cardShape)
            .drawBehind {
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF06B6D4)),
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx()),
                )
            }
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))))
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(15.dp))
                }
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
            trailing()
        }
        content()
    }
}

@Composable
private fun DestHistoryAnimatedRow(
    index: Int,
    key: String,
    content: @Composable () -> Unit,
) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        visible = false
        delay((index.coerceAtMost(12) * 35L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 8 },
    ) {
        content()
    }
}

@Composable
private fun DestHistoryPrintButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(120),
        label = "printBtnScale",
    )
    Box(
        modifier = Modifier
            .height(30.dp)
            .scale(scale)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color.White)))
            .border(1.dp, Color(0x806366F1), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
            Text("印刷", color = Color(0xFF6366F1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DestHistoryStatChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEEF2FF))
            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 11.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DestHistoryEmptyHint(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(22.dp))
        }
        Text(text, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DestHistoryHeaderCell(text: String, width: androidx.compose.ui.unit.Dp, align: TextAlign) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 6.dp),
        contentAlignment = when (align) {
            TextAlign.End -> Alignment.CenterEnd
            TextAlign.Center -> Alignment.Center
            else -> Alignment.CenterStart
        },
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrderMonthlyColors.TableHeaderTint, textAlign = align)
    }
}

@Composable
private fun DestHistoryHeaderCell(text: String, modifier: Modifier, align: TextAlign) {
    Box(
        modifier = modifier.padding(horizontal = 6.dp),
        contentAlignment = when (align) {
            TextAlign.End -> Alignment.CenterEnd
            TextAlign.Center -> Alignment.Center
            else -> Alignment.CenterStart
        },
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrderMonthlyColors.TableHeaderTint, textAlign = align)
    }
}

@Composable
private fun DestHistoryDateCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.width(width), contentAlignment = Alignment.Center) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4F46E5),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DestHistoryNameCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.width(width).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DestHistoryQuantityCell(quantity: Int) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            destHistNumberFormat.format(quantity),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF059669),
            fontFamily = FontFamily.Monospace,
        )
        Text("個", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 1.dp))
    }
}

@Composable
private fun DestHistoryStatusTag(status: String) {
    val display = status.ifBlank { "-" }
    val gradient = when {
        display.contains("完了") || display.contains("出荷済") -> Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF10B981)))
        display.contains("取消") || display.contains("キャンセル") -> Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFEF4444)))
        display.contains("処理中") || display.contains("未出荷") || display.contains("保留") ->
            Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))
        else -> Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
    }
    Text(
        display,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(gradient)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
