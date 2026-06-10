package com.example.smart_emap.ui.erp.production.planning

fun buildProcessMachinePlanPrintHtml(state: ProcessMachinePlanUiState): String {
    val grand = state.data?.grandTotal
    val title = when (state.viewMode) {
        ProcessMachineViewMode.Summary -> "工程別設備別計画 - 対比集計"
        ProcessMachineViewMode.Daily -> "工程別設備別計画 - 日別明細"
        ProcessMachineViewMode.Trend -> "工程別設備別計画 - 達成率"
    }
    val headers: List<String>
    val tableRows: List<List<String>>
    when (state.viewMode) {
        ProcessMachineViewMode.Summary -> {
            headers = listOf("工程", "設備", "計画", "実績", "差異", "達成率", "実計", "不良", "廃棄", "不良率")
            tableRows = state.summaryTableRows.map { ProcessMachinePlanLogic.rowToCells(it) }
        }
        ProcessMachineViewMode.Daily -> {
            headers = ProcessMachinePlanLogic.buildDailyHeaders(state.data?.dates.orEmpty())
            tableRows = state.dailyTableRows.map { row ->
                listOf(row.processLabel, row.machine) +
                    state.data?.dates.orEmpty().map { d ->
                        ProcessMachinePlanLogic.formatDailyCell(row.dailyValues[d] ?: 0, state.dailyMetric)
                    } +
                    listOf(ProcessMachinePlanLogic.formatDailyCell(row.rowTotal, state.dailyMetric))
            }
        }
        ProcessMachineViewMode.Trend -> {
            headers = listOf("日付", "曜", "計画", "実績", "差異", "達成率")
            tableRows = state.trendDailyRows.map { r ->
                listOf(
                    r.date,
                    ProcessMachinePlanLogic.formatDateHeaderWeek(r.date),
                    formatProductionNumber(r.plan),
                    formatProductionNumber(r.actual),
                    formatProductionSigned(r.diff),
                    formatProductionPercent(r.rate),
                )
            }
        }
    }
    val headerHtml = headers.joinToString("") { "<th>$it</th>" }
    val bodyHtml = tableRows.joinToString("") { row ->
        "<tr>${row.joinToString("") { "<td>$it</td>" }}</tr>"
    }
    val summaryHtml = grand?.let {
        "<p>計画合計: ${formatProductionNumber(it.plan)} / 実績合計: ${formatProductionNumber(it.actual)} / 差異: ${formatProductionSigned(it.diff)} / 達成率: ${formatProductionPercent(it.achievementRate)}</p>"
    }.orEmpty()
    return """
        <!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/>
        <title>$title</title>
        <style>
        body{font-family:'Segoe UI',sans-serif;font-size:10pt;color:#0f172a;margin:16px}
        h1{font-size:14pt;margin:0 0 8px}
        table{border-collapse:collapse;width:100%;margin-top:8px}
        th,td{border:1px solid #cbd5e1;padding:4px 6px;font-size:9pt;text-align:center}
        th{background:linear-gradient(180deg,#f8fafc,#f1f5f9);font-weight:700}
        tr:nth-child(even){background:#fafbfc}
        </style>
        </head><body>
        <h1>$title</h1>
        <p>${state.startDate} ～ ${state.endDate}</p>
        $summaryHtml
        <table><thead><tr>$headerHtml</tr></thead><tbody>$bodyHtml</tbody></table>
        </body></html>
    """.trimIndent()
}
