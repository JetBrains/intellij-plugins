// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.getAvailableSlots
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import org.jetbrains.vuejs.model.getMatchingAvailableSlots
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator

class VueAvailableSlotsScope(private val tag: XmlTag) : WebSymbolsScope {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsScope
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.matches(WebSymbol.NAMESPACE_HTML, VueWebSymbolsQueryConfigurator.KIND_VUE_AVAILABLE_SLOTS)
        && params.queryExecutor.allowResolve)
      getMatchingAvailableSlots(tag, qualifiedName.name, true)
    else emptyList()

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (qualifiedKind.matches(WebSymbol.NAMESPACE_HTML, VueWebSymbolsQueryConfigurator.KIND_VUE_AVAILABLE_SLOTS)
        && params.queryExecutor.allowResolve)
      getAvailableSlots(tag, params.expandPatterns, true)
    else emptyList()

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    if (qualifiedName.matches(WebSymbol.NAMESPACE_HTML, VueWebSymbolsQueryConfigurator.KIND_VUE_AVAILABLE_SLOTS)
        && params.queryExecutor.allowResolve)
      getAvailableSlotsCompletions(tag, qualifiedName.name, params.position, true)
    else emptyList()

  override fun createPointer(): Pointer<VueAvailableSlotsScope> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { VueAvailableSlotsScope(it) }
    }
  }
}