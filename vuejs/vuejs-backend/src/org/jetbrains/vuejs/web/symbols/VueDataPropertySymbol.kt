// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_COMPONENT_DATA_PROPERTIES

class VueDataPropertySymbol(
  data: VueDataProperty,
  owner: VueComponent,
) : VuePropertySymbol<VueDataProperty>(data, owner) {

  override val kind: PolySymbolKind
    get() = VUE_COMPONENT_DATA_PROPERTIES

  override val type: JSType?
    get() = item.jsType

  override fun createPointer(): Pointer<VueDataPropertySymbol> =
    object : NamedSymbolPointer<VueDataProperty, VueDataPropertySymbol>(this) {

      override fun locateSymbol(owner: VueComponent): VueDataProperty? {
        var result: VueDataProperty? = null
        // TODO ambiguous resolution in case of duplicated names
        owner.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
            if (dataProperty.name == name) {
              result = dataProperty
            }
            return result == null
          }
        })
        return result
      }

      override fun createWrapper(owner: VueComponent, symbol: VueDataProperty): VueDataPropertySymbol =
        VueDataPropertySymbol(symbol, owner)

    }
}