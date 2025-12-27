package org.jetbrains.qodana.inspectionKts.stat

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.FUS_RECORDER
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

internal object InspectionKtsEventsCollector : CounterUsagesCollector() {
  private val GROUP = EventLogGroup("qodana.flex.inspect", 1, FUS_RECORDER, "Group with events related to FlexInspect")

  override fun getGroup() = GROUP

  private val flexInspectTotalFilesField = EventFields.RoundedInt("files_total")
  private val flexInspectCompiledFilesField = EventFields.RoundedInt("files_compiled")
  private val flexInspectFailedFiles = EventFields.RoundedInt("files_failed")
  private val flexInspectCompiledInspections = EventFields.RoundedInt("inspections_compiled")

  private val flexInspectCompiled = GROUP.registerVarargEvent(
    "flex.inspect.compiled",
    flexInspectTotalFilesField,
    flexInspectCompiledFilesField,
    flexInspectFailedFiles,
    flexInspectCompiledInspections
  )

  fun logInspectionKtsCompiled(
    inspectionKtsFilesCount: Int,
    compiledInspectionKtsFilesCount: Int,
    failedToCompileInspectionKtsFilesCount: Int,
    inspectionKtsInspectionsCount: Int,
  ) {
    flexInspectCompiled.log(
      flexInspectTotalFilesField.with(inspectionKtsFilesCount),
      flexInspectCompiledFilesField.with(compiledInspectionKtsFilesCount),
      flexInspectFailedFiles.with(failedToCompileInspectionKtsFilesCount),
      flexInspectCompiledInspections.with(inspectionKtsInspectionsCount)
    )
  }
}
