package com.example.smart_emap.ui.erp.production.planning

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun escHtml(s: String): String = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun printStatusCell(status: PlanScheduleStatus): String {
    val dotClass = when (status) {
        PlanScheduleStatus.Done -> "print-status-dot print-status-dot--done"
        PlanScheduleStatus.Ongoing -> "print-status-dot print-status-dot--ongoing"
        PlanScheduleStatus.Pending -> "print-status-dot print-status-dot--pending"
    }
    return """<span class="$dotClass"></span>${escHtml(status.label)}"""
}

private fun ovFlagClass(kind: OperationVarianceProgressKind): String = when (kind) {
    OperationVarianceProgressKind.None -> "none"
    OperationVarianceProgressKind.Normal -> "normal"
    OperationVarianceProgressKind.Ahead -> "ahead"
    OperationVarianceProgressKind.Behind -> "behind"
    OperationVarianceProgressKind.OverPlan -> "over_plan"
    OperationVarianceProgressKind.SevereBehind -> "severe_behind"
}

private fun buildPrintFilterSummary(state: PlanScheduleUiState): String {
    val monthStr = "${state.filterMonth}月"
    val eng = state.filterEngineering.ifBlank { "すべて" }
    val mach = state.filterMachineName.ifBlank { "すべて" }
    val prod = state.filterProductName.ifBlank { "すべて" }
    return "月：${escHtml(monthStr)}　工程：${escHtml(eng)}　ライン：${escHtml(mach)}　品名：${escHtml(prod)}"
}

fun buildPlanSchedulePrintHtml(state: PlanScheduleUiState): String {
    val printDate = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy/M/d HH:mm", Locale.JAPAN),
    )
    val blocks = buildString {
        state.groupedSections.forEach { sec ->
            append("""<div class="print-section">""")
            val crit = PlanScheduleLogic.sectionCriticalProgressLines(state.varianceMap, sec)
            var critHtml = ""
            if (crit.overPlan.isNotEmpty()) {
                critHtml += """　<span class="print-sec-crit print-sec-crit--over">生産進捗（計画超過）${escHtml(crit.overPlan.joinToString("、"))}</span>"""
            }
            if (crit.severeBehind.isNotEmpty()) {
                critHtml += """　<span class="print-sec-crit print-sec-crit--severe">生産進捗（大幅な遅れ）${escHtml(crit.severeBehind.joinToString("、"))}</span>"""
            }
            append(
                """<div class="print-section-title">月：${escHtml(sec.monthLabel)}　工程：${escHtml(sec.engineering)}$critHtml</div>""",
            )
            sec.machines.forEach { mc ->
                append("""<div class="print-line-block">""")
                val ov = PlanScheduleLogic.machineOvHeadParts(state.varianceMap, sec.engineering, mc.machineName)
                val ovNeg = if (ov.negative) "print-ov-negative" else ""
                val ovPart = """操業度差異：<span class="$ovNeg">${escHtml(ov.display)}</span>"""
                val ppPart = """生産進捗：<span class="print-ov-flag print-ov-flag--${ovFlagClass(ov.kind)}">${escHtml(ov.progressLabel)}</span>"""
                append(
                    """<div class="print-machine-title">ライン：${escHtml(mc.machineName)}　$ovPart　$ppPart</div>""",
                )
                val rowHtml = mc.rows.joinToString("") { row ->
                    """
                    <tr>
                      <td>${escHtml((row.orderNo ?: row.id).toString())}</td>
                      <td class="text-left">${escHtml(row.itemName)}</td>
                      <td>${escHtml(PlanScheduleLogic.displayDate(row.startDate))}</td>
                      <td>${escHtml(PlanScheduleLogic.displayDate(row.endDate))}</td>
                      <td class="num">${escHtml(formatProductionNumber(row.plannedQty))}</td>
                      <td class="num">${escHtml(formatProductionNumber(row.actualQty))}</td>
                      <td class="num">${escHtml(formatProductionNumber(row.remainingQty))}</td>
                      <td class="num">${escHtml(row.progressPct)}</td>
                      <td class="status-print-cell">${printStatusCell(row.status)}</td>
                    </tr>
                    """.trimIndent()
                }
                append(
                    """
                    <table class="print-table">
                      <thead><tr>
                        <th>生産順</th><th>品名</th><th>開始日</th><th>終了日</th><th>計画数</th><th>実績</th><th>生産残数</th><th>進捗度</th><th>生産状況</th>
                      </tr></thead>
                      <tbody>$rowHtml</tbody>
                    </table>
                    """.trimIndent(),
                )
                append("</div>")
            }
            append("</div>")
        }
    }
    return """
        <!DOCTYPE html>
        <html lang="ja"><head><meta charset="UTF-8"/><title>生産スケジュール</title>
        <style>
          * { box-sizing: border-box; }
          html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          body { margin: 0; padding: 5.5mm 6.5mm; font-family: 'Segoe UI','Meiryo',sans-serif; font-size: 7.5pt; color: #0f172a; line-height: 1.32; background: #fff; }
          .print-header { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 1.5mm 5mm; margin-bottom: 2.8mm; padding-bottom: 2mm; border-bottom: 2px solid #2563eb; }
          .print-title { font-size: 11.5pt; font-weight: 800; margin: 0; }
          .print-meta { font-size: 6.8pt; color: #475569; text-align: right; max-width: 58%; }
          .print-section { margin-top: 2.6mm; }
          .print-section-title { font-size: 8pt; font-weight: 700; padding: 1.4mm 2.2mm; margin: 0 0 1.8mm 0; background: linear-gradient(90deg,#e8eefe,#f4f7ff 55%,#fafbfc); border: 1px solid #a5b4fc; border-radius: 2px; color: #1e3a8a; }
          .print-sec-crit { font-weight: 800; }
          .print-sec-crit--over { color: #b91c1c; }
          .print-sec-crit--severe { color: #7f1d1d; }
          .print-line-block { page-break-inside: avoid; margin: 0 0 2.2mm 0; padding: 1.2mm 2mm 1.5mm 2.4mm; background: #f8fafc; border-radius: 0 3px 3px 0; border: 1px solid #e2e8f0; border-left: 3px solid #3b82f6; }
          .print-machine-title { font-size: 7.3pt; font-weight: 700; margin: 0 0 0.8mm 0; color: #1d4ed8; }
          .print-ov-negative { color: #b91c1c; font-weight: 700; }
          .print-ov-flag { display: inline-block; padding: 0.35mm 1.4mm; border-radius: 1.2mm; font-size: 6.9pt; font-weight: 700; }
          .print-ov-flag--none { background: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1; }
          .print-ov-flag--normal { background: #dcfce7; color: #166534; border: 1px solid #86efac; }
          .print-ov-flag--ahead { background: #dbeafe; color: #1e40af; border: 1px solid #93c5fd; }
          .print-ov-flag--behind { background: #ffedd5; color: #9a3412; border: 1px solid #fdba74; }
          .print-ov-flag--over_plan, .print-ov-flag--severe_behind { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
          .print-table { width: 100%; border-collapse: collapse; font-size: 6.85pt; table-layout: fixed; border: 1px solid #94a3b8; }
          .print-table th, .print-table td { border: 1px solid #cbd5e1; padding: 0.55mm 0.75mm; vertical-align: middle; word-wrap: break-word; }
          .print-table th { background: #e2e8f0; font-weight: 700; color: #334155; font-size: 6.5pt; }
          .print-table tbody tr:nth-child(even) { background: #fff; }
          .print-table tbody tr:nth-child(odd) { background: #f9fafb; }
          .print-table .text-left { text-align: left; }
          .print-table .num { text-align: right; font-variant-numeric: tabular-nums; }
          .status-print-cell { text-align: center; white-space: nowrap; font-size: 6.7pt; }
          .print-status-dot { display: inline-block; width: 2.3mm; height: 2.3mm; border-radius: 50%; margin-right: 1.2mm; vertical-align: middle; }
          .print-status-dot--done { background: #64748b; }
          .print-status-dot--ongoing { background: #22c55e; }
          .print-status-dot--pending { background: #f59e0b; }
        </style></head><body>
        <div class="print-header">
          <div class="print-title">生産スケジュール</div>
          <div class="print-meta">
            <div>印刷日時：${escHtml(printDate)}　｜　行数：${state.displayRows.size}</div>
            <div>絞込：${buildPrintFilterSummary(state)}</div>
          </div>
        </div>
        $blocks
        </body></html>
    """.trimIndent()
}
