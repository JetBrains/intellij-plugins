// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_COMPONENT_COMPUTED_PROPERTIES

class VueComputedPropertySymbol(
  data: VueComputedProperty,
  owner: VueComponent,
  origin: PolySymbolOrigin,
) : VuePropertySymbol<VueComputedProperty>(data, owner, origin) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_COMPONENT_COMPUTED_PROPERTIES

  override val type: JSType?
    get() = item.jsType

  override fun createPointer(): Pointer<VueComputedPropertySymbol> =
    object : NamedSymbolPointer<VueComputedProperty, VueComputedPropertySymbol>(this) {

      override fun locateSymbol(owner: VueComponent): VueComputedProperty? {
        var result: VueComputedProperty? = null
        // TODO ambiguous resolution in case of duplicated names
        owner.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
            if (computedProperty.name == name) {
              result = computedProperty
            }
            return result == null
          }
        })
        return result
      }

      override fun createWrapper(owner: VueComponent, symbol: VueComputedProperty): VueComputedPropertySymbol =
        VueComputedPropertySymbol(symbol, owner, origin)

    }
}