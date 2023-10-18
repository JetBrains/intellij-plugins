// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.*
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import org.jetbrains.astro.codeInsight.ASTRO_DEFINE_VARS_DIRECTIVE

class AstroStyleDefineVarsScope(styleTag: XmlTag)
  : WebSymbolsScopeWithCache<XmlTag, Unit>(null, styleTag.project, styleTag, Unit) {
  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    dataHolder.attributes
      .find { it.name == ASTRO_DEFINE_VARS_DIRECTIVE }
      ?.valueElement
      ?.childrenOfType<JSEmbeddedContent>()
      ?.firstOrNull()
      ?.childrenOfType<JSObjectLiteralExpression>()
      ?.firstOrNull()
      ?.let {
        it.asWebSymbol().getJSPropertySymbols().forEach(consumer)
        consumer(AstroDefinedCssPropertySymbol)
      }
  }

  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let { AstroStyleDefineVarsScope(it) } }
  }

  object AstroDefinedCssPropertySymbol : WebSymbol {
    override val namespace: SymbolNamespace
      get() = WebSymbol.NAMESPACE_CSS

    override val kind: SymbolKind
      get() = WebSymbol.KIND_CSS_PROPERTIES

    override val name: String
      get() = "Astro Defined CSS Property"

    override val pattern: WebSymbolsPattern =
      WebSymbolsPatternFactory.createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          WebSymbolsPatternReferenceResolver.Reference(
            qualifiedKind = WebSymbolQualifiedKind(WebSymbol.NAMESPACE_JS, WebSymbol.KIND_JS_PROPERTIES)),
          )
        ), false,
        WebSymbolsPatternFactory.createPatternSequence(
          WebSymbolsPatternFactory.createStringMatch("--"),
          WebSymbolsPatternFactory.createSymbolReferencePlaceholder()
        )
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId?
        get() = null
    }

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }
}
