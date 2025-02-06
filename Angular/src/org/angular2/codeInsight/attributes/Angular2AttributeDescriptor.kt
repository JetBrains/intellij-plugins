// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import org.angular2.entities.Angular2Directive
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute
import org.angular2.web.Angular2DescriptorSymbolsProvider
import org.angular2.web.Angular2SymbolOrigin

class Angular2AttributeDescriptor(info: WebSymbolHtmlAttributeInfo, tag: XmlTag?)
  : WebSymbolAttributeDescriptor(info, tag) {

  /**
   * Represents most of the matched directives, even these out-of-scopes. Some directives
   * might be filtered out though, if there is a better match. This property should not
   * be used where perfect matching is required.
   *
   * Prefer to use [Angular2ApplicableDirectivesProvider] and [org.angular2.codeInsight.Angular2DeclarationsScope]
   */
  val sourceDirectives: List<Angular2Directive> get() = bindingInfoProvider.directives

  @get:JvmName("hasErrorSymbols")
  val hasErrorSymbols: Boolean
    get() = bindingInfoProvider.errorSymbols.isNotEmpty()

  @get:JvmName("hasNonDirectiveSymbols")
  val hasNonDirectiveSymbols: Boolean
    get() = bindingInfoProvider.nonDirectiveSymbols.isNotEmpty()

  val info: Angular2AttributeNameParser.AttributeInfo by lazy(LazyThreadSafetyMode.PUBLICATION) {
    tag?.attributes?.find { it.name == name }?.asSafely<Angular2HtmlBoundAttribute>()?.attributeInfo
    ?: Angular2AttributeNameParser.parse(name, tag)
  }

  private val bindingInfoProvider by lazy(LazyThreadSafetyMode.PUBLICATION) { Angular2DescriptorSymbolsProvider(this.symbol) }

  companion object {
    @JvmStatic
    @Deprecated(message = "Deprecated, returns fake descriptor. Use web-types or Web Symbols instead")
    fun create(
      tag: XmlTag,
      attributeName: String,
      @Suppress("UNUSED_PARAMETER")
      element: PsiElement,
    ): Angular2AttributeDescriptor {
      val symbol = object : WebSymbol {
        override val origin: WebSymbolOrigin
          get() = Angular2SymbolOrigin.empty
        override val namespace: SymbolNamespace
          get() = WebSymbol.NAMESPACE_HTML
        override val kind: SymbolKind
          get() = WebSymbol.KIND_HTML_ATTRIBUTES

        override val name: String
          get() = "Fake symbol"

        override fun createPointer(): Pointer<out WebSymbol> =
          Pointer.hardPointer(this)
      }

      return Angular2AttributeDescriptor(WebSymbolHtmlAttributeInfo.create(attributeName, symbol), tag)
    }
  }

}