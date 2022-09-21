// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.utils.match
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueCompositionApp
import org.jetbrains.vuejs.model.source.VueUnresolvedComponent
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider
import org.jetbrains.vuejs.web.asWebSymbolPriority

class VueComponentSymbol(matchedName: String, component: VueComponent, private val vueProximity: VueModelVisitor.Proximity) :
  VueScopeElementSymbol<VueComponent>(matchedName, component) {

  private val isCompositionComponent: Boolean = VueCompositionApp.isCompositionAppComponent(component)

  override val kind: SymbolKind
    get() = VueWebSymbolsAdditionalContextProvider.KIND_VUE_COMPONENTS

  override val name: String
    get() = matchedName

  // The source field is used for refactoring purposes by Web Symbols framework
  override val source: PsiElement?
    get() = (item as? VueRegularComponent)?.nameElement ?: item.source

  override val priority: WebSymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun equals(other: Any?): Boolean =
    super.equals(other)
    && (other as VueComponentSymbol).vueProximity == vueProximity

  override fun hashCode(): Int =
    31 * super.hashCode() + vueProximity.hashCode()

  // Use actual item source field for navigation
  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    item.source?.let { listOf(VueComponentSourceNavigationTarget(it)) } ?: emptyList()

  override val properties: Map<String, Any>
    get() = mapOf(Pair(VueWebSymbolsAdditionalContextProvider.PROP_VUE_PROXIMITY, vueProximity), Pair(
      VueWebSymbolsAdditionalContextProvider.PROP_VUE_COMPOSITION_COMPONENT, isCompositionComponent))

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: String,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == null || namespace == WebSymbolsContainer.Namespace.HTML)
      when (kind) {
        VueWebSymbolsAdditionalContextProvider.KIND_VUE_COMPONENT_PROPS -> {
          val props = mutableListOf<VueInputProperty>()
          // TODO ambiguous resolution in case of duplicated names
          item.acceptPropertiesAndMethods(object : VueModelVisitor() {
            override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
              props.add(prop)
              return true
            }
          })
          props.mapWithNameFilter(name, params, context) { VueInputPropSymbol(it, item, this.origin) }
        }
        WebSymbol.KIND_HTML_SLOTS -> {
          (item as? VueContainer)
            ?.slots
            ?.mapWithNameFilter(name, params, context) { VueSlotSymbol(it, item, this.origin) }
          ?: if (!name.isNullOrEmpty()
                 && ((item is VueContainer && item.template == null)
                     || item is VueUnresolvedComponent)) {
            listOf(WebSymbolMatch.create(name, listOf(WebSymbol.NameSegment(0, name.length)), WebSymbolsContainer.Namespace.HTML,
                                         WebSymbol.KIND_HTML_SLOTS, this.origin))
          }
          else emptyList()
        }
        VueWebSymbolsAdditionalContextProvider.KIND_VUE_MODEL -> {
          (item as? VueContainer)
            ?.collectModelDirectiveProperties()
            ?.takeIf { it.prop != null || it.event != null }
            ?.let { listOf(VueModelSymbol(this.origin, it)) }
          ?: emptyList()
        }
        else -> emptyList()
      }
    else if (namespace == WebSymbolsContainer.Namespace.JS && kind == WebSymbol.KIND_JS_EVENTS) {
      (item as? VueContainer)
        ?.emits
        ?.mapWithNameFilter(name, params, context) { VueEmitCallSymbol(it, item, this.origin) }
      ?: emptyList()
    }
    else emptyList()

  override fun createPointer(): Pointer<VueComponentSymbol> {
    val component = item.createPointer()
    val matchedName = this.matchedName
    val vueProximity = this.vueProximity
    return Pointer {
      component.dereference()?.let { VueComponentSymbol(matchedName, it, vueProximity) }
    }
  }


  private fun <T> List<T>.mapWithNameFilter(name: String?,
                                            params: WebSymbolsNameMatchQueryParams,
                                            context: Stack<WebSymbolsContainer>,
                                            mapper: (T) -> WebSymbol): List<WebSymbol> =
    if (name != null) {
      asSequence()
        .map(mapper)
        .flatMap { it.match(name, context, params) }
        .toList()
    }
    else this.map(mapper)
}