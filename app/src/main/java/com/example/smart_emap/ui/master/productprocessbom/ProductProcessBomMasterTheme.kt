package com.example.smart_emap.ui.master.productprocessbom

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object PpbTheme {
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo500 = Color(0xFF6366F1)
    val Violet700 = Color(0xFF6D28D9)
    val Violet600 = Color(0xFF7C3AED)
    val Indigo950 = Color(0xFF1E1B4B)
    val Indigo900 = Color(0xFF312E81)
    val Indigo800 = Color(0xFF4338CA)
    val Violet800 = Color(0xFF5B21B6)
    val Sky300 = Color(0xFF7DD3FC)
    val Green400 = Color(0xFF4ADE80)
    val Orange400 = Color(0xFFFB923C)
    val Emerald = Color(0xFF059669)
    val EmeraldLight = Color(0xFF10B981)
    val Slate900 = Color(0xFF0F172A)
    val Slate600 = Color(0xFF475569)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate50 = Color(0xFFF8FAFC)
    val Rose = Color(0xFFDC2626)

    val PageBg = Brush.linearGradient(
        listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFEEF2F7)),
    )
    val HeroBg = Brush.linearGradient(
        listOf(Indigo950, Indigo900, Indigo800, Violet800),
    )
    val HeroAccentBar = Brush.horizontalGradient(
        listOf(Indigo500, Color(0xFF8B5CF6), Color(0xFF0EA5E9)),
    )
    val SearchBtn = Brush.linearGradient(listOf(Indigo600, Violet600))
    val SyncBtnBg = Color(0xFFECFDF5)
    val SyncBtnBorder = Color(0xFF10B981)
    val SyncBtnText = Emerald

    val CardShape = RoundedCornerShape(12.dp)
    val PanelShape = RoundedCornerShape(12.dp)
    val ChipShape = RoundedCornerShape(8.dp)
    val FieldShape = RoundedCornerShape(8.dp)
    val PillShape = RoundedCornerShape(6.dp)

    val ColMeta = Color(0xFFF8FAFC)
    val ColStock = Color(0xFFFFFBEB)
    val ColG0 = Color(0xFFEFF6FF)
    val ColG1 = Color(0xFFECFDF5)
    val ColG2 = Color(0xFFF5F3FF)
    val ColG3 = Color(0xFFFFF7ED)
    val ColAct = Color(0xFFF1F5F9)
}

internal enum class PpbColGroup { Meta, Stock, G0, G1, G2, G3, Act }

internal fun PpbColGroup.bg(): Color = when (this) {
    PpbColGroup.Meta -> PpbTheme.ColMeta
    PpbColGroup.Stock -> PpbTheme.ColStock
    PpbColGroup.G0 -> PpbTheme.ColG0
    PpbColGroup.G1 -> PpbTheme.ColG1
    PpbColGroup.G2 -> PpbTheme.ColG2
    PpbColGroup.G3 -> PpbTheme.ColG3
    PpbColGroup.Act -> PpbTheme.ColAct
}
