#!/usr/bin/env python3
"""Generate Cutting productivity Kotlin files from Welding templates."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PRODUCTIVITY = ROOT / "app/src/main/java/com/example/smart_emap/ui/mes/productivity"

REPLACEMENTS = [
    ("WeldingProductivity", "CuttingProductivity"),
    ("WeldingOperatorProduct", "CuttingOperatorProduct"),
    ("WeldingDailyBatch", "CuttingDailyBatch"),
    ("welding_productivity_print", "cutting_productivity_print"),
    ("weldingRepository", "cuttingRepository"),
    ("WeldingRepository", "CuttingRepository"),
    ("wpa_daily", "cpa_daily"),
    ("Wpa", "Cpa"),
    ("溶接工程 — 生産性分析", "切断工程 — 生産性分析"),
    ("溶接生産性分析", "切断生産性分析"),
    ("実績 · 能率 · 不良率 · 稼働", "実績 · 能率 · 差異率 · 稼働"),
    ("溶接作業者", "ライン"),
    ("TOP作業者", "TOPライン"),
    ("作業者データ", "ラインデータ"),
    ("不良内訳（KT07）", "差異内訳"),
    ("不良数", "差異数"),
    ("不良率", "差異率"),
    ('label = "不良"', 'label = "差異"'),
    ('"不良"', '"差異"'),
    ("filterOperatorId", "filterLineName"),
    ("setFilterOperatorId", "setFilterLineName"),
    ("operatorUserId", "productionLine"),
    ("operatorOptions", "lineOptions"),
    ("loadOperators", "loadLines"),
    ("loadDefectLabels", "loadVarianceLabels"),
    ("loadWeldingSectionOperators", "loadProductivityLines"),
    ("isWeldingSectionOperatorUser", "isCuttingLineOption"),
    ("operatorLabel", "lineLabel"),
    ("operatorLabel =", "lineLabel ="),
    ("operatorLabel:", "lineLabel:"),
    ("operatorLabel)", "lineLabel)"),
    ("operatorLabel,", "lineLabel,"),
    ("operatorLabel ", "lineLabel "),
    ("operatorLabel\n", "lineLabel\n"),
    ("operatorLabel}", "lineLabel}"),
    ("operatorLabel`", "lineLabel`"),
    ("operatorLabel'", "lineLabel'"),
    ("operatorLabel\"", "lineLabel\""),
    ("operatorLabel?", "lineLabel?"),
    ("operatorLabel.", "lineLabel."),
    ("operatorLabel:", "lineLabel:"),
    ("operatorLabel =", "lineLabel ="),
    ("operatorLabel)", "lineLabel)"),
    ("operatorLabel,", "lineLabel,"),
    ("operatorLabel ", "lineLabel "),
    ("operatorLabel\n", "lineLabel\n"),
    ("operatorLabel}", "lineLabel}"),
    ("operatorLabel`", "lineLabel`"),
    ("operatorLabel'", "lineLabel'"),
    ("operatorLabel\"", "lineLabel\""),
    ("operatorLabel?", "lineLabel?"),
    ("operatorLabel.", "lineLabel."),
    ("operatorLabel:", "lineLabel:"),
    ("operatorLabel =", "lineLabel ="),
    ("operatorLabel)", "lineLabel)"),
    ("operatorLabel,", "lineLabel,"),
    ("operatorLabel ", "lineLabel "),
    ("operatorLabel\n", "lineLabel\n"),
    ("operatorLabel}", "lineLabel}"),
    ("operatorLabel`", "lineLabel`"),
    ("operatorLabel'", "lineLabel'"),
    ("operatorLabel\"", "lineLabel\""),
    ("operatorLabel?", "lineLabel?"),
    ("operatorLabel.", "lineLabel."),
    ("operatorLabel:", "lineLabel:"),
    ("operatorLabel =", "lineLabel ="),
    ("operatorLabel)", "lineLabel)"),
    ("operatorLabel,", "lineLabel,"),
    ("operatorLabel ", "lineLabel "),
    ("operatorLabel\n", "lineLabel\n"),
    ("operatorLabel}", "lineLabel}"),
    ("operatorLabel`", "lineLabel`"),
    ("operatorLabel'", "lineLabel'"),
    ("operatorLabel\"", "lineLabel\""),
    ("operatorLabel?", "lineLabel?"),
    ("operatorLabel.", "lineLabel."),
    ("WELDING_DEPARTMENT_NAME", "CUTTING_SECTION_PLACEHOLDER"),
    ("WELDING_SECTION_NAME", "CUTTING_SECTION_PLACEHOLDER2"),
    ("UserListItemDto", "String"),
    ("SystemUserRepository", "Unit"),
    ("userRepository", "_unusedUserRepository"),
    ("defectLabelMap", "varianceLabelMap"),
    ("defectLabel", "varianceLabel"),
    ("IpaWeldingDefectSection", "CpaVarianceSection"),
    ("IpaWeldingOperatorProductSplit", "CpaLineProductSplit"),
    ("IpaToolbarCard", "CpaLineToolbarCard"),
    ("filterInspectorId", "filterLineName"),
    ("onInspectorChange", "onLineChange"),
    ("inspectorOptions", "lineOptions"),
    ("personPillLabel = \"溶接作業者\"", 'personPillLabel = "ライン"'),
    ("personPillLabel = \"ライン\"", 'personPillLabel = "ライン"'),
    ("WpaProductRankSection", "CpaProductRankSection"),
    ("WpaSessionDetailSection", "CpaSessionDetailSection"),
    ("WeldingProductivityLogic", "CuttingProductivityLogic"),
    ("WeldingProductivityReportLogic", "CuttingProductivityReportLogic"),
    ("WeldingProductivityReportCommand", "CuttingProductivityReportCommand"),
    ("WeldingProductivityReportFilters", "CuttingProductivityReportFilters"),
    ("WeldingProductivityPrintContext", "CuttingProductivityPrintContext"),
    ("WeldingProductivityUiState", "CuttingProductivityUiState"),
    ("WeldingProductivityViewModel", "CuttingProductivityViewModel"),
    ("WeldingProductivityScreen", "CuttingProductivityScreen"),
]

# dedupe replacements - keep order, last wins for duplicates - actually use unique
seen = set()
unique_replacements = []
for a, b in REPLACEMENTS:
    if a not in seen:
        unique_replacements.append((a, b))
        seen.add(a)


def transform(content: str, extra: list[tuple[str, str]] | None = None) -> str:
    reps = unique_replacements + (extra or [])
    for old, new in reps:
        content = content.replace(old, new)
    return content


def write_from_welding(welding_name: str, cutting_name: str, extra: list[tuple[str, str]] | None = None):
    src = PRODUCTIVITY / welding_name
    dst = PRODUCTIVITY / cutting_name
    text = src.read_text(encoding="utf-8")
    text = transform(text, extra)
    dst.write_text(text, encoding="utf-8")
    print(f"Wrote {dst.name}")


def patch_logic():
    path = PRODUCTIVITY / "CuttingProductivityLogic.kt"
    text = path.read_text(encoding="utf-8")
    # Remove welding department constants usage
    text = re.sub(
        r"const val CUTTING_SECTION_PLACEHOLDER = .*?\nconst val CUTTING_SECTION_PLACEHOLDER2 = .*?\n\n",
        "",
        text,
        flags=re.DOTALL,
    )
    text = text.replace(
        """    fun isCuttingLineOption(user: String): Boolean =
        user.department?.trim() == CUTTING_SECTION_PLACEHOLDER &&
            user.section?.trim() == CUTTING_SECTION_PLACEHOLDER2

""",
        "",
    )
    # Fix KPI defect icon -> still use Defect icon for variance
    text = text.replace('label = "差異"', 'label = "差異数"')
    # Fix buildOperatorProductRows to use line name key
    old_build = """    fun buildOperatorProductRows(
        sessions: List<WeldingProductivitySessionRowDto>,
        operatorKey: String,
    ): List<CuttingOperatorProductDisplayRow> {
        val map = linkedMapOf<String, ProductRowAgg>()
        for (s in sessions) {
            val opId = s.mesOperatorUserId
            val key = opId?.toString() ?: "none"
            if (key != operatorKey) continue"""
    new_build = """    fun sessionLineKey(session: WeldingProductivitySessionRowDto): String =
        session.operatorDisplayName?.trim()?.ifBlank { null }
            ?: session.mesOperatorName?.trim()?.ifBlank { null }
            ?: "—"

    fun buildOperatorProductRows(
        sessions: List<WeldingProductivitySessionRowDto>,
        lineKey: String,
    ): List<CuttingOperatorProductDisplayRow> {
        val map = linkedMapOf<String, ProductRowAgg>()
        for (s in sessions) {
            val key = sessionLineKey(s)
            if (key != lineKey) continue"""
    text = text.replace(old_build, new_build)
    # Fix ranking from sessions
    old_rank = """            val opId = s.mesOperatorUserId
            val opKey = opId?.toString() ?: "none"
            val opName = s.operatorDisplayName?.trim()
                ?: s.mesOperatorName?.trim()
                ?: "—"
            val inv = prod.operators.getOrPut(opKey) {
                OperatorAgg(opId, opName.ifBlank { "—" })
            }"""
    new_rank = """            val opKey = sessionLineKey(s)
            val opName = opKey
            val inv = prod.operators.getOrPut(opKey) {
                OperatorAgg(null, opName.ifBlank { "—" })
            }"""
    text = text.replace(old_rank, new_rank)
    path.write_text(text, encoding="utf-8")
    print("Patched CuttingProductivityLogic.kt")


def patch_viewmodel():
    path = PRODUCTIVITY / "CuttingProductivityViewModel.kt"
    text = path.read_text(encoding="utf-8")
    # Fix UiState filter type
    text = text.replace("val filterLineName: Int? = null", "val filterLineName: String = \"\"")
    text = text.replace(
        "val lineOptions: List<String> = emptyList()",
        "val lineOptions: List<String> = emptyList()",
    )
    # Fix setFilterLineName
    text = text.replace(
        "fun setFilterLineName(id: Int?) {\n        _uiState.update { it.copy(filterLineName = id) }",
        'fun setFilterLineName(line: String) {\n        _uiState.update { it.copy(filterLineName = line) }',
    )
    # Fix loadLines
    old_load = """    private fun loadLines() {
        viewModelScope.launch {
            runCatching { cuttingRepository.loadProductivityLines() }
                .onSuccess { list ->
                    _uiState.update { it.copy(lineOptions = list) }
                }
                .onFailure {
                    _unusedUserRepository.getUsers(status = "active", page = 1, pageSize = 500)
                        .onSuccess { res ->
                            val filtered = res.items.orEmpty().filter { user ->
                                user.id != null && CuttingProductivityLogic.isCuttingLineOption(user)
                            }
                            _uiState.update { it.copy(lineOptions = filtered) }
                        }
                }
        }
    }"""
    new_load = """    private fun loadLines() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) return
        viewModelScope.launch {
            runCatching {
                cuttingRepository.loadProductivityLines(state.startDate, state.endDate)
            }.onSuccess { list ->
                val current = _uiState.value.filterLineName
                val next = if (current.isNotBlank() && current !in list) "" else current
                _uiState.update { it.copy(lineOptions = list, filterLineName = next) }
            }.onFailure {
                _uiState.update { it.copy(lineOptions = emptyList()) }
            }
        }
    }"""
    if old_load in text:
        text = text.replace(old_load, new_load)
    else:
        # try transformed version
        text = re.sub(
            r"private fun loadLines\(\) \{.*?\n    \}",
            new_load,
            text,
            count=1,
            flags=re.DOTALL,
        )
    # loadLines on date change
    text = text.replace(
        "fun setDateRange(start: String, end: String) {\n        _uiState.update { it.copy(startDate = start, endDate = end) }\n        scheduleLoadAnalysis()",
        "fun setDateRange(start: String, end: String) {\n        _uiState.update { it.copy(startDate = start, endDate = end) }\n        loadLines()\n        scheduleLoadAnalysis()",
    )
    # init loadVarianceLabels noop
    text = text.replace(
        """    private fun loadVarianceLabels() {
        viewModelScope.launch {
            runCatching { cuttingRepository.loadDefectItems() }
                .onSuccess { items ->
                    val map = items.associate { item ->
                        val key = item.defectCd?.trim()?.takeIf { it.isNotBlank() } ?: item.id?.toString().orEmpty()
                        key to (item.defectName ?: "")
                    }
                    _uiState.update { it.copy(varianceLabelMap = map) }
                }
        }
    }""",
        "    private fun loadVarianceLabels() { /* 切断は差異ラベル固定 */ }",
    )
    # load products
    text = text.replace(
        "runCatching { cuttingRepository.loadProducts() }",
        "runCatching { cuttingRepository.loadProductivityProducts() }",
    )
    # API call productionLine
    text = text.replace(
        "productionLine = state.filterLineName,",
        "productionLine = state.filterLineName.ifBlank { null },",
    )
    # buildReportFilters line label
    text = text.replace(
        """        val lineLabel = state.filterLineName?.let { id ->
            state.lineOptions.find { it.id == id }?.displayLabel()?.ifBlank { null }
                ?: "#$id"
        } ?: "（すべて）"""",
        '        val lineLabel = state.filterLineName.ifBlank { "（すべて）" }',
    )
    # batch daily by lines
    old_batch = """        for (op in state.lineOptions) {
            val id = op.id ?: continue
            val data = cuttingRepository.loadProductivityAnalysis(
                startDate = filters.startDate,
                endDate = filters.endDate,
                productionLine = id,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = filters.includeIncomplete,
            ).getOrNull() ?: continue
            val daily = data.daily.orEmpty()
            if (daily.isEmpty()) continue
            val chartFileName = renderDailyChartFile(printCacheDir, daily, "cpa_daily_batch_${chartIndex++}.png")
                ?: continue
            val label = op.displayLabel().ifBlank { op.username.orEmpty() }
            items.add(CuttingDailyBatchPrintItem(lineLabel = label, daily = daily, chartFileName = chartFileName))
        }"""
    new_batch = """        for (line in state.lineOptions) {
            if (line.isBlank()) continue
            val data = cuttingRepository.loadProductivityAnalysis(
                startDate = filters.startDate,
                endDate = filters.endDate,
                productionLine = line,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = filters.includeIncomplete,
            ).getOrNull() ?: continue
            val daily = data.daily.orEmpty()
            if (daily.isEmpty()) continue
            val chartFileName = renderDailyChartFile(printCacheDir, daily, "cpa_daily_batch_${chartIndex++}.png")
                ?: continue
            items.add(CuttingDailyBatchPrintItem(lineLabel = line, daily = daily, chartFileName = chartFileName))
        }"""
    text = text.replace(old_batch, new_batch)
    # operator product batch
    old_op_batch = """        return state.lineOptions.mapNotNull { op ->
            val id = op.id ?: return@mapNotNull null
            val rows = CuttingProductivityLogic.buildOperatorProductRows(sessions, id.toString())
            if (rows.isEmpty()) return@mapNotNull null
            val label = op.displayLabel().ifBlank { op.username.orEmpty() }
            label to rows
        }"""
    new_op_batch = """        return state.lineOptions.mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val rows = CuttingProductivityLogic.buildOperatorProductRows(sessions, line)
            if (rows.isEmpty()) return@mapNotNull null
            line to rows
        }"""
    text = text.replace(old_op_batch, new_op_batch)
    # varianceLabel function
    text = text.replace(
        "fun varianceLabel(defectCd: String): String {\n        val cd = defectCd.trim()\n        return _uiState.value.varianceLabelMap[cd] ?: cd\n    }",
        'fun varianceLabel(defectCd: String): String = defectCd.trim().ifBlank { "—" }',
    )
    # Factory - remove user repo
    text = text.replace(
        """class Factory(
        private val cuttingRepository: CuttingRepository,
        private val _unusedUserRepository: Unit,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CuttingProductivityViewModel(cuttingRepository, _unusedUserRepository) as T
    }""",
        """class Factory(
        private val cuttingRepository: CuttingRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CuttingProductivityViewModel(cuttingRepository) as T
    }""",
    )
    text = text.replace(
        "class CuttingProductivityViewModel(\n    private val cuttingRepository: CuttingRepository,\n    private val _unusedUserRepository: Unit,\n) : ViewModel() {",
        "class CuttingProductivityViewModel(\n    private val cuttingRepository: CuttingRepository,\n) : ViewModel() {",
    )
    text = text.replace("loadVarianceLabels()\n        }", "}")
    text = text.replace(
        "            loadLines()\n            loadProductOptions()\n            loadVarianceLabels()",
        "            loadLines()\n            loadProductOptions()",
    )
    path.write_text(text, encoding="utf-8")
    print("Patched CuttingProductivityViewModel.kt")


def patch_screen():
    path = PRODUCTIVITY / "CuttingProductivityScreen.kt"
    text = path.read_text(encoding="utf-8")
    text = text.replace("viewModel::varianceLabel", "viewModel::varianceLabel")
    text = text.replace("CpaVarianceSection(", "CpaVarianceSection(")
    path.write_text(text, encoding="utf-8")


def patch_components():
    path = PRODUCTIVITY / "CuttingProductivityComponents.kt"
    if not path.exists():
        return
    text = path.read_text(encoding="utf-8")
    # Session table without machine column labels
    text = text.replace(
        'val headers = listOf("生産日", "ライン", "設備", "CD", "製品名", "生産", "差異", "差異率", "能率", "稼働", "停止", "状態")',
        'val headers = listOf("生産日", "ライン", "CD", "製品名", "生産", "差異", "差異率", "能率", "稼働", "停止", "状態")',
    )
    path.write_text(text, encoding="utf-8")
    print("Patched CuttingProductivityComponents.kt (if exists)")


def main():
    write_from_welding("WeldingProductivityLogic.kt", "CuttingProductivityLogic.kt")
    write_from_welding("WeldingProductivityViewModel.kt", "CuttingProductivityViewModel.kt")
    write_from_welding("WeldingProductivityScreen.kt", "CuttingProductivityScreen.kt")
    write_from_welding("WeldingProductivityReportLogic.kt", "CuttingProductivityReportLogic.kt")
    patch_logic()
    patch_viewmodel()
    patch_screen()


if __name__ == "__main__":
    main()
