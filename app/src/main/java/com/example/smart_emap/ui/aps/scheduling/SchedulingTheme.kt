package com.example.smart_emap.ui.aps.scheduling

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SchedulingTheme {
    /** 筛选下拉固定宽度（与 Web Scheduling.vue 相当） */
    val FilterProcessWidth = 115.dp
    val FilterLineWidth = 128.dp
    val FilterProductWidth = 200.dp
    val FilterPeriodMinWidth = 228.dp
    val FilterFieldHeight = 40.dp
    /** スケジューリング画面で選択可能な工程（成型 KT04 / 溶接 KT07） */
    val processOptions: List<Pair<String, String>> = listOf(
        "KT04" to "成型",
        "KT07" to "溶接",
    )

    fun processLabel(processCd: String): String =
        processOptions.firstOrNull { it.first == processCd.trim() }?.second ?: processCd

    val PageBgStart = Color(0xFFF3F6FB)
    val PageBgEnd = Color(0xFFF3F6FB)
    val TitleDark = Color(0xFF0F172A)
    val Subtitle = Color(0xFF5F6F86)
    val CardBorder = Color(0xFFE2E8F0)
    val FilterLabel = Color(0xFF1E3A8A)
    val FeatureBlue = Color(0xFF1D4ED8)

    val StatLines = Color(0xFF1E3A8A)
    val StatPlan = Color(0xFF065F46)
    val StatEfficiency = Color(0xFF92400E)
    val StatHours = Color(0xFF5B21B6)

    val TableHeaderBg = Color(0xFFEEF4FB)
    val TableBorder = Color(0xFFE6EDF5)
    val WeekendRed = Color(0xFFDC2626)
    val TodayHeader = Color(0xFFFFEAB0)
    val GroupLineBlue = Color(0xFF1E3A8A)
    val FooterBg = Color(0xFFF3F8FF)

    val CellActual = Color(0xE0FEF9C3)
    val CellActive = Color(0x80BBF7D0)
    val CellHigh = Color(0x3822C55E)
    val CellMid = Color(0x2EF59E0B)
    val CellLow = Color(0x2EEF4444)
    val CellShortage = Color(0x42EF4444)
    val CellDue = Color(0xFFF59E0B)

    val UpstreamCutting = Color(0xD1FECACA)
    val UpstreamPlanned = Color(0x9EBBF7D0)

    fun toneColor(tone: SchedulingCellTone): Color? = when (tone) {
        SchedulingCellTone.Actual -> CellActual
        SchedulingCellTone.Active -> CellActive
        SchedulingCellTone.High -> CellHigh
        SchedulingCellTone.Mid -> CellMid
        SchedulingCellTone.Low -> CellLow
        SchedulingCellTone.Shortage -> CellShortage
        SchedulingCellTone.None -> null
    }
}
