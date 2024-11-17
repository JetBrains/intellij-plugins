package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.reportDescriptorIfSelectedOrLoading
import org.jetbrains.qodana.protocol.OpenInIdeCloudReportDescriptor
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType

internal class OpenQodanaCloudOpenInIdeReportAction : DumbAwareToggleAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean {
    val project = e.project
    if (project == null) {
      e.presentation.isEnabledAndVisible = false
      return false
    }

    // do not load state if not loaded
    val reportService = project.serviceIfCreated<QodanaHighlightedReportService>()
    if (reportService == null) {
      e.presentation.isEnabledAndVisible = false
      return false
    }

    val currentReportDescriptor = reportService.highlightedReportState.value.reportDescriptorIfSelectedOrLoading
    if (currentReportDescriptor !is OpenInIdeCloudReportDescriptor) {
      e.presentation.isEnabledAndVisible = false
      return false
    }

    val linked = currentReportDescriptor.linkedState()
    if (linked != null) {
      e.presentation.isEnabledAndVisible = false
      return false
    }

    e.presentation.text = QodanaBundle.message("action.open.qodana.cloud.open.in.ide.report.action", currentReportDescriptor.projectName ?: "")
    return true
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val project = e.project ?: return
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      QodanaHighlightedReportService.getInstance(project).highlightReport(null)
      logUnhighlightStats(project)
    }
  }

  private fun logUnhighlightStats(project: Project) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      false,
      StatsReportType.OPEN_IN_IDE_CLOUD_REPORT,
      SourceHighlight.TOOLS_LIST
    )
  }
}