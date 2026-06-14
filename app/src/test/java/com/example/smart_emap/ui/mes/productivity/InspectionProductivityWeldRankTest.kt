package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.data.model.InspectionProductivitySessionRowDto
import com.example.smart_emap.data.model.ProductProcessBomRowDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Web `InspectionProductivityAnalysis.vue` 溶接ランキング算法对齐测试 */
class InspectionProductivityWeldRankTest {

    @Test
    fun buildWeldingProductCdSetFromProductCds_matchesBomRowSet() {
        val fromCds = InspectionProductivityLogic.buildWeldingProductCdSetFromProductCds(listOf(100, 200))
        assertTrue(fromCds.contains("100"))
        assertTrue(fromCds.contains("200"))
    }

    @Test
    fun buildWeldingProductCdSet_matchesWebBomFlags() {
        val rows = listOf(
            ProductProcessBomRowDto(productCd = 100, weldingProcess = 1),
            ProductProcessBomRowDto(productCd = 200, outsourcedWeldingProcess = 1),
            ProductProcessBomRowDto(productCd = 300, postInspectionWelding = 1),
            ProductProcessBomRowDto(productCd = 400, prePlatingWelding = 1),
            ProductProcessBomRowDto(productCd = 500, weldingProcess = 0),
        )
        val set = InspectionProductivityLogic.buildWeldingProductCdSet(rows)
        assertTrue(set.contains("100"))
        assertTrue(set.contains("200"))
        assertTrue(set.contains("300"))
        assertTrue(set.contains("400"))
        assertTrue(!set.contains("500"))
    }

    @Test
    fun sessionProductHasWelding_normalizesNumericProductCd() {
        val welding = setOf("12345")
        assertTrue(InspectionProductivityLogic.sessionProductHasWelding("12345", welding))
        assertTrue(InspectionProductivityLogic.sessionProductHasWelding("012345", welding))
    }

    @Test
    fun weldRankWithoutWelding_excludesWeldingProducts() {
        val welding = setOf("100")
        val sessions = listOf(
            session(productCd = "100", inspectorId = 1, name = "A", qty = 100, sec = 3600),
            session(productCd = "200", inspectorId = 1, name = "A", qty = 200, sec = 3600),
        )
        val rows = rankWithoutWelding(sessions, welding)
        assertEquals(1, rows.size)
        assertEquals(200, rows[0].sumActualQty)
        assertEquals(200.0, rows[0].avgEfficiencyPerHour)
    }

    @Test
    fun weldRankWithWelding_onlyIncludesWeldingProducts() {
        val welding = setOf("100")
        val sessions = listOf(
            session(productCd = "100", inspectorId = 1, name = "A", qty = 100, sec = 3600),
            session(productCd = "200", inspectorId = 1, name = "A", qty = 200, sec = 3600),
        )
        val rows = rankWithWelding(sessions, welding)
        assertEquals(1, rows.size)
        assertEquals(100, rows[0].sumActualQty)
        assertEquals(100.0, rows[0].avgEfficiencyPerHour)
    }

    @Test
    fun weldRank_sortsByAvgEfficiencyDescendingAndAssignsRank() {
        val sessions = listOf(
            session(productCd = "P1", inspectorId = 1, name = "Fast", qty = 600, sec = 3600),
            session(productCd = "P2", inspectorId = 2, name = "Slow", qty = 300, sec = 3600),
        )
        val rows = rankWithoutWelding(sessions, emptySet())
        assertEquals(2, rows.size)
        assertEquals(1, rows[0].rank)
        assertEquals("Fast", rows[0].inspectorName)
        assertEquals(600.0, rows[0].avgEfficiencyPerHour)
        assertEquals(2, rows[1].rank)
        assertEquals("Slow", rows[1].inspectorName)
        assertEquals(300.0, rows[1].avgEfficiencyPerHour)
    }

    @Test
    fun weldRank_excludesInspectorsWithoutNetProductionTime() {
        val sessions = listOf(
            session(productCd = "P1", inspectorId = 1, name = "NoTime", qty = 100, sec = 0),
            session(productCd = "P2", inspectorId = 2, name = "Ok", qty = 100, sec = 3600),
        )
        val rows = rankWithoutWelding(sessions, emptySet())
        assertEquals(1, rows.size)
        assertEquals("Ok", rows[0].inspectorName)
    }

    @Test
    fun weldRank_defectRateRoundedToOneDecimal() {
        val sessions = listOf(
            session(productCd = "P1", inspectorId = 1, name = "A", qty = 1000, defect = 5, sec = 3600),
        )
        val rows = rankWithoutWelding(sessions, emptySet())
        assertEquals(0.5, rows[0].defectRatePercent!!, 0.001)
    }

    @Test
    fun weldRank_aggregatesMultipleSessionsPerInspector() {
        val sessions = listOf(
            session(productCd = "P1", inspectorId = 1, name = "A", qty = 100, sec = 1800),
            session(productCd = "P2", inspectorId = 1, name = "A", qty = 100, sec = 1800),
        )
        val rows = rankWithoutWelding(sessions, emptySet())
        assertEquals(1, rows.size)
        assertEquals(2, rows[0].sessionCount)
        assertEquals(200, rows[0].sumActualQty)
        assertEquals(200.0, rows[0].avgEfficiencyPerHour)
    }

    private fun rankWithoutWelding(
        sessions: List<InspectionProductivitySessionRowDto>,
        welding: Set<String>,
    ) = InspectionProductivityLogic.buildInspectorAvgRankBySessions(sessions) { cd ->
        !InspectionProductivityLogic.sessionProductHasWelding(cd, welding)
    }

    private fun rankWithWelding(
        sessions: List<InspectionProductivitySessionRowDto>,
        welding: Set<String>,
    ) = InspectionProductivityLogic.buildInspectorAvgRankBySessions(sessions) { cd ->
        InspectionProductivityLogic.sessionProductHasWelding(cd, welding)
    }

    private fun session(
        productCd: String,
        inspectorId: Int,
        name: String,
        qty: Int,
        sec: Int,
        defect: Int = 0,
    ) = InspectionProductivitySessionRowDto(
        mesInspectorUserId = inspectorId,
        inspectorDisplayName = name,
        productCd = productCd,
        actualProductionQuantity = qty,
        defectQty = defect,
        netProductionSec = sec,
    )
}
