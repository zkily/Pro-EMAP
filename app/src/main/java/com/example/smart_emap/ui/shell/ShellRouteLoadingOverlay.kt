package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/** 菜单 / 标签页切换时的全屏加载提示 */
@Composable
fun ShellRouteLoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(LayoutColors.ShellBg.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = LayoutColors.TabActive,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp),
        )
    }
}
