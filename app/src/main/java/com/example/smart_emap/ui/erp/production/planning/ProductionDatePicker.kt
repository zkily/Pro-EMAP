package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.smart_emap.ui.common.BeautifulDatePickerCompactChip
import com.example.smart_emap.ui.common.BeautifulDatePickerDialog
import com.example.smart_emap.ui.common.BeautifulDatePickerLabeledCompactField
import com.example.smart_emap.ui.common.BeautifulDateRangeCompactField
import com.example.smart_emap.ui.common.BeautifulDateRangeField

@Composable
fun ProductionSingleDatePickerField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = label,
) {
    BeautifulDatePickerLabeledCompactField(
        value = value,
        onValueChange = onChange,
        label = label,
        placeholder = placeholder,
        accentColor = ProductionPlanningColors.AccentBlue,
        modifier = modifier,
    )
}

@Composable
fun ProductionCompactDateRangeField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BeautifulDateRangeCompactField(
        startDate = startDate,
        endDate = endDate,
        onStartChange = onStartChange,
        onEndChange = onEndChange,
        accentColor = ProductionPlanningColors.AccentBlue,
        modifier = modifier,
    )
}

@Composable
fun ProductionDateRangePickerField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    startLabel: String = "開始日",
    endLabel: String = "終了日",
) {
    BeautifulDateRangeField(
        startDate = startDate,
        endDate = endDate,
        onStartChange = onStartChange,
        onEndChange = onEndChange,
        accentColor = ProductionPlanningColors.AccentBlue,
        startLabel = startLabel,
        endLabel = endLabel,
        modifier = modifier,
    )
}

@Composable
fun ProductionDateChip(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BeautifulDatePickerCompactChip(
        value = value,
        placeholder = placeholder,
        onClick = onClick,
        accentColor = ProductionPlanningColors.AccentBlue,
        modifier = modifier,
    )
}

@Composable
fun ProductionBeautifulDatePickerDialog(
    value: String,
    title: String = "日付を選択",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    BeautifulDatePickerDialog(
        value = value,
        title = title,
        accentColor = ProductionPlanningColors.AccentBlue,
        confirmLabel = "確定",
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}
