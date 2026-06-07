package com.example.smart_emap.ui.mes.cuttinginstruction



import android.content.Intent

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.Scaffold

import androidx.compose.material3.SnackbarHost

import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.example.smart_emap.data.model.InstructionCuttingRowDto



@Composable

fun CuttingInstructionScreen(viewModel: CuttingInstructionViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val scroll = rememberScrollState()



    LaunchedEffect(uiState.snackbarMessage) {

        val msg = uiState.snackbarMessage ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(msg)

        viewModel.clearSnackbar()

    }



    LaunchedEffect(uiState.pendingPrintHtml) {

        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect

        val subject = uiState.pendingPrintSubject ?: "切断・面取指示"

        context.startActivity(

            Intent.createChooser(

                Intent(Intent.ACTION_SEND).apply {

                    type = "text/html"

                    putExtra(Intent.EXTRA_SUBJECT, subject)

                    putExtra(Intent.EXTRA_TEXT, html)

                },

                "印刷 / 共有",

            ),

        )

        viewModel.clearPendingPrintHtml()

    }



    CuttingInstructionDialogHost(uiState.activeDialog, uiState, viewModel)



    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },

        containerColor = CuttingInstructionTheme.PageBg,

    ) { padding ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .background(

                    Brush.verticalGradient(

                        listOf(

                            CuttingInstructionTheme.PageBgTop,

                            CuttingInstructionTheme.PageBgMid,

                            CuttingInstructionTheme.PageBgBottom,

                        ),

                    ),

                ),

        ) {

            if (uiState.isLoading && uiState.allPlans.isEmpty()) {

                CircularProgressIndicator(

                    modifier = Modifier.align(Alignment.Center),

                    color = CuttingInstructionTheme.BatchAccent,

                )

            } else {

                Column(

                    modifier = Modifier

                        .fillMaxSize()

                        .verticalScroll(scroll)

                        .padding(horizontal = 10.dp, vertical = 8.dp),

                    verticalArrangement = Arrangement.spacedBy(10.dp),

                ) {

                    CuttingInstructionHeader(

                        onMoldingPreInventory = viewModel::openMoldingPreInventory,

                        onCuttingDone = viewModel::openCuttingDoneList,

                        onChamferingDone = viewModel::openChamferingDoneList,

                    )



                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        Column(Modifier.weight(0.58f)) {

                            ProductionLotListCard(

                                notesCount = uiState.notesCount,

                                actionLoading = uiState.actionLoading,

                                equipmentFilter = uiState.equipmentFilter,

                                machineOptions = uiState.machineOptions,

                                productNameFilter = uiState.productNameFilter,

                                productNameOptions = viewModel.productNameOptions,

                                materialNameFilter = uiState.materialNameFilter,

                                materialNameOptions = viewModel.materialNameOptions,

                                rows = viewModel.pagedPlans,

                                selectedPlanId = uiState.selectedPlanId,

                                loading = uiState.isLoading,

                                planPage = uiState.planPage,

                                planTotalPages = viewModel.planTotalPages,

                                planTotal = viewModel.filteredPlans.size,

                                onEquipmentFilter = viewModel::setEquipmentFilter,

                                onProductNameFilter = viewModel::setProductNameFilter,

                                onMaterialNameFilter = viewModel::setMaterialNameFilter,

                                onOpenNotes = viewModel::openNotes,

                                onSyncLengths = viewModel::syncLengthsFromProducts,

                                onNewPlan = viewModel::openNewPlan,

                                onSelectPlan = viewModel::selectPlan,

                                onToggleStock = viewModel::togglePlanStockSub,

                                onDeletePlan = viewModel::deletePlan,

                                onMoveToCutting = viewModel::openMoveToCutting,

                                onEditPlan = viewModel::openEditPlan,

                                onPlanPageChange = viewModel::setPlanPage,

                            )

                        }

                        Column(Modifier.weight(0.42f)) {

                            ProductDetailPanel(uiState.selectedProductCd, uiState.productDetail, uiState.equipmentEfficiency, uiState.productDetailLoading)

                        }

                    }



                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        Column(Modifier.weight(0.59f)) {

                            CuttingInstructionTodayCard(

                                date = uiState.cuttingDateToday,

                                onPrevDate = { viewModel.shiftCuttingDateToday(-1) },

                                onNextDate = { viewModel.shiftCuttingDateToday(1) },

                                machineOptions = viewModel.cuttingMachineChipOptions,

                                selectedMachine = uiState.cuttingMachineFilter,

                                onMachineSelect = viewModel::setCuttingMachineFilter,

                                rows = uiState.cuttingToday,

                                loading = uiState.cuttingLoading,

                                onToggleCompleted = viewModel::toggleCuttingCompleted,

                                onDuplicate = viewModel::duplicateCuttingRow,

                                onDelete = viewModel::deleteCuttingRow,

                                onEdit = viewModel::openEditCutting,

                                onSplit = { viewModel.openDialog(CuttingInstructionDialog.SplitCutting(it)) },

                                onPrintPlan = viewModel::printCuttingPlan,

                                onPrintSheet = viewModel::printCuttingInstructionSheet,

                                onConfirmActual = viewModel::openConfirmCuttingActual,

                                onMoveBackToBatch = viewModel::moveCuttingBackToBatch,

                            )

                        }

                        Column(Modifier.weight(0.41f)) {

                            InstructionSectionCard(

                                accent = CuttingInstructionTheme.CuttingAccent,

                                title = "切断指示-翌日",

                                titleColor = CuttingInstructionTheme.CuttingTitle,

                                headerActions = {

                                    InstructionDateNav(uiState.cuttingDateTomorrow, { viewModel.shiftCuttingDateTomorrow(-1) }, { viewModel.shiftCuttingDateTomorrow(1) })

                                },

                            ) {

                                CuttingManagementTable(

                                    uiState.cuttingTomorrow, true, uiState.cuttingLoading,

                                    viewModel::toggleCuttingCompleted, viewModel::duplicateCuttingRow, viewModel::deleteCuttingRow,

                                    onMoveBackToBatch = viewModel::moveCuttingBackToBatch,

                                )

                                CuttingMgmtSummaryFooter(
                                    uiState.cuttingTomorrow,
                                    showDefect = false,
                                    showUsage = false,
                                )

                            }

                        }

                    }



                    Column(Modifier.fillMaxWidth()) {

                        InstructionSectionCard(

                            accent = Color(0xFF6366F1),

                            title = "使用材料数（材料別）- 今日",

                            titleColor = Color(0xFF4338CA),

                            headerActions = {

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {

                                    InstructionDateNav(uiState.usageSummaryDateToday, { viewModel.shiftUsageSummaryDateToday(-1) }, { viewModel.shiftUsageSummaryDateToday(1) })

                                    UsageSummaryActionButton("使用数反映", UsageSummaryActionStyle.Reflect, viewModel::openConfirmUsageReflection)

                                    UsageSummaryActionButton("指定日", UsageSummaryActionStyle.SpecifiedDate, viewModel::openSpecifiedDateMaterial)

                                }

                            },

                        ) {

                            UsageSummaryTable(uiState.usageSummaryToday, uiState.usageSummaryLoading, uiState.reflectedCodesToday, viewModel::toggleUsageSummaryStock, viewModel::openEditUsageCount)

                            val c = viewModel.usageSummaryTodayCounts

                            UsageSummaryFooter(c)

                        }

                    }



                    InstructionSectionCard(

                        accent = CuttingInstructionTheme.ChamferingAccent,

                        title = "面取ロット一覧",

                        titleColor = CuttingInstructionTheme.ChamferingTitle,

                        headerActions = {

                            GradientActionButton("新規追加", listOf(CuttingInstructionTheme.ChamferingAccent, Color(0xFF047857)), viewModel::openNewChamferingPlan)

                        },

                    ) {

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                            Column(Modifier.weight(0.65f)) {

                                ChamferingPlanTable(

                                    rows = uiState.chamferingPlans,

                                    selectedId = uiState.selectedChamferingPlanId,

                                    loading = uiState.chamferingPlansLoading,

                                    cuttingProductionDayByMgmtCode = uiState.cuttingProductionDayByMgmtCode,

                                    cuttingFormingStartDateByMgmtCode = uiState.cuttingFormingStartDateByMgmtCode,

                                    onSelect = viewModel::selectChamferingPlan,

                                    onToggleSw = viewModel::toggleChamferingPlanSw,

                                    onCopy = viewModel::copyChamferingPlan,

                                    onDelete = viewModel::deleteChamferingPlan,

                                    onMove = viewModel::openMoveChamferingPlan,

                                    onEdit = viewModel::openEditChamferingPlan,

                                )

                            }

                            Column(Modifier.weight(0.35f)) {

                                CuttingEquipmentEfficiencyPanel(

                                    productCd = uiState.chamferingPlans.find { it.id == uiState.selectedChamferingPlanId }?.productCd,

                                    efficiency = uiState.chamferingPlanEfficiency,

                                    loading = uiState.chamferingPlanEfficiencyLoading,

                                    accent = CuttingInstructionTheme.ChamferingAccent,

                                )

                            }

                        }

                    }



                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        Column(Modifier.weight(0.59f)) {

                            InstructionSectionCard(

                                accent = CuttingInstructionTheme.ChamferingAccent,

                                title = "面取指示-今日",

                                titleColor = CuttingInstructionTheme.ChamferingTitle,

                                titleExtras = {

                                    InstructionDateNav(uiState.chamferingDateToday, { viewModel.shiftChamferingDateToday(-1) }, { viewModel.shiftChamferingDateToday(1) })

                                },

                                titleSubRow = {

                                    InstructionMachineChips(

                                        uiState.chamferingMachineOptions,

                                        uiState.chamferingMachineFilter,

                                        viewModel::setChamferingMachineFilter,

                                        chipStyle = MachineChipStyle.Chamfering,

                                        compact = true,

                                        modifier = Modifier.padding(top = 8.dp),

                                    )

                                },

                                headerActions = {

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {

                                        ChamferingHeaderActionButton("新規追加", ChamferingHeaderActionStyle.New, viewModel::openNewChamferingInstruction)

                                        ChamferingHeaderActionButton("計画印刷", ChamferingHeaderActionStyle.PlanPrint, viewModel::printChamferingPlan)

                                        ChamferingHeaderActionButton("指示書発行", ChamferingHeaderActionStyle.IssueSheet, viewModel::printChamferingInstructionSheet)

                                        ChamferingHeaderActionButton("実績確定", ChamferingHeaderActionStyle.ConfirmActual, viewModel::openConfirmChamferingActual)

                                    }

                                },

                            ) {

                                ChamferingManagementTable(
                                    rows = uiState.chamferingToday,
                                    loading = uiState.chamferingLoading,
                                    formingStartDateByMgmtCode = uiState.cuttingFormingStartDateByMgmtCode,
                                    onToggleCompleted = viewModel::toggleChamferingCompleted,
                                    onToggleNoCount = viewModel::toggleChamferingNoCount,
                                    onEdit = viewModel::openEditChamfering,
                                    onDuplicate = viewModel::duplicateChamferingRow,
                                    onDelete = viewModel::deleteChamferingRow,
                                    onSplit = { viewModel.openDialog(CuttingInstructionDialog.SplitChamfering(it)) },
                                )

                            }

                        }

                        Column(Modifier.weight(0.41f)) {

                            InstructionSectionCard(

                                accent = CuttingInstructionTheme.ChamferingAccent,

                                title = "面取指示-翌日",

                                titleColor = CuttingInstructionTheme.ChamferingTitle,

                                headerActions = {

                                    InstructionDateNav(uiState.chamferingDateTomorrow, { viewModel.shiftChamferingDateTomorrow(-1) }, { viewModel.shiftChamferingDateTomorrow(1) })

                                },

                            ) {

                                ChamferingManagementTable(
                                    rows = uiState.chamferingTomorrow,
                                    loading = uiState.chamferingLoading,
                                    formingStartDateByMgmtCode = uiState.cuttingFormingStartDateByMgmtCode,
                                    onToggleCompleted = viewModel::toggleChamferingCompleted,
                                    onToggleNoCount = viewModel::toggleChamferingNoCount,
                                    compact = true,
                                )

                            }

                        }

                    }



                    InstructionSectionCard(

                        accent = CuttingInstructionTheme.KanbanAccent,

                        title = "カンバン発行",

                        titleColor = CuttingInstructionTheme.KanbanTitle,

                        headerActions = {

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                                GradientActionButton("今日", listOf(CuttingInstructionTheme.BtnIssue, CuttingInstructionTheme.BtnIssueEnd), viewModel::setKanbanToday)

                                GradientActionButton("一括発行", listOf(CuttingInstructionTheme.KanbanAccent, Color(0xFFEA580C)), viewModel::batchIssueKanban)

                                GradientActionButton("同期更新", listOf(Color(0xFF64748B), Color(0xFF475569)), viewModel::syncKanbanProductionDay)

                            }

                        },

                    ) {

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                            InstructionDateNav(uiState.kanbanDate, { viewModel.setKanbanDate(shiftInstructionDate(uiState.kanbanDate, -1)) }, { viewModel.setKanbanDate(shiftInstructionDate(uiState.kanbanDate, 1)) })

                            InstructionFilterDropdown("状態", uiState.kanbanStatus, listOf("" to "全部", "pending" to "待発行", "issued" to "発行済"), viewModel::setKanbanStatus)

                            InstructionFilterDropdown("製品", uiState.kanbanProductName, uiState.kanbanProductOptions.map { it to it }, viewModel::setKanbanProductName)

                        }

                        Spacer(Modifier.height(8.dp))

                        KanbanTable(uiState.kanbanRows, uiState.selectedKanbanIds, uiState.kanbanLoading, viewModel::toggleKanbanSelection, viewModel::issueKanban, viewModel::reissueKanban, viewModel::openEditKanban)

                    }

                }

            }

        }

    }

}



@Composable

private fun CuttingInstructionHeader(

    onMoldingPreInventory: () -> Unit,

    onCuttingDone: () -> Unit,

    onChamferingDone: () -> Unit,

) {

    Surface(

        modifier = Modifier

            .fillMaxWidth()

            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CuttingInstructionTheme.CardShadow, spotColor = CuttingInstructionTheme.CardShadow),

        shape = RoundedCornerShape(12.dp),

        color = CuttingInstructionTheme.HeaderBg,

        border = androidx.compose.foundation.BorderStroke(1.dp, CuttingInstructionTheme.HeaderBorder),

    ) {

        Row(

            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically,

        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text("切断・面取指示管理", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CuttingInstructionTheme.Title)

                Text("ロット一覧・切断指示・面取指示・カンバン発行を一括管理", fontSize = 12.sp, color = CuttingInstructionTheme.Subtitle, modifier = Modifier.padding(top = 2.dp))

            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                HeaderToolbarButton("成型前在庫・時間換算", HeaderToolbarButtonStyle.Molding, onMoldingPreInventory)

                HeaderToolbarButton("切断済リスト", HeaderToolbarButtonStyle.CuttingDone, onCuttingDone)

                HeaderToolbarButton("面取済リスト", HeaderToolbarButtonStyle.ChamferingDone, onChamferingDone)

            }

        }

    }

}



@Composable

private fun UsageSummaryFooter(counts: UsageSummaryCounts) {

    Row(

        Modifier

            .fillMaxWidth()

            .padding(top = 8.dp)

            .clip(RoundedCornerShape(8.dp))

            .background(CuttingInstructionTheme.TableRowAlt)

            .padding(horizontal = 10.dp, vertical = 6.dp),

        horizontalArrangement = Arrangement.spacedBy(16.dp),

    ) {

        SummaryChip("合計", counts.total.toString())

        SummaryChip("反映済", counts.reflected.toString())

        SummaryChip("未反映", counts.notReflected.toString())

    }

}



@Composable

private fun SummaryChip(label: String, value: String) {

    Text("$label：", fontSize = 11.sp, color = CuttingInstructionTheme.Subtitle)

    Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CuttingInstructionTheme.Title)

}


