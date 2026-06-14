package com.example.smart_emap.ui.mes.utilization

import com.example.smart_emap.core.mes.MesCalendarUtils
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.model.InspectionUtilizationDailyInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationDailyRowDto
import com.example.smart_emap.data.model.InspectionUtilizationInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationSessionGapDto
import com.example.smart_emap.data.model.InspectionUtilizationSummaryDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.round

enum class InspectionUtilizationReportCommand {
    PRINT_FULL,
    PRINT_DAILY,
    PRINT_DAILY_BATCH,
}

enum class IuaReportMenuTone {
    GREEN,
    SKY,
    TEAL,
}

data class IuaReportMenuItem(
    val command: InspectionUtilizationReportCommand,
    val label: String,
    val hint: String,
    val tone: IuaReportMenuTone,
    val divided: Boolean = false,
)

data class IuaInspectorOption(val id: Int, val name: String)

data class IuaChartDailyRow(
    val day: String,
    val utilizationPercent: Double?,
    val sumNetProductionMin: Int?,
    val overtimeMin: Int?,
    val sumOvertimeSec: Int?,
)

data class IuaReportFilters(
    val startDate: String,
    val endDate: String,
    val inspectorLabel: String,
    val includeIncomplete: Boolean,
)

enum class IuaKpiTone { Green, Blue, Indigo, Amber, Violet }

enum class IuaKpiIcon { Utilization, Calendar, NetTime, Overtime, Inspectors }

data class IuaKpiCard(
    val key: String,
    val label: String,
    val value: String,
    val hint: String,
    val tone: IuaKpiTone,
    val icon: IuaKpiIcon,
)

object InspectionUtilizationLogic {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultDateRange(): Pair<String, String> {
        val end = MesCalendarUtils.jstToday()
        val start = MesCalendarUtils.shiftDateYmd(end, -29)
        return start to end
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

    fun fmtScheduledHours(hours: Double?): String {
        if (hours == null || hours <= 0) return "—"
        return String.format("%.1f", hours)
    }

    fun fmtInt(value: Int?): String {
        if (value == null) return "—"
        return "%,d".format(value)
    }

    fun buildKpiCards(summary: InspectionUtilizationSummaryDto?, calendarWorkdays: Int?): List<IuaKpiCard> {
        if (summary == null) {
            return listOf(
                kpi("util", "平均稼働率", "—", "出勤日基準7.6h", IuaKpiTone.Green, IuaKpiIcon.Utilization),
                kpi("calendar", "カレンダー稼働率", "—", "会社稼働日 — 日", IuaKpiTone.Blue, IuaKpiIcon.Calendar),
                kpi("net", "正味稼働合計", "—", "— セッション", IuaKpiTone.Indigo, IuaKpiIcon.NetTime),
                kpi("overtime", "残業合計", "—", "所定内 —", IuaKpiTone.Amber, IuaKpiIcon.Overtime),
                kpi("inspectors", "検査員", "—", "対象人数", IuaKpiTone.Violet, IuaKpiIcon.Inspectors),
            )
        }
        val unassignedHint = summary.unassignedSessionCount?.takeIf { it > 0 }
            ?.let { "未割当 ${it} 件" } ?: "対象人数"
        return listOf(
            kpi("util", "平均稼働率", fmtPct(summary.utilizationPercent), "出勤日基準7.6h", IuaKpiTone.Green, IuaKpiIcon.Utilization),
            kpi(
                "calendar",
                "カレンダー稼働率",
                fmtPct(summary.calendarUtilizationPercent),
                "会社稼働日 ${calendarWorkdays ?: summary.calendarWorkdaysInRange ?: "—"} 日",
                IuaKpiTone.Blue,
                IuaKpiIcon.Calendar,
            ),
            kpi(
                "net",
                "正味稼働合計",
                fmtDuration(summary.sumNetProductionMin),
                "${fmtInt(summary.sessionCount)} セッション",
                IuaKpiTone.Indigo,
                IuaKpiIcon.NetTime,
            ),
            kpi(
                "overtime",
                "残業合計",
                fmtDuration(summary.overtimeMin),
                "所定内 ${fmtDuration(summary.regularMin)}",
                IuaKpiTone.Amber,
                IuaKpiIcon.Overtime,
            ),
            kpi("inspectors", "検査員", fmtInt(summary.inspectorCount), unassignedHint, IuaKpiTone.Violet, IuaKpiIcon.Inspectors),
        )
    }

    fun buildInspectorOptions(rows: List<InspectionUtilizationInspectorRowDto>): List<IuaInspectorOption> {
        val seen = mutableSetOf<Int>()
        val options = mutableListOf<IuaInspectorOption>()
        for (row in rows) {
            val id = row.inspectorUserId ?: continue
            val name = row.inspectorName?.trim().orEmpty()
            if (name.isBlank() || seen.contains(id)) continue
            seen.add(id)
            options.add(IuaInspectorOption(id, name))
        }
        return options.sortedBy { it.name }
    }

    fun filterDailyRows(
        rows: List<InspectionUtilizationDailyInspectorRowDto>,
        inspectorUserId: Int?,
    ): List<InspectionUtilizationDailyInspectorRowDto> {
        if (inspectorUserId == null) return rows
        return rows.filter { it.inspectorUserId == inspectorUserId }
    }

    fun filterByInspector(
        rows: List<InspectionUtilizationInspectorRowDto>,
        inspectorUserId: Int?,
    ): List<InspectionUtilizationInspectorRowDto> {
        if (inspectorUserId == null) return rows
        return rows.filter { it.inspectorUserId == inspectorUserId }
    }

    fun buildChartDailyRows(
        data: InspectionUtilizationAnalysisDataDto?,
        filterInspectorId: Int?,
    ): List<IuaChartDailyRow> {
        if (data == null) return emptyList()
        if (filterInspectorId == null) {
            return data.daily.orEmpty().map { it.toChartRow() }
        }
        return data.dailyByInspector.orEmpty()
            .filter { it.inspectorUserId == filterInspectorId }
            .sortedBy { it.day.orEmpty() }
            .map { it.toChartRow() }
    }

    fun chartBadgeLabel(filterInspectorId: Int?, inspectorOptions: List<IuaInspectorOption>): String {
        if (filterInspectorId == null) return "検査員合算"
        return inspectorOptions.firstOrNull { it.id == filterInspectorId }?.name ?: "検査員別"
    }

    fun overtimeChartTotalLabel(rows: List<IuaChartDailyRow>): String {
        val totalMin = rows.sumOf { overtimeMinFromRow(it) }
        return fmtDuration(totalMin)
    }

    fun overtimeMinFromRow(row: IuaChartDailyRow): Int {
        if (row.overtimeMin != null && row.overtimeMin > 0) return row.overtimeMin
        val sec = row.sumOvertimeSec ?: 0
        return if (sec > 0) round(sec / 60.0).toInt() else 0
    }

    fun overtimeHoursFromRow(row: IuaChartDailyRow): Float {
        val min = overtimeMinFromRow(row)
        if (min <= 0) return 0f
        return (round(min / 60.0 * 10) / 10.0).toFloat()
    }

    fun netHoursFromRow(row: IuaChartDailyRow): Float {
        val min = row.sumNetProductionMin ?: 0
        if (min <= 0) return 0f
        return (round(min / 60.0 * 10) / 10.0).toFloat()
    }

    fun avgUtilizationPercent(rows: List<IuaChartDailyRow>): Double? {
        val values = rows.mapNotNull { it.utilizationPercent }.filter { !it.isNaN() }
        if (values.isEmpty()) return null
        return round(values.average() * 10) / 10.0
    }

    fun sumOvertimeMinFromDaily(rows: List<IuaChartDailyRow>): Int =
        rows.sumOf { overtimeMinFromRow(it) }

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

    fun inspectorRankTone(index: Int): IuaRankTone = when (index) {
        0 -> IuaRankTone.Gold
        1 -> IuaRankTone.Silver
        2 -> IuaRankTone.Bronze
        else -> IuaRankTone.None
    }

    fun utilPillTone(pct: Double?): IuaUtilPillTone {
        val v = pct ?: 0.0
        return when {
            v >= 85 -> IuaUtilPillTone.High
            v >= 60 -> IuaUtilPillTone.Mid
            v > 0 -> IuaUtilPillTone.Low
            else -> IuaUtilPillTone.None
        }
    }

    fun sessionProductLabel(s: InspectionUtilizationSessionGapDto): String {
        val cd = s.productCd?.trim().orEmpty()
        val name = s.productName?.trim().orEmpty()
        return when {
            cd.isNotBlank() && name.isNotBlank() -> "$cd（$name）"
            cd.isNotBlank() -> cd
            name.isNotBlank() -> name
            else -> "製品情報なし"
        }
    }

    fun sessionTimeGapReason(s: InspectionUtilizationSessionGapDto): String {
        val started = s.mesProductionStartedAt
        val ended = s.mesProductionEndedAt
        return when {
            started.isNullOrBlank() && ended.isNullOrBlank() -> "開始・終了時刻なし"
            started.isNullOrBlank() -> "開始時刻なし"
            ended.isNullOrBlank() -> "終了時刻なし"
            else -> "正味秒未設定"
        }
    }

    fun reportMenuItems(): List<IuaReportMenuItem> = listOf(
        IuaReportMenuItem(
            command = InspectionUtilizationReportCommand.PRINT_FULL,
            label = "印刷（全体）",
            hint = "KPI · グラフ · 集計表",
            tone = IuaReportMenuTone.GREEN,
        ),
        IuaReportMenuItem(
            command = InspectionUtilizationReportCommand.PRINT_DAILY,
            label = "日別稼働率推移（印刷）",
            hint = "現在の検査員フィルタ反映",
            tone = IuaReportMenuTone.SKY,
            divided = true,
        ),
        IuaReportMenuItem(
            command = InspectionUtilizationReportCommand.PRINT_DAILY_BATCH,
            label = "日別稼働率推移（検査員別・一括印刷）",
            hint = "合算 + 検査員ごとに分割",
            tone = IuaReportMenuTone.TEAL,
        ),
    )

    private fun InspectionUtilizationDailyRowDto.toChartRow() = IuaChartDailyRow(
        day = day.orEmpty(),
        utilizationPercent = utilizationPercent,
        sumNetProductionMin = sumNetProductionMin,
        overtimeMin = overtimeMin,
        sumOvertimeSec = sumOvertimeSec,
    )

    private fun InspectionUtilizationDailyInspectorRowDto.toChartRow() = IuaChartDailyRow(
        day = day.orEmpty(),
        utilizationPercent = utilizationPercent,
        sumNetProductionMin = sumNetProductionMin,
        overtimeMin = overtimeMin,
        sumOvertimeSec = sumOvertimeSec,
    )

    private fun kpi(
        key: String,
        label: String,
        value: String,
        hint: String,
        tone: IuaKpiTone,
        icon: IuaKpiIcon,
    ) = IuaKpiCard(key, label, value, hint, tone, icon)
}

enum class IuaDayCategoryTone { Weekday, ExtraWorkday, HolidayActual }

enum class IuaRankTone { Gold, Silver, Bronze, None }

enum class IuaUtilPillTone { High, Mid, Low, None }
