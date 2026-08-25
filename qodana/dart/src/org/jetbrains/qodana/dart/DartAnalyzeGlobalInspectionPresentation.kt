package org.jetbrains.qodana.dart

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ui.AggregateResultsExporter
import com.intellij.codeInspection.ui.DefaultInspectionToolPresentation
import com.intellij.openapi.application.runReadActionBlocking
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.profile.DetectedProblemGroup
import java.util.function.Consumer

internal class DartAnalyzeGlobalInspectionPresentation(
  toolWrapper: InspectionToolWrapper<*, *>,
  context: GlobalInspectionContextImpl,
) : DefaultInspectionToolPresentation(toolWrapper, context), AggregateResultsExporter {
  override fun exportAggregateResults(resultConsumer: Consumer<in Element>) {
    runReadActionBlocking {
      val qodanaContext = context as? QodanaGlobalInspectionContext
      qodanaContext?.profileState?.registerRuntimeInspections(
        problemElements.keys().flatMap { refEntity ->
          if (isExcluded(refEntity)) return@flatMap emptyList()
          problemElements[refEntity].orEmpty().filterNot(::isExcluded).filterIsInstance<ProblemDescriptor>()
        }.mapNotNull { it.problemGroup?.problemName }.map { problemGroupId ->
          DetectedProblemGroup(DART_ANALYZE_SHORT_NAME, problemGroupId)
        },
      )
      problemElements.keys().forEach { refEntity ->
        if (isExcluded(refEntity)) return@forEach
        val descriptors = problemElements[refEntity]?.filterNot(::isExcluded) ?: return@forEach
        val problemDescriptors = descriptors.filterIsInstance<ProblemDescriptor>()
        problemDescriptors.forEach { descriptor ->
          exportResults(arrayOf(descriptor), refEntity, Consumer { problemElement ->
            val inspectionId = descriptor.problemGroup?.problemName
              ?.takeIf { qodanaContext?.profileState?.hasInspection(it, DART_ANALYZE_SHORT_NAME) == true }
              ?: DART_ANALYZE_SHORT_NAME
            problemElement.getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)?.setAttribute("id", inspectionId)
            resultConsumer.accept(problemElement)
          }) { false }
        }
      }
    }
  }
}
