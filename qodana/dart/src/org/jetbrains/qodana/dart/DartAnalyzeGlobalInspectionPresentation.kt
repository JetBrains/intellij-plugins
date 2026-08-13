package org.jetbrains.qodana.dart

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.reference.RefEntity
import com.intellij.codeInspection.ui.AggregateResultsExporter
import com.intellij.codeInspection.ui.DefaultInspectionToolPresentation
import com.intellij.openapi.application.runReadActionBlocking
import org.jdom.Element
import java.util.function.Consumer

internal class DartAnalyzeGlobalInspectionPresentation(
  toolWrapper: InspectionToolWrapper<*, *>,
  context: GlobalInspectionContextImpl,
) : DefaultInspectionToolPresentation(toolWrapper, context), AggregateResultsExporter {
  override fun exportAggregateResults(resultConsumer: Consumer<in Element>) {
    runReadActionBlocking {
      problemElements.keys().forEach { refEntity ->
        if (isExcluded(refEntity)) return@forEach
        val descriptors = problemElements[refEntity]?.filterNot(::isExcluded) ?: return@forEach
        descriptors.filterIsInstance<ProblemDescriptor>().forEach { descriptor ->
          exportResults(arrayOf(descriptor), refEntity, Consumer { problemElement ->
            descriptor.problemGroup?.problemName?.let { inspectionId ->
              problemElement.getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)?.setAttribute("id", inspectionId)
            }
            resultConsumer.accept(problemElement)
          }) { false }
        }
      }
    }
  }
}
