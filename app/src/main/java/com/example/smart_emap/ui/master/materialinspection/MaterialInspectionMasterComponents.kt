package com.example.smart_emap.ui.master.materialinspection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterInspectionDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

private val inspectionSlateGradient = Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
private val inspectionTableRowHeight = 44.dp
private val inspectionTableHeaderHeight = 42.dp

private data class InspectionTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val inspectionTableColumns = listOf(
    InspectionTableColumn("inspection_cd", "検品CD", 108, TextAlign.Center),
    InspectionTableColumn("inspection_standard", "検品規格", 180),
    InspectionTableColumn("created_at", "作成日時", 128, TextAlign.Center),
    InspectionTableColumn("updated_at", "更新日時", 128, TextAlign.Center),
    InspectionTableColumn("actions", "操作", 88, TextAlign.Center),
)

private val inspectionTableWidth = (inspectionTableColumns.sumOf { it.widthDp } + 16).dp

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
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, spotColor = Color(0x40475569))
            .clip(shape)
            .background(inspectionSlateGradient)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text("材料検品マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "仕入先材料の検品基準を登録・管理します",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InspectionStatChip(total.toString(), "総件数")
            InspectionStatChip(displayed.toString(), "表示件数")
        }
    }
}

@Composable
private fun InspectionStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
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
fun MaterialInspectionToolbar(
    total: Int,
    actionLoading: Boolean,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                Text("検品基準一覧", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF334155))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(14.dp)
                        .background(Color(0xFFE2E8F0)),
                )
                Text("全 $total 件", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            InspectionGradientButton(
                label = "新規追加",
                brush = inspectionSlateGradient,
                icon = Icons.Default.Add,
                enabled = !actionLoading,
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun InspectionGradientButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        modifier = Modifier
            .background(brush, RoundedCornerShape(10.dp))
            .then(if (!enabled) Modifier else Modifier),
        shadowElevation = if (enabled) 3.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 5.dp),
            )
        }
    }
}

@Composable
fun MaterialInspectionTable(
    items: List<MasterInspectionDto>,
    loading: Boolean,
    onRowClick: (MasterInspectionDto) -> Unit,
    onEdit: (MasterInspectionDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color(0xFF64748B), strokeWidth = 2.5.dp)
            }
            return@Card
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(hScroll),
        ) {
            Row(
                modifier = Modifier
                    .width(inspectionTableWidth)
                    .height(inspectionTableHeaderHeight)
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                inspectionTableColumns.forEach { col ->
                    Text(
                        col.label,
                        modifier = Modifier.width(col.widthDp.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        textAlign = col.align,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 2.dp)
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .width(inspectionTableWidth)
                        .height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(36.dp))
                        Text("データがありません", color = Color(0xFF94A3B8), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                items.forEachIndexed { index, item ->
                    val id = item.id ?: return@forEachIndexed
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
                    Row(
                        modifier = Modifier
                            .width(inspectionTableWidth)
                            .height(inspectionTableRowHeight)
                            .background(bg)
                            .clickable { onRowClick(item) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.width(108.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                Text(
                                    item.inspectionCd.orEmpty(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 4.dp),
                                )
                            }
                        }
                        Text(
                            item.inspectionStandard.orEmpty(),
                            modifier = Modifier.width(180.dp).padding(horizontal = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            formatInspectionDateTime(item.createdAt),
                            modifier = Modifier.width(128.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            formatInspectionDateTime(item.updatedAt),
                            modifier = Modifier.width(128.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                        )
                        Row(
                            modifier = Modifier.width(88.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(onClick = { onEdit(item) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "編集", modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                            }
                            TextButton(onClick = { onDelete(id) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("全 $total 件", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                sizes.forEach { size ->
                    val active = size == pageSize
                    Surface(
                        onClick = { onPageSizeChange(size) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (active) Color(0xFF64748B) else Color(0xFFF1F5F9),
                    ) {
                        Text(
                            "$size",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (active) Color.White else Color(0xFF64748B),
                        )
                    }
                }
                TextButton(onClick = { if (page > 1) onPageChange(page - 1) }, enabled = page > 1) {
                    Text("前へ", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Text("$page / $maxPage", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                TextButton(onClick = { if (page < maxPage) onPageChange(page + 1) }, enabled = page < maxPage) {
                    Text("次へ", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun MaterialInspectionDetailDialog(item: MasterInspectionDto, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.92f),
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("材料検品マスタ詳細", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                InspectionDetailRow("検品CD", item.inspectionCd.orEmpty(), isCode = true)
                InspectionDetailRow("検品規格", item.inspectionStandard.orEmpty(), block = true)
                InspectionDetailRow("作成日時", formatInspectionDateTime(item.createdAt))
                InspectionDetailRow("更新日時", formatInspectionDateTime(item.updatedAt))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("閉じる", color = Color(0xFF64748B)) }
                }
            }
        }
    }
}

@Composable
private fun InspectionDetailRow(label: String, value: String, block: Boolean = false, isCode: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        Text(
            value,
            fontSize = if (isCode) 13.sp else 13.sp,
            color = if (isCode) Color(0xFF475569) else Color(0xFF334155),
            fontWeight = if (isCode) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
            modifier = if (block) Modifier.padding(top = 4.dp) else Modifier.padding(top = 2.dp),
            lineHeight = 20.sp,
        )
    }
}
