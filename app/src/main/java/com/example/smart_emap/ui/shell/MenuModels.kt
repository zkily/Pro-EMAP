package com.example.smart_emap.ui.shell

import androidx.compose.ui.graphics.vector.ImageVector

data class ShellTab(
    val path: String,
    val title: String,
    val closable: Boolean = true,
)

sealed class AppMenuNode {
    abstract val code: String
    abstract val label: String
    abstract val icon: ImageVector

    data class Leaf(
        override val code: String,
        override val label: String,
        override val icon: ImageVector,
        val path: String,
        val isHome: Boolean = false,
    ) : AppMenuNode()

    data class Group(
        override val code: String,
        override val label: String,
        override val icon: ImageVector,
        val children: List<AppMenuNode>,
    ) : AppMenuNode()
}
