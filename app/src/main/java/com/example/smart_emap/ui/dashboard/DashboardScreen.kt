package com.example.smart_emap.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.DailyConfirmedSeriesDto
import com.example.smart_emap.ui.theme.LoginColors
import kotlin.math.max

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF5F7FA), Color(0xFFE4E8ED)),
                ),
            ),
    ) {
        val isWide = maxWidth >= 1200.dp
        val isTablet = maxWidth >= 768.dp
        val statColumns = when {
            isWide -> 4
            isTablet -> 2
            else -> 1
        }
        val quickColumns = when {
            isWide -> 6
            isTablet -> 3
            else -> 2
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WelcomeBanner(
                title = viewModel.welcomeTitle(),
                subtitle = "Smart-EMAP システムへようこそ",
                dateTime = uiState.currentDateTime,
            )

            if (uiState.isLoading && uiState.statCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = LoginColors.Primary)
                }
            } else {
                StatsGrid(cards = uiState.statCards, columns = statColumns)
            }

            DailyOrderChartSection(series = uiState.dailySeries)

            QuickAccessSection(
                items = uiState.quickAccessItems,
                columns = quickColumns,
            )

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomeBanner(
    title: String,
    subtitle: String,
    dateTime: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF4F46E5),
                            Color(0xFF6366F1),
                            Color(0xFF7C3AED),
                            Color(0xFF5B21B6),
                        ),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                            radius = 500f,
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.08f)),
                                ),
                            )
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFEEF2FF), modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0x330F172A))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Brush.linearGradient(listOf(Color(0xFFA5F3FC), Color(0xFF34D399)))),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.5.sp,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFF8FAFC), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dateTime, color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(cards: List<StatCardUi>, columns: Int) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (columns >= 4) 100.dp else 200.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(cards, key = { it.key }) { card ->
            StatCard(card)
        }
    }
}

private fun statIconForKey(key: String): ImageVector = when (key) {
    "sales" -> Icons.Default.TrendingUp
    "orders" -> Icons.Default.Description
    "inventory" -> Icons.Default.Inventory2
    "products" -> Icons.Default.ShoppingBag
    else -> Icons.Default.BarChart
}

private fun quickIconForRoute(route: String): ImageVector = when {
    route.contains("monthly") -> Icons.Default.CalendarMonth
    route.contains("daily") -> Icons.Default.Description
    route.contains("shipping/list") -> Icons.Default.GridView
    route.contains("shipping/report") -> Icons.Default.LocalShipping
    route.contains("data-management") -> Icons.Default.TrendingUp
    route.contains("plan-schedules") -> Icons.Default.Settings
    route.contains("planning-list") -> Icons.Default.Description
    route.contains("welding-planning") -> Icons.Default.Description
    route.contains("cutting") -> Icons.Default.Settings
    route.contains("forming") -> Icons.Default.Inventory2
    route.contains("welding") -> Icons.Default.GridView
    else -> Icons.Default.GridView
}

@Composable
private fun StatCard(card: StatCardUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(card.gradientStart, card.gradientEnd))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(statIconForKey(card.key), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = card.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoginColors.TitleDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = card.label,
                    fontSize = 11.sp,
                    color = LoginColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DailyOrderChartSection(series: DailyConfirmedSeriesDto?) {
    SectionHeader(
        icon = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(LoginColors.Primary.copy(alpha = 0.12f), Color(0xFF8B5CF6).copy(alpha = 0.1f))))
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = LoginColors.Primary)
            }
        },
        title = "日別受注数量（確定本数）",
        subtitle = "過去2週間・今後1週間（JST）",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White, Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF10B981)),
                        ),
                    ),
            )
            val items = series?.items.orEmpty()
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(292.dp)
                        .padding(top = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("データを取得できませんでした", color = LoginColors.TextMuted, fontSize = 13.sp)
                }
            } else {
                DailyOrderBarChart(
                    series = series!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(292.dp)
                        .padding(horizontal = 12.dp, vertical = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun DailyOrderBarChart(
    series: DailyConfirmedSeriesDto,
    modifier: Modifier = Modifier,
) {
    val items = series.items
    val asOf = series.asOfDate
    val maxValue = max(items.maxOfOrNull { it.confirmedUnits } ?: 1, 1)

    Canvas(modifier = modifier) {
        val barCount = items.size
        if (barCount == 0) return@Canvas

        val leftPad = 36f
        val bottomPad = 48f
        val topPad = 24f
        val chartW = size.width - leftPad - 12f
        val chartH = size.height - bottomPad - topPad
        val slotW = chartW / barCount
        val barW = minOf(slotW * 0.55f, 26f)

        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - 12f, topPad + chartH),
            strokeWidth = 1f,
        )

        items.forEachIndexed { index, item ->
            val ratio = item.confirmedUnits.toFloat() / maxValue
            val barH = chartH * ratio
            val x = leftPad + index * slotW + (slotW - barW) / 2f
            val y = topPad + chartH - barH

            val color = when {
                item.date < asOf -> Color(0xFF6366F1)
                item.date == asOf -> Color(0xFF10B981)
                else -> Color(0xFF94A3B8)
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(6f, 6f),
            )

            if (item.date == asOf) {
                val lineX = x + barW / 2f
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(lineX, topPad),
                    end = Offset(lineX, topPad + chartH),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                )
            }
        }
    }
}

@Composable
private fun QuickAccessSection(
    items: List<QuickAccessUi>,
    columns: Int,
) {
    SectionHeader(
        icon = { Icon(Icons.Default.GridView, contentDescription = null, tint = LoginColors.Primary, modifier = Modifier.size(18.dp)) },
        title = "クイックアクセス",
        subtitle = null,
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .height(quickAccessGridHeight(itemCount = items.size, columns = columns)),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(items, key = { it.route }) { item ->
            QuickAccessCard(item = item, onClick = { /* 次フェーズで各画面へ遷移 */ })
        }
    }
}

private fun quickAccessGridHeight(itemCount: Int, columns: Int): androidx.compose.ui.unit.Dp {
    val rows = (itemCount + columns - 1) / columns
    return (rows * 88 + (rows - 1) * 12).dp
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessUi,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(item.gradientStart, item.gradientEnd))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(quickIconForRoute(item.route), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoginColors.TitleDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = LoginColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = LoginColors.TextLight,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LoginColors.TitleDark)
            subtitle?.let {
                Text(text = it, fontSize = 11.sp, color = LoginColors.TextMuted)
            }
        }
    }
}
