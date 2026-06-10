package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PlanBaselineFixedGenerateDialog(
    processName: String,
    weekdayBaseline: String,
    saturdayBaseline: String,
    sundayBaseline: String,
    loading: Boolean,
    onWeekdayChange: (String) -> Unit,
    onSaturdayChange: (String) -> Unit,
    onSundayChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("基準計画の入力", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "工程「$processName」は、平日（月〜金）は同じ基準計画を各日に書き込みます。土曜・日曜は通常は書き込みません。",
                    fontSize = 11.sp,
                    color = ProductionPlanningColors.TextSecondary,
                    lineHeight = 16.sp,
                )
                FixedBaselineNumberField("平日（月〜金）の基準計画数（必須）", weekdayBaseline, onWeekdayChange, loading)
                FixedBaselineNumberField("土曜（任意）", saturdayBaseline, onSaturdayChange, loading)
                FixedBaselineNumberField("日曜（任意）", sundayBaseline, onSundayChange, loading)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    Button(onClick = onConfirm, enabled = !loading, modifier = Modifier.padding(start = 8.dp)) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("生成")
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedBaselineNumberField(label: String, value: String, onChange: (String) -> Unit, enabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ProductionPlanningColors.TextPrimary)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, ProductionPlanningColors.CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            textStyle = TextStyle(fontSize = 12.sp),
        )
    }
}
