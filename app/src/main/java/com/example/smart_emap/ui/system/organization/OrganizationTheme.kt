package com.example.smart_emap.ui.system.organization

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.smart_emap.ui.system.user.SystemUserTheme

object OrganizationTheme {
    val pageGradient = SystemUserTheme.pageGradient
    val headerGradient = SystemUserTheme.headerGradient
    val primaryGradient = SystemUserTheme.primaryButtonGradient
    val shapePage = SystemUserTheme.shapePage
    val shapePanel = SystemUserTheme.shapePage
    val shapeChip = SystemUserTheme.shapeChip
    val shapeInput = SystemUserTheme.shapeInput
    val CardBg = SystemUserTheme.CardBg
    val PanelHeaderBg = Color(0xFFF8FAFC)
    val Border = SystemUserTheme.Border
    val BorderLight = SystemUserTheme.BorderLight
    val TextPrimary = SystemUserTheme.TextPrimary
    val TextSecondary = SystemUserTheme.TextSecondary
    val TextMuted = SystemUserTheme.TextMuted
    val TreeSelectedBg = Color(0xFFEDE9FE)
    val TreeHintBg = Color(0xFFF0F9FF)
    val TreeHintText = Color(0xFF0369A1)
    val Error = SystemUserTheme.Error
    val ShadowSoft = SystemUserTheme.ShadowSoft
}

fun orgTypeLabel(type: String?): String = when (type?.lowercase()) {
    "company" -> "会社"
    "site" -> "拠点"
    "department" -> "部門"
    "section" -> "課"
    "line" -> "ライン"
    else -> type.orEmpty().ifEmpty { "—" }
}

fun orgTypeBadgeColors(type: String?): Pair<Color, Color> = when (type?.lowercase()) {
    "company" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    "site" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    "department" -> Color(0xFFEDE9FE) to Color(0xFF7C3AED)
    "section" -> Color(0xFFCFFAFE) to Color(0xFF0891B2)
    "line" -> Color(0xFFD1FAE5) to Color(0xFF059669)
    else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
}

fun orgTypeIconGradient(type: String?): Brush = when (type?.lowercase()) {
    "company" -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
    "site" -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
    "department" -> Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
    "section" -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2)))
    "line" -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    else -> OrganizationTheme.primaryGradient
}

fun countOrgNodes(nodes: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>): Int {
    return nodes.sumOf { 1 + countOrgNodes(it.children) }
}

fun countDeptNodes(nodes: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>): Int {
    return nodes.sumOf { node ->
        (if (node.type == "department") 1 else 0) + countDeptNodes(node.children)
    }
}

fun collectAllNodeIds(nodes: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>): Set<Int> {
    val ids = mutableSetOf<Int>()
    fun walk(list: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>) {
        list.forEach { n ->
            ids.add(n.id)
            walk(n.children)
        }
    }
    walk(nodes)
    return ids
}

fun findOrgName(nodes: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>, id: Int): String? {
    for (node in nodes) {
        if (node.id == id) return node.name
        findOrgName(node.children, id)?.let { return it }
    }
    return null
}

fun flattenOrgTree(
    nodes: List<com.example.smart_emap.data.model.OrganizationTreeNodeDto>,
    level: Int = 0,
): List<Pair<Int, String>> {
    val result = mutableListOf<Pair<Int, String>>()
    nodes.forEach { node ->
        val prefix = if (level > 0) "　".repeat(level) else ""
        result.add(node.id to "$prefix${node.name}")
        result.addAll(flattenOrgTree(node.children, level + 1))
    }
    return result
}
