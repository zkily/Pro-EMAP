package com.example.smart_emap.ui.master.productmachineconfig

import com.example.smart_emap.data.model.AvailableProductDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.ProductMachineConfigCreateBody
import com.example.smart_emap.data.model.ProductMachineConfigRowDto
import com.example.smart_emap.data.model.ProductMachineConfigSyncDataDto
import com.example.smart_emap.data.model.ProductMachineConfigUpdateBody

data class ProductMachineConfigUiRow(
    val id: Int,
    val productCd: String,
    val productName: String,
    val cuttingMachine: String = "",
    val chamferingMachine: String = "",
    val swMachine: String = "",
    val moldingMachine: String = "",
    val platingMachine: String = "",
    val weldingMachine: String = "",
    val inspectorMachine: String = "",
    val outsourcedPlatingMachine: String = "",
    val outsourcedWeldingMachine: String = "",
)

data class MachineOption(val value: String, val label: String)

object ProductMachineConfigMasterLogic {
    fun fromDto(dto: ProductMachineConfigRowDto): ProductMachineConfigUiRow? {
        val id = dto.id ?: return null
        val cd = dto.productCd?.trim().orEmpty()
        if (cd.isEmpty()) return null
        return ProductMachineConfigUiRow(
            id = id,
            productCd = cd,
            productName = dto.productName.orEmpty(),
            cuttingMachine = dto.cuttingMachine.orEmpty(),
            chamferingMachine = dto.chamferingMachine.orEmpty(),
            swMachine = dto.swMachine.orEmpty(),
            moldingMachine = dto.moldingMachine.orEmpty(),
            platingMachine = dto.platingMachine.orEmpty(),
            weldingMachine = dto.weldingMachine.orEmpty(),
            inspectorMachine = dto.inspectorMachine.orEmpty(),
            outsourcedPlatingMachine = dto.outsourcedPlatingMachine.orEmpty(),
            outsourcedWeldingMachine = dto.outsourcedWeldingMachine.orEmpty(),
        )
    }

    fun filterRows(rows: List<ProductMachineConfigUiRow>, keyword: String): List<ProductMachineConfigUiRow> {
        val q = keyword.trim().lowercase()
        if (q.isEmpty()) return rows
        return rows.filter {
            it.productCd.lowercase().contains(q) || it.productName.lowercase().contains(q)
        }
    }

    fun machineOptions(machines: List<MasterMachineFullDto>): List<MachineOption> =
        machines.mapNotNull { m ->
            val cd = m.machineCd?.trim().orEmpty()
            if (cd.isEmpty()) return@mapNotNull null
            val name = m.machineName.orEmpty()
            MachineOption(cd, if (name.isBlank()) cd else "$name ($cd)")
        }.distinctBy { it.value }

    fun productOptions(products: List<AvailableProductDto>): List<Pair<String, String>> =
        products.mapNotNull { p ->
            val cd = p.productCd?.trim().orEmpty()
            if (cd.isEmpty()) return@mapNotNull null
            val name = p.productName.orEmpty()
            cd to if (name.isBlank()) cd else "$name ($cd)"
        }

    fun toCreateBody(row: ProductMachineConfigUiRow): ProductMachineConfigCreateBody =
        ProductMachineConfigCreateBody(
            productCd = row.productCd,
            productName = row.productName,
            cuttingMachine = row.cuttingMachine.blankToNull(),
            chamferingMachine = row.chamferingMachine.blankToNull(),
            swMachine = row.swMachine.blankToNull(),
            moldingMachine = row.moldingMachine.blankToNull(),
            platingMachine = row.platingMachine.blankToNull(),
            weldingMachine = row.weldingMachine.blankToNull(),
            inspectorMachine = row.inspectorMachine.blankToNull(),
            outsourcedPlatingMachine = row.outsourcedPlatingMachine.blankToNull(),
            outsourcedWeldingMachine = row.outsourcedWeldingMachine.blankToNull(),
        )

    fun toUpdateBody(row: ProductMachineConfigUiRow): ProductMachineConfigUpdateBody =
        ProductMachineConfigUpdateBody(
            productName = row.productName,
            cuttingMachine = row.cuttingMachine.blankToNull(),
            chamferingMachine = row.chamferingMachine.blankToNull(),
            swMachine = row.swMachine.blankToNull(),
            moldingMachine = row.moldingMachine.blankToNull(),
            platingMachine = row.platingMachine.blankToNull(),
            weldingMachine = row.weldingMachine.blankToNull(),
            inspectorMachine = row.inspectorMachine.blankToNull(),
            outsourcedPlatingMachine = row.outsourcedPlatingMachine.blankToNull(),
            outsourcedWeldingMachine = row.outsourcedWeldingMachine.blankToNull(),
        )

    fun syncMessage(data: ProductMachineConfigSyncDataDto): String {
        val added = data.added ?: 0
        val updated = data.updated ?: 0
        return when {
            added > 0 || updated > 0 -> "同期完了: 新規追加 ${added}件、更新 ${updated}件"
            else -> "同期完了: 更新するデータがありませんでした"
        }
    }

    fun emptyFormRow(): ProductMachineConfigUiRow = ProductMachineConfigUiRow(
        id = 0,
        productCd = "",
        productName = "",
    )

    private fun String.blankToNull(): String? = trim().ifBlank { null }
}
