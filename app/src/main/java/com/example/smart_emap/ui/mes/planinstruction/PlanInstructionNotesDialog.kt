package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.data.model.PlanInstructionNoteDto

@Composable
fun PlanInstructionNotesDialog(
    notes: List<PlanInstructionNoteDto>,
    loading: Boolean,
    saving: Boolean,
    onAdd: (String, () -> Unit) -> Unit,
    onToggleDone: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var newContent by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }
    val listScroll = rememberScrollState()
    val canAdd = !saving && newContent.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 520.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x734C1D95),
                        spotColor = Color(0x734C1D95),
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.NotesDialogBorder),
            ) {
                Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    PlanInstructionTheme.NotesDialogHeaderStart,
                                    PlanInstructionTheme.NotesDialogHeaderMid,
                                    PlanInstructionTheme.NotesDialogHeaderEnd,
                                ),
                            ),
                        )
                        .drawBehind {
                            drawLine(
                                color = PlanInstructionTheme.NotesDialogBorder,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        "メモ（TODO）",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PlanInstructionTheme.NotesDialogTitle,
                        letterSpacing = 0.2.sp,
                    )
                }

                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    PlanInstructionTheme.NotesDialogBodyTop,
                                    PlanInstructionTheme.NotesDialogBodyBottom,
                                ),
                            ),
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        PlanInstructionTheme.NotesDialogAddBgStart,
                                        PlanInstructionTheme.NotesDialogAddBgEnd,
                                    ),
                                ),
                            )
                            .border(1.dp, PlanInstructionTheme.NotesDialogAddBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicTextField(
                            value = newContent,
                            onValueChange = { if (it.length <= 200) newContent = it },
                            textStyle = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFF334155),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp, max = 72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, PlanInstructionTheme.NotesDialogInputBorder, RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            decorationBox = { inner ->
                                if (newContent.isEmpty()) {
                                    Text(
                                        "簡単なメモを入力（短文）",
                                        fontSize = 12.sp,
                                        color = PlanInstructionTheme.Subtitle,
                                    )
                                }
                                inner()
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${newContent.length}/200",
                                fontSize = 11.sp,
                                color = PlanInstructionTheme.NotesDialogCharCount,
                            )
                            Button(
                                onClick = { onAdd(newContent) { newContent = "" } },
                                enabled = canAdd,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PlanInstructionTheme.NotesDialogAddBtn,
                                    disabledContainerColor = PlanInstructionTheme.NotesDialogAddBtn.copy(alpha = 0.45f),
                                ),
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                    )
                                } else {
                                    Text("追加", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                    ) {
                        when {
                            loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = PlanInstructionTheme.NotesDialogAddBtn,
                                    )
                                }
                            }
                            notes.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("未登録", fontSize = 12.sp, color = PlanInstructionTheme.Subtitle)
                                }
                            }
                            else -> {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(listScroll),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        notes.forEach { note ->
                                            val id = note.id ?: return@forEach
                                            val done = note.isDone == 1
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(
                                                        1.dp,
                                                        PlanInstructionTheme.NotesDialogRowBorder,
                                                        RoundedCornerShape(8.dp),
                                                    )
                                                    .background(PlanInstructionTheme.NotesDialogRowBg)
                                                    .padding(horizontal = 6.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.Top,
                                            ) {
                                                Checkbox(
                                                    checked = done,
                                                    onCheckedChange = { onToggleDone(id, it) },
                                                    enabled = !saving,
                                                    modifier = Modifier.size(20.dp),
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = PlanInstructionTheme.NotesDialogCheckbox,
                                                        uncheckedColor = Color(0xFFC4B5FD),
                                                        checkmarkColor = Color.White,
                                                    ),
                                                )
                                                Text(
                                                    note.content.orEmpty(),
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp,
                                                    color = if (done) Color(0xFF94A3B8) else Color(0xFF334155),
                                                    textDecoration = if (done) TextDecoration.LineThrough else null,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(top = 2.dp, end = 4.dp),
                                                )
                                                IconButton(
                                                    onClick = { pendingDeleteId = id },
                                                    enabled = !saving,
                                                    modifier = Modifier.size(32.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "削除",
                                                        tint = PlanInstructionTheme.NotesDialogDelete,
                                                        modifier = Modifier.size(16.dp),
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

                HorizontalDivider(color = Color(0xFFEDE9FE))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PlanInstructionTheme.NotesDialogFooterBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.FilterBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    ) {
                        Text("閉じる", fontSize = 12.sp, color = PlanInstructionTheme.Subtitle)
                    }
                }
            }
            }

            if (pendingDeleteId != null) {
                val dismissConfirm = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.38f))
                        .clickable(
                            interactionSource = dismissConfirm,
                            indication = null,
                            onClick = { pendingDeleteId = null },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .widthIn(max = 320.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                "削除確認",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B),
                            )
                            Text(
                                "このメモを削除しますか？",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(onClick = { pendingDeleteId = null }) {
                                    Text("取消", fontSize = 13.sp, color = PlanInstructionTheme.Subtitle)
                                }
                                TextButton(
                                    onClick = {
                                        pendingDeleteId?.let(onDelete)
                                        pendingDeleteId = null
                                    },
                                ) {
                                    Text("削除", fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
