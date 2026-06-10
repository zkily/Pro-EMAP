package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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

private val SetupPreviewInputHeight = 24.dp
private val SetupPreviewTableWidth = 1080.dp
private val SetupPreviewHeaderBg = Color(0xFFF5F7FA)
private val SetupPreviewBorder = Color(0xFFEBEEF5)
private val SetupPreviewInputBorder = Color(0xFFDCDFE6)

private fun setupPreviewPlannedHoursBackground(value: String): Color {
    val n = value.toDoubleOrNull() ?: 0.0
    if (n == 0.0) return Color(0xFFF5F7FA)
    return when {
        n < 8 -> Color(0xFFFDEAEA)
        n < 16 -> Color(0xFFFFF6DB)
        n < 22.5 -> Color(0xFFE8F7EC)
        else -> Color(0xFFCFEECF)
    }
}

private data class SetupPreviewColumn(
    val label: String,
    val width: Int,
)

private val setupPreviewColumns = listOf(
    SetupPreviewColumn("生産残数", 92),
    SetupPreviewColumn("ライン", 70),
    SetupPreviewColumn("予定稼働(H)", 92),
    SetupPreviewColumn("操業度(進捗)", 72),
    SetupPreviewColumn("生産品種", 108),
    SetupPreviewColumn("能率", 90),
    SetupPreviewColumn("当日計画数", 88),
    SetupPreviewColumn("残生産時間", 88),
    SetupPreviewColumn("", 42),
    SetupPreviewColumn("次生産品種", 130),
    SetupPreviewColumn("次品種計画数", 92),
    SetupPreviewColumn("備考", 206),
)

@Composable
fun SetupSchedulePreviewDialog(
    meta: SetupSchedulePreviewMeta?,
    rows: List<SetupScheduleRow>,
    onPrint: (List<SetupScheduleRow>) -> Unit,
    onDismiss: () -> Unit,
) {
    val editableRows = remember(rows) {
        mutableStateListOf<SetupScheduleRow>().also { it.addAll(rows) }
    }
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "段取予定プレビュー（編集可）",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = PlanInstructionTheme.Subtitle)
                    }
                }
                HorizontalDivider(color = SetupPreviewBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (meta != null) {
                        SetupPreviewMetaBar(
                            productionDate = meta.productionDate,
                            totalQuantity = PlanInstructionLogic.formatNumber(meta.totalQuantity),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, SetupPreviewBorder, RoundedCornerShape(8.dp)),
                    ) {
                        Row(modifier = Modifier.horizontalScroll(hScroll)) {
                            SetupPreviewHeaderRow(modifier = Modifier.width(SetupPreviewTableWidth))
                        }
                        HorizontalDivider(color = SetupPreviewBorder, thickness = 1.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .verticalScroll(vScroll)
                                .horizontalScroll(hScroll),
                        ) {
                            editableRows.forEachIndexed { index, row ->
                                SetupPreviewDataRow(
                                    row = row,
                                    stripe = index % 2 == 1,
                                    modifier = Modifier.width(SetupPreviewTableWidth),
                                    onRowChange = { editableRows[index] = it },
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = SetupPreviewBorder)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onPrint(editableRows.toList()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF409EFF),
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("印刷", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .height(36.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SetupPreviewInputBorder),
                        ) {
                            Text("閉じる", fontSize = 13.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupPreviewMetaBar(
    productionDate: String,
    totalQuantity: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                ),
            )
            .border(1.dp, SetupPreviewBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SetupPreviewMetaItem(label = "生産日", value = productionDate)
        SetupPreviewMetaItem(label = "生産計画合計数", value = totalQuantity)
    }
}

@Composable
private fun SetupPreviewMetaItem(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = PlanInstructionTheme.Subtitle, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SetupPreviewHeaderRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(SetupPreviewHeaderBg)
            .padding(vertical = 5.dp),
    ) {
        setupPreviewColumns.forEach { column ->
            Text(
                column.label,
                modifier = Modifier
                    .width(column.width.dp)
                    .padding(horizontal = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
            )
        }
    }
}

@Composable
private fun SetupPreviewDataRow(
    row: SetupScheduleRow,
    stripe: Boolean,
    modifier: Modifier = Modifier,
    onRowChange: (SetupScheduleRow) -> Unit,
) {
    val bg = if (stripe) Color(0xFFFAFAFA) else Color.White
    Row(
        modifier = modifier
            .background(bg)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SetupPreviewCell(modifier = Modifier.width(92.dp)) {
            SetupPreviewInput(
                value = row.totalPlanQuantity?.toString().orEmpty(),
                onValueChange = { onRowChange(row.copy(totalPlanQuantity = it.toIntOrNull())) },
            )
        }
        SetupPreviewCell(modifier = Modifier.width(70.dp)) {
            Text(
                row.line,
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(92.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SetupPreviewInputHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(setupPreviewPlannedHoursBackground(row.plannedWorkingHours)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    formatSetupPreviewPlannedHours(row.plannedWorkingHours),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
        SetupPreviewCell(modifier = Modifier.width(72.dp)) {
            SetupPreviewInput(
                value = row.operationVariance,
                onValueChange = { onRowChange(row.copy(operationVariance = it)) },
                onBlur = {
                    onRowChange(row.copy(operationVariance = normalizeSetupPreviewOperationVariance(row.operationVariance)))
                },
                textAlign = TextAlign.Center,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(108.dp)) {
            SetupPreviewInput(
                value = row.productName,
                onValueChange = { onRowChange(row.copy(productName = it)) },
            )
        }
        SetupPreviewCell(modifier = Modifier.width(90.dp)) {
            SetupPreviewInput(
                value = row.efficiency,
                onValueChange = { onRowChange(row.copy(efficiency = it)) },
                textAlign = TextAlign.Center,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(88.dp)) {
            SetupPreviewInput(
                value = row.planQuantity?.toString().orEmpty(),
                onValueChange = { onRowChange(row.copy(planQuantity = it.toIntOrNull())) },
                bold = true,
                textAlign = TextAlign.Center,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(88.dp)) {
            SetupPreviewInput(
                value = row.setupAfterHours,
                onValueChange = { onRowChange(row.copy(setupAfterHours = it)) },
                textAlign = TextAlign.Center,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(42.dp)) {
            if (setupPreviewHasNextProduct(row.nextProductName)) {
                Text(
                    "→",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        SetupPreviewCell(modifier = Modifier.width(130.dp)) {
            SetupPreviewInput(
                value = row.nextProductName,
                onValueChange = { onRowChange(row.copy(nextProductName = it)) },
            )
        }
        SetupPreviewCell(modifier = Modifier.width(92.dp)) {
            SetupPreviewInput(
                value = row.nextQuantity?.toString().orEmpty(),
                onValueChange = { onRowChange(row.copy(nextQuantity = it.toIntOrNull())) },
                textAlign = TextAlign.Center,
            )
        }
        SetupPreviewCell(modifier = Modifier.width(206.dp)) {
            SetupPreviewInput(
                value = row.remarks,
                onValueChange = { onRowChange(row.copy(remarks = it)) },
                singleLine = false,
                minHeight = SetupPreviewInputHeight,
            )
        }
    }
}

@Composable
private fun SetupPreviewCell(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun SetupPreviewInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = SetupPreviewInputHeight,
    textAlign: TextAlign = TextAlign.Start,
    onBlur: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = Color.Black,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            textAlign = textAlign,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = if (singleLine) SetupPreviewInputHeight else 48.dp)
            .onFocusChanged { state ->
                if (focused && !state.isFocused) {
                    onBlur?.invoke()
                }
                focused = state.isFocused
            }
            .border(1.dp, SetupPreviewInputBorder, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = if (singleLine) 0.dp else 2.dp),
    )
}
