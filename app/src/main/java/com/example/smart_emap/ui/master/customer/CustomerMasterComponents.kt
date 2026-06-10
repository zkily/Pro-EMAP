package com.example.smart_emap.ui.master.customer

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
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
import com.example.smart_emap.data.model.MasterCustomerDto

val customerPageBackground = Brush.linearGradient(listOf(Color(0xFFF0F4F8), Color(0xFFE2E8F0)))
private val customerSkyGradient = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)))
private val customerControlHeight = 32.dp
private val customerTableRowHeight = 40.dp
private val customerTableHeaderHeight = 38.dp

private data class CustomerTableColumn(
    val key: String,
    val label: String,
    val widthDp: Int,
    val align: TextAlign = TextAlign.Start,
)

private val customerTableColumns = listOf(
    CustomerTableColumn("customer_cd", "顧客CD", 96, TextAlign.Center),
    CustomerTableColumn("customer_name", "顧客名", 120),
    CustomerTableColumn("phone", "電話", 100),
    CustomerTableColumn("address", "住所", 140),
    CustomerTableColumn("customer_type", "種別", 64, TextAlign.Center),
    CustomerTableColumn("status", "状態", 72, TextAlign.Center),
    CustomerTableColumn("actions", "操作", 80, TextAlign.Center),
)

private val customerTableWidth = (customerTableColumns.sumOf { it.widthDp } + 16).dp

@Composable
fun CustomerMasterHeroBar(total: Int, activeCount: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x400EA5E9))
            .clip(shape)
            .background(customerSkyGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.People, contentDescription = null, tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.size(22.dp))
            Column {
                Text("顧客マスタ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("顧客情報の登録・編集・管理", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CustomerStatChip(total.toString(), "総件数")
            CustomerStatChip(activeCount.toString(), "有効")
        }
    }
}

@Composable
private fun CustomerStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMasterFilterCard(
    keyword: String,
    statusFilter: String,
    typeFilter: String,
    displayedCount: Int,
    total: Int,
    hasActiveFilters: Boolean,
    actionLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(15.dp))
                    Text("検索・絞り込み", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF334155))
                    Text("表示 $displayedCount / $total", fontSize = 10.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 6.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onClear, enabled = !actionLoading, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF64748B))
                        Text("クリア", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    CustomerGradientButton("顧客追加", customerSkyGradient, Icons.Default.Add, enabled = !actionLoading, onClick = onAdd)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("キーワード", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    BasicTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(customerControlHeight)
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
                                    if (keyword.isEmpty()) Text("顧客CD・名称・電話", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    inner()
                                }
                                if (keyword.isNotEmpty()) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).clickable { onKeywordChange("") })
                                }
                            }
                        },
                    )
                }
                CustomerFilterDropdown("状態", statusFilter, listOf("" to "全て", "1" to "有効", "0" to "無効"), onStatusChange, Modifier.width(88.dp))
                CustomerFilterDropdown("種別", typeFilter, listOf("" to "全て", "法人" to "法人", "個人" to "個人", "代理店" to "代理店"), onTypeChange, Modifier.width(88.dp))
                CustomerGradientButton("検索", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))), Icons.Default.Search, enabled = !actionLoading, onClick = onSearch)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerFilterDropdown(
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
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().height(customerControlHeight),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White,
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(options.firstOrNull { it.first == value }?.second.orEmpty().ifBlank { "全て" }, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun CustomerGradientButton(
    label: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.background(brush, RoundedCornerShape(8.dp)),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun CustomerMasterTable(
    customers: List<MasterCustomerDto>,
    loading: Boolean,
    statusUpdatingIds: Set<Int>,
    onToggleStatus: (MasterCustomerDto, Boolean) -> Unit,
    onEdit: (MasterCustomerDto) -> Unit,
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
        if (loading && customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0EA5E9), strokeWidth = 2.dp)
            }
            return@Card
        }
        Column(modifier = Modifier.horizontalScroll(hScroll)) {
            Row(
                modifier = Modifier
                    .width(customerTableWidth)
                    .height(customerTableHeaderHeight)
                    .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                customerTableColumns.forEach { col ->
                    Text(col.label, modifier = Modifier.width(col.widthDp.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = col.align)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            if (customers.isEmpty()) {
                Box(modifier = Modifier.width(customerTableWidth).height(120.dp), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                customers.forEachIndexed { index, customer ->
                    val id = customer.id ?: return@forEachIndexed
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFBFC)
                    Row(
                        modifier = Modifier
                            .width(customerTableWidth)
                            .height(customerTableRowHeight)
                            .background(bg)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0F9FF), modifier = Modifier.width(96.dp)) {
                            Text(
                                customer.customerCd.orEmpty(),
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
                        Text(customer.customerName.orEmpty(), modifier = Modifier.width(120.dp).padding(horizontal = 4.dp), fontSize = 11.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(customer.phone.orEmpty().ifBlank { "—" }, modifier = Modifier.width(100.dp), fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(customer.address.orEmpty().ifBlank { "—" }, modifier = Modifier.width(140.dp), fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        CustomerTypeBadge(customer.customerType, Modifier.width(64.dp))
                        Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                            if (id in statusUpdatingIds) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF0EA5E9))
                            } else {
                                Switch(
                                    checked = customer.status == 1,
                                    onCheckedChange = { onToggleStatus(customer, it) },
                                    modifier = Modifier.size(width = 36.dp, height = 20.dp),
                                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF0EA5E9)),
                                )
                            }
                        }
                        Row(modifier = Modifier.width(80.dp), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { onEdit(customer) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "編集", modifier = Modifier.size(15.dp), tint = Color(0xFF0EA5E9))
                            }
                            TextButton(onClick = { onDelete(id) }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", modifier = Modifier.size(15.dp), tint = Color(0xFFEF4444))
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
private fun CustomerTypeBadge(type: String?, modifier: Modifier = Modifier) {
    val (bg, fg) = when (type) {
        "法人" -> Color(0xFFEEF2FF) to Color(0xFF4F46E5)
        "個人" -> Color(0xFFECFDF5) to Color(0xFF059669)
        "代理店" -> Color(0xFFFFFBEB) to Color(0xFFD97706)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(10.dp), color = bg) {
            Text(type.orEmpty().ifBlank { "—" }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Medium, color = fg)
        }
    }
}
