// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolNameSegment
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.query.PolySymbolMatch
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.symbols.VueAnySlot

class VueUnresolvedComponent(
  override val source: PsiElement?,
) : VueComponent {

  override val elementToImport: PsiElement?
    get() = null

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = emptyList()

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    emptyList()

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val thisType: JSType
    get() = getDefaultVueComponentInstanceType(source) ?: JSAnyType.get(source)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.matches(HTML_SLOTS))
      listOf(PolySymbolMatch.create(qualifiedName.name, HTML_SLOTS, PolySymbolNameSegment.create(0, qualifiedName.name.length)))
    else
      super.getMatchingSymbols(qualifiedName, params, stack)

  override fun createPointer(): Pointer<VueUnresolvedComponent> {
    val source = this.source?.createSmartPointer()
    return Pointer {
      val newSource = source?.let { it.dereference() ?: return@Pointer null }
      VueUnresolvedComponent(newSource)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueUnresolvedComponent
    && other.source == source

  override fun hashCode(): Int =
    source.hashCode()

  override val components: Map<String, VueNamedComponent>
    get() = emptyMap()
  override val directives: Map<String, VueDirective>
    get() = emptyMap()
  override val filters: Map<String, VueFilter>
    get() = emptyMap()
  override val mixins: List<VueMixin>
    get() = emptyList()
  override val data: List<VueDataProperty>
    get() = emptyList()
  override val computed: List<VueComputedProperty>
    get() = emptyList()
  override val methods: List<VueMethod>
    get() = emptyList()
  override val props: List<VueInputProperty>
    get() = emptyList()
  override val emits: List<VueEmitCall>
    get() = emptyList()
  override val slots: List<VueSlot>
    get() = listOf(VueAnySlot)
  override val provides: List<VueProvide>
    get() = emptyList()
  override val injects: List<VueInject>
    get() = emptyList()
  override val extends: List<VueContainer>
    get() = emptyList()
  override val model: VueModelDirectiveProperties?
    get() = null
}
