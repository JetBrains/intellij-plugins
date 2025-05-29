// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.*
import com.intellij.webSymbols.PolySymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.utils.qualifiedKind
import org.jetbrains.astro.codeInsight.ASTRO_DEFINE_VARS_DIRECTIVE

abstract class AstroDefineVarsScope(tag: XmlTag)
  : WebSymbolsScopeWithCache<XmlTag, Unit>(null, tag.project, tag, Unit) {

  protected abstract val providedSymbol: PolySymbol

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == providedSymbol.qualifiedKind
    || qualifiedKind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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
        providedSymbol.let(consumer)
      }
  }
}

class AstroScriptDefineVarsScope(scriptTag: XmlTag) : AstroDefineVarsScope(scriptTag) {
  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroScriptDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = object : PolySymbol {
    override val namespace: SymbolNamespace
      get() = PolySymbol.NAMESPACE_JS

    override val kind: SymbolKind
      get() = PolySymbol.KIND_JS_SYMBOLS

    override val name: String
      get() = "Astro Defined Script Variable"

    override val pattern: WebSymbolsPattern =
      WebSymbolsPatternFactory.createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          WebSymbolsPatternReferenceResolver.Reference(
            qualifiedKind = PolySymbolQualifiedKind(PolySymbol.NAMESPACE_JS, PolySymbol.KIND_JS_PROPERTIES)),
        )
        ),
        false,
        WebSymbolsPatternFactory.createPatternSequence(WebSymbolsPatternFactory.createSymbolReferencePlaceholder())
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId?
        get() = null
    }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }
}

class AstroStyleDefineVarsScope(styleTag: XmlTag) : AstroDefineVarsScope(styleTag) {
  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroStyleDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = object : PolySymbol {
    override val namespace: SymbolNamespace
      get() = PolySymbol.NAMESPACE_CSS

    override val kind: SymbolKind
      get() = PolySymbol.KIND_CSS_PROPERTIES

    override val name: String
      get() = "Astro Defined CSS Variable"

    override val pattern: WebSymbolsPattern =
      WebSymbolsPatternFactory.createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          WebSymbolsPatternReferenceResolver.Reference(
            qualifiedKind = PolySymbolQualifiedKind(PolySymbol.NAMESPACE_JS, PolySymbol.KIND_JS_PROPERTIES)),
        )
        ),
        false,
        WebSymbolsPatternFactory.createPatternSequence(
          WebSymbolsPatternFactory.createStringMatch("--"),
          WebSymbolsPatternFactory.createSymbolReferencePlaceholder()
        )
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId?
        get() = null
    }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }
}
