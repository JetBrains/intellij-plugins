package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.LocalReportDescriptor
import org.jetbrains.qodana.report.QodanaLocalReportsService
import org.jetbrains.qodana.report.getHighlightedState
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.toStatsReportType

internal class OpenLocalReportAction(private val reportDescriptor: LocalReportDescriptor) : DumbAwareToggleAction() {
  companion object {
    fun getLocalReportsActions(project: Project): List<OpenLocalReportAction> {
      return QodanaLocalReportsService.getInstance(project).getReports().map {
        OpenLocalReportAction(it)
      }
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean {
    val project = e.project
    if (project == null) {
      e.presentation.isEnabledAndVisible = false
      return false
    }
    val highlightedState = reportDescriptor.getHighlightedState(project)
    e.presentation.text = reportDescriptor.getName(highlightedState)

    return when(highlightedState) {
      LocalReportDescriptor.HighlightedState.SELECTED, LocalReportDescriptor.HighlightedState.LOADING -> true
      LocalReportDescriptor.HighlightedState.NOT_SELECTED -> false
    }
  }

  override fun setSelected(e: AnActionEvent, newSelectedButtonState: Boolean) {
    val project = e.project ?: return
    val highlightedReportService = QodanaHighlightedReportService.getInstance(project)

    when(reportDescriptor.getHighlightedState(project)) {
      LocalReportDescriptor.HighlightedState.NOT_SELECTED -> {
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          highlightedReportService.highlightReport(reportDescriptor)
          logHighlightStats(project, true)
        }
      }
      else -> {
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          highlightedReportService.highlightReport(null)
          logHighlightStats(project, false)
        }
      }
    }
  }

  private fun logHighlightStats(project: Project, isHighlighted: Boolean) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      isHighlighted,
      reportDescriptor.toStatsReportType(),
      SourceHighlight.TOOLS_LIST
    )
  }
}