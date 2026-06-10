package com.example.smart_emap.ui.master.process

import android.graphics.Bitmap
import android.util.Base64
import com.example.smart_emap.data.model.MasterProcessDto
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.text.Collator
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

data class ProcessQrPrintItem(
    val dataUrl: String,
    val code: String,
    val name: String,
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

private fun qrCodeContent(processCd: String): String =
    if (processCd.length > 2) processCd.substring(2) else processCd

fun buildProcessQrPrintItems(processes: List<MasterProcessDto>): List<ProcessQrPrintItem> =
    processes
        .asSequence()
        .filter { !it.processCd.isNullOrBlank() }
        .sortedWith(compareBy(jaCollator) { it.processCd.orEmpty() })
        .mapNotNull { process ->
            val cd = process.processCd.orEmpty()
            val code = qrCodeContent(cd)
            val dataUrl = generateQrCodeDataUrl(code) ?: return@mapNotNull null
            ProcessQrPrintItem(dataUrl = dataUrl, code = code, name = process.processName.orEmpty())
        }
        .toList()

fun buildProcessQrPrintHtml(qrCodes: List<ProcessQrPrintItem>): String {
    val qrCodesPerRow = 4
    val qrCodesPerPage = 28
    val totalPages = if (qrCodes.isEmpty()) 0 else ceil(qrCodes.size.toDouble() / qrCodesPerPage).toInt()
    val pagesHtml = buildString {
        for (page in 0 until totalPages) {
            val startIndex = page * qrCodesPerPage
            if (startIndex >= qrCodes.size) break
            val endIndex = min(startIndex + qrCodesPerPage, qrCodes.size)
            val pageQRCodes = qrCodes.subList(startIndex, endIndex)
            append("""<div class="page"><div class="page-title">工程QR</div><div class="qr-grid">""")
            pageQRCodes.forEach { item ->
                val nameHtml = if (item.name.isNotBlank()) {
                    """<div class="qr-name">${escapeHtml(item.name)}</div>"""
                } else ""
                append(
                    """
                    <div class="qr-item">
                      <img src="${item.dataUrl}" alt="QRコード" class="qr-code" />
                      <div class="qr-code-text">${escapeHtml(item.code)}</div>
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
        <html><head><meta charset="UTF-8"><title>工程QR</title>
        <style>
          @page { size: A4 portrait; margin: 0; }
          body { margin: 0; font-family: Arial, sans-serif; }
          .page { width: 210mm; height: 297mm; padding: 12mm; box-sizing: border-box; page-break-after: always; display: flex; flex-direction: column; }
          .page:last-child { page-break-after: avoid; }
          .page-title { text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 8mm; }
          .qr-grid { display: grid; grid-template-columns: repeat($qrCodesPerRow, 1fr); gap: 1.5mm; }
          .qr-item { display: flex; flex-direction: column; align-items: center; padding: 1mm; border: 1px solid #ddd; border-radius: 2px; }
          .qr-code { width: 70px; height: 70px; margin-bottom: 2px; }
          .qr-code-text { font-size: 11px; font-weight: bold; }
          .qr-name { font-size: 12px; font-weight: bold; word-break: break-all; text-align: center; }
        </style></head><body>$pagesHtml</body></html>
    """.trimIndent()
}
