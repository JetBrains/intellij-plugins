package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.ui.link.LinkCloudProjectDialog
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.ui.problemsView.viewModel.toUiStateStatsType


class UiStateNotLinkedImpl(
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  override val authorized: QodanaProblemsViewModel.AuthorizedState,
  override val ciState: QodanaProblemsViewModel.CiState,
) : QodanaProblemsViewModel.UiState.NotLinked {
  override fun showLinkDialog() {
    viewModelScope.launch(QodanaDispatchers.Ui) {
      LinkCloudProjectDialog(project).show()
      QodanaPluginStatsCounterCollector.LINK_PROJECT_ACTION.log(toUiStateStatsType())
    }
  }
}