package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.ProductMachineConfigRowDto

enum class PlanBomBulkField(val label: String) {
    Safety("安全在庫日数"),
    ProcessLt("工程LT"),
    Both("両方"),
}

private val planPrimary = Color(0xFF4F46E5)
private val planPrimaryDark = Color(0xFF4338CA)
private val planPanelBorder = Color(0xFFE2E8F0)
private val planBodyBg = Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE2E8F0)))
private val planHeaderBg = Brush.linearGradient(
    listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF), Color(0xFFF5F3FF)),
)
private val planPanelBg = Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
private val planFooterBg = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
private val planResultBorder = Color(0xFFC7D2FE)

@Composable
fun PlanCreateMainDialog(
    kind: PlanCreateKind,
    form: PlanCreateFormState,
    results: List<PlanCreateResultRow>,
    loading: Boolean,
    clearLoading: Boolean,
    inventoryTrendLoading: Boolean,
    onMonthChange: (String) -> Unit,
    onBaseDateChange: (String) -> Unit,
    onWorkingDaysChange: (Int) -> Unit,
    onCoefficientChange: (Double) -> Unit,
    onClearFromDateChange: (String) -> Unit,
    onOpenMachineConfig: () -> Unit,
    onOpenBom: () -> Unit,
    onExecuteCreate: () -> Unit,
    onRequestClear: () -> Unit,
    onRequestInventoryTrend: () -> Unit,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(14.dp)
        val machineBtnLabel = if (kind == PlanCreateKind.Molding) "成型機器設定" else "溶接機器設定"
        val clearHint = when (kind) {
            PlanCreateKind.Molding -> "指定日以降の生産サマリーで molding_plan / molding_actual_plan を 0 にクリアします。"
            PlanCreateKind.Welding -> "指定日以降の生産サマリーで welding_plan / welding_actual_plan を 0 にクリアします。"
        }
        Column(
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .fillMaxWidth(0.98f)
                .heightIn(max = 720.dp)
                .shadow(16.dp, shape, spotColor = Color(0x330F172A))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color(0x1F94A3B8), shape),
        ) {
            PlanCreateDialogHeader(
                title = kind.title,
                subtitle = "条件入力・計画クリア・計算結果",
                onDismiss = onDismiss,
            )
            HorizontalDivider(color = planPanelBorder.copy(alpha = 0.95f))
            Column(
                modifier = Modifier
                    .background(planBodyBg)
                    .heightIn(max = 580.dp)
                    .verticalScrollCompat()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(10.dp), spotColor = Color(0x0D0F172A))
                        .clip(RoundedCornerShape(10.dp))
                        .background(planPanelBg)
                        .border(1.dp, planPanelBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlanCreateSectionTitle("条件")
                    val condScroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(condScroll),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        PlanCreateFormItem("生産計画月", Modifier.width(112.dp)) {
                            PlanCreateMonthField(
                                value = form.month,
                                onChange = onMonthChange,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PlanCreateFormItem("基準日", Modifier.width(132.dp)) {
                            PlanCreateDateField(
                                value = form.baseDate,
                                onChange = onBaseDateChange,
                                placeholder = "翌月1日",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PlanCreateFormItem("稼働日", Modifier.width(96.dp)) {
                            PlanCreateIntStepper(
                                value = form.workingDays,
                                min = 1,
                                max = 31,
                                onChange = onWorkingDaysChange,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PlanCreateFormItem("加工減耗係数", Modifier.width(112.dp)) {
                            PlanCreateCoefficientField(
                                value = form.coefficient,
                                onChange = onCoefficientChange,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = planPanelBorder,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlanCreateSecondaryButton(machineBtnLabel, Icons.Default.Monitor, onOpenMachineConfig)
                        PlanCreateSecondaryButton("製品工程BOM", Icons.Default.Settings, onOpenBom)
                        PlanCreatePrimaryButton(
                            label = kind.title,
                            icon = Icons.Default.Description,
                            loading = loading,
                            onClick = onExecuteCreate,
                        )
                    }
                    PlanCreateDashedDivider(modifier = Modifier.padding(top = 8.dp))
                    PlanCreateSectionTitle("計画クリア", sub = true)
                    Text(clearHint, fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        PlanCreateFormItem("計画クリア開始日", Modifier.width(168.dp)) {
                            PlanCreateDateField(
                                value = form.clearFromDate,
                                onChange = onClearFromDateChange,
                                placeholder = "開始日を選択",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PlanCreateWarnButton(
                            label = "計画クリア実行",
                            icon = Icons.Default.Delete,
                            loading = clearLoading,
                            enabled = form.clearFromDate.isNotBlank(),
                            onClick = onRequestClear,
                        )
                        PlanCreateSyncButton(
                            label = "在庫・推移更新",
                            icon = Icons.Default.Refresh,
                            loading = inventoryTrendLoading,
                            enabled = !clearLoading,
                            onClick = onRequestInventoryTrend,
                        )
                    }
                    Text(
                        "在庫・推移の開始日は「初期」ログ（数量>0）の最新取引日（無ければ当月月初・JST）。メニュー「在庫・推移更新」と同じ。計画クリア開始日とは連動しません。",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (results.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Color(0x0F4F46E5))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, planResultBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "計算結果",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ProductionPlanningColors.TextPrimary,
                                )
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEEF2FF)) {
                                    Text(
                                        "${results.size} 件",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = planPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            PlanCreateSecondaryButton("印刷", Icons.Default.Print, onPrint)
                        }
                        Spacer(Modifier.height(8.dp))
                        PlanCreateResultTable(kind, results)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(planFooterBg),
            ) {
                HorizontalDivider(color = planPanelBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    PlanCreateCloseButton("閉じる", onDismiss)
                }
            }
        }
    }
}

@Composable
private fun PlanCreateResultTable(kind: PlanCreateKind, rows: List<PlanCreateResultRow>) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .border(1.dp, ProductionPlanningColors.CardBorder, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProductionPlanningColors.TableHeaderBg)
                .horizontalScroll(scroll)
                .padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            listOf("対応日", kind.machineLabel, "製品名", "製品CD", "実計推移", "必要数", "ロット", "ロット数", "計画数", "能率", "工時", "日当り")
                .forEachIndexed { i, h ->
                    Text(
                        h,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(planColWidth(i)),
                    )
                }
        }
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            itemsIndexed(rows, key = { idx, r -> "${r.productCd}|${r.lookupDate}|$idx" }) { _, r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scroll)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlanCreateCell(r.lookupDate, 0)
                    PlanCreateCell(r.machine.ifBlank { "—" }, 1)
                    PlanCreateCell(r.productName, 2, overflow = true)
                    PlanCreateCell(r.productCd, 3)
                    PlanCreateCell(
                        r.trendRaw.toString(),
                        4,
                        color = if (r.trendRaw < 0) ProductionPlanningColors.Negative else ProductionPlanningColors.TextPrimary,
                    )
                    PlanCreateCell(ProductionPlanCreateLogic.formatInt(r.requiredQty), 5)
                    PlanCreateCell(ProductionPlanCreateLogic.formatInt(r.lotSize), 6)
                    PlanCreateCell(r.lotCount.toString(), 7)
                    PlanCreateCell(ProductionPlanCreateLogic.formatInt(r.batchQty), 8, bold = true)
                    PlanCreateCell(ProductionPlanCreateLogic.formatEfficiency(r.efficiencyRate), 9)
                    PlanCreateCell(ProductionPlanCreateLogic.formatHours(r.processHours), 10)
                    PlanCreateCell(ProductionPlanCreateLogic.formatInt(r.dailyQty), 11)
                }
                HorizontalDivider(color = ProductionPlanningColors.CardBorder.copy(alpha = 0.5f))
            }
        }
    }
}

private fun planColWidth(index: Int): androidx.compose.ui.unit.Dp = when (index) {
    0 -> 96.dp
    1 -> 100.dp
    2 -> 140.dp
    3 -> 72.dp
    else -> 72.dp
}

@Composable
private fun PlanCreateCell(
    text: String,
    colIndex: Int,
    color: Color = ProductionPlanningColors.TextPrimary,
    bold: Boolean = false,
    overflow: Boolean = false,
) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = if (colIndex >= 4) TextAlign.End else TextAlign.Start,
        maxLines = if (overflow) 1 else Int.MAX_VALUE,
        overflow = if (overflow) TextOverflow.Ellipsis else TextOverflow.Clip,
        modifier = Modifier.width(planColWidth(colIndex)).padding(horizontal = 2.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreateMachineConfigDialog(
    kind: PlanCreateKind,
    rows: List<ProductMachineConfigRowDto>,
    machineOptions: List<Pair<String, String>>,
    loading: Boolean,
    savingId: Int?,
    onMachineChange: (ProductMachineConfigRowDto, String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth(0.95f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            PlanCreateDialogHeader(
                title = if (kind == PlanCreateKind.Molding) "成型機器設定" else "溶接機器設定",
                subtitle = "product_machine_config · 変更すると自動保存",
                onDismiss = onDismiss,
            )
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("データがありません", color = ProductionPlanningColors.TextSecondary)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ProductionPlanningColors.TableHeaderBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text("製品CD", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
                            Text("製品名", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(kind.machineLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(180.dp))
                        }
                    }
                    itemsIndexed(rows, key = { _, r -> r.id ?: r.productCd.orEmpty() }) { _, row ->
                        var expanded by remember(row.id) { mutableStateOf(false) }
                        val current = when (kind) {
                            PlanCreateKind.Molding -> row.moldingMachine.orEmpty()
                            PlanCreateKind.Welding -> row.weldingMachine.orEmpty()
                        }
                        val options = buildMachineSelectOptions(machineOptions, rows, kind)
                        val display = options.find { it.second == current }?.first ?: current.ifBlank { "（未設定）" }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(row.productCd.orEmpty(), fontSize = 12.sp, modifier = Modifier.width(90.dp))
                            Text(row.productName.orEmpty(), fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.width(180.dp),
                            ) {
                                OutlinedTextField(
                                    value = if (savingId == row.id) "保存中..." else display,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = savingId != row.id,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true,
                                )
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("（クリア）", fontSize = 12.sp) },
                                        onClick = { onMachineChange(row, ""); expanded = false },
                                    )
                                    options.forEach { (label, value) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 12.sp) },
                                            onClick = { onMachineChange(row, value); expanded = false },
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = ProductionPlanningColors.CardBorder.copy(alpha = 0.4f))
                    }
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
                PlanCreateOutlinedButton("閉じる", onDismiss)
            }
        }
    }
}

private fun buildMachineSelectOptions(
    base: List<Pair<String, String>>,
    rows: List<ProductMachineConfigRowDto>,
    kind: PlanCreateKind,
): List<Pair<String, String>> {
    val seen = base.map { it.second }.toMutableSet()
    val extra = mutableListOf<Pair<String, String>>()
    rows.forEach { r ->
        val v = when (kind) {
            PlanCreateKind.Molding -> r.moldingMachine.orEmpty().trim()
            PlanCreateKind.Welding -> r.weldingMachine.orEmpty().trim()
        }
        if (v.isNotBlank() && v !in seen) {
            seen += v
            extra += "$v（マスタ外）" to v
        }
    }
    return base + extra
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreateBomDialog(
    kind: PlanCreateKind,
    rows: List<PlanBomUiRow>,
    loading: Boolean,
    bulkField: PlanBomBulkField,
    bulkLoading: Boolean,
    selected: Set<Int>,
    onBulkFieldChange: (PlanBomBulkField) -> Unit,
    onToggleSelect: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onSafetyChange: (Int, Int) -> Unit,
    onProcessLtChange: (Int, Int) -> Unit,
    onBulkDelta: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val processLabel = if (kind == PlanCreateKind.Molding) "成型LT" else "溶接LT"
    var bulkExpanded by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(0.95f)
                .shadow(8.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, ProductionPlanningColors.CardBorder, shape),
        ) {
            PlanCreateDialogHeader(
                title = "製品工程BOM",
                subtitle = "安全在庫日数・$processLabel",
                onDismiss = onDismiss,
            )
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("一括対象", fontSize = 12.sp)
                    ExposedDropdownMenuBox(expanded = bulkExpanded, onExpandedChange = { bulkExpanded = it }) {
                        OutlinedTextField(
                            value = bulkField.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bulkExpanded) },
                            modifier = Modifier.menuAnchor().width(140.dp),
                        )
                        ExposedDropdownMenu(expanded = bulkExpanded, onDismissRequest = { bulkExpanded = false }) {
                            PlanBomBulkField.entries.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.label, fontSize = 12.sp) },
                                    onClick = { onBulkFieldChange(f); bulkExpanded = false },
                                )
                            }
                        }
                    }
                    Button(onClick = { onBulkDelta(1) }, enabled = !bulkLoading, modifier = Modifier.height(32.dp)) {
                        Text("選択行 +1", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onBulkDelta(-1) },
                        enabled = !bulkLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7), contentColor = Color(0xFFB45309)),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text("選択行 -1", fontSize = 11.sp)
                    }
                }
                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ProductionPlanningColors.TableHeaderBg)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = selected.size == rows.size && rows.isNotEmpty(), onCheckedChange = { onSelectAll() })
                                Text("CD", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(64.dp))
                                Text("製品名", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Text("安全在庫", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                                Text(processLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp), textAlign = TextAlign.Center)
                            }
                        }
                        itemsIndexed(rows, key = { _, r -> r.productCd }) { _, row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = row.productCd in selected, onCheckedChange = { onToggleSelect(row.productCd) })
                                Text(row.productCd.toString(), fontSize = 12.sp, modifier = Modifier.width(64.dp))
                                Text(row.productName, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                PlanCreateIntStepper(
                                    value = row.safetyStockDays,
                                    min = 0,
                                    max = 9999,
                                    onChange = { onSafetyChange(row.productCd, it) },
                                    modifier = Modifier.width(80.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                PlanCreateIntStepper(
                                    value = row.processLt,
                                    min = 0,
                                    max = 9999,
                                    onChange = { onProcessLtChange(row.productCd, it) },
                                    modifier = Modifier.width(72.dp),
                                )
                            }
                            HorizontalDivider(color = ProductionPlanningColors.CardBorder.copy(alpha = 0.4f))
                        }
                    }
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
                PlanCreateOutlinedButton("閉じる", onDismiss)
            }
        }
    }
}

@Composable
fun PlanCreateClearConfirmDialog(
    kind: PlanCreateKind,
    startDate: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val planLabel = when (kind) {
        PlanCreateKind.Molding -> "成型計画（molding_plan）および実績計画（molding_actual_plan）"
        PlanCreateKind.Welding -> "溶接計画（welding_plan）および実績計画（welding_actual_plan）"
    }
    DataMgmtSimpleConfirmDialog(
        title = "計画クリアの確認",
        message = "この日付以降の $planLabel をすべて 0 にクリアします。\n開始日：$startDate",
        confirmLabel = "実行",
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun PlanCreateDialogHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(planHeaderBg)
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0x8C4F46E5))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(planPrimary, planPrimaryDark))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.3.sp,
                    color = ProductionPlanningColors.TextPrimary,
                )
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = ProductionPlanningColors.TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PlanCreateSectionTitle(text: String, sub: Boolean = false) {
    Text(
        text.uppercase(),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(top = if (sub) 2.dp else 0.dp, bottom = 2.dp),
    )
}

@Composable
private fun PlanCreateDashedDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        )
    }
}

@Composable
private fun PlanCreateFormItem(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
        content()
    }
}

@Composable
private fun PlanCreateMonthField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        ProductionBeautifulDatePickerDialog(
            value = if (value.length >= 7) "${value}-01" else value,
            title = "生産計画月",
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it.take(7)); showPicker = false },
        )
    }
    PlanCreateCompactInputChip(
        text = value.take(7).ifBlank { "月選択" },
        placeholder = value.isBlank(),
        modifier = modifier,
        onClick = { showPicker = true },
    )
}

@Composable
private fun PlanCreateDateField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        ProductionBeautifulDatePickerDialog(
            value = value,
            title = placeholder,
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
        )
    }
    PlanCreateCompactInputChip(
        text = value.take(10).ifBlank { placeholder },
        placeholder = value.isBlank(),
        modifier = modifier,
        onClick = { showPicker = true },
    )
}

@Composable
private fun PlanCreateCompactInputChip(
    text: String,
    placeholder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(14.dp),
        )
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (placeholder) Color(0xFF94A3B8) else ProductionPlanningColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PlanCreateSecondaryButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    PlanCreateStyledButton(
        label = label,
        icon = icon,
        onClick = onClick,
        enabled = enabled,
        background = Color.White,
        contentColor = Color(0xFF334155),
        borderColor = Color(0xFFCBD5E1),
    )
}

@Composable
private fun PlanCreatePrimaryButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    onClick: () -> Unit,
) {
    PlanCreateStyledButton(
        label = label,
        icon = icon,
        onClick = onClick,
        enabled = !loading,
        loading = loading,
        background = Brush.verticalGradient(listOf(planPrimary, planPrimaryDark)),
        contentColor = Color.White,
        borderColor = planPrimaryDark,
    )
}

@Composable
private fun PlanCreateWarnButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    PlanCreateStyledButton(
        label = label,
        icon = icon,
        onClick = onClick,
        enabled = enabled && !loading,
        loading = loading,
        background = Color(0xFFFFFBEB),
        contentColor = Color(0xFF9A3412),
        borderColor = Color(0xFFFCD34D),
    )
}

@Composable
private fun PlanCreateSyncButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    PlanCreateStyledButton(
        label = label,
        icon = icon,
        onClick = onClick,
        enabled = enabled && !loading,
        loading = loading,
        background = Color(0xFFF0FDFA),
        contentColor = Color(0xFF0F766E),
        borderColor = Color(0xFF5EEAD4),
    )
}

@Composable
private fun PlanCreateCloseButton(label: String, onClick: () -> Unit) {
    PlanCreateStyledButton(
        label = label,
        icon = null,
        onClick = onClick,
        background = Color.White,
        contentColor = Color(0xFF475569),
        borderColor = Color(0xFFCBD5E1),
        minWidth = 88.dp,
    )
}

@Composable
private fun PlanCreateStyledButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    background: Color,
    contentColor: Color,
    borderColor: Color,
    minWidth: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = Modifier
            .then(if (minWidth > 0.dp) Modifier.widthIn(min = minWidth) else Modifier)
            .height(32.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        color = background,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = contentColor)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Composable
private fun PlanCreateStyledButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    background: Brush,
    contentColor: Color,
    borderColor: Color,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = contentColor)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Composable
private fun PlanCreateOutlinedButton(label: String, onClick: () -> Unit) {
    PlanCreateCloseButton(label, onClick)
}

@Composable
private fun PlanCreateIntStepper(
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlanCreateNumberStepper(
        text = value.toString(),
        onDecrease = { onChange((value - 1).coerceIn(min, max)) },
        onIncrease = { onChange((value + 1).coerceIn(min, max)) },
        modifier = modifier,
    )
}

@Composable
private fun PlanCreateCoefficientField(
    value: Double,
    onChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = 0.001
    PlanCreateNumberStepper(
        text = String.format("%.3f", value),
        onDecrease = { onChange((value - step).coerceIn(1.0, 2.0)) },
        onIncrease = { onChange((value + step).coerceIn(1.0, 2.0)) },
        modifier = modifier,
    )
}

@Composable
private fun PlanCreateNumberStepper(
    text: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
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
                    .clickable(onClick = onIncrease),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(onClick = onDecrease),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun Modifier.verticalScrollCompat(): Modifier = verticalScroll(rememberScrollState())
