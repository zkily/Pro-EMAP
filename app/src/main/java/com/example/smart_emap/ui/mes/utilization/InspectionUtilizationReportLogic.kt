package com.example.smart_emap.ui.mes.utilization

import com.example.smart_emap.data.model.InspectionUtilizationDailyInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationInspectorRowDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

data class IuaReportKpiCard(
    val label: String,
    val value: String,
    val hint: String,
    val tone: String,
)

data class IuaReportCharts(
    val daily: String?,
    val overtime: String?,
)

data class IuaReportContext(
    val filters: IuaReportFilters,
    val kpiCards: List<IuaReportKpiCard>,
    val charts: IuaReportCharts,
    val inspectorRows: List<InspectionUtilizationInspectorRowDto>,
    val dailyDetailRows: List<InspectionUtilizationDailyInspectorRowDto>,
)

data class IuaDailyBatchItem(
    val inspectorUserId: Int?,
    val inspectorLabel: String,
    val chartSvg: String?,
    val dayCount: Int,
    val avgUtilizationPercent: Double?,
    val sumOvertimeMin: Int,
)

object InspectionUtilizationReportLogic {
    private const val PRINT_INSPECTOR_TABLE_MAX = 12
    private const val PRINT_DAILY_TABLE_MAX = 16

    fun buildFullPrintHtml(ctx: IuaReportContext): String = buildPrintDocumentShell(
        filters = ctx.filters,
        mode = "full",
        includeKpi = true,
        kpiCards = ctx.kpiCards,
        body = buildFullPrintBody(ctx),
    )

    fun buildDailyPrintHtml(ctx: IuaReportContext): String = buildPrintDocumentShell(
        filters = ctx.filters,
        mode = "section",
        orientation = "landscape",
        sectionTitle = "日別稼働率推移",
        body = panelSection(
            title = "日別稼働率推移",
            titleInline = ctx.filters.inspectorLabel,
            theme = "chart",
            badges = panelBadge("稼働率 · 正味(H)", "chart"),
            chartSvg = ctx.charts.daily,
            chartAlt = "日別稼働率推移",
            chartTall = true,
            flowBreak = true,
        ),
    )

    fun buildDailyBatchPrintHtml(filters: IuaReportFilters, items: List<IuaDailyBatchItem>): String {
        val printedAt = printedAtJa()
        val body = items.mapIndexed { idx, item ->
            val badges = buildString {
                append(panelBadge("${item.dayCount} 日", "soft"))
                item.avgUtilizationPercent?.let {
                    append(panelBadge("平均稼働率 ${fmtPct(it)}", "chart"))
                }
                if (item.sumOvertimeMin > 0) {
                    append(panelBadge("残業合計 ${fmtDurationMin(item.sumOvertimeMin)}", "overtime"))
                }
            }
            val panel = panelSection(
                title = "日別稼働率推移",
                titleInline = item.inspectorLabel,
                theme = "chart",
                badges = badges,
                chartSvg = item.chartSvg,
                chartAlt = "日別稼働率推移 — ${item.inspectorLabel}",
                chartTall = true,
                flowBreak = true,
            )
            """<div class="print-page${if (idx > 0) " print-page--break" else ""}">
                ${buildDailyBatchPrintPageHeader(filters, printedAt)}
                $panel
            </div>"""
        }.joinToString("")
        return """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>検査工程 — 稼働率分析 — 日別稼働率推移（検査員別）</title>
  <style>${getPrintStyles("section", "landscape")}</style>
</head>
<body>
  $body
  <footer class="ft">Smart-EMAPs · 検査稼働率分析 · ${escHtml(printedAt)}</footer>
</body>
</html>"""
    }

    fun buildReportCharts(dailyRows: List<IuaChartDailyRow>): IuaReportCharts = IuaReportCharts(
        daily = if (dailyRows.isNotEmpty()) buildDailyChartSvg(dailyRows) else null,
        overtime = if (dailyRows.isNotEmpty()) buildOvertimeChartSvg(dailyRows) else null,
    )

    fun buildDailyChartSvg(rows: List<IuaChartDailyRow>, width: Int = 960, height: Int = 380): String {
        val leftPad = 52f
        val rightPad = 48f
        val topPad = 48f
        val bottomPad = 36f
        val chartLeft = leftPad
        val chartRight = width - rightPad
        val chartTop = topPad
        val chartBottom = height - bottomPad
        val chartWidth = max(chartRight - chartLeft, 1f)
        val chartHeight = max(chartBottom - chartTop, 1f)
        val count = max(rows.size, 1)
        val groupWidth = chartWidth / count
        val pctMax = 120f
        val hoursMax = rows.maxOfOrNull { InspectionUtilizationLogic.netHoursFromRow(it) }?.coerceAtLeast(0.1f)?.times(1.15f) ?: 1f
        val gridLines = (0..4).joinToString("") { tick ->
            val y = chartBottom - chartHeight * tick / 4f
            """<line x1="$chartLeft" y1="$y" x2="$chartRight" y2="$y" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4 4"/>"""
        }
        val bars = rows.mapIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val hours = InspectionUtilizationLogic.netHoursFromRow(row)
            val barH = hours / hoursMax * chartHeight
            val barW = (groupWidth * 0.35f).coerceIn(4f, 18f)
            if (barH <= 0f) "" else {
                val x = centerX - barW / 2f
                val y = chartBottom - barH
                """<rect x="$x" y="$y" width="$barW" height="$barH" fill="#c7d2fe" rx="4"/>"""
            }
        }.joinToString("")
        val points = rows.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val pct = row.utilizationPercent?.toFloat() ?: 0f
            val y = chartTop + chartHeight * (1f - pct / pctMax)
            x to y
        }
        val linePath = if (points.size >= 2) {
            val d = points.mapIndexed { i, (x, y) ->
                if (i == 0) "M$x,$y" else "L$x,$y"
            }.joinToString(" ")
            val area = buildString {
                append("M${points.first().first},$chartBottom ")
                points.forEach { (x, y) -> append("L$x,$y ") }
                append("L${points.last().first},$chartBottom Z")
            }
            """<path d="$area" fill="rgba(16,185,129,0.12)"/>
               <path d="$d" fill="none" stroke="#10b981" stroke-width="2.5"/>"""
        } else ""
        val dots = points.joinToString("") { (x, y) ->
            """<circle cx="$x" cy="$y" r="3" fill="#10b981"/><circle cx="$x" cy="$y" r="4" fill="#fff"/>"""
        }
        val labels = rows.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val label = InspectionUtilizationLogic.chartDayLabel(row.day)
            """<text x="$x" y="${height - 8f}" text-anchor="middle" font-size="10" fill="#64748b">$label</text>"""
        }.joinToString("")
        return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="100%" role="img" aria-label="日別稼働率推移">
            <rect width="100%" height="100%" fill="#fff"/>
            $gridLines
            <line x1="$chartLeft" y1="$chartBottom" x2="$chartRight" y2="$chartBottom" stroke="#e2e8f0"/>
            $bars
            $linePath
            $dots
            $labels
        </svg>"""
    }

    fun buildOvertimeChartSvg(rows: List<IuaChartDailyRow>, width: Int = 960, height: Int = 380): String {
        val leftPad = 48f
        val rightPad = 20f
        val topPad = 44f
        val bottomPad = 36f
        val chartLeft = leftPad
        val chartRight = width - rightPad
        val chartTop = topPad
        val chartBottom = height - bottomPad
        val chartWidth = max(chartRight - chartLeft, 1f)
        val chartHeight = max(chartBottom - chartTop, 1f)
        val count = max(rows.size, 1)
        val groupWidth = chartWidth / count
        val hoursMax = rows.maxOfOrNull { InspectionUtilizationLogic.overtimeHoursFromRow(it) }?.coerceAtLeast(0.1f)?.times(1.15f) ?: 1f
        val gridLines = (0..4).joinToString("") { tick ->
            val y = chartBottom - chartHeight * tick / 4f
            """<line x1="$chartLeft" y1="$y" x2="$chartRight" y2="$y" stroke="#f1f5f9" stroke-width="1" stroke-dasharray="4 4"/>"""
        }
        val bars = rows.mapIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val hours = InspectionUtilizationLogic.overtimeHoursFromRow(row)
            val barH = hours / hoursMax * chartHeight
            val barW = (groupWidth * 0.4f).coerceIn(4f, 22f)
            if (barH <= 0f) "" else {
                val x = centerX - barW / 2f
                val y = chartBottom - barH
                """<rect x="$x" y="$y" width="$barW" height="$barH" fill="#f59e0b" rx="5"/>"""
            }
        }.joinToString("")
        val labels = rows.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val label = InspectionUtilizationLogic.chartDayLabel(row.day)
            """<text x="$x" y="${height - 8f}" text-anchor="middle" font-size="10" fill="#64748b">$label</text>"""
        }.joinToString("")
        return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="100%" role="img" aria-label="日別残業推移">
            <rect width="100%" height="100%" fill="#fffbeb"/>
            $gridLines
            <line x1="$chartLeft" y1="$chartBottom" x2="$chartRight" y2="$chartBottom" stroke="#e2e8f0"/>
            $bars
            $labels
        </svg>"""
    }

    private fun buildFullPrintBody(ctx: IuaReportContext): String {
        val inspectorBadges = panelBadge("${ctx.inspectorRows.size} 名", "inspector")
        val dailyBadges = panelBadge("${ctx.dailyDetailRows.size} 行", "soft")
        return """
        <div class="charts-stack">
          ${panelSection(
            title = "日別稼働率推移",
            theme = "chart",
            badges = panelBadge("稼働率 · 正味(H)", "chart"),
            chartSvg = ctx.charts.daily,
            chartAlt = "日別稼働率推移",
            chartTall = true,
        )}
          ${panelSection(
            title = "日別残業推移",
            theme = "overtime",
            badges = panelBadge("残業(H)", "overtime"),
            chartSvg = ctx.charts.overtime,
            chartAlt = "日別残業推移",
            chartTall = true,
        )}
        </div>
        <div class="tables-row">
          ${panelSection(
            title = "検査員別サマリ",
            theme = "inspector",
            badges = inspectorBadges,
            tableHtml = buildInspectorPrintTable(ctx.inspectorRows),
        )}
          ${panelSection(
            title = "検査員 × 日別明細",
            theme = "daily",
            badges = dailyBadges,
            tableHtml = buildDailyDetailPrintTable(ctx.dailyDetailRows),
        )}
        </div>
        """
    }

    private fun buildPrintDocumentShell(
        filters: IuaReportFilters,
        mode: String,
        orientation: String = "portrait",
        sectionTitle: String? = null,
        includeKpi: Boolean = false,
        kpiCards: List<IuaReportKpiCard> = emptyList(),
        body: String,
    ): String {
        val printedAt = printedAtJa()
        val kpiHtml = if (includeKpi && kpiCards.isNotEmpty()) {
            """<div class="kpi-row">${kpiCards.joinToString("") { kpiCardHtml(it) }}</div>"""
        } else ""
        val sectionHtml = sectionTitle?.let { """<div class="hd__section">${escHtml(it)}</div>""" }.orEmpty()
        return """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>検査工程 — 稼働率分析${sectionTitle?.let { " — $it" }.orEmpty()}</title>
  <style>${getPrintStyles(mode, orientation)}</style>
</head>
<body>
  <header class="hd">
    <div class="hd__title">検査工程 — 稼働率分析</div>
    $sectionHtml
    ${buildMetaLineHtml(filters, printedAt)}
  </header>
  $kpiHtml
  $body
  <footer class="ft">Smart-EMAPs · 検査稼働率分析 · ${escHtml(printedAt)}</footer>
</body>
</html>"""
    }

    private fun buildDailyBatchPrintPageHeader(filters: IuaReportFilters, printedAt: String): String =
        """<header class="hd">
            <div class="hd__title">検査工程 — 稼働率分析</div>
            <div class="hd__section">日別稼働率推移（検査員別・一括）</div>
            ${buildMetaLineHtml(filters, printedAt, compact = true)}
        </header>"""

    private fun buildMetaLineHtml(filters: IuaReportFilters, printedAt: String, compact: Boolean = false): String {
        if (compact) {
            return """<div class="meta-line">
                <span><span class="meta-line__label">集計期間</span> ${escHtml(filters.startDate)} ～ ${escHtml(filters.endDate)}</span>
                <span class="meta-line__sep">|</span>
                <span><span class="meta-line__label">出力日時</span> ${escHtml(printedAt)}</span>
            </div>"""
        }
        return """<div class="meta-line">
            <span><span class="meta-line__label">集計期間</span> ${escHtml(filters.startDate)} ～ ${escHtml(filters.endDate)}</span>
            <span class="meta-line__sep">|</span>
            <span><span class="meta-line__label">出力日時</span> ${escHtml(printedAt)}</span>
            <span class="meta-line__sep">|</span>
            <span><span class="meta-line__label">検査員</span> ${escHtml(filters.inspectorLabel)}</span>
            <span class="meta-line__sep">|</span>
            <span><span class="meta-line__label">未確定を含む</span> ${escHtml(if (filters.includeIncomplete) "はい" else "いいえ")}</span>
        </div>"""
    }

    private fun kpiCardHtml(card: IuaReportKpiCard): String =
        """<div class="kpi-card kpi-card--${card.tone}">
            <div class="kpi-card__accent"></div>
            <div class="kpi-card__body">
              <div class="kpi-card__label">${escHtml(card.label)}</div>
              <div class="kpi-card__value">${escHtml(card.value)}</div>
              <div class="kpi-card__hint">${escHtml(card.hint)}</div>
            </div>
        </div>"""

    private fun panelBadge(text: String, tone: String = "soft"): String =
        """<span class="panel-badge panel-badge--$tone">${escHtml(text)}</span>"""

    private fun panelSection(
        title: String,
        theme: String,
        badges: String = "",
        titleInline: String? = null,
        chartSvg: String? = null,
        chartAlt: String? = null,
        tableHtml: String? = null,
        chartTall: Boolean = false,
        flowBreak: Boolean = false,
    ): String {
        val chartClass = if (chartTall) "chart-wrap chart-wrap--tall" else "chart-wrap"
        val chartHtml = chartSvg?.let {
            """<div class="$chartClass">$it</div>"""
        }.orEmpty()
        val titleHtml = if (titleInline != null) {
            """<div class="panel__title-row">
                <span class="panel__title">${escHtml(title)}</span>
                <span class="panel__title-inline">${escHtml(titleInline)}</span>
            </div>"""
        } else {
            """<div class="panel__title">${escHtml(title)}</div>"""
        }
        val flowClass = if (flowBreak) " panel--flow" else ""
        val badgesHtml = badges.takeIf { it.isNotBlank() }?.let { """<div class="panel__badges">$it</div>""" }.orEmpty()
        return """<section class="panel panel--$theme$flowClass">
            <div class="panel__head">
              <div class="panel__titles">$titleHtml</div>
              $badgesHtml
            </div>
            $chartHtml
            ${tableHtml.orEmpty()}
        </section>"""
    }

    private fun buildInspectorPrintTable(rows: List<InspectionUtilizationInspectorRowDto>): String {
        if (rows.isEmpty()) return """<p class="empty">データなし</p>"""
        val display = rows.take(PRINT_INSPECTOR_TABLE_MAX)
        val body = display.mapIndexed { index, row ->
            """<tr>
                <td class="center">${index + 1}</td>
                <td>${escHtml(row.inspectorName ?: "—")}</td>
                <td class="num">${escHtml("${row.scheduledWorkDayCount ?: 0}/${row.workDayCount ?: 0}")}</td>
                <td class="num">${escHtml(fmtInt(row.sessionCount))}</td>
                <td class="num">${escHtml(secToHours(row.sumNetProductionSec))}</td>
                <td class="num">${escHtml(secToHours(row.sumRegularSec))}</td>
                <td class="num warn">${escHtml(secToHours(row.sumOvertimeSec))}</td>
                <td class="num"><span class="pill pill--util">${escHtml(fmtPct(row.utilizationPercent))}</span></td>
                <td class="num">${escHtml(fmtPct(row.calendarUtilizationPercent))}</td>
            </tr>"""
        }.joinToString("")
        val more = if (rows.size > PRINT_INSPECTOR_TABLE_MAX) {
            """<p class="table-more">… 他 ${rows.size - PRINT_INSPECTOR_TABLE_MAX} 名</p>"""
        } else ""
        return """<table class="data data--inspector data--compact">
            <thead>${tableHead(listOf("#", "検査員", "出勤日", "件", "正味(h)", "所定内(h)", "残業(h)", "稼働率", "ｶﾚﾝﾀﾞｰ"))}</thead>
            <tbody>$body</tbody>
        </table>$more"""
    }

    private fun buildDailyDetailPrintTable(rows: List<InspectionUtilizationDailyInspectorRowDto>): String {
        if (rows.isEmpty()) return """<p class="empty">データなし</p>"""
        val display = rows.take(PRINT_DAILY_TABLE_MAX)
        val body = display.joinToString("") { row ->
            """<tr>
                <td>${escHtml(row.day)}</td>
                <td>${escHtml(row.inspectorName ?: "—")}</td>
                <td class="num">${escHtml(fmtInt(row.sessionCount))}</td>
                <td class="num">${escHtml(InspectionUtilizationLogic.fmtScheduledHours(row.scheduledHours))}</td>
                <td class="num">${escHtml(fmtDurationMin(row.sumNetProductionMin))}</td>
                <td class="num">${escHtml(fmtDurationMin(row.regularMin))}</td>
                <td class="num warn">${escHtml(fmtDurationMin(row.overtimeMin))}</td>
                <td class="num"><span class="pill pill--util">${escHtml(fmtPct(row.utilizationPercent))}</span></td>
            </tr>"""
        }
        val more = if (rows.size > PRINT_DAILY_TABLE_MAX) {
            """<p class="table-more">… 他 ${rows.size - PRINT_DAILY_TABLE_MAX} 行</p>"""
        } else ""
        return """<table class="data data--daily data--compact">
            <thead>${tableHead(listOf("生産日", "検査員", "件", "所定(h)", "正味", "所定内", "残業", "稼働率"))}</thead>
            <tbody>$body</tbody>
        </table>$more"""
    }

    private fun tableHead(cells: List<String>): String =
        """<tr>${cells.joinToString("") { """<th>${escHtml(it)}</th>""" }}</tr>"""

    private fun printedAtJa(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))

    private fun escHtml(value: Any?): String = value.toString()
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun fmtPct(value: Double?): String =
        InspectionUtilizationLogic.fmtPct(value)

    private fun fmtInt(value: Int?): String =
        InspectionUtilizationLogic.fmtInt(value)

    private fun fmtDurationMin(min: Int?): String {
        val n = min ?: 0
        if (n <= 0) return "0m"
        val h = n / 60
        val m = n % 60
        return if (h > 0) if (m > 0) "${h}h${m}m" else "${h}h" else "${m}m"
    }

    private fun secToHours(sec: Int?): String =
        InspectionUtilizationLogic.fmtHours(sec)

    private fun getPrintStyles(mode: String, orientation: String): String {
        val compact = mode == "full"
        val landscape = orientation == "landscape"
        val chartTall = if (landscape) "480px" else "280px"
        val chartNormal = if (landscape) "480px" else if (compact) "280px" else "220px"
        val kpiMinH = if (compact) "48px" else "58px"
        val pageRule = if (landscape) {
            "@page { size: A4 landscape; margin: 8mm 10mm; }"
        } else {
            "@page { size: A4 portrait; margin: 10mm 12mm; }"
        }
        return """
    html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    $pageRule
    * { box-sizing: border-box; }
    body { margin: 0; color: #0f172a; font: 10px/1.4 "Segoe UI", "Yu Gothic UI", "Hiragino Sans", Meiryo, sans-serif; background: #fff; }
    .hd { border-bottom: 2px solid #10b981; padding-bottom: 10px; margin-bottom: 10px; }
    .hd__title { font-size: 17px; font-weight: 800; letter-spacing: -0.02em; }
    .hd__section { margin-top: 4px; font-size: 12px; font-weight: 700; color: #047857; }
    .meta-line { display: flex; flex-wrap: wrap; align-items: center; gap: 2px 0; margin-top: 6px; font-size: 8.5px; line-height: 1.5; color: #475569; }
    .meta-line__label { font-weight: 700; color: #334155; }
    .meta-line__sep { margin: 0 8px; color: #cbd5e1; }
    .kpi-row { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 5px; margin-bottom: 6px; }
    .kpi-card { position: relative; border-radius: 8px; padding: 6px 7px 5px; border: 1px solid transparent; overflow: hidden; min-height: $kpiMinH; }
    .kpi-card__accent { position: absolute; top: 0; left: 0; right: 0; height: 3px; }
    .kpi-card__label { font-size: 7.5px; font-weight: 700; letter-spacing: 0.02em; }
    .kpi-card__value { margin-top: 3px; font-size: 15px; font-weight: 800; line-height: 1.1; }
    .kpi-card__hint { margin-top: 2px; font-size: 7px; font-weight: 600; }
    .kpi-card--green { background: linear-gradient(160deg, #fff 0%, #d1fae5 100%); border-color: rgba(16,185,129,.22); }
    .kpi-card--green .kpi-card__accent { background: linear-gradient(90deg, #10b981, #34d399); }
    .kpi-card--green .kpi-card__label { color: #059669; }
    .kpi-card--green .kpi-card__value { color: #047857; }
    .kpi-card--green .kpi-card__hint { color: #34d399; }
    .kpi-card--blue { background: linear-gradient(160deg, #fff 0%, #e0f2fe 100%); border-color: rgba(14,165,233,.22); }
    .kpi-card--blue .kpi-card__accent { background: linear-gradient(90deg, #0ea5e9, #38bdf8); }
    .kpi-card--blue .kpi-card__label { color: #0284c7; }
    .kpi-card--blue .kpi-card__value { color: #0369a1; }
    .kpi-card--blue .kpi-card__hint { color: #38bdf8; }
    .kpi-card--indigo { background: linear-gradient(160deg, #fff 0%, #eef2ff 100%); border-color: rgba(99,102,241,.2); }
    .kpi-card--indigo .kpi-card__accent { background: linear-gradient(90deg, #6366f1, #818cf8); }
    .kpi-card--indigo .kpi-card__label { color: #6366f1; }
    .kpi-card--indigo .kpi-card__value { color: #4338ca; }
    .kpi-card--indigo .kpi-card__hint { color: #818cf8; }
    .kpi-card--amber { background: linear-gradient(160deg, #fff 0%, #ffedd5 100%); border-color: rgba(249,115,22,.22); }
    .kpi-card--amber .kpi-card__accent { background: linear-gradient(90deg, #f97316, #fb923c); }
    .kpi-card--amber .kpi-card__label { color: #ea580c; }
    .kpi-card--amber .kpi-card__value { color: #c2410c; }
    .kpi-card--amber .kpi-card__hint { color: #fb923c; }
    .kpi-card--violet { background: linear-gradient(160deg, #fff 0%, #ede9fe 100%); border-color: rgba(124,58,237,.22); }
    .kpi-card--violet .kpi-card__accent { background: linear-gradient(90deg, #7c3aed, #a78bfa); }
    .kpi-card--violet .kpi-card__label { color: #7c3aed; }
    .kpi-card--violet .kpi-card__value { color: #6d28d9; }
    .kpi-card--violet .kpi-card__hint { color: #a78bfa; }
    .panel { margin-top: 6px; padding: 6px 8px 8px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; break-inside: avoid-page; page-break-inside: avoid; }
    .panel--flow { break-inside: auto; page-break-inside: auto; margin-top: 0; }
    .panel--flow .chart-wrap { break-inside: avoid; page-break-inside: avoid; }
    .panel--chart { border-color: rgba(16,185,129,.18); background: linear-gradient(165deg, #fff 0%, #f0fdf4 100%); }
    .panel--overtime { border-color: rgba(245,158,11,.2); background: linear-gradient(165deg, #fff 0%, #fffbeb 100%); }
    .panel--inspector { background: linear-gradient(165deg, #fff 0%, #f5f3ff 100%); border-color: rgba(99,102,241,.16); }
    .panel--daily { background: linear-gradient(165deg, #fff 0%, #f8fafc 100%); border-color: rgba(148,163,184,.2); }
    .charts-stack { display: flex; flex-direction: column; gap: 6px; margin-top: 6px; }
    .charts-stack .panel { margin-top: 0; width: 100%; }
    .charts-stack .chart-wrap { width: 100%; min-height: ${if (compact) "240px" else "200px"}; display: flex; align-items: center; justify-content: center; }
    .charts-stack .chart-wrap--tall svg, .charts-stack svg { width: 100%; max-height: $chartTall; }
    .tables-row { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; margin-top: 6px; break-inside: avoid-page; page-break-inside: avoid; }
    .tables-row .panel { margin-top: 0; min-width: 0; break-inside: avoid; page-break-inside: avoid; }
    .panel__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; margin-bottom: 6px; }
    .panel__title { font-size: 11px; font-weight: 800; color: #1e293b; }
    .panel__title-row { display: flex; align-items: baseline; flex-wrap: wrap; gap: 8px; }
    .panel__title-inline { font-size: 11px; font-weight: 700; color: #047857; }
    .print-page { break-inside: avoid-page; page-break-inside: avoid; }
    .print-page--break { break-before: page; page-break-before: always; margin-top: 0; }
    .print-page .hd { margin-bottom: 8px; }
    .print-page .panel { margin-top: 0; }
    .panel__badges { display: flex; flex-wrap: wrap; gap: 4px; justify-content: flex-end; }
    .panel-badge { display: inline-block; padding: 2px 7px; border-radius: 999px; font-size: 7.5px; font-weight: 700; white-space: nowrap; }
    .panel-badge--soft { color: #64748b; background: rgba(148,163,184,.14); }
    .panel-badge--chart { color: #047857; background: rgba(16,185,129,.12); border: 1px solid rgba(16,185,129,.18); }
    .panel-badge--overtime { color: #b45309; background: rgba(245,158,11,.14); border: 1px solid rgba(245,158,11,.22); }
    .panel-badge--inspector { color: #4338ca; background: rgba(99,102,241,.12); border: 1px solid rgba(99,102,241,.18); }
    .chart-wrap { margin-bottom: 6px; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; background: linear-gradient(180deg, #fafbfc 0%, #fff 100%); }
    .chart-wrap--tall svg { max-height: $chartTall; width: 100%; }
    table.data { width: 100%; border-collapse: collapse; table-layout: fixed; margin-top: 4px; }
    table.data th, table.data td { border: 1px solid #cbd5e1; padding: 3px 4px; vertical-align: middle; word-break: break-word; }
    table.data th { font-size: 7.5px; font-weight: 700; }
    table.data td { font-size: 8px; }
    table.data--inspector th { background: linear-gradient(180deg, #ede9fe, #e0e7ff); color: #4338ca; }
    table.data--daily th { background: linear-gradient(180deg, #d1fae5, #a7f3d0); color: #047857; }
    table.data tbody tr:nth-child(even) { background: rgba(248,250,252,.85); }
    .data--compact th, .data--compact td { font-size: 6.5px; padding: 2px 3px; }
    .num { text-align: right; font-variant-numeric: tabular-nums; }
    .center { text-align: center; }
    .warn { color: #c2410c; font-weight: 700; }
    .pill { display: inline-block; padding: 1px 5px; border-radius: 5px; font-size: 7.5px; font-weight: 700; }
    .pill--util { color: #047857; background: rgba(16,185,129,.12); border: 1px solid rgba(16,185,129,.18); }
    .empty, .table-more { margin: 4px 0 0; font-size: 8px; color: #94a3b8; }
    .ft { margin-top: 10px; padding-top: 6px; border-top: 1px solid #e2e8f0; font-size: 7.5px; color: #94a3b8; text-align: right; }
    @media print { body { margin: 0; } .ft { position: fixed; bottom: 0; right: 0; left: 0; } }
  """
    }
}
