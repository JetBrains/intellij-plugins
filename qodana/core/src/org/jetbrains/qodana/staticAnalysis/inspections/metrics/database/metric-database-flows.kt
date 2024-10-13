package org.jetbrains.qodana.staticAnalysis.inspections.metrics.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase

fun <T: Any> QodanaToolResultDatabase.getResultOfQueryFromMetricsTable(
  sqlQuery: String,
  numberOfColumns: Int,
  transform: (Array<String>) -> T,
): Flow<T> = flow {
  getResultsFromMetricsTable(sqlQuery, numberOfColumns).use { query ->
    for (resultSet in query.executeQuery()) {
      if (resultSet.isNotEmpty()) {
        emit(transform(resultSet))
      }
    }
  }
}

