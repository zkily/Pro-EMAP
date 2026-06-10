package com.example.smart_emap.ui.mes.cuttinginstruction

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CompactBreakpoint = 900.dp
private val WideBreakpoint = 1280.dp

data class CuttingInstructionLayoutMode(
    val isPortrait: Boolean,
    val stackDualPanels: Boolean,
    val lotListWeight: Float,
    val detailWeight: Float,
    val todayWeight: Float,
    val tomorrowWeight: Float,
    val chamferPlanWeight: Float,
    val chamferEfficiencyWeight: Float,
    val contentPaddingHorizontal: Dp,
    val contentPaddingVertical: Dp,
    val sectionSpacing: Dp,
    val headerCompact: Boolean,
)

fun resolveCuttingInstructionLayout(
    maxWidth: Dp,
    orientation: Int,
): CuttingInstructionLayoutMode {
    val isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
    val stackDualPanels = isPortrait || maxWidth < CompactBreakpoint
    val isWide = !isPortrait && maxWidth >= WideBreakpoint

    return when {
        stackDualPanels -> CuttingInstructionLayoutMode(
            isPortrait = isPortrait,
            stackDualPanels = true,
            lotListWeight = 1f,
            detailWeight = 1f,
            todayWeight = 1f,
            tomorrowWeight = 1f,
            chamferPlanWeight = 1f,
            chamferEfficiencyWeight = 1f,
            contentPaddingHorizontal = 8.dp,
            contentPaddingVertical = 8.dp,
            sectionSpacing = 10.dp,
            headerCompact = true,
        )
        isWide -> CuttingInstructionLayoutMode(
            isPortrait = false,
            stackDualPanels = false,
            lotListWeight = 0.8f,
            detailWeight = 0.2f,
            todayWeight = 0.7f,
            tomorrowWeight = 0.3f,
            chamferPlanWeight = 0.65f,
            chamferEfficiencyWeight = 0.35f,
            contentPaddingHorizontal = 10.dp,
            contentPaddingVertical = 8.dp,
            sectionSpacing = 10.dp,
            headerCompact = false,
        )
        else -> CuttingInstructionLayoutMode(
            isPortrait = false,
            stackDualPanels = false,
            lotListWeight = 0.72f,
            detailWeight = 0.28f,
            todayWeight = 0.62f,
            tomorrowWeight = 0.38f,
            chamferPlanWeight = 0.6f,
            chamferEfficiencyWeight = 0.4f,
            contentPaddingHorizontal = 10.dp,
            contentPaddingVertical = 8.dp,
            sectionSpacing = 10.dp,
            headerCompact = maxWidth < 1024.dp,
        )
    }
}

@Composable
fun CuttingInstructionDualPanelRow(
    layout: CuttingInstructionLayoutMode,
    primaryWeight: Float,
    secondaryWeight: Float,
    modifier: Modifier = Modifier,
    matchHeight: Boolean = true,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
) {
    if (layout.stackDualPanels) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
        ) {
            Column(Modifier.fillMaxWidth()) { primary() }
            Column(Modifier.fillMaxWidth()) { secondary() }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(if (matchHeight) Modifier.height(IntrinsicSize.Max) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
        ) {
            Column(
                Modifier
                    .weight(primaryWeight)
                    .then(if (matchHeight) Modifier.fillMaxHeight() else Modifier),
            ) {
                primary()
            }
            Column(
                Modifier
                    .weight(secondaryWeight)
                    .then(if (matchHeight) Modifier.fillMaxHeight() else Modifier),
            ) {
                secondary()
            }
        }
    }
}
