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
  private val GROUP = EventLogGroup("qodana.flex.inspect.ide", 1, FUS_RECORDER, "Group with events related to FlexInspect in IDE")

  override fun getGroup() = GROUP

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

  override fun getMetrics(project: Project): Set<MetricEvent> {
    val inspectionKtsFileStatuses = KtsInspectionsManager.getInstance(project).ktsInspectionsFlow.value ?: emptySet()

    val compiledInspectionFiles = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Compiled>()

    val inspectionKtsFilesCount = inspectionKtsFileStatuses.count()
    val compiledInspectionKtsFilesCount = compiledInspectionFiles.count()
    val failedToCompileInspectionKtsFilesCount = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Error>().count()
    val inspectionKtsInspectionsCount = compiledInspectionFiles.flatMap { it.inspections.inspections }.count()

    val inspectionKtsMetric = FLEXINSPECT_COMPILED.metric(
      FLEXINSPECT_TOTAL_FILES_FIELD.with(inspectionKtsFilesCount),
      FLEXINSPECT_COMPILED_FILES_FIELD.with(compiledInspectionKtsFilesCount),
      FLEXINSPECT_FAILED_FILES_FIELD.with(failedToCompileInspectionKtsFilesCount),
      FLEXINSPECT_COMPILED_INSPECTIONS_FIELD.with(inspectionKtsInspectionsCount)
    )
    return mutableSetOf(inspectionKtsMetric)
  }
}
