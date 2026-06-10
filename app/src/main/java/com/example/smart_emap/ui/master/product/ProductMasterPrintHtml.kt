package com.example.smart_emap.ui.master.product

import android.graphics.Bitmap
import android.util.Base64
import com.example.smart_emap.data.model.MasterProductDto
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.Collator
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

val productTypePrintOptions = listOf("量産品", "試作品", "補給品", "その他")

private val jaCollator: Collator = Collator.getInstance(Locale.JAPANESE)

private fun escapeHtml(value: String?): String =
    (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

private fun formatLengthValue(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    val formatted = String.format(Locale.JAPAN, "%.2f", value)
    return if (formatted == "0.00") "--" else formatted
}

private fun generateQrCodeDataUrl(content: String, size: Int = 95): String? = runCatching {
    val hints = mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8")
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    "data:image/png;base64,$base64"
}.getOrNull()

data class ProductQrPrintItem(
    val dataUrl: String,
    val productCd: String,
    val productName: String,
)

fun buildProductQrPrintItems(
    products: List<MasterProductDto>,
    selectedTypes: Set<String>,
): List<ProductQrPrintItem> {
    val sorted = products
        .asSequence()
        .filter { product -> selectedTypes.contains(product.productType.orEmpty()) }
        .filter { product ->
            val cd = product.productCd.orEmpty()
            cd.isNotBlank() && cd.last() == '1'
        }
        .sortedWith(compareBy(jaCollator) { it.productName.orEmpty() })
        .toList()

    return sorted.mapNotNull { product ->
        val cd = product.productCd.orEmpty()
        val dataUrl = generateQrCodeDataUrl(cd) ?: return@mapNotNull null
        ProductQrPrintItem(
            dataUrl = dataUrl,
            productCd = cd,
            productName = product.productName.orEmpty(),
        )
    }
}

fun buildProductQrPrintHtml(qrCodes: List<ProductQrPrintItem>): String {
    val qrCodesPerRow = 10
    val qrCodesPerColumn = 9
    val qrCodesPerPage = 90
    val totalPages = if (qrCodes.isEmpty()) 0 else ceil(qrCodes.size.toDouble() / qrCodesPerPage).toInt()

    val pagesHtml = buildString {
        for (page in 0 until totalPages) {
            val startIndex = page * qrCodesPerPage
            if (startIndex >= qrCodes.size) break
            val endIndex = min(startIndex + qrCodesPerPage, qrCodes.size)
            val pageQRCodes = qrCodes.subList(startIndex, endIndex)
            if (pageQRCodes.isEmpty()) break

            append("""<div class="page"><div class="qr-grid">""")
            pageQRCodes.forEachIndexed { index, item ->
                val col = index / qrCodesPerColumn + 1
                val row = index % qrCodesPerColumn + 1
                val nameHtml = if (item.productName.isNotBlank()) {
                    """<div class="qr-product-name">${escapeHtml(item.productName)}</div>"""
                } else {
                    ""
                }
                append(
                    """
                    <div class="qr-item" style="grid-column: $col; grid-row: $row;">
                      <img src="${item.dataUrl}" alt="QRコード" class="qr-code" />
                      $nameHtml
                    </div>
                    """.trimIndent(),
                )
            }
            append("</div></div>")
        }
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>製品QRコード印刷</title>
          <style>
            @page { size: A3 landscape; margin: 0; }
            body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
            .page {
              width: 420mm; height: 297mm; padding: 12mm; margin: 0;
              box-sizing: border-box; display: flex; flex-direction: column;
            }
            .page:not(:last-child) { page-break-after: always; }
            .page:last-child { page-break-after: avoid; }
            .qr-grid {
              display: grid;
              grid-template-columns: repeat($qrCodesPerRow, 1fr);
              grid-template-rows: repeat(9, 1fr);
              gap: 1.5mm; width: 100%; height: 100%; align-content: start;
            }
            .qr-item {
              display: flex; flex-direction: column; align-items: center; justify-content: center;
              padding: 1mm; border: 1px solid #ddd; border-radius: 2px;
              page-break-inside: avoid; box-sizing: border-box;
            }
            .qr-code { width: 70px; height: 70px; margin-bottom: 2px; flex-shrink: 0; }
            .qr-product-name {
              font-size: 12px; font-weight: bold; text-align: center; color: #000;
              word-break: break-all; line-height: 1.3; margin-top: 2px; padding: 0 2px;
            }
            @media print {
              body { margin: 0; padding: 0; }
              .page { margin: 0; padding: 12mm; }
            }
          </style>
        </head>
        <body>
          $pagesHtml
        </body>
        </html>
    """.trimIndent()
}

fun buildProductCuttingLengthPrintHtml(
    products: List<MasterProductDto>,
    materialNameMap: Map<String, String>,
): String {
    if (products.isEmpty()) return ""

    val grouped = products.groupBy { it.materialCd?.takeIf { cd -> cd.isNotBlank() } ?: "未設定" }
    val sortedMaterials = grouped.keys.sortedWith { a, b ->
        val nameA = materialNameMap[a].orEmpty().ifBlank { a }
        val nameB = materialNameMap[b].orEmpty().ifBlank { b }
        val byName = jaCollator.compare(nameA, nameB)
        if (byName != 0) byName else jaCollator.compare(a, b)
    }

    val generatedAt = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))

    val sectionsHtml = sortedMaterials.joinToString("\n") { material ->
        val materialName = materialNameMap[material].orEmpty()
        val materialLabel = if (materialName.isNotBlank()) "$material｜$materialName" else material
        val rows = grouped.getValue(material)
            .sortedWith(compareBy(jaCollator) { it.productName.orEmpty() })
            .joinToString("\n") { item ->
                """
                <tr>
                  <td>${escapeHtml(item.productCd)}</td>
                  <td>${escapeHtml(item.productName)}</td>
                  <td class="num">${formatLengthValue(item.cutLength)}</td>
                  <td class="num">${formatLengthValue(item.chamferLength)}</td>
                  <td class="num">${formatLengthValue(item.developedLength)}</td>
                  <td class="num">${formatLengthValue(item.scrapLength)}</td>
                  <td class="num">${item.takeCount?.toString() ?: "—"}</td>
                </tr>
                """.trimIndent()
            }

        """
        <section class="material-section">
          <div class="material-title">材料: ${escapeHtml(materialLabel)}</div>
          <table>
            <thead>
              <tr>
                <th>製品CD</th>
                <th>製品名称</th>
                <th>切断長さ(mm)</th>
                <th>面取り長さ(mm)</th>
                <th>展開長さ(mm)</th>
                <th>端材長さ(mm)</th>
                <th>取り数</th>
              </tr>
            </thead>
            <tbody>
              $rows
            </tbody>
          </table>
        </section>
        """.trimIndent()
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>切断長印刷</title>
          <style>
            @page { size: A4 portrait; margin: 10mm; }
            body {
              margin: 0;
              font-family: "Yu Gothic UI", "Meiryo", sans-serif;
              color: #111827;
              font-size: 11px;
            }
            .report-header { margin-bottom: 10px; }
            .report-title { font-size: 18px; font-weight: 700; margin: 0 0 4px; }
            .report-meta { font-size: 11px; color: #4b5563; }
            .material-section {
              margin-top: 8px;
              break-inside: avoid-page;
              page-break-inside: avoid;
            }
            .material-title {
              font-size: 14px; font-weight: 700; margin: 0 0 6px; padding: 4px 6px;
              background: #f3f4f6; border-left: 4px solid #2563eb;
              break-after: avoid-page; page-break-after: avoid;
            }
            table {
              width: 100%; border-collapse: collapse; table-layout: fixed;
              break-before: avoid-page; page-break-before: avoid;
            }
            th, td {
              border: 1px solid #d1d5db; padding: 4px 6px;
              vertical-align: middle; word-break: break-word;
            }
            tr { break-inside: avoid; page-break-inside: avoid; }
            th { background: #f9fafb; font-weight: 700; text-align: center; }
            td.num { text-align: right; font-variant-numeric: tabular-nums; }
            @media print {
              body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
            }
          </style>
        </head>
        <body>
          <div class="report-header">
            <h1 class="report-title">切断長印刷</h1>
            <div class="report-meta">出力日時: ${escapeHtml(generatedAt)} / 総件数: ${products.size}</div>
          </div>
          $sectionsHtml
        </body>
        </html>
    """.trimIndent()
}
