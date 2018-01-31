package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.codeInsight.highlighting.HtmlClosingTagErrorFilter
import com.intellij.psi.PsiErrorElement
import org.jetbrains.vuejs.VueLanguage

/**
 * @author Irina.Chernushina on 1/10/2018.
 */
class VueHighlightErrorFilter: HighlightErrorFilter() {
  override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
    return element.language !== VueLanguage.INSTANCE || !HtmlClosingTagErrorFilter.skip(element)
  }
}