package com.example.smart_emap.ui.mes.planinstruction

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog

@Composable
fun PlanInstructionDialogHost(
    dialog: PlanInstructionDialog,
    uiState: PlanInstructionUiState,
    config: PlanInstructionConfig,
    viewModel: PlanInstructionViewModel,
) {
    when (dialog) {
        PlanInstructionDialog.None -> Unit
        PlanInstructionDialog.Notes -> PlanInstructionNotesDialog(
            notes = uiState.notes,
            loading = uiState.notesLoading,
            saving = uiState.notesSaving,
            onAdd = viewModel::createNote,
            onToggleDone = viewModel::toggleNoteDone,
            onDelete = viewModel::deleteNote,
            onDismiss = viewModel::closeDialog,
        )
        PlanInstructionDialog.SetupPreview -> SetupSchedulePreviewDialog(
            meta = uiState.setupPreviewMeta,
            rows = uiState.setupPreviewRows,
            onPrint = { editedRows ->
                viewModel.printFromSetupPreview(editedRows)
                viewModel.closeDialog()
            },
            onDismiss = viewModel::closeDialog,
        )
        PlanInstructionDialog.EfficiencyUpdate -> EfficiencyUpdateDialog(
            startDate = uiState.efficiencyUpdateStartDate,
            loading = uiState.actionLoading,
            onStartDateChange = viewModel::setEfficiencyUpdateStartDate,
            onConfirm = viewModel::updateEfficiency,
            onDismiss = viewModel::closeDialog,
        )
        PlanInstructionDialog.WorkTimeConfig -> WorkTimeConfigDialog(
            machines = uiState.machines,
            configs = uiState.workTimeConfigs,
            loading = uiState.actionLoading,
            onCreate = viewModel::createWorkTimeConfig,
            onDelete = viewModel::deleteWorkTimeConfig,
            onDismiss = viewModel::closeDialog,
        )
        PlanInstructionDialog.PrintPreview -> PrintPreviewDialog(
            html = uiState.printPreviewHtml.orEmpty(),
            onPrint = {
                viewModel.printInstructions()
                viewModel.closeDialog()
            },
            onDismiss = viewModel::closeDialog,
        )
    }
}

@Composable
private fun EfficiencyUpdateDialog(
    startDate: String,
    loading: Boolean,
    onStartDateChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("能率・段取時間更新", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("開始日以降のデータを更新します。", fontSize = 12.sp, color = PlanInstructionTheme.Subtitle)
                Surface(onClick = { showPicker = true }, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.FilterBorder)) {
                    Text("開始日: $startDate", modifier = Modifier.padding(10.dp), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !loading) {
                if (loading) CircularProgressIndicator(modifier = Modifier.height(16.dp))
                else Text("更新実行")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
    )
    if (showPicker) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = Color(0xFF2563EB),
            onDismiss = { showPicker = false },
            onConfirm = onStartDateChange,
        )
    }
}

@Composable
private fun WorkTimeConfigDialog(
    machines: List<com.example.smart_emap.data.model.MasterMachineFullDto>,
    configs: List<com.example.smart_emap.data.model.MachineWorkTimeConfigDto>,
    loading: Boolean,
    onCreate: (String, String, String) -> Unit,
    onDelete: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var machineCd by remember { mutableStateOf("") }
    var machineName by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("17-19") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("設備運行時間設定", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = machineCd, onValueChange = { machineCd = it }, label = { Text("設備コード") }, singleLine = true)
                OutlinedTextField(value = machineName, onValueChange = { machineName = it }, label = { Text("設備名") }, singleLine = true)
                OutlinedTextField(value = timeSlot, onValueChange = { timeSlot = it }, label = { Text("時間帯") }, singleLine = true)
                configs.take(5).forEach { cfg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${cfg.machineName} ${cfg.timeSlot}", modifier = Modifier.weight(1f), fontSize = 12.sp)
                        cfg.id?.let { id ->
                            IconButton(onClick = { onDelete(id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(machineCd, machineName, timeSlot) },
                enabled = !loading && machineCd.isNotBlank() && machineName.isNotBlank(),
            ) { Text("追加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("閉じる") } },
    )
}

@Composable
private fun PrintPreviewDialog(
    html: String,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).height(520.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onPrint) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text("印刷実行")
                    }
                    TextButton(onClick = onDismiss) { Text("閉じる") }
                }
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.defaultTextEncodingName = "UTF-8"
                            loadDataWithBaseURL(null, html, "text/html; charset=UTF-8", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }
}
