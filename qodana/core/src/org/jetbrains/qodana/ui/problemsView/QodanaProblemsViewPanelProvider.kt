package org.jetbrains.qodana.ui.problemsView

import com.intellij.analysis.problemsView.toolWindow.ProblemsViewPanelProvider
import com.intellij.analysis.problemsView.toolWindow.ProblemsViewTab
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService

class QodanaProblemsViewPanelProvider(private val project: Project) : ProblemsViewPanelProvider {
  override fun create(): ProblemsViewTab {
    // if Qodana report is selected we need to initialize view because of the counter in tab name
    val highlightedReportState = QodanaHighlightedReportService.getInstanceIfCreated(project)?.highlightedReportState?.value
    return QodanaProblemsViewTab(
      project,
      initializeViewEagerly = highlightedReportState is HighlightedReportState.Selected
    )
  }
}