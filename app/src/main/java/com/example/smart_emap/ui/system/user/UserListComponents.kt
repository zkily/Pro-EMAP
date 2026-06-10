package com.example.smart_emap.ui.system.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OrganizationDto
import com.example.smart_emap.data.model.UserListItemDto

@Composable
fun UserPageBackground() {
    Box(modifier = Modifier.fillMaxSize().background(SystemUserTheme.pageGradient))
}

@Composable
fun UserListPageCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, SystemUserTheme.shapePage, spotColor = Color(0x14000000)),
        shape = SystemUserTheme.shapePage,
        colors = CardDefaults.cardColors(containerColor = SystemUserTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun UserErrorBanner(message: String) {
    Text(
        message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        color = SystemUserTheme.Error,
        fontSize = 12.sp,
    )
}

@Composable
fun UserListHeroBar(
    total: Int,
    activeCount: Int,
    lockedCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SystemUserTheme.shapeHeader)
            .background(SystemUserTheme.headerGradient)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(SystemUserTheme.shapeInput)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("ユーザー管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "アカウント管理・権限設定・セキュリティ",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(SystemUserTheme.shapeInput)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderStat("$total", "総ユーザー", Color.White)
                HeaderStatDivider()
                HeaderStat("$activeCount", "有効", SystemUserTheme.StatActive)
                HeaderStatDivider()
                HeaderStat("$lockedCount", "ロック中", SystemUserTheme.StatLocked)
            }
        }
    }
}

@Composable
private fun HeaderStatDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 28.dp)
            .background(Color.White.copy(alpha = 0.22f)),
    )
}

@Composable
private fun HeaderStat(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 9.sp)
    }
}

@Composable
fun UserListFilterSection(
    keyword: String,
    departmentId: Int?,
    statusFilter: String,
    departments: List<OrganizationDto>,
    canCreate: Boolean,
    canExport: Boolean,
    onKeywordChange: (String) -> Unit,
    onDepartmentChange: (Int?) -> Unit,
    onStatusChange: (String) -> Unit,
    onAdd: () -> Unit,
    onPrint: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            FilterSearchField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1.1f),
            )
            UserFilterDropdown(
                modifier = Modifier.weight(0.85f),
                label = "部門",
                value = departments.find { it.id == departmentId }?.name ?: "すべて",
                options = listOf(null to "すべて") + departments.map { it.id.toString() to it.name },
                onSelect = { onDepartmentChange(it?.toIntOrNull()) },
            )
            UserFilterDropdown(
                modifier = Modifier.weight(0.75f),
                label = "ステータス",
                value = when (statusFilter) {
                    "active" -> "有効"
                    "locked" -> "ロック"
                    else -> "すべて"
                },
                options = listOf("" to "すべて", "active" to "有効", "locked" to "ロック"),
                onSelect = { onStatusChange(it.orEmpty()) },
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            if (canCreate) {
                PrimaryToolbarButton(text = "新規登録", icon = Icons.Default.Add, onClick = onAdd)
            }
            if (canExport) {
                PrintToolbarButton(onClick = onPrint)
            }
        }
    }
}

@Composable
private fun FilterSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FilterLabel("キーワード")
        Surface(
            shape = SystemUserTheme.shapeInput,
            color = Color.White,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.BorderLight),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = SystemUserTheme.TextMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = SystemUserTheme.TextPrimary),
                    cursorBrush = SolidColor(SystemUserTheme.PrimaryStart),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text("ユーザー名・氏名", fontSize = 12.sp, color = SystemUserTheme.TextMuted)
                        }
                        inner()
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(
        text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = SystemUserTheme.TextSecondary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun PrimaryToolbarButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = SystemUserTheme.shapeInput,
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .background(SystemUserTheme.primaryButtonGradient)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PrintToolbarButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = SystemUserTheme.shapeInput,
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .background(SystemUserTheme.printButtonGradient)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text("印刷", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFilterDropdown(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    options: List<Pair<String?, String>>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        FilterLabel(label)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = SystemUserTheme.shapeInput,
                color = Color.White,
                shadowElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (expanded) SystemUserTheme.BorderFocus else SystemUserTheme.BorderLight,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(value, modifier = Modifier.weight(1f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name, fontSize = 12.sp) },
                        onClick = { onSelect(id); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
fun UserListTable(
    users: List<UserListItemDto>,
    isLoading: Boolean,
    canEdit: Boolean,
    currentUserId: Int,
    onEdit: (UserListItemDto) -> Unit,
    onToggleLock: (UserListItemDto) -> Unit,
    onResetPassword: (UserListItemDto) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SystemUserTheme.TableHeaderBg)
                .horizontalScroll(scroll)
                .padding(vertical = 9.dp, horizontal = 6.dp),
        ) {
            tableColumns(canEdit).forEach { (label, w) ->
                Box(Modifier.width(w.dp), contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SystemUserTheme.TableHeaderText)
                }
            }
        }
        HorizontalDivider(color = SystemUserTheme.Border, thickness = 2.dp)
        when {
            isLoading && users.isEmpty() -> Box(
                Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SystemUserTheme.PrimaryStart, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
            }
            users.isEmpty() -> Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = SystemUserTheme.TextMuted, modifier = Modifier.size(28.dp))
                Text("ユーザーが見つかりません", fontSize = 12.sp, color = SystemUserTheme.TextSecondary)
            }
            else -> users.forEachIndexed { index, user ->
                UserListTableRow(
                    user = user,
                    index = index,
                    scroll = scroll,
                    canEdit = canEdit,
                    currentUserId = currentUserId,
                    onEdit = onEdit,
                    onToggleLock = onToggleLock,
                    onResetPassword = onResetPassword,
                )
            }
        }
    }
}

private fun tableColumns(canEdit: Boolean): List<Pair<String, Int>> {
    val cols = mutableListOf(
        "ID" to 44,
        "ユーザー名" to 120,
        "氏名" to 76,
        "メール" to 150,
        "部門" to 90,
        "ロール" to 88,
        "ステータス" to 72,
        "2FA" to 44,
        "最終ログイン" to 112,
    )
    if (canEdit) cols.add("操作" to 100)
    return cols
}

@Composable
private fun UserListTableRow(
    user: UserListItemDto,
    index: Int,
    scroll: androidx.compose.foundation.ScrollState,
    canEdit: Boolean,
    currentUserId: Int,
    onEdit: (UserListItemDto) -> Unit,
    onToggleLock: (UserListItemDto) -> Unit,
    onResetPassword: (UserListItemDto) -> Unit,
) {
    val isLocked = user.status == "locked"
    val rowBg = when {
        isLocked -> SystemUserTheme.LockedRowBg
        index % 2 == 1 -> SystemUserTheme.RowAlt
        else -> Color.White
    }
    val (roleBg, roleFg) = roleTagColors(user.role)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .horizontalScroll(scroll)
            .padding(vertical = 7.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Text(
                "${user.id ?: ""}",
                modifier = Modifier
                    .clip(SystemUserTheme.shapeChip)
                    .background(SystemUserTheme.IdBadgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SystemUserTheme.TextSecondary,
            )
        }
        Box(Modifier.width(120.dp), contentAlignment = Alignment.CenterStart) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                UserAvatarChip(user.username)
                Text(user.username.orEmpty(), fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
            Text(user.fullName.orEmpty().ifEmpty { "—" }, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.width(150.dp), contentAlignment = Alignment.CenterStart) {
            Text(user.email.orEmpty(), fontSize = 10.sp, color = SystemUserTheme.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.width(90.dp), contentAlignment = Alignment.Center) {
            DeptBadge(user.department)
        }
        Box(Modifier.width(88.dp), contentAlignment = Alignment.Center) {
            RoleBadge(roleLabel(user.role), roleBg, roleFg)
        }
        Box(Modifier.width(72.dp), contentAlignment = Alignment.Center) {
            StatusIndicator(isLocked)
        }
        Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            TwoFaIndicator(user.twoFactor == true)
        }
        Box(Modifier.width(112.dp), contentAlignment = Alignment.Center) {
            Text(user.lastLogin.orEmpty().ifEmpty { "—" }, fontSize = 9.sp, color = SystemUserTheme.TextSecondary, maxLines = 1)
        }
        if (canEdit) {
            Row(
                Modifier.width(100.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledActionButton(Icons.Default.Edit, SystemUserTheme.BtnPrimary) { onEdit(user) }
                Spacer(Modifier.width(4.dp))
                val canLock = isLocked || user.id != currentUserId
                FilledActionButton(
                    if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    if (isLocked) SystemUserTheme.ActiveGreen else SystemUserTheme.BtnWarning,
                    enabled = canLock,
                ) { onToggleLock(user) }
                Spacer(Modifier.width(4.dp))
                FilledActionButton(Icons.Default.Key, SystemUserTheme.BtnInfo) { onResetPassword(user) }
            }
        }
    }
    HorizontalDivider(color = SystemUserTheme.BorderLight, thickness = 1.dp)
}

@Composable
private fun UserAvatarChip(username: String?) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(SystemUserTheme.shapeAvatar)
            .background(avatarGradientFor(username)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            username?.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DeptBadge(text: String?) {
    if (text.isNullOrBlank()) {
        Text("—", fontSize = 10.sp, color = SystemUserTheme.TextMuted)
    } else {
        Text(
            text,
            modifier = Modifier
                .clip(SystemUserTheme.shapeChip)
                .background(SystemUserTheme.DeptBadgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = SystemUserTheme.DeptBadgeText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RoleBadge(text: String, bg: Color, fg: Color) {
    Text(
        text,
        modifier = Modifier
            .clip(SystemUserTheme.shapeChip)
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StatusIndicator(isLocked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isLocked) SystemUserTheme.LockedDot else SystemUserTheme.ActiveDot),
        )
        Text(
            if (isLocked) "ロック" else "有効",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isLocked) SystemUserTheme.LockedOrange else SystemUserTheme.ActiveGreen,
        )
    }
}

@Composable
private fun TwoFaIndicator(enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(SystemUserTheme.shapeChip)
            .background(if (enabled) SystemUserTheme.TwoFaOnBg else SystemUserTheme.TwoFaOffBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (enabled) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (enabled) SystemUserTheme.TwoFaOnIcon else SystemUserTheme.TextMuted,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun FilledActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(28.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun UserListPagination(
    page: Int,
    pageSize: Int,
    total: Int,
    shown: Int,
    pages: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val safePages = pages.coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SystemUserTheme.FooterBg)
            .border(width = 0.dp, color = Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("表示: $shown / $total 件", fontSize = 11.sp, color = SystemUserTheme.TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PageSizeDropdown(pageSize = pageSize, onPageSizeChange = onPageSizeChange)
            PageNavButton(enabled = page > 1, icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onPageChange(page - 1) }
            val window = rememberPageWindow(page, safePages)
            window.forEach { p ->
                PageNumberButton(page = p, selected = p == page) { onPageChange(p) }
            }
            PageNavButton(enabled = page < safePages, icon = Icons.AutoMirrored.Filled.KeyboardArrowRight) { onPageChange(page + 1) }
        }
    }
}

private fun rememberPageWindow(current: Int, total: Int): List<Int> {
    if (total <= 5) return (1..total).toList()
    val start = (current - 2).coerceAtLeast(1)
    val end = (start + 4).coerceAtMost(total)
    return (start..end).toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageSizeDropdown(pageSize: Int, onPageSizeChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = SystemUserTheme.shapeChip,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.Border),
        ) {
            Text(
                "${pageSize}件/ページ",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                fontSize = 10.sp,
                color = SystemUserTheme.TextSecondary,
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(10, 20, 50, 100).forEach { size ->
                DropdownMenuItem(
                    text = { Text("${size}件/ページ", fontSize = 12.sp) },
                    onClick = { onPageSizeChange(size); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun PageNumberButton(page: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = SystemUserTheme.shapeChip,
        color = if (selected) SystemUserTheme.BtnPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) SystemUserTheme.BtnPrimary else SystemUserTheme.Border),
        modifier = Modifier.size(width = 28.dp, height = 26.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "$page",
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else SystemUserTheme.TextSecondary,
            )
        }
    }
}

@Composable
private fun PageNavButton(
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = SystemUserTheme.shapeChip,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SystemUserTheme.Border),
        modifier = Modifier.size(width = 28.dp, height = 26.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) SystemUserTheme.TextPrimary else SystemUserTheme.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
