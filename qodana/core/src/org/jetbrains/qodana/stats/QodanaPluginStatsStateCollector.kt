package org.jetbrains.qodana.stats

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.highlight.*
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector.FIELD_USER_STATE
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector.IS_LINK
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector.REPORT_TYPE

internal class QodanaPluginStatsStateCollector : ProjectUsagesCollector() {
  override fun getGroup() = GROUP

  override fun getMetrics(project: Project): MutableSet<MetricEvent> {
    val service = QodanaHighlightedReportService.getInstanceIfCreated(project) ?: return mutableSetOf()

    val highlightedReportState = service.highlightedReportState.value
    val highlightedReportDataIfSelected = highlightedReportState.highlightedReportDataIfSelected

    val problemsTypesCount = highlightedReportDataIfSelected?.let { getProblemsTypesCount(it) } ?: ProblemsTypesCount(0, 0, 0)

    val reportStateStats = getHighlightReportStateStats(highlightedReportState)
    val highlightedReportType = highlightedReportState.reportDescriptorIfSelectedOrLoading?.toStatsReportType()
                                ?: StatsReportType.NONE

    @Suppress("DEPRECATION") val userState = QodanaCloudStateService.getInstance().getUserStateStatsState()
    @Suppress("DEPRECATION") val isLinked = QodanaCloudProjectLinkService.getInstance(project).getIsLinkedForStats()

    val coverageLanguages = if (highlightedReportDataIfSelected != null) {
      highlightedReportDataIfSelected.reportMetadata.map.values
        .filterIsInstance<CoverageMetaDataArtifact>()
        .map { CoverageLanguage.mapEngine(it.id).name }
        .distinct()
    }
    else {
      emptyList()
    }

    val inspectionKtsFileStatuses = KtsInspectionsManager.getInstance(project).ktsInspectionsFlow.value ?: emptySet()

    val compiledInspectionFiles = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Compiled>()

    val inspectionKtsFilesCount = inspectionKtsFileStatuses.count()
    val compiledInspectionKtsFilesCount = compiledInspectionFiles.count()
    val failedToCompileInspectionKtsFilesCount = compiledInspectionFiles.filterIsInstance<InspectionKtsFileStatus.Error>().count()
    val inspectionKtsInspectionsCount = compiledInspectionFiles.flatMap { it.inspections.inspections }.count()

    val inspectionKtsMetric = FLEXINSPECT_COMPILED.metric(
      FLEXINSPECT_TOTAL_FILES_FIELD.with(inspectionKtsFilesCount),
      FLEXINSPECT_COMPILED_FILES_FIELD.with(compiledInspectionKtsFilesCount),
      FLEXINSPECT_FAILED_FILES_FIELD.with(failedToCompileInspectionKtsFilesCount),
      FLEXINSPECT_COMPILED_INSPECTIONS_FIELD.with(inspectionKtsInspectionsCount)
    )

    return mutableSetOf(
      inspectionKtsMetric,
      REPORT_PROBLEMS_DATA.metric(problemsTypesCount.total, problemsTypesCount.missing, problemsTypesCount.fixed),
      HIGHLIGHTED_REPORT_STATE.metric(reportStateStats, highlightedReportType),
      COVERAGE_IN_REPORT.metric(coverageLanguages),
      USER_STATE.metric(userState),
      LINK_STATE.metric(isLinked)
    )
  }

  private data class ProblemsTypesCount(
    val total: Int,
    val missing: Int,
    val fixed: Int
  )

  private fun getProblemsTypesCount(highlightedReportData: HighlightedReportData): ProblemsTypesCount {
    var total = 0
    var missing = 0
    var fixed = 0
    highlightedReportData.sarifProblemPropertiesProvider.value.problemsWithProperties
      .map { it.properties }
      .forEach {
        total++
        when {
          !it.isPresent -> {
            missing++
          }
          // not a mistake, conflicting naming
          it.isMissing -> {
            fixed++
          }
        }
      }
    return ProblemsTypesCount(total = total, missing = missing, fixed = fixed)
  }

  private fun getHighlightReportStateStats(state: HighlightedReportState): StatsHighlightedReportState {
    return when (state) {
      is HighlightedReportState.Selected -> StatsHighlightedReportState.SELECTED
      is HighlightedReportState.Loading -> StatsHighlightedReportState.LOADING
      HighlightedReportState.NotSelected -> StatsHighlightedReportState.NOT_SELECTED
    }
  }

  private val GROUP = EventLogGroup("qodana.plugin.state", 5)

  // --------------------
  // Status of Qodana problems in report
  // --------------------

  private val TOTAL_PROBLEMS_COUNT = EventFields.RoundedInt("total")
  private val MISSING_PROBLEMS_COUNT = EventFields.RoundedInt("missing")
  private val FIXED_PROBLEMS_COUNT = EventFields.RoundedInt("fixed")

  private val REPORT_PROBLEMS_DATA = GROUP.registerEvent(
    "problems.data.reported",
    TOTAL_PROBLEMS_COUNT,
    MISSING_PROBLEMS_COUNT,
    FIXED_PROBLEMS_COUNT
  )

  // --------------------
  // Is Qodana report active
  // --------------------

  private val FIELD_HIGHLIGHTED_REPORT_STATE = EventFields.Enum<StatsHighlightedReportState>("highlighted_report_state")

  enum class StatsHighlightedReportState {
    SELECTED,
    LOADING,
    NOT_SELECTED,
  }

  private val HIGHLIGHTED_REPORT_STATE = GROUP.registerEvent(
    "highlighted_report_state",
    FIELD_HIGHLIGHTED_REPORT_STATE,
    REPORT_TYPE
  )

  // --------------------
  // Link and auth state
  // --------------------

  private val LINK_STATE = GROUP.registerEvent("link_state", IS_LINK)
  private val USER_STATE = GROUP.registerEvent("user_state", FIELD_USER_STATE)

  // --------------------
  // Coverage state
  // --------------------

  private val COVERAGE_IN_REPORT = GROUP.registerEvent(
    "coverage.in.report.shown",
    EventFields.StringList("language", CoverageLanguage.values().map { it.name })
  )

  // --------------------
  // FlexInspect status
  // --------------------

  private val FLEXINSPECT_TOTAL_FILES_FIELD = EventFields.RoundedInt("files_total")
  private val FLEXINSPECT_COMPILED_FILES_FIELD = EventFields.RoundedInt("files_compiled")
  private val FLEXINSPECT_FAILED_FILES_FIELD = EventFields.RoundedInt("files_failed")
  private val FLEXINSPECT_COMPILED_INSPECTIONS_FIELD = EventFields.RoundedInt("inspections_compiled")

  private val FLEXINSPECT_COMPILED = GROUP.registerVarargEvent(
    "flexinspect.compiled.ide",
    FLEXINSPECT_TOTAL_FILES_FIELD,
    FLEXINSPECT_COMPILED_FILES_FIELD,
    FLEXINSPECT_FAILED_FILES_FIELD,
    FLEXINSPECT_COMPILED_INSPECTIONS_FIELD
  )
}