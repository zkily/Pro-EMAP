package com.example.smart_emap.core.mes

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** inspection_management の MES 日時（JST 壁時計・MySQL DATETIME） */
object MesDateTime {
    private val JST = ZoneId.of("Asia/Tokyo")

    private val naivePatterns = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    )

    /** ISO8601（Z/オフセット付き）または DB の naive JST 文字列を epoch ms に変換 */
    fun parseToMillis(value: String?): Long? {
        val raw = value?.trim().orEmpty()
        if (raw.isEmpty()) return null
        runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()?.let { return it }
        val normalized = raw.replace(' ', 'T')
        for (pattern in naivePatterns) {
            runCatching {
                LocalDateTime.parse(normalized, pattern).atZone(JST).toInstant().toEpochMilli()
            }.getOrNull()?.let { return it }
            runCatching {
                LocalDateTime.parse(raw, pattern).atZone(JST).toInstant().toEpochMilli()
            }.getOrNull()?.let { return it }
        }
        return null
    }
}
