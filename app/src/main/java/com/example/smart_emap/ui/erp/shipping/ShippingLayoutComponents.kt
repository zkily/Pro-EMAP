package com.example.smart_emap.ui.erp.shipping

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ShippingDarkBg = Brush.linearGradient(
    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
)

private val ShippingLightBg = Brush.linearGradient(
    listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF), Color(0xFFF1F5F9)),
)

@Composable
fun ShippingDashboardBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(ShippingDarkBg)) { content() }
}

@Composable
fun ShippingLightPageBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(ShippingLightBg)) { content() }
}

@Composable
fun ShippingGlassHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    loading: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    dark: Boolean = true,
) {
    val bg = if (dark) Color(0xFF1E293B).copy(0.88f) else Color.White.copy(0.92f)
    val border = if (dark) Color.White.copy(0.1f) else Color(0xFFE2E8F0)
    val titleColor = if (dark) Color.White else Color(0xFF0F172A)
    val subColor = if (dark) Color.White.copy(0.62f) else Color(0xFF64748B)
    val accent = Color(0xFF6366F1)

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = accent.copy(0.14f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = titleColor)
                Text(subtitle, fontSize = 10.sp, color = subColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            trailing?.invoke()
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.size(34.dp)) {
                    if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = accent)
                    else Icon(Icons.Default.Refresh, null, tint = accent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShippingStatCardsRow(
    cards: List<Pair<String, String>>,
    accentColors: List<Color>,
    onCardClick: ((Int) -> Unit)? = null,
    dark: Boolean = true,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        cards.forEachIndexed { index, (label, value) ->
            val color = accentColors.getOrElse(index % accentColors.size) { Color(0xFF6366F1) }
            ShippingStatCard(label, value, color, dark, Modifier.weight(1f, fill = false).widthIn(min = 96.dp)) {
                onCardClick?.invoke(index)
            }
        }
    }
}

@Composable
fun ShippingStatCard(
    label: String,
    value: String,
    accent: Color,
    dark: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickable = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Surface(
        modifier = modifier.then(clickable).height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (dark) Color.White.copy(0.06f) else Color.White.copy(0.95f),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(0.08f) else Color(0xFFE2E8F0)),
    ) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(3.dp, 22.dp).clip(RoundedCornerShape(2.dp)).background(accent))
            Column {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White.copy(0.55f) else Color(0xFF64748B))
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (dark) Color.White else Color(0xFF0F172A))
            }
        }
    }
}

@Composable
fun ShippingModuleGrid(
    modules: List<ShippingModuleCard>,
    onNavigate: (String) -> Unit,
    startIndex: Int = 0,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        modules.forEachIndexed { index, module ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(280, delayMillis = (startIndex + index) * 40)) +
                    slideInVertically(tween(280, delayMillis = (startIndex + index) * 40)) { it / 3 },
            ) {
                ShippingModuleCardItem(module) { onNavigate(module.path) }
            }
        }
    }
}

@Composable
fun ShippingModuleCardItem(module: ShippingModuleCard, onClick: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(Color(module.startColor), Color(module.endColor)))
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(0.08f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(gradient), contentAlignment = Alignment.Center) {
                Icon(module.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(module.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(module.description, fontSize = 10.sp, color = Color.White.copy(0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(0.5f))
        }
    }
}

@Composable
fun ShippingFilterBar(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onPrevDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onSearch: () -> Unit,
    loading: Boolean = false,
    extra: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(0.95f),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(startDate, onStartChange, label = { Text("開始", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 12.sp))
                Text("～", fontSize = 11.sp, color = Color(0xFF64748B))
                OutlinedTextField(endDate, onEndChange, label = { Text("終了", fontSize = 10.sp) }, modifier = Modifier.weight(1f), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 12.sp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                ShippingChipButton("前日", onPrevDay)
                ShippingChipButton("今日", onToday, filled = true)
                ShippingChipButton("翌日", onNextDay)
                extra()
                Spacer(Modifier.weight(1f))
                Button(onSearch, enabled = !loading, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) {
                    if (loading) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text("検索", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ShippingChipButton(label: String, onClick: () -> Unit, filled: Boolean = false) {
    val bg = if (filled) Color(0xFF6366F1) else Color(0xFFF1F5F9)
    val fg = if (filled) Color.White else Color(0xFF475569)
    Surface(onClick, shape = RoundedCornerShape(8.dp), color = bg) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
fun ShippingTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    ScrollableTabRow(selected, edgePadding = 0.dp, divider = {}, indicator = {}, modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                modifier = Modifier.height(36.dp),
                text = {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected == index) Color(0xFF4F46E5) else Color(0xFF64748B),
                    )
                },
            )
        }
    }
}

@Composable
fun ShippingDataTable(
    columns: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    emptyText: String = "データがありません",
) {
    if (rows.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(emptyText, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        return
    }
    val scroll = rememberScrollState()
    Surface(modifier, shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.98f), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Column {
            Row(
                Modifier.horizontalScroll(scroll).background(Color(0xFFF8FAFC)).padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                columns.forEach { col ->
                    Text(col, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.widthIn(min = 72.dp))
                }
            }
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(rows) { index, row ->
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
                    Row(
                        Modifier.fillMaxWidth().background(bg).horizontalScroll(scroll).padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { cell ->
                            Text(cell, fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.widthIn(min = 72.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShippingSectionTitle(title: String, badge: String? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF334155))
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFEEF2FF)) {
                Text(badge, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ShippingProgressRing(percent: Float, label: String, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(percent.coerceIn(0f, 100f) / 100f, animationSpec = tween(600), label = "ring")
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animated },
                modifier = Modifier.size(52.dp),
                strokeWidth = 5.dp,
                color = Color(0xFF6366F1),
                trackColor = Color(0xFFE2E8F0),
            )
            Text("${(animated * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
        }
        Text(label, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ShippingFadeIn(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300, delayMillis = index * 35)) + slideInVertically(tween(300, delayMillis = index * 35)) { it / 4 },
    ) { content() }
}
