// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.codeInspection.ex.InspectListener.InspectionKind
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsagesCollector
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.internal.statistic.utils.PluginInfo
import com.intellij.internal.statistic.utils.getPluginInfoByDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager

internal const val FLEXINSPECT_STATS_INSPECTION_ID = "flexinspect"

internal object InspectionEventsCollector : CounterUsagesCollector() {
  private val GROUP = EventLogGroup("qodana.inspections", 13)

  override fun getGroup() = GROUP

  private val inspectionIdField = EventFields.StringValidatedByCustomRule<InspectionIdValidationRule>("inspectionId")
  private val durationField = EventFields.Long("duration")
  private val threadIdField = EventFields.Long("threadId")
  private val problemsCountField = EventFields.RoundedInt("problemsCount")
  private val inspectionKindField = EventFields.Enum<InspectionKind>("kind")
  private val activityKindField = EventFields.String("kind", listOf("REFERENCE_SEARCH",
      "GLOBAL_POST_RUN_ACTIVITIES",
      "EXTERNAL_TOOLS_CONFIGURATION",
      "EXTERNAL_TOOLS_EXECUTION"))
  private val lowerBoundField = EventFields.Long("lowerBound")
  private val upperBoundField = EventFields.Long("upperBound")
  private val filesCountField = EventFields.Int("filesCount")
  private val filetypeField = EventFields.StringValidatedByCustomRule<FileTypeUsagesCollector.ValidationRule>("filetype")
  private val totalCountField = EventFields.RoundedInt("totalCount")
  private val analyzedCountField = EventFields.RoundedInt("analyzedCount")

  private val inspectionFinished = GROUP.registerVarargEvent(
    "inspection.finished",
    inspectionIdField,
    durationField,
    threadIdField,
    problemsCountField,
    inspectionKindField,
    EventFields.PluginInfo
  )

  private val activityFinished = GROUP.registerVarargEvent(
    "activity.finished",
    durationField,
    threadIdField,
    activityKindField
  )

  private val inspectionDuration = GROUP.registerVarargEvent(
    "inspection.duration",
    inspectionIdField,
    lowerBoundField,
    upperBoundField,
    problemsCountField,
    filesCountField,
    inspectionKindField,
    EventFields.PluginInfo
  )

  private val inspectionFingerprint = GROUP.registerVarargEvent(
    "inspection.fingerprint",
    filetypeField,
    totalCountField,
    analyzedCountField
  )

  private val qodanaActivityFinished = GROUP.registerEvent(
    "qodana.activity.finished",
    durationField,
    EventFields.Enum<QodanaActivityKind>("activityKind")
  )

  private val flexInspectTotalFilesField = EventFields.RoundedInt("files_total")
  private val flexInspectCompiledFilesField = EventFields.RoundedInt("files_compiled")
  private val flexInspectFailedFiles = EventFields.RoundedInt("files_failed")
  private val flexInspectCompiledInspections = EventFields.RoundedInt("inspections_compiled")

  private val flexInspectCompiled = GROUP.registerVarargEvent(
    "flexinspect.compiled",
    flexInspectTotalFilesField,
    flexInspectCompiledFilesField,
    flexInspectFailedFiles,
    flexInspectCompiledInspections
  )

  enum class QodanaActivityKind {
    LINTER_EXECUTION,
    PROJECT_OPENING,
    PROJECT_CONFIGURATION,
    PROJECT_ANALYSIS
  }

  suspend fun <T> logQodanaActivityDuration(activityKind: QodanaActivityKind, action: suspend () -> T): T {
    val start = System.currentTimeMillis()
    try {
      return action()
    }
    finally {
      qodanaActivityFinished.log(System.currentTimeMillis() - start, activityKind)
    }
  }

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

  @JvmStatic
  fun logInspectionDuration(lowerBound: Long,
                            upperBound: Long,
                            problemsCount: Int,
                            filesCount: Int,
                            tool: InspectionToolWrapper<*, *>,
                            kind: InspectionKind,
                            project: Project) {
    val pluginInfo = getInfo(tool)
    val inspectionId = getInspectionIdToReport(project, pluginInfo, tool.id)

    val pairs = listOf(
      inspectionIdField.with(inspectionId),
      lowerBoundField.with(lowerBound),
      upperBoundField.with(upperBound),
      problemsCountField.with(problemsCount),
      filesCountField.with(filesCount),
      inspectionKindField.with(kind)
    )

    if (pluginInfo != null) {
      inspectionDuration.log(project, pairs + EventFields.PluginInfo.with(pluginInfo))
    }
    else {
      inspectionDuration.log(project, pairs)
    }
  }

  @JvmStatic
  fun logInspectionFinished(duration: Long, threadId: Long, problemsCount: Int, tool: InspectionToolWrapper<*, *>,
                            kind: InspectionKind, project: Project) {
    val pluginInfo = getInfo(tool)
    val inspectionId = getInspectionIdToReport(project, pluginInfo, tool.id)

    val pairs = listOf(
      inspectionIdField.with(inspectionId),
      durationField.with(duration),
      threadIdField.with(threadId),
      problemsCountField.with(problemsCount),
      inspectionKindField.with(kind)
    )

    if (pluginInfo != null) {
      inspectionFinished.log(project, pairs + EventFields.PluginInfo.with(pluginInfo))
    }
    else {
      inspectionFinished.log(project, pairs)
    }
  }

  @JvmStatic
  fun logActivityFinished(duration: Long, threadId: Long, activityKind: String, project: Project) =
    activityFinished.log(
      project,
      durationField.with(duration),
      threadIdField.with(threadId),
      activityKindField.with(activityKind)
    )

  @JvmStatic
  fun logInspectionFingerprint(filetype: String, totalCount: Int, analyzedCount: Int, project: Project) =
    inspectionFingerprint.log(
      project,
      filetypeField.with(filetype),
      totalCountField.with(totalCount),
      analyzedCountField.with(analyzedCount)
    )

  private fun getInfo(tool: InspectionToolWrapper<*, *>): PluginInfo? {
    val extension = tool.extension
    val pluginDescriptor = extension?.pluginDescriptor
    return pluginDescriptor?.let { getPluginInfoByDescriptor(it) }
  }

  private fun getInspectionIdToReport(project: Project, pluginInfo: PluginInfo?, toolId: String): String {
    return when {
      pluginInfo == null && isFlexinspectInspection(project, toolId) -> {
        FLEXINSPECT_STATS_INSPECTION_ID
      }
      pluginInfo != null && pluginInfo.isSafeToReport() -> {
        toolId
      }
      else -> {
        "third.party"
      }
    }
  }

  private fun isFlexinspectInspection(project: Project, toolId: String): Boolean {
    val flexInsectInspectionsStatuses = KtsInspectionsManager.getInstance(project).ktsInspectionsFlow.value ?: return false
    if (flexInsectInspectionsStatuses.isEmpty()) {
      return false
    }

    return flexInsectInspectionsStatuses
      .asSequence()
      .filterIsInstance<InspectionKtsFileStatus.Compiled>()
      .any { compiled ->
        compiled.inspections.inspections.any { it.toolWrapper.id == toolId }
      }
  }
}
