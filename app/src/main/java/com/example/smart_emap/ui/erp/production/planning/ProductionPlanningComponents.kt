package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.purchase.PurchasePageGradient

@Composable
fun ProductionPageBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.background(PurchasePageGradient)) {
        content()
    }
}

@Composable
fun ProductionHeroBar(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    actionLoading: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    extraActions: @Composable (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(ProductionPlanningColors.CardBg)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(ProductionPlanningColors.AccentBlue, ProductionPlanningColors.AccentGreen))),
                contentAlignment = Alignment.Center,
            ) {
                Text("📊", fontSize = 16.sp)
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ProductionPlanningColors.TextPrimary)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
                }
            }
            if (!badge.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, color = ProductionPlanningColors.AccentBlue)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            extraActions?.invoke()
            if (onPrint != null) {
                ProductionActionButton("印刷", Icons.Default.Print, Color(0xFFF59E0B), onPrint, actionLoading)
            }
            if (onRefresh != null) {
                ProductionActionButton("更新", Icons.Default.Refresh, ProductionPlanningColors.AccentBlue, onRefresh, actionLoading)
            }
            if (actionLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun ProductionActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    loading: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier.height(32.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}

data class ProductionKpiCard(
    val label: String,
    val value: String,
    val description: String? = null,
    val accent: Brush,
    val isNegative: Boolean = false,
)

@Composable
fun ProductionKpiStrip(cards: List<ProductionKpiCard>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cards.forEach { card ->
            val shape = RoundedCornerShape(8.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .shadow(1.dp, shape)
                    .clip(shape)
                    .background(ProductionPlanningColors.CardBg)
                    .border(1.dp, ProductionPlanningColors.CardBorder, shape)
                    .padding(10.dp),
            ) {
                Text(card.label, fontSize = 10.sp, color = ProductionPlanningColors.TextSecondary)
                Text(
                    card.value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = when {
                        card.isNegative -> ProductionPlanningColors.Negative
                        card.value != "-" -> ProductionPlanningColors.Positive
                        else -> ProductionPlanningColors.TextPrimary
                    },
                )
                if (!card.description.isNullOrBlank()) {
                    Text(card.description, fontSize = 9.sp, color = ProductionPlanningColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
fun ProductionFilterSurface(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape)
            .clip(shape)
            .background(ProductionPlanningColors.CardBg)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
fun ProductionFilterLabel(text: String) {
    Text(text, fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary, fontWeight = FontWeight.Medium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionDropdownFilter(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "全て" }
    Column(modifier = modifier) {
        ProductionFilterLabel(label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, ProductionPlanningColors.CardBorder),
            ) {
                Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                    Text(display, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (key, text) ->
                    DropdownMenuItem(
                        text = { Text(text, fontSize = 12.sp) },
                        onClick = {
                            onSelect(key)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ProductionTextFilter(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ProductionFilterLabel(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 12.sp, color = ProductionPlanningColors.TextPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, ProductionPlanningColors.CardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun ProductionTabStrip(
    tabs: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tabs.forEach { (key, label) ->
            val active = key == selected
            Surface(
                modifier = Modifier.clickable { onSelect(key) },
                shape = RoundedCornerShape(8.dp),
                color = if (active) ProductionPlanningColors.AccentBlue else Color(0xFFF1F5F9),
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.White else ProductionPlanningColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
fun ProductionTableCard(
    title: String? = null,
    subtitle: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    expandContent: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (expandContent) Modifier.fillMaxSize() else Modifier)
            .shadow(1.dp, shape)
            .clip(shape)
            .background(ProductionPlanningColors.CardBg)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape),
    ) {
        if (!title.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (!subtitle.isNullOrBlank()) {
                        Text(subtitle, fontSize = 10.sp, color = ProductionPlanningColors.TextSecondary)
                    }
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            HorizontalDivider(color = ProductionPlanningColors.CardBorder)
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .then(
                    if (expandContent) Modifier.weight(1f).fillMaxSize() else Modifier,
                ),
        ) {
            content()
        }
    }
}

@Composable
fun ProductionDataTable(
    headers: List<String>,
    rows: List<List<String>>,
    columnWidths: List<Int> = emptyList(),
    columnKeys: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    lazyBody: Boolean = rows.size > 30,
) {
    val horizontalScroll = rememberScrollState()

    fun columnWidth(index: Int) = (columnWidths.getOrNull(index) ?: 80).dp

    fun columnKey(index: Int) = columnKeys.getOrNull(index)

    fun headerAlign() = TextAlign.Center

    fun cellAlign(index: Int): TextAlign = when (columnKey(index)) {
        "product_name" -> TextAlign.Start
        "product_cd", "date" -> TextAlign.Center
        else -> TextAlign.End
    }

    @Composable
    fun HeaderCell(index: Int, text: String) {
        Box(
            modifier = Modifier
                .width(columnWidth(index))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = headerAlign(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    fun BodyCell(index: Int, text: String) {
        val align = cellAlign(index)
        Box(
            modifier = Modifier
                .width(columnWidth(index))
                .padding(horizontal = 4.dp),
            contentAlignment = when (align) {
                TextAlign.Start -> Alignment.CenterStart
                TextAlign.End -> Alignment.CenterEnd
                else -> Alignment.Center
            },
        ) {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                textAlign = align,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    @Composable
    fun HeaderRow() {
        Row(
            modifier = Modifier
                .background(ProductionPlanningColors.TableHeaderBg)
                .padding(vertical = 6.dp),
        ) {
            headers.forEachIndexed { i, h -> HeaderCell(i, h) }
        }
    }

    @Composable
    fun DataRow(rowIndex: Int, row: List<String>) {
        Row(
            modifier = Modifier
                .background(if (rowIndex % 2 == 0) Color.White else ProductionPlanningColors.TableStripe)
                .padding(vertical = 4.dp),
        ) {
            row.forEachIndexed { i, cell -> BodyCell(i, cell) }
        }
    }

    if (!lazyBody) {
        Column(modifier = modifier.horizontalScroll(horizontalScroll)) {
            HeaderRow()
            rows.forEachIndexed { rowIndex, row -> DataRow(rowIndex, row) }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .background(ProductionPlanningColors.TableHeaderBg)
                .padding(vertical = 6.dp),
        ) {
            headers.forEachIndexed { i, h -> HeaderCell(i, h) }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(rows, key = { index, _ -> index }) { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll)
                        .background(if (rowIndex % 2 == 0) Color.White else ProductionPlanningColors.TableStripe)
                        .padding(vertical = 4.dp),
                ) {
                    row.forEachIndexed { i, cell -> BodyCell(i, cell) }
                }
            }
        }
    }
}

