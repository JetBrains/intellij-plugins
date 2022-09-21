// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.model.Pointer
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import org.jetbrains.vuejs.model.getAvailableSlots
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider

class VueAvailableSlotsContainer(private val tag: XmlTag) : WebSymbolsContainer {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsContainer
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getSymbols(namespace: SymbolNamespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if ((namespace == null || namespace == WebSymbolsContainer.NAMESPACE_HTML)
        && kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_AVAILABLE_SLOTS
        && params.registry.allowResolve)
      getAvailableSlots(tag, name, true)
    else emptyList()

  override fun getCodeCompletions(namespace: SymbolNamespace?,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
    if ((namespace == null || namespace == WebSymbolsContainer.NAMESPACE_HTML)
        && kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_AVAILABLE_SLOTS
        && params.registry.allowResolve)
      getAvailableSlotsCompletions(tag, name, params.position, true)
    else emptyList()

  override fun createPointer(): Pointer<VueAvailableSlotsContainer> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { VueAvailableSlotsContainer(it) }
    }
  }
}