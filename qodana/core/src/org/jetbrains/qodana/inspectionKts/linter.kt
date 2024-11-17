package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.ProjectInspectionToolRegistrar
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector

private val LOG = Logger.getInstance("org.jetbrains.qodana.inspectionKts.linter")

internal suspend fun waitForDynamicInspectionsInitialization(project: Project, messageReporter: QodanaMessageReporter) {
  fun reportFlexInspectError(message: String) {
    messageReporter.reportError("FlexInspect| $message")
  }

  LOG.info("Loading dynamic inspections (FlexInspect)")
  ProjectInspectionToolRegistrar.getInstance(project).waitForDynamicInspectionsInitialization()
  val inspectionKtsFileStatuses = KtsInspectionsManager.getInstance(project).ktsInspectionsFlow.value
  if (inspectionKtsFileStatuses == null) {
    error("FlexInspect is not initialized")
  }

  inspectionKtsFileStatuses.forEach { status ->
    when (status) {
      is InspectionKtsFileStatus.Cancelled -> {
        reportFlexInspectError("compilation of ${status.file} was cancelled")
      }
      is InspectionKtsFileStatus.Compiled -> {
      }
      is InspectionKtsFileStatus.Compiling -> {
        reportFlexInspectError("is still compiling, must have been already compiled")
      }
      is InspectionKtsFileStatus.Error -> {
        reportFlexInspectError("failed to compile ${status.file}")
        messageReporter.reportError(status.exception)
      }
    }
  }
  val compiledInspectionFiles = inspectionKtsFileStatuses.filterIsInstance<InspectionKtsFileStatus.Compiled>()

  val inspectionKtsFilesCount = inspectionKtsFileStatuses.count()
  val compiledInspectionKtsFilesCount = compiledInspectionFiles.count()
  val failedToCompileInspectionKtsFilesCount = compiledInspectionFiles.filterIsInstance<InspectionKtsFileStatus.Error>().count()
  val inspectionKtsInspectionsCount = compiledInspectionFiles.flatMap { it.inspections.inspections }.count()

  InspectionEventsCollector.logInspectionKtsCompiled(
    inspectionKtsFilesCount,
    compiledInspectionKtsFilesCount,
    failedToCompileInspectionKtsFilesCount,
    inspectionKtsInspectionsCount
  )

  LOG.info(
    """
    FlexInspect: finished loading
    - total number of inspection.kts files: $inspectionKtsFilesCount}
    - number of compiled inspection.kts files: $compiledInspectionKtsFilesCount}
    - number of inspections from compiled inspection.kts files: $inspectionKtsInspectionsCount}
    - number of failed to compile inspection.kts files: $failedToCompileInspectionKtsFilesCount} 
  """.trimIndent()
  )
}