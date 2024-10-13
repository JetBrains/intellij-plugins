package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.CyclomaticComplexityMetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable

data class CyclomaticComplexityMetricTableRowData(
  override val filePath: String, val methodName: String, val cyclomaticComplexityValue: Int, val methodFileOffset: Int
) : MetricTableRowData {
  override val metricTable: CyclomaticComplexityMetricTable
    get() = MetricTable.EP.findExtension(CyclomaticComplexityMetricTable::class.java)!!

  override fun getColumnsToValuesMapping(): Map<MetricTableColumn, Any> = mapOf(
    metricTable.filePathColumn to filePath,
    metricTable.methodNameColumn to methodName,
    metricTable.cyclomaticComplexityValueColumn to cyclomaticComplexityValue,
    metricTable.methodFileOffsetColumn to methodFileOffset
  )
}