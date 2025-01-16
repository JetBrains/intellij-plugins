// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.entities.Angular2AliasedDirectiveProperty
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import java.util.*

open class Angular2DirectiveSymbolWrapper private constructor(
  val directive: Angular2Directive,
  delegate: Angular2Symbol,
  private val forcedPriority: WebSymbol.Priority? = null,
  private val location: PsiFile,
) : Angular2SymbolDelegate<Angular2Symbol>(delegate) {

  companion object {
    @JvmStatic
    fun create(
      directive: Angular2Directive,
      delegate: Angular2Symbol,
      location: PsiFile,
      forcedPriority: WebSymbol.Priority? = null,
    ): Angular2DirectiveSymbolWrapper =
      when (delegate) {
        is PsiSourcedWebSymbol -> Angular2PsiSourcedDirectiveSymbolWrapper(directive, delegate, forcedPriority, location)
        else -> Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority, location)
      }
  }

  override val priority: WebSymbol.Priority?
    get() = forcedPriority ?: super.priority

  override val attributeValue: WebSymbolHtmlAttributeValue?
    get() = if (delegate is Angular2DirectiveSelectorSymbol)
      WebSymbolHtmlAttributeValue.create(required = false)
    else JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(location) {
      super.attributeValue
    }

  override fun createPointer(): Pointer<out Angular2DirectiveSymbolWrapper> =
    createPointer(::Angular2DirectiveSymbolWrapper)

  override val properties: Map<String, Any>
    get() = super.properties + Pair(PROP_SYMBOL_DIRECTIVE, directive)

  override val apiStatus: WebSymbolApiStatus
    get() = directive.apiStatus.coalesceWith(delegate.apiStatus)

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    return this == symbol || delegate.isEquivalentTo(symbol)
  }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
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
    Objects.hash(directive, delegate)

  protected fun <T : Angular2DirectiveSymbolWrapper> createPointer(
    create: (
      directive: Angular2Directive,
      delegate: Angular2Symbol,
      forcedPriority: WebSymbol.Priority?,
      location: PsiFile,
    ) -> T,
  ): Pointer<T> {
    val directivePtr = directive.createPointer()
    val delegatePtr = delegate.createPointer()
    val forcedPriority = this.forcedPriority
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val location = locationPtr.dereference() ?: return@Pointer null
      create(directive, delegate, forcedPriority, location)
    }
  }

  private class Angular2PsiSourcedDirectiveSymbolWrapper(
    directive: Angular2Directive,
    delegate: Angular2Symbol,
    forcedPriority: WebSymbol.Priority?,
    location: PsiFile,
  ) : Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority, location), PsiSourcedWebSymbol {

    override val source: PsiElement?
      get() = (this.delegate as PsiSourcedWebSymbol).source

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