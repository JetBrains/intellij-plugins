package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import org.jetbrains.qodana.run.QodanaRunState
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

class UiStateRunningQodanaImpl(private val running: QodanaRunState.Running) : QodanaProblemsViewModel.UiState.RunningQodana {
  override fun cancel() {
    running.cancel()
  }
}