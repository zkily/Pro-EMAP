package com.example.smart_emap.ui.erp.production.requirements

import com.example.smart_emap.data.model.ComponentRequirementsDailyMatrixRowDto
import com.example.smart_emap.data.model.MaterialRequirementsDailyMatrixRowDto

internal fun formatQty(n: Double?): String {
    if (n == null || n.isNaN()) return "0"
    return if (n == Math.floor(n) && !n.isInfinite()) {
        "%,d".format(n.toLong())
    } else {
        "%,.4f".format(n).trimEnd('0').trimEnd('.')
    }
}

internal fun formatInt(n: Double?): String {
    if (n == null || n.isNaN()) return "0"
    return "%,d".format(n.toLong())
}

internal fun shortDateLabel(iso: String): String {
    if (iso.length < 10) return iso
    return iso.substring(5, 10)
}

internal fun escapeHtml(text: String?): String {
    if (text == null) return ""
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}

private val printStyles = """
    @page { size: A4 landscape; margin: 8mm; }
    * { box-sizing: border-box; }
    html, body { margin: 0; padding: 0; }
    body { font-family: 'Hiragino Sans', 'Yu Gothic', Meiryo, sans-serif; color: #111; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    .print-mfg { padding: 0 0 3mm 0; }
    .print-doc-hdr { margin: 0 0 4mm 0; padding: 0 0 3mm 0; border-bottom: 0.75pt solid #94a3b8; }
    .print-doc-hdr h1 { font-size: 12.5pt; margin: 0 0 2.5mm 0; font-weight: 800; color: #0f172a; }
    .print-doc-meta { font-size: 8.5pt; margin: 0; color: #475569; padding: 1.8mm 3mm; background: #f1f5f9; border: 0.5pt solid #cbd5e1; border-radius: 5pt; display: inline-block; }
    .print-doc-meta strong { color: #1e293b; font-weight: 800; }
    .print-tbl-wrap { border-radius: 5pt; overflow: hidden; border: 0.55pt solid #94a3b8; background: #fff; }
    .print-tbl { width: 100%; border-collapse: collapse; table-layout: fixed; font-size: 7.25pt; }
    .print-tbl th, .print-tbl td { border: 0.45pt solid #cbd5e1; padding: 0.65mm 0.55mm; line-height: 1.6; }
    .print-tbl th { background: #e2e8f0; font-weight: 700; text-align: center; }
    .td-num { text-align: right; font-variant-numeric: tabular-nums; }
""".trimIndent()

object MaterialRequirementsPrint {
    fun build(
        dates: List<String>,
        rows: List<MaterialRequirementsDailyMatrixRowDto>,
        period: String,
    ): String {
        val title = "日別・材料別需要"
        val byMaker = LinkedHashMap<String, MutableList<MaterialRequirementsDailyMatrixRowDto>>()
        rows.forEach { r ->
            val m = r.materialManufacturer ?: ""
            byMaker.getOrPut(m) { mutableListOf() }.add(r)
        }
        val makers = byMaker.keys.sortedWith(compareBy { it })

        val dateThs = dates.joinToString("") { "<th>${escapeHtml(shortDateLabel(it))}</th>" }
        val header = """<thead><tr>
            <th>メーカー</th><th>材料名</th><th>規格</th>$dateThs<th>期間合計件数</th>
        </tr></thead>"""

        val sections = StringBuilder()
        makers.forEachIndexed { mi, maker ->
            val sorted = (byMaker[maker] ?: emptyList()).sortedWith(
                compareBy({ it.materialName ?: "" }, { it.standardSpecification ?: "" }),
            )
            val bodyRows = sorted.joinToString("") { row ->
                val tds = dates.joinToString("") { d ->
                    val v = row.byDate?.get(d) ?: 0.0
                    val cell = if (v == 0.0) "" else formatQty(v)
                    "<td class=\"td-num\">${escapeHtml(cell)}</td>"
                }
                """<tr>
                    <td>${escapeHtml(row.materialManufacturer)}</td>
                    <td>${escapeHtml(row.materialName)}</td>
                    <td>${escapeHtml(row.standardSpecification)}</td>
                    $tds
                    <td class="td-num">${escapeHtml(formatInt(row.rowTotal))}</td>
                </tr>"""
            }
            val pageBreak = if (mi < makers.size - 1) "page-break-after: always; break-after: page;" else ""
            sections.append(
                """<section class="print-mfg" style="$pageBreak">
                    <header class="print-doc-hdr">
                        <h1>${escapeHtml(title)}</h1>
                        <p class="print-doc-meta">${escapeHtml(period)} &nbsp;|&nbsp; メーカー: <strong>${escapeHtml(maker.ifBlank { "—" })}</strong></p>
                    </header>
                    <div class="print-tbl-wrap"><table class="print-tbl">$header<tbody>$bodyRows</tbody></table></div>
                </section>""",
            )
        }
        return "<!DOCTYPE html><html lang=\"ja\"><head><meta charset=\"utf-8\"/><title>$title</title><style>$printStyles</style></head><body>$sections</body></html>"
    }
}

object ComponentRequirementsPrint {
    fun build(
        dates: List<String>,
        rows: List<ComponentRequirementsDailyMatrixRowDto>,
        period: String,
        title: String,
    ): String {
        val sorted = rows.sortedBy { it.componentName ?: "" }
        val dateThs = dates.joinToString("") { "<th>${escapeHtml(shortDateLabel(it))}</th>" }
        val header = """<thead><tr>
            <th>部品CD</th><th>部品名</th><th>単位</th>$dateThs<th>所要量合計</th>
        </tr></thead>"""
        val bodyRows = sorted.joinToString("") { row ->
            val tds = dates.joinToString("") { d ->
                val v = row.byDate?.get(d) ?: 0.0
                val cell = if (v == 0.0) "" else formatQty(v)
                "<td class=\"td-num\">${escapeHtml(cell)}</td>"
            }
            """<tr>
                <td>${escapeHtml(row.componentCd)}</td>
                <td>${escapeHtml(row.componentName)}</td>
                <td>${escapeHtml(row.componentUom)}</td>
                $tds
                <td class="td-num">${escapeHtml(formatQty(row.rowTotal))}</td>
            </tr>"""
        }
        val body = """<section class="print-mfg">
            <header class="print-doc-hdr">
                <h1>${escapeHtml(title)}</h1>
                <p class="print-doc-meta">${escapeHtml(period)}</p>
            </header>
            <div class="print-tbl-wrap"><table class="print-tbl">$header<tbody>$bodyRows</tbody></table></div>
        </section>"""
        return "<!DOCTYPE html><html lang=\"ja\"><head><meta charset=\"utf-8\"/><title>${escapeHtml(title)}</title><style>$printStyles</style></head><body>$body</body></html>"
    }
}
