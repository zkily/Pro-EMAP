package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.common.BeautifulDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.OrderMonthlyItemDto

@Composable
fun OrderMonthlyDialogs(
    state: OrderMonthlyUiState,
    viewModel: OrderMonthlyViewModel,
) {
    when (state.activeDialog) {
        OrderMonthlyDialog.GenerateDaily -> GenerateDailyConfirmDialog(
            year = state.year,
            month = state.month,
            destinationLabel = state.generateDailyDestinationLabel,
            onConfirm = viewModel::confirmGenerateDaily,
            onDismiss = viewModel::dismissDialog,
        )
        OrderMonthlyDialog.ForecastUpdate -> {
            val destLabel = state.destinationCd.takeIf { it.isNotBlank() }?.let { cd ->
                state.destinationOptions.find { it.cd == cd }?.let { "${it.cd} | ${it.name}" } ?: cd
            } ?: "全納入先"
            ForecastUpdateConfirmDialog(
                year = state.year,
                month = state.month,
                destinationLabel = destLabel,
                onConfirm = viewModel::confirmForecastUpdate,
                onDismiss = viewModel::dismissDialog,
            )
        }
        OrderMonthlyDialog.UpdateFields -> UpdateFieldsDialog(
            startDate = state.updateFieldsStartDate,
            syncProduct = state.updateFieldsSyncProduct,
            onStartDateChange = viewModel::setUpdateFieldsStartDate,
            onSyncProductChange = viewModel::setUpdateFieldsSyncProduct,
            onSubmit = viewModel::submitUpdateFieldsRequest,
            onDismiss = viewModel::dismissDialog,
        )
        OrderMonthlyDialog.UpdateFieldsConfirm -> UpdateFieldsConfirmDialog(
            startDate = state.updateFieldsStartDate,
            syncProduct = state.updateFieldsSyncProduct,
            onConfirm = viewModel::confirmUpdateFields,
            onDismiss = viewModel::dismissDialog,
        )
        OrderMonthlyDialog.Edit -> EditMonthlyDialog(state, viewModel)
        OrderMonthlyDialog.Delete -> AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text("削除確認") },
            text = { Text("受注 ${state.deleteTarget?.orderId ?: ""} を削除しますか？日受注も削除されます。") },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = OrderMonthlyColors.DiffNegative),
                ) { Text("削除") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDialog) { Text("キャンセル") } },
        )
        OrderMonthlyDialog.BatchConfirm -> BatchRegisterConfirmDialog(
            createCount = state.batchPendingCreateCount,
            updateCount = state.batchPendingUpdateCount,
            year = state.batchYear,
            month = state.batchMonth,
            destinationLabel = state.destinationOptions.find { it.cd == state.batchDestinationCd }
                ?.let { "${it.cd} | ${it.name}" } ?: state.batchDestinationCd,
            onConfirm = viewModel::confirmBatchRegister,
            onDismiss = viewModel::dismissDialog,
        )
        OrderMonthlyDialog.BatchRegister -> BatchRegisterDialog(state, viewModel)
        OrderMonthlyDialog.DailyBatchEdit -> DailyBatchEditDialog(state, viewModel)
        OrderMonthlyDialog.DailyManage -> DailyManageDialog(state, viewModel)
        OrderMonthlyDialog.None -> Unit
    }
}

@Composable
private fun GenerateDailyConfirmDialog(
    year: Int,
    month: Int,
    destinationLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 411.dp)
                .fillMaxWidth(0.99f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x503B82F6))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xEB3B82F6),
                                    Color(0xF06366F1),
                                    Color(0xEB8B5CF6),
                                ),
                            ),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                                    radius = 420f,
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.32f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 38.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.24f))
                                .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "日受注リスト生成",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp,
                            )
                            Text(
                                "月次受注から日別受注データを作成します",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFAFBFF), Color(0xFFF5F6FB)),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0A000000))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0x0F000000), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GenerateDailyInfoRow(
                            label = "対象年月",
                            icon = Icons.Default.CalendarMonth,
                            accent = Color(0xFF6366F1),
                        ) {
                            Text(
                                "${year}年 ${month}月",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrderMonthlyColors.TextPrimary,
                            )
                        }
                        GenerateDailyInfoRow(
                            label = "種別",
                            icon = Icons.Default.NoteAdd,
                            accent = Color(0xFF10B981),
                            stripe = true,
                        ) {
                            GenerateDailyTag(text = "量産品", color = Color(0xFF047857), bg = Color(0x1A10B981))
                        }
                        GenerateDailyInfoRow(
                            label = "納入先",
                            icon = Icons.Default.LocalShipping,
                            accent = Color(0xFF3B82F6),
                        ) {
                            Text(
                                destinationLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrderMonthlyColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(0.65f),
                            )
                        }
                    }

                    Text(
                        "上記条件で日受注リストを生成します。よろしいですか？",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(listOf(Color(0x0F000000), Color.Transparent)),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, OrderMonthlyColors.BorderLight, RoundedCornerShape(10.dp))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("キャンセル", fontSize = 13.sp, color = OrderMonthlyColors.TextMuted, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color(0x403B82F6))
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF4F46E5)),
                                ),
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                                    ),
                                ),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Text("生成", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateDailyInfoRow(
    label: String,
    icon: ImageVector,
    accent: Color,
    stripe: Boolean = false,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (stripe) Color(0x086366F1) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            }
            Text(label, fontSize = 12.sp, color = OrderMonthlyColors.TextMuted, fontWeight = FontWeight.Medium)
        }
        value()
    }
}

@Composable
private fun GenerateDailyTag(text: String, color: Color, bg: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(listOf(bg, bg.copy(alpha = 0.6f))),
            )
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
    )
}

@Composable
private fun ForecastUpdateConfirmDialog(
    year: Int,
    month: Int,
    destinationLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val teal = Color(0xFF0D9488)
    val tealLight = Color(0xFF14B8A6)
    val steps = listOf(
        "Step1" to "確定本数を内示本数に同期（将来90日）",
        "Step2" to "確定なし・内示ありの期間をクリア",
        "Step3" to "製品別最終確定日まで内示をクリア",
        "Step4" to "最終確定日以降の残留内示をクリア",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 411.dp)
                .fillMaxWidth(0.99f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x5014B8A6))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xEB14B8A6),
                                    Color(0xF00D9488),
                                    Color(0xEB0F766E),
                                ),
                            ),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                                    radius = 420f,
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.32f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 38.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.24f))
                                .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "内示本数更新",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp,
                            )
                            Text(
                                "四段階ルールで日受注の内示を一括更新",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF0FDF4), Color(0xFFECFDF5)),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ForecastFilterChip("${year}年${month}月", Color(0xFF6366F1))
                        ForecastFilterChip(destinationLabel, Color(0xFF3B82F6))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0A000000))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0x0F000000), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        steps.forEachIndexed { index, (step, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (index % 2 == 0) Color(0x0614B8A6) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    step,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(tealLight.copy(alpha = 0.2f), teal.copy(alpha = 0.15f)),
                                            ),
                                        )
                                        .border(1.dp, teal.copy(alpha = 0.25f), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = teal,
                                )
                                Text(
                                    desc,
                                    fontSize = 11.sp,
                                    color = OrderMonthlyColors.TextPrimary,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    Text(
                        "上記四段階ルールで内示本数更新を実行します。よろしいですか？",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F766E),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(listOf(Color(0x0F000000), Color.Transparent)),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, OrderMonthlyColors.BorderLight, RoundedCornerShape(10.dp))
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("キャンセル", fontSize = 13.sp, color = OrderMonthlyColors.TextMuted, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color(0x4014B8A6))
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(tealLight, teal, Color(0xFF0F766E)),
                                ),
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                                    ),
                                ),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Text("実行", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastFilterChip(text: String, accent: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.1f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun UpdateFieldsDialog(
    startDate: String,
    syncProduct: Boolean,
    onStartDateChange: (String) -> Unit,
    onSyncProductChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val amber = Color(0xFFD97706)
    val amberLight = Color(0xFFF59E0B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 411.dp)
                .fillMaxWidth(0.99f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x50F59E0B))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                UpdateFieldsHeader(
                    title = "製品情報一括更新",
                    subtitle = "開始日以降の受注情報を一括更新",
                    onDismiss = onDismiss,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0A000000))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, amber.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "開始日",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F),
                        )
                        UpdateFieldsDateField(
                            value = startDate,
                            accent = amber,
                            onDateSelected = onStartDateChange,
                        )
                        Text(
                            "※ 開始日以降の受注データが更新対象です",
                            fontSize = 10.sp,
                            color = Color(0xFF92400E),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSyncProductChange(!syncProduct) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            UpdateFieldsCheckBox(checked = syncProduct, accent = amber)
                            Text(
                                "製品情報を最新データに更新",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                            )
                        }

                        if (syncProduct) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xE6FEF3C7), Color(0x80FDE68A)),
                                        ),
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(amber, amber.copy(alpha = 0.3f), Color.Transparent),
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = amber,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    "月受注・日受注の製品名・別名・タイプ・入数・納入先名を最新に一括更新します。",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 15.sp,
                                )
                            }
                        }
                    }
                }

                UpdateFieldsFooter(
                    confirmLabel = "更新",
                    confirmIcon = Icons.Default.Inventory2,
                    accent = amberLight,
                    accentDark = amber,
                    onDismiss = onDismiss,
                    onConfirm = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun UpdateFieldsConfirmDialog(
    startDate: String,
    syncProduct: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val amber = Color(0xFFD97706)
    val amberLight = Color(0xFFF59E0B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 411.dp)
                .fillMaxWidth(0.99f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x50F59E0B))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                UpdateFieldsHeader(
                    title = "更新確認",
                    subtitle = "以下の条件で製品情報を一括更新します",
                    onDismiss = onDismiss,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, amber.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GenerateDailyInfoRow(
                            label = "開始日",
                            icon = Icons.Default.CalendarMonth,
                            accent = amber,
                        ) {
                            Text(startDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrderMonthlyColors.TextPrimary)
                        }
                        GenerateDailyInfoRow(
                            label = "製品同期",
                            icon = Icons.Default.Inventory2,
                            accent = Color(0xFF10B981),
                            stripe = true,
                        ) {
                            GenerateDailyTag(
                                text = if (syncProduct) "あり" else "なし",
                                color = if (syncProduct) Color(0xFF047857) else Color(0xFF64748B),
                                bg = if (syncProduct) Color(0x1A10B981) else Color(0x1A64748B),
                            )
                        }
                    }
                    Text(
                        "製品情報を一括更新します。よろしいですか？",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF92400E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                UpdateFieldsFooter(
                    confirmLabel = "実行",
                    confirmIcon = Icons.Default.Refresh,
                    accent = amberLight,
                    accentDark = amber,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun UpdateFieldsHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xEBF59E0B),
                        Color(0xF0D97706),
                        Color(0xEBB45309),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        radius = 420f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.32f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 38.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.28f))
                    .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun UpdateFieldsDateField(
    value: String,
    accent: Color,
    onDateSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        BeautifulDatePickerDialog(
            value = value,
            title = "日付を選択",
            accentColor = accent,
            confirmLabel = "確定",
            onDismiss = { showPicker = false },
            onConfirm = {
                onDateSelected(it)
                showPicker = false
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = value.ifBlank { "日付を選択" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (value.isBlank()) OrderMonthlyColors.TextMuted else OrderMonthlyColors.TextPrimary,
        )
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun UpdateFieldsCheckBox(checked: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (checked) accent.copy(alpha = 0.15f) else Color.White)
            .border(
                width = 1.5.dp,
                color = if (checked) accent else OrderMonthlyColors.BorderLight,
                shape = RoundedCornerShape(5.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun UpdateFieldsFooter(
    confirmLabel: String,
    confirmIcon: ImageVector,
    accent: Color,
    accentDark: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color(0x0F000000), Color.Transparent)),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, OrderMonthlyColors.BorderLight, RoundedCornerShape(10.dp))
                .clickable(onClick = onDismiss)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("キャンセル", fontSize = 13.sp, color = OrderMonthlyColors.TextMuted, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .height(36.dp)
                .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = accent.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(accent, accentDark)))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clickable(onClick = onConfirm)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                        ),
                    ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(confirmIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Text(confirmLabel, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditMonthlyDialog(state: OrderMonthlyUiState, viewModel: OrderMonthlyViewModel) {
    val form = state.editForm
    val saving = state.actionLoading
    val greenDark = Color(0xFF064E3B)

    Dialog(
        onDismissRequest = viewModel::dismissDialog,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.94f)
                .shadow(20.dp, RoundedCornerShape(14.dp), spotColor = Color(0x33000000))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF0FDF4), Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                            ),
                        )
                        .border(width = 1.dp, color = Color(0xFFA7F3D0))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0x5910B981))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "月別受注編集",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = greenDark,
                        letterSpacing = 0.3.sp,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFAFBFC), Color.White),
                                startY = 0f,
                                endY = 120f,
                            ),
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MonthlyEditFormItem(label = "納入先", required = true) {
                        MonthlyEditDisabledSelect("${form.destinationCd} | ${form.destinationName}")
                    }
                    MonthlyEditFormItem(label = "年", required = true) {
                        MonthlyEditNumberStepper(
                            value = form.year.toString(),
                            enabled = false,
                            onDecrease = {},
                            onIncrease = {},
                        )
                    }
                    MonthlyEditFormItem(label = "月", required = true) {
                        MonthlyEditNumberStepper(
                            value = form.month.toString(),
                            enabled = false,
                            onDecrease = {},
                            onIncrease = {},
                        )
                    }
                    MonthlyEditFormItem(label = "製品", required = true) {
                        MonthlyEditDisabledSelect("${form.productCd} ${form.productName}")
                    }
                    MonthlyEditFormItem(label = "タイプ") {
                        MonthlyEditDisabledSelect(form.productType)
                    }
                    MonthlyEditFormItem(label = "内示") {
                        MonthlyEditNumberStepper(
                            value = form.forecastUnits,
                            enabled = !saving,
                            onDecrease = { viewModel.adjustEditForecastUnits(-1) },
                            onIncrease = { viewModel.adjustEditForecastUnits(1) },
                            onValueChange = viewModel::setEditForecastUnits,
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            .clickable(enabled = !saving, onClick = viewModel::dismissDialog)
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(15.dp))
                        Text("キャンセル", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        modifier = Modifier
                            .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = Color(0x663B82F6))
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF2563EB), Color(0xFF1D4ED8)),
                                ),
                            )
                            .clickable(enabled = !saving, onClick = viewModel::saveEdit)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                        Text(
                            if (saving) "保存中..." else "保存",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyEditFormItem(
    label: String,
    required: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (required) {
                Text("*", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
            )
        }
        content()
    }
}

@Composable
private fun MonthlyEditDisabledSelect(value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .shadow(1.dp, RoundedCornerShape(10.dp), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F7FA))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value.ifBlank { "選択" },
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC0C4CC),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun MonthlyEditNumberStepper(
    value: String,
    enabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onValueChange: ((String) -> Unit)? = null,
) {
    val controlBg = if (enabled) Color.White else Color(0xFFF5F7FA)
    val controlBorder = Color(0xFFE2E8F0)
    val textColor = if (enabled) Color(0xFF1E293B) else Color(0xFFC0C4CC)
    val buttonBg = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
    val buttonTint = if (enabled) Color(0xFF64748B) else Color(0xFFCBD5E1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .background(buttonBg)
                .border(1.dp, controlBorder, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .clickable(enabled = enabled, onClick = onDecrease),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, contentDescription = "減", tint = buttonTint, modifier = Modifier.size(16.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(controlBg)
                .border(1.dp, controlBorder),
            contentAlignment = Alignment.Center,
        ) {
            if (onValueChange != null && enabled) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = value.ifBlank { "0" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(buttonBg)
                .border(1.dp, controlBorder, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .clickable(enabled = enabled, onClick = onIncrease),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = "増", tint = buttonTint, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchRegisterDialog(state: OrderMonthlyUiState, viewModel: OrderMonthlyViewModel) {
    var destExpanded by remember { mutableStateOf(false) }
    var quantityEditIndex by remember { mutableStateOf<Int?>(null) }
    val green = Color(0xFF059669)
    val greenLight = Color(0xFF34D399)
    val greenDark = Color(0xFF047857)
    val destLabel = state.destinationOptions.find { it.cd == state.batchDestinationCd }
        ?.let { "${it.cd} | ${it.name}" } ?: ""
    val filledCount = state.batchProducts.count { row ->
        row.quantity.isNotBlank() && row.quantity.toIntOrNull()?.let { it > 0 } == true
    }

    quantityEditIndex?.let { editIndex ->
        if (editIndex in state.batchProducts.indices) {
            key(editIndex) {
                val row = state.batchProducts[editIndex]
                BatchRegisterQuantityDialog(
                    row = row,
                    index = editIndex,
                    total = state.batchProducts.size,
                    initialValue = row.quantity,
                    onDismiss = { quantityEditIndex = null },
                    onConfirm = { value ->
                        viewModel.setBatchQuantity(editIndex, value)
                        val nextIndex = editIndex + 1
                        quantityEditIndex = if (nextIndex < state.batchProducts.size) nextIndex else null
                    },
                )
            }
        } else {
            quantityEditIndex = null
        }
    }

    Dialog(
        onDismissRequest = viewModel::dismissDialog,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(0.95f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x50059669))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BatchRegisterHeader(
                    productCount = state.batchProducts.size,
                    filledCount = filledCount,
                    onDismiss = viewModel::dismissDialog,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                            ),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BatchRegisterFilterBar(
                        year = state.batchYear,
                        month = state.batchMonth,
                        destinationCd = state.batchDestinationCd,
                        destinationOptions = state.destinationOptions,
                        destExpanded = destExpanded,
                        onDestExpandedChange = { destExpanded = it },
                        onPrevMonth = viewModel::batchGoPrevMonth,
                        onCurrentMonth = viewModel::batchGoCurrentMonth,
                        onNextMonth = viewModel::batchGoNextMonth,
                        onDestinationChange = viewModel::setBatchDestinationCd,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .shadow(4.dp, RoundedCornerShape(9.dp), spotColor = green.copy(alpha = 0.4f))
                                .clip(RoundedCornerShape(9.dp))
                                .background(Brush.linearGradient(listOf(greenLight, green, greenDark)))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                                .clickable(enabled = state.batchDestinationCd.isNotBlank() && !state.batchLoading) {
                                    viewModel.loadBatchProducts()
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                if (state.batchLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White,
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    if (state.batchLoading) "読込中..." else "製品読込",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(
                                        alpha = if (state.batchDestinationCd.isBlank() || state.batchLoading) 0.55f else 1f,
                                    ),
                                )
                            }
                        }
                        if (state.batchProducts.isNotEmpty()) {
                            BatchRegisterStatChip("${state.batchProducts.size}件", green)
                            if (filledCount > 0) {
                                BatchRegisterStatChip("入力 $filledCount", greenDark)
                            }
                        } else if (destLabel.isNotBlank()) {
                            Text(
                                "納入先: $destLabel",
                                fontSize = 10.sp,
                                color = Color(0xFF065F46),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Color(0x12059669))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.94f))
                            .border(1.dp, green.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    ) {
                        when {
                            state.batchLoading && state.batchProducts.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = green, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                                }
                            }
                            state.batchProducts.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.FileCopy,
                                            contentDescription = null,
                                            tint = green.copy(alpha = 0.35f),
                                            modifier = Modifier.size(28.dp),
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "納入先を選択して製品を読込",
                                            fontSize = 11.sp,
                                            color = OrderMonthlyColors.TextMuted,
                                        )
                                    }
                                }
                            }
                            else -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(green.copy(alpha = 0.12f), green.copy(alpha = 0.06f)),
                                                ),
                                            )
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("製品名", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = greenDark, modifier = Modifier.weight(1f))
                                        Text("状態", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = greenDark, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
                                        Text("内示本数", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = greenDark, modifier = Modifier.width(72.dp), textAlign = TextAlign.Center)
                                    }
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 280.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        itemsIndexed(state.batchProducts) { index, row ->
                                            BatchRegisterProductRow(
                                                row = row,
                                                index = index,
                                                green = green,
                                                greenDark = greenDark,
                                                onQuantityClick = { quantityEditIndex = index },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                UpdateFieldsFooter(
                    confirmLabel = "一括登録",
                    confirmIcon = Icons.Default.FileCopy,
                    accent = greenLight,
                    accentDark = green,
                    onDismiss = viewModel::dismissDialog,
                    onConfirm = viewModel::submitBatchRegister,
                )
            }
        }
    }
}

@Composable
private fun BatchRegisterHeader(
    productCount: Int,
    filledCount: Int,
    onDismiss: () -> Unit,
    title: String = "一括登録",
    subtitle: String = "月次受注を納入先単位で一括登録",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xEB34D399),
                        Color(0xF0059669),
                        Color(0xEB047857),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        radius = 420f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.32f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 38.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.28f))
                    .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.FileCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                )
            }
            if (productCount > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("$productCount 件", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (filledCount > 0) {
                        Text("入力 $filledCount", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchRegisterFilterBar(
    year: Int,
    month: Int,
    destinationCd: String,
    destinationOptions: List<DestinationOptionDto>,
    destExpanded: Boolean,
    onDestExpandedChange: (Boolean) -> Unit,
    onPrevMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDestinationChange: (String) -> Unit,
) {
    val green = Color(0xFF059669)
    val greenDark = Color(0xFF047857)
    val japanZone = remember { ZoneId.of("Asia/Tokyo") }
    val isCurrentMonth = year == LocalDate.now(japanZone).year && month == LocalDate.now(japanZone).monthValue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(11.dp), spotColor = green.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, green.copy(alpha = 0.2f), RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = green, modifier = Modifier.size(15.dp))
            Text("対象月", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = greenDark)
            BatchRegisterNavChip("前月", onPrevMonth, green)
            BatchRegisterNavChip("今月", onCurrentMonth, green, filled = isCurrentMonth)
            BatchRegisterNavChip("翌月", onNextMonth, green)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.linearGradient(listOf(green.copy(alpha = 0.15f), green.copy(alpha = 0.08f))),
                    )
                    .border(1.dp, green.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    "${year}年${month}月",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = greenDark,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(15.dp))
            Text("納入先", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
            Box(modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End)) {
                ExposedDropdownMenuBox(
                    expanded = destExpanded,
                    onExpandedChange = onDestExpandedChange,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color(0x206366F1))
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F3FF))),
                            )
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = destinationOptions.find { it.cd == destinationCd }
                                ?.let { "${it.cd} | ${it.name}" } ?: "納入先を選択",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (destinationCd.isBlank()) OrderMonthlyColors.TextMuted else OrderMonthlyColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (destinationCd.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            onDestinationChange("")
                                            onDestExpandedChange(false)
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = OrderMonthlyColors.TextMuted, modifier = Modifier.size(12.dp))
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                    }
                    ExposedDropdownMenu(expanded = destExpanded, onDismissRequest = { onDestExpandedChange(false) }) {
                        destinationOptions.forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${d.cd} | ${d.name}",
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = {
                                    onDestinationChange(d.cd)
                                    onDestExpandedChange(false)
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
private fun BatchRegisterNavChip(
    text: String,
    onClick: () -> Unit,
    accent: Color,
    filled: Boolean = false,
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .height(28.dp)
            .then(
                if (filled) {
                    Modifier
                        .shadow(3.dp, shape, spotColor = accent.copy(alpha = 0.4f))
                        .clip(shape)
                        .background(Brush.linearGradient(listOf(Color(0xFF34D399), accent)))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                } else {
                    Modifier
                        .clip(shape)
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, accent.copy(alpha = 0.2f), shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = if (filled) FontWeight.Bold else FontWeight.Medium,
            color = if (filled) Color.White else accent,
        )
    }
}

@Composable
private fun BatchRegisterStatChip(text: String, accent: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = accent,
    )
}

@Composable
private fun BatchRegisterProductRow(
    row: BatchProductRowUi,
    index: Int,
    green: Color,
    greenDark: Color,
    onQuantityClick: () -> Unit,
) {
    val hasQty = row.quantity.isNotBlank() && (row.quantity.toIntOrNull() ?: 0) > 0
    val displayQty = row.quantity.ifBlank { "0" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(if (index % 2 == 0) Color.White else Color(0xFFF8FAFC))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                row.productName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = OrderMonthlyColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                row.productType,
                fontSize = 9.sp,
                color = OrderMonthlyColors.TextMuted,
                maxLines = 1,
            )
        }
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            BatchRegisterStatusTag(exists = row.exists)
        }
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(28.dp)
                .shadow(if (hasQty) 2.dp else 0.dp, RoundedCornerShape(7.dp), spotColor = green.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(7.dp))
                .background(if (hasQty) Color(0xFFECFDF5) else Color.White)
                .border(
                    1.dp,
                    if (hasQty) green.copy(alpha = 0.45f) else OrderMonthlyColors.BorderLight,
                    RoundedCornerShape(7.dp),
                )
                .clickable(onClick = onQuantityClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayQty,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                color = if (hasQty) greenDark else OrderMonthlyColors.TextMuted,
            )
        }
    }
}

@Composable
private fun BatchRegisterQuantityDialog(
    row: BatchProductRowUi,
    index: Int,
    total: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val green = Color(0xFF059669)
    val greenLight = Color(0xFF34D399)
    val greenDark = Color(0xFF047857)
    val bg = Color(0xFFECFDF5)
    val border = Color(0xFF6EE7B7)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun submit() {
        onConfirm(text.filter { it.isDigit() })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 292.dp)
                .fillMaxWidth(0.58f)
                .shadow(18.dp, RoundedCornerShape(14.dp), spotColor = green.copy(alpha = 0.45f))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, border.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Brush.linearGradient(listOf(greenLight, green, greenDark))),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                                    radius = 280f,
                                ),
                            ),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("本", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "内示本数 入力",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                            )
                            Text(
                                "${row.productName} · ${index + 1}/$total",
                                color = Color.White.copy(alpha = 0.86f),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(7.dp))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .height(38.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = green.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.verticalGradient(listOf(bg, Color.White)))
                            .border(1.5.dp, border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = greenDark,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        "0",
                                        color = border.copy(alpha = 0.55f),
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                                inner()
                            }
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "数字のみ · Enter→次行",
                            fontSize = 9.sp,
                            color = OrderMonthlyColors.TextMuted,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        BatchRegisterQuantityBtn("取消", filled = false, green = green, onClick = onDismiss)
                        BatchRegisterQuantityBtn("確定", filled = true, green = green, onClick = ::submit)
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchRegisterQuantityBtn(
    label: String,
    filled: Boolean,
    green: Color,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .height(30.dp)
            .then(
                if (filled) {
                    Modifier
                        .shadow(4.dp, shape, spotColor = green.copy(alpha = 0.4f))
                        .clip(shape)
                        .background(Brush.linearGradient(listOf(Color(0xFF34D399), green)))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                } else {
                    Modifier
                        .clip(shape)
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, OrderMonthlyColors.BorderLight, shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else OrderMonthlyColors.TextMuted,
        )
    }
}

@Composable
private fun BatchRegisterStatusTag(exists: Boolean) {
    val (text, color, bg) = if (exists) {
        Triple("登録済", Color(0xFF047857), Color(0x1A10B981))
    } else {
        Triple("新規", Color(0xFF2563EB), Color(0x1A3B82F6))
    }
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(5.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Composable
private fun BatchRegisterConfirmDialog(
    createCount: Int,
    updateCount: Int,
    year: Int,
    month: Int,
    destinationLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val green = Color(0xFF059669)
    val greenLight = Color(0xFF34D399)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 411.dp)
                .fillMaxWidth(0.99f)
                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color(0x50059669))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BatchRegisterHeader(
                    productCount = createCount + updateCount,
                    filledCount = createCount + updateCount,
                    onDismiss = onDismiss,
                    title = "登録確認",
                    subtitle = "以下の内容で一括登録を実行",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, green.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        GenerateDailyInfoRow(label = "対象月", icon = Icons.Default.CalendarMonth, accent = green) {
                            Text("${year}年${month}月", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrderMonthlyColors.TextPrimary)
                        }
                        GenerateDailyInfoRow(label = "納入先", icon = Icons.Default.LocalShipping, accent = Color(0xFF6366F1), stripe = true) {
                            Text(destinationLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OrderMonthlyColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        GenerateDailyInfoRow(label = "新規登録", icon = Icons.Default.FileCopy, accent = Color(0xFF3B82F6)) {
                            GenerateDailyTag(text = "${createCount} 件", color = Color(0xFF2563EB), bg = Color(0x1A3B82F6))
                        }
                        GenerateDailyInfoRow(label = "更新", icon = Icons.Default.Refresh, accent = green, stripe = true) {
                            GenerateDailyTag(text = "${updateCount} 件", color = green, bg = Color(0x1A10B981))
                        }
                    }
                    Text(
                        "上記内容で一括登録を実行します。よろしいですか？",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF065F46),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                UpdateFieldsFooter(
                    confirmLabel = "登録実行",
                    confirmIcon = Icons.Default.Check,
                    accent = greenLight,
                    accentDark = green,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}
