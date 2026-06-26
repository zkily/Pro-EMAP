package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.data.model.WeldingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.WeldingProductivityDailyRowDto
import com.example.smart_emap.data.model.WeldingProductivityOperatorRowDto
import com.example.smart_emap.data.model.WeldingProductivityProductRankingDto
import com.example.smart_emap.data.model.WeldingProductivityProductRowDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object WeldingProductivityReportLogic {
    private val printedAtFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    private const val PRINT_OPERATOR_TABLE_MAX = 5

    private enum class PrintMode { FULL, SECTION }

    private enum class PrintOrientation { PORTRAIT, LANDSCAPE }

    fun buildPrintHtml(
        command: WeldingProductivityReportCommand,
        data: WeldingProductivityAnalysisDataDto,
        ctx: WeldingProductivityPrintContext,
    ): String = when (command) {
        WeldingProductivityReportCommand.PRINT_FULL -> buildFullPrintHtml(ctx)
        WeldingProductivityReportCommand.PRINT_DAILY -> documentShell(
            filters = ctx.filters,
            sectionTitle = "日別推移",
            mode = PrintMode.SECTION,
            orientation = PrintOrientation.LANDSCAPE,
            body = panelSection(
                title = "日別推移",
                theme = "chart",
                badges = panelBadge("生産数 · 能率", "chart"),
                chartFileName = ctx.dailyChartFileName,
                chartAlt = "日別推移",
                chartTall = true,
                tableHtml = "",
            ),
        )
        WeldingProductivityReportCommand.PRINT_OPERATOR -> {
            val badges = operatorSectionBadges(ctx)
            documentShell(
                filters = ctx.filters,
                sectionTitle = "溶接作業者別",
                mode = PrintMode.SECTION,
                body = panelSection(
                    title = "溶接作業者別",
                    theme = "operator",
                    badges = badges,
                    tableHtml = buildOperatorPrintTable(ctx.operatorRows),
                ),
            )
        }
        WeldingProductivityReportCommand.PRINT_PRODUCT -> {
            val badges = productSectionBadges(ctx)
            documentShell(
                filters = ctx.filters,
                sectionTitle = "製品別",
                mode = PrintMode.SECTION,
                body = panelSection(
                    title = "製品別",
                    theme = "product",
                    badges = badges,
                    tableHtml = buildProductPrintTable(ctx.productRows),
                    flowBreak = true,
                ),
            )
        }
        WeldingProductivityReportCommand.PRINT_PRODUCT_RANK -> documentShell(
            filters = ctx.filters,
            sectionTitle = "製品別 · 溶接作業者能率ランキング",
            mode = PrintMode.SECTION,
            body = buildProductRankSectionBody(ctx),
        )
        WeldingProductivityReportCommand.PRINT_DAILY_BATCH,
        WeldingProductivityReportCommand.PRINT_OPERATOR_PRODUCT_BATCH,
        -> throw IllegalStateException("Use batch print builders")
    }

    fun buildDailyBatchPrintHtml(
        filters: WeldingProductivityReportFilters,
        items: List<WeldingDailyBatchPrintItem>,
    ): String {
        val printedAt = printedAt()
        val body = items.mapIndexed { idx, item ->
            val daily = item.daily
            val sumActualQty = daily.sumOf { it.sumActualQty ?: 0 }
            val totalSec = daily.sumOf { it.sumNetProductionSec ?: 0 }
            val avgEfficiency = WeldingProductivityLogic.periodAvgEfficiencyFromBucket(sumActualQty, totalSec)
            val badges = buildString {
                append(panelBadge("${daily.size} 日", "soft"))
                if (sumActualQty > 0) append(panelBadge("生産合計 ${WeldingProductivityLogic.fmtInt(sumActualQty)}", "product"))
                if (avgEfficiency != null) {
                    append(panelBadge("平均能率 ${WeldingProductivityLogic.fmtEfficiency(avgEfficiency)} 個/時", "inspector"))
                }
            }
            val panel = panelSection(
                title = "日別推移",
                titleInline = item.operatorLabel,
                theme = "chart",
                badges = badges,
                chartFileName = item.chartFileName,
                chartAlt = "日別推移 — ${item.operatorLabel}",
                chartTall = true,
                tableHtml = "",
                flowBreak = true,
            )
            """<div class="print-page${if (idx > 0) " print-page--break" else ""}">
              ${dailyBatchPageHeader(filters, printedAt)}
              $panel
            </div>"""
        }.joinToString("")
        return batchDocumentShell(
            title = "溶接工程 — 生産性分析 — 日別推移（溶接作業者別）",
            mode = PrintMode.SECTION,
            orientation = PrintOrientation.LANDSCAPE,
            body = body.ifBlank { emptyNote() },
            printedAt = printedAt,
        )
    }

    fun buildOperatorProductBatchPrintHtml(
        filters: WeldingProductivityReportFilters,
        items: List<Pair<String, List<WeldingOperatorProductDisplayRow>>>,
    ): String {
        val printedAt = printedAt()
        val body = items.mapIndexed { idx, (label, rows) ->
            val sessionCount = rows.sumOf { it.sessionCount }
            val sumActualQty = rows.sumOf { it.sumActualQty }
            val totalSec = rows.sumOf { it.sumNetProductionSec }
            val avgEfficiency = WeldingProductivityLogic.periodAvgEfficiencyFromBucket(sumActualQty, totalSec)
            val badges = buildString {
                append(panelBadge("${rows.size} 品目", "product"))
                append(panelBadge("セッション ${WeldingProductivityLogic.fmtInt(sessionCount)}", "soft"))
                if (sumActualQty > 0) append(panelBadge("生産合計 ${WeldingProductivityLogic.fmtInt(sumActualQty)}", "product"))
                if (avgEfficiency != null) {
                    append(panelBadge("平均能率 ${WeldingProductivityLogic.fmtEfficiency(avgEfficiency)} 個/時", "inspector"))
                }
            }
            val panel = panelSection(
                title = "生産製品一覧",
                titleInline = label,
                theme = "operator",
                badges = badges,
                hideChart = true,
                tableHtml = buildOperatorProductPrintTable(rows),
                flowBreak = true,
            )
            """<div class="print-page${if (idx > 0) " print-page--break" else ""}">
              ${operatorProductBatchPageHeader(filters, printedAt)}
              $panel
            </div>"""
        }.joinToString("")
        return batchDocumentShell(
            title = "溶接工程 — 生産性分析 — 溶接作業者別製品別",
            mode = PrintMode.SECTION,
            orientation = PrintOrientation.PORTRAIT,
            body = body.ifBlank { emptyNote() },
            printedAt = printedAt,
        )
    }

    private fun buildFullPrintHtml(ctx: WeldingProductivityPrintContext): String {
        val body = buildString {
            append(panelSection(
                title = "日別推移",
                theme = "chart",
                badges = panelBadge("生産数 · 能率", "chart"),
                chartFileName = ctx.dailyChartFileName,
                chartAlt = "日別推移",
                chartTall = true,
                tableHtml = "",
            ))
            append(panelSection(
                title = "溶接作業者別",
                theme = "operator",
                badges = operatorSectionBadges(ctx),
                tableHtml = buildOperatorPrintTable(ctx.operatorRows, PRINT_OPERATOR_TABLE_MAX),
            ))
            append(panelSection(
                title = "製品別",
                theme = "product",
                badges = productSectionBadges(ctx),
                tableHtml = buildProductPrintTable(ctx.productRows),
                pageBreak = true,
            ))
        }
        return documentShell(
            filters = ctx.filters,
            mode = PrintMode.FULL,
            includeKpi = true,
            kpiCards = ctx.kpiCards,
            body = body,
        )
    }

    private fun operatorSectionBadges(ctx: WeldingProductivityPrintContext): String = buildString {
        append(panelBadge("${ctx.operatorRows.size} 名", "soft"))
        ctx.operatorSectionAvgEfficiency?.let { avg ->
            append(panelBadge("平均能率 ${WeldingProductivityLogic.fmtEfficiency(avg)} 個/時", "inspector"))
        }
    }

    private fun productSectionBadges(ctx: WeldingProductivityPrintContext): String = buildString {
        append(panelBadge("${ctx.productRows.size} 品目", "soft"))
        if (ctx.productSectionTotalQty > 0) {
            append(panelBadge("生産合計 ${WeldingProductivityLogic.fmtInt(ctx.productSectionTotalQty)}", "product"))
        }
    }

    private fun buildProductRankSectionBody(ctx: WeldingProductivityPrintContext): String {
        val selected = ctx.selectedProductRanking
        val selectedBody = if (selected != null) {
            val stats = productRankStats(selected)
            val operatorCount = selected.operators.orEmpty().size
            buildProductRankHeroHtml(selected, stats) +
                panelSection(
                    title = "溶接作業者別能率",
                    theme = "rank",
                    badges = panelBadge("$operatorCount 名", "rank"),
                    tableHtml = buildProductRankOperatorTable(selected.operators.orEmpty()),
                )
        } else {
            emptyNote("対象製品がありません")
        }
        val overviewBody = if (ctx.productRankTopOverview.isEmpty()) {
            ""
        } else {
            """<div class="sub-panel__title">全製品 · 能率 TOP1 一覧</div>${buildProductRankOverviewTable(ctx.productRankTopOverview)}"""
        }
        return selectedBody + overviewBody
    }

    private fun productRankStats(ranking: WeldingProductivityProductRankingDto): ProductRankPrintStats {
        val operators = ranking.operators.orEmpty()
        val top = operators.firstOrNull()
        var totalEff = 0.0
        var effCount = 0
        for (row in operators) {
            val eff = row.efficiencyPerHour
            if (eff != null) {
                totalEff += eff
                effCount++
            }
        }
        return ProductRankPrintStats(
            topEfficiency = top?.efficiencyPerHour,
            avgEfficiency = if (effCount > 0) totalEff / effCount else null,
            operatorCount = ranking.rankedOperatorCount ?: operators.size,
        )
    }

    private data class ProductRankPrintStats(
        val topEfficiency: Double?,
        val avgEfficiency: Double?,
        val operatorCount: Int,
    )

    private fun buildOperatorPrintTable(
        rows: List<WeldingProductivityOperatorRowDto>,
        maxRows: Int = 0,
    ): String {
        if (rows.isEmpty()) return emptyNote()
        val display = if (maxRows > 0) rows.take(maxRows) else rows
        val body = display.mapIndexed { index, row ->
            val avg = WeldingProductivityLogic.periodAvgEfficiencyFromBucket(row)
            """<tr>
              <td class="center">${operatorRowRankHtml(index)}</td>
              <td>${esc(row.operatorName ?: "—")}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sessionCount))}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sumActualQty))}</td>
              <td class="num warn">${esc(WeldingProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num"><span class="pill pill--operator">${esc(WeldingProductivityLogic.fmtEfficiency(avg))}</span></td>
            </tr>"""
        }.joinToString("")
        val more = if (maxRows > 0 && rows.size > maxRows) {
            """<p class="table-more">… 他 ${rows.size - maxRows} 名</p>"""
        } else {
            ""
        }
        return """<table class="data data--operator data--compact">
          <thead>${tableHead(listOf("#", "溶接作業者", "件", "生産", "不良率", "平均能率"))}</thead>
          <tbody>$body</tbody>
        </table>$more"""
    }

    private fun buildProductPrintTable(rows: List<WeldingProductivityProductRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val body = rows.joinToString("") { row ->
            val avg = WeldingProductivityLogic.periodAvgEfficiencyFromBucket(row)
            """<tr>
              <td><span class="product-cd">${esc(row.productCd ?: "")}</span></td>
              <td>${esc(row.productName ?: "")}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sessionCount))}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sumActualQty))}</td>
              <td class="num warn">${esc(WeldingProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num"><span class="pill pill--product">${esc(WeldingProductivityLogic.fmtEfficiency(avg))}</span></td>
            </tr>"""
        }
        return """<table class="data data--product">
          <thead>${tableHead(listOf("CD", "製品名", "件", "生産", "不良率", "能率"))}</thead>
          <tbody>$body</tbody>
        </table>"""
    }

    private fun buildOperatorProductPrintTable(rows: List<WeldingOperatorProductDisplayRow>): String {
        if (rows.isEmpty()) return emptyNote()
        val body = rows.joinToString("") { row ->
            """<tr>
              <td><span class="product-cd">${esc(row.productCd)}</span></td>
              <td>${esc(row.productName)}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sessionCount))}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sumActualQty))}</td>
              <td class="num warn">${esc(WeldingProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num"><span class="pill pill--product">${esc(WeldingProductivityLogic.fmtEfficiency(row.avgEfficiencyPerHour))}</span></td>
            </tr>"""
        }
        return """<table class="data data--product">
          <thead>${tableHead(listOf("CD", "製品名", "件", "生産", "不良率", "能率"))}</thead>
          <tbody>$body</tbody>
        </table>"""
    }

    private fun buildProductRankOperatorTable(rows: List<WeldingProductivityOperatorRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val body = rows.joinToString("") { row ->
            """<tr>
              <td class="center">${rankBadgeHtml(row.rank ?: 0)}</td>
              <td>${esc(row.operatorName ?: "—")}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sessionCount))}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sumActualQty))}</td>
              <td class="num"><span class="pill pill--rank">${esc(WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</span></td>
              <td class="num warn">${esc(WeldingProductivityLogic.fmtPct(row.defectRatePercent))}</td>
            </tr>"""
        }
        return """<table class="data data--rank">
          <thead>${tableHead(listOf("順位", "溶接作業者", "件", "生産", "能率", "不良率"))}</thead>
          <tbody>$body</tbody>
        </table>"""
    }

    private fun buildProductRankOverviewTable(rows: List<WeldingProductivityProductRankingDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val body = rows.joinToString("") { row ->
            """<tr>
              <td><span class="product-cd product-cd--rank">${esc(row.productCd)}</span></td>
              <td>${esc(row.productName ?: "")}</td>
              <td>${esc(row.topOperatorName ?: "—")}</td>
              <td class="num"><span class="pill pill--rank">${esc(WeldingProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour))}</span></td>
              <td class="num">${esc(row.rankedOperatorCount ?: 0)}</td>
              <td class="num">${esc(WeldingProductivityLogic.fmtInt(row.sumActualQty))}</td>
            </tr>"""
        }
        return """<table class="data data--rank-overview">
          <thead>${tableHead(listOf("CD", "製品名", "TOP溶接作業者", "能率", "対象人数", "生産"))}</thead>
          <tbody>$body</tbody>
        </table>"""
    }

    private fun buildProductRankHeroHtml(
        ranking: WeldingProductivityProductRankingDto,
        stats: ProductRankPrintStats,
    ): String = """<div class="rank-hero">
      <div class="rank-hero__main">
        <span class="rank-hero__cd">${esc(ranking.productCd)}</span>
        <div class="rank-hero__name">${esc(ranking.productName?.ifBlank { "—" } ?: "—")}</div>
      </div>
      <div class="rank-hero__stats">
        <div class="rank-hero__stat"><span class="rank-hero__stat-label">生産合計</span><span class="rank-hero__stat-val">${esc(WeldingProductivityLogic.fmtInt(ranking.sumActualQty))}</span></div>
        <div class="rank-hero__stat"><span class="rank-hero__stat-label">溶接作業者</span><span class="rank-hero__stat-val">${esc(stats.operatorCount)}<small>名</small></span></div>
        <div class="rank-hero__stat rank-hero__stat--accent"><span class="rank-hero__stat-label">TOP能率</span><span class="rank-hero__stat-val">${esc(WeldingProductivityLogic.fmtEfficiency(stats.topEfficiency ?: ranking.topEfficiencyPerHour))}<small>個/時</small></span></div>
        <div class="rank-hero__stat"><span class="rank-hero__stat-label">平均能率</span><span class="rank-hero__stat-val">${esc(WeldingProductivityLogic.fmtEfficiency(stats.avgEfficiency))}<small>個/時</small></span></div>
      </div>
    </div>"""

    private fun panelSection(
        title: String,
        theme: String,
        tableHtml: String,
        subtitle: String? = null,
        titleInline: String? = null,
        badges: String = "",
        chartFileName: String? = null,
        chartAlt: String? = null,
        pageBreak: Boolean = false,
        chartTall: Boolean = false,
        hideChart: Boolean = false,
        flowBreak: Boolean = false,
    ): String {
        val chartClass = if (chartTall) "chart-wrap chart-wrap--tall" else "chart-wrap"
        val chartHtml = when {
            hideChart -> ""
            !chartFileName.isNullOrBlank() -> {
                val safeName = chartFileName
                    .replace("&", "")
                    .replace("\"", "")
                    .replace("'", "")
                    .replace("<", "")
                    .replace(">", "")
                """<div class="$chartClass"><img class="chart-img" src="$safeName" alt="${esc(chartAlt ?: title)}" /></div>"""
            }
            else -> """<p class="chart-empty">グラフを表示できません</p>"""
        }
        val titleHtml = if (titleInline != null) {
            """<div class="panel__title-row">
              <span class="panel__title">${esc(title)}</span>
              <span class="panel__title-inline">${esc(titleInline)}</span>
            </div>"""
        } else {
            """<div class="panel__title">${esc(title)}</div>
            ${subtitle?.let { """<div class="panel__subtitle">${esc(it)}</div>""" }.orEmpty()}"""
        }
        val flowClass = if (flowBreak) " panel--flow" else ""
        val breakClass = if (pageBreak) " panel--break" else ""
        val badgesHtml = if (badges.isNotEmpty()) """<div class="panel__badges">$badges</div>""" else ""
        return """<section class="panel panel--$theme$breakClass$flowClass">
          <div class="panel__head">
            <div class="panel__titles">$titleHtml</div>
            $badgesHtml
          </div>
          $chartHtml
          $tableHtml
        </section>"""
    }

    private fun panelBadge(text: String, tone: String): String =
        """<span class="panel-badge panel-badge--$tone">${esc(text)}</span>"""

    private fun kpiCardHtml(card: IpaKpiCard): String {
        val tone = when (card.tone) {
            IpaKpiTone.Indigo -> "indigo"
            IpaKpiTone.Sky -> "sky"
            IpaKpiTone.Amber -> "amber"
            IpaKpiTone.Emerald -> "emerald"
            IpaKpiTone.Violet -> "violet"
        }
        return """<div class="kpi-card kpi-card--$tone">
          <div class="kpi-card__accent"></div>
          <div class="kpi-card__body">
            <div class="kpi-card__label">${esc(card.label)}</div>
            <div class="kpi-card__value">${esc(card.value)}</div>
            <div class="kpi-card__hint">${esc(card.hint)}</div>
          </div>
        </div>"""
    }

    private fun rankBadgeHtml(rank: Int): String {
        val cls = when (rank) {
            1 -> "rank-badge rank-badge--gold"
            2 -> "rank-badge rank-badge--silver"
            3 -> "rank-badge rank-badge--bronze"
            else -> "rank-badge"
        }
        return """<span class="$cls">${esc(rank.toString())}</span>"""
    }

    private fun operatorRowRankHtml(index: Int): String {
        val cls = when (index) {
            0 -> "row-rank row-rank--gold"
            1 -> "row-rank row-rank--silver"
            2 -> "row-rank row-rank--bronze"
            else -> "row-rank"
        }
        return """<span class="$cls">${esc((index + 1).toString())}</span>"""
    }

    private fun documentShell(
        filters: WeldingProductivityReportFilters,
        mode: PrintMode = PrintMode.SECTION,
        orientation: PrintOrientation = PrintOrientation.PORTRAIT,
        sectionTitle: String? = null,
        includeKpi: Boolean = false,
        kpiCards: List<IpaKpiCard> = emptyList(),
        body: String,
    ): String {
        val printedAt = printedAt()
        val kpiHtml = if (includeKpi && kpiCards.isNotEmpty()) {
            """<div class="kpi-row">${kpiCards.joinToString("") { kpiCardHtml(it) }}</div>"""
        } else {
            ""
        }
        val metaHtml = metaLineHtml(filters, printedAt)
        val sectionHtml = sectionTitle?.let { """<div class="hd__section">${esc(it)}</div>""" }.orEmpty()
        val titleSuffix = sectionTitle?.let { " — $it" }.orEmpty()
        return """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>溶接工程 — 生産性分析$titleSuffix</title>
  <style>${printStyles(mode, orientation)}</style>
</head>
<body>
  <header class="hd">
    <div class="hd__title">溶接工程 — 生産性分析</div>
    $sectionHtml
    $metaHtml
  </header>
  $kpiHtml
  $body
  <footer class="ft">Smart-EMAPs · 溶接生産性分析 · ${esc(printedAt)}</footer>
</body>
</html>"""
    }

    private fun batchDocumentShell(
        title: String,
        mode: PrintMode,
        orientation: PrintOrientation,
        body: String,
        printedAt: String,
    ): String = """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <title>$title</title>
  <style>${printStyles(mode, orientation)}</style>
</head>
<body>
  $body
  <footer class="ft">Smart-EMAPs · 溶接生産性分析 · ${esc(printedAt)}</footer>
</body>
</html>"""

    private fun dailyBatchPageHeader(
        filters: WeldingProductivityReportFilters,
        printedAt: String,
    ): String = """<header class="hd">
    <div class="hd__title">溶接工程 — 生産性分析</div>
    <div class="hd__section">日別推移（溶接作業者別）</div>
    ${metaLineHtml(filters, printedAt, compact = true)}
  </header>"""

    private fun operatorProductBatchPageHeader(
        filters: WeldingProductivityReportFilters,
        printedAt: String,
    ): String = """<header class="hd">
    <div class="hd__title">溶接工程 — 生産性分析</div>
    <div class="hd__section">溶接作業者別製品別</div>
    ${metaLineHtml(filters, printedAt, compact = true)}
  </header>"""

    private fun metaLineHtml(
        filters: WeldingProductivityReportFilters,
        printedAt: String,
        compact: Boolean = false,
    ): String = if (compact) {
        """<div class="meta-line">
      <span><span class="meta-line__label">集計期間</span> ${esc(filters.startDate)} ～ ${esc(filters.endDate)}</span>
      <span class="meta-line__sep">|</span>
      <span><span class="meta-line__label">出力日時</span> ${esc(printedAt)}</span>
    </div>"""
    } else {
        """<div class="meta-line">
      <span><span class="meta-line__label">集計期間</span> ${esc(filters.startDate)} ～ ${esc(filters.endDate)}</span>
      <span class="meta-line__sep">|</span>
      <span><span class="meta-line__label">出力日時</span> ${esc(printedAt)}</span>
      <span class="meta-line__sep">|</span>
      <span><span class="meta-line__label">溶接作業者</span> ${esc(filters.operatorLabel)}</span>
      <span class="meta-line__sep">|</span>
      <span><span class="meta-line__label">製品</span> ${esc(filters.productLabel)}</span>
      <span class="meta-line__sep">|</span>
      <span><span class="meta-line__label">未確定を含む</span> ${esc(if (filters.includeIncomplete) "はい" else "いいえ")}</span>
    </div>"""
    }

    private fun printStyles(mode: PrintMode, orientation: PrintOrientation): String {
        val compact = mode == PrintMode.FULL
        val landscape = orientation == PrintOrientation.LANDSCAPE
        val chartTall = if (landscape) "480px" else if (compact) "96px" else "280px"
        val chartNormal = if (landscape) "480px" else if (compact) "82px" else "220px"
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
    body {
      margin: 0;
      color: #0f172a;
      font: 10px/1.4 "Segoe UI", "Yu Gothic UI", "Hiragino Sans", Meiryo, sans-serif;
      background: #fff;
    }
    .hd {
      border-bottom: 2px solid #6366f1;
      padding-bottom: 10px;
      margin-bottom: 10px;
    }
    .hd__title { font-size: 17px; font-weight: 800; letter-spacing: -0.02em; }
    .hd__section { margin-top: 4px; font-size: 12px; font-weight: 700; color: #4338ca; }
    .meta-line {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 2px 0;
      margin-top: 6px;
      font-size: 8.5px;
      line-height: 1.5;
      color: #475569;
    }
    .meta-line__label { font-weight: 700; color: #334155; }
    .meta-line__sep { margin: 0 8px; color: #cbd5e1; }
    .kpi-row {
      display: grid;
      grid-template-columns: repeat(5, minmax(0, 1fr));
      gap: 5px;
      margin-bottom: 6px;
    }
    .kpi-card {
      position: relative;
      border-radius: 8px;
      padding: 6px 7px 5px;
      border: 1px solid transparent;
      overflow: hidden;
      min-height: $kpiMinH;
    }
    .kpi-card__accent { position: absolute; top: 0; left: 0; right: 0; height: 3px; }
    .kpi-card__label { font-size: 7.5px; font-weight: 700; letter-spacing: 0.02em; }
    .kpi-card__value { margin-top: 3px; font-size: 15px; font-weight: 800; line-height: 1.1; }
    .kpi-card__hint { margin-top: 2px; font-size: 7px; font-weight: 600; }
    .kpi-card--indigo { background: linear-gradient(160deg, #fff 0%, #eef2ff 100%); border-color: rgba(99,102,241,.2); }
    .kpi-card--indigo .kpi-card__accent { background: linear-gradient(90deg, #6366f1, #818cf8); }
    .kpi-card--indigo .kpi-card__label { color: #6366f1; }
    .kpi-card--indigo .kpi-card__value { color: #4338ca; }
    .kpi-card--indigo .kpi-card__hint { color: #818cf8; }
    .kpi-card--sky { background: linear-gradient(160deg, #fff 0%, #e0f2fe 100%); border-color: rgba(14,165,233,.22); }
    .kpi-card--sky .kpi-card__accent { background: linear-gradient(90deg, #0ea5e9, #38bdf8); }
    .kpi-card--sky .kpi-card__label { color: #0284c7; }
    .kpi-card--sky .kpi-card__value { color: #0369a1; }
    .kpi-card--sky .kpi-card__hint { color: #38bdf8; }
    .kpi-card--amber { background: linear-gradient(160deg, #fff 0%, #ffedd5 100%); border-color: rgba(249,115,22,.22); }
    .kpi-card--amber .kpi-card__accent { background: linear-gradient(90deg, #f97316, #fb923c); }
    .kpi-card--amber .kpi-card__label { color: #ea580c; }
    .kpi-card--amber .kpi-card__value { color: #c2410c; }
    .kpi-card--amber .kpi-card__hint { color: #fb923c; }
    .kpi-card--emerald { background: linear-gradient(160deg, #fff 0%, #d1fae5 100%); border-color: rgba(16,185,129,.22); }
    .kpi-card--emerald .kpi-card__accent { background: linear-gradient(90deg, #10b981, #34d399); }
    .kpi-card--emerald .kpi-card__label { color: #059669; }
    .kpi-card--emerald .kpi-card__value { color: #047857; font-size: 16px; }
    .kpi-card--emerald .kpi-card__hint { color: #34d399; }
    .kpi-card--violet { background: linear-gradient(160deg, #fff 0%, #ede9fe 100%); border-color: rgba(124,58,237,.22); }
    .kpi-card--violet .kpi-card__accent { background: linear-gradient(90deg, #7c3aed, #a78bfa); }
    .kpi-card--violet .kpi-card__label { color: #7c3aed; }
    .kpi-card--violet .kpi-card__value { color: #6d28d9; }
    .kpi-card--violet .kpi-card__hint { color: #a78bfa; }
    .panel {
      margin-top: 6px;
      padding: 6px 8px 8px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      background: #fff;
      break-inside: avoid-page;
      page-break-inside: avoid;
    }
    .panel--break { break-before: page; page-break-before: always; margin-top: 0; }
    .panel--flow {
      break-inside: auto;
      page-break-inside: auto;
      margin-top: 0;
    }
    .panel--flow .chart-wrap { break-inside: avoid; page-break-inside: avoid; }
    .panel--chart { border-color: rgba(226,232,240,.95); }
    .panel--operator { background: linear-gradient(165deg, #fff 0%, #f5f3ff 100%); border-color: rgba(99,102,241,.16); }
    .panel--product { background: linear-gradient(165deg, #fff 0%, #f0f9ff 100%); border-color: rgba(14,165,233,.16); }
    .panel--rank { background: linear-gradient(165deg, #fff 0%, #fffbeb 100%); border-color: rgba(245,158,11,.22); }
    .panel__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; margin-bottom: 6px; }
    .panel__title { font-size: 11px; font-weight: 800; color: #1e293b; }
    .panel__title-row { display: flex; align-items: baseline; flex-wrap: wrap; gap: 8px; }
    .panel__title-inline { font-size: 11px; font-weight: 700; color: #4338ca; }
    .panel__subtitle { margin-top: 1px; font-size: 8px; font-weight: 600; color: #64748b; }
    .print-page { break-inside: avoid-page; page-break-inside: avoid; }
    .print-page--break { break-before: page; page-break-before: always; margin-top: 0; }
    .print-page .hd { margin-bottom: 8px; }
    .print-page .panel { margin-top: 0; }
    .panel__badges { display: flex; flex-wrap: wrap; gap: 4px; justify-content: flex-end; }
    .panel-badge { display: inline-block; padding: 2px 7px; border-radius: 999px; font-size: 7.5px; font-weight: 700; white-space: nowrap; }
    .panel-badge--soft { color: #64748b; background: rgba(148,163,184,.14); }
    .panel-badge--chart { color: #6366f1; background: rgba(99,102,241,.1); }
    .panel-badge--operator { color: #4338ca; background: rgba(99,102,241,.12); border: 1px solid rgba(99,102,241,.18); }
    .panel-badge--inspector { color: #4338ca; background: rgba(99,102,241,.12); border: 1px solid rgba(99,102,241,.18); }
    .panel-badge--product { color: #0369a1; background: rgba(14,165,233,.12); border: 1px solid rgba(14,165,233,.2); }
    .panel-badge--rank { color: #b45309; background: rgba(251,191,36,.18); border: 1px solid rgba(245,158,11,.28); }
    .chart-wrap { margin-bottom: 6px; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; background: linear-gradient(180deg, #fafbfc 0%, #fff 100%); }
    .chart-wrap--tall .chart-img { max-height: $chartTall; }
    .chart-img { display: block; width: 100%; height: auto; max-height: $chartNormal; object-fit: contain; }
    .chart-empty { margin: 0 0 6px; color: #94a3b8; font-size: 8px; }
    table.data { width: 100%; border-collapse: collapse; table-layout: fixed; margin-top: 4px; }
    table.data th, table.data td { border: 1px solid #cbd5e1; padding: 3px 4px; vertical-align: middle; word-break: break-word; }
    table.data th { font-size: 7.5px; font-weight: 700; }
    table.data td { font-size: 8px; }
    table.data--operator th { background: linear-gradient(180deg, #ede9fe, #e0e7ff); color: #4338ca; }
    table.data--product th { background: linear-gradient(180deg, #e0f2fe, #bae6fd); color: #0369a1; }
    table.data--rank th, table.data--rank-overview th { background: linear-gradient(180deg, #fef3c7, #fde68a); color: #92400e; }
    table.data tbody tr:nth-child(even) { background: rgba(248,250,252,.85); }
    .data--compact th, .data--compact td { font-size: 6.5px; padding: 2px 3px; }
    .num { text-align: right; font-variant-numeric: tabular-nums; }
    .center { text-align: center; }
    .warn { color: #c2410c; font-weight: 700; }
    .product-cd { font-family: ui-monospace, monospace; font-size: 7.5px; font-weight: 700; color: #0369a1; }
    .product-cd--rank { color: #b45309; }
    .pill { display: inline-block; padding: 1px 5px; border-radius: 5px; font-size: 7.5px; font-weight: 700; }
    .pill--operator { color: #4338ca; background: rgba(99,102,241,.1); border: 1px solid rgba(99,102,241,.15); }
    .pill--product { color: #047857; background: rgba(16,185,129,.1); border: 1px solid rgba(16,185,129,.15); }
    .pill--rank { color: #b45309; background: rgba(251,191,36,.15); border: 1px solid rgba(245,158,11,.28); }
    .row-rank, .rank-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 16px; height: 16px; padding: 0 3px; border-radius: 5px; font-size: 7.5px; font-weight: 700; color: #64748b; background: rgba(148,163,184,.15); }
    .row-rank--gold, .rank-badge--gold { color: #92400e; background: linear-gradient(135deg, #fde68a, #fcd34d); }
    .row-rank--silver, .rank-badge--silver { color: #475569; background: linear-gradient(135deg, #e2e8f0, #cbd5e1); }
    .row-rank--bronze, .rank-badge--bronze { color: #9a3412; background: linear-gradient(135deg, #fed7aa, #fdba74); }
    .table-more { margin: 2px 0 0; font-size: 6.5px; color: #94a3b8; text-align: right; }
    .rank-hero { display: flex; flex-wrap: wrap; gap: 8px; padding: 8px 10px; margin-bottom: 6px; border-radius: 8px; background: linear-gradient(135deg, #fffbeb, #fff); border: 1px solid rgba(251,191,36,.35); }
    .rank-hero__cd { display: inline-block; font-family: ui-monospace, monospace; font-size: 9px; font-weight: 800; color: #b45309; padding: 2px 8px; border-radius: 6px; background: #fff; border: 1px solid rgba(245,158,11,.25); }
    .rank-hero__name { margin-top: 4px; font-size: 12px; font-weight: 800; color: #1e293b; }
    .rank-hero__stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 6px; flex: 1; min-width: 240px; }
    .rank-hero__stat { padding: 5px 6px; border-radius: 6px; background: rgba(255,255,255,.8); border: 1px solid #e2e8f0; }
    .rank-hero__stat--accent { background: linear-gradient(135deg, #fef3c7, #fde68a); border-color: rgba(245,158,11,.35); }
    .rank-hero__stat-label { display: block; font-size: 7px; font-weight: 700; color: #94a3b8; }
    .rank-hero__stat-val { display: block; margin-top: 1px; font-size: 11px; font-weight: 800; color: #0f172a; }
    .rank-hero__stat-val small { font-size: 7px; font-weight: 600; color: #64748b; margin-left: 2px; }
    .sub-panel__title { margin: 8px 0 4px; font-size: 9px; font-weight: 700; color: #78350f; }
    .empty { margin: 0; color: #94a3b8; font-size: 8px; }
    .ft { margin-top: 10px; padding-top: 6px; border-top: 1px solid #e2e8f0; font-size: 7.5px; color: #94a3b8; text-align: right; }
    @media print {
      body { margin: 0; }
      .panel { box-shadow: none; }
      table.data thead { display: table-header-group; }
      table.data tr { break-inside: avoid; page-break-inside: avoid; }
    }
        """.trimIndent()
    }

    private fun tableHead(cells: List<String>): String =
        "<tr>${cells.joinToString("") { "<th>${esc(it)}</th>" }}</tr>"

    private fun emptyNote(message: String = "データなし"): String =
        """<p class="empty">${esc(message)}</p>"""

    private fun printedAt(): String = LocalDateTime.now().format(printedAtFormatter)

    private fun esc(value: String?): String = (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun esc(value: Int): String = esc(value.toString())
}
