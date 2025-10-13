// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_COMPONENT_PROPS

class VueInputPropSymbol(
  property: VueInputProperty,
  owner: VueComponent,
  origin: PolySymbolOrigin,
) : VuePropertySymbol<VueInputProperty>(property, owner, origin) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_COMPONENT_PROPS

  override val type: JSType?
    get() = item.jsType

  override val modifiers: Set<PolySymbolModifier>
    get() = when (item.required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
    }

  override val attributeValue: PolySymbolHtmlAttributeValue =
    object : PolySymbolHtmlAttributeValue {
      override val default: String?
        get() = item.defaultValue
    }

  override fun createPointer(): Pointer<VueInputPropSymbol> =
    object : NamedSymbolPointer<VueInputProperty, VueInputPropSymbol>(this) {

      override fun locateSymbol(owner: VueComponent): VueInputProperty? {
        var result: VueInputProperty? = null
        // TODO ambiguous resolution in case of duplicated names
        owner.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
            if (prop.name == name) {
              result = prop
            }
            return result == null
          }
        })
        return result
      }

      override fun createWrapper(owner: VueComponent, symbol: VueInputProperty): VueInputPropSymbol =
        VueInputPropSymbol(symbol, owner, origin)

    }
}