// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.codeInsight.FileModificationService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.XmlElementFactoryImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.IncorrectOperationException
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector

class Angular2HtmlNgContentSelectorManipulator : AbstractElementManipulator<Angular2HtmlNgContentSelector?>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: Angular2HtmlNgContentSelector,
                                   range: TextRange,
                                   newContent: String): Angular2HtmlNgContentSelector {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return element
    val newSelector = range.replace(element.text, newContent)
    val newSelectAttribute = createNgContentSelectAttribute(element.project, newSelector)
    val newSelectorElement = PsiTreeUtil.findChildOfType(
      newSelectAttribute.valueElement,
      Angular2HtmlNgContentSelector::class.java)
    LOG.assertTrue(newSelectorElement != null, newSelectAttribute.parent.text)
    return element.replace(newSelectorElement!!) as Angular2HtmlNgContentSelector
  }

  companion object {
    private val LOG = Logger.getInstance(Angular2HtmlNgContentSelectorManipulator::class.java)

    private fun createNgContentSelectAttribute(project: Project, value: String): XmlAttribute {
      val quotedValue = XmlElementFactoryImpl.quoteValue(value)
      val tag = XmlElementFactory.getInstance(project).createTagFromText(
        "<ng-content select=$quotedValue></ng-content>",
        Angular2HtmlLanguage.INSTANCE)
      val attributes = tag.attributes
      LOG.assertTrue(attributes.size == 1, tag.text)
      return attributes[0]
    }
  }
}