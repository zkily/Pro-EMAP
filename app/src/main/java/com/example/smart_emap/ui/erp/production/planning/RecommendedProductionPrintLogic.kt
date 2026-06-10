package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object RecommendedProductionPrintLogic {
    private val japanZone = ZoneId.of("Asia/Tokyo")
    private val outputTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm", Locale.JAPAN)
    private const val MAX_YMD = "\uFFFF"

    enum class ProcessKind { PLATING, MOLDING, WELDING }

    data class PrintConfig(
        val kind: ProcessKind,
        val trend: (ProductionSummaryFullRowDto) -> Int?,
        val primaryDateKeyForPick: String = "date",
        val titleOverride: String? = null,
        val hidePreInventoryColumns: Boolean = false,
        val jobName: String,
    ) {
        companion object {
            fun plating() = PrintConfig(
                kind = ProcessKind.PLATING,
                trend = { it.platingTrend },
                jobName = "メッキ推奨生産日リスト",
            )

            fun welding() = PrintConfig(
                kind = ProcessKind.WELDING,
                trend = { it.weldingTrend },
                jobName = "溶接推奨生産日リスト",
            )

            fun molding() = PrintConfig(
                kind = ProcessKind.MOLDING,
                trend = { it.moldingTrend },
                jobName = "成型推奨生産日リスト",
            )

            fun moldingPlan() = PrintConfig(
                kind = ProcessKind.MOLDING,
                trend = { it.moldingActualPlanTrend },
                primaryDateKeyForPick = "molding_production_date",
                titleOverride = "成型計画推奨生産日リスト",
                hidePreInventoryColumns = true,
                jobName = "成型計画推奨生産日リスト",
            )

            fun weldingPlan() = PrintConfig(
                kind = ProcessKind.WELDING,
                trend = { it.weldingActualPlanTrend },
                primaryDateKeyForPick = "welding_production_date",
                titleOverride = "溶接計画推奨生産日リスト",
                hidePreInventoryColumns = true,
                jobName = "溶接計画推奨生産日リスト",
            )

            fun fromAction(action: String): PrintConfig? = when (action) {
                "print-rec-plating" -> plating()
                "print-rec-welding" -> welding()
                "print-rec-molding" -> molding()
                "print-rec-molding-plan" -> moldingPlan()
                "print-rec-welding-plan" -> weldingPlan()
                else -> null
            }
        }
    }

    /** 分页 ingest，与 Web collectRecommendedProductionRows 相同 */
    class Collector(
        private val monthStart: String,
        private val config: PrintConfig,
    ) {
        private val productionDateKey = when (config.kind) {
            ProcessKind.PLATING -> "plating_production_date"
            ProcessKind.MOLDING -> "molding_production_date"
            ProcessKind.WELDING -> "welding_production_date"
        }
        private val secondaryDateKeyForPick =
            if (config.primaryDateKeyForPick == "date") productionDateKey else "date"
        private val bestByProduct = linkedMapOf<String, ProductionSummaryFullRowDto>()

        fun ingest(rows: List<ProductionSummaryFullRowDto>) {
            rows.forEach { row ->
                val pid = row.productCd?.trim().orEmpty()
                if (pid.isEmpty()) return@forEach
                val d = rowCalendarDateStr(row)
                if (d.isBlank() || d < monthStart) return@forEach
                val trendVal = config.trend(row)
                if (trendVal == null || trendVal >= 0) return@forEach
                val prev = bestByProduct[pid]
                if (prev == null) {
                    bestByProduct[pid] = row
                } else {
                    bestByProduct[pid] = pickBetterRecommendedRow(
                        prev = prev,
                        row = row,
                        trend = config.trend,
                        primaryDateKeyForPick = config.primaryDateKeyForPick,
                        secondaryDateKeyForPick = secondaryDateKeyForPick,
                    )
                }
            }
        }

        fun sortedRows(): List<ProductionSummaryFullRowDto> =
            sortRecommendedPrintRows(bestByProduct.values.toList(), config.kind)
    }

    fun buildHtml(
        rows: List<ProductionSummaryFullRowDto>,
        config: PrintConfig,
        monthStart: String,
        rangeEnd: String,
    ): String {
        val pc = processConfig(config.kind)
        val sheetTitle = config.titleOverride ?: pc.title
        val hidePreInvCols = config.hidePreInventoryColumns
        val now = ZonedDateTime.now(japanZone).format(outputTimeFormatter)
        val todayYmd = LocalDate.now(japanZone).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val printGridColumns = if (hidePreInvCols) {
            "minmax(0, 1.25fr) 70px"
        } else {
            "minmax(0, 1.25fr) 70px 70px 5.0em"
        }

        var blocks = ""
        var prevGroupKey: String? = null
        rows.forEach { row ->
            val machine = pc.machine(row)?.trim().orEmpty()
            val groupKey = machine.ifBlank { "__empty__" }
            if (groupKey != prevGroupKey) {
                if (prevGroupKey != null) blocks += "</div></section>"
                prevGroupKey = groupKey
                val heading = machine.ifBlank { "（未設定）" }
                blocks += """<section class="machine-group"><header class="machine-title">${pc.machineHeading}: ${escapeHtml(heading)}</header><div class="machine-body">"""
                blocks += """<div class="col-head"><span class="ch-pname">製品名</span><span class="ch-date">${pc.dateLabel}</span>"""
                if (!hidePreInvCols) {
                    blocks += """<span class="ch-inv">${pc.invColHeader}</span><span class="ch-prev">直前工程</span>"""
                }
                blocks += "</div>"
            }
            val pname = row.productName?.takeIf { !it.isNullOrBlank() } ?: row.productCd.orEmpty()
            val prodDate = pc.date(row)
            val dateDueClass = if (isRecommendedDateDueOrPast(prodDate, todayYmd)) " cell-date--due" else ""
            blocks += """<div class="item-row"><span class="cell-pname">${escapeHtml(pname)}</span>"""
            blocks += """<span class="cell-date$dateDueClass">${dateCellPlain(prodDate)}</span>"""
            if (!hidePreInvCols) {
                blocks += """<span class="cell-inv">${preInvPlain(row, pc)}</span><span class="cell-prev">${prePrevPlain(row, pc)}</span>"""
            }
            blocks += "</div>"
        }
        if (prevGroupKey != null) blocks += "</div></section>"
        if (rows.isEmpty()) blocks = """<p class="empty-msg">該当データがありません</p>"""

        val preInvSum = if (hidePreInvCols) 0 else rows.sumOf { pc.preInv(it) ?: 0 }
        val preInvSumStr = formatProductionNumber(preInvSum)

        return """<!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/><title>$sheetTitle</title>
<style>
@page { size: A4 portrait; margin: 10mm; }
*{box-sizing:border-box;}
body{font-family:sans-serif;margin:0;padding:12px;font-size:10.5pt;-webkit-print-color-adjust:exact;print-color-adjust:exact;}
@media print{body{padding:0;}}
h1.sheet-title{font-size:14pt;margin:0 0 6px 0;font-weight:700;}
p.subtitle{font-size:9pt;color:#64748b;margin:0 0 12px 0;}
.print-meta-row{display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:8px 20px;margin:0 0 10px 0;}
.print-meta-row .subtitle{flex:1;min-width:220px;margin:0;}
.subtitle-total{margin:0;padding-top:1px;font-size:9pt;color:#334155;text-align:right;white-space:nowrap;}
.subtitle-total strong{font-variant-numeric:tabular-nums;font-size:10pt;color:#0f172a;}
.columns-wrap{column-count:2;column-gap:14px;column-fill:auto;}
.machine-group{break-inside:avoid-page;page-break-inside:avoid;margin-bottom:12px;}
.machine-title{font-weight:700;background:#e2e8f0;padding:5px 8px;border:1px solid #94a3b8;border-bottom:none;}
.machine-body{padding:4px 8px 6px 14px;border:1px solid #cbd5e1;border-top:none;background:#fff;}
.col-head,.item-row{display:grid;grid-template-columns:$printGridColumns;column-gap:6px;row-gap:2px;font-size:9pt;}
.col-head{align-items:end;padding:2px 0 4px;border-bottom:1px solid #e2e8f0;margin-bottom:2px;font-size:8pt;color:#475569;font-weight:600;}
.item-row{align-items:center;padding:3px 0;border-bottom:1px solid #f1f5f9;font-size:9.5pt;}
.item-row:last-child{border-bottom:none;}
.ch-pname,.cell-pname{text-align:left;min-width:0;word-break:break-word;overflow-wrap:anywhere;}
.ch-date,.ch-inv,.ch-prev{text-align:center;justify-self:stretch;}
.ch-inv,.ch-prev{font-size:8.5pt;}
.cell-date,.cell-inv,.cell-prev{text-align:right;font-variant-numeric:tabular-nums;justify-self:stretch;}
.cell-date.cell-date--due{color:#dc2626;font-weight:700;}
.empty-msg{color:#64748b;font-size:10pt;}
.print-note{font-size:8pt;color:#64748b;margin:4px 0 0 0;line-height:1.35;}
</style></head><body>
<h1 class="sheet-title">$sheetTitle</h1>
<div class="print-meta-row">
<p class="subtitle">対象期間: $monthStart ～ $rangeEnd / 出力: $now</p>
${if (hidePreInvCols) "" else """<p class="subtitle-total">${pc.sumLabel}: <strong>$preInvSumStr</strong></p>"""}
</div>
${if (hidePreInvCols) "" else """<p class="print-note">${pc.footNote}</p>"""}
<div class="columns-wrap">$blocks</div>
</body></html>"""
    }

    private data class ProcessConfig(
        val title: String,
        val machineHeading: String,
        val dateLabel: String,
        val invColHeader: String,
        val sumLabel: String,
        val footNote: String,
        val machine: (ProductionSummaryFullRowDto) -> String?,
        val date: (ProductionSummaryFullRowDto) -> String?,
        val preInv: (ProductionSummaryFullRowDto) -> Int?,
        val prePrev: (ProductionSummaryFullRowDto) -> String?,
    )

    private fun processConfig(kind: ProcessKind): ProcessConfig = when (kind) {
        ProcessKind.PLATING -> ProcessConfig(
            title = "メッキ推奨生産日リスト",
            machineHeading = "メッキ治具",
            dateLabel = "推奨生産日",
            invColHeader = "メッキ前在庫",
            sumLabel = "メッキ前在庫合計",
            footNote = "メッキ前在庫・直前工程は、工程ルート上でメッキ（または外注メッキ）の直前工程の前日15時の集計在庫です（一覧と同じ計算）。",
            machine = { it.platingMachine },
            date = { it.platingProductionDate },
            preInv = { it.prePlatingInventory },
            prePrev = { it.prePlatingPrevProcess },
        )
        ProcessKind.MOLDING -> ProcessConfig(
            title = "成型推奨生産日リスト",
            machineHeading = "成型機",
            dateLabel = "推奨生産日",
            invColHeader = "成型前在庫",
            sumLabel = "成型前在庫合計",
            footNote = "成型前在庫・直前工程は、工程ルート上で成型の直前工程の集計在庫です（一覧・メッキ前在庫と同じ算出方式）。",
            machine = { it.moldingMachine },
            date = { it.moldingProductionDate },
            preInv = { it.preMoldingInventory },
            prePrev = { it.preMoldingPrevProcess },
        )
        ProcessKind.WELDING -> ProcessConfig(
            title = "溶接推奨生産日リスト",
            machineHeading = "溶接機",
            dateLabel = "推奨生産日",
            invColHeader = "溶接前在庫",
            sumLabel = "溶接前在庫合計",
            footNote = "溶接前在庫・直前工程は、工程ルート上で溶接（または外注溶接）の直前工程の集計在庫です（一覧・メッキ前在庫と同じ算出方式）。",
            machine = { it.weldingMachine },
            date = { it.weldingProductionDate },
            preInv = { it.preWeldingInventory },
            prePrev = { it.preWeldingPrevProcess },
        )
    }

    private fun pickBetterRecommendedRow(
        prev: ProductionSummaryFullRowDto,
        row: ProductionSummaryFullRowDto,
        trend: (ProductionSummaryFullRowDto) -> Int?,
        primaryDateKeyForPick: String,
        secondaryDateKeyForPick: String,
    ): ProductionSummaryFullRowDto {
        val pa = pickYmd(row, primaryDateKeyForPick)
        val pb = pickYmd(prev, primaryDateKeyForPick)
        if (pa < pb) return row
        if (pb < pa) return prev
        val sa = pickYmd(row, secondaryDateKeyForPick)
        val sb = pickYmd(prev, secondaryDateKeyForPick)
        if (sa < sb) return row
        if (sb < sa) return prev
        val ta = trend(prev) ?: 0
        val tb = trend(row) ?: 0
        return if (ta <= tb) prev else row
    }

    private fun sortRecommendedPrintRows(
        rows: List<ProductionSummaryFullRowDto>,
        kind: ProcessKind,
    ): List<ProductionSummaryFullRowDto> {
        val machineKey = when (kind) {
            ProcessKind.PLATING -> { r: ProductionSummaryFullRowDto -> r.platingMachine }
            ProcessKind.MOLDING -> { r: ProductionSummaryFullRowDto -> r.moldingMachine }
            ProcessKind.WELDING -> { r: ProductionSummaryFullRowDto -> r.weldingMachine }
        }
        val dateKey = when (kind) {
            ProcessKind.PLATING -> { r: ProductionSummaryFullRowDto -> r.platingProductionDate }
            ProcessKind.MOLDING -> { r: ProductionSummaryFullRowDto -> r.moldingProductionDate }
            ProcessKind.WELDING -> { r: ProductionSummaryFullRowDto -> r.weldingProductionDate }
        }
        return rows.sortedWith(compareBy(
            { (machineKey(it)?.trim().orEmpty()).ifBlank { MAX_YMD } },
            { productionDateYmdKey(dateKey(it)) },
        ))
    }

    private fun rowCalendarDateStr(row: ProductionSummaryFullRowDto): String =
        row.date?.trim()?.take(10).orEmpty()

    private fun productionDateYmdKey(value: String?): String {
        if (value.isNullOrBlank()) return MAX_YMD
        return value.trim().take(10).ifBlank { MAX_YMD }
    }

    private fun pickYmd(row: ProductionSummaryFullRowDto, key: String): String = when (key) {
        "date" -> rowCalendarDateStr(row).ifBlank { MAX_YMD }
        "molding_production_date" -> productionDateYmdKey(row.moldingProductionDate)
        "welding_production_date" -> productionDateYmdKey(row.weldingProductionDate)
        "plating_production_date" -> productionDateYmdKey(row.platingProductionDate)
        else -> MAX_YMD
    }

    private val prePrevLabels = mapOf(
        "cutting" to "切断",
        "chamfering" to "面取",
        "molding" to "成型",
        "plating" to "メッキ",
        "welding" to "溶接",
        "inspection" to "検査",
        "warehouse" to "倉庫",
        "outsourced_warehouse" to "外注倉庫",
        "outsourced_plating" to "外注メッキ",
        "outsourced_welding" to "外注溶接",
        "pre_welding_inspection" to "溶接前検査",
        "pre_inspection" to "外注支給前",
        "pre_outsourcing" to "外注検査前",
    )

    private fun formatPrePrevProcess(value: String?): String {
        if (value.isNullOrBlank()) return "—"
        return prePrevLabels[value] ?: value
    }

    private fun dateCellPlain(value: String?): String {
        if (value.isNullOrBlank()) return "—"
        return escapeHtml(value.trim().take(10))
    }

    private fun preInvPlain(row: ProductionSummaryFullRowDto, pc: ProcessConfig): String {
        val v = pc.preInv(row) ?: return "—"
        return escapeHtml(formatProductionNumber(v))
    }

    private fun prePrevPlain(row: ProductionSummaryFullRowDto, pc: ProcessConfig): String =
        escapeHtml(formatPrePrevProcess(pc.prePrev(row)))

    private fun isRecommendedDateDueOrPast(value: String?, todayYmd: String): Boolean {
        val d = value?.trim()?.take(10).orEmpty()
        if (d.length != 10) return false
        return d <= todayYmd
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
