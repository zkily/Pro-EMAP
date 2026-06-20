package com.example.smart_emap.ui.erp.production.metrics

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UtilizationConfig = ProductionMetricConfig(
    title = "稼働率",
    description = "設備や工程の計画時間に対する実稼働状況を確認する生産指標ページ",
    emoji = "🖥️",
    gradient = listOf(Color(0xFF409EFF), Color(0xFF36CFC9)),
    formula = "稼働率 = 実稼働時間 ÷ 計画稼働時間 × 100",
    note = "設備能力がどれだけ使われているかを見る指標です。生産計画、指示、実績を結びつけることで負荷の偏りや未稼働時間を把握できます。",
    summaryCards = listOf(
        ProductionMetricSummaryCard("当月稼働率", "-- %", "設備実績 API 接続後に算出"),
        ProductionMetricSummaryCard("実稼働時間", "-- h", "実績または作業ログから集計"),
        ProductionMetricSummaryCard("計画稼働時間", "-- h", "計画・カレンダーから集計"),
    ),
    analysisRows = listOf(
        ProductionMetricAnalysisRow("設備別", "設備ごとの稼働率を比較して負荷偏りを可視化", "machines / instruction_plans"),
        ProductionMetricAnalysisRow("工程別", "工程単位で計画負荷と実績負荷の差を見る", "process_cd / machine_type"),
        ProductionMetricAnalysisRow("期間別", "日別・週別・月別の稼働率推移を確認", "start_date / actual_datetime"),
    ),
    implementationMemos = listOf(
        "計画稼働時間は設備カレンダー、指示計画、標準工数のどれを基準にするかを決める必要があります。",
        "実稼働時間は作業開始・終了ログがない場合、実績数量 × 標準サイクルタイムで近似できます。",
        "将来的に設備別ヒートマップ、低稼働設備一覧、工程別負荷グラフを追加できる構成にしています。",
    ),
)

private val DefectConfig = ProductionMetricConfig(
    title = "不良率",
    description = "工程・製品別に不良数量の発生傾向を確認する品質系の生産指標ページ",
    emoji = "⚠️",
    gradient = listOf(Color(0xFFE6A23C), Color(0xFFF7BA2A)),
    formula = "不良率 = 不良数 ÷ (良品実績数 + 不良数) × 100",
    note = "投入または完成実績に対して不良がどれだけ発生したかを見る指標です。工程別の不良率を追うことで品質異常の発生箇所を特定しやすくします。",
    summaryCards = listOf(
        ProductionMetricSummaryCard("当月不良率", "-- %", "API 接続後に実績から算出"),
        ProductionMetricSummaryCard("不良数", "--", "stock_transaction_logs の不良数量"),
        ProductionMetricSummaryCard("良品実績数", "--", "実績数量から集計"),
    ),
    analysisRows = listOf(
        ProductionMetricAnalysisRow("工程別", "不良率が高い工程を抽出して重点改善対象を確認", "stock_transaction_logs.process_cd"),
        ProductionMetricAnalysisRow("製品別", "製品ごとの不良傾向や特定品番の品質変動を確認", "product_cd / product_name"),
        ProductionMetricAnalysisRow("日別推移", "不良率の急上昇日を検知して原因調査につなげる", "transaction_time"),
    ),
    implementationMemos = listOf(
        "不良数は在庫取引ログの transaction_type = 不良 を基本データにする想定です。",
        "分母に廃棄を含めるかどうかは廃棄率との定義分離に合わせて調整できます。",
        "将来的に不良理由別集計、工程別ランキング、製品別明細テーブルを追加できる構成にしています。",
    ),
)

@Composable
fun UtilizationRateScreen() {
    ProductionMetricScreen(UtilizationConfig)
}

@Composable
fun DefectRateScreen() {
    ProductionMetricScreen(DefectConfig)
}
