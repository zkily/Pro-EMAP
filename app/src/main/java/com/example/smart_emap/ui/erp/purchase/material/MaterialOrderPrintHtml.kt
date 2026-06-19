package com.example.smart_emap.ui.erp.purchase.material

import com.example.smart_emap.data.model.MaterialOrderPrintRow
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class MaterialOrderPrintFormUi(
    val recipientCompany: String = "丸一鋼管株式会社 御中",
    val recipientPersons: String = "鈴木様 村松様 只井様",
    val approver: String = "篠田",
    val issuer: String = "趙",
    val note1: String = "1.支払期日には法定税率による消費税額及び地方消費税分を加算して支払います。",
    val note2: String = "2.支払期日・支払方法・検査完了期日・有償支給原材料代金の決済期日及び方法については、令和8年1月1日の「支払方法等について」によります。",
)

data class MaterialManualOrderFormUi(
    val date: String = "",
    val materialCd: String = "",
    val materialName: String = "",
    val orderQuantity: Int = 0,
    val orderBundleQuantity: Int = 0,
    val remarks: String = "バラ束",
)

/** Web `MARUICHI_ORDER_SHEET_STYLES` と同等（A4 縦） */
private val MARUICHI_ORDER_SHEET_STYLES = """
    html {
      height: 100%;
    }
    body {
      font-family: 'Meiryo', 'Yu Gothic', sans-serif;
      margin: 1cm 0.5cm 0.5cm 0.5cm;
      padding-top: 0.3cm;
      min-height: 100%;
      font-size: 10pt;
      line-height: 1.4;
      background-color: #ffffff;
      color: #000000;
      box-sizing: border-box;
    }
    .order-sheet {
      width: 100%;
      margin: 0 auto;
      box-sizing: border-box;
      position: relative;
      min-height: 275mm;
      padding-bottom: 36mm;
    }
    .order-sheet-main {
      width: 100%;
    }
    .header {
      margin-bottom: 1mm;
      position: relative;
    }
    .issued-info {
      text-align: left;
      font-size: 9pt;
      margin-bottom: 1mm;
    }
    .title {
      text-align: center;
      font-size: 24pt;
      font-weight: bold;
      margin: 2mm 0 3mm;
      color: #000000;
      text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
      letter-spacing: 2px;
    }
    .recipient-block {
      margin-bottom: 4mm;
      margin-top: 4mm;
    }
    .recipient-block div {
      margin-bottom: 2mm;
      font-size: 18pt;
      font-weight: bold;
      color: #000000;
    }
    .sender-block {
      text-align: right;
      margin-bottom: 1mm;
      margin-top: -12mm;
    }
    .sender-block div {
      margin-bottom: 2mm;
      font-size: 10pt;
      color: #000000;
    }
    .approval-box {
      border: 2px solid #34495e;
      width: 120px;
      margin-left: auto;
      text-align: center;
      margin-top: 1mm;
      border-collapse: collapse;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .approval-box table {
      width: 100%;
      border-collapse: collapse;
      margin: 0;
    }
    .approval-box td {
      border: 1px solid #34495e;
      padding: 1mm;
      text-align: center;
      font-size: 8pt;
      width: 50%;
      background-color: #f8f9fa;
      font-weight: 500;
    }
    .delivery-info {
      margin: 5mm 0;
      display: flex;
      justify-content: space-between;
    }
    .delivery-info div {
      font-size: 13pt;
      font-weight: bold;
      color: #000000;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 2mm 0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-radius: 6px;
      overflow: hidden;
    }
    th, td {
      border: 1px solid #dee2e6;
      padding: 2mm 3mm;
      text-align: left;
      font-size: 9pt;
    }
    th {
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      text-align: center;
      font-weight: bold;
      color: #000000;
      border-bottom: 2px solid #dee2e6;
    }
    .text-center { text-align: center; }
    .text-right { text-align: right; }
    .summary-row {
      display: flex;
      justify-content: flex-end;
      margin-top: 4mm;
      gap: 8mm;
      padding: 4mm 8mm;
      border-top: 2px solid #dee2e6;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border-radius: 6px;
    }
    .summary-item {
      font-weight: bold;
      font-size: 11pt;
      color: #2c3e50;
      padding: 2mm 4mm;
      background-color: #ffffff;
      border-radius: 4px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
      border: 1px solid #dee2e6;
    }
    .notes {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      font-size: 9pt;
      line-height: 1.6;
      background-color: #f8f9fa;
      padding: 4mm 6mm;
      border-radius: 6px;
      border-left: 4px solid #6c757d;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      box-sizing: border-box;
    }
    .notes p {
      margin: 2mm 0;
      color: #495057;
      font-weight: 400;
    }
    .notes p:first-child { margin-top: 0; }
    .notes p:last-child { margin-bottom: 0; }
    @page {
      size: A4 portrait;
      margin: 1cm 0.5cm 0.5cm 0.5cm;
    }
    @media print {
      html, body {
        height: auto;
        min-height: 0;
      }
      body {
        margin: 0;
        padding-top: 0.3cm;
      }
      .order-sheet {
        position: relative;
        min-height: 275mm;
        padding-bottom: 36mm;
        page-break-inside: avoid;
      }
      .notes {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
      }
    }
""".trimIndent()

fun buildMaterialOrderPrintHtml(
    rows: List<MaterialOrderPrintRow>,
    deliveryDate: String,
    form: MaterialOrderPrintFormUi,
): String {
    val sorted = rows.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.materialName.orEmpty() })
    val totalWeight = sorted.sumOf { it.bundleWeight }
    val totalBundles = sorted.sumOf { it.orderQuantity }
    val totalPieces = sorted.sumOf { it.orderBundleQuantity }
    val issuedDateTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"))
    val delivery = deliveryDate.ifBlank { "未指定" }

    val tableRows = sorted.joinToString("\n") { row ->
        val materialName = row.materialName.orEmpty()
        val length = Regex("(\\d{4})$").find(materialName)?.groupValues?.getOrNull(1).orEmpty()
        """
        <tr>
          <td class="text-center">${escapeHtml(row.standardSpec.orEmpty())}</td>
          <td class="text-center">${escapeHtml(materialName)}</td>
          <td class="text-right">${escapeHtml(length)}</td>
          <td class="text-right">${row.bundleWeight.roundToInt()} kg</td>
          <td class="text-center">${row.orderQuantity}</td>
          <td class="text-center">${row.orderBundleQuantity}</td>
          <td>${escapeHtml(row.remarks.orEmpty())}</td>
        </tr>
        """.trimIndent()
    }

    val bodyContent = """
    <div class="order-sheet">
      <div class="order-sheet-main">
      <div class="issued-info">発行日: $issuedDateTime</div>
      <div class="title">注 文 書</div>
      <div class="header">
        <div class="recipient-block">
          <div>${escapeHtml(form.recipientCompany)}</div>
          <div>${escapeHtml(form.recipientPersons)}</div>
        </div>
        <div class="sender-block">
          <div>日鉄物産荒井オートモーティブ(株)     </div>
          <div>〒496-0902 愛知県愛西市須依町2189  </div>
          <div>TEL&lt;0567&gt;28-4171</div>
          <div>FAX&lt;0567&gt;26-2281</div>
          <div class="approval-box">
            <table>
              <tr><td>承認</td><td>発行</td></tr>
              <tr><td>${escapeHtml(form.approver)}</td><td>${escapeHtml(form.issuer)}</td></tr>
            </table>
          </div>
        </div>
        <div class="delivery-info">
          <div>納入日 $delivery</div>
          <div>(納入場所:長尺材置場)</div>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th width="13%">規格</th>
            <th width="20%">サイズ</th>
            <th width="10%">長さ</th>
            <th width="13%">重量</th>
            <th width="13%">注文束数</th>
            <th width="13%">注文本数</th>
            <th width="18%">備考</th>
          </tr>
        </thead>
        <tbody>
          $tableRows
        </tbody>
      </table>
      <div class="summary-row">
        <div class="summary-item">総重量  ${totalWeight.roundToInt()} Kg</div>
        <div class="summary-item">総束数  $totalBundles 束</div>
        <div class="summary-item">総本数  $totalPieces 本</div>
      </div>
      </div>
      <div class="notes">
        <p>${escapeHtml(form.note1)}</p>
        <p>${escapeHtml(form.note2)}</p>
      </div>
    </div>
    """.trimIndent()

    return """
        <!DOCTYPE html>
        <html lang="ja">
        <head>
          <meta charset="UTF-8"/>
          <title>注文書</title>
          <style>$MARUICHI_ORDER_SHEET_STYLES</style>
        </head>
        <body>$bodyContent</body>
        </html>
    """.trimIndent()
}

private fun escapeHtml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
