package com.example.smart_emap.ui.mes.planinstruction

fun buildWeldingSetupSchedulePrintHtml(
    config: PlanInstructionConfig,
    data: SetupScheduleData,
    operationVarianceRows: List<OperationVarianceRow>,
    comparison: PlanComparisonSummary,
): String {
    val title = escapeHtml(config.setupSchedulePrintTitle)
    val productionDate = escapeHtml(data.productionDate)
    val currentDateTime = escapeHtml(data.currentDateTime)
    val totalQuantity = PlanInstructionLogic.formatNumber(data.totalQuantity)
    val totalRequiredStaff = data.tableRows.sumOf { it.requiredStaffCount }

    val tableBody = data.tableRows.joinToString("\n") { row ->
        val planQty = row.planQuantity?.let { PlanInstructionLogic.formatNumber(it) }.orEmpty()
        val nextQty = row.nextQuantity?.let { PlanInstructionLogic.formatNumber(it) }.orEmpty()
        """
        <tr>
          <td>${escapeHtml(row.line)}</td>
          <td>${escapeHtml(row.productName)}</td>
          <td class="numeric-cell">${escapeHtml(row.efficiency)}</td>
          <td class="numeric-cell">$planQty</td>
          <td class="numeric-cell">${escapeHtml(row.setupAfterHours)}</td>
          <td>${escapeHtml(row.nextProductName)}</td>
          <td class="numeric-cell">$nextQty</td>
          <td>${escapeHtml(row.remarks)}</td>
        </tr>
        """.trimIndent()
    }

    val varianceBody = if (operationVarianceRows.isEmpty()) {
        """
        <tr>
          <td colspan="3" class="no-data-cell small">データなし</td>
        </tr>
        """.trimIndent()
    } else {
        operationVarianceRows.joinToString("\n") { row ->
            val value = row.operationVariance
            val negativeClass = if (value < 0) "variance-negative" else ""
            val judgeClass = operationVarianceJudgmentClass(value)
            """
            <tr>
              <td>${escapeHtml(row.machineName)}</td>
              <td class="$negativeClass">${formatOperationVarianceHours(value)}</td>
              <td class="$judgeClass">${escapeHtml(operationVarianceJudgment(value))}</td>
            </tr>
            """.trimIndent()
        }
    }

    val comparisonDateNote = comparison.lastActualDate?.let {
        "（実績データ: ${escapeHtml(it)}まで）"
    }.orEmpty()

    val actualDiffClass = comparison.actualDifference?.let {
        if (it >= 0) "positive-difference" else "negative-difference"
    }.orEmpty()
    val achievementDiffClass = comparison.achievementRatioDifference?.let {
        if (it >= 0) "positive-difference" else "negative-difference"
    }.orEmpty()
    val statusClass = when (comparison.productionStatus) {
        "生産遅れ" -> "negative-difference"
        "生産先行", "生産早い" -> "positive-difference"
        else -> ""
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>$title</title>
      <style>
        ${weldingSetupScheduleStyles()}
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
            </div>
          </div>
          <div class="header-right">
            <div class="print-total">
              生産計画合計数 $totalQuantity ／ 必要人員合計(参考) ${totalRequiredStaff}人
            </div>
          </div>
        </div>

        <div class="table-wrapper">
          <table class="main-table">
            <thead>
              <tr>
                <th style="width: 10%;">ライン</th>
                <th style="width: 16%;">生産品種</th>
                <th style="width: 9%;">能率</th>
                <th style="width: 12%;">当日計画数</th>
                <th style="width: 13%;">所要生産時間(h)</th>
                <th style="width: 16%;">次生産品種</th>
                <th style="width: 12%;">次生産品種計画数</th>
                <th style="width: 12%;">備考(参考)</th>
              </tr>
            </thead>
            <tbody>
              $tableBody
            </tbody>
          </table>
        </div>

        <div class="lower-section">
          <div class="bottom-left panel">
            <div class="panel-title">操業度差異（溶接設備）</div>
            <table class="note-table variance-table">
              <thead>
                <tr>
                  <th>設備名</th>
                  <th>操業度差異</th>
                  <th>判定</th>
                </tr>
              </thead>
              <tbody>
                $varianceBody
              </tbody>
            </table>
          </div>
          <div class="bottom-right panel">
            <div class="panel-title">生産計画と実績比較$comparisonDateNote</div>
            <div class="memo-grid">
              <div class="memo-column">
                <div class="memo-block">
                  <div class="memo-label">基準計画合計:</div>
                  <div class="memo-value">${formatPlanComparisonValue(comparison.baselinePlanTotal, suffix = " 本")}</div>
                </div>
                <div class="memo-block">
                  <div class="memo-label">現行計画合計:</div>
                  <div class="memo-value">${formatPlanComparisonValue(comparison.currentPlanTotal, suffix = " 本")}</div>
                </div>
                <div class="memo-block">
                  <div class="memo-label">計画差異:</div>
                  <div class="memo-value">${formatPlanComparisonValue(comparison.planDifference, suffix = " 本")}</div>
                </div>
              </div>
              <div class="memo-column">
                <div class="memo-block">
                  <div class="memo-label">基準日平均生産数:</div>
                  <div class="memo-value">${formatPlanComparisonValue(comparison.baselineDailyAverage, suffix = "本/日")}</div>
                </div>
                <div class="memo-block">
                  <div class="memo-label">現行実績合計:</div>
                  <div class="memo-value">${formatPlanComparisonValue(comparison.currentActualTotal, suffix = " 本")}</div>
                </div>
                <div class="memo-block">
                  <div class="memo-label">計画対実績差:</div>
                  <div class="memo-value $actualDiffClass">${formatPlanComparisonValue(comparison.actualDifference, suffix = " 本")}</div>
                </div>
              </div>
            </div>
            <div class="memo-ratios">
              <div class="memo-ratio">
                <div class="memo-label">基準計画達成率:</div>
                <div class="memo-ratio-value">${formatPlanComparisonValue(comparison.baselinePlanAchievementRatio, suffix = "%", fractionDigits = 1)}</div>
              </div>
              <div class="memo-ratio">
                <div class="memo-label">現行計画達成率:</div>
                <div class="memo-ratio-value">${formatPlanComparisonValue(comparison.currentPlanAchievementRatio, suffix = "%", fractionDigits = 1)}</div>
              </div>
              <div class="memo-ratio">
                <div class="memo-label">達成率差異:</div>
                <div class="memo-ratio-value $achievementDiffClass">${formatPlanComparisonValue(comparison.achievementRatioDifference, suffix = "%", fractionDigits = 1)}</div>
              </div>
              <div class="memo-ratio">
                <div class="memo-label">生産状態:</div>
                <div class="memo-ratio-value $statusClass">${escapeHtml(comparison.productionStatus ?: "-")}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="print-footer">
          <div class="print-footer-left">*** 集計時間:前日15:00~当日15:00</div>
          <div class="print-footer-right">$currentDateTime 発行</div>
        </div>
      </div>
    </body>
    </html>
    """.trimIndent()
}

private fun weldingSetupScheduleStyles(): String = """
  @page { size: A4 landscape; margin: 12mm 12mm 8mm 12mm; }
  body {
    font-family: 'Yu Gothic', 'Hiragino Sans', sans-serif;
    font-size: 11px;
    margin: 0;
    color: #000;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .print-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
  }
  .print-title { font-size: 18px; font-weight: bold; }
  .print-production-date { font-size: 14px; font-weight: bold; text-align: center; }
  .print-total { font-size: 14px; font-weight: bold; text-align: right; }
  .main-table {
    width: 100%;
    border-collapse: collapse;
    border: 2px solid #000;
    font-size: 13px;
  }
  .main-table th, .main-table td {
    border: 1px solid #000;
    padding: 4px;
    text-align: center;
    vertical-align: middle;
  }
  .main-table tbody tr, .main-table tbody tr td { height: 32px; }
  .numeric-cell { text-align: center; }
  .lower-section {
    display: flex;
    gap: 10px;
    margin-top: 10px;
  }
  .panel {
    border: 1px solid #000;
    padding: 6px;
    flex: 1;
  }
  .panel-title {
    font-weight: bold;
    font-size: 12px;
    margin-bottom: 6px;
    border-bottom: 1px solid #666;
    padding-bottom: 4px;
  }
  .note-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 11px;
  }
  .note-table th, .note-table td {
    border: 1px solid #999;
    padding: 3px 4px;
    text-align: center;
  }
  .no-data-cell { color: #666; }
  .variance-negative { color: #dc2626; font-weight: bold; }
  .variance-judge-very-ahead, .positive-difference { color: #16a34a; font-weight: bold; }
  .variance-judge-ahead { color: #22c55e; }
  .variance-judge-very-delay, .negative-difference { color: #dc2626; font-weight: bold; }
  .variance-judge-delay { color: #f97316; }
  .memo-grid { display: flex; gap: 8px; }
  .memo-column { flex: 1; }
  .memo-block { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 11px; }
  .memo-label { color: #334155; }
  .memo-value { font-weight: bold; }
  .memo-ratios {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px 8px;
    margin-top: 8px;
    font-size: 11px;
  }
  .memo-ratio { display: flex; justify-content: space-between; }
  .memo-ratio-value { font-weight: bold; }
  .print-footer {
    margin-top: 12px;
    display: flex;
    justify-content: space-between;
    font-size: 9px;
  }
""".trimIndent()
