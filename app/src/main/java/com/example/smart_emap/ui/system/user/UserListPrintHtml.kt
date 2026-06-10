package com.example.smart_emap.ui.system.user

import com.example.smart_emap.data.model.UserListItemDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun buildUserListPrintHtml(users: List<UserListItemDto>): String {
    val generatedAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
    val rows = users.joinToString("") { user ->
        """
        <tr>
          <td>${user.id ?: ""}</td>
          <td>${escapeHtml(user.username.orEmpty())}</td>
          <td>${escapeHtml(user.fullName.orEmpty())}</td>
          <td>${escapeHtml(user.email.orEmpty())}</td>
          <td>${escapeHtml(user.department.orEmpty())}</td>
          <td>${escapeHtml(roleLabel(user.role))}</td>
          <td>${if (user.status == "locked") "ロック中" else "有効"}</td>
          <td>${if (user.twoFactor == true) "ON" else "OFF"}</td>
          <td>${escapeHtml(user.lastLogin.orEmpty())}</td>
        </tr>
        """.trimIndent()
    }
    return """
        <!DOCTYPE html>
        <html><head><meta charset="utf-8"/>
        <style>
          body { font-family: sans-serif; font-size: 11px; margin: 16px; }
          h1 { font-size: 16px; margin: 0 0 4px; }
          .meta { color: #64748b; margin-bottom: 12px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #e2e8f0; padding: 6px 8px; text-align: center; }
          th { background: #f8fafc; color: #334155; }
        </style></head>
        <body>
          <h1>ユーザー一覧</h1>
          <div class="meta">印刷日時: $generatedAt</div>
          <table>
            <thead><tr>
              <th>ID</th><th>ユーザー名</th><th>氏名</th><th>メール</th>
              <th>部門</th><th>ロール</th><th>状態</th><th>2FA</th><th>最終ログイン</th>
            </tr></thead>
            <tbody>$rows</tbody>
          </table>
        </body></html>
    """.trimIndent()
}

private fun escapeHtml(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
