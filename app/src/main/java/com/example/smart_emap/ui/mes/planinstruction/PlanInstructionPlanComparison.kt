package com.example.smart_emap.ui.mes.planinstruction

import com.example.smart_emap.data.model.PlanBaselineComparisonDto
import com.example.smart_emap.data.model.PlanBaselineComparisonItemDto
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class PlanComparisonSummary(
    val baselinePlanTotal: Double? = null,
    val currentPlanTotal: Double? = null,
    val planDifference: Double? = null,
    val currentActualTotal: Double? = null,
    val actualDifference: Double? = null,
    val baselineDailyAverage: Double? = null,
    val baselinePlanAchievementRatio: Double? = null,
    val currentPlanAchievementRatio: Double? = null,
    val achievementRatioDifference: Double? = null,
    val productionStatus: String? = null,
    val lastActualDate: String? = null,
)

fun buildPlanComparisonSummary(
    summary: PlanBaselineComparisonDto?,
    items: List<PlanBaselineComparisonItemDto>?,
    workingDays: Int?,
): PlanComparisonSummary {
    if (summary == null) return PlanComparisonSummary()

    val actualTotal = summary.currentActualTotal ?: 0.0
    val currentPlanAchievement =
        summary.currentPlanTotal?.takeIf { it != 0.0 }?.let { actualTotal / it * 100.0 }
    val baselinePlanAchievement =
        summary.baselinePlanTotal?.takeIf { it != 0.0 }?.let { actualTotal / it * 100.0 }

    val workingDaysCount = workingDays?.takeIf { it > 0 }
        ?: autoWorkingDaysFromItems(items).coerceAtLeast(1)

    val baselineDailyAverage =
        summary.baselinePlanTotal?.let { total ->
            if (workingDaysCount > 0) total / workingDaysCount else null
        }

    val achievementRatioDifference =
        summary.actualDifference?.let { diff ->
            summary.baselinePlanTotal?.takeIf { it != 0.0 }?.let { diff / it * 100.0 }
        }

    val productionStatus = achievementRatioDifference?.let { ratio ->
        when {
            ratio > 5 -> "生産先行"
            ratio < -5 -> "生産遅れ"
            else -> "生産正常"
        }
    }

    val lastActualDate = items
        ?.filter { (it.currentActual ?: 0.0) > 0 }
        ?.maxByOrNull { it.planDate.orEmpty() }
        ?.planDate
        ?.take(10)
        ?.let { PlanInstructionLogic.formatDisplayDate(it) }

    return PlanComparisonSummary(
        baselinePlanTotal = summary.baselinePlanTotal,
        currentPlanTotal = summary.currentPlanTotal,
        planDifference = summary.planDifference,
        currentActualTotal = summary.currentActualTotal,
        actualDifference = summary.actualDifference,
        baselineDailyAverage = baselineDailyAverage,
        baselinePlanAchievementRatio = baselinePlanAchievement,
        currentPlanAchievementRatio = currentPlanAchievement,
        achievementRatioDifference = achievementRatioDifference,
        productionStatus = productionStatus,
        lastActualDate = lastActualDate,
    )
}

private fun autoWorkingDaysFromItems(items: List<PlanBaselineComparisonItemDto>?): Int {
    if (items.isNullOrEmpty()) return 1
    return items.mapNotNull { item ->
        val date = item.planDate?.take(10) ?: return@mapNotNull null
        runCatching {
            val parsed = java.time.LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            parsed.dayOfWeek.value
        }.getOrNull()?.let { dow ->
            if (dow in 1..5) date else null
        }
    }.distinct().count().coerceAtLeast(1)
}

fun formatPlanComparisonValue(
    value: Double?,
    suffix: String = "",
    fractionDigits: Int = 0,
): String {
    if (value == null || !value.isFinite()) return "-"
    val formatted = if (fractionDigits <= 0) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.${fractionDigits}f", value)
    }
    return formatted + suffix
}
