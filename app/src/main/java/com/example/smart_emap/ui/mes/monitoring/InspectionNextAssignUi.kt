package com.example.smart_emap.ui.mes.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.data.model.UserListItemDto

private val AssignTextMuted = Color(0xFF64748B)
private val AssignTextPrimary = Color(0xFF0F172A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionNextAssignPanelSheet(
    visible: Boolean,
    rows: List<NextAssignPanelRow>,
    assignmentCount: Int,
    loading: Boolean,
    onDismiss: () -> Unit,
    onAddInspector: () -> Unit,
    onOpenRow: (NextAssignPanelRow) -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White.copy(alpha = 0.97f),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                    Text("次製品指定", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AssignTextPrimary)
                    Text("稼働中の指定と初回製品の事前指定", fontSize = 11.sp, color = AssignTextMuted)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる")
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onAddInspector,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("検査員を追加して指定", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("指定対象の検査はありません", color = AssignTextMuted)
                }
            } else {
                val activeRows = rows.filter { !it.isFirstProduct }
                val idleRows = rows.filter { it.isFirstProduct }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (activeRows.isNotEmpty()) {
                        SectionTitle("稼働中")
                        activeRows.forEach { row -> NextAssignPanelCard(row = row, onAssign = { onOpenRow(row) }) }
                    }
                    if (idleRows.isNotEmpty()) {
                        SectionTitle("初回製品（未稼働）")
                        idleRows.forEach { row -> NextAssignPanelCard(row = row, onAssign = { onOpenRow(row) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = AssignTextMuted,
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

@Composable
private fun NextAssignPanelCard(
    row: NextAssignPanelRow,
    onAssign: () -> Unit,
) {
    val borderColor = when {
        row.commStale -> Color(0xFFFCA5A5)
        row.isFirstProduct -> Color(0xFFCBD5E1)
        row.status == MonitorRowStatus.Running -> Color(0xFF86EFAC)
        row.status == MonitorRowStatus.Paused -> Color(0xFFFCD34D)
        row.status == MonitorRowStatus.Break -> Color(0xFF7DD3FC)
        else -> Color(0xFFE2E8F0)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (row.isFirstProduct) row.inspectorName else row.currentProductLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!row.isFirstProduct && row.inspectorName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(row.inspectorName, fontSize = 12.sp, color = AssignTextMuted)
                        }
                    } else if (row.isFirstProduct) {
                        Text("未稼働", fontSize = 12.sp, color = AssignTextMuted)
                    }
                }
                StatusPill(
                    text = if (row.isFirstProduct) "未稼働" else MonitorLogic.statusLabel(row.status),
                    running = row.status == MonitorRowStatus.Running,
                )
            }
            if (!row.isFirstProduct && (row.elapsedSec > 0 || row.status == MonitorRowStatus.Paused || row.status == MonitorRowStatus.Break)) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (row.elapsedSec > 0) {
                        MetricText("経過", MonitorLogic.formatDuration(row.elapsedSec))
                    }
                    if (row.status == MonitorRowStatus.Paused) {
                        MetricText(
                            "一時停止",
                            if (row.pausedSec > 0) MonitorLogic.formatDuration(row.pausedSec) else "—",
                        )
                    }
                    if (row.status == MonitorRowStatus.Break) {
                        MetricText(
                            "休憩",
                            if (row.breakSec > 0) MonitorLogic.formatDuration(row.breakSec) else "—",
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.7f), Color(0xFFE0F2FE).copy(alpha = 0.45f)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!row.nextProductName.isNullOrBlank()) {
                    NextProductChip(
                        label = if (row.isFirstProduct) "初回" else "次",
                        productName = row.nextProductName,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                NextAssignGlassButton(
                    text = if (row.isFirstProduct && row.nextProductName.isNullOrBlank()) "初回製品を指定" else "次製品指定",
                    onClick = onAssign,
                )
            }
        }
    }
}

@Composable
fun NextAssignHeaderIconButton(
    assignmentCount: Int,
    onClick: () -> Unit,
) {
    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color(0xFFE0F2FE).copy(alpha = 0.8f)),
                    ),
                )
                .border(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = "次製品指定", tint = Color(0xFF0369A1), modifier = Modifier.size(18.dp))
        }
        if (assignmentCount > 0) {
            val badgeLabel = assignmentCount.coerceAtMost(99).toString()
            val badgeSize = if (badgeLabel.length > 1) 18.dp else 16.dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF6366F1))))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    badgeLabel,
                    color = Color.White,
                    fontSize = if (badgeLabel.length > 1) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = if (badgeLabel.length > 1) 8.sp else 9.sp,
                )
            }
        }
    }
}

@Composable
private fun NextProductChip(label: String, productName: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))
        Spacer(Modifier.width(6.dp))
        Text(productName, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun NextAssignGlassButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF4F46E5)),
                ),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun StatusPill(text: String, running: Boolean) {
    Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFF8FAFC), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (running) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                Spacer(Modifier.width(4.dp))
            }
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricText(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = AssignTextMuted)
        Spacer(Modifier.width(3.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
    }
}

@Composable
fun InspectionNextAssignDialog(
    dialog: NextAssignDialogState,
    inspectors: List<UserListItemDto>,
    products: List<MonitorProductOption>,
    loading: Boolean,
    hasExistingAssignment: Boolean,
    onDismiss: () -> Unit,
    onInspectorSelected: (Int?) -> Unit,
    onProductSelected: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
) {
    if (!dialog.visible) return
    Dialog(onDismissRequest = { if (!dialog.submitting) onDismiss() }) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.98f)) {
            Column(modifier = Modifier.padding(16.dp).widthIn(max = 420.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (dialog.isFirstProduct) "初回製品を指定" else "次製品を指定",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (dialog.pickInspector) {
                    Text("検査員", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AssignTextMuted)
                    Spacer(Modifier.height(4.dp))
                    InspectorPicker(
                        inspectors = inspectors,
                        selectedId = dialog.inspectorUserId,
                        onSelected = onInspectorSelected,
                    )
                    Spacer(Modifier.height(10.dp))
                } else {
                    InfoRow("検査員", dialog.inspectorName)
                    if (!dialog.isFirstProduct) {
                        InfoRow("現在", dialog.currentProductLabel)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    if (dialog.isFirstProduct) "初回製品" else "次製品",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AssignTextMuted,
                )
                Spacer(Modifier.height(4.dp))
                ProductPicker(
                    products = products,
                    selectedCode = dialog.productCd,
                    onSelected = onProductSelected,
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onDismiss, enabled = !dialog.submitting) {
                        Text("キャンセル")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hasExistingAssignment) {
                            Button(
                                onClick = onClear,
                                enabled = !dialog.submitting,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            ) {
                                Text("指定解除")
                            }
                        }
                        Button(
                            onClick = onSave,
                            enabled = !dialog.submitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        ) {
                            if (dialog.submitting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text("保存")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.width(72.dp), fontSize = 11.sp, color = AssignTextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun InspectorPicker(
    inspectors: List<UserListItemDto>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 140.dp)
            .verticalScroll(rememberScrollState())
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
    ) {
        inspectors.forEach { user ->
            val id = user.id ?: return@forEach
            val label = user.fullName?.trim().orEmpty().ifBlank { user.username.orEmpty() }
            val selected = selectedId == id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(id) }
                    .background(if (selected) Color(0xFFEEF2FF) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ProductPicker(
    products: List<MonitorProductOption>,
    selectedCode: String,
    onSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 180.dp)
            .verticalScroll(rememberScrollState())
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
    ) {
        products.forEach { product ->
            val selected = selectedCode == product.productCode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(product.productCode) }
                    .background(if (selected) Color(0xFFEEF2FF) else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    "${product.productCode} · ${product.productName}",
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
