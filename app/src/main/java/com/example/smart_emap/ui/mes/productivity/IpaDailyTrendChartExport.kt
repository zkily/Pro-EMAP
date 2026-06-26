package com.example.smart_emap.ui.mes.productivity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.smart_emap.data.model.InspectionProductivityDailyRowDto
import java.io.File
import java.io.FileOutputStream

/**
 * 日別推移チャートを画面表示（IpaDailyChartCard）と同系の見た目で Bitmap 化し、
 * Web の ECharts getDataURL と同様に印刷 HTML へ埋め込める data URL を返す。
 */
object IpaDailyTrendChartExport {
    private const val LOGICAL_WIDTH = 960f
    private const val LOGICAL_HEIGHT = 380f
    private const val PIXEL_RATIO = 2

    private val colorBarTop = Color.parseColor("#7DD3FC")
    private val colorBarMid = Color.parseColor("#6366F1")
    private val colorBarBottom = Color.parseColor("#4338CA")
    private val colorEffLine = Color.parseColor("#10B981")
    private val colorQtyLabel = Color.parseColor("#EF4444")
    private val colorMuted = Color.parseColor("#94A3B8")
    private val colorLegendText = Color.parseColor("#64748B")
    private val colorGrid = Color.parseColor("#F1F5F9")
    private val colorAxis = Color.parseColor("#E2E8F0")
    private val colorBoxBorder = Color.parseColor("#D9E2E8F0")
    private val colorEffPillText = Color.parseColor("#047857")

    /** 印刷用に PNG をキャッシュへ保存し、HTML から参照するファイル名を返す。 */
    fun savePngFile(
        cacheDir: File,
        fileName: String,
        daily: List<InspectionProductivityDailyRowDto>,
        fontSizeOffset: Int = 0,
    ): String? {
        if (daily.isEmpty()) return null
        cacheDir.mkdirs()
        val bitmap = render(daily, fontSizeOffset)
        val file = File(cacheDir, fileName)
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            fileName
        } catch (_: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    fun render(
        daily: List<InspectionProductivityDailyRowDto>,
        fontSizeOffset: Int = 0,
    ): Bitmap {
        val width = (LOGICAL_WIDTH * PIXEL_RATIO).toInt()
        val height = (LOGICAL_HEIGHT * PIXEL_RATIO).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.scale(PIXEL_RATIO.toFloat(), PIXEL_RATIO.toFloat())
        drawFullChart(canvas, daily, fontSizeOffset)
        return bitmap
    }

    private fun drawFullChart(
        canvas: Canvas,
        daily: List<InspectionProductivityDailyRowDto>,
        fontSizeOffset: Int,
    ) {
        val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1)
        val effMax = (daily.maxOfOrNull { (it.efficiencyPerHour ?: 0.0).toInt() } ?: 1).coerceAtLeast(1)

        drawLegend(canvas, fontSizeOffset)

        val boxLeft = 8f
        val boxTop = 30f
        val boxRight = LOGICAL_WIDTH - 8f
        val boxBottom = 328f
        val boxRadius = 12f
        drawChartBox(canvas, boxLeft, boxTop, boxRight, boxBottom, boxRadius)

        val axisWidth = 34f
        val plotLeft = boxLeft + axisWidth
        val plotRight = boxRight - axisWidth
        val plotTop = boxTop + 36f
        val plotBottom = boxBottom - 24f

        drawYAxisLabels(canvas, plotTop, plotBottom, qtyMax, effMax, plotLeft - 4f, plotRight + 4f, fontSizeOffset)
        drawPlot(canvas, daily, plotLeft, plotTop, plotRight, plotBottom, fontSizeOffset)
        drawXAxisLabels(canvas, daily, plotLeft, plotRight, boxBottom + 4f, fontSizeOffset)
    }

    private fun drawLegend(canvas: Canvas, fontSizeOffset: Int) {
        val textSize = 9f + fontSizeOffset
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            color = colorLegendText
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val square = 8f
        val gap = 3f
        val pairGap = 16f
        val label1 = "生産数"
        val label2 = "能率"
        val w1 = square + gap + paint.measureText(label1)
        val w2 = square + gap + paint.measureText(label2)
        var x = (LOGICAL_WIDTH - w1 - pairGap - w2) / 2f
        val y = 14f
        val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        squarePaint.color = colorBarTop
        canvas.drawRoundRect(RectF(x, y - square, x + square, y), 2f, 2f, squarePaint)
        x += square + gap
        canvas.drawText(label1, x, y, paint)
        x += paint.measureText(label1) + pairGap
        squarePaint.color = colorEffLine
        canvas.drawRoundRect(RectF(x, y - square, x + square, y), 2f, 2f, squarePaint)
        x += square + gap
        canvas.drawText(label2, x, y, paint)
    }

    private fun drawChartBox(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float,
    ) {
        val rect = RectF(left, top, right, bottom)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                left,
                top,
                left,
                bottom,
                Color.parseColor("#CCF8FAFC"),
                Color.parseColor("#66FFFFFF"),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = colorBoxBorder
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint)
    }

    private fun drawYAxisLabels(
        canvas: Canvas,
        top: Float,
        bottom: Float,
        qtyMax: Int,
        effMax: Int,
        leftX: Float,
        rightX: Float,
        fontSizeOffset: Int,
    ) {
        val textSize = 8f + fontSizeOffset
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            color = colorMuted
        }
        val leftPaint = Paint(paint).apply { textAlign = Paint.Align.RIGHT }
        val rightPaint = Paint(paint).apply { textAlign = Paint.Align.LEFT }
        for (tick in 4 downTo 0) {
            val y = top + (bottom - top) * (4 - tick) / 4f + textSize / 3f
            canvas.drawText("${qtyMax * tick / 4}", leftX, y, leftPaint)
            canvas.drawText("${effMax * tick / 4}", rightX, y, rightPaint)
        }
    }

    private fun drawXAxisLabels(
        canvas: Canvas,
        daily: List<InspectionProductivityDailyRowDto>,
        left: Float,
        right: Float,
        y: Float,
        fontSizeOffset: Int,
    ) {
        if (daily.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f + fontSizeOffset
            color = colorMuted
            textAlign = Paint.Align.CENTER
        }
        val count = daily.size.coerceAtLeast(1)
        val step = (right - left) / count
        daily.forEachIndexed { index, row ->
            val x = left + step * index + step / 2f
            val label = InspectionProductivityLogic.chartDayLabel(row.day)
            canvas.drawText(label, x, y + paint.textSize, paint)
        }
    }

    private fun drawPlot(
        canvas: Canvas,
        daily: List<InspectionProductivityDailyRowDto>,
        plotLeft: Float,
        plotTop: Float,
        plotRight: Float,
        plotBottom: Float,
        fontSizeOffset: Int,
    ) {
        val chartLeft = plotLeft + 4f
        val chartRight = plotRight - 4f
        val chartTop = plotTop + 22f
        val chartBottom = plotBottom - 28f
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val count = daily.size.coerceAtLeast(1)
        val groupWidth = chartWidth / count

        val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1).toFloat() * 1.18f
        val effMax = (daily.maxOfOrNull { it.efficiencyPerHour?.toFloat() ?: 0f } ?: 1f).coerceAtLeast(1f) * 1.22f

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorGrid
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
        for (tick in 0..4) {
            val y = chartBottom - chartHeight * tick / 4f
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorAxis
            strokeWidth = 1f
        }
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        val barWidth = (groupWidth * 0.42f).coerceIn(6f, 22f)
        val barTopRadius = 5f
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val qty = (row.sumActualQty ?: 0).toFloat()
            val barH = qty / qtyMax * chartHeight
            if (barH > 0f) {
                val barTop = chartBottom - barH
                val barRect = RectF(centerX - barWidth / 2f, barTop, centerX + barWidth / 2f, chartBottom)
                val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        barRect.left,
                        barRect.top,
                        barRect.left,
                        barRect.bottom,
                        intArrayOf(colorBarTop, colorBarMid, colorBarBottom),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                }
                canvas.drawRoundRect(barRect, barTopRadius, barTopRadius, barPaint)
            }
        }

        val effPoints = daily.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val eff = row.efficiencyPerHour?.toFloat() ?: 0f
            val y = chartTop + chartHeight * (1f - eff / effMax)
            x to y
        }
        if (effPoints.size >= 2) {
            val linePath = Path()
            effPoints.forEachIndexed { i, (x, y) ->
                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
            }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorEffLine
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(linePath, linePaint)
            val areaPath = Path().apply {
                moveTo(effPoints.first().first, chartBottom)
                effPoints.forEach { (x, y) -> lineTo(x, y) }
                lineTo(effPoints.last().first, chartBottom)
                close()
            }
            val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(31, 16, 185, 129)
                style = Paint.Style.FILL
            }
            canvas.drawPath(areaPath, areaPaint)
        }
        val dotOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val dotInner = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorEffLine }
        effPoints.forEach { (x, y) ->
            canvas.drawCircle(x, y, 5f, dotOuter)
            canvas.drawCircle(x, y, 3.5f, dotInner)
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val qtyTextSize = 8f + fontSizeOffset
        val effTextSize = 7.5f + fontSizeOffset
        val pillPadH = 5f
        val pillPadV = 2.5f
        val pillRadius = 4f

        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val qty = row.sumActualQty ?: 0
            if (qty > 0) {
                val barH = qty / qtyMax * chartHeight
                val barTop = chartBottom - barH
                val qtyLabel = InspectionProductivityLogic.fmtInt(qty)
                labelPaint.textSize = qtyTextSize
                labelPaint.color = colorQtyLabel
                labelPaint.setShadowLayer(3f, 0f, 1f, Color.argb(204, 255, 255, 255))
                val textH = labelPaint.fontMetrics.let { it.descent - it.ascent }
                if (barH >= textH + 6f) {
                    val centerY = barTop + barH / 2f
                    val textY = centerY + (labelPaint.fontMetrics.descent - labelPaint.fontMetrics.ascent) / 2f
                    canvas.drawText(qtyLabel, centerX, textY, labelPaint)
                }
                labelPaint.clearShadowLayer()
            }

            val eff = row.efficiencyPerHour
            if (eff != null && eff > 0) {
                val effY = chartTop + chartHeight * (1f - eff.toFloat() / effMax)
                val effLabel = InspectionProductivityLogic.fmtEfficiency(eff)
                labelPaint.textSize = effTextSize
                labelPaint.color = colorEffPillText
                val textW = labelPaint.measureText(effLabel)
                val textH = labelPaint.fontMetrics.let { it.descent - it.ascent }
                val pillW = textW + pillPadH * 2
                val pillH = textH + pillPadV * 2
                val pillTop = (effY - pillH - 8f).coerceAtLeast(chartTop)
                val pillLeft = centerX - pillW / 2f
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F2FFFFFF")
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4710B981")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val pillRect = RectF(pillLeft, pillTop, pillLeft + pillW, pillTop + pillH)
                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, bgPaint)
                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, borderPaint)
                canvas.drawText(effLabel, centerX, pillTop + pillPadV - labelPaint.fontMetrics.ascent, labelPaint)
            }
        }
    }

}
