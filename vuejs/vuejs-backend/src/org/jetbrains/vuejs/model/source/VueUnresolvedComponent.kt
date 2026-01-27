// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
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
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.model.*

class VueUnresolvedComponent(
  override val rawSource: PsiElement?,
) : VueComponent {

  override val componentSource: PsiElement? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    (rawSource as? ES6ImportSpecifier)?.resolveIfImportSpecifier() ?: rawSource
  }

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = emptyList()

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    emptyList()

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val thisType: JSType
    get() = getDefaultVueComponentInstanceType(rawSource) ?: JSAnyType.get(rawSource)

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
    val source = this.rawSource?.createSmartPointer()
    return Pointer {
      val newSource = source?.let { it.dereference() ?: return@Pointer null }
      VueUnresolvedComponent(newSource)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueUnresolvedComponent
    && other.rawSource == rawSource

  override fun hashCode(): Int =
    rawSource.hashCode()

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
    get() = emptyList()
  override val provides: List<VueProvide>
    get() = emptyList()
  override val injects: List<VueInject>
    get() = emptyList()
  override val extends: List<VueContainer>
    get() = emptyList()
  override val model: VueModelDirectiveProperties?
    get() = null
}
