// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language.xml

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterNotToSet

class XmlSupport : LanguageSupport() {
  override fun isRelevant(element: PsiElement) = element is XmlText || (element is XmlToken && element.tokenType == XML_ATTRIBUTE_VALUE_TOKEN)

  override fun check(element: PsiElement) = GrammarChecker.default.check(
    element).filterNotToSet { typo -> typo.location.isAtStart() || typo.location.isAtEnd() }
}
