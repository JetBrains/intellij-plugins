// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainer
import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolNamespace
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider

class VueModelSymbol(override val origin: WebSymbolsContainer.Origin,
                     private val vueModel: VueModelDirectiveProperties) : WebSymbol {

  override val namespace: SymbolNamespace get() = WebSymbolsContainer.NAMESPACE_HTML
  override val kind: SymbolKind get() = VueWebSymbolsAdditionalContextProvider.KIND_VUE_MODEL

  override val properties: Map<String, Any>
    get() {
      val map = mutableMapOf<String, Any>()
      vueModel.prop?.let { map[VueWebSymbolsAdditionalContextProvider.PROP_VUE_MODEL_PROP] = it }
      vueModel.event?.let { map[VueWebSymbolsAdditionalContextProvider.PROP_VUE_MODEL_EVENT] = it }
      return map
    }

  override fun createPointer(): Pointer<VueModelSymbol> =
    Pointer.hardPointer(this)
}