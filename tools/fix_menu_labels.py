# -*- coding: utf-8 -*-
"""Restore Japanese menu labels in AppMenuConfig.kt from Web menuConfig.ts."""
import re
from pathlib import Path

TS = Path(r"C:\Users\arai-235\Desktop\Smart-EMAPs\frontend\src\router\menuConfig.ts")
KT = Path(r"C:\Users\arai-235\Desktop\SmartEMAP\app\src\main\java\com\example\smart_emap\ui\shell\AppMenuConfig.kt")

names: dict[str, str] = {}
for m in re.finditer(r"\{ code: '([^']+)', name: '([^']+)'", TS.read_text(encoding="utf-8")):
    names[m.group(1)] = m.group(2)

names["ERP_PRODUCTION_PROCESS_MACHINE_PLAN"] = "工程別設備別計画"

text = KT.read_text(encoding="utf-8", errors="replace")


def leaf_repl(match: re.Match[str]) -> str:
    code, _old, rest = match.group(1), match.group(2), match.group(3)
    label = names.get(code, _old)
    return f'AppMenuNode.Leaf("{code}", "{label}", {rest}'


text = re.sub(
    r'AppMenuNode\.Leaf\("([^"]+)", "([^"]*)", ([^\n]+)',
    leaf_repl,
    text,
)

text = re.sub(
    r'(\s+)code = "([^"]+)",\n\1label = "([^"]*)",',
    lambda m: f'{m.group(1)}code = "{m.group(2)}",\n{m.group(1)}label = "{names.get(m.group(2), m.group(3))}",',
    text,
)

text = text.replace(
    "* Web Smart-EMAPs `src/router/menuConfig.ts` ???????????",
    "* Web Smart-EMAPs `src/router/menuConfig.ts` と同等のメニュー定義。",
)
text = text.replace("* ??:", "* 方針:")
text = text.replace(
    "* - Android ???????? path + ??????????? Placeholder?",
    "* - Android 実装済み画面のみ path + ラベルを登録（未実装は Placeholder）",
)
text = text.replace(
    "* - ????????? menu_codes ?????",
    "* - 表示権限はユーザー menu_codes でフィルタ",
)
text = text.replace(
    "/** Web `menuConfig.ts` ???????? + ????? leaf ?????? */",
    "/** Web `menuConfig.ts` の工程別分析 leaf（例: 切断生産性） */",
)
text = text.replace(
    "/** 同一 path に複数 menu code がある場合は leaf 以外の path 一致も含める */",
    "/** 同一 path に複数 menu code がある場合は leaf 以外の path 一致も含める */",
)
text = text.replace(
    "/** ログインユーザーの menu_codes に基づき表示可能なメニューのみ返す */",
    "/** ログインユーザーの menu_codes に基づき表示可能なメニューのみ返す */",
)

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

text = re.sub(
    r"/\*\* Web `menuConfig\.ts` .*? \*/",
    "/** Web `menuConfig.ts` の工程別分析 leaf（例: 切断生産性） */",
    text,
    count=1,
)

KT.write_text(text, encoding="utf-8")
print(f"Updated labels from {len(names)} menu entries")
print("DASHBOARD:", names.get("DASHBOARD"))
print("MES_ACTUAL_INSPECTION:", names.get("MES_ACTUAL_INSPECTION"))
