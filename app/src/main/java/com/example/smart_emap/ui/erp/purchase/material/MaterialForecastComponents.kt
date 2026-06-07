package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

private data class ForecastKpiSpec(
    val label: String,
    val value: String,
    val unit: String = "",
    val accent: Brush,
    val icon: ImageVector,
)

@Composable
fun MaterialForecastHeroBar(
    actionLoading: Boolean,
    onRefresh: () -> Unit,
    onPrint: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column {
                Text("材料内示管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Material Forecast Management", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ForecastActionBtn("データ更新", Icons.Default.Refresh, Color.White.copy(alpha = 0.18f), !actionLoading, onRefresh)
            ForecastActionBtn("印刷", Icons.Default.Print, Color(0xE6FAAD14), !actionLoading, onPrint)
        }
    }
}

@Composable
private fun ForecastActionBtn(
    text: String,
    icon: ImageVector,
    container: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun MaterialForecastKpiStrip(stats: MaterialForecastStatsDto) {
    val requiredText = stats.totalMaterialRequired?.let { formatRequired(it) } ?: "0"
    val cards = listOf(
        ForecastKpiSpec("製品種類数", jpNumber.format(stats.totalProducts ?: 0), "", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Inventory2),
        ForecastKpiSpec("材料種類数", jpNumber.format(stats.totalMaterials ?: 0), "", Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))), Icons.Default.Inventory2),
        ForecastKpiSpec("仕入先数", jpNumber.format(stats.totalSuppliers ?: 0), "", Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))), Icons.Default.Business),
        ForecastKpiSpec("内示数量合計", jpNumber.format(stats.totalForecastUnits ?: 0), "本", Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))), Icons.AutoMirrored.Filled.TrendingUp),
        ForecastKpiSpec("材料必要数合計", requiredText, "", Brush.linearGradient(listOf(Color(0xFFFFECD2), Color(0xFFFCB69F))), Icons.Default.Analytics),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
        items(cards) { card -> ForecastKpiCard(card) }
    }
}

@Composable
private fun ForecastKpiCard(spec: ForecastKpiSpec) {
    Surface(modifier = Modifier.width(128.dp), shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(spec.accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(spec.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(spec.value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (spec.unit.isNotBlank()) {
                        Text(spec.unit, fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 2.dp, bottom = 1.dp))
                    }
                }
                Text(spec.label, fontSize = 9.sp, color = Color(0xFF64748B), maxLines = 2, lineHeight = 11.sp)
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
    val shape = RoundedCornerShape(10.dp)
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0x1A667EEA), shape)
            .horizontalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ForecastFilterGroup("年") {
            ForecastIntDropdown(
                value = year,
                options = ((year - 3)..(year + 3)).toList(),
                format = { "${it}年" },
                onSelect = onYearChange,
            )
        }
        ForecastFilterGroup("月") {
            ForecastIntDropdown(value = month, options = (1..12).toList(), format = { "${it}月" }, onSelect = onMonthChange)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                ForecastNavBtn(Icons.AutoMirrored.Filled.KeyboardArrowLeft, onPrevMonth)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(forecastAccent)
                        .clickable(onClick = onCurrentMonth)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("今月", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                ForecastNavBtn(Icons.AutoMirrored.Filled.KeyboardArrowRight, onNextMonth)
            }
        }
        ForecastFilterGroup("仕入先") {
            ForecastSupplierDropdown(
                supplierCd = supplierCd,
                options = supplierOptions,
                onSelect = onSupplierChange,
            )
        }
        ForecastFilterGroup("キーワード") {
            Row(
                modifier = Modifier
                    .widthIn(min = 140.dp, max = 220.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                BasicTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (keyword.isEmpty()) {
                            Text("製品名・材料名・仕入先名", fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    },
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .clickable(onClick = onReset)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Text("リセット", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ForecastFilterGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        content()
    }
}

@Composable
private fun ForecastNavBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastIntDropdown(
    value: Int,
    options: List<Int>,
    format: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .width(88.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(format(value), fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f), maxLines = 1)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
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
                .widthIn(min = 100.dp, max = 160.dp)
                .height(28.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = forecastAccent, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
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
    val outerShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, outerShape, spotColor = Color(0x18000000))
            .clip(outerShape)
            .background(Color.White)
            .border(1.dp, Color(0x0F667EEA), outerShape),
    ) {
        MaterialForecastTabStrip(selected = selectedTab, onSelect = onTabSelect)
        HorizontalDivider(color = Color(0xFFE2E8F0))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = forecastAccent, modifier = Modifier.size(28.dp))
            }
        } else {
            content()
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
            .background(Brush.linearGradient(listOf(Color(0xFFF8F9FA), Color(0xFFEEF2F7))))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MaterialForecastTab.entries.forEach { tab ->
            val active = tab == selected
            val icon = if (tab == MaterialForecastTab.Detail) Icons.Default.Description else Icons.Default.Analytics
            val shape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .clip(shape)
                    .then(
                        if (active) {
                            Modifier.background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))).shadow(3.dp, shape)
                        } else {
                            Modifier.background(Color.Transparent)
                        },
                    )
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(icon, contentDescription = null, tint = if (active) Color.White else forecastAccent, modifier = Modifier.size(14.dp))
                    Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else Color(0xFF6B7280))
                }
            }
        }
    }
}

private object ForecastCol {
    val Year = 40.dp
    val Month = 36.dp
    val Supplier = 74.dp
    val Material = 100.dp
    val Product = 88.dp
    val Number = 56.dp
    val Lot = 56.dp
    val Required = 56.dp
    val Count = 44.dp
    val RowHeight = 34.dp
}

@Composable
fun MaterialForecastDetailTable(
    items: List<MaterialForecastDetailDto>,
    stats: MaterialForecastStatsDto,
    useStatsForSummary: Boolean,
) {
    val scroll = rememberScrollState()
    Column(Modifier.horizontalScroll(scroll)) {
        ForecastHeaderRow(
            listOf("年", "月", "仕入先", "材料名", "製品名", "内示数量", "ロットサイズ", "材料必要数"),
            listOf(ForecastCol.Year, ForecastCol.Month, ForecastCol.Supplier, ForecastCol.Material, ForecastCol.Product, ForecastCol.Number, ForecastCol.Lot, ForecastCol.Required),
        )
        items.forEachIndexed { index, row -> ForecastDetailRow(row, index % 2 == 1) }
        if (items.isNotEmpty()) {
            val forecastSum = if (useStatsForSummary) stats.totalForecastUnits ?: 0 else items.sumOf { it.forecastUnits ?: 0 }
            val requiredSum = if (useStatsForSummary) stats.totalMaterialRequired ?: 0.0 else items.sumOf { it.materialRequired ?: 0.0 }
            ForecastDetailSummaryRow(forecastSum, requiredSum)
        }
    }
}

@Composable
private fun ForecastDetailRow(row: MaterialForecastDetailDto, striped: Boolean) {
    ForecastDataRow(striped) {
        forecastCell((row.year ?: 0).toString(), ForecastCol.Year)
        forecastCell((row.month ?: 0).toString(), ForecastCol.Month)
        forecastCell(row.supplierName.orEmpty().take(10), ForecastCol.Supplier, Color(0xFF2563EB))
        forecastCell(row.materialName.orEmpty(), ForecastCol.Material)
        forecastCell(row.productName.orEmpty(), ForecastCol.Product, Color(0xFF334155), bold = true)
        forecastCell(jpNumber.format(row.forecastUnits ?: 0), ForecastCol.Number, alignEnd = true)
        forecastCell(row.lotSize?.toString() ?: "-", ForecastCol.Lot, alignEnd = true)
        forecastCell(formatRequired(row.materialRequired), ForecastCol.Required, Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
private fun ForecastDetailSummaryRow(forecastSum: Int, requiredSum: Double) {
    ForecastDataRow(striped = false, bg = Color(0xFFF1F5F9)) {
        forecastCell("合計", ForecastCol.Year, Color(0xFF475569), bold = true)
        forecastCell("", ForecastCol.Month)
        forecastCell("", ForecastCol.Supplier)
        forecastCell("", ForecastCol.Material)
        forecastCell("", ForecastCol.Product)
        forecastCell(jpNumber.format(forecastSum), ForecastCol.Number, Color(0xFF1E293B), bold = true, alignEnd = true)
        forecastCell("", ForecastCol.Lot)
        forecastCell(formatRequired(requiredSum), ForecastCol.Required, Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
fun MaterialForecastSummaryTable(items: List<MaterialForecastSummaryDto>) {
    val scroll = rememberScrollState()
    Column(Modifier.horizontalScroll(scroll)) {
        ForecastHeaderRow(
            listOf("仕入先", "材料名", "製品数", "内示数量合計", "平均ロットサイズ", "材料必要数合計"),
            listOf(ForecastCol.Supplier, ForecastCol.Material, ForecastCol.Count, ForecastCol.Number, ForecastCol.Lot, ForecastCol.Required + 12.dp),
        )
        items.forEachIndexed { index, row -> ForecastSummaryRow(row, index % 2 == 1) }
    }
}

@Composable
private fun ForecastSummaryRow(row: MaterialForecastSummaryDto, striped: Boolean) {
    ForecastDataRow(striped) {
        forecastCell(row.supplierName.orEmpty().take(10), ForecastCol.Supplier, Color(0xFF2563EB))
        forecastCell(row.materialName.orEmpty(), ForecastCol.Material)
        Box(modifier = Modifier.width(ForecastCol.Count), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE0E7FF)) {
                Text("${row.productCount ?: 0}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
            }
        }
        forecastCell(jpNumber.format(row.totalForecastUnits ?: 0), ForecastCol.Number, alignEnd = true)
        forecastCell(row.avgLotSize?.roundToInt()?.toString() ?: "-", ForecastCol.Lot, alignEnd = true)
        forecastCell(formatRequired(row.totalMaterialRequired), ForecastCol.Required + 12.dp, Color(0xFF2563EB), bold = true, alignEnd = true)
    }
}

@Composable
private fun ForecastHeaderRow(labels: List<String>, widths: List<androidx.compose.ui.unit.Dp>) {
    Row(
        modifier = Modifier
            .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))))
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        labels.forEachIndexed { index, label ->
            Box(modifier = Modifier.width(widths[index]).padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), maxLines = 1, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ForecastDataRow(
    striped: Boolean,
    bg: Color? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .height(ForecastCol.RowHeight)
            .background(bg ?: if (striped) Color(0xFFFAFBFC) else Color.White)
            .drawBehind {
                drawLine(Color(0xFFE2E8F0), Offset(0f, size.height), Offset(size.width, size.height), 1f)
            }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        content = content,
    )
}

@Composable
private fun RowScope.forecastCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = Color(0xFF334155),
    bold: Boolean = false,
    alignEnd: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontSize = 10.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Center,
    )
}

private fun formatRequired(value: Double?): String {
    if (value == null) return "-"
    return if (kotlin.math.abs(value % 1.0) < 0.05) value.toInt().toString() else String.format(Locale.JAPAN, "%.1f", value)
}
