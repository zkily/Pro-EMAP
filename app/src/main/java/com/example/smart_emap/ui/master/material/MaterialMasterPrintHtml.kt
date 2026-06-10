package com.example.smart_emap.ui.master.material

import android.graphics.Bitmap
import android.util.Base64
import com.example.smart_emap.data.model.MasterMaterialDto
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
import kotlin.math.round

data class MaterialQrPrintItem(
    val dataUrl: String,
    val materialCd: String,
    val materialName: String,
)

data class MaterialPrintColumnOption(
    val key: String,
    val label: String,
    val width: String,
    val visible: Boolean,
)

data class MaterialPrintSettings(
    val columns: Map<String, Boolean> = defaultMaterialPrintColumns(),
    val sortBy: String = "supplier_name",
    val sortOrderAsc: Boolean = true,
    val showHeader: Boolean = true,
    val showStats: Boolean = true,
)

fun defaultMaterialPrintColumns(): Map<String, Boolean> = mapOf(
    "material_cd" to true,
    "material_name" to true,
    "material_type" to true,
    "standard_spec" to true,
    "unit" to true,
    "supply_classification" to true,
    "usegae" to true,
    "supplier_name" to true,
    "storage_location" to true,
    "status" to true,
    "note" to true,
)

private val jaCollator: Collator = Collator.getInstance(Locale.JAPANESE)

private fun escapeHtml(value: String?): String =
    (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

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

fun buildMaterialQrPrintItems(materials: List<MasterMaterialDto>): List<MaterialQrPrintItem> {
    val sorted = materials
        .asSequence()
        .filter { !it.materialCd.isNullOrBlank() }
        .sortedWith(compareBy(jaCollator) { it.materialName.orEmpty() })
        .toList()
    return sorted.mapNotNull { material ->
        val cd = material.materialCd.orEmpty()
        val dataUrl = generateQrCodeDataUrl(cd) ?: return@mapNotNull null
        MaterialQrPrintItem(
            dataUrl = dataUrl,
            materialCd = cd,
            materialName = material.materialName.orEmpty(),
        )
    }
}

fun buildMaterialQrPrintHtml(qrCodes: List<MaterialQrPrintItem>): String {
    val qrCodesPerRow = 5
    val qrCodesPerPage = 40
    val totalPages = if (qrCodes.isEmpty()) 0 else ceil(qrCodes.size.toDouble() / qrCodesPerPage).toInt()

    val pagesHtml = buildString {
        for (page in 0 until totalPages) {
            val startIndex = page * qrCodesPerPage
            if (startIndex >= qrCodes.size) break
            val endIndex = min(startIndex + qrCodesPerPage, qrCodes.size)
            val pageQRCodes = qrCodes.subList(startIndex, endIndex)
            if (pageQRCodes.isEmpty()) break

            append("""<div class="page"><div class="page-title">材料マスタQR</div><div class="qr-grid">""")
            pageQRCodes.forEachIndexed { index, item ->
                val col = index % qrCodesPerRow + 1
                val row = index / qrCodesPerRow + 1
                val nameHtml = if (item.materialName.isNotBlank()) {
                    """<div class="qr-material-name">${escapeHtml(item.materialName)}</div>"""
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
        <html>
        <head>
          <meta charset="UTF-8">
          <title>材料QRコード印刷</title>
          <style>
            @page { size: A4 portrait; margin: 0; }
            body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
            .page {
              width: 210mm; height: 297mm; padding: 12mm; margin: 0;
              box-sizing: border-box; display: flex; flex-direction: column;
            }
            .page:not(:last-child) { page-break-after: always; }
            .page:last-child { page-break-after: avoid; }
            .page-title {
              text-align: center; font-size: 18px; font-weight: bold;
              margin-bottom: 8mm; color: #333; flex-shrink: 0;
            }
            .qr-grid {
              display: grid; grid-template-columns: repeat($qrCodesPerRow, 1fr);
              grid-template-rows: repeat(8, 1fr); gap: 1.2mm;
              width: 100%; height: 100%; align-content: start;
            }
            .qr-item {
              display: flex; flex-direction: column; align-items: center; justify-content: center;
              padding: 1mm; border: 1px solid #ddd; border-radius: 2px;
              page-break-inside: avoid; box-sizing: border-box;
            }
            .qr-code { width: 70px; height: 70px; margin-bottom: 2px; flex-shrink: 0; }
            .qr-material-name {
              font-size: 12px; font-weight: bold; text-align: center; color: #000;
              word-break: break-all; line-height: 1.3; margin-top: 2px; padding: 0 2px;
            }
            @media print {
              body { margin: 0; padding: 0; }
              .page { margin: 0; padding: 12mm; }
            }
          </style>
        </head>
        <body>$pagesHtml</body>
        </html>
    """.trimIndent()
}

private val printColumnDefs = listOf(
    MaterialPrintColumnOption("index", "No", "40px", true),
    MaterialPrintColumnOption("material_cd", "材料CD", "80px", true),
    MaterialPrintColumnOption("material_name", "材料名", "120px", true),
    MaterialPrintColumnOption("material_type", "種類", "60px", true),
    MaterialPrintColumnOption("standard_spec", "規格", "100px", true),
    MaterialPrintColumnOption("unit", "単位", "50px", true),
    MaterialPrintColumnOption("diameter", "直径", "60px", false),
    MaterialPrintColumnOption("thickness", "厚さ", "60px", false),
    MaterialPrintColumnOption("length", "長さ", "60px", false),
    MaterialPrintColumnOption("supply_classification", "支給区分", "80px", true),
    MaterialPrintColumnOption("pieces_per_bundle", "束本数", "70px", false),
    MaterialPrintColumnOption("usegae", "用途", "60px", true),
    MaterialPrintColumnOption("supplier_cd", "仕入先CD", "90px", false),
    MaterialPrintColumnOption("supplier_name", "仕入先名", "100px", true),
    MaterialPrintColumnOption("unit_price", "単重単価", "80px", false),
    MaterialPrintColumnOption("long_weight", "長尺単重", "80px", false),
    MaterialPrintColumnOption("single_price", "一本単価", "80px", false),
    MaterialPrintColumnOption("safety_stock", "安全在庫", "80px", false),
    MaterialPrintColumnOption("lead_time", "リードタイム", "80px", false),
    MaterialPrintColumnOption("storage_location", "保管場所", "80px", true),
    MaterialPrintColumnOption("status", "状態", "50px", true),
    MaterialPrintColumnOption("note", "備考", "100px", true),
)

fun buildMaterialListPrintHtml(
    materials: List<MasterMaterialDto>,
    settings: MaterialPrintSettings,
): String {
    if (materials.isEmpty()) return ""

    val generatedAt = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
    val activeCount = materials.count { it.status == 1 }

    val sorted = materials.sortedWith { a, b ->
        val av = printSortValue(a, settings.sortBy)
        val bv = printSortValue(b, settings.sortBy)
        val cmp = when {
            av is Number && bv is Number -> av.toDouble().compareTo(bv.toDouble())
            else -> jaCollator.compare(av.toString(), bv.toString())
        }
        if (settings.sortOrderAsc) cmp else -cmp
    }

    val visibleColumns = printColumnDefs.filter { col ->
        when (col.key) {
            "index" -> true
            else -> settings.columns[col.key] ?: col.visible
        }
    }

    val headerHtml = if (settings.showHeader) {
        """
        <div class="print-header">
          <div class="print-title">材料マスタ一覧</div>
          <div class="print-subtitle">材料の登録・編集・仕入先管理</div>
        </div>
        <div class="print-info"><div>印刷日時: ${escapeHtml(generatedAt)}</div></div>
        """
    } else ""

    val statsHtml = if (settings.showStats) {
        """
        <div class="print-stats">
          <div class="stat-item">総件数: ${materials.size}件</div>
          <div class="stat-item">有効件数: ${activeCount}件</div>
          <div class="stat-item">無効件数: ${materials.size - activeCount}件</div>
        </div>
        """
    } else ""

    val rowsHtml = sorted.mapIndexed { index, item ->
        val cells = visibleColumns.joinToString("") { col ->
            val value = when (col.key) {
                "index" -> (index + 1).toString()
                "status" -> if (item.status == 1) "有効" else "無効"
                else -> printCellValue(item, col.key)
            }
            """<td>${escapeHtml(value)}</td>"""
        }
        "<tr>$cells</tr>"
    }.joinToString("\n")

    val thHtml = visibleColumns.joinToString("") { col ->
        """<th style="width: ${col.width};">${escapeHtml(col.label)}</th>"""
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>材料マスタ一覧</title>
          <style>
            body {
              font-family: 'Yu Gothic UI', 'Meiryo', sans-serif;
              font-size: 12px; line-height: 1.4; margin: 20px; color: #333;
            }
            .print-header {
              text-align: center; margin-bottom: 30px;
              border-bottom: 2px solid #333; padding-bottom: 15px;
            }
            .print-title { font-size: 24px; font-weight: bold; margin-bottom: 10px; }
            .print-subtitle { font-size: 14px; color: #666; }
            .print-info { margin-bottom: 20px; font-size: 11px; }
            .print-stats { display: flex; gap: 20px; margin-bottom: 20px; }
            .stat-item { background: #f5f5f5; padding: 8px 12px; border-radius: 4px; font-size: 11px; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; font-size: 10px; }
            th { background-color: #f8f9fa; font-weight: bold; text-align: center; }
            tr:nth-child(even) { background-color: #f9f9f9; }
            @media print {
              body { margin: 0; }
              .print-header { page-break-after: avoid; }
              tr { page-break-inside: avoid; }
            }
          </style>
        </head>
        <body>
          $headerHtml
          $statsHtml
          <table>
            <thead><tr>$thHtml</tr></thead>
            <tbody>$rowsHtml</tbody>
          </table>
        </body>
        </html>
    """.trimIndent()
}

private fun printSortValue(item: MasterMaterialDto, key: String): Comparable<*> = when (key) {
    "material_cd" -> item.materialCd.orEmpty()
    "material_name" -> item.materialName.orEmpty()
    "material_type" -> item.materialType.orEmpty()
    "supplier_name" -> item.supplierName.orEmpty()
    "unit_price" -> item.unitPrice ?: 0.0
    "single_price" -> item.singlePrice ?: 0.0
    "safety_stock" -> item.safetyStock ?: 0
    "lead_time" -> item.leadTime ?: 0
    "status" -> item.status ?: 0
    else -> ""
}

private fun printCellValue(item: MasterMaterialDto, key: String): String = when (key) {
    "material_cd" -> item.materialCd.orEmpty()
    "material_name" -> item.materialName.orEmpty()
    "material_type" -> item.materialType.orEmpty()
    "standard_spec" -> item.standardSpec.orEmpty()
    "unit" -> item.unit.orEmpty()
    "diameter" -> item.diameter?.toString().orEmpty()
    "thickness" -> item.thickness?.toString().orEmpty()
    "length" -> item.length?.toString().orEmpty()
    "supply_classification" -> item.supplyClassification.orEmpty()
    "pieces_per_bundle" -> item.piecesPerBundle?.toString().orEmpty()
    "usegae" -> item.usegae.orEmpty()
    "supplier_cd" -> item.supplierCd.orEmpty()
    "supplier_name" -> item.supplierName.orEmpty()
    "unit_price" -> item.unitPrice?.toString().orEmpty()
    "long_weight" -> item.longWeight?.toString().orEmpty()
    "single_price" -> item.singlePrice?.toString().orEmpty()
    "safety_stock" -> item.safetyStock?.toString().orEmpty()
    "lead_time" -> item.leadTime?.toString().orEmpty()
    "storage_location" -> item.storageLocation.orEmpty()
    "note" -> item.note.orEmpty()
    else -> ""
}.ifBlank { "-" }

fun calcMaterialSinglePrice(
    diameter: Double,
    thickness: Double,
    length: Double,
    unitPrice: Double,
): Pair<Double, Double> {
    val longWeight = ((diameter - thickness) * thickness * length * 0.02466) / 1000.0
    val singlePrice = longWeight * unitPrice
    val roundedLongWeight = round(longWeight * 1000.0) / 1000.0
    val roundedSinglePrice = round(singlePrice * 100.0) / 100.0
    return roundedLongWeight to roundedSinglePrice
}
