package com.example.smart_emap.ui.erp.order

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object OrderMonthlyColors {
    val PageBgTop = Color(0xFFF0F4FF)
    val PageBgMid = Color(0xFFE8ECF8)
    val PageBgBottom = Color(0xFFF5F0FF)

    val TextPrimary = Color(0xFF1E293B)
    val TextMuted = Color(0xFF64748B)
    val DiffNegative = Color(0xFFE74C3C)
    val DiffPositive = Color(0xFF2ECC71)

    val TableHeaderBg = Color(0xFFF1F5F9)
    val TableHeaderTint = Color(0xFF6366F1)
    val ActionBlue = Color(0xFF409EFF)
    val ActionEdit = Color(0xFF66B1FF)
    val BtnBlue = Color(0xFF409EFF)
    val BtnIndigo = Color(0xFF6366F1)
    val BorderLight = Color(0xFFE2E8F0)
    val TableRowDivider = Color(0x0A000000)
    val TableRowStripe = Color(0x80F8FAFC)
    val TableRowHover = Color(0x146366F1)

    val tableHeaderBackground: Brush
        get() = Brush.verticalGradient(
            colors = listOf(Color(0x1F6366F1), Color(0x148B5CF6)),
        )

    val pageBackground: Brush
        get() = Brush.linearGradient(listOf(PageBgTop, PageBgMid, PageBgBottom))

    /** Web .page-toolbar 125deg gradient */
    val toolbarBackground: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                Color(0xF04F46E5),
                Color(0xEB6D28D9),
                Color(0xE69333EA),
                Color(0xE0A855F7),
            ),
        )
}
