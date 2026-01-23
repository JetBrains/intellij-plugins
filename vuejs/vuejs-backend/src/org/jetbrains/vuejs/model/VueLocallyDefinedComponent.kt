// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier

sealed class VueLocallyDefinedComponent<T : PsiElement> protected constructor(
  override val name: String,
  override val delegate: VueComponent,
  val sourceElement: T,
  override val vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.LOCAL,
) : VueDelegatedContainer<VueComponent>(), VueComponent {

  companion object {
    fun create(delegate: VueComponent, source: JSLiteralExpression): VueLocallyDefinedComponent<*>? =
      getTextIfLiteral(source)?.let { VueStringLiteralLocallyDefinedComponent(it, delegate, source) }

    fun create(delegate: VueComponent, source: JSPsiNamedElementBase): VueLocallyDefinedComponent<*>? =
      source.name?.let { VuePsiNamedElementLocallyDefinedComponent(it, delegate, source) }

  }

  override val defaultName: String
    get() = name

  override val source: PsiElement
    get() = sourceElement

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(
      this, location,
      PolySymbolDocumentationProvider<VueLocallyDefinedComponent<*>> { symbol, location ->
        val myDoc = PolySymbolDocumentation.create(symbol, location) {
          library = getLibraryNameForDocumentationOf(symbol.source)
        }
        symbol.delegate
          .getDocumentationTarget(location)
          .asSafely<PolySymbolDocumentationTarget>()
          ?.documentation
          ?.withLibrary(myDoc.library)
          ?.withName(myDoc.name)
          ?.withDefinition(myDoc.definition)
          ?.withIcon(myDoc.icon)
        ?: myDoc
      })

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = delegate.typeParameters

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueLocallyDefinedComponent<*>
    && other.javaClass == javaClass
    && other.name == name
    && other.delegate == delegate
    && other.sourceElement == sourceElement

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + delegate.hashCode()
    result = 31 * result + sourceElement.hashCode()
    return result
  }

  protected fun <C : VueLocallyDefinedComponent<T>> createPointer(constructor: (String, VueComponent, T, VueModelVisitor.Proximity) -> C): Pointer<C> {
    val name = this.name
    val vueProximity = this.vueProximity
    val delegate = this.delegate.createPointer()
    val sourceElement = this.sourceElement.createSmartPointer()
    return Pointer {
      val newDelegate = delegate.dereference() ?: return@Pointer null
      val newSourceElement = sourceElement.dereference() ?: return@Pointer null
      constructor(name, newDelegate, newSourceElement, vueProximity)
    }
  }

  abstract override fun createPointer(): Pointer<out VueLocallyDefinedComponent<T>>
}

private class VuePsiNamedElementLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  sourceElement: JSPsiNamedElementBase,
  vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.LOCAL,
) : VueLocallyDefinedComponent<JSPsiNamedElementBase>(name, delegate, sourceElement, vueProximity),
    PsiSourcedPolySymbol {

  override val nameElement: PsiElement
    get() = componentSource

  override val componentSource: PsiElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
    sourceElement.resolveIfImportSpecifier()
  }

  override val rawSource: PsiElement
    get() = sourceElement

  override fun withNameAndProximity(name: String, proximity: VueModelVisitor.Proximity): VueComponent =
    VuePsiNamedElementLocallyDefinedComponent(name, delegate, sourceElement, proximity)

  override fun createPointer(): Pointer<VuePsiNamedElementLocallyDefinedComponent> =
    createPointer(::VuePsiNamedElementLocallyDefinedComponent)
}

private class VueStringLiteralLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  sourceElement: JSLiteralExpression,
  vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.LOCAL,
) : VueLocallyDefinedComponent<JSLiteralExpression>(name, delegate, sourceElement, vueProximity),
    PolySymbolDeclaredInPsi {

  override val textRangeInSourceElement: TextRange
    get() = TextRange(1, sourceElement.textRange.length - 1)

  override val psiContext: PsiElement?
    get() = super<PolySymbolDeclaredInPsi>.psiContext

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<PolySymbolDeclaredInPsi>.getNavigationTargets(project)

  override val nameElement: PsiElement
    get() = sourceElement

  override val componentSource: PsiElement
    get() = delegate.componentSource ?: sourceElement

  override val rawSource: PsiElement
    get() = delegate.rawSource ?: sourceElement

  override val renameTarget: PolySymbolRenameTarget?
    get() = PolySymbolRenameTarget.create(this)

  override fun withNameAndProximity(name: String, proximity: VueModelVisitor.Proximity): VueComponent =
    VueStringLiteralLocallyDefinedComponent(name, delegate, sourceElement, proximity)

  override fun createPointer(): Pointer<VueStringLiteralLocallyDefinedComponent> =
    createPointer(::VueStringLiteralLocallyDefinedComponent)
}