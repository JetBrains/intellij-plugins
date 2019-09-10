// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language.javascript

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class JsStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
  override fun isRelevant(element: PsiElement) = element is JSLiteralExpression

  override fun check(element: PsiElement) = when (element) {
    is JSStringTemplateExpression -> {
      val textRanges = element.stringRanges
      GrammarChecker.default.check(element, tokenRules = GrammarChecker.TokenRules(ignoreByIndex = linkedSetOf({ _, index ->
                                                                                                                 textRanges.all { range -> index !in range }
                                                                                                               })))
    }
    else -> GrammarChecker.default.check(element)
  }
}
