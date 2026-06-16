# -*- coding: utf-8 -*-
import re
from pathlib import Path

TS = Path(r"C:\Users\arai-235\Desktop\Smart-EMAPs\frontend\src\router\menuConfig.ts")
KT = Path(r"C:\Users\arai-235\Desktop\SmartEMAP\app\src\main\java\com\example\smart_emap\ui\shell\AppMenuConfig.kt")

names: dict[str, str] = {}
# Web version uses "name" or "title" or something else? Let's be more flexible.
# Based on the previous script's regex: { code: '([^']+)', name: '([^']+)'
content_ts = TS.read_text(encoding="utf-8")
for m in re.finditer(r"code:\s*'([^']+)',\s*name:\s*'([^']+)'", content_ts):
    names[m.group(1)] = m.group(2)

# Manual overrides if needed
names["ERP_PRODUCTION_PROCESS_MACHINE_PLAN"] = "工程別設備別計画"
if "DASHBOARD" not in names: names["DASHBOARD"] = "ダッシュボード"

text = KT.read_text(encoding="utf-8", errors="replace")

def leaf_repl(match: re.Match[str]) -> str:
    code = match.group(1)
    old_label = match.group(2)
    rest = match.group(3)
    label = names.get(code, old_label)
    # If it's still ???? then we don't have a match, keep old if it wasn't ????
    if label.strip("?") == "" and old_label.strip("?") != "":
        label = old_label
    return f'AppMenuNode.Leaf("{code}", "{label}", {rest}'

text = re.sub(
    r'AppMenuNode\.Leaf\("([^"]+)",\s*"([^"]*)",\s*([^\n]+)',
    leaf_repl,
    text,
)

# Fix Groups
def group_repl(match: re.Match[str]) -> str:
    indent = match.group(1)
    code = match.group(2)
    old_label = match.group(3)
    label = names.get(code, old_label)
    if label.strip("?") == "" and old_label.strip("?") != "":
        label = old_label
    return f'{indent}code = "{code}",\n{indent}label = "{label}",'

text = re.sub(
    r'(\s+)code = "([^"]+)",\n\1label = "([^"]*)",',
    group_repl,
    text,
)

# Fix static analysis labels
text = text.replace("???????", "ダッシュボード")
text = text.replace("ERP??????", "ERPサブシステム")
text = text.replace("APS??????", "APSサブシステム")
text = text.replace("MES??????", "MESサブシステム")

# Analysis categories
text = text.replace('categoryLabel = "???"', 'categoryLabel = "分析"')
text = text.replace('categoryLabel = "??"', 'categoryLabel = "進捗"')
text = text.replace('categoryLabel = "??"', 'categoryLabel = "品質"') # Note: this might overlap

# Process labels in MesActualAnalysisProcess
proc_map = {
    "CUTTING": "切断",
    "CHAMFERING": "面取",
    "FORMING": "成型",
    "PLATING": "メッキ",
    "WELDING": "溶接",
    "INSPECTION": "検査",
}
for code, label in proc_map.items():
    text = re.sub(
        rf'MesActualAnalysisProcess\("{code}", "[^"]*",',
        f'MesActualAnalysisProcess("{code}", "{label}",',
        text,
    )

# Clean up top comments
text = re.sub(r"/\*\*.*?\*/", "/** Web Smart-EMAPs menuConfig.ts と同期 */", text, count=1, flags=re.DOTALL)

KT.write_text(text, encoding="utf-8")
print(f"Updated labels from {len(names)} menu entries")
