package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.qodana.sarif.model.Result

/**
 * Provides the ability to redefine margin for specific rules during SARIF computation
 */
interface ContextMarginProvider {
  companion object {
    val EP_NAME: ExtensionPointName<ContextMarginProvider> =
      ExtensionPointName.create("org.intellij.qodana.contextMarginProvider")

    /**
     * Runs registered context margin providers. [defaultMargin] will be returned by default
     */
    internal fun resetContextMargin(result: Result, defaultMargin: Int): Int {
      for (provider in EP_NAME.extensionList) {
        if (provider.supportedRule(result)) {
          return provider.redefineMargin(result, defaultMargin)
        }
      }
      return defaultMargin
    }
  }

  fun supportedRule(result: Result): Boolean

  fun redefineMargin(result: Result, defaultMargin: Int): Int
}