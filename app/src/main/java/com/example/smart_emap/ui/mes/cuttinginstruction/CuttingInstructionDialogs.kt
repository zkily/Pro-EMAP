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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import com.example.smart_emap.data.model.CuttingInstructionNoteDto

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

            onAdd = viewModel::createNote,

            onToggleDone = viewModel::toggleNoteDone,

            onDelete = viewModel::deleteNote,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.NewPlan -> PlanFormDialog(

            title = if (dialog.isTrial) "試作追加" else "新規追加",

            initial = dialog.initial,

            machineOptions = uiState.machineOptions,

            materialOptions = uiState.materialMasterOptions,

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

        is CuttingInstructionDialog.SplitCutting -> SplitQuantityDialog(

            title = "翌日へ分割（切断）",

            maxQty = dialog.row.actualProductionQuantity ?: 0,

            onConfirm = { qty -> viewModel.splitCutting(dialog.row, qty) },

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditChamfering -> ChamferingEditDialog(

            row = dialog.row,

            machines = uiState.chamferingMachineOptions,

            onSave = viewModel::saveChamferingEdit,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.NewChamferingInstruction -> NewChamferingInstructionDialog(

            initial = dialog.initial,

            machines = uiState.chamferingMachineOptions,

            productOptions = uiState.chamferingProductOptions,

            materialOptions = uiState.chamferingMaterialOptions,

            onSave = viewModel::saveNewChamferingInstruction,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.SplitChamfering -> SplitQuantityDialog(

            title = "翌日へ分割（面取）",

            maxQty = dialog.row.actualProductionQuantity ?: 0,

            onConfirm = { qty -> viewModel.splitChamfering(dialog.row, qty) },

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.NewChamferingPlan -> ChamferingPlanFormDialog(

            title = "面取ロット新規追加",

            initial = dialog.initial,

            machines = uiState.chamferingMachineOptions,

            onSave = viewModel::saveNewChamferingPlan,

            onDismiss = viewModel::dismissDialog,

        )

        is CuttingInstructionDialog.EditChamferingPlan -> ChamferingPlanFormDialog(

            title = "面取ロット編集",

            initial = dialog.initial,

            machines = uiState.chamferingMachineOptions,

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

        CuttingInstructionDialog.ConfirmCuttingActual -> ConfirmDialog(

            title = "切断実績確定",

            message = "${uiState.cuttingDateToday} の切断実績を確定します。よろしいですか？",

            onConfirm = viewModel::confirmCuttingActualAction,

            onDismiss = viewModel::dismissDialog,

        )

        CuttingInstructionDialog.ConfirmChamferingActual -> ConfirmDialog(

            title = "面取実績確定",

            message = "${uiState.chamferingDateToday} の面取実績を確定します。よろしいですか？",

            onConfirm = viewModel::confirmChamferingActualAction,

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

    AlertDialog(

        onDismissRequest = onDismiss,

        title = { Text("実績確定 結果") },

        text = {

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                    Text("登録件数", fontSize = 13.sp)

                    Text("$inserted 件", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                    Text("数量合計（生産数）", fontSize = 13.sp)

                    Text("$totalQty 本", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CuttingInstructionTheme.CuttingTitle)

                }

            }

        },

        confirmButton = { Button(onClick = onDismiss) { Text("閉じる") } },

    )

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

    onAdd: (String) -> Unit,

    onToggleDone: (Int, Boolean) -> Unit,

    onDelete: (Int) -> Unit,

    onDismiss: () -> Unit,

) {

    var newContent by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp).heightIn(max = 480.dp)) {

            Text("メモ（TODO）", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                OutlinedTextField(newContent, { newContent = it }, modifier = Modifier.weight(1f), label = { Text("新規メモ") })

                IconButton(onClick = { if (newContent.isNotBlank()) { onAdd(newContent); newContent = "" } }) {

                    Icon(Icons.Default.Add, "追加")

                }

            }

            if (loading) CircularProgressIndicator()

            else LazyColumn {

                items(notes, key = { it.id ?: 0 }) { note ->

                    val id = note.id ?: return@items

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(onClick = { onToggleDone(id, note.isDone != true) }) {

                            Icon(Icons.Default.Check, null, tint = if (note.isDone == true) CuttingInstructionTheme.ChamferingAccent else CuttingInstructionTheme.Subtitle)

                        }

                        Text(

                            note.content.orEmpty(),

                            fontSize = 12.sp,

                            modifier = Modifier.weight(1f),

                            color = if (note.isDone == true) CuttingInstructionTheme.Subtitle else CuttingInstructionTheme.Title,

                        )

                        IconButton(onClick = { onDelete(id) }) { Icon(Icons.Default.Delete, null) }

                    }

                }

            }

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("閉じる") }

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

@Composable
private fun PlanFormDialog(
    title: String,
    subtitle: String = "",
    initial: PlanFormState,
    machineOptions: List<Pair<String, String>>,
    materialOptions: List<Pair<String, String>>,
    lockBasicFields: Boolean = false,
    onSave: (PlanFormState) -> Unit,
    onDismiss: () -> Unit,
) {
    var form by remember { mutableStateOf(initial) }
    val scrollState = rememberScrollState()
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
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) CuttingInstructionTheme.CuttingBtnIssueSolid else Color(0xFFCBD5E1),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) CuttingInstructionTheme.CuttingBtnIssueSolid else Color(0xFFF8FAFC),
            contentColor = if (selected) Color.White else Color(0xFF475569),
        ),
    ) {
        Text(name, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CuttingEditDialog(

    row: InstructionCuttingRowDto,

    machines: List<Pair<String, String>>,

    onSave: (InstructionCuttingRowDto, String, String, Int?, Int?, String?) -> Unit,

    onDismiss: () -> Unit,

) {

    var day by remember { mutableStateOf(row.productionDay?.take(10).orEmpty()) }

    var machine by remember { mutableStateOf(row.cuttingMachine.orEmpty()) }

    var qty by remember { mutableStateOf(row.actualProductionQuantity?.toString().orEmpty()) }

    var defect by remember { mutableStateOf(row.defectQty?.toString().orEmpty()) }

    var remarks by remember { mutableStateOf(row.remarks.orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp)) {

            Text("切断指示編集", fontWeight = FontWeight.Bold)

            OutlinedTextField(day, { day = it }, label = { Text("生産日") }, modifier = Modifier.fillMaxWidth())

            InstructionFilterDropdown("切断機", machine, machines, { machine = it }, Modifier.fillMaxWidth(), includeAll = false)

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



@Composable

private fun SplitQuantityDialog(title: String, maxQty: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {

    var qty by remember { mutableStateOf("") }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = { Text(title) },

        text = {

            Column {

                Text("今日の生産数（最大 $maxQty）", fontSize = 12.sp)

                OutlinedTextField(qty, { qty = it }, label = { Text("今日分") })

            }

        },

        confirmButton = { Button(onClick = { qty.toIntOrNull()?.let(onConfirm) }) { Text("分割") } },

        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },

    )

}



data class ChamferingPlanFormState(

    val productionMonth: String = "",

    val productionDay: String = "",

    val productionLine: String = "",

    val productCd: String = "",

    val productName: String = "",

    val actualProductionQuantity: String = "0",

    val productionLotSize: String = "",

    val lotNumber: String = "",

    val materialName: String = "",

    val hasSwProcess: Boolean = false,

)



@Composable

private fun ChamferingPlanFormDialog(

    title: String,

    initial: ChamferingPlanFormState,

    machines: List<Pair<String, String>>,

    onSave: (ChamferingPlanFormState) -> Unit,

    onDismiss: () -> Unit,

) {

    var form by remember { mutableStateOf(initial) }

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {

            Text(title, fontWeight = FontWeight.Bold)

            OutlinedTextField(form.productionMonth, { form = form.copy(productionMonth = it) }, label = { Text("生産月") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(form.productionDay, { form = form.copy(productionDay = it) }, label = { Text("生産日") }, modifier = Modifier.fillMaxWidth())

            InstructionFilterDropdown("ライン", form.productionLine, machines, { form = form.copy(productionLine = it) }, Modifier.fillMaxWidth(), includeAll = false)

            OutlinedTextField(form.productCd, { form = form.copy(productCd = it) }, label = { Text("製品CD") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(form.productName, { form = form.copy(productName = it) }, label = { Text("製品名") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(form.actualProductionQuantity, { form = form.copy(actualProductionQuantity = it) }, label = { Text("生産数") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text("SW", fontSize = 12.sp)

                Switch(form.hasSwProcess, { form = form.copy(hasSwProcess = it) })

            }

            Row {

                Button(onClick = { onSave(form) }) { Text("保存") }

                TextButton(onClick = onDismiss) { Text("キャンセル") }

            }

        }

    }

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

    machines: List<Pair<String, String>>,

    productOptions: List<Pair<String, String>>,

    materialOptions: List<String>,

    onSave: (ChamferingInstructionFormState) -> Unit,

    onDismiss: () -> Unit,

) {

    var form by remember { mutableStateOf(initial) }

    val managementCode = previewChamferingManagementCode(

        form.productionDay,

        form.productionLine,

        form.productCd,

        form.productionSequence,

    )

    Dialog(onDismissRequest = onDismiss) {

        Column(Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {

            Text("面取指示 新規追加", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(

                form.productionDay,

                { form = form.copy(productionDay = it) },

                label = { Text("生産日") },

                modifier = Modifier.fillMaxWidth(),

            )

            OutlinedTextField(

                form.productionLine,

                { form = form.copy(productionLine = it) },

                label = { Text("ライン") },

                modifier = Modifier.fillMaxWidth(),

            )

            InstructionFilterDropdown(

                "面取機",

                form.chamferingMachine,

                machines,

                { form = form.copy(chamferingMachine = it) },

                Modifier.fillMaxWidth(),

                includeAll = false,

            )

            OutlinedTextField(

                form.productionSequence,

                { form = form.copy(productionSequence = it) },

                label = { Text("生産順") },

                modifier = Modifier.fillMaxWidth(),

            )

            InstructionFilterDropdown(

                "製品",

                form.productCd,

                productOptions.map { (cd, name) -> cd to "$name [$cd]" },

                { cd ->

                    val name = productOptions.firstOrNull { it.first == cd }?.second.orEmpty()

                    form = form.copy(productCd = cd, productName = name)

                },

                Modifier.fillMaxWidth(),

                includeAll = false,

            )

            OutlinedTextField(

                form.productName,

                {},

                label = { Text("製品名") },

                modifier = Modifier.fillMaxWidth(),

                readOnly = true,

            )

            OutlinedTextField(

                form.actualProductionQuantity,

                { form = form.copy(actualProductionQuantity = it) },

                label = { Text("生産数") },

                modifier = Modifier.fillMaxWidth(),

            )

            InstructionFilterDropdown(

                "材料",

                form.materialName,

                materialOptions.map { it to it },

                { form = form.copy(materialName = it) },

                Modifier.fillMaxWidth(),

            )

            OutlinedTextField(

                managementCode,

                {},

                label = { Text("管理CD") },

                modifier = Modifier.fillMaxWidth(),

                readOnly = true,

            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(onClick = { onSave(form) }) { Text("登録") }

                TextButton(onClick = onDismiss) { Text("キャンセル") }

            }

        }

    }

}

