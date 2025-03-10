package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandlerProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class IncorrectFormattingResultHandlerProviderQodana: IncorrectFormattingResultHandlerProvider {
  companion object {
    const val QODANA_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY: String = "qodana.new.incorrect.formatting.output"
  }
  override fun getApplicableResultHandler(globalContext: GlobalInspectionContext): IncorrectFormattingResultHandler? {
    val newOutputProperty = System.getProperty(QODANA_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY, "false").toBoolean()
    return if (newOutputProperty && globalContext is QodanaGlobalInspectionContext) {
      IncorrectFormattingResultHandlerQodana()
    } else null
  }
}