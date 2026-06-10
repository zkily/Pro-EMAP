package com.example.smart_emap.ui.master.productprocessroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.MasterProcessOptionDto

@Composable
fun ProductProcessRouteProcessSelectDialog(
    processes: List<MasterProcessOptionDto>,
    loading: Boolean,
    onSelect: (MasterProcessOptionDto) -> Unit,
    onDismiss: () -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    val filtered = processes.filter {
        val k = keyword.trim()
        if (k.isBlank()) return@filter true
        it.processCd.orEmpty().contains(k, true) || it.processName.orEmpty().contains(k, true)
    }

    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEEF2FF), Color.White)))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🛠️", fontSize = 18.sp)
                    Text(
                        "工程選択",
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                    )
                    IconButton(onClick = onDismiss, enabled = !loading) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる")
                    }
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                BasicTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(36.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.padding(end = 6.dp))
                            if (keyword.isEmpty()) Text("工程CD・名称で検索", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            inner()
                        }
                    },
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it.processCd.orEmpty() }) { process ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFAFBFC), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                process.processCd.orEmpty(),
                                modifier = Modifier.weight(0.35f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4F46E5),
                            )
                            Text(
                                process.processName.orEmpty(),
                                modifier = Modifier.weight(0.45f),
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                            )
                            Button(
                                onClick = { onSelect(process) },
                                enabled = !loading,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            ) {
                                Text("選択", fontSize = 11.sp)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("閉じる") }
                }
            }
        }
    }
}
