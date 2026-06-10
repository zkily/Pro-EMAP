package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ProductionDataTab(val key: String, val label: String) {
    Order("custom", "受注"),
    Actual("actual", "実績"),
    Inventory("inventory", "在庫"),
    Trend("trend", "推移"),
    ActualPlanTrend("actual_plan_trend", "実計推移"),
    Defect("defect", "不良"),
    Scrap("scrap", "廃棄"),
    OnHold("on_hold", "保留"),
    Plan("plan", "計画"),
    CarryOver("carry_over", "繰越"),
    ColumnCustomize("column_custom", "カスタム"),
}

data class ProductionColumnDef(
    val key: String,
    val label: String,
    val group: String,
    val width: Int = 80,
    val isText: Boolean = false,
)

object ProductionDataManagementLogic {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    /** Web processPrefixes と同順 */
    private val processPrefixGroups = listOf(
        "cutting" to "切断",
        "chamfering" to "面取",
        "molding" to "成型",
        "plating" to "メッキ",
        "welding" to "溶接",
        "inspection" to "検査",
        "warehouse" to "倉庫",
        "outsourced_warehouse" to "外注倉庫",
        "outsourced_plating" to "外注メッキ",
        "outsourced_welding" to "外注溶接",
        "pre_welding_inspection" to "溶接前検査",
        "pre_inspection" to "外注支給前",
        "pre_outsourcing" to "外注検査前",
    )

    private val tabBaseKeys = listOf("date", "product_cd", "product_name")

    private val suffixLabels = mapOf(
        "_carry_over" to "繰越",
        "_actual" to "実績",
        "_defect" to "不良",
        "_scrap" to "廃棄",
        "_on_hold" to "保留品",
        "_inventory" to "在庫",
        "_trend" to "推移",
        "_plan" to "計画",
        "_actual_plan_trend" to "実計推移",
    )

    private val allTabSuffixes = suffixLabels.keys.toList()

    private val prefixTabSuffixes: Map<String, List<String>> = buildMap {
        processPrefixGroups.forEach { (prefix, _) ->
            put(
                prefix,
                when (prefix) {
                    "warehouse" -> listOf(
                        "_carry_over", "_actual", "_defect", "_scrap", "_on_hold", "_inventory", "_trend",
                    )
                    "pre_welding_inspection" -> listOf(
                        "_carry_over", "_actual", "_defect", "_scrap", "_on_hold", "_inventory", "_trend",
                    )
                    "pre_inspection", "pre_outsourcing" -> listOf(
                        "_carry_over", "_actual", "_scrap", "_inventory", "_trend",
                    )
                    else -> allTabSuffixes
                },
            )
        }
    }

    /** 受注タブ固定列（表示順・列設定不可） */
    private val orderTabKeys = listOf(
        "date",
        "product_cd",
        "product_name",
        "order_quantity",
        "forecast_quantity",
        "safety_stock",
        "warehouse_inventory",
    )

    /** カスタムタブで先頭に並べる基本列 */
    private val customizeLeadingKeys = listOf(
        "date",
        "day_of_week",
        "route_cd",
        "product_cd",
        "product_name",
        "order_quantity",
        "forecast_quantity",
        "safety_stock",
    )

    val columnDefinitions: Map<String, ProductionColumnDef> = buildMap {
        put("date", ProductionColumnDef("date", "日付", "基本情報", ProductionDataManagementTableLayout.DATE_WIDTH))
        put("day_of_week", ProductionColumnDef("day_of_week", "曜日", "基本情報", 60))
        put("route_cd", ProductionColumnDef("route_cd", "工程グループ", "基本情報", 120))
        put("product_cd", ProductionColumnDef("product_cd", "製品CD", "基本情報", ProductionDataManagementTableLayout.PRODUCT_CD_WIDTH))
        put("product_name", ProductionColumnDef("product_name", "製品名", "基本情報", ProductionDataManagementTableLayout.PRODUCT_NAME_WIDTH))
        put("order_quantity", ProductionColumnDef("order_quantity", "受注数", "受注・内示", 70))
        put("forecast_quantity", ProductionColumnDef("forecast_quantity", "内示数", "受注・内示", 70))
        put("safety_stock", ProductionColumnDef("safety_stock", "安全在庫", "受注・内示", 90))
        put("warehouse_inventory", ProductionColumnDef("warehouse_inventory", "倉庫在庫", "倉庫", 90))
        processPrefixGroups.forEach { (prefix, groupLabel) ->
            prefixTabSuffixes[prefix].orEmpty().forEach { suffix ->
                val key = "${prefix}${suffix}"
                val suffixLabel = suffixLabels[suffix].orEmpty()
                val width = when {
                    prefix.startsWith("outsourced_") || prefix.startsWith("pre_") -> 100
                    prefix == "chamfering" -> 85
                    suffix == "_on_hold" -> 80
                    suffix == "_actual_plan_trend" -> 100
                    else -> 70
                }
                put(key, ProductionColumnDef(key, "$groupLabel$suffixLabel", groupLabel, width))
            }
        }
        put("chamfering_machine", ProductionColumnDef("chamfering_machine", "面取機", "面取", 80, isText = true))
        put("sw_machine", ProductionColumnDef("sw_machine", "sw機", "面取", 80, isText = true))
        put("sw_plan", ProductionColumnDef("sw_plan", "sw計画", "面取", 80))
    }

    /** カスタムタブ専用の列表示設定（他タブには影響しない） */
    val defaultCustomizeVisibleColumns: Map<String, Boolean> = buildMap {
        columnDefinitions.keys.forEach { put(it, false) }
        put("date", true)
        put("product_cd", true)
        put("product_name", true)
        put("order_quantity", true)
        put("forecast_quantity", true)
        put("safety_stock", true)
    }

    val columnGroups: Map<String, List<String>> =
        columnDefinitions.entries
            .groupBy({ it.value.group }, { it.key })
            .mapValues { (_, keys) -> keys.sorted() }

    private val tabProcessSuffix = mapOf(
        ProductionDataTab.Actual to "_actual",
        ProductionDataTab.Inventory to "_inventory",
        ProductionDataTab.Trend to "_trend",
        ProductionDataTab.ActualPlanTrend to "_actual_plan_trend",
        ProductionDataTab.Defect to "_defect",
        ProductionDataTab.Scrap to "_scrap",
        ProductionDataTab.OnHold to "_on_hold",
        ProductionDataTab.Plan to "_plan",
        ProductionDataTab.CarryOver to "_carry_over",
    )

    private val stripKeywords = mapOf(
        ProductionDataTab.CarryOver to listOf("繰越"),
        ProductionDataTab.Actual to listOf("実績"),
        ProductionDataTab.Defect to listOf("不良"),
        ProductionDataTab.Scrap to listOf("廃棄"),
        ProductionDataTab.OnHold to listOf("保留品", "保留"),
        ProductionDataTab.Inventory to listOf("在庫"),
        ProductionDataTab.Trend to listOf("推移"),
        ProductionDataTab.Plan to listOf("計画"),
        ProductionDataTab.ActualPlanTrend to listOf("実計推移"),
    )

    /** Web 筛选默认：今天～今天 */
    fun defaultDateRange(): Pair<String, String> = todayRange()

    fun todayRange(): Pair<String, String> {
        val today = LocalDate.now().format(iso)
        return today to today
    }

    fun firstDayOfCurrentMonth(): String =
        LocalDate.now().withDayOfMonth(1).format(iso)

    fun shiftDateRange(start: String, end: String, dayOffset: Int): Pair<String, String> {
        val startDate = LocalDate.parse(start.take(10), iso).plusDays(dayOffset.toLong())
        val endDate = LocalDate.parse(end.take(10), iso).plusDays(dayOffset.toLong())
        return startDate.format(iso) to endDate.format(iso)
    }

    /** Web: 当月1日 ～ 当月起算4ヶ月後の月末 */
    fun generateDateRange(): Pair<String, String> {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        val endAnchor = today.plusMonths(4)
        val end = endAnchor.withDayOfMonth(endAnchor.lengthOfMonth())
        return start.format(iso) to end.format(iso)
    }

    /** 製品マスタ / 設備フィールド更新默认：今天 ～ 4个月后 */
    fun updatePeriodDefaultRange(): Pair<String, String> {
        val today = LocalDate.now()
        val end = today.plusMonths(4)
        return today.format(iso) to end.format(iso)
    }

    fun monthRange(monthOffset: Int): Pair<String, String> {
        val anchor = LocalDate.now().plusMonths(monthOffset.toLong())
        val start = anchor.withDayOfMonth(1)
        val end = start.plusMonths(1).minusDays(1)
        return start.format(iso) to end.format(iso)
    }

    fun columnsForTab(
        tab: ProductionDataTab,
        customizeVisibleColumns: Map<String, Boolean>,
    ): List<ProductionColumnDef> {
        if (tab == ProductionDataTab.Order) {
            return orderTabKeys.mapNotNull { columnDefinitions[it] }
        }
        if (tab == ProductionDataTab.ColumnCustomize) {
            return columnsForCustomizeTab(customizeVisibleColumns)
        }
        val suffix = tabProcessSuffix[tab] ?: return emptyList()
        val keywords = stripKeywords[tab].orEmpty()
        val baseCols = tabBaseKeys.mapNotNull { columnDefinitions[it] }
        val processCols = processPrefixGroups.mapNotNull { (prefix, _) ->
            val key = "${prefix}$suffix"
            val def = columnDefinitions[key] ?: return@mapNotNull null
            var label = def.label
            keywords.sortedByDescending { it.length }.forEach { kw ->
                label = label.replace(kw, "")
            }
            def.copy(label = label.trim().ifEmpty { def.label })
        }
        return baseCols + processCols
    }

    private fun columnsForCustomizeTab(visibleColumns: Map<String, Boolean>): List<ProductionColumnDef> {
        val keys = linkedSetOf<String>()
        customizeLeadingKeys.forEach { key ->
            if (visibleColumns[key] == true) keys.add(key)
        }
        columnGroups.forEach { (_, groupKeys) ->
            groupKeys.forEach { key ->
                if (key !in customizeLeadingKeys && visibleColumns[key] == true) {
                    keys.add(key)
                }
            }
        }
        return keys.mapNotNull { columnDefinitions[it] }
    }

    fun cellValue(row: ProductionSummaryFullRowDto, key: String): String = when (key) {
        "date" -> row.date.orEmpty()
        "day_of_week" -> row.dayOfWeek.orEmpty().ifBlank { "—" }
        "route_cd" -> row.routeCd.orEmpty().ifBlank { "—" }
        "product_cd" -> row.productCd.orEmpty()
        "product_name" -> row.productName.orEmpty()
        "chamfering_machine" -> row.chamferingMachine.orEmpty().ifBlank { "—" }
        "sw_machine" -> row.swMachine.orEmpty().ifBlank { "—" }
        else -> formatProductionNumber(productionSummaryIntField(row, key))
    }

    fun buildTable(
        rows: List<ProductionSummaryFullRowDto>,
        tab: ProductionDataTab,
        customizeVisibleColumns: Map<String, Boolean>,
    ): ProductionTableData {
        val cols = columnsForTab(tab, customizeVisibleColumns)
        val headers = cols.map { it.label }
        val keys = cols.map { it.key }
        val widths = cols.map { it.width }
        val body = rows.map { row -> cols.map { cellValue(row, it.key) } }
        val summary = buildSummaryRow(rows, keys)
        return ProductionTableData(headers, body, widths, keys, summary)
    }

    /** Web getSummaries と同様：数値列を合計、安全在庫は製品ごと最新日のみ */
    fun buildSummaryRow(
        rows: List<ProductionSummaryFullRowDto>,
        keys: List<String>,
    ): List<String> = keys.mapIndexed { index, key ->
        when {
            index == 0 -> "合計"
            !isProductionSummaryNumericField(key) -> ""
            key == "safety_stock" -> formatProductionNumber(sumSafetyStockByLatestDatePerProduct(rows))
            else -> {
                val sum = rows.sumOf { productionSummaryIntField(it, key)?.toLong() ?: 0L }
                formatProductionNumber(sum)
            }
        }
    }

    private fun sumSafetyStockByLatestDatePerProduct(rows: List<ProductionSummaryFullRowDto>): Int {
        val byProduct = linkedMapOf<String, Pair<String, Int>>()
        rows.forEach { row ->
            val productCd = row.productCd?.trim().orEmpty()
            if (productCd.isEmpty()) return@forEach
            val date = row.date?.take(10).orEmpty()
            val value = row.safetyStock ?: 0
            val current = byProduct[productCd]
            if (current == null || date > current.first) {
                byProduct[productCd] = date to value
            }
        }
        return byProduct.values.sumOf { it.second }
    }
}

data class ProductionTableData(
    val headers: List<String>,
    val rows: List<List<String>>,
    val widths: List<Int>,
    val keys: List<String>,
    val summaryRow: List<String> = emptyList(),
)

object ProductionDataManagementTableLayout {
    /** 日付 yyyy-MM-dd 等 11 桁表示 */
    const val DATE_WIDTH = 88
    const val PRODUCT_CD_WIDTH = 56
    const val PRODUCT_NAME_WIDTH = 100
    val FIXED_COLUMN_KEYS = listOf("date", "product_cd", "product_name")

    fun fixedColumnCount(keys: List<String>): Int {
        var count = 0
        FIXED_COLUMN_KEYS.forEachIndexed { index, key ->
            if (keys.getOrNull(index) == key) count++ else return@forEachIndexed
        }
        return count
    }
}
