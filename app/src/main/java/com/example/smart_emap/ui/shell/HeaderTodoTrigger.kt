package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.smart_emap.data.model.UserTodoItemDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HeaderTodoTrigger(
    state: HeaderTodoUiState,
    onOpen: () -> Unit,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onClearDone: () -> Unit,
    onUpdateContent: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val pendingBadge = if (state.pendingCount > 99) "99+" else state.pendingCount.toString()

    Box(modifier = modifier) {
        HeaderTodoIconButton(
            active = expanded,
            pendingCount = state.pendingCount,
            pendingBadge = pendingBadge,
            onClick = {
                expanded = true
                onOpen()
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 320.dp, max = 380.dp)
                .heightIn(max = 520.dp),
            properties = PopupProperties(focusable = true),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFECFDF5)),
                        ),
                        RoundedCornerShape(14.dp),
                    )
                    .border(1.dp, Color(0x8CA7F3D0), RoundedCornerShape(14.dp)),
            ) {
                HeaderTodoPanelContent(
                    state = state,
                    onDraftChange = onDraftChange,
                    onAdd = onAdd,
                    onToggle = onToggle,
                    onDelete = onDelete,
                    onClearDone = onClearDone,
                    onUpdateContent = onUpdateContent,
                )
            }
        }
    }
}

@Composable
private fun HeaderTodoIconButton(
    active: Boolean,
    pendingCount: Int,
    pendingBadge: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(15.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.32f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x2EFFFFFF),
                            Color(0x3834D399),
                            Color(0x2E10B981),
                        ),
                    ),
                )
                .border(1.dp, Color(0x59A7F3D0), RoundedCornerShape(7.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = if (pendingCount > 0) {
                    "連絡事項（未完了 $pendingBadge 件）"
                } else {
                    "連絡事項"
                },
                tint = if (active) Color(0xFFECFDF5) else Color(0xFFA7F3D0),
                modifier = Modifier.size(13.dp),
            )
            if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp, end = 1.dp)
                        .height(14.dp)
                        .widthIn(min = 14.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF059669)),
                            ),
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = pendingBadge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderTodoPanelContent(
    state: HeaderTodoUiState,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggle: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onClearDone: () -> Unit,
    onUpdateContent: (Int, String) -> Unit,
) {
    var editingId by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .widthIn(min = 300.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "連絡事項",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                    )
                    Text(
                        text = "連絡事項（サーバー保存）",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                    )
                }
            }
            if (state.pendingCount > 0) {
                Text(
                    text = "未完了 ${state.pendingCount} 件",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xD9D1FAE5))
                        .border(1.dp, Color(0x736EE7B7), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("連絡事項を追加、Enter で登録", fontSize = 12.sp) },
                singleLine = true,
                enabled = !state.submitting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                shape = RoundedCornerShape(11.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onAdd,
                enabled = state.draft.trim().isNotEmpty() && !state.submitting,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)),
                        ),
                    ),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "追加", tint = Color.White)
                }
            }
        }

        state.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = Color(0xFFDC2626), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color(0xFF10B981), strokeWidth = 2.dp)
                }
            }
            state.sortedItems.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("✨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "連絡事項がありません。上の欄から追加してください。",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                    )
                }
            }
            else -> {
                val listScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .verticalScroll(listScrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.sortedItems.forEach { item ->
                        HeaderTodoRow(
                            item = item,
                            isEditing = editingId == item.id,
                            editingText = if (editingId == item.id) editingText else item.content,
                            onStartEdit = {
                                if (item.isDone != 1) {
                                    editingId = item.id
                                    editingText = item.content
                                }
                            },
                            onEditingTextChange = { editingText = it },
                            onCommitEdit = {
                                onUpdateContent(item.id, editingText)
                                editingId = null
                                editingText = ""
                            },
                            onCancelEdit = {
                                editingId = null
                                editingText = ""
                            },
                            onToggle = { onToggle(item.id) },
                            onDelete = { onDelete(item.id) },
                        )
                    }
                }
            }
        }

        if (state.doneCount > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClearDone) {
                    Text(
                        text = "完了 ${state.doneCount} 件を削除",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderTodoRow(
    item: UserTodoItemDto,
    isEditing: Boolean,
    editingText: String,
    onStartEdit: () -> Unit,
    onEditingTextChange: (String) -> Unit,
    onCommitEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val done = item.isDone == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFCFCFD))
            .border(1.dp, Color(0x3D94A3B8), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = done,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF10B981),
                checkmarkColor = Color.White,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onCommitEdit() }),
                    shape = RoundedCornerShape(8.dp),
                )
            } else {
                Text(
                    text = item.content,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (done) Color(0xFF64748B) else Color(0xFF0F172A),
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    modifier = Modifier.clickable(onClick = onStartEdit),
                )
            }
            Row(
                modifier = Modifier.padding(top = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item.createdBy?.takeIf { it.isNotBlank() }?.let { author ->
                    Text(
                        text = "登録 $author",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "作成 ${formatTodoDateTime(item.createdAt)}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                )
                if (done && !item.completedAt.isNullOrBlank()) {
                    Text(
                        text = "完了 ${formatTodoDateTime(item.completedAt)}",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                    )
                }
            }
        }
        if (isEditing) {
            IconButton(onClick = onCommitEdit, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, contentDescription = "保存", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onCancelEdit, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "取消", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            }
        } else {
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            }
        }
    }
}

private fun formatTodoDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "--"
    return runCatching {
        val normalized = value.replace(" ", "T").take(19)
        LocalDateTime.parse(normalized).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }.getOrDefault("--")
}
