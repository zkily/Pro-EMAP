package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Web `el-table-column` 幅をベースにしたレスポンシブ列定義 */
internal data class MoColumnSpec(
    val weight: Float,
    val minWidth: Dp,
)

internal object MoCols {
    val date = MoColumnSpec(1.2f, 72.dp)
    val dateShort = MoColumnSpec(0.9f, 52.dp)
    val supplier = MoColumnSpec(1.5f, 80.dp)
    val cd = MoColumnSpec(1.2f, 56.dp)
    val name = MoColumnSpec(1.8f, 96.dp)
    val safety = MoColumnSpec(1.0f, 40.dp)
    val stock = MoColumnSpec(1.0f, 44.dp)
    val stepper = MoColumnSpec(1.4f, 64.dp)
    val bundle = MoColumnSpec(1.2f, 44.dp)
    val weightCol = MoColumnSpec(1.2f, 44.dp)
    val action = MoColumnSpec(1.2f, 52.dp)
    val spec = MoColumnSpec(1.5f, 64.dp)
    val subQty = MoColumnSpec(1.1f, 44.dp)
    val orderAmount = MoColumnSpec(1.2f, 56.dp)
    val remarks = MoColumnSpec(2.0f, 80.dp)
    val subStatus = MoColumnSpec(1.0f, 52.dp)
    val subLabel = MoColumnSpec(1.0f, 56.dp)
    val subAction = MoColumnSpec(1.0f, 56.dp)

    val rowHeight = 34.dp
    val subRowHeight = 38.dp

    val dailyStock = listOf(dateShort, supplier, cd, name, safety, stock, stepper, stepper)
    val dailyStockTransfer = dailyStock + listOf(bundle, weightCol, action)
    val purchaseOrder = listOf(date, cd, name, supplier, spec, stock, stepper, subQty, weightCol, orderAmount, remarks)
    val initialStock = listOf(dateShort, supplier, cd, name, stepper, stepper)
    val usage = listOf(date, supplier, cd, name, stock, stepper)
    val subStock = listOf(cd, name, supplier, spec, subQty, subQty, stepper, subStatus, subLabel, remarks, subAction)

    fun minTableWidth(columns: List<MoColumnSpec>): Dp =
        columns.fold(0.dp) { acc, col -> acc + col.minWidth } +
            3.dp * (columns.size - 1).coerceAtLeast(0) + 12.dp
}

@Composable
internal fun MoTableShell(
    columns: List<MoColumnSpec>,
    content: @Composable ColumnScope.() -> Unit,
) {
    val minWidth = MoCols.minTableWidth(columns)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val needsScroll = minWidth > maxWidth
        val scroll = rememberScrollState()
        if (needsScroll) {
            Column(Modifier.horizontalScroll(scroll)) {
                Column(Modifier.widthIn(min = minWidth)) {
                    content()
                }
            }
        } else {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

internal fun Modifier.moTableHeaderRow(): Modifier = this
    .fillMaxWidth()
    .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))))
    .padding(horizontal = 6.dp, vertical = 7.dp)

internal fun Modifier.moDataRow(
    striped: Boolean,
    height: Dp = MoCols.rowHeight,
): Modifier = this
    .fillMaxWidth()
    .height(height)
    .background(if (striped) Color(0xFFFAFBFC) else Color.White)
    .drawBehindRowDivider(Color(0xFFE2E8F0))
    .padding(horizontal = 6.dp)

internal fun Modifier.moSummaryRow(): Modifier = this
    .fillMaxWidth()
    .height(MoCols.rowHeight)
    .background(Color(0xFFF1F5F9))
    .drawBehindRowDivider(Color(0xFFCBD5E1))
    .padding(horizontal = 6.dp)

@Composable
internal fun RowScope.MoHeaderCell(
    text: String,
    spec: MoColumnSpec,
    tint: Color = Color.Transparent,
    align: TextAlign = TextAlign.Center,
) {
    Box(
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth)
            .clip(RoundedCornerShape(4.dp))
            .background(tint.copy(alpha = if (tint == Color.Transparent) 0f else 0.45f))
            .padding(vertical = 2.dp, horizontal = 2.dp),
        contentAlignment = when (align) {
            TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
            TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
            else -> Alignment.Center
        },
    ) {
        Text(
            text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = align,
            lineHeight = 10.sp,
        )
    }
}

@Composable
internal fun RowScope.MoBodyCell(
    text: String,
    spec: MoColumnSpec,
    color: Color = Color(0xFF334155),
    bold: Boolean = false,
    align: TextAlign = TextAlign.Center,
) {
    Text(
        text,
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth)
            .padding(horizontal = 2.dp),
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
        lineHeight = 12.sp,
    )
}

@Composable
internal fun RowScope.MoStepperCell(
    value: Int,
    spec: MoColumnSpec,
    bg: Color,
    minValue: Int? = 0,
    blankWhenZero: Boolean = false,
    onChange: (Int) -> Unit,
) {
    val btnShape = RoundedCornerShape(4.dp)
    Row(
        modifier = Modifier
            .weight(spec.weight)
            .widthIn(min = spec.minWidth)
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(btnShape)
                .background(Color.White.copy(alpha = 0.85f))
                .clickable {
                    val next = value - 1
                    onChange(if (minValue != null) next.coerceAtLeast(minValue) else next)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("−", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        }
        Text(
            if (blankWhenZero && value == 0) "" else value.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(btnShape)
                .background(Color.White.copy(alpha = 0.85f))
                .clickable { onChange(value + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        }
    }
}

internal fun Modifier.drawBehindRowDivider(color: Color): Modifier = this.then(
    Modifier.drawBehind {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 1f,
        )
    },
)
