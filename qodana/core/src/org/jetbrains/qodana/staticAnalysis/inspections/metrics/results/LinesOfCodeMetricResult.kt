package org.jetbrains.qodana.staticAnalysis.inspections.metrics.results

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics

class LinesOfCodeMetricResult(override val metricsValue: Int) : MetricResult {
  override val metricsName: String
    get() = "Lines of code"

  override fun mapToProperties(): Pair<CodeQualityMetrics, Int> = Pair(
    CodeQualityMetrics.LINES_OF_CODE, metricsValue
  )
}