package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import org.jetbrains.qodana.staticAnalysis.sarif.ContextMarginProvider
import com.jetbrains.qodana.sarif.model.Result

/**
 * Sets margin to 1 line to make SARIF smaller and make baseline comparison easier.
 */
class CoverageContextMarginProvider: ContextMarginProvider {
  override fun supportedRule(result: Result): Boolean = COVERAGE_INSPECTIONS_NAMES.contains(result.ruleId)

  override fun redefineMargin(result: Result, defaultMargin: Int): Int = 0
}