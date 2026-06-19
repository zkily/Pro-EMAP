package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MaterialForecastDetailDto
import com.example.smart_emap.data.model.MaterialForecastStatsDto
import com.example.smart_emap.data.model.MaterialForecastSummaryDto
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

enum class MaterialForecastTab(val label: String) {
    Detail("製品別一覧"),
    Summary("材料別集計"),
}

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)
private val forecastAccent = Color(0xFF667EEA)
private val forecastGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val cardShape = RoundedCornerShape(10.dp)

private data class ForecastKpiSpec(
    val label: String,
    val value: String,
    val unit: String = "",
    val accent: Brush,
    val icon: ImageVector,
)

private data class ForecastColSpec(
    val weight: Float,
    val minWidth: Dp,
    val alignStart: Boolean = false,
)

@Composable
fun MaterialForecastHeroBar(
    actionLoading: Boolean,
    onRefresh: () -> Unit,
    onPrint: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, cardShape, spotColor = Color(0x30667EEA)),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4)),
    ) {
        Box {
            Box(
                Modifier
                    .width(3.dp)
                    .height(52.dp)
                    .background(forecastGradient)
                    .align(Alignment.CenterStart),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(forecastGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("材料内示管理", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A202C))
                        Text("Material Forecast Management", fontSize = 9.sp, color = Color(0xFF718096))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    ForecastActionBtn("データ更新", Icons.Default.Refresh, forecastAccent.copy(alpha = 0.12f), forecastAccent, !actionLoading, onRefresh)
                    ForecastActionBtn("印刷", Icons.Default.Print, Color(0xFFFFF7E6), Color(0xFFD97706), !actionLoading, onPrint)
                }
            }
        }
    }
}

@Composable
private fun ForecastActionBtn(
    text: String,
    icon: ImageVector,
    container: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = contentColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(icon, null, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun MaterialForecastKpiStrip(stats: MaterialForecastStatsDto) {
    val requiredText = stats.totalMaterialRequired?.let { formatRequired(it) } ?: "0"
    val cards = listOf(
        ForecastKpiSpec("製品種類数", jpNumber.format(stats.totalProducts ?: 0), "", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Inventory2),
        ForecastKpiSpec("材料種類数", jpNumber.format(stats.totalMaterials ?: 0), "", Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))), Icons.Default.Inventory2),
        ForecastKpiSpec("仕入先数", jpNumber.format(stats.totalSuppliers ?: 0), "", Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))), Icons.Default.Business),
        ForecastKpiSpec("内示数量合計", jpNumber.format(stats.totalForecastUnits ?: 0), "本", Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))), Icons.AutoMirrored.Filled.TrendingUp),
        ForecastKpiSpec("材料必要数合計", requiredText, "", Brush.linearGradient(listOf(Color(0xFFFF8C00), Color(0xFFFFA500))), Icons.Default.Analytics),
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 6.dp
        val perCardWidth = (maxWidth - gap * 4) / 5
        val compact = perCardWidth < 110.dp
        val hideIcon = perCardWidth < 82.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cards.forEach { card ->
                ForecastKpiCard(
                    spec = card,
                    modifier = Modifier.weight(1f),
                    compact = compact,
                    hideIcon = hideIcon,
                )
            }
        }
    }
}

@Composable
private fun ForecastKpiCard(spec: ForecastKpiSpec, modifier: Modifier, compact: Boolean, hideIcon: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    val iconSize = if (hideIcon) 0.dp else if (compact) 24.dp else 28.dp
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 40.dp else 48.dp),
        shape = shape,
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x0A000000)),
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(spec.accent))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = if (compact) 5.dp else 7.dp, vertical = if (compact) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
            ) {
                if (!hideIcon) {
                    Box(
                        modifier = Modifier.size(iconSize).clip(RoundedCornerShape(6.dp)).background(spec.accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(spec.icon, null, tint = Color.White, modifier = Modifier.size(if (compact) 11.dp else 13.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            spec.value,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 12.sp else 14.sp,
                            color = Color(0xFF1A202C),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (spec.unit.isNotBlank()) {
                            Text(spec.unit, fontSize = 8.sp, color = Color(0xFF718096), modifier = Modifier.padding(start = 2.dp, bottom = 1.dp))
                        }
                    }
                    Text(
                        spec.label,
                        fontSize = if (compact) 7.sp else 8.sp,
                        color = Color(0xFF718096),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 9.sp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialForecastFilterBar(
    year: Int,
    month: Int,
    keyword: String,
    supplierCd: String,
    supplierOptions: List<Pair<String, String>>,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onKeywordChange: (String) -> Unit,
    onSupplierChange: (String) -> Unit,
    onReset: () -> Unit,
) {
    val filterShape = RoundedCornerShape(10.dp)
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, filterShape, spotColor = Color(0x12000000))
            .clip(filterShape)
            .background(Color.White)
            .border(1.dp, Color(0x1A667EEA), filterShape)
            .horizontalScroll(scroll)
            .heightIn(min = 36.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForecastFilterSection(isFirst = true) {
            ForecastFilterLabel(Icons.Default.CalendarMonth, "年")
            ForecastIntDropdown(year, ((year - 3)..(year + 3)).toList(), { "${it}年" }, onYearChange, Modifier.width(82.dp))
        }
        ForecastFilterDivider()
        ForecastFilterSection {
            ForecastFilterLabel(Icons.Default.CalendarMonth, "月")
            ForecastIntDropdown(month, (1..12).toList(), { "${it}月" }, onMonthChange, Modifier.width(72.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                ForecastNavBtn(Icons.AutoMirrored.Filled.KeyboardArrowLeft, onPrevMonth)
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(forecastGradient)
                        .clickable(onClick = onCurrentMonth)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("今月", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                ForecastNavBtn(Icons.AutoMirrored.Filled.KeyboardArrowRight, onNextMonth)
            }
        }
        ForecastFilterDivider()
        ForecastFilterSection {
            ForecastFilterLabel(Icons.Default.Person, "仕入先")
            ForecastSupplierDropdown(supplierCd, supplierOptions, onSupplierChange)
        }
        ForecastFilterDivider()
        ForecastFilterSection {
            ForecastFilterLabel(Icons.Default.Search, "キーワード")
            ForecastKeywordField(keyword, onKeywordChange)
        }
        ForecastFilterDivider()
        ForecastFilterSection(isLast = true) {
            ForecastResetBtn(onReset)
        }
    }
}

@Composable
private fun RowScope.ForecastFilterSection(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(
            start = if (isFirst) 0.dp else 10.dp,
            end = if (isLast) 0.dp else 10.dp,
        ),
        content = content,
    )
}

@Composable
private fun ForecastFilterLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, null, tint = forecastAccent, modifier = Modifier.size(12.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), maxLines = 1)
    }
}

@Composable
private fun ForecastKeywordField(value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .widthIn(min = 140.dp, max = 200.dp)
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("製品名・材料名・仕入先", fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                inner()
            },
        )
    }
}

@Composable
private fun ForecastResetBtn(onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Default.Refresh, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
        Text("リセット", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ForecastFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(Color(0xFFE5E7EB)),
    )
}

@Composable
private fun ForecastNavBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastIntDropdown(
    value: Int,
    options: List<Int>,
    format: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(format(value), fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(format(option), fontSize = 12.sp) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastSupplierDropdown(
    supplierCd: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.first == supplierCd }?.second ?: if (supplierCd.isBlank()) "全て" else supplierCd
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .widthIn(min = 120.dp, max = 168.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 11.sp, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("全て", fontSize = 12.sp) }, onClick = { onSelect(""); expanded = false })
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = { onSelect(cd); expanded = false },
                )
            }
        }
    }
}

@Composable
fun MaterialForecastTablePanel(
    selectedTab: MaterialForecastTab,
    onTabSelect: (MaterialForecastTab) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0x0F667EEA)),
    ) {
        Column {
            MaterialForecastTabStrip(selected = selectedTab, onSelect = onTabSelect)
            HorizontalDivider(color = Color(0xFFE2E8F0))
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "forecast-table-loading",
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = forecastAccent, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    }
                } else {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                        label = "forecast-tab-content",
                    ) {
                        Box(Modifier.padding(bottom = 4.dp)) { content() }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialForecastTabStrip(
    selected: MaterialForecastTab,
    onSelect: (MaterialForecastTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFBFC))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MaterialForecastTab.entries.forEach { tab ->
            val active = tab == selected
            val icon = if (tab == MaterialForecastTab.Detail) Icons.Default.Description else Icons.Default.Analytics
            val shape = RoundedCornerShape(7.dp)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .clickable { onSelect(tab) },
                shape = shape,
                color = if (active) forecastAccent.copy(alpha = 0.1f) else Color.Transparent,
                border = BorderStroke(1.dp, if (active) forecastAccent.copy(alpha = 0.35f) else Color(0xFFE5E7EB)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, null, tint = if (active) forecastAccent else Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tab.label, fontSize = 10.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, color = if (active) forecastAccent else Color(0xFF6B7280))
                }
            }
        }
    }
}

private val detailCols = listOf(
    ForecastColSpec(0.55f, 36.dp),
    ForecastColSpec(0.55f, 32.dp),
    ForecastColSpec(1.2f, 72.dp, alignStart = true),
    ForecastColSpec(1.35f, 80.dp, alignStart = true),
    ForecastColSpec(1.35f, 80.dp, alignStart = true),
    ForecastColSpec(0.85f, 52.dp),
    ForecastColSpec(0.85f, 52.dp),
    ForecastColSpec(0.95f, 56.dp),
)

private val summaryCols = listOf(
    ForecastColSpec(1.2f, 72.dp, alignStart = true),
    ForecastColSpec(1.35f, 80.dp, alignStart = true),
    ForecastColSpec(0.75f, 44.dp),
    ForecastColSpec(0.95f, 56.dp),
    ForecastColSpec(0.95f, 56.dp),
    ForecastColSpec(1.0f, 64.dp),
)

private fun minTableWidth(cols: List<ForecastColSpec>): Dp =
    cols.fold(0.dp) { acc, c -> acc + c.minWidth } + 3.dp * (cols.size - 1).coerceAtLeast(0) + 10.dp

@Composable
fun MaterialForecastDetailTable(
    items: List<MaterialForecastDetailDto>,
    stats: MaterialForecastStatsDto,
    useStatsForSummary: Boolean,
) {
    val labels = listOf("年", "月", "仕入先", "材料名", "製品名", "内示数量", "ロットサイズ", "材料必要数")
    val hScroll = rememberScrollState()
    val minW = minTableWidth(detailCols)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val needsScroll = minW > maxWidth
        val mod = if (needsScroll) Modifier.horizontalScroll(hScroll).widthIn(min = minW) else Modifier.fillMaxWidth()
        Column(mod.padding(horizontal = 4.dp, vertical = 4.dp)) {
            ForecastHeaderRow(labels, detailCols)
            items.forEachIndexed { index, row ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(160, delayMillis = (index.coerceAtMost(12)) * 25)),
                ) {
                    ForecastDetailRow(row, index % 2 == 1)
                }
            }
            if (items.isNotEmpty()) {
                val forecastSum = if (useStatsForSummary) stats.totalForecastUnits ?: 0 else items.sumOf { it.forecastUnits ?: 0 }
                val requiredSum = if (useStatsForSummary) stats.totalMaterialRequired ?: 0.0 else items.sumOf { it.materialRequired ?: 0.0 }
                ForecastDetailSummaryRow(forecastSum, requiredSum)
            }
        }
    }
}

@Composable
private fun ForecastDetailRow(row: MaterialForecastDetailDto, striped: Boolean) {
    ForecastDataRow(striped) {
        ForecastBodyCell(detailCols[0], (row.year ?: 0).toString())
        ForecastBodyCell(detailCols[1], (row.month ?: 0).toString())
        ForecastBodyCell(detailCols[2], row.supplierName.orEmpty(), Color(0xFF2563EB))
        ForecastBodyCell(detailCols[3], row.materialName.orEmpty())
        ForecastBodyCell(detailCols[4], row.productName.orEmpty(), Color(0xFF334155), bold = true)
        ForecastBodyCell(detailCols[5], jpNumber.format(row.forecastUnits ?: 0), alignEnd = true)
        ForecastBodyCell(detailCols[6], row.lotSize?.toString() ?: "-", alignEnd = true)
        ForecastBodyCell(detailCols[7], formatRequired(row.materialRequired), Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
private fun ForecastDetailSummaryRow(forecastSum: Int, requiredSum: Double) {
    ForecastDataRow(striped = false, bg = Color(0xFFF1F5F9)) {
        ForecastBodyCell(detailCols[0], "合計", Color(0xFF475569), bold = true)
        ForecastBodyCell(detailCols[1], "")
        ForecastBodyCell(detailCols[2], "")
        ForecastBodyCell(detailCols[3], "")
        ForecastBodyCell(detailCols[4], "")
        ForecastBodyCell(detailCols[5], jpNumber.format(forecastSum), Color(0xFF1E293B), bold = true, alignEnd = true)
        ForecastBodyCell(detailCols[6], "")
        ForecastBodyCell(detailCols[7], formatRequired(requiredSum), Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
fun MaterialForecastSummaryTable(items: List<MaterialForecastSummaryDto>) {
    val labels = listOf("仕入先", "材料名", "製品数", "内示数量合計", "平均ロットサイズ", "材料必要数合計")
    val hScroll = rememberScrollState()
    val minW = minTableWidth(summaryCols)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val needsScroll = minW > maxWidth
        val mod = if (needsScroll) Modifier.horizontalScroll(hScroll).widthIn(min = minW) else Modifier.fillMaxWidth()
        Column(mod.padding(horizontal = 4.dp, vertical = 4.dp)) {
            ForecastHeaderRow(labels, summaryCols)
            items.forEachIndexed { index, row ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(160, delayMillis = (index.coerceAtMost(12)) * 25)),
                ) {
                    ForecastSummaryRow(row, index % 2 == 1)
                }
            }
        }
    }
}

@Composable
private fun ForecastSummaryRow(row: MaterialForecastSummaryDto, striped: Boolean) {
    ForecastDataRow(striped) {
        ForecastBodyCell(summaryCols[0], row.supplierName.orEmpty(), Color(0xFF2563EB))
        ForecastBodyCell(summaryCols[1], row.materialName.orEmpty())
        Box(Modifier.weight(summaryCols[2].weight).widthIn(min = summaryCols[2].minWidth), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE0E7FF)) {
                Text("${row.productCount ?: 0}", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
            }
        }
        ForecastBodyCell(summaryCols[3], jpNumber.format(row.totalForecastUnits ?: 0), alignEnd = true)
        ForecastBodyCell(summaryCols[4], row.avgLotSize?.roundToInt()?.toString() ?: "-", alignEnd = true)
        ForecastBodyCell(summaryCols[5], formatRequired(row.totalMaterialRequired), Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
private fun ForecastHeaderRow(labels: List<String>, cols: List<ForecastColSpec>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE8EDF3))), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        labels.forEachIndexed { i, label ->
            ForecastHeaderCell(cols[i], label)
        }
    }
}

@Composable
private fun RowScope.ForecastHeaderCell(spec: ForecastColSpec, label: String) {
    Text(
        label,
        modifier = Modifier.weight(spec.weight).widthIn(min = spec.minWidth).padding(horizontal = 2.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF475569),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (spec.alignStart) TextAlign.Start else TextAlign.Center,
        lineHeight = 11.sp,
    )
}

@Composable
private fun RowScope.ForecastBodyCell(
    spec: ForecastColSpec,
    text: String,
    color: Color = Color(0xFF334155),
    bold: Boolean = false,
    alignEnd: Boolean = false,
) {
    Text(
        text.ifBlank { if (bold) text else "—" },
        modifier = Modifier.weight(spec.weight).widthIn(min = spec.minWidth).padding(horizontal = 2.dp),
        fontSize = 10.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = when {
            alignEnd -> TextAlign.End
            spec.alignStart -> TextAlign.Start
            else -> TextAlign.Center
        },
    )
}

@Composable
private fun ForecastDataRow(
    striped: Boolean,
    bg: Color? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 32.dp)
            .background(bg ?: if (striped) Color(0xFFFAFBFC) else Color.White)
            .drawBehind {
                drawLine(Color(0xFFE8ECF4), Offset(0f, size.height), Offset(size.width, size.height), 1f)
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        content = content,
    )
}

private fun formatRequired(value: Double?): String {
    if (value == null) return "-"
    return if (kotlin.math.abs(value % 1.0) < 0.05) value.toInt().toString() else String.format(Locale.JAPAN, "%.1f", value)
}
