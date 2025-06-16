// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolInfo
import com.intellij.model.Pointer
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.angular2.entities.Angular2Directive
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute
import org.angular2.web.Angular2DescriptorSymbolsProvider
import org.angular2.web.Angular2SymbolOrigin

class Angular2AttributeDescriptor(info: HtmlAttributeSymbolInfo, tag: XmlTag?)
  : HtmlAttributeSymbolDescriptor(info, tag) {

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
    @Deprecated(message = "Deprecated, returns fake descriptor. Use web-types or Poly Symbols instead")
    fun create(
      tag: XmlTag,
      attributeName: String,
      @Suppress("UNUSED_PARAMETER")
      element: PsiElement,
    ): Angular2AttributeDescriptor {
      val symbol = object : PolySymbol {
        override val origin: PolySymbolOrigin
          get() = Angular2SymbolOrigin.empty

        override val qualifiedKind: PolySymbolQualifiedKind
          get() = HTML_ATTRIBUTES

        override val name: String
          get() = "Fake symbol"

        override fun createPointer(): Pointer<out PolySymbol> =
          Pointer.hardPointer(this)
      }

      return Angular2AttributeDescriptor(HtmlAttributeSymbolInfo.create(attributeName, symbol), tag)
    }
  }

}