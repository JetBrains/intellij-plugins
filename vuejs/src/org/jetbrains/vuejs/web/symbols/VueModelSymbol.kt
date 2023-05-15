// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator

class VueModelSymbol(override val origin: WebSymbolOrigin,
                     private val vueModel: VueModelDirectiveProperties) : WebSymbol {

  override val name: String
    get() = "Vue Model"
  override val namespace: SymbolNamespace get() = WebSymbol.NAMESPACE_HTML
  override val kind: SymbolKind get() = VueWebSymbolsQueryConfigurator.KIND_VUE_MODEL

  override val properties: Map<String, Any>
    get() {
      val map = mutableMapOf<String, Any>()
      vueModel.prop?.let { map[VueWebSymbolsQueryConfigurator.PROP_VUE_MODEL_PROP] = it }
      vueModel.event?.let { map[VueWebSymbolsQueryConfigurator.PROP_VUE_MODEL_EVENT] = it }
      return map
    }

  override fun createPointer(): Pointer<VueModelSymbol> =
    Pointer.hardPointer(this)
}