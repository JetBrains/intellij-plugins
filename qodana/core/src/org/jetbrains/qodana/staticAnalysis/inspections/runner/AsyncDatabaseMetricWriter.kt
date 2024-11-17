package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.MetricFileData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.MetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber.Companion.LOG

class AsyncDatabaseMetricWriter(
  private val scope: CoroutineScope,
  val project: Project,
  private val database: QodanaToolResultDatabase,
) {
  private val channel: Channel<MetricFileData> = Channel(1000)

  private val writerJob: Job = scope.launch(StaticAnalysisDispatchers.IO, CoroutineStart.LAZY) {
    for (fileData in channel) {
      // first delete all data previously collected for a file
      deleteMetricDataForFile(metricTable = fileData.metricTable, filePath = fileData.filePath)

      // insert new data
      val tableRows: List<MetricTableRowData> = fileData.tableRows
      tableRows.forEach { tableRow -> writeMetricsData(tableRow) }
    }
  }

  private val consumerScope: CoroutineScope = scope.childScope()

  fun consume(fileData: MetricFileData) {
    writerJob.start()
    consumerScope.launch(StaticAnalysisDispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
      channel.send(fileData)
    }
  }

  suspend fun close() {
    val consumerJob = consumerScope.coroutineContext.job
    consumerJob.children.toList().joinAll()
    consumerJob.cancelAndJoin()

    channel.close()
    writerJob.join()

    scope.coroutineContext.job.cancelAndJoin()
  }

  private suspend fun writeMetricsData(metricRowData: MetricTableRowData) {
    try {
      withContext(StaticAnalysisDispatchers.IO) {
        database.insertMetricsData(metricRowData)
      }
    }
    catch (e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      LOG.warn(e)
    }
  }

  private suspend fun deleteMetricDataForFile(metricTable: MetricTable, filePath: String) {
    try {
      withContext(StaticAnalysisDispatchers.IO) {
        database.deleteMetricsDataForFile(filePath, metricTable)
      }
    }
    catch (e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      LOG.warn(e)
    }
  }

}