package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext

interface QuickFixesStrategyProvider {
  companion object {
    val EP_NAME: ExtensionPointName<QuickFixesStrategyProvider> =
      ExtensionPointName.create("org.intellij.qodana.quickFixesStrategyProvider")

    suspend fun runCustomProviders(sarifRun: Run, runContext: QodanaRunContext) {
      for (provider in EP_NAME.extensionList) {
        provider.applyFixes(sarifRun, runContext)
      }
    }
  }

  suspend fun applyFixes(sarifRun: Run, runContext: QodanaRunContext)
}