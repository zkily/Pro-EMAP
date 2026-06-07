package com.example.smart_emap.ui.master.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProductTypeSelectorDialog(
    selectedTypes: Set<String>,
    onToggleType: (String, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("製品種別選択", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "印刷する製品種別を選択してください：",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                )
                productTypePrintOptions.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(
                            checked = selectedTypes.contains(type),
                            onCheckedChange = { checked -> onToggleType(type, checked) },
                        )
                        Text(type, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("確定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
fun ProductScrapLengthConfirmDialog(
    loading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("端材長計算", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "全製品の端材長を一括再計算し、データベースに保存します。よろしいですか？",
                fontSize = 13.sp,
                color = Color(0xFF475569),
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !loading) {
                Text(if (loading) "実行中..." else "実行")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
        },
        shape = RoundedCornerShape(12.dp),
    )
}
