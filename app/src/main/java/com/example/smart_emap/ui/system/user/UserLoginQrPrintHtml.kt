package com.example.smart_emap.ui.system.user

import android.graphics.Bitmap
import android.util.Base64
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.UserLoginQrItemDto
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.min

data class UserLoginQrPrintItem(
    val dataUrl: String,
    val username: String,
    val fullName: String,
    val email: String,
    val department: String,
    val section: String,
)

private fun escapeHtml(value: String?): String =
    (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private fun generateQrCodeDataUrl(content: String, size: Int = 220): String? = runCatching {
    val hints = mapOf(EncodeHintType.MARGIN to 1, EncodeHintType.CHARACTER_SET to "UTF-8")
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

private fun formatOrg(department: String, section: String): String = when {
    department.isNotBlank() && section.isNotBlank() -> "$department / $section"
    department.isNotBlank() -> department
    section.isNotBlank() -> section
    else -> "—"
}

fun buildUserLoginQrPrintItems(
    items: List<UserLoginQrItemDto>,
    users: List<UserListItemDto> = emptyList(),
): List<UserLoginQrPrintItem> {
    val byId = users.associateBy { it.id }
    return items.mapNotNull { item ->
        val dataUrl = generateQrCodeDataUrl(item.payload) ?: return@mapNotNull null
        val row = byId[item.userId]
        UserLoginQrPrintItem(
            dataUrl = dataUrl,
            username = item.username,
            fullName = item.fullName.orEmpty().ifBlank { row?.fullName.orEmpty() }.trim(),
            email = item.email.orEmpty().ifBlank { row?.email.orEmpty() }.trim(),
            department = item.department.orEmpty().ifBlank { row?.department.orEmpty() }.trim(),
            section = item.section.orEmpty().ifBlank { row?.section.orEmpty() }.trim(),
        )
    }
}

fun buildUserLoginQrPrintHtml(qrCodes: List<UserLoginQrPrintItem>): String {
    val perPage = 8
    val totalPages = if (qrCodes.isEmpty()) 0 else ceil(qrCodes.size.toDouble() / perPage).toInt()
    val pagesHtml = buildString {
        for (page in 0 until totalPages) {
            val startIndex = page * perPage
            val endIndex = min(startIndex + perPage, qrCodes.size)
            val pageItems = qrCodes.subList(startIndex, endIndex)
            append("""<section class="page">""")
            pageItems.forEach { item ->
                append(
                    """
                    <article class="card">
                      <div class="card-body">
                        <div class="card-main">
                          <div class="brand-row">
                            <div class="logo-mark"><span>S</span></div>
                            <div class="brand-text">
                              <div class="brand-name">SMART-EMAP</div>
                              <div class="brand-sub">LOGIN CARD</div>
                            </div>
                          </div>
                          <dl class="fields">
                            <div class="row"><dt>氏名</dt><dd class="name">${escapeHtml(item.fullName.ifBlank { "—" })}</dd></div>
                            <div class="row"><dt>ユーザー名</dt><dd>${escapeHtml(item.username.ifBlank { "—" })}</dd></div>
                            <div class="row"><dt>部門</dt><dd>${escapeHtml(formatOrg(item.department, item.section))}</dd></div>
                            <div class="row"><dt>メール</dt><dd class="email">${escapeHtml(item.email.ifBlank { "—" })}</dd></div>
                          </dl>
                        </div>
                        <div class="qr-panel">
                          <img class="qr-code" src="${item.dataUrl}" alt="QR"/>
                        </div>
                      </div>
                    </article>
                    """.trimIndent(),
                )
            }
            append("</section>")
        }
    }
    return """
        <html><head><meta charset="UTF-8"><title>ログインQR</title>
        <style>
          @page { size: A4 portrait; margin: 0; }
          * { box-sizing: border-box; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          html, body { margin: 0; padding: 0; background: #fff; }
          body { font-family: "Segoe UI", "Hiragino Sans", "Yu Gothic", "Noto Sans JP", sans-serif; }
          .page {
            width: 210mm; height: 297mm; padding: 14mm 11mm;
            display: grid; grid-template-columns: 85.6mm 85.6mm;
            grid-auto-rows: 54mm; gap: 7mm 10.8mm; justify-content: center;
            page-break-after: always;
          }
          .page:last-child { page-break-after: auto; }
          .card {
            position: relative; width: 85.6mm; height: 54mm; overflow: hidden;
            border-radius: 3.2mm; background: transparent;
            border: 0.45mm solid #1e293b; color: #0f172a;
          }
          .card-body {
            height: 100%; display: flex; padding: 2.8mm 3mm 2.8mm 3.4mm; gap: 2.4mm;
          }
          .card-main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
          .brand-row { display: flex; align-items: center; gap: 1.8mm; flex-shrink: 0; }
          .logo-mark {
            width: 6.4mm; height: 6.4mm; border-radius: 1.4mm; flex-shrink: 0;
            border: 0.3mm solid #1e293b; display: flex; align-items: center; justify-content: center;
          }
          .logo-mark span { font-size: 3.7mm; font-weight: 800; color: #1e293b; line-height: 1; }
          .brand-name { font-size: 3mm; font-weight: 800; letter-spacing: 0.22mm; line-height: 1.1; }
          .brand-sub { font-size: 1.8mm; letter-spacing: 0.5mm; color: #64748b; margin-top: 0.3mm; }
          .fields { margin: auto 0 0; display: flex; flex-direction: column; gap: 1.05mm; }
          .row { display: grid; grid-template-columns: 15.5mm minmax(0, 1fr); gap: 1.2mm; align-items: start; }
          .row dt { font-size: 1.7mm; color: #64748b; font-weight: 700; line-height: 1.35; padding-top: 0.15mm; }
          .row dd {
            margin: 0; font-size: 2.55mm; font-weight: 700; line-height: 1.25;
            color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
          }
          .row dd.name { font-size: 3.15mm; font-weight: 800; }
          .row dd.email {
            font-size: 2.05mm; font-weight: 600; color: #334155;
            white-space: normal; word-break: break-all; line-height: 1.2;
            display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
          }
          .qr-panel {
            width: 26.8mm; height: 26.8mm; align-self: center; flex-shrink: 0;
            border: 0.35mm solid #1e293b; border-radius: 1.8mm; padding: 1.2mm;
            display: flex; align-items: center; justify-content: center;
          }
          .qr-code { width: 24.2mm; height: 24.2mm; display: block; }
        </style></head>
        <body>$pagesHtml</body></html>
    """.trimIndent()
}
