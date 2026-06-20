package com.example.smart_emap.ui.erp.purchase.outsourcing.plating

import com.example.smart_emap.data.model.OutsourcingPlatingReceivingDto
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculatePlatingReceivingStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val PLATING_RECEIVING_INSPECTORS = listOf("篠田", "小森", "趙", "竹村", "孫", "東條")

val PLATING_DEFECT_REASONS = listOf(
    "plating_defect" to "メッキ不良",
    "scratch" to "傷・打痕",
    "deform" to "変形",
    "dimension" to "寸法不良",
    "other" to "その他",
)

fun defectReasonLabel(code: String?): String =
    PLATING_DEFECT_REASONS.find { it.first == code }?.second ?: code.orEmpty()

fun buildPlatingReceivingPrintHtml(items: List<OutsourcingPlatingReceivingDto>): String {
    val now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
    val printDate = now.format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm", Locale.JAPAN))
    val grouped = linkedMapOf<String, MutableList<OutsourcingPlatingReceivingDto>>()
    items.forEach { row ->
        val status = calculatePlatingReceivingStatus(row)
        grouped.getOrPut(status) { mutableListOf() }.add(row)
    }
    val statusOrder = listOf("未検収", "一部検収", "検収済")
    val sortedGroups = statusOrder.filter { grouped.containsKey(it) }.map { status ->
        status to grouped.getValue(status).sortedWith(
            compareBy<OutsourcingPlatingReceivingDto> { it.receivingDate.orEmpty() }
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
                  </td>
                </tr>
                """.trimIndent(),
            )
            groupItems.forEach { row ->
                append(
                    """
                    <tr>
                      <td class="text-center">${row.receivingDate.orEmpty().ifBlank { "-" }}</td>
                      <td class="text-left">${row.supplierName ?: row.supplierCd.orEmpty().ifBlank { "-" }}</td>
                      <td class="text-left">${row.productName.orEmpty().ifBlank { "-" }}</td>
                      <td class="text-right">${formatOutsourcingNumber(row.orderQty)}</td>
                      <td class="text-right">${formatOutsourcingNumber(row.receivingQty)}</td>
                      <td class="text-right">${formatOutsourcingNumber(row.goodQty)}</td>
                      <td class="text-right">${formatOutsourcingNumber(row.defectQty)}</td>
                      <td><span class="status-tag $statusClass">$status</span></td>
                    </tr>
                    """.trimIndent(),
                )
            }
        }
    }
    return """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8">
        <style>
          @page { size: A4 portrait; margin: 12mm; }
          body { font-family: 'Meiryo','Yu Gothic',sans-serif; font-size: 9pt; line-height: 1.2; margin: 0; padding: 0; }
          .print-header { text-align: center; margin-bottom: 6mm; border-bottom: 1.5px solid #333; padding-bottom: 2mm; }
          .print-title { font-size: 16pt; font-weight: bold; margin-bottom: 1.5mm; }
          .print-date { font-size: 9pt; color: #666; }
          .print-table { width: 100%; border-collapse: collapse; margin-top: 5mm; font-size: 8pt; }
          .print-table th, .print-table td { border: 1px solid #333; padding: 2mm 1mm; }
          .print-table th { background: #f5f5f5; font-weight: bold; text-align: center; }
          .text-left { text-align: left; } .text-right { text-align: right; } .text-center { text-align: center; }
          .status-tag { display: inline-block; padding: 1px 6px; border-radius: 2px; font-size: 7pt; font-weight: 500; }
          .status-warning { background: #fef0c0; color: #e6a23c; }
          .status-success { background: #f0f9ff; color: #67c23a; }
          .status-info { background: #f4f4f5; color: #909399; }
        </style></head><body>
        <div class="print-header">
          <div class="print-title">外注メッキ受入一覧</div>
          <div class="print-date">印刷日時: $printDate</div>
        </div>
        <table class="print-table">
          <thead><tr>
            <th style="width:14%;">受入予定日</th>
            <th style="width:17%;">外注先</th>
            <th style="width:13%;">製品名</th>
            <th style="width:9%;">注文数</th>
            <th style="width:9%;">受入数</th>
            <th style="width:9%;">良品数</th>
            <th style="width:9%;">不良数</th>
            <th style="width:10%;">検収状態</th>
          </tr></thead>
          <tbody>$rowsHtml</tbody>
        </table>
        </body></html>
    """.trimIndent()
}
