// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTES

interface Angular2DirectiveAttribute : Angular2Symbol, Angular2Element {

  override val name: String

  val type: JSType?

  val required: Boolean? get() = null

  override val modifiers: Set<PolySymbolModifier>
    get() = when (required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
      null -> emptySet()
    }

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      else -> super.get(property)
    }

  override val project: Project
    get() = sourceElement.project

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = NG_DIRECTIVE_ATTRIBUTES

  override val apiStatus: PolySymbolApiStatus

  override fun createPointer(): Pointer<out Angular2DirectiveAttribute>
}
