package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database

enum class MetricTableColumnType(val typeName: String) {
  INT("INT"),
  TEXT("TEXT")
}

data class MetricTableColumn(val columnName: String, val columnType: MetricTableColumnType, val index: Int)
