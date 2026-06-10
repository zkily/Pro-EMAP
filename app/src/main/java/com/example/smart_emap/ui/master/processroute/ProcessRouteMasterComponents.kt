package com.example.smart_emap.ui.master.processroute

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterProcessRouteDto
import com.example.smart_emap.data.model.MasterRouteStepDto
import kotlin.math.max
import kotlin.math.min

val processRoutePageBackground = Brush.linearGradient(listOf(Color(0xFFF0F4F8), Color(0xFFD9E2EC)))
private val processRouteGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private const val PROCESS_ROUTE_SUBTITLE = "工程ルートの登録・編集・ステップ管理を行います"

private val filterControlHeight = 34.dp
private val tableRowHeight = 40.dp
private val tableHeaderHeight = 40.dp

private data class RouteTableColumn(val key: String, val label: String, val widthDp: Int, val align: TextAlign = TextAlign.Start)

private val routeTableColumns = listOf(
    RouteTableColumn("route_cd", "ルートCD", 96, TextAlign.Center),
    RouteTableColumn("route_name", "ルート名", 120),
    RouteTableColumn("description", "説明", 140),
    RouteTableColumn("is_active", "使用", 56, TextAlign.Center),
    RouteTableColumn("is_default", "デフォルト", 72, TextAlign.Center),
    RouteTableColumn("actions", "操作", 148, TextAlign.Center),
)

private val routeTableWidth = (routeTableColumns.sumOf { it.widthDp } + 16).dp

private data class StepTableColumn(val key: String, val label: String, val widthDp: Int, val align: TextAlign = TextAlign.Start)

private val stepTableColumns = listOf(
    StepTableColumn("step_no", "順番", 56, TextAlign.Center),
    StepTableColumn("process_name", "工程", 120),
    StepTableColumn("yield_percent", "歩留率(%)", 72, TextAlign.End),
    StepTableColumn("cycle_sec", "サイクル(s)", 80, TextAlign.End),
    StepTableColumn("remarks", "備考", 100),
    StepTableColumn("actions", "操作", 88, TextAlign.Center),
)

private val stepTableWidth = (stepTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun ProcessRouteHeroBar(total: Int, displayed: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(processRouteGradient)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Route, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("工程ルートマスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(PROCESS_ROUTE_SUBTITLE, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProcessRouteStatChip(total.toString(), "総件数")
            ProcessRouteStatChip(displayed.toString(), "表示件数")
        }
    }
}

@Composable
fun ProcessRouteStepHeroBar(routeCd: String, routeName: String?, stepCount: Int, onBack: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(processRouteGradient)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable(onClick = onBack),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("戻る", color = Color.White, fontSize = 11.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("ルートステップ編集", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.15f)) {
                    Text(
                        "${routeName.orEmpty()} ($routeCd)",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                    )
                }
            }
            ProcessRouteStatChip(stepCount.toString(), "ステップ数")
        }
    }
}

@Composable
private fun ProcessRouteStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 20.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
fun ProcessRouteFilterCard(
    keyword: String,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onAdd: () -> Unit,
) {
    val buttonScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                }
                Row(
                    modifier = Modifier.horizontalScroll(buttonScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onClear,
                        enabled = !actionLoading,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                        Text("クリア", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    ProcessRouteGradientButton("ルート追加", processRouteGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🔍 キーワード検索", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(filterControlHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFAFBFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                if (keyword.isEmpty()) {
                                    Text("ルートCD・名称・説明", fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                                }
                                inner()
                            }
                        },
                    )
                }
                ProcessRouteGradientButton("検索", processRouteGradient, Icons.Default.Search, enabled = !actionLoading, onClick = onSearch)
            }
            if (hasActiveFilters) {
                Row(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF)) {
                        Text("キーワード: $keyword", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 11.sp, color = Color(0xFF667EEA))
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessRouteStepActionBar(actionLoading: Boolean, onAddStep: () -> Unit, onSaveOrder: () -> Unit, hasSteps: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProcessRouteGradientButton("ステップ追加", processRouteGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAddStep)
        ProcessRouteGradientButton(
            "順序保存",
            Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            Icons.Default.Save,
            enabled = !actionLoading && hasSteps,
            onClick = onSaveOrder,
        )
    }
}

@Composable
private fun ProcessRouteGradientButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .then(
                if (enabled) Modifier.background(brush, shape).clickable(onClick = onClick)
                else Modifier.background(Color(0xFFE2E8F0), shape),
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun ProcessRouteTable(
    routes: List<MasterProcessRouteDto>,
    loading: Boolean,
    total: Int,
    modifier: Modifier = Modifier,
    onSteps: (MasterProcessRouteDto) -> Unit,
    onEdit: (MasterProcessRouteDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    Card(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                Row(
                    modifier = Modifier
                        .width(routeTableWidth)
                        .height(tableHeaderHeight)
                        .background(processRouteGradient)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    routeTableColumns.forEach { col ->
                        Text(
                            col.label,
                            modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = col.align,
                            maxLines = 1,
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().heightIn(min = 140.dp)) {
                when {
                    loading && routes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                        }
                    }
                    routes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("データがありません", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(vScroll).horizontalScroll(hScroll),
                        ) {
                            routes.forEachIndexed { index, route -> ProcessRouteTableRow(route, index, onSteps, onEdit, onDelete) }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "表示件数: ${routes.size} / $total",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun ProcessRouteTableRow(
    route: MasterProcessRouteDto,
    index: Int,
    onSteps: (MasterProcessRouteDto) -> Unit,
    onEdit: (MasterProcessRouteDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val id = route.id ?: return
    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    Row(
        modifier = Modifier.width(routeTableWidth).height(tableRowHeight).background(bg).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.width(96.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                route.routeCd.orEmpty(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF667EEA),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ProcessRouteTextCell(route.routeName.orEmpty(), 120, FontWeight.Medium)
        ProcessRouteTextCell(route.description.orEmpty().ifBlank { "—" }, 140, color = Color(0xFF64748B))
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            ProcessRouteStatusBadge(route.isActive != false)
        }
        Box(Modifier.width(72.dp), contentAlignment = Alignment.Center) {
            if (route.isDefault == true) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            } else {
                Text("—", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            }
        }
        Row(
            modifier = Modifier.width(148.dp).padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProcessRouteActionChip("ステップ", Color(0xFF667EEA), Icons.Default.List) { onSteps(route) }
            ProcessRouteActionChip("編集", Color(0xFFF59E0B), Icons.Default.Edit) { onEdit(route) }
            ProcessRouteActionChip("削除", Color(0xFFEF4444), Icons.Default.Delete) { onDelete(id) }
        }
    }
}

@Composable
fun ProcessRouteStepTable(
    steps: List<MasterRouteStepDto>,
    loading: Boolean,
    modifier: Modifier = Modifier,
    onEdit: (MasterRouteStepDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    Card(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                Row(
                    modifier = Modifier
                        .width(stepTableWidth)
                        .height(tableHeaderHeight)
                        .background(processRouteGradient)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    stepTableColumns.forEach { col ->
                        Text(
                            col.label,
                            modifier = Modifier.width(col.widthDp.dp).padding(horizontal = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = col.align,
                            maxLines = 1,
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().heightIn(min = 120.dp)) {
                when {
                    loading && steps.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                        }
                    }
                    steps.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ステップなし", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(vScroll).horizontalScroll(hScroll)) {
                            steps.forEachIndexed { index, step -> ProcessRouteStepTableRow(step, index, onEdit, onDelete) }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "ステップ数: ${steps.size}",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun ProcessRouteStepTableRow(
    step: MasterRouteStepDto,
    index: Int,
    onEdit: (MasterRouteStepDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val id = step.id ?: return
    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    Row(
        modifier = Modifier.width(stepTableWidth).height(tableRowHeight).background(bg).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF2FF)) {
                Text(
                    step.stepNo?.toString().orEmpty(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA),
                )
            }
        }
        ProcessRouteTextCell(step.processName.orEmpty().ifBlank { step.processCd.orEmpty() }, 120, FontWeight.Medium)
        ProcessRouteTextCell(step.yieldPercent?.toString() ?: "100", 72, color = Color(0xFF374151), align = TextAlign.End, mono = true)
        ProcessRouteTextCell(step.cycleSec?.toString() ?: "0", 80, color = Color(0xFF374151), align = TextAlign.End, mono = true)
        ProcessRouteTextCell(step.remarks.orEmpty().ifBlank { "—" }, 100, color = Color(0xFF64748B))
        Row(
            modifier = Modifier.width(88.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProcessRouteActionChip("編集", Color(0xFF667EEA), Icons.Default.Edit) { onEdit(step) }
            ProcessRouteActionChip("削除", Color(0xFFEF4444), Icons.Default.Delete) { onDelete(id) }
        }
    }
}

@Composable
private fun ProcessRouteTextCell(
    text: String,
    widthDp: Int,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF334155),
    align: TextAlign = TextAlign.Start,
    mono: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp),
        fontSize = 10.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
    )
}

@Composable
private fun ProcessRouteStatusBadge(active: Boolean) {
    val bg = if (active) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)
    val fg = if (active) Color(0xFF059669) else Color(0xFF64748B)
    Surface(shape = RoundedCornerShape(4.dp), color = bg) {
        Text(
            if (active) "有効" else "無効",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun ProcessRouteActionChip(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
            Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
fun ProcessRoutePaginationBar(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    if (total <= pageSize) return
    val totalPages = max(1, (total + pageSize - 1) / pageSize)
    val currentPage = min(page, totalPages)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$currentPage / $totalPages ページ（全 $total 件）", fontSize = 11.sp, color = Color(0xFF64748B))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { onPageChange(max(1, currentPage - 1)) }, enabled = currentPage > 1) {
                    Text("前へ", fontSize = 11.sp)
                }
                TextButton(onClick = { onPageChange(min(totalPages, currentPage + 1)) }, enabled = currentPage < totalPages) {
                    Text("次へ", fontSize = 11.sp)
                }
            }
        }
    }
}
