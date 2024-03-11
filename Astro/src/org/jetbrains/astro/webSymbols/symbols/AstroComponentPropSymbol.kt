// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.javascript.webSymbols.jsType
import com.intellij.javascript.webSymbols.symbols.JSPropertySymbol
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.webSymbols.*
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import org.jetbrains.astro.webSymbols.ASTRO_COMPONENT_PROPS

class AstroComponentPropSymbol(private val propertySymbol: JSPropertySymbol)
  : PsiSourcedWebSymbol {
  override val type: JSType?
    get() = propertySymbol.jsType

  override val origin: WebSymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = ASTRO_COMPONENT_PROPS.namespace

  override val kind: SymbolKind
    get() = ASTRO_COMPONENT_PROPS.kind

  override val name: String
    get() = propertySymbol.name

  override val source: PsiElement?
    get() = propertySymbol.source

  override val attributeValue: WebSymbolHtmlAttributeValue
    get() = WebSymbolHtmlAttributeValue.create(kind = WebSymbolHtmlAttributeValue.Kind.PLAIN)

  override val required: Boolean
    get() = !(propertySymbol.psiContext.asSafely<PropertySignature>()?.isOptional ?: false)

  override fun createPointer(): Pointer<out PsiSourcedWebSymbol> {
    val sourcePtr = propertySymbol.createPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroComponentPropSymbol(propertySymbol) }
    }
  }
}