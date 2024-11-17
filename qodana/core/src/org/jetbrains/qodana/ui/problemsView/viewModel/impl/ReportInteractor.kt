package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.ReportDescriptor

class ReportInteractor(
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  private val reportDescriptor: ReportDescriptor,
) {
  fun refresh() {
    viewModelScope.launch(QodanaDispatchers.Default) {
      val refreshed = reportDescriptor.refreshReport() ?: return@launch
      QodanaHighlightedReportService.getInstance(project).highlightReport(refreshed, forceReload = true)
    }
  }

  fun close() {
    viewModelScope.launch(QodanaDispatchers.Default) {
      QodanaHighlightedReportService.getInstance(project).highlightReport(null)
    }
  }
}