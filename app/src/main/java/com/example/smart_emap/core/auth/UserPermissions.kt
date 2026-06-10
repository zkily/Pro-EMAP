package com.example.smart_emap.core.auth

import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.shell.AppMenuConfig

/**
 * 与 Web `menuPermissions.ts` 对齐的权限判断（P0: 常に menu_codes でページ制御）。
 *
 * 后端 `get_user_permissions(role)`:
 * - admin → ["all"]
 * - guest / viewer → ["read"]
 * - 其他 → ["read", "write"]
 *
 * 页面可见性由 `menu_codes`（user_roles → role_menu_permissions）控制。
 */
fun UserDto.hasPermission(permission: String): Boolean =
    permissions.contains(permission)

fun UserDto.isAdmin(): Boolean = hasPermission("all")

fun UserDto.canRead(): Boolean = isAdmin() || hasPermission("read")

fun UserDto.canWrite(): Boolean = isAdmin() || hasPermission("write")

/** 一覧 API 用：管理员全件，其余仅本部门（与 Web scopeDepartmentId 一致） */
fun UserDto.scopeDepartmentId(): Int? = if (isAdmin()) null else departmentId

/** P0: ログイン済みユーザーは常に menu_codes によるページ制御 */
fun UserDto.usesMenuCodePermissions(): Boolean = true

fun UserDto.hasMenuCode(code: String): Boolean =
    isAdmin() || menuCodes.contains(code)

fun UserDto.canAccessMenuCode(code: String): Boolean {
    if (code == "DASHBOARD") return true
    if (isAdmin()) return true
    if (code == "SYSTEM" || code.startsWith("SYSTEM_")) return false
    return hasMenuCode(code)
}

fun UserDto.canAccessPath(path: String): Boolean {
    val normalized = path.trim().ifEmpty { "/dashboard" }
    if (normalized == "/access-denied") return true
    if (normalized == "/dashboard") return true
    if (normalized == "/system" || normalized.startsWith("/system/")) return isAdmin()

    val codes = AppMenuConfig.codesForPath(normalized)
    if (codes.isEmpty()) return true
    if (isAdmin()) return true
    return codes.any { hasMenuCode(it) }
}

fun UserDto.resolveAccessiblePath(path: String): String =
    if (canAccessPath(path)) path.trim().ifEmpty { "/dashboard" } else "/access-denied"
