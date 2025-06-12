// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolsCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.utils.match
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.model.DEFAULT_SLOT_NAME
import org.jetbrains.vuejs.model.getAvailableSlots
import org.jetbrains.vuejs.model.getAvailableSlotsCompletions
import org.jetbrains.vuejs.model.getMatchingAvailableSlots
import org.jetbrains.vuejs.web.VUE_AVAILABLE_SLOTS
import org.jetbrains.vuejs.web.VueFramework

class VueAvailableSlotsScope(private val tag: XmlTag) : PolySymbolsScope {

  override fun hashCode(): Int = tag.hashCode()

  override fun equals(other: Any?): Boolean =
    other is VueAvailableSlotsScope
    && other.tag == tag

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      qualifiedName.matches(HTML_ATTRIBUTES) ->
        DefaultSlotSymbol.match(qualifiedName.name, params, scope)
      qualifiedName.matches(VUE_AVAILABLE_SLOTS) ->
        getMatchingAvailableSlots(tag, qualifiedName.name, true)
      else -> emptyList()
    }

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    when {
      !params.queryExecutor.allowResolve -> emptyList()
      qualifiedKind == HTML_ATTRIBUTES ->
        listOf(DefaultSlotSymbol)
      qualifiedKind == VUE_AVAILABLE_SLOTS ->
        getAvailableSlots(tag, params.expandPatterns, true)
      else -> emptyList()
    }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsCodeCompletionQueryParams,
    scope: Stack<PolySymbolsScope>,
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

  object DefaultSlotSymbol : PolySymbol {
    override val qualifiedKind: PolySymbolQualifiedKind
      get() = HTML_ATTRIBUTES

    override val name: String
      get() = "v-slot"

    override val origin: PolySymbolOrigin = object : PolySymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

    override val pattern: PolySymbolsPattern =
      PolySymbolsPatternFactory.createSingleSymbolReferencePattern(
        listOf(VUE_AVAILABLE_SLOTS.withName(DEFAULT_SLOT_NAME))
      )

    override fun createPointer(): Pointer<out PolySymbol> {
      return Pointer.hardPointer(this)
    }
  }
}