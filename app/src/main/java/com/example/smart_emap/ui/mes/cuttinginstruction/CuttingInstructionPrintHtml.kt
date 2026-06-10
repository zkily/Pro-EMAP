package com.example.smart_emap.ui.mes.cuttinginstruction



import com.example.smart_emap.data.model.InstructionChamferingRowDto

import com.example.smart_emap.data.model.InstructionCuttingRowDto

import com.example.smart_emap.data.model.KanbanIssuanceRowDto

import com.example.smart_emap.data.model.MaterialStockItemDto

import com.example.smart_emap.data.model.MaterialStockSubItemDto



private fun escapeHtml(s: String): String = s

    .replace("&", "&amp;")

    .replace("<", "&lt;")

    .replace(">", "&gt;")

    .replace("\"", "&quot;")



private fun formatStockDisplay(value: Number?): String {
    if (value == null || value.toDouble() == 0.0) return ""
    val d = value.toDouble()
    return if (d == d.toLong().toDouble()) d.toLong().toString() else String.format("%.1f", d)
}

private fun formatPrintTotalQty(total: Int): String =
    java.text.NumberFormat.getNumberInstance(java.util.Locale.JAPAN).format(total)

private fun instructionSheetBaseStyles(extraStyles: String = ""): String = """
  @page { size: 210mm 148mm landscape; margin: 10mm 0; }
  html, body { width: 210mm; margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'MS Gothic', 'Yu Gothic', sans-serif; font-size: 11px; padding: 8px 0.6cm; }
  .instruction-sheet-page { page-break-after: always; width: 100%; box-sizing: border-box; }
  .instruction-sheet-page:last-child { page-break-after: auto; }
  .instruction-sheet-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; padding: 6px 0; border-bottom: 1px solid #999; }
  .instruction-sheet-title { font-weight: bold; font-size: 22px; }
  .instruction-sheet-machine { font-size: 22px; }
  .instruction-sheet-date { font-size: 22px; }
  .instruction-sheet-table-wrap { overflow: auto; }
  .instruction-sheet-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
  .instruction-sheet-table th, .instruction-sheet-table td { border: 1px solid #999; padding: 3px 7px; text-align: center; line-height: 1.8; }
  .instruction-sheet-table th { background: #fff; font-weight: bold; font-size: 10px; }
  .instruction-sheet-table td { font-size: 14px; }
  .instruction-sheet-footer { margin-top: 12px; padding-top: 8px; display: flex; justify-content: flex-end; gap: 24px; font-weight: bold; }
  @media print { html, body { width: 210mm; } .instruction-sheet-page { overflow: hidden; } }
  $extraStyles
""".trimIndent()

private fun formingStartDateDisplay(raw: String?): String {
    val formatted = formatInstructionDate(raw)
    return if (formatted == "-") "" else formatted
}

private fun formingStartDatePrintClass(formingDay: String, today: String): String {
    if (formingDay.isBlank()) return ""
    if (formingDay == today) return "forming-date-red"
    if (formingDay == shiftInstructionDate(today, 1)) return "forming-date-light-red"
    return ""
}



fun buildCuttingPlanPrintHtml(

    productionDay: String,

    rows: List<InstructionCuttingRowDto>,

    stockList: List<MaterialStockItemDto>,

    stockSubList: List<MaterialStockSubItemDto>,

): String {

    val byMachine = rows.groupBy { it.cuttingMachine.orEmpty().trim().ifBlank { "（未設定）" } }

    val machineNames = byMachine.keys.sorted()

    val dayDisplay = productionDay.take(10).replace("-", "/")

    val leftBlocks = machineNames.joinToString("\n") { machineName ->

        val sorted = byMachine.getValue(machineName).sortedBy { it.productionSequence ?: 0 }

        val trs = sorted.joinToString("\n") { r ->

            """

            <tr>

              <td>${escapeHtml(r.cd ?: r.managementCode ?: "")}</td>

              <td>${escapeHtml(r.productionLine.orEmpty())}</td>

              <td>${escapeHtml(r.productName.orEmpty())}</td>

              <td>${escapeHtml(r.materialName.orEmpty())}</td>

              <td>${r.productionSequence ?: ""}</td>

              <td>${r.actualProductionQuantity ?: ""}</td>

              <td>${escapeHtml(r.remarks.orEmpty())}</td>

            </tr>

            """.trimIndent()

        }

        """

        <div class="print-cut-block">

          <div class="print-cut-block-title">${escapeHtml(machineName)}</div>

          <table class="print-cut-table"><thead><tr>

            <th>コード</th><th>ライン</th><th>製品名</th><th>原材料</th><th>順位</th><th>生産数</th><th>備考</th>

          </tr></thead><tbody>$trs</tbody></table>

        </div>

        """.trimIndent()

    }

    val stockRows = stockList.sortedWith(
        compareBy(
            { it.supplierName.orEmpty() },
            { it.materialName.orEmpty() },
        ),
    ).joinToString("\n") { r ->

        "<tr><td>${escapeHtml(r.supplierName.orEmpty())}</td><td>${escapeHtml(r.materialName.orEmpty())}</td><td>${formatStockDisplay(r.currentStock)}</td></tr>"

    }.ifBlank { "<tr><td colspan=\"3\">-</td></tr>" }

    val stockSubRows = stockSubList.filter { r ->

        val ob = r.orderBundleQuantity?.toDouble() ?: 0.0

        val oq = r.orderQuantity?.toDouble() ?: 0.0

        if (ob <= 0 && oq <= 0) return@filter false

        val pu = r.plannedUsage?.toDouble() ?: 0.0

        pu <= 0

    }.sortedWith(
        compareBy(
            { it.supplierName.orEmpty() },
            { it.materialName.orEmpty() },
        ),
    ).joinToString("\n") { r ->

        val qty = r.orderBundleQuantity?.toDouble() ?: r.orderQuantity?.toDouble() ?: 0.0

        "<tr><td>${escapeHtml(r.supplierName.orEmpty())}</td><td>${escapeHtml(r.materialName.orEmpty())}</td><td>${formatStockDisplay(qty)}</td></tr>"

    }.ifBlank { "<tr><td colspan=\"3\">-</td></tr>" }

    return """

<!DOCTYPE html><html><head><meta charset="UTF-8"><title>切断計画リスト</title><style>

  @page { size: 210mm 297mm portrait; margin: 8mm; }

  html, body { width: 210mm; margin: 0; padding: 0; box-sizing: border-box; }

  body { font-family: 'MS Gothic', 'Yu Gothic', sans-serif; font-size: 10px; padding: 6px; }

  .print-layout { display: flex; gap: 12px; width: 100%; min-height: 100vh; box-sizing: border-box; }

  .print-left { flex: 1; min-width: 0; }

  .print-right { width: 220px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px; }

  .print-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; padding-bottom: 6px; font-size: 14px; border-bottom: 1px solid #333; }

  .print-header-row .print-title { font-weight: bold; }

  .print-cut-block { margin-bottom: 10px; break-inside: avoid; }

  .print-cut-block-title { font-weight: bold; margin-bottom: 4px; font-size: 11px; }

  .print-cut-table { width: 100%; border-collapse: collapse; table-layout: fixed; }

  .print-cut-table th, .print-cut-table td { border: 1px solid #333; padding: 4px 2px; text-align: center; font-size: 11px; line-height: 1.2; }

  .print-cut-table th { background: #f0f0f0; }

  .print-cut-table th:nth-child(1), .print-cut-table td:nth-child(1) { width: 8%; }

  .print-cut-table th:nth-child(2), .print-cut-table td:nth-child(2) { width: 8%; }

  .print-cut-table th:nth-child(3), .print-cut-table td:nth-child(3) { width: 17%; }

  .print-cut-table th:nth-child(4), .print-cut-table td:nth-child(4) { width: 20%; }

  .print-cut-table th:nth-child(5), .print-cut-table td:nth-child(5) { width: 6%; }

  .print-cut-table th:nth-child(6), .print-cut-table td:nth-child(6) { width: 8%; }

  .print-cut-table th:nth-child(7), .print-cut-table td:nth-child(7) { width: 20%; }

  .print-cut-table td:nth-child(3), .print-cut-table td:nth-child(4), .print-cut-table td:nth-child(7) { text-align: left; }

  .print-stock-section { break-inside: avoid; }

  .print-stock-section h3 { margin: 0 0 6px; font-size: 11px; }

  .print-stock-table { width: 100%; border-collapse: collapse; font-size: 10px; table-layout: fixed; }

  .print-stock-table th, .print-stock-table td { border: 1px solid #333; padding: 2.25px 4px; line-height: 1.296; }

  .print-stock-table th { background: #f0f0f0; }

  .print-stock-table th:nth-child(1), .print-stock-table td:nth-child(1) { width: 40%; }

  .print-stock-table th:nth-child(2), .print-stock-table td:nth-child(2) { width: 46%; text-align: center; }

  .print-stock-table th:nth-child(3), .print-stock-table td:nth-child(3) { width: 16%; text-align: center; }

  @media print {
    html, body { width: 210mm; height: auto; }
    .print-layout { min-height: auto; }
  }

</style></head><body>

  <div class="print-header-row"><span class="print-title">切断計画リスト</span><span class="print-date">生産日 ${escapeHtml(dayDisplay)}</span></div>

  <div class="print-layout">

    <div class="print-left">$leftBlocks</div>

    <div class="print-right">

      <div class="print-stock-section">

        <h3>材料在庫</h3>

        <table class="print-stock-table"><thead><tr><th>仕入先</th><th>材料名</th><th>在庫</th></tr></thead><tbody>$stockRows</tbody></table>

      </div>

      <div class="print-stock-section">

        <h3>材料バラ在庫</h3>

        <table class="print-stock-table"><thead><tr><th>仕入先</th><th>材料名</th><th>在庫</th></tr></thead><tbody>$stockSubRows</tbody></table>

      </div>

    </div>

  </div>

</body></html>

    """.trimIndent()

}



fun buildCuttingInstructionSheetHtml(
    productionDay: String,
    rows: List<InstructionCuttingRowDto>,
): String {
    val byMachine = rows.groupBy { it.cuttingMachine.orEmpty().trim().ifBlank { "（未設定）" } }
    val dayDisplay = productionDay.take(10).replace("-", "/")
    val pages = byMachine.keys.sorted().joinToString("\n") { machine ->
        val sorted = byMachine.getValue(machine).sortedBy { it.productionSequence ?: 0 }
        var totalQty = 0
        val trs = sorted.joinToString("\n") { r ->
            val qty = r.actualProductionQuantity ?: 0
            totalQty += qty
            """
            <tr>
              <td>${escapeHtml(r.cd.orEmpty())}</td>
              <td>${escapeHtml(r.productName.orEmpty())}</td>
              <td>${r.productionSequence ?: ""}</td>
              <td>${escapeHtml(r.materialName.orEmpty())}</td>
              <td>${r.actualProductionQuantity ?: ""}</td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
              <td>${escapeHtml(r.remarks.orEmpty())}</td>
            </tr>
            """.trimIndent()
        }
        """
        <div class="instruction-sheet-page">
          <div class="instruction-sheet-header">
            <span class="instruction-sheet-title">切断生産指示書</span>
            <span class="instruction-sheet-machine">${escapeHtml(machine)}</span>
            <span class="instruction-sheet-date">生産日 $dayDisplay</span>
          </div>
          <div class="instruction-sheet-table-wrap">
            <table class="instruction-sheet-table">
              <thead><tr>
                <th>CD</th><th>製品名</th><th>順位</th><th>原材料</th><th>生産数</th><th>実績数</th><th>不良</th><th>段取</th><th>開始</th><th>終了</th><th>作業者</th><th>備考</th>
              </tr></thead>
              <tbody>$trs</tbody>
            </table>
          </div>
          <div class="instruction-sheet-footer">
            <span>合計 ${formatPrintTotalQty(totalQty)}</span>
          </div>
        </div>
        """.trimIndent()
    }
    val styles = instructionSheetBaseStyles(
        """
        .instruction-sheet-table th:nth-child(1), .instruction-sheet-table td:nth-child(1) { width: 4%; }
        .instruction-sheet-table th:nth-child(2), .instruction-sheet-table td:nth-child(2) { width: 15%; }
        .instruction-sheet-table th:nth-child(3), .instruction-sheet-table td:nth-child(3) { width: 4%; }
        .instruction-sheet-table th:nth-child(4), .instruction-sheet-table td:nth-child(4) { width: 15%; }
        .instruction-sheet-table th:nth-child(5), .instruction-sheet-table td:nth-child(5) { width: 5%; }
        .instruction-sheet-table th:nth-child(6), .instruction-sheet-table td:nth-child(6) { width: 5%; }
        .instruction-sheet-table th:nth-child(7), .instruction-sheet-table td:nth-child(7) { width: 4%; }
        .instruction-sheet-table th:nth-child(8), .instruction-sheet-table td:nth-child(8) { width: 4%; }
        .instruction-sheet-table th:nth-child(9), .instruction-sheet-table td:nth-child(9) { width: 4%; }
        .instruction-sheet-table th:nth-child(10), .instruction-sheet-table td:nth-child(10) { width: 4%; }
        .instruction-sheet-table th:nth-child(11), .instruction-sheet-table td:nth-child(11) { width: 5%; }
        .instruction-sheet-table th:nth-child(12), .instruction-sheet-table td:nth-child(12) { width: 10%; }
        .instruction-sheet-table th { font-size: 10px; }
        .instruction-sheet-table td:nth-child(1), .instruction-sheet-table td:nth-child(12) { font-size: 10px; }
        .instruction-sheet-table th:nth-child(12), .instruction-sheet-table td:nth-child(12) { color: #d00; }
        .instruction-sheet-table td:nth-child(2), .instruction-sheet-table td:nth-child(4), .instruction-sheet-table td:nth-child(12) { text-align: left; }
        """.trimIndent(),
    )
    return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>切断生産指示書</title><style>$styles</style></head><body>$pages</body></html>
    """.trimIndent()
}



fun buildChamferingPlanPrintHtml(
    productionDay: String,
    rows: List<InstructionChamferingRowDto>,
    machineNames: List<String>,
): String {
    val dayDisplay = productionDay.take(10).replace("-", "/")
    val byMachine = rows
        .groupBy { it.chamferingMachine.orEmpty().trim().ifBlank { "（未設定）" } }
        .mapValues { (_, list) -> list.sortedBy { it.productionSequence ?: 0 } }
    val sortedMachineNames = machineNames.map { it.trim().ifBlank { "（未設定）" } }.distinct().sorted()
    val blocks = sortedMachineNames.joinToString("\n") { machineName ->
        val list = byMachine[machineName].orEmpty()
        val trs = if (list.isEmpty()) {
            """<tr><td colspan="7" class="print-chamfer-empty">該当日のデータがありません</td></tr>"""
        } else {
            list.joinToString("\n") { r ->
                val noCountDisplay = if (r.noCount == 1) "Yes" else "No"
                """
                <tr>
                  <td>${escapeHtml(r.cd ?: r.managementCode ?: "")}</td>
                  <td>${escapeHtml(r.productName.orEmpty())}</td>
                  <td>${r.productionSequence ?: ""}</td>
                  <td>${r.actualProductionQuantity ?: ""}</td>
                  <td>$noCountDisplay</td>
                  <td>${r.productionTime ?: ""}</td>
                  <td>${escapeHtml(r.productionLine.orEmpty())}</td>
                </tr>
                """.trimIndent()
            }
        }
        """
        <div class="print-chamfer-block">
          <div class="print-chamfer-block-title">${escapeHtml(machineName)}</div>
          <table class="print-chamfer-table"><thead><tr>
            <th>コード</th><th>製品名</th><th>順位</th><th>生産数</th><th>カ無</th><th>時間</th><th>ライン</th>
          </tr></thead><tbody>$trs</tbody></table>
        </div>
        """.trimIndent()
    }
    return """
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>面取計画リスト</title><style>
  @page { size: 210mm 297mm portrait; margin: 8mm; }
  html, body { width: 210mm; margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'MS Gothic', 'Yu Gothic', sans-serif; font-size: 10px; padding: 6px; }
  .print-chamfer-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; padding-bottom: 6px; font-size: 14px; border-bottom: 1px solid #333; }
  .print-chamfer-header .print-title { font-weight: bold; }
  .print-chamfer-body { display: flex; flex-wrap: wrap; gap: 12px 16px; width: 100%; box-sizing: border-box; }
  .print-chamfer-block { width: calc(50% - 8px); min-width: 280px; break-inside: avoid; margin-bottom: 4px; }
  .print-chamfer-block-title { font-weight: bold; margin-bottom: 4px; font-size: 11px; }
  .print-chamfer-table { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 10px; }
  .print-chamfer-table th, .print-chamfer-table td { border: 1px solid #333; padding: 4px 2px; text-align: center; line-height: 1.2; }
  .print-chamfer-table th { background: #f0f0f0; font-size: 11px; }
  .print-chamfer-table th:nth-child(1), .print-chamfer-table td:nth-child(1) { width: 11%; }
  .print-chamfer-table th:nth-child(2), .print-chamfer-table td:nth-child(2) { width: 20%; }
  .print-chamfer-table th:nth-child(3), .print-chamfer-table td:nth-child(3) { width: 8%; }
  .print-chamfer-table th:nth-child(4), .print-chamfer-table td:nth-child(4) { width: 11%; }
  .print-chamfer-table th:nth-child(5), .print-chamfer-table td:nth-child(5) { width: 8%; }
  .print-chamfer-table th:nth-child(6), .print-chamfer-table td:nth-child(6) { width: 8%; }
  .print-chamfer-table th:nth-child(7), .print-chamfer-table td:nth-child(7) { width: 16%; text-align: center; }
  .print-chamfer-table td:nth-child(2) { text-align: left; }
  .print-chamfer-table td:nth-child(7) { text-align: left; }
  .print-chamfer-empty { color: #666; padding: 8px 4px !important; }
  @media print {
    html, body { width: 210mm; height: auto; }
    .print-chamfer-block { break-inside: avoid; }
  }
</style></head><body>
  <div class="print-chamfer-header"><span class="print-title">面取計画リスト</span><span class="print-date">生産日 ${escapeHtml(dayDisplay)}</span></div>
  <div class="print-chamfer-body">$blocks</div>
</body></html>
    """.trimIndent()
}



fun buildChamferingInstructionSheetHtml(
    productionDay: String,
    rows: List<InstructionChamferingRowDto>,
    formingStartDateByMgmtCode: Map<String, String> = emptyMap(),
    today: String = instructionToday(),
): String {
    val byMachine = rows.groupBy { it.chamferingMachine.orEmpty().trim().ifBlank { "（未設定）" } }
    val dayDisplay = productionDay.take(10).replace("-", "/")
    val pages = byMachine.keys.sorted().joinToString("\n") { machine ->
        val sorted = byMachine.getValue(machine).sortedBy { it.productionSequence ?: 0 }
        var totalQty = 0
        val trs = sorted.joinToString("\n") { r ->
            val qty = r.actualProductionQuantity ?: 0
            totalQty += qty
            val mgmtCode = r.managementCode.orEmpty().trim()
            val formingStartDisplay = formingStartDateDisplay(formingStartDateByMgmtCode[mgmtCode])
            val formingDateClass = formingStartDatePrintClass(formingStartDisplay, today)
            val noCountDisplay = if (r.noCount == 1) "あり" else "--"
            """
            <tr>
              <td>${escapeHtml(r.cd ?: r.managementCode ?: "")}</td>
              <td>${escapeHtml(r.productionLine.orEmpty())}</td>
              <td class="$formingDateClass">${escapeHtml(formingStartDisplay)}</td>
              <td>${escapeHtml(r.productName.orEmpty())}</td>
              <td>${r.productionSequence ?: ""}</td>
              <td>${r.actualProductionQuantity ?: ""}</td>
              <td></td>
              <td>$noCountDisplay</td>
              <td></td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            """.trimIndent()
        }
        """
        <div class="instruction-sheet-page">
          <div class="instruction-sheet-header">
            <span class="instruction-sheet-title">面取生産指示書</span>
            <span class="instruction-sheet-machine">${escapeHtml(machine)}</span>
            <span class="instruction-sheet-date">生産日 $dayDisplay</span>
          </div>
          <div class="instruction-sheet-table-wrap">
            <table class="instruction-sheet-table chamfering-sheet-table">
              <thead><tr>
                <th>CD</th><th>ライン</th><th>成型予定日</th><th>製品名</th><th>順位</th><th>計画</th><th>実績</th><th>カ無</th><th>運転時間</th><th>停止時間</th><th>1直</th><th>2直</th>
              </tr></thead>
              <tbody>$trs</tbody>
            </table>
          </div>
          <div class="instruction-sheet-footer">
            <span>合計 ${formatPrintTotalQty(totalQty)}</span>
          </div>
        </div>
        """.trimIndent()
    }
    val styles = instructionSheetBaseStyles(
        """
        .chamfering-sheet-table th:nth-child(1), .chamfering-sheet-table td:nth-child(1) { width: 6%; }
        .chamfering-sheet-table th:nth-child(2), .chamfering-sheet-table td:nth-child(2) { width: 7%; }
        .chamfering-sheet-table th:nth-child(3), .chamfering-sheet-table td:nth-child(3) { width: 12%; }
        .chamfering-sheet-table th:nth-child(4), .chamfering-sheet-table td:nth-child(4) { width: 12%; }
        .chamfering-sheet-table th:nth-child(5), .chamfering-sheet-table td:nth-child(5) { width: 5%; }
        .chamfering-sheet-table th:nth-child(6), .chamfering-sheet-table td:nth-child(6) { width: 6%; }
        .chamfering-sheet-table th:nth-child(7), .chamfering-sheet-table td:nth-child(7) { width: 6%; }
        .chamfering-sheet-table th:nth-child(8), .chamfering-sheet-table td:nth-child(8) { width: 5%; }
        .chamfering-sheet-table th:nth-child(9), .chamfering-sheet-table td:nth-child(9) { width: 8%; }
        .chamfering-sheet-table th:nth-child(10), .chamfering-sheet-table td:nth-child(10) { width: 8%; }
        .chamfering-sheet-table th:nth-child(11), .chamfering-sheet-table td:nth-child(11) { width: 5%; }
        .chamfering-sheet-table th:nth-child(12), .chamfering-sheet-table td:nth-child(12) { width: 5%; }
        .instruction-sheet-table th { font-size: 11px; }
        .chamfering-sheet-table td:nth-child(1), .chamfering-sheet-table td:nth-child(2), .chamfering-sheet-table td:nth-child(4) { font-size: 14px; text-align: left; }
        .forming-date-red { color: #990000; }
        .forming-date-light-red { color: #cc0000; }
        """.trimIndent(),
    )
    return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>面取生産指示書</title><style>$styles</style></head><body>$pages</body></html>
    """.trimIndent()
}



private fun formatKanbanPrintDate(value: String?): String =
    value?.trim()?.take(10)?.replace("-", "/").orEmpty()

private fun todayKanbanPrintDate(): String =
    java.time.LocalDate.now().toString().replace("-", "/")

private fun encodeQrData(value: String): String =
    java.net.URLEncoder.encode(value, Charsets.UTF_8.name())

private const val KANBAN_TICKET_PRINT_STYLES = """
    @page { size: A4 portrait; margin: 8mm; }
    * { box-sizing: border-box; }
    html, body { margin: 0; padding: 0; width: 100%; font-family: 'Yu Gothic','MS Gothic',sans-serif; font-size: 10px; }
    .page { width: 194mm; height: 281mm; margin: 0; padding: 0; page-break-after: always; }
    .page:last-child { page-break-after: auto; }
    .ticket-sheet { width: 100%; height: 281mm; display: flex; flex-direction: column; margin: 0; padding: 0; position: relative; }
    .ticket-block { flex: 0 0 calc(281mm / 3); height: calc(281mm / 3); display: flex; flex-direction: column; padding: 4mm 6mm; margin: 0; overflow: hidden; }
    .ticket-sheet .ticket-block:nth-of-type(2) { margin-bottom: 15px; }
    .ticket-block .ticket { flex: 1; display: flex; flex-direction: column; min-height: 0; }
    .cut-line { position: absolute; left: 0; right: 0; height: 0; border: none; border-top: 2px dotted #999; pointer-events: none; }
    .cut-line-1 { top: calc(281mm / 3 - 15px); }
    .cut-line-2 { top: calc(281mm * 2 / 3 + 4px); }
    .ticket-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 3px; border: 1px solid #333; padding: 4px; }
    .ticket-title { font-size: 16px; font-weight: bold; }
    .ticket-product { font-size: 42px; font-weight: bold; text-align: center; flex: 1; }
    .ticket-top-right { display: flex; flex-direction: column; align-items: flex-end; min-width: 80px; }
    .ticket-machine { font-size: 20px; font-weight: bold; color: #cc0000; text-align: right; }
    .ticket-mgmt-qr-wrap { display: flex; flex-direction: column; align-items: center; margin-top: 4px; }
    .ticket-mgmt-qr { width: 30px; height: 30px; border: 1px solid #999; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .ticket-mgmt-qr img { display: block; width: 32px; height: 32px; object-fit: contain; }
    .ticket-mgmt-qr-label { font-size: 6px; color: #666; margin-top: 0px; }
    .ticket-qr-wrap { display: flex; flex-direction: column; align-items: flex-start; margin-top: 10px; margin-bottom: 1px; }
    .ticket-qr { width: 30px; height: 30px; border: 1px solid #999; display: flex; align-items: center; justify-content: center; font-size: 6px; color: #aaa; }
    .ticket-qr-label { font-size: 6px; color: #666; margin-top: 0px; }
    .tbl { width: 100%; border-collapse: collapse; font-size: 14px; table-layout: fixed; }
    .tbl th, .tbl td { border: 1px solid #333; padding: 2.4px 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; line-height: 1.6; }
    .tbl th { background: transparent; font-weight: bold; text-align: left; }
    .tbl th.tbl-th-normal { font-weight: normal; }
    .tbl .tbl-c { text-align: center; }
    .tbl th:nth-child(1), .tbl td:nth-child(1) { width: 60px; }
    .tbl th:nth-child(2), .tbl td:nth-child(2) { width: 110px; }
    .tbl th:nth-child(3), .tbl td:nth-child(3) { width: 60px; }
    .tbl th:nth-child(4), .tbl td:nth-child(4) { width: 150px; }
    .tbl th:nth-child(5), .tbl td:nth-child(5) { width: 70px; }
    .tbl th:nth-child(6), .tbl td:nth-child(6) { width: 95px; }
    .tbl th:nth-child(7), .tbl td:nth-child(7) { width: 85px; }
    .tbl th:nth-child(8), .tbl td:nth-child(8) { width: 95px; }
    .tbl .tbl-row-lotno th, .tbl .tbl-row-lotno td { line-height: 3.05; }
    .tbl .tbl-lotno-val { font-size: 14px; line-height: 1.1; }
    .red { color: #cc0000; font-weight: bold; font-size: 12px; }
    .big { font-size: 12px; font-weight: bold; }
    .tbl-developed-val {
      background-color: #555 !important;
      color: #ffffff !important;
      font-weight: bold !important;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
    .kanban-no { font-size: 8px; color: #666; text-align: right; margin-top: 2px; }
    @media print {
      html, body { margin: 0 !important; padding: 0 !important; }
      .page { width: 194mm !important; height: 281mm !important; margin: 0 !important; padding: 0 !important; page-break-after: always !important; }
      .page:last-child { page-break-after: auto !important; }
      .ticket-sheet { height: 281mm !important; }
      .ticket-block { flex: 0 0 calc(281mm / 3) !important; height: calc(281mm / 3) !important; }
      .tbl-developed-val {
        background-color: #555 !important;
        color: #ffffff !important;
        font-weight: bold !important;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
      }
    }
"""

private fun buildOneKanbanTicketHtml(row: KanbanIssuanceRowDto, kanbanNo: String): String {
    val lotQty = row.actualProductionQuantity?.toString().orEmpty()
    val lineDisplay = escapeHtml(row.productionLine.orEmpty())
    val cuttingMachineDisplay = escapeHtml(row.cuttingMachine.orEmpty())
    val lineShort = escapeHtml(row.productionLine.orEmpty().replace("号機", ""))
    val chamferDisplay = if (row.hasChamferingProcess == true) "有り" else "--"
    val chamferingLengthDisplay = if (row.chamferingLength != null && row.chamferingLength != 0.0) {
        escapeHtml(row.chamferingLength.toString())
    } else {
        "--"
    }
    val hasDevelopedLength = row.developedLength != null && row.developedLength != 0.0
    val developedLengthDisplay = if (hasDevelopedLength) escapeHtml(row.developedLength.toString()) else "--"
    val lotNoFromMgmt = escapeHtml(row.managementCode.orEmpty().takeLast(5))
    val productCd = row.productCd.orEmpty()
    val qrSrc = if (productCd.isNotBlank()) {
        "https://api.qrserver.com/v1/create-qr-code/?size=80x80&data=${encodeQrData(productCd)}"
    } else {
        ""
    }
    val managementCode = row.managementCode.orEmpty().trim()
    val mgmtQrSrc = if (managementCode.isNotBlank()) {
        "https://api.qrserver.com/v1/create-qr-code/?size=80x80&data=${encodeQrData(managementCode)}"
    } else {
        ""
    }
    val developedClass = if (hasDevelopedLength) " tbl-developed-val" else ""
    return """
    <div class="ticket-top">
      <div>
        <div class="ticket-title">切断現品票</div>
        <div class="ticket-qr-wrap">
          <div class="ticket-qr">${if (qrSrc.isNotBlank()) """<img src="$qrSrc" alt="QR" width="32" height="32" />""" else "<span>QR</span>"}</div>
          <span class="ticket-qr-label">製品CD</span>
        </div>
      </div>
      <div class="ticket-product">${escapeHtml(row.productName.orEmpty())}</div>
      <div class="ticket-top-right">
        <div class="ticket-machine">$lineDisplay</div>
        <div class="ticket-mgmt-qr-wrap">
          <div class="ticket-mgmt-qr">${if (mgmtQrSrc.isNotBlank()) """<img src="$mgmtQrSrc" alt="管理コードQR" width="32" height="32" />""" else ""}</div>
          <span class="ticket-mgmt-qr-label">管理コード</span>
        </div>
      </div>
    </div>
    <table class="tbl">
      <tr>
        <th>規格</th>
        <td colspan="3">${escapeHtml(row.standardSpecification.orEmpty())}</td>
        <th>管理コード</th>
        <td colspan="3">${escapeHtml(row.managementCode.orEmpty())}</td>
      </tr>
      <tr>
        <th>原材料</th>
        <td colspan="3">${escapeHtml(row.materialName.orEmpty())}</td>
        <th>成型予定期間</th>
        <td colspan="1">${formatKanbanPrintDate(row.startDate)}</td>
        <th class="tbl-c">～</th>
        <td colspan="1">${formatKanbanPrintDate(row.endDate)}</td>
      </tr>
      <tr>
        <th>製造番号</th>
        <td colspan="3"></td>
        <th class="tbl-th-normal">成型計画数</th>
        <td class="big tbl-c">${escapeHtml(row.plannedQuantity?.toString().orEmpty())}</td>
        <th class="tbl-th-normal">ロットNo.</th>
        <td class="big tbl-c">${escapeHtml(row.productionLotSize?.toString().orEmpty())}</td>
      </tr>
      <tr>
        <th>ロット本数</th>
        <td class="red tbl-c">${escapeHtml(lotQty)}</td>
        <th>取数</th>
        <td class="big tbl-c">${escapeHtml(row.takeCount?.toString().orEmpty())}</td>
        <th>工程</th>
        <td class="tbl-c">切断工程</td>
        <td class="tbl-c">面取工程</td>
        <th class="tbl-c tbl-th-normal">成型ライン</th>
      </tr>
      <tr>
        <th>切断長</th>
        <td class="big tbl-c">${escapeHtml(row.cuttingLength?.toString().orEmpty())}</td>
        <th>面取長</th>
        <td class="tbl-c">$chamferingLengthDisplay</td>
        <th>設備No.</th>
        <td class="tbl-c">$cuttingMachineDisplay</td>
        <td></td>
        <td class="tbl-c">$lineShort</td>
      </tr>
      <tr>
        <th>展開長</th>
        <td class="tbl-c$developedClass">$developedLengthDisplay</td>
        <th>面取</th>
        <td class="tbl-c">$chamferDisplay</td>
        <th>実績数</th>
        <td></td>
        <td></td>
        <td></td>
      </tr>
      <tr class="tbl-row-lotno">
        <th>ロットNo</th>
        <td colspan="3" class="big tbl-c tbl-lotno-val">$lotNoFromMgmt</td>
        <th>確認</th>
        <td></td>
        <td></td>
        <td></td>
      </tr>
    </table>
    <div class="kanban-no">カンバン番号: ${escapeHtml(kanbanNo)}　発行日: ${todayKanbanPrintDate()}</div>
    """.trimIndent()
}

private fun buildKanbanTicketSheetHtml(oneTicket: String): String = """
    <div class="page">
      <div class="ticket-sheet">
        <div class="cut-line cut-line-1"></div>
        <div class="cut-line cut-line-2"></div>
        <div class="ticket-block"><div class="ticket">$oneTicket</div></div>
        <div class="ticket-block"><div class="ticket">$oneTicket</div></div>
        <div class="ticket-block"><div class="ticket">$oneTicket</div></div>
      </div>
    </div>
""".trimIndent()

fun buildKanbanTicketPrintHtml(row: KanbanIssuanceRowDto, kanbanNo: String): String {
    val oneTicket = buildOneKanbanTicketHtml(row, kanbanNo)
    val sheet = buildKanbanTicketSheetHtml(oneTicket)
    return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>切断現品票</title><style>$KANBAN_TICKET_PRINT_STYLES</style></head><body>$sheet</body></html>
    """.trimIndent()
}

private val kanbanPrintSort = compareBy<Pair<KanbanIssuanceRowDto, String>> { it.first.productionDay.orEmpty() }
    .thenBy { it.first.cuttingMachine.orEmpty() }
    .thenBy { it.first.sourceId ?: it.first.id ?: 0 }

fun buildKanbanBatchPrintHtml(items: List<Pair<KanbanIssuanceRowDto, String>>): String {
    val pagesHtml = items
        .sortedWith(kanbanPrintSort)
        .joinToString("\n") { (row, kanbanNo) ->
            buildKanbanTicketSheetHtml(buildOneKanbanTicketHtml(row, kanbanNo))
        }
    return """
        <!DOCTYPE html><html><head><meta charset="UTF-8"><title>切断現品票（${items.size}枚）</title><style>$KANBAN_TICKET_PRINT_STYLES</style></head><body>$pagesHtml</body></html>
    """.trimIndent()
}



fun buildMoldingPreInventoryPrintHtml(date: String, groups: List<MoldingPreInventoryGroup>): String {

    val rows = groups.joinToString("\n") { g ->

        val invLow = if (g.totalProductionHours in 0.0..<35.0) " low-h" else ""

        val postLow = if (g.totalPostRefCuttingHours in 0.0..<35.0) " low-h" else ""

        """

        <tr>

          <td class="col-machine">${escapeHtml(g.moldingMachine)}</td>

          <td class="col-num$invLow">${g.totalProductionHours}</td>

          <td class="col-num col-post$postLow">${g.totalPostRefCuttingHours}</td>

        </tr>

        """.trimIndent()

    }

    val grand = groups.fold(MoldingPreInventoryGrand(0, 0.0, 0.0)) { acc, g ->

        acc.copy(

            machineCount = acc.machineCount + 1,

            totalProductionHours = acc.totalProductionHours + g.totalProductionHours,

            totalPostRefCuttingHours = acc.totalPostRefCuttingHours + g.totalPostRefCuttingHours,

        )

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/><title>成型前在庫</title>

        <style>body{font-family:Meiryo,sans-serif;font-size:11px;margin:12px}

        h1{font-size:14px;color:#5b21b6}table{border-collapse:collapse;width:100%;max-width:520px}

        th,td{border:1px solid #e2e8f0;padding:4px 8px}.col-num{text-align:right;font-weight:bold}

        .low-h{color:#dc2626}tfoot td{background:#ede9fe;font-weight:800}</style></head><body>

        <h1>成型前在庫・時間換算 — 設備集計</h1>

        <p>参照日 $date</p>

        <table><thead><tr><th>成型設備</th><th>在庫(H)</th><th>参照後(H)</th></tr></thead>

        <tbody>$rows</tbody>

        <tfoot><tr><td>合計</td><td class="col-num">${grand.totalProductionHours}</td>

        <td class="col-num">${grand.totalPostRefCuttingHours}</td></tr></tfoot></table>

        </body></html>

    """.trimIndent()

}



data class MoldingPreInventoryGroup(

    val moldingMachine: String,

    val totalProductionHours: Double,

    val totalPostRefCuttingHours: Double,

)



data class MoldingPreInventoryGrand(

    val machineCount: Int,

    val totalProductionHours: Double,

    val totalPostRefCuttingHours: Double,

)


