package com.example.smart_emap.ui.erp.production.planning

data class DataMgmtConfirmSpec(
    val title: String,
    val message: String,
    val confirmLabel: String = "実行",
)

object DataMgmtUpdateConfig {
    val simpleConfirmSpecs = mapOf(
        "carry-over" to DataMgmtConfirmSpec("繰越データ更新確認", "繰越データを更新します。"),
        "actual" to DataMgmtConfirmSpec("実績データ更新確認", "実績データを更新します。"),
        "defect" to DataMgmtConfirmSpec("不良データ更新確認", "不良データを更新します。"),
        "scrap" to DataMgmtConfirmSpec("廃棄データ更新確認", "廃棄データを更新します。"),
        "on-hold" to DataMgmtConfirmSpec("保留データ更新確認", "保留データを更新します。"),
        "production-dates" to DataMgmtConfirmSpec(
            "生産計画日更新確認",
            "各工程の生産計画日を営業日換算で更新します。",
        ),
    )

    val allUpdateSteps = listOf(
        "受注データ更新",
        "実績データ更新",
        "不良データ更新",
        "廃棄データ更新",
        "保留データ更新",
        "計画データ更新",
        "在庫・推移・安全在庫更新",
    )

    const val planConfirmMessage =
        "当月月初～+5ヶ月の plan 列をいったんクリアしたうえで、schedule_details の日次 planned_qty を設備の工程に応じて集計し、production_summarys の plan 列に反映して actual_plan を更新します。"

}
