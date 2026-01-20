// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS

interface VueDirectiveModifier : VueSymbol {

  override val kind: PolySymbolKind
    get() = VUE_DIRECTIVE_MODIFIERS

  fun withProximity(proximity: VueModelVisitor.Proximity?): VueDirectiveModifier

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.directive.modifier", name))
      .icon(icon)
      .presentation()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  abstract override fun createPointer(): Pointer<out VueDirectiveModifier>
}
