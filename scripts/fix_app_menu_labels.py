# -*- coding: utf-8 -*-
"""Restore Japanese labels in AppMenuConfig.kt from Web menuConfig.ts."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MENU_TS = ROOT.parent / "Smart-EMAPs" / "frontend" / "src" / "router" / "menuConfig.ts"
APP_MENU = ROOT / "app" / "src" / "main" / "java" / "com" / "example" / "smart_emap" / "ui" / "shell" / "AppMenuConfig.kt"

# Static labels not in menuConfig or composed in Kotlin
EXTRA_LABELS: dict[str, str] = {
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_CUTTING": "切断生産性",
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_CHAMFERING": "面取生産性",
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_FORMING": "成型生産性",
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_PLATING": "メッキ生産性",
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_WELDING": "溶接生産性",
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY_INSPECTION": "検査生産性",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_CUTTING": "切断稼働率",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_CHAMFERING": "面取稼働率",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_FORMING": "成型稼働率",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_PLATING": "メッキ稼働率",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_WELDING": "溶接稼働率",
    "MES_ACTUAL_ANALYSIS_UTILIZATION_INSPECTION": "検査稼働率",
    "MES_ACTUAL_ANALYSIS_PROGRESS_CUTTING": "切断進捗",
    "MES_ACTUAL_ANALYSIS_PROGRESS_CHAMFERING": "面取進捗",
    "MES_ACTUAL_ANALYSIS_PROGRESS_FORMING": "成型進捗",
    "MES_ACTUAL_ANALYSIS_PROGRESS_PLATING": "メッキ進捗",
    "MES_ACTUAL_ANALYSIS_PROGRESS_WELDING": "溶接進捗",
    "MES_ACTUAL_ANALYSIS_PROGRESS_INSPECTION": "検査進捗",
    "MES_ACTUAL_ANALYSIS_QUALITY_CUTTING": "切断品質",
    "MES_ACTUAL_ANALYSIS_QUALITY_CHAMFERING": "面取品質",
    "MES_ACTUAL_ANALYSIS_QUALITY_FORMING": "成型品質",
    "MES_ACTUAL_ANALYSIS_QUALITY_PLATING": "メッキ品質",
    "MES_ACTUAL_ANALYSIS_QUALITY_WELDING": "溶接品質",
    "MES_ACTUAL_ANALYSIS_QUALITY_INSPECTION": "検査品質",
    "MES_ACTUAL_ANALYSIS_COST_CUTTING": "切断コスト",
    "MES_ACTUAL_ANALYSIS_COST_CHAMFERING": "面取コスト",
    "MES_ACTUAL_ANALYSIS_COST_FORMING": "成型コスト",
    "MES_ACTUAL_ANALYSIS_COST_PLATING": "メッキコスト",
    "MES_ACTUAL_ANALYSIS_COST_WELDING": "溶接コスト",
    "MES_ACTUAL_ANALYSIS_COST_INSPECTION": "検査コスト",
}

PROCESS_LABELS = {
    "CUTTING": "切断",
    "CHAMFERING": "面取",
    "FORMING": "成型",
    "PLATING": "メッキ",
    "WELDING": "溶接",
    "INSPECTION": "検査",
}

CATEGORY_LABELS = {
    "productivity": "生産性",
    "utilization": "稼働率",
    "progress": "進捗",
    "quality": "品質",
    "cost": "コスト",
}

GROUP_CATEGORY_LABELS = {
    "MES_ACTUAL_ANALYSIS_PRODUCTIVITY": "生産性分析",
    "MES_ACTUAL_ANALYSIS_UTILIZATION": "稼働率分析",
    "MES_ACTUAL_ANALYSIS_PROGRESS": "進捗分析",
    "MES_ACTUAL_ANALYSIS_QUALITY": "品質分析",
    "MES_ACTUAL_ANALYSIS_COST": "コスト分析",
}


def parse_menu_ts(text: str) -> dict[str, str]:
    labels: dict[str, str] = {}
    pattern = re.compile(
        r"\{\s*code:\s*'([^']+)',\s*name:\s*'((?:\\'|[^'])*)'",
        re.MULTILINE,
    )
    for code, name in pattern.findall(text):
        labels[code] = name.replace("\\'", "'")
    return labels


def kotlin_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def fix_file(content: str, labels: dict[str, str]) -> str:
    all_labels = {**labels, **EXTRA_LABELS}

    def leaf_repl(m: re.Match[str]) -> str:
        code, name = m.group(1), all_labels.get(m.group(1))
        if not name:
            return m.group(0)
        return f'AppMenuNode.Leaf("{code}", "{kotlin_escape(name)}",{m.group(2)}'

    content = re.sub(
        r'AppMenuNode\.Leaf\("([^"]+)",\s*"[^"]*",(\s*)',
        leaf_repl,
        content,
    )

    def group_repl(m: re.Match[str]) -> str:
        indent, code = m.group(1), m.group(2)
        name = all_labels.get(code)
        if not name:
            return m.group(0)
        return f'{indent}code = "{code}",\n{indent}label = "{kotlin_escape(name)}",'

    content = re.sub(
        r'(\s*)code = "([^"]+)",\n\1label = "[^"]*",',
        group_repl,
        content,
    )

    # mesActualAnalysis categoryLabel in function calls
    for cat, jp in CATEGORY_LABELS.items():
        content = content.replace(
            f'analysisCategory = "{cat}",\n                            categoryLabel = "',
            f'analysisCategory = "{cat}",\n                            categoryLabel = "PLACEHOLDER_{cat}_',
            1,
        )
    for cat, jp in CATEGORY_LABELS.items():
        content = re.sub(
            rf'categoryLabel = "PLACEHOLDER_{cat}_[^"]*"',
            f'categoryLabel = "{jp}"',
            content,
        )

    # mesActualAnalysisProcesses process labels
    for suffix, jp in PROCESS_LABELS.items():
        content = re.sub(
            rf'MesActualAnalysisProcess\("{suffix}",\s*"[^"]*"',
            f'MesActualAnalysisProcess("{suffix}", "{jp}"',
            content,
        )

    # KDoc
    content = content.replace(
        " * ? Smart-EMAPs frontend `src/router/menuConfig.ts` ??????\n *\n * ???\n * - Android ???????? path + ????????????? Tab?\n * - ???????????????????\n",
        " * 与 Smart-EMAPs frontend `src/router/menuConfig.ts` 层级对齐。\n *\n * 说明：\n * - Android 侧暂时只用「路由 path + 显示名称」来驱动左侧菜单与 Tab。\n * - 图标映射为简化映射，不影响路由一致性。\n",
    )
    content = content.replace(
        "    /** ???????????? code?????????? */",
        "    /** 指定 path に紐づく menu code（複数 leaf が同一 path の場合あり） */",
    )
    content = content.replace(
        "    /** ?????? menu_codes???????????? */",
        "    /** ユーザーの menu_codes に基づき表示可能なメニューを返す */",
    )
    content = content.replace(
        "/** Web `menuConfig.ts` ???????: ????? / ??????? leaf ????? */",
        "/** Web `menuConfig.ts` と同様に、工程名 + 分析種別で leaf ラベルを生成 */",
    )

    return content


def main() -> None:
    ts_text = MENU_TS.read_text(encoding="utf-8")
    labels = parse_menu_ts(ts_text)
    kt_text = APP_MENU.read_text(encoding="utf-8")
    fixed = fix_file(kt_text, labels)
    APP_MENU.write_text(fixed, encoding="utf-8", newline="\n")
    print(f"Parsed {len(labels)} labels from menuConfig.ts")
    print(f"Wrote {APP_MENU}")


if __name__ == "__main__":
    main()
