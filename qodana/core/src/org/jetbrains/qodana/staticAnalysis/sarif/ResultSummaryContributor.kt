package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.script.scoped.REVERSE_SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_BASELINE_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.STAGE_ARG
import org.jetbrains.qodana.staticAnalysis.script.scoped.Stage
import org.jetbrains.qodana.util.QodanaMessageReporter

private const val MATCHED_BY_PROPERTY = "matchedMethod"
private val logger by lazy { logger<ResultSummaryContributor>() }

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
private fun debugResultSummaryContributor(message: String, messageReporter: QodanaMessageReporter) {
  logger.warn(message)
  System.out.print(message)
  System.out.print('\n')
  messageReporter.reportError(message)
}

internal class ResultSummaryContributor : SarifReportContributor {
  companion object {
    const val TOTAL_KEY = "total"
  }

  private fun Run.debugBaselineStateCounts(): Map<String, Int> =
    results.orEmpty().groupingBy { it.baselineState?.name ?: "<null>" }.eachCount()

  private fun Run.debugMatchedByCounts(): Map<String, Int> =
    results.orEmpty()
      .mapNotNull { it.properties?.get(MATCHED_BY_PROPERTY) as? String }
      .groupingBy { it }
      .eachCount()

  private fun QodanaConfig.countOnlyNewResultsInSummary(messageReporter: QodanaMessageReporter): Boolean {
    val stage = script.parameters[STAGE_ARG]?.toString()
    val scopedBaselinePath = System.getProperty(SCOPED_BASELINE_PROPERTY)
    val enabled = when {
      baseline != null -> true
      script.name != REVERSE_SCOPED_SCRIPT_NAME -> false
      stage != Stage.OLD.name -> false
      else -> scopedBaselinePath != null
    }
    debugResultSummaryContributor(
      "REVERSE-SCOPED DEBUG [ResultSummaryContributor/countOnlyNewResultsInSummary] baseline=${baseline ?: "<null>"} script=${script.name} stage=${stage ?: "<null>"} scopedBaseline=${scopedBaselinePath ?: "<null>"} enabled=$enabled",
      messageReporter
    )
    return enabled
  }

  override fun contribute(run: Run, project: Project, config: QodanaConfig, messageReporter: QodanaMessageReporter) {
    val countOnlyNewResults = config.countOnlyNewResultsInSummary(messageReporter)
    val baselineFilter: (Result) -> Boolean = when {
      !countOnlyNewResults -> { _ -> true }
      else -> { x -> x.baselineState == Result.BaselineState.NEW }
    }

    val resultsBySeverity = run.results.orEmpty()
      .asSequence()
      .filter(baselineFilter)
      .groupingBy(Result::qodanaSeverity)
      .eachCount()

    val total = resultsBySeverity.values.sum()

    val summary = buildMap {
      resultsBySeverity.forEach { (k, v) -> put(k.name.lowercase(), v) }
      put(TOTAL_KEY, total)
    }
    run.resultSummary = summary

    val filterMode = if (countOnlyNewResults) "BASELINE_NEW_ONLY" else "ALL_RESULTS"
    debugResultSummaryContributor(
      "REVERSE-SCOPED DEBUG [ResultSummaryContributor] baseline=${config.baseline ?: "<null>"} filter=$filterMode summary=$summary baselineState=${run.debugBaselineStateCounts()} matchedBy=${run.debugMatchedByCounts()}",
      messageReporter
    )
    val matchedButWithoutBaselineState = run.results.orEmpty().count {
      it.baselineState == null && (it.properties?.get(MATCHED_BY_PROPERTY) as? String) != null
    }
    if (!countOnlyNewResults && matchedButWithoutBaselineState > 0) {
      debugResultSummaryContributor(
        "REVERSE-SCOPED DEBUG [ResultSummaryContributor] matchedButWithoutBaselineState=$matchedButWithoutBaselineState. They are counted because NEW-only filtering is enabled only when config.baseline != null.",
        messageReporter
      )
    }
  }
}
