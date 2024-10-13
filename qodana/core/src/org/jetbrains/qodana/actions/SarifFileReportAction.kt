package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.filetype.SarifFileType
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.highlightedReportDataIfSelected
import org.jetbrains.qodana.report.FileReportDescriptor
import org.jetbrains.qodana.report.openReportFromFileAndHighlight
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType
import java.nio.file.Path

class SarifFileReportAction : DumbAwareAction() {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.file
    if (project == null || file == null || file.fileType != SarifFileType) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val highlightedReportService = project.serviceIfCreated<QodanaHighlightedReportService>()
    if (highlightedReportService == null) {
      e.presentation.isEnabledAndVisible = true
      return
    }

    e.presentation.text = if (highlightedReportService.highlightedReportPath == file.toNioPath())
      QodanaBundle.message("file.sarif.close.report.action.text")
    else
      QodanaBundle.message("action.Qodana.SarifFileReportAction.text")
    e.presentation.isEnabledAndVisible = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.file ?: return

    val highlightedReportService = project.service<QodanaHighlightedReportService>()
    if (highlightedReportService.highlightedReportPath == file.toNioPath()) {
      project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
        highlightedReportService.highlightReport(null)
        logHighlightStats(project, false)
      }
    }
    else {
      project.qodanaProjectScope.launch(QodanaDispatchers.Ui) {
        if (openReportFromFileAndHighlight(project, file.toNioPath()) == null) return@launch

        logHighlightStats(project, true)
      }
    }
  }

  private fun logHighlightStats(project: Project, isHighlighted: Boolean) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      isHighlighted,
      StatsReportType.FILE,
      SourceHighlight.SARIF_FILE
    )
  }
}

private val QodanaHighlightedReportService.highlightedReportPath: Path?
  get() = (highlightedReportState.value.highlightedReportDataIfSelected?.sourceReportDescriptor as? FileReportDescriptor)?.reportPath

private val AnActionEvent.file: VirtualFile? get() = getData(CommonDataKeys.VIRTUAL_FILE)