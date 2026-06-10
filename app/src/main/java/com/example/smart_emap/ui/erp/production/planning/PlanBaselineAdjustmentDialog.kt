package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PlanBaselineAdjustmentDialog(
    baselineMonth: String,
    processName: String,
    monthLabel: String,
    items: List<PlanBaselineAdjustmentItem>,
    loading: Boolean,
    onMonthChange: (String) -> Unit,
    onProcessChange: (String) -> Unit,
    onLoad: () -> Unit,
    onReset: () -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onSaveRow: (Int) -> Unit,
    onDeleteRow: (Int) -> Unit,
    onBatchSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.96f), shape = RoundedCornerShape(14.dp), color = Color.White) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ベースライン計画修正", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("基準月の計画値をまとめて再調整", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f)) {
                        Text(monthLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White)
                    }
                }
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ProductionSingleDatePickerField(baselineMonth, onMonthChange, "基準月", modifier = Modifier.weight(1f))
                        ProductionDropdownFilter("工程", processName, PlanBaselineLogic.processOptions, onProcessChange, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = onLoad, enabled = !loading) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("データ取得", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        OutlinedButton(onClick = onReset, enabled = !loading) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("リセット", fontSize = 11.sp)
                        }
                    }
                    if (loading) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (items.isEmpty()) {
                        Text("該当するデータがありません", color = ProductionPlanningColors.TextSecondary, modifier = Modifier.padding(16.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items.forEachIndexed { index, item ->
                                PlanBaselineAdjustmentRow(item, index, onQuantityChange, onSaveRow, onDeleteRow)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = onBatchSave, enabled = !loading && items.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))) {
                            Text("変更を一括保存", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(start = 8.dp)) { Text("閉じる") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanBaselineAdjustmentRow(
    item: PlanBaselineAdjustmentItem,
    index: Int,
    onQuantityChange: (Int, String) -> Unit,
    onSaveRow: (Int) -> Unit,
    onDeleteRow: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProductionPlanningColors.CardBorder),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(PlanBaselineLogic.formatBaselineDate(item.planDate), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE0E7FF)) {
                    Text(item.processName.ifBlank { "未指定" }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF4338CA))
                }
            }
            Text("現在値 ${formatProductionNumber(item.planQuantity)}", fontSize = 10.sp, color = ProductionPlanningColors.TextSecondary)
            BasicTextField(
                value = item.tempPlanQuantity,
                onValueChange = { onQuantityChange(index, it) },
                enabled = !item.saving && !item.deleting,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(1.dp, ProductionPlanningColors.CardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(fontSize = 12.sp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { onSaveRow(index) }, enabled = !item.saving && !item.deleting, modifier = Modifier.weight(1f)) {
                    if (item.saving) CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    else Text("保存", fontSize = 11.sp)
                }
                OutlinedButton(onClick = { onDeleteRow(index) }, enabled = !item.saving && !item.deleting, modifier = Modifier.weight(1f)) {
                    Text("削除", fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
