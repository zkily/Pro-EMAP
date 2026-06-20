package com.example.smart_emap.ui.erp.purchase.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.smart_emap.data.model.MasterPartDto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.smart_emap.ui.common.BeautifulDatePickerDialog
import com.example.smart_emap.ui.common.formatBeautifulDisplayDate
import com.example.smart_emap.ui.erp.order.GlassButtonStyle
import com.example.smart_emap.ui.erp.order.GlassPillButton
import com.example.smart_emap.ui.erp.order.GlassToolbarButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

private val SyncDialogShape = RoundedCornerShape(18.dp)

private val ManualOrderAccent = Color(0xFF10B981)
private val ManualOrderAccentDark = Color(0xFF059669)
private val ManualOrderAccentLight = Color(0xFF34D399)
private val ManualOrderDialogShape = RoundedCornerShape(24.dp)
private val ManualOrderHeaderGradient = Brush.linearGradient(
    listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399)),
)

private val SyncFieldItems = listOf(
    "部品名" to "part_name",
    "分類" to "category",
    "単位" to "unit",
    "単価" to "unit_price",
    "仕入先CD" to "supplier_cd",
    "仕入先名" to "supplier_name",
    "梱本数" to "pieces_per_bundle",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartSyncMasterConfirmDialog(
    startDate: String,
    endDate: String,
    loading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(20.dp, SyncDialogShape, spotColor = Color(0x50667EEA))
                .clip(SyncDialogShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFAFBFF), Color(0xFFF1F5F9), Color(0xFFEEF2FF)),
                    ),
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.95f), SyncDialogShape),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SyncMasterDialogHeader(loading = loading, onDismiss = onDismiss)

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7ED),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                                        ),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "部品マスタを在庫へ同期",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF92400E),
                                )
                                Text(
                                    "parts テーブルの最新情報を part_stock に反映します。既存の在庫行が上書き更新されます。",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFFB45309),
                                )
                            }
                        }
                    }

                    SyncMasterPeriodCard(startDate = startDate, endDate = endDate)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "同期対象項目",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp,
                            ),
                        ) {
                            SyncFieldItems.forEach { (label, field) ->
                                SyncFieldChip(label = label, field = field)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassPillButton(
                        text = "キャンセル",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        filled = false,
                    )
                    GlassToolbarButton(
                        label = if (loading) "実行中…" else "実行",
                        icon = Icons.Default.Sync,
                        style = GlassButtonStyle.Amber,
                        enabled = !loading,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.2f),
                    )
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Color(0xFF667EEA),
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncMasterDialogHeader(loading: Boolean, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF6D28D9)),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    ),
                ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(
                        "部品マスタ更新確認",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(
                        "Part Master Sync",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                    )
                }
            }
            IconButton(onClick = onDismiss, enabled = !loading, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
private fun SyncMasterPeriodCard(startDate: String, endDate: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E7FF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1))),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("対象期間", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Text(
                    "$startDate  ～  $endDate",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEEF2FF),
            ) {
                Text(
                    "期間内のみ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4F46E5),
                )
            }
        }
    }
}

@Composable
private fun SyncFieldChip(label: String, field: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF667EEA)),
            )
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
            Text("·", fontSize = 10.sp, color = Color(0xFFCBD5E1))
            Text(field, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}

private val DataGenDialogShape = RoundedCornerShape(12.dp)
private val DataGenAccent = Color(0xFF409EFF)
private val DataGenConfirmGradient = Brush.linearGradient(listOf(Color(0xFF409EFF), Color(0xFF6366F1)))

private val DataGenNoticeItems = listOf(
    "既存のデータがある場合はスキップされます",
    "重複データは自動的に検出・スキップされます",
    "生成には時間がかかる場合があります",
    "期間が長いほど生成時間が長くなります",
)

@Composable
fun PartDataGenerationDialog(
    startDate: String,
    endDate: String,
    loading: Boolean,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val canConfirm = startDate.isNotBlank() && endDate.isNotBlank() && !loading

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.92f)
                .shadow(12.dp, DataGenDialogShape, spotColor = Color(0x33000000)),
            shape = DataGenDialogShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E7ED)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "データ生成期間設定",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF303133),
                    )
                    IconButton(onClick = onDismiss, enabled = !loading, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFF909399), modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = Color(0xFFEBEEF5))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DataGenFormSection(
                        title = "期間設定",
                        icon = Icons.Default.CalendarMonth,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DataGenDateFieldRow(
                                label = "開始日",
                                value = startDate,
                                placeholder = "開始日を選択",
                                accent = DataGenAccent,
                                enabled = !loading,
                                onChange = onStartDateChange,
                            )
                            DataGenDateFieldRow(
                                label = "終了日",
                                value = endDate,
                                placeholder = "終了日を選択",
                                accent = DataGenAccent,
                                enabled = !loading,
                                onChange = onEndDateChange,
                            )
                        }
                    }

                    DataGenFormSection(
                        title = "注意事項",
                        icon = Icons.Default.Info,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DataGenNoticeItems.forEach { item ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text("•", fontSize = 12.sp, color = Color(0xFF606266))
                                    Text(item, fontSize = 12.sp, color = Color(0xFF606266), lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEBEEF5))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DataGenCancelButton(enabled = !loading, onClick = onDismiss)
                    Spacer(modifier = Modifier.width(10.dp))
                    DataGenConfirmButton(
                        enabled = canConfirm,
                        loading = loading,
                        onClick = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun DataGenFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBEEF5)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECF5FF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, contentDescription = null, tint = DataGenAccent, modifier = Modifier.size(16.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF303133))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DataGenDateFieldRow(
    label: String,
    value: String,
    placeholder: String,
    accent: Color,
    enabled: Boolean,
    onChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        BeautifulDatePickerDialog(
            value = value,
            title = label,
            accentColor = accent,
            confirmLabel = "確定",
            onDismiss = { showPicker = false },
            onConfirm = { picked -> onChange(picked); showPicker = false },
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF606266),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFDCDFE6), RoundedCornerShape(6.dp))
                .then(if (enabled) Modifier.clickable { showPicker = true } else Modifier)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFC0C4CC), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value.ifBlank { placeholder },
                fontSize = 12.sp,
                color = if (value.isBlank()) Color(0xFFA8ABB2) else Color(0xFF303133),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DataGenCancelButton(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDFE6)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF909399), modifier = Modifier.size(14.dp))
            Text("キャンセル", fontSize = 13.sp, color = Color(0xFF606266))
        }
    }
}

@Composable
private fun DataGenConfirmButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) DataGenConfirmGradient else Brush.linearGradient(listOf(Color(0xFFA0CFFF), Color(0xFFA5B4FC))))
            .then(if (enabled && !loading) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Text("生成実行", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PartManualOrderDialog(
    form: PartManualOrderFormUi,
    partOptions: List<MasterPartDto>,
    selectedPart: MasterPartDto?,
    loading: Boolean,
    onDateChange: (String) -> Unit,
    onPartChange: (String) -> Unit,
    onOrderQuantityChange: (Int) -> Unit,
    onRemarksChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var partExpanded by remember { mutableStateOf(false) }
    var dialogVisible by remember { mutableStateOf(false) }
    val ppb = form.piecesPerBundle.coerceAtLeast(1)
    val calculatedAmount = form.orderQuantity * ppb * form.unitPrice
    val jpFmt = remember { java.text.NumberFormat.getIntegerInstance(java.util.Locale.JAPAN) }
    val scroll = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(Unit) { dialogVisible = true }

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AnimatedVisibility(
            visible = dialogVisible,
            enter = scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            ) + fadeIn(tween(280)),
            exit = fadeOut(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 420.dp)
                    .shadow(28.dp, ManualOrderDialogShape, spotColor = ManualOrderAccent.copy(alpha = 0.45f))
                    .clip(ManualOrderDialogShape)
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.85f), ManualOrderDialogShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ManualOrderHeaderGradient)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "部品注文追加",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White,
                            )
                            Text(
                                "新しい部品注文を手動で入力",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.88f),
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !loading,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(scroll)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ManualOrderFormSection(title = "基本情報", icon = Icons.Default.CalendarMonth) {
                        ManualOrderFieldLabel("日付")
                        ManualOrderSelectField(
                            value = if (form.date.isBlank()) "" else formatBeautifulDisplayDate(form.date, withWeekday = false),
                            placeholder = "日付を選択",
                            accent = ManualOrderAccent,
                            enabled = !loading,
                            leadingIcon = Icons.Default.CalendarMonth,
                            onClick = { showDatePicker = true },
                        )

                        ManualOrderFieldLabel("部品")
                        ExposedDropdownMenuBox(
                            expanded = partExpanded,
                            onExpandedChange = { if (!loading) partExpanded = it },
                        ) {
                            ManualOrderSelectField(
                                value = if (form.partCd.isBlank()) "" else "${form.partCd} - ${form.partName}",
                                placeholder = "部品を選択",
                                accent = ManualOrderAccent,
                                enabled = !loading,
                                leadingIcon = Icons.Default.Inventory2,
                                onClick = { partExpanded = true },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(
                                expanded = partExpanded,
                                onDismissRequest = { partExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp),
                            ) {
                                partOptions.forEach { part ->
                                    val cd = part.partCd.orEmpty()
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "$cd - ${part.partName.orEmpty()}",
                                                fontSize = 13.sp,
                                                maxLines = 2,
                                            )
                                        },
                                        onClick = {
                                            onPartChange(cd)
                                            partExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    ManualOrderFormSection(title = "注文数量", icon = Icons.Default.Add) {
                        ManualOrderFieldLabel("注文本数")
                        ManualOrderQuantityField(
                            value = form.orderQuantity,
                            enabled = !loading,
                            accent = Color(0xFFFDE68A),
                            accentBorder = Color(0xFFF59E0B),
                            onChange = onOrderQuantityChange,
                        )

                        ManualOrderFieldLabel("備考")
                        androidx.compose.material3.OutlinedTextField(
                            value = form.remarks,
                            onValueChange = onRemarksChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF334155)),
                            placeholder = { Text("備考（任意）", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ManualOrderAccent,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                            ),
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedPart != null,
                        enter = fadeIn(tween(320)) + expandVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        ) + slideInVertically(initialOffsetY = { it / 3 }),
                        exit = fadeOut(tween(200)) + shrinkVertically(),
                    ) {
                        selectedPart?.let { part ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, ManualOrderAccent.copy(alpha = 0.28f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = ManualOrderAccent, modifier = Modifier.size(18.dp))
                                        Text("部品詳細", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                                        if (calculatedAmount > 0) {
                                            Spacer(Modifier.weight(1f))
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = ManualOrderAccent.copy(alpha = 0.12f),
                                            ) {
                                                Text(
                                                    "参考 ¥${jpFmt.format(calculatedAmount.toInt())}",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ManualOrderAccentDark,
                                                )
                                            }
                                        }
                                    }
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ManualOrderDetailChip("仕入先CD", part.supplierCd.orEmpty())
                                        ManualOrderDetailChip("分類", part.category.orEmpty())
                                        ManualOrderDetailChip("単位", part.uom.orEmpty())
                                        ManualOrderDetailChip("単価", "¥${part.unitPrice?.toInt() ?: 0}")
                                        ManualOrderDetailChip("梱本数", "${form.piecesPerBundle}")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ManualOrderFooterButton(
                        text = "キャンセル",
                        filled = false,
                        enabled = !loading,
                        onClick = onDismiss,
                    )
                    Spacer(Modifier.width(10.dp))
                    ManualOrderFooterButton(
                        text = if (loading) "登録中…" else "登録",
                        filled = true,
                        enabled = !loading,
                        loading = loading,
                        onClick = onConfirm,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        BeautifulDatePickerDialog(
            value = form.date,
            title = "日付",
            accentColor = ManualOrderAccent,
            confirmLabel = "確定",
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun ManualOrderFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ManualOrderAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = ManualOrderAccent, modifier = Modifier.size(15.dp))
            }
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
        }
        content()
    }
}

@Composable
private fun ManualOrderSelectField(
    value: String,
    placeholder: String,
    accent: Color,
    enabled: Boolean,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val hasValue = value.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF8FAFC), if (hasValue) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)),
                ),
            )
            .border(
                1.dp,
                if (hasValue) accent.copy(alpha = 0.45f) else Color(0xFFE2E8F0),
                shape,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(leadingIcon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        Text(
            text = value.ifBlank { placeholder },
            fontSize = 13.sp,
            fontWeight = if (hasValue) FontWeight.Medium else FontWeight.Normal,
            color = if (hasValue) Color(0xFF1E293B) else Color(0xFF94A3B8),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ManualOrderFooterButton(
    text: String,
    filled: Boolean,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 100.dp)
            .clip(shape)
            .then(
                if (filled) {
                    Modifier.background(
                        Brush.linearGradient(listOf(ManualOrderAccentDark, ManualOrderAccent, ManualOrderAccentLight)),
                    )
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), shape)
                },
            )
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading && filled) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (filled) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text(
                    text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (filled) Color.White else Color(0xFF64748B),
                )
            }
        }
    }
}

@Composable
private fun ManualOrderFieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
}

@Composable
private fun ManualOrderQuantityField(
    value: Int,
    enabled: Boolean,
    accent: Color,
    accentBorder: Color,
    onChange: (Int) -> Unit,
    editable: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    var text by remember { mutableStateOf(value.toString()) }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!focused) text = value.toString()
    }

    val numericValue = if (editable) text.toIntOrNull() ?: value else value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = 0.55f))
            .border(1.dp, accentBorder.copy(alpha = 0.35f), shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ManualOrderStepperBtn(enabled = enabled) {
            val next = (numericValue - 1).coerceAtLeast(0)
            if (editable) text = next.toString()
            onChange(next)
        }
        if (editable) {
            BasicTextField(
                value = text,
                onValueChange = { raw ->
                    val digits = raw.filter { it.isDigit() }.take(6)
                    text = digits
                    onChange(digits.toIntOrNull() ?: 0)
                },
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height(32.dp)
                    .onFocusChanged { state ->
                        focused = state.isFocused
                        if (!state.isFocused) {
                            val parsed = text.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            text = parsed.toString()
                            onChange(parsed)
                        }
                    },
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (text.isEmpty() && !focused) {
                            Text(
                                "0",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                            )
                        }
                        inner()
                    }
                },
            )
        } else {
            Text(
                value.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
        }
        ManualOrderStepperBtn(enabled = enabled, plus = true) {
            val next = numericValue + 1
            if (editable) text = next.toString()
            onChange(next)
        }
    }
}

@Composable
private fun ManualOrderStepperBtn(enabled: Boolean, plus: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.92f else 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (plus) "+" else "−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
    }
}

@Composable
private fun ManualOrderDetailChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, ManualOrderAccent.copy(alpha = 0.15f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF94A3B8))
            Text(
                value.ifBlank { "—" },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val PrintConfirmAccent = Color(0xFF667EEA)
private val PrintConfirmDialogShape = RoundedCornerShape(12.dp)
private val PrintConfirmHeaderGradient = Brush.linearGradient(
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
)

@Composable
fun PartPrintOrderConfirmDialog(
    form: PartOrderPrintFormUi,
    orderCount: Int,
    loading: Boolean,
    onRecipientCompanyChange: (String) -> Unit,
    onRecipientPersonsChange: (String) -> Unit,
    onApproverChange: (String) -> Unit,
    onIssuerChange: (String) -> Unit,
    onNote1Change: (String) -> Unit,
    onNote2Change: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scroll = rememberScrollState()

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .shadow(20.dp, PrintConfirmDialogShape, spotColor = Color(0x26000000))
                .clip(PrintConfirmDialogShape)
                .background(Color.White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrintConfirmHeaderGradient)
                    .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "注文書印刷確認",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF0A0A0A),
                    modifier = Modifier.weight(1f),
                )
                PrintConfirmHeaderButton(
                    label = if (loading) "処理中…" else "印刷実行",
                    loading = loading,
                    enabled = !loading,
                    onClick = onConfirm,
                )
                IconButton(
                    onClick = onDismiss,
                    enabled = !loading,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scroll)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrintConfirmSection(title = "受注先情報", icon = Icons.Default.Person) {
                    PrintConfirmFieldRow("受注先会社名", form.recipientCompany, loading, onRecipientCompanyChange)
                    PrintConfirmFieldRow("受注先担当者", form.recipientPersons, loading, onRecipientPersonsChange)
                }

                PrintConfirmSection(title = "承認・発行情報", icon = Icons.Default.Edit) {
                    PrintConfirmFieldRow("承認者", form.approver, loading, onApproverChange)
                    PrintConfirmFieldRow("発行者", form.issuer, loading, onIssuerChange)
                }

                PrintConfirmSection(title = "備考・注意事項", icon = Icons.Default.Inventory2) {
                    PrintConfirmFieldRow(
                        label = "備考1",
                        value = form.note1,
                        enabled = !loading,
                        onChange = onNote1Change,
                        minLines = 2,
                    )
                    PrintConfirmFieldRow(
                        label = "備考2",
                        value = form.note2,
                        enabled = !loading,
                        onChange = onNote2Change,
                        minLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrintConfirmSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .border(BorderStroke(1.dp, Color(0xFFE5E7EB)))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(icon, contentDescription = null, tint = PrintConfirmAccent, modifier = Modifier.size(13.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PrintConfirmHeaderButton(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(5.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(Color(0x9617F19E))
            .border(1.dp, Color(0xC6080707), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF070707),
                )
            } else {
                Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF070707), modifier = Modifier.size(12.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF070707))
        }
    }
}

@Composable
private fun PrintConfirmFieldRow(
    label: String,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
    minLines: Int = 1,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            modifier = Modifier
                .widthIn(min = 95.dp)
                .then(if (minLines > 1) Modifier.padding(top = 6.dp) else Modifier),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569),
        )
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            textStyle = TextStyle(
                fontSize = if (minLines > 1) 10.sp else 11.sp,
                color = Color(0xFF1F2937),
                lineHeight = if (minLines > 1) 13.sp else 15.sp,
            ),
            minLines = minLines,
            singleLine = minLines == 1,
            shape = RoundedCornerShape(4.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrintConfirmAccent,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF9FAFB),
            ),
        )
    }
}
