package com.example.smart_emap.ui.mes.productivity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.smart_emap.data.model.InspectionProductivityInspectorRowDto
import com.example.smart_emap.data.model.InspectionProductivityProductRowDto
import java.io.File
import java.io.FileOutputStream

/** 検査員別 / 製品別の横棒グラフを印刷用 Bitmap 化する。 */
object IpaBarChartExport {
    private const val LOGICAL_WIDTH = 960f
    private const val ROW_HEIGHT = 30f
    private const val PAD_V = 16f
    private const val PAD_H = 10f
    private const val LABEL_W = 76f
    private const val VALUE_W = 48f
    private const val PIXEL_RATIO = 2

    private val shellBgTop = Color.parseColor("#BFF8FAFC")
    private val shellBgBottom = Color.parseColor("#80FFFFFF")
    private val shellBorder = Color.parseColor("#CCE2E8F0")
    private val trackBg = Color.parseColor("#F1F5F9")
    private val labelColor = Color.parseColor("#334155")
    private val valueColor = Color.parseColor("#475569")

    private val inspectorColors = intArrayOf(
        0xFF8B5CF6.toInt(), 0xFF6366F1.toInt(), 0xFF0EA5E9.toInt(), 0xFF10B981.toInt(),
        0xFF14B8A6.toInt(), 0xFFF59E0B.toInt(), 0xFFF97316.toInt(), 0xFFEC4899.toInt(),
    )
    private val productColors = intArrayOf(
        0xFF38BDF8.toInt(), 0xFF0EA5E9.toInt(), 0xFF6366F1.toInt(), 0xFF8B5CF6.toInt(),
        0xFF10B981.toInt(), 0xFF14B8A6.toInt(), 0xFFF59E0B.toInt(), 0xFFEC4899.toInt(),
    )

    fun saveInspectorEfficiencyChart(
        cacheDir: File,
        fileName: String,
        rows: List<InspectionProductivityInspectorRowDto>,
    ): String? {
        if (rows.isEmpty()) return null
        val chartRows = rows
            .sortedByDescending { inspectorEfficiency(it) ?: -1.0 }
            .map { row ->
                BarRow(
                    label = row.inspectorName.orEmpty().ifBlank { "—" },
                    value = inspectorEfficiency(row) ?: 0.0,
                )
            }
        return saveChart(cacheDir, fileName, chartRows, inspectorColors)
    }

    fun saveProductQtyChart(
        cacheDir: File,
        fileName: String,
        rows: List<InspectionProductivityProductRowDto>,
    ): String? {
        if (rows.isEmpty()) return null
        val chartRows = rows
            .sortedByDescending { it.sumActualQty ?: 0 }
            .map { row ->
                BarRow(
                    label = row.productName?.trim().orEmpty().ifBlank { row.productCd.orEmpty() },
                    value = (row.sumActualQty ?: 0).toDouble(),
                    valueLabel = InspectionProductivityLogic.fmtInt(row.sumActualQty),
                )
            }
        return saveChart(cacheDir, fileName, chartRows, productColors)
    }

    fun saveProductRankChart(
        cacheDir: File,
        fileName: String,
        rows: List<InspectionProductivityInspectorRowDto>,
    ): String? = saveInspectorEfficiencyChart(cacheDir, fileName, rows)

    private data class BarRow(
        val label: String,
        val value: Double,
        val valueLabel: String? = null,
    )

    private fun inspectorEfficiency(row: InspectionProductivityInspectorRowDto): Double? =
        InspectionProductivityLogic.periodAvgEfficiencyFromBucket(row.sumActualQty, row.sumNetProductionSec)
            ?: row.efficiencyPerHour

    private fun saveChart(
        cacheDir: File,
        fileName: String,
        rows: List<BarRow>,
        palette: IntArray,
    ): String? {
        cacheDir.mkdirs()
        val logicalHeight = PAD_V * 2 + rows.size * ROW_HEIGHT
        val width = (LOGICAL_WIDTH * PIXEL_RATIO).toInt()
        val height = (logicalHeight * PIXEL_RATIO).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(PIXEL_RATIO.toFloat(), PIXEL_RATIO.toFloat())
        drawShell(canvas, logicalHeight)
        val maxVal = rows.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 1.0
        val barLeft = PAD_H + LABEL_W + 4f
        val barRight = LOGICAL_WIDTH - PAD_H - VALUE_W - 4f
        val barMaxW = (barRight - barLeft).coerceAtLeast(1f)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = labelColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = valueColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        rows.forEachIndexed { index, row ->
            val top = PAD_V + index * ROW_HEIGHT
            val centerY = top + ROW_HEIGHT / 2f
            val displayLabel = ellipsize(labelPaint, row.label, LABEL_W - 4f)
            canvas.drawText(displayLabel, PAD_H, centerY - (labelPaint.descent() + labelPaint.ascent()) / 2f, labelPaint)
            val trackTop = top + 7f
            val trackBottom = top + ROW_HEIGHT - 7f
            val trackRect = RectF(barLeft, trackTop, barRight, trackBottom)
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = trackBg }
            canvas.drawRoundRect(trackRect, 8f, 8f, trackPaint)
            val fraction = (row.value / maxVal).toFloat().coerceIn(0f, 1f)
            if (fraction > 0f) {
                val barRect = RectF(barLeft, trackTop, barLeft + barMaxW * fraction, trackBottom)
                val color = palette[index % palette.size]
                val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        barRect.left, barRect.top, barRect.right, barRect.top,
                        Color.argb(217, Color.red(color), Color.green(color), Color.blue(color)),
                        color,
                        Shader.TileMode.CLAMP,
                    )
                }
                canvas.drawRoundRect(barRect, 8f, 8f, barPaint)
            }
            val valueText = row.valueLabel ?: InspectionProductivityLogic.fmtEfficiency(row.value)
            canvas.drawText(valueText, LOGICAL_WIDTH - PAD_H, centerY - (valuePaint.descent() + valuePaint.ascent()) / 2f, valuePaint)
        }
        return try {
            FileOutputStream(File(cacheDir, fileName)).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            fileName
        } catch (_: Exception) {
            null
        } finally {
            bitmap.recycle()
        }
    }

    private fun drawShell(canvas: Canvas, height: Float) {
        val rect = RectF(0f, 0f, LOGICAL_WIDTH, height)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, height, shellBgTop, shellBgBottom, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = shellBorder
        }
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)
    }

    private fun ellipsize(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText("${text.take(end)}…") > maxWidth) end--
        return if (end <= 0) "…" else "${text.take(end)}…"
    }
}
