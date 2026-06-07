package com.example.smart_emap.ui.master

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleCard
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleItem
import com.example.smart_emap.ui.erp.purchase.PurchaseSectionTitle
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MasterHomeScreen(onNavigate: (String) -> Unit) {
    MasterPageScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MasterHeroBar(
                title = "マスタホーム",
                subtitle = "Master Data Management",
                icon = Icons.Default.Folder,
                stats = listOf("モジュール数" to MasterPageRegistry.homeModules.size.toString()),
            )
            PurchaseSectionTitle("マスタ一覧")
            MasterPageRegistry.homeModules.forEach { module ->
                PurchaseModuleCard(
                    PurchaseModuleItem(
                        path = module.path,
                        title = module.title,
                        description = module.description,
                        icon = module.icon,
                        gradientStart = module.gradientStart,
                        gradientEnd = module.gradientEnd,
                    ),
                ) { onNavigate(module.path) }
            }
            PurchaseSectionTitle("クイックアクション")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("/master/product" to "製品追加", "/master/material" to "材料追加", "/master/supplier" to "仕入先追加").forEach { (path, label) ->
                    Button(
                        onClick = { onNavigate(path) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    ) { Text(label, fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
fun MasterScreen(path: String, viewModel: MasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(path) { viewModel.setPath(path) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        MasterPageScaffold {
            when (MasterPageRegistry.kindForPath(path)) {
                MasterPageKind.ProductProcessRoute -> MasterProductRouteScreen(uiState, viewModel, Modifier.padding(padding))
                MasterPageKind.DestinationHoliday -> MasterDestinationHolidayScreen(uiState, viewModel, Modifier.padding(padding))
                else -> MasterListScreen(path, uiState, viewModel, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun MasterListScreen(path: String, uiState: MasterListUiState, viewModel: MasterViewModel, modifier: Modifier = Modifier) {
    val def = MasterPageRegistry.pageForPath(path) ?: return
    val scroll = rememberScrollState()
    val secondaryOptions = when {
        def.kind == MasterPageKind.ProcessingFee && uiState.processOptions.isNotEmpty() ->
            listOf("" to "全工程") + uiState.processOptions.map { it.first to "${it.first} ${it.second.take(8)}" }
        else -> def.secondaryFilterOptions
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MasterHeroBar(
            title = def.title,
            subtitle = def.subtitle,
            icon = Icons.Default.Folder,
            stats = listOf(
                def.statsLabels.getOrElse(0) { "総件数" } to uiState.totalCount.toString(),
                def.statsLabels.getOrElse(1) { "表示件数" } to uiState.rows.size.toString(),
            ),
        )
        MasterFilterBar(
            keyword = uiState.keyword,
            onKeywordChange = viewModel::setKeyword,
            onSearch = viewModel::refreshAll,
            onReset = viewModel::resetFilters,
            onAdd = if (def.supportsCreate) viewModel::openCreateForm else null,
            secondaryLabel = def.secondaryFilterLabel,
            secondaryValue = uiState.secondaryFilter,
            secondaryOptions = secondaryOptions,
            onSecondaryChange = viewModel::setSecondaryFilter,
            loading = uiState.isLoading,
        )
        MasterLoadingBox(uiState.isLoading && uiState.rows.isEmpty())
        MasterDataTable(
            columns = def.columns,
            columnWidths = def.columnWidths,
            rows = uiState.rows,
            onEdit = { row ->
                if (def.kind == MasterPageKind.ProcessRoute) {
                    viewModel.openRouteSteps(row.cells.firstOrNull().orEmpty())
                } else {
                    viewModel.openEditForm(row)
                }
            },
            onDelete = viewModel::deleteRow,
        )
    }

    if (uiState.showForm) {
        MasterFormDialog(
            title = if (uiState.editingId == null) "新規追加" else "編集",
            fields = def.formFields,
            values = uiState.formValues,
            onValueChange = viewModel::setFormValue,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
            loading = uiState.actionLoading,
        )
    }

    uiState.routeStepsDialogRouteCd?.let { routeCd ->
        RouteStepsDialog(routeCd, uiState.routeStepsList, onDismiss = viewModel::closeRouteSteps)
    }
}

@Composable
private fun TextButtonLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color(0xFF6366F1),
        fontSize = 11.sp,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp),
    )
}

@Composable
private fun RouteStepsDialog(
    routeCd: String,
    steps: List<com.example.smart_emap.data.model.MasterRouteStepDto>,
    onDismiss: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.92f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("工程ステップ: $routeCd", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                if (steps.isEmpty()) {
                    Text("ステップなし", color = Color(0xFF94A3B8))
                } else {
                    steps.forEach { step ->
                        Text(
                            "${step.stepNo}. ${step.processCd} ${step.processName.orEmpty()}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("閉じる") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasterProductRouteScreen(uiState: MasterListUiState, viewModel: MasterViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MasterHeroBar(
            title = "製品ルートマスタ",
            subtitle = "Product Route Step Manager",
            icon = Icons.Default.Timeline,
            stats = listOf(
                "製品数" to uiState.productOptions.size.toString(),
                "選択中" to (uiState.selectedProductCd.ifBlank { "—" }),
            ),
        )
        OutlinedTextField(
            value = uiState.productKeyword,
            onValueChange = viewModel::setProductKeyword,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("製品CD・名称で検索") },
            singleLine = true,
        )
        Text("製品一覧", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF475569))
        uiState.productOptions.take(50).forEach { (cd, name) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectProduct(cd) },
                colors = CardDefaults.cardColors(
                    containerColor = if (cd == uiState.selectedProductCd) Color(0xFFEEF2FF) else Color.White,
                ),
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(cd, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    Text(name, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }
        }
        uiState.routeInfo?.let { info ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("製品詳細", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${info.productCd} ${info.productName}", fontSize = 12.sp)
                    Text("ルート: ${info.routeCd} ${info.routeName}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("納入先: ${info.deliveryDestinationName}", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
            Text("工程ステップ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (uiState.routeSteps.isEmpty()) {
                Text("ステップなし", color = Color(0xFF94A3B8), modifier = Modifier.padding(8.dp))
            } else {
                uiState.routeSteps.forEach { step ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("${step.stepNo}. ${step.processCd} ${step.processName}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            step.machines.orEmpty().forEach { m ->
                                Text("  設備: ${m.machineCd} ${m.machineName}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        } ?: Text("左の一覧から製品を選択してください", color = Color(0xFF94A3B8), modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasterDestinationHolidayScreen(uiState: MasterListUiState, viewModel: MasterViewModel, modifier: Modifier = Modifier) {
    var destExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MasterHeroBar(
            title = "納入先休日設定",
            subtitle = "Destination Holiday Settings",
            icon = Icons.Default.Folder,
            stats = listOf("休日" to uiState.holidays.size.toString(), "臨時出勤" to uiState.workdays.size.toString()),
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = destExpanded, onExpandedChange = { destExpanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = uiState.destinationOptions.find { it.first == uiState.selectedDestinationCd }?.let { "${it.first}｜${it.second}" }
                        ?: "納入先を選択",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(destExpanded) },
                )
                ExposedDropdownMenu(expanded = destExpanded, onDismissRequest = { destExpanded = false }) {
                    uiState.destinationOptions.forEach { (cd, name) ->
                        DropdownMenuItem(
                            text = { Text("$cd｜$name", fontSize = 12.sp) },
                            onClick = { viewModel.setDestination(cd); destExpanded = false },
                        )
                    }
                }
            }
            Button(onClick = viewModel::loadHolidayData, enabled = uiState.selectedDestinationCd.isNotBlank()) {
                Text("読込", fontSize = 12.sp)
            }
        }

        HolidayCard(
            title = "休日一覧",
            accent = Color(0xFFEF4444),
            addLabel = "休日追加",
            dateValue = uiState.newHolidayDate,
            onDateChange = viewModel::setNewHolidayDate,
            onAdd = viewModel::addHoliday,
            enabled = uiState.selectedDestinationCd.isNotBlank(),
        ) {
            uiState.holidays.forEach { h ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(h.holidayDate.orEmpty(), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteHolidayItem(h.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFFEF4444))
                    }
                }
            }
        }

        HolidayCard(
            title = "臨時出勤日一覧",
            accent = Color(0xFF10B981),
            addLabel = "出勤追加",
            dateValue = uiState.newWorkdayDate,
            onDateChange = viewModel::setNewWorkdayDate,
            onAdd = viewModel::addWorkday,
            enabled = uiState.selectedDestinationCd.isNotBlank(),
            extraField = uiState.newWorkdayReason,
            onExtraChange = viewModel::setNewWorkdayReason,
            extraLabel = "理由",
        ) {
            uiState.workdays.forEach { w ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(w.workDate.orEmpty(), fontSize = 12.sp)
                        Text(w.reason.orEmpty().ifBlank { "—" }, fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    IconButton(onClick = { viewModel.deleteWorkdayItem(w.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
private fun HolidayCard(
    title: String,
    accent: Color,
    addLabel: String,
    dateValue: String,
    onDateChange: (String) -> Unit,
    onAdd: () -> Unit,
    enabled: Boolean,
    extraField: String = "",
    onExtraChange: (String) -> Unit = {},
    extraLabel: String = "",
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(accent))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateValue,
                    onValueChange = onDateChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM-DD") },
                    enabled = enabled,
                    singleLine = true,
                )
                if (extraLabel.isNotBlank()) {
                    OutlinedTextField(
                        value = extraField,
                        onValueChange = onExtraChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(extraLabel) },
                        enabled = enabled,
                        singleLine = true,
                    )
                }
                Button(onClick = onAdd, enabled = enabled && dateValue.isNotBlank()) {
                    Text(addLabel, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
