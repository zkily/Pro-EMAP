package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.ui.graphics.Color

enum class PlanInstructionKind {
    FORMING,
    WELDING,
}

data class PlanInstructionConfig(
    val kind: PlanInstructionKind,
    val pageTitle: String,
    val pageSubtitle: String,
    val processName: String,
    val machineType: String,
    val apsProcessCd: String,
    val notesApiPath: String,
    val instructionPrintTitle: String,
    val setupSchedulePrintTitle: String,
    val persistRemarksToApi: Boolean,
    val excludePrintMachines: Set<String> = emptySet(),
    val showWorkTimeConfig: Boolean = false,
    val showSetupSchedulePreview: Boolean = false,
    val showSpecifiedWorkingDays: Boolean = false,
    val showEfficiencyUpdate: Boolean = false,
    val useWeldingSetupPrint: Boolean = false,
    val headerAccent: Color,
) {
    companion object {
        val Forming = PlanInstructionConfig(
            kind = PlanInstructionKind.FORMING,
            pageTitle = "成型指示書発行管理",
            pageSubtitle = "生産計画データ管理・指示発行システム",
            processName = "成型",
            machineType = "成型",
            apsProcessCd = "KT04",
            notesApiPath = "forming-instruction-notes",
            instructionPrintTitle = "成型生産指示書",
            setupSchedulePrintTitle = "成型生産計画段替予定表",
            persistRemarksToApi = false,
            excludePrintMachines = setOf("成型他"),
            showSetupSchedulePreview = true,
            headerAccent = Color(0xFF06B6D4),
        )

        val Welding = PlanInstructionConfig(
            kind = PlanInstructionKind.WELDING,
            pageTitle = "溶接指示書発行管理",
            pageSubtitle = "生産計画データ管理・指示発行システム",
            processName = "溶接",
            machineType = "溶接",
            apsProcessCd = "KT07",
            notesApiPath = "welding-instruction-notes",
            instructionPrintTitle = "溶接生産指示書",
            setupSchedulePrintTitle = "溶接生産計画段取予定表",
            persistRemarksToApi = true,
            showSpecifiedWorkingDays = true,
            showEfficiencyUpdate = true,
            useWeldingSetupPrint = true,
            headerAccent = Color(0xFF6366F1),
        )
    }
}
