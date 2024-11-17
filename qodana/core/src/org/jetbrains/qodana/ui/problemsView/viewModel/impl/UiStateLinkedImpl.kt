package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import com.intellij.util.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.projectFrontendUrlForQodanaCloud
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

class UiStateLinkedImpl(
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  private val linked: LinkState.Linked,
  override val ciState: QodanaProblemsViewModel.CiState,
  private val availableReportId: String?,
  override val authorized: QodanaProblemsViewModel.AuthorizedState,
) : QodanaProblemsViewModel.UiState.Linked {
  private val qodanaCloudUrl: String
    get() = authorized.qodanaCloudUrl.toString()

  override val cloudProjectUrl: Url
    get() = projectFrontendUrlForQodanaCloud(linked.projectDataProvider.projectId, reportId = null, qodanaCloudUrl)

  override val availableReportUrl: Url?
    get() = availableReportId?.let { projectFrontendUrlForQodanaCloud(linked.projectDataProvider.projectId, availableReportId, qodanaCloudUrl) }

  override val cloudProjectName: String
    get() {
      return linked.projectDataProvider.projectProperties.value.lastLoadedValue?.asSuccess()?.name
             ?: linked.projectDataProvider.projectPrimaryData.id
    }

  override fun openReport() {
    val highlightedReportService = QodanaHighlightedReportService.getInstance(project)
    viewModelScope.launch(QodanaDispatchers.Default) {
      val cloudReportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()
      highlightedReportService.highlightReport(cloudReportDescriptor)
      QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
        project,
        true,
        StatsReportType.CLOUD,
        SourceHighlight.PROBLEMS_VIEW_OPEN_REPORT
      )
    }
  }
  override fun unlink() {
    linked.unlink()
  }
}
