package org.jetbrains.qodana.ui.problemsView

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.util.NlsActions
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.actions.qodanaProblemsViewModel
import org.jetbrains.qodana.actions.qodanaProblemsViewPanel
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewState

class QodanaProblemsViewGroupByActionGroup : ActionGroup(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val uiState = e.qodanaProblemsViewModel?.uiStateFlow?.value
    val isLoadingOrLoaded = uiState is QodanaProblemsViewModel.UiState.LoadingReport || uiState is QodanaProblemsViewModel.UiState.Loaded
    e.presentation.isEnabled = isLoadingOrLoaded
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val viewModel = e?.qodanaProblemsViewModel ?: return emptyArray<AnAction>()
    return listOf(
      Separator(QodanaBundle.message("separator.Qodana.ProblemsView.Options.GroupBy")),
      QodanaProblemsViewGroupBySeverityAction(viewModel),
      QodanaProblemsViewGroupByInspectionAction(viewModel),
      QodanaProblemsViewGroupByModuleAction(viewModel),
      QodanaProblemsViewGroupByDirectoryAction(viewModel),

      Separator.getInstance(),

      QodanaProblemsViewShowBaselineAction(viewModel)
    ).toTypedArray()
  }
}

class QodanaProblemsViewGroupBySeverityAction(viewModel: QodanaProblemsViewModel) : QodanaProblemsViewToggleActionBase(
  viewModel,
  QodanaBundle.message("action.Qodana.ProblemsView.GroupBySeverity.text"),
  isSelected = { it.groupBySeverity },
  update = { viewState, doGroup -> viewState.copy(groupBySeverity = doGroup) }
)

class QodanaProblemsViewGroupByInspectionAction(viewModel: QodanaProblemsViewModel) : QodanaProblemsViewToggleActionBase(
  viewModel,
  QodanaBundle.message("action.Qodana.ProblemsView.GroupByInspection.text"),
  isSelected = { it.groupByInspection },
  update = { viewState, doGroup -> viewState.copy(groupByInspection = doGroup) }
)

class QodanaProblemsViewGroupByModuleAction(viewModel: QodanaProblemsViewModel) : QodanaProblemsViewToggleActionBase(
  viewModel,
  QodanaBundle.message("action.Qodana.ProblemsView.GroupByModule.text"),
  isSelected = { it.groupByModule },
  update = { viewState, doGroup -> viewState.copy(groupByModule = doGroup) }
) {
  override fun update(e: AnActionEvent) {
    val isGroupByModuleSupported = QodanaGroupByModuleSupportService.getInstance().isSupported.value
    if (!isGroupByModuleSupported) {
      e.presentation.isEnabledAndVisible = false
      return
    }
    super.update(e)
  }
}

class QodanaProblemsViewGroupByDirectoryAction(viewModel: QodanaProblemsViewModel) : QodanaProblemsViewToggleActionBase(
  viewModel,
  QodanaBundle.message("action.Qodana.ProblemsView.GroupByDirectory.text"),
  isSelected = { it.groupByDirectory },
  update = { viewState, doGroup -> viewState.copy(groupByDirectory = doGroup) }
)

class QodanaProblemsViewShowBaselineAction(viewModel: QodanaProblemsViewModel) : QodanaProblemsViewToggleActionBase(
  viewModel,
  QodanaBundle.message("action.Qodana.ProblemsView.ShowBaseline.text"),
  isSelected = { it.showBaselineProblems },
  update = { viewState, doShow -> viewState.copy(showBaselineProblems = doShow) }
)

class QodanaProblemsViewShowPreviewAction : DumbAware, ToggleOptionAction({ it.qodanaProblemsViewPanel?.showPreview }) {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(event: AnActionEvent) {
    val showPreviewOption = event.qodanaProblemsViewPanel?.showPreview
    if (showPreviewOption != null) {
      event.qodanaProblemsViewModel?.updateShowPreviewFlow(showPreviewOption.isSelected)
    }
    super.update(event)
  }
}

abstract class QodanaProblemsViewToggleActionBase(
  private val viewModel: QodanaProblemsViewModel,
  @NlsActions.ActionText text: String,
  private val isSelected: (QodanaProblemsViewState) -> Boolean,
  private val update: (QodanaProblemsViewState, Boolean) -> (QodanaProblemsViewState)
) : DumbAwareToggleAction(text) {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean {
    return isSelected.invoke(viewModel.problemsViewState.value)
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    viewModel.updateProblemsViewState { problemsViewState ->
      update.invoke(problemsViewState, state)
    }
  }
}