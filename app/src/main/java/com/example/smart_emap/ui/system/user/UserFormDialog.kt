package com.example.smart_emap.ui.system.user

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.OrganizationDto
import com.example.smart_emap.data.model.RoleListItemDto
import kotlinx.coroutines.delay

private const val DIALOG_WIDTH_FRACTION = 0.64f // 0.92 × 0.7 ≈ 30% narrower

private val SectionBasic = Color(0xFF667EEA)
private val SectionAccess = Color(0xFF0EA5E9)
private val SectionSecurity = Color(0xFFF59E0B)

@Composable
fun UserFormDialog(
    isEdit: Boolean,
    form: UserFormState,
    roles: List<RoleListItemDto>,
    departments: List<OrganizationDto>,
    isSubmitting: Boolean,
    onFormChange: ((UserFormState) -> UserFormState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.94f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "dialogScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(200),
        label = "dialogAlpha",
    )

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(DIALOG_WIDTH_FRACTION)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .shadow(14.dp, SystemUserTheme.shapePage, spotColor = SystemUserTheme.ShadowSoft),
            shape = SystemUserTheme.shapePage,
            color = SystemUserTheme.CardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.Border),
        ) {
            Column {
                FormDialogHeader(isEdit = isEdit)
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    FormSection(
                        title = "基本情報",
                        accent = SectionBasic,
                        icon = Icons.Default.Person,
                        delayIndex = 0,
                    ) {
                        UserFormField(
                            label = "ユーザー名",
                            value = form.username,
                            enabled = !isEdit,
                            icon = Icons.Default.Person,
                            accent = SectionBasic,
                            onValueChange = { onFormChange { f -> f.copy(username = it) } },
                        )
                        UserFormField(
                            label = "氏名",
                            value = form.fullName,
                            icon = Icons.Default.PersonOutline,
                            accent = SectionBasic,
                            onValueChange = { onFormChange { f -> f.copy(fullName = it) } },
                        )
                        UserFormField(
                            label = "メール",
                            value = form.email,
                            icon = Icons.Default.Email,
                            accent = SectionBasic,
                            onValueChange = { onFormChange { f -> f.copy(email = it) } },
                        )
                    }
                    FormSection(
                        title = "所属・権限",
                        accent = SectionAccess,
                        icon = Icons.Default.Key,
                        delayIndex = 1,
                    ) {
                        UserDropdownField(
                            label = "部門",
                            value = departments.find { it.id == form.departmentId }?.name ?: "未選択",
                            options = listOf(null to "未選択") + departments.map { it.id to it.name },
                            accent = SectionAccess,
                            onSelect = { id -> onFormChange { f -> f.copy(departmentId = id) } },
                        )
                        UserDropdownField(
                            label = "ロール",
                            value = roles.find { it.id == form.roleId }?.name ?: "選択",
                            options = roles.map { it.id to it.name },
                            accent = SectionAccess,
                            onSelect = { id -> onFormChange { f -> f.copy(roleId = id) } },
                        )
                    }
                    FormSection(
                        title = "セキュリティ",
                        accent = SectionSecurity,
                        icon = Icons.Default.Shield,
                        delayIndex = 2,
                    ) {
                        TwoFactorRow(
                            enabled = form.twoFactorEnabled,
                            onChange = { checked -> onFormChange { f -> f.copy(twoFactorEnabled = checked) } },
                        )
                        if (!isEdit) {
                            UserFormField(
                                label = "パスワード",
                                value = form.password,
                                isPassword = true,
                                icon = Icons.Default.Lock,
                                accent = SectionSecurity,
                                onValueChange = { onFormChange { f -> f.copy(password = it) } },
                            )
                        }
                    }
                }
                HorizontalDivider(color = SystemUserTheme.Border, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DialogTextButton(text = "キャンセル", onClick = onDismiss, enabled = !isSubmitting)
                    DialogPrimaryButton(
                        text = if (isEdit) "更新" else "登録",
                        isSubmitting = isSubmitting,
                        onClick = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun FormDialogHeader(isEdit: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SystemUserTheme.headerGradient)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopEnd)
                .offset(x = 12.dp, y = (-18).dp)
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)),
                ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(SystemUserTheme.shapeChip)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), SystemUserTheme.shapeChip),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isEdit) Icons.Default.PersonOutline else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column {
                Text(
                    if (isEdit) "ユーザー編集" else "ユーザー新規登録",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                )
                Text(
                    if (isEdit) "情報を更新" else "アカウント作成",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    accent: Color,
    icon: ImageVector,
    delayIndex: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((delayIndex * 50).toLong())
        visible = true
    }
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 8f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "sectionOffset",
    )
    val sectionAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200),
        label = "sectionAlpha",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY
                alpha = sectionAlpha
            }
            .shadow(2.dp, SystemUserTheme.shapeChip, spotColor = Color(0x08000000)),
        shape = SystemUserTheme.shapeChip,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.BorderLight),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp, end = 7.dp, top = 5.dp, bottom = 5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
                    Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                }
                content()
            }
        }
    }
}

@Composable
private fun TwoFactorRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(
        targetValue = if (enabled) SectionBasic else SystemUserTheme.Border,
        animationSpec = tween(180),
        label = "2faTrack",
    )
    Surface(
        shape = SystemUserTheme.shapeChip,
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("2FA", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SystemUserTheme.TextPrimary)
                Text("追加認証", fontSize = 8.sp, color = SystemUserTheme.TextMuted, lineHeight = 9.sp)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onChange,
                modifier = Modifier.scale(0.78f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = trackColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = trackColor,
                ),
            )
        }
    }
}

@Composable
private fun UserFormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    icon: ImageVector? = null,
    accent: Color = SectionBasic,
    onValueChange: (String) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (focused) accent else SystemUserTheme.Border,
        animationSpec = tween(160),
        label = "fieldBorder",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = SystemUserTheme.TextSecondary)
        Surface(
            shape = SystemUserTheme.shapeChip,
            color = if (enabled) Color.White else Color(0xFFF1F5F9),
            shadowElevation = if (focused) 3.dp else 0.5.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = accent.copy(alpha = 0.9f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = SystemUserTheme.TextPrimary),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    cursorBrush = SolidColor(accent),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDropdownField(
    label: String,
    value: String,
    options: List<Pair<Int?, String>>,
    modifier: Modifier = Modifier,
    accent: Color = SectionAccess,
    onSelect: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (expanded) accent else SystemUserTheme.Border,
        animationSpec = tween(160),
        label = "dropdownBorder",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = SystemUserTheme.TextSecondary)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = SystemUserTheme.shapeChip,
                color = Color.White,
                shadowElevation = if (expanded) 3.dp else 0.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(value, modifier = Modifier.weight(1f), fontSize = 10.sp, maxLines = 1)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name, fontSize = 11.sp) },
                        onClick = { onSelect(id); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DialogTextButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = SystemUserTheme.shapeChip,
        color = Color.Transparent,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            color = SystemUserTheme.TextSecondary,
        )
    }
}

@Composable
internal fun DialogPrimaryButton(text: String, isSubmitting: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = !isSubmitting,
        shape = SystemUserTheme.shapeChip,
        color = Color.Transparent,
        shadowElevation = 3.dp,
        modifier = Modifier.padding(start = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(SystemUserTheme.primaryButtonGradient)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
