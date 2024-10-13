package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.util.io.NioFiles
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getDeleteStatementForFile
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getInsertStatement
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.getSchemaForAllTables
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.MetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.getValues
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable
import org.jetbrains.sqlite.EmptyBinder
import org.jetbrains.sqlite.ObjectBinder
import org.jetbrains.sqlite.SqliteConnection
import org.jetbrains.sqlite.SqlitePreparedStatement
import java.nio.file.Path

@Language("SQLite")
private const val TABLE_SCHEMA = """
  BEGIN TRANSACTION;

  CREATE TABLE results (
    inspectionGroup TEXT,
    inspection TEXT,
    hash TEXT,
    json TEXT
  );
  
  CREATE INDEX idx_results_inspection_hash ON results (inspection, hash);
  
  CREATE TABLE duplicates (
    file TEXT,
    line INT,
    start INT,
    end INT,
    hash TEXT,
    json TEXT
  );
  
  CREATE INDEX idx_duplicates_location ON duplicates (file, line, start, end);
  
  CREATE TABLE related_problem (
    hash TEXT,
    json TEXT
  );
  
  CREATE INDEX idx_related_problem_hash ON related_problem (hash);
  
  COMMIT;
"""

private const val INSERT_STATEMENT = "INSERT INTO results VALUES (?, ?, ?, ?);"
private const val SELECT_STATEMENT = "SELECT inspection, hash, json FROM results WHERE inspectionGroup = ? ORDER BY inspection, hash;"

private const val SELECT_INSPECTION_IDS = "SELECT DISTINCT inspection FROM results;"

private const val INSERT_DUPLICATES_STATEMENT = "INSERT INTO duplicates VALUES (?, ?, ?, ?, ?, ?);"
private const val SELECT_DUPLICATES_STATEMENT = "SELECT json FROM duplicates WHERE file = ? AND line = ? AND start = ? ORDER BY hash;"

private const val INSERT_RELATED_PROBLEM = "INSERT INTO related_problem VALUES (?, ?);"
private const val SELECT_RELATED_PROBLEM = "SELECT json FROM related_problem WHERE hash = ?;"

internal const val QODANA_DB_FILENAME = "tool-results.db"

/**
 * SQLite based database with thread-safe interface that holds inspection tool execution results.
 * Connection is established only if [insert] or [select] methods are called.
 * Provided methods are synchronous, but if [close] is called, it terminates all the queries that were not fully executed.
 * Factory method provide:
 *  1) the ability to create a tool result database. When file with such database exists, it will be overriden.
 *  2) the ability to re-open previously closed database.
 *  Note, there is no lock file provided, that prevents opening non-closed DB.
 */
class QodanaToolResultDatabase private constructor(private val connection: SqliteConnection) : AutoCloseable by connection {
  companion object {
    fun create(path: Path): QodanaToolResultDatabase {
      NioFiles.deleteRecursively(path.resolve(QODANA_DB_FILENAME))
      val db = open(path)
      db.connection.execute(TABLE_SCHEMA)
      @Language("SQLite") val createMetricsTableSchema: String = MetricTable.getSchemaForAllTables()
      db.connection.execute(createMetricsTableSchema)
      return db
    }

    fun open(path: Path) =
      QodanaToolResultDatabase(SqliteConnection(path.resolve(QODANA_DB_FILENAME)))
  }

  fun insert(inspectionGroup: String, inspectionId: String, hash: String, json: String) {
    connection.execute(INSERT_STATEMENT, arrayOf(inspectionGroup, inspectionId, hash, json))
  }

  fun insertDuplicate(file: String, line: Int, start: Int, end: Int, hash: String, json: String) {
    connection.execute(INSERT_DUPLICATES_STATEMENT, arrayOf(file, line, start, end, hash, json))
  }

  fun insertMetricsData(rowData: MetricTableRowData) {
    val metricTable: MetricTable = rowData.metricTable
    @Language("SQLite") val insertStatement: String = metricTable.getInsertStatement()
    connection.execute(insertStatement, rowData.getValues())
  }

  fun deleteMetricsDataForFile(filePath: String, metricTable: MetricTable) {
    @Language("SQLite") val deleteStatement: String = metricTable.getDeleteStatementForFile()
    connection.execute(deleteStatement, arrayOf(filePath))
  }

  fun select(inspectionGroup: String): ToolResultsClosableQuery {
    val binder = ObjectBinder(paramCount = 1)
    val statement = connection.prepareStatement(SELECT_STATEMENT, binder)
    binder.bind(inspectionGroup)
    return ToolResultsClosableQuery(statement)
  }

  fun selectDuplicate(file: String, line: Int, start: Int): StringColumnClosableQuery {
    val binder = ObjectBinder(paramCount = 3)
    val statement = connection.prepareStatement(SELECT_DUPLICATES_STATEMENT, binder)
    binder.bindMultiple(file, line, start)
    return StringColumnClosableQuery(statement)
  }

  fun getResultsFromMetricsTable(@Language("SQLite") query: String, numberOfColumns: Int): MetricColumnClosableQuery {
    val statement = connection.prepareStatement(query, EmptyBinder)
    return MetricColumnClosableQuery(statement, numberOfColumns)
  }

  fun insertRelatedProblem(hash: String, json: String) {
    connection.execute(INSERT_RELATED_PROBLEM, arrayOf(hash, json))
  }

  fun selectRelatedProblems(hash: String): StringColumnClosableQuery {
    val binder = ObjectBinder(paramCount = 1)
    val statement = connection.prepareStatement(SELECT_RELATED_PROBLEM, binder)
    binder.bindMultiple(hash)
    return StringColumnClosableQuery(statement)
  }

  fun selectTriggeredInspectionIds(): StringColumnClosableQuery =
    StringColumnClosableQuery(connection.prepareStatement(SELECT_INSPECTION_IDS, EmptyBinder))

  class MetricColumnClosableQuery internal constructor(
    private val statement: SqlitePreparedStatement<*>, private val numberOfColumns: Int,
  ) : AutoCloseable {
    fun executeQuery(): Sequence<Array<String>> = sequence {
      val resultSet = statement.executeQuery()
      while (resultSet.next()) {
        try {
          val values = Array(numberOfColumns) { index ->
            resultSet.getString(index)!!
          }
          yield(values)
        } catch (e: Exception) {
          yield(emptyArray())
        }
      }
    }

    override fun close() {
      statement.close()
    }
  }

  class StringColumnClosableQuery internal constructor(private val statement: SqlitePreparedStatement<*>) : AutoCloseable {
    /**
     * Provides iterable for sequential access to the underlying result set with tool results
     */
    fun executeQuery() = sequence {
      val resultSet = statement.executeQuery()
      while (resultSet.next()) {
        yield(resultSet.getString(0)!!)
      }
    }

    override fun close() {
      statement.close()
    }
  }

  class ToolResultsClosableQuery internal constructor(private val statement: SqlitePreparedStatement<*>) : AutoCloseable {
    /**
     * Provides iterable for sequential access to the underlying result set with tool results
     */
    fun executeQuery() = sequence {
      val resultSet = statement.executeQuery()
      while (resultSet.next()) {
        yield(ToolResultRecord(resultSet.getString(0)!!,
                               resultSet.getString(1)!!,
                               resultSet.getString(2)!!))
      }
    }

    override fun close() {
      statement.close()
    }
  }
}

data class ToolResultRecord(val inspectionId: String, val hash: String, val json: String)
