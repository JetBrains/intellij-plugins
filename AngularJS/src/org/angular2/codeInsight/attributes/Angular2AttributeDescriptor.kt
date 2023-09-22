// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.angular2.Angular2Framework
import org.angular2.codeInsight.config.isStrictTemplates
import org.angular2.entities.Angular2Directive
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.web.Angular2DescriptorSymbolsProvider
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
import javax.swing.Icon

class Angular2AttributeDescriptor(info: WebSymbolHtmlAttributeInfo, tag: XmlTag?)
  : WebSymbolAttributeDescriptor(info, tag) {

  val sourceDirectives: List<Angular2Directive> get() = bindingInfoProvider.directives

  @get:JvmName("hasErrorSymbols")
  val hasErrorSymbols: Boolean
    get() = bindingInfoProvider.errorSymbols.isNotEmpty()

  @get:JvmName("hasNonDirectiveSymbols")
  val hasNonDirectiveSymbols: Boolean
    get() = bindingInfoProvider.nonDirectiveSymbols.isNotEmpty()

  val info: Angular2AttributeNameParser.AttributeInfo = Angular2AttributeNameParser.parse(name, tag)

  override fun validateValue(context: XmlElement?, value: String?): String? {
    val isOneTimeBinding = symbol.unwrapMatchedSymbols()
      .any { it.kind == Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS }
    if (value != null && context != null && isOneTimeBinding && isStrictTemplates(context)) {
      val valueType = JSStringLiteralTypeImpl(value, false, JSTypeSource.EXPLICITLY_DECLARED)
      val symbolType = symbol.jsType
      JSTypeChecker.getErrorMessageIfTypeNotAssignableToType(context, symbolType, valueType, Angular2Language.INSTANCE.optionHolder,
                                                             "javascript.type.is.not.assignable.to.type")
        ?.let { return it }
    }
    return super.validateValue(context, value)
  }

  private val bindingInfoProvider by lazy(LazyThreadSafetyMode.PUBLICATION) { Angular2DescriptorSymbolsProvider(this.symbol) }

  companion object {
    @JvmStatic
    @Deprecated(message = "Deprecated, returns fake descriptor. Use web-types or Web Symbols instead")
    fun create(tag: XmlTag,
               attributeName: String,
               @Suppress("UNUSED_PARAMETER")
               element: PsiElement): Angular2AttributeDescriptor {
      return Angular2AttributeDescriptor(object : WebSymbolHtmlAttributeInfo {
        override val name: String
          get() = attributeName
        override val symbol: WebSymbol
          get() = object : WebSymbol {
            override val origin: WebSymbolOrigin
              get() = WebSymbolOrigin.create(Angular2Framework.ID)
            override val namespace: SymbolNamespace
              get() = WebSymbol.NAMESPACE_HTML
            override val kind: SymbolKind
              get() = WebSymbol.KIND_HTML_ATTRIBUTES

            override val name: String
              get() = "Fake symbol"

            override fun createPointer(): Pointer<out WebSymbol> =
              Pointer.hardPointer(this)
          }
        override val acceptsNoValue: Boolean
          get() = false
        override val acceptsValue: Boolean
          get() = true
        override val enumValues: List<WebSymbolCodeCompletionItem>?
          get() = null
        override val strictEnumValues: Boolean
          get() = false
        override val type: Any?
          get() = null
        override val icon: Icon?
          get() = null
        override val required: Boolean
          get() = false
        override val defaultValue: String?
          get() = null
        override val priority: WebSymbol.Priority
          get() = WebSymbol.Priority.NORMAL
      }, tag)
    }
  }

}