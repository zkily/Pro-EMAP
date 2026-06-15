package com.example.smart_emap.ui.mes.inspectionregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.mes.inspection.InspectionManagementRowExt
import java.text.NumberFormat
import java.util.Locale

private object ImrColors {
    val PageBg = Color(0xFFF1F5F9)
    val CardBg = Color.White
    val Teal = Color(0xFF0D9488)
    val TealLight = Color(0xFFCCFBF1)
    val Indigo = Color(0xFF6366F1)
    val Amber = Color(0xFFF59E0B)
    val Rose = Color(0xFFE11D48)
    val Slate600 = Color(0xFF475569)
    val Slate500 = Color(0xFF64748B)
    val Border = Color(0xFFE2E8F0)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectionManualRegistrationScreen(
    viewModel: InspectionManualRegistrationViewModel,
    user: UserDto,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val mesModule = OperationModules.MES
    val canCreate = remember(user.id, user.operationPermissions) { user.canCreate(mesModule) }
    val canEdit = remember(user.id, user.operationPermissions) { user.canEdit(mesModule) }
    val canDelete = remember(user.id, user.operationPermissions) { user.canDelete(mesModule) }
    val canSave = if (uiState.isEdit) canEdit else canCreate

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    uiState.confirmDeleteRow?.let { row ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("削除確認") },
            text = { Text("この登録を削除しますか？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteRow) { Text("削除", color = ImrColors.Rose) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) { Text("キャンセル") }
            },
        )
    }

    if (uiState.confirmQtyMismatch) {
        AlertDialog(
            onDismissRequest = viewModel::dismissQtyMismatch,
            title = { Text("本数・箱数の確認") },
            text = { Text("入数と本数が一致しません。このまま保存しますか？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmQtyMismatchSave) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissQtyMismatch) { Text("キャンセル") }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ImrColors.PageBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "検査実績登録",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ImrColors.Slate600,
            )
            Text(
                "手動で検査実績を登録・修正します",
                fontSize = 11.sp,
                color = ImrColors.Slate500,
            )

            ProductionDayCard(
                productionDay = uiState.productionDay,
                onPrev = { viewModel.shiftProductionDay(-1) },
                onNext = { viewModel.shiftProductionDay(1) },
                onToday = viewModel::goProductionDayToday,
            )

            FormSectionCard(title = "① 検査員", accent = ImrColors.Teal) {
                InspectorDropdown(
                    inspectors = uiState.inspectors,
                    selectedId = uiState.inspectorUserId,
                    enabled = !uiState.isLoadingInspectors,
                    onSelected = viewModel::setInspectorUserId,
                )
            }

            FormSectionCard(title = "② 製品", accent = ImrColors.Indigo) {
                ProductDropdown(
                    products = uiState.products,
                    selectedCd = uiState.productCd,
                    enabled = uiState.inspectorSelected && !uiState.isEdit && !uiState.isLoadingProducts,
                    onSelected = viewModel::onProductSelected,
                )
                if (uiState.productSelected) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QtyField(
                            label = "箱数",
                            value = uiState.boxQtyText,
                            enabled = uiState.unitPerBox > 0,
                            onValueChange = viewModel::onBoxQtyInput,
                            modifier = Modifier.weight(1f),
                        )
                        QtyField(
                            label = "本数",
                            value = uiState.pieceQtyText,
                            enabled = true,
                            onValueChange = viewModel::onPieceQtyInput,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (uiState.unitPerBox > 0) {
                        Text(
                            "入数: ${uiState.unitPerBox}",
                            fontSize = 11.sp,
                            color = ImrColors.Slate500,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            FormSectionCard(title = "③ 生産時間", accent = ImrColors.Amber) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField(
                        label = "開始",
                        value = uiState.startedAtText,
                        onValueChange = viewModel::onStartedAtInput,
                        onBlur = viewModel::onStartedAtBlur,
                        modifier = Modifier.weight(1f),
                    )
                    TimeField(
                        label = "終了",
                        value = uiState.endedAtText,
                        onValueChange = viewModel::onEndedAtInput,
                        onBlur = viewModel::onEndedAtBlur,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinuteStepper(
                        label = "休憩(分)",
                        value = uiState.breakMin,
                        onChange = viewModel::setBreakMin,
                        modifier = Modifier.weight(1f),
                    )
                    MinuteStepper(
                        label = "停止(分)",
                        value = uiState.stopMin,
                        onChange = viewModel::setStopMin,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (uiState.defectGroups.isNotEmpty()) {
                FormSectionCard(title = "④ 不良", accent = ImrColors.Rose) {
                    uiState.defectGroups.forEach { group ->
                        Text(
                            group.processName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ImrColors.Slate600,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            group.items.forEach { item ->
                                DefectChip(
                                    label = item.defectName,
                                    qty = uiState.defects[item.defectCd] ?: 0,
                                    onBump = { viewModel.bumpDefect(item.defectCd, it) },
                                    onQtyInput = { viewModel.onDefectQtyInput(item.defectCd, it) },
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        "不良合計: ${uiState.totalDefects}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ImrColors.Rose,
                    )
                }
            }

            FormSectionCard(title = "⑤ 備考・保存", accent = ImrColors.Teal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.registrationNote,
                        onValueChange = viewModel::setRegistrationNote,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("備考") },
                    )
                    if (canSave) {
                        Button(
                            onClick = { viewModel.submitForm(canSave) },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ImrColors.Teal),
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(if (uiState.isEdit) "更新" else "保存")
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.resetForm() },
                        modifier = Modifier.height(40.dp),
                    ) {
                        Text("クリア")
                    }
                }
            }

            RegistrationListSection(
                rows = uiState.filteredRows,
                inspectors = uiState.inspectors,
                isLoading = uiState.isLoadingRows,
                deletingRowId = uiState.deletingRowId,
                canEdit = canEdit,
                canDelete = canDelete,
                inspectorFilterId = uiState.inspectorFilterId,
                onInspectorFilter = viewModel::setInspectorFilterId,
                onEditRow = viewModel::loadRowIntoForm,
                onDeleteRow = viewModel::requestDeleteRow,
            )
        }
    }
}

@Composable
private fun ProductionDayCard(
    productionDay: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ImrColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "前日")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = ImrColors.Teal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        MesCalendarUtils.formatDateWithWeekday(productionDay),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
                TextButton(onClick = onToday) { Text("今日", fontSize = 11.sp) }
            }
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "翌日")
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ImrColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )
                Spacer(Modifier.width(6.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ImrColors.Slate600)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectorDropdown(
    inspectors: List<UserListItemDto>,
    selectedId: Int?,
    enabled: Boolean,
    onSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = inspectors.find { it.id == selectedId }?.displayLabel().orEmpty().ifBlank { "検査員を選択" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("（未選択）") },
                onClick = { onSelected(null); expanded = false },
            )
            inspectors.forEach { insp ->
                val id = insp.id ?: return@forEach
                DropdownMenuItem(
                    text = { Text(insp.displayLabel()) },
                    onClick = { onSelected(id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDropdown(
    products: List<com.example.smart_emap.data.model.ErpProductDto>,
    selectedCd: String,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = products.find { it.normalizedCode() == selectedCd }
    val label = selected?.let { "${it.normalizedCode()} · ${it.normalizedName()}" }
        ?: if (selectedCd.isNotBlank()) selectedCd else "製品を選択"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            products.forEach { p ->
                val cd = p.normalizedCode()
                DropdownMenuItem(
                    text = {
                        Text(
                            "${cd} · ${p.normalizedName()}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = { onSelected(cd); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun QtyField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onBlur: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("HH:MM") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.onFocusChanged { if (!it.isFocused) onBlur() },
    )
}

@Composable
private fun MinuteStepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = ImrColors.Slate500)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(value - 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }
            Text(
                value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.widthIn(min = 32.dp),
            )
            IconButton(onClick = { onChange(value + 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun DefectChip(
    label: String,
    qty: Int,
    onBump: (Int) -> Unit,
    onQtyInput: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ImrColors.Border, RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 100.dp))
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = { onBump(-1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
        }
        OutlinedTextField(
            value = if (qty > 0) qty.toString() else "",
            onValueChange = onQtyInput,
            modifier = Modifier.width(48.dp).heightIn(max = 48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        IconButton(onClick = { onBump(1) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationListSection(
    rows: List<InspectionManagementRowDto>,
    inspectors: List<UserListItemDto>,
    isLoading: Boolean,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    inspectorFilterId: Int?,
    onInspectorFilter: (Int?) -> Unit,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ImrColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("登録一覧", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            InspectorFilterDropdown(
                inspectors = inspectors,
                selectedId = inspectorFilterId,
                onSelected = onInspectorFilter,
            )
            Spacer(Modifier.height(8.dp))
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ImrColors.Teal, modifier = Modifier.size(28.dp))
                }
            } else if (rows.isEmpty()) {
                Text("データがありません", color = ImrColors.Slate500, fontSize = 12.sp)
            } else {
                val hScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScroll),
                ) {
                    RegistrationTable(
                        rows = rows,
                        inspectors = inspectors,
                        deletingRowId = deletingRowId,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        onEditRow = onEditRow,
                        onDeleteRow = onDeleteRow,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectorFilterDropdown(
    inspectors: List<UserListItemDto>,
    selectedId: Int?,
    onSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedId?.let { id -> inspectors.find { it.id == id }?.displayLabel() }
        ?: "全検査員"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("全検査員") },
                onClick = { onSelected(null); expanded = false },
            )
            inspectors.forEach { insp ->
                val id = insp.id ?: return@forEach
                DropdownMenuItem(
                    text = { Text(insp.displayLabel()) },
                    onClick = { onSelected(id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun RegistrationTable(
    rows: List<InspectionManagementRowDto>,
    inspectors: List<UserListItemDto>,
    deletingRowId: Int?,
    canEdit: Boolean,
    canDelete: Boolean,
    onEditRow: (InspectionManagementRowDto) -> Unit,
    onDeleteRow: (InspectionManagementRowDto) -> Unit,
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale.JAPAN) }
    Column {
        Row(
            modifier = Modifier
                .background(Color(0xFFF1F5F9))
                .padding(vertical = 6.dp, horizontal = 4.dp),
        ) {
            listOf("順", "製品", "本数", "不良", "能率", "取得元", "備考", "").forEach { h ->
                Text(
                    h,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImrColors.Slate500,
                    modifier = Modifier.width(when (h) {
                        "順" -> 28.dp
                        "製品" -> 120.dp
                        "本数", "不良" -> 48.dp
                        "能率", "取得元" -> 52.dp
                        "備考" -> 100.dp
                        else -> 64.dp
                    }),
                )
            }
        }
        HorizontalDivider()
        rows.forEach { row ->
            val canEditRow = canEdit && !InspectionManualRegistrationLogic.isRowMesInProgress(row)
            val efficiency = InspectionManualRegistrationLogic.resolveEfficiencyRate(row)
            val efficiencyOut = InspectionManualRegistrationLogic.isEfficiencyOutOfRange(efficiency)
            val dataSource = InspectionManualRegistrationLogic.dataSourceLabel(
                InspectionManualRegistrationLogic.resolveDataSource(row),
            )
            val defects = InspectionManagementRowExt.historyDefectQty(row)
            val note = row.manualRegistrationNote?.trim().orEmpty().ifBlank { "—" }
            Row(
                modifier = Modifier
                    .clickable(enabled = canEditRow) { onEditRow(row) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    (row.productionSequence ?: "—").toString(),
                    fontSize = 11.sp,
                    modifier = Modifier.width(28.dp),
                )
                Text(
                    row.productName?.trim().orEmpty().ifBlank { row.productCd.orEmpty() },
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(120.dp),
                )
                Text(
                    row.actualProductionQuantity?.let { nf.format(it) } ?: "—",
                    fontSize = 11.sp,
                    modifier = Modifier.width(48.dp),
                )
                Text(defects.toString(), fontSize = 11.sp, modifier = Modifier.width(48.dp))
                Text(
                    efficiency?.toString() ?: "—",
                    fontSize = 11.sp,
                    color = if (efficiencyOut) ImrColors.Rose else Color.Unspecified,
                    fontWeight = if (efficiencyOut) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(52.dp),
                )
                Text(dataSource, fontSize = 11.sp, modifier = Modifier.width(52.dp))
                Text(note, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(100.dp))
                Row(modifier = Modifier.width(64.dp)) {
                    if (canEditRow) {
                        IconButton(onClick = { onEditRow(row) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = ImrColors.Indigo)
                        }
                    }
                    if (canDelete) {
                        val deleting = deletingRowId == row.id
                        IconButton(
                            onClick = { onDeleteRow(row) },
                            enabled = !deleting,
                            modifier = Modifier.size(28.dp),
                        ) {
                            if (deleting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ImrColors.Rose)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = ImrColors.Border)
        }
    }
}
