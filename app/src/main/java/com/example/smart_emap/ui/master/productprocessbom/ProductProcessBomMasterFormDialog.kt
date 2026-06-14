package com.example.smart_emap.ui.master.productprocessbom

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProductProcessBomFormDialog(
    row: ProductProcessBomUiRow,
    loading: Boolean,
    onUpdate: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("基本情報", "材料・切断", "成型・メッキ", "溶接・検査")
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth(0.94f)
                .shadow(16.dp, RoundedCornerShape(18.dp)),
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(PpbTheme.HeroBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Column {
                            Text("製品工程BOM 編集", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(
                                "${row.productCd} · ${row.productName}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 1,
                            )
                        }
                    }
                }
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = { positions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(positions[tab]),
                                height = 2.dp,
                                color = PpbTheme.Indigo500,
                            )
                        },
                    ) {
                        tabs.forEachIndexed { i, label ->
                            Tab(
                                selected = tab == i,
                                onClick = { tab = i },
                                text = {
                                    Text(
                                        label,
                                        fontSize = 10.sp,
                                        fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (tab == i) PpbTheme.Indigo500 else PpbTheme.Slate500,
                                    )
                                },
                            )
                        }
                    }
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                        label = "form-tab",
                        modifier = Modifier.padding(top = 6.dp),
                    ) { activeTab ->
                        Column(
                            Modifier
                                .height(300.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            when (activeTab) {
                                0 -> BasicTab(row, onUpdate)
                                1 -> MaterialTab(row, onUpdate)
                                2 -> FormingTab(row, onUpdate)
                                3 -> WeldingTab(row, onUpdate)
                            }
                        }
                    }
                    HorizontalDivider(color = PpbTheme.Slate200, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss, enabled = !loading) {
                            Text("キャンセル", fontSize = 12.sp, color = PpbTheme.Slate500)
                        }
                        Spacer(Modifier.padding(2.dp))
                        PpbSaveButton(loading, onConfirm)
                    }
                }
            }
        }
    }
}

@Composable
private fun PpbSaveButton(loading: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90), label = "save-scale")
    Button(
        onClick = onClick,
        enabled = !loading,
        interactionSource = interaction,
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = PpbTheme.Indigo500),
        shape = PpbTheme.ChipShape,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
        } else {
            Text("保存する", fontSize = 12.sp)
        }
    }
}

private val fieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PpbTheme.Indigo500,
    unfocusedBorderColor = PpbTheme.Slate200,
    focusedLabelColor = PpbTheme.Indigo500,
)

@Composable
private fun BasicTab(row: ProductProcessBomUiRow, onUpdate: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit) {
    OutlinedTextField(value = row.productCd.toString(), onValueChange = {}, readOnly = true, label = { Text("製品CD", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
    OutlinedTextField(value = row.productName, onValueChange = {}, readOnly = true, label = { Text("製品名", fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
    LtField("最低在庫日数", row.minStockDays) { v -> onUpdate { it.copy(minStockDays = v) } }
    LtField("安全在庫日数", row.safetyStockDays) { v -> onUpdate { it.copy(safetyStockDays = v) } }
    SwitchRow("終息", row.isDiscontinued) { v -> onUpdate { it.copy(isDiscontinued = v) } }
}

@Composable
private fun MaterialTab(row: ProductProcessBomUiRow, onUpdate: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit) {
    SwitchRow("材料工程", row.materialProcess) { v -> onUpdate { it.copy(materialProcess = v) } }
    if (row.materialProcess) LtField("材料工程LT", row.materialProcessLt) { v -> onUpdate { it.copy(materialProcessLt = v) } }
    SwitchRow("切断工程", row.cutingProcess) { v -> onUpdate { it.copy(cutingProcess = v) } }
    if (row.cutingProcess) LtField("切断工程LT", row.cutingProcessLt) { v -> onUpdate { it.copy(cutingProcessLt = v) } }
    SwitchRow("面取工程", row.chamferingProcess) { v -> onUpdate { it.copy(chamferingProcess = v) } }
    if (row.chamferingProcess) LtField("面取工程LT", row.chamferingProcessLt) { v -> onUpdate { it.copy(chamferingProcessLt = v) } }
    SwitchRow("SW工程", row.swagingProcess) { v -> onUpdate { it.copy(swagingProcess = v) } }
    if (row.swagingProcess) LtField("SW工程LT", row.swagingProcessLt) { v -> onUpdate { it.copy(swagingProcessLt = v) } }
}

@Composable
private fun FormingTab(row: ProductProcessBomUiRow, onUpdate: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit) {
    SwitchRow("成型工程", row.formingProcess) { v -> onUpdate { it.copy(formingProcess = v) } }
    if (row.formingProcess) LtField("成型工程LT", row.formingProcessLt) { v -> onUpdate { it.copy(formingProcessLt = v) } }
    SwitchRow("メッキ工程", row.platingProcess) { v -> onUpdate { it.copy(platingProcess = v) } }
    if (row.platingProcess) LtField("メッキ工程LT", row.platingProcessLt) { v -> onUpdate { it.copy(platingProcessLt = v) } }
    SwitchRow("外注メッキ工程", row.outsourcedPlatingProcess) { v -> onUpdate { it.copy(outsourcedPlatingProcess = v) } }
    if (row.outsourcedPlatingProcess) LtField("外注メッキ工程LT", row.outsourcedPlatingProcessLt) { v -> onUpdate { it.copy(outsourcedPlatingProcessLt = v) } }
}

@Composable
private fun WeldingTab(row: ProductProcessBomUiRow, onUpdate: ((ProductProcessBomUiRow) -> ProductProcessBomUiRow) -> Unit) {
    SwitchRow("溶接工程", row.weldingProcess) { v -> onUpdate { it.copy(weldingProcess = v) } }
    if (row.weldingProcess) LtField("溶接工程LT", row.weldingProcessLt) { v -> onUpdate { it.copy(weldingProcessLt = v) } }
    SwitchRow("外注溶接工程", row.outsourcedWeldingProcess) { v -> onUpdate { it.copy(outsourcedWeldingProcess = v) } }
    if (row.outsourcedWeldingProcess) LtField("外注溶接工程LT", row.outsourcedWeldingProcessLt) { v -> onUpdate { it.copy(outsourcedWeldingProcessLt = v) } }
    SwitchRow("検査工程", row.inspectionProcess) { v -> onUpdate { it.copy(inspectionProcess = v) } }
    if (row.inspectionProcess) LtField("検査工程LT", row.inspectionProcessLt) { v -> onUpdate { it.copy(inspectionProcessLt = v) } }
    SwitchRow("外注倉庫工程", row.outsourcedWarehouseProcess) { v -> onUpdate { it.copy(outsourcedWarehouseProcess = v) } }
    if (row.outsourcedWarehouseProcess) LtField("外注倉庫工程LT", row.outsourcedWarehouseProcessLt) { v -> onUpdate { it.copy(outsourcedWarehouseProcessLt = v) } }
    SwitchRow("メッキ前溶接", row.prePlatingWelding) { v -> onUpdate { it.copy(prePlatingWelding = v) } }
    SwitchRow("検査後溶接", row.postInspectionWelding) { v -> onUpdate { it.copy(postInspectionWelding = v) } }
    if (row.postInspectionWelding) LtField("検査後溶接工程LT", row.postInspectionWeldingLt) { v -> onUpdate { it.copy(postInspectionWeldingLt = v) } }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        shape = PpbTheme.ChipShape,
        color = PpbTheme.Slate50,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, PpbTheme.Slate200),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 12.sp, color = PpbTheme.Slate900)
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PpbTheme.Indigo500),
            )
        }
    }
}

@Composable
private fun LtField(label: String, value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onChange(ProductProcessBomMasterLogic.parseLt(it)) },
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = fieldColors,
    )
}
