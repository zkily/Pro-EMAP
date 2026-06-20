package com.example.smart_emap.ui.erp.purchase.outsourcing.plating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingCurrency
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingTodayJapan

private val DialogHeaderGradient = Brush.linearGradient(listOf(Color(0xFF4ECDC4), Color(0xFF44B09E)))
private val FieldBorder = Color(0xFFDCDEE6)
private val LabelColor = Color(0xFF606266)

@Composable
fun PlatingOrderDialogShell(
    title: String,
    loading: Boolean,
    confirmEnabled: Boolean,
    confirmText: String = "登録する",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 720.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DialogHeaderGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Column(
                    modifier = Modifier
                        .background(Color(0xFFF5F7FA))
                        .padding(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(max = 460.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            content = content,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlatingDialogFooterBtn(
                        text = "キャンセル",
                        icon = Icons.Default.Close,
                        filled = false,
                        enabled = !loading,
                        onClick = onDismiss,
                    )
                    PlatingDialogFooterBtn(
                        text = confirmText,
                        icon = Icons.Default.Check,
                        filled = true,
                        enabled = !loading && confirmEnabled,
                        onClick = onConfirm,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatingDialogFooterBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(5.dp)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(32.dp),
        shape = shape,
        color = if (filled) Color.Transparent else Color.White,
        border = BorderStroke(1.dp, if (filled) Color.Transparent else FieldBorder),
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (filled) {
                        Modifier.background(
                            brush = if (enabled) {
                                Brush.linearGradient(listOf(Color(0xFF409EFF), Color(0xFF337ECC)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                            },
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (filled) Color.White else LabelColor)
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (filled) Color.White else LabelColor)
        }
    }
}

@Composable
fun PlatingDialogFormLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.width(56.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LabelColor)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatingDialogDropdown(
    value: String,
    options: List<Pair<String, String>>,
    placeholder: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: placeholder
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(32.dp)
                .clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(6.dp),
            color = if (enabled) Color.White else Color(0xFFF5F7FA),
            border = BorderStroke(1.dp, FieldBorder),
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                Text(display, fontSize = 12.sp, color = if (value.isBlank()) Color(0xFFA8ABB2) else Color(0xFF303133), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 12.sp) },
                    onClick = { onSelect(id); expanded = false },
                )
            }
        }
    }
}

@Composable
fun PlatingDialogDateField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label.isNotBlank()) {
            PlatingDialogFormLabel(label)
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .clickable { showPicker = true },
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            border = BorderStroke(1.dp, FieldBorder),
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                Text(value.ifBlank { "選択" }, fontSize = 12.sp, color = Color(0xFF303133))
            }
        }
    }
    if (showPicker) {
        OrderDailyDatePickerDialog(
            value = value.ifBlank { outsourcingTodayJapan() },
            accent = Color(0xFF4ECDC4),
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
        )
    }
}

@Composable
fun PlatingDialogLoadButton(loading: Boolean, onClick: () -> Unit) {
    val btnShape = RoundedCornerShape(6.dp)
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = btnShape,
        color = Color.Transparent,
        modifier = Modifier
            .height(32.dp)
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF409EFF), Color(0xFF337ECC))),
                shape = btnShape,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Text("読込", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun PlatingDialogMonthShortcuts(
    onPrevMonth: () -> Unit,
    onThisMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        PlatingDialogMiniBtn("前月", filled = false, onClick = onPrevMonth)
        PlatingDialogMiniBtn("今月", filled = true, onClick = onThisMonth)
        PlatingDialogMiniBtn("翌月", filled = false, onClick = onNextMonth)
    }
}

@Composable
private fun PlatingDialogMiniBtn(text: String, filled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (filled) Color(0xFF409EFF) else Color.White,
        border = BorderStroke(1.dp, if (filled) Color(0xFF409EFF) else FieldBorder),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = if (filled) Color.White else LabelColor,
        )
    }
}

@Composable
fun PlatingDialogPlaceholder(loading: Boolean, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
            .background(Color(0xFFF9FAFB))
            .padding(vertical = 18.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF409EFF))
                Text("データ読込中...", fontSize = 13.sp, color = Color(0xFF909399))
            } else {
                Text(message, fontSize = 13.sp, color = Color(0xFF909399), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun PlatingDialogQuantityInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val qty = value.toIntOrNull() ?: 0
    val bg = if (qty > 0) Color.White else Color(0xFFFEF0F0)
    val border = if (qty > 0) FieldBorder else Color(0xFFFBC4C4)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color(0xFF303133), textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Color(0xFF409EFF)),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text("数量", fontSize = 10.sp, color = Color(0xFFC0C4CC))
                }
                inner()
            }
        },
    )
}

@Composable
fun PlatingCreateProductTable(
    products: List<PlatingOrderProductRowUi>,
    onQuantityChange: (String, String) -> Unit,
) {
    val scroll = rememberScrollState()
    val minWidth = 760.dp
    Column(Modifier.horizontalScroll(scroll).widthIn(min = minWidth)) {
        PlatingDialogTableHeader(
            listOf(
                "製品CD" to 80.dp,
                "製品名" to 120.dp,
                "単価" to 70.dp,
                "納入場所" to 140.dp,
                "区分" to 130.dp,
                "内容" to 100.dp,
                "数量" to 120.dp,
            ),
        )
        products.forEachIndexed { index, row ->
            HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .height(38.dp)
                    .fillMaxWidth()
                    .background(if (index % 2 == 1) Color(0xFFFAFAFA) else Color.White)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatingDialogCell(row.productCd, 80.dp)
                PlatingDialogCell(row.productName, 120.dp, alignStart = true)
                PlatingDialogCell(formatOutsourcingCurrency(row.unitPrice), 70.dp)
                PlatingDialogCell(row.deliveryLocation.orEmpty(), 140.dp, alignStart = true)
                PlatingDialogCell(row.category.orEmpty(), 130.dp)
                PlatingDialogCell(row.content.orEmpty(), 100.dp, alignStart = true)
                Box(Modifier.width(120.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                    PlatingDialogQuantityInput(
                        value = row.quantityText,
                        onValueChange = { onQuantityChange(row.productCd, it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun PlatingBatchOrderTable(
    rows: List<PlatingBatchOrderRowUi>,
    onQuantityChange: (String, String) -> Unit,
    onDeliveryDateChange: (String, String) -> Unit,
) {
    val scroll = rememberScrollState()
    val minWidth = 820.dp
    Column(Modifier.horizontalScroll(scroll).widthIn(min = minWidth)) {
        PlatingDialogTableHeader(
            listOf(
                "注文日" to 110.dp,
                "製品CD" to 90.dp,
                "製品名" to 130.dp,
                "単価" to 80.dp,
                "内容" to 100.dp,
                "納期" to 130.dp,
                "数量" to 120.dp,
            ),
        )
        rows.forEachIndexed { index, row ->
            HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .background(if (index % 2 == 1) Color(0xFFFAFAFA) else Color.White)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatingDialogCell(row.orderDate, 110.dp)
                PlatingDialogCell(row.productCd, 90.dp)
                PlatingDialogCell(row.productName, 130.dp, alignStart = true)
                PlatingDialogCell(formatOutsourcingCurrency(row.unitPrice), 80.dp)
                PlatingDialogCell(row.content.orEmpty(), 100.dp, alignStart = true)
                PlatingDialogEditableDateCell(row.deliveryDate, 130.dp) { onDeliveryDateChange(row.orderDate, it) }
                Box(Modifier.width(120.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                    PlatingDialogQuantityInput(
                        value = row.quantityText,
                        onValueChange = { onQuantityChange(row.orderDate, it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatingDialogTableHeader(columns: List<Pair<String, Dp>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA))
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { (label, width) ->
            Box(Modifier.width(width).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF606266))
            }
        }
    }
}

@Composable
private fun PlatingDialogCell(
    text: String,
    width: Dp,
    alignStart: Boolean = false,
) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(text, fontSize = 11.sp, color = Color(0xFF303133), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlatingDialogEditableDateCell(value: String, width: Dp, onChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, FieldBorder, RoundedCornerShape(4.dp))
            .background(Color.White)
            .clickable { showPicker = true }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(value, fontSize = 10.sp, color = Color(0xFF303133), maxLines = 1)
    }
    if (showPicker) {
        OrderDailyDatePickerDialog(
            value = value.ifBlank { outsourcingTodayJapan() },
            accent = Color(0xFF4ECDC4),
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
        )
    }
}
