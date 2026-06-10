package com.example.smart_emap.ui.system.organization

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OrganizationDetailDto
import com.example.smart_emap.data.model.OrganizationTreeNodeDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.system.user.avatarGradientFor
import com.example.smart_emap.ui.system.user.roleLabel

@Composable
fun OrgPageBackground() {
    Box(modifier = Modifier.fillMaxSize().background(OrganizationTheme.pageGradient))
}

@Composable
fun OrgListHeroBar(orgCount: Int, deptCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(8.dp, OrganizationTheme.shapePage, spotColor = Color(0x14000000)),
        shape = OrganizationTheme.shapePage,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrganizationTheme.headerGradient)
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
                            .clip(OrganizationTheme.shapeInput)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("組織・部門管理", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("会社・拠点・部門・課・ライン階層構造", color = Color.White.copy(alpha = 0.82f), fontSize = 10.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(OrganizationTheme.shapeInput)
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OrgStat("$orgCount", "総組織数", Color.White)
                    Box(Modifier.size(width = 1.dp, height = 24.dp).background(Color.White.copy(alpha = 0.22f)))
                    OrgStat("$deptCount", "部門", Color(0xFFA5F3FC))
                }
            }
        }
    }
}

@Composable
private fun OrgStat(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 9.sp)
    }
}

@Composable
fun OrgTwoColumnLayout(
    treeContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.weight(0.38f).fillMaxHeight()) { treeContent() }
        Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) { detailContent() }
    }
}

@Composable
fun OrgTreePanel(
    isLoading: Boolean,
    orgTree: List<OrganizationTreeNodeDto>,
    selectedId: Int?,
    expandedIds: Set<Int>,
    canCreate: Boolean,
    onAdd: () -> Unit,
    onToggleExpand: (Int) -> Unit,
    onSelect: (OrganizationTreeNodeDto) -> Unit,
    onEdit: (OrganizationTreeNodeDto) -> Unit,
) {
    OrgPanelCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrganizationTheme.PanelHeaderBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(16.dp))
                Text("組織ツリー", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextPrimary)
            }
            if (canCreate) {
                Surface(
                    onClick = onAdd,
                    shape = OrganizationTheme.shapeChip,
                    color = Color.Transparent,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .background(OrganizationTheme.primaryGradient)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrganizationTheme.TreeHintBg)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = OrganizationTheme.TreeHintText, modifier = Modifier.size(12.dp))
            Text("タップで選択、長押しで編集", fontSize = 9.sp, color = OrganizationTheme.TreeHintText)
        }
        HorizontalDivider(color = OrganizationTheme.Border, thickness = 0.5.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(6.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF667EEA), strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
                orgTree.isEmpty() -> Text("組織がありません", fontSize = 11.sp, color = OrganizationTheme.TextMuted, modifier = Modifier.padding(12.dp))
                else -> Column {
                    orgTree.forEach { node ->
                        OrgTreeNodeItem(
                            node = node,
                            level = 0,
                            selectedId = selectedId,
                            expandedIds = expandedIds,
                            onToggleExpand = onToggleExpand,
                            onSelect = onSelect,
                            onEdit = onEdit,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrgTreeNodeItem(
    node: OrganizationTreeNodeDto,
    level: Int,
    selectedId: Int?,
    expandedIds: Set<Int>,
    onToggleExpand: (Int) -> Unit,
    onSelect: (OrganizationTreeNodeDto) -> Unit,
    onEdit: (OrganizationTreeNodeDto) -> Unit,
) {
    val hasChildren = node.children.isNotEmpty()
    val expanded = expandedIds.contains(node.id)
    val selected = selectedId == node.id
    val (badgeBg, badgeFg) = orgTypeBadgeColors(node.type)
  Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 10).dp, top = 1.dp, bottom = 1.dp)
            .clip(OrganizationTheme.shapeChip)
            .background(if (selected) OrganizationTheme.TreeSelectedBg else Color.Transparent)
            .combinedClickable(
                onClick = { onSelect(node) },
                onLongClick = { onEdit(node) },
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasChildren) {
            Surface(
                onClick = { onToggleExpand(node.id) },
                shape = OrganizationTheme.shapeChip,
                color = Color.Transparent,
                modifier = Modifier.size(18.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = OrganizationTheme.TextMuted,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        } else {
            Spacer(Modifier.width(18.dp))
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(OrganizationTheme.shapeChip)
                .background(orgTypeIconGradient(node.type)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(orgTypeIcon(node.type), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            node.name,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = OrganizationTheme.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            orgTypeLabel(node.type),
            modifier = Modifier
                .clip(OrganizationTheme.shapeChip)
                .background(badgeBg)
                .padding(horizontal = 5.dp, vertical = 1.dp),
            fontSize = 8.sp,
            color = badgeFg,
        )
    }
    if (hasChildren && expanded) {
        node.children.forEach { child ->
            OrgTreeNodeItem(
                node = child,
                level = level + 1,
                selectedId = selectedId,
                expandedIds = expandedIds,
                onToggleExpand = onToggleExpand,
                onSelect = onSelect,
                onEdit = onEdit,
            )
        }
    }
}

private fun orgTypeIcon(type: String): ImageVector = when (type.lowercase()) {
    "company" -> Icons.Default.Business
    "site" -> Icons.Default.Home
    "department" -> Icons.Default.GridView
    "section" -> Icons.Default.Folder
    "line" -> Icons.Default.Settings
    else -> Icons.Default.Business
}

@Composable
fun OrgDetailPanel(
    selectedOrg: OrganizationDetailDto?,
    parentName: String?,
    orgUsers: List<UserListItemDto>,
    isLoading: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    OrgPanelCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrganizationTheme.PanelHeaderBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
                    Text(
                        selectedOrg?.name ?: "組織を選択",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedOrg != null) OrganizationTheme.TextPrimary else OrganizationTheme.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (selectedOrg != null) {
                    Text("— 詳細情報", fontSize = 9.sp, color = OrganizationTheme.TextSecondary)
                }
            }
            if (selectedOrg != null && canEdit) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallActionButton(Icons.Default.Edit, Color(0xFF409EFF), onEdit)
                    if (canDelete) {
                        SmallActionButton(Icons.Default.Delete, Color(0xFFF56C6C), onDelete)
                    }
                }
            }
        }
        HorizontalDivider(color = OrganizationTheme.Border, thickness = 0.5.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF667EEA), strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                }
                selectedOrg == null -> OrgEmptyState()
                else -> OrgDetailContent(selectedOrg, parentName, orgUsers)
            }
        }
    }
}

@Composable
private fun OrgTypeInfoCard(type: String, modifier: Modifier = Modifier) {
    val (badgeBg, badgeFg) = orgTypeBadgeColors(type)
    Surface(
        modifier = modifier,
        shape = OrganizationTheme.shapeInput,
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.BorderLight),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(OrganizationTheme.shapeChip)
                    .background(Color(0xFF0EA5E9).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(orgTypeIcon(type), contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(14.dp))
            }
            Column {
                Text("種類", fontSize = 8.sp, color = OrganizationTheme.TextMuted)
                Text(
                    orgTypeLabel(type),
                    modifier = Modifier
                        .clip(OrganizationTheme.shapeChip)
                        .background(badgeBg)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    fontSize = 9.sp,
                    color = badgeFg,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun OrgPanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, OrganizationTheme.shapePanel, spotColor = Color(0x0A000000)),
        shape = OrganizationTheme.shapePanel,
        colors = CardDefaults.cardColors(containerColor = OrganizationTheme.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.BorderLight),
    ) {
        Column(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
private fun OrgEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Default.TouchApp, contentDescription = null, tint = OrganizationTheme.TextMuted, modifier = Modifier.size(40.dp))
        Text("組織を選択してください", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextPrimary)
        Text(
            "左側のツリーから組織を選択すると、詳細情報が表示されます",
            fontSize = 10.sp,
            color = OrganizationTheme.TextSecondary,
        )
    }
}

@Composable
private fun OrgDetailContent(
    org: OrganizationDetailDto,
    parentName: String?,
    orgUsers: List<UserListItemDto>,
) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OrgInfoCard(Icons.Default.TouchApp, "組織コード", org.code, Color(0xFF667EEA), Modifier.weight(1f))
            OrgTypeInfoCard(org.type, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OrgInfoCard(Icons.Default.Share, "親組織", parentName ?: "—", Color(0xFF8B5CF6), Modifier.weight(1f))
            OrgInfoCard(Icons.Default.Person, "責任者", org.managerName ?: "—", Color(0xFFF59E0B), Modifier.weight(1f))
        }
        OrgDetailRow(Icons.Default.LocationOn, "所在地", org.location)
        OrgDetailRow(Icons.Default.Phone, "電話番号", org.phone)
        OrgDetailRow(Icons.Default.Email, "メール", org.email)
        if (!org.description.isNullOrBlank()) {
            OrgDetailRow(Icons.Default.Info, "説明", org.description)
        }
        OrgMembersSection(orgUsers)
    }
}

@Composable
private fun OrgInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = OrganizationTheme.shapeInput,
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.BorderLight),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(OrganizationTheme.shapeChip)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            }
            Column {
                Text(label, fontSize = 8.sp, color = OrganizationTheme.TextMuted)
                Text(value, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun OrgDetailRow(icon: ImageVector, label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrganizationTheme.shapeChip)
            .background(Color(0xFFFCFDFF))
            .border(1.dp, OrganizationTheme.BorderLight, OrganizationTheme.shapeChip)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = OrganizationTheme.TextMuted, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 9.sp, color = OrganizationTheme.TextSecondary, modifier = Modifier.width(52.dp))
        Text(value.orEmpty().ifEmpty { "—" }, fontSize = 10.sp, color = OrganizationTheme.TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OrgMembersSection(users: List<UserListItemDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OrganizationTheme.shapeInput)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, OrganizationTheme.BorderLight, OrganizationTheme.shapeInput)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF667EEA), modifier = Modifier.size(14.dp))
            Text("所属ユーザー", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextPrimary)
            Text(
                "${users.size}名",
                modifier = Modifier
                    .clip(OrganizationTheme.shapeChip)
                    .background(Color(0xFFE0E7FF))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                fontSize = 9.sp,
                color = Color(0xFF4F46E5),
            )
        }
        if (users.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = OrganizationTheme.TextMuted, modifier = Modifier.size(28.dp))
                Text("所属ユーザーはいません", fontSize = 10.sp, color = OrganizationTheme.TextMuted)
            }
        } else {
            users.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OrganizationTheme.shapeChip)
                        .background(Color.White)
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(OrganizationTheme.shapeChip)
                            .background(avatarGradientFor(user.username)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            user.username?.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.username.orEmpty(), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text(user.fullName.orEmpty(), fontSize = 8.sp, color = OrganizationTheme.TextMuted)
                    }
                    Text(
                        roleLabel(user.role),
                        modifier = Modifier
                            .clip(OrganizationTheme.shapeChip)
                            .background(Color(0xFFDBEAFE))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        fontSize = 8.sp,
                        color = Color(0xFF2563EB),
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallActionButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = OrganizationTheme.shapeChip, color = color, modifier = Modifier.size(28.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
