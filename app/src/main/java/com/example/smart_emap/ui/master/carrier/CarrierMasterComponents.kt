package com.example.smart_emap.ui.master.carrier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterCarrierDto

val carrierPageBackground = Brush.linearGradient(listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)))
private val carrierSkyGradient = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4)))
private val carrierTableHeaderGradient = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4)))
private val carrierAccent = Color(0xFF0EA5E9)
private val carrierControlHeight = 32.dp
private val carrierTableRowHeight = 40.dp
private val carrierTableHeaderHeight = 38.dp

private data class CarrierTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val carrierTableColumns = listOf(
    CarrierTableColumn("carrier_cd", "運送便CD", 96, TextAlign.Center),
    CarrierTableColumn("carrier_name", "運送便名称", 120),
    CarrierTableColumn("contact_person", "連絡人", 80),
    CarrierTableColumn("phone", "電話番号", 100),
    CarrierTableColumn("shipping_time", "出荷時間", 72, TextAlign.Center),
    CarrierTableColumn("report_no", "報告No", 64, TextAlign.Center),
    CarrierTableColumn("note", "備考", 100),
    CarrierTableColumn("status", "状態", 88, TextAlign.Center),
    CarrierTableColumn("actions", "操作", 76, TextAlign.Center),
)

private val carrierTableWidth = (carrierTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun CarrierMasterHeroBar(
    total: Int,
    activeCount: Int,
    actionLoading: Boolean,
    onAdd: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x400EA5E9))
            .clip(shape)
            .background(carrierSkyGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(22.dp),
            )
            Column {
                Text("運送便マスタ管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("運送便情報の登録・編集", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 4.dp)) {
                CarrierStatChip(total.toString(), "件")
                CarrierStatChip(activeCount.toString(), "有効", active = true)
            }
        }
        CarrierOutlineButton("+ 運送便追加", enabled = !actionLoading, onClick = onAdd)
    }
}

@Composable
private fun CarrierStatChip(value: String, label: String, active: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (active) Color(0xFF10B981).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun CarrierOutlineButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrierMasterFilterCard(
    keyword: String,
    statusFilter: String,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(carrierControlHeight)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Box(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                                if (keyword.isEmpty()) {
                                    Text("運送便CD・名称・連絡人で検索...", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                inner()
                            }
                            if (keyword.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp).clickable { onKeywordChange("") },
                                )
                            }
                        }
                    },
                )
            }
            CarrierFilterDropdown(
                label = "状態",
                value = statusFilter,
                options = listOf("" to "全て", "1" to "有効", "0" to "無効"),
                onChange = onStatusChange,
                modifier = Modifier.width(88.dp),
            )
            TextButton(
                onClick = onClear,
                enabled = !actionLoading,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF64748B))
                Text("クリア", fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarrierFilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Surface(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().height(carrierControlHeight),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White,
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        options.firstOrNull { it.first == value }?.second.orEmpty().ifBlank { "全て" },
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (v, labelText) ->
                    DropdownMenuItem(text = { Text(labelText, fontSize = 12.sp) }, onClick = { onChange(v); expanded = false })
                }
            }
        }
    }
}

@Composable
fun CarrierMasterTable(
    carriers: List<MasterCarrierDto>,
    loading: Boolean,
    displayedCount: Int,
    total: Int,
    statusUpdatingIds: Set<Int>,
    onToggleStatus: (MasterCarrierDto, Boolean) -> Unit,
    onEdit: (MasterCarrierDto) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        if (loading && carriers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = carrierAccent, strokeWidth = 2.dp)
            }
            return@Card
        }
        Column(modifier = Modifier.horizontalScroll(hScroll)) {
            Row(
                modifier = Modifier
                    .width(carrierTableWidth)
                    .height(carrierTableHeaderHeight)
                    .background(carrierTableHeaderGradient)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                carrierTableColumns.forEach { col ->
                    Text(
                        col.label,
                        modifier = Modifier.width(col.widthDp.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = col.align,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            if (carriers.isEmpty()) {
                Box(modifier = Modifier.width(carrierTableWidth).height(120.dp), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                carriers.forEachIndexed { index, carrier ->
                    val id = carrier.id ?: return@forEachIndexed
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
                    val inactive = carrier.status != 1
                    Row(
                        modifier = Modifier
                            .width(carrierTableWidth)
                            .height(carrierTableRowHeight)
                            .background(bg)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0F9FF), modifier = Modifier.width(96.dp)) {
                            Text(
                                carrier.carrierCd.orEmpty(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0284C7),
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            carrier.carrierName.orEmpty(),
                            modifier = Modifier.width(120.dp).padding(horizontal = 4.dp),
                            fontSize = 11.sp,
                            color = if (inactive) Color(0xFF94A3B8) else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            carrier.contactPerson.orEmpty().ifBlank { "—" },
                            modifier = Modifier.width(80.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            carrier.phone.orEmpty().ifBlank { "—" },
                            modifier = Modifier.width(100.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            formatCarrierShippingTime(carrier.shippingTime).ifBlank { "—" },
                            modifier = Modifier.width(72.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                        Text(
                            carrier.reportNo.orEmpty().ifBlank { "—" },
                            modifier = Modifier.width(64.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            carrier.note.orEmpty().ifBlank { "—" },
                            modifier = Modifier.width(100.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(modifier = Modifier.width(88.dp), contentAlignment = Alignment.Center) {
                            if (id in statusUpdatingIds) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = carrierAccent)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Switch(
                                        checked = carrier.status == 1,
                                        onCheckedChange = { onToggleStatus(carrier, it) },
                                        modifier = Modifier.size(width = 32.dp, height = 18.dp),
                                        colors = SwitchDefaults.colors(checkedTrackColor = carrierAccent),
                                    )
                                    Text(
                                        if (carrier.status == 1) "有効" else "無効",
                                        fontSize = 9.sp,
                                        color = if (carrier.status == 1) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                    )
                                }
                            }
                        }
                        Row(modifier = Modifier.width(76.dp), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { onEdit(carrier) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "編集", modifier = Modifier.size(15.dp), tint = Color(0xFFF59E0B))
                            }
                            TextButton(onClick = { onDelete(id) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", modifier = Modifier.size(15.dp), tint = Color(0xFFA78BFA))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("表示: $displayedCount / $total 件", fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}
