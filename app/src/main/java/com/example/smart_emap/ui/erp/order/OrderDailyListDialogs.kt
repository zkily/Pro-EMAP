package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDailyDatePickerDialog(
    value: String,
    accent: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val japanZone = remember { ZoneId.of("Asia/Tokyo") }
    val initialMillis = remember(value) {
        parseDailyListDateMillis(value) ?: LocalDate.now(japanZone)
            .atStartOfDay(japanZone).toInstant().toEpochMilli()
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(formatDailyListDateMillis(it, japanZone)) }
                onDismiss()
            }) { Text("確定", color = accent, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル", color = OrderMonthlyColors.TextMuted) }
        },
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = accent,
                todayDateBorderColor = accent,
                selectedYearContainerColor = accent,
            ),
        )
    }
}

@Composable
fun OrderDailyDialogs(state: OrderDailyUiState, viewModel: OrderDailyViewModel) {
    when (state.activeDialog) {
        OrderDailyPageDialog.Form -> OrderDailyFormDialog(
            form = state.form,
            saving = state.formSaving,
            destinationOptions = state.destinationOptions,
            productOptions = state.filteredProductOptions,
            onDismiss = viewModel::dismissDialog,
            onDateChange = viewModel::setFormDate,
            onDeliveryDateChange = viewModel::setFormDeliveryDate,
            onDestinationChange = viewModel::setFormDestinationCd,
            onProductChange = viewModel::setFormProductCd,
            onProductTypeChange = viewModel::setFormProductType,
            onUnitPerBoxChange = viewModel::setFormUnitPerBox,
            onConfirmedBoxesChange = viewModel::setFormConfirmedBoxes,
            onSubmit = viewModel::submitForm,
        )
        OrderDailyPageDialog.DeleteConfirm -> state.deleteTarget?.let { target ->
            OrderDailyDeleteDialog(
                row = target,
                onDismiss = viewModel::dismissDialog,
                onConfirm = viewModel::confirmDelete,
            )
        }
        OrderDailyPageDialog.None -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDailyFormDialog(
    form: OrderDailyFormUi,
    saving: Boolean,
    destinationOptions: List<DestinationOptionDto>,
    productOptions: List<MasterProductItemDto>,
    onDismiss: () -> Unit,
    onDateChange: (String) -> Unit,
    onDeliveryDateChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onProductTypeChange: (String) -> Unit,
    onUnitPerBoxChange: (String) -> Unit,
    onConfirmedBoxesChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val accent = Color(0xFF6366F1)
    val isEdit = form.editId != null
    val productEnabled = form.destinationCd.isNotBlank()
    val productPlaceholder = when {
        form.destinationCd.isBlank() -> "先に納入先を選択"
        productOptions.isEmpty() -> "該当製品がありません"
        else -> "製品を選択"
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.94f)
                .shadow(14.dp, RoundedCornerShape(14.dp), spotColor = Color(0x406366F1))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                        Text(
                            if (isEdit) "日別受注編集" else "新規受注追加(試作品・補給品等)",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss)
                            .padding(6.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CompactFormSection("日付情報", Icons.Default.CalendarMonth, accent) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FormDatePickerField(
                                label = "出荷日",
                                value = form.date,
                                accent = accent,
                                required = true,
                                modifier = Modifier.weight(1f),
                                onChange = onDateChange,
                            )
                            FormDatePickerField(
                                label = "納入日",
                                value = form.deliveryDate,
                                accent = accent,
                                required = true,
                                modifier = Modifier.weight(1f),
                                onChange = onDeliveryDateChange,
                            )
                        }
                    }

                    CompactFormSection("基本情報", Icons.Default.Inventory2, accent) {
                        FormDropdownField(
                            label = "納入先",
                            value = destinationOptions.find { it.cd == form.destinationCd }?.let { "${it.cd} | ${it.name}" } ?: "",
                            placeholder = "納入先を選択",
                            required = true,
                            options = destinationOptions.map { it.cd to "${it.cd} | ${it.name}" },
                            onSelect = onDestinationChange,
                        )
                        FormDropdownField(
                            label = "製品",
                            value = productOptions.find { it.productCd == form.productCd }?.let { "${it.productCd} | ${it.productName}" } ?: "",
                            placeholder = productPlaceholder,
                            required = true,
                            enabled = productEnabled && productOptions.isNotEmpty(),
                            options = productOptions.map { it.productCd.orEmpty() to "${it.productCd.orEmpty()} | ${it.productName.orEmpty()}" },
                            onSelect = onProductChange,
                        )
                        FormDropdownField(
                            label = "製品タイプ",
                            value = form.productType,
                            placeholder = "製品タイプを選択",
                            options = OrderDailyViewModel.PRODUCT_TYPES.map { it to it },
                            onSelect = onProductTypeChange,
                        )
                    }

                    CompactFormSection("数量情報", Icons.Default.Check, Color(0xFF059669)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FormNumberField(
                                label = "入数",
                                value = form.unitPerBox,
                                variant = FormNumberVariant.UnitPerBox,
                                modifier = Modifier.weight(1f),
                                onChange = onUnitPerBoxChange,
                            )
                            FormNumberField(
                                label = "箱数",
                                value = form.confirmedBoxes,
                                variant = FormNumberVariant.ConfirmedBoxes,
                                modifier = Modifier.weight(1f),
                                onChange = onConfirmedBoxesChange,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("確定本数", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF047857))
                            Text(
                                form.confirmedUnits.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF047857),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("キャンセル", fontSize = 12.sp, color = OrderMonthlyColors.TextMuted) }
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = accent.copy(0.35f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF818CF8), accent)))
                            .clickable(enabled = !saving, onClick = onSubmit)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (saving) "保存中..." else "保存",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDailyDeleteDialog(
    row: OrderDailyItemDto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Text("削除確認", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("日別受注 ID「${row.id}」を削除しますか？", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("キャンセル", modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp), color = OrderMonthlyColors.TextMuted)
                Text("削除", modifier = Modifier.clickable(onClick = onConfirm).padding(8.dp), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompactFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8ECF1), RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFF5F7FF), Color(0xFFEEF1FF))))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF303133))
        }
        HorizontalDivider(color = Color(0xFFE0E6F7), thickness = 1.dp)
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun FormFieldLabel(label: String, required: Boolean = false) {
    Text(
        text = if (required) "$label *" else label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF606266),
        modifier = Modifier.width(72.dp),
    )
}

@Composable
private fun FormDatePickerField(
    label: String,
    value: String,
    accent: Color,
    required: Boolean,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        OrderDailyDatePickerDialog(
            value = value,
            accent = accent,
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
        )
    }
    Column(modifier = modifier) {
        Text(
            text = if (required) "$label *" else label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF606266),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFDCDFE6), RoundedCornerShape(6.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value.ifBlank { "選択" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.isBlank()) Color(0xFFA8ABB2) else Color(0xFF303133),
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.CalendarMonth, contentDescription = "日付選択", tint = accent, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdownField(
    label: String,
    value: String,
    placeholder: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormFieldLabel(label, required)
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = it },
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (enabled) Color.White else Color(0xFFF5F7FA))
                    .border(1.dp, Color(0xFFDCDFE6), RoundedCornerShape(6.dp))
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = when {
                        !enabled -> Color(0xFFC0C4CC)
                        value.isBlank() -> Color(0xFFA8ABB2)
                        else -> Color(0xFF303133)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (cd, text) ->
                    DropdownMenuItem(
                        text = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp) },
                        onClick = { onSelect(cd); expanded = false },
                    )
                }
            }
        }
    }
}

private enum class FormNumberVariant(
    val labelColor: Color,
    val background: Color,
    val border: Color,
    val text: Color,
    val placeholder: String,
) {
    UnitPerBox(
        labelColor = Color(0xFF4F46E5),
        background = Color(0xFFEEF2FF),
        border = Color(0xFF818CF8),
        text = Color(0xFF4338CA),
        placeholder = "入数",
    ),
    ConfirmedBoxes(
        labelColor = Color(0xFF2563EB),
        background = Color(0xFFEFF6FF),
        border = Color(0xFF60A5FA),
        text = Color(0xFF1D4ED8),
        placeholder = "箱数",
    ),
}

@Composable
private fun FormNumberField(
    label: String,
    value: String,
    variant: FormNumberVariant,
    modifier: Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = variant.labelColor)
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.filter { ch -> ch.isDigit() }) },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(variant.background)
                .border(1.dp, variant.border.copy(alpha = 0.65f), RoundedCornerShape(6.dp)),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                color = variant.text,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            variant.placeholder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = variant.text.copy(alpha = 0.38f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

private fun parseDailyListDateMillis(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneId.of("Asia/Tokyo")).toInstant().toEpochMilli()
    }.getOrNull()
}

private fun formatDailyListDateMillis(millis: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

/** Web `el-date-picker type="daterange"` 相当：開始・終了をそれぞれカレンダーで選択 */
@Composable
fun OrderDailyCalendarRangeField(
    startDate: String,
    endDate: String,
    accent: Color,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldHeight: androidx.compose.ui.unit.Dp = 34.dp,
    fieldMinWidth: androidx.compose.ui.unit.Dp = 130.dp,
    startPlaceholder: String = "開始日",
    endPlaceholder: String = "終了日",
    elevated: Boolean = false,
    separatorColor: Color = Color(0xFF64748B),
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = accent,
            onDismiss = { pickStart = false },
            onConfirm = { onStartChange(it); pickStart = false },
        )
    }
    if (pickEnd) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = accent,
            onDismiss = { pickEnd = false },
            onConfirm = { onEndChange(it); pickEnd = false },
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CalendarRangeDateBox(
            value = startDate,
            placeholder = startPlaceholder,
            accent = accent,
            height = fieldHeight,
            minWidth = fieldMinWidth,
            elevated = elevated,
            onClick = { pickStart = true },
        )
        Text("～", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = separatorColor)
        CalendarRangeDateBox(
            value = endDate,
            placeholder = endPlaceholder,
            accent = accent,
            height = fieldHeight,
            minWidth = fieldMinWidth,
            elevated = elevated,
            onClick = { pickEnd = true },
        )
    }
}

@Composable
private fun CalendarRangeDateBox(
    value: String,
    placeholder: String,
    accent: Color,
    height: androidx.compose.ui.unit.Dp,
    minWidth: androidx.compose.ui.unit.Dp,
    elevated: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .widthIn(min = minWidth)
            .then(if (elevated) Modifier.shadow(5.dp, shape, spotColor = Color(0x40000000)) else Modifier)
            .height(height)
            .clip(shape)
            .background(
                if (elevated) {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                } else {
                    Brush.linearGradient(listOf(Color.White, Color.White))
                },
            )
            .border(1.dp, if (elevated) Color.White.copy(alpha = 0.85f) else Color(0xFFDCDFE6), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value.ifBlank { placeholder },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (value.isNotBlank()) FontFamily.Monospace else FontFamily.Default,
            color = if (value.isBlank()) Color(0xFFA8ABB2) else accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = placeholder,
            tint = accent,
            modifier = Modifier.size(16.dp),
        )
    }
}
