package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.FailureConditions
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import kotlin.experimental.ExperimentalTypeInference

internal class FailureConditionsContributor : SarifReportContributor {

  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val thresholds = config.failureConditions
    if (thresholds == FailureConditions.DEFAULT) return
    val props = run.properties ?: PropertyBag()

    val configMap = smallMap {
      yield("severityThresholds" to smallMap {
        QodanaSeverity.entries.forEach { sev ->
          yield(sev.name.lowercase() to thresholds.bySeverity(sev))
        }
        yield("any" to thresholds.severityThresholds.any)
      })

      yield("testCoverageThresholds" to smallMap {
        CoverageData.entries.forEach { cov ->
          yield(cov.prop to thresholds.byCoverage(cov))
        }
      })
    }
    if (configMap != null) {
      props[QODANA_FAILURE_CONDITIONS] = configMap
    }
    run.properties = props
  }


  // a map that never contains null values, and itself is never empty (but null)
  @OptIn(ExperimentalTypeInference::class)
  @Suppress("UNCHECKED_CAST")
  private fun <K : Any, V : Any> smallMap(@BuilderInference f: suspend SequenceScope<Pair<K, V?>>.() -> Unit): Map<K, V>? =
    sequence(f).mapNotNull { if (it.second != null) it as Pair<K, V> else null }.toMap().takeUnless { it.isEmpty() }
}
