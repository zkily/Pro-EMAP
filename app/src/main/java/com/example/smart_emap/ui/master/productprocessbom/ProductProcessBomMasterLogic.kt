package com.example.smart_emap.ui.master.productprocessbom

import com.example.smart_emap.data.model.ProductProcessBomRowDto
import com.example.smart_emap.data.model.UpdateProductProcessBomBody

data class ProductProcessBomUiRow(
    val productCd: Int,
    val productName: String,
    val isDiscontinued: Boolean = false,
    val minStockDays: Int = 0,
    val safetyStockDays: Int = 0,
    val materialProcess: Boolean = false,
    val materialProcessLt: Int = 0,
    val cutingProcess: Boolean = false,
    val cutingProcessLt: Int = 0,
    val chamferingProcess: Boolean = false,
    val chamferingProcessLt: Int = 0,
    val swagingProcess: Boolean = false,
    val swagingProcessLt: Int = 0,
    val formingProcess: Boolean = false,
    val formingProcessLt: Int = 0,
    val platingProcess: Boolean = false,
    val platingProcessLt: Int = 0,
    val outsourcedPlatingProcess: Boolean = false,
    val outsourcedPlatingProcessLt: Int = 0,
    val weldingProcess: Boolean = false,
    val weldingProcessLt: Int = 0,
    val outsourcedWeldingProcess: Boolean = false,
    val outsourcedWeldingProcessLt: Int = 0,
    val inspectionProcess: Boolean = false,
    val inspectionProcessLt: Int = 0,
    val outsourcedWarehouseProcess: Boolean = false,
    val outsourcedWarehouseProcessLt: Int = 0,
    val prePlatingWelding: Boolean = false,
    val postInspectionWelding: Boolean = false,
    val postInspectionWeldingLt: Int = 0,
)

object ProductProcessBomMasterLogic {
    fun isFlagOn(value: Int?): Boolean = value == 1

    fun flagToApi(value: Boolean): Int = if (value) 1 else 0

    fun clampLt(value: Int): Int = value.coerceIn(0, 999)

    fun parseLt(text: String): Int = text.filter { it.isDigit() }.toIntOrNull()?.let(::clampLt) ?: 0

    fun fromDto(dto: ProductProcessBomRowDto): ProductProcessBomUiRow? {
        val cd = dto.productCd ?: return null
        return ProductProcessBomUiRow(
            productCd = cd,
            productName = dto.productName.orEmpty(),
            isDiscontinued = isFlagOn(dto.isDiscontinued),
            minStockDays = dto.minStockDays ?: 0,
            safetyStockDays = dto.safetyStockDays ?: 0,
            materialProcess = isFlagOn(dto.materialProcess),
            materialProcessLt = dto.materialProcessLt ?: 0,
            cutingProcess = isFlagOn(dto.cutingProcess),
            cutingProcessLt = dto.cutingProcessLt ?: 0,
            chamferingProcess = isFlagOn(dto.chamferingProcess),
            chamferingProcessLt = dto.chamferingProcessLt ?: 0,
            swagingProcess = isFlagOn(dto.swagingProcess),
            swagingProcessLt = dto.swagingProcessLt ?: 0,
            formingProcess = isFlagOn(dto.formingProcess),
            formingProcessLt = dto.formingProcessLt ?: 0,
            platingProcess = isFlagOn(dto.platingProcess),
            platingProcessLt = dto.platingProcessLt ?: 0,
            outsourcedPlatingProcess = isFlagOn(dto.outsourcedPlatingProcess),
            outsourcedPlatingProcessLt = dto.outsourcedPlatingProcessLt ?: 0,
            weldingProcess = isFlagOn(dto.weldingProcess),
            weldingProcessLt = dto.weldingProcessLt ?: 0,
            outsourcedWeldingProcess = isFlagOn(dto.outsourcedWeldingProcess),
            outsourcedWeldingProcessLt = dto.outsourcedWeldingProcessLt ?: 0,
            inspectionProcess = isFlagOn(dto.inspectionProcess),
            inspectionProcessLt = dto.inspectionProcessLt ?: 0,
            outsourcedWarehouseProcess = isFlagOn(dto.outsourcedWarehouseProcess),
            outsourcedWarehouseProcessLt = dto.outsourcedWarehouseProcessLt ?: 0,
            prePlatingWelding = isFlagOn(dto.prePlatingWelding),
            postInspectionWelding = isFlagOn(dto.postInspectionWelding),
            postInspectionWeldingLt = dto.postInspectionWeldingLt ?: 0,
        )
    }

    fun toUpdateBody(row: ProductProcessBomUiRow): UpdateProductProcessBomBody =
        UpdateProductProcessBomBody(
            minStockDays = row.minStockDays,
            safetyStockDays = row.safetyStockDays,
            materialProcess = flagToApi(row.materialProcess),
            materialProcessLt = row.materialProcessLt,
            cutingProcess = flagToApi(row.cutingProcess),
            cutingProcessLt = row.cutingProcessLt,
            chamferingProcess = flagToApi(row.chamferingProcess),
            chamferingProcessLt = row.chamferingProcessLt,
            swagingProcess = flagToApi(row.swagingProcess),
            swagingProcessLt = row.swagingProcessLt,
            formingProcess = flagToApi(row.formingProcess),
            formingProcessLt = row.formingProcessLt,
            platingProcess = flagToApi(row.platingProcess),
            platingProcessLt = row.platingProcessLt,
            outsourcedPlatingProcess = flagToApi(row.outsourcedPlatingProcess),
            outsourcedPlatingProcessLt = row.outsourcedPlatingProcessLt,
            weldingProcess = flagToApi(row.weldingProcess),
            weldingProcessLt = row.weldingProcessLt,
            outsourcedWeldingProcess = flagToApi(row.outsourcedWeldingProcess),
            outsourcedWeldingProcessLt = row.outsourcedWeldingProcessLt,
            inspectionProcess = flagToApi(row.inspectionProcess),
            inspectionProcessLt = row.inspectionProcessLt,
            outsourcedWarehouseProcess = flagToApi(row.outsourcedWarehouseProcess),
            outsourcedWarehouseProcessLt = row.outsourcedWarehouseProcessLt,
            prePlatingWelding = flagToApi(row.prePlatingWelding),
            postInspectionWelding = flagToApi(row.postInspectionWelding),
            postInspectionWeldingLt = row.postInspectionWeldingLt,
            isDiscontinued = flagToApi(row.isDiscontinued),
        )

    fun syncMessage(data: com.example.smart_emap.data.model.ProductProcessBomSyncDataDto): String {
        val inserted = data.insertedCount ?: 0
        val updated = data.updatedCount ?: 0
        val total = data.totalProcessed ?: 0
        return if (inserted > 0 || updated > 0) {
            "製品情報を同期しました（新規: ${inserted}件、更新: ${updated}件、処理済み: ${total}件）"
        } else {
            data.message ?: "同期するデータがありませんでした"
        }
    }
}
