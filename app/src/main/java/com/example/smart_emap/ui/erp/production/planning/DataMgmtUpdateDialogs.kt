package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto

@Composable
fun DataMgmtSimpleConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "実行",
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DataMgmtInfoDialogShell(
        title = title,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmLabel = confirmLabel,
        confirmColor = Color(0xFF3B82F6),
    ) {
        Text(message, fontSize = 13.sp, color = ProductionPlanningColors.TextPrimary)
    }
}

private val allUpdateAccent = Color(0xFF3B82F6)
private val allUpdateBorder = Color(0xFFE2E8F0)

@Composable
fun DataMgmtAllUpdateConfirmDialog(
    steps: List<String>,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth(0.92f)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, allUpdateBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEFF6FF), Color.White)))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "全部一括更新確認",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = allUpdateBorder)
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(allUpdateAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "以下の順で一括更新します",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, allUpdateBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        steps.forEachIndexed { index, step ->
                            AllUpdateStepRow(index = index + 1, label = step)
                        }
                    }
                    Text(
                        "この処理には時間がかかる場合があります。",
                        fontSize = 12.sp,
                        color = ProductionPlanningColors.TextSecondary,
                    )
                }
            }
            HorizontalDivider(color = allUpdateBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BatchActualOutlinedButton("キャンセル", onDismiss, enabled = !loading)
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = allUpdateAccent),
                    modifier = Modifier.height(36.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("一括更新開始", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AllUpdateStepRow(index: Int, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                index.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = allUpdateAccent,
            )
        }
        Text(
            label,
            fontSize = 13.sp,
            color = ProductionPlanningColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun DataMgmtPlanConfirmDialog(
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DataMgmtInfoDialogShell(
        title = "計画データ更新確認",
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmLabel = "更新",
        confirmColor = Color(0xFF3B82F6),
    ) {
        Text(
            "計画データを更新しますか？",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = ProductionPlanningColors.TextPrimary,
        )
        Text(
            DataMgmtUpdateConfig.planConfirmMessage,
            fontSize = 12.sp,
            color = ProductionPlanningColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private val inventoryTrendAccent = Color(0xFF059669)
private val inventoryTrendBorder = Color(0xFFE2E8F0)

@Composable
fun DataMgmtInventoryTrendConfirmDialog(
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.9f)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, inventoryTrendBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFECFDF5), Color.White)))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF34D399)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "在庫・推移更新確認",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = inventoryTrendBorder)
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Text(
                    "在庫・推移を更新しますか？",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
            }
            HorizontalDivider(color = inventoryTrendBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BatchActualOutlinedButton("キャンセル", onDismiss, enabled = !loading)
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = inventoryTrendAccent),
                    modifier = Modifier.height(36.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("更新", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DataMgmtDateRangeUpdateDialog(
    title: String,
    headline: String,
    description: String,
    startDate: String,
    endDate: String,
    loading: Boolean = false,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DataMgmtInfoDialogShell(
        title = title,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmLabel = "更新",
        confirmColor = Color(0xFF3B82F6),
    ) {
        Text(headline, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = ProductionPlanningColors.TextPrimary)
        Text("更新期間", fontSize = 12.sp, color = ProductionPlanningColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
        ProductionDateRangePickerField(
            startDate = startDate,
            endDate = endDate,
            onStartChange = onStartDateChange,
            onEndChange = onEndDateChange,
            modifier = Modifier.fillMaxWidth(),
            startLabel = "開始日",
            endLabel = "終了日",
        )
        if (description.isNotBlank()) {
            Text(description, fontSize = 12.sp, color = ProductionPlanningColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

private val productMasterAccent = Color(0xFF3B82F6)
private val productMasterBorder = Color(0xFFE2E8F0)

@Composable
fun DataMgmtProductMasterUpdateDialog(
    startDate: String,
    endDate: String,
    loading: Boolean = false,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, productMasterBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEFF6FF), Color.White)))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "製品マスタ更新",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = productMasterBorder)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "製品マスタを更新しますか？",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, productMasterBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "更新期間",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProductionPlanningColors.TextSecondary,
                    )
                    ProductionDateRangePickerField(
                        startDate = startDate,
                        endDate = endDate,
                        onStartChange = onStartDateChange,
                        onEndChange = onEndDateChange,
                        modifier = Modifier.fillMaxWidth(),
                        startLabel = "開始日",
                        endLabel = "終了日",
                    )
                }
            }
            HorizontalDivider(color = productMasterBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BatchActualOutlinedButton("キャンセル", onDismiss, enabled = !loading)
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading && startDate.isNotBlank() && endDate.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = productMasterAccent),
                    modifier = Modifier.height(36.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("更新", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private val machineUpdateAccent = Color(0xFF6366F1)
private val machineUpdateBorder = Color(0xFFE2E8F0)

@Composable
fun DataMgmtMachineUpdateDialog(
    startDate: String,
    endDate: String,
    loading: Boolean = false,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, machineUpdateBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color.White)))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "設備フィールド更新",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ProductionPlanningColors.TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = machineUpdateBorder)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "機器フィールドを更新しますか？",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, machineUpdateBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "更新期間",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProductionPlanningColors.TextSecondary,
                    )
                    ProductionDateRangePickerField(
                        startDate = startDate,
                        endDate = endDate,
                        onStartChange = onStartDateChange,
                        onEndChange = onEndDateChange,
                        modifier = Modifier.fillMaxWidth(),
                        startLabel = "開始日",
                        endLabel = "終了日",
                    )
                }
            }
            HorizontalDivider(color = machineUpdateBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BatchActualOutlinedButton("キャンセル", onDismiss, enabled = !loading)
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading && startDate.isNotBlank() && endDate.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = machineUpdateAccent),
                    modifier = Modifier.height(36.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("更新", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataMgmtBatchInitialStockDialog(
    month: String,
    processCd: String,
    processOptions: List<Pair<String, String>>,
    rows: List<BatchInitialStockRow>,
    loading: Boolean,
    saving: Boolean,
    onMonthChange: (String) -> Unit,
    onProcessChange: (String) -> Unit,
    onQuantityChange: (Int, Int?) -> Unit,
    onSearch: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var processExpanded by remember { mutableStateOf(false) }
    val totalQty = rows.sumOf { it.editQuantity ?: 0 }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(0.95f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            DataMgmtDialogHeader("初期在庫一括登録", onDismiss)
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("月（必須）", fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    ProductionSingleDatePickerField(
                        value = if (month.length >= 7) "${month}-01" else month,
                        onChange = { picked -> onMonthChange(picked.take(7)) },
                        label = "月",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("工程（必須）", fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    ExposedDropdownMenuBox(
                        expanded = processExpanded,
                        onExpandedChange = { processExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = processOptions.find { it.first == processCd }?.let { "${it.first} - ${it.second}" }
                                ?: processCd,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("工程を選択") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = processExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = processExpanded, onDismissRequest = { processExpanded = false }) {
                            processOptions.forEach { (cd, name) ->
                                DropdownMenuItem(
                                    text = { Text("$cd - $name", fontSize = 13.sp) },
                                    onClick = {
                                        onProcessChange(cd)
                                        processExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onSearch, enabled = !loading && !saving) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("検索", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = !loading && !saving && rows.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    ) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("一括保存", fontSize = 12.sp)
                    }
                }
                if (rows.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    ) {
                        Text("製品CD", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
                        Text("製品名", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("初期在庫", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        itemsIndexed(rows, key = { _, item -> item.productCd }) { index, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(row.productCd, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                                Text(
                                    row.productName,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = row.editQuantity?.toString().orEmpty(),
                                    onValueChange = { raw ->
                                        val qty = raw.trim().toIntOrNull()
                                        onQuantityChange(index, qty)
                                    },
                                    modifier = Modifier.width(90.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                )
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                    Text("合計: $totalQty", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

private val batchActualAccent = Color(0xFF3B82F6)
private val batchActualTableBorder = Color(0xFFE2E8F0)
private val batchActualHeaderBg = Color(0xFFF8FAFC)
private val batchActualStripeBg = Color(0xFFFAFBFC)

@Composable
fun DataMgmtBatchActualDialog(
    date: String,
    rows: List<BatchActualRow>,
    productOptions: List<ProductionSummaryProductOptionDto>,
    saving: Boolean,
    onDateChange: (String) -> Unit,
    onProductChange: (Int, String) -> Unit,
    onCuttingChange: (Int, Int?) -> Unit,
    onChamferingChange: (Int, Int?) -> Unit,
    onMoldingChange: (Int, Int?) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 780.dp)
                .fillMaxWidth(0.96f)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, batchActualTableBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFF8FAFF), Color.White)))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "実績一括登録",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = batchActualTableBorder)
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, batchActualTableBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "日付（必須）",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProductionPlanningColors.TextSecondary,
                    )
                    ProductionSingleDatePickerField(
                        value = date,
                        onChange = onDateChange,
                        label = "日付",
                        modifier = Modifier.widthIn(min = 160.dp, max = 200.dp),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, batchActualTableBorder, RoundedCornerShape(10.dp)),
                ) {
                    BatchActualTableHeaderRow()
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        itemsIndexed(rows, key = { index, item -> item.productCd.ifBlank { "row-$index" } }) { index, row ->
                            BatchActualTableDataRow(
                                row = row,
                                displayDate = row.date.ifBlank { date }.ifBlank { "—" },
                                productOptions = productOptions,
                                striped = index % 2 == 1,
                                onProductChange = { onProductChange(index, it) },
                                onCuttingChange = { onCuttingChange(index, it) },
                                onChamferingChange = { onChamferingChange(index, it) },
                                onMoldingChange = { onMoldingChange(index, it) },
                            )
                            if (index < rows.lastIndex) {
                                HorizontalDivider(color = batchActualTableBorder, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = batchActualTableBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BatchActualOutlinedButton("リセット", onReset, enabled = !saving)
                Spacer(Modifier.width(10.dp))
                BatchActualOutlinedButton("キャンセル", onDismiss, enabled = !saving)
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onSave,
                    enabled = !saving,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = batchActualAccent),
                    modifier = Modifier.height(36.dp),
                ) {
                    if (saving) {
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("保存", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BatchActualTableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(batchActualHeaderBg)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "製品",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            modifier = Modifier.weight(1.45f),
        )
        Text(
            "日付",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(96.dp),
        )
        BatchActualHeaderQtyLabel("切断実績", Modifier.width(108.dp))
        BatchActualHeaderQtyLabel("面取実績", Modifier.width(108.dp))
        BatchActualHeaderQtyLabel("成型実績", Modifier.width(108.dp))
    }
}

@Composable
private fun BatchActualHeaderQtyLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun BatchActualTableDataRow(
    row: BatchActualRow,
    displayDate: String,
    productOptions: List<ProductionSummaryProductOptionDto>,
    striped: Boolean,
    onProductChange: (String) -> Unit,
    onCuttingChange: (Int?) -> Unit,
    onChamferingChange: (Int?) -> Unit,
    onMoldingChange: (Int?) -> Unit,
) {
    var productExpanded by remember(row.productCd) { mutableStateOf(false) }
    val productLabel = productOptions.find { it.productCd == row.productCd }?.let { p ->
        if (!p.productName.isNullOrBlank()) "${p.productCd} - ${p.productName}" else p.productCd
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (striped) batchActualStripeBg else Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1.45f)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { productExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, batchActualTableBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        productLabel ?: "製品を選択",
                        fontSize = 12.sp,
                        color = if (productLabel != null) ProductionPlanningColors.TextPrimary else ProductionPlanningColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = ProductionPlanningColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            DropdownMenu(
                expanded = productExpanded,
                onDismissRequest = { productExpanded = false },
                modifier = Modifier
                    .widthIn(min = 220.dp, max = 360.dp)
                    .heightIn(max = 280.dp),
            ) {
                productOptions.forEach { p ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (!p.productName.isNullOrBlank()) "${p.productCd} - ${p.productName}" else p.productCd,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            onProductChange(p.productCd)
                            productExpanded = false
                        },
                    )
                }
            }
        }
        Text(
            displayDate,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = ProductionPlanningColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(96.dp),
        )
        BatchActualQtyStepper(row.cuttingActual, Modifier.width(108.dp), onCuttingChange)
        BatchActualQtyStepper(row.chamferingActual, Modifier.width(108.dp), onChamferingChange)
        BatchActualQtyStepper(row.moldingActual, Modifier.width(108.dp), onMoldingChange)
    }
}

@Composable
private fun BatchActualQtyStepper(
    value: Int?,
    modifier: Modifier = Modifier,
    onChange: (Int?) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val btnShape = RoundedCornerShape(6.dp)
    val display = value?.toString().orEmpty()
    val current = value ?: 0
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, batchActualTableBorder, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .clip(btnShape)
                .clickable {
                    val next = current - 1
                    onChange(if (next <= 0) null else next)
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = batchActualTableBorder,
        )
        BasicTextField(
            value = display,
            onValueChange = { raw ->
                val trimmed = raw.trim()
                onChange(if (trimmed.isEmpty()) null else trimmed.toIntOrNull()?.coerceAtLeast(0))
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ProductionPlanningColors.TextPrimary,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(batchActualAccent),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    inner()
                }
            },
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = batchActualTableBorder,
        )
        Box(
            modifier = Modifier
                .width(30.dp)
                .fillMaxHeight()
                .clip(btnShape)
                .clickable {
                    onChange(if (current <= 0) 1 else current + 1)
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun BatchActualOutlinedButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, batchActualTableBorder),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(label, fontSize = 13.sp, color = ProductionPlanningColors.TextPrimary)
        }
    }
}

@Composable
private fun DataMgmtDialogHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ProductionPlanningColors.TextPrimary)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DataMgmtInfoDialogShell(
    title: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    confirmColor: Color,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth(0.92f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            DataMgmtDialogHeader(title, onDismiss)
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    content()
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                DataMgmtDialogTextButton("キャンセル", onDismiss, enabled = !loading)
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(confirmLabel, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DataMgmtDialogTextButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = ProductionPlanningColors.TextPrimary),
    ) {
        Text(label, fontSize = 12.sp)
    }
}
