package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandlerProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class IncorrectFormattingResultHandlerProviderQodana: IncorrectFormattingResultHandlerProvider {
  companion object {
    const val QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY: String = "qodana.enable.new.incorrect.formatting.output"
  }

  override fun getApplicableResultHandler(globalContext: GlobalInspectionContext): IncorrectFormattingResultHandler? {
    val newOutputProperty = java.lang.Boolean.parseBoolean(System.getProperty(QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY, "true"));
    return if (newOutputProperty && globalContext is QodanaGlobalInspectionContext) {
      IncorrectFormattingResultHandlerQodana()
    } else null
  }
}