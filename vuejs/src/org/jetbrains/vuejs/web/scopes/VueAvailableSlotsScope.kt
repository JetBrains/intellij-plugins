// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.getAvailableSlots
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator

class VueAvailableSlotsScope(private val tag: XmlTag) : WebSymbolsScope {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsScope
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (namespace == WebSymbol.NAMESPACE_HTML
        && kind == VueWebSymbolsQueryConfigurator.KIND_VUE_AVAILABLE_SLOTS
        && params.queryExecutor.allowResolve)
      getAvailableSlots(tag, name, true)
    else emptyList()

  override fun getCodeCompletions(namespace: SymbolNamespace,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    if (namespace == WebSymbol.NAMESPACE_HTML
        && kind == VueWebSymbolsQueryConfigurator.KIND_VUE_AVAILABLE_SLOTS
        && params.queryExecutor.allowResolve)
      getAvailableSlotsCompletions(tag, name, params.position, true)
    else emptyList()

  override fun createPointer(): Pointer<VueAvailableSlotsScope> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { VueAvailableSlotsScope(it) }
    }
  }
}