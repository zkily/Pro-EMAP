package com.example.smart_emap.ui.shell

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Web MainLayout.vue と同じブレークポイント */
private val MobileBreakpoint = 768.dp
private val TabletBreakpoint = 1024.dp

data class ShellLayoutMode(
    /** 幅 < 768dp：メニューをオーバーレイ表示（横画面の狭い端末向け） */
    val isMobile: Boolean,
    /** 768dp〜1024dp、または縦画面：アイコン幅 80dp の折りたたみサイドバー */
    val useCompactSidebar: Boolean,
    /** 縦画面ではオーバーレイを使わず常にコンパクトサイドバーを表示 */
    val useMobileOverlay: Boolean,
)

fun resolveShellLayoutMode(
    maxWidth: Dp,
    orientation: Int,
): ShellLayoutMode {
    val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
    val isMobile = maxWidth < MobileBreakpoint
    val isTablet = !isMobile && maxWidth <= TabletBreakpoint
    val useCompactSidebar = isPortrait || isTablet
    return ShellLayoutMode(
        isMobile = isMobile,
        useCompactSidebar = useCompactSidebar,
        useMobileOverlay = isMobile && !isPortrait,
    )
}

fun shouldAutoCollapseSidebar(layout: ShellLayoutMode): Boolean {
    return layout.useMobileOverlay || layout.useCompactSidebar
}
