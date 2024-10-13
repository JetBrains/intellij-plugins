package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.LinesOfCodeMetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable

data class LinesOfCodeMetricTableRowData(override val filePath: String, val numberOfLines: Int) : MetricTableRowData {
  override val metricTable: LinesOfCodeMetricTable
    get() = MetricTable.EP.findExtension(LinesOfCodeMetricTable::class.java)!!

  override fun getColumnsToValuesMapping(): Map<MetricTableColumn, Any> = mapOf(
    metricTable.filePathColumn to filePath,
    metricTable.numberOfLinesColumn to numberOfLines
  )
}