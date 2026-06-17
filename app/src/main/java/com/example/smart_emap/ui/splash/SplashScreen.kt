package com.example.smart_emap.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.R
import com.example.smart_emap.ui.theme.LoginColors

@Composable
fun SplashScreenContent(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "splash-bg")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "splash-drift",
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "splash-glow",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        LoginColors.BgGradientStart,
                        LoginColors.BgGradientMid,
                        LoginColors.BgGradientEnd,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size((340 * glowPulse).dp)
                .align(Alignment.TopCenter)
                .offset(y = (-48 + drift * 24).dp)
                .alpha(0.42f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LoginColors.Primary.copy(alpha = 0.38f),
                            LoginColors.Accent.copy(alpha = 0.14f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (20 - drift * 50).dp, y = (40 - drift * 35).dp)
                .alpha(0.28f)
                .background(LoginColors.Accent.copy(alpha = 0.25f), CircleShape),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(1.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.smart_logo_full),
                    contentDescription = null,
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(48.dp),
                            spotColor = LoginColors.Primary.copy(alpha = 0.55f),
                        )
                        .clip(RoundedCornerShape(48.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(48.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "生産管理システム",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.92f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SplashModuleBadge("ERP", LoginColors.ErpBadgeStart, LoginColors.ErpBadgeEnd)
                    SplashModuleBadge("APS", LoginColors.ApsBadgeStart, LoginColors.ApsBadgeEnd)
                    SplashModuleBadge("MES", LoginColors.MesBadgeStart, LoginColors.MesBadgeEnd)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "製造業のDXを実現する次世代統合管理システム",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            SplashCredits()
        }
    }
}

@Composable
private fun SplashModuleBadge(text: String, start: Color, end: Color) {
    Text(
        text = text,
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = start.copy(alpha = 0.55f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Composable
private fun SplashCredits() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Chinomeスタジオ",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.88f),
            letterSpacing = 0.08.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "✦",
                fontSize = 12.sp,
                color = LoginColors.Accent.copy(alpha = 0.95f),
            )
            Text(
                text = "プロデューサー ZK",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.72f),
                letterSpacing = 0.04.sp,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "著作権所有 ZK（HAYASHI）",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.55f),
            letterSpacing = 0.02.sp,
        )
    }
}
