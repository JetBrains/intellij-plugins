package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.problem.SarifProblem

interface QodanaHighlightInfoActionProvider {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaHighlightInfoActionProvider> = ExtensionPointName("org.intellij.qodana.highlightInfoActionProvider")
    fun provide(element: PsiElement?, sarifProblem: SarifProblem): Collection<IntentionAction> =
      EP_NAME.extensionList.flatMap { it.provide(element, sarifProblem) }
  }

  fun provide(element: PsiElement?, sarifProblem: SarifProblem): Collection<IntentionAction>
}