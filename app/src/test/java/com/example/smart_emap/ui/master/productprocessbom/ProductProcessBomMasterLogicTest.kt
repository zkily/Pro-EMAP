package com.example.smart_emap.ui.master.productprocessbom

import com.example.smart_emap.data.model.ProductProcessBomRowDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductProcessBomMasterLogicTest {

    @Test
    fun fromDto_readsMaterialCutingChamferingFlagsAndLt() {
        val dto = ProductProcessBomRowDto(
            productCd = 12345,
            productName = "テスト製品",
            materialProcess = 1,
            materialProcessLt = 3,
            cutingProcess = 1,
            cutingProcessLt = 5,
            chamferingProcess = 0,
            chamferingProcessLt = 7,
            minStockDays = 10,
            safetyStockDays = 20,
            isDiscontinued = 0,
        )
        val row = ProductProcessBomMasterLogic.fromDto(dto)!!

        assertEquals(12345, row.productCd)
        assertEquals("テスト製品", row.productName)
        assertTrue(row.materialProcess)
        assertEquals(3, row.materialProcessLt)
        assertTrue(row.cutingProcess)
        assertEquals(5, row.cutingProcessLt)
        assertFalse(row.chamferingProcess)
        assertEquals(7, row.chamferingProcessLt)
        assertEquals(10, row.minStockDays)
        assertEquals(20, row.safetyStockDays)
        assertFalse(row.isDiscontinued)
    }

    @Test
    fun isFlagOn_matchesWebBooleanConversion() {
        assertTrue(ProductProcessBomMasterLogic.isFlagOn(1))
        assertFalse(ProductProcessBomMasterLogic.isFlagOn(0))
        assertFalse(ProductProcessBomMasterLogic.isFlagOn(null))
    }

    @Test
    fun fromDto_nullFlagsDefaultToOff() {
        val row = ProductProcessBomMasterLogic.fromDto(
            ProductProcessBomRowDto(productCd = 1, productName = "A"),
        )!!
        assertFalse(row.materialProcess)
        assertFalse(row.cutingProcess)
        assertFalse(row.chamferingProcess)
        assertEquals(0, row.materialProcessLt)
        assertEquals(0, row.cutingProcessLt)
        assertEquals(0, row.chamferingProcessLt)
    }
}
