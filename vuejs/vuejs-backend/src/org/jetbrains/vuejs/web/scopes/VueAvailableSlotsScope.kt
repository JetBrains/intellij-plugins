// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.utils.match
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.model.DEFAULT_SLOT_NAME
import org.jetbrains.vuejs.model.getAvailableSlots
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import org.jetbrains.vuejs.model.getMatchingAvailableSlots
import org.jetbrains.vuejs.web.VUE_AVAILABLE_SLOTS
import org.jetbrains.vuejs.web.symbols.VueSymbol

class VueAvailableSlotsScope(private val tag: XmlTag) : PolySymbolScope {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsScope
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      qualifiedName.matches(HTML_ATTRIBUTES) ->
        DefaultSlotSymbol.match(qualifiedName.name, params, stack)
      qualifiedName.matches(VUE_AVAILABLE_SLOTS) ->
        getMatchingAvailableSlots(tag, qualifiedName.name, true)
      else -> emptyList()
    }

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      kind == HTML_ATTRIBUTES ->
        listOf(DefaultSlotSymbol)
      kind == VUE_AVAILABLE_SLOTS ->
        getAvailableSlots(tag, params.expandPatterns, true)
      else -> emptyList()
    }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    if (qualifiedName.matches(VUE_AVAILABLE_SLOTS) && params.queryExecutor.allowResolve)
      getAvailableSlotsCompletions(tag, qualifiedName.name, params.position, true)
    else emptyList()

  override fun createPointer(): Pointer<VueAvailableSlotsScope> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { VueAvailableSlotsScope(it) }
    }
  }

  object DefaultSlotSymbol : PolySymbolWithPattern, VueSymbol {
    override val kind: PolySymbolKind
      get() = HTML_ATTRIBUTES

    override val name: String
      get() = "v-slot"

    override val pattern: PolySymbolPattern =
      PolySymbolPatternFactory.createSingleSymbolReferencePattern(
        listOf(VUE_AVAILABLE_SLOTS.withName(DEFAULT_SLOT_NAME))
      )

    override fun createPointer(): Pointer<out PolySymbol> {
      return Pointer.hardPointer(this)
    }
  }
}