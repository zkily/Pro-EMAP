package com.example.smart_emap.ui.master.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val printSortOptions = listOf(
    "supplier_name" to "仕入先名",
    "material_cd" to "材料CD",
    "material_name" to "材料名",
)

private val printColumnLabels = mapOf(
    "material_cd" to "材料CD",
    "material_name" to "材料名",
    "material_type" to "種類",
    "standard_spec" to "規格",
    "unit" to "単位",
    "supply_classification" to "支給区分",
    "usegae" to "用途",
    "supplier_name" to "仕入先名",
    "storage_location" to "保管場所",
    "status" to "状態",
    "note" to "備考",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPrintSettingsDialog(
    settings: MaterialPrintSettings,
    onChange: (MaterialPrintSettings) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var sortExpanded by remember { mutableStateOf(false) }
    val sortLabel = printSortOptions.find { it.first == settings.sortBy }?.second ?: settings.sortBy

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("印刷設定", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("表示列", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                printColumnLabels.forEach { (key, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = settings.columns[key] ?: false,
                            onCheckedChange = { checked ->
                                onChange(settings.copy(columns = settings.columns + (key to checked)))
                            },
                        )
                        Text(label, fontSize = 13.sp)
                    }
                }
                Text("並び順", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                ExposedDropdownMenuBox(expanded = sortExpanded, onExpandedChange = { sortExpanded = it }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(sortLabel, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            ExposedDropdownMenuDefaults.TrailingIcon(sortExpanded)
                        }
                    }
                    ExposedDropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        printSortOptions.forEach { (value, label) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(label, fontSize = 13.sp) },
                                onClick = {
                                    onChange(settings.copy(sortBy = value))
                                    sortExpanded = false
                                },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onChange(settings.copy(sortOrderAsc = true)) }) {
                        Text(if (settings.sortOrderAsc) "▲ 昇順" else "昇順", fontSize = 12.sp)
                    }
                    TextButton(onClick = { onChange(settings.copy(sortOrderAsc = false)) }) {
                        Text(if (!settings.sortOrderAsc) "▼ 降順" else "降順", fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = settings.showHeader, onCheckedChange = { onChange(settings.copy(showHeader = it)) })
                    Text("ヘッダーを表示", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = settings.showStats, onCheckedChange = { onChange(settings.copy(showStats = it)) })
                    Text("統計を表示", fontSize = 13.sp)
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("印刷") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
        shape = RoundedCornerShape(12.dp),
    )
}
