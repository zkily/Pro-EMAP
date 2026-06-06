package com.example.smart_emap.ui.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.example.smart_emap.ui.theme.LoginColors
import kotlinx.coroutines.delay

private data class FlyoutLayer(
    val group: AppMenuNode.Group,
    /** 相对根弹出层顶部的垂直偏移，使子菜单与父项对齐 */
    val offsetFromRootPx: Int = 0,
)

private data class CollapsedFlyoutState(
    val stack: List<FlyoutLayer>,
    val rootAnchorTopPx: Int,
)

/** 折叠态：图标 + 底部短名称 */
private val CollapsedSidebarWidth = 80.dp
private val CollapsedSidebarLabelSize = 10.sp
private val ExpandedSidebarWidth = 220.dp
/** 与展开侧栏同宽，容纳日文菜单名 */
private val CollapsedFlyoutWidth = 220.dp
private val FlyoutEnterMs = 240
private val FlyoutExitMs = 180
private val FlyoutPanelShape = RoundedCornerShape(16.dp)
private val FlyoutListHorizontalPadding = 10.dp
private val FlyoutListTopPadding = 8.dp
/** 最后一项与底部圆角边框之间的留白 */
private val FlyoutListBottomPadding = 14.dp
private const val FlyoutVisibleItemCount = 5
private val FlyoutItemRowHeight = 44.dp
private val FlyoutItemSpacing = 4.dp
/** 列表区域最多显示 5 条菜单的高度 */
private val FlyoutListMaxViewportHeight =
    FlyoutListTopPadding +
        FlyoutItemRowHeight * FlyoutVisibleItemCount +
        FlyoutItemSpacing * (FlyoutVisibleItemCount - 1) +
        FlyoutListBottomPadding
private val FlyoutHeaderBlockHeight = 60.dp
private val FlyoutScreenBottomMargin = 16.dp
private val FlyoutMinListHeight = 88.dp

private fun flyoutIdealListHeight(itemCount: Int): Dp =
    if (itemCount <= FlyoutVisibleItemCount) {
        flyoutContentListHeight(itemCount)
    } else {
        FlyoutListMaxViewportHeight
    }

private fun flyoutContentListHeight(itemCount: Int): Dp =
    FlyoutListTopPadding +
        FlyoutItemRowHeight * itemCount +
        FlyoutItemSpacing * (itemCount - 1).coerceAtLeast(0) +
        FlyoutListBottomPadding

private data class FlyoutPanelLayout(
    val offsetFromRootPx: Int,
    /** 列表区域固定高度（不含标题栏） */
    val listViewportHeight: Dp,
)

private fun computeFlyoutPanelLayout(
    itemCount: Int,
    rootAnchorTopPx: Int,
    layerOffsetFromRootPx: Int,
    screenHeightPx: Int,
    bottomMarginPx: Int,
    density: Density,
): FlyoutPanelLayout {
    val desiredTopPx = rootAnchorTopPx + layerOffsetFromRootPx
    val headerPx = with(density) { FlyoutHeaderBlockHeight.roundToPx() }
    val idealListPx = with(density) { flyoutIdealListHeight(itemCount).roundToPx() }
    val minListPx = with(density) { FlyoutMinListHeight.roundToPx() }
    val idealPanelPx = headerPx + idealListPx

    // 整块面板（标题 + 列表）必须落在屏幕内，否则底部会被 Popup 裁切且无法滚到底
    val clampedTopPx = desiredTopPx.coerceAtMost(
        (screenHeightPx - idealPanelPx - bottomMarginPx).coerceAtLeast(0),
    ).coerceAtLeast(0)

    val spaceBelowPx = (screenHeightPx - clampedTopPx - bottomMarginPx).coerceAtLeast(0)
    val listFromSpacePx = (spaceBelowPx - headerPx).coerceAtLeast(0)
    val listPx = when {
        itemCount <= 0 -> 0
        itemCount > FlyoutVisibleItemCount -> minOf(idealListPx, listFromSpacePx).coerceAtLeast(minListPx.coerceAtMost(listFromSpacePx))
        else -> minOf(idealListPx, listFromSpacePx)
    }

    return FlyoutPanelLayout(
        offsetFromRootPx = clampedTopPx - rootAnchorTopPx,
        listViewportHeight = with(density) { listPx.toDp() },
    )
}

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
    var flyoutState by remember { mutableStateOf<CollapsedFlyoutState?>(null) }
    var flyoutVisible by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(isCollapsed) {
        if (!isCollapsed) {
            flyoutVisible = false
            flyoutState = null
        }
    }

    LaunchedEffect(flyoutState) {
        flyoutVisible = flyoutState != null
    }

    fun dismissFlyout() {
        flyoutVisible = false
    }

    LaunchedEffect(flyoutVisible) {
        if (!flyoutVisible && flyoutState != null) {
            delay(FlyoutExitMs.toLong())
            flyoutState = null
        }
    }

    fun openCollapsedFlyout(group: AppMenuNode.Group, anchorTopPx: Int) {
        if (flyoutState?.stack?.firstOrNull()?.group?.code == group.code && flyoutVisible) {
            dismissFlyout()
        } else {
            flyoutState = CollapsedFlyoutState(
                stack = listOf(FlyoutLayer(group)),
                rootAnchorTopPx = anchorTopPx,
            )
            flyoutVisible = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(if (isCollapsed) CollapsedSidebarWidth else ExpandedSidebarWidth),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(LayoutColors.SidebarStart, LayoutColors.SidebarMid, LayoutColors.SidebarEnd),
                    ),
                ),
        ) {
            SidebarLogo(
                isCollapsed = isCollapsed,
                onClick = { onNavigate("/dashboard") },
            )

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
                        onOpenCollapsedFlyout = ::openCollapsedFlyout,
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

        val state = flyoutState
        if (isCollapsed && state != null) {
            val sidebarWidthPx = with(density) { CollapsedSidebarWidth.roundToPx() }
            val configuration = LocalConfiguration.current
            val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }
            val bottomMarginPx = with(density) { FlyoutScreenBottomMargin.roundToPx() }
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset.Zero,
                onDismissRequest = ::dismissFlyout,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                ),
            ) {
                AnimatedVisibility(
                    visible = flyoutVisible,
                    enter = fadeIn(tween(FlyoutEnterMs, easing = FastOutSlowInEasing)),
                    exit = fadeOut(tween(FlyoutExitMs, easing = FastOutSlowInEasing)),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.14f))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = ::dismissFlyout,
                                ),
                        )
                        AnimatedVisibility(
                            visible = flyoutVisible,
                            enter = fadeIn(tween(FlyoutEnterMs, easing = FastOutSlowInEasing)) +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = tween(FlyoutEnterMs, easing = FastOutSlowInEasing),
                                ),
                            exit = fadeOut(tween(FlyoutExitMs)) +
                                scaleOut(targetScale = 0.96f, animationSpec = tween(FlyoutExitMs)),
                            modifier = Modifier
                                .zIndex(1f)
                                .offset {
                                    IntOffset(sidebarWidthPx, state.rootAnchorTopPx)
                                },
                        ) {
                            val panelStridePx = with(density) {
                                (CollapsedFlyoutWidth + 8.dp).roundToPx()
                            }
                            Box(modifier = Modifier.wrapContentSize(align = Alignment.TopStart)) {
                                state.stack.forEachIndexed { panelIndex, layer ->
                                    val panelLayout = computeFlyoutPanelLayout(
                                        itemCount = layer.group.children.size,
                                        rootAnchorTopPx = state.rootAnchorTopPx,
                                        layerOffsetFromRootPx = layer.offsetFromRootPx,
                                        screenHeightPx = screenHeightPx,
                                        bottomMarginPx = bottomMarginPx,
                                        density = density,
                                    )
                                    key(layer.group.code) {
                                        Box(
                                            modifier = Modifier.offset {
                                                IntOffset(
                                                    x = panelIndex * panelStridePx,
                                                    y = panelLayout.offsetFromRootPx,
                                                )
                                            },
                                        ) {
                                            CollapsedFlyoutPanel(
                                                group = layer.group,
                                                depth = panelIndex,
                                                offsetFromRootPx = layer.offsetFromRootPx,
                                                activePath = activePath,
                                                listViewportHeight = panelLayout.listViewportHeight,
                                                onLeafClick = { path ->
                                                    dismissFlyout()
                                                    onNavigate(path)
                                                },
                                                onGroupClick = { childGroup, offsetFromRootPx ->
                                                    flyoutState = state.copy(
                                                        stack = state.stack.take(panelIndex + 1) +
                                                            FlyoutLayer(
                                                                group = childGroup,
                                                                offsetFromRootPx = offsetFromRootPx,
                                                            ),
                                                    )
                                                },
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
    onOpenCollapsedFlyout: (AppMenuNode.Group, Int) -> Unit,
    onToggleCollapse: () -> Unit,
) {
    when (node) {
        is AppMenuNode.Leaf -> SidebarLeafItem(
            label = node.label,
            collapsedLabel = collapsedSidebarLabel(node.code, node.label),
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
            var anchorTopPx by remember(node.code) { mutableStateOf(0) }

            SidebarGroupItem(
                label = node.label,
                collapsedLabel = collapsedSidebarLabel(node.code, node.label),
                icon = node.icon,
                isCollapsed = isCollapsed,
                expanded = expanded,
                isActiveInSubtree = activeInSubtree,
                depth = depth,
                modifier = if (isCollapsed && depth == 0) {
                    Modifier.onGloballyPositioned { coordinates ->
                        anchorTopPx = coordinates.boundsInWindow().top.toInt()
                    }
                } else {
                    Modifier
                },
                onToggle = {
                    if (isCollapsed && depth == 0) {
                        onOpenCollapsedFlyout(node, anchorTopPx)
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
                            onOpenCollapsedFlyout = onOpenCollapsedFlyout,
                            onToggleCollapse = onToggleCollapse,
                        )
                    }
                }
            }
        }
    }
}

/** 折叠侧栏图标下方显示的简短名称 */
private fun collapsedSidebarLabel(code: String, label: String): String = when (code) {
    "DASHBOARD" -> "ホーム"
    "ERP" -> "ERP"
    "APS" -> "APS"
    "MES" -> "MES"
    "MASTER" -> "マスタ"
    "SYSTEM" -> "システム"
    else -> label
        .substringBefore('(')
        .substringBefore('（')
        .trim()
        .take(6)
}

@Composable
private fun SidebarCollapsedMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    fontWeight: FontWeight,
    background: Brush?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (background != null) Modifier.background(background) else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = tint,
            fontSize = CollapsedSidebarLabelSize,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier.fillMaxWidth(),
        )
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

/** Web SidebarMenu.vue 折叠态 `el-menu--popup`：从父菜单位置向右、子项向下展开 */
@Composable
private fun CollapsedFlyoutPanel(
    group: AppMenuNode.Group,
    depth: Int,
    offsetFromRootPx: Int,
    activePath: String,
    listViewportHeight: Dp,
    onLeafClick: (String) -> Unit,
    onGroupClick: (AppMenuNode.Group, Int) -> Unit,
) {
    var panelTopPx by remember(group.code, depth) { mutableStateOf(0) }
    var headerBlockHeightDp by remember(group.code, depth) { mutableStateOf(FlyoutHeaderBlockHeight) }
    val density = LocalDensity.current
    val panelGradient = Brush.verticalGradient(
        listOf(LayoutColors.SidebarStart, LayoutColors.SidebarMid, LayoutColors.SidebarEnd),
    )
    val totalPanelHeight = headerBlockHeightDp + listViewportHeight

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                panelTopPx = coordinates.boundsInWindow().top.toInt()
            }
            .padding(horizontal = 4.dp)
            .width(CollapsedFlyoutWidth)
            .height(totalPanelHeight)
            .shadow(
                elevation = 16.dp,
                shape = FlyoutPanelShape,
                clip = false,
                ambientColor = LoginColors.Primary.copy(alpha = 0.22f),
                spotColor = LoginColors.PrimaryDark.copy(alpha = 0.16f),
            )
            .clip(FlyoutPanelShape)
            .background(panelGradient)
            .border(
                width = 1.dp,
                color = LoginColors.Primary.copy(alpha = 0.32f),
                shape = FlyoutPanelShape,
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    headerBlockHeightDp = with(density) { coordinates.size.height.toDp() }
                },
            ) {
                FlyoutPanelHeader(group = group)
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(listViewportHeight),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = FlyoutListHorizontalPadding,
                        end = FlyoutListHorizontalPadding,
                        top = FlyoutListTopPadding,
                        bottom = FlyoutListBottomPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(FlyoutItemSpacing),
                ) {
                    items(
                        items = group.children,
                        key = { child ->
                            when (child) {
                                is AppMenuNode.Leaf -> child.code
                                is AppMenuNode.Group -> child.code
                            }
                        },
                    ) { child ->
                        when (child) {
                            is AppMenuNode.Leaf -> CollapsedFlyoutLeafItem(
                                label = child.label,
                                icon = child.icon,
                                isHome = child.isHome,
                                isActive = activePath == child.path,
                                onClick = { onLeafClick(child.path) },
                            )

                            is AppMenuNode.Group -> CollapsedFlyoutGroupItem(
                                label = child.label,
                                icon = child.icon,
                                isActiveInSubtree = isActiveInSubtree(child, activePath),
                                onClick = { itemTopPx ->
                                    onGroupClick(child, offsetFromRootPx + (itemTopPx - panelTopPx))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlyoutPanelHeader(group: AppMenuNode.Group) {
    val headerAccent = LoginColors.Primary.copy(alpha = 0.35f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Brush.horizontalGradient(listOf(headerAccent, Color.Transparent)))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            group.icon,
            contentDescription = null,
            tint = LayoutColors.SidebarTextActive,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = group.label,
            color = LayoutColors.SidebarTextActive,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CollapsedFlyoutLeafItem(
    label: String,
    icon: ImageVector,
    isHome: Boolean,
    isActive: Boolean,
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FlyoutItemRowHeight)
            .clip(RoundedCornerShape(8.dp))
            .then(if (bg != null) Modifier.background(bg) else Modifier.background(Color.White.copy(alpha = 0.04f)))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CollapsedFlyoutGroupItem(
    label: String,
    icon: ImageVector,
    isActiveInSubtree: Boolean,
    onClick: (Int) -> Unit,
) {
    val tint = if (isActiveInSubtree) LayoutColors.SidebarTextActive else LayoutColors.SidebarText
    var itemTopPx by remember(label) { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FlyoutItemRowHeight)
            .onGloballyPositioned { coordinates ->
                itemTopPx = coordinates.boundsInWindow().top.toInt()
            }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable { onClick(itemTopPx) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = tint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LayoutColors.SidebarText,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SidebarGroupItem(
    label: String,
    collapsedLabel: String,
    icon: ImageVector,
    isCollapsed: Boolean,
    expanded: Boolean,
    isActiveInSubtree: Boolean,
    depth: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (isActiveInSubtree) LayoutColors.SidebarTextActive else LayoutColors.SidebarText

    if (isCollapsed) {
        SidebarCollapsedMenuItem(
            icon = icon,
            label = collapsedLabel,
            tint = tint,
            fontWeight = FontWeight.Bold,
            background = null,
            onClick = onToggle,
            modifier = modifier,
        )
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = (10 + depth * 8).dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
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

@Composable
private fun SidebarLeafItem(
    label: String,
    collapsedLabel: String,
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

    if (isCollapsed) {
        SidebarCollapsedMenuItem(
            icon = icon,
            label = collapsedLabel,
            tint = textColor,
            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
            background = bg,
            onClick = onClick,
        )
        return
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
                horizontal = (10 + depth * 8).dp,
                vertical = if (depth > 0) 8.dp else 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
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
