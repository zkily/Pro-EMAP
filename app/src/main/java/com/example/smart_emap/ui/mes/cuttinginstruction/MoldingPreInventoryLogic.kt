package com.example.smart_emap.ui.mes.cuttinginstruction

import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.InstructionCuttingRowDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.ProductionSummaryRowDto
import kotlin.math.roundToInt

internal data class MoldingPreInventoryTableRow(
    val productCd: String,
    val productName: String,
    val preMoldingInventory: Int?,
    val moldingMachine: String,
    val efficiencyRate: Double?,
)

private fun isPositiveInventory(value: Int?): Boolean = (value ?: 0) > 0

private fun productionHours(inventory: Int?, rate: Double?): Int {
    if (!isPositiveInventory(inventory)) return 0
    val r = rate ?: return 0
    if (r <= 0.0) return 0
    return ((inventory ?: 0) / r).roundToInt()
}

private fun buildMachineNameResolver(machines: List<MasterMachineFullDto>): (String?) -> String {
    val cdToName = machines.mapNotNull { m ->
        val cd = m.machineCd.orEmpty().trim()
        val name = m.machineName.orEmpty().trim()
        if (cd.isNotBlank() && name.isNotBlank()) cd to name else null
    }.toMap()
    val knownNames = machines.mapNotNull { it.machineName?.trim()?.takeIf { n -> n.isNotEmpty() } }.toSet()
    return { raw ->
        val v = raw.orEmpty().trim()
        if (v.isBlank()) "（未設定）"
        else cdToName[v] ?: if (knownNames.contains(v)) v else v
    }
}

private fun buildEfficiencyResolver(
    effRows: List<EquipmentEfficiencyRowDto>,
    machines: List<MasterMachineFullDto>,
): (String, String) -> Double? {
    val nameToCd = machines.mapNotNull { m ->
        val n = m.machineName.orEmpty().trim()
        val c = m.machineCd.orEmpty().trim()
        if (n.isNotBlank() && c.isNotBlank()) n to c else null
    }.toMap()
    val byProdName = mutableMapOf<String, Double>()
    val byProdCd = mutableMapOf<String, Double>()
    effRows.forEach { e ->
        if (e.status == 0) return@forEach
        val p = e.productCd.orEmpty().trim()
        if (p.isBlank()) return@forEach
        val rate = e.efficiencyRate ?: return@forEach
        if (rate <= 0.0) return@forEach
        val mn = e.machinesName.orEmpty().trim()
        val mc = e.machineCd.orEmpty().trim()
        if (mn.isNotBlank()) byProdName["$p|$mn"] = rate
        if (mc.isNotBlank()) byProdCd["$p|$mc"] = rate
    }
    return { productCd, moldingMachine ->
        val p = productCd.trim()
        val mm = moldingMachine.trim()
        if (p.isBlank() || mm.isBlank() || mm == "（未設定）") {
            null
        } else {
            byProdName["$p|$mm"]?.takeIf { it > 0.0 }
                ?: nameToCd[mm]?.let { mc -> byProdCd["$p|$mc"]?.takeIf { it > 0.0 } }
                ?: byProdCd["$p|$mm"]?.takeIf { it > 0.0 }
        }
    }
}

private fun buildPostRefHoursByMachine(
    cuttingRows: List<InstructionCuttingRowDto>,
    refDate: String,
    resolveEff: (String, String) -> Double?,
    resolveMachineName: (String?) -> String,
    moldingMachineByProduct: Map<String, String>,
): Map<String, Int> {
    val ref = refDate.trim().take(10)
    if (ref.length < 10) return emptyMap()
    val hoursByLineAndMachine = mutableMapOf<String, MutableMap<String, Int>>()
    cuttingRows.forEach { row ->
        val day = row.productionDay.orEmpty().trim().take(10)
        if (day.length < 10 || day <= ref) return@forEach
        val cd = row.productCd.orEmpty().trim()
        if (cd.isBlank()) return@forEach
        val qty = row.actualProductionQuantity ?: return@forEach
        if (qty <= 0) return@forEach
        val moldingMachine = resolveMachineName(moldingMachineByProduct[cd])
        val rate = resolveEff(cd, moldingMachine) ?: return@forEach
        val hours = (qty / rate).roundToInt()
        if (hours <= 0) return@forEach
        val line = row.productionLine.orEmpty().trim().ifBlank { "（未設定）" }
        val perMachine = hoursByLineAndMachine.getOrPut(line) { mutableMapOf() }
        perMachine[moldingMachine] = (perMachine[moldingMachine] ?: 0) + hours
    }
    val byMachine = mutableMapOf<String, Int>()
    hoursByLineAndMachine.values.forEach { perMachine ->
        perMachine.forEach { (machine, h) -> byMachine[machine] = (byMachine[machine] ?: 0) + h }
    }
    return byMachine
}

fun buildMoldingPreInventoryGroups(
    summaryRows: List<ProductionSummaryRowDto>,
    moldingMachineByProduct: Map<String, String>,
    machines: List<MasterMachineFullDto>,
    formingEfficiency: List<EquipmentEfficiencyRowDto>,
    cuttingRows: List<InstructionCuttingRowDto>,
    refDate: String,
): List<MoldingPreInventoryGroup> {
    val resolveMachineName = buildMachineNameResolver(machines)
    val resolveEff = buildEfficiencyResolver(formingEfficiency, machines)
    val postRefByMachine = buildPostRefHoursByMachine(
        cuttingRows, refDate, resolveEff, resolveMachineName, moldingMachineByProduct,
    )
    val tableRows = summaryRows.mapNotNull { row ->
        val cd = row.productCd.orEmpty().trim()
        if (cd.isBlank()) return@mapNotNull null
        val inv = row.preMoldingInventory?.takeIf { isPositiveInventory(it) } ?: return@mapNotNull null
        val machine = resolveMachineName(moldingMachineByProduct[cd])
        MoldingPreInventoryTableRow(
            productCd = cd,
            productName = row.productName.orEmpty().trim().ifBlank { cd },
            preMoldingInventory = inv,
            moldingMachine = machine,
            efficiencyRate = resolveEff(cd, machine),
        )
    }.sortedWith(compareBy<MoldingPreInventoryTableRow> { it.moldingMachine }.thenBy { it.productName })
    return tableRows.groupBy { it.moldingMachine }
        .toSortedMap(compareBy { it })
        .map { (machine, rows) ->
            val totalHours = rows.sumOf { productionHours(it.preMoldingInventory, it.efficiencyRate) }
            MoldingPreInventoryGroup(
                moldingMachine = machine,
                totalProductionHours = totalHours.toDouble(),
                totalPostRefCuttingHours = postRefByMachine[machine]?.toDouble() ?: 0.0,
            )
        }
}

fun instructionCurrentMonthPeriod(): Pair<String, String> {
    val now = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Tokyo"))
    val start = now.withDayOfMonth(1).toString()
    val end = now.withDayOfMonth(now.lengthOfMonth()).toString()
    return start to end
}

fun previewChamferingManagementCode(
    productionDay: String,
    productionLine: String,
    productCd: String,
    productionSequence: String,
): String {
    val day = productionDay.trim().take(10)
    if (day.length < 10) return ""
    val yy = day.substring(2, 4)
    val mm = day.substring(5, 7)
    val lineSuffix = productionLine.trim().takeLast(2).padEnd(2, '0')
    val seqStr = (productionSequence.trim().toIntOrNull() ?: 1).toString().padStart(2, '0')
    return "$yy$mm${productCd.trim()}$lineSuffix$seqStr"
}
