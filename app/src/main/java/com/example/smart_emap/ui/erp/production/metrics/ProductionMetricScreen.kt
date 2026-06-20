package com.example.smart_emap.ui.erp.production.metrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors
import com.example.smart_emap.ui.erp.production.planning.ProductionScaffold
import com.example.smart_emap.ui.erp.production.planning.productionPageScaffoldPadding

data class ProductionMetricSummaryCard(
    val label: String,
    val value: String,
    val sub: String,
)

data class ProductionMetricAnalysisRow(
    val axis: String,
    val content: String,
    val source: String,
)

data class ProductionMetricConfig(
    val title: String,
    val description: String,
    val emoji: String,
    val gradient: List<Color>,
    val formula: String,
    val note: String,
    val summaryCards: List<ProductionMetricSummaryCard>,
    val analysisRows: List<ProductionMetricAnalysisRow>,
    val implementationMemos: List<String>,
)

@Composable
fun ProductionMetricScreen(config: ProductionMetricConfig) {
    ProductionScaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF8FAFC), Color(0xFFEEF2F7)),
                    ),
                ),
        ) {
            var shown by remember { mutableStateOf(false) }
            LaunchedAppear { shown = true }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .productionPageScaffoldPadding(padding, horizontal = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricHeader(config)

                AnimatedVisibility(
                    visible = shown,
                    enter = fadeIn() + expandVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryGrid(config.summaryCards)
                        MetricPanel(markColor = ProductionPlanningColors.AccentBlue, title = "指標定義") {
                            DefinitionBox(config.formula, config.note)
                        }
                        MetricPanel(markColor = ProductionPlanningColors.AccentGreen, title = "分析軸") {
                            AnalysisTable(config.analysisRows)
                        }
                        MetricPanel(markColor = Color(0xFF909399), title = "実装メモ", muted = true) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                config.implementationMemos.forEach { memo ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("・", fontSize = 12.sp, color = Color(0xFF475569))
                                        Text(memo, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaunchedAppear(onAppear: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) { onAppear() }
}

@Composable
private fun MetricHeader(config: ProductionMetricConfig) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, Color(0x4094A3B8), shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(config.gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(config.emoji, fontSize = 22.sp)
            }
            Column {
                Text(config.title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1F2937))
                Text(config.description, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
        Surface(color = Color(0xFFEFF2F7), shape = RoundedCornerShape(999.dp)) {
            Text(
                "生産指標",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 11.sp,
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun SummaryGrid(cards: List<ProductionMetricSummaryCard>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        cards.forEach { card ->
            val shape = RoundedCornerShape(12.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC))))
                    .border(1.dp, Color(0xFFE5E7EB), shape)
                    .padding(12.dp),
            ) {
                Text(card.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(Modifier.height(6.dp))
                Text(card.value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                Spacer(Modifier.height(2.dp))
                Text(card.sub, fontSize = 10.sp, color = Color(0xFF94A3B8), lineHeight = 13.sp)
            }
        }
    }
}

@Composable
private fun MetricPanel(
    markColor: Color,
    title: String,
    muted: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (muted) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp)
                } else Modifier,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(markColor),
            )
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF334155))
        }
        content()
    }
}

@Composable
private fun DefinitionBox(formula: String, note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(formula, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
        Text(note, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 19.sp)
    }
}

@Composable
private fun AnalysisTable(rows: List<ProductionMetricAnalysisRow>) {
    val border = Color(0xFFE2E8F0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProductionPlanningColors.TableHeaderBg)
                .padding(vertical = 7.dp, horizontal = 8.dp),
        ) {
            HeaderCell("分析軸", 0.32f)
            HeaderCell("内容", 0.36f)
            HeaderCell("想定データ", 0.32f)
        }
        rows.forEachIndexed { index, r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) Color.White else ProductionPlanningColors.TableStripe)
                    .padding(vertical = 7.dp, horizontal = 8.dp),
            ) {
                BodyCell(r.axis, 0.32f, FontWeight.SemiBold, Color(0xFF334155))
                BodyCell(r.content, 0.36f, FontWeight.Normal, Color(0xFF475569))
                BodyCell(r.source, 0.32f, FontWeight.Normal, Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text,
        modifier = Modifier.weight(weight).padding(end = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155),
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BodyCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight,
    color: Color,
) {
    Text(
        text,
        modifier = Modifier.weight(weight).padding(end = 4.dp),
        fontSize = 11.sp,
        fontWeight = fontWeight,
        color = color,
        lineHeight = 15.sp,
    )
}
