package org.jetbrains.qodana.staticAnalysis.inspections.metrics.aggregators

import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.MetricResult
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase

interface MetricAggregator {
  companion object {
    val EP = ExtensionPointName<MetricAggregator>("org.intellij.qodana.metricsAggregator")
  }

  suspend fun getData(database: QodanaToolResultDatabase): MetricResult?
}