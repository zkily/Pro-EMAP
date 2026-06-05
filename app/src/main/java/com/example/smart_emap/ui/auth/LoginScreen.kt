package com.example.smart_emap.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.network.ApiDefaults
import com.example.smart_emap.ui.theme.LoginColors

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

/** 按屏幕尺寸缩放，10 寸横屏（约 960×600dp）走 wide + compact */
private data class LoginDimensions(
    val isWide: Boolean,
    val logoSize: Dp,
    val logoIconSize: Dp,
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
    // 10 寸横屏通常宽 > 高，且宽度 ≥ 720dp
    val isWide = maxWidth >= 720.dp && maxWidth > maxHeight
    val isCompactHeight = maxHeight < 700.dp
    val compact = isWide || isCompactHeight

    return LoginDimensions(
        isWide = isWide,
        logoSize = if (compact) 56.dp else 72.dp,
        logoIconSize = if (compact) 28.dp else 36.dp,
        brandTitleSize = if (compact) 24.sp else 28.sp,
        brandSubtitleSize = if (compact) 13.sp else 15.sp,
        formTitleSize = if (compact) 22.sp else 26.sp,
        headerIconSize = if (compact) 44.dp else 52.dp,
        panelPaddingH = if (compact) 20.dp else 28.dp,
        panelPaddingV = if (compact) 16.dp else 24.dp,
        sectionGap = if (compact) 10.dp else 16.dp,
        fieldGap = if (compact) 10.dp else 14.dp,
        featureRowPaddingV = if (compact) 8.dp else 12.dp,
        featureIconSize = if (compact) 34.dp else 40.dp,
        outerPadding = if (compact) 12.dp else 16.dp,
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

    val formCallbacks = LoginFormCallbacks(
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onApiBaseUrlChange = viewModel::onApiBaseUrlChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onToggleServerSettings = viewModel::toggleServerSettings,
        onTogglePasswordVisible = viewModel::togglePasswordVisible,
        onForgotPassword = { showForgotDialog = true },
        onLogin = { viewModel.login(onLoginSuccess) },
        onContactAdmin = { showContactDialog = true },
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            val dims = rememberLoginDimensions(maxWidth, maxHeight)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dims.outerPadding),
                contentAlignment = Alignment.Center,
            ) {
                if (dims.isWide) {
                    WideLoginLayout(
                        uiState = uiState,
                        dims = dims,
                        callbacks = formCallbacks,
                    )
                } else {
                    NarrowLoginLayout(
                        uiState = uiState,
                        dims = dims,
                        callbacks = formCallbacks,
                    )
                }
            }
        }
    }
}

private data class LoginFormCallbacks(
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onApiBaseUrlChange: (String) -> Unit,
    val onRememberMeChange: (Boolean) -> Unit,
    val onToggleServerSettings: () -> Unit,
    val onTogglePasswordVisible: () -> Unit,
    val onForgotPassword: () -> Unit,
    val onLogin: () -> Unit,
    val onContactAdmin: () -> Unit,
)

/** 横屏 / 10 寸平板：左右双栏，一屏展示（对齐 Web login-wrapper） */
@Composable
private fun WideLoginLayout(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
) {
    Card(
        modifier = Modifier
            .widthIn(max = dims.cardMaxWidth)
            .fillMaxWidth()
            .fillMaxHeight(dims.cardMaxHeightFraction),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
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
                    BrandHeader(dims = dims, embedded = true)
                    Spacer(modifier = Modifier.height(dims.sectionGap))
                    FeatureList(dims = dims)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                LoginColors.RightPanelBg,
                                Color(0xFFF1F5F9),
                                Color(0xFFEEF2FF),
                            ),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LoginFormContent(
                        uiState = uiState,
                        dims = dims,
                        callbacks = callbacks,
                        showCardShell = false,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2026 Smart-EMAP. All rights reserved.",
                        fontSize = 10.sp,
                        color = LoginColors.TextLight,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** 竖屏手机：纵向滚动 */
@Composable
private fun NarrowLoginLayout(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandHeader(dims = dims, embedded = false)
        Spacer(modifier = Modifier.height(dims.sectionGap))
        FeatureList(dims = dims)
        Spacer(modifier = Modifier.height(dims.sectionGap))
        LoginFormContent(
            uiState = uiState,
            dims = dims,
            callbacks = callbacks,
            showCardShell = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "© 2026 Smart-EMAP. All rights reserved.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BrandHeader(dims: LoginDimensions, embedded: Boolean) {
    val content = @Composable {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(dims.logoSize)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(dims.logoIconSize),
                )
            }
            Spacer(modifier = Modifier.height(if (dims.isWide) 10.dp else 14.dp))
            Text(
                text = "Smart-EMAP",
                fontSize = dims.brandTitleSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = "生産管理システム",
                fontSize = dims.brandSubtitleSize,
                color = Color.White.copy(alpha = 0.92f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModuleBadge("ERP", LoginColors.ErpBadgeStart, LoginColors.ErpBadgeEnd)
                ModuleBadge("APS", LoginColors.ApsBadgeStart, LoginColors.ApsBadgeEnd)
                ModuleBadge("MES", LoginColors.MesBadgeStart, LoginColors.MesBadgeEnd)
            }
            if (!dims.isWide) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "製造業のDXを実現する次世代統合管理システム",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
            }
        }
    }

    if (embedded) {
        content()
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                LoginColors.BrandPanelStart,
                                LoginColors.BrandPanelMid,
                                LoginColors.BrandPanelEnd,
                            ),
                        ),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ModuleBadge(text: String, start: Color, end: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Composable
private fun FeatureList(dims: LoginDimensions) {
    Column(verticalArrangement = Arrangement.spacedBy(if (dims.isWide) 6.dp else 10.dp)) {
        features.forEach { feat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = if (dims.isWide) 0.14f else 0.12f))
                    .padding(horizontal = 12.dp, vertical = dims.featureRowPaddingV),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(dims.featureIconSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(feat.gradient.first, feat.gradient.second))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        feat.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (dims.isWide) 16.dp else 20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        feat.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.85f),
                    )
                    Text(
                        feat.label,
                        fontSize = if (dims.isWide) 11.sp else 13.sp,
                        color = Color.White,
                        lineHeight = if (dims.isWide) 14.sp else 18.sp,
                    )
                }
                if (!dims.isWide) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginFormContent(
    uiState: LoginUiState,
    dims: LoginDimensions,
    callbacks: LoginFormCallbacks,
    showCardShell: Boolean,
) {
    val inner = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = if (dims.isWide) 380.dp else Dp.Unspecified),
        ) {
            if (showCardShell) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(LoginColors.Primary, LoginColors.Accent, LoginColors.PrimaryDark),
                            ),
                        ),
                )
                Spacer(modifier = Modifier.height(dims.sectionGap))
            }

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
                        modifier = Modifier.size(if (dims.isWide) 20.dp else 24.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ログイン",
                    fontSize = dims.formTitleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = LoginColors.TitleDark,
                )
            }

            Spacer(modifier = Modifier.height(if (dims.isWide) 14.dp else 20.dp))

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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { callbacks.onLogin() }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = loginFieldColors(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (dims.isWide) 10.dp else 14.dp),
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
                TextButton(
                    onClick = callbacks.onForgotPassword,
                    modifier = Modifier.padding(0.dp),
                ) {
                    Text("パスワードを忘れた場合", fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { callbacks.onToggleServerSettings() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = LoginColors.TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (uiState.showServerSettings) "サーバー設定を隠す" else "サーバー設定（Android）",
                    fontSize = 11.sp,
                    color = LoginColors.Primary,
                )
            }
            AnimatedVisibility(visible = uiState.showServerSettings) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.apiBaseUrl,
                        onValueChange = callbacks.onApiBaseUrlChange,
                        label = { Text("API サーバー", fontSize = 12.sp) },
                        placeholder = { Text(ApiDefaults.displayBaseUrl, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = loginFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    )
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

            Spacer(modifier = Modifier.height(if (dims.isWide) 12.dp else 16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (dims.isWide) 44.dp else 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5B6FD6), LoginColors.Primary, LoginColors.PrimaryDark),
                        ),
                    )
                    .clickable(enabled = !uiState.isLoading) { callbacks.onLogin() },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ログイン", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (dims.isWide) 12.dp else 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text("または", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 10.sp, color = LoginColors.TextLight)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(if (dims.isWide) 10.dp else 14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { callbacks.onContactAdmin() }
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LoginColors.Primary.copy(alpha = 0.06f),
                                LoginColors.PrimaryDark.copy(alpha = 0.04f),
                            ),
                        ),
                    )
                    .padding(if (dims.isWide) 10.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (dims.isWide) 32.dp else 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(LoginColors.Primary, LoginColors.PrimaryDark))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LoginColors.CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV)) {
                inner()
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LoginColors.CardBg.copy(alpha = 0.92f))
                    .padding(horizontal = dims.panelPaddingH, vertical = dims.panelPaddingV),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(LoginColors.Primary, LoginColors.Accent, LoginColors.PrimaryDark),
                            ),
                        ),
                )
                Spacer(modifier = Modifier.height(dims.sectionGap))
                inner()
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
