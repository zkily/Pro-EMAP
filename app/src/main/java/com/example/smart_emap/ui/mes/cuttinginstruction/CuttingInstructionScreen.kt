package com.example.smart_emap.ui.mes.cuttinginstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.system.HtmlPrintHelper

@Composable
fun CuttingInstructionScreen(viewModel: CuttingInstructionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    DisposableEffect(Unit) {
        viewModel.setPageActive(true)
        onDispose { viewModel.setPageActive(false) }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val subject = uiState.pendingPrintSubject ?: "切断・面取指示"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        snackbarHostState.showSnackbar(
            if (opened) "印刷用ウィンドウを開きました" else "印刷画面を開けませんでした",
        )
    }

    CuttingInstructionDialogHost(uiState.activeDialog, uiState, viewModel)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CuttingInstructionTheme.PageBg,
    ) { padding ->
        BoxWithConstraints(
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
            val configuration = LocalConfiguration.current
            val layout = resolveCuttingInstructionLayout(maxWidth, configuration.orientation)
            val panelFillModifier = if (layout.stackDualPanels) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.fillMaxHeight()
            }

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
                        .padding(
                            horizontal = layout.contentPaddingHorizontal,
                            vertical = layout.contentPaddingVertical,
                        ),
                    verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
                ) {
                    CuttingInstructionHeader(
                        layout = layout,
                        onMoldingPreInventory = viewModel::openMoldingPreInventory,
                        onCuttingDone = viewModel::openCuttingDoneList,
                        onChamferingDone = viewModel::openChamferingDoneList,
                    )

                    CuttingInstructionDualPanelRow(
                        layout = layout,
                        primaryWeight = layout.lotListWeight,
                        secondaryWeight = layout.detailWeight,
                        matchHeight = false,
                        primary = {
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
                        },
                        secondary = {
                            ProductDetailPanel(
                                uiState.selectedProductCd,
                                uiState.productDetail,
                                uiState.equipmentEfficiency,
                                uiState.productDetailLoading,
                            )
                        },
                    )

                    CuttingInstructionDualPanelRow(
                        layout = layout,
                        primaryWeight = layout.todayWeight,
                        secondaryWeight = layout.tomorrowWeight,
                        primary = {
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
                                confirmActualLoading = uiState.confirmCuttingActualLoading,
                                onConfirmActual = viewModel::confirmCuttingActual,
                                onMoveBackToBatch = viewModel::moveCuttingBackToBatch,
                                machineFilter = uiState.cuttingMachineFilter,
                                onCommitReorder = viewModel::commitCuttingTodayReorder,
                                modifier = panelFillModifier,
                            )
                        },
                        secondary = {
                            CuttingInstructionTomorrowCard(
                                date = uiState.cuttingDateTomorrow,
                                onPrevDate = { viewModel.shiftCuttingDateTomorrow(-1) },
                                onNextDate = { viewModel.shiftCuttingDateTomorrow(1) },
                                rows = uiState.cuttingTomorrow,
                                loading = uiState.cuttingLoading,
                                onMoveBackToBatch = viewModel::moveCuttingBackToBatch,
                                modifier = panelFillModifier,
                            )
                        },
                    )

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
                        UsageSummaryFooter(viewModel.usageSummaryTodayCounts)
                    }

                    InstructionSectionCard(
                        accent = CuttingInstructionTheme.ChamferingAccent,
                        title = "面取ロット一覧",
                        titleColor = CuttingInstructionTheme.ChamferingTitle,
                        headerActions = {
                            ChamferPlanNewAddButton(onClick = viewModel::openNewChamferingPlan)
                        },
                    ) {
                        CuttingInstructionDualPanelRow(
                            layout = layout,
                            primaryWeight = layout.chamferPlanWeight,
                            secondaryWeight = layout.chamferEfficiencyWeight,
                            matchHeight = false,
                            primary = {
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
                            },
                            secondary = {
                                CuttingEquipmentEfficiencyPanel(
                                    productCd = uiState.chamferingPlans.find { it.id == uiState.selectedChamferingPlanId }?.productCd,
                                    efficiency = uiState.chamferingPlanEfficiency,
                                    loading = uiState.chamferingPlanEfficiencyLoading,
                                    accent = CuttingInstructionTheme.ChamferingAccent,
                                )
                            },
                        )
                    }

                    CuttingInstructionDualPanelRow(
                        layout = layout,
                        primaryWeight = layout.todayWeight,
                        secondaryWeight = layout.tomorrowWeight,
                        primary = {
                            InstructionSectionCard(
                                modifier = panelFillModifier,
                                fillHeight = !layout.stackDualPanels,
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
                                        ChamferingHeaderActionButton(
                                            "実績確定",
                                            ChamferingHeaderActionStyle.ConfirmActual,
                                            viewModel::confirmChamferingActual,
                                            loading = uiState.confirmChamferingActualLoading,
                                        )
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
                                    onCommitReorder = viewModel::commitChamferingTodayReorder,
                                    reorderEnabled = !uiState.chamferingLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        },
                        secondary = {
                            InstructionSectionCard(
                                modifier = panelFillModifier,
                                fillHeight = !layout.stackDualPanels,
                                accent = CuttingInstructionTheme.ChamferingAccent,
                                title = "面取指示-翌日",
                                titleColor = CuttingInstructionTheme.ChamferingTitle,
                                titleExtras = {
                                    InstructionDateNav(uiState.chamferingDateTomorrow, { viewModel.shiftChamferingDateTomorrow(-1) }, { viewModel.shiftChamferingDateTomorrow(1) })
                                },
                                titleSubRow = { ChamferMgmtHeaderPlaceholderSubRow() },
                                headerActions = { ChamferMgmtHeaderPlaceholderActions() },
                            ) {
                                ChamferingManagementTable(
                                    rows = uiState.chamferingTomorrow,
                                    loading = uiState.chamferingLoading,
                                    formingStartDateByMgmtCode = uiState.cuttingFormingStartDateByMgmtCode,
                                    onToggleCompleted = viewModel::toggleChamferingCompleted,
                                    onToggleNoCount = viewModel::toggleChamferingNoCount,
                                    compact = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        },
                    )

                    KanbanIssuanceSection(
                        date = uiState.kanbanDate,
                        onPrevDate = { viewModel.setKanbanDate(shiftInstructionDate(uiState.kanbanDate, -1)) },
                        onNextDate = { viewModel.setKanbanDate(shiftInstructionDate(uiState.kanbanDate, 1)) },
                        onToday = viewModel::setKanbanToday,
                        status = uiState.kanbanStatus,
                        onStatusChange = viewModel::setKanbanStatus,
                        productName = uiState.kanbanProductName,
                        productOptions = uiState.kanbanProductOptions,
                        onProductChange = viewModel::setKanbanProductName,
                        rows = viewModel.kanbanPagedRows,
                        selectedIds = uiState.selectedKanbanIds,
                        loading = uiState.kanbanLoading,
                        batchIssueLoading = uiState.kanbanBatchIssueLoading,
                        syncLoading = uiState.kanbanSyncLoading,
                        page = uiState.kanbanPage,
                        totalPages = viewModel.kanbanTotalPages,
                        totalCount = uiState.kanbanRows.size,
                        onPageChange = viewModel::setKanbanPage,
                        onToggleSelect = viewModel::toggleKanbanSelection,
                        onTogglePageSelect = viewModel::toggleKanbanPageSelection,
                        onBatchIssue = viewModel::batchIssueKanban,
                        onSync = viewModel::syncKanbanProductionDay,
                        onIssue = viewModel::issueKanban,
                        onReissue = viewModel::reissueKanban,
                        onEdit = viewModel::openEditKanban,
                        issueLoadingId = uiState.kanbanIssuePendingLoading,
                        reissueLoadingId = uiState.kanbanReissueLoading,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CuttingInstructionHeader(
    layout: CuttingInstructionLayoutMode,
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
        if (layout.headerCompact) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column {
                    Text("切断・面取指示管理", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CuttingInstructionTheme.Title)
                    Text(
                        "ロット一覧・切断指示・面取指示・カンバン発行を一括管理",
                        fontSize = 12.sp,
                        color = CuttingInstructionTheme.Subtitle,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeaderToolbarButton("成型前在庫・時間換算", HeaderToolbarButtonStyle.Molding, onMoldingPreInventory)
                    HeaderToolbarButton("切断済リスト", HeaderToolbarButtonStyle.CuttingDone, onCuttingDone)
                    HeaderToolbarButton("面取済リスト", HeaderToolbarButtonStyle.ChamferingDone, onChamferingDone)
                }
            }
        } else {
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
