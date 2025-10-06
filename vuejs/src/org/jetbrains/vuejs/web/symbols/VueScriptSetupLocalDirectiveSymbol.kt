// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolQualifiedKind
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.vuejs.web.VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

class VueScriptSetupLocalDirectiveSymbol(
  directive: VueScriptSetupLocalDirective,
  override val vueProximity: VueModelVisitor.Proximity,
) : VueDirectiveSymbolBase<VueScriptSetupLocalDirective>(
  name = directive.defaultName,
  directive = directive,
) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

  override fun createPointer(): Pointer<VueScriptSetupLocalDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueScriptSetupLocalDirectiveSymbol(it, vueProximity) }
    }
  }
}