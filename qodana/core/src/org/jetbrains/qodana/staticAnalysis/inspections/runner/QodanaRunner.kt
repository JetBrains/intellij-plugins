package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.diagnostic.telemetry.helpers.useWithScope
import com.intellij.util.io.createDirectories
import com.intellij.util.io.delete
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.qodanaTracer
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.coverageStats
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.aggregators.MetricAggregator
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.codeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.MetricResult
import org.jetbrains.qodana.staticAnalysis.sarif.createRun
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.resultsFlowByGroup
import org.jetbrains.qodana.staticAnalysis.sarif.writeReport
import org.jetbrains.qodana.staticAnalysis.script.QodanaScript
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptResult
import java.io.IOException
import kotlin.io.path.exists

internal const val FULL_SARIF_REPORT_NAME = "qodana.sarif.json"
private const val SHORT_SARIF_REPORT_NAME = "qodana-short.sarif.json"

private val LOG = logger<QodanaRunner>()

/**
 * The [QodanaRunner] runs a [QodanaScript] and after run processes SARIF (adds needed fields etc.)
 */
class QodanaRunner(val script: QodanaScript, private val config: QodanaConfig, private val messageReporter: QodanaMessageReporter) {
  val sarifRun: Run = createRun()
  val sarif: SarifReport = createSarifReport(mutableListOf(sarifRun))

  companion object {
    internal suspend fun getInspectionIdToNameMap(
      inspectionNames: Map<String, String>,
      config: QodanaConfig
    ): (String) -> String {
      val extensionsList = getBaselineReport(config, false)?.runs?.firstOrNull()?.tool?.extensions?.flatMap { it.rules }
      return { inspectionId ->
        inspectionNames[inspectionId] ?:
        extensionsList?.find { it.id == inspectionId }?.shortDescription?.text
        ?: inspectionId
      }
    }
  }

  suspend fun run() {
    try {
      val resultsStorageDir = config.resultsStorage
      runInterruptible(StaticAnalysisDispatchers.IO) {
        if (!resultsStorageDir.exists()) {
          resultsStorageDir.createDirectories()
        }
        try {
          FileUtil.deleteRecursively(config.coverage.coveragePath)
        }
        catch (e: IOException) {
          LOG.warn("Exception while cleaning up coverage directory", e)
        }
      }

      val scriptName = config.script.name
      val scriptResult = qodanaTracer().spanBuilder("qodanaScriptRun").setAttribute("name", scriptName).useWithScope {
        script.execute(sarif, sarifRun)
      }
      val commandLineResultsPrinter = createCommandLineResultsPrinter(scriptResult.inspectionNames)
      val properties = sarifRun.properties ?: PropertyBag()
      sarifRun.properties = properties
      val sanityResults = resultsByInspectionGroup("sanity", scriptResult)
      if (!sanityResults.isNullOrEmpty()) {
        properties["qodana.sanity.results"] = sanityResults
        commandLineResultsPrinter?.printSanityResults(sanityResults)
      }

      val sarifResults = sarifRun.results
      when {
        sarifResults == null -> Unit // do nothing if script didn't set results
        config.baseline != null -> {
          val resultsWithBaselineState = sarifResults.filter { it.baselineState != null }
          commandLineResultsPrinter?.printResultsWithBaselineState(
            resultsWithBaselineState,
            config.includeAbsent
          )
        }
        else -> {
          commandLineResultsPrinter?.printResults(
            sarifRun.results,
            sectionTitle = QodanaBundle.message("cli.main.results.title")
          )
        }
      }

      val promoResults = resultsByInspectionGroup("promo", scriptResult)
      if (!promoResults.isNullOrEmpty()) {
        properties["qodana.promo.results"] = promoResults
        commandLineResultsPrinter?.printResults(
          promoResults,
          sectionTitle = QodanaBundle.message("cli.promo.results.title"),
          message = QodanaBundle.message("cli.promo.results.grouping.message")
        )
      }

      val coverageStatistics = scriptResult.coverageStats
      if (coverageStatistics != null) {
        val stat = coverageStatistics.computeStat()
        if (!stat.isNullOrEmpty()) {
          sarifRun.coverageStats = stat.mapKeys { (k, _) -> k.prop }
        }
        // Inspection may be executed, however, we need to check whether the coverage files were actually provided
        if (scriptResult.coverageFiles.isNotEmpty()) {
          commandLineResultsPrinter?.printCoverage(stat, sectionTitle = QodanaBundle.message("cli.coverage.title"))
        }
      }

      val metricResults: List<MetricResult> = QodanaToolResultDatabase.open(path = scriptResult.outputPath).use { db ->
        MetricAggregator.EP.extensionList.mapNotNull { aggregator ->
          aggregator.getData(db)
        }
      }

      val metrics: Map<CodeQualityMetrics, Any> = metricResults.associate { it.mapToProperties() }
      if (metrics.isNotEmpty()) {
        sarifRun.codeQualityMetrics = metrics.mapKeys { (k, _) -> k.prop }
        commandLineResultsPrinter?.printCodeQualityMetrics(metrics, sectionTitle = QodanaBundle.message("cli.metrics.title"))
      }

    }
    catch (e: Throwable) {
      val invocation = sarifRun.invocations.first()
      invocation.exitCode = 1
      invocation.executionSuccessful = false
      invocation.exitCodeDescription = "Internal error"
      throw e
    }
    finally {
      setInvocationExitStatus(sarifRun, config)
      withContext(NonCancellable) {
        clearResultsDirIfNeeded()
      }
    }
  }

  private suspend fun createCommandLineResultsPrinter(inspectionNames: Map<String, String>): CommandLineResultsPrinter? {
    if (config.skipResultOutput) return null
    return CommandLineResultsPrinter(
      inspectionIdToName = getInspectionIdToNameMap(inspectionNames, config),
      cliPrinter = { message -> messageReporter.reportMessage(1, message) }
    )
  }

  private suspend fun resultsByInspectionGroup(groupName: String, scriptResult: QodanaScriptResult): List<Result>? {
    // database is closed at this moment, so we need to reopen it one more time to collect the results
    return QodanaToolResultDatabase.open(scriptResult.outputPath).use { db ->
      val inspectionGroupStateIsPresent = groupName in scriptResult.profileState.stateByGroupName

      if (inspectionGroupStateIsPresent) {
        db.resultsFlowByGroup(groupName, messageReporter).toList()
      }
      else {
        null
      }
    }
  }

  private suspend fun clearResultsDirIfNeeded() {
    if (config.outputFormat != OutputFormat.INSPECT_SH_FORMAT) {
      runInterruptible(StaticAnalysisDispatchers.IO) {
        config.resultsStorage.delete(true)
      }
    }
  }

  suspend fun writeFullSarifReport() {
    writeReport(config.outPath.resolve(FULL_SARIF_REPORT_NAME), sarif)
  }

  suspend fun writeShortSarifReport() {
    // avoid creating a copy because it can easily OOM on large reports
    val ext = sarifRun.tool.extensions
    val taxa = sarifRun.tool.driver.taxa
    val results = sarifRun.results

    sarifRun.tool.withExtensions(emptySet())
    sarifRun.tool.driver.withTaxa(emptyList())
    sarifRun.withResults(emptyList())
    try {
      writeReport(config.outPath.resolve(SHORT_SARIF_REPORT_NAME), sarif)
    }
    finally {
      sarifRun.tool.withExtensions(ext)
      sarifRun.tool.driver.withTaxa(taxa)
      sarifRun.withResults(results)
    }
  }

}
