package com.example.smart_emap.ui.master.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun MaterialColumnSettingsDialog(
    draft: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("列設定", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("表示する列を選択してください：", fontSize = 13.sp, color = Color(0xFF475569))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onSelectAll) { Text("全選択", fontSize = 12.sp) }
                    TextButton(onClick = onDeselectAll) { Text("全解除", fontSize = 12.sp) }
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    materialOptionalColumnDefinitions.forEach { col ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = draft[col.key] ?: col.defaultVisible,
                                onCheckedChange = { onToggle(col.key, it) },
                            )
                            Text(col.label, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
        shape = RoundedCornerShape(12.dp),
    )
}
