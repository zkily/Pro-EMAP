package com.example.smart_emap.ui.erp.production.requirements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors

@Composable
fun MonthQuickBar(
    enabled: Boolean,
    onQuick: (Long) -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F5F9))))
            .border(1.dp, Color(0x386366F1), shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickBtn("前月", Icons.AutoMirrored.Filled.KeyboardArrowLeft, true, enabled) { onQuick(-1) }
        Divider()
        QuickBtn("今月", Icons.Default.CalendarMonth, true, enabled) { onQuick(0) }
        Divider()
        QuickBtn("翌月", Icons.AutoMirrored.Filled.KeyboardArrowRight, false, enabled) { onQuick(1) }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(Color(0x386366F1)),
    )
}

@Composable
private fun QuickBtn(
    label: String,
    icon: ImageVector,
    iconFirst: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val color = if (enabled) Color(0xFF3730A3) else Color(0xFF94A3B8)
        if (iconFirst) {
            Icon(icon, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        if (!iconFirst) {
            Icon(icon, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun RequirementSearchButton(loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
        modifier = Modifier.height(34.dp),
    ) {
        Icon(Icons.Default.Search, null, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text("集計", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RequirementChips(chips: List<Triple<String, String, Color>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { (label, value, color) ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(color.copy(alpha = 0.10f))
                    .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(label, fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
        }
    }
}

@Composable
fun RequirementWarningBar(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("⚠️", fontSize = 13.sp)
        Text(text, fontSize = 11.sp, color = Color(0xFF92400E), lineHeight = 15.sp)
    }
}

@Suppress("unused")
private val outlineBorder = BorderStroke(1.dp, ProductionPlanningColors.CardBorder)
