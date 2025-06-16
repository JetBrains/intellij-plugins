// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.attributes

import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolInfo
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag

class AstroAttributeDescriptor(info: HtmlAttributeSymbolInfo, tag: XmlTag?)
  : HtmlAttributeSymbolDescriptor(info, tag) {
  override fun validateValue(context: XmlElement?, value: String?): String? {
    return null
  }
}