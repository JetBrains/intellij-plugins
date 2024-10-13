package org.jetbrains.qodana.highlight

import org.jetbrains.qodana.report.ReportDescriptor

/**
 * State of currently highlighted report
 */
sealed interface HighlightedReportState {
  /** Selected, highlights are applied */
  interface Selected : HighlightedReportState {
    val highlightedReportData: HighlightedReportData
  }

  /** Report is being loaded from [sourceReportDescriptor] */
  interface Loading : HighlightedReportState {
    val sourceReportDescriptor: ReportDescriptor
  }

  /** No report is highlighted */
  object NotSelected : HighlightedReportState
}

val HighlightedReportState.highlightedReportDataIfSelected: HighlightedReportData?
  get() = (this as? HighlightedReportState.Selected)?.highlightedReportData

val HighlightedReportState.reportDescriptorIfSelectedOrLoading: ReportDescriptor?
  get() {
    return when(this) {
      is HighlightedReportState.Selected -> this.highlightedReportData.sourceReportDescriptor
      is HighlightedReportState.Loading -> this.sourceReportDescriptor
      is HighlightedReportState.NotSelected -> null
    }
  }