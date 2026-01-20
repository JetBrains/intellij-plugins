// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.model.source.VueCompositionContainer
import org.jetbrains.vuejs.web.*
import org.jetbrains.vuejs.web.symbols.VueScopeElementSymbol

interface VueComponent : VueInstanceOwner, VueScopeElementSymbol,
                         PolySymbolScope, PsiSourcedPolySymbol /* to be removed */ {

  val defaultName: String?
    get() = name

  @Suppress("DEPRECATION")
  val rawSource: PsiElement?
    get() = componentSource

  val componentSource: PsiElement?

  override val kind: PolySymbolKind
    get() = VUE_COMPONENTS

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.component", name))
      .icon(icon)
      .presentation()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  val mode: VueMode
    get() = VueMode.CLASSIC

  val vueProximity: VueModelVisitor.Proximity?

  fun withNameAndProximity(name: String, proximity: VueModelVisitor.Proximity): VueComponent

  val typeParameters: List<TypeScriptTypeParameter>
    get() = emptyList()

  override val source: PsiElement?
    get() = rawSource

  override val priority: PolySymbol.Priority?
    get() = vueProximity?.asPolySymbolPriority()

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when (kind) {
      VUE_COMPONENT_PROPS -> {
        val props = mutableListOf<VueInputProperty>()
        acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
            props.add(prop)
            return true
          }
        })
        props
      }
      VUE_COMPONENT_DATA_PROPERTIES -> {
        val props = mutableListOf<VueDataProperty>()
        acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
            props.add(dataProperty)
            return true
          }
        }, onlyPublic = false)
        props
      }
      VUE_COMPONENT_COMPUTED_PROPERTIES -> {
        val props = mutableListOf<VueComputedProperty>()
        acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
            props.add(computedProperty)
            return true
          }
        }, onlyPublic = false)
        props
      }
      else -> emptyList()
    }

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_VUE_PROXIMITY -> vueProximity as T
      PROP_VUE_COMPOSITION_COMPONENT -> VueCompositionContainer.isCompositionAppComponent(this) as T
      else -> null
    }

  override fun createPointer(): Pointer<out VueComponent>
}