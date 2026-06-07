package com.example.smart_emap.ui.erp.purchase.material

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
import com.example.smart_emap.data.model.MaterialMasterItemDto
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
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.order.GlassButtonStyle
import com.example.smart_emap.ui.erp.order.GlassPillButton
import com.example.smart_emap.ui.erp.order.GlassToolbarButton

private val SyncDialogShape = RoundedCornerShape(18.dp)

private val SyncFieldItems = listOf(
    "材料名" to "material_name",
    "安全在庫" to "safety_stock",
    "仕入先CD" to "supplier_cd",
    "仕入先名" to "supplier_name",
    "束本数" to "bundle_quantity",
    "束重量" to "bundle_weight",
    "規格" to "standard_spec",
    "単価" to "unit_price",
    "束当たり本数" to "pieces_per_bundle",
    "一本重量" to "long_weight",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialSyncMasterConfirmDialog(
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
                                    "材料マスタを在庫へ同期",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF92400E),
                                )
                                Text(
                                    "materials テーブルの最新情報を material_stock に反映します。既存の在庫行が上書き更新されます。",
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
                        "材料マスタ更新確認",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(
                        "Material Master Sync",
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
fun MaterialDataGenerationDialog(
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
        OrderDailyDatePickerDialog(
            value = value,
            accent = accent,
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
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
fun MaterialManualOrderDialog(
    form: MaterialManualOrderFormUi,
    materialOptions: List<MaterialMasterItemDto>,
    selectedMaterial: MaterialMasterItemDto?,
    loading: Boolean,
    onDateChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onOrderQuantityChange: (Int) -> Unit,
    onOrderBundleQuantityChange: (Int) -> Unit,
    onRemarksChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }
    val calculatedWeight = form.orderBundleQuantity * (selectedMaterial?.longWeight ?: 0.0)
    val calculatedAmount = calculatedWeight * (selectedMaterial?.unitPrice ?: 0.0)

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(16.dp, SyncDialogShape)
                .clip(SyncDialogShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), SyncDialogShape),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text("材料注文追加", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("新しい材料注文を手動で入力", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, enabled = !loading) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFF94A3B8))
                    }
                }

                ManualOrderFieldLabel("日付")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .clickable(enabled = !loading) { showDatePicker = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(form.date.ifBlank { "日付を選択" }, fontSize = 12.sp, color = Color(0xFF334155))
                }

                ManualOrderFieldLabel("材料")
                ExposedDropdownMenuBox(
                    expanded = materialExpanded,
                    onExpandedChange = { if (!loading) materialExpanded = it },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (form.materialCd.isBlank()) "材料を選択" else "${form.materialCd} - ${form.materialName}",
                            fontSize = 12.sp,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = materialExpanded,
                        onDismissRequest = { materialExpanded = false },
                        modifier = Modifier.heightIn(max = 260.dp),
                    ) {
                        materialOptions.forEach { material ->
                            val cd = material.materialCd.orEmpty()
                            DropdownMenuItem(
                                text = { Text("$cd - ${material.materialName.orEmpty()}", fontSize = 12.sp) },
                                onClick = {
                                    onMaterialChange(cd)
                                    materialExpanded = false
                                },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ManualOrderFieldLabel("注文束数")
                        ManualOrderQuantityField(form.orderQuantity, loading) { onOrderQuantityChange(it) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ManualOrderFieldLabel("注文本数")
                        ManualOrderQuantityField(form.orderBundleQuantity, loading) { onOrderBundleQuantityChange(it) }
                    }
                }

                ManualOrderFieldLabel("備考")
                androidx.compose.material3.OutlinedTextField(
                    value = form.remarks,
                    onValueChange = onRemarksChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    textStyle = TextStyle(fontSize = 12.sp),
                    placeholder = { Text("備考（任意）", fontSize = 12.sp) },
                    minLines = 2,
                )

                selectedMaterial?.let { material ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("材料詳細", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569))
                            if (calculatedWeight > 0 || calculatedAmount > 0) {
                                Text(
                                    "重量 ${calculatedWeight.toInt()}kg · 金額 ¥${java.text.NumberFormat.getIntegerInstance(java.util.Locale.JAPAN).format(calculatedAmount.toInt())}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2563EB),
                                )
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ManualOrderDetailChip("仕入先", material.supplierName.orEmpty())
                                ManualOrderDetailChip("規格", material.standardSpec.orEmpty())
                                ManualOrderDetailChip("単価", "¥${material.unitPrice?.toInt() ?: 0}")
                                ManualOrderDetailChip("束本数", "${material.piecesPerBundle ?: 0}")
                                ManualOrderDetailChip("一本重量", "${material.longWeight ?: 0}kg")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassPillButton("キャンセル", onClick = onDismiss)
                    Spacer(Modifier.width(8.dp))
                    GlassPillButton(
                        text = if (loading) "登録中…" else "登録",
                        onClick = onConfirm,
                        filled = !loading,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        OrderDailyDatePickerDialog(
            value = form.date,
            accent = Color(0xFF16A34A),
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun ManualOrderFieldLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
}

@Composable
private fun ManualOrderQuantityField(value: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .clickable(enabled = enabled) { onChange((value - 1).coerceAtLeast(0)) },
            contentAlignment = Alignment.Center,
        ) { Text("−", fontWeight = FontWeight.Bold) }
        Text(value.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .clickable(enabled = enabled) { onChange(value + 1) },
            contentAlignment = Alignment.Center,
        ) { Text("+", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ManualOrderDetailChip(label: String, value: String) {
    Text("$label: ${value.ifBlank { "—" }}", fontSize = 10.sp, color = Color(0xFF64748B))
}

@Composable
fun MaterialPrintOrderConfirmDialog(
    form: MaterialOrderPrintFormUi,
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
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(16.dp, SyncDialogShape)
                .clip(SyncDialogShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), SyncDialogShape),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("注文書印刷確認", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    GlassToolbarButton(
                        label = if (loading) "処理中…" else "印刷実行",
                        icon = Icons.Default.Print,
                        style = GlassButtonStyle.Blue,
                        enabled = !loading,
                        onClick = onConfirm,
                    )
                }
                Text("対象注文: ${orderCount}件", fontSize = 12.sp, color = Color(0xFF64748B))
                PrintFormField("受注先会社名", form.recipientCompany, loading, onRecipientCompanyChange)
                PrintFormField("受注先担当者", form.recipientPersons, loading, onRecipientPersonsChange)
                PrintFormField("承認者", form.approver, loading, onApproverChange)
                PrintFormField("発行者", form.issuer, loading, onIssuerChange)
                PrintFormField("備考1", form.note1, loading, onNote1Change, minLines = 2)
                PrintFormField("備考2", form.note2, loading, onNote2Change, minLines = 2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    GlassPillButton("閉じる", onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun PrintFormField(
    label: String,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit,
    minLines: Int = 1,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = TextStyle(fontSize = 12.sp),
            minLines = minLines,
        )
    }
}
