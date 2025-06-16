// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.javascript.polySymbols.symbols.asPolySymbol
import com.intellij.javascript.polySymbols.symbols.getJSPropertySymbols
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.FrameworkId
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.css.CSS_PROPERTIES
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.codeInsight.ASTRO_DEFINE_VARS_DIRECTIVE

abstract class AstroDefineVarsScope(tag: XmlTag)
  : PolySymbolScopeWithCache<XmlTag, Unit>(null, tag.project, tag, Unit) {

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
        it.asPolySymbol().getJSPropertySymbols().forEach(consumer)
        providedSymbol.let(consumer)
      }
  }
}

class AstroScriptDefineVarsScope(scriptTag: XmlTag) : AstroDefineVarsScope(scriptTag) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroScriptDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = object : PolySymbolWithPattern {

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = JS_SYMBOLS

    override val name: String
      get() = "Astro Defined Script Variable"

    override val pattern: PolySymbolPattern =
      PolySymbolPatternFactory.createComplexPattern(
        ComplexPatternOptions(symbolsResolver = PolySymbolPatternReferenceResolver(
          PolySymbolPatternReferenceResolver.Reference(qualifiedKind = JS_PROPERTIES),
        )
        ),
        false,
        PolySymbolPatternFactory.createPatternSequence(PolySymbolPatternFactory.createSymbolReferencePlaceholder())
      )

    override val origin: PolySymbolOrigin = object : PolySymbolOrigin {
      override val framework: FrameworkId?
        get() = null
    }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }
}

class AstroStyleDefineVarsScope(styleTag: XmlTag) : AstroDefineVarsScope(styleTag) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroStyleDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = object : PolySymbolWithPattern {

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = CSS_PROPERTIES

    override val name: String
      get() = "Astro Defined CSS Variable"

    override val pattern: PolySymbolPattern =
      PolySymbolPatternFactory.createComplexPattern(
        ComplexPatternOptions(symbolsResolver = PolySymbolPatternReferenceResolver(
          PolySymbolPatternReferenceResolver.Reference(qualifiedKind = JS_PROPERTIES),
        )),
        false,
        PolySymbolPatternFactory.createPatternSequence(
          PolySymbolPatternFactory.createStringMatch("--"),
          PolySymbolPatternFactory.createSymbolReferencePlaceholder()
        )
      )

    override val origin: PolySymbolOrigin = object : PolySymbolOrigin {
      override val framework: FrameworkId?
        get() = null
    }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }
}
