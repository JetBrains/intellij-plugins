package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.ui.problemsView.viewModel.*
import org.jetbrains.qodana.ui.run.wizard.RunQodanaWizard
import org.jetbrains.qodana.ui.settings.QodanaCloudSettingsPanel

internal class UiStateNotAuthorizedImpl(
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  override val ciState: QodanaProblemsViewModel.CiState,
) : QodanaProblemsViewModel.UiState.NotAuthorized {
  override fun authorize() {
    viewModelScope.launch(QodanaDispatchers.Ui) {
      QodanaCloudSettingsPanel.openSettings(project)
      QodanaPluginStatsCounterCollector.LOGIN_ACTION.log(toUiStateStatsType())
    }
  }

  override fun showRunDialog() {
    viewModelScope.launch(QodanaDispatchers.Ui) {
      RunQodanaWizard.create(project).show()
      QodanaPluginStatsCounterCollector.RUN_QODANA_ACTION.log(toUiStateStatsType())
    }
  }
}