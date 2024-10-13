package org.jetbrains.qodana.staticAnalysis.inspections.metrics.aggregators

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getResultOfQueryFromMetricsTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getSumStatement
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.CyclomaticComplexityMetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.CyclomaticComplexityMetricResult
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.MetricResult
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase

class CyclomaticComplexityMetricAggregator: MetricAggregator {
  private val table: CyclomaticComplexityMetricTable? = MetricTable.EP.findExtension(CyclomaticComplexityMetricTable::class.java)

  override suspend fun getData(database: QodanaToolResultDatabase): MetricResult? {
    if (table == null) return null
    val resultsFromTable: Flow<Int> = database.getResultOfQueryFromMetricsTable(
      sqlQuery = table.getSumStatement(table.cyclomaticComplexityValueColumn),
      numberOfColumns = 1
    ) { array ->
      Integer.parseInt(array[0])
    }
    val cyclomaticComplexity: Int? = resultsFromTable.firstOrNull()
    return cyclomaticComplexity?.let { CyclomaticComplexityMetricResult(metricsValue = it) }
  }
}