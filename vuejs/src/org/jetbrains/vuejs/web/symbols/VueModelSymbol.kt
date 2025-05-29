// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.PolySymbol
import com.intellij.webSymbols.PolySymbolOrigin
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.web.PROP_VUE_MODEL_EVENT
import org.jetbrains.vuejs.web.PROP_VUE_MODEL_PROP
import org.jetbrains.vuejs.web.VUE_MODEL

class VueModelSymbol(
  override val origin: PolySymbolOrigin,
  private val vueModel: VueModelDirectiveProperties,
) : PolySymbol {

  override val name: String
    get() = "Vue Model"
  override val namespace: SymbolNamespace get() = PolySymbol.NAMESPACE_HTML
  override val kind: SymbolKind get() = VUE_MODEL.kind

  override val properties: Map<String, Any>
    get() {
      val map = mutableMapOf<String, Any>()
      vueModel.prop?.let { map[PROP_VUE_MODEL_PROP] = it }
      vueModel.event?.let { map[PROP_VUE_MODEL_EVENT] = it }
      return map
    }

  override fun createPointer(): Pointer<VueModelSymbol> =
    Pointer.hardPointer(this)
}