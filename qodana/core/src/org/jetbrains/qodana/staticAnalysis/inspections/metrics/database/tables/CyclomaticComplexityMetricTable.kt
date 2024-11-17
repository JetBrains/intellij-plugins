package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumnType

class CyclomaticComplexityMetricTable : MetricTable {
  override val filePathColumn = MetricTableColumn("filePath", MetricTableColumnType.TEXT, 0)
  val methodNameColumn = MetricTableColumn("methodName", MetricTableColumnType.TEXT, 1)
  val cyclomaticComplexityValueColumn = MetricTableColumn("cyclomaticComplexityValue", MetricTableColumnType.INT, 2)
  val methodFileOffsetColumn = MetricTableColumn("methodFileOffset", MetricTableColumnType.INT, 3)

  override val columns: List<MetricTableColumn>
    get() = listOf(filePathColumn, methodNameColumn, cyclomaticComplexityValueColumn, methodFileOffsetColumn)

  override val tableName: String
    get() = "metrics_cyclomatic_complexity"

  override val indexColumn: MetricTableColumn
    get() = filePathColumn
}