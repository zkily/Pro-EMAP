package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class ShortcutItemDto(
    val path: String,
    @Json(name = "menu_code") val menuCode: String? = null,
    @Json(name = "visit_count") val visitCount: Int? = null,
    @Json(name = "last_visited_at") val lastVisitedAt: String? = null,
)

data class ShortcutsResponseDto(
    val pinned: List<ShortcutItemDto> = emptyList(),
    val frequent: List<ShortcutItemDto> = emptyList(),
)

data class ShortcutVisitRequestDto(
    val path: String,
)

data class ShortcutPinsUpdateRequestDto(
    val paths: List<String>,
)
