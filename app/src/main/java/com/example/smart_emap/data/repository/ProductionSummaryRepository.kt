package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.BatchActualBody
import com.example.smart_emap.data.model.BatchUpdateLockBody
import com.example.smart_emap.data.model.GenerateProductionSummaryBody
import com.example.smart_emap.data.model.PrevCarryBreakdownDataDto
import com.example.smart_emap.data.model.PrevCarryWipTotalDataDto
import com.example.smart_emap.data.model.ProcessMachinePlanDataDto
import com.example.smart_emap.data.model.ProcessMachineProductsDataDto
import com.example.smart_emap.data.model.InventoryStagnationDataDto
import com.example.smart_emap.data.model.InventoryStagnationRowDto
import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductMachineConfigRowDto
import com.example.smart_emap.data.model.ProductMachineConfigUpdateBody
import com.example.smart_emap.data.model.ProductProcessBomRowDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.model.UpdateProductProcessBomBody
import com.example.smart_emap.ui.erp.production.planning.PlanCreateFormState
import com.example.smart_emap.ui.erp.production.planning.PlanCreateKind
import com.example.smart_emap.ui.erp.production.planning.PlanCreateResultRow
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanCreateLogic
import com.example.smart_emap.data.model.StartDateBody
import com.example.smart_emap.data.model.UpdateFromOrderDailyBody
import com.example.smart_emap.ui.erp.production.planning.BatchInitialStockRow
import com.example.smart_emap.ui.erp.production.planning.DataMgmtBatchLogic
import com.example.smart_emap.ui.erp.production.planning.ProcessPlanPrintLogic
import com.example.smart_emap.ui.erp.production.planning.ProductionDataManagementLogic
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ProductionSummaryFilters(
    val startDate: String,
    val endDate: String,
    val productCd: String = "",
    val keyword: String = "",
    val page: Int = 1,
    val limit: Int = 500,
    val sortBy: String = "product_name",
    val sortOrder: String = "ASC",
)

data class AllBatchUpdateProgress(
    val stepIndex: Int,
    val stepTotal: Int,
    val stepLabel: String,
)

class ProductionSummaryRepository(
    private val apiClient: ApiClient,
) {
    suspend fun loadList(filters: ProductionSummaryFilters): Pair<List<ProductionSummaryFullRowDto>, Int> =
        runCatching {
            val res = apiClient.productionSummaryApiLong().listProductionSummarys(
                page = filters.page,
                limit = filters.limit,
                startDate = filters.startDate,
                endDate = filters.endDate,
                productCd = filters.productCd.trim().ifBlank { null },
                keyword = filters.keyword.trim().ifBlank { null },
                sortBy = filters.sortBy,
                sortOrder = filters.sortOrder,
            )
            val list = res.data?.list.orEmpty()
            val total = res.data?.pagination?.total ?: list.size
            list to total
        }.getOrElse { emptyList<ProductionSummaryFullRowDto>() to 0 }

    suspend fun loadProducts(): List<ProductionSummaryProductOptionDto> = runCatching {
        apiClient.productionSummaryApi().listProducts().data.orEmpty()
    }.getOrElse { emptyList() }

    suspend fun loadProcessOptions(): List<Pair<String, String>> = runCatching {
        apiClient.masterApi().listProcesses(pageSize = 500).items()
            .map { (it.processCd.orEmpty()) to (it.processName.orEmpty()) }
            .filter { it.first.isNotBlank() }
    }.getOrElse { emptyList() }

    suspend fun forEachSummaryInRange(
        startDate: String,
        endDate: String,
        pageHandler: (List<ProductionSummaryFullRowDto>) -> Unit,
    ) {
        val api = apiClient.productionSummaryApiLong()
        val pageSize = 3_000
        var page = 1
        var reportedTotal: Int? = null
        while (page <= 20) {
            val res = api.listProductionSummarys(
                page = page,
                limit = pageSize,
                startDate = startDate,
                endDate = endDate,
            )
            val list = res.data?.list.orEmpty()
            reportedTotal = res.data?.pagination?.total ?: reportedTotal
            pageHandler(list)
            if (list.isEmpty()) break
            val total = reportedTotal ?: break
            if (page * pageSize >= total) break
            page++
        }
    }

    suspend fun prepareProcessPrint(targetDate: String): ProcessPlanPrintLogic.Accumulator {
        val normalized = ProcessPlanPrintLogic.normalizeTargetDate(targetDate)
        val iso = DateTimeFormatter.ISO_LOCAL_DATE
        val anchor = LocalDate.parse(normalized, iso)
        val startDate = anchor.minusDays(90).format(iso)
        val endDate = anchor.plusDays(90).format(iso)
        val accumulator = ProcessPlanPrintLogic.Accumulator(normalized)
        forEachSummaryInRange(startDate, endDate) { accumulator.ingest(it) }
        return accumulator
    }

    suspend fun loadInventoryStagnation(
        asOf: String,
        minQuantity: Int,
        stableCalendarDays: Int,
        productCd: String = "",
        keyword: String = "",
    ): Pair<List<InventoryStagnationRowDto>, InventoryStagnationDataDto?> = runCatching {
        val res = apiClient.productionSummaryApi().getInventoryStagnation(
            asOf = asOf.trim().ifBlank { null },
            minQuantity = minQuantity,
            stableCalendarDays = stableCalendarDays,
            productCd = productCd.trim().ifBlank { null },
            keyword = keyword.trim().ifBlank { null },
        )
        val data = res.data
        data?.list.orEmpty() to data
    }.getOrElse { throw it }

    suspend fun generate(startDate: String, endDate: String): String = postAction {
        apiClient.productionSummaryApiLong().generate(
            GenerateProductionSummaryBody(startDate, endDate),
        ).message
    }

    suspend fun updateFromOrderDaily(allMode: Boolean = false): String = postAction {
        val body = if (allMode) {
            UpdateFromOrderDailyBody(updateMode = "all")
        } else {
            UpdateFromOrderDailyBody(updateMode = "recent", days = 10, clearBeforeUpdate = true)
        }
        apiClient.productionSummaryApiLong().updateFromOrderDaily(body).message
    }

    suspend fun updateCarryOver(): String = postAction {
        val api = apiClient.productionSummaryApiLong()
        runCatching { api.clearCarryOver() }
        api.updateCarryOver().message
    }

    suspend fun updateActual(): String = postAction {
        apiClient.productionSummaryApiLong().updateActual().message
    }

    suspend fun updateDefect(): String = postAction {
        apiClient.productionSummaryApiLong().updateDefect().message
    }

    suspend fun updateScrap(): String = postAction {
        apiClient.productionSummaryApiLong().updateScrap().message
    }

    suspend fun updateOnHold(): String = postAction {
        apiClient.productionSummaryApiLong().updateOnHold().message
    }

    suspend fun updateProductionDates(): String = postAction {
        apiClient.productionSummaryApiLong().updateProductionDates().message
    }

    suspend fun updatePlan(): String = postAction {
        val startDate = ProductionDataManagementLogic.firstDayOfCurrentMonth()
        val api = apiClient.productionSummaryApiLong()
        runCatching { api.clearCalculatedFields(StartDateBody(startDate)) }
        runCatching { api.clearPlanFields(StartDateBody(startDate)) }
        api.updatePlan(StartDateBody(startDate)).message
    }

    suspend fun resolveInventoryTrendCalcStartDate(): String = runCatching {
        val raw = apiClient.productionSummaryApi().getInventoryTrendCalcStartDate().data?.startDate
        if (!raw.isNullOrBlank() && raw.length >= 10) raw.take(10) else ProductionDataManagementLogic.firstDayOfCurrentMonth()
    }.getOrElse { ProductionDataManagementLogic.firstDayOfCurrentMonth() }

    suspend fun updateInventoryTrendSequence(
        onProgress: (String) -> Unit = {},
    ): String {
        val startDate = resolveInventoryTrendCalcStartDate()
        val api = apiClient.productionSummaryApiLong()
        runCatching { api.clearCalculatedFields(StartDateBody(startDate)) }
        onProgress("在庫データを更新中...")
        val invMsg = postAction { api.updateInventory(StartDateBody(startDate)).message }
        onProgress("推移データを更新中...")
        val trendMsg = postAction { api.updateTrend(StartDateBody(startDate)).message }
        onProgress("安全在庫を更新中...")
        val safetyMsg = postAction { api.updateSafetyStock(StartDateBody(startDate)).message }
        return listOfNotNull(invMsg, trendMsg, safetyMsg).joinToString("\n").ifBlank {
            "在庫・推移の更新が完了しました（$startDate～）"
        }
    }

    suspend fun updateProductMaster(startDate: String, endDate: String): String = postAction {
        apiClient.productionSummaryApiLong().updateProductMaster(
            GenerateProductionSummaryBody(startDate, endDate),
        ).message
    }

    suspend fun updateMachine(startDate: String, endDate: String): String = postAction {
        apiClient.productionSummaryApiLong().updateMachine(
            GenerateProductionSummaryBody(startDate, endDate),
        ).message
    }

    suspend fun runAllBatchUpdate(
        onProgress: (AllBatchUpdateProgress) -> Unit,
    ): String {
        val lock = UUID.randomUUID().toString()
        val api = apiClient.productionSummaryApiLong()
        val acquired = api.acquireBatchUpdateLock(BatchUpdateLockBody(lock, 600)).data?.acquired == true
        if (!acquired) {
            throw IllegalStateException("他の端末で一括更新が実行中です。数分待ってから再実行してください。")
        }
        val stepNames = listOf(
            "受注データ更新",
            "実績データ更新",
            "不良データ更新",
            "廃棄データ更新",
            "保留データ更新",
            "計画データ更新",
            "在庫・推移・安全在庫更新",
        )
        val results = mutableListOf<Pair<String, Boolean>>()
        return try {
            val steps: List<suspend () -> Unit> = listOf(
                { updateFromOrderDaily(allMode = true) },
                { updateActual() },
                { updateDefect() },
                { updateScrap() },
                { updateOnHold() },
                {
                    val startDate = ProductionDataManagementLogic.firstDayOfCurrentMonth()
                    runCatching { api.clearPlanFields(StartDateBody(startDate)) }
                    postAction { api.updatePlan(StartDateBody(startDate)).message }
                },
                {
                    val startDate = resolveInventoryTrendCalcStartDate()
                    runCatching { api.clearCalculatedFields(StartDateBody(startDate)) }
                    postAction { api.updateInventory(StartDateBody(startDate)).message }
                    postAction { api.updateTrend(StartDateBody(startDate)).message }
                    postAction { api.updateSafetyStock(StartDateBody(startDate)).message }
                },
            )
            steps.forEachIndexed { index, step ->
                onProgress(
                    AllBatchUpdateProgress(
                        stepIndex = index + 1,
                        stepTotal = stepNames.size,
                        stepLabel = stepNames[index],
                    ),
                )
                val ok = runCatching { step() }.isSuccess
                results += stepNames[index] to ok
            }
            val failNames = results.filter { !it.second }.map { it.first }
            if (failNames.isEmpty()) {
                "全部一括更新が完了しました"
            } else {
                "全部一括更新が完了しました（失敗: ${failNames.joinToString("、")}）"
            }
        } finally {
            runCatching { api.releaseBatchUpdateLock(BatchUpdateLockBody(lock)) }
        }
    }

    suspend fun searchBatchInitialStock(month: String, processCd: String): List<BatchInitialStockRow> {
        val products = runCatching {
            apiClient.masterApi().listProductsByProcess(processCd)
        }.getOrElse {
            DataMgmtBatchLogic.fallbackProductsFromSummary(loadProducts())
        }
        val monthFirst = "$month-01"
        val logs = runCatching {
            val res = apiClient.stockTransactionLogApi().listStockLogs(
                transactionType = "初期",
                processCd = processCd,
                dateStart = "$monthFirst 00:00:00",
                dateEnd = monthFirst,
            )
            res.data?.list ?: res.list.orEmpty()
        }.getOrElse { emptyList() }
        return DataMgmtBatchLogic.buildInitialStockRows(products, logs)
    }

    suspend fun saveBatchInitialStock(
        month: String,
        processCd: String,
        rows: List<BatchInitialStockRow>,
    ): String {
        val (inserts, updates) = DataMgmtBatchLogic.buildInitialStockUpdates(month, processCd, rows)
        if (inserts.isEmpty() && updates.isEmpty()) {
            return "変更がありません"
        }
        val api = apiClient.stockTransactionLogApiLong()
        inserts.forEach { api.createStockLog(it) }
        updates.forEach { (id, body) -> api.updateStockLog(id, body) }
        return "更新 ${updates.size} 件、追加 ${inserts.size} 件"
    }

    suspend fun submitBatchActual(date: String, rows: List<com.example.smart_emap.ui.erp.production.planning.BatchActualRow>): String {
        val transactions = DataMgmtBatchLogic.buildBatchActualTransactions(date, rows)
        if (transactions.isEmpty()) {
            throw IllegalArgumentException("登録する実績データがありません")
        }
        return postAction {
            apiClient.stockTransactionLogApiLong().batchActual(BatchActualBody(transactions)).message
                ?: "実績データを${transactions.size}件登録しました"
        }
    }

    suspend fun loadProcessMachinePlan(
        startDate: String,
        endDate: String,
        processes: String?,
    ): ProcessMachinePlanDataDto? = runCatching {
        apiClient.productionSummaryApi().getProcessMachinePlan(startDate, endDate, processes).data
    }.getOrNull()

    suspend fun loadProcessMachineProducts(
        startDate: String,
        endDate: String,
        process: String,
        machine: String,
    ): ProcessMachineProductsDataDto? = runCatching {
        apiClient.productionSummaryApi().getProcessMachinePlanProducts(startDate, endDate, process, machine).data
    }.getOrNull()

    suspend fun loadPrevCarryWipTotal(month: String): PrevCarryWipTotalDataDto? = runCatching {
        apiClient.productionSummaryApi().getPrevCarryWipTotal(month).data
    }.getOrNull()

    suspend fun loadPrevCarryBreakdown(month: String, column: String): PrevCarryBreakdownDataDto? = runCatching {
        apiClient.productionSummaryApi().getPrevCarryBreakdown(month, column).data
    }.getOrNull()

    suspend fun fetchAllProductProcessBom(): List<ProductProcessBomRowDto> {
        val api = apiClient.masterApi()
        val limit = 100
        val all = mutableListOf<ProductProcessBomRowDto>()
        var page = 1
        while (page <= 200) {
            val list = api.listProductProcessBom(page = page, limit = limit).items()
            if (list.isEmpty()) break
            all += list
            if (list.size < limit) break
            page++
        }
        return all
    }

    suspend fun fetchAllMasterProducts(): List<MasterProductDto> {
        val api = apiClient.masterApi()
        val pageSize = 10_000
        val all = mutableListOf<MasterProductDto>()
        var page = 1
        while (page <= 50) {
            val res = api.listProducts(page = page, pageSize = pageSize)
            val list = res.items()
            if (list.isEmpty()) break
            all += list
            val total = res.totalCount()
            if (list.size < pageSize || page * pageSize >= total) break
            page++
        }
        return all
    }

    suspend fun fetchProductLotSizeMap(): Map<String, Int> =
        fetchAllMasterProducts().mapNotNull { p ->
            val cd = p.productCd.orEmpty().trim()
            if (cd.isBlank()) null else cd to (p.lotSize ?: 1)
        }.toMap()

    suspend fun loadAllSummaryInRange(startDate: String, endDate: String): List<ProductionSummaryFullRowDto> {
        val collected = mutableListOf<ProductionSummaryFullRowDto>()
        forEachSummaryInRange(startDate, endDate) { page -> collected += page }
        return collected
    }

    suspend fun loadProductMachineConfig(): List<ProductMachineConfigRowDto> = runCatching {
        apiClient.masterApi().listProductMachineConfig().items()
            .sortedBy { it.productName.orEmpty() }
    }.getOrElse { emptyList() }

    suspend fun loadMachineOptions(): List<MasterMachineFullDto> = runCatching {
        apiClient.masterApi().listMachines(pageSize = 99_999).items()
    }.getOrElse { emptyList() }

    suspend fun loadEquipmentEfficiencyAll(): List<EquipmentEfficiencyRowDto> = runCatching {
        apiClient.masterApi().listEquipmentEfficiencyMaster(limit = 99_999).items()
    }.getOrElse { emptyList() }

    suspend fun updateProductMachineConfigMolding(id: Int, machine: String) {
        apiClient.masterApi().updateProductMachineConfig(
            id,
            ProductMachineConfigUpdateBody(moldingMachine = machine.ifBlank { null }),
        )
    }

    suspend fun updateProductMachineConfigWelding(id: Int, machine: String) {
        apiClient.masterApi().updateProductMachineConfig(
            id,
            ProductMachineConfigUpdateBody(weldingMachine = machine.ifBlank { null }),
        )
    }

    suspend fun updateProductProcessBomMolding(
        productCd: Int,
        safetyStockDays: Int,
        formingProcessLt: Int,
    ) {
        apiClient.masterApi().updateProductProcessBom(
            productCd,
            UpdateProductProcessBomBody(
                safetyStockDays = safetyStockDays,
                formingProcessLt = formingProcessLt,
            ),
        )
    }

    suspend fun updateProductProcessBomWelding(
        productCd: Int,
        safetyStockDays: Int,
        weldingProcessLt: Int,
    ) {
        apiClient.masterApi().updateProductProcessBom(
            productCd,
            UpdateProductProcessBomBody(
                safetyStockDays = safetyStockDays,
                weldingProcessLt = weldingProcessLt,
            ),
        )
    }

    suspend fun clearMoldingPlan(startDate: String): String = postAction {
        val res = apiClient.productionSummaryApiLong().clearMoldingPlan(StartDateBody(startDate))
        res.message ?: "成型計画をクリアしました（${res.data?.cleared ?: 0}件）"
    }

    suspend fun clearWeldingPlan(startDate: String): String = postAction {
        val res = apiClient.productionSummaryApiLong().clearWeldingPlan(StartDateBody(startDate))
        res.message ?: "溶接計画をクリアしました（${res.data?.cleared ?: 0}件）"
    }

    suspend fun executePlanCreate(
        kind: PlanCreateKind,
        form: PlanCreateFormState,
    ): List<PlanCreateResultRow> {
        val baseDate = form.baseDate.trim()
        if (baseDate.isBlank()) throw IllegalArgumentException("基準日を選択してください")
        val bomRows = fetchAllProductProcessBom()
        if (bomRows.isEmpty()) return emptyList()
        val lookupDates = bomRows.mapNotNull { bom ->
            val lt = when (kind) {
                PlanCreateKind.Molding -> bom.formingProcessLt ?: 0
                PlanCreateKind.Welding -> bom.weldingProcessLt ?: 0
            }
            val safety = bom.safetyStockDays ?: 0
            ProductionPlanCreateLogic.addBusinessDays(baseDate, lt + safety)
        }.filter { it.isNotBlank() }.sorted()
        if (lookupDates.isEmpty()) return emptyList()
        val summaryRows = loadAllSummaryInRange(lookupDates.first(), lookupDates.last())
        val lotSizeMap = fetchProductLotSizeMap()
        val resolveEff = ProductionPlanCreateLogic.buildEfficiencyResolver(
            loadEquipmentEfficiencyAll(),
            loadMachineOptions(),
        )
        return ProductionPlanCreateLogic.calculate(
            kind = kind,
            baseDate = baseDate,
            coefficient = form.coefficient,
            workingDays = form.workingDays,
            bomRows = bomRows,
            summaryRows = summaryRows,
            lotSizeMap = lotSizeMap,
            resolveEff = resolveEff,
        )
    }

    private suspend fun postAction(block: suspend () -> String?): String = runCatching {
        block() ?: "更新が完了しました"
    }.getOrElse { e -> throw e }
}
