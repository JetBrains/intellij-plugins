package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.qodana.ui.problemsView.QodanaProblemsViewPanel
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

val AnActionEvent.qodanaProblemsViewModel: QodanaProblemsViewModel?
  get() = getData(QodanaProblemsViewModel.DATA_KEY)

val AnActionEvent.qodanaProblemsViewPanel: QodanaProblemsViewPanel?
  get() = getData(QodanaProblemsViewPanel.DATA_KEY)