// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.model.source.VueCompositionContainer
import org.jetbrains.vuejs.web.VueCompositionComponentProperty
import org.jetbrains.vuejs.web.VUE_COMPONENTS

interface VueNamedComponent : VueComponent, VueSymbol {

  override val kind: PolySymbolKind
    get() = VUE_COMPONENTS

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.component", name))
      .icon(icon)
      .presentation()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  @PolySymbol.Property(VueCompositionComponentProperty::class)
  private val isCompositionComponent: Boolean
    get() = VueCompositionContainer.isCompositionAppComponent(this)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget>

  override fun createPointer(): Pointer<out VueNamedComponent>

}