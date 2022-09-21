// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import org.jetbrains.vuejs.index.findScriptTag
import java.util.*

/**
 * This container ensures that components from other container are not self referred without export declaration with component name or script setup
 */
class VueIncorrectlySelfReferredComponentFilteringContainer(private val delegate: WebSymbolsContainer,
                                                            private val file: PsiFile) : WebSymbolsContainer {

  override fun getSymbols(namespace: SymbolNamespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    delegate.getSymbols(namespace, kind, name, params, context)
      .filter { isNotIncorrectlySelfReferred(it) }

  override fun getCodeCompletions(namespace: SymbolNamespace?,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
    delegate.getCodeCompletions(namespace, kind, name, params, context)
      .filter { isNotIncorrectlySelfReferred(it.symbol) }

  override fun createPointer(): Pointer<out WebSymbolsContainer> {
    val delegatePtr = delegate.createPointer()
    val filePtr = file.createSmartPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val file = filePtr.dereference() ?: return@Pointer null
      VueIncorrectlySelfReferredComponentFilteringContainer(delegate, file)
    }
  }

  override fun getModificationCount(): Long =
    delegate.modificationCount

  override fun equals(other: Any?): Boolean =
    other is VueIncorrectlySelfReferredComponentFilteringContainer
    && other.delegate == delegate
    && other.file == file

  override fun hashCode(): Int =
    Objects.hash(delegate, file)

  override fun toString(): String {
    return "IncorrectlySelfReferredComponentFilteringContainer($delegate)"
  }

  // Cannot self refer without export declaration with component name or script setup
  private fun isNotIncorrectlySelfReferred(symbol: WebSymbolsContainer?) =
    symbol !is PsiSourcedWebSymbol
    || (symbol.source as? JSImplicitElement)?.context.let { context ->
      context == null
      || context != file
      || context.containingFile.asSafely<XmlFile>()?.let { findScriptTag(it, true) } != null
    }

}