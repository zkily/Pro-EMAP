package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.PlanBaselineFullComparisonItemDto
import com.example.smart_emap.ui.mes.planinstruction.formatPlanComparisonValue

fun buildBaselineExportHtml(
    monthLabel: String,
    tabs: List<PlanBaselineProcessTab>,
    operationRateMonthLabel: String,
    operationRateProcessLabel: String,
    operationRateRows: List<PlanBaselineUtilizationRow>,
    scheduledWorkdays: Int? = null,
): String {
    val sections = tabs.joinToString("") { tab ->
        val inner = buildBaselineComparisonPrintHtml(tab.name, monthLabel, tab.items, scheduledWorkdays)
        val bodyStart = inner.indexOf("<body>") + 6
        val bodyEnd = inner.indexOf("</body>")
        val body = if (bodyStart > 5 && bodyEnd > bodyStart) inner.substring(bodyStart, bodyEnd) else inner
        """<section class="report-section">$body</section>"""
    }
    val operationSection = if (operationRateRows.isNotEmpty()) {
        val inner = buildBaselineOperationRatePrintHtml(operationRateMonthLabel, operationRateProcessLabel, operationRateRows)
        val bodyStart = inner.indexOf("<body>") + 6
        val bodyEnd = inner.indexOf("</body>")
        val body = if (bodyStart > 5 && bodyEnd > bodyStart) inner.substring(bodyStart, bodyEnd) else inner
        """<section class="report-section page-break">$body</section>"""
    } else ""
    return """
      <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>
      <title>工程別報告書</title>
      <style>
        @page { margin: 12mm; }
        body { font-family: 'Segoe UI','Meiryo',sans-serif; font-size: 11px; color: #111; }
        .report-cover { margin-bottom: 16px; padding-bottom: 10px; border-bottom: 2px solid #6366f1; }
        .report-cover h1 { margin: 0 0 6px; font-size: 18px; }
        .report-cover p { margin: 0; color: #475569; font-size: 12px; }
        .report-section { margin-bottom: 18px; page-break-inside: avoid; }
        .page-break { page-break-before: always; }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td { border: 1px solid #cbd5e1; padding: 5px 6px; }
        th { background: #f1f5f9; font-weight: 700; text-align: center; }
        .td-num, .num { text-align: right; font-family: Consolas, monospace; }
        .td-date { text-align: center; }
        .diff-pos { color: #059669; font-weight: 700; }
        .diff-neg { color: #dc2626; font-weight: 700; }
        .diff-muted { color: #94a3b8; }
        .row-total td { background: #f8fafc; font-weight: 700; }
        h1 { font-size: 15px; margin: 0 0 6px; }
        .meta { font-size: 10px; color: #333; margin: 0 0 10px; }
      </style></head><body>
      <div class="report-cover">
        <h1>生産計画ベースライン 工程別報告書</h1>
        <p>対象月: $monthLabel</p>
      </div>
      $sections
      $operationSection
      </body></html>
    """.trimIndent()
}

fun buildBaselineComparisonPrintHtml(
    processName: String,
    monthLabel: String,
    items: List<PlanBaselineFullComparisonItemDto>,
    scheduledWorkdays: Int? = null,
): String {
    fun esc(v: String) = v
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    fun diffCell(value: Double?): String {
        val text = formatPlanComparisonValue(value)
        if (value == null) return """<td class="td-num"><span class="diff-muted">${esc(text)}</span></td>"""
        val cls = when (PlanBaselineLogic.diffTone(value)) {
            PlanBaselineDiffTone.Positive -> "diff-pos"
            PlanBaselineDiffTone.Negative -> "diff-neg"
            else -> ""
        }
        return if (cls.isBlank()) """<td class="td-num">${esc(text)}</td>"""
        else """<td class="td-num"><span class="$cls">${esc(text)}</span></td>"""
    }

    val bodyRows = items.joinToString("") { row ->
        val actualCell = if (row.currentActual != null) {
            """<td class="td-num">${esc(formatPlanComparisonValue(row.currentActual))}</td>"""
        } else {
            """<td class="td-num"><span class="diff-muted">-</span></td>"""
        }
        """
        <tr>
          <td class="td-date">${esc(PlanBaselineLogic.formatBaselineDate(row.planDate))}</td>
          <td class="td-num">${esc(formatPlanComparisonValue(row.baselinePlan))}</td>
          <td class="td-num">${esc(formatPlanComparisonValue(row.currentPlan))}</td>
          ${diffCell(row.planDiff)}
          $actualCell
          ${diffCell(row.actualDiff)}
        </tr>
        """.trimIndent()
    }

    val totals = PlanBaselineLogic.buildTabTotals(items)
    val baselinePlanTotal = items.sumOf { it.baselinePlan ?: 0.0 }
    val workingDays = scheduledWorkdays?.takeIf { it > 0 } ?: items.size
    val avgDaily = if (workingDays > 0) kotlin.math.round(baselinePlanTotal / workingDays).toInt() else 0
    val statsLine = if (workingDays > 0) {
        "稼働日数: ${workingDays}日（会社稼働カレンダー）　平均日当たり基準計画: ${formatProductionNumber(avgDaily.toDouble())}"
    } else ""
    val footer = """
      <tr class="row-total">
        <td class="td-date">${esc("合計")}</td>
        <td class="td-num">${esc(formatPlanComparisonValue(items.sumOf { it.baselinePlan ?: 0.0 }))}</td>
        <td class="td-num">${esc(formatPlanComparisonValue(totals.currentPlan))}</td>
        ${diffCell(totals.planDiff)}
        <td class="td-num">${esc(formatPlanComparisonValue(totals.currentActual.takeIf { items.any { i -> i.currentActual != null } }))}</td>
        ${diffCell(totals.actualDiff.takeIf { items.any { i -> i.actualDiff != null } })}
      </tr>
    """.trimIndent()

    return """
      <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>
      <title>ベースライン比較</title>
      <style>
        @page { margin: 12mm; }
        body { font-family: 'Segoe UI','Meiryo','Hiragino Sans',sans-serif; font-size: 11px; color: #111; }
        h1 { font-size: 15px; margin: 0 0 6px; font-weight: 700; }
        .meta { font-size: 10px; color: #333; margin: 0 0 10px; line-height: 1.5; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #cbd5e1; padding: 5px 6px; }
        th { background: #f1f5f9; font-weight: 700; text-align: center; }
        .td-num { text-align: right; font-family: Consolas, monospace; }
        .td-date { text-align: center; }
        .diff-pos { color: #059669; font-weight: 700; }
        .diff-neg { color: #dc2626; font-weight: 700; }
        .diff-muted { color: #94a3b8; }
        .row-total td { background: #f8fafc; font-weight: 700; }
      </style></head><body>
      <h1>ベースライン比較一覧</h1>
      <p class="meta">対象月: ${esc(monthLabel)} / 工程: ${esc(processName)}${if (statsLine.isNotBlank()) "<br/>${esc(statsLine)}" else ""}</p>
      <table>
        <thead><tr>
          <th>日付</th><th>基準計画</th><th>現行計画</th><th>計画差異</th><th>現行実績合計</th><th>計画対実績差</th>
        </tr></thead>
        <tbody>$bodyRows$footer</tbody>
      </table>
      </body></html>
    """.trimIndent()
}

fun buildBaselineOperationRatePrintHtml(monthLabel: String, processLabel: String, rows: List<PlanBaselineUtilizationRow>): String {
    fun esc(v: String) = v.replace("&", "&amp;").replace("<", "&lt;")
    val body = rows.joinToString("") { r ->
        """
        <tr>
          <td>${esc(r.lineLabel)}</td>
          <td class="num">${r.scheduleCount}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilHours(r.availableHours))}</td>
          <td class="num">${esc(formatProductionNumber(r.plannedQty))}</td>
          <td class="num">${esc(formatProductionNumber(r.actualQty))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilHours(r.plannedHours))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilHours(r.actualHours))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilPercent(r.planUtilizationPct))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilPercent(r.actualUtilizationPct))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilDiffHours(r.diffHours))}</td>
          <td class="num">${esc(PlanBaselineUtilizationLogic.formatUtilPercent(r.diffUtilizationPct))}</td>
        </tr>
        """.trimIndent()
    }
    return """
      <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/><title>操業度</title>
      <style>body{font-family:sans-serif;font-size:10px}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:4px}.num{text-align:right}</style>
      </head><body>
      <h1>操業度</h1><p>$monthLabel / $processLabel</p>
      <table><thead><tr>
        <th>設備</th><th>指示数</th><th>理論稼働(H)</th><th>計画数</th><th>実績数</th>
        <th>計画時間(H)</th><th>実績時間(H)</th><th>計画操業度</th><th>実績操業度</th><th>操業度差異(H)</th><th>差異操業度(%)</th>
      </tr></thead><tbody>$body</tbody></table></body></html>
    """.trimIndent()
}
