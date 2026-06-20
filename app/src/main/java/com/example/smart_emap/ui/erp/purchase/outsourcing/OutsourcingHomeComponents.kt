package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OutsourcingDashboardDto
import com.example.smart_emap.data.model.OutsourcingSupplierSummaryDto
import com.example.smart_emap.data.model.OutsourcingUpcomingDeliveryDto
import java.text.NumberFormat
import java.util.Locale

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

private data class DashboardStatSpec(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val iconGradient: Brush,
    val details: List<String>,
    val hasAlert: Boolean = false,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutsourcingDashboardStatsGrid(
    stats: OutsourcingHomeStatsUi,
    dashboard: OutsourcingDashboardDto?,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        DashboardStatSpec(
            value = stats.todayOrders.toString(),
            label = "本日の注文",
            icon = Icons.Default.Assignment,
            iconGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
            details = listOf(
                "メッキ: ${dashboard?.todayOrders?.get("plating_orders") ?: 0}",
                "溶接: ${dashboard?.todayOrders?.get("welding_orders") ?: 0}",
            ),
        ),
        DashboardStatSpec(
            value = stats.pendingOrders.toString(),
            label = "未完了注文",
            icon = Icons.Default.Schedule,
            iconGradient = Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C))),
            details = listOf(
                "メッキ: ${dashboard?.pendingOrders?.get("plating_pending") ?: 0}",
                "溶接: ${dashboard?.pendingOrders?.get("welding_pending") ?: 0}",
            ),
        ),
        DashboardStatSpec(
            value = stats.todayReceivings.toString(),
            label = "本日の受入",
            icon = Icons.Default.CheckCircle,
            iconGradient = Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
            details = listOf(
                "メッキ: ${dashboard?.todayReceivings?.get("plating_receivings") ?: 0}",
                "溶接: ${dashboard?.todayReceivings?.get("welding_receivings") ?: 0}",
            ),
        ),
        DashboardStatSpec(
            value = stats.stockAlerts.toString(),
            label = "在庫警告",
            icon = Icons.Default.Warning,
            iconGradient = Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))),
            details = listOf(
                "メッキ: ${dashboard?.stockAlerts?.get("plating_alerts") ?: 0}",
                "溶接: ${dashboard?.stockAlerts?.get("welding_alerts") ?: 0}",
                "材料: ${dashboard?.stockAlerts?.get("material_alerts") ?: 0}",
            ),
            hasAlert = stats.stockAlerts > 0,
        ),
        DashboardStatSpec(
            value = stats.overdueOrders.toString(),
            label = "納期遅延",
            icon = Icons.Default.WarningAmber,
            iconGradient = Brush.linearGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199))),
            details = listOf(
                "メッキ: ${dashboard?.overdueOrders?.get("plating_overdue") ?: 0}",
                "溶接: ${dashboard?.overdueOrders?.get("welding_overdue") ?: 0}",
            ),
            hasAlert = stats.overdueOrders > 0,
        ),
    )

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val gap = 6.dp
        val minCard = 132.dp
        val cols = ((maxWidth + gap) / (minCard + gap)).toInt().coerceIn(1, 5)
        val cardWidth = if (cols == 1) maxWidth else (maxWidth - gap * (cols - 1)) / cols

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
            maxItemsInEachRow = cols,
        ) {
            items.forEach { spec ->
                OutsourcingDashboardStatCard(
                    value = spec.value,
                    label = spec.label,
                    icon = spec.icon,
                    iconGradient = spec.iconGradient,
                    details = spec.details,
                    hasAlert = spec.hasAlert,
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}

@Composable
fun OutsourcingDashboardMainContent(
    isLoading: Boolean,
    upcomingDeliveries: List<OutsourcingUpcomingDeliveryDto>,
    supplierSummary: List<OutsourcingSupplierSummaryDto>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val gap = 6.dp
        if (maxWidth >= 560.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                OutsourcingDeliveriesTableCard(
                    isLoading = isLoading,
                    rows = upcomingDeliveries,
                    modifier = Modifier.weight(1f),
                )
                OutsourcingSuppliersTableCard(
                    isLoading = isLoading,
                    rows = supplierSummary,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                OutsourcingDeliveriesTableCard(isLoading = isLoading, rows = upcomingDeliveries)
                OutsourcingSuppliersTableCard(isLoading = isLoading, rows = supplierSummary)
            }
        }
    }
}

@Composable
private fun OutsourcingDeliveriesTableCard(
    isLoading: Boolean,
    rows: List<OutsourcingUpcomingDeliveryDto>,
    modifier: Modifier = Modifier,
) {
    OutsourcingDashboardTableCard(
        title = "直近の納期一覧（7日以内）",
        icon = Icons.Default.CalendarMonth,
        isLoading = isLoading,
        isEmpty = rows.isEmpty(),
        modifier = modifier,
        minTableWidth = 640.dp,
    ) {
        OutsourcingDashboardTableHeader(
            cells = listOf(
                DashboardCellSpec("種別", 52.dp, TextAlign.Center),
                DashboardCellSpec("注文番号", 96.dp, TextAlign.Start),
                DashboardCellSpec("品番", 88.dp, TextAlign.Start),
                DashboardCellSpec("外注先", 120.dp, TextAlign.Start),
                DashboardCellSpec("数量", 52.dp, TextAlign.End),
                DashboardCellSpec("残数", 52.dp, TextAlign.End),
                DashboardCellSpec("納期", 88.dp, TextAlign.Center),
                DashboardCellSpec("残日数", 56.dp, TextAlign.Center),
            ),
        )
        rows.forEachIndexed { index, row ->
            val remaining = (row.quantity ?: 0) - (row.receivedQty ?: 0)
            OutsourcingDashboardTableRow(striped = index % 2 == 1) {
                DashboardTableCell(width = 52.dp, align = TextAlign.Center) {
                    OutsourcingTag(
                        text = outsourcingDeliveryTypeLabel(row.type),
                        containerColor = outsourcingDeliveryTypeColor(row.type),
                    )
                }
                DashboardTableCell(width = 96.dp, align = TextAlign.Start) {
                    Text(row.orderNo.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DashboardTableCell(width = 88.dp, align = TextAlign.Start) {
                    Text(row.productCd.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DashboardTableCell(width = 120.dp, align = TextAlign.Start) {
                    Text(row.supplierName.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DashboardTableCell(width = 52.dp, align = TextAlign.End) {
                    Text("${row.quantity ?: 0}", fontSize = 10.sp)
                }
                DashboardTableCell(width = 52.dp, align = TextAlign.End) {
                    Text("$remaining", fontSize = 10.sp)
                }
                DashboardTableCell(width = 88.dp, align = TextAlign.Center) {
                    Text(row.deliveryDate.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DashboardTableCell(width = 56.dp, align = TextAlign.Center) {
                    OutsourcingTag(
                        text = "${row.daysRemaining ?: 0}日",
                        containerColor = outsourcingDaysRemainingColor(row.daysRemaining),
                    )
                }
            }
        }
    }
}

@Composable
private fun OutsourcingSuppliersTableCard(
    isLoading: Boolean,
    rows: List<OutsourcingSupplierSummaryDto>,
    modifier: Modifier = Modifier,
) {
    OutsourcingDashboardTableCard(
        title = "外注先別サマリー",
        icon = Icons.Default.Business,
        isLoading = isLoading,
        isEmpty = rows.isEmpty(),
        modifier = modifier,
        minTableWidth = 560.dp,
    ) {
        OutsourcingDashboardTableHeader(
            cells = listOf(
                DashboardCellSpec("コード", 72.dp, TextAlign.Start),
                DashboardCellSpec("外注先名", 120.dp, TextAlign.Start),
                DashboardCellSpec("種別", 56.dp, TextAlign.Center),
                DashboardCellSpec("メッキ注文", 68.dp, TextAlign.Center),
                DashboardCellSpec("溶接注文", 68.dp, TextAlign.Center),
                DashboardCellSpec("支給材料在庫", 88.dp, TextAlign.End),
            ),
        )
        rows.forEachIndexed { index, row ->
            OutsourcingDashboardTableRow(striped = index % 2 == 1) {
                DashboardTableCell(width = 72.dp, align = TextAlign.Start) {
                    Text(
                        row.supplierCd.orEmpty(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B6DE0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DashboardTableCell(width = 120.dp, align = TextAlign.Start) {
                    Text(row.supplierName.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DashboardTableCell(width = 56.dp, align = TextAlign.Center) {
                    OutsourcingTag(
                        text = outsourcingSupplierTypeLabel(row.supplierType),
                        containerColor = outsourcingSupplierTypeColor(row.supplierType),
                    )
                }
                DashboardTableCell(width = 68.dp, align = TextAlign.Center) {
                    val count = row.platingOrderCount ?: 0
                    Text(
                        "$count",
                        fontSize = 10.sp,
                        fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (count > 0) Color(0xFF409EFF) else Color(0xFF64748B),
                    )
                }
                DashboardTableCell(width = 68.dp, align = TextAlign.Center) {
                    val count = row.weldingOrderCount ?: 0
                    Text(
                        "$count",
                        fontSize = 10.sp,
                        fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (count > 0) Color(0xFF409EFF) else Color(0xFF64748B),
                    )
                }
                DashboardTableCell(width = 88.dp, align = TextAlign.End) {
                    Text(jpNumber.format(row.materialStockCount ?: 0), fontSize = 10.sp)
                }
            }
        }
    }
}

private data class DashboardCellSpec(
    val label: String,
    val width: Dp,
    val align: TextAlign,
)

@Composable
private fun OutsourcingDashboardTableCard(
    title: String,
    icon: ImageVector,
    isLoading: Boolean,
    isEmpty: Boolean,
    minTableWidth: Dp,
    modifier: Modifier = Modifier,
    tableContent: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1A1A2E))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .heightIn(max = 280.dp),
            ) {
                when {
                    isLoading && isEmpty -> OutsourcingLoadingBox(true)
                    isEmpty -> Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text("データがありません", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    else -> {
                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            val scrollH = rememberScrollState()
                            val scrollV = rememberScrollState()
                            val needsHScroll = minTableWidth > maxWidth
                            val tableModifier = if (needsHScroll) {
                                Modifier.horizontalScroll(scrollH).widthIn(min = minTableWidth)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                            Column(
                                modifier = tableModifier.verticalScroll(scrollV),
                            ) {
                                tableContent()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutsourcingDashboardTableHeader(cells: List<DashboardCellSpec>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEach { cell ->
            Box(
                modifier = Modifier.width(cell.width).padding(horizontal = 2.dp),
                contentAlignment = when (cell.align) {
                    TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
                    TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
                    else -> Alignment.Center
                },
            ) {
                Text(
                    cell.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = cell.align,
                    maxLines = 2,
                    lineHeight = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun OutsourcingDashboardTableRow(
    striped: Boolean,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(if (striped) Color(0xFFFAFBFC) else Color.White)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun DashboardTableCell(
    width: Dp,
    align: TextAlign,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 2.dp),
        contentAlignment = when (align) {
            TextAlign.Start, TextAlign.Left -> Alignment.CenterStart
            TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
            else -> Alignment.Center
        },
    ) {
        content()
    }
}
