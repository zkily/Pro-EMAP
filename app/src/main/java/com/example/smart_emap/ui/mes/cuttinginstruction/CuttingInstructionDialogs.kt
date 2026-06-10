package com.example.smart_emap.ui.mes.cuttinginstruction



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.runtime.rememberCoroutineScope
import com.example.smart_emap.data.model.CuttingInstructionNoteDto
import com.example.smart_emap.data.model.ProductBatchDetailDto
import kotlinx.coroutines.launch

import com.example.smart_emap.data.model.InstructionChamferingPlanRowDto

import com.example.smart_emap.data.model.InstructionChamferingRowDto

import com.example.smart_emap.data.model.InstructionCuttingRowDto

import com.example.smart_emap.data.model.InstructionPlanRowDto

import com.example.smart_emap.data.model.KanbanIssuanceRowDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog



@Composable

fun CuttingInstructionDialogHost(

    dialog: CuttingInstructionDialog,

    uiState: CuttingInstructionUiState,

    viewModel: CuttingInstructionViewModel,

) {

    when (dialog) {

        CuttingInstructionDialog.None -> Unit

        CuttingInstructionDialog.MoldingPreInventory -> MoldingPreInventoryDialog(

            date = uiState.moldingPreInventoryDate,

            groups = uiState.moldingPreInventoryGroups,

            loading = uiState.moldingPreInventoryLoading,

            onDateChange = viewModel::setMoldingPreInventoryDate,

            onReload = viewModel::loadMoldingPreInventory,

            onPrint = viewModel::printMoldingPreInventory,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.CuttingDoneList -> DoneListDialog(

            title = "切断済リスト",

            loading = uiState.doneListLoading,

            rows = viewModel.cuttingDonePaged,

            totalCount = viewModel.cuttingDoneFiltered.size,

            page = uiState.doneListPage,

            totalPages = viewModel.cuttingDoneTotalPages,

            onPageChange = viewModel::setDoneListPage,

            headers = listOf("生産日", "管理CD", "製品名", "切断機", "生産数", "完了"),

            rowCells = { row ->

                val r = row as InstructionCuttingRowDto

                listOf(

                    formatInstructionDate(r.productionDay),

                    r.managementCode ?: "-",

                    r.productName ?: "-",

                    r.cuttingMachine ?: "-",

                    r.actualProductionQuantity?.toString() ?: "-",

                    if (r.productionCompletedCheck == 1) "○" else "-",

                )

            },

            periodStart = uiState.cuttingDonePeriodStart,

            periodEnd = uiState.cuttingDonePeriodEnd,

            onPeriodStartChange = viewModel::setCuttingDonePeriodStart,

            onPeriodEndChange = viewModel::setCuttingDonePeriodEnd,

            filterProduct = uiState.cuttingDoneProductFilter,

            productOptions = viewModel.cuttingDoneProductOptions,

            onFilterProduct = viewModel::setCuttingDoneProductFilter,

            managementCodeFilter = uiState.cuttingDoneManagementCodeFilter,

            onManagementCodeFilter = viewModel::setCuttingDoneManagementCodeFilter,

            onlyCompleted = uiState.cuttingDoneOnlyCompleted,

            onToggleOnlyCompleted = viewModel::setCuttingDoneOnlyCompleted,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.ChamferingDoneList -> DoneListDialog(

            title = "面取済リスト",

            loading = uiState.doneListLoading,

            rows = viewModel.chamferingDonePaged,

            totalCount = viewModel.chamferingDoneFiltered.size,

            page = uiState.doneListPage,

            totalPages = viewModel.chamferingDoneTotalPages,

            onPageChange = viewModel::setDoneListPage,

            headers = listOf("生産日", "CD", "製品名", "面取機", "生産数", "完了"),

            rowCells = { row ->

                val r = row as InstructionChamferingRowDto

                listOf(

                    formatInstructionDate(r.productionDay),

                    r.cd ?: "-",

                    r.productName ?: "-",

                    r.chamferingMachine ?: "-",

                    r.actualProductionQuantity?.toString() ?: "-",

                    if (r.productionCompletedCheck == 1) "○" else "-",

                )

            },

            periodStart = uiState.chamferingDonePeriodStart,

            periodEnd = uiState.chamferingDonePeriodEnd,

            onPeriodStartChange = viewModel::setChamferingDonePeriodStart,

            onPeriodEndChange = viewModel::setChamferingDonePeriodEnd,

            filterProduct = uiState.chamferingDoneProductFilter,

            productOptions = viewModel.chamferingDoneProductOptions,

            onFilterProduct = viewModel::setChamferingDoneProductFilter,

            onlyCompleted = uiState.chamferingDoneOnlyCompleted,

            onToggleOnlyCompleted = viewModel::setChamferingDoneOnlyCompleted,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.DataManagement -> DataManagementDialog(

            loading = uiState.dataManagementLoading,

            rows = uiState.dataManagementRows,

            monthFilter = uiState.dataManagementMonth,

            monthOptions = viewModel.dataManagementMonthOptions,

            onMonthChange = viewModel::setDataManagementMonth,

            onReload = viewModel::loadDataManagement,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.Notes -> NotesDialog(

            notes = uiState.notes,

            loading = uiState.notesLoading,

            saving = uiState.notesSaving,

            onAdd = { content, onSuccess -> viewModel.createNote(content, onSuccess) },

            onToggleDone = viewModel::toggleNoteDone,

            onDelete = viewModel::deleteNote,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.NewPlan -> PlanFormDialog(

            title = if (dialog.isTrial) "試作追加" else "新規追加",

            subtitle = if (dialog.isTrial) "試作品" else "量産品",

            initial = dialog.initial,

            machineOptions = uiState.machineOptions,

            materialOptions = uiState.materialMasterOptions,

            productOptions = dialog.productOptions,

            onLoadProductDetail = { viewModel.loadPlanProductBatchDetail(it) },

            lockBasicFields = false,

            onSave = { viewModel.saveNewPlan(dialog.isTrial, it) },

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditPlan -> PlanFormDialog(

            title = "ロット内容編集",

            subtitle = dialog.initial.productName,

            initial = dialog.initial,

            machineOptions = uiState.machineOptions,

            materialOptions = uiState.materialMasterOptions,

            lockBasicFields = true,

            onSave = { viewModel.saveEditPlan(dialog.planId, it) },

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditUsageCount -> UsageCountEditDialog(

            current = dialog.current,

            onSave = { viewModel.saveUsageCount(dialog.rowId, dialog.isCutting, it) },

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.SpecifiedDateMaterial -> SpecifiedDateMaterialDialog(

            date = uiState.specifiedMaterialDate,

            rows = uiState.specifiedMaterialRows,

            loading = uiState.specifiedMaterialLoading,

            onDateChange = viewModel::setSpecifiedMaterialDate,

            onReload = viewModel::loadSpecifiedMaterialRows,

            onPrint = viewModel::printSpecifiedDateMaterial,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.MoveToCutting -> MoveToCuttingDialog(

            plan = dialog.plan,

            machines = viewModel.cuttingMachineChipOptions,

            defaultDate = uiState.cuttingDateToday,

            defaultMachine = uiState.cuttingMachineFilter,

            onConfirm = viewModel::confirmMoveToCuttingAdvanced,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditCutting -> CuttingEditDialog(

            row = dialog.row,

            machines = viewModel.cuttingMachineChipOptions,

            onSave = viewModel::saveCuttingEdit,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.SplitCutting -> {
            val row = dialog.row
            SplitToNextDayDialog(
                machineLabel = "切断機",
                machineName = row.cuttingMachine.orEmpty(),
                productionDay = row.productionDay,
                totalQty = row.actualProductionQuantity ?: 0,
                productSubtitle = row.productName ?: row.productCd.orEmpty(),
                submitting = uiState.splitToNextDaySubmitting,
                onConfirm = { todayQty, nextDay -> viewModel.splitCutting(row, todayQty, nextDay) },
                onDismiss = viewModel::dismissDialog,
            )
        }

        is CuttingInstructionDialog.EditChamfering -> ChamferingEditDialog(

            row = dialog.row,

            machines = uiState.chamferingMachineOptions,

            onSave = viewModel::saveChamferingEdit,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.NewChamferingInstruction -> NewChamferingInstructionDialog(

            initial = dialog.initial,

            lineOptions = uiState.machineOptions,

            chamferingMachines = uiState.chamferingMachineOptions,

            productOptions = uiState.chamferingProductOptions,

            materialOptions = uiState.chamferingMaterialOptions,

            submitting = uiState.chamferingNewSubmitting,

            onSave = viewModel::saveNewChamferingInstruction,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.SplitChamfering -> {
            val row = dialog.row
            SplitToNextDayDialog(
                machineLabel = "面取機",
                machineName = row.chamferingMachine.orEmpty(),
                productionDay = row.productionDay,
                totalQty = row.actualProductionQuantity ?: 0,
                productSubtitle = row.productName ?: row.productCd.orEmpty(),
                submitting = uiState.splitToNextDaySubmitting,
                onConfirm = { todayQty, nextDay -> viewModel.splitChamfering(row, todayQty, nextDay) },
                onDismiss = viewModel::dismissDialog,
            )
        }

        is CuttingInstructionDialog.NewChamferingPlan -> ChamferingPlanFormDialog(

            title = "面取ロット 新規追加",

            subtitle = "chamfering_plans",

            initial = dialog.initial,

            machines = uiState.chamferingMachineOptions,

            productOptions = uiState.chamferingProductOptions,

            onLoadProductDetail = viewModel::loadPlanProductBatchDetail,

            submitting = uiState.chamferingPlanFormSubmitting,

            onSave = viewModel::saveNewChamferingPlan,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditChamferingPlan -> ChamferingPlanFormDialog(

            title = "面取ロット 編集",

            subtitle = dialog.initial.productName.ifBlank { "chamfering_plans" },

            initial = dialog.initial,

            machines = uiState.chamferingMachineOptions,

            productOptions = uiState.chamferingProductOptions,

            onLoadProductDetail = viewModel::loadPlanProductBatchDetail,

            submitting = uiState.chamferingPlanFormSubmitting,

            onSave = { viewModel.saveEditChamferingPlan(dialog.planId, it) },

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.MoveChamferingPlan -> MoveChamferingPlanDialog(

            plan = dialog.plan,

            machines = uiState.chamferingMachineOptions,

            defaultDate = uiState.chamferingDateToday,

            onConfirm = viewModel::confirmMoveChamferingPlan,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditKanban -> KanbanEditDialog(

            row = dialog.row,

            onSave = viewModel::saveKanbanEdit,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.ConfirmUsageReflection -> ConfirmDialog(

            title = "使用数反映",

            message = "材料使用数を在庫へ反映します。よろしいですか？",

            onConfirm = viewModel::confirmUsageReflection,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.ConfirmActualResult -> ActualResultDialog(

            inserted = dialog.inserted,

            totalQty = dialog.totalQty,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.ConfirmDelete -> ConfirmDialog(

            title = "削除の確認",

            message = dialog.message,

            onConfirm = dialog.onConfirm,

            onDismiss = viewModel::dismissDialog,

        )

    }

}



@Composable

private fun ConfirmDialog(

    title: String,

    message: String,

    onConfirm: () -> Unit,

    onDismiss: () -> Unit,

) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = { Text(title) },

        text = { Text(message) },

        confirmButton = { Button(onClick = onConfirm) { Text("確定") } },

        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },

    )

}



@Composable
private fun ActualResultDialog(inserted: Int, totalQty: Int, onDismiss: () -> Unit) {
    val qtyText = java.text.NumberFormat.getNumberInstance(java.util.Locale.JAPAN).format(totalQty)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.widthIn(max = 420.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        "実績確定 結果",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActualResultCard(
                        label = "登録件数",
                        value = "$inserted 件",
                        highlight = false,
                    )
                    ActualResultCard(
                        label = "数量合計（生産数）",
                        value = "$qtyText 本",
                        highlight = true,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("閉じる") }
                }
            }
        }
    }
}

@Composable
private fun ActualResultCard(label: String, value: String, highlight: Boolean) {
    val bg = if (highlight) {
        Brush.linearGradient(listOf(Color(0xFFF0FDF4), Color(0xFFECFDF5)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF8FAFC)))
    }
    val borderColor = if (highlight) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)
    val valueColor = if (highlight) Color(0xFF047857) else Color(0xFF0F172A)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
        Text(
            value,
            fontSize = if (highlight) 22.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}



@Composable

private fun MoldingPreInventoryDialog(

    date: String,

    groups: List<MoldingPreInventoryGroup>,

    loading: Boolean,

    onDateChange: (String) -> Unit,

    onReload: () -> Unit,

    onPrint: () -> Unit,

    onDismiss: () -> Unit,

) {

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {

            Text("成型前在庫・時間換算", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(date, onDateChange, label = { Text("在庫参照日") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(onClick = onReload) { Text("読込") }

                Button(onClick = onPrint, enabled = groups.isNotEmpty()) { Text("印刷") }

                TextButton(onClick = onDismiss) { Text("閉じる") }

            }

            if (loading) CircularProgressIndicator()

            else if (groups.isEmpty()) Text("データなし", color = CuttingInstructionTheme.Subtitle)

            else groups.forEach { g ->

                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {

                    Text(g.moldingMachine, fontSize = 12.sp)

                    Text("在庫(H): ${g.totalProductionHours} / 参照後(H): ${g.totalPostRefCuttingHours}", fontSize = 11.sp)

                }

            }

        }

    }

}



@Composable

private fun DoneListDialog(

    title: String,

    loading: Boolean,

    rows: List<Any>,

    totalCount: Int,

    page: Int,

    totalPages: Int,

    onPageChange: (Int) -> Unit,

    headers: List<String>,

    rowCells: (Any) -> List<String>,

    periodStart: String = "",

    periodEnd: String = "",

    onPeriodStartChange: (String) -> Unit = {},

    onPeriodEndChange: (String) -> Unit = {},

    filterProduct: String,

    productOptions: List<String>,

    onFilterProduct: (String) -> Unit,

    managementCodeFilter: String = "",

    onManagementCodeFilter: ((String) -> Unit)? = null,

    onlyCompleted: Boolean,

    onToggleOnlyCompleted: (Boolean) -> Unit,

    onDismiss: () -> Unit,

) {

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp).heightIn(max = 560.dp)) {

            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {

                OutlinedTextField(

                    value = periodStart,

                    onValueChange = onPeriodStartChange,

                    label = { Text("開始日", fontSize = 11.sp) },

                    modifier = Modifier.weight(1f),

                    singleLine = true,

                )

                OutlinedTextField(

                    value = periodEnd,

                    onValueChange = onPeriodEndChange,

                    label = { Text("終了日", fontSize = 11.sp) },

                    modifier = Modifier.weight(1f),

                    singleLine = true,

                )

            }

            InstructionFilterDropdown("製品", filterProduct, productOptions.map { it to it }, onFilterProduct, Modifier.fillMaxWidth())

            if (onManagementCodeFilter != null) {

                OutlinedTextField(

                    value = managementCodeFilter,

                    onValueChange = onManagementCodeFilter,

                    label = { Text("管理CD") },

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                )

            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                Checkbox(checked = onlyCompleted, onCheckedChange = onToggleOnlyCompleted)

                Text("完了のみ", fontSize = 12.sp)

                Spacer(Modifier.weight(1f))

                Text("${totalCount}件", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)

            }

            if (loading) CircularProgressIndicator()

            else LazyColumn(modifier = Modifier.weight(1f, fill = false)) {

                item {

                    Row {

                        headers.forEach { h -> Text(h, Modifier.width(72.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold) }

                    }

                    HorizontalDivider()

                }

                items(rows) { row ->

                    Row(Modifier.padding(vertical = 2.dp)) {

                        rowCells(row).forEach { c -> Text(c, Modifier.width(72.dp), fontSize = 10.sp, maxLines = 1) }

                    }

                }

            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                TextButton(onClick = { onPageChange(page - 1) }, enabled = page > 1) { Text("前へ") }

                Text("$page / $totalPages", fontSize = 12.sp)

                TextButton(onClick = { onPageChange(page + 1) }, enabled = page < totalPages) { Text("次へ") }

                TextButton(onClick = onDismiss) { Text("閉じる") }

            }

        }

    }

}



@Composable

private fun DataManagementDialog(

    loading: Boolean,

    rows: List<InstructionPlanRowDto>,

    monthFilter: String,

    monthOptions: List<String>,

    onMonthChange: (String) -> Unit,

    onReload: () -> Unit,

    onDismiss: () -> Unit,

) {

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp).heightIn(max = 480.dp)) {

            Text("データ管理", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            InstructionFilterDropdown("生産月", monthFilter, monthOptions.map { it to it }, onMonthChange, Modifier.fillMaxWidth())

            Button(onClick = onReload) { Text("読込") }

            if (loading) CircularProgressIndicator()

            else LazyColumn {

                items(rows) { row ->

                    Text(

                        "${formatInstructionDate(row.startDate)} ${row.productName ?: "-"} / ${row.materialName ?: "-"}",

                        fontSize = 11.sp,

                        modifier = Modifier.padding(vertical = 2.dp),

                    )

                }

            }

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("閉じる") }

        }

    }

}



@Composable

private fun NotesDialog(

    notes: List<CuttingInstructionNoteDto>,

    loading: Boolean,

    saving: Boolean,

    onAdd: (String, () -> Unit) -> Unit,

    onToggleDone: (Int, Boolean) -> Unit,

    onDelete: (Int) -> Unit,

    onDismiss: () -> Unit,

) {

    var newContent by remember { mutableStateOf("") }

    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    val listScroll = rememberScrollState()

    if (pendingDeleteId != null) {

        AlertDialog(

            onDismissRequest = { pendingDeleteId = null },

            title = { Text("削除確認", fontWeight = FontWeight.Bold, fontSize = 15.sp) },

            text = { Text("このメモを削除しますか？", fontSize = 13.sp) },

            confirmButton = {

                TextButton(

                    onClick = {

                        pendingDeleteId?.let(onDelete)

                        pendingDeleteId = null

                    },

                ) {

                    Text("削除", color = Color(0xFFDC2626))

                }

            },

            dismissButton = {

                TextButton(onClick = { pendingDeleteId = null }) {

                    Text("取消")

                }

            },

        )

    }

    Dialog(onDismissRequest = onDismiss) {

        Surface(

            modifier = Modifier

                .fillMaxWidth(0.92f)

                .widthIn(max = 520.dp)

                .shadow(24.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x734C1D95), spotColor = Color(0x734C1D95)),

            shape = RoundedCornerShape(16.dp),

            color = Color.White,

            border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.NotesDialogBorder),

        ) {

            Column {

                Box(

                    modifier = Modifier

                        .fillMaxWidth()

                        .background(

                            Brush.linearGradient(

                                listOf(

                                    CuttingInstructionTheme.NotesDialogHeaderStart,

                                    CuttingInstructionTheme.NotesDialogHeaderMid,

                                    CuttingInstructionTheme.NotesDialogHeaderEnd,

                                ),

                            ),

                        )

                        .border(

                            width = 0.dp,

                            color = Color.Transparent,

                        )

                        .drawBehind {

                            drawLine(

                                color = CuttingInstructionTheme.NotesDialogBorder,

                                start = Offset(0f, size.height),

                                end = Offset(size.width, size.height),

                                strokeWidth = 1.dp.toPx(),

                            )

                        }

                        .padding(horizontal = 16.dp, vertical = 12.dp),

                ) {

                    Text(

                        "メモ（TODO）",

                        fontWeight = FontWeight.ExtraBold,

                        fontSize = 14.sp,

                        color = CuttingInstructionTheme.NotesDialogTitle,

                        letterSpacing = 0.2.sp,

                    )

                }

                Column(

                    modifier = Modifier

                        .background(

                            Brush.verticalGradient(

                                listOf(

                                    CuttingInstructionTheme.NotesDialogBodyTop,

                                    CuttingInstructionTheme.NotesDialogBodyBottom,

                                ),

                            ),

                        )

                        .padding(12.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp),

                ) {

                    Column(

                        modifier = Modifier

                            .fillMaxWidth()

                            .clip(RoundedCornerShape(12.dp))

                            .background(

                                Brush.linearGradient(

                                    listOf(

                                        CuttingInstructionTheme.NotesDialogAddBgStart,

                                        CuttingInstructionTheme.NotesDialogAddBgEnd,

                                    ),

                                ),

                            )

                            .border(1.dp, CuttingInstructionTheme.NotesDialogAddBorder, RoundedCornerShape(12.dp))

                            .padding(10.dp),

                        verticalArrangement = Arrangement.spacedBy(8.dp),

                    ) {

                        BasicTextField(

                            value = newContent,

                            onValueChange = { if (it.length <= 200) newContent = it },

                            textStyle = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, color = Color(0xFF1F2937)),

                            modifier = Modifier

                                .fillMaxWidth()

                                .heightIn(min = 56.dp, max = 72.dp)

                                .clip(RoundedCornerShape(10.dp))

                                .border(1.dp, CuttingInstructionTheme.NotesDialogInputBorder, RoundedCornerShape(10.dp))

                                .background(Color.White)

                                .padding(horizontal = 10.dp, vertical = 8.dp),

                            decorationBox = { inner ->

                                if (newContent.isEmpty()) {

                                    Text(

                                        "簡単なメモを入力（短文）",

                                        fontSize = 12.sp,

                                        color = CuttingInstructionTheme.Subtitle,

                                    )

                                }

                                inner()

                            },

                        )

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween,

                            verticalAlignment = Alignment.CenterVertically,

                        ) {

                            Text(

                                "${newContent.length}/200",

                                fontSize = 11.sp,

                                fontWeight = FontWeight.SemiBold,

                                color = CuttingInstructionTheme.NotesDialogCharCount,

                            )

                            Button(

                                onClick = {

                                    onAdd(newContent) { newContent = "" }

                                },

                                enabled = !saving,

                                shape = RoundedCornerShape(9.dp),

                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),

                                colors = ButtonDefaults.buttonColors(

                                    containerColor = CuttingInstructionTheme.NotesDialogAddBtnStart,

                                    disabledContainerColor = CuttingInstructionTheme.NotesDialogAddBtnStart.copy(alpha = 0.5f),

                                ),

                            ) {

                                if (saving) {

                                    CircularProgressIndicator(

                                        modifier = Modifier.size(14.dp),

                                        strokeWidth = 2.dp,

                                        color = Color.White,

                                    )

                                } else {

                                    Text("追加", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                                }

                            }

                        }

                    }

                    Box(

                        modifier = Modifier

                            .fillMaxWidth()

                            .heightIn(max = 320.dp),

                    ) {

                        when {

                            loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {

                                CircularProgressIndicator(

                                    modifier = Modifier.size(24.dp),

                                    strokeWidth = 2.dp,

                                    color = CuttingInstructionTheme.NotesDialogAddBtnStart,

                                )

                            }

                            notes.isEmpty() -> Box(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .clip(RoundedCornerShape(10.dp))

                                    .border(

                                        1.dp,

                                        CuttingInstructionTheme.NotesDialogInputBorder,

                                        RoundedCornerShape(10.dp),

                                    )

                                    .background(Color.White)

                                    .padding(vertical = 22.dp),

                                contentAlignment = Alignment.Center,

                            ) {

                                Text("未登録", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)

                            }

                            else -> Column(

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .verticalScroll(listScroll),

                                verticalArrangement = Arrangement.spacedBy(8.dp),

                            ) {

                                notes.forEach { note ->

                                    val id = note.id ?: return@forEach

                                    val done = note.isDone == 1

                                    Row(

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .clip(RoundedCornerShape(10.dp))

                                            .border(1.dp, CuttingInstructionTheme.NotesDialogRowBorder, RoundedCornerShape(10.dp))

                                            .background(Color.White)

                                            .padding(horizontal = 10.dp, vertical = 10.dp),

                                        verticalAlignment = Alignment.Top,

                                    ) {

                                        Checkbox(

                                            checked = done,

                                            onCheckedChange = { onToggleDone(id, it) },

                                            enabled = !saving,

                                            modifier = Modifier.size(20.dp),

                                            colors = CheckboxDefaults.colors(

                                                checkedColor = CuttingInstructionTheme.NotesDialogCheckbox,

                                                uncheckedColor = Color(0xFFC4B5FD),

                                                checkmarkColor = Color.White,

                                            ),

                                        )

                                        Text(

                                            note.content.orEmpty(),

                                            fontSize = 12.sp,

                                            lineHeight = 17.sp,

                                            color = if (done) Color(0xFF9CA3AF) else Color(0xFF1F2937),

                                            textDecoration = if (done) TextDecoration.LineThrough else null,

                                            modifier = Modifier

                                                .weight(1f)

                                                .padding(top = 2.dp, end = 4.dp),

                                        )

                                        IconButton(

                                            onClick = { pendingDeleteId = id },

                                            enabled = !saving,

                                            modifier = Modifier.size(28.dp),

                                        ) {

                                            Icon(

                                                Icons.Default.Delete,

                                                contentDescription = "削除",

                                                tint = CuttingInstructionTheme.NotesDialogDelete,

                                                modifier = Modifier.size(16.dp),

                                            )

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

                HorizontalDivider(color = Color(0xFFECEBFF))

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .background(CuttingInstructionTheme.NotesDialogFooterBg)

                        .padding(horizontal = 12.dp, vertical = 10.dp),

                    horizontalArrangement = Arrangement.End,

                ) {

                    OutlinedButton(

                        onClick = onDismiss,

                        shape = RoundedCornerShape(8.dp),

                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),

                        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),

                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),

                    ) {

                        Text("閉じる", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)

                    }

                }

            }

        }

    }

}



data class PlanFormState(
    val productionMonth: String = "",
    val productionLine: String = "",
    val priorityOrder: String = "0",
    val productCd: String = "",
    val productName: String = "",
    val plannedQuantity: String = "0",
    val actualProductionQuantity: String = "0",
    val startDate: String = "",
    val endDate: String = "",
    val productionLotSize: String = "",
    val lotNumber: String = "",
    val isCuttingInstructed: Boolean = false,
    val hasChamferingProcess: Boolean = false,
    val isChamferingInstructed: Boolean = false,
    val hasSwProcess: Boolean = false,
    val isSwInstructed: Boolean = false,
    val managementCode: String = "",
    val takeCount: String = "",
    val cuttingLength: String = "",
    val chamferingLength: String = "",
    val developedLength: String = "",
    val scrapLength: String = "",
    val materialName: String = "",
    val materialManufacturer: String = "",
    val standardSpecification: String = "",
    val useMaterialStockSub: Boolean = false,
    val usageCount: String = "1.0",
)

private fun formatPlanFormNumber(value: Double?): String {
    if (value == null) return ""
    val rounded = kotlin.math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
}

private fun PlanFormState.clearPlanProductAutoFields(): PlanFormState = copy(
    productCd = "",
    productName = "",
    materialName = "",
    materialManufacturer = "",
    standardSpecification = "",
    takeCount = "",
    cuttingLength = "",
    chamferingLength = "",
    developedLength = "",
    scrapLength = "",
    hasChamferingProcess = false,
    hasSwProcess = false,
)

private fun PlanFormState.applyPlanProductDetail(
    productCd: String,
    fallbackName: String,
    detail: ProductBatchDetailDto?,
): PlanFormState {
    if (detail == null) {
        return copy(productCd = productCd, productName = fallbackName)
    }
    return copy(
        productCd = productCd,
        productName = detail.productName?.takeIf { it.isNotBlank() } ?: fallbackName,
        materialName = detail.materialName.orEmpty(),
        materialManufacturer = detail.materialManufacturer.orEmpty(),
        standardSpecification = detail.standardSpecification.orEmpty(),
        takeCount = detail.takeCount?.toString().orEmpty(),
        cuttingLength = formatPlanFormNumber(detail.cuttingLength ?: detail.cutLength),
        chamferingLength = formatPlanFormNumber(detail.chamferingLength ?: detail.chamferLength),
        developedLength = formatPlanFormNumber(detail.developedLength),
        scrapLength = formatPlanFormNumber(detail.scrapLength),
        hasChamferingProcess = detail.hasChamferingProcess == true,
        hasSwProcess = detail.hasSwProcess == true,
    )
}

@Composable
private fun PlanFormDialog(
    title: String,
    subtitle: String = "",
    initial: PlanFormState,
    machineOptions: List<Pair<String, String>>,
    materialOptions: List<Pair<String, String>>,
    productOptions: List<Pair<String, String>> = emptyList(),
    onLoadProductDetail: suspend (String) -> ProductBatchDetailDto? = { null },
    lockBasicFields: Boolean = false,
    onSave: (PlanFormState) -> Unit,
    onDismiss: () -> Unit,
) {
    var form by remember { mutableStateOf(initial) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val useProductDropdown = !lockBasicFields && productOptions.isNotEmpty()

    suspend fun applyProductSelection(productCd: String) {
        if (productCd.isBlank()) {
            form = form.clearPlanProductAutoFields()
            return
        }
        val fallbackName = productOptions.firstOrNull { it.first == productCd }
            ?.second
            ?.substringBeforeLast("  [")
            ?.trim()
            .orEmpty()
        val detail = onLoadProductDetail(productCd)
        form = form.applyPlanProductDetail(productCd, fallbackName, detail)
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 900.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(CuttingInstructionTheme.PlanEditHeaderStart, CuttingInstructionTheme.PlanEditHeaderEnd),
                            ),
                        )
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            if (subtitle.isNotBlank()) {
                                Text(
                                    subtitle,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White.copy(alpha = 0.75f))
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PlanEditSection("基本情報", CuttingInstructionTheme.PlanEditSecBlue) {
                        PlanEditGridRow {
                            PlanEditDateField(
                                label = "生産月",
                                value = form.productionMonth,
                                onValueChange = { form = form.copy(productionMonth = it) },
                                editable = !lockBasicFields,
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditDropdownField(
                                label = "ライン",
                                value = form.productionLine,
                                options = machineOptions,
                                onSelect = { form = form.copy(productionLine = it) },
                                emptyLabel = "設備を選択",
                                editable = !lockBasicFields,
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditField(
                                label = "順位",
                                value = form.priorityOrder,
                                onValueChange = { form = form.copy(priorityOrder = it) },
                                modifier = Modifier.weight(1f),
                                readOnly = lockBasicFields,
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Number,
                            )
                        }
                        if (useProductDropdown) {
                            PlanEditGridRow {
                                PlanEditDropdownField(
                                    label = "製品名",
                                    value = form.productCd,
                                    options = productOptions,
                                    onSelect = { cd -> scope.launch { applyProductSelection(cd) } },
                                    emptyLabel = "製品名／CDで選択",
                                    includeEmptyOption = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            PlanEditGridRow {
                                PlanEditField(
                                    label = "製品CD",
                                    value = form.productCd,
                                    onValueChange = {},
                                    modifier = Modifier.weight(1f),
                                    readOnly = true,
                                    textAlign = TextAlign.Center,
                                )
                                PlanEditField(
                                    label = "製品名",
                                    value = form.productName,
                                    onValueChange = {},
                                    modifier = Modifier.weight(2f),
                                    readOnly = true,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        } else {
                            PlanEditGridRow {
                                PlanEditField(
                                    label = "製品CD",
                                    value = form.productCd,
                                    onValueChange = { form = form.copy(productCd = it) },
                                    modifier = Modifier.weight(1f),
                                    readOnly = lockBasicFields,
                                    textAlign = TextAlign.Center,
                                )
                                PlanEditField(
                                    label = "製品名",
                                    value = form.productName,
                                    onValueChange = { form = form.copy(productName = it) },
                                    modifier = Modifier.weight(2f),
                                    readOnly = lockBasicFields,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        PlanEditGridRow {
                            PlanEditField(
                                label = "計画数",
                                value = form.plannedQuantity,
                                onValueChange = { form = form.copy(plannedQuantity = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Number,
                            )
                            PlanEditField(
                                label = "生産数",
                                value = form.actualProductionQuantity,
                                onValueChange = { form = form.copy(actualProductionQuantity = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Number,
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    PlanEditSection("日程・ロット", CuttingInstructionTheme.PlanEditSecGreen) {
                        PlanEditGridRow {
                            PlanEditDateField(
                                label = "開始日",
                                value = form.startDate,
                                onValueChange = { form = form.copy(startDate = it) },
                                placeholder = "選択",
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditDateField(
                                label = "終了日",
                                value = form.endDate,
                                onValueChange = { form = form.copy(endDate = it) },
                                placeholder = "選択",
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditField(
                                label = "生産ロット数",
                                value = form.productionLotSize,
                                onValueChange = { form = form.copy(productionLotSize = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Number,
                            )
                            PlanEditField(
                                label = "ロットNo",
                                value = form.lotNumber,
                                onValueChange = { form = form.copy(lotNumber = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    PlanEditSection("工程フラグ", CuttingInstructionTheme.PlanEditSecAmber) {
                        PlanEditGridRow {
                            PlanEditSwitchItem("面取工程", form.hasChamferingProcess, { form = form.copy(hasChamferingProcess = it) }, Modifier.weight(1f))
                            PlanEditSwitchItem("SW工程", form.hasSwProcess, { form = form.copy(hasSwProcess = it) }, Modifier.weight(1f))
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    PlanEditSection("寸法・材料", CuttingInstructionTheme.PlanEditSecPurple) {
                        PlanEditGridRow {
                            PlanEditField(
                                label = "管理コード",
                                value = form.managementCode,
                                onValueChange = {},
                                modifier = Modifier.weight(1f),
                                readOnly = true,
                                placeholder = "自動生成（保存時に再計算）",
                                textAlign = TextAlign.Center,
                            )
                            PlanEditField(
                                label = "取数",
                                value = form.takeCount,
                                onValueChange = { form = form.copy(takeCount = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Number,
                            )
                            PlanEditField(
                                label = "切断長",
                                value = form.cuttingLength,
                                onValueChange = { form = form.copy(cuttingLength = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Decimal,
                            )
                        }
                        PlanEditGridRow {
                            PlanEditField(
                                label = "面取長",
                                value = form.chamferingLength,
                                onValueChange = { form = form.copy(chamferingLength = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Decimal,
                            )
                            PlanEditField(
                                label = "展開長",
                                value = form.developedLength,
                                onValueChange = { form = form.copy(developedLength = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Decimal,
                            )
                            PlanEditField(
                                label = "端材長(mm)",
                                value = form.scrapLength,
                                onValueChange = { form = form.copy(scrapLength = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Decimal,
                            )
                        }
                        PlanEditGridRow {
                            PlanEditDropdownField(
                                label = "原材料",
                                value = form.materialName,
                                options = materialOptions,
                                onSelect = { form = form.copy(materialName = it) },
                                emptyLabel = "原材料を選択",
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditField(
                                label = "材料メーカー",
                                value = form.materialManufacturer,
                                onValueChange = { form = form.copy(materialManufacturer = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                            PlanEditField(
                                label = "規格",
                                value = form.standardSpecification,
                                onValueChange = { form = form.copy(standardSpecification = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                        PlanEditGridRow {
                            PlanEditStockSwitch(
                                checked = form.useMaterialStockSub,
                                onCheckedChange = { form = form.copy(useMaterialStockSub = it) },
                                modifier = Modifier.weight(1f),
                            )
                            PlanEditField(
                                label = "材料使用数",
                                value = form.usageCount,
                                onValueChange = { form = form.copy(usageCount = it) },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                keyboardType = KeyboardType.Decimal,
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                HorizontalDivider(color = CuttingInstructionTheme.PlanEditSectionBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CuttingInstructionTheme.PlanEditFooterBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.FilterBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text("取消", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(form) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CuttingInstructionTheme.PlanEditSaveStart),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("保存", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanEditSection(
    label: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CuttingInstructionTheme.PlanEditSectionBg)
            .border(1.dp, CuttingInstructionTheme.PlanEditSectionBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent, letterSpacing = 0.6.sp)
        }
        content()
    }
}

@Composable
private fun PlanEditGridRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun PlanEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            color = CuttingInstructionTheme.PlanEditLabel,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(3.dp))
        if (readOnly) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.ifBlank { placeholder.ifBlank { "-" } },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CuttingInstructionTheme.Subtitle,
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }
        } else if (textAlign == TextAlign.Center) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF303133),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (value.isEmpty() && placeholder.isNotBlank()) {
                            Text(
                                placeholder,
                                fontSize = 12.sp,
                                color = CuttingInstructionTheme.Subtitle,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    placeholder = if (placeholder.isNotBlank()) {
                        { Text(placeholder, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    } else {
                        null
                    },
                    textStyle = TextStyle(fontSize = 12.sp, lineHeight = 14.sp),
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF93C5FD),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                    ),
                )
            }
        }
    }
}

@Composable
private fun PlanEditDateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "選択",
    editable: Boolean = true,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (editable && showPicker) {
        OrderDailyDatePickerDialog(
            value = value,
            accent = CuttingInstructionTheme.PlanEditSecBlue,
            onDismiss = { showPicker = false },
            onConfirm = { selected ->
                onValueChange(selected)
                showPicker = false
            },
        )
    }
    val fieldBg = if (editable) Color.White else Color(0xFFF1F5F9)
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            color = CuttingInstructionTheme.PlanEditLabel,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 118.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(fieldBg)
                .then(if (editable) Modifier.clickable { showPicker = true } else Modifier)
                .padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value.ifBlank { placeholder },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (value.isBlank()) CuttingInstructionTheme.Subtitle else Color(0xFF303133),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f),
            )
            if (editable) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "日付選択",
                    tint = CuttingInstructionTheme.PlanEditSecBlue,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditDropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    emptyLabel: String = "選択",
    editable: Boolean = true,
    includeEmptyOption: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.firstOrNull { (cd, name) -> cd == value || name == value }?.second
        ?: value.ifBlank { emptyLabel }
    val fieldBg = if (editable) Color.White else Color(0xFFF1F5F9)
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            color = CuttingInstructionTheme.PlanEditLabel,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(3.dp))
        if (editable) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                        .background(fieldBg)
                        .clickable { expanded = true }
                        .padding(start = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = displayText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (value.isBlank()) CuttingInstructionTheme.Subtitle else Color(0xFF303133),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = CuttingInstructionTheme.PlanEditSecBlue,
                        modifier = Modifier.size(18.dp),
                    )
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    if (includeEmptyOption) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    emptyLabel,
                                    fontSize = 12.sp,
                                    color = CuttingInstructionTheme.Subtitle,
                                )
                            },
                            onClick = {
                                onSelect("")
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        )
                        HorizontalDivider(color = CuttingInstructionTheme.PlanEditSectionBorder.copy(alpha = 0.6f))
                    }
                    options.forEach { (cd, name) ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 12.sp) },
                            onClick = {
                                onSelect(cd)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                    .background(fieldBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = displayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isBlank()) CuttingInstructionTheme.Subtitle else Color(0xFF303133),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PlanEditSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(24.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CuttingInstructionTheme.PlanEditSecBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
            ),
        )
        Text(label, fontSize = 12.sp, color = Color(0xFF334155))
    }
}

@Composable
private fun PlanEditStockSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            "使用サブ在庫",
            fontSize = 11.sp,
            color = CuttingInstructionTheme.PlanEditLabel,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("主表", fontSize = 11.sp, color = if (!checked) CuttingInstructionTheme.PlanEditSecBlue else CuttingInstructionTheme.Subtitle, fontWeight = if (!checked) FontWeight.SemiBold else FontWeight.Normal)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CuttingInstructionTheme.PlanEditSecPurple,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1),
                ),
            )
            Text("サブ", fontSize = 11.sp, color = if (checked) CuttingInstructionTheme.PlanEditSecPurple else CuttingInstructionTheme.Subtitle, fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}



@Composable

private fun UsageCountEditDialog(current: Double, onSave: (Double) -> Unit, onDismiss: () -> Unit) {

    var value by remember { mutableStateOf(formatUsageCount(current)) }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = { Text("材料使用数編集") },

        text = { OutlinedTextField(value, { value = it }, label = { Text("使用数（束）") }) },

        confirmButton = {

            Button(onClick = { value.toDoubleOrNull()?.let { if (it > 0) onSave(it) } }) { Text("保存") }

        },

        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },

    )

}



@Composable

private fun SpecifiedDateMaterialDialog(

    date: String,

    rows: List<InstructionCuttingRowDto>,

    loading: Boolean,

    onDateChange: (String) -> Unit,

    onReload: () -> Unit,

    onPrint: () -> Unit,

    onDismiss: () -> Unit,

) {

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp)) {

            Text("指定日材料数", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(date, onDateChange, label = { Text("指定日") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(onClick = onReload) { Text("読込") }

                Button(onClick = onPrint, enabled = rows.isNotEmpty()) { Text("印刷") }

            }

            if (loading) CircularProgressIndicator()

            else rows.forEach { r ->

                Text("${r.productName ?: "-"} / ${r.materialName ?: "-"} : ${formatUsageCount(r.usageCount)}", fontSize = 11.sp)

            }

            TextButton(onClick = onDismiss) { Text("閉じる") }

        }

    }

}



@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveToCuttingDialog(
    plan: InstructionPlanRowDto,
    machines: List<Pair<String, String>>,
    defaultDate: String,
    defaultMachine: String = "",
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var date by remember(defaultDate) { mutableStateOf(defaultDate) }
    var machine by remember(defaultMachine, machines) {
        mutableStateOf(
            defaultMachine.takeIf { selected -> machines.any { it.first == selected } }
                ?: machines.firstOrNull()?.first.orEmpty(),
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        OrderDailyDatePickerDialog(
            value = date,
            accent = Color(0xFF4F46E5),
            onDismiss = { showDatePicker = false },
            onConfirm = { selected ->
                date = selected
                showDatePicker = false
            },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3730A3), Color(0xFF4F46E5)),
                            ),
                        )
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "切断指示の登録",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                            Text(
                                "生産日と切断機を指定してロットを移行します",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "閉じる",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    MtDialogFieldLabel("生産日")
                    MtDialogProductionDateControl(
                        date = date,
                        onDateChange = { date = it },
                        onOpenPicker = { showDatePicker = true },
                    )
                    Spacer(Modifier.height(16.dp))
                    MtDialogFieldLabel("切断機")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            machines.forEach { (name, _) ->
                                MtDialogMachineButton(
                                    name = name,
                                    selected = machine == name,
                                    onClick = { machine = name },
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF475569),
                        ),
                    ) {
                        Text("キャンセル", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { onConfirm(date, machine) },
                        enabled = date.isNotBlank() && machine.isNotBlank(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CuttingInstructionTheme.CuttingBtnIssueSolid,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFCBD5E1),
                        ),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("登録", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MtDialogFieldLabel(text: String) {
    Text(
        text,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun MtDialogProductionDateControl(
    date: String,
    onDateChange: (String) -> Unit,
    onOpenPicker: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconButton(
            onClick = { onDateChange(shiftInstructionDate(date.ifBlank { instructionToday() }, -1)) },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "前日",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF64748B),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onOpenPicker)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                date.ifBlank { "生産日を選択" },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (date.isBlank()) Color(0xFF94A3B8) else Color(0xFF1E293B),
            )
        }
        IconButton(
            onClick = { onDateChange(shiftInstructionDate(date.ifBlank { instructionToday() }, 1)) },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "翌日",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF64748B),
            )
        }
        OutlinedButton(
            onClick = { onDateChange(instructionToday()) },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier.height(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF475569),
            ),
        ) {
            Text("今日", fontSize = 11.sp)
        }
    }
}

@Composable
private fun MtDialogMachineButton(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val borderColor = if (selected) CuttingInstructionTheme.CuttingBtnIssueSolid else Color(0xFFCBD5E1)
    val backgroundColor = if (selected) CuttingInstructionTheme.CuttingBtnIssueSolid else Color(0xFFF8FAFC)
    val textColor = if (selected) Color.White else Color(0xFF475569)
    Box(
        modifier = Modifier
            .graphicsLayer { alpha = if (pressed) 0.88f else 1f }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(name, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

data class CuttingEditFormState(
    val cuttingMachine: String = "",
    val productionSequence: String = "1",
    val actualProductionQuantity: String = "",
    val defectQty: String = "",
    val productionLotSize: String = "",
    val lotNumber: String = "",
    val useMaterialStockSub: Boolean = false,
    val usageCount: String = "1",
    val remarks: String = "",
)

private val CuttingEditRemarkTags = listOf("取合せ・試作", "取合せ", "成型17号用", "青ニス", "半端材本")

private enum class CuttingEditFieldStyle {
    Normal,
    Qty,
    Defect,
}

private object CuttingEditDialogColors {
    val HeaderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B)),
    )
    val BodyBg = Color(0xFFFAFBFC)
    val Label = Color(0xFF64748B)
    val SaveBlue = Color(0xFF2563EB)
    val TagBg = Color(0xFFEFF6FF)
    val TagBorder = Color(0xFFBFDBFE)
    val TagText = Color(0xFF1D4ED8)
}

@Composable
private fun CuttingEditFormItem(
    label: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 72.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = CuttingEditDialogColors.Label,
            modifier = Modifier.width(labelWidth),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun CuttingEditFormRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        content()
    }
}

@Composable
private fun CuttingEditInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    style: CuttingEditFieldStyle = CuttingEditFieldStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    singleLine: Boolean = true,
) {
    val (background, border) = when (style) {
        CuttingEditFieldStyle.Normal -> Color.White to Color(0xFFE2E8F0)
        CuttingEditFieldStyle.Qty -> Color(0xFFEFF6FF) to Color(0xFFBFDBFE)
        CuttingEditFieldStyle.Defect -> Color(0xFFFFFBEB) to Color(0xFFFDE68A)
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            fontSize = 12.sp,
            lineHeight = if (singleLine) 14.sp else 18.sp,
            textAlign = textAlign,
            color = Color(0xFF303133),
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(32.dp) else Modifier.heightIn(min = 64.dp))
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = if (singleLine) 0.dp else 8.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (singleLine && textAlign == TextAlign.Center) {
                    Alignment.Center
                } else if (singleLine) {
                    Alignment.CenterStart
                } else {
                    Alignment.TopStart
                },
            ) {
                if (value.isEmpty() && placeholder.isNotBlank()) {
                    Text(
                        placeholder,
                        fontSize = 12.sp,
                        color = CuttingInstructionTheme.Subtitle,
                        textAlign = textAlign,
                    )
                }
                innerTextField()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuttingEditDropdownInput(
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    emptyLabel: String = "選択",
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options.firstOrNull { (cd, name) -> cd == value || name == value }?.second
        ?: value.ifBlank { emptyLabel }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = displayText,
                fontSize = 12.sp,
                color = if (value.isBlank()) CuttingInstructionTheme.Subtitle else Color(0xFF303133),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 12.sp) },
                    onClick = {
                        onSelect(cd)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CuttingEditStockSwitchInput(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "主表",
            fontSize = 11.sp,
            color = if (!checked) CuttingEditDialogColors.SaveBlue else CuttingInstructionTheme.Subtitle,
            fontWeight = if (!checked) FontWeight.SemiBold else FontWeight.Normal,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CuttingEditDialogColors.SaveBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
            ),
        )
        Text(
            "サブ",
            fontSize = 11.sp,
            color = if (checked) CuttingEditDialogColors.SaveBlue else CuttingInstructionTheme.Subtitle,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun CuttingEditTagButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingEditDialogColors.TagBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CuttingEditDialogColors.TagBg,
            contentColor = CuttingEditDialogColors.TagText,
        ),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuttingEditDialog(
    row: InstructionCuttingRowDto,
    machines: List<Pair<String, String>>,
    onSave: (InstructionCuttingRowDto, CuttingEditFormState) -> Unit,
    onDismiss: () -> Unit,
) {
    val rowUsage = row.usageCount?.toDouble()?.takeIf { it > 0 } ?: 1.0
    var machine by remember(row.id) { mutableStateOf(row.cuttingMachine.orEmpty()) }
    var sequence by remember(row.id) { mutableStateOf((row.productionSequence ?: 1).toString()) }
    var qty by remember(row.id) { mutableStateOf(row.actualProductionQuantity?.toString().orEmpty()) }
    var defect by remember(row.id) { mutableStateOf(row.defectQty?.toString().orEmpty()) }
    var lotSize by remember(row.id) { mutableStateOf(row.productionLotSize?.toString().orEmpty()) }
    var lotNumber by remember(row.id) { mutableStateOf(row.lotNumber.orEmpty()) }
    var useSubStock by remember(row.id) { mutableStateOf(row.useMaterialStockSub == 1) }
    var usageCount by remember(row.id) { mutableStateOf(formatUsageCount(rowUsage)) }
    var remarks by remember(row.id) { mutableStateOf(row.remarks.orEmpty()) }
    fun appendRemark(label: String) {
        val current = remarks.trim()
        remarks = if (current.isEmpty()) label else "$current $label"
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier
                .widthIn(max = 504.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CuttingEditDialogColors.HeaderGradient)
                        .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "切断指示編集",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.98f),
                        letterSpacing = 0.2.sp,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .background(CuttingEditDialogColors.BodyBg)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CuttingEditFormRow {
                        CuttingEditFormItem(
                            label = "切断機",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditDropdownInput(
                                value = machine,
                                options = machines,
                                onSelect = { machine = it },
                                emptyLabel = "切断機を選択",
                            )
                        }
                        CuttingEditFormItem(
                            label = "生産順",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = sequence,
                                onValueChange = { sequence = it.filter(Char::isDigit).take(4) },
                                placeholder = "1",
                                keyboardType = KeyboardType.Number,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    CuttingEditFormRow {
                        CuttingEditFormItem(
                            label = "生産数",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = qty,
                                onValueChange = { qty = it.filter(Char::isDigit) },
                                placeholder = "生産数",
                                keyboardType = KeyboardType.Number,
                                style = CuttingEditFieldStyle.Qty,
                                textAlign = TextAlign.Center,
                            )
                        }
                        CuttingEditFormItem(
                            label = "不良数",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = defect,
                                onValueChange = { defect = it.filter(Char::isDigit) },
                                placeholder = "不良数",
                                keyboardType = KeyboardType.Number,
                                style = CuttingEditFieldStyle.Defect,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    CuttingEditFormRow {
                        CuttingEditFormItem(
                            label = "ロット数",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = lotSize,
                                onValueChange = { lotSize = it.filter(Char::isDigit).take(4) },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number,
                                textAlign = TextAlign.Center,
                            )
                        }
                        CuttingEditFormItem(
                            label = "ロットNo.",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = lotNumber,
                                onValueChange = { lotNumber = it },
                                placeholder = "ロットNo.",
                            )
                        }
                    }
                    CuttingEditFormRow {
                        CuttingEditFormItem(
                            label = "使用サブ在庫",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditStockSwitchInput(
                                checked = useSubStock,
                                onCheckedChange = { useSubStock = it },
                            )
                        }
                        CuttingEditFormItem(
                            label = "材料使用数",
                            modifier = Modifier.weight(1f),
                        ) {
                            CuttingEditInputField(
                                value = usageCount,
                                onValueChange = { usageCount = it },
                                placeholder = "1",
                                keyboardType = KeyboardType.Decimal,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    CuttingEditFormItem(
                        label = "備考",
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                CuttingEditRemarkTags.forEach { tag ->
                                    CuttingEditTagButton(label = tag, onClick = { appendRemark(tag) })
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 72.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                            ) {
                                BasicTextField(
                                    value = remarks,
                                    onValueChange = { if (it.length <= 500) remarks = it },
                                    textStyle = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF303133),
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .padding(bottom = 16.dp),
                                )
                                if (remarks.isEmpty()) {
                                    Text(
                                        "備考",
                                        fontSize = 12.sp,
                                        color = CuttingInstructionTheme.Subtitle,
                                        modifier = Modifier.align(Alignment.TopStart),
                                    )
                                }
                                Text(
                                    "${remarks.length} / 500",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    ) {
                        Text("取消", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                row,
                                CuttingEditFormState(
                                    cuttingMachine = machine,
                                    productionSequence = sequence.ifBlank { "1" },
                                    actualProductionQuantity = qty,
                                    defectQty = defect,
                                    productionLotSize = lotSize,
                                    lotNumber = lotNumber,
                                    useMaterialStockSub = useSubStock,
                                    usageCount = usageCount,
                                    remarks = remarks,
                                ),
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CuttingEditDialogColors.SaveBlue,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("保存", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}



@Composable

private fun ChamferingEditDialog(

    row: InstructionChamferingRowDto,

    machines: List<Pair<String, String>>,

    onSave: (InstructionChamferingRowDto, String, String, Int?, Int?, String?) -> Unit,

    onDismiss: () -> Unit,

) {

    var day by remember { mutableStateOf(row.productionDay?.take(10).orEmpty()) }

    var machine by remember { mutableStateOf(row.chamferingMachine.orEmpty()) }

    var qty by remember { mutableStateOf(row.actualProductionQuantity?.toString().orEmpty()) }

    var defect by remember { mutableStateOf(row.defectQty?.toString().orEmpty()) }

    var remarks by remember { mutableStateOf(row.remarks.orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp)) {

            Text("面取指示編集", fontWeight = FontWeight.Bold)

            OutlinedTextField(day, { day = it }, label = { Text("生産日") }, modifier = Modifier.fillMaxWidth())

            InstructionFilterDropdown("面取機", machine, machines, { machine = it }, Modifier.fillMaxWidth(), includeAll = false)

            OutlinedTextField(qty, { qty = it }, label = { Text("生産数") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(defect, { defect = it }, label = { Text("不良数") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(remarks, { remarks = it }, label = { Text("備考") }, modifier = Modifier.fillMaxWidth())

            Row {

                Button(onClick = {

                    onSave(row, day, machine, qty.toIntOrNull(), defect.toIntOrNull(), remarks.ifBlank { null })

                }) { Text("保存") }

                TextButton(onClick = onDismiss) { Text("キャンセル") }

            }

        }

    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SplitToNextDayDialog(
    machineLabel: String,
    machineName: String,
    productionDay: String?,
    totalQty: Int,
    productSubtitle: String,
    submitting: Boolean,
    onConfirm: (todayQty: Int, nextDay: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val japanZone = remember { ZoneId.of("Asia/Tokyo") }
    val productionDayStr = productionDay?.trim()?.take(10).orEmpty()
    var todayQtyInput by remember(productionDayStr, totalQty) { mutableStateOf("0") }
    var nextDay by remember(productionDayStr) { mutableStateOf(nextWeekdayFrom(productionDayStr)) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val todayQty = todayQtyInput.toIntOrNull() ?: 0
    val remainderQty = (totalQty - todayQty).coerceAtLeast(0)
    val headerGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFF59E0B)),
    )
    val infoCardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFEFF6FF), Color(0xFFF0F9FF)),
    )
    val warningColor = Color(0xFFE6A23C)

    if (showDatePicker) {
        val initialMillis = remember(nextDay) {
            runCatching { LocalDate.parse(nextDay.trim().take(10)).atStartOfDay(japanZone).toInstant().toEpochMilli() }
                .getOrElse { LocalDate.now(japanZone).atStartOfDay(japanZone).toInstant().toEpochMilli() }
        }
        val selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(japanZone).toLocalDate()
                    return date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
                }
            }
        }
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = selectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(japanZone).toLocalDate()
                        nextDay = selected.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("確定", color = warningColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("キャンセル", color = Color(0xFF64748B))
                }
            },
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = warningColor,
                    todayDateBorderColor = warningColor,
                    selectedYearContainerColor = warningColor,
                ),
            )
        }
    }

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .shadow(12.dp, RoundedCornerShape(12.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.KeyboardDoubleArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "未完了分を翌日へ順延",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        if (productSubtitle.isNotBlank()) {
                            Text(
                                productSubtitle,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = { if (!submitting) onDismiss() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White.copy(alpha = 0.85f))
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(infoCardGradient)
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SplitToNextDayInfoRow("生産日", formatInstructionDate(productionDay))
                        SplitToNextDayInfoRow(machineLabel, machineName.ifBlank { "-" })
                        SplitToNextDayInfoRow("元生産数", totalQty.toString(), highlight = true)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(12.dp)
                                    .background(Color(0xFFF59E0B), RoundedCornerShape(2.dp)),
                            )
                            Text(
                                "分割設定",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                letterSpacing = 0.6.sp,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("当日完成数", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                    Text(" *", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                                OutlinedTextField(
                                    value = todayQtyInput,
                                    onValueChange = {
                                        todayQtyInput = it.filter(Char::isDigit).take(8)
                                        validationError = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("0", fontSize = 13.sp) },
                                    suffix = { Text("個", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 13.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = warningColor,
                                        cursorColor = warningColor,
                                    ),
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("翌日順延数", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 56.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFF7ED))
                                        .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        remainderQty.toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEA580C),
                                    )
                                    Text("個", fontSize = 11.sp, color = Color(0xFFC2410C), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("翌日の生産日", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .clickable(enabled = !submitting) { showDatePicker = true }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    nextDay.ifBlank { "選択" },
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    color = if (nextDay.isBlank()) Color(0xFF94A3B8) else Color(0xFF1E293B),
                                )
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "順延後、元の行は自動的に「完了」になります。",
                            fontSize = 11.sp,
                            color = Color(0xFF78716C),
                            lineHeight = 15.sp,
                        )
                    }

                    validationError?.let { msg ->
                        Text(msg, fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !submitting) {
                        Text("取消", color = Color(0xFF64748B))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (todayQty < 0 || todayQty >= totalQty) {
                                validationError = "当日完成数は 0 以上、かつ現在の生産数より少なく入力してください"
                                return@Button
                            }
                            onConfirm(todayQty, nextDay)
                        },
                        enabled = !submitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = warningColor,
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                            Spacer(Modifier.width(6.dp))
                        } else {
                            Icon(
                                Icons.Default.KeyboardDoubleArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("順延する", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitToNextDayInfoRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.width(52.dp))
        Text(
            value,
            fontSize = if (highlight) 14.sp else 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) Color(0xFF2563EB) else Color(0xFF1E293B),
        )
    }
}


data class ChamferingPlanFormState(

    val productionMonth: String = "",

    val productionDay: String = "",

    val productionLine: String = "",

    val productionOrder: String = "",

    val productCd: String = "",

    val productName: String = "",

    val actualProductionQuantity: String = "0",

    val productionLotSize: String = "",

    val lotNumber: String = "",

    val materialName: String = "",

    val chamferingLength: String = "",

    val hasSwProcess: Boolean = false,

)



@Composable

private fun ChamferingPlanFormDialog(

    title: String,

    subtitle: String,

    initial: ChamferingPlanFormState,

    machines: List<Pair<String, String>>,

    productOptions: List<Pair<String, String>>,

    onLoadProductDetail: suspend (String) -> ProductBatchDetailDto?,

    submitting: Boolean,

    onSave: (ChamferingPlanFormState) -> Unit,

    onDismiss: () -> Unit,

) {

    var form by remember { mutableStateOf(initial) }

    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()

    val productDropdownOptions = remember(productOptions) {

        productOptions.map { (cd, name) -> cd to "$name  [$cd]" }

    }

    suspend fun applyProductSelection(productCd: String) {

        if (productCd.isBlank()) {

            form = form.copy(productCd = "", productName = "", materialName = "", chamferingLength = "")

            return

        }

        val fallbackName = productOptions.firstOrNull { it.first == productCd }?.second.orEmpty()

        val detail = onLoadProductDetail(productCd)

        form = form.copy(

            productCd = productCd,

            productName = detail?.productName?.takeIf { it.isNotBlank() } ?: fallbackName,

            materialName = detail?.materialName.orEmpty(),

            chamferingLength = formatPlanFormNumber(detail?.chamferingLength ?: detail?.chamferLength),

        )

    }

    Dialog(onDismissRequest = onDismiss) {

        Surface(

            modifier = Modifier

                .fillMaxWidth(0.96f)

                .widthIn(max = 580.dp),

            shape = RoundedCornerShape(12.dp),

            color = Color.White,

            shadowElevation = 12.dp,

        ) {

            Column {

                Box(

                    modifier = Modifier

                        .fillMaxWidth()

                        .background(

                            Brush.linearGradient(

                                listOf(CuttingInstructionTheme.PlanEditHeaderStart, CuttingInstructionTheme.PlanEditHeaderEnd),

                            ),

                        )

                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),

                ) {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        verticalAlignment = Alignment.CenterVertically,

                    ) {

                        Box(

                            modifier = Modifier

                                .size(30.dp)

                                .clip(RoundedCornerShape(8.dp))

                                .background(Color.White.copy(alpha = 0.18f))

                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),

                            contentAlignment = Alignment.Center,

                        ) {

                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))

                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {

                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)

                            if (subtitle.isNotBlank()) {

                                Text(

                                    subtitle,

                                    fontSize = 11.sp,

                                    color = Color.White.copy(alpha = 0.7f),

                                    maxLines = 1,

                                    overflow = TextOverflow.Ellipsis,

                                )

                            }

                        }

                        IconButton(onClick = onDismiss) {

                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White.copy(alpha = 0.75f))

                        }

                    }

                }

                Column(

                    modifier = Modifier

                        .heightIn(max = 520.dp)

                        .verticalScroll(scrollState)

                        .padding(horizontal = 16.dp, vertical = 12.dp),

                    verticalArrangement = Arrangement.spacedBy(10.dp),

                ) {

                    PlanEditSection("基本情報", CuttingInstructionTheme.PlanEditSecBlue) {

                        PlanEditGridRow {

                            PlanEditMonthField(

                                label = "生産月",

                                value = form.productionMonth,

                                onValueChange = { form = form.copy(productionMonth = it) },

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditDateField(

                                label = "生産日",

                                value = form.productionDay,

                                onValueChange = { selected ->

                                    form = form.copy(

                                        productionDay = selected,

                                        productionMonth = selected.take(7).ifBlank { form.productionMonth },

                                    )

                                },

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditDropdownField(

                                label = "ライン（面取機）",

                                value = form.productionLine,

                                options = machines,

                                onSelect = { form = form.copy(productionLine = it) },

                                emptyLabel = "面取機を選択",

                                includeEmptyOption = true,

                                modifier = Modifier.weight(1f),

                            )

                        }

                        PlanEditGridRow {

                            PlanEditField(

                                label = "順位",

                                value = form.productionOrder,

                                onValueChange = { form = form.copy(productionOrder = it) },

                                placeholder = "省略可",

                                keyboardType = KeyboardType.Number,

                                textAlign = TextAlign.Center,

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditDropdownField(

                                label = "製品",

                                value = form.productCd,

                                options = productDropdownOptions,

                                onSelect = { cd -> scope.launch { applyProductSelection(cd) } },

                                emptyLabel = "製品を選択",

                                includeEmptyOption = true,

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditField(

                                label = "製品名（確認）",

                                value = form.productName,

                                onValueChange = {},

                                readOnly = true,

                                modifier = Modifier.weight(1f),

                            )

                        }

                        PlanEditGridRow {

                            PlanEditField(

                                label = "ロットNo",

                                value = form.lotNumber,

                                onValueChange = { form = form.copy(lotNumber = it) },

                                placeholder = "ロットNo",

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditField(

                                label = "原材料",

                                value = form.materialName,

                                onValueChange = { form = form.copy(materialName = it) },

                                placeholder = "原材料名",

                                textAlign = TextAlign.Center,

                                modifier = Modifier.weight(1f),

                            )

                            Spacer(Modifier.weight(1f))

                        }

                    }

                    PlanEditSection("数量・寸法", CuttingInstructionTheme.PlanEditSecGreen) {

                        PlanEditGridRow {

                            PlanEditField(

                                label = "生産数",

                                value = form.actualProductionQuantity,

                                onValueChange = { form = form.copy(actualProductionQuantity = it) },

                                keyboardType = KeyboardType.Number,

                                textAlign = TextAlign.Center,

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditField(

                                label = "ロット数",

                                value = form.productionLotSize,

                                onValueChange = { form = form.copy(productionLotSize = it) },

                                keyboardType = KeyboardType.Number,

                                textAlign = TextAlign.Center,

                                modifier = Modifier.weight(1f),

                            )

                            PlanEditField(

                                label = "面取長",

                                value = form.chamferingLength,

                                onValueChange = { form = form.copy(chamferingLength = it) },

                                keyboardType = KeyboardType.Decimal,

                                textAlign = TextAlign.Center,

                                modifier = Modifier.weight(1f),

                            )

                        }

                    }

                    PlanEditSection("工程", CuttingInstructionTheme.PlanEditSecAmber) {

                        PlanEditGridRow {

                            Column(modifier = Modifier.weight(1f)) {

                                Text(

                                    "SW工程",

                                    fontSize = 11.sp,

                                    color = CuttingInstructionTheme.PlanEditLabel,

                                    fontWeight = FontWeight.Medium,

                                )

                                PlanEditSwitchItem(

                                    label = "SW",

                                    checked = form.hasSwProcess,

                                    onCheckedChange = { form = form.copy(hasSwProcess = it) },

                                )

                            }

                            Spacer(Modifier.weight(1f))

                            Spacer(Modifier.weight(1f))

                        }

                    }

                }

                HorizontalDivider(color = CuttingInstructionTheme.PlanEditSectionBorder)

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .background(CuttingInstructionTheme.PlanEditFooterBg)

                        .padding(horizontal = 16.dp, vertical = 10.dp),

                    horizontalArrangement = Arrangement.End,

                    verticalAlignment = Alignment.CenterVertically,

                ) {

                    OutlinedButton(

                        onClick = onDismiss,

                        enabled = !submitting,

                        shape = RoundedCornerShape(8.dp),

                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),

                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),

                        modifier = Modifier.height(32.dp),

                    ) {

                        Text("キャンセル", fontSize = 12.sp, color = Color(0xFF64748B))

                    }

                    Spacer(Modifier.width(8.dp))

                    Button(

                        onClick = { onSave(form) },

                        enabled = !submitting,

                        shape = RoundedCornerShape(8.dp),

                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),

                        modifier = Modifier.height(32.dp),

                        colors = ButtonDefaults.buttonColors(containerColor = CuttingInstructionTheme.PlanEditSaveStart),

                    ) {

                        if (submitting) {

                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)

                        } else {

                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))

                            Spacer(Modifier.width(4.dp))

                            Text("保存", fontSize = 12.sp)

                        }

                    }

                }

            }

        }

    }

}



@Composable

private fun PlanEditMonthField(

    label: String,

    value: String,

    onValueChange: (String) -> Unit,

    modifier: Modifier = Modifier,

) {

    val pickerSeed = remember(value) {

        when {

            value.length >= 7 -> "${value.take(7)}-01"

            value.isNotBlank() -> value

            else -> instructionToday()

        }

    }

    PlanEditDateField(

        label = label,

        value = pickerSeed,

        onValueChange = { onValueChange(it.take(7)) },

        modifier = modifier,

        placeholder = "YYYY-MM",

    )

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MtDialogMachineSelect(
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1E293B),
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    value.ifBlank { placeholder },
                    fontSize = 12.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
                    color = if (value.isBlank()) Color(0xFF94A3B8) else Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (cd, name) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 12.sp) },
                    onClick = {
                        onSelect(cd)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveChamferingPlanDialog(
    plan: InstructionChamferingPlanRowDto,
    machines: List<Pair<String, String>>,
    defaultDate: String,
    onConfirm: (String, String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var date by remember(defaultDate) { mutableStateOf(defaultDate) }
    var machine by remember(machines) {
        mutableStateOf(machines.firstOrNull()?.first.orEmpty())
    }
    var machine2 by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val headerStart = Color(0xFF065F46)
    val headerEnd = Color(0xFF059669)
    val submitStart = Color(0xFF059669)
    val submitEnd = Color(0xFF10B981)

    if (showDatePicker) {
        OrderDailyDatePickerDialog(
            value = date,
            accent = headerEnd,
            onDismiss = { showDatePicker = false },
            onConfirm = { selected ->
                date = selected
                showDatePicker = false
            },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .shadow(16.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x20059669), spotColor = Color(0x30059669)),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(headerStart, headerEnd)))
                        .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "面取指示の登録",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                letterSpacing = 0.3.sp,
                            )
                            Text(
                                "生産日と面取機を指定してロットを移行します",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.78f),
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "閉じる",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    MtDialogFieldLabel("生産日")
                    MtDialogProductionDateControl(
                        date = date,
                        onDateChange = { date = it },
                        onOpenPicker = { showDatePicker = true },
                    )
                    Spacer(Modifier.height(16.dp))
                    MtDialogFieldLabel("面取機")
                    MtDialogMachineSelect(
                        value = machine,
                        options = machines,
                        onSelect = { machine = it },
                        placeholder = "面取機を選択",
                    )
                    if (plan.hasSwProcess == 1) {
                        Spacer(Modifier.height(16.dp))
                        MtDialogFieldLabel("面取機（SW）")
                        MtDialogMachineSelect(
                            value = machine2,
                            options = machines,
                            onSelect = { machine2 = it },
                            placeholder = "面取機（SW）を選択",
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF475569),
                        ),
                    ) {
                        Text("キャンセル", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = { onConfirm(date, machine, machine2.ifBlank { null }) },
                        enabled = date.isNotBlank() && machine.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFCBD5E1),
                        ),
                        modifier = Modifier.background(
                            if (date.isNotBlank() && machine.isNotBlank()) {
                                Brush.horizontalGradient(listOf(submitStart, submitEnd))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1)))
                            },
                            RoundedCornerShape(8.dp),
                        ),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("登録", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}



@Composable

private fun KanbanEditDialog(

    row: KanbanIssuanceRowDto,

    onSave: (KanbanIssuanceRowDto, String?, Int?, String?) -> Unit,

    onDismiss: () -> Unit,

) {

    var productName by remember { mutableStateOf(row.productName.orEmpty()) }

    var qty by remember { mutableStateOf(row.actualProductionQuantity?.toString().orEmpty()) }

    var day by remember { mutableStateOf(row.productionDay?.take(10).orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp)) {

            Text("カンバン編集", fontWeight = FontWeight.Bold)

            OutlinedTextField(productName, { productName = it }, label = { Text("製品名") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(qty, { qty = it }, label = { Text("生産数") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(day, { day = it }, label = { Text("生産日") }, modifier = Modifier.fillMaxWidth())

            Row {

                Button(onClick = { onSave(row, productName.ifBlank { null }, qty.toIntOrNull(), day.ifBlank { null }) }) { Text("保存") }

                TextButton(onClick = onDismiss) { Text("キャンセル") }

            }

        }

    }

}



data class ChamferingInstructionFormState(

    val productionDay: String = "",

    val productionLine: String = "",

    val chamferingMachine: String = "",

    val productCd: String = "",

    val productName: String = "",

    val actualProductionQuantity: String = "",

    val productionSequence: String = "",

    val materialName: String = "",

)



@Composable
private fun NewChamferingInstructionDialog(
    initial: ChamferingInstructionFormState,
    lineOptions: List<Pair<String, String>>,
    chamferingMachines: List<Pair<String, String>>,
    productOptions: List<Pair<String, String>>,
    materialOptions: List<String>,
    submitting: Boolean,
    onSave: (ChamferingInstructionFormState) -> Unit,
    onDismiss: () -> Unit,
) {
    var form by remember { mutableStateOf(initial) }
    val scrollState = rememberScrollState()
    val productDropdownOptions = remember(productOptions) {
        productOptions.map { (cd, name) -> cd to "$cd $name".trim() }
    }
    val materialDropdownOptions = remember(materialOptions) {
        materialOptions.map { it to it }
    }
    val managementCode = previewChamferingManagementCode(
        form.productionDay,
        form.productionLine,
        form.productCd,
        form.productionSequence,
    )
    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF0FDF4), Color.White),
                            ),
                        )
                        .drawBehind {
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "面取指示 - 新規追加",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF047857),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss, enabled = !submitting) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color(0xFF64748B))
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(scrollState)
                        .background(Color(0xFFFAFBFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PlanEditGridRow {
                        PlanEditDateField(
                            label = "生産日",
                            value = form.productionDay,
                            onValueChange = { form = form.copy(productionDay = it) },
                            modifier = Modifier.weight(1f),
                            placeholder = "生産日",
                        )
                        PlanEditDropdownField(
                            label = "ライン",
                            value = form.productionLine,
                            options = lineOptions,
                            onSelect = { form = form.copy(productionLine = it) },
                            modifier = Modifier.weight(1f),
                            emptyLabel = "ラインを選択",
                            includeEmptyOption = true,
                        )
                    }
                    PlanEditGridRow {
                        PlanEditDropdownField(
                            label = "面取機",
                            value = form.chamferingMachine,
                            options = chamferingMachines,
                            onSelect = { form = form.copy(chamferingMachine = it) },
                            modifier = Modifier.weight(1f),
                            emptyLabel = "面取機を選択",
                            includeEmptyOption = false,
                        )
                        PlanEditField(
                            label = "生産順",
                            value = form.productionSequence,
                            onValueChange = { form = form.copy(productionSequence = it) },
                            modifier = Modifier.weight(1f),
                            placeholder = "省略時は自動",
                            keyboardType = KeyboardType.Number,
                        )
                    }
                    PlanEditDropdownField(
                        label = "製品CD",
                        value = form.productCd,
                        options = productDropdownOptions,
                        onSelect = { cd ->
                            val name = productOptions.firstOrNull { it.first == cd }?.second.orEmpty()
                            form = form.copy(productCd = cd, productName = name)
                        },
                        emptyLabel = "製品を選択（面取工程）",
                        includeEmptyOption = true,
                    )
                    PlanEditField(
                        label = "製品名",
                        value = form.productName,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "製品CD選択で自動入力",
                    )
                    PlanEditGridRow {
                        PlanEditField(
                            label = "生産数",
                            value = form.actualProductionQuantity,
                            onValueChange = { form = form.copy(actualProductionQuantity = it) },
                            modifier = Modifier.weight(1f),
                            placeholder = "生産数",
                            keyboardType = KeyboardType.Number,
                        )
                        PlanEditDropdownField(
                            label = "原材料",
                            value = form.materialName,
                            options = materialDropdownOptions,
                            onSelect = { form = form.copy(materialName = it) },
                            modifier = Modifier.weight(1f),
                            emptyLabel = "原材料を選択",
                            includeEmptyOption = true,
                        )
                    }
                    PlanEditField(
                        label = "管理コード",
                        value = managementCode,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "自動生成",
                    )
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !submitting,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Text("取消", fontSize = 12.sp, color = Color(0xFF606266))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(form) },
                        enabled = !submitting,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CuttingInstructionTheme.ChamferMgmtBtnPrimaryBg),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("登録", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

