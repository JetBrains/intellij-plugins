// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.entities.Angular2Directive

open class Angular2StructuralDirectiveSymbol private constructor(
  val directive: Angular2Directive,
  sourceSymbol: Angular2Symbol,
  private val hasInputsToBind: Boolean,
  private val location: PsiFile,
) :
  Angular2SymbolDelegate<Angular2Symbol>(sourceSymbol) {

  companion object {
    @JvmStatic
    fun create(
      directive: Angular2Directive,
      sourceSymbol: Angular2Symbol,
      hasInputsToBind: Boolean,
      location: PsiFile,
    ): Angular2StructuralDirectiveSymbol =
      when (sourceSymbol) {
        is PsiSourcedWebSymbol ->
          Angular2PsiSourcedStructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind, location)
        else -> Angular2StructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind, location)
      }
  }

  override val attributeValue: WebSymbolHtmlAttributeValue?
    get() = if (!hasInputsToBind)
      WebSymbolHtmlAttributeValue.create(required = false)
    else JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(location) {
      super.attributeValue
    }

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.HIGH

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = NG_STRUCTURAL_DIRECTIVES

  override val properties: Map<String, Any>
    get() = super.properties + Pair(PROP_SYMBOL_DIRECTIVE, directive)

  override val apiStatus: WebSymbolApiStatus
    get() = directive.apiStatus.coalesceWith(delegate.apiStatus)

  override fun createPointer(): Pointer<out Angular2StructuralDirectiveSymbol> =
    createPointer(::Angular2StructuralDirectiveSymbol)

  protected fun <T : Angular2StructuralDirectiveSymbol> createPointer(
    create: (
      directive: Angular2Directive,
      sourceSymbol: Angular2Symbol,
      hasInputsToBind: Boolean,
      location: PsiFile,
    ) -> T,
  ): Pointer<T> {
    val directivePtr = directive.createPointer()
    val selectorPtr = delegate.createPointer()
    val hasInputsToBind = this.hasInputsToBind
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val selector = selectorPtr.dereference() ?: return@Pointer null
      val location = locationPtr.dereference() ?: return@Pointer null
      create(directive, selector, hasInputsToBind, location)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is Angular2StructuralDirectiveSymbol
    && other.delegate == delegate

  override fun hashCode(): Int =
    delegate.hashCode()

  private class Angular2PsiSourcedStructuralDirectiveSymbol(
    directive: Angular2Directive,
    sourceSymbol: Angular2Symbol,
    hasInputsToBind: Boolean,
    location: PsiFile,
  )
    : Angular2StructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind, location), PsiSourcedWebSymbol {

    override val source: PsiElement?
      get() = (delegate as PsiSourcedWebSymbol).source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<Angular2StructuralDirectiveSymbol>.getNavigationTargets(project)

    override val psiContext: PsiElement?
      get() = super<Angular2StructuralDirectiveSymbol>.psiContext

    override fun createPointer(): Pointer<Angular2PsiSourcedStructuralDirectiveSymbol> =
      createPointer(::Angular2PsiSourcedStructuralDirectiveSymbol)
  }
}