// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.polySymbols.html.HtmlSymbolsXmlExtension
import com.intellij.polySymbols.html.StandardHtmlSymbol
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.utils.unwrapMatchedSymbols
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.lang.AstroFileType

class AstroHtmlExtension : HtmlSymbolsXmlExtension() {

  override fun isAvailable(file: PsiFile?): Boolean {
    return file != null
           && file.fileType == AstroFileType
  }

  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
    val descriptor = tag.descriptor
    if (descriptor is HtmlElementSymbolDescriptor) {
      val hasStandardSymbol = descriptor.symbol
        .unwrapMatchedSymbols()
        .any { it is StandardHtmlSymbol }
      if (!hasStandardSymbol) return true
    }
    return super.isSelfClosingTagAllowed(tag)
  }

  override fun isRequiredAttributeImplicitlyPresent(tag: XmlTag?, attrName: String?): Boolean {
    if (tag == null || attrName == null) return false
    for (attribute in tag.attributes) {
      val attributeText = attribute.text.trim()
      if (attributeText == "{$attrName}") {
        return true
      }
    }

    return super.isRequiredAttributeImplicitlyPresent(tag, attrName)
  }
}
