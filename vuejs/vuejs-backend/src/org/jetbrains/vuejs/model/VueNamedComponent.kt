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
import org.jetbrains.vuejs.web.PROP_VUE_COMPOSITION_COMPONENT
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.VUE_COMPONENTS
import org.jetbrains.vuejs.web.asPolySymbolPriority

interface VueNamedComponent : VueComponent, VueSymbol {

  override val kind: PolySymbolKind
    get() = VUE_COMPONENTS

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.component", name))
      .icon(icon)
      .presentation()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  override val priority: PolySymbol.Priority?
    get() = vueProximity?.asPolySymbolPriority()

  val delegate: VueComponent?

  val vueProximity: VueModelVisitor.Proximity?

  fun withProximity(proximity: VueModelVisitor.Proximity): VueNamedComponent

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_VUE_PROXIMITY -> vueProximity as T
      PROP_VUE_COMPOSITION_COMPONENT -> VueCompositionContainer.isCompositionAppComponent(this) as T
      else -> null
    }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget>

  override fun createPointer(): Pointer<out VueNamedComponent>

}