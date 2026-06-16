package com.example.smart_emap.ui.mes.inspectionregistration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.data.model.UserDto

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
        val inProgress = InspectionManualRegistrationLogic.isRowMesInProgress(row)
        val ds = InspectionManualRegistrationLogic.resolveDataSource(row)
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("削除確認") },
            text = {
                Text(
                    buildString {
                        append("この登録を削除しますか？")
                        if (inProgress) append("\n\n※ MES生産中の行です。")
                        if (ds == "excel" || ds == "csv") append("\n※ ${InspectionManualRegistrationLogic.dataSourceLabel(ds)}取込データです。")
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteRow) {
                    Text("削除", color = ImrTheme.Rose)
                }
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
        containerColor = ImrTheme.PageBg,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
        InspectionManualRegistrationBody(
            uiState = uiState,
            canSave = canSave,
            canEdit = canEdit,
            canDelete = canDelete,
            onPrevDay = { viewModel.shiftProductionDay(-1) },
            onNextDay = { viewModel.shiftProductionDay(1) },
            onToday = viewModel::goProductionDayToday,
            onRefresh = viewModel::refreshAll,
            onInspectorSelected = viewModel::setInspectorUserId,
            onProductSelected = viewModel::onProductSelected,
            onBoxQty = viewModel::onBoxQtyInput,
            onPieceQty = viewModel::onPieceQtyInput,
            onStartedAt = viewModel::onStartedAtInput,
            onEndedAt = viewModel::onEndedAtInput,
            onStartedBlur = viewModel::onStartedAtBlur,
            onEndedBlur = viewModel::onEndedAtBlur,
            onBreakMin = viewModel::onBreakMinInput,
            onStopMin = viewModel::onStopMinInput,
            onNote = viewModel::setRegistrationNote,
            onBumpDefect = viewModel::bumpDefect,
            onDefectQty = viewModel::onDefectQtyInput,
            onSave = { viewModel.submitForm(canSave) },
            onClear = { viewModel.resetForm() },
            onInspectorFilter = viewModel::setInspectorFilterId,
            onListPagePrev = viewModel::prevListPage,
            onListPageNext = viewModel::nextListPage,
            onEditRow = viewModel::loadRowIntoForm,
            onDeleteRow = viewModel::requestDeleteRow,
            inspectorLabel = viewModel::inspectorLabel,
            canEditRow = viewModel::canEditRow,
        )
        }
    }
}
