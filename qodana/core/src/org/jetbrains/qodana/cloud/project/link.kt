package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.SourceLinkState
import org.jetbrains.qodana.stats.StatsReportType

suspend fun linkWithCloudProjectAndApply(
  project: Project,
  cloudProjectData: CloudProjectData,
  linkSource: SourceLinkState
) {
  val authorized = QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized ?: return
  val linked = when(val currentLinkState = QodanaCloudProjectLinkService.getInstance(project).linkState.value) {
    is LinkState.Linked -> {
      currentLinkState
    }
    is LinkState.NotLinked -> {
      val linked = currentLinkState.linkWithQodanaCloudProject(authorized, cloudProjectData)
      if (linked != null) {
        logLinkStats(project, linkSource)
      }
      linked
    }
  } ?: return
  val cloudReportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()
  QodanaHighlightedReportService.getInstance(project).highlightReport(cloudReportDescriptor)
  logHighlightOnLinkStats(project)
}

private fun logLinkStats(project: Project, linkSource: SourceLinkState) {
  QodanaPluginStatsCounterCollector.UPDATE_CLOUD_LINK.log(
    project,
    true,
    linkSource
  )
}

private fun logHighlightOnLinkStats(project: Project) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    true,
    StatsReportType.CLOUD,
    SourceHighlight.CLOUD_HIGHLIGHT_ON_LINK
  )
}