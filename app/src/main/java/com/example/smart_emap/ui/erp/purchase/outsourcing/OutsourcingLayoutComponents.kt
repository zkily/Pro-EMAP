package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val OutsourcingAccentGradient = Brush.linearGradient(
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
)

private val OutsourcingDashboardBg = Brush.linearGradient(
    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)),
)

private val OutsourcingOrderBg = Brush.linearGradient(
    listOf(Color(0xFFF5F7FA), Color(0xFFE4E8ED)),
)

private val OutsourcingProcessBg = Brush.linearGradient(
    listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
)

private val OutsourcingMasterBg = Brush.linearGradient(
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
)

data class OutsourcingQuickRoute(
    val path: String,
    val label: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color,
)

@Composable
fun OutsourcingDashboardBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OutsourcingDashboardBg),
    ) {
        content()
    }
}

@Composable
fun OutsourcingOrderPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OutsourcingOrderBg),
    ) {
        content()
    }
}

@Composable
fun OutsourcingMasterPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OutsourcingMasterBg),
    ) {
        content()
    }
}

@Composable
fun OutsourcingProcessPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OutsourcingProcessBg),
    ) {
        content()
    }
}

@Composable
fun OutsourcingDashboardGlassHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4FACFE), modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Button(
                onClick = onRefresh,
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("更新", fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
fun OutsourcingOrderGlassHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    statValue: String,
    actionLabel: String,
    actionLoading: Boolean,
    onAction: () -> Unit,
) {
    OutsourcingHeroHeader(
        title = title,
        subtitle = subtitle,
        icon = icon,
        gradient = OutsourcingAccentGradient,
        badge = statValue,
        trailing = {
            Button(
                onClick = onAction,
                enabled = !actionLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.22f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                if (actionLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(actionLabel, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    )
}

@Composable
fun OutsourcingProcessHeroHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    stats: List<Pair<String, String>>,
) {
    OutsourcingHeroHeader(
        title = title,
        subtitle = subtitle,
        icon = icon,
        gradient = OutsourcingAccentGradient,
        kpis = stats.map { (value, label) -> OutsourcingKpiItem(value, label) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutsourcingDashboardStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconGradient: Brush,
    details: List<String>,
    hasAlert: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (hasAlert) Color(0xFFFF6347).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasAlert) Color(0xFFFF6B6B) else Color.White,
                lineHeight = 22.sp,
            )
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            if (details.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    details.forEach { line ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.1f),
                        ) {
                            Text(
                                line,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingTablePanel(
    title: String,
    icon: ImageVector,
    darkGlass: Boolean = false,
    content: @Composable () -> Unit,
) {
    val accent = if (darkGlass) {
        Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
    } else {
        OutsourcingGlassGradient
    }
    OutsourcingGlassCard(accent = accent) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, contentDescription = null, tint = if (darkGlass) Color.White else Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (darkGlass) Color.White else Color(0xFF1E293B),
                )
            }
            content()
        }
    }
}

@Composable
fun OutsourcingLoadingBox(isLoading: Boolean) {
    if (!isLoading) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = Color(0xFF6366F1))
    }
}

@Composable
fun OutsourcingEmptyState(
    emoji: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, fontSize = 28.sp)
        Text(message, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
        ) {
            Text(actionLabel, fontSize = 11.sp)
        }
    }
}

@Composable
fun OutsourcingTag(
    text: String,
    containerColor: Color,
    contentColor: Color = Color.White,
) {
    Surface(shape = RoundedCornerShape(6.dp), color = containerColor) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutsourcingQuickAccessSection(
    routes: List<OutsourcingQuickRoute>,
    onNavigate: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("クイックアクセス", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                routes.forEach { route ->
                    Surface(
                        modifier = Modifier.clickable { onNavigate(route.path) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Brush.linearGradient(listOf(route.startColor, route.endColor)))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(route.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(route.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
