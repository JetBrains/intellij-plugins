package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig

private const val MATCHED_BY_PROPERTY = "matchedMethod"
private val logger by lazy { logger<ResultSummaryContributor>() }

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

  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val baselineFilter: (Result) -> Boolean = when {
      config.baseline == null -> { _ -> true }
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

    val filterMode = if (config.baseline == null) "ALL_RESULTS" else "BASELINE_NEW_ONLY"
    logger.warn("REVERSE-SCOPED DEBUG [ResultSummaryContributor] baseline=${config.baseline ?: "<null>"} filter=$filterMode summary=$summary baselineState=${run.debugBaselineStateCounts()} matchedBy=${run.debugMatchedByCounts()}")
    val matchedButWithoutBaselineState = run.results.orEmpty().count {
      it.baselineState == null && (it.properties?.get(MATCHED_BY_PROPERTY) as? String) != null
    }
    if (config.baseline == null && matchedButWithoutBaselineState > 0) {
      logger.warn("REVERSE-SCOPED DEBUG [ResultSummaryContributor] matchedButWithoutBaselineState=$matchedButWithoutBaselineState. They are counted because NEW-only filtering is enabled only when config.baseline != null.")
    }
  }
}
