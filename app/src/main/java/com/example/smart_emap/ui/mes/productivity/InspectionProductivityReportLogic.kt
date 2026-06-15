package com.example.smart_emap.ui.mes.productivity

import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionProductivityDailyRowDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorMetricsRowDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorRowDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.InspectionProductivityProductRowDto
import com.example.smart_emap.data.model.InspectionProductivitySessionRowDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class InspectionProductivityPrintContext(
    val filters: InspectionProductivityReportFilters,
    val kpiCards: List<IpaKpiCard>,
    val inspectorRows: List<InspectionProductivityInspectorRowDto>,
    val productRows: List<InspectionProductivityProductRowDto>,
    val inspectorSectionAvgEfficiency: Double?,
    val productSectionTotalQty: Int,
    val weldRankOff: List<InspectorAvgRankRow>,
    val weldRankOn: List<InspectorAvgRankRow>,
    val productRankList: List<InspectionProductivityProductRankingDto>,
    val selectedProductRanking: InspectionProductivityProductRankingDto?,
    val productRankTopOverview: List<InspectionProductivityProductRankingDto>,
)

object InspectionProductivityReportLogic {
    private val printedAtFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    fun reportMenuItems(): List<IpaReportMenuItem> = InspectionProductivityLogic.reportMenuItems()

    fun buildPrintHtml(
        command: InspectionProductivityReportCommand,
        data: InspectionProductivityAnalysisDataDto,
        ctx: InspectionProductivityPrintContext,
    ): String = when (command) {
        InspectionProductivityReportCommand.PRINT_FULL -> buildFullPrintHtml(data, ctx)
        InspectionProductivityReportCommand.PRINT_DAILY -> buildSectionHtml(data, ctx, "日別推移", dailyTable(data.daily.orEmpty()))
        InspectionProductivityReportCommand.PRINT_INSPECTOR -> buildSectionHtml(
            data,
            ctx,
            "検査員別",
            inspectorTable(ctx.inspectorRows),
        )
        InspectionProductivityReportCommand.PRINT_PRODUCT -> buildSectionHtml(
            data,
            ctx,
            "製品別",
            productTable(ctx.productRows),
        )
        InspectionProductivityReportCommand.PRINT_WELD_RANK -> buildSectionHtml(
            data,
            ctx,
            "検査員平均能率ランキング",
            weldRankTables(ctx.weldRankOff, ctx.weldRankOn),
        )
        InspectionProductivityReportCommand.PRINT_PRODUCT_RANK -> buildSectionHtml(
            data,
            ctx,
            "製品別 · 検査員能率ランキング",
            productRankSection(ctx.selectedProductRanking, ctx.productRankTopOverview),
        )
        InspectionProductivityReportCommand.PRINT_DAILY_BATCH -> buildDailyBatchHtml(data, ctx)
        InspectionProductivityReportCommand.PRINT_INSPECTOR_PRODUCT_BATCH -> buildInspectorProductBatchHtml(data, ctx)
        InspectionProductivityReportCommand.PRINT_INSPECTOR_METRICS -> throw IllegalStateException("Use buildInspectorMetricsPrintHtml")
    }

    fun buildInspectorMetricsPrintHtml(
        filters: InspectionProductivityReportFilters,
        metrics: InspectorMetricsPrepared,
        kpiCards: List<IpaKpiCard>,
    ): String {
        val body = buildString {
            append(kpiHtml(kpiCards))
            append(
                sectionBlock(
                    "検査員別指標 · 時間 / 生産",
                    inspectorMetricsTimeTable(metrics),
                ),
            )
            append(
                sectionBlock(
                    "検査員別指標 · 不良内訳",
                    inspectorMetricsDefectTable(metrics),
                ),
            )
        }
        return inspectorMetricsDocumentShell(filters, body)
    }

    private fun inspectorMetricsDocumentShell(
        filters: InspectionProductivityReportFilters,
        body: String,
    ): String {
        val printedAt = LocalDateTime.now().format(printedAtFormatter)
        val sectionTitle = "検査員別指標表"
        val metaHtml = metaLineHtml(filters, printedAt)
        return """
            <!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/>
            <title>検査工程 — 生産性分析 — $sectionTitle</title>
            <style>
            html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
            @page { size: A4 landscape; margin: 8mm 10mm; }
            body { margin:0; color:#0f172a; font:10px/1.4 sans-serif; }
            .hd { border-bottom:2px solid #6366f1; padding-bottom:8px; margin-bottom:10px; }
            .hd__title { font-size:16px; font-weight:800; }
            .hd__section { font-size:12px; font-weight:700; color:#4338ca; }
            .hd__section-row { display:flex; align-items:center; flex-wrap:wrap; gap:6px 14px; margin-top:4px; }
            .hd__section-row .hd__section { margin-top:0; flex-shrink:0; }
            .meta-line { display:flex; flex-wrap:wrap; align-items:center; font-size:8.5px; color:#475569; flex:1; justify-content:flex-end; }
            .meta-line__sep { margin:0 8px; color:#cbd5e1; }
            .panel { margin-top:8px; padding:8px; border:1px solid #e2e8f0; border-radius:8px; break-inside:avoid; }
            .panel__title { font-size:11px; font-weight:800; margin-bottom:6px; color:#4338ca; }
            table.data { width:100%; border-collapse:collapse; table-layout:fixed; }
            table.data th, table.data td { border:1px solid #cbd5e1; padding:1.8px 2.7px; font-size:6.5px; word-break:break-word; vertical-align:middle; line-height:1.26; }
            table.data th { background:linear-gradient(180deg,#ede9fe,#e0e7ff); color:#4338ca; font-weight:700; }
            table.data td.name { font-weight:700; color:#334155; }
            table.data tr.row-total td { font-weight:800; border-top:2px dashed rgba(99,102,241,.45); background:rgba(238,242,255,.55); }
            table.data tr.row-support td { background:#fff; }
            .num { text-align:right; font-variant-numeric:tabular-nums; }
            .num--good { color:#047857; font-weight:700; }
            .pill { display:inline-block; padding:1px 4px; border-radius:4px; font-size:6.5px; font-weight:700; color:#4338ca; background:rgba(99,102,241,.1); border:1px solid rgba(99,102,241,.15); }
            .kpi-row { display:grid; grid-template-columns:repeat(5,minmax(0,1fr)); gap:5px; margin-bottom:8px; }
            .kpi-card { border:1px solid #e2e8f0; border-radius:8px; padding:6px; }
            .kpi-card__label { font-size:7px; font-weight:700; color:#64748b; }
            .kpi-card__value { font-size:14px; font-weight:800; margin-top:2px; }
            .kpi-card__hint { font-size:7px; color:#94a3b8; margin-top:2px; }
            .empty { color:#94a3b8; font-size:8px; }
            </style></head><body>
            <header class="hd">
              <div class="hd__title">検査工程 — 生産性分析</div>
              <div class="hd__section-row">
                <div class="hd__section">$sectionTitle</div>
                $metaHtml
              </div>
            </header>
            $body
            <footer style="margin-top:10px;padding-top:6px;border-top:1px solid #e2e8f0;font-size:7.5px;color:#94a3b8;text-align:right;">Smart-EMAPs · 検査生産性分析 · $printedAt</footer>
            </body></html>
        """.trimIndent()
    }

    private fun metaLineHtml(filters: InspectionProductivityReportFilters, printedAt: String): String =
        """<div class="meta-line">
            <span><b>集計期間</b> ${esc(filters.startDate)} ～ ${esc(filters.endDate)}</span>
            <span class="meta-line__sep">|</span>
            <span><b>出力日時</b> ${esc(printedAt)}</span>
            <span class="meta-line__sep">|</span>
            <span><b>検査員</b> ${esc(filters.inspectorLabel)}</span>
            <span class="meta-line__sep">|</span>
            <span><b>製品</b> ${esc(filters.productLabel)}</span>
            <span class="meta-line__sep">|</span>
            <span><b>未確定を含む</b> ${if (filters.includeIncomplete) "はい" else "いいえ"}</span>
        </div>"""

    private fun inspectorMetricsTimeTable(metrics: InspectorMetricsPrepared): String {
        if (metrics.rows.isEmpty()) return emptyNote()
        val head = tableHead(
            listOf("検査員", "シフト", "休憩", "ロス時間", "作業すべき時間", "作業時間", "作業率", "検査総数", "能率", "稼働率"),
        )
        val body = buildString {
            metrics.rows.forEach { append(inspectorMetricsTimeRow(it)) }
            if (InspectionProductivityLogic.inspectorMetricsRowHasActivity(metrics.supportRow, metrics.defectHeaders)) {
                append(inspectorMetricsTimeRow(metrics.supportRow, support = true))
            }
            append(inspectorMetricsTimeRow(metrics.totalRow, total = true))
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun inspectorMetricsDefectTable(metrics: InspectorMetricsPrepared): String {
        if (metrics.rows.isEmpty()) return emptyNote()
        val defectHead = metrics.defectHeaders.map { InspectionProductivityLogic.metricsDefectHeaderLabel(it) }
        val head = tableHead(listOf("検査員") + defectHead + listOf("不良合計"))
        val body = buildString {
            metrics.rows.forEach { append(inspectorMetricsDefectRow(it, metrics.defectHeaders)) }
            if (InspectionProductivityLogic.inspectorMetricsRowHasActivity(metrics.supportRow, metrics.defectHeaders)) {
                append(inspectorMetricsDefectRow(metrics.supportRow, metrics.defectHeaders, support = true))
            }
            append(inspectorMetricsDefectRow(metrics.totalRow, metrics.defectHeaders, total = true))
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun inspectorMetricsTimeRow(
        row: InspectionProductivityInspectorMetricsRowDto,
        total: Boolean = false,
        support: Boolean = false,
    ): String {
        val cls = when {
            total -> "row-total"
            support -> "row-support"
            else -> ""
        }
        val eff = InspectionProductivityLogic.fmtMetricEfficiencyDecimal(row.efficiencyPerHour)
        val effHtml = if (eff == "—") esc(eff) else """<span class="pill">$eff</span>"""
        return """<tr class="$cls">
            <td class="name">${esc(row.inspectorName)}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricHours(row.shiftHours))}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricHours(row.breakHours))}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricHours(row.stopHours))}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricHours(row.targetWorkHours))}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricHours(row.workHours))}</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.workRatePercent))}</td>
            <td class="num num--good">${esc(InspectionProductivityLogic.fmtMetricQtyDisplay(row.sumInspectionQty))}</td>
            <td class="num">$effHtml</td>
            <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.operatingRatePercent))}</td>
        </tr>"""
    }

    private fun inspectorMetricsDefectRow(
        row: InspectionProductivityInspectorMetricsRowDto,
        defectHeaders: List<String>,
        total: Boolean = false,
        support: Boolean = false,
    ): String {
        val cls = when {
            total -> "row-total"
            support -> "row-support"
            else -> ""
        }
        val defectCells = defectHeaders.joinToString("") { header ->
            val qty = row.defects?.get(header) ?: 0
            """<td class="num">${esc(InspectionProductivityLogic.fmtMetricQtyDisplay(qty))}</td>"""
        }
        val defectTotal = InspectionProductivityLogic.sumInspectorMetricsDefectQty(row, defectHeaders)
        return """<tr class="$cls">
            <td class="name">${esc(row.inspectorName)}</td>
            $defectCells
            <td class="num">${esc(InspectionProductivityLogic.fmtMetricQtyDisplay(defectTotal))}</td>
        </tr>"""
    }

    private fun buildFullPrintHtml(
        data: InspectionProductivityAnalysisDataDto,
        ctx: InspectionProductivityPrintContext,
    ): String {
        val body = buildString {
            append(kpiHtml(ctx.kpiCards))
            append(sectionBlock("日別推移", dailyTable(data.daily.orEmpty())))
            append(sectionBlock("検査員別", inspectorTable(ctx.inspectorRows)))
            append(sectionBlock("製品別", productTable(ctx.productRows)))
            append(sectionBlock("検査員平均能率ランキング", weldRankTables(ctx.weldRankOff, ctx.weldRankOn)))
            append(sectionBlock("製品別 · 検査員能率ランキング", productRankSection(ctx.selectedProductRanking, ctx.productRankTopOverview)))
            append(sectionBlock("セッション明細", sessionTable(data.sessions.orEmpty())))
        }
        return documentShell(ctx.filters, "印刷（全体）", body, landscape = false)
    }

    private fun buildSectionHtml(
        data: InspectionProductivityAnalysisDataDto,
        ctx: InspectionProductivityPrintContext,
        title: String,
        tableHtml: String,
    ): String = documentShell(ctx.filters, title, sectionBlock(title, tableHtml), landscape = title.contains("日別"))

    fun buildDailyBatchPrintHtml(
        filters: InspectionProductivityReportFilters,
        items: List<Pair<String, List<InspectionProductivityDailyRowDto>>>,
    ): String {
        val body = items.joinToString("") { (label, daily) ->
            pageBlock(label, dailyTable(daily))
        }
        return documentShell(
            filters,
            "日別推移（検査員別）",
            body.ifBlank { emptyNote() },
            landscape = true,
        )
    }

    fun buildInspectorProductBatchPrintHtml(
        filters: InspectionProductivityReportFilters,
        items: List<Pair<String, List<InspectorProductDisplayRow>>>,
    ): String {
        val body = items.joinToString("") { (label, rows) ->
            pageBlock(label, inspectorProductTable(rows))
        }
        return documentShell(
            filters,
            "検査員別製品別",
            body.ifBlank { emptyNote() },
            landscape = false,
        )
    }

    private fun buildDailyBatchHtml(
        data: InspectionProductivityAnalysisDataDto,
        ctx: InspectionProductivityPrintContext,
    ): String = buildDailyBatchPrintHtml(ctx.filters, emptyList())

    private fun buildInspectorProductBatchHtml(
        data: InspectionProductivityAnalysisDataDto,
        ctx: InspectionProductivityPrintContext,
    ): String = buildInspectorProductBatchPrintHtml(ctx.filters, emptyList())

    private fun documentShell(
        filters: InspectionProductivityReportFilters,
        sectionTitle: String,
        body: String,
        landscape: Boolean,
    ): String {
        val printedAt = LocalDateTime.now().format(printedAtFormatter)
        val pageRule = if (landscape) "@page { size: A4 landscape; margin: 8mm 10mm; }" else "@page { size: A4 portrait; margin: 10mm 12mm; }"
        return """
            <!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/>
            <title>検査工程 — 生産性分析 — $sectionTitle</title>
            <style>
            html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
            $pageRule
            body { margin:0; color:#0f172a; font:10px/1.4 sans-serif; }
            .hd { border-bottom:2px solid #6366f1; padding-bottom:8px; margin-bottom:10px; }
            .hd__title { font-size:16px; font-weight:800; }
            .hd__section { margin-top:4px; font-size:12px; font-weight:700; color:#4338ca; }
            .meta { margin-top:6px; font-size:8.5px; color:#475569; }
            .panel { margin-top:8px; padding:8px; border:1px solid #e2e8f0; border-radius:8px; break-inside:avoid; }
            .panel__title { font-size:11px; font-weight:800; margin-bottom:6px; }
            .print-page { break-inside:avoid; }
            .print-page--break { break-before:page; page-break-before:always; margin-top:0; }
            table.data { width:100%; border-collapse:collapse; table-layout:fixed; }
            table.data th, table.data td { border:1px solid #cbd5e1; padding:3px 4px; font-size:8px; word-break:break-word; }
            table.data th { background:#eef2ff; color:#4338ca; font-weight:700; }
            .num { text-align:right; }
            .center { text-align:center; }
            .kpi-row { display:grid; grid-template-columns:repeat(5,minmax(0,1fr)); gap:5px; margin-bottom:8px; }
            .kpi-card { border:1px solid #e2e8f0; border-radius:8px; padding:6px; }
            .kpi-card__label { font-size:7px; font-weight:700; color:#64748b; }
            .kpi-card__value { font-size:14px; font-weight:800; margin-top:2px; }
            .kpi-card__hint { font-size:7px; color:#94a3b8; margin-top:2px; }
            .sub-title { font-size:9px; font-weight:700; color:#047857; margin:8px 0 4px; }
            .empty { color:#94a3b8; font-size:8px; }
            </style></head><body>
            <header class="hd">
              <div class="hd__title">検査工程 — 生産性分析</div>
              <div class="hd__section">$sectionTitle</div>
              <div class="meta">期間 ${filters.startDate} ～ ${filters.endDate} · 検査員 ${filters.inspectorLabel} · 製品 ${filters.productLabel} · 未確定を含む ${if (filters.includeIncomplete) "はい" else "いいえ"} · 出力 $printedAt</div>
            </header>
            $body
            <footer style="margin-top:10px;padding-top:6px;border-top:1px solid #e2e8f0;font-size:7.5px;color:#94a3b8;text-align:right;">Smart-EMAPs · 検査生産性分析 · $printedAt</footer>
            </body></html>
        """.trimIndent()
    }

    private fun sectionBlock(title: String, tableHtml: String): String =
        """<section class="panel"><div class="panel__title">${esc(title)}</div>$tableHtml</section>"""

    private fun pageBlock(titleInline: String, tableHtml: String): String =
        """<div class="print-page print-page--break"><section class="panel"><div class="panel__title">${esc(titleInline)}</div>$tableHtml</section></div>"""

    private fun kpiHtml(cards: List<IpaKpiCard>): String {
        val items = cards.joinToString("") { card ->
            """<div class="kpi-card"><div class="kpi-card__label">${esc(card.label)}</div><div class="kpi-card__value">${esc(card.value)}</div><div class="kpi-card__hint">${esc(card.hint)}</div></div>"""
        }
        return """<div class="kpi-row">$items</div>"""
    }

    private fun dailyTable(rows: List<InspectionProductivityDailyRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("日付", "件数", "確定", "生産", "不良", "不良率", "能率"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td>${esc(row.day)}</td>
              <td class="num">${row.sessionCount ?: 0}</td>
              <td class="num">${row.completedSessionCount ?: 0}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumDefectQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun inspectorTable(rows: List<InspectionProductivityInspectorRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("#", "検査員", "件", "生産", "不良率", "平均能率"))
        val body = rows.mapIndexed { index, row ->
            """<tr>
              <td class="center">${index + 1}</td>
              <td>${esc(row.inspectorName)}</td>
              <td class="num">${row.sessionCount ?: 0}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</td>
            </tr>"""
        }.joinToString("")
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun productTable(rows: List<InspectionProductivityProductRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("CD", "製品名", "件", "生産", "不良率", "能率"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td>${esc(row.productCd)}</td>
              <td>${esc(row.productName)}</td>
              <td class="num">${row.sessionCount ?: 0}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun weldRankTables(off: List<InspectorAvgRankRow>, on: List<InspectorAvgRankRow>): String =
        """<div class="sub-title">溶接工程なし製品</div>${weldRankTable(off)}
           <div class="sub-title">溶接工程あり製品</div>${weldRankTable(on)}"""

    private fun weldRankTable(rows: List<InspectorAvgRankRow>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("順位", "検査員", "件", "生産", "不良率", "平均能率"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td class="center">${row.rank}</td>
              <td>${esc(row.inspectorName)}</td>
              <td class="num">${row.sessionCount}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.avgEfficiencyPerHour))}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun productRankSection(
        selected: InspectionProductivityProductRankingDto?,
        overview: List<InspectionProductivityProductRankingDto>,
    ): String {
        val selectedHtml = selected?.let {
            """<div class="sub-title">${esc(it.productCd)} · ${esc(it.productName)}</div>${rankInspectorTable(it.inspectors.orEmpty())}"""
        }.orEmpty()
        val overviewHtml = if (overview.isEmpty()) "" else {
            """<div class="sub-title">全製品 · 能率 TOP1 一覧</div>${rankOverviewTable(overview)}"""
        }
        return selectedHtml + overviewHtml
    }

    private fun rankInspectorTable(rows: List<InspectionProductivityInspectorRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("順位", "検査員", "件", "生産", "能率", "不良率"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td class="center">${row.rank ?: ""}</td>
              <td>${esc(row.inspectorName)}</td>
              <td class="num">${row.sessionCount ?: 0}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun rankOverviewTable(rows: List<InspectionProductivityProductRankingDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("CD", "製品名", "TOP検査員", "能率", "対象人数"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td>${esc(row.productCd)}</td>
              <td>${esc(row.productName)}</td>
              <td>${esc(row.topInspectorName ?: "—")}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour))}</td>
              <td class="num">${row.rankedInspectorCount ?: 0}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun inspectorProductTable(rows: List<InspectorProductDisplayRow>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("CD", "製品名", "件", "生産", "不良率", "能率"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td>${esc(row.productCd)}</td>
              <td>${esc(row.productName)}</td>
              <td class="num">${row.sessionCount}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.sumActualQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.avgEfficiencyPerHour))}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun sessionTable(rows: List<InspectionProductivitySessionRowDto>): String {
        if (rows.isEmpty()) return emptyNote()
        val head = tableHead(listOf("生産日", "検査員", "CD", "製品名", "生産", "不良", "不良率", "能率", "稼働", "停止", "状態"))
        val body = rows.joinToString("") { row ->
            """<tr>
              <td>${esc(row.productionDay)}</td>
              <td>${esc(row.inspectorDisplayName ?: row.mesInspectorName)}</td>
              <td>${esc(row.productCd)}</td>
              <td>${esc(row.productName)}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.actualProductionQuantity)}</td>
              <td class="num">${InspectionProductivityLogic.fmtInt(row.defectQty)}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtPct(row.defectRatePercent))}</td>
              <td class="num">${esc(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))}</td>
              <td class="num">${row.netProductionMin ?: "—"}</td>
              <td class="num">${row.pausedMin ?: "—"}</td>
              <td>${if (row.isCompleted == true) "確定" else "未確定"}</td>
            </tr>"""
        }
        return """<table class="data"><thead>$head</thead><tbody>$body</tbody></table>"""
    }

    private fun tableHead(cells: List<String>): String =
        "<tr>${cells.joinToString("") { "<th>${esc(it)}</th>" }}</tr>"

    private fun emptyNote(): String = """<p class="empty">データなし</p>"""

    private fun esc(value: String?): String = (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
