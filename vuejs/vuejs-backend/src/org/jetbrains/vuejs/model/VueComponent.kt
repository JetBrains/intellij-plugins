// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.web.VUE_COMPONENT_COMPUTED_PROPERTIES
import org.jetbrains.vuejs.web.VUE_COMPONENT_DATA_PROPERTIES
import org.jetbrains.vuejs.web.VUE_COMPONENT_PROPS
import org.jetbrains.vuejs.web.VUE_MODEL

interface VueComponent : VueContainer, PolySymbolScope, VueMixin {

  @Suppress("DEPRECATION")
  val rawSource: PsiElement?
    get() = componentSource

  val componentSource: PsiElement?

  val mode: VueMode
    get() = VueMode.CLASSIC

  val typeParameters: List<TypeScriptTypeParameter>

  fun getNavigationTargets(project: Project): Collection<NavigationTarget>

  override val source: PsiElement?
    get() = rawSource

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
      HTML_SLOTS -> slots
      JS_EVENTS -> emits
      VUE_MODEL -> {
        collectModelDirectiveProperties()
          .takeIf { it.prop != null || it.event != null }
          ?.let { listOf(it) }
        ?: emptyList()
      }
      else -> emptyList()
    }

  override fun getModificationCount(): Long = -1

  override fun createPointer(): Pointer<out VueComponent>
}