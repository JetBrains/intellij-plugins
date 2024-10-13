package org.jetbrains.qodana.staticAnalysis.inspections.metrics.results

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics

interface MetricResult {
  val scope: Scope
    get() = Scope.PROJECT

  val metricsName: String
  val metricsValue: Any

  fun mapToProperties(): Pair<CodeQualityMetrics, Any>

  enum class Scope(val text: String) {
    PROJECT("project"),
    FILE("file")
  }
}