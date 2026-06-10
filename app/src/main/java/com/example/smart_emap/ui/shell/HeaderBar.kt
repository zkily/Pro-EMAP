package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.UserDto
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HeaderBar(
    user: UserDto,
    isMobile: Boolean,
    sidebarOpen: Boolean,
    onToggleSidebar: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentTime by remember { mutableStateOf(formatHeaderTime()) }
    var weatherInfo by remember { mutableStateOf(HeaderWeatherInfo(temperature = "--", emoji = "🌤️")) }
    var userMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = formatHeaderTime()
            delay(60_000)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            weatherInfo = HeaderWeatherFetcher.fetch()
            delay(HeaderWeatherFetcher.REFRESH_MS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        LayoutColors.HeaderStart,
                        Color(0xFF312E81),
                        LayoutColors.HeaderMid,
                        Color(0xFF5B21B6),
                        LayoutColors.HeaderEnd,
                    ),
                ),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isMobile) {
                    IconButton(onClick = onToggleSidebar) {
                        Icon(
                            imageVector = if (sidebarOpen) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "メニュー",
                            tint = Color.White,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x470F172A))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFC7D2FE), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentTime,
                        color = Color(0xFFF8FAFC),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    HeaderWeatherInline(weatherInfo = weatherInfo)
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { userMenuExpanded = true }
                        .background(Color(0x470F172A))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFE0E7FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.fullName ?: user.username,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(
                    expanded = userMenuExpanded,
                    onDismissRequest = { userMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(user.fullName ?: user.username, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = roleDisplayName(user.role),
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                )
                            }
                        },
                        onClick = { userMenuExpanded = false },
                        enabled = false,
                    )
                    DropdownMenuItem(
                        text = { Text("ログアウト") },
                        onClick = {
                            userMenuExpanded = false
                            onLogout()
                        },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderWeatherInline(
    weatherInfo: HeaderWeatherInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(15.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.32f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = weatherInfo.emoji,
            fontSize = 13.sp,
            lineHeight = 13.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = weatherInfo.temperature,
            color = Color(0xFFC7D2FE),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 36.dp),
        )
    }
}

private fun formatHeaderTime(): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd (E) HH:mm", Locale.JAPAN)
    return ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).format(formatter)
}

private fun roleDisplayName(role: String): String = when (role) {
    "admin" -> "管理者"
    "manager" -> "マネージャー"
    "worker" -> "作業者"
    "guest" -> "ゲスト"
    "viewer" -> "閲覧者"
    else -> "一般ユーザー"
}
