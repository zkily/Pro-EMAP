package com.example.smart_emap.ui.master.materialinspection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterInspectionDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

private val masterPurpleGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))

fun formatInspectionDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return runCatching {
        val instant = Instant.parse(value.replace(" ", "T").let { if (it.endsWith("Z")) it else "${it}Z" })
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrElse {
        runCatching {
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(java.time.LocalDateTime.parse(value.substring(0, min(19, value.length)).replace(" ", "T")))
        }.getOrElse { value }
    }
}

@Composable
fun MaterialInspectionHeroBar(total: Int, displayed: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(masterPurpleGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text("材料検品マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("仕入先の材料マスタを管理します", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InspectionStatChip(total.toString(), "総件数")
            InspectionStatChip(displayed.toString(), "表示件数")
        }
    }
}

@Composable
private fun InspectionStatChip(value: String, label: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.18f)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
        }
    }
}

@Composable
fun MaterialInspectionActionSection(
    displayedCount: Int,
    hasActiveFilters: Boolean,
    keyword: String,
    selectedCount: Int,
    actionLoading: Boolean,
    onClear: () -> Unit,
    onAdd: () -> Unit,
    onBatchDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                    Text("検索・フィルター", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (displayedCount > 0 || hasActiveFilters) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
                            Text(" 表示 $displayedCount 件", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("クリア", fontSize = 12.sp)
                    }
                    InspectionActionButton("新規追加", Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), Icons.Default.Add, onAdd)
                    InspectionActionButton(
                        "一括削除",
                        Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
                        Icons.Default.Delete,
                        onBatchDelete,
                        enabled = selectedCount > 0 && !actionLoading,
                    )
                }
            }
            if (keyword.isNotBlank()) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEEF2FF)) {
                    Text("キーワード: $keyword", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color(0xFF667EEA))
                }
            }
        }
    }
}

@Composable
private fun InspectionActionButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.background(brush, RoundedCornerShape(8.dp)).alphaIfDisabled(enabled),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Modifier.alphaIfDisabled(enabled: Boolean): Modifier =
    if (enabled) this else this.alpha(0.45f)

@Composable
fun MaterialInspectionFilterField(keyword: String, onKeywordChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("🔍 キーワード", fontSize = 12.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
            BasicTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                singleLine = true,
                decorationBox = { inner ->
                    if (keyword.isEmpty()) {
                        Text("検品CD・検品規格", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
fun MaterialInspectionTable(
    items: List<MasterInspectionDto>,
    selectedIds: Set<Int>,
    loading: Boolean,
    onToggleSelectAll: (Boolean) -> Unit,
    onToggleSelect: (Int) -> Unit,
    onRowClick: (MasterInspectionDto) -> Unit,
    onEdit: (MasterInspectionDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val allSelected = items.isNotEmpty() && items.all { it.id in selectedIds }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF667EEA))
            }
            return@Card
        }
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF5F7FA), Color(0xFFEEF2F6))))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = allSelected, onCheckedChange = onToggleSelectAll, modifier = Modifier.size(32.dp))
                Text("検品CD", modifier = Modifier.width(100.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("検品規格", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("作成日時", modifier = Modifier.width(120.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("更新日時", modifier = Modifier.width(120.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("操作", modifier = Modifier.width(80.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            items.forEachIndexed { index, item ->
                val id = item.id ?: return@forEachIndexed
                val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .clickable { onRowClick(item) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = id in selectedIds,
                        onCheckedChange = { onToggleSelect(id) },
                        modifier = Modifier.size(32.dp),
                    )
                    Text(item.inspectionCd.orEmpty(), modifier = Modifier.width(100.dp), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.inspectionStandard.orEmpty(), modifier = Modifier.weight(1f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(formatInspectionDateTime(item.createdAt), modifier = Modifier.width(120.dp), fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(formatInspectionDateTime(item.updatedAt), modifier = Modifier.width(120.dp), fontSize = 10.sp, color = Color(0xFF64748B))
                    Row(modifier = Modifier.width(80.dp)) {
                        TextButton(onClick = { onEdit(item) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "編集", modifier = Modifier.size(14.dp), tint = Color(0xFF667EEA))
                        }
                        TextButton(onClick = { onDelete(id) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "削除", modifier = Modifier.size(14.dp), tint = Color(0xFFEF4444))
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9))
            }
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MaterialInspectionPaginationBar(
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val maxPage = max(1, (total + pageSize - 1) / pageSize)
    val sizes = listOf(10, 20, 50, 100)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("全 $total 件", fontSize = 12.sp, color = Color(0xFF64748B))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            sizes.forEach { size ->
                val active = size == pageSize
                Surface(
                    onClick = { onPageSizeChange(size) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (active) Color(0xFF667EEA) else Color(0xFFF1F5F9),
                ) {
                    Text(
                        "$size",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = if (active) Color.White else Color(0xFF64748B),
                    )
                }
            }
            TextButton(onClick = { if (page > 1) onPageChange(page - 1) }, enabled = page > 1) { Text("前へ") }
            Text("$page / $maxPage", fontSize = 12.sp)
            TextButton(onClick = { if (page < maxPage) onPageChange(page + 1) }, enabled = page < maxPage) { Text("次へ") }
        }
    }
}

@Composable
fun MaterialInspectionDetailDialog(item: MasterInspectionDto, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.92f)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("材料検品マスタ詳細", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                InspectionDetailRow("検品CD", item.inspectionCd.orEmpty())
                InspectionDetailRow("検品規格", item.inspectionStandard.orEmpty(), block = true)
                InspectionDetailRow("作成日時", formatInspectionDateTime(item.createdAt))
                InspectionDetailRow("更新日時", formatInspectionDateTime(item.updatedAt))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("閉じる") }
                }
            }
        }
    }
}

@Composable
private fun InspectionDetailRow(label: String, value: String, block: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(10.dp),
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF667EEA), fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, color = Color(0xFF334155), modifier = if (block) Modifier.padding(top = 4.dp) else Modifier)
    }
}
