package com.example.smart_emap.ui.master.process

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import com.example.smart_emap.data.model.MasterProcessDto

val processPageBackground = Brush.linearGradient(listOf(Color(0xFFF0F4F8), Color(0xFFD9E2EC)))
private val processPurpleGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val processFilterControlHeight = 34.dp
private val processTableRowHeight = 40.dp
private val processTableHeaderHeight = 42.dp

private data class ProcessTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val processTableColumns = listOf(
    ProcessTableColumn("process_cd", "工程CD", 72, TextAlign.Center),
    ProcessTableColumn("process_name", "工程名", 100),
    ProcessTableColumn("short_name", "略称", 48, TextAlign.Center),
    ProcessTableColumn("category", "分類", 56, TextAlign.Center),
    ProcessTableColumn("is_outsource", "外注", 52, TextAlign.Center),
    ProcessTableColumn("default_cycle_sec", "サイクル", 64, TextAlign.End),
    ProcessTableColumn("default_yield", "歩留%", 56, TextAlign.End),
    ProcessTableColumn("capacity_unit", "単位", 44, TextAlign.Center),
    ProcessTableColumn("remark", "備考", 100),
    ProcessTableColumn("actions", "操作", 88, TextAlign.Center),
)

private val processTableWidth = (processTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun ProcessMasterHeroBar(total: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(processPurpleGradient)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("工程マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("工程情報の登録・編集", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
        }
        ProcessStatChip(total.toString(), "件")
    }
}

@Composable
private fun ProcessStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
        }
    }
}

@Composable
fun ProcessMasterActionCard(
    keyword: String,
    displayedCount: Int,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onQrPrint: () -> Unit,
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
                    Text("操作", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                }
                Row(
                    modifier = Modifier.horizontalScroll(buttonScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProcessGradientButton(
                        "QR印刷",
                        Brush.linearGradient(listOf(Color(0xFFF39C12), Color(0xFFE67E22))),
                        Icons.Default.Print,
                        enabled = !actionLoading,
                        onClick = onQrPrint,
                    )
                    ProcessGradientButton("工程追加", processPurpleGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("キーワード", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(bottom = 4.dp))
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(processFilterControlHeight)
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
                                if (keyword.isEmpty()) Text("工程CD・名称", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                inner()
                            }
                        },
                    )
                }
                ProcessGradientButton("検索", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))), Icons.Default.Search, enabled = !actionLoading, onClick = onSearch)
                TextButton(onClick = onClear, enabled = !actionLoading, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF64748B))
                    Text("リセット", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
            if (hasActiveFilters || displayedCount > 0) {
                Text(
                    "表示: $displayedCount 件",
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                )
            }
        }
    }
}

@Composable
private fun ProcessGradientButton(
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
        androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
fun ProcessMasterTable(
    processes: List<MasterProcessDto>,
    loading: Boolean,
    total: Int,
    modifier: Modifier = Modifier,
    onEdit: (MasterProcessDto) -> Unit,
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
                        .width(processTableWidth)
                        .height(processTableHeaderHeight)
                        .background(processPurpleGradient)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    processTableColumns.forEach { col ->
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
                    loading && processes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF667EEA), modifier = Modifier.size(28.dp))
                        }
                    }
                    processes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("データがありません", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(vScroll)
                                .horizontalScroll(hScroll),
                        ) {
                            processes.forEachIndexed { index, process ->
                                ProcessMasterTableRow(process, index, onEdit, onDelete)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Text(
                "表示件数: ${processes.size} / $total",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun ProcessMasterTableRow(
    process: MasterProcessDto,
    index: Int,
    onEdit: (MasterProcessDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val id = process.id ?: return
    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
    val outsource = process.isOutsource == true
    Row(
        modifier = Modifier
            .width(processTableWidth)
            .height(processTableRowHeight)
            .background(bg)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            process.processCd.orEmpty(),
            modifier = Modifier.width(72.dp).padding(horizontal = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF667EEA),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ProcessTableTextCell(process.processName.orEmpty(), 100, FontWeight.Medium)
        ProcessTableTextCell(process.shortName.orEmpty().ifBlank { "—" }, 48, textAlign = TextAlign.Center)
        ProcessCategoryChip(process.category, 56)
        ProcessOutsourceChip(outsource, 52)
        ProcessTableTextCell(formatProcessCycleSec(process.defaultCycleSec), 64, textAlign = TextAlign.End)
        ProcessTableTextCell(formatProcessYieldPercent(process.defaultYield), 56, textAlign = TextAlign.End)
        ProcessTableTextCell(process.capacityUnit.orEmpty().ifBlank { "pcs" }, 44, textAlign = TextAlign.Center)
        ProcessTableTextCell(process.remark.orEmpty().ifBlank { "—" }, 100)
        Row(
            modifier = Modifier.width(88.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "編集",
                color = Color(0xFF667EEA),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onEdit(process) }.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Text(
                "削除",
                color = Color(0xFFEF4444),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onDelete(id) }.padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

@Composable
private fun ProcessTableTextCell(
    text: String,
    widthDp: Int,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text,
        modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp),
        fontSize = 10.sp,
        fontWeight = fontWeight,
        color = Color(0xFF334155),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 13.sp,
        textAlign = textAlign,
    )
}

@Composable
private fun ProcessCategoryChip(category: String?, widthDp: Int) {
    val label = processCategoryLabel(category).takeIf { it != "—" }
    Box(modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        if (label.isNullOrBlank()) {
            Text("—", fontSize = 10.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
        } else {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF2FF), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E7FF))) {
                Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = Color(0xFF667EEA), maxLines = 1)
            }
        }
    }
}

@Composable
private fun ProcessOutsourceChip(isOutsource: Boolean, widthDp: Int) {
    val color = if (isOutsource) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
    val textColor = if (isOutsource) Color(0xFFDC2626) else Color(0xFF059669)
    val label = if (isOutsource) "外注" else "社内"
    Box(modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(10.dp), color = color) {
            Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = textColor, maxLines = 1)
        }
    }
}
