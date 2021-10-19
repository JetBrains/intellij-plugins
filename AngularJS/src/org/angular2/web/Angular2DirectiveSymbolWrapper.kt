// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolHtmlAttributeValueData
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.model.Pointer
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.PROP_SYMBOL_DIRECTIVE
import java.util.*

class Angular2DirectiveSymbolWrapper(private val directive: Angular2Directive,
                                     delegate: Angular2Symbol,
                                     private val forcedPriority: WebSymbol.Priority? = null)
  : Angular2SymbolDelegate<Angular2Symbol>(delegate) {

  override fun createPointer(): Pointer<out Angular2SymbolDelegate<Angular2Symbol>> {
    val directivePtr = directive.createPointer()
    val delegatePtr = delegate.createPointer()
    val forcedPriority = this.forcedPriority
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      Angular2DirectiveSymbolWrapper(directive, delegate, forcedPriority)
    }
  }

  override val priority: WebSymbol.Priority?
    get() = forcedPriority ?: super.priority

  override val attributeValue: WebSymbol.AttributeValue?
    get() = if (delegate is Angular2DirectiveSelectorSymbol) {
      WebSymbolHtmlAttributeValueData(required = false)
    }
    else super.attributeValue

  override val namespace: WebSymbolsContainer.Namespace
    get() = delegate.namespace

  override val kind: SymbolKind
    get() = delegate.kind

  override val properties: Map<String, Any>
    get() = super.properties + Pair(PROP_SYMBOL_DIRECTIVE, directive)

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is Angular2DirectiveSymbolWrapper
    && other.directive == directive
    && other.delegate == delegate

  override fun hashCode(): Int =
    Objects.hash(directive, delegate)

}