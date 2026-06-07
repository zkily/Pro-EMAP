package com.example.smart_emap.ui.erp.order

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GlassButtonStyle(
    val gradient: Brush,
    val shadowColor: Color,
    val bold: Boolean = false,
) {
    Blue(
        gradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF1E40AF))),
        shadowColor = Color(0xFF2563EB),
    ),
    Teal(
        gradient = Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF0D9488), Color(0xFF0F766E))),
        shadowColor = Color(0xFF0D9488),
    ),
    Amber(
        gradient = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFB45309))),
        shadowColor = Color(0xFFD97706),
    ),
    Indigo(
        gradient = Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5))),
        shadowColor = Color(0xFF4F46E5),
    ),
    Green(
        gradient = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF047857))),
        shadowColor = Color(0xFF059669),
        bold = true,
    ),
}

@Composable
fun GlassToolbarButton(
    label: String,
    icon: ImageVector,
    style: GlassButtonStyle,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "glass-btn-scale",
    )
    val elevation by animateFloatAsState(
        targetValue = if (pressed) 4f else 10f,
        animationSpec = tween(120),
        label = "glass-btn-elev",
    )

    Box(
        modifier = modifier
            .height(40.dp)
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = style.shadowColor.copy(alpha = 0.35f),
                spotColor = style.shadowColor.copy(alpha = 0.55f),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(style.gradient)
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.24f), Color.Transparent),
                        startY = 0f,
                        endY = 80f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x33000000)),
                        startY = 0f,
                        endY = 200f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.35f)),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.65f),
                fontSize = 13.sp,
                fontWeight = if (style.bold) FontWeight.Bold else FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
            )
        }
    }
}

@Composable
fun Modifier.orderMonthlyGlassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    elevation: Dp = 6.dp,
): Modifier = this
    .shadow(elevation, shape, ambientColor = Color(0x14000000), spotColor = Color(0x1A6366F1))
    .clip(shape)
    .background(Color.White.copy(alpha = 0.72f))
    .border(1.dp, Color.White.copy(alpha = 0.78f), shape)

@Composable
fun Modifier.orderMonthlyFilterGlass(
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
): Modifier = this
    .shadow(4.dp, shape, ambientColor = Color(0x0A000000))
    .clip(shape)
    .background(Color.White.copy(alpha = 0.65f))
    .border(1.dp, Color.White.copy(alpha = 0.7f), shape)

@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (filled) {
                    Modifier.background(
                        Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
                    )
                } else {
                    Modifier
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (filled) Color.White else OrderMonthlyColors.TextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
