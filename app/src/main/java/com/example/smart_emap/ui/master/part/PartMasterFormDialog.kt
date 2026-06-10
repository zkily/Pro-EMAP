package com.example.smart_emap.ui.master.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private val partFormPrimary = Color(0xFF6366F1)
private val partFormControlHeight = 32.dp
private val partFormFieldGap = 6.dp
private val partFormBlockGap = 8.dp

@Composable
fun PartMasterFormDialog(
    isEdit: Boolean,
    values: Map<String, String>,
    loading: Boolean,
    supplierOptions: List<Pair<String, String>>,
    onValueChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val previewJpy = calcPartPreviewJpy(
        values["unit_price"].orEmpty(),
        values["material_unit_price"].orEmpty(),
        values["exchange_rate"].orEmpty(),
    )
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column {
                PartFormDialogHeader(
                    title = if (isEdit) "部品を編集" else "部品を登録",
                    loading = loading,
                    onDismiss = onDismiss,
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(partFormBlockGap),
                ) {
                    PartFormSection(title = "基本", background = Color(0xFFF8FAFC)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartFormTextField(
                                label = "部品CD",
                                value = values["part_cd"].orEmpty(),
                                onChange = { if (it.length <= 50) onValueChange("part_cd", it) },
                                modifier = Modifier.weight(1.4f),
                                enabled = !isEdit,
                                required = true,
                                maxLength = 50,
                            )
                            PartFormTextField(
                                label = "単位",
                                value = values["uom"].orEmpty(),
                                onChange = { if (it.length <= 20) onValueChange("uom", it) },
                                modifier = Modifier.weight(1f),
                                maxLength = 20,
                            )
                        }
                        PartFormTextField(
                            label = "部品名",
                            value = values["part_name"].orEmpty(),
                            onChange = { if (it.length <= 200) onValueChange("part_name", it) },
                            required = true,
                            maxLength = 200,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartFormTextField(
                                label = "分類",
                                value = values["category"].orEmpty(),
                                onChange = { if (it.length <= 100) onValueChange("category", it) },
                                modifier = Modifier.weight(1f),
                                maxLength = 100,
                            )
                            PartFormSelectField(
                                label = "種別",
                                value = values["kind"].orEmpty(),
                                onChange = { onValueChange("kind", it) },
                                options = PART_KINDS.map { it to it },
                                modifier = Modifier.weight(1f),
                                required = true,
                            )
                        }
                        PartFormSelectField(
                            label = "決済種類",
                            value = values["settlement_type"].orEmpty(),
                            onChange = { onValueChange("settlement_type", it) },
                            options = PART_SETTLEMENT_TYPES.map { it to it },
                            required = true,
                        )
                        PartFormSearchableSelect(
                            label = "仕入先CD",
                            value = values["supplier_cd"].orEmpty(),
                            onChange = { onValueChange("supplier_cd", it) },
                            options = listOf("" to "仕入先を選択") + supplierOptions.map { (cd, name) -> cd to "$cd｜$name" },
                            placeholder = "仕入先を選択",
                        )
                    }
                    PartFormSection(
                        title = "単価・為替",
                        background = Brush.linearGradient(listOf(Color(0xFFFAFBFF), Color(0xFFF1F5F9))),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PartFormSelectField(
                                label = "通貨",
                                value = values["currency"].orEmpty(),
                                onChange = { onValueChange("currency", it) },
                                options = PART_CURRENCIES.map { it to it },
                                modifier = Modifier.weight(1f),
                                required = true,
                            )
                            PartFormNumberField(
                                label = "単価（原通貨）",
                                value = values["unit_price"].orEmpty(),
                                onChange = { onValueChange("unit_price", it) },
                                modifier = Modifier.weight(1f),
                                required = true,
                                decimals = 2,
                            )
                            PartFormNumberField(
                                label = "部品材料単価（原通貨）",
                                value = values["material_unit_price"].orEmpty(),
                                onChange = { onValueChange("material_unit_price", it) },
                                modifier = Modifier.weight(1f),
                                required = true,
                                decimals = 2,
                            )
                        }
                        Row {
                            PartFormNumberField(
                                label = "為替レート",
                                value = values["exchange_rate"].orEmpty(),
                                onChange = { onValueChange("exchange_rate", it) },
                                modifier = Modifier.fillMaxWidth(0.34f),
                                required = true,
                                decimals = 2,
                                step = 0.01,
                                min = 0.01,
                            )
                        }
                        Text(
                            "JPYのときは1。USD等は「1単位の外貨＝何円」（例: 1USD=150 なら 150）",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.75f))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = Color(0xFF64748B),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))))
                                .border(1.dp, partFormPrimary.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("総単価（円）", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF4338CA))
                            Text(
                                "¥${formatPartNum(previewJpy)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF312E81),
                            )
                        }
                    }
                    PartFormSection(title = "", background = Color.White, showLabel = false) {
                        PartFormStatusToggle(
                            active = values["status"] != "0",
                            onChange = { onValueChange("status", if (it) "1" else "0") },
                        )
                        PartFormTextArea(
                            label = "備考",
                            value = values["remarks"].orEmpty(),
                            onChange = { if (it.length <= 500) onValueChange("remarks", it) },
                            maxLength = 500,
                        )
                    }
                }
                PartFormDialogFooter(loading = loading, onDismiss = onDismiss, onConfirm = onConfirm)
            }
        }
    }
}

@Composable
private fun PartFormDialogHeader(title: String, loading: Boolean, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)))),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF22D3EE), partFormPrimary, Color(0xFFA78BFA)))),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 44.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                ) {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = null,
                        tint = Color(0xFFE0E7FF),
                        modifier = Modifier.padding(10.dp).size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color(0xFFF8FAFC), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                    Text(
                        "必須項目の入力と、総単価（円）をご確認ください",
                        color = Color(0xFFE2E8F0).copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
        IconButton(
            onClick = onDismiss,
            enabled = !loading,
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PartFormSection(
    title: String,
    background: Any,
    showLabel: Boolean = true,
    content: @Composable () -> Unit,
) {
    val bgModifier = when (background) {
        is Brush -> Modifier.background(background, RoundedCornerShape(10.dp))
        is Color -> Modifier.background(background, RoundedCornerShape(10.dp))
        else -> Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(bgModifier)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(partFormFieldGap),
    ) {
        if (showLabel && title.isNotBlank()) {
            Text(
                title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        content()
    }
}

@Composable
private fun PartFormFieldLabel(label: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 2.dp)) {
        if (required) Text("* ", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), lineHeight = 13.sp)
    }
}

@Composable
private fun PartFormTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    required: Boolean = false,
    maxLength: Int? = null,
    placeholder: String = "",
) {
    Column(modifier = modifier) {
        PartFormFieldLabel(label, required)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontSize = 12.sp, color = if (enabled) Color(0xFF334155) else Color(0xFF94A3B8)),
            modifier = Modifier
                .fillMaxWidth()
                .height(partFormControlHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) Color.White else Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(partFormControlHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        inner()
                    }
                    if (maxLength != null) {
                        Text(
                            "${value.length} / $maxLength",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartFormSelectField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    required: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "選択" }
    Column(modifier = modifier) {
        PartFormFieldLabel(label, required)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(partFormControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (v, text) ->
                    DropdownMenuItem(
                        text = { Text(text, fontSize = 12.sp) },
                        onClick = { onChange(v); expanded = false },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartFormSearchableSelect(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    placeholder: String = "選択",
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val display = options.find { it.first == value }?.second ?: placeholder
    val filtered = if (search.isBlank()) options else options.filter { (_, t) -> t.contains(search, ignoreCase = true) }
    Column {
        PartFormFieldLabel(label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it; if (!it) search = "" }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(partFormControlHeight)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        display,
                        fontSize = 12.sp,
                        color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF334155),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; search = "" }) {
                DropdownMenuItem(
                    text = {
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 12.sp),
                            decorationBox = { inner ->
                                if (search.isEmpty()) Text("検索...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                inner()
                            },
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                filtered.forEach { (v, text) ->
                    DropdownMenuItem(
                        text = { Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp) },
                        onClick = { onChange(v); expanded = false; search = "" },
                    )
                }
            }
        }
    }
}

@Composable
private fun PartFormNumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    required: Boolean = false,
    decimals: Int = 2,
    step: Double = 0.01,
    min: Double = 0.0,
) {
    Column(modifier = modifier) {
        PartFormFieldLabel(label, required)
        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(partFormControlHeight)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { raw -> if (raw.isEmpty() || raw.toDoubleOrNull() != null) onChange(raw) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155), textAlign = TextAlign.End),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { inner() }
                },
            )
            Column(
                modifier = Modifier
                    .width(22.dp)
                    .fillMaxHeight()
                    .border(1.dp, Color(0xFFE2E8F0)),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            val current = value.toDoubleOrNull() ?: min
                            onChange(formatPartNumField(current + step, decimals))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
                }
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            val current = value.toDoubleOrNull() ?: min
                            onChange(formatPartNumField(max(min, current - step), decimals))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun PartFormStatusToggle(active: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        PartFormFieldLabel("状態")
        val shape = RoundedCornerShape(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(shape)
                .border(1.dp, Color(0xFFE2E8F0), shape),
        ) {
            PartFormStatusSegment(
                text = "有効",
                selected = active,
                modifier = Modifier.weight(1f),
                onClick = { onChange(true) },
            )
            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE2E8F0)))
            PartFormStatusSegment(
                text = "無効",
                selected = !active,
                modifier = Modifier.weight(1f),
                onClick = { onChange(false) },
            )
        }
    }
}

@Composable
private fun PartFormStatusSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBrush = Brush.horizontalGradient(listOf(partFormPrimary, Color(0xFF4F46E5)))
    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (selected) Modifier.background(selectedBrush)
                else Modifier.background(Color.White),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFF64748B),
        )
    }
}

@Composable
private fun PartFormTextArea(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    maxLength: Int,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PartFormFieldLabel(label)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF334155), lineHeight = 16.sp),
        )
        Text(
            "${value.length} / $maxLength",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun PartFormDialogFooter(loading: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
            .border(1.dp, Color(0xFFE2E8F0))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onDismiss,
            enabled = !loading,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDFE6)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF606266)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text("キャンセル", fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onConfirm,
            enabled = !loading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = partFormPrimary),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("保存", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatPartNum(value: Double, decimals: Int = 2): String =
    if (decimals <= 0) max(0, value.roundToInt()).toString()
    else String.format(Locale.JAPAN, "%.${decimals}f", value)

private fun formatPartNumField(value: Double, decimals: Int): String {
    val rounded = if (decimals <= 0) value.roundToInt().toDouble() else {
        val factor = Math.pow(10.0, decimals.toDouble())
        (value * factor).roundToInt() / factor
    }
    return if (decimals <= 0) rounded.roundToInt().toString()
    else String.format(Locale.JAPAN, "%.${decimals}f", rounded)
}
