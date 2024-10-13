package org.jetbrains.qodana.actions

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.currentlyHighlightedReportStatsType

internal class ProblemsViewCloseReportAction : ProblemsViewReportActionBase(
  loadingActionPerformed = { project, loadingReport ->
    logUnhighlightReportStats(project)
    loadingReport.closeReport()
  },
  loadedActionPerformed = { project, loaded ->
    logUnhighlightReportStats(project)
    loaded.closeReport()
  }
)

private fun logUnhighlightReportStats(project: Project) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    false,
    currentlyHighlightedReportStatsType(project),
    SourceHighlight.CLOSE_ACTION_PANEL
  )
}