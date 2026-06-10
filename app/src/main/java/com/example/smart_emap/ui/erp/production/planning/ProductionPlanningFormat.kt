package com.example.smart_emap.ui.erp.production.planning

import java.text.NumberFormat
import java.util.Locale

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

fun formatProductionNumber(value: Number?): String {
    if (value == null) return "—"
    return jpNumber.format(value)
}

fun formatProductionPercent(value: Double?): String {
    if (value == null || !value.isFinite()) return "—"
    return "${value.toInt()}%"
}

fun formatProductionSigned(value: Int?): String {
    if (value == null) return "—"
    val formatted = formatProductionNumber(kotlin.math.abs(value))
    return if (value > 0) "+$formatted" else if (value < 0) "-$formatted" else formatted
}

fun achievementRateColor(rate: Double?): androidx.compose.ui.graphics.Color {
    if (rate == null) return ProductionPlanningColors.TextPrimary
    return when {
        rate >= 100 -> ProductionPlanningColors.Positive
        rate < 80 -> ProductionPlanningColors.Negative
        else -> androidx.compose.ui.graphics.Color(0xFFD97706)
    }
}

fun diffValueColor(value: Int): androidx.compose.ui.graphics.Color = when {
    value > 0 -> ProductionPlanningColors.Positive
    value < 0 -> ProductionPlanningColors.Negative
    else -> ProductionPlanningColors.TextPrimary
}

/** 表格表示用文字が負数か（例: -1,234） */
fun isNegativeDisplayNumber(text: String): Boolean {
    if (text.isBlank() || text == "—") return false
    val normalized = text.replace(",", "").trim()
    if (!normalized.startsWith("-")) return false
    return normalized.toDoubleOrNull()?.let { it < 0 } == true
}
