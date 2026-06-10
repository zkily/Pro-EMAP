package com.example.smart_emap.ui.master.productprocessroute

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterProductRouteInfoDto

val productRoutePageBackground = Brush.linearGradient(
    listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
)
private val productRouteHeaderGradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF8B5CF6)))
private val productListHeaderGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val productInfoHeaderGradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF8B5CF6)))

@Composable
fun ProductRoutePageHeader(selectedProductCd: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("製品ルートマスタ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text("製品を選択し、工程ステップ・設備を設定します", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }
            if (!selectedProductCd.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF312E81),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(selectedProductCd, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRouteProductListPanel(
    keyword: String,
    products: List<Pair<String, String>>,
    selectedProductCd: String,
    loading: Boolean,
    page: Int,
    total: Int,
    pageSize: Int,
    onKeywordChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxPage = maxOf(1, (total + pageSize - 1) / pageSize)
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(productListHeaderGradient)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("製品一覧", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            BasicTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(32.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 11.sp),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        Box(Modifier.weight(1f).padding(start = 6.dp)) {
                            if (keyword.isEmpty()) Text("製品CD・名称", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            inner()
                        }
                    }
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(productListHeaderGradient)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text("製品CD", modifier = Modifier.width(72.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("製品名", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (loading && products.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF667EEA))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(products, key = { _, item -> item.first }) { _, (cd, name) ->
                            val selected = cd == selectedProductCd
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(cd) }
                                    .background(if (selected) Color(0xFFEEF2FF) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    cd,
                                    modifier = Modifier.width(72.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4F46E5),
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(name, modifier = Modifier.weight(1f), fontSize = 10.sp, color = Color(0xFF334155), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { if (page > 1) onPageChange(page - 1) }, enabled = page > 1, contentPadding = PaddingValues(0.dp)) {
                    Text("‹", fontSize = 14.sp)
                }
                Text("$page / $maxPage ($total)", fontSize = 9.sp, color = Color(0xFF64748B))
                TextButton(onClick = { if (page < maxPage) onPageChange(page + 1) }, enabled = page < maxPage, contentPadding = PaddingValues(0.dp)) {
                    Text("›", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProductRouteProductInfoPanel(info: MasterProductRouteInfoDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(productInfoHeaderGradient).padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("製品情報", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ProductInfoRow("製品CD", info.productCd.orEmpty(), mono = true)
                ProductInfoRow("製品名", info.productName.orEmpty())
                ProductInfoRow("工程ルートCD", info.routeCd.orEmpty(), mono = true)
                ProductInfoRow("工程ルート名", info.routeName.orEmpty())
                ProductInfoRow("納入先", info.deliveryDestinationName.orEmpty().ifBlank { "—" })
            }
        }
    }
}

@Composable
private fun ProductInfoRow(label: String, value: String, mono: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(88.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        Text(
            value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            color = if (mono) Color(0xFF4F46E5) else Color(0xFF0F172A),
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (mono) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
fun ProductRouteEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
            Text("製品を選択してください", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF475569))
            Text("左の一覧から製品を選ぶと、工程ルートを編集できます", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductRouteStepsPanel(
    steps: List<ProductRouteStepUi>,
    loading: Boolean,
    dataLoaded: Boolean,
    actionLoading: Boolean,
    machinesForProcess: (String) -> List<MasterMachineFullDto>,
    onAddProcess: () -> Unit,
    onReset: () -> Unit,
    onSaveSteps: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onRemoveStep: (Int) -> Unit,
    onAddMachine: (String) -> Unit,
    onMachineCdChange: (String, String, String) -> Unit,
    onProcessTimeChange: (String, String, Int) -> Unit,
    onSetupTimeChange: (String, String, Int) -> Unit,
    onSaveMachine: (String, String) -> Unit,
    onRemoveMachine: (String, Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFFFAFBFC), Color(0xFFF1F5F9))))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                    }
                    Text("製品別工程ステップ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProductRouteActionChip("工程追加", Color(0xFF16A34A), Icons.Default.Add, enabled = !actionLoading, onClick = onAddProcess)
                    ProductRouteActionChip("リセット", Color(0xFF64748B), Icons.Default.Refresh, enabled = !actionLoading, onClick = onReset)
                    ProductRouteActionChip("保存", Color(0xFF2563EB), Icons.Default.Check, enabled = !actionLoading && steps.isNotEmpty(), onClick = onSaveSteps)
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            when {
                loading && !dataLoaded -> {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1), strokeWidth = 2.dp)
                    }
                }
                dataLoaded && steps.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("工程ルート未設定、またはステップがありません", fontSize = 12.sp, color = Color(0xFF64748B))
                        ProductRouteActionChip("工程を追加", Color(0xFF2563EB), Icons.Default.Add, onClick = onAddProcess)
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        steps.forEachIndexed { index, step ->
                            ProductRouteStepCard(
                                step = step,
                                index = index,
                                stepCount = steps.size,
                                actionLoading = actionLoading,
                                machinesForProcess = machinesForProcess,
                                onMoveUp = { onMoveUp(index) },
                                onMoveDown = { onMoveDown(index) },
                                onRemove = { onRemoveStep(index) },
                                onAddMachine = { onAddMachine(step.localId) },
                                onMachineCdChange = { mId, cd -> onMachineCdChange(step.localId, mId, cd) },
                                onProcessTimeChange = { mId, v -> onProcessTimeChange(step.localId, mId, v) },
                                onSetupTimeChange = { mId, v -> onSetupTimeChange(step.localId, mId, v) },
                                onSaveMachine = { mId -> onSaveMachine(step.localId, mId) },
                                onRemoveMachine = { mIdx -> onRemoveMachine(step.localId, mIdx) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRouteStepCard(
    step: ProductRouteStepUi,
    index: Int,
    stepCount: Int,
    actionLoading: Boolean,
    machinesForProcess: (String) -> List<MasterMachineFullDto>,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onAddMachine: () -> Unit,
    onMachineCdChange: (String, String) -> Unit,
    onProcessTimeChange: (String, Int) -> Unit,
    onSetupTimeChange: (String, Int) -> Unit,
    onSaveMachine: (String) -> Unit,
    onRemoveMachine: (Int) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Icon(Icons.Default.DragHandle, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(28.dp).padding(4.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF)) {
                            Text("順序 ${step.stepNo}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                        }
                        Text(step.processCd, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontFamily = FontFamily.Monospace)
                        Text(step.processName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Surface(shape = RoundedCornerShape(4.dp), color = if (step.id != null) Color(0xFFECFDF5) else Color(0xFFFFFBEB)) {
                            Text(
                                if (step.id != null) "保存済み" else "未保存",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = if (step.id != null) Color(0xFF059669) else Color(0xFFD97706),
                            )
                        }
                    }
                }
                IconButton(onClick = onMoveUp, enabled = index > 0 && !actionLoading, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = index < stepCount - 1 && !actionLoading, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                TextButton(onClick = onRemove, enabled = !actionLoading, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Text("削除", fontSize = 10.sp, color = Color(0xFFEF4444))
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                        Text("設備一覧", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        if (step.machines.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF2FF)) {
                                Text("${step.machines.size}台", modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 9.sp, color = Color(0xFF4F46E5))
                            }
                        }
                    }
                    TextButton(onClick = onAddMachine, enabled = !actionLoading, contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2563EB))
                        Text("設備追加", fontSize = 10.sp, color = Color(0xFF2563EB))
                    }
                }
                if (step.machines.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("設備が未設定です", fontSize = 11.sp, color = Color(0xFF64748B))
                        TextButton(onClick = onAddMachine) { Text("設備を追加", fontSize = 10.sp) }
                    }
                } else {
                    Column(modifier = Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        step.machines.forEachIndexed { mIndex, machine ->
                            ProductRouteMachineCard(
                                machine = machine,
                                options = machinesForProcess(step.processName),
                                actionLoading = actionLoading,
                                onMachineCdChange = { onMachineCdChange(machine.localId, it) },
                                onProcessTimeChange = { onProcessTimeChange(machine.localId, it) },
                                onSetupTimeChange = { onSetupTimeChange(machine.localId, it) },
                                onSave = { onSaveMachine(machine.localId) },
                                onRemove = { onRemoveMachine(mIndex) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductRouteMachineCard(
    machine: ProductRouteMachineUi,
    options: List<MasterMachineFullDto>,
    actionLoading: Boolean,
    onMachineCdChange: (String) -> Unit,
    onProcessTimeChange: (Int) -> Unit,
    onSetupTimeChange: (Int) -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = if (machine.id != null) Color(0x7322C55E) else Color(0x73F59E0B)
    val bg = if (machine.id != null) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(shape = RoundedCornerShape(4.dp), color = if (machine.id != null) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)) {
                Text(
                    if (machine.id != null) "保存済み" else "新規",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    color = if (machine.id != null) Color(0xFF059669) else Color(0xFFD97706),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("設備CD", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth().height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            onClick = { expanded = true },
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    machine.machineCd.ifBlank { "選択" },
                                    modifier = Modifier.weight(1f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            }
                        }
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            options.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text("${opt.machineCd} - ${opt.machineName}", fontSize = 11.sp) },
                                    onClick = {
                                        onMachineCdChange(opt.machineCd.orEmpty())
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("設備名", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Text(machine.machineName.ifBlank { "—" }, modifier = Modifier.fillMaxWidth().height(32.dp).padding(top = 6.dp), fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ProductRouteNumberField("加工時間(秒)", machine.processTimeSec, onProcessTimeChange, Modifier.weight(1f))
                ProductRouteNumberField("段取り時間(分)", machine.setupTime, onSetupTimeChange, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (machine.machineCd.isNotBlank()) {
                    ProductRouteActionChip(
                        if (machine.id != null) "更新" else "保存",
                        Color(0xFF16A34A),
                        Icons.Default.Check,
                        enabled = !actionLoading,
                        onClick = onSave,
                    )
                    Spacer(Modifier.width(4.dp))
                }
                ProductRouteActionChip("削除", Color(0xFFEF4444), Icons.Default.Delete, enabled = !actionLoading, onClick = onRemove)
            }
        }
    }
}

@Composable
private fun ProductRouteNumberField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        BasicTextField(
            value = value.toString(),
            onValueChange = { onChange(it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(32.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 11.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun ProductRouteActionChip(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = if (enabled) 1f else 0.4f),
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 3.dp))
        }
    }
}
