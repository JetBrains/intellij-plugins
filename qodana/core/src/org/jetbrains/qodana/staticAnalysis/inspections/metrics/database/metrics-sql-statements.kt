package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable

fun MetricTable.getCreateStatement(): String {
  val columnNameTypes: String = columns.joinToString(separator = ",") { c -> "${c.columnName} ${c.columnType.typeName}" }
  return "CREATE TABLE $tableName ($columnNameTypes);"
}

fun MetricTable.getInsertStatement(): String {
  val placeholderString: String = columns.joinToString(separator = ", ") { "?" }
  return "INSERT OR REPLACE INTO $tableName VALUES($placeholderString);"
}

fun MetricTable.getDeleteStatementForFile(): String {
  return "DELETE FROM $tableName WHERE ${filePathColumn.columnName} = ?;"
}

fun MetricTable.getIndexStatement(): String? {
  return indexColumn?.let { index ->
    "CREATE INDEX idx_${tableName}_file_path ON $tableName (${index.columnName});"
  }
}

fun MetricTable.getSumStatement(sumColumn: MetricTableColumn): String {
  return "SELECT SUM(${sumColumn.columnName}) FROM $tableName;"
}

fun MetricTable.Companion.getSchemaForAllTables(): String {
  val tables: List<MetricTable> = EP.extensionList
  val createStatements: String = tables.joinToString(separator = "") { it.getCreateStatement() }
  val indexStatements: String = tables.joinToString(separator = "") { it.getIndexStatement() ?: "" }
  return """
    BEGIN TRANSACTION;
    $createStatements
    $indexStatements
    COMMIT;
    """.trimIndent()
}