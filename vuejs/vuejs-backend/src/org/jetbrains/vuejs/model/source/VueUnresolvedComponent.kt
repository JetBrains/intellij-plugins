// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
import com.intellij.lang.javascript.psi.JSType
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
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.getDefaultVueComponentInstanceType
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget

class VueUnresolvedComponent(
  private val context: PsiElement,
  override val rawSource: PsiElement?,
  override val defaultName: String?,
  override val vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.OUT_OF_SCOPE,
) : VueComponent {

  override val name: String = defaultName ?: "<unnamed>"

  override val componentSource: PsiElement? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    (rawSource as? ES6ImportSpecifier)?.resolveIfImportSpecifier() ?: rawSource
  }

  override fun withNameAndProximity(
    name: String,
    proximity: VueModelVisitor.Proximity,
  ): VueComponent =
    VueUnresolvedComponent(context, rawSource, name, proximity)

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val thisType: JSType
    get() = getDefaultVueComponentInstanceType(context) ?: JSAnyType.get(context)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    val source = componentSource ?: return emptyList()
    val adjustedSources = JSDeclarationEvaluator.adjustDeclaration(source, null) ?: source
    return listOf(VueComponentSourceNavigationTarget(adjustedSources))
  }

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
    val context = this.context.createSmartPointer()
    val source = this.componentSource?.createSmartPointer()
    val defaultName = this.defaultName
    val vueProximity = this.vueProximity
    return Pointer {
      val newContext = context.dereference() ?: return@Pointer null
      val newSource = source?.let { it.dereference() ?: return@Pointer null }
      VueUnresolvedComponent(newContext, newSource, defaultName, vueProximity)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueUnresolvedComponent
    && other.defaultName == defaultName
    && other.context == context
    && other.rawSource == rawSource

  override fun hashCode(): Int {
    var result = defaultName.hashCode()
    result = 31 * result + context.hashCode()
    result = 31 * result + rawSource.hashCode()
    return result
  }
}
