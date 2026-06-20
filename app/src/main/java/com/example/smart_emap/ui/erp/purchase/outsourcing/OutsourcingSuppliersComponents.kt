package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OutsourcingSupplierDto

private val HeroGradient = Brush.linearGradient(listOf(Color(0xCCFFFFFF), Color(0x99FFFFFF)))
private val FilterAccent = Color(0xFF667EEA)

@Composable
fun OutsourcingSuppliersHeroBar(count: Int, actionLoading: Boolean, onCreate: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape)
            .clip(shape)
            .background(HeroGradient)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("外注先マスタ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text("外注業者の登録・管理", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.22f)) {
                Text(
                    "$count 件",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.clickable(enabled = !actionLoading, onClick = onCreate),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (actionLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("新規登録", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutsourcingSuppliersFilterBar(
    filterType: String,
    filterIsActive: Boolean?,
    keyword: String,
    onTypeChange: (String) -> Unit,
    onActiveChange: (Boolean?) -> Unit,
    onKeywordChange: (String) -> Unit,
    onReset: () -> Unit,
) {
    val scroll = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SupplierFilterGroup("種別", Icons.Default.Business) {
                SupplierCompactDropdown(
                    value = filterType,
                    options = OUTSOURCING_SUPPLIER_TYPE_OPTIONS,
                    width = 120.dp,
                    onSelect = onTypeChange,
                )
            }
            SupplierFilterDivider()
            SupplierFilterGroup("状態") {
                SupplierCompactDropdown(
                    value = when (filterIsActive) {
                        true -> "true"
                        false -> "false"
                        null -> ""
                    },
                    options = listOf("" to "すべて", "true" to "有効", "false" to "無効"),
                    width = 88.dp,
                    onSelect = { v ->
                        onActiveChange(
                            when (v) {
                                "true" -> true
                                "false" -> false
                                else -> null
                            },
                        )
                    },
                )
            }
            SupplierFilterDivider()
            SupplierFilterGroup("キーワード", Icons.Default.Search) {
                SupplierKeywordField(keyword, onKeywordChange)
            }
            IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "リセット", tint = FilterAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun OutsourcingSuppliersTablePanel(
    isLoading: Boolean,
    suppliers: List<OutsourcingSupplierDto>,
    togglingId: Int?,
    onAdd: () -> Unit,
    onEdit: (OutsourcingSupplierDto) -> Unit,
    onDelete: (OutsourcingSupplierDto) -> Unit,
    onToggle: (OutsourcingSupplierDto, Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.97f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        when {
            isLoading && suppliers.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = FilterAccent)
                }
            }
            suppliers.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("🏭", fontSize = 24.sp)
                    Text("外注先が登録されていません", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    TextButton(onClick = onAdd) {
                        Text("新規登録", fontSize = 11.sp, color = FilterAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.width(920.dp)) {
                        OutsourcingSuppliersTable(suppliers, togglingId, onEdit, onDelete, onToggle)
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingSuppliersTable(
    suppliers: List<OutsourcingSupplierDto>,
    togglingId: Int?,
    onEdit: (OutsourcingSupplierDto) -> Unit,
    onDelete: (OutsourcingSupplierDto) -> Unit,
    onToggle: (OutsourcingSupplierDto, Boolean) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4FF))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("コード", "外注先名", "種別", "住所", "電話", "担当", "LT", "状態", "操作").forEach { h ->
                Text(
                    h,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF606266),
                    modifier = Modifier.weight(if (h == "外注先名" || h == "住所") 1.3f else 0.85f),
                    maxLines = 1,
                )
            }
        }
        HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
        suppliers.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFAFA)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(bg)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    row.supplierCd.orEmpty(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5B6DE0),
                    modifier = Modifier.weight(0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(row.supplierName.orEmpty(), fontSize = 10.sp, modifier = Modifier.weight(1.3f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(modifier = Modifier.weight(0.85f)) {
                    OutsourcingTag(
                        text = outsourcingSupplierTypeLabel(row.supplierType),
                        containerColor = outsourcingSupplierTypeColor(row.supplierType),
                    )
                }
                Text(row.address.orEmpty().ifBlank { "—" }, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.3f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.phone.orEmpty().ifBlank { "—" }, fontSize = 9.sp, modifier = Modifier.weight(0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.contactPerson.orEmpty().ifBlank { "—" }, fontSize = 9.sp, modifier = Modifier.weight(0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${row.leadTimeDays ?: 0}日", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667EEA), modifier = Modifier.weight(0.85f))
                Switch(
                    checked = row.isActive == true,
                    onCheckedChange = { onToggle(row, it) },
                    enabled = togglingId != row.id,
                    modifier = Modifier.weight(0.85f),
                )
                Row(modifier = Modifier.weight(0.85f), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    TextButton(onClick = { onEdit(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("編集", fontSize = 9.sp, color = Color(0xFF409EFF))
                    }
                    TextButton(onClick = { onDelete(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("削除", fontSize = 9.sp, color = Color(0xFFF56C6C))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun SupplierFilterGroup(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = FilterAccent, modifier = Modifier.size(14.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FilterAccent)
        }
        content()
    }
}

@Composable
private fun SupplierFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFFE2E8F0)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierCompactDropdown(
    value: String,
    options: List<Pair<String, String>>,
    width: androidx.compose.ui.unit.Dp,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(value) { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "すべて" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier
                .width(width)
                .height(32.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0x33667EEA)),
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(display, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = { onSelect(code); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SupplierKeywordField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(32.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0x33667EEA)),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
                cursorBrush = SolidColor(FilterAccent),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("外注先名/コード", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    inner()
                },
            )
        }
    }
}
