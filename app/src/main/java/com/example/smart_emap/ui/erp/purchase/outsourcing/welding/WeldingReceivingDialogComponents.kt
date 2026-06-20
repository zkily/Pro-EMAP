package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingReceivingStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculateWeldingReceivingStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingCurrency
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.orderAmount
import com.example.smart_emap.ui.erp.purchase.outsourcing.weldingOrderReceivingQty

private val ReceivingDialogHeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
)
private val FieldBorder = Color(0xFFDCDEE6)
private val LabelColor = Color(0xFF606266)
private val LabelWidth = 72.dp
private val FieldHeight = 28.dp
private val RowGap = 5.dp
private val SectionGap = 7.dp

private enum class FieldLabelStyle(val color: Color) {
    Primary(Color(0xFF409EFF)),
    Success(Color(0xFF67C23A)),
    Warning(Color(0xFFE6A23C)),
    Danger(Color(0xFFF56C6C)),
    Info(Color(0xFF909399)),
}

@Composable
fun WeldingReceivingDialogShell(
    title: String,
    loading: Boolean,
    confirmEnabled: Boolean,
    confirmText: String = "登録",
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
                .fillMaxWidth(0.97f)
                .widthIn(max = 836.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ReceivingDialogHeaderGradient)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Text(
                        title,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    IconButton(onClick = onDismiss, enabled = !loading, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .background(Color(0xFFF5F7FA))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(SectionGap),
                    content = content,
                )
                HorizontalDivider(color = Color(0xFFEBEEF5))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReceivingDialogFooterBtn("キャンセル", Icons.Default.Close, filled = false, enabled = !loading, onClick = onDismiss)
                    ReceivingDialogFooterBtn(
                        text = confirmText,
                        icon = Icons.Default.Check,
                        filled = true,
                        enabled = !loading && confirmEnabled,
                        loading = loading,
                        onClick = onConfirm,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceivingDialogFooterBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(32.dp),
        shape = shape,
        color = if (filled) Color.Transparent else Color.White,
        border = BorderStroke(1.dp, if (filled) Color.Transparent else FieldBorder),
        shadowElevation = if (filled && enabled) 1.dp else 0.dp,
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
                    } else Modifier,
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading && filled) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (filled) Color.White else LabelColor)
            }
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (filled) Color.White else LabelColor)
        }
    }
}

@Composable
private fun ReceivingFormSection(
    title: String,
    icon: String,
    accent: Color,
    sectionGradient: Brush,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(sectionGradient),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(icon, fontSize = 13.sp)
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF303133))
            }
            HorizontalDivider(color = Color(0xFFEBEEF5), modifier = Modifier.padding(bottom = 5.dp))
            Column(verticalArrangement = Arrangement.spacedBy(RowGap), content = content)
        }
    }
}

@Composable
fun WeldingReceivingFormFields(
    form: WeldingReceivingFormUi,
    isEdit: Boolean,
    pendingOrders: List<WeldingPendingOrderOption>,
    onOrderSelect: (String) -> Unit,
    onFormChange: (WeldingReceivingFormUi) -> Unit,
    onReceivingQtyChange: (String) -> Unit,
    onGoodQtyChange: (String) -> Unit,
) {
    val primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFECF5FF), Color.White))
    val warningGradient = Brush.horizontalGradient(listOf(Color(0xFFFDF6EC), Color.White))
    val successGradient = Brush.horizontalGradient(listOf(Color(0xFFF0F9EB), Color.White))

    ReceivingFormSection("基本情報", "📋", Color(0xFF409EFF), primaryGradient) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (isEdit) {
                ReceivingCompactReadonly("注文番号", form.orderNo, FieldLabelStyle.Primary, Modifier.weight(1f))
            } else {
                ReceivingCompactDropdown(
                    label = "注文番号",
                    value = form.orderNo,
                    options = pendingOrders.map { it.orderNo to "${it.orderNo} ${it.productName}（残:${it.remainQty}）" },
                    labelStyle = FieldLabelStyle.Primary,
                    placeholder = "注文を選択",
                    modifier = Modifier.weight(1f),
                    onSelect = onOrderSelect,
                )
            }
            ReceivingCompactDate(
                label = "受入予定",
                value = form.receivingDate,
                labelStyle = FieldLabelStyle.Primary,
                modifier = Modifier.weight(1f),
                onChange = { onFormChange(form.copy(receivingDate = it)) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ReceivingCompactReadonly("外注先", form.supplierName.ifBlank { "—" }, FieldLabelStyle.Info, Modifier.weight(1f))
            ReceivingCompactReadonly("製品CD", form.productCd.ifBlank { "—" }, FieldLabelStyle.Info, Modifier.weight(1f))
            ReceivingCompactReadonly("製品名", form.productName.ifBlank { "—" }, FieldLabelStyle.Info, Modifier.weight(1f))
        }
    }

    ReceivingFormSection("数量情報", "📊", Color(0xFFE6A23C), warningGradient) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ReceivingCompactReadonly("注文数", formatOutsourcingNumber(form.orderQty), FieldLabelStyle.Warning, Modifier.weight(1f))
            ReceivingCompactReadonly("既納数", formatOutsourcingNumber(form.deliveredQty), FieldLabelStyle.Info, Modifier.weight(1f))
            ReceivingCompactReadonly(
                label = "残数",
                value = formatOutsourcingNumber(form.remainQty),
                labelStyle = FieldLabelStyle.Danger,
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFFF56C6C),
                valueBold = true,
                highlightDanger = true,
            )
            ReceivingCompactReadonly("溶接種類", form.weldingType.ifBlank { "—" }, FieldLabelStyle.Info, Modifier.weight(1f))
        }
    }

    ReceivingFormSection("検収情報", "✅", Color(0xFF67C23A), successGradient) {
        val receivingQty = form.receivingQtyText.toIntOrNull() ?: 0
        val orderMax = form.orderQty.coerceAtLeast(1)
        val goodMax = minOf(orderMax, receivingQty.coerceAtLeast(0)).coerceAtLeast(0)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ReceivingCompactNumberField(
                label = "受入数",
                valueText = form.receivingQtyText,
                min = 1,
                max = orderMax,
                labelStyle = FieldLabelStyle.Success,
                modifier = Modifier.weight(1f),
                onValueChange = onReceivingQtyChange,
                onStep = { stepped ->
                    onReceivingQtyChange(stepped.toString())
                    onGoodQtyChange(stepped.toString())
                },
            )
            ReceivingCompactNumberField(
                label = "良品数",
                valueText = form.goodQtyText,
                min = 0,
                max = goodMax.coerceAtLeast(0),
                labelStyle = FieldLabelStyle.Success,
                modifier = Modifier.weight(1f),
                onValueChange = onGoodQtyChange,
            )
            ReceivingCompactReadonly(
                label = "不良数",
                value = form.defectQtyText,
                labelStyle = FieldLabelStyle.Danger,
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFFF56C6C),
                valueBold = true,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            val defectEnabled = (form.defectQtyText.toIntOrNull() ?: 0) > 0
            ReceivingCompactDropdown(
                label = "不良理由",
                value = form.defectReason,
                options = listOf("" to "選択") + WELDING_DEFECT_REASONS,
                labelStyle = FieldLabelStyle.Danger,
                enabled = defectEnabled,
                modifier = Modifier.weight(1f),
                onSelect = { onFormChange(form.copy(defectReason = it)) },
            )
            ReceivingCompactDropdown(
                label = "検収者",
                value = form.inspector,
                options = listOf("" to "検収者を選択") + WELDING_RECEIVING_INSPECTORS.map { it to it },
                labelStyle = FieldLabelStyle.Primary,
                modifier = Modifier.weight(1f),
                onSelect = { onFormChange(form.copy(inspector = it)) },
            )
        }
        ReceivingCompactTextArea(
            label = "備考",
            value = form.remarks,
            labelStyle = FieldLabelStyle.Info,
            placeholder = "備考を入力",
            onValueChange = { onFormChange(form.copy(remarks = it)) },
        )
    }
}

@Composable
private fun ReceivingFormFieldRow(
    label: String,
    labelStyle: FieldLabelStyle,
    modifier: Modifier = Modifier,
    field: @Composable () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.width(LabelWidth),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelStyle.color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.weight(1f)) { field() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivingCompactDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    labelStyle: FieldLabelStyle,
    enabled: Boolean = true,
    placeholder: String = "選択",
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: if (value.isBlank()) placeholder else value
    ReceivingFormFieldRow(label, labelStyle, modifier) {
        ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }) {
            Surface(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(FieldHeight)
                    .clickable(enabled = enabled) { expanded = true },
                shape = RoundedCornerShape(4.dp),
                color = if (enabled) Color.White else Color(0xFFF5F7FA),
                border = BorderStroke(1.dp, FieldBorder),
                shadowElevation = 1.dp,
            ) {
                Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                    Text(display, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (enabled) Color(0xFF303133) else Color(0xFF909399))
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, text) ->
                    DropdownMenuItem(text = { Text(text, fontSize = 12.sp) }, onClick = { onSelect(id); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun ReceivingCompactDate(
    label: String,
    value: String,
    labelStyle: FieldLabelStyle,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    ReceivingFormFieldRow(label, labelStyle, modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(FieldHeight).clickable { showPicker = true },
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            border = BorderStroke(1.dp, FieldBorder),
            shadowElevation = 1.dp,
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                Text(value, fontSize = 13.sp, color = Color(0xFF303133))
            }
        }
    }
    if (showPicker) {
        OrderDailyDatePickerDialog(value = value, accent = Color(0xFFFF6B6B), onDismiss = { showPicker = false }, onConfirm = { onChange(it); showPicker = false })
    }
}

@Composable
private fun ReceivingCompactReadonly(
    label: String,
    value: String,
    labelStyle: FieldLabelStyle,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF303133),
    valueBold: Boolean = false,
    highlightDanger: Boolean = false,
) {
    ReceivingFormFieldRow(label, labelStyle, modifier) {
        val bg = if (highlightDanger) Brush.horizontalGradient(listOf(Color(0xFFFEF0F0), Color.White)) else Brush.linearGradient(listOf(Color(0xFFF5F7FA), Color(0xFFF5F7FA)))
        Surface(
            modifier = Modifier.fillMaxWidth().height(FieldHeight).background(bg, RoundedCornerShape(4.dp)),
            shape = RoundedCornerShape(4.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFFE4E7ED)),
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                Text(value, fontSize = 13.sp, color = valueColor, fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ReceivingCompactNumberField(
    label: String,
    valueText: String,
    min: Int,
    max: Int,
    labelStyle: FieldLabelStyle,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onStep: ((Int) -> Unit)? = null,
) {
    val current = valueText.toIntOrNull() ?: min
    ReceivingFormFieldRow(label, labelStyle, modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(FieldHeight),
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            border = BorderStroke(1.dp, FieldBorder),
            shadowElevation = 1.dp,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val next = (current - 1).coerceIn(min, max)
                        if (onStep != null) onStep(next) else onValueChange(next.toString())
                    },
                    enabled = current > min,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFF909399), modifier = Modifier.size(14.dp))
                }
                BasicTextField(
                    value = valueText,
                    onValueChange = { raw ->
                        if (raw.isEmpty() || raw.all { it.isDigit() }) onValueChange(raw)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(FieldHeight),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF303133),
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color(0xFF67C23A)),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (valueText.isEmpty()) {
                                Text("0", fontSize = 13.sp, color = Color(0xFFC0C4CC), textAlign = TextAlign.Center)
                            }
                            inner()
                        }
                    },
                )
                IconButton(
                    onClick = {
                        val next = (current + 1).coerceIn(min, max)
                        if (onStep != null) onStep(next) else onValueChange(next.toString())
                    },
                    enabled = current < max,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF909399), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ReceivingCompactTextArea(
    label: String,
    value: String,
    labelStyle: FieldLabelStyle,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    ReceivingFormFieldRow(label, labelStyle) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            border = BorderStroke(1.dp, FieldBorder),
            shadowElevation = 1.dp,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xFF303133)),
                cursorBrush = SolidColor(Color(0xFF67C23A)),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = Color(0xFFC0C4CC))
                    inner()
                },
            )
        }
    }
}

@Composable
fun WeldingReceivingDetailDialog(row: OutsourcingWeldingReceivingDto, onDismiss: () -> Unit, onEdit: () -> Unit) {
    val status = calculateWeldingReceivingStatus(row)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("受入詳細", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF303133))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                ReceivingDetailGrid(
                    listOf(
                        "受入番号" to row.receivingNo.orEmpty(),
                        "受入日" to row.receivingDate.orEmpty(),
                        "注文番号" to row.orderNo.orEmpty(),
                        "外注先" to (row.supplierName ?: row.supplierCd.orEmpty()),
                        "品番" to row.productCd.orEmpty(),
                        "品名" to row.productName.orEmpty(),
                        "溶接種類" to row.weldingType.orEmpty().ifBlank { "—" },
                        "注文数" to formatOutsourcingNumber(row.orderQty),
                        "受入数" to formatOutsourcingNumber(row.receivingQty),
                        "良品数" to formatOutsourcingNumber(row.goodQty),
                        "不良数" to formatOutsourcingNumber(row.defectQty),
                        "検収者" to row.inspector.orEmpty().ifBlank { "—" },
                        "備考" to row.remarks.orEmpty().ifBlank { "—" },
                    ),
                )
                Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("検収状態", fontSize = 12.sp, color = LabelColor)
                    OutsourcingReceivingStatusChip(status)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.End) {
                    ReceivingDialogFooterBtn("閉じる", Icons.Default.Close, filled = false, enabled = true, onClick = onDismiss)
                    ReceivingDialogFooterBtn("編集", Icons.Default.Check, filled = true, enabled = true, onClick = onEdit, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun WeldingReceivingOrderDetailDialog(order: OutsourcingWeldingOrderDto, onDismiss: () -> Unit) {
    val received = weldingOrderReceivingQty(order)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("注文詳細", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF303133))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                ReceivingDetailGrid(
                    listOf(
                        "注文番号" to order.orderNo.orEmpty(),
                        "注文日" to order.orderDate.orEmpty(),
                        "外注先" to (order.supplierName ?: order.supplierCd.orEmpty()),
                        "品番" to order.productCd.orEmpty(),
                        "品名" to order.productName.orEmpty(),
                        "溶接種類" to order.weldingType.orEmpty().ifBlank { "—" },
                        "数量" to formatOutsourcingNumber(order.quantity),
                        "単価" to formatOutsourcingCurrency(order.unitPrice),
                        "金額" to formatOutsourcingCurrency(orderAmount(order.quantity, order.unitPrice)),
                        "納期" to order.deliveryDate.orEmpty().ifBlank { "—" },
                        "入庫数" to formatOutsourcingNumber(received),
                    ),
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.End) {
                    ReceivingDialogFooterBtn("閉じる", Icons.Default.Close, filled = false, enabled = true, onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun ReceivingDetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontSize = 11.sp, color = Color(0xFF909399))
                        Text(value, fontSize = 12.sp, color = Color(0xFF303133), modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (row.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}
