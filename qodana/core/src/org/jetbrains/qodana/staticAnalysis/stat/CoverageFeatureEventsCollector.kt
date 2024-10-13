package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext

object CoverageFeatureEventsCollector : CounterUsagesCollector() {
  private val GROUP = EventLogGroup("qodana.coverage", 2)

  override fun getGroup() = GROUP

  /**
   * Coverage data loaded by inspection per language (for Qodana runs having coverage inspections enabled)
   */
  @JvmField
  val INSPECTION_LOADED_COVERAGE = GROUP.registerEvent("input.coverage.loaded",
                                                       EventFields.Enum<CoverageLanguage>("language"))

  /**
   * Coverage computation state (for all Qodana runs) - Total / Fresh coverage
   */
  @JvmField
  val TOTAL_COVERAGE_REPORTED = GROUP.registerEvent("total.coverage.reported",
                                                    EventFields.Boolean("is_total_computed"),
                                                    EventFields.Int("total_coverage_value"))

  @JvmField
  val FRESH_COVERAGE_REPORTED = GROUP.registerEvent("fresh.coverage.reported",
                                                    EventFields.Boolean("is_fresh_computed"),
                                                    EventFields.Int("fresh_coverage_value"))

  fun logCoverageStatistics(runContext: QodanaRunContext, statistics: CoverageStatisticsData?) {
    val stats = statistics?.computeStat()
    val total = stats?.get(CoverageData.TOTAL_COV)
    val fresh = stats?.get(CoverageData.FRESH_COV)

    TOTAL_COVERAGE_REPORTED.log(runContext.project, total != null, total ?: -1)
    FRESH_COVERAGE_REPORTED.log(runContext.project, fresh != null, fresh ?: -1)
  }
}
