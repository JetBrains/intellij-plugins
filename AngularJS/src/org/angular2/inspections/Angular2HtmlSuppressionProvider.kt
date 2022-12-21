// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttributeValue
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.INTERPOLATION_START

class Angular2HtmlSuppressionProvider : DefaultXmlSuppressionProvider() {

  private val HTML_UNKNOWN_TARGET_INSPECTION_ID = InspectionProfileEntry.getShortName(HtmlUnknownTargetInspection::class.java.simpleName)

  override fun isProviderAvailable(file: PsiFile): Boolean {
    return file.language.isKindOf(Angular2HtmlLanguage.INSTANCE)
  }

  override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
    return if (HTML_UNKNOWN_TARGET_INSPECTION_ID == inspectionId
               && element is XmlAttributeValue
               && element.children.any { el -> el.node.elementType === INTERPOLATION_START }) {
      true
    }
    else super.isSuppressedFor(element, inspectionId)
  }
}
