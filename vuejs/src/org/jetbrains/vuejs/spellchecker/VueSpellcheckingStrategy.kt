// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.spellchecker

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.spellchecker.xml.HtmlSpellcheckingStrategy
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression

class VueSpellcheckingStrategy : HtmlSpellcheckingStrategy() {
  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element is XmlAttributeValue) {
      val jsEmbeddedContent = PsiTreeUtil.getChildOfType(element, JSEmbeddedContent::class.java)
                              ?: PsiTreeUtil.getChildOfType(element, ASTWrapperPsiElement::class.java)
                                ?.let { PsiTreeUtil.getChildOfType(it, VueJSEmbeddedExpression::class.java) }
      if (element.valueTextRange == jsEmbeddedContent?.textRange) return EMPTY_TOKENIZER
    }
    return super.getTokenizer(element)
  }
}
