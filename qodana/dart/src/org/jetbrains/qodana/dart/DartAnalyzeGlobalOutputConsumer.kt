package org.jetbrains.qodana.dart

import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT
import com.intellij.openapi.project.Project
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path

internal class DartAnalyzeGlobalOutputConsumer : GlobalOutputConsumer {
  override fun ownedFiles(paths: List<Path>): List<Path> = paths.filter { it.fileName.toString() == "DartAnalyze.xml" }

  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit,
  ) {
    for (path in paths) {
      GlobalOutputConsumer.consumeOutputXmlFile(path) { inspectionId, root ->
        val (sanityProblems, analyzeProblems) = root.getChildren("problem").partition { element ->
          element.resultId() == DART_SANITY_INSPECTION_ID
        }
        if (sanityProblems.isNotEmpty()) {
          consumer(sanityProblems.map(GlobalOutputConsumer::createXmlProblem), DART_SANITY_INSPECTION_ID)
        }
        val problems = analyzeProblems.map { element ->
          val problem = GlobalOutputConsumer.createXmlProblem(element)
          val derivedInspectionId = element.resultId() ?: return@map problem
          val code = dartInspectionCode(derivedInspectionId) ?: return@map problem
          if (profileState.getInspectionState(derivedInspectionId) != null) return@map problem
          profileState.registerDerivedInspection(
            DerivedInspection(
              derivedInspectionId,
              inspectionId,
              QodanaDartBundle.message("inspection.dart.analyze.diagnostic.name", code),
              QodanaDartBundle.message("inspection.dart.analyze.diagnostic.description", code),
            )
          )
          problem
        }
        consumer(problems, inspectionId)
      }
    }
  }
}

private fun Element.resultId(): String? =
  getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)?.getAttributeValue(INSPECTION_RESULTS_ID_ATTRIBUTE)
