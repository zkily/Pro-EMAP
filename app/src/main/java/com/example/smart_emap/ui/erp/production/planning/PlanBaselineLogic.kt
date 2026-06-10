package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.PlanBaselineComparisonDto
import com.example.smart_emap.data.model.PlanBaselineFullComparisonItemDto
import com.example.smart_emap.ui.mes.planinstruction.buildPlanComparisonSummary
import com.example.smart_emap.ui.mes.planinstruction.formatPlanComparisonValue
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class PlanBaselineProcessTab(
    val name: String,
    val label: String,
    val items: List<PlanBaselineFullComparisonItemDto>,
    val count: Int,
)

data class PlanBaselineTabTotals(
    val currentPlan: Double,
    val planDiff: Double,
    val currentActual: Double,
    val actualDiff: Double,
)

data class PlanBaselineAdjustmentItem(
    val planDate: String,
    val processName: String,
    val planQuantity: Double,
    val tempPlanQuantity: String,
    val saving: Boolean = false,
    val deleting: Boolean = false,
)

object PlanBaselineLogic {
    val processOptions = listOf(
        "" to "全工程",
        "切断" to "切断",
        "面取" to "面取",
        "成型" to "成型",
        "メッキ" to "メッキ",
        "溶接" to "溶接",
        "検査" to "検査",
        "外注メッキ" to "外注メッキ",
        "外注溶接" to "外注溶接",
    )

    val fixedBaselineProcesses = setOf("メッキ", "検査")

    private val processOrder = listOf(
        "切断", "面取", "成型", "メッキ", "溶接", "検査", "外注メッキ", "外注溶接",
    )

    private val excludedProcesses = setOf(
        "溶接前検査", "外注検査前", "外注支給前", "外注支給前工程",
    )

    fun defaultBaselineMonth(): String =
        LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun formatBaselineMonthLabel(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return runCatching {
            val d = LocalDate.parse(isoDate.take(10))
            "${d.year}年${d.monthValue}月"
        }.getOrElse { isoDate.take(7) }
    }

    fun formatBaselineDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        return dateStr.take(10)
    }

    fun diffTone(value: Double?): PlanBaselineDiffTone = when {
        value == null -> PlanBaselineDiffTone.Neutral
        value > 0 -> PlanBaselineDiffTone.Positive
        value < 0 -> PlanBaselineDiffTone.Negative
        else -> PlanBaselineDiffTone.Zero
    }

    fun buildSummaryCards(summary: PlanBaselineComparisonDto?, items: List<PlanBaselineFullComparisonItemDto>?): List<ProductionKpiCard> {
        if (summary == null) {
            return listOf(
                kpi("基準計画合計", "-", "ベースライン計画合計", bluePurple()),
                kpi("現行計画合計", "-", "最新計画合計", tealGreen()),
                kpi("計画差異", "-", "現行計画 - ベースライン計画", orangeRed()),
                kpi("現行実績合計", "-", "最新実績合計", greenTeal()),
                kpi("計画対実績差", "-", "ベースライン計画 - 現行実績", purpleBlue()),
                kpi("計画達成率", "-", "現行実績 ÷ 現行計画", blueGreen()),
                kpi("達成率差異", "-", "計画対実績差 ÷ 基準計画合計", orangeRed()),
            )
        }

        val built = buildPlanComparisonSummary(summary, items?.map {
            com.example.smart_emap.data.model.PlanBaselineComparisonItemDto(
                planDate = it.planDate,
                currentActual = it.currentActual,
            )
        }, null)

        val currentPlanTotal = summary.currentPlanTotal ?: 0.0
        val baselinePlanTotal = summary.baselinePlanTotal ?: 0.0
        val planDifference = summary.planDifference ?: 0.0
        val actualDifference = summary.actualDifference ?: 0.0
        val planAchievement = if (currentPlanTotal == 0.0) null
        else (summary.currentActualTotal ?: 0.0) / currentPlanTotal * 100.0
        val achievementDifference = if (baselinePlanTotal == 0.0) null
        else actualDifference / baselinePlanTotal * 100.0

        return listOf(
            kpi("基準計画合計", formatPlanComparisonValue(summary.baselinePlanTotal), "ベースライン計画合計", bluePurple(), baselinePlanTotal < 0),
            kpi("現行計画合計", formatPlanComparisonValue(summary.currentPlanTotal), "最新計画合計", tealGreen(), currentPlanTotal < 0),
            kpi("計画差異", formatPlanComparisonValue(summary.planDifference), "現行計画 - ベースライン計画", orangeRed(), planDifference < 0),
            kpi("現行実績合計", if (summary.currentActualTotal == null) "-" else formatPlanComparisonValue(summary.currentActualTotal), "最新実績合計", greenTeal()),
            kpi("計画対実績差", formatPlanComparisonValue(summary.actualDifference), "ベースライン計画 - 現行実績", purpleBlue(), actualDifference < 0),
            kpi("計画達成率", formatPlanComparisonValue(planAchievement, "%", 1), "現行実績 ÷ 現行計画", blueGreen()),
            kpi("達成率差異", formatPlanComparisonValue(achievementDifference, "%", 1), "計画対実績差 ÷ 基準計画合計", orangeRed()),
        )
    }

    fun buildPdfExportTabs(tabs: List<PlanBaselineProcessTab>): List<PlanBaselineProcessTab> {
        val tabMap = tabs.associateBy { it.name }
        return pdfExportProcessOrder.mapNotNull { tabMap[it] }.filter { it.count > 0 }
    }

    private val pdfExportProcessOrder = listOf(
        "切断", "面取", "成型", "メッキ", "溶接", "検査", "外注メッキ", "外注溶接",
    )

    fun buildProcessTabs(items: List<PlanBaselineFullComparisonItemDto>): List<PlanBaselineProcessTab> {
        if (items.isEmpty()) return emptyList()
        val grouped = items
            .filter { !excludedProcesses.contains(it.processName.orEmpty()) }
            .groupBy { it.processName.orEmpty().ifBlank { "未指定" } }

        return grouped.entries.map { (name, group) ->
            PlanBaselineProcessTab(
                name = name,
                label = name,
                items = group.sortedBy { it.planDate.orEmpty() },
                count = group.size,
            )
        }.sortedWith(compareBy({ processOrderIndex(it.name) }, { it.label }))
    }

    fun buildTabTotals(items: List<PlanBaselineFullComparisonItemDto>): PlanBaselineTabTotals {
        var currentPlan = 0.0
        var planDiff = 0.0
        var currentActual = 0.0
        var actualDiff = 0.0
        var actualCount = 0
        var actualDiffCount = 0
        items.forEach { item ->
            currentPlan += item.currentPlan ?: 0.0
            planDiff += item.planDiff ?: 0.0
            item.currentActual?.let {
                currentActual += it
                actualCount += 1
            }
            item.actualDiff?.let {
                actualDiff += it
                actualDiffCount += 1
            }
        }
        return PlanBaselineTabTotals(
            currentPlan = currentPlan,
            planDiff = planDiff,
            currentActual = if (actualCount > 0) currentActual else 0.0,
            actualDiff = if (actualDiffCount > 0) actualDiff else 0.0,
        )
    }

    private fun processOrderIndex(name: String): Int {
        val idx = processOrder.indexOf(name)
        return if (idx >= 0) idx else 1000
    }

    private fun kpi(
        label: String,
        value: String,
        description: String,
        accent: androidx.compose.ui.graphics.Brush,
        isNegative: Boolean = false,
    ) = ProductionKpiCard(label, value, description, accent, isNegative)

    private fun bluePurple() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentBlue, ProductionPlanningColors.AccentPurple),
    )

    private fun tealGreen() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentTeal, ProductionPlanningColors.AccentGreen),
    )

    private fun orangeRed() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentOrange, ProductionPlanningColors.AccentRed),
    )

    private fun greenTeal() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentGreen, ProductionPlanningColors.AccentTeal),
    )

    private fun purpleBlue() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentPurple, ProductionPlanningColors.AccentBlue),
    )

    private fun blueGreen() = androidx.compose.ui.graphics.Brush.linearGradient(
        listOf(ProductionPlanningColors.AccentBlue, ProductionPlanningColors.AccentGreen),
    )
}

enum class PlanBaselineDiffTone { Positive, Negative, Zero, Neutral }
