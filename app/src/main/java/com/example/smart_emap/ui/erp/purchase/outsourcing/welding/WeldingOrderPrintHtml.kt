package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingOrderDateJa
import java.text.NumberFormat
import java.util.Locale

data class WeldingOrderPrintFormUi(
    val recipientCompany: String = "",
    val approver: String = "小森",
    val issuer: String = "東條",
    val note1: String = "1.納品書と請求書には必ずこの注文番号をご記入下さい。",
    val note2: String = "2.支払期日には法定税率による消費税額及び地方消費税分を加算して支払います。",
    val note3: String = "3.支払期日・支払方法・検査完了期日・有償支給原材料代金の決済期日及び方法については、令和8年1月1日の「支払方法等について」によります。",
)

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)
private val jpCurrency = NumberFormat.getNumberInstance(Locale.JAPAN).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private fun fmtNum(value: Int?): String = jpNumber.format(value ?: 0)

private fun fmtCurrency(value: Double?): String = "¥${jpCurrency.format(value ?: 0.0)}"

private fun escHtml(text: String?): String = (text ?: "")
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

fun buildWeldingOrderPrintHtml(
    orders: List<OutsourcingWeldingOrderDto>,
    form: WeldingOrderPrintFormUi,
): String {
    val valid = orders.filter { (it.quantity ?: 0) > 0 }
    if (valid.isEmpty()) return ""

    val grouped = valid.groupBy { row ->
        val supplier = row.supplierName ?: row.supplierCd ?: "未指定"
        val orderDate = row.orderDate ?: "未指定"
        "$supplier|$orderDate"
    }.toSortedMap()

    val styles = """
        html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
        body { font-family: 'Meiryo', 'Yu Gothic', sans-serif; margin: 1cm; font-size: 10pt; line-height: 1.4; color: #000; }
        .order-sheet { width: 100%; min-height: 281mm; box-sizing: border-box; page-break-after: always; }
        .order-sheet:last-child { page-break-after: auto; }
        .issued-info { text-align: left; font-size: 13pt; font-weight: 600; margin-bottom: 1mm; }
        .title { text-align: center; font-size: 30pt; font-weight: bold; margin: 1mm 0; letter-spacing: 2px; }
        .recipient-block div { font-size: 26pt; font-weight: bold; margin-top: 4mm; }
        .sender-block { text-align: right; margin-top: -8mm; }
        .sender-block div { font-size: 10pt; margin-bottom: 2mm; }
        .approval-box { border: 2px solid #34495e; width: 120px; margin-left: auto; border-radius: 4px; }
        .approval-box table { width: 100%; border-collapse: collapse; }
        .approval-box td { border: 1px solid #34495e; padding: 1mm; text-align: center; font-size: 8pt; width: 50%; background: #f8f9fa; }
        .delivery-meta { display: flex; justify-content: space-between; margin: 6mm 0 4mm; font-size: 18pt; font-weight: 600; }
        .delivery-meta-left, .delivery-meta-right { background: #f8f9fa; padding: 4px 8px; border-radius: 4px; border: 1px solid #e5e7eb; }
        .order-note { margin: 3mm 0 2mm; font-size: 11pt; font-weight: 600; }
        table { width: 100%; border-collapse: collapse; margin: 2mm 0; }
        th, td { border: 1px solid #34495e; padding: 2mm; font-size: 9pt; }
        th { background: #ecf0f1; font-weight: 600; }
        .text-center { text-align: center; }
        .text-right { text-align: right; }
        .summary-row { margin-top: 4mm; font-size: 12pt; font-weight: 600; }
        .notes { margin-top: 6mm; font-size: 9pt; line-height: 1.5; }
    """.trimIndent()

    val pages = grouped.entries.mapIndexed { index, (_, items) ->
        val sorted = items.sortedBy { it.productName.orEmpty() }
        val first = sorted.first()
        val supplierKey = first.supplierName ?: first.supplierCd ?: "未指定"
        val recipient = form.recipientCompany.ifBlank { "$supplierKey 御中" }
        val orderDateDisplay = formatOutsourcingOrderDateJa(first.orderDate)
        val deliveryDateDisplay = first.deliveryDate ?: "未指定"
        val deliveryLocationDisplay = first.deliveryLocation ?: "未指定"
        val totalQty = sorted.sumOf { it.quantity ?: 0 }

        val rowsHtml = sorted.joinToString("") { row ->
            """
            <tr>
              <td class="text-center">${escHtml(row.orderNo)}</td>
              <td class="text-center">${escHtml(row.productName)}</td>
              <td class="text-center">${escHtml(row.content ?: row.weldingType)}</td>
              <td class="text-right">${fmtCurrency(row.unitPrice)}</td>
              <td class="text-center">${fmtNum(row.quantity)}</td>
              <td class="text-center">本</td>
              <td class="text-center">${escHtml(row.category)}</td>
            </tr>
            """.trimIndent()
        }

        """
        <div class="order-sheet">
          <div class="issued-info">注文日: ${escHtml(orderDateDisplay)}</div>
          <div class="title">注 文 書</div>
          <div class="recipient-block"><div>${escHtml(recipient)}</div></div>
          <div class="sender-block">
            <div>日鉄物産荒井オートモーティブ(株)</div>
            <div>〒496-0902 愛知県愛西市須依町2189</div>
            <div>TEL&lt;0567&gt;28-4171</div>
            <div>FAX&lt;0567&gt;26-2281</div>
            <div class="approval-box">
              <table>
                <tr><td>承認</td><td>発行</td></tr>
                <tr><td>${escHtml(form.approver)}</td><td>${escHtml(form.issuer)}</td></tr>
              </table>
            </div>
          </div>
          <div class="delivery-meta">
            <div class="delivery-meta-left">納入日: ${escHtml(deliveryDateDisplay)}</div>
            <div class="delivery-meta-right">納品場所: ${escHtml(deliveryLocationDisplay)}</div>
          </div>
          <div class="order-note">＊下記のとおり注文いたします。</div>
          <table>
            <thead>
              <tr>
                <th width="25%">注文番号</th>
                <th width="15%">製品名</th>
                <th width="15%">内容</th>
                <th width="10%">単価(円)</th>
                <th width="10%">注文数</th>
                <th width="6%">単位</th>
                <th width="19%">区分</th>
              </tr>
            </thead>
            <tbody>$rowsHtml</tbody>
          </table>
          <div class="summary-row">総注文数  ${fmtNum(totalQty)} 本</div>
          <div class="notes">
            <p>${escHtml(form.note1)}</p>
            <p>${escHtml(form.note2)}</p>
            <p>${escHtml(form.note3)}</p>
          </div>
        </div>
        """.trimIndent()
    }.joinToString("\n")

    return """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><title>注文書</title><style>$styles</style></head>
        <body>$pages</body></html>
    """.trimIndent()
}
