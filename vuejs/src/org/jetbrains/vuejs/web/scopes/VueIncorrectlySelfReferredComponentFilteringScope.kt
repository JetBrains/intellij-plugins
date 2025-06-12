// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolsCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.index.findScriptTag

/**
 * This container ensures that components from other container are not self referred without export declaration with component name or script setup
 */
class VueIncorrectlySelfReferredComponentFilteringScope(
  private val delegate: PolySymbolsScope,
  private val file: PsiFile,
) : PolySymbolsScope {

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    delegate.getMatchingSymbols(qualifiedName, params, scope)
      .filter { isNotIncorrectlySelfReferred(it) }

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    delegate.getSymbols(qualifiedKind, params, scope)
      .filter { isNotIncorrectlySelfReferred(it) }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsCodeCompletionQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolCodeCompletionItem> =
    delegate.getCodeCompletions(qualifiedName, params, scope)
      .filter { isNotIncorrectlySelfReferred(it.symbol) }

  override fun createPointer(): Pointer<out PolySymbolsScope> {
    val delegatePtr = delegate.createPointer()
    val filePtr = file.createSmartPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val file = filePtr.dereference() ?: return@Pointer null
      VueIncorrectlySelfReferredComponentFilteringScope(delegate, file)
    }
  }

  override fun getModificationCount(): Long =
    delegate.modificationCount

  override fun equals(other: Any?): Boolean =
    other is VueIncorrectlySelfReferredComponentFilteringScope
    && other.delegate == delegate
    && other.file == file

  override fun hashCode(): Int =
    31 * delegate.hashCode() + file.hashCode()

  override fun toString(): String {
    return "IncorrectlySelfReferredComponentFilteringContainer($delegate)"
  }

  // Cannot self refer without export declaration with component name or script setup
  private fun isNotIncorrectlySelfReferred(symbol: PolySymbol?) =
    symbol !is PsiSourcedPolySymbol
    || (symbol.source as? JSImplicitElement)?.context.let { context ->
      context == null
      || context != file
      || context.containingFile.asSafely<XmlFile>()?.let { findScriptTag(it, true) } != null
    }

}