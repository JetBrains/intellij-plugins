// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.codeInsight.html.WebSymbolsHtmlAdditionalContextProvider
import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolAttributeDescriptor
import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolHtmlElementInfo
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.javascript.web.symbols.WebSymbolNamesProvider
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.psi.xml.XmlTag
import icons.AngularJSIcons
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2TagDescriptor
import org.angular2.lang.html.Angular2HtmlFileType
import org.angular2.lang.svg.Angular2SvgFileType

import javax.swing.*
import java.util.Collections

import com.intellij.javascript.web.symbols.WebSymbol.Companion.KIND_HTML_EVENTS
import org.angular2.web.Angular2IgnoredAttributesProvider

class Angular2Framework : WebFramework() {

  override val icon: Icon?
    get() = AngularJSIcons.Angular2

  override val displayName: String
    get() = "Angular"

  override val htmlFileType: WebFrameworkHtmlFileType?
    get() = Angular2HtmlFileType.INSTANCE

  override val svgFileType: WebFrameworkHtmlFileType?
    get() = Angular2SvgFileType.INSTANCE

  override fun createHtmlAttributeDescriptor(info: WebSymbolHtmlAttributeInfo,
                                             tag: XmlTag?): WebSymbolAttributeDescriptor =
    Angular2AttributeDescriptor(info, tag)

  override fun createHtmlElementDescriptor(info: WebSymbolHtmlElementInfo,
                                           tag: XmlTag): WebSymbolElementDescriptor =
    Angular2TagDescriptor(info, tag)

  override fun getNames(namespace: WebSymbolsContainer.Namespace,
                        kind: String,
                        name: String,
                        target: WebSymbolNamesProvider.Target): List<String> =
    if (namespace === WebSymbolsContainer.Namespace.HTML && kind == KIND_HTML_EVENTS) {
      listOf(name)
    }
    else emptyList()

  override fun getAttributesToIgnoreInCodeCompletion(tag: XmlTag): List<String> =
    Angular2IgnoredAttributesProvider.get(tag)

  companion object {

    const val ID = "angular"

    @JvmStatic
    val instance: WebFramework
      get() = get(ID)
  }
}
