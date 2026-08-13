package org.jetbrains.qodana.dart

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalTraceFlowOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile

internal class DartAnalyzeGlobalInspectionOutputConsumer : GlobalTraceFlowOutputConsumer() {
  override fun getInspectionName(): String = DartAnalyzeGlobalInspection.SHORT_NAME

  override fun getDependentInspectionNames(project: Project, profile: QodanaInspectionProfile): Collection<String> =
    profile.tools.filter { it.shortName.isDartAnalyzeTool() }.map { it.shortName }
}
