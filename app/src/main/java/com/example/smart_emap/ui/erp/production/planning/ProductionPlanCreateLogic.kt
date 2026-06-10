package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.EquipmentEfficiencyRowDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.MasterProductDto
import com.example.smart_emap.data.model.ProductProcessBomRowDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class PlanCreateKind(val title: String, val machineLabel: String) {
    Molding("成型計画作成", "成型機"),
    Welding("溶接計画作成", "溶接機"),
}

data class PlanCreateFormState(
    val month: String = "",
    val baseDate: String = "",
    val workingDays: Int = 20,
    val coefficient: Double = 1.043,
    val clearFromDate: String = "",
    val results: List<PlanCreateResultRow> = emptyList(),
)

data class PlanCreateResultRow(
    val lookupDate: String,
    val machine: String,
    val productCd: String,
    val productName: String,
    val trendRaw: Int,
    val requiredQty: Int,
    val lotSize: Int,
    val lotCount: Int,
    val batchQty: Int,
    val dailyQty: Int,
    val efficiencyRate: Double?,
    val processHours: Double,
)

data class PlanBomUiRow(
    val productCd: Int,
    val productName: String,
    val safetyStockDays: Int,
    val processLt: Int,
)

object ProductionPlanCreateLogic {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE
    private val outputTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm", Locale.JAPAN)

    fun defaultFormState(): PlanCreateFormState {
        val month = currentPlanMonth()
        val base = baseDateFromPlanMonth(month)
        return PlanCreateFormState(
            month = month,
            baseDate = base,
            workingDays = calcWorkingDaysFallback(month),
            coefficient = 1.043,
            clearFromDate = base,
        )
    }

    fun currentPlanMonth(): String {
        val today = LocalDate.now()
        return "%04d-%02d".format(today.year, today.monthValue)
    }

    fun baseDateFromPlanMonth(yearMonth: String): String {
        val trimmed = yearMonth.trim()
        if (!Regex("""\d{4}-\d{2}""").matches(trimmed)) return ""
        val parts = trimmed.split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return ""
        val d = LocalDate.of(parts[0], parts[1], 1).plusMonths(1)
        return d.format(iso)
    }

    /** 月〜金のみ（会社カレンダー API 失敗時のフォールバック） */
    fun calcWorkingDaysFallback(yearMonth: String): Int {
        val trimmed = yearMonth.trim()
        if (!Regex("""\d{4}-\d{2}""").matches(trimmed)) return 20
        val parts = trimmed.split("-").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return 20
        val year = parts[0]
        val month = parts[1]
        val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()
        var count = 0
        for (d in 1..daysInMonth) {
            val dow = LocalDate.of(year, month, d).dayOfWeek.value
            if (dow < 6) count++
        }
        return count.coerceAtLeast(1)
    }

    fun calcWorkingDaysFallbackFromDate(dateIso: String): Int {
        val date = runCatching { LocalDate.parse(dateIso.take(10), iso) }.getOrNull() ?: return 20
        return calcWorkingDaysFallback("%04d-%02d".format(date.year, date.monthValue))
    }

    fun applyMonthChange(month: String): PlanCreateFormState {
        val base = baseDateFromPlanMonth(month)
        return PlanCreateFormState(
            month = month,
            baseDate = base,
            workingDays = calcWorkingDaysFallback(month),
            coefficient = 1.043,
            clearFromDate = base,
        )
    }

    fun addBusinessDays(dateStr: String, days: Int): String {
        if (dateStr.isBlank() || days <= 0) return dateStr
        var d = LocalDate.parse(dateStr.take(10), iso)
        var remaining = days
        while (remaining > 0) {
            d = d.plusDays(1)
            if (d.dayOfWeek.value < 6) remaining--
        }
        return d.format(iso)
    }

    fun summaryRowDate(row: ProductionSummaryFullRowDto): String =
        row.date?.trim()?.take(10).orEmpty()

    fun filterBomRows(
        rows: List<PlanBomUiRow>,
        products: List<MasterProductDto>,
    ): List<PlanBomUiRow> {
        val productMap = products.associateBy { it.productCd.orEmpty().trim() }
        return rows.filter { row ->
            val cd = row.productCd.toString().trim()
            if (cd.isBlank() || !cd.endsWith("1")) return@filter false
            if (row.productName.contains("加工") || row.productName.contains("アーチ")) return@filter false
            val p = productMap[cd] ?: return@filter false
            p.productType == "量産品" && p.status == "active"
        }.sortedBy { it.productName }
    }

    fun toBomRows(bom: List<ProductProcessBomRowDto>, kind: PlanCreateKind): List<PlanBomUiRow> =
        bom.mapNotNull { r ->
            val cd = r.productCd ?: return@mapNotNull null
            PlanBomUiRow(
                productCd = cd,
                productName = r.productName.orEmpty(),
                safetyStockDays = r.safetyStockDays ?: 0,
                processLt = when (kind) {
                    PlanCreateKind.Molding -> r.formingProcessLt ?: 0
                    PlanCreateKind.Welding -> r.weldingProcessLt ?: 0
                },
            )
        }

    fun buildEfficiencyResolver(
        effRows: List<EquipmentEfficiencyRowDto>,
        machines: List<MasterMachineFullDto>,
    ): (String, String) -> Double? {
        val nameToCd = machines.mapNotNull { m ->
            val n = m.machineName.orEmpty().trim()
            val c = m.machineCd.orEmpty().trim()
            if (n.isNotBlank() && c.isNotBlank()) n to c else null
        }.toMap()
        val byProdName = mutableMapOf<String, Double>()
        val byProdCd = mutableMapOf<String, Double>()
        effRows.forEach { e ->
            if (e.status == 0) return@forEach
            val p = e.productCd.orEmpty().trim()
            if (p.isBlank()) return@forEach
            val rate = e.efficiencyRate ?: return@forEach
            if (rate <= 0) return@forEach
            e.machinesName?.trim()?.takeIf { it.isNotBlank() }?.let { byProdName["$p|$it"] = rate }
            e.machineCd?.trim()?.takeIf { it.isNotBlank() }?.let { byProdCd["$p|$it"] = rate }
        }
        return { productCd, machine ->
            val p = productCd.trim()
            val mm = machine.trim()
            if (p.isBlank() || mm.isBlank()) {
                null
            } else {
                byProdName["$p|$mm"]?.takeIf { it > 0 }
                    ?: nameToCd[mm]?.let { mc -> byProdCd["$p|$mc"]?.takeIf { it > 0 } }
                    ?: byProdCd["$p|$mm"]?.takeIf { it > 0 }
            }
        }
    }

    fun calculate(
        kind: PlanCreateKind,
        baseDate: String,
        coefficient: Double,
        workingDays: Int,
        bomRows: List<ProductProcessBomRowDto>,
        summaryRows: List<ProductionSummaryFullRowDto>,
        lotSizeMap: Map<String, Int>,
        resolveEff: (String, String) -> Double?,
    ): List<PlanCreateResultRow> {
        val lookups = bomRows.mapNotNull { bom ->
            val cd = bom.productCd?.toString()?.trim().orEmpty()
            if (cd.isBlank()) return@mapNotNull null
            val lt = when (kind) {
                PlanCreateKind.Molding -> bom.formingProcessLt ?: 0
                PlanCreateKind.Welding -> bom.weldingProcessLt ?: 0
            }
            val safety = bom.safetyStockDays ?: 0
            val bizDays = lt + safety
            Triple(
                cd,
                bom.productName.orEmpty(),
                addBusinessDays(baseDate, bizDays),
            )
        }
        val summaryByKey = summaryRows.associateBy { row ->
            val pcd = row.productCd.orEmpty().trim()
            val ds = summaryRowDate(row)
            "$ds|$pcd"
        }
        val result = mutableListOf<PlanCreateResultRow>()
        lookups.forEach { (cd, name, lookupDate) ->
            val row = summaryByKey["$lookupDate|$cd"] ?: return@forEach
            val trend = when (kind) {
                PlanCreateKind.Molding -> row.moldingActualPlanTrend ?: 0
                PlanCreateKind.Welding -> row.weldingActualPlanTrend ?: 0
            }
            if (trend >= 0) return@forEach
            val requiredQty = kotlin.math.round(kotlin.math.abs(trend.toDouble()) * coefficient).toInt()
            val lotSize = lotSizeMap[cd] ?: 1
            val lotCount = if (lotSize > 0) kotlin.math.ceil(requiredQty.toDouble() / lotSize).toInt() else requiredQty
            val batchQty = lotCount * lotSize
            val dailyQty = if (workingDays > 0) kotlin.math.ceil(batchQty.toDouble() / workingDays).toInt() else batchQty
            val machine = when (kind) {
                PlanCreateKind.Molding -> row.moldingMachine.orEmpty()
                PlanCreateKind.Welding -> row.weldingMachine.orEmpty()
            }
            val effRate = resolveEff(cd, machine)
            val processHours = if (effRate != null && effRate > 0) batchQty / effRate else 0.0
            result += PlanCreateResultRow(
                lookupDate = lookupDate,
                machine = machine,
                productCd = cd,
                productName = row.productName.orEmpty().ifBlank { name },
                trendRaw = trend,
                requiredQty = requiredQty,
                lotSize = lotSize,
                lotCount = lotCount,
                batchQty = batchQty,
                dailyQty = dailyQty,
                efficiencyRate = effRate,
                processHours = processHours,
            )
        }
        return result.sortedWith(
            compareBy<PlanCreateResultRow> { it.machine.ifBlank { "\uFFFF" } }
                .thenBy { it.lookupDate }
                .thenBy { it.productName },
        )
    }

    fun formatInt(n: Int?): String =
        if (n == null) "—" else String.format(Locale.JAPAN, "%,d", n)

    fun formatEfficiency(v: Double?): String {
        if (v == null || v.isNaN() || v <= 0) return "—"
        val s = if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.JAPAN, "%.1f", v)
        return "${s}本/H"
    }

    fun formatHours(h: Double?): String {
        if (h == null || h.isNaN() || h <= 0) return "—"
        val s = if (h % 1.0 == 0.0) h.toInt().toString() else String.format(Locale.JAPAN, "%.2f", h)
        return "${s}h"
    }

    fun buildPrintHtml(
        kind: PlanCreateKind,
        form: PlanCreateFormState,
        rows: List<PlanCreateResultRow>,
    ): String {
        val sorted = rows.sortedWith(
            compareBy<PlanCreateResultRow> { it.machine.ifBlank { "\uFFFF" } }
                .thenBy { it.lookupDate }
                .thenBy { it.productCd },
        )
        val workDaysSafe = form.workingDays.coerceAtLeast(0)
        val totalsByMachine = sorted.groupBy { it.machine.ifBlank { "（未設定）" } }
            .mapValues { (_, list) ->
                list.sumOf { it.batchQty } to list.sumOf { it.processHours }
            }
        val now = java.time.LocalDateTime.now().format(outputTimeFormatter)
        val metaLine = listOf(
            "生産計画月：${form.month.ifBlank { "—" }}",
            "基準日：${form.baseDate.ifBlank { "—" }}",
            "稼働日：${form.workingDays}",
            "加工減耗係数：${form.coefficient}",
        ).joinToString("　")
        val totalPlanQty = sorted.sumOf { it.batchQty }
        var body = ""
        var prevMachine = ""
        sorted.forEach { r ->
            val machineName = r.machine.ifBlank { "（未設定）" }
            if (machineName != prevMachine) {
                if (prevMachine.isNotEmpty()) body += "</tbody></table></section>"
                val agg = totalsByMachine[machineName] ?: (0 to 0.0)
                val avgDaily = if (workDaysSafe > 0 && agg.second > 0) {
                    String.format(Locale.JAPAN, "%.2fh/日", agg.second / workDaysSafe)
                } else "—"
                val sumHours = if (agg.second > 0) String.format(Locale.JAPAN, "%.2fh", agg.second) else "—"
                body += """<section class="machine-group"><h2 class="machine-title">${kind.machineLabel}：$machineName　／　合計計画数：${formatInt(agg.first)}　／　合計工時：$sumHours　／　平均日稼働：$avgDaily</h2><table><thead><tr><th>対応日</th><th>製品名</th><th>必要数</th><th>ロットサイズ</th><th>ロット数</th><th>計画数</th><th>能率</th><th>工時</th></tr></thead><tbody>"""
                prevMachine = machineName
            }
            body += """<tr>
<td class="indent">${r.lookupDate}</td>
<td>${r.productName}</td>
<td class="num">${formatInt(r.requiredQty)}</td>
<td class="num">${formatInt(r.lotSize)}</td>
<td class="num">${r.lotCount}</td>
<td class="num batch">${formatInt(r.batchQty)}</td>
<td class="num">${formatEfficiency(r.efficiencyRate)}</td>
<td class="num">${formatHours(r.processHours)}</td>
</tr>"""
        }
        if (prevMachine.isNotEmpty()) body += "</tbody></table></section>"
        val title = "${kind.title} — 計算結果"
        return """<!DOCTYPE html><html lang="ja"><head><meta charset="UTF-8"/><title>$title</title>
<style>
@page { size: A4 portrait; margin: 10mm; }
body{font-family:sans-serif;margin:0;padding:12px;font-size:9pt;}
h1{font-size:13pt;margin:0 0 6px;font-weight:700;}
.meta{color:#475569;font-size:8.5pt;margin:0 0 8px;}
.sub{font-size:8pt;color:#64748b;margin:0 0 10px;}
table{border-collapse:collapse;width:100%;}
th,td{border:1px solid #cbd5e1;padding:4px 6px;}
th{background:#f1f5f9;font-weight:600;text-align:center;font-size:8pt;}
td.num{text-align:right;}
td.batch{font-weight:700;}
.machine-group{margin-bottom:10px;}
.machine-title{font-size:10pt;margin:0;padding:6px 8px;background:#e2e8f0;border:1px solid #cbd5e1;border-bottom:none;}
td.indent{padding-left:16px;}
</style></head><body>
<h1>$title</h1>
<p class="meta">$metaLine</p>
<p class="sub">計画数合計：<strong>${formatInt(totalPlanQty)}</strong>　／　出力：$now　／　${sorted.size} 件</p>
${body.ifBlank { "<p class=\"sub\">印刷対象データがありません。</p>" }}
</body></html>"""
    }

    fun clampBomNum(n: Int): Int = n.coerceIn(0, 9999)
}
