package org.angular2.refactoring.inline

import com.intellij.lang.Language
import com.intellij.lang.javascript.refactoring.inline.JSInlineHandler
import com.intellij.lang.javascript.refactoring.inline.JSVarOrFieldInliner
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl

open class Angular2InlineHandler : JSInlineHandler() {

  override fun isEnabledForLanguage(l: Language?): Boolean =
    l is Angular2Language

  override fun canInlineElement(element: PsiElement?): Boolean =
    element is Angular2BlockParameterVariableImpl

  override fun createInliner(settings: Settings, elementToInline: PsiElement): Inliner {
    return Angular2LetVariableInliner(elementToInline as Angular2BlockParameterVariableImpl,
                                      settings as JSVarOrFieldInliner.MySettings)
  }

}