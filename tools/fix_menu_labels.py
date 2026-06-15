#!/usr/bin/env python3
import re
from pathlib import Path

TS = Path(r"C:\Users\arai-235\Desktop\Smart-EMAPs\frontend\src\router\menuConfig.ts")
KT = Path(r"C:\Users\arai-235\Desktop\SmartEMAP\app\src\main\java\com\example\smart_emap\ui\shell\AppMenuConfig.kt")

names = {}
for m in re.finditer(r"\{ code: '([^']+)', name: '([^']+)'", TS.read_text(encoding="utf-8")):
    names[m.group(1)] = m.group(2)
names["ERP_PRODUCTION_PROCESS_MACHINE_PLAN"] = "工程別設備別計画"

content = KT.read_text(encoding="utf-8")

content = re.sub(
    r'AppMenuNode\.Leaf\("([^"]+)", "[^"]*",',
    lambda m: f'AppMenuNode.Leaf("{m.group(1)}", "{names[m.group(1)]}",'
    if m.group(1) in names
    else m.group(0),
    content,
)

content = re.sub(
    r'code = "([^"]+)",\n\s+label = "[^"]*",',
    lambda m: f'code = "{m.group(1)}",\n            label = "{names[m.group(1)]}",'
    if m.group(1) in names
    else m.group(0),
    content,
)

proc_map = {
    "CUTTING": "切断",
    "CHAMFERING": "面取",
    "FORMING": "成型",
    "PLATING": "メッキ",
    "WELDING": "溶接",
    "INSPECTION": "検査",
}
for suffix, label in proc_map.items():
    content = re.sub(
        rf'MesActualAnalysisProcess\("{suffix}", "[^"]*",',
        f'MesActualAnalysisProcess("{suffix}", "{label}",',
        content,
    )

cat_map = {
    "productivity": "生産性",
    "utilization": "稼働率",
    "progress": "進捗",
    "quality": "品質",
    "cost": "コスト",
}
for cat, label in cat_map.items():
    content = re.sub(
        rf'analysisCategory = "{cat}",\n\s+categoryLabel = "[^"]*",',
        f'analysisCategory = "{cat}",\n                            categoryLabel = "{label}",',
        content,
    )

header = (
    "/**\n"
    " * 与 Smart-EMAPs frontend `src/router/menuConfig.ts` 层级对齐。\n"
    " *\n"
    " * 说明：\n"
    " * - Android 侧暂时只用「路由 path + 显示名称」来驱动左侧菜单与 Tab。\n"
    " * - 图标映射为简化映射，不影响路由一致性。\n"
    " */"
)
content = re.sub(r"/\*\*.*?\*/", header, content, count=1, flags=re.DOTALL)

content = content.replace(
    "/** ?? path ???? menu code??? leaf ??? path ?????? */",
    "/** 指定 path に紐づく menu code（複数 leaf が同一 path の場合あり） */",
)
content = content.replace(
    "/** ????? menu_codes ???????????????? */",
    "/** ユーザーの menu_codes に基づき表示可能なメニューを返す */",
)
content = content.replace(
    "/** Web `menuConfig.ts` ???????? + ????? leaf ?????? */",
    "/** Web `menuConfig.ts` と同様に、工程名 + 分析種別で leaf ラベルを生成 */",
)

KT.write_text(content, encoding="utf-8", newline="\n")
print("OK", names.get("DASHBOARD"))
