package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.InspectListener.InspectionKind
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.application


class InspectionEventsListener : InspectListener {
  private val verboseLogging = java.lang.Boolean.getBoolean("qodana.verbose.inspections.statistic")

  override fun inspectionFinished(duration: Long, threadId: Long, problemsCount: Int, tool: InspectionToolWrapper<*, *>,
                                  kind: InspectionKind, file: PsiFile?, project: Project) {
    if (!application.isHeadlessEnvironment) {
      return
    }

    if (verboseLogging) {
      InspectionEventsCollector.logInspectionFinished(duration, threadId, problemsCount, tool, kind, project)
    }
    val service = project.getService(InspectionDurationsAggregatorService::class.java)
    service.addInspectionFinishedEvent(duration, problemsCount, tool, kind)

    InspectionInfoQodanaReporterService.getInstance(project)
      .addInspectionFinishedEvent(duration, problemsCount, tool, kind, file?.virtualFile)
  }

  override fun activityFinished(duration: Long, threadId: Long, activityKind: String, project: Project) {
    if (!application.isHeadlessEnvironment) {
      return
    }

    if (verboseLogging) {
      InspectionEventsCollector.logActivityFinished(duration, threadId, activityKind, project)
    }
  }

  override fun fileAnalyzed(file: PsiFile, project: Project) {
    if (!application.isHeadlessEnvironment) {
      return
    }

    project.getService(InspectionFingerprintAggregatorService::class.java).registerAnalyzedFile(file)
  }
}
