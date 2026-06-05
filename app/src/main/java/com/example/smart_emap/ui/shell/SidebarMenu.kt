package com.example.smart_emap.ui.shell

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.theme.LoginColors

@Composable
fun SidebarMenu(
    isCollapsed: Boolean,
    activePath: String,
    onNavigate: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    showCollapseControl: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(if (isCollapsed) 64.dp else 220.dp)
            .background(
                Brush.verticalGradient(
                    listOf(LayoutColors.SidebarStart, LayoutColors.SidebarMid, LayoutColors.SidebarEnd),
                ),
            ),
    ) {
        SidebarLogo(isCollapsed = isCollapsed, onClick = onToggleCollapse)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isCollapsed) 4.dp else 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppMenuConfig.rootMenus.forEach { node ->
                SidebarNode(
                    node = node,
                    depth = 0,
                    isCollapsed = isCollapsed,
                    activePath = activePath,
                    expandedGroups = expandedGroups,
                    onNavigate = onNavigate,
                    onToggleCollapse = onToggleCollapse,
                )
            }
        }

        if (showCollapseControl) {
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp,
            )
            SidebarCollapseFooter(
                isCollapsed = isCollapsed,
                onToggleCollapse = onToggleCollapse,
            )
        }
    }
}

/** Web SidebarMenu.vue `.collapse-btn`：底部折りたたむ / 展開 */
@Composable
private fun SidebarCollapseFooter(
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        LoginColors.Primary.copy(alpha = 0.1f),
                        LoginColors.PrimaryDark.copy(alpha = 0.08f),
                    ),
                ),
            )
            .clickable(onClick = onToggleCollapse)
            .padding(horizontal = if (isCollapsed) 0.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isCollapsed) {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowLeft
            },
            contentDescription = if (isCollapsed) "展開" else "折りたたむ",
            tint = LayoutColors.SidebarText,
            modifier = Modifier.size(18.dp),
        )
        if (!isCollapsed) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "折りたたむ",
                color = LayoutColors.SidebarText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SidebarNode(
    node: AppMenuNode,
    depth: Int,
    isCollapsed: Boolean,
    activePath: String,
    expandedGroups: MutableMap<String, Boolean>,
    onNavigate: (String) -> Unit,
    onToggleCollapse: () -> Unit,
) {
    when (node) {
        is AppMenuNode.Leaf -> SidebarLeafItem(
            label = node.label,
            icon = node.icon,
            isHome = node.isHome,
            isCollapsed = isCollapsed,
            isActive = activePath == node.path,
            depth = depth,
            onClick = { onNavigate(node.path) },
        )

        is AppMenuNode.Group -> {
            val activeInSubtree = isActiveInSubtree(node, activePath)
            // 当激活路由位于子树中时自动展开，避免用户看不到当前页对应的菜单层级。
            val expanded = expandedGroups[node.code] == true || activeInSubtree
            SidebarGroupItem(
                label = node.label,
                icon = node.icon,
                isCollapsed = isCollapsed,
                expanded = expanded,
                isActiveInSubtree = activeInSubtree,
                depth = depth,
                onToggle = {
                    if (isCollapsed) {
                        onToggleCollapse()
                        expandedGroups[node.code] = true
                    } else {
                        expandedGroups[node.code] = !expanded
                    }
                },
            )

            if (!isCollapsed && expanded) {
                Column(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    node.children.forEach { child ->
                        SidebarNode(
                            node = child,
                            depth = depth + 1,
                            isCollapsed = false,
                            activePath = activePath,
                            expandedGroups = expandedGroups,
                            onNavigate = onNavigate,
                            onToggleCollapse = onToggleCollapse,
                        )
                    }
                }
            }
        }
    }
}

private fun isActiveInSubtree(node: AppMenuNode, activePath: String): Boolean {
    return when (node) {
        is AppMenuNode.Leaf -> node.path == activePath
        is AppMenuNode.Group -> node.children.any { child -> isActiveInSubtree(child, activePath) }
    }
}

@Composable
private fun SidebarLogo(
    isCollapsed: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        LoginColors.Primary.copy(alpha = 0.2f),
                        LoginColors.PrimaryDark.copy(alpha = 0.15f),
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isCollapsed) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(LoginColors.Primary, LoginColors.PrimaryDark))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        } else {
            Text(
                text = "Smart-EMAP",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun SidebarGroupItem(
    label: String,
    icon: ImageVector,
    isCollapsed: Boolean,
    expanded: Boolean,
    isActiveInSubtree: Boolean,
    depth: Int,
    onToggle: () -> Unit,
) {
    val tint = if (isActiveInSubtree) LayoutColors.SidebarTextActive else LayoutColors.SidebarText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(
                horizontal = if (isCollapsed) 0.dp else (10 + depth * 8).dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        if (!isCollapsed) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = tint,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = LayoutColors.SidebarText,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SidebarLeafItem(
    label: String,
    icon: ImageVector,
    isHome: Boolean,
    isCollapsed: Boolean,
    isActive: Boolean,
    depth: Int = 0,
    onClick: () -> Unit,
) {
    val textColor = when {
        isActive -> LayoutColors.SidebarTextActive
        isHome -> LayoutColors.SidebarHome
        else -> LayoutColors.SidebarText
    }
    val bg = if (isActive) {
        Brush.linearGradient(listOf(LoginColors.Primary, LoginColors.PrimaryDark))
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (bg != null) Modifier.background(bg) else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isCollapsed) 0.dp else (10 + depth * 8).dp,
                vertical = if (depth > 0) 8.dp else 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
        if (!isCollapsed) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = if (depth > 0) 13.sp else 14.sp,
                fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
