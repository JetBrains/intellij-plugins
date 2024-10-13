package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumnType

class LinesOfCodeMetricTable : MetricTable {
  private val fileColumn = MetricTableColumn("filePath", MetricTableColumnType.TEXT, 0)
  val numberOfLinesColumn = MetricTableColumn("numberOfLines", MetricTableColumnType.INT, 1)

  override val columns: List<MetricTableColumn>
    get() = listOf(fileColumn, numberOfLinesColumn)

  override val tableName: String
    get() = "metrics_lines_of_code"

  override val filePathColumn: MetricTableColumn
    get() = fileColumn

  override val indexColumn: MetricTableColumn
    get() = filePathColumn
}
