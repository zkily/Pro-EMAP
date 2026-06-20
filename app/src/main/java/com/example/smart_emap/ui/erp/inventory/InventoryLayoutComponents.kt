package com.example.smart_emap.ui.erp.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InventoryDarkBg = Brush.linearGradient(
    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
)

private val InventoryLightBg = Brush.linearGradient(
    listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFF1F5F9)),
)

val InventoryAccentGradient = Brush.linearGradient(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
)

val InventoryGlassBg = Color.White.copy(alpha = 0.7f)
val InventoryGlassBorder = Color.White.copy(alpha = 0.5f)

@Composable
fun InventoryDashboardBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InventoryDarkBg),
    ) {
        content()
    }
}

@Composable
fun InventoryLightPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InventoryLightBg),
    ) {
        content()
    }
}

@Composable
fun InventoryGlassHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    loading: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    dark: Boolean = true,
) {
    val bg = if (dark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.85f)
    val border = if (dark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.6f)
    val titleColor = if (dark) Color.White else Color(0xFF1E293B)
    val subColor = if (dark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
    val iconTint = if (dark) Color(0xFF818CF8) else Color(0xFF6366F1)

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = titleColor, letterSpacing = 0.5.sp)
                Text(subtitle, fontSize = 11.sp, color = subColor, fontWeight = FontWeight.Medium)
            }
            trailing?.invoke()
            if (onRefresh != null) {
                IconButton(
                    onClick = onRefresh,
                    enabled = !loading,
                    modifier = Modifier.size(36.dp).background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.5.dp, color = iconTint)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryQuickNavBar(
    routes: List<InventoryQuickRoute>,
    onNavigate: (String) -> Unit,
    dark: Boolean = true,
) {
    val bg = if (dark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.95f)
    val border = if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            routes.forEach { route ->
                val gradient = Brush.linearGradient(
                    listOf(Color(route.startColor), Color(route.endColor)),
                )
                Surface(
                    modifier = Modifier.clickable { onNavigate(route.path) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (dark) Color.White.copy(alpha = 0.06f) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, border),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(gradient),
                        )
                        Text(
                            route.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryStatCardsGrid(
    fields: List<InventoryStatField>,
    values: Map<String, Int>,
    dark: Boolean = true,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4,
    ) {
        fields.forEachIndexed { index, field ->
            val value = values[field.key] ?: 0
            val color = when (index % 4) {
                0 -> Color(0xFF6366F1)
                1 -> Color(0xFF10B981)
                2 -> Color(0xFFF59E0B)
                else -> Color(0xFFEC4899)
            }
            InventoryStatCard(
                label = field.label,
                value = formatInventoryNum(value),
                color = color,
                dark = dark,
                modifier = Modifier.weight(1f).minWidth(80.dp),
            )
        }
    }
}

@Composable
fun InventoryStatCard(
    label: String,
    value: String,
    color: Color,
    dark: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (dark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.1f) else Color.White),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (dark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = if (dark) Color.White else Color(0xFF1E293B),
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
fun InventoryCategoryHeader(
    title: String,
    subtitle: String,
    accentColor: Color,
    dark: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor),
        )
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (dark) Color.White else Color(0xFF1E293B))
        Text(subtitle, fontSize = 9.sp, color = if (dark) Color.White.copy(alpha = 0.55f) else Color(0xFF94A3B8))
    }
}

@Composable
fun InventoryFilterPanel(
    title: String = "検索フィルタ",
    dark: Boolean = false,
    onReset: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val bg = if (dark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
    val border = if (dark) Color.White.copy(alpha = 0.1f) else Color.White
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(3.dp, 14.dp).background(Color(0xFF6366F1), RoundedCornerShape(1.5.dp)))
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (dark) Color.White else Color(0xFF334155))
                }
                if (onReset != null) {
                    TextButton(
                        onClick = onReset,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Text("リセット", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun InventoryChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    allLabel: String = "全て",
    dark: Boolean = false,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(allLabel).plus(options).forEach { option ->
            val value = if (option == allLabel) "" else option
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(option, fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6366F1),
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
fun InventoryModernTable(
    title: String,
    countLabel: String,
    loading: Boolean,
    dark: Boolean = false,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val bg = if (dark) Color(0xFF0F172A).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.6f)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (dark) Color.White else Color(0xFF1E293B))
            Text(countLabel, fontSize = 11.sp, color = if (dark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.1f) else Color.White),
        ) {
            Column {
                header()
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()
                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp).background(bg.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Color(0xFF6366F1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryTablePanel(
    title: String,
    countLabel: String,
    loading: Boolean,
    dark: Boolean = false,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) = InventoryModernTable(title, countLabel, loading, dark, header, content)

@Composable
fun InventoryTableHeaderCell(text: String, width: Int, dark: Boolean = false) {
    Text(
        text,
        modifier = Modifier.width(width.dp).padding(vertical = 8.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (dark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun InventoryTableCell(
    text: String,
    width: Int,
    dark: Boolean = false,
    bold: Boolean = false,
    fontSize: Int = 12,
    textAlign: TextAlign = TextAlign.Center,
    color: Color? = null,
) {
    Text(
        text,
        modifier = Modifier.width(width.dp).padding(vertical = 6.dp),
        fontSize = fontSize.sp,
        fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Medium,
        color = color ?: (if (dark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155)),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
    )
}

@Composable
fun InventoryPaginationBar(
    page: Int,
    pageSize: Int,
    total: Int,
    pageSizes: List<Int> = listOf(20, 50, 100, 200),
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    dark: Boolean = false,
) {
    val totalPages = maxOf(1, (total + pageSize - 1) / pageSize)
    val bg = if (dark) Color.White.copy(alpha = 0.06f) else Color.White
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${total}件 · ${page}/${totalPages}ページ",
                fontSize = 10.sp,
                color = if (dark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onPageChange(maxOf(1, page - 1)) }, enabled = page > 1, modifier = Modifier.size(28.dp)) {
                    Text("<", fontSize = 12.sp)
                }
                pageSizes.forEach { size ->
                    FilterChip(
                        selected = pageSize == size,
                        onClick = { onPageSizeChange(size) },
                        label = { Text("${size}件", fontSize = 9.sp) },
                    )
                }
                IconButton(onClick = { onPageChange(minOf(totalPages, page + 1)) }, enabled = page < totalPages, modifier = Modifier.size(28.dp)) {
                    Text(">", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InventoryModuleGrid(
    modules: List<InventoryQuickRoute>,
    onNavigate: (String) -> Unit,
    dark: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        modules.forEach { module ->
            val gradient = Brush.linearGradient(listOf(Color(module.startColor), Color(module.endColor)))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(module.path) },
                shape = RoundedCornerShape(12.dp),
                color = if (dark) Color.White.copy(alpha = 0.06f) else Color.White,
                border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(gradient),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            module.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dark) Color.White else Color(0xFF1E293B),
                        )
                    }
                    Text("›", fontSize = 18.sp, color = if (dark) Color.White.copy(alpha = 0.5f) else Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
fun InventoryListDateFilter(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onPrev: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
    dark: Boolean = false,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InventoryFilterActionButton(text = "◀", onClick = onPrev, dark = dark)

        Surface(
            modifier = Modifier.weight(1f),
            color = if (dark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.1f) else Color.White),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InventoryDatePill(
                    text = startDate,
                    onClick = { showStartPicker = true },
                    dark = dark,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "至",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dark) Color.White.copy(alpha = 0.3f) else Color(0xFF94A3B8),
                )
                InventoryDatePill(
                    text = endDate,
                    onClick = { showEndPicker = true },
                    dark = dark,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        InventoryFilterActionButton(text = "当", onClick = onToday, dark = dark)
        InventoryFilterActionButton(text = "▶", onClick = onNext, dark = dark)
    }

    if (showStartPicker) {
        com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog(
            value = startDate,
            accent = Color(0xFF6366F1),
            onDismiss = { showStartPicker = false },
            onConfirm = { onStartChange(it); showStartPicker = false },
        )
    }
    if (showEndPicker) {
        com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog(
            value = endDate,
            accent = Color(0xFF6366F1),
            onDismiss = { showEndPicker = false },
            onConfirm = { onEndChange(it); showEndPicker = false },
        )
    }
}

@Composable
fun InventoryFilterActionButton(
    text: String,
    onClick: () -> Unit,
    dark: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (dark) Color.White.copy(alpha = 0.08f) else Color(0xFFEEF2FF),
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.1f) else Color(0xFFD9E2FF)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun InventoryDatePill(
    text: String,
    onClick: () -> Unit,
    dark: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (dark) Color.White.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (dark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.minWidth(width: androidx.compose.ui.unit.Dp) = this.then(Modifier.widthIn(min = width))

