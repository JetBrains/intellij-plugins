// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.html.webSymbols.PolySymbolsHtmlQueryConfigurator
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.PsiSourcedPolySymbol
import com.intellij.webSymbols.PolySymbol
import com.intellij.webSymbols.PolySymbolQualifiedKind
import com.intellij.webSymbols.PolySymbolQualifiedName
import com.intellij.webSymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.webSymbols.context.PolyContext
import com.intellij.webSymbols.query.PolySymbolsQueryResultsCustomizer
import com.intellij.webSymbols.query.WebSymbolsQueryResultsCustomizerFactory
import com.intellij.webSymbols.utils.nameSegments
import com.intellij.webSymbols.webTypes.WebTypesSymbol
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.symbols.VuePolyTypesMergedSymbol

class VuePolySymbolsQueryResultsCustomizer(private val context: PsiElement) : PolySymbolsQueryResultsCustomizer {

  private val scriptLanguage by lazy(LazyThreadSafetyMode.PUBLICATION) {
    detectVueScriptLanguage(context.containingFile)
  }

  override fun createPointer(): Pointer<out PolySymbolsQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { VuePolySymbolsQueryResultsCustomizer(it) }
    }
  }

  override fun apply(
    matches: List<PolySymbol>,
    strict: Boolean,
    qualifiedName: PolySymbolQualifiedName,
  ): List<PolySymbol> {
    if (qualifiedName.namespace != PolySymbol.NAMESPACE_HTML) return matches

    var result = matches
    if (qualifiedName.matches(VUE_COMPONENTS)) {
      if (result.size > 1) {
        val mergedSymbol = result.find { it is VuePolyTypesMergedSymbol } as? VuePolyTypesMergedSymbol
        if (mergedSymbol != null) {
          val mergedWebTypes = mergedSymbol.webTypesSymbols
          // The check can get very expensive with more web-types merged
          // Limit checks to some reasonable sizes. Usually it should be only one merged.
          if (mergedWebTypes.size < 5) {
            result = result.filter { it !is WebTypesSymbol || mergedWebTypes.none { merged -> merged.isEquivalentTo(it) } }
          }
        }
      }
      if (!strict) return result
    }
    else if (qualifiedName.matches(PolySymbol.HTML_ELEMENTS)) {
      val standardHtmlSymbols = result.filterTo(LinkedHashSet()) { symbol ->
        symbol.nameSegments.flatMap { it.symbols }.any { it is PolySymbolsHtmlQueryConfigurator.StandardHtmlSymbol }
      }
      if (standardHtmlSymbols.isEmpty()) return result
      if (isVueComponentQuery(qualifiedName)) {
        return result.filter { it !in standardHtmlSymbols }
      }
    }

    return result.filter { symbol ->
      symbol.properties[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE ||
      symbol.properties[PROP_VUE_COMPOSITION_COMPONENT] == true
    }
  }

  override fun apply(
    item: PolySymbolCodeCompletionItem,
    qualifiedKind: PolySymbolQualifiedKind,
  ): PolySymbolCodeCompletionItem {
    if (qualifiedKind == VUE_COMPONENTS) {
      val proximity = item.symbol?.properties?.get(PROP_VUE_PROXIMITY)
      val element = (item.symbol as? PsiSourcedPolySymbol)?.source
      if (proximity == VueModelVisitor.Proximity.OUT_OF_SCOPE && element != null) {
        val settings = JSApplicationSettings.getInstance()
        if ((scriptLanguage != null && "ts" == scriptLanguage)
            || (DialectDetector.isTypeScript(element)
                && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
          if (settings.hasTSImportCompletionEffective(element.project)) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, PolySymbol.Priority.LOWEST)
          }
        }
        else {
          if (settings.isUseJavaScriptAutoImport) {
            return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, PolySymbol.Priority.LOWEST)
          }
        }
      }
    }
    return item
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is VuePolySymbolsQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  class Provider : WebSymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: PolyContext): PolySymbolsQueryResultsCustomizer? =
      if (context.framework == VueFramework.ID)
        VuePolySymbolsQueryResultsCustomizer(location)
      else null

  }

}