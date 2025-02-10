package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandlerProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class IncorrectFormattingResultHandlerProviderQodana: IncorrectFormattingResultHandlerProvider {
  override fun getApplicableResultHandler(globalContext: GlobalInspectionContext): IncorrectFormattingResultHandler? {
    return if (globalContext is QodanaGlobalInspectionContext) {
      IncorrectFormattingResultHandlerQodana()
    } else null
  }
}