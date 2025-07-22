package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext

object CoverageFeatureEventsCollector : CounterUsagesCollector() {
  private val GROUP = QodanaEventLogGroup("qodana.coverage", 3)

  override fun getGroup() = GROUP.eventLogGroup

  /**
   * Coverage data loaded by inspection per language (for Qodana runs having coverage inspections enabled)
   */
  val COVERAGE_LANGUAGE_FIELD = EventFields.Enum<CoverageLanguage>("language")

  /**
   * Coverage data loaded by inspection per language (for Qodana runs having coverage inspections enabled)
   */
  @JvmField
  @Deprecated("Use INPUT_COVERAGE_LOADED instead")
  val INSPECTION_LOADED_COVERAGE = GROUP.eventLogGroup.registerEvent("deprecated.input.coverage.loaded",
                                                                     COVERAGE_LANGUAGE_FIELD)

  @JvmField
  val INPUT_COVERAGE_LOADED = GROUP.registerVarargEvent("input.coverage.loaded", COVERAGE_LANGUAGE_FIELD)

  /**
   * Coverage computation state (for all Qodana runs) - Total / Fresh coverage
   */
  private val COVERAGE_IS_TOTAL_COMPUTED_FIELD = EventFields.Boolean("is_total_computed")
  private val COVERAGE_TOTAL_COVERAGE_VALUE_FIELD = EventFields.Int("total_coverage_value")

  @JvmField
  internal val TOTAL_COVERAGE_REPORTED = GROUP.registerVarargEvent("total.coverage.reported",
                                                                   COVERAGE_IS_TOTAL_COMPUTED_FIELD,
                                                                   COVERAGE_TOTAL_COVERAGE_VALUE_FIELD)

  private val COVERAGE_IS_FRESH_COMPUTED_FIELD = EventFields.Boolean("is_fresh_computed")
  private val COVERAGE_FRESH_COVERAGE_VALUE_FIELD = EventFields.Int("fresh_coverage_value")

  @JvmField
  internal val FRESH_COVERAGE_REPORTED = GROUP.registerVarargEvent("fresh.coverage.reported",
                                                                   COVERAGE_IS_FRESH_COMPUTED_FIELD,
                                                                   COVERAGE_FRESH_COVERAGE_VALUE_FIELD)

  fun logCoverageStatistics(runContext: QodanaRunContext, statistics: CoverageStatisticsData?) {
    val stats = statistics?.computeStat()
    val total = stats?.get(CoverageData.TOTAL_COV)
    val fresh = stats?.get(CoverageData.FRESH_COV)

    TOTAL_COVERAGE_REPORTED.log(runContext.project,
                                COVERAGE_IS_TOTAL_COMPUTED_FIELD.with(total != null),
                                COVERAGE_TOTAL_COVERAGE_VALUE_FIELD.with(total ?: -1))
    FRESH_COVERAGE_REPORTED.log(runContext.project,
                                COVERAGE_IS_FRESH_COMPUTED_FIELD.with(fresh != null),
                                COVERAGE_FRESH_COVERAGE_VALUE_FIELD.with(fresh ?: -1))
  }
}
