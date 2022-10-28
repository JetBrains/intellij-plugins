// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator

class VueInputPropSymbol(property: VueInputProperty,
                         owner: VueComponent,
                         origin: WebSymbolOrigin)
  : VueNamedWebSymbol<VueInputProperty>(property, owner, origin) {

  override val kind: SymbolKind
    get() = VueWebSymbolsQueryConfigurator.KIND_VUE_COMPONENT_PROPS

  override val type: JSType?
    get() = item.jsType

  override val required: Boolean
    get() = item.required

  override val attributeValue: WebSymbolHtmlAttributeValue =
    object : WebSymbolHtmlAttributeValue {
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