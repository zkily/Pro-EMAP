package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ProcessPlanPrintLogic {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE
    private val japanZone = ZoneId.of("Asia/Tokyo")
    private val outputTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm", Locale.JAPAN)

    fun defaultTargetDate(): String = LocalDate.now(japanZone).format(iso)

    fun normalizeTargetDate(targetDate: String): String = targetDate.trim().take(10)

    /** 分页 ingest，避免一次性持有 5 万行导致 OOM（逻辑与 Web buildProcessPrintHtml 相同） */
    class Accumulator(targetDate: String) {
        val normalizedTarget: String = normalizeTargetDate(targetDate)
        var totalRowsIngested: Int = 0
            private set

        private val todayDataMap = linkedMapOf<String, ProductionSummaryFullRowDto>()
        private val moldingBest = linkedMapOf<String, ProductionSummaryFullRowDto>()
        private val platingBest = linkedMapOf<String, ProductionSummaryFullRowDto>()
        private val weldingBest = linkedMapOf<String, ProductionSummaryFullRowDto>()
        private val warehouseRows = mutableListOf<ProductionSummaryFullRowDto>()
        private val warehouseSeen = mutableSetOf<String>()

        fun ingest(rows: List<ProductionSummaryFullRowDto>) {
            totalRowsIngested += rows.size
            rows.forEach { row ->
                val pid = row.productCd?.trim().orEmpty()
                val calD = row.date?.take(10).orEmpty()
                if (pid.isNotEmpty() && calD == normalizedTarget) {
                    todayDataMap[pid] = row
                    val inv = row.warehouseInventory ?: row.inspectionInventory
                    if (inv != null && inv < 0 && pid !in warehouseSeen) {
                        warehouseSeen.add(pid)
                        warehouseRows.add(row)
                    }
                }
                if (pid.isEmpty()) return@forEach

                if (dateYmdKey(row.moldingProductionDate) == normalizedTarget) {
                    val trend = row.moldingTrend
                    if (trend != null && trend < 0) {
                        moldingBest[pid] = pickMinTrendRow(moldingBest[pid], row) { it.moldingTrend }
                    }
                }
                if (dateYmdKey(row.platingProductionDate) == normalizedTarget) {
                    val trend = row.platingTrend
                    if (trend != null && trend < 0) {
                        platingBest[pid] = pickMinTrendRow(platingBest[pid], row) { it.platingTrend }
                    }
                }
                if (dateYmdKey(row.weldingProductionDate) == normalizedTarget) {
                    val trend = row.weldingTrend
                    if (trend != null && trend < 0) {
                        weldingBest[pid] = pickMinTrendRow(weldingBest[pid], row) { it.weldingTrend }
                    }
                }
            }
        }

        fun buildHtml(): String {
            val now = java.time.ZonedDateTime.now(japanZone).format(outputTimeFormatter)
            val tablesHtml = buildString {
                append(buildProcessTable(
                    name = "成型工程",
                    color = "#6366f1",
                    rows = moldingBest.values.sortedBy { productLabel(it) },
                    todayDataMap = todayDataMap,
                    config = ProcessTableConfig(
                        productionDate = { dateYmdDisplay(it.moldingProductionDate) },
                        trend = { it.moldingTrend },
                        plan = { it.moldingPlan },
                        dateLabel = "推奨成型生産日",
                        trendLabel = "成型推移",
                        planLabel = "成型計画",
                    ),
                ))
                append(buildProcessTable(
                    name = "メッキ工程",
                    color = "#f59e0b",
                    rows = platingBest.values.sortedBy { productLabel(it) },
                    todayDataMap = todayDataMap,
                    config = ProcessTableConfig(
                        productionDate = { dateYmdDisplay(it.platingProductionDate) },
                        trend = { it.platingTrend },
                        plan = { it.platingPlan },
                        dateLabel = "推奨メッキ生産日",
                        trendLabel = "メッキ推移",
                        planLabel = "メッキ計画",
                    ),
                ))
                append(buildProcessTable(
                    name = "溶接工程",
                    color = "#10b981",
                    rows = weldingBest.values.sortedBy { productLabel(it) },
                    todayDataMap = todayDataMap,
                    config = ProcessTableConfig(
                        productionDate = { dateYmdDisplay(it.weldingProductionDate) },
                        trend = { it.weldingTrend },
                        plan = { it.weldingPlan },
                        dateLabel = "推奨溶接生産日",
                        trendLabel = "溶接推移",
                        planLabel = "溶接計画",
                    ),
                ))
                append(buildWarehouseTable(rows = warehouseRows.sortedBy { productLabel(it) }))
            }
            return """<!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/><title>工程別生産計画確認サマリー</title>
<style>
body{font-family: sans-serif; padding: 12px; font-size: 12px;}
@media print { body { padding: 0; } }
h1{font-size:16px;margin:0 0 4px 0;}
.subtitle{font-size:11px;color:#64748b;margin-bottom:16px;}
.print-table{border-collapse:collapse;width:100%;margin-bottom:8px;}
.print-table th,.print-table td{border:1px solid #cbd5e1;padding:6px 8px;text-align:left;}
.print-table th{background:#f1f5f9;font-weight:600;}
.print-table td.negative{color:#dc2626;}
.print-table td.plan-ok{background:#dcfce7;}
.print-table td.plan-warn{background:#fee2e2;}
.print-table td.check-cell{text-align:center;}
.process-block{page-break-inside:avoid;}
</style>
</head><body>
<h1>工程別生産計画確認サマリー(成型、メッキ、溶接、倉庫)</h1>
<p class="subtitle">対象日: $normalizedTarget / 出力時間: $now</p>
$tablesHtml
</body></html>"""
        }
    }

    private data class ProcessTableConfig(
        val productionDate: (ProductionSummaryFullRowDto) -> String,
        val trend: (ProductionSummaryFullRowDto) -> Int?,
        val plan: (ProductionSummaryFullRowDto) -> Int?,
        val dateLabel: String,
        val trendLabel: String,
        val planLabel: String,
    )

    private fun buildProcessTable(
        name: String,
        color: String,
        rows: List<ProductionSummaryFullRowDto>,
        todayDataMap: Map<String, ProductionSummaryFullRowDto>,
        config: ProcessTableConfig,
    ): String {
        val body = rows.joinToString("") { row ->
            val pid = row.productCd.orEmpty()
            val todayRow = todayDataMap[pid]
            val planVal = todayRow?.let { config.plan(it) }
            val planState = if (planVal != null && planVal > 0) "生産計画あり" else "確認必要"
            val planClass = if (planVal != null && planVal > 0) "plan-ok" else "plan-warn"
            val trendVal = config.trend(row)
            val trendClass = if (trendVal != null && trendVal < 0) "negative" else ""
            val planDisplay = config.plan(row)?.toString() ?: "—"
            val trendDisplay = trendVal?.toString() ?: "—"
            """<tr><td>${escapeHtml(productLabel(row))}</td><td>${config.productionDate(row)}</td><td class="$trendClass">$trendDisplay</td><td>$planDisplay</td><td class="$planClass">$planState</td><td class="check-cell">□</td></tr>"""
        }
        return """<div class="process-block" style="margin-bottom:20px;break-inside:avoid;"><h2 style="color:$color;font-size:14px;margin:0 0 8px 0;">$name</h2>
<table class="print-table"><thead><tr><th>製品名</th><th>${config.dateLabel}</th><th>${config.trendLabel}</th><th>${config.planLabel}</th><th>計画状態</th><th>確認</th></tr></thead><tbody>$body</tbody></table></div>"""
    }

    private fun buildWarehouseTable(rows: List<ProductionSummaryFullRowDto>): String {
        val body = rows.joinToString("") { row ->
            val inv = row.inspectionInventory
            val wh = row.warehouseInventory
            val invDisplay = inv?.toString() ?: "—"
            val whDisplay = wh?.toString() ?: "—"
            val invClass = if (inv != null && inv < 0) "negative" else ""
            val whClass = if (wh != null && wh < 0) "negative" else ""
            """<tr><td>${escapeHtml(productLabel(row))}</td><td class="$invClass">$invDisplay</td><td class="$whClass">$whDisplay</td><td class="check-cell">□</td></tr>"""
        }
        return """<div class="process-block" style="margin-bottom:20px;break-inside:avoid;"><h2 style="color:#8b5cf6;font-size:14px;margin:0 0 8px 0;">倉庫</h2>
<table class="print-table"><thead><tr><th>製品名</th><th>検査在庫</th><th>倉庫在庫</th><th>確認</th></tr></thead><tbody>$body</tbody></table></div>"""
    }

    private fun pickMinTrendRow(
        prev: ProductionSummaryFullRowDto?,
        row: ProductionSummaryFullRowDto,
        trendGetter: (ProductionSummaryFullRowDto) -> Int?,
    ): ProductionSummaryFullRowDto {
        if (prev == null) return row
        val tr = trendGetter(row)
        val tp = trendGetter(prev)
        if (tr == null) return prev
        if (tp == null) return row
        return if (tp < tr) prev else row
    }

    private fun dateYmdKey(value: String?): String = value?.trim()?.take(10).orEmpty()

    private fun dateYmdDisplay(value: String?): String {
        val key = dateYmdKey(value)
        return key.ifBlank { "—" }
    }

    private fun productLabel(row: ProductionSummaryFullRowDto): String =
        row.productName?.trim().takeUnless { it.isNullOrEmpty() } ?: row.productCd.orEmpty()

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
