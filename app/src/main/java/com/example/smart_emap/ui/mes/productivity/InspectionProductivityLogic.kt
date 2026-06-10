package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionProductivityBucketDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorRowDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.InspectionProductivitySessionRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class IpaKpiTone { Indigo, Sky, Amber, Emerald, Violet }

enum class IpaKpiIcon { Sessions, Production, Defect, Efficiency, Runtime }

data class IpaKpiCard(
    val key: String,
    val label: String,
    val value: String,
    val hint: String,
    val tone: IpaKpiTone,
    val icon: IpaKpiIcon,
)

object InspectionProductivityLogic {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultDateRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        return start.format(isoFormatter) to end.format(isoFormatter)
    }

    fun fmtInt(value: Int?): String {
        if (value == null) return "0"
        return "%,d".format(value)
    }

    fun fmtPct(value: Double?): String {
        if (value == null || value.isNaN()) return "—"
        return String.format("%.1f%%", value)
    }

    fun fmtEfficiency(value: Double?): String {
        if (value == null || value.isNaN()) return "—"
        return "${value.toLong()}"
    }

    fun fmtDurationMin(min: Int?): String {
        val n = min ?: 0
        if (n <= 0) return "—"
        val h = n / 60
        val m = n % 60
        return when {
            h > 0 && m > 0 -> "${h}h${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun rangeLabel(start: String?, end: String?): String? {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return null
        return "${start.take(10)} ～ ${end.take(10)}"
    }

    fun chartDayLabel(isoDay: String?): String = isoDay?.take(10)?.substring(5) ?: ""

    fun rankMedal(rank: Int?): String = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> rank?.toString() ?: "—"
    }

    fun productRankOptionLabel(p: InspectionProductivityProductRankingDto): String {
        val name = p.productName?.trim().orEmpty()
        return if (name.isNotEmpty()) "${p.productCd} · $name" else p.productCd
    }

    fun buildKpiCards(summary: InspectionProductivityBucketDto?): List<IpaKpiCard> {
        val s = summary ?: emptyBucket()
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

    fun resolveProductRankList(data: InspectionProductivityAnalysisDataDto?): List<InspectionProductivityProductRankingDto> {
        val fromApi = data?.byProductInspectorRanking.orEmpty()
        if (fromApi.isNotEmpty()) return fromApi
        return buildProductInspectorRankingFromSessions(data?.sessions.orEmpty())
    }

    fun buildProductInspectorRankingFromSessions(
        sessions: List<InspectionProductivitySessionRowDto>,
    ): List<InspectionProductivityProductRankingDto> {
        val productMap = linkedMapOf<String, ProductAgg>()

        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty().ifBlank { "unknown" }
            val productName = s.productName?.trim().orEmpty().ifBlank { productCd }
            val prod = productMap.getOrPut(productCd) {
                ProductAgg(productCd, productName)
            }
            prod.sumActualQty += s.actualProductionQuantity ?: 0
            prod.sessionCount += 1

            val inspId = s.mesInspectorUserId
            val inspKey = inspId?.toString() ?: "none"
            val inspName = s.inspectorDisplayName?.trim()
                ?: s.mesInspectorName?.trim()
                ?: "—"
            val inv = prod.inspectors.getOrPut(inspKey) {
                InspectorAgg(inspId, inspName.ifBlank { "—" })
            }
            inv.sessionCount += 1
            inv.sumActualQty += s.actualProductionQuantity ?: 0
            inv.sumDefectQty += s.defectQty ?: 0
            inv.sumNetProductionSec += s.netProductionSec ?: 0
        }

        return productMap.values.map { prod ->
            val inspectors = prod.inspectors.values.mapNotNull { inv ->
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
                InspectionProductivityInspectorRowDto(
                    inspectorUserId = inv.inspectorUserId,
                    inspectorName = inv.inspectorName,
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

            InspectionProductivityProductRankingDto(
                productCd = prod.productCd,
                productName = prod.productName,
                sumActualQty = prod.sumActualQty,
                sessionCount = prod.sessionCount,
                inspectorCount = prod.inspectors.size,
                rankedInspectorCount = inspectors.size,
                inspectors = inspectors,
                topInspectorName = inspectors.firstOrNull()?.inspectorName,
                topEfficiencyPerHour = inspectors.firstOrNull()?.efficiencyPerHour,
            )
        }.sortedByDescending { it.sumActualQty ?: 0 }
    }

    fun podiumInspectors(ranking: InspectionProductivityProductRankingDto?): List<InspectionProductivityInspectorRowDto> {
        val top3 = ranking?.inspectors.orEmpty().filter { (it.rank ?: 99) <= 3 }
        val order = listOf(2, 1, 3)
        return order.mapNotNull { rank -> top3.find { it.rank == rank } }
    }

    private fun emptyBucket() = InspectionProductivityBucketDto()

    private data class ProductAgg(
        val productCd: String,
        val productName: String,
        var sumActualQty: Int = 0,
        var sessionCount: Int = 0,
        val inspectors: MutableMap<String, InspectorAgg> = linkedMapOf(),
    )

    private data class InspectorAgg(
        val inspectorUserId: Int?,
        val inspectorName: String,
        var sessionCount: Int = 0,
        var sumActualQty: Int = 0,
        var sumDefectQty: Int = 0,
        var sumNetProductionSec: Int = 0,
    )
}
