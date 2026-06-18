package com.example.smart_emap.data.repository

import com.example.smart_emap.core.network.ApiClient
import com.example.smart_emap.data.model.ShortcutPinsUpdateRequestDto
import com.example.smart_emap.data.model.ShortcutVisitRequestDto
import com.example.smart_emap.data.model.ShortcutsResponseDto

class SidebarShortcutsRepository(
    private val apiClient: ApiClient,
) {
    suspend fun getShortcuts(): ShortcutsResponseDto =
        apiClient.shortcutsApi().listShortcuts()

    suspend fun updatePins(paths: List<String>): ShortcutsResponseDto =
        apiClient.shortcutsApi().updatePins(ShortcutPinsUpdateRequestDto(paths))

    suspend fun recordVisit(path: String) {
        apiClient.shortcutsApi().recordVisit(ShortcutVisitRequestDto(path))
    }
}
