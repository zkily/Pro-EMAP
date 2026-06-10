package com.example.smart_emap.ui.mes.utilization

import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.model.InspectionUtilizationDailyInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationSummaryDto
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class IuaKpiTone { Green, Blue, Indigo, Amber, Violet }

data class IuaKpiCard(
    val key: String,
    val label: String,
    val value: String,
    val hint: String,
    val tone: IuaKpiTone,
)

object InspectionUtilizationLogic {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultDateRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        return start.format(isoFormatter) to end.format(isoFormatter)
    }

    fun fmtPct(value: Double?): String {
        if (value == null || value.isNaN()) return "—"
        return String.format("%.1f%%", value)
    }

    fun fmtMin(value: Int?): String {
        if (value == null || value <= 0) return "—"
        return "${value}m"
    }

    fun fmtDuration(min: Int?): String {
        if (min == null || min <= 0) return "0m"
        val h = min / 60
        val m = min % 60
        return when {
            h <= 0 -> "${m}m"
            m > 0 -> "${h}h${m}m"
            else -> "${h}h"
        }
    }

    fun fmtHours(sec: Int?): String {
        if (sec == null || sec <= 0) return "—"
        return String.format("%.1f", sec / 3600.0)
    }

    fun fmtInt(value: Int?): String {
        if (value == null) return "—"
        return value.toLocaleString()
    }

    private fun Int.toLocaleString(): String = "%,d".format(this)

    fun buildKpiCards(summary: InspectionUtilizationSummaryDto?, calendarWorkdays: Int?): List<IuaKpiCard> {
        if (summary == null) {
            return listOf(
                kpi("util", "平均稼働率", "—", "所定7.6h内 / 出勤日基準", IuaKpiTone.Green),
                kpi("calendar", "カレンダー稼働率", "—", "会社稼働日 — 日", IuaKpiTone.Blue),
                kpi("net", "正味稼働合計", "—", "— セッション", IuaKpiTone.Indigo),
                kpi("overtime", "残業合計", "—", "所定内 —", IuaKpiTone.Amber),
                kpi("inspectors", "検査員", "—", "対象人数", IuaKpiTone.Violet),
            )
        }
        val unassignedHint = summary.unassignedSessionCount?.takeIf { it > 0 }
            ?.let { "未割当 ${it} 件" } ?: "対象人数"
        return listOf(
            kpi("util", "平均稼働率", fmtPct(summary.utilizationPercent), "所定7.6h内 / 出勤日基準", IuaKpiTone.Green),
            kpi(
                "calendar",
                "カレンダー稼働率",
                fmtPct(summary.calendarUtilizationPercent),
                "会社稼働日 ${calendarWorkdays ?: summary.calendarWorkdaysInRange ?: "—"} 日",
                IuaKpiTone.Blue,
            ),
            kpi(
                "net",
                "正味稼働合計",
                fmtDuration(summary.sumNetProductionMin),
                "${fmtInt(summary.sessionCount)} セッション",
                IuaKpiTone.Indigo,
            ),
            kpi(
                "overtime",
                "残業合計",
                fmtDuration(summary.overtimeMin),
                "所定内 ${fmtDuration(summary.regularMin)}",
                IuaKpiTone.Amber,
            ),
            kpi("inspectors", "検査員", fmtInt(summary.inspectorCount), unassignedHint, IuaKpiTone.Violet),
        )
    }

    fun filterDailyRows(
        rows: List<InspectionUtilizationDailyInspectorRowDto>,
        inspectorUserId: Int?,
    ): List<InspectionUtilizationDailyInspectorRowDto> {
        if (inspectorUserId == null) return rows
        return rows.filter { it.inspectorUserId == inspectorUserId }
    }

    fun dayCategoryLabel(row: InspectionUtilizationDailyInspectorRowDto): String = when {
        row.isExtraWorkday == true -> "土日出勤"
        row.isScheduledWorkday == false -> "休日実績"
        else -> "平日"
    }

    fun dayCategoryTone(row: InspectionUtilizationDailyInspectorRowDto): IuaDayCategoryTone = when {
        row.isExtraWorkday == true -> IuaDayCategoryTone.ExtraWorkday
        row.isScheduledWorkday == false -> IuaDayCategoryTone.HolidayActual
        else -> IuaDayCategoryTone.Weekday
    }

    fun rangeLabel(start: String?, end: String?): String? {
        if (start.isNullOrBlank() || end.isNullOrBlank()) return null
        return "${start.take(10)} ～ ${end.take(10)}"
    }

    fun chartDayLabel(isoDay: String?): String = isoDay?.take(10)?.substring(5) ?: ""

    private fun kpi(key: String, label: String, value: String, hint: String, tone: IuaKpiTone) =
        IuaKpiCard(key, label, value, hint, tone)

    fun toneBrush(tone: IuaKpiTone) = when (tone) {
        IuaKpiTone.Green -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(ProductionPlanningColors.AccentGreen, ProductionPlanningColors.AccentTeal),
        )
        IuaKpiTone.Blue -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(ProductionPlanningColors.AccentBlue, ProductionPlanningColors.AccentTeal),
        )
        IuaKpiTone.Indigo -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(ProductionPlanningColors.AccentPurple, ProductionPlanningColors.AccentBlue),
        )
        IuaKpiTone.Amber -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(ProductionPlanningColors.AccentOrange, ProductionPlanningColors.AccentRed),
        )
        IuaKpiTone.Violet -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(ProductionPlanningColors.AccentPurple, ProductionPlanningColors.AccentBlue),
        )
    }
}

enum class IuaDayCategoryTone { Weekday, ExtraWorkday, HolidayActual }
