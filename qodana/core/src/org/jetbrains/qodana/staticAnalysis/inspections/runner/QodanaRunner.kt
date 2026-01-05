package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.diagnostic.telemetry.helpers.useWithScope
import com.intellij.psi.PsiElement
import com.intellij.util.io.createDirectories
import com.intellij.util.io.delete
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Level
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
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.hasCoverageFiles
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.aggregators.MetricAggregator
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.codeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.results.MetricResult
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.getOrCreateRun
import org.jetbrains.qodana.staticAnalysis.sarif.resultsFlowByGroup
import org.jetbrains.qodana.staticAnalysis.sarif.writeReport
import org.jetbrains.qodana.staticAnalysis.script.QodanaScript
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptResult
import org.jetbrains.qodana.staticAnalysis.stat.QodanaEventsService
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.io.IOException
import kotlin.io.path.exists

internal const val FULL_SARIF_REPORT_NAME = "qodana.sarif.json"
private const val SHORT_SARIF_REPORT_NAME = "qodana-short.sarif.json"
private const val SANITY_RESULTS_KEY = "qodana.sanity.results"
private const val PROMO_RESULTS_KEY = "qodana.promo.results"
private const val VARY_RUN_PREFIX = "original."

private val LOG = logger<QodanaRunner>()

/**
 * The [QodanaRunner] runs a [QodanaScript] and after run processes SARIF (adds needed fields etc.)
 */
class QodanaRunner(val script: QodanaScript, private val config: QodanaConfig, private val messageReporter: QodanaMessageReporter) {
  companion object {
    internal suspend fun getInspectionIdToNameMap(
      inspectionNames: Map<String, String>,
      config: QodanaConfig,
    ): (String) -> String {
      val extensionsList = getBaselineReport(config, false)?.runs?.firstOrNull()?.tool?.extensions?.flatMap { it.rules }
      return { inspectionId ->
        inspectionNames[inspectionId] ?: extensionsList?.find { it.id == inspectionId }?.shortDescription?.text
        ?: inspectionId
      }
    }
  }

  suspend fun run(): SarifReport {
    val sarif: SarifReport = createSarifReport(emptyList())
    val sarifRun = sarif.getOrCreateRun()
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
      QodanaEventsService.getInstance().initAnalysisKind(script.analysisKind)

      val scriptResult = qodanaTracer().spanBuilder("qodanaScriptRun").setAttribute("name", scriptName).useWithScope {
        script.execute(sarif)
      }
      sarifRun.run {
        storeSanityResults(this, scriptResult)
        storePromoResults(this, scriptResult)
        storeCoverageData(this, scriptResult)
        storeMetricsData(this, scriptResult)
      }

      sarif.getOrCreateRun().run {
        if (config.skipResultStrategy.shouldSkip(this)) return@run
        createCommandLineResultsPrinter(getInspectionNamesFromRun(this, scriptResult))?.let { commandLineResultsPrinter ->
          printSanityResults(this, commandLineResultsPrinter)
          printMainResults(this, commandLineResultsPrinter)
          printPromoResults(this, commandLineResultsPrinter)
          printCoverageData(this, commandLineResultsPrinter)
          printMetricsData(this, commandLineResultsPrinter)
        }
      }

      return sarif
    }
    catch (e: Throwable) {
      val invocation = sarif.getOrCreateRun().invocations.first()
      invocation.exitCode = 1
      invocation.executionSuccessful = false
      invocation.exitCodeDescription = "Internal error"
      throw e
    }
    finally {
      setInvocationExitStatus(sarif.getOrCreateRun(), config)
      withContext(NonCancellable) {
        clearResultsDirIfNeeded()
        writeFullSarifReport(sarif)
        writeShortSarifReport(sarif)
        // if report run was substituted, store original as well
        if (sarifRun != sarif.getOrCreateRun()) {
          createSarifReport(listOf(sarifRun)).let {
            writeFullSarifReport(it, VARY_RUN_PREFIX)
            writeShortSarifReport(it, VARY_RUN_PREFIX)
          }
        }
      }
    }
  }

  private fun printMainResults(
    resultingRun: Run,
    commandLineResultsPrinter: CommandLineResultsPrinter?,
  ) {
    val sarifResults = resultingRun.results
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
          sarifResults,
          sectionTitle = QodanaBundle.message("cli.main.results.title")
        )
      }
    }
  }

  private suspend fun storeSanityResults(
    run: Run,
    scriptResult: QodanaScriptResult,
  ) {
    val properties = run.properties!!
    val sanityResults = resultsByInspectionGroup("sanity", scriptResult)
      ?.filter { it.level == Level.ERROR }
    if (!sanityResults.isNullOrEmpty()) {
      properties[SANITY_RESULTS_KEY] = sanityResults
    }
  }

  private fun printSanityResults(
    resultingRun: Run,
    commandLineResultsPrinter: CommandLineResultsPrinter,
  ) {
    resultingRun.convertPropertyToResults(SANITY_RESULTS_KEY)?.let { results ->
      commandLineResultsPrinter.printSanityResults(results)
    }
  }

  private suspend fun storePromoResults(
    run: Run,
    scriptResult: QodanaScriptResult,
  ) {
    val properties = run.properties!!
    val promoResults = resultsByInspectionGroup("promo", scriptResult)
    if (!promoResults.isNullOrEmpty()) {
      properties[PROMO_RESULTS_KEY] = promoResults
    }
  }

  private fun printPromoResults(
    resultingRun: Run,
    commandLineResultsPrinter: CommandLineResultsPrinter,
  ) {
    resultingRun.convertPropertyToResults(PROMO_RESULTS_KEY)?.let { results ->
      commandLineResultsPrinter.printResults(
        results,
        sectionTitle = QodanaBundle.message("cli.promo.results.title"),
        message = QodanaBundle.message("cli.promo.results.grouping.message")
      )
    }
  }

  private fun Run.convertPropertyToResults(propertyKey: String): List<Result>? {
    val property = this.properties?.get(propertyKey) ?: return null

    if (property is List<*> && property.all { it is Result }) {
      @Suppress("UNCHECKED_CAST")
      return property as List<Result>
    }

    return try {
      val gson = SarifUtil.createGson()
      val json = gson.toJson(property)
      val type = object : TypeToken<List<Result>>() {}.type
      gson.fromJson(json, type) ?: emptyList()
    }
    catch (_: Throwable) {
      emptyList()
    }
  }

  private fun storeCoverageData(
    sarifRun: Run,
    scriptResult: QodanaScriptResult,
  ) {
    sarifRun.hasCoverageFiles = scriptResult.coverageFilesPresent
    val coverageStatistics = scriptResult.coverageStats
    if (coverageStatistics != null) {
      val stat = coverageStatistics.computeStat()
      if (!stat.isNullOrEmpty()) {
        sarifRun.coverageStats = stat.mapKeys { (k, _) -> k.prop }
      }
    }
  }

  private fun printCoverageData(
    resultingRun: Run,
    commandLineResultsPrinter: CommandLineResultsPrinter,
  ) {
    resultingRun.coverageStats.let { stat ->
      if (stat.isEmpty()) {
        if (resultingRun.hasCoverageFiles) {
          // printer will notify about coverage configuration error
          commandLineResultsPrinter.printCoverage(null, sectionTitle = QodanaBundle.message("cli.coverage.title"))
        }
      }
      else {
        commandLineResultsPrinter.printCoverage(stat, sectionTitle = QodanaBundle.message("cli.coverage.title"))
      }
    }
  }

  private suspend fun storeMetricsData(
    sarifRun: Run,
    scriptResult: QodanaScriptResult,
  ) {
    val metricResults: List<MetricResult> = QodanaToolResultDatabase.open(path = scriptResult.outputPath).use { db ->
      MetricAggregator.EP.extensionList.mapNotNull { aggregator ->
        aggregator.getData(db)
      }
    }

    val metrics: Map<CodeQualityMetrics, Any> = metricResults.associate { it.mapToProperties() }
    if (metrics.isNotEmpty()) {
      sarifRun.codeQualityMetrics = metrics.mapKeys { (k, _) -> k.prop }
    }
  }

  private fun printMetricsData(
    resultingRun: Run,
    commandLineResultsPrinter: CommandLineResultsPrinter,
  ) {
    resultingRun.codeQualityMetrics.let { metrics ->
      if (metrics.isNotEmpty()) {
        commandLineResultsPrinter.printCodeQualityMetrics(metrics, sectionTitle = QodanaBundle.message("cli.metrics.title"))
      }
    }
  }

  private suspend fun getInspectionNamesFromRun(resultingRun: Run, scriptResult: QodanaScriptResult) = withContext(StaticAnalysisDispatchers.Default) {
    buildMap {
      val nullPsiElement: PsiElement? = null
      val profile = scriptResult.profileState.mainState.inspectionGroup.profile
      val unknownIds = mutableMapOf<String, String>()
      val resolveUnknown: (String) -> String = { input ->
        // this is a rare situation when we handle 2 runs having different plugin ids
        // it is expensive, however, calling it for 1-2 is less expensive than storing
        // thousands of mapped values
        unknownIds.computeIfAbsent(input) {
          resultingRun.tool.extensions.forEach { e -> e.rules.forEach { r -> if (r.id == input) return@computeIfAbsent r.shortDescription.text } }
          input
        }
      }

      val shouldIncludeResult: (Result) -> Boolean = { result ->
        val baselineState = result.baselineState
        when {
          baselineState == Result.BaselineState.ABSENT -> false
          else -> true
        }
      }

      resultingRun.results?.filter(shouldIncludeResult)?.forEach { result ->
        result.ruleId?.let { put(it, profile.getInspectionTool(it, nullPsiElement)?.displayName ?: resolveUnknown(it)) }
      }

      resultingRun.convertPropertyToResults("qodana.sanity.results")
        ?.filter(shouldIncludeResult)?.forEach { result ->
          result.ruleId?.let { put(it, profile.getInspectionTool(it, nullPsiElement)?.displayName ?: resolveUnknown(it)) }
        }

      resultingRun.convertPropertyToResults("qodana.promo.results")
        ?.filter(shouldIncludeResult)?.forEach { result ->
          result.ruleId?.let { put(it, profile.getInspectionTool(it, nullPsiElement)?.displayName ?: resolveUnknown(it)) }
        }
    }
  }

  private suspend fun createCommandLineResultsPrinter(inspectionNames: Map<String, String>): CommandLineResultsPrinter? {
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

  suspend fun writeFullSarifReport(sarif: SarifReport, prefix: String = "") {
    writeReport(config.outPath.resolve(prefix + FULL_SARIF_REPORT_NAME), sarif)
  }

  suspend fun writeShortSarifReport(sarif: SarifReport, prefix: String = "") {
    val sarifRun = sarif.getOrCreateRun()
    // avoid creating a copy because it can easily OOM on large reports
    val ext = sarifRun.tool.extensions
    val taxa = sarifRun.tool.driver.taxa
    val results = sarifRun.results

    sarifRun.tool.withExtensions(emptySet())
    sarifRun.tool.driver.withTaxa(emptyList())
    sarifRun.withResults(emptyList())
    try {
      writeReport(config.outPath.resolve(prefix + SHORT_SARIF_REPORT_NAME), sarif)
    }
    finally {
      sarifRun.tool.withExtensions(ext)
      sarifRun.tool.driver.withTaxa(taxa)
      sarifRun.withResults(results)
    }
  }

}
