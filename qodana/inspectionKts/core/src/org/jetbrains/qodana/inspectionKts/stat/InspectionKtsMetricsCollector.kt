package org.jetbrains.qodana.inspectionKts.stat

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.FUS_RECORDER
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager

internal class InspectionKtsMetricsCollector : ProjectUsagesCollector() {
  private val GROUP = EventLogGroup("qodana.flex.inspect.ide", 1, FUS_RECORDER)

  override fun getGroup() = GROUP

  private val FLEX_INSPECT_TOTAL_FILES_FIELD = EventFields.RoundedInt("files_total")
  private val FLEX_INSPECT_COMPILED_FILES_FIELD = EventFields.RoundedInt("files_compiled")
  private val FLEX_INSPECT_FAILED_FILES_FIELD = EventFields.RoundedInt("files_failed")
  private val FLEX_INSPECT_COMPILED_INSPECTIONS_FIELD = EventFields.RoundedInt("inspections_compiled")

  private val FLEX_INSPECT_COMPILED = GROUP.registerVarargEvent(
    "flexinspect.compiled.ide",
    FLEX_INSPECT_TOTAL_FILES_FIELD,
    FLEX_INSPECT_COMPILED_FILES_FIELD,
    FLEX_INSPECT_FAILED_FILES_FIELD,
    FLEX_INSPECT_COMPILED_INSPECTIONS_FIELD
  )

  override fun getMetrics(project: Project): Set<MetricEvent> {
    val inspectionKtsFileStatuses = KtsInspectionsManager.getInstance(project).ktsInspectionsFlow.value ?: emptySet()

    val compiledInspectionFiles = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Compiled>()

    val inspectionKtsFilesCount = inspectionKtsFileStatuses.count()
    val compiledInspectionKtsFilesCount = compiledInspectionFiles.count()
    val failedToCompileInspectionKtsFilesCount = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Error>().count()
    val inspectionKtsInspectionsCount = compiledInspectionFiles.flatMap { it.inspections.inspections }.count()

    val inspectionKtsMetric = FLEX_INSPECT_COMPILED.metric(
      FLEX_INSPECT_TOTAL_FILES_FIELD.with(inspectionKtsFilesCount),
      FLEX_INSPECT_COMPILED_FILES_FIELD.with(compiledInspectionKtsFilesCount),
      FLEX_INSPECT_FAILED_FILES_FIELD.with(failedToCompileInspectionKtsFilesCount),
      FLEX_INSPECT_COMPILED_INSPECTIONS_FIELD.with(inspectionKtsInspectionsCount)
    )
    return mutableSetOf(inspectionKtsMetric)
  }
}
