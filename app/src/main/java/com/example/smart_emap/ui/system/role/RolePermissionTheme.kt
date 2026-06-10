package com.example.smart_emap.ui.system.role

import androidx.compose.ui.graphics.Color
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.ui.system.organization.OrganizationTheme

object RolePermissionTheme {
    val pageGradient = OrganizationTheme.pageGradient
    val headerGradient = OrganizationTheme.headerGradient
    val primaryGradient = OrganizationTheme.primaryGradient
    val shapePage = OrganizationTheme.shapePage
    val shapePanel = OrganizationTheme.shapePanel
    val shapeChip = OrganizationTheme.shapeChip
    val shapeInput = OrganizationTheme.shapeInput
    val CardBg = OrganizationTheme.CardBg
    val PanelHeaderBg = OrganizationTheme.PanelHeaderBg
    val Border = OrganizationTheme.Border
    val BorderLight = OrganizationTheme.BorderLight
    val TextPrimary = OrganizationTheme.TextPrimary
    val TextSecondary = OrganizationTheme.TextSecondary
    val TextMuted = OrganizationTheme.TextMuted
    val RowSelectedBg = Color(0xFFEDE9FE)
    val HintBg = Color(0xFFF0F9FF)
    val HintText = Color(0xFF0369A1)
    val Error = OrganizationTheme.Error
    val ShadowSoft = OrganizationTheme.ShadowSoft
    val ScopeActiveBorder = Color(0xFF667EEA)
    val ScopeActiveBg = Color(0xFFF5F3FF)
}

val OPERATION_MODULES_LIST = listOf(
    OperationModules.SALES,
    OperationModules.PURCHASE,
    OperationModules.INVENTORY,
    OperationModules.COST,
    OperationModules.FINANCE,
    OperationModules.PRODUCTION_PLAN,
    OperationModules.MES,
    OperationModules.QUALITY,
    OperationModules.SYSTEM,
)

private val roleColors = listOf(
    Color(0xFF667EEA),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
)

fun roleColorFor(name: String): Color {
    val idx = name.firstOrNull()?.code?.rem(roleColors.size) ?: 0
    return roleColors[idx]
}
