package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.reportDescriptorIfSelectedOrLoading
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.toStatsReportType
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

class UiStateLoadingReportImpl(
  private val project: Project,
  private val reportInteractor: ReportInteractor
) : QodanaProblemsViewModel.UiState.LoadingReport {
  override fun cancel() {
    reportInteractor.close()
    logCancelReportLoadingStats()
  }

  override fun refreshReport() {
    reportInteractor.refresh()
  }

  override fun closeReport() {
    reportInteractor.close()
  }

  private fun logCancelReportLoadingStats() {
    val highlightedReportState = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value
    val reportDescriptor = highlightedReportState.reportDescriptorIfSelectedOrLoading ?: return

    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      false,
      reportDescriptor.toStatsReportType(),
      SourceHighlight.QODANA_PANEL_CANCEL_LOADING
    )
  }
}