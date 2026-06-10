package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class InspectionUtilizationAnalysisResponse(
    val success: Boolean? = null,
    val data: InspectionUtilizationAnalysisDataDto? = null,
    val message: String? = null,
)

data class InspectionUtilizationAnalysisDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    @Json(name = "include_incomplete") val includeIncomplete: Boolean? = null,
    @Json(name = "standard_workday_hours") val standardWorkdayHours: Double? = null,
    @Json(name = "standard_workday_sec") val standardWorkdaySec: Int? = null,
    @Json(name = "extra_workdays") val extraWorkdays: List<String>? = null,
    @Json(name = "extra_holidays") val extraHolidays: List<String>? = null,
    @Json(name = "company_calendar_applied") val companyCalendarApplied: Boolean? = null,
    @Json(name = "company_calendar_extra_workdays") val companyCalendarExtraWorkdays: List<String>? = null,
    @Json(name = "company_calendar_holidays") val companyCalendarHolidays: List<String>? = null,
    @Json(name = "calendar_workdays_in_range") val calendarWorkdaysInRange: Int? = null,
    val summary: InspectionUtilizationSummaryDto? = null,
    @Json(name = "by_inspector") val byInspector: List<InspectionUtilizationInspectorRowDto>? = null,
    @Json(name = "daily_by_inspector") val dailyByInspector: List<InspectionUtilizationDailyInspectorRowDto>? = null,
    val daily: List<InspectionUtilizationDailyRowDto>? = null,
    @Json(name = "data_gaps") val dataGaps: List<String>? = null,
)

data class InspectionUtilizationSummaryDto(
    @Json(name = "inspector_count") val inspectorCount: Int? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "completed_session_count") val completedSessionCount: Int? = null,
    @Json(name = "calendar_workdays_in_range") val calendarWorkdaysInRange: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_regular_sec") val sumRegularSec: Int? = null,
    @Json(name = "sum_overtime_sec") val sumOvertimeSec: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "regular_min") val regularMin: Int? = null,
    @Json(name = "overtime_min") val overtimeMin: Int? = null,
    @Json(name = "utilization_percent") val utilizationPercent: Double? = null,
    @Json(name = "calendar_utilization_percent") val calendarUtilizationPercent: Double? = null,
    @Json(name = "unassigned_session_count") val unassignedSessionCount: Int? = null,
    @Json(name = "sessions_without_time_count") val sessionsWithoutTimeCount: Int? = null,
)

data class InspectionUtilizationInspectorRowDto(
    @Json(name = "inspector_user_id") val inspectorUserId: Int? = null,
    @Json(name = "inspector_name") val inspectorName: String? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "work_day_count") val workDayCount: Int? = null,
    @Json(name = "scheduled_work_day_count") val scheduledWorkDayCount: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_regular_sec") val sumRegularSec: Int? = null,
    @Json(name = "sum_overtime_sec") val sumOvertimeSec: Int? = null,
    @Json(name = "utilization_percent") val utilizationPercent: Double? = null,
    @Json(name = "calendar_utilization_percent") val calendarUtilizationPercent: Double? = null,
)

data class InspectionUtilizationDailyInspectorRowDto(
    val day: String? = null,
    @Json(name = "inspector_user_id") val inspectorUserId: Int? = null,
    @Json(name = "inspector_name") val inspectorName: String? = null,
    @Json(name = "is_scheduled_workday") val isScheduledWorkday: Boolean? = null,
    @Json(name = "is_extra_workday") val isExtraWorkday: Boolean? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "regular_min") val regularMin: Int? = null,
    @Json(name = "overtime_min") val overtimeMin: Int? = null,
    @Json(name = "utilization_percent") val utilizationPercent: Double? = null,
    @Json(name = "load_percent") val loadPercent: Double? = null,
)

data class InspectionUtilizationDailyRowDto(
    val day: String? = null,
    @Json(name = "is_scheduled_workday") val isScheduledWorkday: Boolean? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "inspector_count") val inspectorCount: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "utilization_percent") val utilizationPercent: Double? = null,
)
