package com.example.smart_emap.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.smart_emap.ui.system.user.avatarGradientFor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.DailyConfirmedSeriesDto
import com.example.smart_emap.core.auth.canAccessPath
import com.example.smart_emap.core.auth.isAdmin
import com.example.smart_emap.ui.shell.LocalCurrentUser
import com.example.smart_emap.ui.theme.LoginColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val DashboardBgTop = Color(0xFFEEF2FF)
private val DashboardBgMid = Color(0xFFF5F3FF)
private val DashboardBgBottom = Color(0xFFECFEFF)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = LocalCurrentUser.current
    val visibleQuickAccessItems = remember(uiState.quickAccessItems, user.id, user.menuCodes) {
        uiState.quickAccessItems.filter { user.canAccessPath(it.route) }
    }
    val showMenuAccessWarning = remember(user.id, user.menuCodes) {
        !user.isAdmin() && user.menuCodes.isEmpty()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        DashboardAmbientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StaggeredReveal(index = 0) {
                WelcomeBanner(
                    displayName = user.fullName?.trim().orEmpty().ifEmpty { user.username },
                    role = user.role,
                    departmentName = user.departmentName,
                    subtitle = "Smart-EMAP システムへようこそ",
                )
            }

            if (uiState.isLoading && uiState.statCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = LoginColors.Primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(28.dp),
                    )
                }
            } else {
                StatsRow(cards = uiState.statCards)
            }

            StaggeredReveal(index = 2) {
                DailyOrderChartSection(series = uiState.dailySeries)
            }

            if (showMenuAccessWarning) {
                MenuAccessWarningBanner()
            }

            StaggeredReveal(index = 3) {
                QuickAccessSection(
                    items = visibleQuickAccessItems,
                    onNavigate = onNavigate,
                )
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFDC2626),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun MenuAccessWarningBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = "メニュー権限が未設定です。管理者にロールとメニュー権限の割り当てを依頼してください。",
            color = Color(0xFF92400E),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun DashboardAmbientBackground() {
    val transition = rememberInfiniteTransition(label = "dashboard-bg")
    val blob1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "blob1",
    )
    val blob2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(11000), RepeatMode.Reverse),
        label = "blob2",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DashboardBgTop, DashboardBgMid, DashboardBgBottom),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (blob1 * 40).dp, y = (blob2 * 24).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x336366F1), Color.Transparent),
                        radius = 280f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-blob2 * 30).dp, y = (blob1 * 50).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x288B5CF6), Color.Transparent),
                        radius = 240f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (blob2 * 20).dp, y = (-blob1 * 16).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x2210B981), Color.Transparent),
                        radius = 260f,
                    ),
                ),
        )
    }
}

@Composable
private fun StaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 90L)
        visible = true
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 4 } +
            scaleIn(initialScale = 0.96f, animationSpec = tween(420, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}

@Composable
private fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 6.dp,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color(0x336366F1),
        spotColor = Color(0x556366F1),
    )
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.78f),
                Color.White.copy(alpha = 0.58f),
            ),
        ),
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.35f),
                Color(0xFF6366F1).copy(alpha = 0.12f),
            ),
        ),
        shape = shape,
    )

@Composable
private fun WelcomeBanner(
    displayName: String,
    role: String,
    departmentName: String?,
    subtitle: String,
) {
    val avatarLetter = remember(displayName) {
        displayName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty().ifEmpty { "?" }
    }
    val ambientTransition = rememberInfiniteTransition(label = "welcome-ambient")
    val shineX by ambientTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(3600), RepeatMode.Restart),
        label = "shine-x",
    )
    val floatY by ambientTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label = "avatar-float",
    )
    val pulseScale by ambientTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "status-pulse",
    )
    val shimmerPhase by ambientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Restart),
        label = "name-shimmer",
    )
    val nameBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFE0E7FF),
            Color.White,
            Color(0xFFC7D2FE),
            Color(0xFFF8FAFC),
        ),
        start = Offset(shimmerPhase * 480f - 120f, 0f),
        end = Offset(shimmerPhase * 480f + 120f, 48f),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x554F46E5),
                spotColor = Color(0x774F46E5),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4F46E5),
                        Color(0xFF6366F1),
                        Color(0xFF7C3AED),
                        Color(0xFF5B21B6),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = floatY.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color(0x660F172A))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                        .padding(3.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(6.dp, RoundedCornerShape(13.dp), spotColor = Color(0x550F172A))
                            .clip(RoundedCornerShape(13.dp))
                            .background(avatarGradientFor(displayName))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(13.dp))
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.28f),
                                            Color.Transparent,
                                        ),
                                        startY = 0f,
                                        endY = size.height * 0.45f,
                                    ),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = avatarLetter,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0x660F172A),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 6f,
                                ),
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "おかえりなさい",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = displayName,
                            style = TextStyle(
                                brush = nameBrush,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0x400F172A),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 10f,
                                ),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "さん",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp, bottom = 1.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WelcomeRoleChip(label = dashboardRoleDisplayName(role))
                        departmentName?.trim()?.takeIf { it.isNotEmpty() }?.let { dept ->
                            WelcomeGlassChip(text = dept)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x331F2937))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF6EE7B7), Color(0xFF34D399)),
                            ),
                        )
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFF34D399).copy(alpha = 0.35f),
                                radius = size.minDimension * 0.9f,
                            )
                        },
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                        radius = 520f,
                    ),
                ),
        )
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-12).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x73A78BFA), Color.Transparent),
                            radius = 280f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x260F172A), Color.Transparent),
                            radius = 360f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .fillMaxSize()
                    .offset(x = (shineX * 340).dp, y = (-8).dp)
                    .graphicsLayer { rotationZ = -12f }
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.06f),
                                Color.White.copy(alpha = 0.16f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeRoleChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.08f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
        )
    }
}

@Composable
private fun WelcomeGlassChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x2E0F172A))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun dashboardRoleDisplayName(role: String): String = when (role) {
    "admin" -> "管理者"
    "manager" -> "マネージャー"
    "worker" -> "作業者"
    "guest" -> "ゲスト"
    "viewer" -> "閲覧者"
    else -> "一般ユーザー"
}

@Composable
private fun StatsRow(cards: List<StatCardUi>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cards.forEachIndexed { index, card ->
            StaggeredReveal(
                index = 1 + index,
                modifier = Modifier.weight(1f),
            ) {
                StatCard(card = card)
            }
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
    route.contains("/erp/order/daily") -> Icons.Default.Description
    route.contains("shipping/list") -> Icons.Default.GridView
    route.contains("shipping/report") -> Icons.Default.LocalShipping
    route.contains("data-management") -> Icons.Default.TrendingUp
    route.contains("/aps/scheduling") -> Icons.Default.TrendingUp
    route.contains("welding-planning") -> Icons.Default.Description
    route.contains("planning-list") -> Icons.Default.Description
    route.contains("actualDataCollection/cutting") -> Icons.Default.Settings
    route.contains("actualDataCollection/chamfering") -> Icons.Default.Settings
    route.contains("actualDataCollection/welding") -> Icons.Default.GridView
    route.contains("actualDataCollection/inspection") -> Icons.Default.BarChart
    route.contains("productionInstruction/cutting") -> Icons.Default.Settings
    route.contains("forming") -> Icons.Default.Inventory2
    route.contains("productionInstruction/welding") -> Icons.Default.GridView
    else -> Icons.Default.GridView
}

@Composable
private fun StatCard(card: StatCardUi) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(12.dp), elevation = 5.dp),
    ) {
        val useHorizontal = maxWidth >= 140.dp
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            card.gradientStart.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        if (useHorizontal) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatCardIcon(card = card, size = 32.dp, iconSize = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    StatCardValue(card = card, fontSize = 14.sp)
                    StatCardLabel(card = card)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StatCardIcon(card = card, size = 28.dp, iconSize = 14.dp)
                Spacer(modifier = Modifier.height(4.dp))
                StatCardValue(card = card, fontSize = 12.sp)
                StatCardLabel(card = card, centered = true)
            }
        }
    }
}

@Composable
private fun StatCardIcon(card: StatCardUi, size: Dp, iconSize: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = card.gradientEnd.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(card.gradientStart, card.gradientEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(statIconForKey(card.key), contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun StatCardValue(card: StatCardUi, fontSize: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = card.value,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = LoginColors.TitleDark,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StatCardLabel(card: StatCardUi, centered: Boolean = false) {
    Text(
        text = card.label,
        fontSize = 9.5.sp,
        color = LoginColors.TextMuted,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 11.sp,
        textAlign = if (centered) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DailyOrderChartSection(series: DailyConfirmedSeriesDto?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(14.dp), elevation = 6.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF10B981)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(LoginColors.Primary.copy(alpha = 0.14f), Color(0xFF8B5CF6).copy(alpha = 0.1f)),
                                ),
                            )
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = LoginColors.Primary, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "日別受注数量（確定本数）",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000),
                        )
                        Text(
                            text = "過去2週間・今後1週間（JST）",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 12.sp,
                        )
                    }
                }
                Text(
                    text = "単位：本",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                )
            }
            val items = series?.items.orEmpty()
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(252.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("データを取得できませんでした", color = LoginColors.TextMuted, fontSize = 12.sp)
                }
            } else {
                DailyOrderChartLegend()
                DailyOrderBarChart(
                    series = series!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(236.dp)
                        .padding(start = 6.dp, end = 10.dp, bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun DailyOrderChartLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegendChip(
                gradient = listOf(Color(0xFF818CF8), Color(0xFF4338CA)),
                label = "過去",
                textColor = Color(0xFF4338CA),
            )
            ChartLegendChip(
                gradient = listOf(Color(0xFF34D399), Color(0xFF047857)),
                label = "今日",
                textColor = Color(0xFF047857),
            )
            ChartLegendChip(
                gradient = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)),
                label = "予定",
                textColor = Color(0xFF64748B),
            )
        }
    }
}

@Composable
private fun ChartLegendChip(
    gradient: List<Color>,
    label: String,
    textColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(gradient)),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

private data class DailyBarVisual(
    val gradient: List<Color>,
    val shadowColor: Color,
    val labelColor: Color,
)

private fun dailyBarVisual(date: String, asOf: String): DailyBarVisual = when {
    date < asOf -> DailyBarVisual(
        gradient = listOf(Color(0xFFC7D2FE), Color(0xFF818CF8), Color(0xFF4338CA)),
        shadowColor = Color(0x334338CA),
        labelColor = Color(0xFF4338CA),
    )
    date == asOf -> DailyBarVisual(
        gradient = listOf(Color(0xFF6EE7B7), Color(0xFF34D399), Color(0xFF047857)),
        shadowColor = Color(0x52047857),
        labelColor = Color(0xFF047857),
    )
    else -> DailyBarVisual(
        gradient = listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
        shadowColor = Color(0x3394A3B8),
        labelColor = Color(0xFF64748B),
    )
}

private fun formatChartDateLabel(isoDate: String): String = runCatching {
    LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("MM/dd"))
}.getOrElse {
    isoDate.takeLast(5).replace('-', '/')
}

private val chartNumberFormat = NumberFormat.getNumberInstance(Locale.JAPAN)

private fun chartXLabelStep(barCount: Int): Int = when {
    barCount > 18 -> 3
    barCount > 12 -> 2
    else -> 1
}

private fun chartValueFontPx(barCount: Int, density: Float): Float = when {
    barCount > 18 -> 7.5f * density
    barCount > 14 -> 8f * density
    else -> 8.5f * density
}

@Composable
private fun DailyOrderBarChart(
    series: DailyConfirmedSeriesDto,
    modifier: Modifier = Modifier,
) {
    val items = series.items
    val asOf = series.asOfDate
    val maxValue = max(items.maxOfOrNull { it.confirmedUnits } ?: 1, 1)
    val barProgress = remember { Animatable(0f) }

    LaunchedEffect(series) {
        barProgress.snapTo(0f)
        barProgress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val barCount = items.size
        if (barCount == 0) return@Canvas

        val leftPad = 44f
        val rightPad = 8f
        val bottomPad = 34f
        val topPad = 18f
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - bottomPad - topPad
        val slotW = chartW / barCount
        val barW = minOf(slotW * 0.46f, 22f)
        val progress = barProgress.value
        val yTicks = (0..4).map { tick -> (maxValue * tick) / 4 }
        val xLabelStep = chartXLabelStep(barCount)
        val valueFontPx = chartValueFontPx(barCount, density)
        val xLabelFontPx = 9f * density
        val valueLabelOffset = 5f * density

        yTicks.forEach { tick ->
            val ratio = tick.toFloat() / maxValue
            val gridY = topPad + chartH * (1f - ratio)
            if (tick > 0) {
                drawLine(
                    color = Color(0xFFE8ECF1),
                    start = Offset(leftPad, gridY),
                    end = Offset(size.width - rightPad, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 5f)),
                )
            }
            if (tick > 0 || maxValue == 0) {
                drawChartLabel(
                    text = chartNumberFormat.format(tick),
                    x = leftPad - 8f,
                    y = gridY + 3.5f,
                    color = Color(0xFF94A3B8),
                    fontSizePx = 9f * density,
                    align = Paint.Align.RIGHT,
                )
            }
        }

        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - rightPad, topPad + chartH),
            strokeWidth = 1.2f,
        )

        var todayLineX: Float? = null

        items.forEachIndexed { index, item ->
            val ratio = item.confirmedUnits.toFloat() / maxValue
            val barH = chartH * ratio * progress
            val x = leftPad + index * slotW + (slotW - barW) / 2f
            val y = topPad + chartH - barH
            val visual = dailyBarVisual(item.date, asOf)
            val isToday = item.date == asOf

            if (barH > 1f) {
                drawRoundRect(
                    color = visual.shadowColor,
                    topLeft = Offset(x + 1f, y + 2.5f),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to visual.gradient[0],
                            0.55f to visual.gradient[1],
                            1f to visual.gradient[2],
                        ),
                        startY = y,
                        endY = y + barH,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f),
                )
                drawRoundRect(
                    color = Color(0xFFF8FAFC).copy(alpha = if (isToday) 0.22f else 0.14f),
                    topLeft = Offset(x + 1.5f, y + 1.5f),
                    size = Size(barW - 3f, barH * 0.3f),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }

            if (progress > 0.9f) {
                val valueText = chartNumberFormat.format(item.confirmedUnits)
                val labelY = if (barH > 2f) {
                    maxOf(y - valueLabelOffset, topPad + 2f)
                } else {
                    topPad + chartH - valueLabelOffset - 2f
                }
                drawChartLabel(
                    text = valueText,
                    x = x + barW / 2f,
                    y = labelY,
                    color = visual.labelColor,
                    fontSizePx = if (isToday) valueFontPx + 0.5f * density else valueFontPx,
                    align = Paint.Align.CENTER,
                    bold = isToday,
                )
            }

            val showXLabel = index % xLabelStep == 0 || isToday
            if (showXLabel) {
                drawChartLabel(
                    text = formatChartDateLabel(item.date),
                    x = x + barW / 2f,
                    y = topPad + chartH + 10f * density,
                    color = if (isToday) Color(0xFFC2410C) else Color(0xFF64748B),
                    fontSizePx = if (isToday) xLabelFontPx + 0.5f * density else xLabelFontPx,
                    align = Paint.Align.CENTER,
                    bold = isToday,
                )
            }

            if (isToday) {
                todayLineX = x + barW / 2f
            }
        }

        todayLineX?.let { lineX ->
            drawLine(
                color = Color(0xFFF59E0B).copy(alpha = 0.75f),
                start = Offset(lineX, topPad),
                end = Offset(lineX, topPad + chartH),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
            )
        }
    }
}

private fun DrawScope.drawChartLabel(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    fontSizePx: Float,
    align: Paint.Align = Paint.Align.CENTER,
    bold: Boolean = false,
    rotation: Float = 0f,
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
        if (rotation != 0f) {
            save()
            translate(x, y)
            rotate(rotation)
            drawText(text, 0f, 0f, paint)
            restore()
        } else {
            drawText(text, x, y, paint)
        }
    }
}

private const val QuickAccessColumns = 5
private val QuickAccessCardHeight = 78.dp
private val QuickAccessGridSpacing = 6.dp

@Composable
private fun QuickAccessSection(
    items: List<QuickAccessUi>,
    onNavigate: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(14.dp), elevation = 6.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF06B6D4)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(LoginColors.Primary.copy(alpha = 0.14f), Color(0xFF06B6D4).copy(alpha = 0.1f)),
                            ),
                        )
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, tint = LoginColors.Primary, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "クイックアクセス",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                )
            }
            if (items.isEmpty()) {
                Text(
                    text = "利用可能なクイックアクセスがありません",
                    color = LoginColors.TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(QuickAccessColumns),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(quickAccessGridHeight(itemCount = items.size)),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(QuickAccessGridSpacing),
                    verticalArrangement = Arrangement.spacedBy(QuickAccessGridSpacing),
                    userScrollEnabled = false,
                ) {
                    itemsIndexed(items, key = { _, item -> item.route }) { index, item ->
                        StaggeredReveal(index = 4 + index) {
                            QuickAccessCard(item = item, onClick = { onNavigate(item.route) })
                        }
                    }
                }
            }
        }
    }
}

private fun quickAccessGridHeight(itemCount: Int): Dp {
    if (itemCount <= 0) return 0.dp
    val rows = (itemCount + QuickAccessColumns - 1) / QuickAccessColumns
    return (rows * QuickAccessCardHeight.value + (rows - 1).coerceAtLeast(0) * QuickAccessGridSpacing.value).dp
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessUi,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "quick-press",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(QuickAccessCardHeight)
            .scale(scale)
            .shadow(
                elevation = if (pressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = item.gradientStart.copy(alpha = 0.2f),
                spotColor = item.gradientEnd.copy(alpha = 0.28f),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.82f),
                        Color.White.copy(alpha = 0.65f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        item.gradientStart.copy(alpha = 0.18f),
                    ),
                ),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        val compact = maxWidth < 72.dp
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(item.gradientStart, item.gradientEnd))),
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (compact) 3.dp else 5.dp, vertical = if (compact) 4.dp else 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 26.dp else 30.dp)
                            .shadow(3.dp, RoundedCornerShape(8.dp), spotColor = item.gradientEnd.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(item.gradientStart, item.gradientEnd))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            quickIconForRoute(item.route),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (compact) 14.dp else 16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(if (compact) 3.dp else 4.dp))
                    Text(
                        text = item.title,
                        fontSize = if (compact) 9.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = if (compact) 11.sp else 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    if (!compact) {
                        Text(
                            text = item.description,
                            fontSize = 8.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
            if (!compact) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = item.gradientStart.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 5.dp, bottom = 4.dp)
                        .size(10.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
) {
    Row(
        modifier = Modifier.padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LoginColors.TitleDark)
            subtitle?.let {
                Text(text = it, fontSize = 10.sp, color = LoginColors.TextMuted, lineHeight = 12.sp)
            }
        }
    }
}
