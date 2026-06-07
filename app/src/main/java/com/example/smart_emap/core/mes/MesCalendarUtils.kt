package com.example.smart_emap.core.mes

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object MesCalendarUtils {
    private val JST = ZoneId.of("Asia/Tokyo")
    private val ymd = DateTimeFormatter.ISO_LOCAL_DATE
    private val dayKeyRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun jstToday(): String = LocalDate.now(JST).format(ymd)

    fun shiftDateYmd(dateStr: String, deltaDays: Int): String {
        val base = LocalDate.parse(dateStr.trim(), ymd)
        return base.plusDays(deltaDays.toLong()).format(ymd)
    }

    fun nextWeekdayYmdJST(dateStr: String): String {
        var s = shiftDateYmd(dateStr, 1)
        var w = LocalDate.parse(s, ymd).dayOfWeek
        if (w == DayOfWeek.SUNDAY) s = shiftDateYmd(s, 1)
        else if (w == DayOfWeek.SATURDAY) s = shiftDateYmd(s, 2)
        return s
    }

    fun normalizeProductionDayKey(value: String?): String {
        val raw = value?.trim().orEmpty()
        return if (raw.length >= 10 && raw[4] == '-' && raw[7] == '-') raw.take(10) else "—"
    }

    /** Web formatDateWithWeekdayJST — e.g. 2026/06/06(土) */
    fun formatDateWithWeekday(dayKey: String, localeTag: String = "ja"): String {
        if (dayKey == "—" || !dayKeyRegex.matches(dayKey)) return dayKey
        val date = LocalDate.parse(dayKey, ymd)
        val locale = when (localeTag.lowercase()) {
            "en" -> Locale.US
            "zh" -> Locale.CHINA
            "vi" -> Locale.forLanguageTag("vi-VN")
            else -> Locale.JAPAN
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)", locale))
    }
}
