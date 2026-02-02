// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import org.jetbrains.vuejs.web.*
import org.jetbrains.vuejs.web.symbols.VueFilterWithProximity

interface VueEntitiesContainer : VueScopeElement, PolySymbolScope, VueInstanceOwner {
  override fun createPointer(): Pointer<out VueEntitiesContainer>

  val components: Map<String, VueNamedComponent>
  val directives: Map<String, VueDirective>
  val filters: Map<String, VueFilter>
  val mixins: List<VueMixin>

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
      VUE_MODEL -> {
        collectModelDirectiveProperties()
          .takeIf { it.prop != null || it.event != null }
          ?.let { listOf(it) }
        ?: emptyList()
      }
      VUE_FILTERS -> {
        val filters = mutableListOf<VueFilter>()
        acceptEntities(object : VueModelVisitor() {
          override fun visitFilter(filter: VueFilter, proximity: Proximity): Boolean {
            filters.add(VueFilterWithProximity.create(filter, proximity))
            return true
          }
        })
        filters
      }
      else -> emptyList()
    }

  override fun getModificationCount(): Long = -1

}
