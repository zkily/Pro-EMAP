package com.example.smart_emap.ui.system.role

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OperationPermissionDto
import com.example.smart_emap.data.model.RoleDetailDto
import com.example.smart_emap.data.model.RoleListItemDto

@Composable
fun RolePageBackground() {
    Box(modifier = Modifier.fillMaxSize().background(RolePermissionTheme.pageGradient))
}

@Composable
fun RoleHeroBar(roleCount: Int, totalUsers: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(8.dp, RolePermissionTheme.shapePage, spotColor = Color(0x14000000)),
        shape = RolePermissionTheme.shapePage,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RolePermissionTheme.headerGradient)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RolePermissionTheme.shapeInput)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("権限・ロール管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("ロール別メニュー・操作・データ範囲の設定", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RolePermissionTheme.shapeInput)
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RoleStat("$roleCount", "ロール数", Color.White)
                    Box(Modifier.size(width = 1.dp, height = 24.dp).background(Color.White.copy(alpha = 0.22f)))
                    RoleStat("$totalUsers", "総ユーザー", Color(0xFFA5F3FC))
                }
            }
        }
    }
}

@Composable
private fun RoleStat(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 9.sp)
    }
}

@Composable
fun RoleTwoColumnLayout(
    roleListContent: @Composable () -> Unit,
    permissionContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.weight(0.38f).fillMaxHeight()) { roleListContent() }
        Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) { permissionContent() }
    }
}

@Composable
private fun RolePanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, RolePermissionTheme.shapePanel, spotColor = Color(0x0A000000)),
        shape = RolePermissionTheme.shapePanel,
        colors = CardDefaults.cardColors(containerColor = RolePermissionTheme.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, RolePermissionTheme.BorderLight),
    ) {
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun RoleListPanel(
    isLoading: Boolean,
    roles: List<RoleListItemDto>,
    selectedId: Int?,
    canCreate: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onAdd: () -> Unit,
    onSelect: (RoleListItemDto) -> Unit,
    onEdit: (RoleListItemDto) -> Unit,
    onDelete: (RoleListItemDto) -> Unit,
) {
    RolePanelCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RolePermissionTheme.PanelHeaderBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                Text("ロール一覧", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextPrimary)
            }
            if (canCreate) {
                Surface(
                    onClick = onAdd,
                    shape = RolePermissionTheme.shapeChip,
                    color = Color.Transparent,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .background(RolePermissionTheme.primaryGradient)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("追加", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = Color(0xFF667EEA))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(roles, key = { it.id }) { role ->
                    RoleListRow(
                        role = role,
                        selected = role.id == selectedId,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        onSelect = { onSelect(role) },
                        onEdit = { onEdit(role) },
                        onDelete = { onDelete(role) },
                    )
                    HorizontalDivider(color = RolePermissionTheme.BorderLight, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun RoleListRow(
    role: RoleListItemDto,
    selected: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val bg = if (selected) RolePermissionTheme.RowSelectedBg else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RolePermissionTheme.shapeChip)
                .background(roleColorFor(role.name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(role.name.firstOrNull()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            Text(role.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (role.isSystem) {
                Text("システム", fontSize = 9.sp, color = Color(0xFF7C3AED))
            }
        }
        Box(
            modifier = Modifier
                .clip(RolePermissionTheme.shapeChip)
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text("${role.userCount}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextSecondary)
        }
        if (canEdit) {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "編集", tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
            }
        }
        if (canDelete) {
            IconButton(
                onClick = onDelete,
                enabled = !role.isSystem,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = if (role.isSystem) Color(0xFFCBD5E1) else Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
fun RolePermissionPanel(
    selectedRole: RoleDetailDto?,
    isDetailLoading: Boolean,
    isSaving: Boolean,
    activeTab: Int,
    permissionMenuTree: List<PermissionMenuNode>,
    isMenuLoading: Boolean,
    expandedMenuCodes: Set<String>,
    checkedMenuCodes: Set<String>,
    operationPermissions: List<OperationPermissionDto>,
    dataScope: String,
    customDepartments: List<String>,
    departmentOptions: List<Pair<Int, String>>,
    canEdit: Boolean,
    onTabChange: (Int) -> Unit,
    onSave: () -> Unit,
    onToggleMenuExpand: (String) -> Unit,
    onToggleMenuCheck: (String, Boolean) -> Unit,
    onOperationChange: (String, String, Boolean) -> Unit,
    onDataScopeChange: (String) -> Unit,
    onToggleCustomDept: (String) -> Unit,
) {
    RolePanelCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RolePermissionTheme.PanelHeaderBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                    Text(
                        selectedRole?.name ?: "ロールを選択",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedRole != null) RolePermissionTheme.TextPrimary else RolePermissionTheme.TextMuted,
                    )
                }
                if (selectedRole != null) {
                    Text("権限設定", fontSize = 9.sp, color = RolePermissionTheme.TextMuted)
                }
            }
            if (selectedRole != null && canEdit) {
                Surface(
                    onClick = onSave,
                    enabled = !isSaving,
                    shape = RolePermissionTheme.shapeChip,
                    color = Color.Transparent,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .background(RolePermissionTheme.primaryGradient)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                        Text("保存", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        when {
            isDetailLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = Color(0xFF667EEA))
                }
            }
            selectedRole == null -> RoleEmptyState()
            else -> {
                val tabs = listOf("メニュー権限", "操作権限", "データ範囲")
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFF667EEA),
                        indicator = { positions ->
                            if (activeTab < positions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(positions[activeTab]),
                                    color = Color(0xFF667EEA),
                                    height = 2.dp,
                                )
                            }
                        },
                        divider = { HorizontalDivider(color = RolePermissionTheme.BorderLight) },
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = activeTab == index,
                                onClick = { onTabChange(index) },
                                text = {
                                    Text(title, fontSize = 10.sp, fontWeight = if (activeTab == index) FontWeight.SemiBold else FontWeight.Normal)
                                },
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    ) {
                        when (activeTab) {
                            0 -> MenuPermissionTab(
                                menuTree = permissionMenuTree,
                                isLoading = isMenuLoading,
                                expandedCodes = expandedMenuCodes,
                                checkedCodes = checkedMenuCodes,
                                enabled = canEdit,
                                onToggleExpand = onToggleMenuExpand,
                                onToggleCheck = onToggleMenuCheck,
                            )
                            1 -> OperationPermissionTab(
                                permissions = operationPermissions,
                                enabled = canEdit,
                                onChange = onOperationChange,
                            )
                            2 -> DataScopeTab(
                                dataScope = dataScope,
                                customDepartments = customDepartments,
                                departmentOptions = departmentOptions,
                                enabled = canEdit,
                                onScopeChange = onDataScopeChange,
                                onToggleDept = onToggleCustomDept,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RolePermissionTheme.shapePage)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("ロールを選択してください", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextPrimary)
        Text("左の一覧からロールを選び、権限を設定します", fontSize = 10.sp, color = RolePermissionTheme.TextMuted)
    }
}

@Composable
private fun MenuPermissionTab(
    menuTree: List<PermissionMenuNode>,
    isLoading: Boolean,
    expandedCodes: Set<String>,
    checkedCodes: Set<String>,
    enabled: Boolean,
    onToggleExpand: (String) -> Unit,
    onToggleCheck: (String, Boolean) -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF667EEA))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            menuTree.forEach { node ->
                MenuTreeNodeRow(
                    node = node,
                    depth = 0,
                    expandedCodes = expandedCodes,
                    checkedCodes = checkedCodes,
                    enabled = enabled,
                    onToggleExpand = onToggleExpand,
                    onToggleCheck = onToggleCheck,
                )
            }
        }
    }
}

@Composable
private fun MenuTreeNodeRow(
    node: PermissionMenuNode,
    depth: Int,
    expandedCodes: Set<String>,
    checkedCodes: Set<String>,
    enabled: Boolean,
    onToggleExpand: (String) -> Unit,
    onToggleCheck: (String, Boolean) -> Unit,
) {
    val hasChildren = node.children.isNotEmpty()
    val expanded = node.code in expandedCodes
    val checked = node.code in checkedCodes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 14).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasChildren) {
            IconButton(onClick = { onToggleExpand(node.code) }, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = RolePermissionTheme.TextMuted,
                )
            }
        } else {
            Spacer(Modifier.width(24.dp))
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onToggleCheck(node.code, it) },
            enabled = enabled,
            modifier = Modifier.size(32.dp),
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF667EEA)),
        )
        Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(node.label, fontSize = 11.sp, color = RolePermissionTheme.TextPrimary)
    }
    if (hasChildren && expanded) {
        node.children.forEach { child ->
            MenuTreeNodeRow(
                node = child,
                depth = depth + 1,
                expandedCodes = expandedCodes,
                checkedCodes = checkedCodes,
                enabled = enabled,
                onToggleExpand = onToggleExpand,
                onToggleCheck = onToggleCheck,
            )
        }
    }
}

@Composable
private fun OperationPermissionTab(
    permissions: List<OperationPermissionDto>,
    enabled: Boolean,
    onChange: (String, String, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RolePermissionTheme.shapeChip)
                .background(RolePermissionTheme.HintBg)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = RolePermissionTheme.HintText, modifier = Modifier.size(12.dp))
            Text("各モジュールの新規・編集・削除・出力・承認権限を設定", fontSize = 9.sp, color = RolePermissionTheme.HintText)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            Column {
                OperationTableHeader()
                permissions.forEach { op ->
                    OperationTableRow(op, enabled, onChange)
                    HorizontalDivider(color = RolePermissionTheme.BorderLight, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun OperationTableHeader() {
    Row(
        modifier = Modifier
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OpHeaderCell("モジュール", 100.dp)
        OpHeaderCell("新規", 52.dp)
        OpHeaderCell("編集", 52.dp)
        OpHeaderCell("削除", 52.dp)
        OpHeaderCell("出力", 52.dp)
        OpHeaderCell("承認", 52.dp)
    }
}

@Composable
private fun OpHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun OperationTableRow(
    op: OperationPermissionDto,
    enabled: Boolean,
    onChange: (String, String, Boolean) -> Unit,
) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            op.module,
            modifier = Modifier.width(100.dp).padding(horizontal = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = RolePermissionTheme.TextPrimary,
            maxLines = 2,
        )
        OpCheckCell(op.canCreate, enabled) { onChange(op.module, "create", it) }
        OpCheckCell(op.canEdit, enabled) { onChange(op.module, "edit", it) }
        OpCheckCell(op.canDelete, enabled) { onChange(op.module, "delete", it) }
        OpCheckCell(op.canExport, enabled) { onChange(op.module, "export", it) }
        OpCheckCell(op.canApprove, enabled) { onChange(op.module, "approve", it) }
    }
}

@Composable
private fun OpCheckCell(checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Box(Modifier.width(52.dp), contentAlignment = Alignment.Center) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onChange(it) },
            enabled = enabled,
            modifier = Modifier.size(28.dp),
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF667EEA)),
        )
    }
}

@Composable
private fun DataScopeTab(
    dataScope: String,
    customDepartments: List<String>,
    departmentOptions: List<Pair<Int, String>>,
    enabled: Boolean,
    onScopeChange: (String) -> Unit,
    onToggleDept: (String) -> Unit,
) {
    val options = listOf(
        DataScopeOption("self", "本人のみ", "自分のデータのみ", Icons.Default.Person),
        DataScopeOption("department", "所属部門", "同じ部門のデータ", Icons.Default.GridView),
        DataScopeOption("department_below", "部門以下", "配下部門を含む", Icons.Default.Home),
        DataScopeOption("all", "全社", "すべてのデータ", Icons.Default.Tune),
        DataScopeOption("custom", "カスタム", "部門を指定", Icons.Default.Settings),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("データ参照範囲", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextSecondary)
        options.forEach { opt ->
            val active = dataScope == opt.value
            Surface(
                onClick = { if (enabled) onScopeChange(opt.value) },
                enabled = enabled,
                shape = RolePermissionTheme.shapeInput,
                color = if (active) RolePermissionTheme.ScopeActiveBg else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (active) 1.5.dp else 1.dp,
                    color = if (active) RolePermissionTheme.ScopeActiveBorder else RolePermissionTheme.BorderLight,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(opt.icon, contentDescription = null, tint = if (active) Color(0xFF667EEA) else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(opt.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextPrimary)
                        Text(opt.desc, fontSize = 9.sp, color = RolePermissionTheme.TextMuted)
                    }
                }
            }
        }
        if (dataScope == "custom") {
            Text("カスタム部門", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RolePermissionTheme.TextSecondary)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RolePermissionTheme.shapeInput)
                    .border(1.dp, RolePermissionTheme.BorderLight, RolePermissionTheme.shapeInput)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (departmentOptions.isEmpty()) {
                    Text("部門データがありません", fontSize = 10.sp, color = RolePermissionTheme.TextMuted)
                } else {
                    departmentOptions.forEach { (_, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled) { onToggleDept(name) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = name in customDepartments,
                                onCheckedChange = { if (enabled) onToggleDept(name) },
                                enabled = enabled,
                                modifier = Modifier.size(28.dp),
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF667EEA)),
                            )
                            Text(name, fontSize = 11.sp, color = RolePermissionTheme.TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

private data class DataScopeOption(
    val value: String,
    val label: String,
    val desc: String,
    val icon: ImageVector,
)
