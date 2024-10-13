package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService

interface LocalReportDescriptor : ReportDescriptor {
  enum class HighlightedState {
    SELECTED, LOADING, NOT_SELECTED
  }

  /** Report name (shown in the drop-down list of reports) */
  fun getName(highlightedState: HighlightedState): @NlsSafe String

  fun checkAvailability(): Boolean

  fun markAsUnavailable()
}

fun LocalReportDescriptor.getHighlightedState(project: Project): LocalReportDescriptor.HighlightedState {
  when (val highlightedReportState = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value) {
    is HighlightedReportState.Selected -> {
      if (this == highlightedReportState.highlightedReportData.sourceReportDescriptor) return LocalReportDescriptor.HighlightedState.SELECTED
    }
    is HighlightedReportState.Loading -> {
      if (this == highlightedReportState.sourceReportDescriptor) return LocalReportDescriptor.HighlightedState.LOADING
    }
    else -> {}
  }
  return LocalReportDescriptor.HighlightedState.NOT_SELECTED
}