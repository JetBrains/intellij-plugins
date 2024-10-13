package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable

interface MetricTableRowData {
  val filePath: String
  val metricTable: MetricTable
  fun getColumnsToValuesMapping(): Map<MetricTableColumn, Any>
}

fun MetricTableRowData.getValues(): Array<Any> = getColumnsToValuesMapping().entries
  .sortedBy { (column, _) -> column.index }
  .map { it.value }
  .toTypedArray()