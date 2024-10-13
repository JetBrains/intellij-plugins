package org.jetbrains.qodana.staticAnalysis.inspections.metrics

import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.MetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable

/**
 * Class that stores metrics data that is collected for one file.
 *
 * @property filePath path of the file.
 * @property tableRows list of table rows to be inserted into the database.
 */
data class MetricFileData(
  val filePath: String,
  val metricTable: MetricTable,
  val tableRows: List<MetricTableRowData>,
)