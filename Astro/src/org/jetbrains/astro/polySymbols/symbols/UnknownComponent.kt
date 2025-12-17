// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.lang.typescript.getNavigationFromService
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.*
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.*
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_NAMESPACES

class UnknownComponent(override val source: PsiElement, override val name: @NlsSafe String) : PolySymbolWithPattern, ComponentPolySymbol,
                                                                                              PolySymbolScope {
  override val pattern: PolySymbolPattern = PolySymbolPatternFactory.createRegExMatch(".*")

  override val kind: PolySymbolKind
    get() = UI_FRAMEWORK_COMPONENT_NAMESPACES

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> = emptyList()

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> = emptyList()

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? = when (property) {
    PolySymbol.PROP_HIDE_FROM_COMPLETION -> true as T
    PolySymbol.PROP_DOC_HIDE_PATTERN -> true as T
    else -> null
  }

  override fun getModificationCount(): Long = PsiModificationTracker.getInstance(source.project).modificationCount

  override fun createPointer(): Pointer<UnknownComponent> {
    val filePtr = source.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { UnknownComponent(it, name) }
    }
  }

  override fun computeNavigationElement(project: Project): PsiElement? {
    val offsetInSourceElement = source.text.indexOf(name)
    return getNavigationFromService(project, source, null, offsetInSourceElement)?.firstOrNull()
  }
}