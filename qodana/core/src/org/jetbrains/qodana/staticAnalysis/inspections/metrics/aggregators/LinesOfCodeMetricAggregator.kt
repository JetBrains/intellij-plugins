package org.jetbrains.qodana.staticAnalysis.inspections.metrics.aggregators

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getResultOfQueryFromMetricsTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getSumStatement
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.LinesOfCodeMetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.LinesOfCodeMetricResult
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.MetricResult
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase

class LinesOfCodeMetricAggregator : MetricAggregator {
  private val table: LinesOfCodeMetricTable? = MetricTable.EP.findExtension(LinesOfCodeMetricTable::class.java)

  override suspend fun getData(database: QodanaToolResultDatabase): MetricResult? {
    if (table == null) return null
    val resultsFromTable: Flow<Int> = database.getResultOfQueryFromMetricsTable(
      sqlQuery = table.getSumStatement(table.numberOfLinesColumn),
      numberOfColumns = 1
    ) { array ->
      Integer.parseInt(array[0])
    }
    val linesOfCode: Int? = resultsFromTable.firstOrNull()
    return linesOfCode?.let { LinesOfCodeMetricResult(metricsValue = it) }
  }
}