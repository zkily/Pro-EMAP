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

    if (value == null) return "-"

    val d = value.toDouble()

    return if (d == d.toLong().toDouble()) d.toLong().toString() else String.format("%.1f", d)

}



fun buildCuttingPlanPrintHtml(

    productionDay: String,

    rows: List<InstructionCuttingRowDto>,

    stockList: List<MaterialStockItemDto>,

    stockSubList: List<MaterialStockSubItemDto>,

): String {

    val byMachine = rows.groupBy { it.cuttingMachine.orEmpty().ifBlank { "（未設定）" } }

        .toSortedMap()

    val dayDisplay = productionDay.replace("-", "/")

    val leftBlocks = byMachine.entries.joinToString("\n") { (machine, list) ->

        val sorted = list.sortedBy { it.productionSequence ?: 0 }

        val trs = sorted.joinToString("\n") { r ->

            """

            <tr>

              <td>${escapeHtml(r.cd.orEmpty())}</td>

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

          <div class="print-cut-block-title">${escapeHtml(machine)}</div>

          <table class="print-cut-table"><thead><tr>

            <th>コード</th><th>ライン</th><th>製品名</th><th>原材料</th><th>順位</th><th>生産数</th><th>備考</th>

          </tr></thead><tbody>$trs</tbody></table>

        </div>

        """.trimIndent()

    }

    val stockRows = stockList.sortedWith(
        compareBy(
            { it.supplierName.orEmpty().lowercase() },
            { it.materialName.orEmpty().lowercase() },
        ),
    ).joinToString("\n") { r ->

        "<tr><td>${escapeHtml(r.supplierName.orEmpty())}</td><td>${escapeHtml(r.materialName.orEmpty())}</td><td>${formatStockDisplay(r.currentStock)}</td></tr>"

    }

    val stockSubRows = stockSubList.filter { r ->

        val ob = r.orderBundleQuantity?.toDouble() ?: 0.0

        val oq = r.orderQuantity?.toDouble() ?: 0.0

        if (ob <= 0 && oq <= 0) return@filter false

        val pu = r.plannedUsage?.toDouble() ?: 0.0

        pu <= 0

    }.sortedWith(
        compareBy(
            { it.supplierName.orEmpty().lowercase() },
            { it.materialName.orEmpty().lowercase() },
        ),
    ).joinToString("\n") { r ->

        "<tr><td>${escapeHtml(r.supplierName.orEmpty())}</td><td>${escapeHtml(r.materialName.orEmpty())}</td><td>${formatStockDisplay(r.currentStock)}</td></tr>"

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>

        <title>切断計画 $dayDisplay</title>

        <style>

          body{font-family:Meiryo,sans-serif;font-size:10px;margin:8px;color:#1e293b}

          h1{font-size:14px;color:#3730a3;margin:0 0 8px}

          .print-cut-block{margin-bottom:10px;page-break-inside:avoid}

          .print-cut-block-title{font-weight:bold;background:#eef2ff;padding:4px 8px;border:1px solid #c7d2fe}

          table{border-collapse:collapse;width:100%;margin-top:4px}

          th,td{border:1px solid #e2e8f0;padding:3px 5px}

          th{background:#f8fafc;font-size:9px}

          .stock-section{margin-top:12px}

          .stock-section h2{font-size:11px;color:#4338ca;margin:0 0 4px}

        </style></head><body>

        <h1>切断計画リスト — $dayDisplay</h1>

        $leftBlocks

        <div class="stock-section"><h2>材料在庫</h2>

        <table><thead><tr><th>仕入先</th><th>材料名</th><th>在庫</th></tr></thead><tbody>$stockRows</tbody></table></div>

        <div class="stock-section"><h2>半端材料（バラ在庫）</h2>

        <table><thead><tr><th>仕入先</th><th>材料名</th><th>在庫</th></tr></thead><tbody>$stockSubRows</tbody></table></div>

        </body></html>

    """.trimIndent()

}



fun buildCuttingInstructionSheetHtml(

    productionDay: String,

    rows: List<InstructionCuttingRowDto>,

): String {

    val byMachine = rows.groupBy { it.cuttingMachine.orEmpty().ifBlank { "（未設定）" } }

    val pages = byMachine.entries.joinToString("<div style='page-break-after:always'></div>") { (machine, list) ->

        val sorted = list.sortedBy { it.productionSequence ?: 0 }

        val trs = sorted.joinToString("\n") { r ->

            """

            <tr>

              <td>${r.productionSequence ?: ""}</td>

              <td>${escapeHtml(r.cd.orEmpty())}</td>

              <td>${escapeHtml(r.productName.orEmpty())}</td>

              <td>${escapeHtml(r.materialName.orEmpty())}</td>

              <td>${r.actualProductionQuantity ?: ""}</td>

              <td>${escapeHtml(r.remarks.orEmpty())}</td>

            </tr>

            """.trimIndent()

        }

        """

        <div class="sheet">

          <h1>切断指示書</h1>

          <p>生産日: ${productionDay.replace("-", "/")} / 切断機: ${escapeHtml(machine)}</p>

          <table><thead><tr><th>順</th><th>CD</th><th>製品名</th><th>材料</th><th>生産数</th><th>備考</th></tr></thead>

          <tbody>$trs</tbody></table>

        </div>

        """.trimIndent()

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>

        <title>切断指示書</title>

        <style>

          @page{size:A5 landscape;margin:8mm}

          body{font-family:Meiryo,sans-serif;font-size:11px}

          h1{font-size:16px;color:#3730a3;margin:0}

          table{border-collapse:collapse;width:100%;margin-top:8px}

          th,td{border:1px solid #333;padding:4px 6px}

          th{background:#eef2ff}

        </style></head><body>$pages</body></html>

    """.trimIndent()

}



fun buildChamferingPlanPrintHtml(

    productionDay: String,

    rows: List<InstructionChamferingRowDto>,

): String {

    val byMachine = rows.groupBy { it.chamferingMachine.orEmpty().ifBlank { "（未設定）" } }

    val blocks = byMachine.entries.joinToString("\n") { (machine, list) ->

        val sorted = list.sortedBy { it.productionSequence ?: 0 }

        val trs = sorted.joinToString("\n") { r ->

            """

            <tr>

              <td>${escapeHtml(r.cd.orEmpty())}</td>

              <td>${escapeHtml(r.productName.orEmpty())}</td>

              <td>${r.productionSequence ?: ""}</td>

              <td>${r.actualProductionQuantity ?: ""}</td>

              <td>${escapeHtml(r.remarks.orEmpty())}</td>

            </tr>

            """.trimIndent()

        }

        """

        <div class="block"><h2>${escapeHtml(machine)}</h2>

        <table><thead><tr><th>CD</th><th>製品名</th><th>順</th><th>生産数</th><th>備考</th></tr></thead>

        <tbody>$trs</tbody></table></div>

        """.trimIndent()

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>

        <title>面取計画 $productionDay</title>

        <style>

          body{font-family:Meiryo,sans-serif;font-size:10px;margin:8px}

          h1{font-size:14px;color:#047857}

          h2{font-size:12px;color:#059669;background:#ecfdf5;padding:4px 8px}

          table{border-collapse:collapse;width:100%}

          th,td{border:1px solid #d1fae5;padding:3px 5px}

          th{background:#f0fdf4}

        </style></head><body>

        <h1>面取計画リスト — ${productionDay.replace("-", "/")}</h1>

        $blocks

        </body></html>

    """.trimIndent()

}



fun buildChamferingInstructionSheetHtml(

    productionDay: String,

    rows: List<InstructionChamferingRowDto>,

): String {

    val byMachine = rows.groupBy { it.chamferingMachine.orEmpty().ifBlank { "（未設定）" } }

    val pages = byMachine.entries.joinToString("<div style='page-break-after:always'></div>") { (machine, list) ->

        val sorted = list.sortedBy { it.productionSequence ?: 0 }

        val trs = sorted.joinToString("\n") { r ->

            """

            <tr>

              <td>${r.productionSequence ?: ""}</td>

              <td>${escapeHtml(r.cd.orEmpty())}</td>

              <td>${escapeHtml(r.productName.orEmpty())}</td>

              <td>${r.actualProductionQuantity ?: ""}</td>

              <td>${escapeHtml(r.remarks.orEmpty())}</td>

            </tr>

            """.trimIndent()

        }

        """

        <div><h1>面取指示書</h1>

        <p>生産日: ${productionDay.replace("-", "/")} / 面取機: ${escapeHtml(machine)}</p>

        <table><thead><tr><th>順</th><th>CD</th><th>製品名</th><th>生産数</th><th>備考</th></tr></thead>

        <tbody>$trs</tbody></table></div>

        """.trimIndent()

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/>

        <title>面取指示書</title>

        <style>@page{size:A5 landscape;margin:8mm}body{font-family:Meiryo,sans-serif;font-size:11px}

        h1{color:#047857}table{border-collapse:collapse;width:100%}th,td{border:1px solid #333;padding:4px}</style>

        </head><body>$pages</body></html>

    """.trimIndent()

}



fun buildKanbanTicketHtml(row: KanbanIssuanceRowDto, kanbanNo: String): String = """

    <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/><title>カンバン $kanbanNo</title>

    <style>body{font-family:Meiryo,sans-serif;font-size:12px;margin:12px}

    .ticket{border:2px solid #d97706;padding:12px;max-width:320px}

    .no{font-size:20px;font-weight:bold;color:#b45309}

    table{width:100%;margin-top:8px}td{padding:3px 0}</style></head><body>

    <div class="ticket">

      <div class="no">${escapeHtml(kanbanNo)}</div>

      <table>

        <tr><td>製品</td><td>${escapeHtml(row.productName.orEmpty())}</td></tr>

        <tr><td>ライン</td><td>${escapeHtml(row.productionLine.orEmpty())}</td></tr>

        <tr><td>切断機</td><td>${escapeHtml(row.cuttingMachine.orEmpty())}</td></tr>

        <tr><td>生産日</td><td>${escapeHtml(formatInstructionDate(row.productionDay))}</td></tr>

        <tr><td>生産数</td><td>${row.actualProductionQuantity ?: ""}</td></tr>

        <tr><td>管理CD</td><td>${escapeHtml(row.managementCode.orEmpty())}</td></tr>

      </table>

    </div></body></html>

""".trimIndent()



fun buildKanbanBatchPrintHtml(items: List<Pair<KanbanIssuanceRowDto, String>>): String {

    val pages = items.joinToString("<div style='page-break-after:always'></div>") { (row, no) ->

        buildKanbanTicketHtml(row, no).substringAfter("<body>").substringBefore("</body>")

    }

    return """

        <!DOCTYPE html><html lang="ja"><head><meta charset="utf-8"/><title>カンバン一括印刷</title>

        <style>body{font-family:Meiryo,sans-serif}.ticket{border:2px solid #d97706;padding:12px;margin:8px}</style>

        </head><body>$pages</body></html>

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


