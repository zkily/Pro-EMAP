package com.example.smart_emap.ui.erp.purchase.material

import com.example.smart_emap.data.model.MaterialOrderPrintRow
import java.text.NumberFormat
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

fun buildMaterialOrderPrintHtml(
    rows: List<MaterialOrderPrintRow>,
    deliveryDate: String,
    form: MaterialOrderPrintFormUi,
): String {
    val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)
    val sorted = rows.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.materialName.orEmpty() })
    val totalWeight = sorted.sumOf { it.bundleWeight }
    val totalBundles = sorted.sumOf { it.orderQuantity }
    val totalPieces = sorted.sumOf { it.orderBundleQuantity }
    val issuedDateTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"))

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

    return """
        <!DOCTYPE html>
        <html lang="ja">
        <head>
          <meta charset="UTF-8"/>
          <title>注文書</title>
          <style>
            body { font-family: "MS Gothic", "Yu Gothic", sans-serif; font-size: 12px; color: #111; }
            .order-sheet { max-width: 900px; margin: 0 auto; }
            .issued-info { text-align: right; margin-bottom: 8px; }
            .title { text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 8px; margin: 16px 0; }
            .header { display: flex; justify-content: space-between; margin-bottom: 16px; }
            .recipient-block { line-height: 1.6; }
            .sender-block { text-align: right; line-height: 1.5; }
            table { width: 100%; border-collapse: collapse; margin-top: 12px; }
            th, td { border: 1px solid #333; padding: 6px 4px; }
            th { background: #f3f4f6; }
            .text-center { text-align: center; }
            .text-right { text-align: right; }
            .summary { margin-top: 12px; line-height: 1.8; }
            .notes { margin-top: 16px; line-height: 1.6; }
          </style>
        </head>
        <body>
          <div class="order-sheet">
            <div class="issued-info">発行日: $issuedDateTime</div>
            <div class="title">注 文 書</div>
            <div class="header">
              <div class="recipient-block">
                <div>${escapeHtml(form.recipientCompany)}</div>
                <div>${escapeHtml(form.recipientPersons)}</div>
              </div>
              <div class="sender-block">
                <div>日鉄物産荒井オートモーティブ(株)</div>
                <div>〒496-0902 愛知県愛西市須依町2189</div>
                <div>TEL 0567-28-4171</div>
                <div>FAX 0567-26-2281</div>
                <div>承認: ${escapeHtml(form.approver)} / 発行: ${escapeHtml(form.issuer)}</div>
              </div>
            </div>
            <div>納入日: ${escapeHtml(deliveryDate)}</div>
            <table>
              <thead>
                <tr>
                  <th>規格</th><th>サイズ</th><th>長さ</th><th>重量</th><th>束数</th><th>本数</th><th>備考</th>
                </tr>
              </thead>
              <tbody>
                $tableRows
              </tbody>
            </table>
            <div class="summary">
              合計 束数: ${jpNumber.format(totalBundles)} / 本数: ${jpNumber.format(totalPieces)} / 重量: ${jpNumber.format(totalWeight.roundToInt())} kg
            </div>
            <div class="notes">
              <p>${escapeHtml(form.note1)}</p>
              <p>${escapeHtml(form.note2)}</p>
            </div>
          </div>
        </body>
        </html>
    """.trimIndent()
}

private fun escapeHtml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
