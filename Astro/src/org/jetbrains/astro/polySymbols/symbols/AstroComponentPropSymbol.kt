// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.javascript.polySymbols.jsType
import com.intellij.javascript.polySymbols.symbols.JSPropertySymbol
import com.intellij.javascript.polySymbols.types.PROP_JS_TYPE
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENT_PROPS

class AstroComponentPropSymbol(private val propertySymbol: JSPropertySymbol)
  : PsiSourcedPolySymbol {

  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = ASTRO_COMPONENT_PROPS

  override val name: String
    get() = propertySymbol.name

  override val source: PsiElement?
    get() = propertySymbol.source

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(propertySymbol.jsType)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(PolySymbolHtmlAttributeValue.create(kind = PolySymbolHtmlAttributeValue.Kind.PLAIN))
      else -> null
    }

  override val modifiers: Set<PolySymbolModifier>
    get() = setOf(
      if (required) PolySymbolModifier.REQUIRED else PolySymbolModifier.OPTIONAL,
    )

  private val required: Boolean
    get() = !(propertySymbol.psiContext.asSafely<PropertySignature>()?.isOptional ?: false)

  override fun createPointer(): Pointer<out PsiSourcedPolySymbol> {
    val sourcePtr = propertySymbol.createPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroComponentPropSymbol(propertySymbol) }
    }
  }
}