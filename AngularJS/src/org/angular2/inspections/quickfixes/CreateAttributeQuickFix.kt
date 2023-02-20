// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.application.options.editor.WebEditorOptions
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.project.Project
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.util.HtmlUtil
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class CreateAttributeQuickFix(private val myAttributeName: String) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-attribute.name", myAttributeName)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-attribute.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val tag = descriptor.psiElement.parentOfType<XmlTag>(true)
    if (tag == null || tag.getAttribute(myAttributeName) != null) {
      return
    }
    val attributeDescriptor = tag.descriptor?.getAttributeDescriptor(myAttributeName, tag)

    val insertQuotes = WebEditorOptions.getInstance().isInsertQuotesForAttributeValue
                       && !preferNoValue(tag, attributeDescriptor)

    val attribute = tag.setAttribute(myAttributeName, "")

    val value = attribute?.valueElement
    if (value != null) {
      if (!insertQuotes) {
        value.prevSibling.delete()
        value.delete()
      } else
        PsiNavigationSupport.getInstance()
          .createNavigatable(project, attribute.containingFile.virtualFile ?: return, value.textRange.startOffset + 1)
          .navigate(true)
    }
  }

  private fun preferNoValue(tag: XmlTag?, attributeDescriptor: XmlAttributeDescriptor?) =
    tag is HtmlTag
    && attributeDescriptor != null
    && HtmlUtil.isShortNotationOfBooleanAttributePreferred()
    && (
      HtmlUtil.isBooleanAttribute(attributeDescriptor, tag)
      || (attributeDescriptor.isEnumerated && attributeDescriptor.enumeratedValues?.any { it.isEmpty() } == true)
       )
}
