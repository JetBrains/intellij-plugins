// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.web.PROP_VUE_MODEL_EVENT
import org.jetbrains.vuejs.web.PROP_VUE_MODEL_PROP
import org.jetbrains.vuejs.web.VUE_MODEL

class VueModelSymbol(
  private val vueModel: VueModelDirectiveProperties,
) : VueSymbol {

  override val name: String
    get() = "Vue Model"

  override val kind: PolySymbolKind
    get() = VUE_MODEL

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_VUE_MODEL_PROP -> vueModel.prop as T?
      PROP_VUE_MODEL_EVENT -> vueModel.event as T?
      else -> null
    }

  override fun createPointer(): Pointer<VueModelSymbol> =
    Pointer.hardPointer(this)
}