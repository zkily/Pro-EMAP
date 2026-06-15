package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionProductivityBucketDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorMetricsDataDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorMetricsRowDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorRowDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.InspectionProductivitySessionRowDto
import com.example.smart_emap.data.model.UserListItemDto
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

enum class InspectorProductScope { NoWelding, WithWelding }

data class InspectorAvgRankRow(
    val inspectorUserId: Int?,
    val inspectorName: String?,
    val sessionCount: Int,
    val sumActualQty: Int,
    val sumDefectQty: Int,
    val sumNetProductionSec: Int,
    val defectRatePercent: Double?,
    val avgEfficiencyPerHour: Double?,
    val rank: Int,
)

data class InspectorProductDisplayRow(
    val productCd: String,
    val productName: String,
    val sessionCount: Int,
    val sumActualQty: Int,
    val sumDefectQty: Int,
    val sumNetProductionSec: Int,
    val defectRatePercent: Double?,
    val avgEfficiencyPerHour: Double?,
)

data class InspectionProductivityReportFilters(
    val startDate: String,
    val endDate: String,
    val inspectorLabel: String,
    val productLabel: String,
    val includeIncomplete: Boolean,
)

enum class InspectionProductivityReportCommand {
    PRINT_FULL,
    PRINT_DAILY,
    PRINT_DAILY_BATCH,
    PRINT_INSPECTOR,
    PRINT_INSPECTOR_METRICS,
    PRINT_INSPECTOR_PRODUCT_BATCH,
    PRINT_PRODUCT,
    PRINT_WELD_RANK,
    PRINT_PRODUCT_RANK,
}

enum class IpaReportMenuTone {
    INDIGO,
    SKY,
    TEAL,
    VIOLET,
    EMERALD,
    AMBER,
    ROSE,
}

data class IpaReportMenuItem(
    val command: InspectionProductivityReportCommand,
    val label: String,
    val hint: String,
    val tone: IpaReportMenuTone,
    val divided: Boolean = false,
)

data class InspectorMetricsPrepared(
    val defectHeaders: List<String>,
    val rows: List<InspectionProductivityInspectorMetricsRowDto>,
    val supportRow: InspectionProductivityInspectorMetricsRowDto,
    val totalRow: InspectionProductivityInspectorMetricsRowDto,
)

object InspectionProductivityLogic {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultDateRange(): Pair<String, String> {
        val end = MesCalendarUtils.jstToday()
        val start = MesCalendarUtils.shiftDateYmd(end, -29)
        return start to end
    }

    fun reportMenuItems(): List<IpaReportMenuItem> = listOf(
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_FULL,
            label = "印刷（全体）",
            hint = "全セクション一括出力",
            tone = IpaReportMenuTone.INDIGO,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_DAILY,
            label = "日別推移（印刷）",
            hint = "生産数 · 能率の推移",
            tone = IpaReportMenuTone.SKY,
            divided = true,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_DAILY_BATCH,
            label = "日別推移（検査員別・一括印刷）",
            hint = "検査員ごとに分割出力",
            tone = IpaReportMenuTone.TEAL,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_INSPECTOR,
            label = "検査員別（印刷）",
            hint = "検査員別サマリー",
            tone = IpaReportMenuTone.VIOLET,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_INSPECTOR_METRICS,
            label = "検査員別指標表（印刷）",
            hint = "不良内訳 · 時間 · 能率",
            tone = IpaReportMenuTone.EMERALD,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_INSPECTOR_PRODUCT_BATCH,
            label = "検査員別製品別（検査員別・一括印刷）",
            hint = "検査員ごとに製品一覧を出力",
            tone = IpaReportMenuTone.VIOLET,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_PRODUCT,
            label = "製品別（印刷）",
            hint = "製品別サマリー",
            tone = IpaReportMenuTone.EMERALD,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_WELD_RANK,
            label = "検査員平均能率ランキング（印刷）",
            hint = "溶接あり / なし比較",
            tone = IpaReportMenuTone.AMBER,
        ),
        IpaReportMenuItem(
            command = InspectionProductivityReportCommand.PRINT_PRODUCT_RANK,
            label = "製品別 · 検査員能率ランキング（印刷）",
            hint = "製品単位の順位表",
            tone = IpaReportMenuTone.ROSE,
        ),
    )

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
        return "${kotlin.math.round(value).toInt()}"
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
        val productMap = linkedMapOf<String, RankingProductAgg>()

        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty().ifBlank { "unknown" }
            val productName = s.productName?.trim().orEmpty().ifBlank { productCd }
            val prod = productMap.getOrPut(productCd) {
                RankingProductAgg(productCd, productName)
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

    fun isProcessFlagOn(value: Int?): Boolean = value == 1

    fun normalizeProductCdKey(cd: Any?): String {
        val raw = cd?.toString()?.trim().orEmpty()
        if (raw.isEmpty()) return ""
        if (raw.all { it.isDigit() }) {
            return raw.toIntOrNull()?.toString() ?: raw
        }
        return raw
    }

    private fun addWeldingProductCdKeys(set: MutableSet<String>, cd: Any?) {
        val raw = cd?.toString()?.trim().orEmpty()
        if (raw.isEmpty()) return
        set.add(raw)
        normalizeProductCdKey(raw).takeIf { it.isNotEmpty() }?.let { set.add(it) }
    }

    fun bomRowHasWeldingProcess(row: com.example.smart_emap.data.model.ProductProcessBomRowDto): Boolean =
        isProcessFlagOn(row.weldingProcess) ||
            isProcessFlagOn(row.outsourcedWeldingProcess) ||
            isProcessFlagOn(row.postInspectionWelding) ||
            isProcessFlagOn(row.prePlatingWelding)

    fun buildWeldingProductCdSet(rows: List<com.example.smart_emap.data.model.ProductProcessBomRowDto>): Set<String> {
        val set = linkedSetOf<String>()
        for (row in rows) {
            if (bomRowHasWeldingProcess(row)) {
                addWeldingProductCdKeys(set, row.productCd)
            }
        }
        return set
    }

    fun buildWeldingProductCdSetFromProductCds(productCds: List<Int>): Set<String> {
        val set = linkedSetOf<String>()
        for (cd in productCds) {
            addWeldingProductCdKeys(set, cd)
        }
        return set
    }

    fun sessionProductHasWelding(productCd: String, welding: Set<String>): Boolean {
        val raw = productCd.trim()
        if (raw.isEmpty()) return false
        if (welding.contains(raw)) return true
        val normalized = normalizeProductCdKey(raw)
        return normalized.isNotEmpty() && welding.contains(normalized)
    }

    fun periodAvgEfficiencyFromBucket(
        sumActualQty: Int?,
        sumNetProductionSec: Int?,
        efficiencyPerHour: Double? = null,
    ): Double? {
        val preset = efficiencyPerHour
        if (preset != null && preset.isFinite()) return kotlin.math.round(preset).toInt().toDouble()
        val qty = sumActualQty ?: 0
        val sec = sumNetProductionSec ?: 0
        if (qty <= 0 || sec <= 0) return null
        return kotlin.math.round(qty / (sec / 3600.0)).toInt().toDouble()
    }

    fun periodAvgEfficiencyFromBucket(row: InspectionProductivityInspectorRowDto): Double? =
        periodAvgEfficiencyFromBucket(
            row.sumActualQty,
            row.sumNetProductionSec,
            row.efficiencyPerHour,
        )

    fun inspectorSectionAvgEfficiency(rows: List<InspectionProductivityInspectorRowDto>): Double? {
        var totalQty = 0
        var totalSec = 0
        for (row in rows) {
            totalQty += row.sumActualQty ?: 0
            totalSec += row.sumNetProductionSec ?: 0
        }
        return periodAvgEfficiencyFromBucket(totalQty, totalSec)
    }

    fun buildInspectorAvgRankBySessions(
        sessions: List<InspectionProductivitySessionRowDto>,
        includeProduct: (String) -> Boolean,
    ): List<InspectorAvgRankRow> {
        val map = linkedMapOf<String, InspectorAgg>()
        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty()
            if (productCd.isEmpty() || !includeProduct(productCd)) continue
            val inspId = s.mesInspectorUserId
            val inspKey = inspId?.toString() ?: "none"
            val inspName = s.inspectorDisplayName?.trim()
                ?: s.mesInspectorName?.trim()
                ?: "—"
            val agg = map.getOrPut(inspKey) { InspectorAgg(inspId, inspName.ifBlank { "—" }) }
            agg.sessionCount += 1
            agg.sumActualQty += s.actualProductionQuantity ?: 0
            agg.sumDefectQty += s.defectQty ?: 0
            agg.sumNetProductionSec += s.netProductionSec ?: 0
        }
        val rows = map.values.mapNotNull { inv ->
            val avg = periodAvgEfficiencyFromBucket(inv.sumActualQty, inv.sumNetProductionSec) ?: return@mapNotNull null
            val actual = inv.sumActualQty
            val defect = inv.sumDefectQty
            val defectRate = if (actual > 0) kotlin.math.round(defect * 1000.0 / actual) / 10.0 else null
            InspectorAvgRankRow(
                inspectorUserId = inv.inspectorUserId,
                inspectorName = inv.inspectorName,
                sessionCount = inv.sessionCount,
                sumActualQty = actual,
                sumDefectQty = defect,
                sumNetProductionSec = inv.sumNetProductionSec,
                defectRatePercent = defectRate,
                avgEfficiencyPerHour = avg,
                rank = 0,
            )
        }.sortedByDescending { it.avgEfficiencyPerHour ?: -1.0 }
        return rows.mapIndexed { index, row -> row.copy(rank = index + 1) }
    }

    fun buildInspectorProductRows(
        sessions: List<InspectionProductivitySessionRowDto>,
        inspectorKey: String,
        includeProduct: (String) -> Boolean,
    ): List<InspectorProductDisplayRow> {
        val map = linkedMapOf<String, ProductAgg>()
        for (s in sessions) {
            val productCd = s.productCd?.trim().orEmpty()
            if (productCd.isEmpty() || !includeProduct(productCd)) continue
            val inspKey = s.mesInspectorUserId?.toString() ?: "none"
            if (inspKey != inspectorKey) continue
            val prod = map.getOrPut(productCd) {
                ProductAgg(productCd, s.productName?.trim().orEmpty().ifBlank { productCd })
            }
            prod.sessionCount += 1
            prod.sumActualQty += s.actualProductionQuantity ?: 0
            prod.sumDefectQty += s.defectQty ?: 0
            prod.sumNetProductionSec += s.netProductionSec ?: 0
        }
        return map.values.map { row ->
            val actual = row.sumActualQty
            val defect = row.sumDefectQty
            InspectorProductDisplayRow(
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

    fun inspectorRankRowKey(row: InspectorAvgRankRow): String =
        row.inspectorUserId?.toString() ?: "none"

    fun buildSessionsCsv(
        data: InspectionProductivityAnalysisDataDto,
        filters: InspectionProductivityReportFilters,
    ): String {
        val lines = buildList {
            addAll(metaCsvLines(filters))
            add(csvLine(listOf("生産日", "検査員", "CD", "製品名", "生産数", "不良数", "不良率", "能率(本/時)", "正味稼働(分)", "停止(分)", "状態")))
            for (row in data.sessions.orEmpty()) {
                add(
                    csvLine(
                        listOf(
                            row.productionDay.orEmpty(),
                            row.inspectorDisplayName ?: row.mesInspectorName.orEmpty(),
                            row.productCd.orEmpty(),
                            row.productName.orEmpty(),
                            row.actualProductionQuantity ?: 0,
                            row.defectQty ?: 0,
                            fmtPct(row.defectRatePercent),
                            fmtEfficiency(row.efficiencyPerHour),
                            row.netProductionMin?.toString().orEmpty(),
                            row.pausedMin?.toString().orEmpty(),
                            if (row.isCompleted == true) "確定" else "未確定",
                        ),
                    ),
                )
            }
        }
        return lines.joinToString("\r\n")
    }

    fun sessionsCsvFilename(filters: InspectionProductivityReportFilters): String =
        "検査生産性分析_${filters.startDate}_${filters.endDate}_セッション.csv"

    private val defaultInspectorMetricsDefectHeaders = listOf(
        "加工キズ",
        "油タレ",
        "曲げ不良",
        "カ他",
        "メッキ後キズ",
        "モヤ/カブリ",
        "ニッケル",
        "接触",
        "メ他",
        "溶接不良",
        "サビ",
        "生地不良",
        "外注メッキ不良",
        "外注溶接不良",
        "W検査　廃棄",
    )

    fun resolveInspectorMetricsDefectHeaders(
        metrics: InspectionProductivityInspectorMetricsDataDto?,
    ): List<String> = metrics?.defectHeaders?.takeIf { it.isNotEmpty() } ?: defaultInspectorMetricsDefectHeaders

    fun metricsDefectHeaderLabel(header: String): String = when (header) {
        "W検査　廃棄" -> "W検査"
        "モヤ/カブリ" -> "モヤ・カブリ"
        else -> header
    }

    fun sumInspectorMetricsDefectQty(
        row: InspectionProductivityInspectorMetricsRowDto,
        defectHeaders: List<String>,
    ): Int = defectHeaders.sumOf { header -> row.defects?.get(header) ?: 0 }

    fun inspectorMetricsRowHasActivity(
        row: InspectionProductivityInspectorMetricsRowDto?,
        defectHeaders: List<String>,
    ): Boolean {
        if (row == null) return false
        if ((row.sumInspectionQty ?: 0) > 0) return true
        if ((row.shiftHours ?: 0.0) > 0.0 || (row.workHours ?: 0.0) > 0.0) return true
        return sumInspectorMetricsDefectQty(row, defectHeaders) > 0
    }

    fun formatInspectorOptionLabel(user: UserListItemDto): String =
        user.fullName?.trim().orEmpty()

    fun prepareInspectorMetricsForDisplay(
        metrics: InspectionProductivityInspectorMetricsDataDto?,
        inspectorOptions: List<UserListItemDto>,
    ): InspectorMetricsPrepared? {
        if (metrics == null) return null
        val defectHeaders = resolveInspectorMetricsDefectHeaders(metrics)
        val labelById = inspectorOptions.mapNotNull { user ->
            val id = user.id ?: return@mapNotNull null
            id to formatInspectorOptionLabel(user)
        }.toMap()

        val rows = metrics.rows.orEmpty()
            .mapNotNull { row ->
                val userId = row.inspectorUserId ?: return@mapNotNull null
                val label = labelById[userId]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                row.copy(inspectorName = label)
            }
            .sortedBy { it.inspectorName.orEmpty() }

        val supportRow = metrics.supportRow
        val supportVisible = inspectorMetricsRowHasActivity(supportRow, defectHeaders)
        val emptySupport = InspectionProductivityInspectorMetricsRowDto(inspectorName = "応援")

        return InspectorMetricsPrepared(
            defectHeaders = defectHeaders,
            rows = rows,
            supportRow = if (supportVisible) supportRow ?: emptySupport else emptySupport,
            totalRow = finalizeInspectorMetricsTotals(rows, defectHeaders),
        )
    }

    fun fmtMetricHours(value: Double?): String {
        if (value == null || value.isNaN()) return "—"
        return String.format("%.2f", value)
    }

    fun fmtMetricEfficiencyDecimal(value: Double?): String {
        if (value == null || value.isNaN()) return "—"
        return String.format("%.1f", value)
    }

    fun fmtMetricQtyDisplay(value: Int?): String {
        val n = value ?: 0
        if (n <= 0) return "—"
        return fmtInt(n)
    }

    private fun finalizeInspectorMetricsTotals(
        rows: List<InspectionProductivityInspectorMetricsRowDto>,
        defectHeaders: List<String>,
    ): InspectionProductivityInspectorMetricsRowDto {
        val defects = defectHeaders.associateWith { header ->
            rows.sumOf { it.defects?.get(header) ?: 0 }
        }
        val shiftHours = roundMetricHours(rows.sumOf { it.shiftHours ?: 0.0 })
        val breakHours = roundMetricHours(rows.sumOf { it.breakHours ?: 0.0 })
        val stopHours = roundMetricHours(rows.sumOf { it.stopHours ?: 0.0 })
        val targetWorkHours = roundMetricHours(shiftHours - breakHours)
        val workHours = roundMetricHours(rows.sumOf { it.workHours ?: 0.0 })
        val sumInspectionQty = rows.sumOf { it.sumInspectionQty ?: 0 }
        val workRatePercent = if (targetWorkHours > 0.0) {
            kotlin.math.round(workHours / targetWorkHours * 1000.0) / 10.0
        } else {
            null
        }
        val efficiencyPerHour = if (workHours > 0.0 && sumInspectionQty > 0) {
            kotlin.math.round(sumInspectionQty / workHours * 10.0) / 10.0
        } else {
            null
        }
        val operatingRatePercent = if (shiftHours > 0.0) {
            kotlin.math.round(workHours / shiftHours * 1000.0) / 10.0
        } else {
            null
        }
        return InspectionProductivityInspectorMetricsRowDto(
            inspectorName = "合計",
            defects = defects,
            shiftHours = shiftHours,
            breakHours = breakHours,
            stopHours = stopHours,
            targetWorkHours = targetWorkHours,
            workHours = workHours,
            workRatePercent = workRatePercent,
            sumInspectionQty = sumInspectionQty,
            efficiencyPerHour = efficiencyPerHour,
            operatingRatePercent = operatingRatePercent,
        )
    }

    private fun roundMetricHours(value: Double): Double =
        kotlin.math.round(value * 100.0) / 100.0

    private fun metaCsvLines(filters: InspectionProductivityReportFilters): List<String> {
        val printedAt = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        )
        return listOf(
            csvLine(listOf("# 検査工程 — 生産性分析")),
            csvLine(listOf("# 集計期間", "${filters.startDate} ～ ${filters.endDate}")),
            csvLine(listOf("# 検査員", filters.inspectorLabel)),
            csvLine(listOf("# 製品", filters.productLabel)),
            csvLine(listOf("# 未確定を含む", if (filters.includeIncomplete) "はい" else "いいえ")),
            csvLine(listOf("# 出力日時", printedAt)),
            "",
        )
    }

    private fun csvLine(cells: List<Any>): String =
        cells.joinToString(",") { escapeCsvCell(it) }

    private fun escapeCsvCell(value: Any): String {
        val text = value.toString()
        return if (text.contains('"') || text.contains(',') || text.contains('\n') || text.contains('\r')) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }

    private fun emptyBucket() = InspectionProductivityBucketDto()

    private data class RankingProductAgg(
        val productCd: String,
        val productName: String,
        var sumActualQty: Int = 0,
        var sessionCount: Int = 0,
        val inspectors: MutableMap<String, InspectorAgg> = linkedMapOf(),
    )

    private data class ProductAgg(
        val productCd: String,
        val productName: String,
        var sessionCount: Int = 0,
        var sumActualQty: Int = 0,
        var sumDefectQty: Int = 0,
        var sumNetProductionSec: Int = 0,
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
