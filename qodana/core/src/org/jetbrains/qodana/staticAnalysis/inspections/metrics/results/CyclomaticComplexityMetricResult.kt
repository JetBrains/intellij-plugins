package org.jetbrains.qodana.staticAnalysis.inspections.metrics.results

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics

class CyclomaticComplexityMetricResult(override val metricsValue: Any) : MetricResult {
  override val metricsName: String
    get() = "Cyclomatic complexity"

  override fun mapToProperties(): Pair<CodeQualityMetrics, Any> = Pair(
    CodeQualityMetrics.CYCLOMATIC_COMPLEXITY, metricsValue
  )
}