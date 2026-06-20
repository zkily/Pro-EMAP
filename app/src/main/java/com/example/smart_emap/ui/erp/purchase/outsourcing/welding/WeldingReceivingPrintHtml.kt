package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculateWeldingReceivingStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val WELDING_RECEIVING_INSPECTORS = listOf("篠田", "小森", "趙", "竹村", "孫", "東條")

val WELDING_DEFECT_REASONS = listOf(
    "welding_defect" to "溶接不良",
    "spatter" to "スパッタ",
    "deform" to "変形・歪み",
    "dimension" to "寸法不良",
    "other" to "その他",
)

fun defectReasonLabel(code: String?): String =
    WELDING_DEFECT_REASONS.find { it.first == code }?.second ?: code.orEmpty()

fun buildWeldingReceivingPrintHtml(items: List<OutsourcingWeldingReceivingDto>): String {
    val now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
    val printDate = now.format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm", Locale.JAPAN))
    val grouped = linkedMapOf<String, MutableList<OutsourcingWeldingReceivingDto>>()
    items.forEach { row ->
        val status = calculateWeldingReceivingStatus(row)
        grouped.getOrPut(status) { mutableListOf() }.add(row)
    }
    val statusOrder = listOf("未検収", "一部検収", "検収済")
    val sortedGroups = statusOrder.filter { grouped.containsKey(it) }.map { status ->
        status to grouped.getValue(status).sortedWith(
            compareBy<OutsourcingWeldingReceivingDto> { it.receivingDate.orEmpty() }
                .thenBy { it.productName.orEmpty() },
        )
    }
    val rowsHtml = buildString {
        sortedGroups.forEach { (status, groupItems) ->
            val statusClass = when (status) {
                "未検収" -> "status-warning"
                "検収済" -> "status-success"
                else -> "status-info"
            }
            append(
                """
                <tr class="group-header-row" style="background-color:#f0f0f0;font-weight:bold;">
                  <td colspan="8" style="padding:2mm 1mm;font-size:9pt;">
                    <span class="status-tag $statusClass">$status</span>
                    <span style="margin-left:4mm;">(${groupItems.size}件)</span>
                  </td>
                </tr>
                """.trimIndent(),
            )
            groupItems.forEach { row ->
                append(
                    """
                    <tr>
                      <td style="padding:1.5mm 1mm;font-size:8pt;">${row.receivingDate.orEmpty()}</td>
                      <td style="padding:1.5mm 1mm;font-size:8pt;">${row.orderNo.orEmpty()}</td>
                      <td style="padding:1.5mm 1mm;font-size:8pt;">${row.supplierName ?: row.supplierCd.orEmpty()}</td>
                      <td style="padding:1.5mm 1mm;font-size:8pt;">${row.productName.orEmpty()}</td>
                      <td class="text-right" style="padding:1.5mm 1mm;font-size:8pt;">${formatOutsourcingNumber(row.receivingQty)}</td>
                      <td class="text-right" style="padding:1.5mm 1mm;font-size:8pt;">${formatOutsourcingNumber(row.goodQty)}</td>
                      <td class="text-right" style="padding:1.5mm 1mm;font-size:8pt;">${formatOutsourcingNumber(row.defectQty)}</td>
                      <td style="padding:1.5mm 1mm;font-size:8pt;">${row.inspector.orEmpty()}</td>
                    </tr>
                    """.trimIndent(),
                )
            }
        }
    }
    return """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"><title>外注溶接受入一覧</title>
        <style>
          html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          body { font-family: 'Meiryo', 'Yu Gothic', sans-serif; margin: 8mm; font-size: 9pt; color: #303133; }
          h1 { text-align: center; font-size: 16pt; margin: 0 0 4mm; color: #667eea; }
          .meta { text-align: right; font-size: 8pt; color: #909399; margin-bottom: 4mm; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #dcdfe6; }
          th { background: #f5f7fa; padding: 2mm 1mm; font-size: 8pt; font-weight: 600; }
          .text-right { text-align: right; }
          .status-tag { display: inline-block; padding: 1px 6px; border-radius: 3px; font-size: 8pt; }
          .status-warning { background: #fdf6ec; color: #e6a23c; }
          .status-success { background: #f0f9eb; color: #67c23a; }
          .status-info { background: #ecf5ff; color: #409eff; }
        </style></head>
        <body>
          <h1>外注溶接受入一覧</h1>
          <div class="meta">印刷日時: $printDate</div>
          <table>
            <thead>
              <tr>
                <th style="width:12%;">受入日</th>
                <th style="width:14%;">注文番号</th>
                <th style="width:16%;">外注先</th>
                <th style="width:22%;">製品名</th>
                <th style="width:9%;">受入数</th>
                <th style="width:9%;">良品数</th>
                <th style="width:9%;">不良数</th>
                <th style="width:9%;">検収者</th>
              </tr>
            </thead>
            <tbody>$rowsHtml</tbody>
          </table>
        </body></html>
    """.trimIndent()
}
