package org.jetbrains.qodana.actions

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.currentlyHighlightedReportStatsType

internal class ProblemsViewRefreshReportAction : ProblemsViewReportActionBase(
  loadingActionPerformed = { project, loadingReport ->
    logHighlightReportStats(project)
    loadingReport.refreshReport()
  },
  loadedActionPerformed = { project, loaded ->
    logHighlightReportStats(project)
    loaded.refreshReport()
  }
)

private fun logHighlightReportStats(project: Project) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    true,
    currentlyHighlightedReportStatsType(project),
    SourceHighlight.CLOUD_REFRESH_ACTION_PANEL
  )
}