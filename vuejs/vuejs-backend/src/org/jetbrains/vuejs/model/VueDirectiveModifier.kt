// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolKind
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS

interface VueDirectiveModifier : VueNamedSymbol {

  override val kind: PolySymbolKind
    get() = VUE_DIRECTIVE_MODIFIERS

  fun withProximity(proximity: VueModelVisitor.Proximity?): VueDirectiveModifier

  abstract override fun createPointer(): Pointer<out VueDirectiveModifier>
}
