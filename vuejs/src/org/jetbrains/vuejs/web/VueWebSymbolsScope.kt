// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.symbols.*
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.PROP_VUE_PROXIMITY

class VueWebSymbolsScope(private val context: PsiElement) : WebSymbolsScope {

  private val scriptLanguage by lazy(LazyThreadSafetyMode.NONE) {
    detectVueScriptLanguage(context.containingFile)
  }

  override fun createPointer(): Pointer<out WebSymbolsScope> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { VueWebSymbolsScope(it) }
    }
  }

  override fun apply(matches: List<WebSymbol>,
                     strict: Boolean,
                     namespace: WebSymbolsContainer.Namespace?,
                     kind: SymbolKind,
                     name: String?): List<WebSymbol> =
    matches.filter { symbol ->
      if (namespace == WebSymbolsContainer.Namespace.HTML
          && kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_COMPONENTS)
        symbol.properties[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE
      else true
    }

  override fun apply(item: WebSymbolCodeCompletionItem,
                     namespace: WebSymbolsContainer.Namespace?,
                     kind: SymbolKind): WebSymbolCodeCompletionItem? {
    if (namespace == WebSymbolsContainer.Namespace.HTML
        && kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_COMPONENTS) {
      val proximity = item.symbol?.properties?.get(PROP_VUE_PROXIMITY)
      val element = (item.symbol as? PsiSourcedWebSymbol)?.source
      if (proximity == VueModelVisitor.Proximity.OUT_OF_SCOPE && element != null) {
        val settings = JSApplicationSettings.getInstance()
        if ((scriptLanguage != null && "ts" == scriptLanguage)
            || (DialectDetector.isTypeScript(element)
                && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
          if (settings.hasTSImportCompletionEffective(element.project)) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, WebSymbol.Priority.LOWEST)
          }
        }
        else {
          if (settings.isUseJavaScriptAutoImport) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, WebSymbol.Priority.LOWEST)
          }
        }
      }
    }
    return item
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is VueWebSymbolsScope
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  class Provider : WebSymbolsScopeProvider {

    override fun get(context: PsiElement, framework: FrameworkId?): WebSymbolsScope? =
      framework
        ?.takeIf { it == VueFramework.ID }
        ?.let { VueWebSymbolsScope(context) }

  }


}