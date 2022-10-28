// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.symbol.RenameableSymbol
import com.intellij.webSymbols.*
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.PROP_SYMBOL_DIRECTIVE
import java.util.*

open class Angular2DirectiveSymbolWrapper private constructor(private val directive: Angular2Directive,
                                                              delegate: Angular2Symbol,
                                                              private val forcedPriority: WebSymbol.Priority? = null)
  : Angular2SymbolDelegate<Angular2Symbol>(delegate) {

  companion object {
    @JvmStatic
    fun create(directive: Angular2Directive,
               delegate: Angular2Symbol,
               forcedPriority: WebSymbol.Priority? = null): Angular2DirectiveSymbolWrapper =
      when (delegate) {
        is PsiSourcedWebSymbol ->
          object : Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority), PsiSourcedWebSymbol {

            override val source: PsiElement?
              get() = (this.delegate as PsiSourcedWebSymbol).source

            override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
              super<Angular2DirectiveSymbolWrapper>.getNavigationTargets(project)

            override fun isEquivalentTo(symbol: Symbol): Boolean =
              super<Angular2DirectiveSymbolWrapper>.isEquivalentTo(symbol)

            override val psiContext: PsiElement?
              get() = super<Angular2DirectiveSymbolWrapper>.psiContext
          }
        is RenameableSymbol, is RenameTarget ->
          object : Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority), RenameableSymbol {
            override val renameTarget: RenameTarget
              get() = renameTargetFromDelegate()
          }
        else -> Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority)
      }
  }

  override val priority: WebSymbol.Priority?
    get() = forcedPriority ?: super.priority

  override val attributeValue: WebSymbolHtmlAttributeValue?
    get() = if (delegate is Angular2DirectiveSelectorSymbol) {
      WebSymbolHtmlAttributeValue.create(required = false)
    }
    else super.attributeValue

  override val namespace: SymbolNamespace
    get() = delegate.namespace

  override val kind: SymbolKind
    get() = delegate.kind

  override val properties: Map<String, Any>
    get() = super.properties + Pair(PROP_SYMBOL_DIRECTIVE, directive)

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    return this == symbol || delegate.isEquivalentTo(symbol)
  }

  override fun createPointer(): Pointer<Angular2DirectiveSymbolWrapper> {
    val directivePtr = directive.createPointer()
    val delegatePtr = delegate.createPointer()
    val forcedPriority = this.forcedPriority
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      create(directive, delegate, forcedPriority)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is Angular2DirectiveSymbolWrapper
    && other.directive == directive
    && other.delegate == delegate

  override fun hashCode(): Int =
    Objects.hash(directive, delegate)

}