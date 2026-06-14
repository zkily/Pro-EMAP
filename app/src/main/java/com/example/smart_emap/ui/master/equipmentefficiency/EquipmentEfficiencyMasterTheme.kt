package com.example.smart_emap.ui.master.equipmentefficiency

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class EeLayoutMode { Compact, Medium, Wide }

fun eeLayoutMode(maxWidth: Dp): EeLayoutMode = when {
    maxWidth < 560.dp -> EeLayoutMode.Compact
    maxWidth < 900.dp -> EeLayoutMode.Medium
    else -> EeLayoutMode.Wide
}

internal object EeTheme {
    val Purple600 = Color(0xFF5B5EA6)
    val Violet600 = Color(0xFF7C3AED)
    val Violet700 = Color(0xFF6D28D9)
    val Indigo500 = Color(0xFF6366F1)
    val Indigo600 = Color(0xFF4F46E5)
    val Sky400 = Color(0xFF38BDF8)
    val Emerald = Color(0xFF059669)
    val EmeraldLight = Color(0xFF10B981)
    val Slate900 = Color(0xFF0F172A)
    val Slate700 = Color(0xFF334155)
    val Slate600 = Color(0xFF475569)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate50 = Color(0xFFF8FAFC)
    val Rose = Color(0xFFDC2626)

    val PageBg = Brush.linearGradient(listOf(Color(0xFFF5F7FA), Color(0xFFEEF1F5), Color(0xFFE8ECF3)))
    val HeroBg = Brush.linearGradient(listOf(Purple600, Violet600, Violet700))
    val HeroAccentBar = Brush.horizontalGradient(listOf(Indigo500, Violet600, Sky400))
    val PrimaryBtn = Brush.linearGradient(listOf(Violet600, Violet700))
    val FilterBg = Brush.verticalGradient(listOf(Color.White, Color(0xFFFAFBFF)))

    val CardShape = RoundedCornerShape(12.dp)
    val PanelShape = RoundedCornerShape(12.dp)
    val ChipShape = RoundedCornerShape(8.dp)
    val FieldShape = RoundedCornerShape(8.dp)
    val PillShape = RoundedCornerShape(6.dp)
    val TabShape = RoundedCornerShape(10.dp)

    val ColMeta = Color(0xFFF8FAFC)
    val ColEff = Color(0xFFEFF6FF)
    val ColStep = Color(0xFFECFDF5)
    val ColStatus = Color(0xFFF5F3FF)
    val ColAct = Color(0xFFF1F5F9)
}

internal enum class EeColGroup { Meta, Eff, Step, Status, Act }

internal fun EeColGroup.bg(): Color = when (this) {
    EeColGroup.Meta -> EeTheme.ColMeta
    EeColGroup.Eff -> EeTheme.ColEff
    EeColGroup.Step -> EeTheme.ColStep
    EeColGroup.Status -> EeTheme.ColStatus
    EeColGroup.Act -> EeTheme.ColAct
}
