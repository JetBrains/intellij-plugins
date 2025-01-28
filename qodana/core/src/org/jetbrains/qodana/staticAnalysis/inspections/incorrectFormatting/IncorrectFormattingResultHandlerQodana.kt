package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInsight.daemon.impl.ProblemRelatedLocation
import com.intellij.codeInsight.daemon.impl.withRelatedLocations
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingInspectionHelper
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class IncorrectFormattingResultHandlerQodana: IncorrectFormattingResultHandler {
  override fun getResults(reportPerFile: Boolean, helper: IncorrectFormattingInspectionHelper): Array<ProblemDescriptor>? {
    val p = helper.createAllReports()
    if (p != null && p.isNotEmpty()) {
      return arrayOf(helper.createGlobalReport().withRelatedLocations(
        evenlyDistributedItems(p, 5).map { problemDescriptor -> ProblemRelatedLocation(problemDescriptor.startElement, problemDescriptor.endElement, "") }
      )
      )
    }
    return null
  }

  override fun isCorrectHandlerForContext(globalContext: GlobalInspectionContext): Boolean {
    return globalContext is QodanaGlobalInspectionContext
  }

  private fun evenlyDistributedItems(array: Array<ProblemDescriptor>, itemCount: Int): Array<ProblemDescriptor> {
    if (itemCount <= 0) return emptyArray()
    if (array.isEmpty()) return emptyArray()
    if (itemCount >= array.size) return array

    val step = array.size.toDouble() / itemCount
    return (0 until itemCount)
      .map { (it * step).toInt() }
      .map { array[it] }
      .toTypedArray()
  }
}