package com.example.smart_emap.ui.master.productmachineconfig

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class PmcLayoutMode { Compact, Medium, Wide }

fun pmcLayoutMode(maxWidth: Dp): PmcLayoutMode = when {
    maxWidth < 560.dp -> PmcLayoutMode.Compact
    maxWidth < 900.dp -> PmcLayoutMode.Medium
    else -> PmcLayoutMode.Wide
}

internal object PmcTheme {
    val Indigo950 = Color(0xFF1E1B4B)
    val Indigo900 = Color(0xFF312E81)
    val Indigo800 = Color(0xFF4338CA)
    val Violet800 = Color(0xFF5B21B6)
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo500 = Color(0xFF6366F1)
    val Violet600 = Color(0xFF7C3AED)
    val Cyan300 = Color(0xFF7DD3FC)
    val Sky400 = Color(0xFF38BDF8)
    val Slate900 = Color(0xFF0F172A)
    val Slate700 = Color(0xFF334155)
    val Slate600 = Color(0xFF475569)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate50 = Color(0xFFF8FAFC)
    val Rose = Color(0xFFDC2626)
    val Emerald = Color(0xFF059669)

    val PageBg = Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF), Color(0xFFF1F5F9)))
    val HeroBg = Brush.linearGradient(listOf(Indigo950, Indigo900, Indigo800, Violet800))
    val HeroAccentBar = Brush.horizontalGradient(listOf(Indigo500, Color(0xFF8B5CF6), Sky400))
    val PrimaryBtn = Brush.linearGradient(listOf(Indigo600, Violet600))
    val FilterBg = Brush.verticalGradient(listOf(Color(0xFFFAFBFF), Color.White))

    val CardShape = RoundedCornerShape(12.dp)
    val PanelShape = RoundedCornerShape(12.dp)
    val ChipShape = RoundedCornerShape(8.dp)
    val FieldShape = RoundedCornerShape(8.dp)
    val PillShape = RoundedCornerShape(6.dp)

    val ColMeta = Color(0xFFF8FAFC)
    val ColCut = Color(0xFFEFF6FF)
    val ColForm = Color(0xFFECFDF5)
    val ColPlate = Color(0xFFF5F3FF)
    val ColWeld = Color(0xFFFFF7ED)
    val ColOut = Color(0xFFFDF2F8)
    val ColAct = Color(0xFFF1F5F9)
}

internal enum class PmcColGroup { Meta, Cut, Form, Plate, Weld, Out, Act }

internal fun PmcColGroup.bg(): Color = when (this) {
    PmcColGroup.Meta -> PmcTheme.ColMeta
    PmcColGroup.Cut -> PmcTheme.ColCut
    PmcColGroup.Form -> PmcTheme.ColForm
    PmcColGroup.Plate -> PmcTheme.ColPlate
    PmcColGroup.Weld -> PmcTheme.ColWeld
    PmcColGroup.Out -> PmcTheme.ColOut
    PmcColGroup.Act -> PmcTheme.ColAct
}
