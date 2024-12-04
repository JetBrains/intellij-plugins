// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.query.WebSymbolMatch
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueCompositionApp
import org.jetbrains.vuejs.model.source.VueSourceContainer
import org.jetbrains.vuejs.model.source.VueSourceEntityDescriptor
import org.jetbrains.vuejs.model.source.VueUnresolvedComponent
import org.jetbrains.vuejs.web.*

class VueComponentSymbol(name: String, component: VueComponent, private val vueProximity: VueModelVisitor.Proximity) :
  VueScopeElementSymbol<VueComponent>(name, component) {

  private val isCompositionComponent: Boolean = VueCompositionApp.isCompositionAppComponent(component)

  val sourceDescriptor: VueSourceEntityDescriptor?
    get() = (item as? VueSourceContainer)?.descriptor

  val typeParameters: List<TypeScriptTypeParameter>
    get() = (item as? VueRegularComponent)?.typeParameters ?: emptyList()

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = VUE_COMPONENTS

  // The source field is used for refactoring purposes by Web Symbols framework
  override val source: PsiElement?
    get() = (item as? VueRegularComponent)?.nameElement ?: item.rawSource

  override val priority: WebSymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun equals(other: Any?): Boolean =
    super.equals(other)
    && (other as VueComponentSymbol).vueProximity == vueProximity

  override fun hashCode(): Int =
    31 * super.hashCode() + vueProximity.hashCode()

  // Use actual item source field for navigation
  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    val source = item.source ?: return emptyList()
    val adjustedSources = JSDeclarationEvaluator.adjustDeclaration(source, null) ?: source
    return listOf(VueComponentSourceNavigationTarget(adjustedSources))
  }

  override val properties: Map<String, Any>
    get() = mapOf(Pair(PROP_VUE_PROXIMITY, vueProximity), Pair(
      PROP_VUE_COMPOSITION_COMPONENT, isCompositionComponent))

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.matches(WebSymbol.HTML_SLOTS) && item is VueUnresolvedComponent)
      listOf(WebSymbolMatch.create(qualifiedName.name, WebSymbol.HTML_SLOTS, this.origin,
                                   WebSymbolNameSegment.create(0, qualifiedName.name.length)))
    else
      super.getMatchingSymbols(qualifiedName, params, scope)

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    when (qualifiedKind) {
      VUE_COMPONENT_PROPS -> {
        val props = mutableListOf<VueInputProperty>()
        item.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
            props.add(prop)
            return true
          }
        })
        props.map { VueInputPropSymbol(it, item, this.origin) }
      }
      VUE_COMPONENT_DATA_PROPERTIES -> {
        val props = mutableListOf<VueDataProperty>()
        item.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
            props.add(dataProperty)
            return true
          }
        }, onlyPublic = false)
        props.map { VueDataPropertySymbol(it, item, this.origin) }
      }
      VUE_COMPONENT_COMPUTED_PROPERTIES -> {
        val props = mutableListOf<VueComputedProperty>()
        item.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
            props.add(computedProperty)
            return true
          }
        }, onlyPublic = false)
        props.map { VueComputedPropertySymbol(it, item, this.origin) }
      }
      WebSymbol.HTML_SLOTS -> {
        (item as? VueContainer)
          ?.slots
          ?.map { VueSlotSymbol(it, item, this.origin) }
        ?: emptyList()
      }
      VUE_MODEL -> {
        (item as? VueContainer)
          ?.collectModelDirectiveProperties()
          ?.takeIf { it.prop != null || it.event != null }
          ?.let { listOf(VueModelSymbol(this.origin, it)) }
        ?: emptyList()
      }
      WebSymbol.JS_EVENTS -> {
        (item as? VueContainer)
          ?.emits
          ?.map { VueEmitCallSymbol(it, item, this.origin) }
        ?: emptyList()
      }
      else -> emptyList()
    }

  override fun createPointer(): Pointer<VueComponentSymbol> {
    val component = item.createPointer()
    val name = this.name
    val vueProximity = this.vueProximity
    return Pointer {
      component.dereference()?.let { VueComponentSymbol(name, it, vueProximity) }
    }
  }

}