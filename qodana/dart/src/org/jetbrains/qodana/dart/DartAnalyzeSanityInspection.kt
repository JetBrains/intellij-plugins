package org.jetbrains.qodana.dart

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_SEVERITY_ATTRIBUTE
import com.intellij.codeInspection.ExportedInspectionsResultModifier
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import org.jdom.Element
import java.nio.file.Path

internal const val DART_SANITY_INSPECTION_ID = "DartAnalyzeSanity"

internal class DartAnalyzeSanityInspection(
  private val runnerFactory: DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory { DartExecutableRunner() },
) : GlobalInspectionTool() {
  override fun getDisplayName(): String = QodanaDartBundle.message("inspection.dart.sanity.name")

  override fun getGroupDisplayName(): String = QodanaDartBundle.message("inspection.dart.analyze.taxonomy.name")

  override fun getGroupPath(): Array<String> = arrayOf(QodanaDartBundle.message("inspection.dart.analyze.group.name"), groupDisplayName)

  override fun isReadActionNeeded(): Boolean = false

  override fun isGraphNeeded(): Boolean = false

  override fun runInspection(
    scope: AnalysisScope,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    if (!scope.containsDartFiles()) return
    val projectPath = Path.of(globalContext.project.basePath ?: return)
    val failure = runBlockingCancellable { runnerFactory.create().checkSdk(projectPath) } ?: return
    logger<DartAnalyzeSanityInspection>().warn("Dart SDK check failed: ${failure.message}")
    registerDartProblem(failure, projectPath, manager, globalContext, problemDescriptionsProcessor, sanityFailure = true)
  }
}

internal class DartSanityProblemDescriptor(descriptor: CommonProblemDescriptor) :
  CommonProblemDescriptor by descriptor, ExportedInspectionsResultModifier {
  override fun modifyResult(element: Element) {
    element.getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)
      .setAttribute(INSPECTION_RESULTS_ID_ATTRIBUTE, DART_SANITY_INSPECTION_ID)
      .setAttribute(INSPECTION_RESULTS_SEVERITY_ATTRIBUTE, HighlightSeverity.ERROR.name)
      .setText(QodanaDartBundle.message("inspection.dart.sanity.name"))
  }
}
