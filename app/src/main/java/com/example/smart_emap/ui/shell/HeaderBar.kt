package com.example.smart_emap.ui.shell

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.system.user.avatarGradientFor
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
    headerTodoState: HeaderTodoUiState = HeaderTodoUiState(),
    onHeaderTodoOpen: () -> Unit = {},
    onHeaderTodoDraftChange: (String) -> Unit = {},
    onHeaderTodoAdd: () -> Unit = {},
    onHeaderTodoToggle: (Int) -> Unit = {},
    onHeaderTodoDelete: (Int) -> Unit = {},
    onHeaderTodoClearDone: () -> Unit = {},
    onHeaderTodoUpdateContent: (Int, String) -> Unit = { _, _ -> },
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
                    HeaderTodoTrigger(
                        state = headerTodoState,
                        onOpen = onHeaderTodoOpen,
                        onDraftChange = onHeaderTodoDraftChange,
                        onAdd = onHeaderTodoAdd,
                        onToggle = onHeaderTodoToggle,
                        onDelete = onHeaderTodoDelete,
                        onClearDone = onHeaderTodoClearDone,
                        onUpdateContent = onHeaderTodoUpdateContent,
                    )
                }
            }

            Box {
                HeaderUserChip(
                    displayName = user.fullName ?: user.username,
                    role = roleDisplayName(user.role),
                    expanded = userMenuExpanded,
                    onClick = { userMenuExpanded = true },
                )
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
private fun HeaderUserChip(
    displayName: String,
    role: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(140),
        label = "header-user-press",
    )
    val pulseTransition = rememberInfiniteTransition(label = "header-user-pulse")
    val ringAlpha by pulseTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "header-user-ring",
    )
    val avatarLetter = remember(displayName) {
        displayName.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty().ifEmpty { "?" }
    }

    Row(
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(start = 5.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = ringAlpha),
                        radius = size.minDimension * 0.62f,
                    )
                }
                .clip(RoundedCornerShape(9.dp))
                .background(avatarGradientFor(displayName))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.24f), Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.5f,
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarLetter,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.widthIn(max = 120.dp)) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0x440F172A),
                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                        blurRadius = 4f,
                    ),
                ),
            )
            Text(
                text = role,
                color = Color(0xFFC7D2FE),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.75f),
            modifier = Modifier
                .size(16.dp)
                .scale(scaleX = 1f, scaleY = if (expanded) -1f else 1f),
        )
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
