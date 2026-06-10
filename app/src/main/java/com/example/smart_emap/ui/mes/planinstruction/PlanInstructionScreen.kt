package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PlanInstructionScreen(viewModel: PlanInstructionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val subject = uiState.pendingPrintSubject ?: viewModel.config.instructionPrintTitle
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = subject,
            layout = uiState.pendingPrintLayout,
        )
        viewModel.clearPendingPrint()
        snackbarHostState.showSnackbar(
            if (opened) "印刷用ウィンドウを開きました" else "印刷画面を開けませんでした",
        )
    }

    PlanInstructionDialogHost(
        dialog = uiState.activeDialog,
        uiState = uiState,
        config = viewModel.config,
        viewModel = viewModel,
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.linearGradient(
                        listOf(
                            PlanInstructionTheme.PageBgStart,
                            PlanInstructionTheme.PageBgEnd,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PlanInstructionPageContent(
                    config = viewModel.config,
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }
        }
    }
}
