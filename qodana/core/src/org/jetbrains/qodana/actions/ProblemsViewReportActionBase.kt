package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

abstract class ProblemsViewReportActionBase(
  private val loadingActionPerformed: (Project, QodanaProblemsViewModel.UiState.LoadingReport) -> Unit,
  private val loadedActionPerformed: (Project, QodanaProblemsViewModel.UiState.Loaded) -> Unit,
) : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val uiState = e.qodanaProblemsViewModel?.uiStateFlow?.value
    val isLoadingOrLoaded = uiState is QodanaProblemsViewModel.UiState.LoadingReport || uiState is QodanaProblemsViewModel.UiState.Loaded
    e.presentation.isEnabled = isLoadingOrLoaded
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val uiState = e.qodanaProblemsViewModel?.uiStateFlow?.value
    when(uiState) {
      is QodanaProblemsViewModel.UiState.Loaded -> loadedActionPerformed.invoke(project, uiState)
      is QodanaProblemsViewModel.UiState.LoadingReport -> loadingActionPerformed.invoke(project, uiState)
      else -> {}
    }
  }
}