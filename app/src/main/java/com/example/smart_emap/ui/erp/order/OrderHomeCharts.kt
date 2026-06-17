package com.example.smart_emap.ui.erp.order

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

private val chartNumberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)

@Composable
fun OrderHomeMonthlyChart(
    points: List<OrderHomeMonthlyPointUi>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    if (points.isEmpty()) {
        OrderHomeChartEmpty("データがありません")
        return
    }
    Canvas(modifier = modifier) {
        val leftPad = 42f
        val rightPad = 10f
        val bottomPad = 44f
        val topPad = 16f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - bottomPad - topPad
        val slotW = chartW / points.size.coerceAtLeast(1)
        val barW = minOf(slotW * 0.22f, 14f)
        val gap = 3f
        val maxValue = max(
            points.maxOfOrNull { max(it.forecastUnits, it.forecastTotalUnits) } ?: 1,
            1,
        )
        val anim = progress.value

        (0..4).forEach { tick ->
            val value = (maxValue * tick) / 4
            val y = topPad + chartH * (1f - tick / 4f)
            if (tick > 0) {
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1f,
                )
            }
            if (tick > 0) {
                drawChartText(
                    chartNumberFormat.format(value),
                    leftPad - 6f,
                    y + 4f,
                    8.5f * density,
                    Color(0xFF94A3B8),
                    Paint.Align.RIGHT,
                )
            }
        }

        points.forEachIndexed { index, point ->
            val xCenter = leftPad + index * slotW + slotW / 2f
            val fuH = chartH * (point.forecastUnits.toFloat() / maxValue) * anim
            val ftH = chartH * (point.forecastTotalUnits.toFloat() / maxValue) * anim
            val fuX = xCenter - barW - gap / 2f
            val ftX = xCenter + gap / 2f
            val fuColors = if (point.isFuture) {
                listOf(Color(0x8CA78BFA), Color(0x616366F1))
            } else {
                listOf(Color(0xFFA5B4FC), Color(0xFF6366F1))
            }
            val ftColors = if (point.isFuture) {
                listOf(Color(0x8C5EEAD4), Color(0x6114B8A6))
            } else {
                listOf(Color(0xFF5EEAD4), Color(0xFF14B8A6))
            }
            drawRoundRect(
                brush = Brush.verticalGradient(fuColors),
                topLeft = Offset(fuX, topPad + chartH - fuH),
                size = Size(barW, fuH),
                cornerRadius = CornerRadius(6f, 6f),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(ftColors),
                topLeft = Offset(ftX, topPad + chartH - ftH),
                size = Size(barW, ftH),
                cornerRadius = CornerRadius(6f, 6f),
            )
            drawChartText(
                point.label,
                xCenter,
                topPad + chartH + 14f * density,
                8.5f * density,
                if (point.isFuture) Color(0xFF7C3AED) else Color(0xFF475569),
                Paint.Align.CENTER,
                bold = point.isFuture,
            )
        }
    }
}

@Composable
fun OrderHomeDailyChart(
    rows: List<OrderHomeDailyRowUi>,
    asOfDate: String,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(rows, asOfDate) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    if (rows.isEmpty()) {
        OrderHomeChartEmpty("データがありません")
        return
    }
    Canvas(modifier = modifier) {
        val leftPad = 44f
        val rightPad = 8f
        val bottomPad = 34f
        val topPad = 18f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - bottomPad - topPad
        val barCount = rows.size
        val slotW = chartW / barCount
        val barW = minOf(slotW * 0.46f, 18f)
        val maxValue = max(rows.maxOfOrNull { it.confirmedUnits } ?: 1, 1)
        val anim = progress.value
        val step = when {
            barCount > 18 -> 3
            barCount > 12 -> 2
            else -> 1
        }

        (0..4).forEach { tick ->
            if (tick == 0) return@forEach
            val ratio = tick / 4f
            val y = topPad + chartH * (1f - ratio)
            drawLine(
                color = Color(0xFFE8ECF1),
                start = Offset(leftPad, y),
                end = Offset(size.width - rightPad, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
            )
            drawChartText(
                chartNumberFormat.format((maxValue * tick) / 4),
                leftPad - 8f,
                y + 3.5f,
                9f * density,
                Color(0xFF94A3B8),
                Paint.Align.RIGHT,
            )
        }

        rows.forEachIndexed { index, row ->
            val ratio = row.confirmedUnits.toFloat() / maxValue
            val barH = chartH * ratio * anim
            val x = leftPad + index * slotW + (slotW - barW) / 2f
            val y = topPad + chartH - barH
            val visual = dailyBarVisual(row.date, asOfDate)
            if (barH > 1f) {
                drawRoundRect(
                    color = visual.shadow,
                    topLeft = Offset(x + 1f, y + 2.5f),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(visual.gradient),
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
            if (anim > 0.9f && row.confirmedUnits > 0) {
                drawChartText(
                    chartNumberFormat.format(row.confirmedUnits),
                    x + barW / 2f,
                    maxOf(y - 5f * density, topPad),
                    8.5f * density,
                    visual.labelColor,
                    Paint.Align.CENTER,
                    bold = row.date == asOfDate,
                )
            }
            val showLabel = index % step == 0 || row.date == asOfDate
            if (showLabel) {
                val label = row.date.takeLast(5).replace('-', '/')
                drawChartText(
                    label,
                    x + barW / 2f,
                    topPad + chartH + 10f * density,
                    9f * density,
                    if (row.date == asOfDate) Color(0xFFC2410C) else Color(0xFF64748B),
                    Paint.Align.CENTER,
                    bold = row.date == asOfDate,
                )
            }
        }
    }
}

@Composable
fun OrderHomeProductRankChart(
    rows: List<OrderHomeProductRankUi>,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(rows) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    if (rows.isEmpty()) {
        OrderHomeChartEmpty("当月の受注データがありません")
        return
    }
    Canvas(modifier = modifier) {
        val leftPad = 4f
        val rightPad = 28f
        val topPad = 8f
        val bottomPad = 8f
        val rowH = minOf((size.height - topPad - bottomPad) / rows.size, 34f * density)
        val chartW = size.width - leftPad - rightPad
        val maxValue = max(rows.maxOfOrNull { it.confirmedUnits } ?: 1, 1)
        val anim = progress.value

        rows.forEachIndexed { index, row ->
            val y = topPad + index * rowH
            val label = rankLabel(row, index)
            drawChartText(
                label,
                leftPad,
                y + rowH * 0.62f,
                9f * density,
                Color(0xFF475569),
                Paint.Align.LEFT,
                bold = index < 3,
            )
            val barLeft = leftPad + (168f * density).coerceAtMost(size.width * 0.42f)
            val barMaxW = size.width - barLeft - rightPad
            val barW = barMaxW * (row.confirmedUnits.toFloat() / maxValue) * anim
            val barTop = y + rowH * 0.28f
            val barHeight = rowH * 0.44f
            drawRoundRect(
                brush = Brush.horizontalGradient(rankGradient(index)),
                topLeft = Offset(barLeft, barTop),
                size = Size(barW.coerceAtLeast(4f), barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
            if (row.confirmedUnits > 0) {
                drawChartText(
                    chartNumberFormat.format(row.confirmedUnits),
                    barLeft + barW + 6f * density,
                    barTop + barHeight * 0.72f,
                    9f * density,
                    Color(0xFF64748B),
                    Paint.Align.LEFT,
                    bold = index < 3,
                )
            }
        }
    }
}

@Composable
private fun OrderHomeChartEmpty(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(message, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

private data class DailyBarVisual(
    val gradient: List<Color>,
    val shadow: Color,
    val labelColor: Color,
)

private fun dailyBarVisual(date: String, asOf: String): DailyBarVisual = when {
    date < asOf -> DailyBarVisual(
        gradient = listOf(Color(0xFFC7D2FE), Color(0xFF818CF8), Color(0xFF4338CA)),
        shadow = Color(0x334338CA),
        labelColor = Color(0xFF4338CA),
    )
    date == asOf -> DailyBarVisual(
        gradient = listOf(Color(0xFF6EE7B7), Color(0xFF34D399), Color(0xFF047857)),
        shadow = Color(0x52047857),
        labelColor = Color(0xFF047857),
    )
    else -> DailyBarVisual(
        gradient = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
        shadow = Color(0x3394A3B8),
        labelColor = Color(0xFF64748B),
    )
}

private fun rankLabel(row: OrderHomeProductRankUi, index: Int): String {
    val base = row.productName.ifBlank { row.productCd }.trim()
    val trimmed = if (base.length > 18) "${base.take(18)}…" else base
    return "${index + 1}. $trimmed"
}

private fun rankGradient(index: Int): List<Color> = when (index) {
    0 -> listOf(Color(0xFFFCD34D), Color(0xFFD97706))
    1 -> listOf(Color(0xFFE2E8F0), Color(0xFF64748B))
    2 -> listOf(Color(0xFFFDBA74), Color(0xFFC2410C))
    else -> listOf(Color(0xFFA5B4FC), Color(0xFF4338CA))
}

private fun DrawScope.drawChartText(
    text: String,
    x: Float,
    y: Float,
    fontSizePx: Float,
    color: Color,
    align: Paint.Align,
    bold: Boolean = false,
) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toArgb()
            textSize = fontSizePx
            textAlign = align
            typeface = Typeface.create(
                Typeface.DEFAULT,
                if (bold) Typeface.BOLD else Typeface.NORMAL,
            )
        }
        drawText(text, x, y, paint)
    }
}

@Composable
fun OrderHomeChartLegend(
    items: List<Pair<Color, String>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, (color, label) ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(14.dp))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(8.dp)
                        .background(color, RoundedCornerShape(4.dp)),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun OrderHomeChartLegendDots(
    items: List<Pair<Color, String>>,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "legend-pulse")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, (color, label) ->
            val pulse by transition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400 + index * 200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot-$index",
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(pulse)
                        .shadow(2.dp, RoundedCornerShape(50), spotColor = color.copy(alpha = 0.5f))
                        .background(color, RoundedCornerShape(50)),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
        }
    }
}
