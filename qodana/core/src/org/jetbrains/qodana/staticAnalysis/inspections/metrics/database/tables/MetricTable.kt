package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables

import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.MetricTableColumn

interface MetricTable {
  companion object {
    val EP = ExtensionPointName<MetricTable>("org.intellij.qodana.metricTable")
  }

  val columns: Collection<MetricTableColumn>
  val tableName: String
  val filePathColumn: MetricTableColumn
  val indexColumn: MetricTableColumn?
}