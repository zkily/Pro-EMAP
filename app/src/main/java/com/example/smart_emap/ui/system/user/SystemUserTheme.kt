package com.example.smart_emap.ui.system.user

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

object SystemUserTheme {
    val PrimaryStart = Color(0xFF667EEA)
    val PrimaryEnd = Color(0xFF764BA2)

    val PageBgStart = Color(0xFFF5F7FA)
    val PageBgEnd = Color(0xFFE4E8ED)
    val CardBg = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFF8FAFC)
    val FooterBg = Color(0xFFF8FAFC)
    val Border = Color(0xFFE2E8F0)
    val BorderLight = Color(0xFFF1F5F9)
    val BorderFocus = Color(0xFFC7D2FE)

    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val TableHeaderBg = Color(0xFFF8FAFC)
    val TableHeaderText = Color(0xFF334155)
    val RowAlt = Color(0xFFFCFDFF)
    val IdBadgeBg = Color(0xFFF1F5F9)

    val ActiveGreen = Color(0xFF16A34A)
    val ActiveDot = Color(0xFF22C55E)
    val LockedOrange = Color(0xFFD97706)
    val LockedDot = Color(0xFFF59E0B)
    val LockedRowBg = Color(0xFFFFFBEB)

    val StatActive = Color(0xFFA5F3FC)
    val StatLocked = Color(0xFFFCD34D)

    val DeptBadgeBg = Color(0xFFE0F2FE)
    val DeptBadgeText = Color(0xFF0369A1)
    val TwoFaOnBg = Color(0xFFDCFCE7)
    val TwoFaOnIcon = Color(0xFF16A34A)
    val TwoFaOffBg = Color(0xFFF1F5F9)

    val BtnPrimary = Color(0xFF409EFF)
    val BtnWarning = Color(0xFFE6A23C)
    val BtnInfo = Color(0xFF909399)

    val ShadowSoft = Color(0x40667EEA)
    val Error = Color(0xFFDC2626)

    val shapePage = RoundedCornerShape(12.dp)
    val shapeHeader = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    val shapeChip = RoundedCornerShape(6.dp)
    val shapeInput = RoundedCornerShape(8.dp)
    val shapeAvatar = RoundedCornerShape(8.dp)

    val headerGradient = Brush.linearGradient(listOf(PrimaryStart, PrimaryEnd))
    val pageGradient = Brush.linearGradient(listOf(PageBgStart, PageBgEnd))
    val primaryButtonGradient = Brush.linearGradient(listOf(PrimaryStart, PrimaryEnd))
    val printButtonGradient = Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
}

fun roleLabel(code: String?): String = when (code?.lowercase()) {
    "admin" -> "管理者"
    "user" -> "一般ユーザー"
    "manager" -> "マネージャー"
    "worker" -> "作業者"
    "guest" -> "ゲスト"
    "viewer" -> "閲覧者"
    else -> code.orEmpty().ifEmpty { "—" }
}

fun roleCodeFromJapaneseName(name: String): String? = when (name) {
    "管理者" -> "admin"
    "一般ユーザー" -> "user"
    "マネージャー" -> "manager"
    "作業者" -> "worker"
    "ゲスト" -> "guest"
    "閲覧者" -> "viewer"
    else -> null
}

fun roleTagColors(code: String?): Pair<Color, Color> = when (code?.lowercase()) {
    "admin" -> Color(0xFFF56C6C) to Color.White
    "user" -> Color(0xFF409EFF) to Color.White
    "manager" -> Color(0xFF67C23A) to Color.White
    "worker" -> Color(0xFFE6A23C) to Color.White
    "guest", "viewer" -> Color(0xFF909399) to Color.White
    else -> Color(0xFF909399) to Color.White
}

fun avatarGradientFor(username: String?): Brush {
    val palettes = listOf(
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        listOf(Color(0xFF10B981), Color(0xFF059669)),
        listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
        listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
        listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
    )
    val idx = abs(username?.hashCode() ?: 0) % palettes.size
    return Brush.linearGradient(palettes[idx])
}
