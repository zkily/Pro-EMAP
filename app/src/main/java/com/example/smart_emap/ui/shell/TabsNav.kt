package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabsNav(
    tabs: List<ShellTab>,
    activePath: String,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onRefresh: () -> Unit,
    onCloseOthers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(LayoutColors.TabsBg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val isActive = tab.path == activePath
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) Color.White else Color(0xFFE2E8F0).copy(alpha = 0.6f))
                        .clickable { onTabSelected(tab.path) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (tab.path == "/dashboard") Icons.Default.Home else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (isActive) LayoutColors.TabActive else LayoutColors.TabText,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = tab.title,
                        color = if (isActive) LayoutColors.TabTextActive else LayoutColors.TabText,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                    if (tab.closable) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = LayoutColors.TabText,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onTabClosed(tab.path) },
                        )
                    }
                }
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "タブ操作", tint = LayoutColors.TabText, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("更新") },
                    onClick = {
                        menuExpanded = false
                        onRefresh()
                    },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = { Text("他を閉じる") },
                    onClick = {
                        menuExpanded = false
                        onCloseOthers()
                    },
                )
            }
        }
    }
}
