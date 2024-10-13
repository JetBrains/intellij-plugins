package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity

data class FailureConditions(
  val severityThresholds: SeverityThresholds = SeverityThresholds(),
  val testCoverageThresholds: TestCoverageThresholds = TestCoverageThresholds()
) {
  companion object {
    val DEFAULT = FailureConditions()
  }

  data class SeverityThresholds(
    val any: Int? = null,
    val critical: Int? = null,
    val high: Int? = null,
    val moderate: Int? = null,
    val low: Int? = null,
    val info: Int? = null,
  )

  data class TestCoverageThresholds(
    val total: Int? = null,
    val fresh: Int? = null
  )

  fun bySeverity(qodanaSeverity: QodanaSeverity) = when (qodanaSeverity) {
    QodanaSeverity.INFO -> severityThresholds.info
    QodanaSeverity.LOW -> severityThresholds.low
    QodanaSeverity.MODERATE -> severityThresholds.moderate
    QodanaSeverity.HIGH -> severityThresholds.high
    QodanaSeverity.CRITICAL -> severityThresholds.critical
  }

  fun byCoverage(coverage: CoverageData) = when (coverage) {
    CoverageData.TOTAL_COV -> testCoverageThresholds.total
    CoverageData.FRESH_COV -> testCoverageThresholds.fresh
    else -> null
  }
}
