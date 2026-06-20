package com.example.smart_emap.ui.erp.production.actual

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

internal val ActualPalette = listOf(
    Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B),
    Color(0xFFEF4444), Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFFEC4899),
    Color(0xFF14B8A6), Color(0xFFF97316), Color(0xFF0EA5E9), Color(0xFFA855F7),
)

internal fun formatActualNum(value: Double, fraction: Int = 0): String {
    return if (fraction <= 0) "%,d".format(value.toLong())
    else "%,.${fraction}f".format(value)
}

private fun DrawScope.drawAxisLabel(
    text: String,
    x: Float,
    y: Float,
    colorArgb: Int,
    fontSizePx: Float,
    align: Paint.Align = Paint.Align.CENTER,
    bold: Boolean = false,
) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = colorArgb
            textSize = fontSizePx
            textAlign = align
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        drawText(text, x, y, paint)
    }
}

/** 縦棒グラフ（日別など）。値ラベルと X 軸ラベルを描画。 */
@Composable
fun ActualVerticalBarChart(
    labels: List<String>,
    values: List<Double>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF3B82F6),
    valueFraction: Int = 0,
) {
    if (labels.isEmpty() || values.isEmpty()) {
        ChartEmpty(modifier)
        return
    }
    val maxValue = max(values.maxOrNull() ?: 0.0, 0.0)
    val axisMuted = Color(0xFF94A3B8).toArgb()
    val labelColor = Color(0xFF475569).toArgb()
    Canvas(modifier = modifier) {
        val n = values.size
        val leftPad = 44f
        val rightPad = 10f
        val topPad = 18f
        val bottomPad = if (n > 12) 34f else 24f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val slotW = chartW / n
        val barW = min(slotW * 0.5f, 26f)
        val safeMax = if (maxValue <= 0.0) 1.0 else maxValue
        val valueFontPx = (if (n > 18) 7.5f else 8.5f) * density
        val xFontPx = 9f * density
        val xStep = when { n > 18 -> 3; n > 12 -> 2; else -> 1 }

        (0..4).forEach { tick ->
            val ratio = tick / 4f
            val gridY = topPad + chartH * (1f - ratio)
            if (tick > 0) {
                drawLine(
                    color = Color(0xFFE8ECF1),
                    start = Offset(leftPad, gridY),
                    end = Offset(size.width - rightPad, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
                )
                drawAxisLabel(
                    formatActualNum(safeMax * ratio),
                    leftPad - 6f, gridY + 3.5f, axisMuted, 8.5f * density, Paint.Align.RIGHT,
                )
            }
        }
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - rightPad, topPad + chartH),
            strokeWidth = 1.2f,
        )

        values.forEachIndexed { i, v ->
            val ratio = (v / safeMax).toFloat()
            val barH = chartH * ratio
            val x = leftPad + i * slotW + (slotW - barW) / 2f
            val y = topPad + chartH - barH
            if (barH > 1f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(5f, 5f),
                )
                if (v > 0) {
                    drawAxisLabel(
                        formatActualNum(v, valueFraction),
                        x + barW / 2f, max(y - 5f, topPad + 2f), labelColor, valueFontPx, Paint.Align.CENTER, true,
                    )
                }
            }
            if (i % xStep == 0) {
                drawAxisLabel(
                    labels[i], x + barW / 2f, topPad + chartH + 13f, labelColor, xFontPx, Paint.Align.CENTER,
                )
            }
        }
    }
}

/** 横棒グラフ（工程別合計・材料別上位）。 */
@Composable
fun ActualHorizontalBarChart(
    items: List<ChartBarItem>,
    modifier: Modifier = Modifier,
    valueFraction: Int = 0,
) {
    if (items.isEmpty()) {
        ChartEmpty(modifier)
        return
    }
    val maxValue = max(items.maxOf { it.value }, 0.0)
    val labelColor = Color(0xFF475569).toArgb()
    val valueColor = Color(0xFF334155).toArgb()
    Canvas(modifier = modifier) {
        val n = items.size
        val leftPad = 96f
        val rightPad = 48f
        val topPad = 6f
        val bottomPad = 6f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val slotH = chartH / n
        val barH = min(slotH * 0.56f, 22f)
        val safeMax = if (maxValue <= 0.0) 1.0 else maxValue
        val fontPx = 9f * density

        items.forEachIndexed { i, item ->
            val ratio = (item.value / safeMax).toFloat()
            val barW = chartW * ratio
            val y = topPad + i * slotH + (slotH - barH) / 2f
            val label = if (item.label.length > 12) item.label.take(12) + "…" else item.label
            drawAxisLabel(label, leftPad - 6f, y + barH / 2f + 3f, labelColor, fontPx, Paint.Align.RIGHT)
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(leftPad, y),
                size = Size(chartW, barH),
                cornerRadius = CornerRadius(4f, 4f),
            )
            if (barW > 1f) {
                drawRoundRect(
                    color = item.color,
                    topLeft = Offset(leftPad, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }
            drawAxisLabel(
                formatActualNum(item.value, valueFraction),
                leftPad + barW + 5f, y + barH / 2f + 3f, valueColor, fontPx, Paint.Align.LEFT, true,
            )
        }
    }
}

/** ドーナツ（構成比）。凡例は呼び出し側で描画。 */
@Composable
fun ActualDonutChart(
    items: List<ChartBarItem>,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.value }
    if (items.isEmpty() || total <= 0.0) {
        ChartEmpty(modifier)
        return
    }
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outer = min(size.width, size.height) / 2f - 8f
        val inner = outer * 0.58f
        var startAngle = -90f
        items.forEach { item ->
            val sweep = (item.value / total * 360.0).toFloat()
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(cx - outer, cy - outer),
                size = Size(outer * 2, outer * 2),
            )
            startAngle += sweep
        }
        drawCircle(color = Color.White, radius = inner, center = Offset(cx, cy))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartLegend(items: List<ChartBarItem>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(item.color),
                )
                Spacer(Modifier.size(4.dp))
                Text(item.label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ChartEmpty(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("データがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

data class ChartBarItem(
    val label: String,
    val value: Double,
    val color: Color,
)

data class StackSeries(
    val name: String,
    val color: Color,
    val values: List<Double>,
)

/** 積み上げ縦棒（不良＋廃棄など）。 */
@Composable
fun ActualStackedBarChart(
    labels: List<String>,
    series: List<StackSeries>,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty() || series.isEmpty()) {
        ChartEmpty(modifier)
        return
    }
    val totals = labels.indices.map { i -> series.sumOf { it.values.getOrElse(i) { 0.0 } } }
    val maxValue = max(totals.maxOrNull() ?: 0.0, 0.0)
    val axisMuted = Color(0xFF94A3B8).toArgb()
    val labelColor = Color(0xFF475569).toArgb()
    Canvas(modifier = modifier) {
        val n = labels.size
        val leftPad = 44f
        val rightPad = 10f
        val topPad = 14f
        val bottomPad = if (n > 8) 36f else 24f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val slotW = chartW / n
        val barW = min(slotW * 0.5f, 30f)
        val safeMax = if (maxValue <= 0.0) 1.0 else maxValue
        val xFontPx = 9f * density

        (0..4).forEach { tick ->
            val ratio = tick / 4f
            val gridY = topPad + chartH * (1f - ratio)
            if (tick > 0) {
                drawLine(
                    color = Color(0xFFE8ECF1),
                    start = Offset(leftPad, gridY),
                    end = Offset(size.width - rightPad, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
                )
                drawAxisLabel(formatActualNum(safeMax * ratio), leftPad - 6f, gridY + 3.5f, axisMuted, 8.5f * density, Paint.Align.RIGHT)
            }
        }
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - rightPad, topPad + chartH),
            strokeWidth = 1.2f,
        )

        labels.indices.forEach { i ->
            val x = leftPad + i * slotW + (slotW - barW) / 2f
            var accBottom = topPad + chartH
            series.forEach { s ->
                val v = s.values.getOrElse(i) { 0.0 }
                if (v > 0) {
                    val h = (chartH * (v / safeMax)).toFloat()
                    val y = accBottom - h
                    drawRoundRect(
                        color = s.color,
                        topLeft = Offset(x, y),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                    accBottom = y
                }
            }
            val step = if (n > 12) 2 else 1
            if (i % step == 0) {
                drawAxisLabel(labels[i], x + barW / 2f, topPad + chartH + 13f, labelColor, xFontPx, Paint.Align.CENTER)
            }
        }
    }
}

/** 縦棒（本数・左軸）＋ 折線（％・右軸）の組合せ。 */
@Composable
fun ActualComboBarLineChart(
    labels: List<String>,
    barValues: List<Double>,
    lineValues: List<Double?>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF7C3AED),
    lineColor: Color = Color(0xFFEA580C),
) {
    if (labels.isEmpty()) {
        ChartEmpty(modifier)
        return
    }
    val barMax = max(barValues.maxOrNull() ?: 0.0, 0.0)
    val lineMax = max(lineValues.filterNotNull().maxOrNull() ?: 0.0, 0.0)
    val axisMuted = Color(0xFF94A3B8).toArgb()
    val labelColor = Color(0xFF475569).toArgb()
    val lineLabelColor = lineColor.toArgb()
    Canvas(modifier = modifier) {
        val n = labels.size
        val leftPad = 44f
        val rightPad = 40f
        val topPad = 16f
        val bottomPad = if (n > 6) 50f else 30f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val slotW = chartW / n
        val barW = min(slotW * 0.5f, 30f)
        val safeBarMax = if (barMax <= 0.0) 1.0 else barMax
        val safeLineMax = if (lineMax <= 0.0) 1.0 else lineMax
        val xFontPx = 8.5f * density

        (0..4).forEach { tick ->
            val ratio = tick / 4f
            val gridY = topPad + chartH * (1f - ratio)
            if (tick > 0) {
                drawLine(
                    color = Color(0xFFE8ECF1),
                    start = Offset(leftPad, gridY),
                    end = Offset(size.width - rightPad, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
                )
                drawAxisLabel(formatActualNum(safeBarMax * ratio), leftPad - 6f, gridY + 3.5f, axisMuted, 8f * density, Paint.Align.RIGHT)
                drawAxisLabel("%.0f%%".format(safeLineMax * ratio), size.width - rightPad + 6f, gridY + 3.5f, lineLabelColor, 8f * density, Paint.Align.LEFT)
            }
        }
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - rightPad, topPad + chartH),
            strokeWidth = 1.2f,
        )

        // bars
        labels.indices.forEach { i ->
            val v = barValues.getOrElse(i) { 0.0 }
            val x = leftPad + i * slotW + (slotW - barW) / 2f
            if (v > 0) {
                val h = (chartH * (v / safeBarMax)).toFloat()
                val y = topPad + chartH - h
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }
            val step = if (n > 8) 2 else 1
            if (i % step == 0) {
                drawAxisLabel(labels[i], x + barW / 2f, topPad + chartH + 13f, labelColor, xFontPx, Paint.Align.CENTER)
            }
        }

        // line
        fun pointAt(i: Int): Offset? {
            val v = lineValues.getOrNull(i) ?: return null
            val cx = leftPad + i * slotW + slotW / 2f
            val cy = topPad + chartH - (chartH * (v / safeLineMax)).toFloat()
            return Offset(cx, cy)
        }
        for (i in 0 until n - 1) {
            val p1 = pointAt(i) ?: continue
            val p2 = pointAt(i + 1) ?: continue
            drawLine(color = lineColor, start = p1, end = p2, strokeWidth = 2.2f)
        }
        labels.indices.forEach { i ->
            val p = pointAt(i) ?: return@forEach
            drawCircle(color = lineColor, radius = 3.2f, center = p)
        }
    }
}
