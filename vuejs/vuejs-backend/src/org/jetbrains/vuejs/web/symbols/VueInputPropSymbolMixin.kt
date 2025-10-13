// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.web.VUE_COMPONENT_PROPS

interface VueInputPropSymbolMixin : VuePropertySymbolMixin, VueInputProperty {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_COMPONENT_PROPS

  override val type: JSType?
    get() = jsType

  override val modifiers: Set<PolySymbolModifier>
    get() = when (required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
    }

  override val attributeValue: PolySymbolHtmlAttributeValue
    get() =
      object : PolySymbolHtmlAttributeValue {
        override val default: String?
          get() = defaultValue
      }

}