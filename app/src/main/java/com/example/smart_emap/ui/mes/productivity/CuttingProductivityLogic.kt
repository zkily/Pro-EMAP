package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.data.model.InspectionProductivityDailyRowDto
import com.example.smart_emap.data.model.CuttingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.CuttingProductivityBucketDto
import com.example.smart_emap.data.model.CuttingProductivityDailyRowDto
import com.example.smart_emap.data.model.CuttingProductivityOperatorRowDto
import com.example.smart_emap.data.model.CuttingProductivityProductRankingDto
import com.example.smart_emap.data.model.CuttingProductivityProductRowDto
import com.example.smart_emap.data.model.CuttingProductivitySessionRowDto

data class CuttingProductivityReportFilters(
    val startDate: String,
    val endDate: String,
    val lineLabel: String,
    val productLabel: String,
    val includeIncomplete: Boolean,
)

enum class CuttingProductivityReportCommand {
    PRINT_FULL,
    PRINT_DAILY,
    PRINT_DAILY_BATCH,
    PRINT_OPERATOR,
    PRINT_OPERATOR_PRODUCT_BATCH,
    PRINT_PRODUCT,
    PRINT_PRODUCT_RANK,
}

data class CuttingOperatorProductDisplayRow(
    val productCd: String,
    val productName: String,
    val sessionCount: Int,
    val sumActualQty: Int,
    val sumDefectQty: Int,
    val sumNetProductionSec: Int,
    val defectRatePercent: Double?,
    val avgEfficiencyPerHour: Double?,
)

data class CuttingProductivityPrintContext(
    val filters: CuttingProductivityReportFilters,
    val kpiCards: List<IpaKpiCard>,
    val operatorRows: List<CuttingProductivityOperatorRowDto>,
    val productRows: List<CuttingProductivityProductRowDto>,
    val operatorSectionAvgEfficiency: Double?,
    val productSectionTotalQty: Int,
    val productRankList: List<CuttingProductivityProductRankingDto>,
    val selectedProductRanking: CuttingProductivityProductRankingDto?,
    val productRankTopOverview: List<CuttingProductivityProductRankingDto>,
    val dailyChartFileName: String? = null,
)

data class CuttingDailyBatchPrintItem(
    val lineLabel: String,
    val daily: List<CuttingProductivityDailyRowDto>,
    val chartFileName: String,
)

object CuttingProductivityLogic {
    fun defaultDateRange(): Pair<String, String> {
        val end = MesCalendarUtils.jstToday()
        val start = MesCalendarUtils.shiftDateYmd(end, -29)
        return start to end
    }

    fun reportMenuItems(): List<IpaReportMenuEntry> = listOf(
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_FULL.name,
            label = "印刷（全体）",
            hint = "全セクション一括出力",
            tone = IpaReportMenuTone.INDIGO,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_DAILY.name,
            label = "日別推移（印刷）",
            hint = "生産数 · 能率の推移",
            tone = IpaReportMenuTone.SKY,
            divided = true,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_DAILY_BATCH.name,
            label = "日別推移（ライン別・一括印刷）",
            hint = "ラインごとに分割出力",
            tone = IpaReportMenuTone.TEAL,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_OPERATOR.name,
            label = "ライン別（印刷）",
            hint = "ライン別サマリー",
            tone = IpaReportMenuTone.VIOLET,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_OPERATOR_PRODUCT_BATCH.name,
            label = "ライン別製品別（一括印刷）",
            hint = "ラインごとに製品一覧を出力",
            tone = IpaReportMenuTone.VIOLET,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_PRODUCT.name,
            label = "製品別（印刷）",
            hint = "製品別サマリー",
            tone = IpaReportMenuTone.EMERALD,
        ),
        IpaReportMenuEntry(
            key = CuttingProductivityReportCommand.PRINT_PRODUCT_RANK.name,
            label = "製品別 · ライン能率ランキング（印刷）",
            hint = "製品単位の順位表",
            tone = IpaReportMenuTone.ROSE,
        ),
    )

    fun fmtInt(value: Int?): String = InspectionProductivityLogic.fmtInt(value)

    fun fmtPct(value: Double?): String = InspectionProductivityLogic.fmtPct(value)

    fun fmtEfficiency(value: Double?): String = InspectionProductivityLogic.fmtEfficiency(value)

    fun fmtDurationMin(min: Int?): String = InspectionProductivityLogic.fmtDurationMin(min)

    fun rangeLabel(start: String?, end: String?): String? = InspectionProductivityLogic.rangeLabel(start, end)

    fun rankMedal(rank: Int?): String = InspectionProductivityLogic.rankMedal(rank)

    fun periodAvgEfficiencyFromBucket(
        sumActualQty: Int?,
        sumNetProductionSec: Int?,
        efficiencyPerHour: Double? = null,
    ): Double? = InspectionProductivityLogic.periodAvgEfficiencyFromBucket(
        sumActualQty,
        sumNetProductionSec,
        efficiencyPerHour,
    )

    fun periodAvgEfficiencyFromBucket(row: CuttingProductivityOperatorRowDto): Double? =
        periodAvgEfficiencyFromBucket(row.sumActualQty, row.sumNetProductionSec, row.efficiencyPerHour)

    fun periodAvgEfficiencyFromBucket(row: CuttingProductivityProductRowDto): Double? =
        periodAvgEfficiencyFromBucket(row.sumActualQty, row.sumNetProductionSec, row.efficiencyPerHour)

    fun operatorSectionAvgEfficiency(rows: List<CuttingProductivityOperatorRowDto>): Double? {
        var totalQty = 0
        var totalSec = 0
        for (row in rows) {
            totalQty += row.sumActualQty ?: 0
            totalSec += row.sumNetProductionSec ?: 0
        }
        return periodAvgEfficiencyFromBucket(totalQty, totalSec)
    }

    fun operatorDisplayRows(rows: List<CuttingProductivityOperatorRowDto>): List<CuttingProductivityOperatorRowDto> =
        rows.map { row ->
            row.copy(
                efficiencyPerHour = periodAvgEfficiencyFromBucket(row),
            )
        }.sortedByDescending { it.efficiencyPerHour ?: -1.0 }

    fun productDisplayRows(rows: List<CuttingProductivityProductRowDto>): List<CuttingProductivityProductRowDto> =
        rows.map { row ->
            row.copy(
                efficiencyPerHour = periodAvgEfficiencyFromBucket(row),
            )
        }.sortedByDescending { it.sumActualQty ?: 0 }

    fun productRankOptionLabel(p: CuttingProductivityProductRankingDto): String {
        val name = p.productName?.trim().orEmpty()
        return if (name.isNotEmpty()) "${p.productCd} · $name" else p.productCd
    }

    fun buildKpiCards(summary: CuttingProductivityBucketDto?): List<IpaKpiCard> {
        val s = summary ?: CuttingProductivityBucketDto()
        return listOf(
            IpaKpiCard(
                key = "sessions",
                label = "確定セッション",
                value = fmtInt(s.completedSessionCount),
                hint = "全 ${fmtInt(s.sessionCount)} 件",
                tone = IpaKpiTone.Indigo,
                icon = IpaKpiIcon.Sessions,
            ),
            IpaKpiCard(
                key = "actual",
                label = "生産数合計",
                value = fmtInt(s.sumActualQty),
                hint = "確定実績合計",
                tone = IpaKpiTone.Sky,
                icon = IpaKpiIcon.Production,
            ),
            IpaKpiCard(
                key = "defect",
                label = "不良数",
                value = fmtInt(s.sumDefectQty),
                hint = "不良率 ${fmtPct(s.defectRatePercent)}",
                tone = IpaKpiTone.Amber,
                icon = IpaKpiIcon.Defect,
            ),
            IpaKpiCard(
                key = "efficiency",
                label = "総合能率",
                value = fmtEfficiency(s.efficiencyPerHour),
                hint = "本 / 時間",
                tone = IpaKpiTone.Emerald,
                icon = IpaKpiIcon.Efficiency,
            ),
            IpaKpiCard(
                key = "runtime",
                label = "正味稼働",
                value = fmtDurationMin(s.sumNetProductionMin),
                hint = "停止 ${fmtDurationMin(s.sumPausedMin)}",
                tone = IpaKpiTone.Violet,
                icon = IpaKpiIcon.Runtime,
            ),
        )
    }

    fun resolveProductRankList(data: CuttingProductivityAnalysisDataDto?): List<CuttingProductivityProductRankingDto> {
        val fromApi = data?.byProductOperatorRanking.orEmpty()
        if (fromApi.isNotEmpty()) return fromApi
        return buildProductOperatorRankingFromSessions(data?.sessions.orEmpty())
    }

    fun buildProductOperatorRankingFromSessions(
        sessions: List<CuttingProductivitySessionRowDto>,
    ): List<CuttingProductivityProductRankingDto> {
        val productMap = linkedMapOf<String, ProductAgg>()

        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty().ifBlank { "unknown" }
            val productName = s.productName?.trim().orEmpty().ifBlank { productCd }
            val prod = productMap.getOrPut(productCd) {
                ProductAgg(productCd, productName)
            }
            prod.sumActualQty += s.actualProductionQuantity ?: 0
            prod.sessionCount += 1

            val opKey = sessionLineKey(s)
            val opName = opKey
            val inv = prod.operators.getOrPut(opKey) {
                OperatorAgg(null, opName.ifBlank { "—" })
            }
            inv.sessionCount += 1
            inv.sumActualQty += s.actualProductionQuantity ?: 0
            inv.sumDefectQty += s.defectQty ?: 0
            inv.sumNetProductionSec += s.netProductionSec ?: 0
        }

        return productMap.values.map { prod ->
            val operators = prod.operators.values.mapNotNull { inv ->
                val actual = inv.sumActualQty
                val netSec = inv.sumNetProductionSec
                val defect = inv.sumDefectQty
                val defectRate = if (actual > 0) kotlin.math.round(defect * 1000.0 / actual) / 10.0 else null
                val efficiency = if (actual > 0 && netSec > 0) {
                    kotlin.math.round(actual / (netSec / 3600.0)).toInt().toDouble()
                } else {
                    null
                }
                if (efficiency == null) return@mapNotNull null
                CuttingProductivityOperatorRowDto(
                    operatorUserId = inv.operatorUserId,
                    operatorName = inv.operatorName,
                    sessionCount = inv.sessionCount,
                    sumActualQty = actual,
                    sumDefectQty = defect,
                    sumNetProductionSec = netSec,
                    sumNetProductionMin = if (netSec > 0) netSec / 60 else 0,
                    defectRatePercent = defectRate,
                    efficiencyPerHour = efficiency,
                )
            }.sortedByDescending { it.efficiencyPerHour ?: 0.0 }
                .mapIndexed { index, row -> row.copy(rank = index + 1) }

            CuttingProductivityProductRankingDto(
                productCd = prod.productCd,
                productName = prod.productName,
                sumActualQty = prod.sumActualQty,
                sessionCount = prod.sessionCount,
                operatorCount = prod.operators.size,
                rankedOperatorCount = operators.size,
                operators = operators,
                topOperatorName = operators.firstOrNull()?.operatorName,
                topEfficiencyPerHour = operators.firstOrNull()?.efficiencyPerHour,
            )
        }.sortedByDescending { it.sumActualQty ?: 0 }
    }

    fun podiumOperators(ranking: CuttingProductivityProductRankingDto?): List<CuttingProductivityOperatorRowDto> {
        val top3 = ranking?.operators.orEmpty().filter { (it.rank ?: 99) <= 3 }
        val order = listOf(2, 1, 3)
        return order.mapNotNull { rank -> top3.find { it.rank == rank } }
    }

    fun sessionLineKey(session: CuttingProductivitySessionRowDto): String =
        session.operatorDisplayName?.trim()?.ifBlank { null }
            ?: session.mesOperatorName?.trim()?.ifBlank { null }
            ?: "—"

    fun buildOperatorProductRows(
        sessions: List<CuttingProductivitySessionRowDto>,
        lineKey: String,
    ): List<CuttingOperatorProductDisplayRow> {
        val map = linkedMapOf<String, ProductRowAgg>()
        for (s in sessions) {
            val key = sessionLineKey(s)
            if (key != lineKey) continue
            val productCd = s.productCd?.trim().orEmpty().ifBlank { continue }
            val productName = s.productName?.trim().orEmpty().ifBlank { productCd }
            val agg = map.getOrPut(productCd) { ProductRowAgg(productCd, productName) }
            agg.sessionCount += 1
            agg.sumActualQty += s.actualProductionQuantity ?: 0
            agg.sumDefectQty += s.defectQty ?: 0
            agg.sumNetProductionSec += s.netProductionSec ?: 0
        }
        return map.values.map { row ->
            val actual = row.sumActualQty
            val defect = row.sumDefectQty
            CuttingOperatorProductDisplayRow(
                productCd = row.productCd,
                productName = row.productName,
                sessionCount = row.sessionCount,
                sumActualQty = actual,
                sumDefectQty = defect,
                sumNetProductionSec = row.sumNetProductionSec,
                defectRatePercent = if (actual > 0) kotlin.math.round(defect * 1000.0 / actual) / 10.0 else null,
                avgEfficiencyPerHour = periodAvgEfficiencyFromBucket(actual, row.sumNetProductionSec),
            )
        }.sortedByDescending { it.sumActualQty }
    }

    fun toInspectionDailyRows(daily: List<CuttingProductivityDailyRowDto>): List<InspectionProductivityDailyRowDto> =
        daily.map { row ->
            InspectionProductivityDailyRowDto(
                day = row.day,
                sessionCount = row.sessionCount,
                completedSessionCount = row.completedSessionCount,
                sumActualQty = row.sumActualQty,
                sumDefectQty = row.sumDefectQty,
                sumNetProductionSec = row.sumNetProductionSec,
                sumNetProductionMin = row.sumNetProductionMin,
                sumPausedSec = row.sumPausedSec,
                sumPausedMin = row.sumPausedMin,
                defectRatePercent = row.defectRatePercent,
                efficiencyPerHour = row.efficiencyPerHour,
            )
        }

    private data class ProductAgg(
        val productCd: String,
        val productName: String,
        var sumActualQty: Int = 0,
        var sessionCount: Int = 0,
        val operators: MutableMap<String, OperatorAgg> = linkedMapOf(),
    )

    private data class OperatorAgg(
        val operatorUserId: Int?,
        val operatorName: String,
        var sessionCount: Int = 0,
        var sumActualQty: Int = 0,
        var sumDefectQty: Int = 0,
        var sumNetProductionSec: Int = 0,
    )

    private data class ProductRowAgg(
        val productCd: String,
        val productName: String,
        var sessionCount: Int = 0,
        var sumActualQty: Int = 0,
        var sumDefectQty: Int = 0,
        var sumNetProductionSec: Int = 0,
    )
}
