package org.jetbrains.vuejs.spellchecker

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueSpellcheckingStrategy : SpellcheckingStrategy() {
  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element is XmlAttributeValue) {
      val jsEmbeddedContent = PsiTreeUtil.getChildOfType(element, JSEmbeddedContent::class.java)
      if (element.valueTextRange == jsEmbeddedContent?.textRange) return EMPTY_TOKENIZER
    }
    return super.getTokenizer(element)
  }
}