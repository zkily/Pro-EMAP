package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_emap.data.repository.PlanInstructionRepository
import com.example.smart_emap.data.repository.MasterRepository

@Composable
fun FormingInstructionScreen(
    repository: PlanInstructionRepository,
    masterRepository: MasterRepository,
) {
    val viewModel: PlanInstructionViewModel = viewModel(
        key = PlanInstructionViewModel.VIEW_MODEL_KEY_FORMING,
        factory = PlanInstructionViewModel.Factory(
            repository = repository,
            masterRepository = masterRepository,
            config = PlanInstructionConfig.Forming,
        ),
    )
    PlanInstructionScreen(viewModel = viewModel)
}
