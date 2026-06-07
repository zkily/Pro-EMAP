package com.example.smart_emap.ui.mes.chamfering

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Web CuttingActualDataCollection.vue の配色 */
object ChamferingActualColors {
    val PageBg = Color(0xFFF2F3F5)
    val CardBg = Color.White
    val Border = Color(0xFFE4E7ED)
    val BorderLight = Color(0xFFEBEEF5)
    val TextPrimary = Color(0xFF303133)
    val TextSecondary = Color(0xFF606266)
    val TextMuted = Color(0xFF64748B)

    val Primary = Color(0xFF409EFF)
    val PrimaryDark = Color(0xFF337ECC)
    val PrimaryLight9 = Color(0xFFECF5FF)
    val PrimaryLight7 = Color(0xFFC6E2FF)
    val PrimaryLight8 = Color(0xFFD9ECFF)
    val PrimaryLight5 = Color(0xFF9FCEFF)

    val Success = Color(0xFF67C23A)
    val SuccessDark = Color(0xFF529B2E)
    val SuccessLight9 = Color(0xFFF0F9EB)
    val SuccessLight7 = Color(0xFFC2E7B0)
    val SuccessLight5 = Color(0xFFB3E19D)

    val Warning = Color(0xFFE6A23C)
    val WarningDark = Color(0xFFB45309)
    val WarningLight9 = Color(0xFFFDF6EC)
    val WarningLight7 = Color(0xFFF5DAB1)
    val WarningLight5 = Color(0xFFF3D19E)

    val Danger = Color(0xFFF56C6C)
    val DangerLight9 = Color(0xFFFEF0F0)
    val DangerLight5 = Color(0xFFFBC4C4)

    val ToolbarDayBorder = Color(0xFFC6E2FF)
    val ToolbarMachineBorder = Color(0xFFF5DAB1)
    val ToolbarMachineLabel = Color(0xFF9A3412)

    val PlanCardBorderLeft = Warning
    val PlanCardBorderLeftConfirmed = Color(0xFF909399)

    /** 実績確定済卡片 — 灰色弱化 */
    val ConfirmedCardBg = Color(0xFFF5F7FA)
    val ConfirmedStatusText = Color(0xFF606266)
    val ConfirmedStatusBg = Color(0xFFEBEEF5)
    val ConfirmedStatusBorder = Color(0xFFDCDFE6)
    val ConfirmedMetaGradientStart = Color(0xFFF5F7FA)
    val ConfirmedMetaGradientMid = Color(0xFFEBEEF5)
    val ConfirmedMetaGradientEnd = Color(0xFFE4E7ED)
    val ConfirmedMetaBorder = Color(0xFFDCDFE6)
    val ConfirmedProductName = Color(0xFF606266)
    val ConfirmedProductBgStart = Color(0xFFEBEEF5)
    val ConfirmedProductBgEnd = Color(0xFFE4E7ED)
    val ConfirmedProductBorder = Color(0xFFC0C4CC)

    val ProductName = Color(0xFF1E3A8A)
    val MaterialText = Color(0xFF0F766E)
    val MaterialBorder = Color(0xFF5EEAD4)

    val MetaGradientStart = Color(0xFFFFFFFF)
    val MetaGradientMid = Color(0xFFECF5FF)
    val MetaGradientEnd = Color(0xFFF5F7FA)
    val MetaBorder = PrimaryLight8

    val ProductPrimaryBgStart = Color(0xFFEFF6FF)
    val ProductPrimaryBgEnd = Color(0xFFDBEAFE)
    val ProductPrimaryBorder = Color(0xFF93C5FD)

    val QtyChipBgStart = Color(0xFFDBEAFE)
    val QtyChipBgEnd = Color(0xFFBFDBFE)
    val QtyChipBorder = Color(0xFF60A5FA)
    val QtyChipLabel = Color(0xFF1D4ED8)
    val QtyChipValue = Color(0xFF1E3A8A)

    val QtyActualBgStart = Color(0xFFDCFCE7)
    val QtyActualBgEnd = Color(0xFFBBF7D0)
    val QtyActualBorder = Color(0xFF4ADE80)

    val CodeChipBgStart = Color(0xFFF3E8FF)
    val CodeChipBgEnd = Color(0xFFE9D5FF)
    val CodeChipBorder = Color(0xFFC084FC)
    val CodeChipText = Color(0xFF6B21A8)

    val OperatorBgStart = Color(0xFFF5F3FF)
    val OperatorBgEnd = Color(0xFFEDE9FE)
    val OperatorBorder = Color(0xFFC4B5FD)
    val OperatorLabel = Color(0xFF5B21B6)

    val SetupBgStart = Color(0xFFFFFBEB)
    val SetupBgEnd = Color(0xFFFEF3C7)
    val SetupBorder = Color(0xFFFCD34D)
    val SetupLabel = Color(0xFFB45309)

    val BladeBgStart = Color(0xFFECFEFF)
    val BladeBgEnd = Color(0xFFCFFAFE)
    val BladeBorder = Color(0xFF67E8F9)
    val BladeLabel = Color(0xFF0E7490)

    val RepairBgStart = Color(0xFFFFF1F2)
    val RepairBgEnd = Color(0xFFFFE4E6)
    val RepairBorder = Color(0xFFFDA4AF)
    val RepairLabel = Color(0xFFBE123C)

    val TimerIdleBgStart = Color(0xFFF8FAFC)
    val TimerIdleBgEnd = Color(0xFFE2E8F0)
    val TimerIdleBorder = Color(0xFFCBD5E1)

    val TimerRunningBgStart = Color(0xFFECFDF5)
    val TimerRunningBgEnd = Color(0xFFD1FAE5)
    val TimerRunningBorder = Color(0xFF6EE7B7)
    val TimerRunningText = Color(0xFF047857)

    val TimerPausedBgStart = Color(0xFFFFFBEB)
    val TimerPausedBgEnd = Color(0xFFFEF3C7)
    val TimerPausedBorder = Color(0xFFFBBF24)
    val TimerPausedText = Color(0xFFB45309)

    val TimerEndedBgStart = Color(0xFFEFF6FF)
    val TimerEndedBgEnd = Color(0xFFDBEAFE)
    val TimerEndedBorder = Color(0xFF93C5FD)
    val TimerEndedText = Color(0xFF1D4ED8)

    val BtnStartStart = Color(0xFF4CD787)
    val BtnStartEnd = Success
    val BtnPauseStart = Color(0xFFFFD06A)
    val BtnPauseEnd = Warning
    val BtnPauseText = Color(0xFF5C3D00)
    val BtnResumeStart = Color(0xFF79BBFF)
    val BtnResumeEnd = Primary
    val BtnEndStart = Color(0xFFF89898)
    val BtnEndEnd = Danger
    val BtnMachineStart = Color(0xFFCFCFCF)
    val BtnMachineEnd = Color(0xFFC4C3C3)
    val BtnDisabledBg = Color(0xFFF5F7FA)
    val BtnDisabledText = Color(0xFFA8ABB2)

    val AnchorDayBorder = PrimaryLight5
    val DayGroupHeadBg = Color(0xFFF5F7FA)
    val OfflineStrip = WarningLight9
    val OfflineStripBorder = WarningLight5

    val LoadBtnStart = Color(0xFF79BBFF)
    val LoadBtnEnd = Primary

    val TitleIconGradient = Brush.linearGradient(
        colors = listOf(Primary, Color(0xFF38BDF8), Success),
    )
}
