// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.html.HtmlAttributeValueProperty
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.symbols.JSPropertySymbol
import com.intellij.polySymbols.js.types.JSTypeProperty
import com.intellij.polySymbols.search.PsiLinkedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENT_PROPS

class AstroComponentPropSymbol(private val propertySymbol: JSPropertySymbol) : PsiLinkedPolySymbol, AstroSymbol {

  override val kind: PolySymbolKind
    get() = ASTRO_COMPONENT_PROPS

  override val name: String
    get() = propertySymbol.name

  override val linkedElement: PsiElement?
    get() = propertySymbol.linkedElement

  @PolySymbol.Property(JSTypeProperty::class)
  private val type: JSType?
    get() = propertySymbol.type

  @PolySymbol.Property(HtmlAttributeValueProperty::class)
  private val htmlAttributeValue: PolySymbolHtmlAttributeValue?
    get() = PolySymbolHtmlAttributeValue.create(kind = PolySymbolHtmlAttributeValue.Kind.PLAIN)

  override val modifiers: Set<PolySymbolModifier>
    get() = setOf(
      if (required) PolySymbolModifier.REQUIRED else PolySymbolModifier.OPTIONAL,
    )

  private val required: Boolean
    get() = !(propertySymbol.psiContext.asSafely<PropertySignature>()?.isOptional ?: false)

  override fun equals(other: Any?): Boolean =
    other === this
    || other is AstroComponentPropSymbol
    && other.propertySymbol == propertySymbol

  override fun hashCode(): Int =
    propertySymbol.hashCode()

  override fun createPointer(): Pointer<out PsiLinkedPolySymbol> {
    val sourcePtr = propertySymbol.createPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroComponentPropSymbol(propertySymbol) }
    }
  }
}