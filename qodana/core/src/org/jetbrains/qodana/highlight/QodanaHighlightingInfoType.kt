package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import org.jetbrains.qodana.problem.SarifProblem

class QodanaHighlightingInfoType(
  val sarifProblem: SarifProblem,
  private val originalType: HighlightInfoType
) : HighlightInfoType by originalType, HighlightInfoType.UpdateOnTypingSuppressible {
  override fun needsUpdateOnTyping() =
    if (originalType is HighlightInfoType.UpdateOnTypingSuppressible) {
      originalType.needsUpdateOnTyping()
    } else {
      false
    }
}