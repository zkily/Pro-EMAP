package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog

private val InventoryFieldAccent = Color(0xFF667EEA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowEmpty: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = options.find { it.first == value }?.second ?: if (value.isBlank()) "" else value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = textAlign, fontSize = 11.sp) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 12.sp, textAlign = textAlign),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InventoryFieldAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
            ),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowEmpty) {
                DropdownMenuItem(
                    text = { Text("—", fontSize = 12.sp) },
                    onClick = { onSelect(""); expanded = false },
                )
            }
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = { onSelect(id); expanded = false },
                )
            }
        }
    }
}

@Composable
fun InventoryTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = TextStyle(fontSize = 12.sp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = InventoryFieldAccent),
    )
}

@Composable
fun InventoryConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "確認",
    loading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.98f),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(message, fontSize = 13.sp, color = Color(0xFF64748B))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    Button(
                        onClick = onConfirm,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = InventoryFieldAccent),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(confirmText)
                        }
                    }
                }
            }
        }
    }
}
