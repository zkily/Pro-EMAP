package com.example.smart_emap.ui.system.role

import com.example.smart_emap.data.model.OperationPermissionDto
import com.example.smart_emap.data.model.RoleDetailDto
import com.example.smart_emap.ui.shell.AppMenuConfig
import com.example.smart_emap.ui.shell.AppMenuNode

/** 権限設定用メニューノード（サイドバー [AppMenuConfig] と同構造） */
data class PermissionMenuNode(
    val code: String,
    val label: String,
    val menuId: Int?,
    val children: List<PermissionMenuNode> = emptyList(),
)

fun buildOperationPermissionsFromRole(role: RoleDetailDto?): List<OperationPermissionDto> {
    val byModule = (role?.operationPermissions ?: emptyList()).associateBy { it.module }
    return OPERATION_MODULES_LIST.map { module ->
        byModule[module] ?: OperationPermissionDto(
            module = module,
            canCreate = false,
            canEdit = false,
            canDelete = false,
            canExport = false,
            canApprove = false,
        )
    }
}

/** menus テーブルの code → id（DB 内 code は一意） */
fun buildMenuCodeToIdMap(items: List<Pair<String, Int>>): Map<String, Int> =
    items.associate { (code, id) -> code to id }

fun buildIdToCodeMap(codeToId: Map<String, Int>): Map<Int, String> =
    codeToId.entries.associate { (code, id) -> id to code }

/**
 * サイドバー定義から権限ツリーを構築。
 * DB に id が無い中間ノードは子を繰り上げ、重複・孤立 DB ノードは表示しない。
 */
fun buildPermissionMenuTree(codeToId: Map<String, Int>): List<PermissionMenuNode> {
    return AppMenuConfig.rootMenus.flatMap { attachPermissionMenuNode(it, codeToId) }
}

private fun attachPermissionMenuNode(node: AppMenuNode, codeToId: Map<String, Int>): List<PermissionMenuNode> {
    val menuId = codeToId[node.code]
    val children = node.children().flatMap { attachPermissionMenuNode(it, codeToId) }
    if (menuId == null && children.isEmpty()) return emptyList()
    if (menuId == null) return children
    return listOf(
        PermissionMenuNode(
            code = node.code,
            label = node.label,
            menuId = menuId,
            children = children,
        ),
    )
}

private fun AppMenuNode.children(): List<AppMenuNode> = when (this) {
    is AppMenuNode.Group -> children
    is AppMenuNode.Leaf -> emptyList()
}

fun collectAllPermissionMenuCodes(nodes: List<PermissionMenuNode>): Set<String> {
    val codes = mutableSetOf<String>()
    fun walk(list: List<PermissionMenuNode>) {
        list.forEach { node ->
            codes.add(node.code)
            walk(node.children)
        }
    }
    walk(nodes)
    return codes
}

fun collectDescendantCodes(node: PermissionMenuNode): Set<String> {
    val codes = mutableSetOf<String>()
    fun walk(n: PermissionMenuNode) {
        codes.add(n.code)
        n.children.forEach { walk(it) }
    }
    walk(node)
    return codes
}

fun findPermissionMenuNode(nodes: List<PermissionMenuNode>, code: String): PermissionMenuNode? {
    for (node in nodes) {
        if (node.code == code) return node
        findPermissionMenuNode(node.children, code)?.let { return it }
    }
    return null
}

fun toggleMenuCheckByCode(
    tree: List<PermissionMenuNode>,
    checked: Set<String>,
    code: String,
    check: Boolean,
): Set<String> {
    val node = findPermissionMenuNode(tree, code) ?: return checked
    if (check) {
        return checked + collectDescendantCodes(node)
    }
    var updated = checked - collectDescendantCodes(node)
    updated = stripAncestorsWithPartialSelection(tree, updated)
    return updated
}

/** 子が一部だけ選択されている親はチェック状態から外す */
private fun stripAncestorsWithPartialSelection(
    tree: List<PermissionMenuNode>,
    checked: Set<String>,
): Set<String> {
    val result = checked.toMutableSet()
    fun visit(node: PermissionMenuNode) {
        node.children.forEach { visit(it) }
        if (node.children.isEmpty() || node.code !in result) return
        val descendants = collectDescendantCodes(node) - node.code
        if (descendants.isNotEmpty() && !descendants.all { it in result }) {
            result.remove(node.code)
        }
    }
    tree.forEach { visit(it) }
    return result
}

/**
 * ロールの menu_permissions（id）→ UI チェック用 code。
 * DB に親 id のみ残っている場合、子が全て揃っていなければ親は表示しない。
 */
fun menuIdsToCheckedCodes(
    menuIds: List<Int>,
    idToCode: Map<Int, String>,
    tree: List<PermissionMenuNode>,
): Set<String> {
    val codes = menuIds.mapNotNull { idToCode[it] }.toMutableSet()
    return pruneParentCodesUnlessFullySelected(tree, codes)
}

private fun pruneParentCodesUnlessFullySelected(
    tree: List<PermissionMenuNode>,
    codes: MutableSet<String>,
): Set<String> {
    fun visit(node: PermissionMenuNode) {
        node.children.forEach { visit(it) }
        if (node.children.isEmpty() || node.code !in codes) return
        val descendants = collectDescendantCodes(node) - node.code
        if (descendants.isNotEmpty() && !descendants.all { it in codes }) {
            codes.remove(node.code)
        }
    }
    tree.forEach { visit(it) }
    return codes
}

/** 保存用: 画面上でチェックされている code のみを menu_id に変換（親の自動追加なし） */
fun menuCodesToMenuIdsForSave(
    checkedCodes: Set<String>,
    codeToId: Map<String, Int>,
): List<Int> = checkedCodes.mapNotNull { codeToId[it] }.distinct()
