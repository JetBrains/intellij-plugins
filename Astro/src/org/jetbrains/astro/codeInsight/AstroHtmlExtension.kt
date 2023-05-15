// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator
import com.intellij.html.webSymbols.WebSymbolsXmlExtension
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.jetbrains.astro.lang.AstroFileType

class AstroHtmlExtension : WebSymbolsXmlExtension() {

  override fun isAvailable(file: PsiFile?): Boolean {
    return file != null
            && file.fileType == AstroFileType.INSTANCE
  }

  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
    val descriptor = tag.descriptor
    if (descriptor is WebSymbolElementDescriptor) {
      val hasStandardSymbol = descriptor.symbol
        .unwrapMatchedSymbols()
        .any { it is WebSymbolsHtmlQueryConfigurator.StandardHtmlSymbol }
      if (!hasStandardSymbol) return true
    }
    return super.isSelfClosingTagAllowed(tag)
  }
}
