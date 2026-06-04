package org.jetbrains.qodana.stats

import com.intellij.openapi.project.Project

fun logSarifFileHighlightStats(project: Project, isHighlighted: Boolean) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    isHighlighted,
    StatsReportType.FILE,
    SourceHighlight.SARIF_FILE
  )
}
