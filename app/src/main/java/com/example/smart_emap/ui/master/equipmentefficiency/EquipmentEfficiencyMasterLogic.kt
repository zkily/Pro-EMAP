package com.example.smart_emap.ui.master.equipmentefficiency

import com.example.smart_emap.data.model.EquipmentEfficiencyCreateBody
import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.EquipmentEfficiencyTabCountsDto
import com.example.smart_emap.data.model.EquipmentEfficiencyUpdateBody
import com.example.smart_emap.data.model.MasterMachineFullDto
import kotlin.math.round

data class EquipmentEfficiencyUiRow(
    val id: Int,
    val machineCd: String,
    val machinesName: String,
    val productCd: String,
    val productName: String,
    val efficiencyRate: Double,
    val stepTime: Int?,
    val unit: String,
    val remarks: String,
    val status: Int,
)

data class EeMachineOption(val value: String, val label: String)

data class EeProcessTab(val label: String, val value: String)

object EquipmentEfficiencyMasterLogic {
    val processTabs = listOf(
        EeProcessTab("全て", "all"),
        EeProcessTab("切断", "cutting"),
        EeProcessTab("面取", "chamfering"),
        EeProcessTab("成型", "forming"),
        EeProcessTab("溶接", "welding"),
        EeProcessTab("メッキ", "plating"),
        EeProcessTab("検査", "inspection"),
        EeProcessTab("その他", "other"),
    )

    fun fromDto(dto: EquipmentEfficiencyRowDto): EquipmentEfficiencyUiRow? {
        val id = dto.id ?: return null
        val machineCd = dto.machineCd?.trim().orEmpty()
        val productCd = dto.productCd?.trim().orEmpty()
        if (machineCd.isEmpty() || productCd.isEmpty()) return null
        return EquipmentEfficiencyUiRow(
            id = id,
            machineCd = machineCd,
            machinesName = dto.machinesName.orEmpty(),
            productCd = productCd,
            productName = dto.productName.orEmpty(),
            efficiencyRate = dto.efficiencyRate ?: 0.0,
            stepTime = dto.stepTime,
            unit = dto.unit.orEmpty(),
            remarks = dto.remarks.orEmpty(),
            status = dto.status ?: 0,
        )
    }

    fun tabCount(counts: EquipmentEfficiencyTabCountsDto, processType: String): Int = when (processType) {
        "all" -> counts.all ?: 0
        "cutting" -> counts.cutting ?: 0
        "chamfering" -> counts.chamfering ?: 0
        "forming" -> counts.forming ?: 0
        "welding" -> counts.welding ?: 0
        "plating" -> counts.plating ?: 0
        "inspection" -> counts.inspection ?: 0
        "other" -> counts.other ?: 0
        else -> 0
    }

    fun machineOptions(machines: List<MasterMachineFullDto>): List<EeMachineOption> =
        machines.mapNotNull { m ->
            val cd = m.machineCd?.trim().orEmpty()
            if (cd.isEmpty()) return@mapNotNull null
            val name = m.machineName.orEmpty()
            EeMachineOption(cd, if (name.isBlank()) cd else "$name ($cd)")
        }.distinctBy { it.value }

    fun formatEfficiency(row: EquipmentEfficiencyUiRow): String {
        val text = round(row.efficiencyRate * 10.0) / 10.0
        val base = if (text % 1.0 == 0.0) text.toInt().toString() else String.format("%.1f", text)
        return if (row.unit.isBlank()) base else "$base ${row.unit}"
    }

    fun formatStepTime(stepTime: Int?): String = stepTime?.let { "${it}分" } ?: "—"

    fun statusLabel(status: Int): String = if (status == 1) "有効" else "無効"

    fun emptyFormRow(): EquipmentEfficiencyUiRow = EquipmentEfficiencyUiRow(
        id = 0,
        machineCd = "",
        machinesName = "",
        productCd = "",
        productName = "",
        efficiencyRate = 0.0,
        stepTime = null,
        unit = "",
        remarks = "",
        status = 1,
    )

    fun toCreateBody(row: EquipmentEfficiencyUiRow): EquipmentEfficiencyCreateBody =
        EquipmentEfficiencyCreateBody(
            machineCd = row.machineCd,
            machinesName = row.machinesName.blankToNull(),
            productCd = row.productCd,
            productName = row.productName.blankToNull(),
            efficiencyRate = row.efficiencyRate,
            stepTime = row.stepTime,
            unit = row.unit.blankToNull(),
            remarks = row.remarks.blankToNull(),
            status = row.status,
        )

    fun toUpdateBody(row: EquipmentEfficiencyUiRow): EquipmentEfficiencyUpdateBody =
        EquipmentEfficiencyUpdateBody(
            machineCd = row.machineCd,
            machinesName = row.machinesName.blankToNull(),
            productCd = row.productCd,
            productName = row.productName.blankToNull(),
            efficiencyRate = row.efficiencyRate,
            stepTime = row.stepTime,
            unit = row.unit.blankToNull(),
            remarks = row.remarks.blankToNull(),
            status = row.status,
        )

    fun toStatusBody(status: Int): EquipmentEfficiencyUpdateBody =
        EquipmentEfficiencyUpdateBody(status = status)

    private fun String.blankToNull(): String? = trim().ifBlank { null }
}
