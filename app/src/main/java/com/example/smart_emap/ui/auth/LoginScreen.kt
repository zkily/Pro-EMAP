package com.example.smart_emap.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.R
import com.example.smart_emap.ui.deviceowner.KioskAdminDialog
import com.example.smart_emap.ui.theme.LoginColors
import kotlinx.coroutines.launch

private val LoginSmoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val LoginAmbientEase = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/** 单一入场进度驱动错峰显现，避免多层 AnimatedVisibility 叠加卡顿 */
private fun revealAt(progress: Float, start: Float, span: Float = 0.38f): Float {
    val t = ((progress - start) / span).coerceIn(0f, 1f)
    return FastOutSlowInEasing.transform(t)
}

@Composable
private fun rememberLoginEntranceProgress(): Animatable<Float, AnimationVector1D> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(920, easing = LoginSmoothEase))
    }
    return progress
}

private fun Modifier.loginReveal(
    progress: Float,
    start: Float,
    span: Float = 0.38f,
    offsetY: Float = 22f,
    offsetX: Float = 0f,
): Modifier = graphicsLayer {
    val reveal = revealAt(progress, start, span)
    alpha = reveal
    translationY = (1f - reveal) * offsetY
    translationX = (1f - reveal) * offsetX
}

private data class FeatureItem(
    val tag: String,
    val label: String,
    val icon: ImageVector,
    val gradient: Pair<Color, Color>,
)

private val features = listOf(
    FeatureItem("ERP", "企業資源計画", Icons.Default.Business, LoginColors.ErpBadgeStart to LoginColors.ErpBadgeEnd),
    FeatureItem("APS", "先進的計画・スケジューリング", Icons.Default.CalendarMonth, LoginColors.ApsBadgeStart to LoginColors.ApsBadgeEnd),
    FeatureItem("MES", "製造実行システム", Icons.Default.Monitor, LoginColors.MesBadgeStart to LoginColors.MesBadgeEnd),
)

private data class LoginDimensions(
    val isWide: Boolean,
    val logoSize: Dp,
    val brandTitleSize: TextUnit,
    val brandSubtitleSize: TextUnit,
    val formTitleSize: TextUnit,
    val headerIconSize: Dp,
    val panelPaddingH: Dp,
    val panelPaddingV: Dp,
    val sectionGap: Dp,
    val fieldGap: Dp,
    val featureRowPaddingV: Dp,
    val featureIconSize: Dp,
    val outerPadding: Dp,
    val cardMaxWidth: Dp,
    val cardMaxHeightFraction: Float,
)

@Composable
private fun rememberLoginDimensions(maxWidth: Dp, maxHeight: Dp): LoginDimensions {
    val isWide = maxWidth >= 720.dp && maxWidth > maxHeight
    val isCompactHeight = maxHeight < 700.dp
    val compact = isWide || isCompactHeight

    return LoginDimensions(
        isWide = isWide,
        logoSize = if (compact) 52.dp else 64.dp,
        brandTitleSize = if (compact) 22.sp else 26.sp,
        brandSubtitleSize = if (compact) 12.sp else 14.sp,
        formTitleSize = if (compact) 22.sp else 24.sp,
        headerIconSize = if (compact) 40.dp else 48.dp,
        panelPaddingH = if (compact) 18.dp else 24.dp,
        panelPaddingV = if (compact) 16.dp else 20.dp,
        sectionGap = if (compact) 10.dp else 14.dp,
        fieldGap = if (compact) 10.dp else 12.dp,
        featureRowPaddingV = if (compact) 8.dp else 10.dp,
        featureIconSize = if (compact) 32.dp else 36.dp,
        outerPadding = if (compact) 10.dp else 14.dp,
        cardMaxWidth = 960.dp,
        cardMaxHeightFraction = if (isWide) 0.94f else 1f,
    )
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showForgotDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showKioskAdminDialog by remember { mutableStateOf(false) }
    val entranceProgress = rememberLoginEntranceProgress()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (showForgotDialog) {
        InfoAlertDialog(
            title = "パスワードを忘れた場合",
            message = "システム管理者に連絡してください",
            onDismiss = { showForgotDialog = false },
        )
    }
    if (showContactDialog) {
        InfoAlertDialog(
            title = "お問い合わせ",
            message = "システム管理者に連絡してください",
            onDismiss = { showContactDialog = false },
        )
    }
    if (showKioskAdminDialog) {
        KioskAdminDialog(onDismiss = { showKioskAdminDialog = false })
    }

    val formCallbacks = LoginFormCallbacks(
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onApiBaseUrlChange = viewModel::onApiBaseUrlChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onTogglePasswordVisible = viewModel::togglePasswordVisible,
        onForgotPassword = { showForgotDialog = true },
        onLogin = { viewModel.login(onLoginSuccess) },
        onContactAdmin = { showContactDialog = true },
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.systemBarsPadding(),
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LoginAnimatedBackground()
            val dims = rememberLoginDimensions(maxWidth, maxHeight)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dims.outerPadding)
                    .imePadding(),
                contentAlignment = Alignment.Center,
            ) {
                if (dims.isWide) {
                    WideLoginLayout(
                        uiState = uiState,
                        dims = dims,
                        callbacks = formCallbacks,
                        entranceProgress = entranceProgress.value,
                        onBrandLongPress = { showKioskAdminDialog = true },
                    )
                } else {
                    NarrowLoginLayout(
                        uiState = uiState,
                        dims = dims,
                        callbacks = formCallbacks,
                        entranceProgress = entranceProgress.value,
                        onBrandLongPress = { showKioskAdminDialog = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginAnimatedBackground() {
    val infinite = rememberInfiniteTransition(label = "login-bg")
    val drift1 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LoginAmbientEase),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orb1",
    )
    val drift2 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LoginAmbientEase),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orb2",
    )
    val spotlightPulse by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = LoginAmbientEase),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "spotlight",
    )

    Box(
        modifier = Modifier
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
                .size(340.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = spotlightPulse
                    scaleY = spotlightPulse
                    translationY = (-48f + drift1 * 24f)
                }
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
                .size(280.dp)
                .graphicsLayer {
                    translationX = (-60f + drift1 * 40f)
                    translationY = (-40f + drift1 * 30f)
                }
                .alpha(0.35f)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer {
                    translationX = (20f - drift2 * 50f)
                    translationY = (40f - drift2 * 35f)
                }
                .alpha(0.28f)
                .background(LoginColors.Accent.copy(alpha = 0.25f), CircleShape),
        )
    }
}

private fun Modifier.loginGlassSurface(
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 10.dp,
    accentGlow: Color = Color.White,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.42f),
            spotColor = accentGlow.copy(alpha = 0.38f),
        )
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.24f),
                    Color.White.copy(alpha = 0.07f),
                    Color.White.copy(alpha = 0.16f),
                ),
            ),
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.48f),
                    Color.White.copy(alpha = 0.1f),
                    Color.White.copy(alpha = 0.3f),
                ),
            ),
            shape = shape,
        )
}

@Composable
private fun BrandLogoMark(
    size: Dp,
    cornerRadius: Dp,
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size + 22.dp)
                .alpha(0.48f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LoginColors.Accent.copy(alpha = 0.55f),
                            LoginColors.Primary.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(size + 5.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(cornerRadius + 2.dp),
                    spotColor = LoginColors.Primary.copy(alpha = 0.55f),
                )
                .clip(RoundedCornerShape(cornerRadius + 2.dp))
                .background(Color.Black.copy(alpha = 0.32f)),
        )
        Image(
            painter = painterResource(R.drawable.smart_logo_full),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .border(1.dp, Color.White.copy(alpha = 0.38f), RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
        )
    }
}

private data class LoginFormCallbacks(
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onApiBaseUrlChange: (String) -> Unit,
    val onRememberMeChange: (Boolean) -> Unit,
    val onTogglePasswordVisible: () -> Unit,
    val onForgotPassword: () -> Unit,
    val onLogin: () -> Unit,
    val onContactAdmin: () -> Unit,
)

@Composable
private fun WideLoginLayout(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
    entranceProgress: Float,
    onBrandLongPress: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .widthIn(max = dims.cardMaxWidth)
            .fillMaxWidth()
            .fillMaxHeight(dims.cardMaxHeightFraction),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                LoginColors.BrandPanelStart,
                                LoginColors.BrandPanelMid,
                                LoginColors.BrandPanelEnd,
                            ),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BrandHeader(
                        dims = dims,
                        embedded = true,
                        entranceProgress = entranceProgress,
                        onBrandLongPress = onBrandLongPress,
                    )
                    Spacer(modifier = Modifier.height(dims.sectionGap))
                    FeatureList(dims = dims, entranceProgress = entranceProgress)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LoginColors.RightPanelBg,
                                Color(0xFFF1F5F9),
                                Color(0xFFEEF2FF),
                            ),
                        ),
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoginFormContent(
                    uiState = uiState,
                    dims = dims,
                    callbacks = callbacks,
                    showCardShell = false,
                    entranceProgress = entranceProgress,
                    revealStart = 0.18f,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "© 2026 Smart-EMAP. All rights reserved.",
                    fontSize = 10.sp,
                    color = LoginColors.TextLight,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NarrowLoginLayout(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
    entranceProgress: Float,
    onBrandLongPress: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandHeader(
            dims = dims,
            embedded = false,
            entranceProgress = entranceProgress,
            onBrandLongPress = onBrandLongPress,
        )
        Spacer(modifier = Modifier.height(dims.sectionGap))
        LoginFormContent(
            uiState = uiState,
            dims = dims,
            callbacks = callbacks,
            showCardShell = true,
            entranceProgress = entranceProgress,
            revealStart = 0.14f,
        )
        Spacer(modifier = Modifier.height(dims.sectionGap))
        FeatureList(dims = dims, entranceProgress = entranceProgress, compact = true)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "© 2026 Smart-EMAP. All rights reserved.",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BrandHeader(
    dims: LoginDimensions,
    embedded: Boolean,
    entranceProgress: Float,
    onBrandLongPress: () -> Unit = {},
) {
    val content = @Composable {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .loginReveal(entranceProgress, start = 0.04f, span = 0.34f, offsetY = 20f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandLogoMark(
                    size = dims.logoSize,
                    cornerRadius = if (embedded) 16.dp else 18.dp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Smart-EMAP",
                    fontSize = dims.brandTitleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    // 隐藏入口：长按标题打开端末管理（Kiosk）对话框，供无网络时解除 Kiosk
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { onBrandLongPress() })
                    },
                )
                Text(
                    text = "生産管理システム",
                    fontSize = dims.brandSubtitleSize,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModuleBadge("ERP", LoginColors.ErpBadgeStart, LoginColors.ErpBadgeEnd, index = 0, entranceProgress = entranceProgress)
                ModuleBadge("APS", LoginColors.ApsBadgeStart, LoginColors.ApsBadgeEnd, index = 1, entranceProgress = entranceProgress)
                ModuleBadge("MES", LoginColors.MesBadgeStart, LoginColors.MesBadgeEnd, index = 2, entranceProgress = entranceProgress)
            }
        }
    }

    if (embedded) {
        content()
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .loginGlassSurface(cornerRadius = 22.dp, elevation = 18.dp, accentGlow = LoginColors.Accent)
                .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun ModuleBadge(
    text: String,
    start: Color,
    end: Color,
    index: Int = 0,
    entranceProgress: Float,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "badge-scale",
    )
    val badgeStart = 0.12f + index * 0.07f

    Text(
        text = text,
        modifier = Modifier
            .loginReveal(entranceProgress, start = badgeStart, span = 0.28f, offsetX = 14f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = start.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Composable
private fun FeatureModuleCard(
    feat: FeatureItem,
    dims: LoginDimensions,
    compact: Boolean,
    index: Int,
    entranceProgress: Float,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 400f),
        label = "feature-scale",
    )
    val cardStart = 0.22f + index * 0.1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .loginReveal(entranceProgress, start = cardStart, span = 0.32f, offsetY = 16f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .loginGlassSurface(
                cornerRadius = 18.dp,
                elevation = 10.dp,
                accentGlow = feat.gradient.first,
            )
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(horizontal = 14.dp, vertical = dims.featureRowPaddingV + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(dims.featureIconSize + 10.dp)
                    .alpha(0.45f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                feat.gradient.first.copy(alpha = 0.75f),
                                Color.Transparent,
                            ),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(dims.featureIconSize)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = feat.gradient.second.copy(alpha = 0.55f),
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(feat.gradient.first, feat.gradient.second)))
                    .border(1.dp, Color.White.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    feat.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (dims.isWide) 16.dp else 18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                feat.tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.88f),
            )
            Text(
                feat.label,
                fontSize = if (compact) 11.sp else 12.sp,
                color = Color.White,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun FeatureList(
    dims: LoginDimensions,
    entranceProgress: Float,
    compact: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (dims.isWide) 8.dp else 10.dp)) {
        features.forEachIndexed { index, feat ->
            FeatureModuleCard(
                feat = feat,
                dims = dims,
                compact = compact,
                index = index,
                entranceProgress = entranceProgress,
            )
        }
    }
}

@Composable
private fun LoginFormContent(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
    showCardShell: Boolean,
    entranceProgress: Float,
    revealStart: Float,
) {
    val usernameBringIntoView = remember { BringIntoViewRequester() }
    val passwordBringIntoView = remember { BringIntoViewRequester() }
    val apiBringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val inner = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = if (dims.isWide) 380.dp else Dp.Unspecified)
                .loginReveal(entranceProgress, start = revealStart, span = 0.4f, offsetY = 24f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(dims.headerIconSize)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(LoginColors.Primary, LoginColors.PrimaryDark))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ログイン",
                    fontSize = dims.formTitleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = LoginColors.TitleDark,
                )
                Text(
                    text = "アカウント情報を入力してください",
                    fontSize = 12.sp,
                    color = LoginColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(if (dims.isWide) 16.dp else 18.dp))

            FormLabel("ユーザー名")
            OutlinedTextField(
                value = uiState.username,
                onValueChange = callbacks.onUsernameChange,
                placeholder = { Text("ユーザー名またはメールアドレス", fontSize = 13.sp) },
                singleLine = true,
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError?.let { { Text(it, fontSize = 11.sp) } },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(usernameBringIntoView)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch { usernameBringIntoView.bringIntoView() }
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = loginFieldColors(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            )

            Spacer(modifier = Modifier.height(dims.fieldGap))
            FormLabel("パスワード")
            OutlinedTextField(
                value = uiState.password,
                onValueChange = callbacks.onPasswordChange,
                placeholder = { Text("パスワードを入力", fontSize = 13.sp) },
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it, fontSize = 11.sp) } },
                visualTransformation = if (uiState.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = callbacks.onTogglePasswordVisible) {
                        Icon(
                            if (uiState.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(passwordBringIntoView)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch { passwordBringIntoView.bringIntoView() }
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = loginFieldColors(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            )

            Spacer(modifier = Modifier.height(dims.fieldGap))
            FormLabel("API サーバー")
            OutlinedTextField(
                value = uiState.apiBaseUrl,
                onValueChange = callbacks.onApiBaseUrlChange,
                placeholder = { Text("http://192.168.1.62:3010", fontSize = 13.sp) },
                singleLine = true,
                isError = uiState.apiBaseUrlError != null,
                supportingText = uiState.apiBaseUrlError?.let { { Text(it, fontSize = 11.sp) } },
                leadingIcon = {
                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { callbacks.onLogin() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(apiBringIntoView)
                    .onFocusEvent { event ->
                        if (event.isFocused) {
                            scope.launch { apiBringIntoView.bringIntoView() }
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = loginFieldColors(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = callbacks.onRememberMeChange,
                    )
                    Text(
                        text = "ログイン状態を保持",
                        fontSize = 12.sp,
                        color = LoginColors.FieldLabel,
                        modifier = Modifier.clickable { callbacks.onRememberMeChange(!uiState.rememberMe) },
                    )
                }
                TextButton(onClick = callbacks.onForgotPassword) {
                    Text("パスワードを忘れた場合", fontSize = 11.sp)
                }
            }

            uiState.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = msg,
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            LoginPrimaryButton(
                isLoading = uiState.isLoading,
                onClick = callbacks.onLogin,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text("または", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 10.sp, color = LoginColors.TextLight)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { callbacks.onContactAdmin() }
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LoginColors.Primary.copy(alpha = 0.07f),
                                LoginColors.PrimaryDark.copy(alpha = 0.04f),
                            ),
                        ),
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(LoginColors.Primary, LoginColors.PrimaryDark))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("アカウントをお持ちでない場合", fontSize = 11.sp, color = LoginColors.TextMuted)
                    Text(
                        "システム管理者に連絡してください",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoginColors.Primary,
                    )
                }
            }
        }
    }

    if (showCardShell) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(LoginColors.Primary, LoginColors.Accent, LoginColors.PrimaryDark),
                        ),
                    ),
            )
            Column(modifier = Modifier.padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV)) {
                inner()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.94f))
                .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
        ) {
            inner()
        }
    }
}

@Composable
private fun LoginPrimaryButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 440f),
        label = "login-btn-scale",
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF5B6FD6), LoginColors.Primary, LoginColors.PrimaryDark),
                ),
            )
            .clickable(
                enabled = !isLoading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ログイン", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LoginColors.FieldLabel)
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LoginColors.Primary,
    unfocusedBorderColor = Color(0xFFE2E8F0),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF8FAFC),
)

@Composable
private fun InfoAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}
