package com.example.smart_emap.ui.erp.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderHomeScreen(
    viewModel: OrderHomeViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEEF2FF), Color(0xFFF5F3FF), Color(0xFFECFEFF)),
                ),
            ),
    ) {
        OrderHomeAnimatedBackground()
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val contentWidth = minOf(maxWidth, 1440.dp)
            val wideAnalytics = maxWidth >= 1100.dp
            val chartHeight = when {
                maxWidth >= 1100.dp -> 240.dp
                maxWidth >= 640.dp -> 220.dp
                else -> 200.dp
            }
            val rankHeight = (uiState.productRank.size.coerceIn(1, 12) * 34).coerceIn(200, 400).dp

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OrderHomeStaggeredReveal(index = 0) {
                    OrderHomeHeroHeader()
                }

                OrderHomeStaggeredReveal(index = 1) {
                    OrderHomeSectionLabel(
                        title = "当月サマリ",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                    )
                    if (uiState.isLoadingSummary) {
                        OrderHomeLoadingBlock(height = 96.dp)
                    } else {
                        OrderMonthlySummaryCards(
                            summary = uiState.summary,
                            premiumStyle = true,
                            useResponsiveGrid = true,
                        )
                    }
                }

                uiState.errorMessage?.let { msg ->
                    OrderHomeErrorBanner(message = msg)
                }

                OrderHomeStaggeredReveal(index = 2) {
                    OrderHomeSectionLabel(
                        title = "分析・推移",
                        icon = Icons.Default.Analytics,
                    )
                }

                if (wideAnalytics) {
                    OrderHomeStaggeredReveal(index = 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OrderHomeMonthlyAnalyticsCard(
                                    loading = uiState.isLoadingAnalytics,
                                    chartHeight = chartHeight,
                                    monthlyPoints = uiState.monthlyPoints,
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OrderHomeDailyAnalyticsCard(
                                    loading = uiState.isLoadingAnalytics,
                                    chartHeight = chartHeight,
                                    dailyRows = uiState.dailyRows,
                                    asOfDate = uiState.asOfDate,
                                )
                            }
                        }
                    }
                    OrderHomeStaggeredReveal(index = 4) {
                        OrderHomeProductRankAnalyticsCard(
                            loading = uiState.isLoadingAnalytics,
                            rankHeight = rankHeight,
                            productRank = uiState.productRank,
                        )
                    }
                } else {
                    OrderHomeStaggeredReveal(index = 3) {
                        OrderHomeMonthlyAnalyticsCard(
                            loading = uiState.isLoadingAnalytics,
                            chartHeight = chartHeight,
                            monthlyPoints = uiState.monthlyPoints,
                        )
                    }
                    OrderHomeStaggeredReveal(index = 4) {
                        OrderHomeDailyAnalyticsCard(
                            loading = uiState.isLoadingAnalytics,
                            chartHeight = chartHeight,
                            dailyRows = uiState.dailyRows,
                            asOfDate = uiState.asOfDate,
                        )
                    }
                    OrderHomeStaggeredReveal(index = 5) {
                        OrderHomeProductRankAnalyticsCard(
                            loading = uiState.isLoadingAnalytics,
                            rankHeight = rankHeight,
                            productRank = uiState.productRank,
                        )
                    }
                }

                OrderHomeStaggeredReveal(index = 6) {
                    OrderHomeSectionLabel(
                        title = "機能メニュー",
                        icon = Icons.Default.GridView,
                    )
                    OrderHomeQuickNavGrid(onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
private fun OrderHomeMonthlyAnalyticsCard(
    loading: Boolean,
    chartHeight: androidx.compose.ui.unit.Dp,
    monthlyPoints: List<OrderHomeMonthlyPointUi>,
) {
    OrderHomeAnalyticsCard(
        title = "月別推移（過去6ヶ月＋未来2ヶ月）",
        subtitle = "直近6ヶ月〜当月を実績、先2ヶ月は登録ベース（APIサマリ）",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        accent = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
        legend = {
            OrderHomeChartLegendDots(
                items = listOf(
                    Color(0xFF6366F1) to "実績帯",
                    Color(0xFF7C3AED) to "先見込",
                ),
            )
        },
        loading = loading,
    ) {
        OrderHomeMonthlyChart(
            points = monthlyPoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 4.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OrderHomeChartLegend(
            items = listOf(
                Color(0xFF6366F1) to "内示本数",
                Color(0xFF14B8A6) to "確定合計本数",
            ),
        )
    }
}

@Composable
private fun OrderHomeDailyAnalyticsCard(
    loading: Boolean,
    chartHeight: androidx.compose.ui.unit.Dp,
    dailyRows: List<OrderHomeDailyRowUi>,
    asOfDate: String,
) {
    OrderHomeAnalyticsCard(
        title = "日別集計（当月）",
        subtitle = "高さ＝確定本数。色は過去／当日／当月内の未来を区別",
        icon = Icons.Default.CalendarMonth,
        accent = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF10B981))),
        legend = {
            OrderHomeChartLegendDots(
                items = listOf(
                    Color(0xFF4338CA) to "過去",
                    Color(0xFF047857) to "今日",
                    Color(0xFF94A3B8) to "未来",
                ),
            )
        },
        loading = loading,
    ) {
        OrderHomeDailyChart(
            rows = dailyRows,
            asOfDate = asOfDate,
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun OrderHomeProductRankAnalyticsCard(
    loading: Boolean,
    rankHeight: androidx.compose.ui.unit.Dp,
    productRank: List<OrderHomeProductRankUi>,
) {
    OrderHomeAnalyticsCard(
        title = "製品別ランキング（当月・確定本数）",
        subtitle = "日別受注データを集計。上位12品目",
        icon = Icons.Default.Analytics,
        accent = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFFF59E0B))),
        legend = {
            OrderHomeChartLegendDots(
                items = listOf(
                    Color(0xFFD97706) to "1〜3位",
                    Color(0xFF4338CA) to "その他",
                ),
            )
        },
        loading = loading,
    ) {
        OrderHomeProductRankChart(
            rows = productRank,
            modifier = Modifier
                .fillMaxWidth()
                .height(rankHeight)
                .padding(horizontal = 4.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderHomeQuickNavGrid(
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cols = when {
            maxWidth >= 960.dp -> 3
            maxWidth >= 640.dp -> 2
            else -> 1
        }
        val gap = 10.dp
        val cardWidth = if (cols == 1) maxWidth else (maxWidth - gap * (cols - 1)) / cols
        val navItems = listOf(
        Triple("月受注", "月別・内示", Icons.Default.CalendarMonth) to
            (listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)) to "/erp/order/monthly"),
        Triple("日受注", "日別・確定", Icons.Default.AccessTime) to
            (listOf(Color(0xFF06B6D4), Color(0xFF0284C7)) to "/erp/order/daily"),
        Triple("納入先履歴", "分析・照会", Icons.Default.Analytics) to
            (listOf(Color(0xFFF59E0B), Color(0xFFD97706)) to "/erp/order/destination-history"),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            navItems.forEach { (meta, route) ->
                val (title, hint, icon) = meta
                val (gradient, path) = route
                OrderHomeQuickNavCard(
                    title = title,
                    hint = hint,
                    icon = icon,
                    gradient = gradient,
                    onClick = { onNavigate(path) },
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}

@Composable
private fun OrderHomeErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33DC2626))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2)),
                ),
            )
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = message,
            color = Color(0xFFB91C1C),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun OrderHomeStaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 5 } +
            scaleIn(initialScale = 0.97f, animationSpec = tween(420, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}

@Composable
private fun OrderHomeAnimatedBackground() {
    val transition = rememberInfiniteTransition(label = "order-home-bg")
    val orb1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "orb1",
    )
    val orb2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(11000), RepeatMode.Reverse),
        label = "orb2",
    )
    val orb3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7500), RepeatMode.Reverse),
        label = "orb3",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = (orb1 * 24 - 12).dp, y = (-80).dp + (orb2 * 16).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x336366F1), Color(0x188B5CF6), Color.Transparent),
                        radius = 340f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-48).dp, y = (orb2 * 30).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2E06B6D4), Color.Transparent),
                        radius = 280f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (orb3 * 20).dp, y = (orb1 * -12).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x26F59E0B), Color.Transparent),
                        radius = 240f,
                    ),
                ),
        )
    }
}

@Composable
private fun OrderHomeHeroHeader() {
    val transition = rememberInfiniteTransition(label = "order-hero")
    val floatY by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label = "hero-float",
    )
    val shineX by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(3600), RepeatMode.Restart),
        label = "hero-shine",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x334F46E5), spotColor = Color(0x554F46E5))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.82f), Color.White.copy(alpha = 0.62f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x1A6366F1), Color.Transparent),
                        radius = 480f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(80.dp)
                .offset(x = (shineX * 280).dp)
                .graphicsLayer { rotationZ = -10f }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.offset(y = floatY.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Color(0x664F46E5))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    startY = 0f,
                                    endY = size.height * 0.45f,
                                ),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "受注管理",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    letterSpacing = 0.3.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "月次・日次の受注と納入先履歴を一元管理",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun OrderHomeSectionLabel(
    title: String,
    icon: ImageVector,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .shadow(3.dp, RoundedCornerShape(7.dp), spotColor = Color(0x336366F1))
                .clip(RoundedCornerShape(7.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun OrderHomeLoadingBlock(height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0x1A6366F1))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.55f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color(0xFF6366F1), strokeWidth = 2.5.dp, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun OrderHomeAnalyticsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Brush,
    legend: @Composable () -> Unit,
    loading: Boolean,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x1A6366F1), spotColor = Color(0x286366F1))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.58f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.4f),
                        Color(0xFF6366F1).copy(alpha = 0.12f),
                    ),
                ),
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accent),
        )
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .shadow(4.dp, RoundedCornerShape(9.dp), spotColor = Color(0x336366F1))
                            .clip(RoundedCornerShape(9.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                    ),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                legend()
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (loading) {
                OrderHomeLoadingBlock(height = 128.dp)
            } else {
                content()
            }
        }
    }
}

@Composable
private fun OrderHomeQuickNavCard(
    title: String,
    hint: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(140),
        label = "quick-nav-press",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (pressed) 4.dp else 10.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = gradient.first().copy(alpha = 0.15f),
                spotColor = gradient.first().copy(alpha = 0.28f),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.82f), Color.White.copy(alpha = 0.65f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.95f), gradient.first().copy(alpha = 0.2f)),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.horizontalGradient(gradient)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .padding(top = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(13.dp), spotColor = gradient.last().copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(gradient))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(13.dp))
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.26f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.5f,
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(hint, fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = gradient.first().copy(alpha = 0.7f),
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = if (pressed) 2.dp else 0.dp),
            )
        }
    }
}
