package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig

internal class ResultSummaryContributor : SarifReportContributor {
  companion object {
    const val TOTAL_KEY = "total"
  }

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

    run.resultSummary = buildMap {
      resultsBySeverity.forEach { (k, v) -> put(k.name.lowercase(), v) }
      put(TOTAL_KEY, total)
    }
  }
}
