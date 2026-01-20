// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.web.VUE_FILTERS
import org.jetbrains.vuejs.web.symbols.VueScopeElementSymbol

interface VueFilter : VueScopeElementSymbol {
  override val source: PsiElement

  override val kind: PolySymbolKind
    get() = VUE_FILTERS

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.component.method", name))
      .icon(icon)
      .presentation()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

}
