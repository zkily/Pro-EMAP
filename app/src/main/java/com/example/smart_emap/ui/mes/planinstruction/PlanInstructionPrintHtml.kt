package com.example.smart_emap.ui.mes.planinstruction

import com.example.smart_emap.data.model.PlanInstructionRecordDto
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val QR_PLACEHOLDER =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjVGNUY1Ii8+Cjx0ZXh0IHg9IjIwIiB5PSIyMCIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjEwIiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+TkE8L3RleHQ+Cjwvc3ZnPgo="

fun escapeHtml(s: String): String = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

fun qrCodeUrl(data: String): String {
    val trimmed = data.trim()
    if (trimmed.isEmpty()) return QR_PLACEHOLDER
    val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.name())
    return "https://api.qrserver.com/v1/create-qr-code/?size=120x120&qzone=2&format=png&data=$encoded"
}

data class PlanInstructionSheetParams(
    val machineName: String,
    val machineCd: String,
    val planRows: List<PlanInstructionRecordDto>,
)

fun buildInstructionSheetHtml(
    config: PlanInstructionConfig,
    machineName: String,
    machineCd: String,
    baseDateIso: String,
    planRows: List<PlanInstructionRecordDto>,
    currentDateDisplay: String,
): String {
    val baseDate = baseDateIso.take(10)
    val productionDateDisplay = PlanInstructionLogic.formatDisplayDate(baseDate)
    val title = escapeHtml(config.instructionPrintTitle)
    val machineLabel = escapeHtml(machineName)
    val machineCdLabel = escapeHtml(machineCd.ifBlank { "未指定" })
    val machineCdQr = machineCd.ifBlank { "N/A" }

    val filteredRows = planRows
        .filter { row ->
            val qty = row.quantity ?: 0
            qty > 0 && !row.productName.isNullOrBlank()
        }
        .filter { row ->
            machineName.isBlank() || row.machineName == machineName
        }
        .sortedWith(
            compareBy<PlanInstructionRecordDto> { it.planDate.orEmpty() }
                .thenBy { it.operator?.toIntOrNull() ?: Int.MAX_VALUE },
        )
        .take(4)

    val tableBody = if (filteredRows.isEmpty()) {
        """
        <tr>
          <td colspan="7" class="no-data-cell">
            <div class="no-data-message">生産計画停止</div>
          </td>
        </tr>
        """.trimIndent()
    } else {
        filteredRows.joinToString("\n") { row ->
            val rowDate = row.planDate?.take(10).orEmpty()
            val isBaseDate = rowDate == baseDate
            val highlightProduct = PlanInstructionLogic.isHighlightProduct(row.productName)
            val rowClass = buildList {
                if (isBaseDate) add("highlighted-row")
                if (highlightProduct) add("product-highlight-row")
            }.joinToString(" ")
            val productCd = row.productCd.orEmpty().ifBlank { "N/A" }
            val remarks = row.remarks?.trim().orEmpty().ifBlank {
                if (highlightProduct) "新聞紙をかける" else ""
            }
            val dateDisplay = PlanInstructionLogic.formatDisplayDate(row.planDate?.take(10))
            """
            <tr class="$rowClass">
              <td class="product-cd-cell">
                <div class="product-qr-container">
                  <img src="${qrCodeUrl(productCd)}" alt="製品CD: ${escapeHtml(productCd)}" class="product-qr-image" />
                </div>
              </td>
              <td>${escapeHtml(dateDisplay)}</td>
              <td>${escapeHtml(row.operator.orEmpty())}</td>
              <td>${escapeHtml(row.productCd.orEmpty())}</td>
              <td>${escapeHtml(row.productName.orEmpty())}</td>
              <td>${escapeHtml(PlanInstructionLogic.formatNumber(row.quantity ?: 0))}</td>
              <td>${escapeHtml(remarks)}</td>
            </tr>
            """.trimIndent()
        }
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>$title - $machineLabel</title>
      <style>
        ${instructionSheetStyles()}
      </style>
    </head>
    <body>
      <div class="print-container instruction-sheet-page">
        <div class="print-header">
          <div class="print-header-top">
            <div class="print-title">$title</div>
            <div class="print-date-section">
              <div class="print-date">生産日 ${escapeHtml(productionDateDisplay)}</div>
              <div class="print-date-sub">集計時間:前日15:00～当日15:00</div>
            </div>
            <div class="print-machine-section">
              ${if (machineName.isNotBlank()) """<div class="print-subtitle">$machineLabel</div>""" else ""}
            </div>
            <div class="print-process">
              <div class="qr-code-container">
                <img src="${qrCodeUrl(machineCdQr)}" alt="設備CD: $machineCdLabel" class="qr-code-image" />
                <div class="qr-code-label">設備CD: $machineCdLabel</div>
              </div>
            </div>
          </div>
          <table class="main-table">
            <thead>
              <tr>
                <th class="product-cd">製品QR</th>
                <th class="plan-date">生産日</th>
                <th class="priority">生産順位</th>
                <th class="product-code">製品CD</th>
                <th class="product-name">生産品種</th>
                <th class="quantity">生産計画数</th>
                <th class="remarks">備考</th>
              </tr>
            </thead>
            <tbody>
              $tableBody
            </tbody>
          </table>
        </div>
        ${shiftTableSection()}
      </div>
    </body>
    </html>
    """.trimIndent()
}

fun buildCombinedInstructionPrintHtml(
    config: PlanInstructionConfig,
    baseDateIso: String,
    currentDateDisplay: String,
    sheets: List<PlanInstructionSheetParams>,
): String {
    if (sheets.isEmpty()) {
        return buildInstructionSheetHtml(
            config = config,
            machineName = "",
            machineCd = "未指定",
            baseDateIso = baseDateIso,
            planRows = emptyList(),
            currentDateDisplay = currentDateDisplay,
        )
    }
    val body = sheets.mapIndexed { index, sheet ->
        val pageClass = if (index < sheets.lastIndex) "instruction-sheet-page page-break" else "instruction-sheet-page"
        val inner = buildInstructionSheetHtml(
            config = config,
            machineName = sheet.machineName,
            machineCd = sheet.machineCd,
            baseDateIso = baseDateIso,
            planRows = sheet.planRows,
            currentDateDisplay = currentDateDisplay,
        )
        val content = inner.substringAfter("<body>").substringBeforeLast("</body>").trim()
        content.replace("instruction-sheet-page", pageClass)
    }.joinToString("\n")

    val title = escapeHtml(config.instructionPrintTitle)
    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>$title</title>
      <style>
        ${instructionSheetStyles()}
        .page-break { page-break-after: always; }
      </style>
    </head>
    <body>
      $body
    </body>
    </html>
    """.trimIndent()
}

private fun instructionSheetStyles(): String = """
  @page {
    size: A5 landscape;
    margin: 8mm;
    marks: none;
    bleed: 0mm;
    page-break-after: auto;
  }
  @media print {
    @page {
      size: A5 landscape;
      margin: 8mm;
      marks: none;
      bleed: 0mm;
    }
    * {
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
    html, body {
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
  }
  body {
    font-family: '遊ゴシック', BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    font-size: 12px;
    line-height: 1.2;
    margin: 0;
    padding: 0;
    color: #000;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .print-container {
    width: 100%;
    height: 100%;
  }
  .instruction-sheet-page {
    width: 100%;
    box-sizing: border-box;
  }
  .print-header {
    display: block;
    margin-bottom: 15px;
    padding-bottom: 5px;
    position: relative;
  }
  .print-header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  .print-title {
    font-size: 20px;
    font-weight: bold;
    color: #000;
    flex: 0 0 auto;
  }
  .print-date-section {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    text-align: center;
    font-size: 20px;
  }
  .print-date {
    font-weight: bold;
    font-size: 20px;
  }
  .print-date-sub {
    font-size: 10px;
    color: #000;
    margin-top: 2px;
  }
  .print-machine-section {
    text-align: center;
    font-size: 20px;
    flex: 0 0 auto;
    margin-left: auto;
    margin-right: 20px;
  }
  .print-subtitle {
    font-size: 20px;
    font-weight: bold;
    color: #000;
  }
  .print-process {
    font-size: 20px;
    color: #000;
    flex: 0 0 auto;
  }
  .qr-code-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
  }
  .qr-code-image {
    width: 33px;
    height: 33px;
  }
  .qr-code-label {
    font-size: 10px;
    color: #000;
    text-align: center;
  }
  .main-table {
    width: 100%;
    border-collapse: collapse;
    border: none;
  }
  .main-table th,
  .main-table td {
    border: 0.8px solid #000;
    padding: 2px 3px;
    text-align: center;
    vertical-align: middle;
  }
  .main-table th {
    background-color: #f0f0f0;
    font-weight: bold;
    font-size: 10px;
    height: 12px;
  }
  .main-table td {
    font-size: 11px;
    height: 10px;
  }
  .main-table .product-cd { width: 10%; }
  .main-table .plan-date { width: 12%; }
  .main-table .priority { width: 8%; }
  .main-table .product-name { width: 18%; }
  .main-table .quantity { width: 12%; }
  .main-table .product-code { width: 12%; }
  .main-table .remarks { width: 20%; }
  .product-cd-cell {
    text-align: center;
    vertical-align: middle;
  }
  .product-qr-container {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .product-qr-image {
    width: 25px;
    height: 25px;
    border: none;
  }
  .shift-table-container {
    position: fixed;
    bottom: 5px;
    left: 0;
    right: 0;
    width: 100%;
    page-break-inside: avoid;
    padding-top: 5px;
  }
  .section-divider {
    position: relative;
    text-align: center;
    margin-bottom: 5px;
  }
  .section-divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 0.5px;
    background: #000;
    z-index: 1;
  }
  .section-title {
    background: white;
    padding: 0 10px;
    font-size: 10px;
    font-weight: bold;
    color: #000;
    position: relative;
    z-index: 2;
  }
  .shift-table {
    width: 100%;
    border-collapse: collapse;
    border: 0.5px solid #000;
    font-size: 9px;
  }
  .shift-table th,
  .shift-table td {
    border: 0.5px solid #000;
    padding: 2px 3px;
    text-align: center;
    vertical-align: middle;
  }
  .shift-table th {
    background-color: #f0f0f0;
    font-weight: normal;
    font-size: 11px;
    height: 25px;
  }
  .shift-table td {
    font-size: 11px;
    height: 30px;
  }
  .shift-table .shift-time { width: 12%; }
  .shift-table .product-name { width: 12%; }
  .shift-table .production-qty { width: 17%; }
  .shift-table .recorder { width: 9%; }
  .shift-table .remarks { width: 10%; }
  .shift-table .notch { width: 8%; }
  .shift-table .bending { width: 8%; }
  .shift-table .chamfering { width: 8%; }
  .shift-table .setup { width: 8%; }
  .shift-table .yellow-box { width: 8%; }
  .time-ranges-row {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 2px;
    padding: 2px 0;
    font-size: 9px;
    flex-wrap: nowrap;
  }
  .time-ranges-row span {
    flex: 0 0 auto;
    text-align: center;
    padding: 1px 2px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2px;
    white-space: nowrap;
  }
  .time-text { font-size: 9px; }
  .checkbox {
    font-size: 12px;
    font-weight: bold;
    line-height: 1;
  }
  .highlighted-row {
    background-color: #f5f5f5;
  }
  .highlighted-row td {
    color: #dc3545;
    font-weight: bold;
  }
  .product-highlight-row td {
    background-color: #fffde7 !important;
  }
  .no-data-cell {
    text-align: center;
    vertical-align: middle;
    height: 120px;
    background-color: #f8f9fa;
  }
  .no-data-message {
    font-size: 24px;
    font-weight: bold;
    color: #6c757d;
    text-align: center;
    margin: 0;
    padding: 20px;
  }
""".trimIndent()

private fun shiftTableSection(): String {
    val shiftRows = listOf(
        "15:00--17:00",
        "17:00--19:00",
        "19:00--21:00",
        "21:00--06:00",
        "06:00--08:00",
        "08:00--15:00",
    )
    val shiftBody = shiftRows.joinToString("\n") { slot ->
        """
        <tr>
          <td>$slot</td>
          <td></td>
          <td>---</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
        """.trimIndent()
    }
    val timeRanges = listOf(
        "15:00~15:10",
        "17:00~17:10",
        "21:00~21:10",
        "23:00~23:10",
        "01:00~02:00",
        "04:00~04:10",
        "06:00~06:10",
        "10:00~10:10",
        "12:00~13:00",
    )
    val timeRangeHtml = timeRanges.joinToString("\n") { range ->
        """<span><span class="time-text">$range</span> <span class="checkbox">☐</span></span>"""
    }
    return """
    <div class="shift-table-container">
      <div class="section-divider">
        <span class="section-title">記入項目</span>
      </div>
      <table class="shift-table">
        <thead>
          <tr>
            <th class="shift-time">勤務時間帯</th>
            <th class="product-name">製品名</th>
            <th class="production-qty">生産数</th>
            <th class="recorder">記入者</th>
            <th class="remarks">備考</th>
            <th class="notch">切欠き</th>
            <th class="bending">曲げ</th>
            <th class="chamfering">面取</th>
            <th class="setup">段取調整</th>
            <th class="yellow-box">黄箱</th>
          </tr>
        </thead>
        <tbody>
          $shiftBody
        </tbody>
      </table>
      <div class="time-ranges-row">
        $timeRangeHtml
      </div>
    </div>
    """.trimIndent()
}
