// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.coalesceWith
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.entities.Angular2AliasedDirectiveProperty
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveSelectorSymbol

open class Angular2DirectiveSymbolWrapper private constructor(
  val directive: Angular2Directive,
  delegate: Angular2Symbol,
  private val forcedPriority: PolySymbol.Priority? = null,
  private val location: PsiFile,
  val isMatchedDirective: Boolean = false,
) : Angular2SymbolDelegate<Angular2Symbol>(delegate) {

  companion object {
    @JvmStatic
    fun create(
      directive: Angular2Directive,
      delegate: Angular2Symbol,
      location: PsiFile,
      isMatchedDirective: Boolean = false,
      forcedPriority: PolySymbol.Priority? = null,
    ): Angular2DirectiveSymbolWrapper =
      when (delegate) {
        is PsiSourcedPolySymbol -> Angular2PsiSourcedDirectiveSymbolWrapper(directive, delegate, forcedPriority, location, isMatchedDirective)
        else -> Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority, location, isMatchedDirective)
      }
  }

  override val required: Boolean?
    get() = isMatchedDirective && super.required == true

  override val priority: PolySymbol.Priority?
    get() = forcedPriority ?: super.priority

  override fun createPointer(): Pointer<out Angular2DirectiveSymbolWrapper> =
    createPointer(::Angular2DirectiveSymbolWrapper)

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_SYMBOL_DIRECTIVE -> property.tryCast(directive)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(
        if (delegate is Angular2DirectiveSelectorSymbol)
          PolySymbolHtmlAttributeValue.create(required = false)
        else JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(location) {
          super[PROP_HTML_ATTRIBUTE_VALUE]
        })
      else -> super.get(property)
    }

  override val apiStatus: PolySymbolApiStatus
    get() = directive.apiStatus.coalesceWith(delegate.apiStatus)

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    return this == symbol || delegate.isEquivalentTo(symbol)
  }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    super.getDocumentationTarget(location).let {
      if (it is Angular2ElementDocumentationTarget)
        it.withDirective((delegate as? Angular2AliasedDirectiveProperty)?.directive ?: directive)
      else
        it
    }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is Angular2DirectiveSymbolWrapper
    && other.directive == directive
    && other.delegate == delegate

  override fun hashCode(): Int =
    31 * directive.hashCode() + delegate.hashCode()

  protected fun <T : Angular2DirectiveSymbolWrapper> createPointer(
    create: (
      directive: Angular2Directive,
      delegate: Angular2Symbol,
      forcedPriority: PolySymbol.Priority?,
      location: PsiFile,
      isMatchedDirective: Boolean,
    ) -> T,
  ): Pointer<T> {
    val directivePtr = directive.createPointer()
    val delegatePtr = delegate.createPointer()
    val forcedPriority = this.forcedPriority
    val locationPtr = location.createSmartPointer()
    val isMatchedDirective = this.isMatchedDirective
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val location = locationPtr.dereference() ?: return@Pointer null
      create(directive, delegate, forcedPriority, location, isMatchedDirective)
    }
  }

  private class Angular2PsiSourcedDirectiveSymbolWrapper(
    directive: Angular2Directive,
    delegate: Angular2Symbol,
    forcedPriority: PolySymbol.Priority?,
    location: PsiFile,
    isMatchedDirective: Boolean,
  ) : Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority, location, isMatchedDirective), PsiSourcedPolySymbol {

    override val source: PsiElement?
      get() = (this.delegate as PsiSourcedPolySymbol).source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<Angular2DirectiveSymbolWrapper>.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<Angular2DirectiveSymbolWrapper>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<Angular2PsiSourcedDirectiveSymbolWrapper> =
      createPointer(::Angular2PsiSourcedDirectiveSymbolWrapper)

    override val psiContext: PsiElement?
      get() = super<Angular2DirectiveSymbolWrapper>.psiContext
  }

}
