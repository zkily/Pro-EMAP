package com.example.smart_emap.ui.master.part

import android.graphics.Bitmap
import android.util.Base64
import com.example.smart_emap.data.model.MasterPartDto
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.Collator
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

data class PartQrPrintItem(
    val dataUrl: String,
    val partCd: String,
    val partName: String,
)

private val jaCollator: Collator = Collator.getInstance(Locale.JAPANESE)

private fun escapeHtml(value: String?): String =
    (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

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
    "data:image/png;base64,${Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)}"
}.getOrNull()

fun buildPartQrPrintItems(parts: List<MasterPartDto>): List<PartQrPrintItem> =
    parts
        .asSequence()
        .filter { !it.partCd.isNullOrBlank() }
        .sortedWith(compareBy(jaCollator) { it.partName.orEmpty() })
        .mapNotNull { part ->
            val cd = part.partCd.orEmpty()
            val dataUrl = generateQrCodeDataUrl(cd) ?: return@mapNotNull null
            PartQrPrintItem(dataUrl = dataUrl, partCd = cd, partName = part.partName.orEmpty())
        }
        .toList()

fun buildPartQrPrintHtml(qrCodes: List<PartQrPrintItem>): String {
    val qrCodesPerRow = 5
    val qrCodesPerPage = 40
    val totalPages = if (qrCodes.isEmpty()) 0 else ceil(qrCodes.size.toDouble() / qrCodesPerPage).toInt()
    val pagesHtml = buildString {
        for (page in 0 until totalPages) {
            val startIndex = page * qrCodesPerPage
            if (startIndex >= qrCodes.size) break
            val endIndex = min(startIndex + qrCodesPerPage, qrCodes.size)
            val pageQRCodes = qrCodes.subList(startIndex, endIndex)
            append("""<div class="page"><div class="page-title">部品マスタQR</div><div class="qr-grid">""")
            pageQRCodes.forEachIndexed { index, item ->
                val col = index % qrCodesPerRow + 1
                val row = index / qrCodesPerRow + 1
                val nameHtml = if (item.partName.isNotBlank()) {
                    """<div class="qr-part-name">${escapeHtml(item.partName)}</div>"""
                } else ""
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
        <html><head><meta charset="UTF-8"><title>部品QRコード印刷</title>
        <style>
          @page { size: A4 portrait; margin: 0; }
          body { margin: 0; font-family: Arial, sans-serif; }
          .page { width: 210mm; height: 297mm; padding: 12mm; box-sizing: border-box; page-break-after: always; }
          .page:last-child { page-break-after: avoid; }
          .page-title { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 8mm; }
          .qr-grid { display: grid; grid-template-columns: repeat($qrCodesPerRow, 1fr); grid-template-rows: repeat(8, 1fr); gap: 1.2mm; flex: 1; }
          .qr-item { display: flex; flex-direction: column; align-items: center; justify-content: center; }
          .qr-code { width: 18mm; height: 18mm; }
          .qr-part-name { font-size: 7px; text-align: center; margin-top: 1mm; word-break: break-all; }
        </style></head><body>$pagesHtml</body></html>
    """.trimIndent()
}
