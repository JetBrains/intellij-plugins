// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.html.polySymbols.HtmlSymbolQueryConfigurator
import com.intellij.html.polySymbols.PolySymbolsXmlExtension
import com.intellij.html.polySymbols.elements.PolySymbolElementDescriptor
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.polySymbols.utils.unwrapMatchedSymbols
import org.jetbrains.astro.lang.AstroFileType

class AstroHtmlExtension : PolySymbolsXmlExtension() {

  override fun isAvailable(file: PsiFile?): Boolean {
    return file != null
           && file.fileType == AstroFileType
  }

  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
    val descriptor = tag.descriptor
    if (descriptor is PolySymbolElementDescriptor) {
      val hasStandardSymbol = descriptor.symbol
        .unwrapMatchedSymbols()
        .any { it is HtmlSymbolQueryConfigurator.StandardHtmlSymbol }
      if (!hasStandardSymbol) return true
    }
    return super.isSelfClosingTagAllowed(tag)
  }
}
