package com.example.smart_emap.ui.mes.planinstruction

import com.example.smart_emap.data.model.PlanInstructionRecordDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class SetupScheduleRow(
    val totalPlanQuantity: Int? = null,
    val actualProduction: Int? = null,
    val remainingProduction: Int? = null,
    val line: String = "",
    val plannedWorkingHours: String = "",
    val operationVariance: String = "",
    val operator: String = "",
    val productName: String = "",
    val efficiency: String = "",
    val planQuantity: Int? = null,
    val setupAfterHours: String = "",
    val setupPredictedTime: String = "",
    val nextProductName: String = "",
    val nextQuantity: Int? = null,
    val remarks: String = "",
    val machineCd: String = "",
    val workTime: Int? = null,
    val requiredStaffCount: Int = 0,
)

data class SetupScheduleData(
    val tableRows: List<SetupScheduleRow>,
    val productionDate: String,
    val totalQuantity: Int,
    val currentDateTime: String,
)

private val japanZone = ZoneId.of("Asia/Tokyo")
private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
private val dateTimeDisplay = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)

fun currentSetupScheduleDateTime(): String =
    LocalDateTime.now(japanZone).format(dateTimeDisplay)

suspend fun generateSetupScheduleData(
    planData: List<PlanInstructionRecordDto>,
    productionDateIso: String,
    efficiencyCache: Map<String, Double>,
    operationVarianceByMachine: suspend (String) -> String?,
    plannedWorkingHoursByMachine: suspend (String) -> Double?,
    kind: PlanInstructionKind = PlanInstructionKind.FORMING,
): SetupScheduleData {
    val filterDate = normalizePlanDate(productionDateIso)
    val productionDate = PlanInstructionLogic.formatDisplayDate(filterDate)
    val currentDateTime = currentSetupScheduleDateTime()
    val grouped = planData.groupBy { it.machineName.orEmpty().trim() }
    val machines = grouped.keys.filter { it.isNotBlank() }.sorted()

    val scheduleMap = buildMesScheduleMap(planData)
    val totalQuantity = planData.sumOf { row ->
        val date = normalizePlanDate(row.planDate)
        if (date == filterDate) row.quantity ?: 0 else 0
    }

    val tableRows = machines.map { machineName ->
        buildSetupScheduleRow(
            machineName = machineName,
            machineData = grouped.getValue(machineName),
            filterDate = filterDate,
            scheduleMap = scheduleMap,
            efficiencyCache = efficiencyCache,
            operationVariance = operationVarianceByMachine(machineName),
            plannedWorkingHours = plannedWorkingHoursByMachine(machineName),
            kind = kind,
        )
    }

    return SetupScheduleData(
        tableRows = tableRows,
        productionDate = productionDate,
        totalQuantity = totalQuantity,
        currentDateTime = currentDateTime,
    )
}

private fun buildMesScheduleMap(planData: List<PlanInstructionRecordDto>): Map<String, MesScheduleAggregate> {
    val map = mutableMapOf<String, MesScheduleAggregate>()
    val dedup = mutableMapOf<String, MutableSet<String>>()
    planData.forEach { item ->
        val productionOrder = (item.operator ?: "").trim()
        val key = "${item.machineName.orEmpty().trim()}|${item.productName.orEmpty().trim()}|$productionOrder"
        val plannedQuantity = item.plannedOutputQty ?: item.quantity ?: 0
        val actualProduction = item.actualProduction ?: item.actualQty ?: 0
        val scheduleId = item.scheduleId.orEmpty().trim()
        if (map.containsKey(key)) {
            val existing = map.getValue(key)
            val seen = dedup.getOrPut(key) { mutableSetOf() }
            if (scheduleId.isNotEmpty() && !seen.contains(scheduleId)) {
                existing.plannedQuantity += plannedQuantity
                seen += scheduleId
            } else if (scheduleId.isEmpty()) {
                existing.plannedQuantity += plannedQuantity
            }
            existing.actualProduction += actualProduction
        } else {
            map[key] = MesScheduleAggregate(
                plannedQuantity = plannedQuantity,
                actualProduction = actualProduction,
            )
            if (scheduleId.isNotEmpty()) {
                dedup[key] = mutableSetOf(scheduleId)
            }
        }
    }
    return map
}

private data class MesScheduleAggregate(
    var plannedQuantity: Int = 0,
    var actualProduction: Int = 0,
)

private fun buildSetupScheduleRow(
    machineName: String,
    machineData: List<PlanInstructionRecordDto>,
    filterDate: String,
    scheduleMap: Map<String, MesScheduleAggregate>,
    efficiencyCache: Map<String, Double>,
    operationVariance: String?,
    plannedWorkingHours: Double?,
    kind: PlanInstructionKind,
): SetupScheduleRow {
    val dateIndex = buildDateIndex(machineData)
    val currentProducts = findValidProducts(dateIndex, filterDate)
    val currentProduct = currentProducts.firstOrNull()
    val machineCdFromData = currentProduct?.machineCd.orEmpty()

    val currentQuantity = if (currentProduct != null &&
        normalizePlanDate(currentProduct.planDate) == filterDate
    ) {
        currentProduct.quantity ?: 0
    } else {
        0
    }

    val isProductionStop = currentProduct == null ||
        currentQuantity == 0 ||
        currentProduct.productName.isNullOrBlank()

    val currentProductName = if (isProductionStop) "" else currentProduct?.productName.orEmpty().trim()
    val workTime = if (currentQuantity > 0) {
        minOf(24, maxOf(1, currentQuantity / 200))
    } else {
        0
    }

    var efficiencyRateNum: Double? = null
    var efficiency = ""
    if (!isProductionStop && currentProductName.isNotEmpty()) {
        val rowRate = currentProduct?.efficiencyRate
        if (rowRate != null && rowRate > 0) {
            efficiencyRateNum = rowRate
            efficiency = PlanInstructionLogic.formatEfficiency(rowRate)
        } else {
            val cached = efficiencyCache["$machineName|$currentProductName"]
            if (cached != null && cached > 0) {
                efficiencyRateNum = cached
                efficiency = PlanInstructionLogic.formatEfficiency(cached)
            } else if (currentQuantity > 0 && workTime > 0) {
                efficiency = (currentQuantity / workTime).toString()
            } else if (currentQuantity > 0) {
                efficiency = (currentQuantity / 8).toString()
            }
        }
    }

    val nextResult = findNextProduct(dateIndex, filterDate, currentProducts, currentProduct)
    val nextProduct = nextResult.first
    val nextValidDate = nextResult.second
    val nextProductName = nextProduct?.productName.orEmpty().trim()
    val nextQuantity = nextProduct?.quantity ?: 0

    var setupAfterHours = ""
    if (nextProduct != null && !isProductionStop && efficiencyRateNum != null && efficiencyRateNum > 0 && currentQuantity > 0) {
        val hours = currentQuantity / efficiencyRateNum
        if (hours > 0 && hours <= 1000) {
            setupAfterHours = String.format(Locale.US, "%.1f", hours)
        }
    }

    val isSameProduct = currentProductName.isNotEmpty() &&
        nextProductName.isNotEmpty() &&
        currentProductName == nextProductName

    val nextProductDateStr = nextProduct?.planDate?.let { normalizePlanDate(it) }
        ?: nextValidDate
    val isNextDateSunday = nextProductDateStr?.let { parseLocalDate(it)?.dayOfWeek == DayOfWeek.SUNDAY } == true
    val isNextDateWeekend = nextProductDateStr?.let {
        val d = parseLocalDate(it)?.dayOfWeek
        d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY
    } == true

    val finalSetupAfterHours = if (isSameProduct || isNextDateSunday) "" else setupAfterHours

    val finalNextProductName = when {
        isSameProduct || isNextDateSunday -> ""
        isNextDateWeekend -> nextProductName.ifBlank { "生产停止" }
        nextProductName.isBlank() -> "生产停止"
        else -> nextProductName
    }

    val finalNextQuantity = when {
        isSameProduct || isNextDateSunday -> null
        nextProduct != null && normalizePlanDate(nextProduct.planDate) == filterDate -> nextQuantity
        nextValidDate == filterDate -> nextQuantity
        else -> null
    }

    val normalizedCurrentProductName = currentProductName.trim()
    val aggregatedPlanQuantity = if (!isProductionStop && normalizedCurrentProductName.isNotEmpty()) {
        currentProducts
            .filter { it.productName.orEmpty().trim() == normalizedCurrentProductName }
            .sumOf { it.quantity ?: 0 }
    } else {
        0
    }

    var totalPlanQuantity: Int? = null
    var actualProduction: Int? = null
    var remainingProduction: Int? = null
    if (!isProductionStop && currentProductName.isNotEmpty()) {
        val operatorNum = operatorAsNumber(currentProduct?.operator)
        val scheduleKey = "$machineName|$currentProductName|$operatorNum"
        val scheduleData = scheduleMap[scheduleKey]
        if (scheduleData != null) {
            totalPlanQuantity = scheduleData.plannedQuantity
            actualProduction = scheduleData.actualProduction
            remainingProduction = max(0, scheduleData.plannedQuantity - scheduleData.actualProduction)
        }
    }

    val staffInfo = buildWeldingStaffRemarks(finalSetupAfterHours, kind)
    var remarks = staffInfo.first
    val requiredStaffCount = staffInfo.second
    if (remarks.isEmpty() && currentProducts.size >= 3) {
        val third = currentProducts[2].productName.orEmpty().trim()
        if (third.isNotEmpty()) remarks = "次生産品種：$third"
    }

    val operationVarianceText = operationVariance.orEmpty().trim().let { raw ->
        if (raw.isEmpty()) return@let ""
        raw.toDoubleOrNull()?.roundToInt()?.toString() ?: raw
    }

    val plannedHoursText = plannedWorkingHours
        ?.takeIf { it.isFinite() && it != 0.0 }
        ?.let { String.format(Locale.US, "%.1f", it) }
        .orEmpty()

    return SetupScheduleRow(
        workTime = workTime.takeIf { it > 0 },
        machineCd = machineCdFromData,
        line = machineName,
        plannedWorkingHours = plannedHoursText,
        operationVariance = operationVarianceText,
        operator = if (isProductionStop) "" else currentProduct?.operator.orEmpty().trim(),
        productName = if (isProductionStop) "生産停止" else currentProductName,
        totalPlanQuantity = if (isProductionStop) null else totalPlanQuantity,
        actualProduction = if (isProductionStop) null else actualProduction,
        remainingProduction = if (isProductionStop) null else remainingProduction,
        efficiency = if (isProductionStop) "" else efficiency,
        planQuantity = if (isProductionStop) null else aggregatedPlanQuantity,
        setupAfterHours = finalSetupAfterHours,
        nextProductName = finalNextProductName,
        nextQuantity = finalNextQuantity,
        remarks = remarks,
        requiredStaffCount = requiredStaffCount,
    )
}

private fun buildWeldingStaffRemarks(setupAfterHours: String, kind: PlanInstructionKind): Pair<String, Int> {
    if (kind != PlanInstructionKind.WELDING || setupAfterHours.isBlank()) return "" to 0
    val hours = setupAfterHours.toDoubleOrNull() ?: return "" to 0
    return when {
        hours < 8 -> "一人体制" to 1
        hours <= 10 -> "一人残業体制" to 1
        else -> "二人体制" to 2
    }
}

private fun normalizePlanDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return raw.take(10).replace('/', '-')
}

private fun parseLocalDate(iso: String): LocalDate? =
    runCatching { LocalDate.parse(iso.take(10), isoDate) }.getOrNull()

private fun buildDateIndex(rows: List<PlanInstructionRecordDto>): Map<String, List<PlanInstructionRecordDto>> =
    rows
        .filter { (it.quantity ?: 0) > 0 && !it.productName.isNullOrBlank() }
        .groupBy { normalizePlanDate(it.planDate) }
        .filterKeys { it.isNotBlank() }

private fun findValidProducts(
    dateIndex: Map<String, List<PlanInstructionRecordDto>>,
    date: String,
): List<PlanInstructionRecordDto> =
    dateIndex[date].orEmpty().sortedBy { it.operator?.toIntOrNull() ?: Int.MAX_VALUE }

private fun findNextProduct(
    dateIndex: Map<String, List<PlanInstructionRecordDto>>,
    filterDate: String,
    currentProducts: List<PlanInstructionRecordDto>,
    currentProduct: PlanInstructionRecordDto?,
): Pair<PlanInstructionRecordDto?, String> {
    if (currentProducts.size > 1 && currentProduct != null) {
        val index = currentProducts.indexOfFirst {
            it.productName == currentProduct.productName && it.operator == currentProduct.operator
        }
        if (index >= 0 && index < currentProducts.lastIndex) {
            return currentProducts[index + 1] to filterDate
        }
    }

    val base = parseLocalDate(filterDate) ?: return null to ""
    val nextDate = base.plusDays(1)
    val nextDateStr = nextDate.format(isoDate)

    return when (nextDate.dayOfWeek) {
        DayOfWeek.SATURDAY -> {
            val saturdayProducts = findValidProducts(dateIndex, nextDateStr)
            if (saturdayProducts.isNotEmpty()) {
                saturdayProducts.first() to nextDateStr
            } else {
                val mondayStr = nextDate.plusDays(2).format(isoDate)
                val mondayProducts = findValidProducts(dateIndex, mondayStr)
                if (mondayProducts.isNotEmpty()) mondayProducts.first() to mondayStr else null to ""
            }
        }
        DayOfWeek.SUNDAY -> {
            val mondayStr = nextDate.plusDays(1).format(isoDate)
            val mondayProducts = findValidProducts(dateIndex, mondayStr)
            if (mondayProducts.isNotEmpty()) mondayProducts.first() to mondayStr else null to ""
        }
        else -> {
            val nextDayProducts = findValidProducts(dateIndex, nextDateStr)
            if (nextDayProducts.isNotEmpty()) nextDayProducts.first() to nextDateStr else null to ""
        }
    }
}

private fun operatorAsNumber(operator: String?): String {
    val raw = operator.orEmpty().trim()
    if (raw.isEmpty()) return ""
    raw.toIntOrNull()?.let { return it.toString() }
    return Regex("\\d+").find(raw)?.value.orEmpty()
}

fun buildSetupSchedulePrintHtml(
    config: PlanInstructionConfig,
    data: SetupScheduleData,
): String {
    val title = escapeHtml(config.setupSchedulePrintTitle)
    val productionDate = escapeHtml(data.productionDate)
    val currentDateTime = escapeHtml(data.currentDateTime)
    val referenceDate = escapeHtml(data.currentDateTime.substringBefore(' '))
    val totalQuantity = PlanInstructionLogic.formatNumber(data.totalQuantity)

    val tableBody = data.tableRows.joinToString("\n") { row ->
        val plannedHoursClass = plannedHoursClass(row.plannedWorkingHours)
        val plannedHoursValue = row.plannedWorkingHours.toDoubleOrNull()
            ?.takeIf { it != 0.0 }
            ?.let { String.format(Locale.US, "%.1f", it) }
            .orEmpty()
        val opClass = operationVarianceClass(row.operationVariance)
        val opValue = row.operationVariance
        val arrow = if (row.nextProductName.isNotBlank()) "&rarr;" else ""
        val totalPlan = row.totalPlanQuantity?.let { PlanInstructionLogic.formatNumber(it) }.orEmpty()
        val planQty = row.planQuantity?.let { PlanInstructionLogic.formatNumber(it) }.orEmpty()
        val nextQty = row.nextQuantity?.let { PlanInstructionLogic.formatNumber(it) }.orEmpty()

        """
        <tr>
          <td class="numeric-cell bold-border-col">$totalPlan</td>
          <td class="blank-col"> </td>
          <td class="line-col">${escapeHtml(row.line)}</td>
          <td class="$plannedHoursClass">$plannedHoursValue</td>
          <td class="op-progress-col $opClass">$opValue</td>
          <td>${escapeHtml(row.productName)}</td>
          <td class="numeric-cell">${escapeHtml(row.efficiency)}</td>
          <td class="numeric-cell plan-quantity-cell">$planQty</td>
          <td class="numeric-cell">${escapeHtml(row.setupAfterHours)}</td>
          <td class="next-arrow-cell">$arrow</td>
          <td>${escapeHtml(row.nextProductName)}</td>
          <td class="numeric-cell">$nextQty</td>
          <td>${escapeHtml(row.remarks)}</td>
        </tr>
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>$title</title>
      <style>
        ${setupScheduleStyles()}
      </style>
    </head>
    <body>
      <div class="print-container">
        <div class="print-header">
          <div class="header-left">
            <div class="print-title">$title</div>
          </div>
          <div class="print-center-section">
            <div class="print-production-date-wrapper">
              <div class="print-production-date">生産日: $productionDate</div>
              <div class="print-aggregation-time">集計時間:前日15:00~当日15:00</div>
            </div>
          </div>
          <div class="header-right">
            <div class="print-date-time">$currentDateTime</div>
            <div class="print-total">生産計画合計数 $totalQuantity</div>
          </div>
        </div>
        <div class="table-wrapper">
          <table class="main-table">
            <thead>
              <tr>
                <th colspan="1" class="bold-border-col" style="width: 8%; border-bottom: none;"><span class="reference-date-red">$referenceDate</span>までの実績(算出)</th>
                <th rowspan="2" class="blank-col" style="width: 3%;"> </th>
                <th rowspan="2" class="line-col" style="width: 7%;">ライン</th>
                <th rowspan="2" style="width: 8%;">予定稼働(H)</th>
                <th rowspan="2" style="width: 7%;">操業度(進捗)</th>
                <th rowspan="2" style="width: 12%;">生産品種</th>
                <th rowspan="2" style="width: 7%;">能率(本/h)</th>
                <th rowspan="2" class="plan-quantity-header" style="width: 7%;">当日計画数</th>
                <th rowspan="2" style="width: 6%;">残生産時間</th>
                <th rowspan="2" class="blank-col" style="width: 6%;"> </th>
                <th rowspan="2" style="width: 9%;">次生産品種</th>
                <th rowspan="2" style="width: 8%;">次品種計画数</th>
                <th rowspan="2" style="width: 15%;">備考</th>
              </tr>
              <tr>
                <th class="bold-border-col reference-col" style="width: 8%;">生産残数(参考)</th>
              </tr>
            </thead>
            <tbody>
              $tableBody
            </tbody>
          </table>
        </div>
        <div class="print-footer-note"></div>
      </div>
    </body>
    </html>
    """.trimIndent()
}

fun formatSetupPreviewPlannedHours(value: String): String {
    val n = value.toDoubleOrNull() ?: return ""
    if (n == 0.0) return ""
    return String.format(Locale.US, "%.1f", n)
}

fun setupPreviewHasNextProduct(name: String): Boolean = name.trim().isNotEmpty()

fun normalizeSetupPreviewOperationVariance(value: String): String {
    if (value.isBlank()) return ""
    val n = value.toDoubleOrNull() ?: return value.trim()
    return Math.round(n).toString()
}

private fun plannedHoursClass(value: String): String {
    val n = value.toDoubleOrNull() ?: return "planned-hours planned-hours-empty"
    if (n == 0.0) return "planned-hours planned-hours-empty"
    return when {
        n < 8 -> "planned-hours planned-hours-low"
        n < 16 -> "planned-hours planned-hours-mid"
        n < 22.5 -> "planned-hours planned-hours-high"
        else -> "planned-hours planned-hours-very-high"
    }
}

private fun operationVarianceClass(value: String): String {
    if (value.isBlank()) return "numeric-cell"
    val n = value.toDoubleOrNull() ?: return "numeric-cell"
    return if (n < 0) "numeric-cell operation-negative" else "numeric-cell"
}

private fun setupScheduleStyles(): String = """
  @page {
    size: A4 landscape;
    margin: 12mm;
    marks: none;
    bleed: 0mm;
    page-break-after: auto;
  }
  @media print {
    @page {
      size: A4 landscape;
      margin: 12mm;
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
    font-family: 'Yu Gothic', 'Hiragino Sans', sans-serif;
    font-size: 11px;
    line-height: 1.1;
    margin: 0;
    padding: 0;
    color: #000;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .print-container {
    width: 100%;
    height: 100%;
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
  .table-wrapper {
    flex: 1;
    overflow: hidden;
  }
  .print-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 6px;
    padding-bottom: 3px;
    position: relative;
  }
  .header-left { flex: 1; text-align: left; }
  .print-title {
    font-size: 16px;
    font-weight: bold;
    color: #000;
    line-height: 1.1;
  }
  .print-center-section {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    text-align: center;
  }
  .print-production-date-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1px;
  }
  .print-production-date {
    font-size: 16px;
    font-weight: bold;
    color: #000;
    line-height: 1.1;
  }
  .print-aggregation-time {
    font-size: 9px;
    color: #000;
    line-height: 1.1;
  }
  .header-right { text-align: right; flex: 1; }
  .print-date-time {
    font-size: 9px;
    color: #000;
    margin-bottom: 2px;
  }
  .print-total {
    font-size: 14px;
    font-weight: bold;
    color: #000;
  }
  .print-footer-note {
    position: absolute;
    bottom: 3mm;
    right: 8mm;
    font-size: 11px;
    color: #ff0000;
    text-align: right;
  }
  .main-table {
    width: 100%;
    border-collapse: collapse;
    border: 2px solid #000;
    font-size: 10px;
  }
  .main-table th,
  .main-table td {
    border: 1px solid #000;
    padding: 2px 2px;
    text-align: center;
    vertical-align: middle;
    color: #000 !important;
  }
  .main-table thead tr:first-child th { border-top: 2px solid #000 !important; }
  .main-table thead tr th:first-child { border-left: 2px solid #000 !important; }
  .main-table thead tr th:last-child { border-right: 2px solid #000 !important; }
  .main-table tbody tr:last-child td { border-bottom: 2px solid #000 !important; }
  .main-table tbody tr td:first-child { border-left: 2px solid #000 !important; }
  .main-table tbody tr td:last-child { border-right: 2px solid #000 !important; }
  .main-table th {
    background-color: #f0f0f0;
    font-weight: 400 !important;
    font-size: 10px;
    height: 20px;
    line-height: 1.1;
  }
  .main-table thead tr:first-child th[colspan] {
    font-size: 9px;
    font-weight: 600;
    padding: 3px 2px;
    background-color: #e8e8e8;
  }
  .main-table .bold-border-col {
    border-left: 2px solid #000 !important;
    border-right: 2px solid #000 !important;
  }
  .main-table thead tr:first-child th.bold-border-col { border-top: 2px solid #000 !important; }
  .main-table thead tr:last-child th.bold-border-col:first-child { border-top: 2px solid #000 !important; }
  .main-table td {
    font-size: 10px;
    height: 19px;
    line-height: 1.5;
  }
  .main-table .reference-date-red { color: #e00 !important; }
  .numeric-cell { text-align: right; padding-right: 8px; }
  .main-table .blank-col {
    border: none !important;
    background-color: transparent !important;
    padding: 0 !important;
  }
  .main-table .line-col { border-left: 2px solid #000 !important; }
  .main-table thead tr:last-child th.reference-col { border-top: 2px solid #000 !important; }
  .main-table .operation-negative { color: #e00 !important; }
  .main-table .op-progress-col {
    width: 7ch;
    min-width: 7ch;
    max-width: 7ch;
  }
  .main-table .planned-hours {
    text-align: center;
    font-weight: 600;
    border-radius: 3px;
  }
  .main-table td.plan-quantity-cell {
    font-weight: 700;
    text-align: center;
  }
  .main-table .planned-hours-low { background: #fdeaea; }
  .main-table .planned-hours-mid { background: #fff6db; }
  .main-table .planned-hours-high { background: #e8f7ec; }
  .main-table .planned-hours-very-high { background: #cfeecf; }
  .main-table .next-arrow-cell {
    text-align: center;
    font-size: 13px;
    font-weight: 900;
    border-top: none !important;
    border-bottom: none !important;
  }
""".trimIndent()
