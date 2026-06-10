package com.example.smart_emap.ui.system.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.OrganizationTreeNodeDto
import com.example.smart_emap.ui.system.user.DialogPrimaryButton
import com.example.smart_emap.ui.system.user.DialogTextButton

private val orgTypes = listOf(
    "company" to "会社",
    "site" to "拠点",
    "department" to "部門",
    "section" to "課",
    "line" to "ライン",
)

@Composable
fun OrganizationFormDialog(
    isEdit: Boolean,
    form: OrganizationFormState,
    orgTree: List<OrganizationTreeNodeDto>,
    isSubmitting: Boolean,
    onFormChange: ((OrganizationFormState) -> OrganizationFormState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val parentOptions = remember(orgTree) {
        listOf(null to "（ルート）") + flattenOrgTree(orgTree).map { (id, name) -> id to name }
    }
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .shadow(14.dp, OrganizationTheme.shapePage, spotColor = OrganizationTheme.ShadowSoft),
            shape = OrganizationTheme.shapePage,
            color = OrganizationTheme.CardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.Border),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OrganizationTheme.headerGradient)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            if (isEdit) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            if (isEdit) "組織編集" else "組織追加",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    OrgFormField(
                        label = "組織コード",
                        value = form.code,
                        enabled = !isEdit,
                        onValueChange = { onFormChange { f -> f.copy(code = it) } },
                    )
                    OrgFormField(
                        label = "組織名",
                        value = form.name,
                        onValueChange = { onFormChange { f -> f.copy(name = it) } },
                    )
                    OrgTypeDropdown(
                        value = form.type,
                        onSelect = { onFormChange { f -> f.copy(type = it) } },
                    )
                    OrgParentDropdown(
                        value = parentOptions.find { it.first == form.parentId }?.second ?: "（ルート）",
                        options = parentOptions,
                        onSelect = { id -> onFormChange { f -> f.copy(parentId = id) } },
                    )
                    OrgFormField(
                        label = "責任者",
                        value = form.managerName,
                        onValueChange = { onFormChange { f -> f.copy(managerName = it) } },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        OrgFormField(
                            label = "所在地",
                            value = form.location,
                            modifier = Modifier.weight(1f),
                            onValueChange = { onFormChange { f -> f.copy(location = it) } },
                        )
                        OrgFormField(
                            label = "電話",
                            value = form.phone,
                            modifier = Modifier.weight(1f),
                            onValueChange = { onFormChange { f -> f.copy(phone = it) } },
                        )
                    }
                    OrgFormField(
                        label = "メール",
                        value = form.email,
                        onValueChange = { onFormChange { f -> f.copy(email = it) } },
                    )
                    OrgFormField(
                        label = "説明",
                        value = form.description,
                        singleLine = false,
                        onValueChange = { onFormChange { f -> f.copy(description = it) } },
                    )
                }
                HorizontalDivider(color = OrganizationTheme.Border)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    DialogTextButton(text = "キャンセル", onClick = onDismiss, enabled = !isSubmitting)
                    DialogPrimaryButton(
                        text = "保存",
                        isSubmitting = isSubmitting,
                        onClick = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrgFormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextSecondary)
        Surface(
            shape = OrganizationTheme.shapeChip,
            color = if (enabled) Color.White else Color(0xFFF1F5F9),
            border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.Border),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = singleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = if (singleLine) 5.dp else 6.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = OrganizationTheme.TextPrimary),
                cursorBrush = SolidColor(Color(0xFF667EEA)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrgTypeDropdown(value: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text("種類", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextSecondary)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = OrganizationTheme.shapeChip,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.Border),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(orgTypeLabel(value), modifier = Modifier.weight(1f), fontSize = 11.sp)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                orgTypes.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text(label, fontSize = 11.sp) },
                        onClick = { onSelect(code); expanded = false },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrgParentDropdown(
    value: String,
    options: List<Pair<Int?, String>>,
    onSelect: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text("親組織", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = OrganizationTheme.TextSecondary)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = OrganizationTheme.shapeChip,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, OrganizationTheme.Border),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(value, modifier = Modifier.weight(1f), fontSize = 10.sp, maxLines = 1)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name, fontSize = 11.sp) },
                        onClick = { onSelect(id); expanded = false },
                    )
                }
            }
        }
    }
}
