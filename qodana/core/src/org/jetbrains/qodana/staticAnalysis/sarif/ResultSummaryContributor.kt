package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.script.scoped.REVERSE_SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedDebugLog
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_BASELINE_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.STAGE_ARG
import org.jetbrains.qodana.staticAnalysis.script.scoped.Stage

private const val MATCHED_BY_PROPERTY = "matchedMethod"

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

  private fun QodanaConfig.countOnlyNewResultsInSummary(): Boolean {
    val stage = script.parameters[STAGE_ARG]?.toString()
    val scopedBaselinePath = System.getProperty(SCOPED_BASELINE_PROPERTY)
    val enabled = when {
      baseline != null -> true
      !QodanaRegistry.isReverseScopedIncludeUnchangedEnabled -> false
      script.name != REVERSE_SCOPED_SCRIPT_NAME -> false
      stage != Stage.OLD.name && stage != Stage.FIXES.name -> false
      else -> scopedBaselinePath != null
    }
    ReverseScopedDebugLog.log {
      "REVERSE-SCOPED DEBUG [ResultSummaryContributor/countOnlyNewResultsInSummary] baseline=${baseline ?: "<null>"} script=${script.name} stage=${stage ?: "<null>"} scopedBaseline=${scopedBaselinePath ?: "<null>"} enabled=$enabled"
    }
    return enabled
  }

  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val countOnlyNewResults = config.countOnlyNewResultsInSummary()
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

    if (!ReverseScopedDebugLog.isEnabled) return
    val filterMode = if (countOnlyNewResults) "BASELINE_NEW_ONLY" else "ALL_RESULTS"
    ReverseScopedDebugLog.log {
      "REVERSE-SCOPED DEBUG [ResultSummaryContributor] baseline=${config.baseline ?: "<null>"} filter=$filterMode summary=$summary baselineState=${run.debugBaselineStateCounts()} matchedBy=${run.debugMatchedByCounts()}"
    }
    val matchedButWithoutBaselineState = run.results.orEmpty().count {
      it.baselineState == null && (it.properties?.get(MATCHED_BY_PROPERTY) as? String) != null
    }
    if (!countOnlyNewResults && matchedButWithoutBaselineState > 0) {
      ReverseScopedDebugLog.log {
        "REVERSE-SCOPED DEBUG [ResultSummaryContributor] matchedButWithoutBaselineState=$matchedButWithoutBaselineState. They are counted because NEW-only filtering is enabled only when config.baseline != null."
      }
    }
  }
}
