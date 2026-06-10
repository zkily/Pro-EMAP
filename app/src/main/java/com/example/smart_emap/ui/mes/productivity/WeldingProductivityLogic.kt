package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.data.model.InspectionProductivityDailyRowDto
import com.example.smart_emap.data.model.WeldingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.WeldingProductivityBucketDto
import com.example.smart_emap.data.model.WeldingProductivityDailyRowDto
import com.example.smart_emap.data.model.WeldingProductivityOperatorRowDto
import com.example.smart_emap.data.model.WeldingProductivityProductRankingDto
import com.example.smart_emap.data.model.WeldingProductivitySessionRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WeldingProductivityLogic {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultDateRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        return start.format(isoFormatter) to end.format(isoFormatter)
    }

    fun fmtInt(value: Int?): String = InspectionProductivityLogic.fmtInt(value)

    fun fmtPct(value: Double?): String = InspectionProductivityLogic.fmtPct(value)

    fun fmtEfficiency(value: Double?): String = InspectionProductivityLogic.fmtEfficiency(value)

    fun fmtDurationMin(min: Int?): String = InspectionProductivityLogic.fmtDurationMin(min)

    fun rangeLabel(start: String?, end: String?): String? = InspectionProductivityLogic.rangeLabel(start, end)

    fun rankMedal(rank: Int?): String = InspectionProductivityLogic.rankMedal(rank)

    fun productRankOptionLabel(p: WeldingProductivityProductRankingDto): String {
        val name = p.productName?.trim().orEmpty()
        return if (name.isNotEmpty()) "${p.productCd} · $name" else p.productCd
    }

    fun buildKpiCards(summary: WeldingProductivityBucketDto?): List<IpaKpiCard> {
        val s = summary ?: WeldingProductivityBucketDto()
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
                hint = "個 / 時間",
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

    fun resolveProductRankList(data: WeldingProductivityAnalysisDataDto?): List<WeldingProductivityProductRankingDto> {
        val fromApi = data?.byProductOperatorRanking.orEmpty()
        if (fromApi.isNotEmpty()) return fromApi
        return buildProductOperatorRankingFromSessions(data?.sessions.orEmpty())
    }

    fun buildProductOperatorRankingFromSessions(
        sessions: List<WeldingProductivitySessionRowDto>,
    ): List<WeldingProductivityProductRankingDto> {
        val productMap = linkedMapOf<String, ProductAgg>()

        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty().ifBlank { "unknown" }
            val productName = s.productName?.trim().orEmpty().ifBlank { productCd }
            val prod = productMap.getOrPut(productCd) {
                ProductAgg(productCd, productName)
            }
            prod.sumActualQty += s.actualProductionQuantity ?: 0
            prod.sessionCount += 1

            val opId = s.mesOperatorUserId
            val opKey = opId?.toString() ?: "none"
            val opName = s.operatorDisplayName?.trim()
                ?: s.mesOperatorName?.trim()
                ?: "—"
            val inv = prod.operators.getOrPut(opKey) {
                OperatorAgg(opId, opName.ifBlank { "—" })
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
                WeldingProductivityOperatorRowDto(
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

            WeldingProductivityProductRankingDto(
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

    fun podiumOperators(ranking: WeldingProductivityProductRankingDto?): List<WeldingProductivityOperatorRowDto> {
        val top3 = ranking?.operators.orEmpty().filter { (it.rank ?: 99) <= 3 }
        val order = listOf(2, 1, 3)
        return order.mapNotNull { rank -> top3.find { it.rank == rank } }
    }

    fun toInspectionDailyRows(daily: List<WeldingProductivityDailyRowDto>): List<InspectionProductivityDailyRowDto> =
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
}
