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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
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
                when (node) {
                    is AppMenuNode.Leaf -> SidebarLeafItem(
                        label = node.label,
                        icon = node.icon,
                        isHome = node.isHome,
                        isCollapsed = isCollapsed,
                        isActive = activePath == node.path,
                        onClick = { onNavigate(node.path) },
                    )
                    is AppMenuNode.Group -> {
                        val expanded = expandedGroups[node.code] == true
                        SidebarGroupItem(
                            label = node.label,
                            icon = node.icon,
                            isCollapsed = isCollapsed,
                            expanded = expanded,
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
                                    .padding(start = 8.dp)
                                    .animateContentSize(),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                node.children.forEach { child ->
                                    if (child is AppMenuNode.Leaf) {
                                        SidebarLeafItem(
                                            label = child.label,
                                            icon = child.icon,
                                            isHome = child.isHome,
                                            isCollapsed = false,
                                            isActive = activePath == child.path,
                                            isNested = true,
                                            onClick = { onNavigate(child.path) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = if (isCollapsed) 0.dp else 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = LayoutColors.SidebarTextActive, modifier = Modifier.size(18.dp))
        if (!isCollapsed) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = LayoutColors.SidebarTextActive,
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
    isNested: Boolean = false,
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
                horizontal = if (isCollapsed) 0.dp else if (isNested) 8.dp else 10.dp,
                vertical = if (isNested) 8.dp else 10.dp,
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
                fontSize = if (isNested) 13.sp else 14.sp,
                fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
