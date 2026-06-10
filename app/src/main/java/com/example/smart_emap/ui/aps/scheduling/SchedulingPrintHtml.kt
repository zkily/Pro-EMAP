package com.example.smart_emap.ui.aps.scheduling

private fun esc(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun cellBgClass(row: SchedulingMatrixRow, date: String, planExtendMode: Boolean): String {
    val cell = SchedulingMatrixLogic.buildCell(row, date, planExtendMode)
    val classes = mutableListOf<String>()
    when (cell.style.tone) {
        SchedulingCellTone.Actual -> classes += "sc-cell-actual"
        SchedulingCellTone.Active -> classes += "tone-active"
        SchedulingCellTone.High -> classes += "tone-high"
        SchedulingCellTone.Mid -> classes += "tone-mid"
        SchedulingCellTone.Low -> classes += "tone-low"
        SchedulingCellTone.Shortage -> classes += "tone-shortage"
        SchedulingCellTone.None -> Unit
    }
    if (cell.style.dueHighlight) classes += "cell-due"
    return classes.joinToString(" ")
}

private fun cellInlineStyle(row: SchedulingMatrixRow, date: String, planExtendMode: Boolean): String {
    val cell = SchedulingMatrixLogic.buildCell(row, date, planExtendMode)
    val style = cell.style
    style.backgroundColor?.let { return "background:${colorToCss(it)};" }
    val gradient = style.gradient
    val split = style.gradientSplit
    if (gradient != null && split != null) {
        val (red, green) = gradient
        val p1 = (split * 100f).coerceIn(0f, 100f)
        return "background:linear-gradient(to right, ${colorToCss(red)} 0%, ${colorToCss(red)} ${p1}%, ${colorToCss(green)} ${p1}%, ${colorToCss(green)} 100%);"
    }
    return ""
}

private fun colorToCss(color: androidx.compose.ui.graphics.Color): String {
    val a = (color.alpha * 255).toInt().coerceIn(0, 255)
    val r = (color.red * 255).toInt().coerceIn(0, 255)
    val g = (color.green * 255).toInt().coerceIn(0, 255)
    val b = (color.blue * 255).toInt().coerceIn(0, 255)
    return "rgba($r,$g,$b,${String.format("%.2f", color.alpha)})"
}

fun buildSchedulingPrintHtml(
    featureLabel: String,
    startDate: String,
    endDate: String,
    dates: List<String>,
    sections: List<SchedulingMatrixSection>,
    overallTotal: Int,
    dailyTotals: Map<String, Int>,
    planExtendMode: Boolean,
): String {
    val dateHeaders = dates.joinToString("") { date ->
        val weekend = if (SchedulingMatrixLogic.isWeekend(date)) " is-weekend" else ""
        val today = if (SchedulingMatrixLogic.isToday(date)) " is-today" else ""
        """
        <th class="date-col$weekend$today">
          <div class="date-header">
            <div class="date-text">${esc(SchedulingMatrixLogic.formatMatrixDate(date))}</div>
            <div class="weekday-text">${esc(SchedulingMatrixLogic.weekdayLabel(date))}</div>
          </div>
        </th>
        """.trimIndent()
    }

    val bodyRows = sections.joinToString("") { section ->
        section.rows.joinToString("") { row ->
            val rowClass = when (row) {
                is SchedulingMatrixRow.Group -> "sc-group-header-row"
                is SchedulingMatrixRow.Item -> buildString {
                    append("sc-item-row")
                    if (row.materialShortage) append(" sc-material-shortage-row")
                }
            }
            val lineText = if (row is SchedulingMatrixRow.Group) esc(row.lineName) else ""
            val orderText = if (row is SchedulingMatrixRow.Item) (row.orderNo?.toString() ?: "-") else ""
            val itemName = if (row is SchedulingMatrixRow.Item) esc(row.itemName) else ""
            val shortageFlag = if (row is SchedulingMatrixRow.Item && row.materialShortage) {
                """<div class="sc-flag">資材不足</div>"""
            } else {
                ""
            }
            val effText = when (row) {
                is SchedulingMatrixRow.Group -> SchedulingMatrixLogic.formatEfficiency(row.avgEfficiency)
                is SchedulingMatrixRow.Item -> SchedulingMatrixLogic.formatEfficiency(row.efficiencyRate)
            }
            val totalText = when (row) {
                is SchedulingMatrixRow.Group -> SchedulingMatrixLogic.formatQty(row.sumPlannedOutputQty)
                is SchedulingMatrixRow.Item -> SchedulingMatrixLogic.formatQty(row.plannedOutputQty)
            }
            val dateCells = dates.joinToString("") { date ->
                val cell = SchedulingMatrixLogic.buildCell(row, date, planExtendMode)
                val cls = cellBgClass(row, date, planExtendMode)
                val inline = cellInlineStyle(row, date, planExtendMode)
                val text = if (row is SchedulingMatrixRow.Item) esc(cell.displayText) else ""
                """<td class="numeric-cell data-cell ${esc(cls)}" style="${esc(inline)}">$text</td>"""
            }
            """
            <tr class="$rowClass">
              <td class="sc-sticky-col sc-line-col"><div class="sc-line-cell"><span class="sc-line-code">$lineText</span></div></td>
              <td class="sc-sticky-col sc-order-col numeric-cell">$orderText</td>
              <td class="sc-sticky-col sc-item-col"><div class="sc-item-cell"><div class="sc-item-name">$itemName</div>$shortageFlag</div></td>
              <td class="sc-sticky-col sc-eff-col numeric-cell">$effText</td>
              <td class="sc-sticky-col sc-total-col numeric-cell">$totalText</td>
              $dateCells
            </tr>
            """.trimIndent()
        }
    }

    val footerCells = dates.joinToString("") { date ->
        """<td class="numeric-cell">${esc(SchedulingMatrixLogic.formatQty(dailyTotals[date] ?: 0))}</td>"""
    }

    return """
<!doctype html>
<html>
<head>
<meta charset="utf-8"/>
<title>生産スケジューリングボード</title>
<style>
@page { size: A3 landscape; margin: 8mm; }
html, body { margin: 0; padding: 0; font-family: "Segoe UI", "Meiryo", sans-serif; }
* { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; box-sizing: border-box; }
.result-card { border: none; padding: 0; margin: 0; }
.result-head { display: flex; justify-content: space-between; margin-bottom: 8px; }
.result-title { font-size: 14px; font-weight: 800; color: #0f172a; }
.result-title-feature { color: #1d4ed8; }
.result-note { font-size: 11px; color: #64748b; background: #f1f5f9; border: 1px solid #e2e8f0; padding: 2px 8px; border-radius: 999px; }
.matrix-table-wrapper { overflow: visible; }
.matrix-table { width: max-content; min-width: 100%; border-collapse: collapse; font-size: 11px; }
.matrix-table th, .matrix-table td { border: 1px solid #e6edf5; padding: 3px 5px; white-space: nowrap; text-align: center; }
.matrix-table thead th { background: linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%); font-weight: 700; color: #42526a; }
.sc-line-col { width: 50px; }
.sc-order-col { width: 50px; }
.sc-item-col { width: 120px; text-align: left; }
.sc-eff-col { width: 66px; }
.sc-total-col { width: 90px; }
.date-col { min-width: 40px; }
.date-header { display: flex; flex-direction: column; align-items: center; line-height: 1.1; }
.date-text { font-size: 10px; font-weight: 650; }
.weekday-text { font-size: 9px; color: #6b7a90; }
.sc-group-header-row { font-weight: 700; border-top: 2px solid #bfdbfe; }
.sc-group-header-row .sc-line-code { font-weight: 800; color: #1e3a8a; }
.sc-item-name { max-width: 170px; overflow: hidden; text-overflow: ellipsis; }
.sc-flag { color: #dc2626; font-size: 9px; font-weight: 700; }
.sc-cell-actual { background: rgba(254, 249, 195, 0.88); }
.tone-active { background: rgba(187, 247, 208, 0.5); }
.tone-high { background: rgba(34, 197, 94, 0.22); }
.tone-mid { background: rgba(245, 158, 11, 0.18); }
.tone-low { background: rgba(239, 68, 68, 0.18); }
.tone-shortage { background: rgba(239, 68, 68, 0.26); }
.cell-due { outline: 1px solid #f59e0b; outline-offset: -2px; }
.is-weekend .date-text, .is-weekend .weekday-text { color: #dc2626; }
.is-today { background: linear-gradient(180deg, #fff3d4 0%, #ffeab0 100%); }
.sc-total-footer-row td { background: #f3f8ff; font-weight: 700; }
</style>
</head>
<body>
<div class="result-card">
  <div class="result-head">
    <div class="result-title">
      <span class="result-title-feature">${esc(featureLabel)}</span>
      <span>・</span>
      <span>スケジューリングマトリクス</span>
    </div>
    <div class="result-note">期間：${esc(startDate)} ~ ${esc(endDate)}</div>
  </div>
  <div class="matrix-table-wrapper">
    <table class="matrix-table">
      <thead>
        <tr>
          <th class="sc-line-col">ライン</th>
          <th class="sc-order-col">順位</th>
          <th class="sc-item-col">製品</th>
          <th class="sc-eff-col">能率(本/H)</th>
          <th class="sc-total-col">生産計画</th>
          $dateHeaders
        </tr>
      </thead>
      <tbody>
        $bodyRows
      </tbody>
      <tfoot>
        <tr class="sc-total-footer-row">
          <td>合計</td><td></td><td></td><td></td>
          <td>${esc(SchedulingMatrixLogic.formatQty(overallTotal))}</td>
          $footerCells
        </tr>
      </tfoot>
    </table>
  </div>
</div>
</body>
</html>
    """.trimIndent()
}
