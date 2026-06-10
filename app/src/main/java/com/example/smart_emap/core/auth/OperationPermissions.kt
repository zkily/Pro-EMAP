package com.example.smart_emap.core.auth

import com.example.smart_emap.data.model.OperationPermissionDto
import com.example.smart_emap.data.model.UserDto

/** 操作権限モジュール名（Web / バックエンドと同期） */
object OperationModules {
    const val SALES = "販売管理"
    const val PURCHASE = "購買管理"
    const val INVENTORY = "在庫管理"
    const val COST = "原価・会計"
    const val FINANCE = "経理・原価・人事"
    const val PRODUCTION_PLAN = "生産計画"
    const val MES = "製造実行"
    const val QUALITY = "品質管理"
    const val SYSTEM = "システム管理"
}

enum class OperationAction {
    CREATE,
    EDIT,
    DELETE,
    EXPORT,
    APPROVE,
}

private fun OperationPermissionDto.allows(action: OperationAction): Boolean = when (action) {
    OperationAction.CREATE -> canCreate
    OperationAction.EDIT -> canEdit
    OperationAction.DELETE -> canDelete
    OperationAction.EXPORT -> canExport
    OperationAction.APPROVE -> canApprove
}

private fun UserDto.fallbackCanOperate(action: OperationAction): Boolean {
    if (isAdmin()) return true
    return when (action) {
        OperationAction.EXPORT -> canRead()
        OperationAction.CREATE,
        OperationAction.EDIT,
        OperationAction.DELETE,
        OperationAction.APPROVE,
        -> canWrite()
    }
}

fun UserDto.modulePermission(module: String): OperationPermissionDto? =
    operationPermissions.firstOrNull { it.module == module }

fun UserDto.canOperate(module: String, action: OperationAction): Boolean {
    if (isAdmin()) return true
    val modPerm = modulePermission(module)
    if (modPerm == null) {
        return if (operationPermissions.isEmpty()) fallbackCanOperate(action) else false
    }
    return modPerm.allows(action)
}

fun UserDto.canCreate(module: String): Boolean = canOperate(module, OperationAction.CREATE)

fun UserDto.canEdit(module: String): Boolean = canOperate(module, OperationAction.EDIT)

fun UserDto.canDelete(module: String): Boolean = canOperate(module, OperationAction.DELETE)

fun UserDto.canExport(module: String): Boolean = canOperate(module, OperationAction.EXPORT)

fun UserDto.canApprove(module: String): Boolean = canOperate(module, OperationAction.APPROVE)
