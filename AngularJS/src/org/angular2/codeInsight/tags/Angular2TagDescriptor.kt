// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags

import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolHtmlElementInfo
import com.intellij.psi.xml.XmlTag
import org.angular2.entities.Angular2Directive
import org.angular2.web.Angular2DescriptorSymbolsProvider

class Angular2TagDescriptor(info: WebSymbolHtmlElementInfo, tag: XmlTag) : WebSymbolElementDescriptor(info, tag) {

  @get:JvmName("isImplied")
  val implied: Boolean get() = tagInfoProvider.errorSymbols.isEmpty() && tagInfoProvider.nonDirectiveSymbols.isNotEmpty()

  val sourceDirectives: List<Angular2Directive> get() = tagInfoProvider.directives

  private val tagInfoProvider by lazy(LazyThreadSafetyMode.NONE) { Angular2DescriptorSymbolsProvider(this.symbol) }

}